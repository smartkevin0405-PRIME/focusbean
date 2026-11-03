package app.bean.focus;

/** One budget shared by accessibility events and the watchdog; stores no page content. */
final class BrowserScanPolicy {
 static final long SCAN_INTERVAL_MS=500, PLAYER_INTERVAL_MS=5000;
 private String owner="";
 private int window=-1;
 private long lastScan=-SCAN_INTERVAL_MS,lastPlayer=-PLAYER_INTERVAL_MS;

 long delay(long now,String pkg,int windowId){
  if(!matches(pkg,windowId))return 0;
  return Math.max(0,SCAN_INTERVAL_MS-(now-lastScan));
 }
 void scanned(long now,String pkg,int windowId){
  if(!matches(pkg,windowId)){owner=pkg;window=windowId;lastPlayer=-PLAYER_INTERVAL_MS;}
  lastScan=now;
 }
 boolean allowPlayer(long now){return now-lastPlayer>=PLAYER_INTERVAL_MS;}
 void playerScanned(long now){lastPlayer=now;}
 boolean matches(String pkg,int windowId){return owner.equals(pkg)&&window==windowId;}
 void reset(){owner="";window=-1;lastScan=-SCAN_INTERVAL_MS;lastPlayer=-PLAYER_INTERVAL_MS;}
}
