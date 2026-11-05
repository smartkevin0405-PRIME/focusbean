package app.bean.focus;

/** One budget shared by accessibility events and the watchdog; stores no page content. */
final class BrowserScanPolicy {
 static final long SCAN_INTERVAL_MS=500, PLAYER_INTERVAL_MS=5000, NAVIGATION_WINDOW_MS=3000, ROOT_RETRY_MS=150;
 private String owner="";
 private int window=-1;
 private long lastScan=-SCAN_INTERVAL_MS,lastPlayer=-PLAYER_INTERVAL_MS;
 private long navigationUntil=-1;
 // An event without a window ID (-1) is bound to the first real window scanned
 // for the same browser instead of being mistaken for a window change.
 private boolean unbound;
 // Last host class read from this window: 1 YouTube, -1 another site, 0 unknown.
 // Only this tri-state is kept, never the address itself.
 private int host;

 long delay(long now,String pkg,int windowId){
  if(!matches(pkg,windowId))return 0;
  return Math.max(0,SCAN_INTERVAL_MS-(now-lastScan));
 }
 void scanned(long now,String pkg,int windowId){
  if(!bind(pkg,windowId))scope(pkg,windowId);
  lastScan=now;
 }
 // Only user navigation/window changes arm this short burst. Content updates from
 // playback never extend it. All retries still share the normal scan budget.
 void navigation(long now,String pkg,int windowId){
  if(windowId<0&&owner.equals(pkg))unbound=true;
  else if(!bind(pkg,windowId)){scope(pkg,windowId);unbound=windowId<0;}
  navigationUntil=now+NAVIGATION_WINDOW_MS;
 }
 boolean navigating(long now){return navigationUntil>=0&&now<=navigationUntil;}
 void endNavigation(){navigationUntil=-1;}
 long retryDelay(long now,String pkg,int windowId){
  if(!matches(pkg,windowId)||!navigating(now))return -1;
  long wait=Math.max(120,delay(now,pkg,windowId));
  return now+wait<=navigationUntil?wait:-1;
 }
 // A page or accessibility provider can briefly return no root while it rebuilds.
 // Retry only inside the remaining navigation deadline for the same browser scope.
 long missingRootDelay(long now,String pkg,int windowId){
  if(!matches(pkg,windowId)||!navigating(now))return -1;
  return now+ROOT_RETRY_MS<=navigationUntil?ROOT_RETRY_MS:-1;
 }
 boolean allowPlayer(long now){return now-lastPlayer>=(navigating(now)?SCAN_INTERVAL_MS:PLAYER_INTERVAL_MS);}
 void playerScanned(long now){lastPlayer=now;}
 void rememberHost(int evidence){if(evidence!=0)host=evidence>0?1:-1;}
 int host(){return host;}
 boolean matches(String pkg,int windowId){return owner.equals(pkg)&&(windowId<0||window<0||unbound||window==windowId);}
 // Attach an unresolved scope to the first real window of the same browser. The
 // navigation deadline survives; a different window gets a fresh scan budget.
 private boolean bind(String pkg,int windowId){
  if(!owner.equals(pkg)||windowId<0||(window>=0&&!unbound))return false;
  if(window!=windowId){window=windowId;lastScan=-SCAN_INTERVAL_MS;lastPlayer=-PLAYER_INTERVAL_MS;host=0;}
  unbound=false;return true;
 }
 private void scope(String pkg,int windowId){
  if(matches(pkg,windowId))return;
  owner=pkg;window=windowId;lastScan=-SCAN_INTERVAL_MS;lastPlayer=-PLAYER_INTERVAL_MS;host=0;unbound=false;endNavigation();
 }
 void reset(){owner="";window=-1;lastScan=-SCAN_INTERVAL_MS;lastPlayer=-PLAYER_INTERVAL_MS;host=0;unbound=false;endNavigation();}
}
