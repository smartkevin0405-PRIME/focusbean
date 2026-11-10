package app.bean.focus;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Samsung Internet shows no web nodes on the device. The inspector re-reads the largest
 * empty view once (a cached copy can stay empty after Chromium's first, empty answer)
 * and reports only class names and counts.
 */
public class BrowserEmptyPageTest {
 private static final String PKG="com.sec.android.app.sbrowser";
 static int checks;static void ok(boolean b){checks++;if(!b)throw new AssertionError("case "+checks);}
 static void add(AccessibilityNodeInfo p,AccessibilityNodeInfo n){p.child(n);n.recycle();}
 static AccessibilityNodeInfo browser(AccessibilityNodeInfo content){
  AccessibilityNodeInfo r=new AccessibilityNodeInfo().pkg(PKG).cls("android.widget.FrameLayout").bounds(0,0,720,1500);
  add(r,new AccessibilityNodeInfo().id(PKG+":id/location_bar_edit_text").text("m.youtube.com").bounds(80,20,600,90));
  add(r,new AccessibilityNodeInfo().id(PKG+":id/back_button").cls("android.widget.ImageButton").bounds(0,1420,100,1500));
  r.child(content);content.recycle();return r;
 }
 static AccessibilityNodeInfo emptyContent(){return new AccessibilityNodeInfo().cls("android.widget.FrameLayout").bounds(0,100,720,1400);}
 static BrowserInspector.Result read(AccessibilityNodeInfo root){BrowserInspector.Result r=BrowserInspector.read(root,PKG,true,false,0);root.recycle();return r;}
 public static void main(String[] args){
  int before=AccessibilityNodeInfo.handles;BrowserInspector.probeIntervalNanos=0;
  // The page view stays empty: no web root, and the report names only class and counts.
  AccessibilityNodeInfo content=emptyContent();AccessibilityNodeInfo keep=AccessibilityNodeInfo.obtain(content);
  BrowserInspector.Result r=read(browser(content));
  ok(r.playerState.contains("웹 루트 없음"));ok(!r.playerChecked);ok(keep.refreshes()==1);
  ok(r.webHint.contains("가장 큰 빈 영역 FrameLayout")&&r.webHint.contains("자식 0")&&r.webHint.contains("Chromium 역할 0"));
  ok(r.diagnostic().contains("웹 영역 진단"));ok(!r.diagnostic().contains("m.youtube.com"));keep.recycle();
  // A stale empty view that is a Chromium page once re-read: the page is inspected.
  AccessibilityNodeInfo page=BrowserHiddenAddressTest.shorts("m.youtube.com");
  AccessibilityNodeInfo stale=emptyContent().refreshedClass("android.webkit.WebView").refreshedChild(page);page.recycle();
  r=read(browser(stale));
  ok(r.webHint.contains("웹 루트 확보"));ok(r.playerChecked);ok(r.youtube());ok(r.playerShorts);
  // A page already exposed: no probe, no extra refresh of other views.
  AccessibilityNodeInfo empty=new AccessibilityNodeInfo().cls("android.widget.FrameLayout").bounds(0,1400,720,1420);AccessibilityNodeInfo emptyKeep=AccessibilityNodeInfo.obtain(empty);
  AccessibilityNodeInfo root=browser(BrowserHiddenAddressTest.watch());root.child(empty);empty.recycle();
  r=read(root);ok(r.webHint.isEmpty());ok(emptyKeep.refreshes()==0);emptyKeep.recycle();
  // A page root reported as not visible is counted, not inspected.
  AccessibilityNodeInfo hidden=BrowserHiddenAddressTest.web().visible(false);
  r=read(browser(hidden));ok(r.hiddenWebRoots==1);ok(r.webHint.contains("숨은 웹 루트 1"));
  // A view that cannot be re-read says so.
  r=read(browser(emptyContent().refreshable(false)));ok(r.webHint.contains("새로고침 실패"));
  // Re-reads are rate limited outside tests.
  BrowserInspector.probeIntervalNanos=60_000_000_000L;read(browser(emptyContent()));AccessibilityNodeInfo again=emptyContent();AccessibilityNodeInfo againKeep=AccessibilityNodeInfo.obtain(again);
  r=read(browser(again));ok(againKeep.refreshes()==0&&r.webHint.contains("새로고침 대기"));againKeep.recycle();
  ok(AccessibilityNodeInfo.handles==before);
  System.out.println("Browser empty page probe: "+checks+" checks passed");
 }
}
