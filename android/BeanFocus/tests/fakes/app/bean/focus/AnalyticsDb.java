package app.bean.focus;
import android.content.Context;
public class AnalyticsDb {
 static final AnalyticsDb db=new AnalyticsDb();static long growthMs,spanMs;static int spans;
 static AnalyticsDb get(Context c){return db;}
 double xp(){return 0;}
 void growth(long ms,long wall){growthMs+=ms;}
 void span(long s,long e,String kind,String key,String source){spanMs+=e-s;spans++;}
}
