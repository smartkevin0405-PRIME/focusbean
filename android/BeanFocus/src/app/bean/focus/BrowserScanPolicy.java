package app.bean.focus;

/** One budget shared by accessibility events and the watchdog; stores no page content. */
final class BrowserScanPolicy {
 static final long SCAN_INTERVAL_MS=500, PLAYER_INTERVAL_MS=5000, NAVIGATION_WINDOW_MS=3000;
 private String owner="";
 private int window=-1;
 private long lastScan=-SCAN_INTERVAL_MS,lastPlayer=-PLAYER_INTERVAL_MS;
 private long navigationUntil=-1;

 long delay(long now,String pkg,int windowId){
  if(!matches(pkg,windowId))return 0;
  return Math.max(0,SCAN_INTERVAL_MS-(now-lastScan));
 }
 void scanned(long now,String pkg,int windowId){
  scope(pkg,windowId);
  lastScan=now;
 }
 // Only user navigation/window changes arm this short burst. Content updates from
 // playback never extend it. All retries still share the normal scan budget.
 void navigation(long now,String pkg,int windowId){scope(pkg,windowId);navigationUntil=now+NAVIGATION_WINDOW_MS;}
 boolean navigating(long now){return navigationUntil>=0&&now<=navigationUntil;}
 void endNavigation(){navigationUntil=-1;}
 long retryDelay(long now,String pkg,int windowId){
  if(!matches(pkg,windowId)||!navigating(now))return -1;
  long wait=Math.max(120,delay(now,pkg,windowId));
  return now+wait<=navigationUntil?wait:-1;
 }
 boolean allowPlayer(long now){return now-lastPlayer>=(navigating(now)?SCAN_INTERVAL_MS:PLAYER_INTERVAL_MS);}
 void playerScanned(long now){lastPlayer=now;}
 boolean matches(String pkg,int windowId){return owner.equals(pkg)&&window==windowId;}
 private void scope(String pkg,int windowId){
  if(matches(pkg,windowId))return;
  owner=pkg;window=windowId;lastScan=-SCAN_INTERVAL_MS;lastPlayer=-PLAYER_INTERVAL_MS;endNavigation();
 }
 void reset(){owner="";window=-1;lastScan=-SCAN_INTERVAL_MS;lastPlayer=-PLAYER_INTERVAL_MS;endNavigation();}
}
