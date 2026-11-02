package android.provider;
public class Settings { public static class Secure {public static String getString(Object r,String k){return null;}} public static class Global {public static final String BOOT_COUNT="boot_count";public static int boot=1;public static int getInt(Object resolver,String key,int fallback){return boot;}} }
