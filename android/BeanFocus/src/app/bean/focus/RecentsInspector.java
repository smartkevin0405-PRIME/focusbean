package app.bean.focus;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.*;
final class RecentsInspector {
 static boolean host(String p){return DeviceConstants.RECENTS_HOSTS.contains(p);}
 private static final String[] IDS=DeviceConstants.RECENTS_IDS;
 static boolean visible(AccessibilityNodeInfo root,String p){if(!host(p))return false;
  for(String id:IDS){List<AccessibilityNodeInfo> nodes=root.findAccessibilityNodeInfosByViewId(p+":id/"+id);if(nodes==null)continue;try{for(AccessibilityNodeInfo n:nodes)if(n!=null&&n.isVisibleToUser())return true;}finally{for(AccessibilityNodeInfo n:nodes)if(n!=null)n.recycle();}}
  return String.valueOf(root.getClassName()).equals(DeviceConstants.RECENTS_CLASS);
 }
}
