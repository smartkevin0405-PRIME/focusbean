package app.bean.focus;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.*;
/** Explicit, in-memory diagnostics. Never includes text, descriptions, URLs or node extras. */
final class ViewIdSnapshot {
 static String collect(AccessibilityNodeInfo root){
  Set<String> ids=new LinkedHashSet<>();ArrayDeque<AccessibilityNodeInfo> q=new ArrayDeque<>();q.add(AccessibilityNodeInfo.obtain(root));int count=0;
  try{while(!q.isEmpty()&&count++<600){AccessibilityNodeInfo n=q.remove();try{if(!n.isVisibleToUser())continue;
   String id=n.getViewIdResourceName();if(id!=null&&id.matches("[A-Za-z0-9_.]+:id/[A-Za-z0-9_]+")&&ids.size()<120)ids.add(id);
   else if(DeviceConstants.WEB_PLAYER_IDS.contains(id)&&ids.size()<120)ids.add("web:"+id);
   for(int i=0;i<n.getChildCount()&&q.size()<600;i++){AccessibilityNodeInfo child=n.getChild(i);if(child!=null)q.add(child);}
  }finally{n.recycle();}}}finally{while(!q.isEmpty())q.remove().recycle();}
  StringBuilder out=new StringBuilder();for(String id:ids)out.append(id).append('\n');return out.length()==0?"표시된 native view ID가 없어요":out.toString();
 }
}
