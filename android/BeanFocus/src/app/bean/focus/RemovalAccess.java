package app.bean.focus;
import android.content.*;
import android.os.SystemClock;
import android.provider.Settings;
/** Separate, scoped permission to reach this app's uninstall/management screens; does not unlock focus. */
final class RemovalAccess {
 static final long DELAY=600000L,WINDOW=300000L;
 private static int boot(Context c){return Settings.Global.getInt(c.getContentResolver(),Settings.Global.BOOT_COUNT,-1);}
 private static boolean scope(Context c){SharedPreferences p=Prefs.get(c);return FocusLock.active(c)&&p.getLong("remove_lock_start",-1)==p.getLong("lock_start_elapsed",0)&&p.getLong("remove_lock",-1)==p.getLong("lock_elapsed",0)&&p.getInt("remove_lock_boot",-2)==p.getInt("lock_boot",-1)&&p.getInt("remove_boot",-2)==boot(c);}
 static boolean requested(Context c){return scope(c)&&Prefs.get(c).getLong("remove_started",-1)>=0;}
 static long remaining(Context c){if(!requested(c))return DELAY;return Math.max(0,DELAY-Math.max(0,SystemClock.elapsedRealtime()-Prefs.get(c).getLong("remove_started",0)));}
 static boolean ready(Context c){return requested(c)&&remaining(c)==0;}
 static long allowedRemaining(Context c){if(!scope(c))return 0;long start=Prefs.get(c).getLong("remove_granted",-1),now=SystemClock.elapsedRealtime();return start<0||now<start?0:Math.max(0,WINDOW-(now-start));}
 static boolean allowed(Context c){return allowedRemaining(c)>0;}
 static boolean request(Context c){if(!FocusLock.active(c)||requested(c)||allowed(c))return false;SharedPreferences p=Prefs.get(c);p.edit().putLong("remove_lock_start",p.getLong("lock_start_elapsed",0)).putLong("remove_lock",p.getLong("lock_elapsed",0)).putInt("remove_lock_boot",p.getInt("lock_boot",-1)).putInt("remove_boot",boot(c)).putLong("remove_started",SystemClock.elapsedRealtime()).putLong("remove_granted",-1).apply();return true;}
 static boolean grant(Context c){if(!ready(c))return false;Prefs.get(c).edit().putLong("remove_started",-1).putLong("remove_granted",SystemClock.elapsedRealtime()).apply();return true;}
 static void cancel(Context c){Prefs.get(c).edit().putLong("remove_started",-1).putLong("remove_granted",-1).apply();}
}
