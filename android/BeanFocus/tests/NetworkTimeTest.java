package app.bean.focus;
import android.os.*;import android.content.Context;import android.provider.Settings;
public class NetworkTimeTest {
 static int n;static void ok(boolean b){n++;if(!b)throw new AssertionError("case "+n);}
 public static void main(String[] a){
  ok(TimerRules.remaining(10000,90000,20000,10000,1,1,6000,0,15000)==4000); // same boot always monotonic
  ok(TimerRules.remaining(10000,90000,20000,10000,1,2,0,0,15000)==5000); // reboot uses network over altered wall
  ok(TimerRules.remaining(10000,90000,20000,10000,1,2,0,89000,-1)==1000);
  ok(TimerRules.remaining(10000,90000,-1,10000,1,2,0,89000,15000)==1000);
  ok(TimerRules.remaining(10000,90000,20000,10000,1,2,0,0,21000)==0);
  ok(NetworkTime.now()==-1);SystemClock.network=100000;ok(NetworkTime.now()==100000);Build.VERSION.SDK_INT=32;ok(NetworkTime.now()==-1);Build.VERSION.SDK_INT=35;
  Context c=new Context();FocusLock.start(c,25);ok(Prefs.get(c).getLong("lock_net",-1)==1600000);FocusLock.request(c);ok(Prefs.get(c).getLong("delay_net",-1)==700000);
  Settings.Global.boot++;SystemClock.network=400000;ok(FocusLock.remaining(c)==1200000);ok(FocusLock.waitRemaining(c)==300000);
  SystemClock.network=-1;ok(FocusLock.remaining(c)>0);
  ok(BlockPolicy.otherProfile(true,true,true,false));ok(!BlockPolicy.otherProfile(true,true,true,true));ok(!BlockPolicy.otherProfile(false,true,true,false));ok(!BlockPolicy.otherProfile(true,false,true,false));
  System.out.println("Network timer and profile policy: "+n+" checks passed");
 }
}
