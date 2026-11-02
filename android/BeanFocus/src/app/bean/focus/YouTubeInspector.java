package app.bean.focus;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;
final class YouTubeInspector {
 static final String PACKAGE=DeviceConstants.YOUTUBE.iterator().next();
 static boolean isPackage(String p){return DeviceConstants.YOUTUBE.contains(p);}
 static final String[] PLAYERS=DeviceConstants.PLAYERS;
 static String player(AccessibilityNodeInfo root){
  String pkg=String.valueOf(root.getPackageName());if(!isPackage(pkg))return "";
  for(String id:PLAYERS){List<AccessibilityNodeInfo> nodes=root.findAccessibilityNodeInfosByViewId(pkg+":id/"+id);if(nodes==null)continue;
   try{for(AccessibilityNodeInfo n:nodes)if(n!=null&&n.isVisibleToUser())return id;}finally{for(AccessibilityNodeInfo n:nodes)if(n!=null)n.recycle();}
  }return "";
 }
}
