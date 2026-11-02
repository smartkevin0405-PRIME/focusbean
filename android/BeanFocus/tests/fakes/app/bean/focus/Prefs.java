package app.bean.focus;
import android.content.*;
public class Prefs {private static final SharedPreferences p=new SharedPreferences();static SharedPreferences get(Context c){return p;} static boolean enabled(Context c){return p.getBoolean("enabled",true);} static java.util.Set<String> games(Context c){return java.util.Collections.emptySet();} }
