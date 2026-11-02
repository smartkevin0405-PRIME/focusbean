package app.bean.focus;
import android.view.accessibility.AccessibilityNodeInfo;
public class YouTubeSettingsTest {
 static int count;static void ok(boolean v){count++;if(!v)throw new AssertionError("case "+count);}static AccessibilityNodeInfo node(){return new AccessibilityNodeInfo();}
 static AccessibilityNodeInfo child(AccessibilityNodeInfo r,String id,String text){AccessibilityNodeInfo n=node().id(id).text(text);r.child(n);return n;}
 public static void main(String[] args){
  AccessibilityNodeInfo root=node();AccessibilityNodeInfo shelf=child(root,"com.google.android.youtube:id/shorts_shelf","Shorts");ok(YouTubeInspector.player(root).isEmpty());shelf.recycle();
  for(String id:YouTubeInspector.PLAYERS){AccessibilityNodeInfo player=child(root,YouTubeInspector.PACKAGE+":id/"+id,"");ok(!YouTubeInspector.player(root).isEmpty());player.visible(false);ok(YouTubeInspector.player(root).isEmpty());player.recycle();}
  AccessibilityNodeInfo player=child(root,YouTubeInspector.PACKAGE+":id/reel_recycler","private video title https://secret.example");String dump=ViewIdSnapshot.collect(root);ok(dump.contains("reel_recycler"));ok(!dump.contains("private video"));ok(!dump.contains("secret"));player.recycle();root.recycle();
  // General app/accessibility lists mentioning the app must remain usable.
  root=node();AccessibilityNodeInfo title=child(root,"android:id/title","집중콩");ok(SettingsGuard.reason(root,"com.android.settings",true).isEmpty());
  AccessibilityNodeInfo force=child(root,"com.android.settings:id/button1","강제 중지");ok(!SettingsGuard.reason(root,"com.android.settings",true).isEmpty());ok(SettingsGuard.reason(root,"com.samsung.android.dialer",true).isEmpty());force.visible(false);
  title.id("com.android.settings:id/entity_header_title");AccessibilityNodeInfo toggle=child(root,"android:id/switch_widget","");ok(!SettingsGuard.reason(root,"com.android.settings",true).isEmpty());
  title.text("다른 앱");ok(SettingsGuard.reason(root,"com.android.settings",true).isEmpty());title.text("YouTube");ok(!SettingsGuard.reason(root,"com.android.settings",true).isEmpty());ok(SettingsGuard.reason(root,"com.android.settings",false).isEmpty());
  toggle.recycle();title.recycle();force.recycle();root.recycle();
  // Service details work with the service label + switch without blocking a whole Settings list.
  root=node();title=child(root,"android:id/title","집중콩 · 게임과 쇼츠 차단");toggle=child(root,"android:id/switch_widget","");ok(!SettingsGuard.reason(root,"com.android.settings",true).isEmpty());title.recycle();toggle.recycle();root.recycle();
  // An installer dialog must name the app, including when its button is only OK.
  root=node();title=child(root,"android:id/message","집중콩 앱을 삭제하시겠습니까?");force=child(root,"android:id/button1","확인");ok(!SettingsGuard.reason(root,"com.google.android.packageinstaller",true).isEmpty());title.text("다른 앱을 삭제하시겠습니까?");ok(SettingsGuard.reason(root,"com.google.android.packageinstaller",true).isEmpty());title.recycle();force.recycle();root.recycle();
  root=node();title=child(root,"android:id/title","집중콩").visible(false);AccessibilityNodeInfo input=child(root,"android:id/search_src_text","집중콩").editing();AccessibilityNodeInfo clear=child(root,"android:id/search_close_btn","").description("삭제");ok(SettingsGuard.reason(root,"com.android.settings",true).isEmpty());title.recycle();input.recycle();clear.recycle();root.recycle();
  for(String pkg:new String[]{"com.samsung.accessibility","com.samsung.android.lool","com.samsung.android.sm"}){
   ok(SettingsGuard.surface(pkg));root=node();title=child(root,pkg+":id/toolbar_title","집중콩");toggle=child(root,pkg+":id/unknown_switch","").cls("android.widget.Switch");SettingsGuard.Match match=SettingsGuard.inspect(root,pkg,true);ok(!match.reason.isEmpty());ok(!match.management);toggle.recycle();
   force=child(root,pkg+":id/button1","강제 중지");match=SettingsGuard.inspect(root,pkg,true);ok(match.own&&!match.removalAllowable);force.recycle();title.recycle();root.recycle();
  }
  root=node();title=child(root,"com.sec.android.app.launcher:id/recents_view","");ok(RecentsInspector.visible(root,"com.sec.android.app.launcher"));ok(!RecentsInspector.visible(root,"other.app"));title.visible(false);ok(!RecentsInspector.visible(root,"com.sec.android.app.launcher"));title.recycle();root.recycle();
  for(String pkg:DeviceConstants.YOUTUBE){root=node().pkg(pkg);player=child(root,pkg+":id/reel_recycler","");ok(YouTubeInspector.isPackage(pkg));ok(!YouTubeInspector.player(root).isEmpty());player.recycle();root.recycle();}
  ok(!SettingsGuard.surface("com.android.phone"));ok(!SettingsGuard.surface("com.android.emergency"));ok(AccessibilityNodeInfo.handles==0);
  System.out.println("YouTube/settings/ID privacy: "+count+" checks passed (Android node fakes)");
 }
}
