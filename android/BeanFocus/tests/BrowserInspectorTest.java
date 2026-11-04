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
  root=node();for(int i=0;i<400;i++){AccessibilityNodeInfo n=node();root.child(n);n.recycle();}bar=node().id(id).text("youtube.com/shorts/deep");root.child(bar);bar.recycle();int queriesBefore=AccessibilityNodeInfo.queries,readsBefore=AccessibilityNodeInfo.childReads;r=BrowserInspector.read(root,pkg);ok(UrlRules.isShorts(r.url));ok(r.nativeTruncated);ok(r.nativeNodes==180);ok(AccessibilityNodeInfo.queries-queriesBefore==BrowserAddress.fieldsFor(pkg).length);ok(AccessibilityNodeInfo.childReads-readsBefore==179);root.recycle();
  // Domain-only toolbar must not hide an explicit document-root home URL.
  root=node();bar=node().id(id).text("m.youtube.com");AccessibilityNodeInfo home=node().cls("android.webkit.WebView");home.getExtras().putCharSequence("url","https://m.youtube.com/");root.child(bar);root.child(home);
  r=BrowserInspector.read(root,pkg);ok(r.confirmedHome());ok(r.knownLocation());ok(r.diagnostic().contains("유튜브 홈 확인"));ok(!BlockPolicy.homeAfterBack(r.url,true,false,r.confirmedHome()));bar.text("youtube.com/shorts/stale");home.getExtras().putCharSequence("url","https://m.youtube.com/?app=m");r=BrowserInspector.read(root,pkg);ok(r.confirmedHome());ok(!UrlRules.isShorts(r.url));bar.recycle();home.recycle();root.recycle();
  // A recommendation link in a WebView is not the active page; only clicks consume targetUrl.
  root=node();AccessibilityNodeInfo web=node().cls("android.webkit.WebView"),link=node(),image=node();
  web.getExtras().putCharSequence("url","https://youtube.com/watch?v=lesson");link.getExtras().putCharSequence(BrowserInspector.TARGET,"https://youtube.com/shorts/recommendation");link.child(image);web.child(link);root.child(web);
  ok(!UrlRules.isShorts(BrowserInspector.read(root,pkg).url));ok(BrowserInspector.clickedShorts(image,false));
  link.getExtras().putCharSequence(BrowserInspector.TARGET,"https://youtube.com.evil.example/shorts/fake");ok(!BrowserInspector.clickedShorts(image,false));
  link.id("shorts-tab");ok(!BrowserInspector.clickedShorts(image,false));ok(BrowserInspector.clickedShorts(image,true));
  link.id(pkg+":id/shorts-tab");ok(!BrowserInspector.clickedShorts(image,true));
  web.getExtras().putCharSequence("url","");ok(!BrowserInspector.read(root,pkg).youtube());
  root.recycle();web.recycle();link.recycle();image.recycle();
  // Samsung/Chromium native WebView wrapper -> virtual document root: no page scan
  // is necessary to recover the path, and sibling toolbar order cannot win.
  root=node();web=node().cls("android.webkit.WebView");AccessibilityNodeInfo document=node();
  document.getExtras().putCharSequence("AccessibilityNodeInfo.chromeRole","rootWebArea");
  document.getExtras().putCharSequence("url","https://m.youtube.com/watch?v=lecture");
  AccessibilityNodeInfo iframe=node();iframe.getExtras().putCharSequence("AccessibilityNodeInfo.chromeRole","rootWebArea");iframe.getExtras().putCharSequence("url","https://m.youtube.com/shorts/embedded");document.child(iframe);web.child(document);root.child(web);
  bar=node().id(id).text("youtube.com/shorts/stale");root.child(bar);
  queriesBefore=AccessibilityNodeInfo.queries;r=BrowserInspector.read(root,pkg,false);
  ok(r.knownLocation());ok(!r.shorts());ok(r.url.contains("/watch"));ok(r.source.equals("웹 문서 루트"));ok(!r.playerChecked);ok(AccessibilityNodeInfo.queries==queriesBefore);ok(document.childReads()==0);
  document.getExtras().putCharSequence("url","https://m.youtube.com/shorts/current");bar.text("m.youtube.com");r=BrowserInspector.read(root,pkg,false);ok(r.shorts());ok(r.documentNodes==2);ok(!r.playerChecked);
  // A focused editor still suppresses enforcement even when the document was first.
  bar.text("youtube.com/watch?v=typing").editing();r=BrowserInspector.read(root,pkg);ok(r.editing);ok(!BlockPolicy.webShorts(r.url,r.editing));ok(!r.playerChecked);
  root.recycle();web.recycle();document.recycle();iframe.recycle();bar.recycle();
  // An ordinary top-level document cannot be overridden by a Shorts iframe.
  root=node();web=node().cls("android.webkit.WebView");web.getExtras().putCharSequence("url","https://example.org/");document=node();document.getExtras().putCharSequence("AccessibilityNodeInfo.chromeRole","rootWebArea");document.getExtras().putCharSequence("url","https://youtube.com/shorts/iframe");web.child(document);root.child(web);bar=node().id(id).text("youtube.com/shorts/stale");root.child(bar);
  r=BrowserInspector.read(root,pkg);ok(!r.youtube());ok(!r.shorts());ok(web.childReads()==0);root.recycle();web.recycle();document.recycle();bar.recycle();
  // Link metadata inside an unlabelled page container is never document metadata.
  root=node();web=node().cls("android.webkit.WebView");link=node();iframe=node();iframe.getExtras().putCharSequence("AccessibilityNodeInfo.chromeRole","rootWebArea");iframe.getExtras().putCharSequence("url","https://youtube.com/shorts/iframe");link.child(iframe);web.child(link);root.child(web);bar=node().id(id).text("m.youtube.com");root.child(bar);
  r=BrowserInspector.read(root,pkg,false);ok(!r.knownLocation());ok(!r.shorts());ok(link.childReads()==0);root.recycle();web.recycle();link.recycle();iframe.recycle();bar.recycle();
  // The reported Samsung domain-only/web-zero case stays unknown and never starts
  // the 600-node player fallback. Repeated reads add no redundant ID queries.
  root=node();bar=node().id(id).text("m.youtube.com");root.child(bar);queriesBefore=AccessibilityNodeInfo.queries;
  for(int i=0;i<5;i++){r=BrowserInspector.read(root,pkg,true);ok(r.youtube());ok(!r.knownLocation());ok(!r.playerChecked);ok(r.playerNodes==0);ok(r.nativeNodes==2);}
  ok(AccessibilityNodeInfo.queries==queriesBefore);root.recycle();bar.recycle();
  // A stable Chrome URL reads the chrome once and never enters its document body.
  root=node();bar=node().id("com.android.chrome:id/url_bar").text("youtube.com/watch?v=lesson");root.child(bar);web=node().cls("android.webkit.WebView");web.getExtras().putCharSequence("url","https://youtube.com/watch?v=lesson");
  for(int i=0;i<800;i++){link=node();web.child(link);link.recycle();}root.child(web);queriesBefore=AccessibilityNodeInfo.queries;readsBefore=AccessibilityNodeInfo.childReads;r=BrowserInspector.read(root,"com.android.chrome",true);
  ok(!r.shorts());ok(!r.playerChecked);ok(r.nativeNodes==3);ok(AccessibilityNodeInfo.queries==queriesBefore);ok(AccessibilityNodeInfo.childReads-readsBefore==2);ok(web.childReads()==0);root.recycle();bar.recycle();web.recycle();
  // The address bar may be hidden during video playback. A host-only document
  // remains YouTube context (not a confirmed home), enabling the player scan.
  root=node();web=node().cls("android.webkit.WebView");web.getExtras().putCharSequence("url","https://m.youtube.com");root.child(web);
  r=BrowserInspector.read(root,pkg);ok(r.youtube());ok(!r.confirmedHome());ok(!r.knownLocation());ok(r.playerChecked);root.recycle();web.recycle();
  // Native wrappers without view IDs may precede Chromium's virtual document.
  root=node();web=node().cls("android.webkit.WebView");AccessibilityNodeInfo shell=node().cls("android.widget.FrameLayout");document=node();document.getExtras().putCharSequence("AccessibilityNodeInfo.chromeRole","rootWebArea");document.getExtras().putCharSequence("url","https://m.youtube.com/shorts/current");shell.child(document);web.child(shell);root.child(web);
  r=BrowserInspector.read(root,pkg,false);ok(r.shorts());ok(r.documentNodes==3);ok(r.documentRefreshes==2);ok(document.childReads()==0);root.recycle();web.recycle();shell.recycle();document.recycle();
  // Several native WebView shells may expose only the host before the virtual root.
  root=node();web=node().cls("android.webkit.WebView");web.getExtras().putCharSequence("url","https://m.youtube.com");shell=node().cls("android.webkit.WebView");shell.getExtras().putCharSequence("url","https://m.youtube.com");document=node();document.getExtras().putCharSequence("AccessibilityNodeInfo.chromeRole","rootWebArea");document.getExtras().putCharSequence("url","https://m.youtube.com/shorts/current");shell.child(document);web.child(shell);root.child(web);r=BrowserInspector.read(root,pkg,false);ok(r.shorts());ok(r.documentRefreshes==3);root.recycle();web.recycle();shell.recycle();document.recycle();
  // A refreshed document's current route must replace the Android snapshot.
  root=node();web=node().cls("android.webkit.WebView").refreshedUrl("https://m.youtube.com/shorts/current");web.getExtras().putCharSequence("url","https://m.youtube.com/");root.child(web);
  r=BrowserInspector.read(root,pkg,false);ok(r.shorts());ok(!r.confirmedHome());ok(web.refreshes()==1);
  web.refreshedUrl("https://m.youtube.com/watch?v=normal");r=BrowserInspector.read(root,pkg,false);ok(!r.shorts());ok(r.url.contains("/watch"));
  web.refreshedUrl("https://m.youtube.com/shorts/detached").refreshable(false);r=BrowserInspector.read(root,pkg);ok(!r.shorts());ok(r.documentUnavailable);ok(!r.playerChecked);root.recycle();web.recycle();
  // Offscreen documents must not replace the currently visible page evidence.
  root=node();web=node().cls("android.webkit.WebView");web.getExtras().putCharSequence("url","https://youtube.com/shorts/offscreen");web.getExtras().putBoolean("AccessibilityNodeInfo.offscreen",true);root.child(web);bar=node().id(id).text("youtube.com/watch?v=current");root.child(bar);
  r=BrowserInspector.read(root,pkg);ok(!r.shorts());ok(r.url.contains("/watch"));root.recycle();web.recycle();bar.recycle();
  // Refresh operations are bounded independently of the shallow-shell budget.
  root=node();for(int i=0;i<20;i++){web=node().cls("android.webkit.WebView");root.child(web);web.recycle();}r=BrowserInspector.read(root,pkg);ok(r.documentRefreshes==4);ok(r.documentUnavailable);root.recycle();
  ok(BrowserInspector.read(null,pkg).url.isEmpty());
  ok(AccessibilityNodeInfo.handles==0);ok(AccessibilityNodeInfo.queries>0);
  System.out.println("Browser inspector: "+checks+" checks passed (Android node fakes)");
 }
}
