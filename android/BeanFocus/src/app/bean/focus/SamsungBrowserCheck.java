package app.bean.focus;
import android.content.*;
import android.content.pm.*;
import android.net.Uri;
import android.os.Build;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.*;

/**
 * Explicit, user-started check of how Samsung Browser handles content blockers on this
 * device: version, installer, the receiver for ACTION_UPDATE, the CONTENTBLOCKER permission,
 * and blocker-related names inside its APK. Reports only package, class and constant names.
 */
final class SamsungBrowserCheck {
 static final String PERMISSION="com.samsung.android.sbrowser.permission.CONTENTBLOCKER";
 static final String META="com.samsung.android.sbrowser.contentBlocker.interfaceVersion";
 private static final long MAX_ENTRY=48L*1024*1024;
 private static final int MAX_NAMES=80;
 private SamsungBrowserCheck(){}

 static String run(Context c){
  StringBuilder out=new StringBuilder("[삼성 브라우저 차단기 점검]\n");
  PackageManager pm=c.getPackageManager();boolean any=false;
  for(String pkg:SamsungContentBlocker.BROWSERS){
   PackageInfo info;
   try{info=pm.getPackageInfo(pkg,PackageManager.GET_PERMISSIONS);}catch(PackageManager.NameNotFoundException e){continue;}catch(RuntimeException e){out.append(pkg).append(": 읽기 실패 ").append(e.getClass().getSimpleName()).append('\n');continue;}
   any=true;ApplicationInfo app=info.applicationInfo;
   out.append("\n패키지 ").append(pkg).append(" · 버전 ").append(info.versionName).append(" (").append(Build.VERSION.SDK_INT>=28?info.getLongVersionCode():info.versionCode).append(")");
   if(app!=null)out.append(" · target ").append(app.targetSdkVersion).append(app.enabled?"":" · 사용 중지됨");
   out.append("\n설치 ").append(date(info.firstInstallTime)).append(" · 업데이트 ").append(date(info.lastUpdateTime)).append(" · 설치 출처 ").append(installer(pm,pkg));
   out.append("\nQUERY_ALL_PACKAGES 요청: ").append(requests(info,"android.permission.QUERY_ALL_PACKAGES")?"예":"아니요");
   out.append("\nACTION_UPDATE 수신기: ").append(components(pm,pm.queryBroadcastReceivers(new Intent(SamsungContentBlocker.ACTION_UPDATE).setData(Uri.parse("package:"+c.getPackageName())).setPackage(pkg),PackageManager.MATCH_DISABLED_COMPONENTS)));
   out.append("\n광고 차단 기능 화면: ").append(components(pm,pm.queryIntentActivities(new Intent(SamsungContentBlocker.ACTION_SETTING).setPackage(pkg),PackageManager.MATCH_DISABLED_COMPONENTS)));
   out.append("\nAPK 안의 차단기 관련 이름:\n").append(apkNames(app));
  }
  if(!any)out.append("삼성 브라우저가 설치돼 있지 않거나 보이지 않음\n");
  out.append("\n\nCONTENTBLOCKER 권한: ").append(permission(pm)).append(" · 집중콩 ").append(c.checkSelfPermission(PERMISSION)==PackageManager.PERMISSION_GRANTED?"허용됨":"허용 안 됨");
  out.append("\n집중콩 차단기 선언: ").append(ownMeta(c,pm)).append(" · 목록 제공자 ").append(ownProvider(c,pm));
  return out.toString();
 }

