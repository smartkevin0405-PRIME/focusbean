package android.os;
public final class Bundle {private final java.util.Map<String,CharSequence> values=new java.util.HashMap<>();public CharSequence getCharSequence(String key){return values.get(key);}public void putCharSequence(String key,CharSequence value){values.put(key,value);}}
