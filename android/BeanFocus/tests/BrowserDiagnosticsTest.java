package app.bean.focus;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Device diagnostics must separate "the browser exposes no web node" from "a web node was
 * rejected", and the structure dump must never carry page text, addresses or descriptions.
 */
public class BrowserDiagnosticsTest {
 private static final String PKG="com.sec.android.app.sbrowser",SECRET="비밀제목-SECRET";
 static int checks;static void ok(boolean b){checks++;if(!b)throw new AssertionError("case "+checks);}
 static void add(AccessibilityNodeInfo p,AccessibilityNodeInfo n){p.child(n);n.recycle();}
 static AccessibilityNodeInfo chrome(boolean web,boolean refreshable){
  AccessibilityNodeInfo root=new AccessibilityNodeInfo().pkg(PKG).bounds(0,0,1000,2000);
  add(root,new AccessibilityNodeInfo().id(PKG+":id/location_bar_edit_text").text("m.youtube.com").bounds(0,100,1000,200));
  AccessibilityNodeInfo bar=new AccessibilityNodeInfo().cls("android.widget.LinearLayout").id(PKG+":id/bottom_bar").bounds(0,1800,1000,2000);
  add(bar,new AccessibilityNodeInfo().cls("android.widget.ImageButton").clickable(true).description(SECRET).bounds(0,1800,200,2000));add(root,bar);
  if(web){
   AccessibilityNodeInfo w=new AccessibilityNodeInfo().cls("android.webkit.WebView").refreshable(refreshable).bounds(0,200,1000,1800);
   w.getExtras().putCharSequence("AccessibilityNodeInfo.chromeRole","rootWebArea");w.getExtras().putCharSequence("url","https://m.youtube.com/shorts/SECRET-URL");
   AccessibilityNodeInfo link=new AccessibilityNodeInfo().cls("android.view.View").id("secret-html-id").text(SECRET).description(SECRET).clickable(true).bounds(10,300,900,400);
   link.getExtras().putCharSequence("AccessibilityNodeInfo.targetUrl","https://m.youtube.com/watch?v=SECRET-VIDEO");link.getExtras().putCharSequence("AccessibilityNodeInfo.chromeRole","link");add(w,link);
   AccessibilityNodeInfo foreign=new AccessibilityNodeInfo().cls("android.view.View").bounds(10,500,900,600);foreign.getExtras().putCharSequence("AccessibilityNodeInfo.targetUrl","https://example.org/SECRET-PAGE");foreign.getExtras().putCharSequence("AccessibilityNodeInfo.chromeRole","SecretRoleName");add(w,foreign);
   add(w,new AccessibilityNodeInfo().id("shorts-player").bounds(0,250,1000,1700));
   add(root,w);
  }
  return root;
 }
 public static void main(String[] args){
  // 1) No web node at all: the diagnostic says so, and no rejection is counted.
  AccessibilityNodeInfo r=chrome(false,true);BrowserInspector.Result res=BrowserInspector.read(r,PKG);r.recycle();
  ok(res.webCandidates==0&&res.refreshFailed==0&&res.webOffscreen==0);ok(res.diagnostic().contains("웹 루트 후보 0 (갱신 실패 0 · 화면 밖 0 · 브라우저가 웹 화면 노드를 주지 않았을 수 있어요)"));
  // 2) A web node whose refresh fails is counted as rejected, not as missing.
  r=chrome(true,false);res=BrowserInspector.read(r,PKG);r.recycle();
  ok(res.webCandidates==1&&res.refreshFailed==1&&res.webOffscreen==0&&res.documentUnavailable);ok(res.diagnostic().contains("웹 루트 후보 1 (갱신 실패 1 · 화면 밖 0)"));ok(res.playerState.contains("웹 루트 없음"));
  // 3) A refreshed web node is a usable candidate.
  r=chrome(true,true);res=BrowserInspector.read(r,PKG);r.recycle();
  ok(res.webCandidates==1&&res.refreshFailed==0&&!res.documentUnavailable);ok(res.diagnostic().contains("웹 루트 후보 1 (갱신 실패 0 · 화면 밖 0)"));
  // 4) An offscreen web node is counted separately.
  r=chrome(true,true);AccessibilityNodeInfo web=r.getChild(2);web.getExtras().putBoolean("AccessibilityNodeInfo.offscreen",true);web.recycle();
  res=BrowserInspector.read(r,PKG);r.recycle();ok(res.webCandidates==1&&res.webOffscreen==1&&res.refreshFailed==0);ok(res.diagnostic().contains("화면 밖 1"));
  // 5) Structure dump: shape and counts are visible...
  r=chrome(true,false);String dump=StructureSnapshot.collect(r);r.recycle();
  ok(dump.contains("웹 루트 후보 1 (갱신 성공 0 · 실패 1 · 화면 밖 0)"));ok(dump.contains("WebView"));ok(dump.contains("refresh=FAIL"));ok(dump.contains("role=rootWebArea"));ok(dump.contains("role=link"));
  ok(dump.contains("id=location_bar_edit_text"));ok(dump.contains("id=bottom_bar"));ok(dump.contains("id=web:shorts-player"));ok(dump.contains("id=html"));
  ok(dump.contains("링크 출처 유튜브 1 / 기타 1"));ok(dump.contains("link=Y"));ok(dump.contains("link=O"));ok(dump.contains("role=other"));
  ok(dump.contains("box=0,5,100,5"));ok(dump.contains("box=1,15,89,5"));
  // ...but no text, description, address, HTML ID or unknown role token ever appears.
  for(String leaked:new String[]{SECRET,"SECRET","secret","Secret","m.youtube.com","example.org","https","shorts/","watch?v","SecretRoleName","com.sec.android"})ok(!dump.contains(leaked));
  // 6) Unrefreshable root with a successful refresh is reported as ok.
  r=chrome(true,true);dump=StructureSnapshot.collect(r);r.recycle();ok(dump.contains("갱신 성공 1 · 실패 0")&&dump.contains("refresh=ok")&&!dump.contains("refresh=FAIL"));
  // 7) Invisible nodes are listed but not descended; the dump is bounded and releases every handle.
  r=new AccessibilityNodeInfo().pkg(PKG).bounds(0,0,1000,2000);AccessibilityNodeInfo hidden=new AccessibilityNodeInfo().cls("android.view.View").visible(false).bounds(0,0,1000,2000);
  add(hidden,new AccessibilityNodeInfo().cls("android.view.View").id(PKG+":id/behind_hidden"));add(r,hidden);dump=StructureSnapshot.collect(r);r.recycle();ok(dump.contains("노드 2 ")&&!dump.contains("behind_hidden"));
  r=new AccessibilityNodeInfo().pkg(PKG).bounds(0,0,1000,2000);for(int i=0;i<500;i++)add(r,new AccessibilityNodeInfo().cls("android.view.View").bounds(0,i,10,i+1));
  dump=StructureSnapshot.collect(r);r.recycle();ok(dump.contains("노드 300+ (한도 300)"));ok(dump.split("\n").length<=302+1);
  ok(StructureSnapshot.collect(null).isEmpty());
  ok(AccessibilityNodeInfo.handles==0);
  System.out.println("Browser diagnostics / structure dump privacy: "+checks+" checks passed (synthetic nodes, not a device test)");
 }
}
