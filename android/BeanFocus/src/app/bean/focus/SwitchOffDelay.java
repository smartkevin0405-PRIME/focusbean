package app.bean.focus;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.provider.Settings;

/**
 * Turning "차단 켜기" or "유튜브 쇼츠 차단" off outside a focus lock takes a 10-minute wait,
 * then a 5-minute window to confirm. Turning a switch on is immediate and cancels the wait.
 * A reboot cancels the request, as with the removal wait.
 */
final class SwitchOffDelay {
 static final long DELAY=10*60*1000L,WINDOW=5*60*1000L;
 static final int NONE=0,WAITING=1,READY=2;
 static final String ENABLED="enabled",SHORTS="shorts";
 private SwitchOffDelay(){}
 private static int boot(Context c){return Settings.Global.getInt(c.getContentResolver(),Settings.Global.BOOT_COUNT,-1);}

 static int state(long started,int startedBoot,long now,int boot){
  if(started<0||startedBoot!=boot||now<started)return NONE;
  long age=now-started;return age<DELAY?WAITING:age<DELAY+WINDOW?READY:NONE;
 }
 /** Time left to wait (WAITING) or to confirm (READY); 0 otherwise. */
 static long left(long started,int startedBoot,long now,int boot){
  int s=state(started,startedBoot,now,boot);long age=now-started;
  return s==WAITING?DELAY-age:s==READY?DELAY+WINDOW-age:0;
 }
 static int state(Context c,String key){SharedPreferences p=Prefs.get(c);return state(p.getLong(key+"_off_started",-1),p.getInt(key+"_off_boot",-2),SystemClock.elapsedRealtime(),boot(c));}
 static long left(Context c,String key){SharedPreferences p=Prefs.get(c);return left(p.getLong(key+"_off_started",-1),p.getInt(key+"_off_boot",-2),SystemClock.elapsedRealtime(),boot(c));}
 /** Starts the wait unless one is already running or ready; never restarts a running timer. */
 static int request(Context c,String key){
  int now=state(c,key);if(now!=NONE)return now;
  Prefs.get(c).edit().putLong(key+"_off_started",SystemClock.elapsedRealtime()).putInt(key+"_off_boot",boot(c)).apply();return WAITING;
 }
 static void cancel(Context c,String key){Prefs.get(c).edit().putLong(key+"_off_started",-1).apply();}
 /** Turns the switch off only inside the confirmation window and never during a focus lock. */
 static boolean turnOff(Context c,String key){
  if(FocusLock.active(c)||state(c,key)!=READY)return false;
  Prefs.get(c).edit().putBoolean(key,false).putLong(key+"_off_started",-1).apply();return true;
 }
 static void turnOn(Context c,String key){Prefs.get(c).edit().putBoolean(key,true).putLong(key+"_off_started",-1).apply();}
}
