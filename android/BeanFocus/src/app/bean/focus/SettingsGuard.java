package app.bean.focus;
import java.util.*;
final class SettingsGuard {
 static boolean surface(String p){return p!=null&&(DeviceConstants.SURFACES.contains(p)||installer(p));}
 static boolean installer(String p){return p!=null&&p.endsWith(".packageinstaller");}
 static final class Node {
  final String text,description,id,className;final boolean checkable,editable;
  Node(String t,String d,String i,String c,boolean check,boolean edit){text=norm(t);description=norm(d);id=i==null?"":i.substring(i.lastIndexOf('/')+1);className=c==null?"":c;checkable=check;editable=edit;}
 }
 static final class Match {
  final String reason;final boolean own,removalAllowable,management,installer;final int rule;
  Match(String r,boolean o,boolean allow,boolean install,int n){reason=r;own=o;removalAllowable=allow;management=allow;installer=install;rule=n;}
 }
 private static String norm(String s){return s==null?"":s.trim().toLowerCase(Locale.ROOT);}
 private static boolean app(String s){return s.equals("집중콩")||s.equals("beanfocus")||s.equals(DeviceConstants.OWN);}
 private static boolean service(String s){return s.startsWith("집중콩 ·");}
 private static boolean ownMention(String s){return s.contains("집중콩")||s.contains("beanfocus")||s.contains(DeviceConstants.OWN);}
 private static boolean yt(String s){return s.equals("youtube")||s.equals("유튜브")||DeviceConstants.YOUTUBE.contains(s);}
 private static Match result(String reason,boolean own,boolean allow,boolean install,int rule,boolean removal){return new Match(removal&&allow?"":reason,own,allow,install,rule);}
 static Match decide(String pkg,List<Node> nodes,boolean protectYouTube,boolean removalAllowed){
  if(!surface(pkg)||DeviceConstants.emergency(pkg))return result("",false,false,false,0,false);
  boolean mention=false,ytMention=false,label=false,appLabel=false,serviceLabel=false,ytLabel=false,ownTitle=false,ytTitle=false,check=false,strong=false,broad=false,auto=false,date=false,time=false;
  for(Node n:nodes){if(n.editable)continue;check|=n.checkable||n.className.endsWith("Switch")||n.className.endsWith("CheckBox")||n.className.endsWith("RadioButton")||DeviceConstants.SWITCH_IDS.contains(n.id);
   for(String text:new String[]{n.text,n.description}){
    boolean a=app(text),s=service(text),y=protectYouTube&&yt(text);appLabel|=a;serviceLabel|=s;label|=a||s;ytLabel|=y;mention|=ownMention(text);ytMention|=protectYouTube&&(text.contains("youtube")||text.contains("유튜브"));
    if(DeviceConstants.TITLE_IDS.contains(n.id)){ownTitle|=a||s;ytTitle|=y;}
    strong|=DeviceConstants.STRONG.contains(text);broad|=DeviceConstants.BROAD.contains(text);auto|=DeviceConstants.AUTO_TIME.contains(text);date|=DeviceConstants.SET_DATE.contains(text);time|=DeviceConstants.SET_TIME.contains(text);
   }
  }
  if(installer(pkg)&&(mention||ytMention))return result("설치기 · 보호 앱 확인",mention,mention,true,1,removalAllowed);
  if(DeviceConstants.CARE.contains(pkg)&&mention)return result("디바이스 케어·루틴 · 집중콩 항목",true,false,false,2,false);
  if(auto||(date&&time))return result("날짜·시간 수동 변경 화면",mention,false,false,3,false);
  if(ownTitle)return result("집중콩 제목 · 앱 관리 화면",true,DeviceConstants.SETTINGS.equals(pkg),false,4,removalAllowed);
  if((serviceLabel||(DeviceConstants.SAMSUNG_ACCESS.equals(pkg)&&appLabel))&&check)return result("집중콩 접근성·바로가기 항목",true,false,false,5,false);
  if(ytTitle&&(broad||check))return result("유튜브 앱 관리 화면",false,false,false,6,false);
  if((label||ytLabel)&&strong)return result("보호 앱 · 강한 관리 동작",label,label&&DeviceConstants.SETTINGS.equals(pkg),false,7,removalAllowed);
  return result("",false,false,false,0,false);
 }
 static Match inspect(android.view.accessibility.AccessibilityNodeInfo root,String p,boolean yt){return decide(p,SettingsNodeCollector.collect(root),yt,false);}
 static String reason(android.view.accessibility.AccessibilityNodeInfo root,String p,boolean yt){return inspect(root,p,yt).reason;}
}
