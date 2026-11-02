package app.bean.focus;
import android.content.Context;import android.os.SystemClock;import android.provider.Settings;
public class RemovalAccessTest {
 static int n;static void ok(boolean b){n++;if(!b)throw new AssertionError("case "+n);}
 public static void main(String[] args){
  Context c=new Context();ok(!RemovalAccess.request(c));FocusLock.start(c,120);ok(RemovalAccess.request(c));ok(!RemovalAccess.grant(c));
  SystemClock.now+=300000;ok(RemovalAccess.remaining(c)==300000);ok(!RemovalAccess.request(c));ok(RemovalAccess.remaining(c)==300000); // no timer restart
  c=new Context();ok(RemovalAccess.requested(c));SystemClock.now+=299999;ok(!RemovalAccess.ready(c));SystemClock.now++;ok(RemovalAccess.ready(c));
  ok(RemovalAccess.grant(c));ok(RemovalAccess.allowed(c));ok(FocusLock.active(c));ok(!FocusLock.ready(c));ok(!RemovalAccess.requested(c)); // no global focus unlock
  SystemClock.now+=299999;ok(RemovalAccess.allowed(c));SystemClock.now++;ok(!RemovalAccess.allowed(c));ok(!RemovalAccess.ready(c));ok(RemovalAccess.request(c));
  Settings.Global.boot++;ok(!RemovalAccess.requested(c));ok(!RemovalAccess.allowed(c));ok(RemovalAccess.request(c)); // reboot resets the wait
  SystemClock.now+=600000;ok(RemovalAccess.grant(c));RemovalAccess.cancel(c);ok(!RemovalAccess.allowed(c));ok(RemovalAccess.request(c));
  SystemClock.now+=600000;ok(RemovalAccess.grant(c));Prefs.get(c).edit().putLong("lock_start_elapsed",SystemClock.now).commit();ok(!RemovalAccess.allowed(c)); // new lock scope cannot inherit
  System.out.println("Removal delay and permission window: "+n+" checks passed (Android API fakes)");
 }
}
