package app.bean.focus;
import android.content.Context;
import android.os.SystemClock;
final class TelemetrySession {
 private final Context c;private final String source;private long lastE=-1,lastW;private String previous=null,detail="";
 TelemetrySession(Context c,String source){this.c=c;this.source=source;}
 void sample(String kind,String key){sampleAt(kind,key,SystemClock.elapsedRealtime(),System.currentTimeMillis());}
 void sampleAt(String kind,String key,long e,long w){if(lastE>=0&&kind!=null&&kind.equals(previous)&&key.equals(detail)&&e-lastE>=1900&&e-lastE<=6000&&Math.abs((w-lastW)-(e-lastE))<1000){AnalyticsDb.get(c).span(lastW,w,kind,key,source);lastE=e;lastW=w;}else if(lastE<0||kind==null||!kind.equals(previous)||!key.equals(detail)||e-lastE>6000){lastE=e;lastW=w;}previous=kind;detail=key;}
 void stop(){lastE=-1;previous=null;}
}
