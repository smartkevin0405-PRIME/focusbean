package app.bean.focus;
import android.content.Context;
import android.os.SystemClock;
public class GrowthTrackerTest {
 static int n;
 static void eq(long expected,long actual){n++;if(expected!=actual)throw new AssertionError("expected "+expected+" got "+actual);}
 public static void main(String[] args){
  Context c=new Context();GrowthTracker tracker=new GrowthTracker(c);tracker.tick();eq(0,GrowthTracker.millis(c));
  SystemClock.now+=30000;tracker.tick();eq(30000,GrowthTracker.millis(c));
  Prefs.get(c).edit().putBoolean("enabled",false).commit();SystemClock.now+=30000;tracker.tick();eq(30000,GrowthTracker.millis(c));
  SystemClock.now+=600000;tracker.tick();eq(30000,GrowthTracker.millis(c));
  Prefs.get(c).edit().putBoolean("enabled",true).commit();tracker.tick();SystemClock.now+=30000;tracker.tick();eq(60000,GrowthTracker.millis(c));
  tracker.stop();SystemClock.now+=300000;tracker=new GrowthTracker(new Context());tracker.tick();eq(60000,GrowthTracker.millis(c));
  SystemClock.now+=30000;tracker.tick();eq(90000,GrowthTracker.millis(c));
  SystemClock.now+=90001;tracker.tick();eq(90000,GrowthTracker.millis(c));
  Prefs.get(c).edit().putBoolean("shorts",false).commit();SystemClock.now+=30000;tracker.tick();eq(90000,GrowthTracker.millis(c));
  System.out.println("Growth tracker: "+n+" checks passed (in-memory Android API fakes)");
 }
}
