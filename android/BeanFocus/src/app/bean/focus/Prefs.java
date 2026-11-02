package app.bean.focus;
import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;
final class Prefs {
 static SharedPreferences get(Context c){return c.getSharedPreferences("focus",Context.MODE_PRIVATE);}
 static boolean enabled(Context c){return FocusLock.active(c)||get(c).getBoolean("enabled",true);}
 static boolean shorts(Context c){return enabled(c)&&get(c).getBoolean("shorts",true);}
 static Set<String> games(Context c){return new HashSet<String>(get(c).getStringSet("games",new HashSet<String>()));}
 static final String[] BROWSERS=DeviceConstants.BROWSERS;
 static boolean browser(String p){for(String b:BROWSERS)if(b.equals(p))return true;return false;}
}
