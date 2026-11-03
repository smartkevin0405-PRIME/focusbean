package app.bean.focus;
import android.content.Context;
public class TelemetrySessionTest {
 static void eq(long a,long b){if(a!=b)throw new AssertionError("expected "+a+" got "+b);}
 public static void main(String[] args){TelemetrySession s=new TelemetrySession(new Context(),"test");s.sampleAt("lecture","ch",10000,10000);s.sampleAt("lecture","ch",12000,12000);eq(2000,AnalyticsDb.spanMs);s.sampleAt("lecture","ch",12500,12500);eq(2000,AnalyticsDb.spanMs);s.sampleAt("lecture","ch",14000,14000);eq(4000,AnalyticsDb.spanMs);s.sampleAt(null,"",16000,16000);s.sampleAt("lecture","ch",18000,18000);eq(4000,AnalyticsDb.spanMs);s.sampleAt("leisure","other",20000,20000);s.sampleAt("leisure","other",22000,22000);eq(6000,AnalyticsDb.spanMs);s.sampleAt("leisure","other",32000,32000);eq(6000,AnalyticsDb.spanMs);s.sampleAt("leisure","other",34000,34000);eq(8000,AnalyticsDb.spanMs);s.stop();s.sampleAt("leisure","other",36000,36000);eq(8000,AnalyticsDb.spanMs);
  // Event scans can win every 500ms slot while watchdog polls lose the shared budget.
  // Sampling from those successful scans must still accumulate time without overcounting.
  s.stop();long before=AnalyticsDb.spanMs;
  for(long now=40000;now<=50000;now+=500)s.sampleAt("unknown","",now,now);
  eq(before+10000,AnalyticsDb.spanMs);
  System.out.println("Telemetry: accumulation, event cadence, category change, hidden/pause, long gap and restart passed");}
}
