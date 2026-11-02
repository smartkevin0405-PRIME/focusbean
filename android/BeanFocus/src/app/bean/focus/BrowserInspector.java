package app.bean.focus;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.*;

/** Reads toolbar candidates and document-root metadata; never treats an unclicked feed link as navigation. */
final class BrowserInspector {
 static final String TARGET="AccessibilityNodeInfo.targetUrl";
 static final class Result {
  String url="",source="없음",documentUrl="",playerState=""; boolean editing,playerShorts;
  // Document-root URLs preserve evidence of an explicit '/', unlike a shortened omnibox.
  void document(String raw){String value=BrowserAddress.clean(raw);if(UrlRules.parse(value)!=null){documentUrl=value;if(BrowserAddress.hasPath(value)||BrowserAddress.isDocumentHome(value)) {url=value;source="웹 문서 루트";}}}
  boolean confirmedHome(){return BrowserAddress.isDocumentHome(documentUrl);}
  boolean knownLocation(){return confirmedHome()||BrowserAddress.hasPath(url);}
  void offer(String raw,String from){String candidate=BrowserAddress.clean(raw);if(BrowserAddress.score(candidate)>BrowserAddress.score(url)){url=candidate;source=from;}}
  boolean youtube(){return UrlRules.isYouTube(url);}
  boolean shorts(){return UrlRules.isShorts(url)||playerShorts;}
  String diagnostic(){return (confirmedHome()?"유튜브 홈 확인":BrowserAddress.status(url))+"\n읽은 위치: "+source+" · 경로 "+(confirmedHome()?"루트 / 확인":BrowserAddress.hasPath(url)?"있음":"미확인")+(editing?" · 주소 입력 중":"")+(playerState.isEmpty()?"":"\n"+playerState);}
 }
 static Result read(AccessibilityNodeInfo root,String pkg){
  Result result=new Result();
  // Direct native-ID queries are independent of the fallback tree budget.
  for(String field:BrowserAddress.fieldsFor(pkg)){
   List<AccessibilityNodeInfo> found=root.findAccessibilityNodeInfosByViewId(pkg+":id/"+field);
   if(found==null)continue;
   try{for(AccessibilityNodeInfo n:found)address(n,result);}finally{for(AccessibilityNodeInfo n:found)if(n!=null)n.recycle();}
  }
  ArrayDeque<AccessibilityNodeInfo> queue=new ArrayDeque<>();queue.add(AccessibilityNodeInfo.obtain(root));int count=0;
  try{while(!queue.isEmpty()&&count++<180){AccessibilityNodeInfo n=queue.remove();try{
   if(!n.isVisibleToUser())continue;
   address(n,result);
   String role=string(n.getExtras().getCharSequence("AccessibilityNodeInfo.chromeRole"));
   boolean web="android.webkit.WebView".contentEquals(string(n.getClassName()))||"rootWebArea".equals(role);
   if(web){ // Only a document root's URL is a current-page signal, never a link target.
    result.document(string(n.getExtras().getCharSequence("url")));continue;
   }
   for(int i=0;i<n.getChildCount()&&queue.size()<180;i++){AccessibilityNodeInfo child=n.getChild(i);if(child!=null)queue.add(child);}
  }finally{n.recycle();}}}finally{while(!queue.isEmpty())queue.remove().recycle();}
  if(result.youtube()&&!result.editing&&!result.knownLocation()){BrowserPlayerInspector.Result player=BrowserPlayerInspector.read(root);result.playerShorts=player.shorts;result.playerState=player.diagnostic();}
  return result;
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
   if(youtubeContext&&("shorts-tab".equals(id)||"shorts-tab".equals(id==null?"":id.substring(id.lastIndexOf('/')+1))))return true;
   AccessibilityNodeInfo parent=n.getParent();n.recycle();n=parent;
  }}finally{if(n!=null)n.recycle();}return false;
 }
 private static String string(CharSequence value){return value==null?"":value.toString();}
}
