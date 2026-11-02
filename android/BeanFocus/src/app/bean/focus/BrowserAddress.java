package app.bean.focus;
import java.net.URI;
import java.util.Locale;
public final class BrowserAddress {
 private BrowserAddress(){}
 static final String[] FIELDS=DeviceConstants.ADDRESS_FIELDS;
 static String[] fieldsFor(String pkg){return DeviceConstants.fieldsFor(pkg);}
 public static boolean field(String id){if(id==null)return false;String s=id.substring(id.lastIndexOf('/')+1);for(String f:FIELDS)if(f.equalsIgnoreCase(s))return true;return false;}
 public static String clean(String raw){return raw==null?"":raw.replaceAll("[\\u200e\\u200f\\u202a-\\u202e\\u2066-\\u2069]","").trim();}
 // A bare host, including a trailing '/', cannot prove that the browser exposed its path.
 public static boolean hasPath(String raw){URI u=UrlRules.parse(raw);return u!=null&&u.getPath()!=null&&u.getPath().length()>1;}
 static int score(String raw){if(UrlRules.parse(raw)==null)return 0;return hasPath(raw)?(UrlRules.isShorts(raw)?4:3):1;}
 static boolean isDocumentHome(String raw){URI u=UrlRules.parse(raw);return u!=null&&UrlRules.isYouTube(raw)&&"/".equals(u.getPath());}
 public static String better(String a,String b){a=clean(a);b=clean(b);return score(b)>score(a)?b:a;}
 static String status(String url){if(UrlRules.parse(url)==null)return "주소 확인 안 됨 · 판별 불가";if(!UrlRules.isYouTube(url))return "유튜브 외 주소";if(UrlRules.isShorts(url))return "쇼츠 경로 감지";return hasPath(url)?"유튜브 경로 확인 · 쇼츠 경로 아님":"유튜브 도메인만 확인 · 판별 불가";}
}
