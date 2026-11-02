package app.bean.focus;
import java.net.URI;
final class UpdateRules {
 static final String DEFAULT_APK_URL="https://github.com/smartkevin0405-PRIME/focusbean/releases/latest/download/BeanFocus-latest.apk";
 static String address(String saved){return saved==null||saved.trim().isEmpty()?DEFAULT_APK_URL:saved.trim();}
 static final long LIMIT=25*1024*1024;
 static final String CERT="5e7325b0fbb0af5df2c80c300f0ac8d7e6e1ccd1d23bae0f8b6e2baec4b748f4";
 static boolean https(String raw){try{URI u=new URI(raw);return "https".equalsIgnoreCase(u.getScheme())&&u.getHost()!=null&&u.getUserInfo()==null&&u.getFragment()==null;}catch(Exception e){return false;}}
 static boolean compatible(String pkg,String sha,long version,long current){return DeviceConstants.OWN.equals(pkg)&&CERT.equalsIgnoreCase(sha)&&version>current;}
}
