package app.bean.focus;
import android.content.Context;import android.os.SystemClock;import android.provider.Settings;
/** Off takes a 10-minute wait and a 5-minute confirmation window; on is immediate. */
public class SwitchOffDelayTest {
 static int n;static void ok(boolean b){n++;if(!b)throw new AssertionError("case "+n);}
 static boolean on(Context c,String key){return Prefs.get(c).getBoolean(key,true);}
 public static void main(String[] args){
  long d=SwitchOffDelay.DELAY,w=SwitchOffDelay.WINDOW;
  // Pure timeline: none, waiting, ready, expired; reboot and a clock that went back cancel it.
  ok(SwitchOffDelay.state(-1,1,5,1)==SwitchOffDelay.NONE);ok(SwitchOffDelay.state(100,1,100+d-1,1)==SwitchOffDelay.WAITING);ok(SwitchOffDelay.left(100,1,100,1)==d);
  ok(SwitchOffDelay.state(100,1,100+d,1)==SwitchOffDelay.READY);ok(SwitchOffDelay.left(100,1,100+d,1)==w);ok(SwitchOffDelay.state(100,1,100+d+w-1,1)==SwitchOffDelay.READY);
  ok(SwitchOffDelay.state(100,1,100+d+w,1)==SwitchOffDelay.NONE);ok(SwitchOffDelay.state(100,1,100+d,2)==SwitchOffDelay.NONE);ok(SwitchOffDelay.state(100,1,99,1)==SwitchOffDelay.NONE);
  Context c=new Context();String k=SwitchOffDelay.SHORTS;
  // Off is refused until the wait ends; a second request does not restart the timer.
  ok(!SwitchOffDelay.turnOff(c,k));ok(on(c,k));ok(SwitchOffDelay.request(c,k)==SwitchOffDelay.WAITING);
  SystemClock.now+=d/2;ok(SwitchOffDelay.request(c,k)==SwitchOffDelay.WAITING);ok(SwitchOffDelay.left(c,k)==d/2);ok(!SwitchOffDelay.turnOff(c,k));ok(on(c,k));
  SystemClock.now+=d/2;ok(SwitchOffDelay.state(c,k)==SwitchOffDelay.READY);ok(SwitchOffDelay.turnOff(c,k));ok(!on(c,k));ok(SwitchOffDelay.state(c,k)==SwitchOffDelay.NONE);
  // On is immediate and clears any wait; the other switch has its own wait.
  SwitchOffDelay.turnOn(c,k);ok(on(c,k));ok(SwitchOffDelay.state(c,SwitchOffDelay.ENABLED)==SwitchOffDelay.NONE);
  SwitchOffDelay.request(c,k);SystemClock.now+=d;SwitchOffDelay.turnOn(c,k);ok(SwitchOffDelay.state(c,k)==SwitchOffDelay.NONE);ok(!SwitchOffDelay.turnOff(c,k));
  // A missed confirmation window means waiting again.
  SwitchOffDelay.request(c,k);SystemClock.now+=d+w;ok(SwitchOffDelay.state(c,k)==SwitchOffDelay.NONE);ok(!SwitchOffDelay.turnOff(c,k));ok(on(c,k));
  // A reboot cancels the request; cancel clears it.
  SwitchOffDelay.request(c,k);SystemClock.now+=d;Settings.Global.boot++;ok(SwitchOffDelay.state(c,k)==SwitchOffDelay.NONE);ok(!SwitchOffDelay.turnOff(c,k));
  SwitchOffDelay.request(c,k);SwitchOffDelay.cancel(c,k);ok(SwitchOffDelay.state(c,k)==SwitchOffDelay.NONE);
  // Starting a focus lock clears waits, and nothing turns off during a lock.
  SwitchOffDelay.request(c,k);SystemClock.now+=d;ok(SwitchOffDelay.state(c,k)==SwitchOffDelay.READY);ok(FocusLock.start(c,30));ok(SwitchOffDelay.state(c,k)==SwitchOffDelay.NONE);
  SwitchOffDelay.request(c,k);SystemClock.now+=d;ok(SwitchOffDelay.state(c,k)==SwitchOffDelay.READY);ok(!SwitchOffDelay.turnOff(c,k));ok(on(c,k));
  System.out.println("Switch off delay: "+n+" checks passed (Android API fakes)");
 }
}
