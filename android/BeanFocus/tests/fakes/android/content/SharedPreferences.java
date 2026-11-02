package android.content;
import java.util.*;
public class SharedPreferences {
 private final Map<String,Object> data=new HashMap<>();
 public long getLong(String k,long fallback){return data.containsKey(k)?(Long)data.get(k):fallback;}
 public int getInt(String k,int fallback){return data.containsKey(k)?(Integer)data.get(k):fallback;}
 public boolean getBoolean(String k,boolean fallback){return data.containsKey(k)?(Boolean)data.get(k):fallback;}
 public String getString(String k,String f){return data.containsKey(k)?(String)data.get(k):f;}
 @SuppressWarnings("unchecked") public Set<String> getStringSet(String k,Set<String> f){return data.containsKey(k)?(Set<String>)data.get(k):f;}
 public Editor edit(){return new Editor();}
 public class Editor {
  private final Map<String,Object> changes=new HashMap<>();
  public Editor putLong(String k,long v){changes.put(k,v);return this;}
  public Editor putInt(String k,int v){changes.put(k,v);return this;}
  public Editor putBoolean(String k,boolean v){changes.put(k,v);return this;}
  public Editor putString(String k,String v){changes.put(k,v);return this;}
  public Editor putStringSet(String k,Set<String> v){changes.put(k,new HashSet<>(v));return this;}
  public void apply(){data.putAll(changes);}
  public boolean commit(){data.putAll(changes);return true;}
 }
}
