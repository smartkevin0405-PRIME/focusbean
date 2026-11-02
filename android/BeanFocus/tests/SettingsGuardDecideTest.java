package app.bean.focus;
import java.util.*;
public class SettingsGuardDecideTest {
 static int count;static SettingsGuard.Node n(String t,String id){return new SettingsGuard.Node(t,"",id,"android.widget.TextView",false,false);}static SettingsGuard.Node check(){return new SettingsGuard.Node("","","toggle","android.widget.CheckBox",true,false);}
 static void test(String pkg,boolean allowed,boolean block,SettingsGuard.Node... nodes){count++;SettingsGuard.Match m=SettingsGuard.decide(pkg,Arrays.asList(nodes),true,allowed);if((!m.reason.isEmpty())!=block)throw new AssertionError("case "+count+": "+m.reason);}
 public static void main(String[] a){String s=DeviceConstants.SAMSUNG_ACCESS,p=DeviceConstants.SETTINGS;SettingsGuard.Node own=n("집중콩","row"),service=n("집중콩 · 게임과 쇼츠 차단","row"),title=n("집중콩","toolbar_title");
  test(s,false,true,n("집중콩 · 게임과 쇼츠 차단","toolbar_title"),check());test(s,true,true,own,check());test(p,false,true,service,check());test(p,false,false,service);test(p,false,true,title,n("열기","button"),n("삭제","button"),n("강제 중지","button"));test(p,true,false,title,n("삭제","button"));
  test("com.google.android.packageinstaller",false,true,own,n("취소","button"),n("확인","button"));test("com.google.android.packageinstaller",true,false,own);test("com.samsung.android.lool",false,true,own,check());test("com.samsung.android.lool",true,true,own,check());test("com.samsung.android.app.routines",true,true,own);
  test(p,false,true,n("자동 날짜 및 시간","row"));test(p,true,true,n("Set date","row"),n("Set time","row"));test(p,false,false,n("일반","toolbar_title"),n("날짜 및 시간","row"));
  test(p,false,true,n("YouTube","toolbar_title"),n("사용 안 함","button"),n("강제 중지","button"));test(p,false,false,n("알림","toolbar_title"),own,n("YouTube","row"),check());test(p,false,false,n("Wi-Fi","toolbar_title"),check());test(p,false,false,n("디스플레이","toolbar_title"));
  test(p,false,false,new SettingsGuard.Node("집중콩","","search","android.widget.EditText",false,true));test(s,true,true,service,check());test(p,false,false,own,n("YouTube","row"));test("com.android.phone",false,false,title,n("삭제","button"));
  for(String y:DeviceConstants.YOUTUBE)test(p,false,true,n(y,"toolbar_title"),n("사용 안 함","button"));
  System.out.println("Settings ordered rules: "+count+" checks passed");
 }
}
