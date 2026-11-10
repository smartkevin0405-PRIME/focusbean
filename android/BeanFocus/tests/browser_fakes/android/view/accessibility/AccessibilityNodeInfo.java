package android.view.accessibility;
import android.os.Bundle;
import java.util.*;
/** Small shared-data / owned-handle fake; detects leaked and recycled node handles. */
public class AccessibilityNodeInfo {
 private static final class Data {String pkg="com.google.android.youtube";String id,text,description,cls="android.view.View",refreshUrl,refreshCls;List<Data> refreshChildren=new ArrayList<>();boolean visible=true,editable,focused,clickable,checked,refreshable=true;int left,top,right,bottom,childReads,refreshes;Bundle extras=new Bundle();Data parent;List<Data> children=new ArrayList<>();}
 private final Data data;private boolean recycled;public static int handles,queries,childReads,queryNodes;
 public static void resetCounts(){queries=0;childReads=0;queryNodes=0;}
 public int childReads(){check();return data.childReads;}
 public AccessibilityNodeInfo(){data=new Data();handles++;}private AccessibilityNodeInfo(Data d){data=d;handles++;}
 private void check(){if(recycled)throw new IllegalStateException("recycled node used");}
 public static AccessibilityNodeInfo obtain(AccessibilityNodeInfo n){n.check();return new AccessibilityNodeInfo(n.data);}
 public void recycle(){check();recycled=true;handles--;}
 public AccessibilityNodeInfo id(String v){data.id=v;return this;}public AccessibilityNodeInfo text(String v){data.text=v;return this;}public AccessibilityNodeInfo description(String v){data.description=v;return this;}public AccessibilityNodeInfo cls(String v){data.cls=v;return this;}
 public AccessibilityNodeInfo visible(boolean v){data.visible=v;return this;}public AccessibilityNodeInfo editing(){data.editable=true;data.focused=true;return this;}
 public AccessibilityNodeInfo refreshedUrl(String v){data.refreshUrl=v;return this;}public AccessibilityNodeInfo refreshable(boolean v){data.refreshable=v;return this;}
 public int refreshes(){check();return data.refreshes;}public boolean refresh(){check();data.refreshes++;if(data.refreshUrl!=null)data.extras.putCharSequence("url",data.refreshUrl);if(data.refreshable){if(data.refreshCls!=null)data.cls=data.refreshCls;for(Data c:data.refreshChildren){data.children.add(c);c.parent=data;}data.refreshChildren.clear();}return data.refreshable;}
 /** Simulates a stale cached view that becomes a Chromium page once re-read. */
 public AccessibilityNodeInfo refreshedClass(String v){data.refreshCls=v;return this;}public AccessibilityNodeInfo refreshedChild(AccessibilityNodeInfo n){data.refreshChildren.add(n.data);return this;}
 public AccessibilityNodeInfo child(AccessibilityNodeInfo n){data.children.add(n.data);n.data.parent=data;return this;}
 public AccessibilityNodeInfo pkg(String v){data.pkg=v;return this;}
 public CharSequence getPackageName(){check();return data.pkg;}
 public Bundle getExtras(){check();return data.extras;}public String getViewIdResourceName(){check();return data.id;}public CharSequence getText(){check();return data.text;}public CharSequence getContentDescription(){check();return data.description;}public CharSequence getClassName(){check();return data.cls;}
 public boolean isCheckable(){check();return data.cls.endsWith("Switch")||data.cls.endsWith("CheckBox");}
 public AccessibilityNodeInfo bounds(int l,int t,int r,int b){data.left=l;data.top=t;data.right=r;data.bottom=b;return this;}
 public AccessibilityNodeInfo clickable(boolean v){data.clickable=v;return this;}
 public boolean isClickable(){check();return data.clickable;}
 public AccessibilityNodeInfo checked(boolean v){data.checked=v;return this;}public boolean isChecked(){check();return data.checked;}
 /** Like Android: case-insensitive "contains" over text and content description. */
 public List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText(String s){check();queries++;List<AccessibilityNodeInfo> out=new ArrayList<>();findText(data,s.toLowerCase(Locale.ROOT),out);return out;}
 private static void findText(Data d,String s,List<AccessibilityNodeInfo> out){queryNodes++;if((d.text!=null&&d.text.toLowerCase(Locale.ROOT).contains(s))||(d.description!=null&&d.description.toLowerCase(Locale.ROOT).contains(s)))out.add(new AccessibilityNodeInfo(d));for(Data c:d.children)findText(c,s,out);}
 public void getBoundsInScreen(android.graphics.Rect r){check();r.left=data.left;r.top=data.top;r.right=data.right;r.bottom=data.bottom;}
 public boolean isVisibleToUser(){check();return data.visible;}public boolean isEditable(){check();return data.editable;}public boolean isFocused(){check();return data.focused;}
 public int getChildCount(){check();return data.children.size();}public AccessibilityNodeInfo getChild(int i){check();childReads++;data.childReads++;return new AccessibilityNodeInfo(data.children.get(i));}public AccessibilityNodeInfo getParent(){check();return data.parent==null?null:new AccessibilityNodeInfo(data.parent);}
 public List<AccessibilityNodeInfo> findAccessibilityNodeInfosByViewId(String id){check();queries++;List<AccessibilityNodeInfo> out=new ArrayList<>();find(data,id,out);return out;}
 private static void find(Data d,String id,List<AccessibilityNodeInfo> out){queryNodes++;if(id.equals(d.id))out.add(new AccessibilityNodeInfo(d));for(Data c:d.children)find(c,id,out);}
}
