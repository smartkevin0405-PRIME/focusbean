package app.bean.focus;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.*;

/**
 * Explicit, in-memory structure diagnostics of the foreground browser window, used to see
 * which nodes a browser actually exposes. One line per node: class name, native view ID,
 * a fixed set of Chromium role tokens, flags, child count, link host class (YouTube or
 * other) and rounded geometry. Never includes text, descriptions, URLs, titles, HTML IDs
 * or any other extra.
 */
final class StructureSnapshot {
 static final int NODE_LIMIT=300;
 private static final Set<String> ROLES=DeviceConstants.set("rootWebArea","iframe","link","button","image","video","heading","list","listItem","paragraph","staticText","genericContainer","textField","main","navigation","banner","article","region","group","dialog","tab","tabList","menu","menuItem","webView");
 private StructureSnapshot(){}

 static String collect(AccessibilityNodeInfo root){
  if(root==null)return "";
  Rect screen=new Rect();root.getBoundsInScreen(screen);
  ArrayDeque<AccessibilityNodeInfo> queue=new ArrayDeque<>();ArrayDeque<Integer> depths=new ArrayDeque<>();
  queue.add(AccessibilityNodeInfo.obtain(root));depths.add(0);
  StringBuilder lines=new StringBuilder();
  int nodes=0,candidates=0,refreshed=0,failed=0,hidden=0,youtube=0,other=0;boolean truncated=false;
  try{while(!queue.isEmpty()){
   AccessibilityNodeInfo n=queue.remove();int depth=depths.remove();
   try{
    if(nodes>=NODE_LIMIT){truncated=true;continue;}
    nodes++;
    String refresh="";
    if(BrowserInspector.webRoot(n)){
     candidates++;
     if(n.refresh()){refreshed++;refresh=" refresh=ok";}else{failed++;refresh=" refresh=FAIL";}
     if(!BrowserInspector.onScreen(n)){hidden++;refresh+=" hidden";}
    }
    int origin=WebShortsRules.origin(string(n.getExtras().getCharSequence(BrowserInspector.TARGET)));
    if(origin>0)youtube++;else if(origin<0)other++;
    boolean visible=n.isVisibleToUser();
    lines.append(indent(depth)).append(shortClass(n.getClassName())).append(" id=").append(viewId(n.getViewIdResourceName()))
     .append(" role=").append(role(string(n.getExtras().getCharSequence("AccessibilityNodeInfo.chromeRole"))))
     .append(" kids=").append(n.getChildCount()).append(" flags=").append(flags(n,visible)).append(" link=").append(origin>0?"Y":origin<0?"O":"-")
     .append(" box=").append(box(n,screen)).append(refresh).append('\n');
    if(!visible)continue;
    int children=n.getChildCount();
    for(int i=0;i<children&&queue.size()<NODE_LIMIT;i++){AccessibilityNodeInfo child=n.getChild(i);if(child!=null){queue.add(child);depths.add(depth+1);}}
   }finally{n.recycle();}
  }}finally{while(!queue.isEmpty())queue.remove().recycle();}
  return "노드 "+nodes+(truncated?"+ (한도 "+NODE_LIMIT+")":"")+" · 웹 루트 후보 "+candidates+" (갱신 성공 "+refreshed+" · 실패 "+failed+" · 화면 밖 "+hidden+") · 링크 출처 유튜브 "+youtube+" / 기타 "+other+"\n"
   +"형식: 클래스 id 역할 자식수 플래그(V보임 O화면밖 C클릭 E편집 F포커스) 링크출처 위치(x,y,너비,높이 %)\n"+lines;
 }
 private static String indent(int depth){StringBuilder out=new StringBuilder();for(int i=0;i<Math.min(depth,12);i++)out.append(' ');return out.toString();}
 private static String shortClass(CharSequence raw){
  String cls=string(raw);cls=cls.substring(cls.lastIndexOf('.')+1);
  return cls.matches("[A-Za-z0-9_$]{1,40}")?cls:cls.isEmpty()?"-":"other";
 }
 // Native IDs are app-defined names; anything else (HTML IDs) is only marked as present.
 private static String viewId(String id){
  if(id==null||id.isEmpty())return "-";
  if(id.matches("[A-Za-z0-9_.]+:id/[A-Za-z0-9_]+"))return id.substring(id.indexOf(":id/")+4);
  if(DeviceConstants.WEB_PLAYER_IDS.contains(id))return "web:"+id;
  return "html";
 }
 private static String role(String raw){return raw.isEmpty()?"-":ROLES.contains(raw)?raw:"other";}
 private static String flags(AccessibilityNodeInfo n,boolean visible){
  return (visible?"V":"")+(n.getExtras().getBoolean("AccessibilityNodeInfo.offscreen",false)?"O":"")+(n.isClickable()?"C":"")+(n.isEditable()?"E":"")+(n.isFocused()?"F":"");
 }
 private static String box(AccessibilityNodeInfo n,Rect screen){
  Rect b=new Rect();n.getBoundsInScreen(b);int w=Math.max(1,screen.width()),h=Math.max(1,screen.height());
  return pct(b.left-screen.left,w)+","+pct(b.top-screen.top,h)+","+pct(b.width(),w)+","+pct(b.height(),h);
 }
 private static int pct(int value,int total){return Math.max(-999,Math.min(999,Math.round(value*100f/total)));}
 private static String string(CharSequence value){return value==null?"":value.toString();}
}
