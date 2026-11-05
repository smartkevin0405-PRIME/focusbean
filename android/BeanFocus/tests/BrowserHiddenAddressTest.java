package app.bean.focus;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Chromium-shaped nodes: no document "url" extra (Chromium never sets it on accessibility
 * nodes), links/media carry absolute target URLs, and Samsung Internet may hide its toolbar.
 */
public class BrowserHiddenAddressTest {
 private static final String PKG="com.sec.android.app.sbrowser",TARGET="AccessibilityNodeInfo.targetUrl";
 static int checks;static void ok(boolean b){checks++;if(!b)throw new AssertionError("case "+checks);}
 static void add(AccessibilityNodeInfo p,AccessibilityNodeInfo n){p.child(n);n.recycle();}
 static AccessibilityNodeInfo link(String target,int l,int t,int r,int b){AccessibilityNodeInfo n=new AccessibilityNodeInfo().bounds(l,t,r,b);n.getExtras().putCharSequence(TARGET,target);return n;}
 static AccessibilityNodeInfo web(){AccessibilityNodeInfo w=new AccessibilityNodeInfo().cls("android.webkit.WebView").bounds(0,100,700,1400);w.getExtras().putCharSequence("AccessibilityNodeInfo.chromeRole","rootWebArea");return w;}
 /** Portrait Shorts overlay: right action column, owner row, optional same-site links. */
 static AccessibilityNodeInfo shorts(String linkHost){
  AccessibilityNodeInfo w=web();String[] labels={"다른 사용자 1.2천명과 함께 이 동영상에 좋아요 표시","댓글 163개","공유"};
  for(int i=0;i<3;i++)add(w,new AccessibilityNodeInfo().cls("android.widget.Button").clickable(true).description(labels[i]).bounds(590,800+i*140,690,890+i*140));
  add(w,new AccessibilityNodeInfo().text("@test-channel").bounds(70,1130,300,1220));add(w,new AccessibilityNodeInfo().cls("android.widget.Button").text("구독").bounds(330,1130,470,1220));
  if(linkHost!=null){add(w,link("https://"+linkHost+"/@test-channel",20,1130,60,1220));add(w,link("blob:https://"+linkHost+"/5f1c",0,150,700,1300));}
  return w;
 }
 /** Normal landscape watch page: YouTube links, no right action column. */
 static AccessibilityNodeInfo watch(){
  AccessibilityNodeInfo w=web();add(w,link("blob:https://m.youtube.com/a1",0,150,700,550));add(w,link("https://m.youtube.com/@lecture",20,600,300,660));
  add(w,new AccessibilityNodeInfo().cls("android.widget.Button").description("좋아요 1.2천").bounds(20,700,160,760));add(w,new AccessibilityNodeInfo().cls("android.widget.Button").description("공유").bounds(180,700,320,760));
  return w;
 }
 static AccessibilityNodeInfo browser(AccessibilityNodeInfo page,String address,boolean toolbarVisible){
  AccessibilityNodeInfo r=new AccessibilityNodeInfo().pkg(PKG);
  if(address!=null)add(r,new AccessibilityNodeInfo().id(PKG+":id/location_bar_edit_text").text(address).visible(toolbarVisible));
  r.child(page);page.recycle();return r;
 }
 static boolean block(BrowserInspector.Result r){return BlockPolicy.browserShorts(r.url,r.editing,r.playerShorts,r.confirmedHome());}
 static BrowserInspector.Result read(AccessibilityNodeInfo root,int memory){BrowserInspector.Result r=BrowserInspector.read(root,PKG,true,false,memory);root.recycle();return r;}
 public static void main(String[] args){
  // Hidden toolbar + unknown host: same-site links establish YouTube, then the player layout blocks.
  BrowserInspector.Result r=read(browser(shorts("m.youtube.com"),"m.youtube.com",false),0);
  ok(r.addressHidden());ok(r.playerChecked);ok(r.youtube());ok(r.playerShorts);ok(block(r));ok(r.hostEvidence()==1);ok(r.youtubeLinks==2&&r.otherLinks==0);
  ok(r.diagnostic().contains("주소창 숨김"));ok(!r.diagnostic().contains("test-channel"));ok(!r.diagnostic().contains("m.youtube.com/"));
  // No toolbar node at all behaves the same way.
  r=read(browser(shorts("m.youtube.com"),null,false),0);ok(block(r));
  // Window last seen on YouTube: the layout alone counts while no other site is evident.
  r=read(browser(shorts(null),null,false),1);ok(r.pageYouTube);ok(block(r));ok(r.hostEvidence()==0);
  // Unknown host and no link evidence: never block on geometry alone.
  r=read(browser(shorts(null),null,false),0);ok(r.playerChecked);ok(!r.pageYouTube);ok(!block(r));ok(r.hostEvidence()==0);ok(r.playerState.contains("유튜브 화면 미확인"));
  // Window last seen on another site: no page walk at all while the toolbar is hidden.
  r=read(browser(shorts("m.youtube.com"),null,false),-1);ok(!r.playerChecked);ok(!block(r));ok(r.playerNodes==0);
  // A short-video layout on another site is never treated as YouTube, even after YouTube.
  r=read(browser(shorts("www.tiktok.com"),null,false),1);ok(r.playerChecked);ok(!r.pageYouTube);ok(!block(r));ok(r.hostEvidence()==-1);
  // Normal YouTube video with hidden toolbar stays allowed.
  r=read(browser(watch(),null,false),1);ok(r.playerChecked);ok(r.youtube());ok(!r.playerShorts);ok(!block(r));
  // Samsung shows the host only: the existing domain-only path still blocks the player.
  r=read(browser(shorts(null),"m.youtube.com",true),0);ok(!r.addressHidden());ok(block(r));ok(r.hostEvidence()==1);
  // A title in the toolbar is not an address; links decide.
  r=read(browser(shorts("m.youtube.com"),"YouTube",true),0);ok(r.addressHidden());ok(block(r));
  // A known non-YouTube address never starts the YouTube page walk.
  r=read(browser(shorts("m.youtube.com"),"example.org/article",true),1);ok(!r.playerChecked);ok(!block(r));ok(r.hostEvidence()==-1);
  // Editing the address suppresses everything.
  AccessibilityNodeInfo root=browser(shorts("m.youtube.com"),"m.youtube.com/shorts/x",true);AccessibilityNodeInfo editor=root.getChild(0);editor.editing();editor.recycle();
  r=read(root,1);ok(r.editing);ok(!r.playerChecked);ok(!block(r));
  // Offscreen links (next reel, comments) do not count as evidence.
  AccessibilityNodeInfo page=shorts(null);AccessibilityNodeInfo off=link("https://m.youtube.com/shorts/next",0,1500,700,2800);off.getExtras().putBoolean("AccessibilityNodeInfo.offscreen",true);add(page,off);
  r=read(browser(page,null,false),0);ok(r.youtubeLinks==0);ok(!block(r));
  // Host classes only: link/media targets are never addresses for navigation.
  ok(WebShortsRules.origin("blob:https://m.youtube.com/5f1c")==1);ok(WebShortsRules.origin("https://youtube.com/@x")==1);ok(WebShortsRules.origin("https://www.tiktok.com/@x")==-1);
  ok(WebShortsRules.origin("/shorts/x")==0);ok(WebShortsRules.origin("javascript:void(0)")==0);ok(WebShortsRules.origin("YouTube")==0);ok(WebShortsRules.origin(null)==0);ok(WebShortsRules.origin("blob:null/1")==0);
  ok(WebShortsRules.page(1,0)==1);ok(WebShortsRules.page(4,2)==1);ok(WebShortsRules.page(3,2)==0);ok(WebShortsRules.page(0,1)==0);ok(WebShortsRules.page(1,3)==-1);ok(WebShortsRules.page(0,0)==0);
  ok(BrowserAddress.known("m.youtube.com"));ok(!BrowserAddress.known("YouTube"));ok(!BrowserAddress.known(""));ok(!BrowserAddress.known("검색어 또는 URL 입력"));
  ok(AccessibilityNodeInfo.handles==0);
  System.out.println("Hidden address / Chromium-shaped nodes: "+checks+" checks passed (synthetic nodes, not a device test)");
 }
}
