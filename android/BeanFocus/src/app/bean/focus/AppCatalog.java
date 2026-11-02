package app.bean.focus;
import android.content.*;
import android.content.pm.*;
import android.net.Uri;
import android.os.SystemClock;
import java.util.*;

final class AppCatalog {
 private final Context context;
 private final Set<String> browsers=new HashSet<>();
 private final Map<String,Boolean> missing=new HashMap<>();
 private final Map<String,Boolean> games=new HashMap<>();
 private long refreshed;
 AppCatalog(Context c){context=c;}
 void invalidate(){refreshed=0;games.clear();missing.clear();}
 void refresh(){long now=SystemClock.elapsedRealtime();if(refreshed!=0&&now-refreshed<30000)return;
  Intent view=new Intent(Intent.ACTION_VIEW,Uri.parse("https://example.com"));view.addCategory(Intent.CATEGORY_BROWSABLE);
  Set<String> found=new HashSet<>();for(ResolveInfo r:context.getPackageManager().queryIntentActivities(view,0))if(r.activityInfo!=null)found.add(r.activityInfo.packageName);
  browsers.clear();browsers.addAll(found);refreshed=now;
 }
 boolean browser(String pkg){return Prefs.browser(pkg)||browsers.contains(pkg);}
 boolean game(String pkg){Boolean value=games.get(pkg);if(value!=null)return value;
  boolean result=false;try{ApplicationInfo info=context.getPackageManager().getApplicationInfo(pkg,0);result=declaredGame(info);}catch(PackageManager.NameNotFoundException ignored){}catch(RuntimeException ignored){return false;}
  games.put(pkg,result);return result;
 }
 boolean newDuringLock(String pkg){
  android.content.SharedPreferences p=Prefs.get(context);
  long start=p.getLong("lock_start_wall",0);
  boolean recorded=NewPackageLedger.contains(p.getString("lock_id",""),p.getString("lock_new_pkgs_id",""),p.getStringSet("lock_new_pkgs",Collections.emptySet()),pkg);
  if(start<=0&&!recorded)return false; // A lock created before 0.9 has no trustworthy install-time baseline.
  try{PackageInfo info=context.getPackageManager().getPackageInfo(pkg,0);
   if(info.applicationInfo==null)return false;
   boolean system=(info.applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM)!=0;
   return BlockPolicy.newApp(Prefs.enabled(context),FocusLock.active(context),p.getBoolean("block_new_apps",false),true,!(recorded||InstallTimePolicy.afterStart(start,info.firstInstallTime)),system,SettingsGuard.surface(pkg));
  }catch(PackageManager.NameNotFoundException e){return false;}
 }
 boolean unknownPackage(String pkg){Boolean known=missing.get(pkg);if(known!=null)return known;boolean unknown=false;try{context.getPackageManager().getApplicationInfo(pkg,0);}catch(PackageManager.NameNotFoundException e){unknown=true;}catch(RuntimeException e){return false;}missing.put(pkg,unknown);return unknown;}

 static boolean webHost(String pkg){return DeviceConstants.webHost(pkg);}
 static boolean declaredGame(ApplicationInfo info){return info.category==ApplicationInfo.CATEGORY_GAME||(info.flags&ApplicationInfo.FLAG_IS_GAME)!=0;}
 static String youtubeStatus(Context c){int enabled=0;for(String pkg:DeviceConstants.YOUTUBE)try{if(c.getPackageManager().getApplicationInfo(pkg,0).enabled)enabled++;}catch(PackageManager.NameNotFoundException ignored){}catch(RuntimeException ignored){}return "YouTube 계열 사용 가능 "+enabled+"개 · 잠금 중 전체 제한 / 평소 쇼츠 화면 감지";}
}
