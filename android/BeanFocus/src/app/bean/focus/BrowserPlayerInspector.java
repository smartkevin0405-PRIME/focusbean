package app.bean.focus;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.*;
import java.util.regex.Pattern;
/** Ambiguous YouTube page fallback. Reads visible WEB descendants, never other apps or page titles. */
final class BrowserPlayerInspector {
 private static final int NODE_LIMIT=600;
 private static final Pattern OWNER=Pattern.compile("@[\\p{L}\\p{N}_.-]{1,100}");
 static final class Result {boolean shorts,strongShorts,truncated;int nodes,webNodes,controls,youtubeLinks,otherLinks;String diagnostic(){return "웹 플레이어: "+(strongShorts?"쇼츠 플레이어 ID·동작 확인":shorts?"쇼츠 버튼 배치 감지":"미확인")+" · 웹 노드 "+webNodes+" · 오른쪽 동작 "+controls;}}
 private static final class Frame {final Rect bounds=new Rect();boolean marker,owner,document;int youtubeLinks,otherLinks;final List<WebShortsRules.Control> controls=new ArrayList<>();}
 private static final class Entry {final AccessibilityNodeInfo node;final Frame frame;Entry(AccessibilityNodeInfo n,Frame f){node=n;frame=f;}}
 static Result read(AccessibilityNodeInfo root){
  if(root==null)return new Result();
  ArrayDeque<Entry> q=new ArrayDeque<>();q.add(new Entry(AccessibilityNodeInfo.obtain(root),null));return scan(q);
 }
 /** Reuse web roots found by BrowserInspector instead of walking native chrome twice. */
 static Result readRoots(List<AccessibilityNodeInfo> roots){
  ArrayDeque<Entry> q=new ArrayDeque<>();
  for(AccessibilityNodeInfo root:roots)if(root!=null)q.add(new Entry(AccessibilityNodeInfo.obtain(root),null));
  return scan(q);
 }
 private static Result scan(ArrayDeque<Entry> q){
  Result result=new Result();List<Frame> frames=new ArrayList<>();
  try{while(!q.isEmpty()&&result.nodes<NODE_LIMIT){Entry item=q.remove();AccessibilityNodeInfo n=item.node;result.nodes++;try{
   if(!BrowserInspector.onScreen(n))continue;Frame frame=item.frame;
   // A native wrapper may lead to its first document, but an iframe's controls
   // never prove which player the top-level YouTube page is currently showing.
   if(BrowserInspector.webRoot(n)){
    if(frame!=null&&frame.document)continue;
    String location=string(n.getExtras().getCharSequence("url"));
    if(UrlRules.parse(location)!=null&&!UrlRules.isYouTube(location))continue;
    frame=new Frame();frame.document=BrowserInspector.documentRole(n)||BrowserAddress.hasPath(location)||BrowserAddress.isDocumentHome(location);
    n.getBoundsInScreen(frame.bounds);frames.add(frame);
   }
   if(frame!=null){result.webNodes++;observe(n,frame);}
   int children=n.getChildCount();for(int i=0;i<children;i++){
    if(result.nodes+q.size()>=NODE_LIMIT){result.truncated=true;break;}
    AccessibilityNodeInfo child=n.getChild(i);if(child!=null)q.add(new Entry(child,frame));
   }
  }finally{n.recycle();}}if(!q.isEmpty())result.truncated=true;}finally{while(!q.isEmpty())q.remove().node.recycle();}
  for(Frame f:frames){result.controls+=f.controls.size();result.youtubeLinks+=f.youtubeLinks;result.otherLinks+=f.otherLinks;if(WebShortsRules.player(f.bounds.height()>f.bounds.width()*1.05f,f.marker,f.owner,f.controls)){result.shorts=true;if(f.marker)result.strongShorts=true;}}return result;
 }
 private static void observe(AccessibilityNodeInfo n,Frame f){
  if(n.isEditable()||f.bounds.width()<=0||f.bounds.height()<=0)return;Rect b=new Rect();n.getBoundsInScreen(b);
  if(b.isEmpty()||b.right<=f.bounds.left||b.left>=f.bounds.right||b.bottom<=f.bounds.top||b.top>=f.bounds.bottom)return;
  // Chromium does not attach the page address to accessibility nodes, but links and
  // media carry absolute target URLs. Only on-screen host classes are counted.
  int origin=WebShortsRules.origin(string(n.getExtras().getCharSequence(BrowserInspector.TARGET)));
  if(origin>0&&f.youtubeLinks<100)f.youtubeLinks++;else if(origin<0&&f.otherLinks<100)f.otherLinks++;
  float x=(b.centerX()-f.bounds.left)/(float)f.bounds.width(),y=(b.centerY()-f.bounds.top)/(float)f.bounds.height();
  String id=n.getViewIdResourceName();if(DeviceConstants.WEB_PLAYER_IDS.contains(id)&&b.width()>=f.bounds.width()*.4f&&b.height()>=f.bounds.height()*.4f)f.marker=true;
  String text=string(n.getText()),description=string(n.getContentDescription());
  if(y>.50f&&b.height()<f.bounds.height()*.16f&&(WebShortsRules.subscribe(text)||WebShortsRules.subscribe(description)||OWNER.matcher(text).matches()||OWNER.matcher(description).matches()))f.owner=true;
  boolean button=n.isClickable()||string(n.getClassName()).endsWith("Button")||"button".equals(string(n.getExtras().getCharSequence("AccessibilityNodeInfo.chromeRole")));
  if(!button||x<.72f||x>1f||y<.15f||y>.97f||b.width()>f.bounds.width()*.28f||b.height()>f.bounds.height()*.18f||f.controls.size()>=40)return;
  int kind=WebShortsRules.control(description);if(kind==0)kind=WebShortsRules.control(text);if(kind!=0)f.controls.add(new WebShortsRules.Control(kind,x,y));
 }
 private static String string(CharSequence c){return c==null?"":c.toString();}
}
