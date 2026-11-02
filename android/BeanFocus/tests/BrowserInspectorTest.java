package app.bean.focus;
import android.view.accessibility.AccessibilityNodeInfo;
public class BrowserInspectorTest {
 private static int checks;static void ok(boolean value){checks++;if(!value)throw new AssertionError("case "+checks);}
 private static AccessibilityNodeInfo node(){return new AccessibilityNodeInfo();}
 public static void main(String[] args){
  String pkg="com.sec.android.app.sbrowser",id=pkg+":id/location_bar_edit_text";
  AccessibilityNodeInfo root=node(),bar=node().id(id).text("m.youtube.com").description("https://m.youtube.com/shorts/abc");root.child(bar);
  BrowserInspector.Result r=BrowserInspector.read(root,pkg);ok(UrlRules.isShorts(r.url));ok(r.source.endsWith("description"));ok(r.diagnostic().contains(id));ok(!r.diagnostic().contains("/abc"));
  bar.description("");r=BrowserInspector.read(root,pkg);ok(r.youtube());ok(!BlockPolicy.webShorts(r.url,r.editing));ok(!BlockPolicy.homeAfterBack(r.url,true,false,r.confirmedHome()));ok(r.diagnostic().contains("판별 불가"));ok(!r.diagnostic().contains("일반 유튜브"));
  bar.text("https://example.com/path");r=BrowserInspector.read(root,pkg);ok(r.diagnostic().contains("유튜브 외 주소"));
  bar.text("youtube.com/shorts/typed").editing();r=BrowserInspector.read(root,pkg);ok(r.editing);ok(!UrlRules.isShorts(r.url));bar.recycle();root.recycle();
  // Hidden toolbar and page nodes must not cause a block from stale text.
  root=node();bar=node().id(id).text("youtube.com/shorts/hidden").visible(false);root.child(bar);ok(!UrlRules.isShorts(BrowserInspector.read(root,pkg).url));bar.recycle();root.recycle();
  // Native toolbar beyond the fallback's 180-node budget is still found by ID lookup.
  root=node();for(int i=0;i<400;i++){AccessibilityNodeInfo n=node();root.child(n);n.recycle();}bar=node().id(id).text("youtube.com/shorts/deep");root.child(bar);bar.recycle();ok(UrlRules.isShorts(BrowserInspector.read(root,pkg).url));root.recycle();
  // Domain-only toolbar must not hide an explicit document-root home URL.
  root=node();bar=node().id(id).text("m.youtube.com");AccessibilityNodeInfo home=node().cls("android.webkit.WebView");home.getExtras().putCharSequence("url","https://m.youtube.com/");root.child(bar);root.child(home);
  r=BrowserInspector.read(root,pkg);ok(r.confirmedHome());ok(r.knownLocation());ok(r.diagnostic().contains("유튜브 홈 확인"));ok(!BlockPolicy.homeAfterBack(r.url,true,false,r.confirmedHome()));bar.text("youtube.com/shorts/stale");home.getExtras().putCharSequence("url","https://m.youtube.com/?app=m");r=BrowserInspector.read(root,pkg);ok(r.confirmedHome());ok(!UrlRules.isShorts(r.url));bar.recycle();home.recycle();root.recycle();
  // A recommendation link in a WebView is not the active page; only clicks consume targetUrl.
  root=node();AccessibilityNodeInfo web=node().cls("android.webkit.WebView"),link=node(),image=node();
  web.getExtras().putCharSequence("url","https://youtube.com/watch?v=lesson");link.getExtras().putCharSequence(BrowserInspector.TARGET,"https://youtube.com/shorts/recommendation");link.child(image);web.child(link);root.child(web);
  ok(!UrlRules.isShorts(BrowserInspector.read(root,pkg).url));ok(BrowserInspector.clickedShorts(image,false));
  link.getExtras().putCharSequence(BrowserInspector.TARGET,"https://youtube.com.evil.example/shorts/fake");ok(!BrowserInspector.clickedShorts(image,false));
  link.id("shorts-tab");ok(!BrowserInspector.clickedShorts(image,false));ok(BrowserInspector.clickedShorts(image,true));
  web.getExtras().putCharSequence("url","");ok(!BrowserInspector.read(root,pkg).youtube());
  root.recycle();web.recycle();link.recycle();image.recycle();
  ok(AccessibilityNodeInfo.handles==0);ok(AccessibilityNodeInfo.queries>0);
  System.out.println("Browser inspector: "+checks+" checks passed (Android node fakes)");
 }
}
