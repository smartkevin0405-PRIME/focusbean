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
 public static void main(String[] args){
  AccessibilityNodeInfo w=page(true),r=browser(w,"m.youtube.com");BrowserInspector.Result result=BrowserInspector.read(r,"com.sec.android.app.sbrowser");ok(result.playerShorts);ok(result.shorts());ok(result.diagnostic().contains("쇼츠 버튼 배치 감지"));ok(!result.diagnostic().contains("@test-channel"));r.recycle();w.recycle();
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
  w=node().cls("android.webkit.WebView").bounds(0,100,700,1400);for(int i=0;i<800;i++)add(w,node().text("#shorts"));BrowserPlayerInspector.read(w);w.recycle();
  ok(AccessibilityNodeInfo.handles==0);System.out.println("Browser web-player fallback: "+checks+" checks passed");
 }
}
