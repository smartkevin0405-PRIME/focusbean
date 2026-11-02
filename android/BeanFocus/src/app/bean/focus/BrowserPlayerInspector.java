package app.bean.focus;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.*;
/** Ambiguous YouTube page fallback. Reads visible WEB descendants, never other apps or page titles. */
final class BrowserPlayerInspector {
 static final class Result {boolean shorts;int nodes,webNodes,controls;String diagnostic(){return "웹 플레이어: "+(shorts?"쇼츠 버튼 배치 감지":"미확인")+" · 웹 노드 "+webNodes+" · 오른쪽 동작 "+controls;}}
 private static final class Frame {final Rect bounds=new Rect();boolean marker,owner;final List<WebShortsRules.Control> controls=new ArrayList<>();}
 private static final class Entry {final AccessibilityNodeInfo node;final Frame frame;Entry(AccessibilityNodeInfo n,Frame f){node=n;frame=f;}}
 static Result read(AccessibilityNodeInfo root){
  Result result=new Result();List<Frame> frames=new ArrayList<>();ArrayDeque<Entry> q=new ArrayDeque<>();q.add(new Entry(AccessibilityNodeInfo.obtain(root),null));
  try{while(!q.isEmpty()&&result.nodes++<600){Entry item=q.remove();AccessibilityNodeInfo n=item.node;try{
   if(!n.isVisibleToUser())continue;Frame frame=item.frame;
   if(frame==null&&("android.webkit.WebView".contentEquals(string(n.getClassName()))||"rootWebArea".equals(string(n.getExtras().getCharSequence("AccessibilityNodeInfo.chromeRole"))))){frame=new Frame();n.getBoundsInScreen(frame.bounds);frames.add(frame);}
   if(frame!=null){result.webNodes++;observe(n,frame);}
   for(int i=0;i<n.getChildCount()&&q.size()<600;i++){AccessibilityNodeInfo child=n.getChild(i);if(child!=null)q.add(new Entry(child,frame));}
  }finally{n.recycle();}}}finally{while(!q.isEmpty())q.remove().node.recycle();}
  for(Frame f:frames){result.controls+=f.controls.size();if(WebShortsRules.player(f.bounds.height()>f.bounds.width()*1.05f,f.marker,f.owner,f.controls))result.shorts=true;}return result;
 }
 private static void observe(AccessibilityNodeInfo n,Frame f){
  if(n.isEditable()||f.bounds.width()<=0||f.bounds.height()<=0)return;Rect b=new Rect();n.getBoundsInScreen(b);
  if(b.isEmpty()||b.right<=f.bounds.left||b.left>=f.bounds.right||b.bottom<=f.bounds.top||b.top>=f.bounds.bottom)return;
  float x=(b.centerX()-f.bounds.left)/(float)f.bounds.width(),y=(b.centerY()-f.bounds.top)/(float)f.bounds.height();
  String id=n.getViewIdResourceName();if(DeviceConstants.WEB_PLAYER_IDS.contains(id)&&b.width()>=f.bounds.width()*.4f&&b.height()>=f.bounds.height()*.4f)f.marker=true;
  String text=string(n.getText()),description=string(n.getContentDescription());
  if(y>.50f&&b.height()<f.bounds.height()*.16f&&(WebShortsRules.subscribe(text)||WebShortsRules.subscribe(description)||text.matches("@[\\p{L}\\p{N}_.-]{1,100}")||description.matches("@[\\p{L}\\p{N}_.-]{1,100}")))f.owner=true;
  boolean button=n.isClickable()||string(n.getClassName()).endsWith("Button")||"button".equals(string(n.getExtras().getCharSequence("AccessibilityNodeInfo.chromeRole")));
  if(!button||x<.72f||x>1f||y<.15f||y>.97f||b.width()>f.bounds.width()*.28f||b.height()>f.bounds.height()*.18f||f.controls.size()>=40)return;
  int kind=WebShortsRules.control(description);if(kind==0)kind=WebShortsRules.control(text);if(kind!=0)f.controls.add(new WebShortsRules.Control(kind,x,y));
 }
 private static String string(CharSequence c){return c==null?"":c.toString();}
}
