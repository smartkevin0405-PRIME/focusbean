package app.bean.focus;
import android.content.Context;
import android.provider.Settings;
final class ShortcutCheck {
 interface Reader {String read(String key);}
 static final class Result {final boolean registered,failed;Result(boolean r,boolean f){registered=r;failed=f;}String status(){return registered?"집중콩 바로가기 등록됨 · 시작 거부":failed?"바로가기 설정 일부 읽기 실패":"바로가기 점검 완료 · 등록 확인 안 됨";}}
 static String last="아직 바로가기 점검 안 함";static boolean warned;
 static Result inspect(Reader reader){boolean registered=false,failed=false;for(String key:DeviceConstants.SHORTCUT_KEYS)try{String value=reader.read(key);registered|=value!=null&&value.contains(DeviceConstants.OWN);}catch(RuntimeException e){failed=true;}return new Result(registered,failed);}
 static Result check(Context c){Result r=inspect(key->Settings.Secure.getString(c.getContentResolver(),key));last=r.status();Prefs.get(c).edit().putString("shortcut_last_result",last).apply();return r;}
}
