package android.os;
public class SystemClock { public static long network=-1;public static java.time.Clock currentNetworkTimeClock(){if(network<0)throw new java.time.DateTimeException("unavailable");return java.time.Clock.fixed(java.time.Instant.ofEpochMilli(network),java.time.ZoneOffset.UTC);}
 public static long now=100000;public static long elapsedRealtime(){return now;} }
