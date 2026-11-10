package app.bean.focus;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.*;

/**
 * Samsung Internet's ad blocker list. During a focus lock BeanFocus's switch there must
 * stay on, so the guard leaves the screen while that switch shows as on. A switch that is
 * already off is left alone, so the user can still turn it on during a lock.
 */
final class BlockerGuard {
 static final int NONE=0,OFF=1,ON=2;
 private static final int ROW_LEVELS=4,ROW_NODES=24;
 private BlockerGuard(){}
 static boolean surface(String pkg){return pkg!=null&&DeviceConstants.SAMSUNG_BROWSERS.contains(pkg);}

 /** State of the switch in the native row labelled exactly with the app name; NONE without one. */
 static int ownSwitch(AccessibilityNodeInfo root){
  if(root==null)return NONE;
  List<AccessibilityNodeInfo> found=root.findAccessibilityNodeInfosByText(DeviceConstants.APP_LABEL);if(found==null)return NONE;
  int result=NONE;
  try{for(AccessibilityNodeInfo n:found){
   if(n==null||!n.isVisibleToUser()||n.isEditable()||web(n)||!DeviceConstants.APP_LABEL.equals(trim(n.getText())))continue;
   result=Math.max(result,rowSwitch(n));
  }}finally{for(AccessibilityNodeInfo n:found)if(n!=null)n.recycle();}
  return result;
 }
 // Climb from the label to the first ancestor holding a switch. If that ancestor holds more
 // than one switch it is the whole list, not this row, so the answer is unknown.
 private static int rowSwitch(AccessibilityNodeInfo label){
  AccessibilityNodeInfo cur=AccessibilityNodeInfo.obtain(label);
  try{for(int level=0;cur!=null&&level<ROW_LEVELS;level++){
   if(web(cur))return NONE;
   int[] found=switches(cur);if(found[0]>1)return NONE;if(found[0]==1)return found[1]==1?ON:OFF;
   AccessibilityNodeInfo parent=cur.getParent();cur.recycle();cur=parent;
  }}finally{if(cur!=null)cur.recycle();}
  return NONE;
 }
 /** {switch count, 1 if the last one is checked} within a small subtree. */
 private static int[] switches(AccessibilityNodeInfo top){
  int[] out={0,0};ArrayDeque<AccessibilityNodeInfo> q=new ArrayDeque<>();q.add(AccessibilityNodeInfo.obtain(top));int seen=0;
  try{while(!q.isEmpty()&&seen++<ROW_NODES){AccessibilityNodeInfo n=q.remove();try{
   if(!n.isVisibleToUser())continue;
   if(toggle(n)){out[0]++;out[1]=n.isChecked()?1:0;if(out[0]>1)return out;continue;}
   for(int i=0;i<n.getChildCount()&&q.size()<ROW_NODES;i++){AccessibilityNodeInfo child=n.getChild(i);if(child!=null)q.add(child);}
  }finally{n.recycle();}}}finally{while(!q.isEmpty())q.remove().recycle();}
  return out;
 }
 private static boolean toggle(AccessibilityNodeInfo n){
  String cls=n.getClassName()==null?"":n.getClassName().toString(),id=n.getViewIdResourceName();
  return n.isCheckable()||cls.endsWith("Switch")||cls.endsWith("SwitchCompat")||cls.endsWith("CheckBox")||(id!=null&&DeviceConstants.SWITCH_IDS.contains(id.substring(id.lastIndexOf('/')+1)));
 }
 // A page element is never the browser's own settings row.
 private static boolean web(AccessibilityNodeInfo n){return BrowserInspector.webRoot(n)||n.getExtras().getCharSequence(BrowserInspector.ROLE)!=null;}
 private static String trim(CharSequence s){return s==null?"":s.toString().trim();}
}
