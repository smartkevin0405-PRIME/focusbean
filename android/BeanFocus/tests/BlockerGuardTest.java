package app.bean.focus;
import android.view.accessibility.AccessibilityNodeInfo;

/** Samsung Internet's ad blocker list, synthetic rows (not a device dump). */
public class BlockerGuardTest {
 static int checks;static void ok(boolean b){checks++;if(!b)throw new AssertionError("case "+checks);}
 static void add(AccessibilityNodeInfo p,AccessibilityNodeInfo n){p.child(n);n.recycle();}
 static AccessibilityNodeInfo text(String t){return new AccessibilityNodeInfo().cls("android.widget.TextView").text(t);}
 /** Row: [title + summary] [switch]. */
 static AccessibilityNodeInfo row(String name,boolean on){
  AccessibilityNodeInfo row=new AccessibilityNodeInfo().cls("android.widget.LinearLayout"),texts=new AccessibilityNodeInfo().cls("android.widget.LinearLayout");
  add(texts,text(name));add(texts,text("광고 차단 앱"));add(row,texts);
  add(row,new AccessibilityNodeInfo().cls("android.widget.Switch").id("android:id/switch_widget").checked(on));return row;
 }
 static AccessibilityNodeInfo page(AccessibilityNodeInfo... rows){
  AccessibilityNodeInfo root=new AccessibilityNodeInfo().pkg("com.sec.android.app.sbrowser").cls("android.widget.FrameLayout");
  add(root,text("광고 차단기"));AccessibilityNodeInfo list=new AccessibilityNodeInfo().cls("androidx.recyclerview.widget.RecyclerView");
  for(AccessibilityNodeInfo r:rows)add(list,r);add(root,list);return root;
 }
 static int read(AccessibilityNodeInfo root){int s=BlockerGuard.ownSwitch(root);root.recycle();return s;}
 public static void main(String[] args){
  int before=AccessibilityNodeInfo.handles;
  ok(BlockerGuard.surface("com.sec.android.app.sbrowser"));ok(BlockerGuard.surface("com.sec.android.app.sbrowser.beta"));ok(!BlockerGuard.surface("com.android.chrome"));ok(!BlockerGuard.surface(null));
  // Own row on: guard. Own row off: leave it so the user can turn it on.
  ok(read(page(row("집중콩",true)))==BlockerGuard.ON);ok(read(page(row("집중콩",false)))==BlockerGuard.OFF);
  // Another blocker's switch never decides for BeanFocus, whichever row comes first.
  ok(read(page(row("AdGuard",true),row("집중콩",false)))==BlockerGuard.OFF);ok(read(page(row("집중콩",true),row("AdGuard",false)))==BlockerGuard.ON);
  ok(read(page(row("AdGuard",true)))==BlockerGuard.NONE);
  // A one-node switch row labelled with the app name.
  AccessibilityNodeInfo single=new AccessibilityNodeInfo().cls("android.widget.Switch").text("집중콩").checked(true);ok(read(page(single))==BlockerGuard.ON);
  // Not exact label, no switch nearby, editable search text: no decision.
  ok(read(page(row("집중콩 도움말",true)))==BlockerGuard.NONE);
  AccessibilityNodeInfo plain=page();add(plain,text("집중콩"));ok(read(plain)==BlockerGuard.NONE);
  AccessibilityNodeInfo search=page(row("AdGuard",true));add(search,new AccessibilityNodeInfo().cls("android.widget.EditText").text("집중콩").editing());ok(read(search)==BlockerGuard.NONE);
  // A label with only other rows' switches around it (two switches) is ambiguous.
  AccessibilityNodeInfo loose=page(row("AdGuard",true),row("Adblock Plus",false));AccessibilityNodeInfo list=loose.getChild(1);add(list,text("집중콩"));list.recycle();ok(read(loose)==BlockerGuard.NONE);
  // Web page text and checkboxes are never the browser's settings row.
  AccessibilityNodeInfo web=BrowserHiddenAddressTest.web();AccessibilityNodeInfo webRow=new AccessibilityNodeInfo();webRow.getExtras().putCharSequence("AccessibilityNodeInfo.chromeRole","genericContainer");
  AccessibilityNodeInfo webText=text("집중콩");webText.getExtras().putCharSequence("AccessibilityNodeInfo.chromeRole","staticText");add(webRow,webText);add(webRow,new AccessibilityNodeInfo().cls("android.widget.CheckBox").checked(true));add(web,webRow);
  AccessibilityNodeInfo browser=new AccessibilityNodeInfo().pkg("com.sec.android.app.sbrowser");add(browser,web);ok(read(browser)==BlockerGuard.NONE);
  // Hidden rows do not count.
  AccessibilityNodeInfo hidden=row("집중콩",true).visible(false);ok(read(page(hidden))==BlockerGuard.NONE);
  ok(AccessibilityNodeInfo.handles==before);
  System.out.println("Samsung Internet ad blocker guard: "+checks+" checks passed (synthetic nodes, not a device test)");
 }
}
