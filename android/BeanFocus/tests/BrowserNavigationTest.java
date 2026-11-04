package app.bean.focus;
import android.view.accessibility.AccessibilityNodeInfo;

/** Synthetic timeline exercising the actual scheduler, node reader and block policy together. */
public class BrowserNavigationTest {
 private static final String PKG="com.sec.android.app.sbrowser";
 private static int checks;
 private static void ok(boolean value){checks++;if(!value)throw new AssertionError("case "+checks);}
 private static void add(AccessibilityNodeInfo parent,AccessibilityNodeInfo child){parent.child(child);child.recycle();}
 private static AccessibilityNodeInfo page(String url,boolean player,boolean marker){
  AccessibilityNodeInfo root=new AccessibilityNodeInfo().pkg(PKG);
  add(root,new AccessibilityNodeInfo().id(PKG+":id/location_bar_edit_text").text("m.youtube.com"));
  AccessibilityNodeInfo web=new AccessibilityNodeInfo().cls("android.webkit.WebView").bounds(0,100,700,1400);
  web.getExtras().putCharSequence("url",url);
  if(player){
   String[] labels={"좋아요","댓글","공유"};
   for(int i=0;i<3;i++)add(web,new AccessibilityNodeInfo().cls("android.widget.Button").description(labels[i]).bounds(590,800+i*140,690,890+i*140));
   add(web,new AccessibilityNodeInfo().text("구독").bounds(330,1130,470,1220));
   if(marker)add(web,new AccessibilityNodeInfo().id("shorts-player").bounds(0,150,700,1300));
  }
  add(root,web);return root;
 }
 private static BrowserInspector.Result scan(BrowserScanPolicy policy,long now,AccessibilityNodeInfo root){
  ok(policy.delay(now,PKG,1)==0);
  policy.scanned(now,PKG,1);
  BrowserInspector.Result r=BrowserInspector.read(root,PKG,policy.allowPlayer(now),policy.navigating(now));
  if(r.playerChecked)policy.playerScanned(now);
  return r;
 }
 private static boolean block(BrowserInspector.Result r){return BlockPolicy.browserShorts(r.url,r.editing,r.playerShorts,r.confirmedHome());}
 public static void main(String[] args){
  BrowserScanPolicy p=new BrowserScanPolicy();
  AccessibilityNodeInfo root=page("https://m.youtube.com",false,false);
  ok(!block(scan(p,0,root)));root.recycle();p.navigation(100,PKG,1);
  // A 450ms click confirmation waits for the slot used by the home scan.
  ok(p.delay(450,PKG,1)==50);
  root=page("https://m.youtube.com",false,false);ok(!block(scan(p,500,root)));root.recycle();
  // Late player construction after the first check is caught by a bounded retry.
  root=page("https://m.youtube.com",true,false);BrowserInspector.Result r=scan(p,1000,root);
  ok(r.playerChecked);ok(block(r));root.recycle();p.endNavigation();ok(p.retryDelay(1000,PKG,1)==-1);
  // A provider-cached home URL is overridden only with current player ID + actions.
  p.reset();p.navigation(0,PKG,1);root=page("https://m.youtube.com/",true,true);
  r=scan(p,450,root);ok(block(r));ok(!r.confirmedHome());root.recycle();p.endNavigation();
  // After BACK, fresh ordinary content must not inherit the prior block decision.
  root=page("https://m.youtube.com/watch?v=lesson",false,false);
  r=BrowserInspector.read(root,PKG,true,true);ok(!block(r));root.recycle();
  // A cancelled link / normal page remains allowed even during the retry burst.
  p.reset();p.navigation(0,PKG,1);
  for(long now=450;now<3000;now+=500){root=page("https://m.youtube.com/watch?v=lesson",true,false);r=scan(p,now,root);ok(!block(r));root.recycle();}
  ok(p.retryDelay(3001,PKG,1)==-1);
  // Address editing suppresses even a genuine current Shorts URL/player.
  p.reset();p.navigation(0,PKG,1);root=page("https://m.youtube.com/shorts/test",true,true);
  AccessibilityNodeInfo editor=root.getChild(0);editor.editing();editor.recycle();
  r=scan(p,450,root);ok(r.editing);ok(!block(r));ok(!r.playerChecked);root.recycle();
  // Switching window clears old navigation scope. No scan result is cached.
  p.navigation(1000,PKG,1);p.scanned(1001,PKG,2);ok(!p.navigating(1001));
  ok(!BlockPolicy.sameWindow(PKG,1,PKG,2));ok(!BlockPolicy.sameWindow(PKG,1,"other.app",1));
  p.reset();ok(!p.navigating(1002));ok(AccessibilityNodeInfo.handles==0);
  System.out.println("Browser navigation timeline: "+checks+" checks passed (synthetic nodes/time, not a device test)");
 }
}
