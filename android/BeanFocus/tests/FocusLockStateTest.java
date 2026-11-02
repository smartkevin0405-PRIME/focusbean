package app.bean.focus;
import android.content.Context;
import android.os.SystemClock;
public class FocusLockStateTest {
 static int n;
 static void check(boolean ok,String message){n++;if(!ok)throw new AssertionError(message);}
 public static void main(String[] args){
  Context c=new Context();
  check(!FocusLock.active(c),"initial unlocked");
  check(FocusLock.start(c,50),"start session");
  check(Prefs.get(c).getBoolean("enabled",false),"starting forces blocker on");
  check(!FocusLock.start(c,25),"cannot shorten active lock");
  check(!FocusLock.release(c),"cannot unlock without waiting");
  FocusLock.request(c);SystemClock.now+=300000;
  check(!FocusLock.ready(c),"not ready after five minutes");
  FocusLock.request(c);
  check(FocusLock.waitRemaining(c)==300000,"second request does not reset delay");
  c=new Context();check(FocusLock.requested(c),"request not stored in activity context");
  FocusLock.cancel(c);check(!FocusLock.requested(c),"cancel removes request");
  FocusLock.request(c);check(FocusLock.waitRemaining(c)==600000,"new request restarts ten minutes");
  SystemClock.now+=599999;check(!FocusLock.ready(c),"cannot unlock one millisecond early");
  SystemClock.now++;check(FocusLock.ready(c),"ready after exact ten minutes");
  check(FocusLock.release(c)&&!FocusLock.active(c),"release clears lock");
  check(Prefs.get(c).getBoolean("enabled",false),"unlock does not turn off blocker");
  check(FocusLock.start(c,25),"start second session");SystemClock.now+=25*60000;
  check(!FocusLock.active(c),"focus session expires naturally");
  check(!FocusLock.requested(c),"expired session has no active release request");
  check(!FocusLock.start(c,0),"zero minutes rejected");check(!FocusLock.start(c,181),"over 180 minutes rejected");check(FocusLock.start(c,37),"custom 37 minutes accepted");check(FocusLock.remaining(c)==37*60000L,"custom duration is in minutes");
  System.out.println("Focus lock state: "+n+" checks passed (in-memory Android API fakes)");
 }
}
