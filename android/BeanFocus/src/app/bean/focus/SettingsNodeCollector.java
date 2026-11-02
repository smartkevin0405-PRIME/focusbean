package app.bean.focus;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.*;
final class SettingsNodeCollector {
 static List<SettingsGuard.Node> collect(AccessibilityNodeInfo root){List<SettingsGuard.Node> out=new ArrayList<>();ArrayDeque<AccessibilityNodeInfo> q=new ArrayDeque<>();q.add(AccessibilityNodeInfo.obtain(root));int visits=0;
  try{while(!q.isEmpty()&&out.size()<200&&visits++<400){AccessibilityNodeInfo n=q.remove();try{if(!n.isVisibleToUser())continue;
   out.add(new SettingsGuard.Node(string(n.getText()),string(n.getContentDescription()),n.getViewIdResourceName(),string(n.getClassName()),n.isCheckable(),n.isEditable()));
   for(int i=0;i<n.getChildCount()&&q.size()<200;i++){AccessibilityNodeInfo child=n.getChild(i);if(child!=null)q.add(child);}
  }finally{n.recycle();}}}finally{while(!q.isEmpty())q.remove().recycle();}return out;
 }
 private static String string(CharSequence s){return s==null?"":s.toString();}
}
