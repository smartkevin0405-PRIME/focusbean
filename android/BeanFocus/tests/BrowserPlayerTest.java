package app.bean.focus;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.*;
public class BrowserPlayerTest {
 static int checks;static void ok(boolean b){checks++;if(!b)throw new AssertionError("case "+checks);}
 static AccessibilityNodeInfo node(){return new AccessibilityNodeInfo();}
 static void add(AccessibilityNodeInfo p,AccessibilityNodeInfo n){p.child(n);n.recycle();}
 static AccessibilityNodeInfo page(boolean vertical){AccessibilityNodeInfo w=node().cls("android.webkit.WebView").bounds(0,100,700,1400);
  String[] labels={"이 동영상에 좋아요 표시 · 704개","댓글 163개","공유"};
  for(int i=0;i<3;i++)add(w,node().cls("android.widget.Button").description(labels[i]).bounds(vertical?590:40+i*200,vertical?800+i*140:800,vertical?690:140+i*200,vertical?890+i*140:890));
  add(w,node().text("구독").bounds(330,1130,470,1220));add(w,node().text("@test-channel").bounds(70,1130,300,1220));return w;}
 static AccessibilityNodeInfo browser(AccessibilityNodeInfo web,String address){AccessibilityNodeInfo r=node();add(r,node().id("com.sec.android.app.sbrowser:id/location_bar_edit_text").text(address));r.child(web);return r;}
 static AccessibilityNodeInfo strongPage(){AccessibilityNodeInfo w=page(true);add(w,node().id("shorts-player").bounds(0,150,700,1300));return w;}
 public static void main(String[] args){
  AccessibilityNodeInfo w=page(true),r=browser(w,"m.youtube.com");BrowserInspector.Result result=BrowserInspector.read(r,"com.sec.android.app.sbrowser");ok(result.playerShorts);ok(result.shorts());ok(result.playerChecked);ok(result.playerNodes==6);ok(r.childReads()==2);ok(result.diagnostic().contains("쇼츠 버튼 배치 감지"));ok(!result.diagnostic().contains("@test-channel"));r.recycle();w.recycle();
  // Service cooldown can skip only the expensive geometry scan while preserving
  // normal address/document detection; there is no implicit cached block result.
  w=page(true);r=browser(w,"m.youtube.com");result=BrowserInspector.read(r,"com.sec.android.app.sbrowser",false);ok(!result.playerChecked);ok(!result.playerShorts);ok(result.playerNodes==0);ok(!result.knownLocation());ok(result.diagnostic().contains("재검사 대기"));ok(result.diagnostic().contains("플레이어 0"));r.recycle();w.recycle();
  w=page(false);r=browser(w,"m.youtube.com");ok(!BrowserInspector.read(r,"com.sec.android.app.sbrowser").shorts());r.recycle();w.recycle();
  // Even matching geometry on a confirmed normal-video URL must not override that location.
  w=page(true);w.getExtras().putCharSequence("url","https://m.youtube.com/watch?v=lesson");r=browser(w,"m.youtube.com");result=BrowserInspector.read(r,"com.sec.android.app.sbrowser");ok(!result.playerShorts);ok(!result.shorts());r.recycle();w.recycle();
  w=page(true);w.getExtras().putCharSequence("url","https://m.youtube.com/");r=browser(w,"m.youtube.com");ok(BrowserInspector.read(r,"com.sec.android.app.sbrowser").confirmedHome());ok(!BrowserInspector.read(r,"com.sec.android.app.sbrowser").shorts());r.recycle();w.recycle();
  w=page(true);r=browser(w,"example.org");ok(!BrowserInspector.read(r,"com.sec.android.app.sbrowser").shorts());r.recycle();w.recycle();
  w=page(true).visible(false);r=browser(w,"m.youtube.com");ok(!BrowserInspector.read(r,"com.sec.android.app.sbrowser").shorts());r.recycle();w.recycle();
  // Shorts titles, links and a Shorts tab do not constitute a player.
  w=node().cls("android.webkit.WebView").bounds(0,100,700,1400);add(w,node().text("재미있는 #shorts 영상"));add(w,node().text("Shorts").id("shorts-tab"));r=browser(w,"m.youtube.com");ok(!BrowserInspector.read(r,"com.sec.android.app.sbrowser").shorts());r.recycle();w.recycle();
  // No WebView: native browser controls must not be mistaken for page controls.
  w=page(true).cls("android.view.View");r=browser(w,"m.youtube.com");ok(!BrowserInspector.read(r,"com.sec.android.app.sbrowser").shorts());r.recycle();w.recycle();
  List<WebShortsRules.Control> c=Arrays.asList(new WebShortsRules.Control(1,.90f,.60f),new WebShortsRules.Control(2,.91f,.72f),new WebShortsRules.Control(3,.90f,.84f));
  ok(WebShortsRules.player(true,false,true,c));ok(!WebShortsRules.player(true,false,false,c));ok(!WebShortsRules.player(false,false,true,c));ok(!WebShortsRules.player(true,false,true,c.subList(0,2)));ok(WebShortsRules.player(false,true,false,c.subList(0,2)));
  ok(WebShortsRules.control("Dislike")==0);ok(WebShortsRules.control("싫어요")==0);ok(WebShortsRules.control("Comments 163")==2);ok(WebShortsRules.control("Share")==3);ok(WebShortsRules.control("좋아요 704")==1);ok(WebShortsRules.subscribe("구독 중"));
  List<WebShortsRules.Control> row=Arrays.asList(new WebShortsRules.Control(1,.8f,.7f),new WebShortsRules.Control(2,.9f,.7f),new WebShortsRules.Control(3,.95f,.7f));ok(!WebShortsRules.player(true,true,true,row));
  w=node().cls("android.webkit.WebView").bounds(0,100,700,1400);add(w,node().id("shorts-player").bounds(0,150,700,1300));add(w,node().cls("android.widget.Button").description("댓글").bounds(590,800,690,890));add(w,node().cls("android.widget.Button").description("공유").bounds(590,1000,690,1090));
  r=browser(w,"m.youtube.com");ok(BrowserInspector.read(r,"com.sec.android.app.sbrowser").shorts());ok(ViewIdSnapshot.collect(r).contains("web:shorts-player"));r.recycle();w.recycle();
  // Unrelated web frames cannot combine their controls into a player.
  r=node();for(int i=0;i<3;i++){w=node().cls("android.webkit.WebView").bounds(0,100,700,1400);add(w,node().cls("android.widget.Button").description(i==0?"좋아요":i==1?"댓글":"공유").bounds(590,700+i*180,690,790+i*180));add(w,node().text("구독").bounds(330,1130,470,1220));add(r,w);}ok(!BrowserPlayerInspector.read(r).shorts);r.recycle();
  // Hidden huge tree and page-budget exhaustion release every owned handle.
  // An outer page's owner/like must not combine with comments/share in an iframe.
  w=node().cls("android.webkit.WebView").bounds(0,100,700,1400);add(w,node().cls("android.widget.Button").description("좋아요").bounds(590,700,690,790));add(w,node().text("구독").bounds(330,1130,470,1220));
  AccessibilityNodeInfo nested=node().bounds(0,100,700,1400);nested.getExtras().putCharSequence("AccessibilityNodeInfo.chromeRole","rootWebArea");add(nested,node().cls("android.widget.Button").description("댓글").bounds(590,880,690,970));add(nested,node().cls("android.widget.Button").description("공유").bounds(590,1060,690,1150));add(w,nested);ok(!BrowserPlayerInspector.read(w).shorts);w.recycle();
  // A transparent native WebView wrapper still permits its own complete document.
  w=node().cls("android.webkit.WebView").bounds(0,100,700,1400);nested=page(true).cls("android.view.View");nested.getExtras().putCharSequence("AccessibilityNodeInfo.chromeRole","rootWebArea");add(w,nested);ok(BrowserPlayerInspector.read(w).shorts);w.recycle();
  w=node().cls("android.webkit.WebView").bounds(0,100,700,1400);for(int i=0;i<800;i++)add(w,node().text("#shorts"));int before=AccessibilityNodeInfo.childReads;BrowserPlayerInspector.Result budget=BrowserPlayerInspector.read(w);ok(budget.nodes==600);ok(budget.truncated);ok(AccessibilityNodeInfo.childReads-before==599);w.recycle();
  // SPA navigation can temporarily keep a home/watch document URL. Only a
  // current player ID plus controls may override it during an explicit hint.
  for(String location:new String[]{"https://m.youtube.com/","https://m.youtube.com/watch?v=normal"}){
   w=page(true);w.getExtras().putCharSequence("url",location);r=browser(w,"m.youtube.com");
   result=BrowserInspector.read(r,"com.sec.android.app.sbrowser",true,true);ok(result.playerChecked);ok(!result.shorts());ok(result.diagnostic().contains("일반 경로 유지"));r.recycle();w.recycle();
   w=strongPage();w.getExtras().putCharSequence("url",location);r=browser(w,"m.youtube.com");
   result=BrowserInspector.read(r,"com.sec.android.app.sbrowser",true,false);ok(!result.playerChecked);ok(!result.shorts());
   result=BrowserInspector.read(r,"com.sec.android.app.sbrowser",true,true);ok(result.playerShorts);ok(!result.confirmedHome());
   result=BrowserInspector.read(r,"com.sec.android.app.sbrowser",false,true);ok(!result.playerChecked);ok(!result.shorts());r.recycle();w.recycle();
  }
  // Hidden toolbar plus document-only host still reaches player detection.
  w=strongPage();w.getExtras().putCharSequence("url","https://m.youtube.com");r=node();r.child(w);result=BrowserInspector.read(r,"com.sec.android.app.sbrowser");ok(result.youtube());ok(result.playerShorts);r.recycle();w.recycle();
  // A stale toolbar from another site cannot hide the freshly read document host.
  w=strongPage();w.getExtras().putCharSequence("url","https://m.youtube.com");r=browser(w,"https://example.org/article");result=BrowserInspector.read(r,"com.sec.android.app.sbrowser");ok(result.youtube());ok(result.playerShorts);r.recycle();w.recycle();
  // A host-only native shell still leads to its same-host virtual document;
  // it must not be mistaken for an iframe below a complete document URL.
  w=node().cls("android.webkit.WebView").bounds(0,100,700,1400);w.getExtras().putCharSequence("url","https://m.youtube.com");nested=strongPage().cls("android.view.View");nested.getExtras().putCharSequence("AccessibilityNodeInfo.chromeRole","rootWebArea");nested.getExtras().putCharSequence("url","https://m.youtube.com");add(w,nested);r=node();r.child(w);result=BrowserInspector.read(r,"com.sec.android.app.sbrowser");ok(result.youtube());ok(result.playerShorts);r.recycle();w.recycle();
  // A defunct virtual document must not re-enter through the wrapper's player scan.
  w=node().cls("android.webkit.WebView").bounds(0,100,700,1400);w.getExtras().putCharSequence("url","https://m.youtube.com");nested=strongPage().cls("android.view.View").refreshable(false);nested.getExtras().putCharSequence("AccessibilityNodeInfo.chromeRole","rootWebArea");add(w,nested);r=node();r.child(w);result=BrowserInspector.read(r,"com.sec.android.app.sbrowser",true,true);ok(!result.playerChecked);ok(!result.shorts());r.recycle();w.recycle();
  // Chromium's visible flag includes offscreen nodes. Explicit offscreen
  // controls or player roots cannot provide active player evidence.
  w=strongPage();w.getExtras().putBoolean("AccessibilityNodeInfo.offscreen",true);ok(!BrowserPlayerInspector.read(w).shorts);w.recycle();
  w=strongPage();AccessibilityNodeInfo control=w.getChild(0);control.getExtras().putBoolean("AccessibilityNodeInfo.offscreen",true);control.recycle();control=w.getChild(1);control.getExtras().putBoolean("AccessibilityNodeInfo.offscreen",true);control.recycle();ok(!BrowserPlayerInspector.read(w).shorts);w.recycle();
  // A complete Shorts player inside an iframe is still not a top-level player.
  w=node().cls("android.webkit.WebView").bounds(0,100,700,1400);w.getExtras().putCharSequence("url","https://m.youtube.com/watch?v=normal");nested=strongPage();nested.getExtras().putCharSequence("AccessibilityNodeInfo.chromeRole","rootWebArea");nested.getExtras().putCharSequence("url","https://m.youtube.com/shorts/embedded");add(w,nested);r=browser(w,"m.youtube.com");result=BrowserInspector.read(r,"com.sec.android.app.sbrowser",true,true);ok(!result.shorts());r.recycle();w.recycle();
  // A non-YouTube web document cannot provide controls for a YouTube toolbar.
  w=strongPage();w.getExtras().putCharSequence("url","https://example.org/");ok(!BrowserPlayerInspector.read(w).shorts);w.recycle();
  ok(AccessibilityNodeInfo.handles==0);System.out.println("Browser web-player fallback: "+checks+" checks passed");
 }
}
