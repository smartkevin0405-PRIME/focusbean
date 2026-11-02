package app.bean.focus;
import android.content.Context;
import android.os.SystemClock;
final class GrowthTracker {
 private final Context context;private long last=-1;private boolean previous=false;
 GrowthTracker(Context c){context=c;}
 static long millis(Context c){return Prefs.get(c).getLong("pet_ms",0);}
 static long xp(Context c){return (long)Math.floor(AnalyticsDb.get(c).xp());}
 static long minutes(Context c){return millis(c)/60000;}
 static boolean eligible(Context c){return Prefs.enabled(c)&&(Prefs.get(c).getBoolean("shorts",true)||!Prefs.games(c).isEmpty());}
 void tick(){tickAt(SystemClock.elapsedRealtime(),System.currentTimeMillis(),eligible(context));}
 void tickAt(long now,long wall,boolean on){long add=last<0?0:GrowthRules.credit(now-last,previous,on);if(add>0){AnalyticsDb.get(context).growth(add,wall);AnalyticsDb.get(context).span(wall-add,wall,"coverage","","service");Prefs.get(context).edit().putLong("pet_ms",millis(context)+add).commit();}last=now;previous=on;}
 void stop(){last=-1;previous=false;}
}
