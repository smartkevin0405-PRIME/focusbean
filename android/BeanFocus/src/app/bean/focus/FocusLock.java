package app.bean.focus;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.provider.Settings;
final class FocusLock {
 static final long DELAY=10*60*1000L;
 private static int boot(Context c){return Settings.Global.getInt(c.getContentResolver(),Settings.Global.BOOT_COUNT,-1);}
 private static long left(Context c,String prefix){SharedPreferences p=Prefs.get(c);int current=boot(c),saved=p.getInt(prefix+"_boot",-1);long net=saved>=0&&saved==current?-1:NetworkTime.now();return TimerRules.remaining(p.getLong(prefix+"_elapsed",0),p.getLong(prefix+"_wall",0),p.getLong(prefix+"_net",-1),p.getLong(prefix+"_duration",0),saved,current,SystemClock.elapsedRealtime(),System.currentTimeMillis(),net);}

 static long remaining(Context c){return left(c,"lock");}
 static boolean active(Context c){return remaining(c)>0;}
 static boolean requested(Context c){return active(c)&&Prefs.get(c).getLong("delay_duration",0)>0;}
 static long waitRemaining(Context c){return requested(c)?left(c,"delay"):DELAY;}
 static boolean ready(Context c){return requested(c)&&waitRemaining(c)==0;}
 private static void deadline(Context c,SharedPreferences.Editor e,String prefix,long duration){long net=NetworkTime.now();e.putLong(prefix+"_net",net<0?-1:net+duration);e.putLong(prefix+"_elapsed",SystemClock.elapsedRealtime()+duration).putLong(prefix+"_wall",System.currentTimeMillis()+duration).putLong(prefix+"_duration",duration).putInt(prefix+"_boot",boot(c));}
 static boolean start(Context c,int minutes){if(active(c)||minutes<1||minutes>180)return false;SharedPreferences.Editor e=Prefs.get(c).edit();deadline(c,e,"lock",minutes*60000L);e.putLong("lock_start_wall",System.currentTimeMillis()).putLong("lock_start_elapsed",SystemClock.elapsedRealtime());String id=java.util.UUID.randomUUID().toString();e.putString("lock_id",id).putString("lock_new_pkgs_id",id).putStringSet("lock_new_pkgs",new java.util.HashSet<>());e.putLong("delay_duration",0).putBoolean("enabled",true).putLong("enabled_off_started",-1).putLong("shorts_off_started",-1);e.apply();return true;}
 static void request(Context c){if(!active(c)||requested(c))return;SharedPreferences.Editor e=Prefs.get(c).edit();deadline(c,e,"delay",DELAY);e.apply();}
 static void cancel(Context c){Prefs.get(c).edit().putLong("delay_duration",0).apply();}
 static boolean release(Context c){if(active(c)&&!ready(c))return false;Prefs.get(c).edit().putLong("lock_duration",0).putLong("delay_duration",0).apply();return true;}
 static String format(long ms){long seconds=(ms+999)/1000;return String.format(java.util.Locale.ROOT,"%02d:%02d",seconds/60,seconds%60);}
}
