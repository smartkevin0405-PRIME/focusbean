package app.bean.focus;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.Uri;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Samsung Internet Content Blocker API 1.0. The browser reads our filter list from
 * ShortsFilterProvider once the user turns BeanFocus on under its ad-blocker settings,
 * so Shorts are blocked inside the browser even when accessibility nodes are missing.
 */
final class SamsungContentBlocker {
 static final String ACTION_UPDATE="com.samsung.android.sbrowser.contentBlocker.ACTION_UPDATE";
 static final String ACTION_SETTING="com.samsung.android.sbrowser.contentBlocker.ACTION_SETTING";
 static final String[] BROWSERS={"com.sec.android.app.sbrowser","com.sec.android.app.sbrowser.beta"};
 private static final String FILE="samsung_shorts_filters.txt";
 private static volatile int sent=-1;
 private SamsungContentBlocker(){}

 /** Tells Samsung Internet to re-read the list when Shorts blocking turns on/off (lock start/end too). */
 static void sync(Context c){
  boolean active=Prefs.shorts(c);if(!ShortsFilterRules.changed(sent,active))return;sent=active?1:0;
  for(String pkg:BROWSERS){
   Intent update=new Intent(ACTION_UPDATE);update.setData(Uri.parse("package:"+c.getPackageName()));update.setPackage(pkg);
   try{c.sendBroadcast(update);}catch(RuntimeException ignored){}
  }
 }
 static boolean installed(Context c){for(String pkg:BROWSERS)if(settings(c,pkg)!=null)return true;return false;}
 static boolean openSettings(Context c){
  for(String pkg:BROWSERS){Intent i=settings(c,pkg);if(i==null)continue;try{c.startActivity(i);return true;}catch(RuntimeException ignored){}}
  return false;
 }
 private static Intent settings(Context c,String pkg){
  Intent i=new Intent(ACTION_SETTING);i.setPackage(pkg);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
  try{return c.getPackageManager().resolveActivity(i,PackageManager.MATCH_DEFAULT_ONLY)==null?null:i;}catch(RuntimeException e){return null;}
 }
 static synchronized File write(Context c) throws IOException {
  File target=new File(c.getFilesDir(),FILE),temp=new File(c.getFilesDir(),FILE+".tmp");
  try(OutputStream out=new FileOutputStream(temp)){out.write(ShortsFilterRules.text(Prefs.shorts(c)).getBytes(StandardCharsets.UTF_8));}
  if(!temp.renameTo(target)){target.delete();if(!temp.renameTo(target))throw new IOException("filter list not replaced");}
  return target;
 }
 /** Records only the time Samsung Internet itself last fetched the list. */
 static void served(Context c,String caller){
  // The provider is public, so other callers must not overwrite the browser's fetch time.
  for(String pkg:BROWSERS)if(pkg.equals(caller)){Prefs.get(c).edit().putLong("samsung_blocker_served_wall",System.currentTimeMillis()).apply();return;}
 }
 static String status(Context c){
  if(!installed(c))return "삼성 인터넷 차단 목록: 삼성 인터넷이 없거나 광고 차단기를 지원하지 않음";
  long at=Prefs.get(c).getLong("samsung_blocker_served_wall",0);
  String rules=Prefs.shorts(c)?"쇼츠 규칙 켜짐":"쇼츠 차단 꺼짐 · 빈 목록";
  if(at<=0)return "삼성 인터넷 차단 목록: 아직 가져가지 않음 · 삼성 인터넷이 목록에 올리고 사용자가 켜야 시작돼요 · "+rules;
  long minutes=Math.max(0,(System.currentTimeMillis()-at)/60000);
  return "삼성 인터넷 차단 목록: 삼성 인터넷이 "+minutes+"분 전에 가져감 · "+rules;
 }
}
