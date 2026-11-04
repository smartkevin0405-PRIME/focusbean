package app.bean.focus;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.*;

/**
 * Reads browser chrome and document metadata, never arbitrary page links as navigation.
 * Current Chromium (and so Samsung Internet) attaches no page URL to accessibility
 * nodes; the "url" extra is read only in case a browser build provides it.
 */
final class BrowserInspector {
 static final String TARGET="AccessibilityNodeInfo.targetUrl";
 private static final int NATIVE_LIMIT=180,DOCUMENT_SHELL_LIMIT=24,DOCUMENT_REFRESH_LIMIT=4;
 static final class Result {
  String url="",source="없음",documentUrl="",playerState="";
  boolean editing,playerShorts,playerChecked,nativeTruncated,playerTruncated,documentUnavailable,pageYouTube;
  int nativeNodes,documentNodes,playerNodes,documentRefreshes,youtubeLinks,otherLinks,pageEvidence;
  // A document location wins over a stale toolbar regardless of sibling order.
  void document(String raw){String value=BrowserAddress.clean(raw);if(UrlRules.parse(value)!=null&&!documentKnown())documentUrl=value;}
  private boolean documentKnown(){return documentLocation(documentUrl);}
  void finishDocument(){if(UrlRules.parse(documentUrl)!=null&&(documentKnown()||!BrowserAddress.hasPath(url)||!UrlRules.isYouTube(documentUrl)||!UrlRules.isYouTube(url))){url=documentUrl;source="웹 문서 루트";}}
  boolean confirmedHome(){return !playerShorts&&BrowserAddress.isDocumentHome(documentUrl);}
  boolean knownLocation(){return confirmedHome()||BrowserAddress.hasPath(url);}
  void offer(String raw,String from){String candidate=BrowserAddress.clean(raw);if(BrowserAddress.score(candidate)>BrowserAddress.score(url)){url=candidate;source=from;}}
  boolean youtube(){return UrlRules.isYouTube(url)||pageYouTube;}
  boolean addressHidden(){return !BrowserAddress.known(url);}
  // Only a host class leaves this object: 1 YouTube, -1 another site, 0 unknown.
  int hostEvidence(){return addressHidden()?pageEvidence:UrlRules.isYouTube(url)?1:-1;}
  boolean shorts(){return UrlRules.isShorts(url)||playerShorts;}
  String diagnostic(){return (confirmedHome()?"유튜브 홈 확인":addressHidden()&&pageYouTube?"주소창 숨김 · 유튜브 화면으로 판단":BrowserAddress.status(url))+(playerChecked&&addressHidden()?" · 화면 링크 유튜브 "+youtubeLinks+" / 기타 "+otherLinks:"")+"\n읽은 위치: "+source+" · 경로 "+(confirmedHome()?"루트 / 확인":BrowserAddress.hasPath(url)?"있음":"미확인")+(editing?" · 주소 입력 중":"")+(playerState.isEmpty()?"":"\n"+playerState)+"\n탐색 노드: UI "+nativeNodes+" · 문서 "+documentNodes+" · 플레이어 "+playerNodes+(nativeTruncated?"\n브라우저 UI 탐색 한도 도달":"")+(playerTruncated?"\n웹 플레이어 탐색 한도 도달":"");}
 }
 static Result read(AccessibilityNodeInfo root,String pkg){return read(root,pkg,true);}
 static Result read(AccessibilityNodeInfo root,String pkg,boolean allowPlayerFallback){
  return read(root,pkg,allowPlayerFallback,false);
 }
 static Result read(AccessibilityNodeInfo root,String pkg,boolean allowPlayerFallback,boolean navigationHint){
  return read(root,pkg,allowPlayerFallback,navigationHint,0);
 }
 /**
  * @param hostMemory host class last read from this browser window (1 YouTube, -1 other,
  *        0 unknown). It lets a hidden toolbar keep its YouTube context without storing a URL.
  */
 static Result read(AccessibilityNodeInfo root,String pkg,boolean allowPlayerFallback,boolean navigationHint,int hostMemory){
  Result result=new Result();if(root==null||pkg==null)return result;
  ArrayDeque<AccessibilityNodeInfo> queue=new ArrayDeque<>();
  List<AccessibilityNodeInfo> webRoots=new ArrayList<>();queue.add(AccessibilityNodeInfo.obtain(root));
  try{
   while(!queue.isEmpty()){
    AccessibilityNodeInfo n=queue.remove();result.nativeNodes++;
    try{
     if(!n.isVisibleToUser())continue;
     if(webRoot(n)){if(refreshDocument(n,result)&&readDocument(n,result))webRoots.add(AccessibilityNodeInfo.obtain(n));continue;}
     address(n,result);int children=n.getChildCount();
     for(int i=0;i<children;i++){
      if(result.nativeNodes+queue.size()>=NATIVE_LIMIT){result.nativeTruncated=true;break;}
      AccessibilityNodeInfo child=n.getChild(i);if(child!=null)queue.add(child);
     }
    }finally{n.recycle();}
   }
   // A complete chrome walk already saw every address candidate. Native-ID queries
   // remain a fallback when the bounded walk could have missed a toolbar/editor.
   if(result.nativeTruncated)for(String field:BrowserAddress.fieldsFor(pkg)){
    List<AccessibilityNodeInfo> found=root.findAccessibilityNodeInfosByViewId(pkg+":id/"+field);
    if(found==null)continue;
    try{for(AccessibilityNodeInfo n:found)address(n,result);}finally{for(AccessibilityNodeInfo n:found)if(n!=null)n.recycle();}
   }
   result.finishDocument();
   // Chromium exposes no page URL on accessibility nodes, and Samsung Internet hides its
   // toolbar while scrolling. With no readable address, check the page only when this
   // window was last seen on YouTube or its host is still unknown; on-page link hosts
   // must then confirm YouTube before any player layout can count.
   boolean hidden=result.addressHidden()&&hostMemory>=0;
   if(!result.editing&&(hidden||result.youtube()&&(!result.knownLocation()||navigationHint))){
    if(webRoots.isEmpty())result.playerState="웹 플레이어: 이번 탐색에서 웹 루트 없음";
    else if(!allowPlayerFallback)result.playerState="웹 플레이어: 재검사 대기";
    else{
     result.playerChecked=true;
     BrowserPlayerInspector.Result player=BrowserPlayerInspector.readRoots(webRoots);
     result.youtubeLinks=player.youtubeLinks;result.otherLinks=player.otherLinks;
     if(hidden){
      result.pageEvidence=WebShortsRules.page(player.youtubeLinks,player.otherLinks);
      result.pageYouTube=result.pageEvidence>0||(hostMemory>0&&result.pageEvidence==0);
      result.playerShorts=result.pageYouTube&&player.shorts;
     }else
     // A click is only a reason to refresh: a known home/watch URL may be
     // overridden only by a current player ID plus controls, never geometry.
     result.playerShorts=result.knownLocation()?player.strongShorts:player.shorts;
     result.playerState=player.shorts&&!result.playerShorts?(hidden?"웹 플레이어: 유튜브 화면 미확인 · 차단 안 함":"웹 플레이어: 일반 경로 유지 · 플레이어 ID 미확인")+" · 웹 노드 "+player.webNodes+" · 오른쪽 동작 "+player.controls:player.diagnostic();
     result.playerNodes=player.nodes;result.playerTruncated=player.truncated;
    }
   }
   return result;
  }finally{while(!queue.isEmpty())queue.remove().recycle();for(AccessibilityNodeInfo n:webRoots)n.recycle();}
 }
 static boolean webRoot(AccessibilityNodeInfo n){return "android.webkit.WebView".contentEquals(string(n.getClassName()))||documentRole(n);}
 static boolean onScreen(AccessibilityNodeInfo n){return n.isVisibleToUser()&&!n.getExtras().getBoolean("AccessibilityNodeInfo.offscreen",false);}
 private static boolean documentLocation(String raw){java.net.URI u=UrlRules.parse(raw);return u!=null&&u.getPath()!=null&&!u.getPath().isEmpty();}
 static boolean documentRole(AccessibilityNodeInfo n){return "rootWebArea".equals(string(n.getExtras().getCharSequence("AccessibilityNodeInfo.chromeRole")));}
 private static boolean refreshDocument(AccessibilityNodeInfo n,Result result){
  // Re-query only document roots, not every video/control node. This bypasses
  // stale Android snapshots after a page transition; provider caches may remain.
  if(result.documentRefreshes>=DOCUMENT_REFRESH_LIMIT){result.documentUnavailable=true;return false;}
  result.documentRefreshes++;if(!n.refresh()||!onScreen(n)){result.documentUnavailable=true;return false;}return true;
 }
 private static boolean nativeWrapper(AccessibilityNodeInfo n){
  if(!string(n.getExtras().getCharSequence("AccessibilityNodeInfo.chromeRole")).isEmpty()||!string(n.getExtras().getCharSequence(TARGET)).isEmpty())return false;
  String id=n.getViewIdResourceName(),cls=string(n.getClassName());
  return (id!=null&&id.contains(":id/"))||"android.webkit.WebView".equals(cls)||"android.widget.FrameLayout".equals(cls)||"android.widget.LinearLayout".equals(cls)||"android.view.ViewGroup".equals(cls);
 }
 private static boolean readDocument(AccessibilityNodeInfo root,Result result){
  if(result.documentKnown())return true;
  String raw=string(root.getExtras().getCharSequence("url"));result.document(raw);
  if(documentRole(root)||documentLocation(raw))return true;
  // A native WebView can wrap a virtual rootWebArea. Read only its shallow shell;
  // stop at the first document root so an iframe can never override its URL.
  ArrayDeque<AccessibilityNodeInfo> queue=new ArrayDeque<>();ArrayDeque<Integer> depths=new ArrayDeque<>();
  queue.add(AccessibilityNodeInfo.obtain(root));depths.add(0);
  try{while(!queue.isEmpty()&&result.documentNodes<DOCUMENT_SHELL_LIMIT){
   AccessibilityNodeInfo n=queue.remove();int depth=depths.remove();result.documentNodes++;
   try{
    if(!onScreen(n))continue;
    if(depth>0&&webRoot(n)){
     if(!refreshDocument(n,result))return false;
     String childUrl=string(n.getExtras().getCharSequence("url"));result.document(childUrl);
     if(documentRole(n)||documentLocation(childUrl))return true;
    }
    // Only native wrapper nodes may lead to the document root. A page element or
    // iframe below an unlabelled web node is not evidence of the current page URL.
    if(depth>0&&!nativeWrapper(n))continue;
    if(depth>=4)continue;int children=n.getChildCount();
    for(int i=0;i<children&&result.documentNodes+queue.size()<DOCUMENT_SHELL_LIMIT;i++){
     AccessibilityNodeInfo child=n.getChild(i);if(child!=null){queue.add(child);depths.add(depth+1);}
    }
   }finally{n.recycle();}
  }}finally{while(!queue.isEmpty())queue.remove().recycle();}return true;
 }
 private static void address(AccessibilityNodeInfo n,Result r){
  if(n==null||!n.isVisibleToUser()||!BrowserAddress.field(n.getViewIdResourceName()))return;
  if(n.isEditable()&&n.isFocused()){r.editing=true;return;}
  // Both values and all address candidates compete; domain text cannot hide a full description.
  String id=n.getViewIdResourceName();
  r.offer(string(n.getText()),id+" · text");r.offer(string(n.getContentDescription()),id+" · description");
 }
 static boolean clickedShorts(AccessibilityNodeInfo source,boolean youtubeContext){
  AccessibilityNodeInfo n=source==null?null:AccessibilityNodeInfo.obtain(source);
  try{for(int depth=0;n!=null&&depth<8;depth++){
   if(UrlRules.isShorts(string(n.getExtras().getCharSequence(TARGET))))return true;
   String id=n.getViewIdResourceName();
   // A plain HTML ID is a web control; a native package:id/shorts-tab is not.
   if(youtubeContext&&"shorts-tab".equals(id))return true;
   AccessibilityNodeInfo parent=n.getParent();n.recycle();n=parent;
  }}finally{if(n!=null)n.recycle();}return false;
 }
 private static String string(CharSequence value){return value==null?"":value.toString();}
}