 private static String date(long ms){return ms<=0?"알 수 없음":new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.ROOT).format(new Date(ms));}
 private static String installer(PackageManager pm,String pkg){
  try{String s=Build.VERSION.SDK_INT>=30?pm.getInstallSourceInfo(pkg).getInstallingPackageName():pm.getInstallerPackageName(pkg);return s==null?"없음(기본 설치)":s;}catch(Exception e){return "알 수 없음";}
 }
 private static boolean requests(PackageInfo info,String permission){if(info.requestedPermissions!=null)for(String p:info.requestedPermissions)if(permission.equals(p))return true;return false;}
 private static String components(PackageManager pm,List<ResolveInfo> found){
  if(found==null||found.isEmpty())return "없음";
  StringBuilder s=new StringBuilder().append(found.size()).append("개");
  for(ResolveInfo r:found){
   ComponentInfo ci=r.activityInfo!=null?r.activityInfo:r.serviceInfo;if(ci==null)continue;
   int state=PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;try{state=pm.getComponentEnabledSetting(new ComponentName(ci.packageName,ci.name));}catch(RuntimeException ignored){}
   boolean on=state==PackageManager.COMPONENT_ENABLED_STATE_ENABLED||(state==PackageManager.COMPONENT_ENABLED_STATE_DEFAULT&&ci.enabled);
   s.append(" · ").append(ci.name.substring(ci.name.lastIndexOf('.')+1)).append(on?"":"(꺼짐)").append(ci.exported?"":"(내보내지 않음)");
  }
  return s.toString();
 }
 private static String permission(PackageManager pm){
  try{
   PermissionInfo p=pm.getPermissionInfo(PERMISSION,0);int level=Build.VERSION.SDK_INT>=28?p.getProtection():p.protectionLevel&PermissionInfo.PROTECTION_MASK_BASE;
   String name=level==PermissionInfo.PROTECTION_NORMAL?"normal":level==PermissionInfo.PROTECTION_DANGEROUS?"dangerous":level==PermissionInfo.PROTECTION_SIGNATURE?"signature":"수준 "+level;
   return "정의 "+p.packageName+" · "+name;
  }catch(PackageManager.NameNotFoundException e){return "어떤 앱도 정의하지 않음";}catch(RuntimeException e){return "읽기 실패";}
 }
 private static String ownMeta(Context c,PackageManager pm){
  try{ApplicationInfo a=pm.getApplicationInfo(c.getPackageName(),PackageManager.GET_META_DATA);String v=a.metaData==null?null:a.metaData.getString(META);return v==null?"없음":v;}catch(Exception e){return "읽기 실패";}
 }
 private static String ownProvider(Context c,PackageManager pm){
  ProviderInfo p=pm.resolveContentProvider(c.getPackageName()+".contentBlocker.contentProvider",0);
  return p==null?"없음":(p.exported?"공개":"비공개")+(p.enabled?"":" · 꺼짐");
 }
 /** Blocker-related names from the manifest and code of the browser's installed APKs. */
 private static String apkNames(ApplicationInfo app){
  if(app==null||app.sourceDir==null)return "  APK 경로 없음\n";
  List<String> files=new ArrayList<>();files.add(app.sourceDir);if(app.splitSourceDirs!=null)files.addAll(Arrays.asList(app.splitSourceDirs));
  TreeSet<String> manifest=new TreeSet<>(),code=new TreeSet<>();StringBuilder problems=new StringBuilder();
  for(String path:files){
   try(ZipFile zip=new ZipFile(path)){
    Enumeration<? extends ZipEntry> entries=zip.entries();
    while(entries.hasMoreElements()){
     ZipEntry e=entries.nextElement();String name=e.getName();
     boolean isManifest=name.equals("AndroidManifest.xml"),isDex=name.matches("classes[0-9]*\\.dex");
     if(!isManifest&&!isDex)continue;if(e.getSize()>MAX_ENTRY){problems.append("  ").append(name).append(" 너무 큼\n");continue;}
     byte[] data=read(zip,e);
     if(isManifest)manifest.addAll(ApkStrings.manifest(data,ApkStrings::interesting));else code.addAll(ApkStrings.dex(data,ApkStrings::interesting));
    }
   }catch(IOException|RuntimeException e){problems.append("  ").append(new File(path).getName()).append(" 읽기 실패 ").append(e.getClass().getSimpleName()).append('\n');}
  }
  StringBuilder s=new StringBuilder(problems);
  s.append("  매니페스트 ").append(manifest.size()).append("개").append(manifest.isEmpty()?"":": "+join(manifest)).append('\n');
  s.append("  코드 ").append(code.size()).append("개").append(code.isEmpty()?"":": "+join(code));
  return s.toString();
 }
 private static String join(Collection<String> names){StringBuilder s=new StringBuilder();int n=0;for(String name:names){if(n++>=MAX_NAMES){s.append(" … 외 ").append(names.size()-MAX_NAMES).append("개");break;}if(s.length()>0)s.append(", ");s.append(name);}return s.toString();}
 private static byte[] read(ZipFile zip,ZipEntry e) throws IOException {
  try(InputStream in=zip.getInputStream(e)){ByteArrayOutputStream out=new ByteArrayOutputStream(e.getSize()>0?(int)e.getSize():1<<16);byte[] buf=new byte[1<<16];int n;long total=0;while((n=in.read(buf))>0){total+=n;if(total>MAX_ENTRY)throw new IOException("too large");out.write(buf,0,n);}return out.toByteArray();}
 }
}
