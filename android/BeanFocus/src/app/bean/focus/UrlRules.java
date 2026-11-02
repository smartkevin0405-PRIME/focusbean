package app.bean.focus;
import java.net.URI;
import java.util.Locale;

public final class UrlRules {
 private UrlRules() {}
 public static URI parse(String raw) {
  try {
   String s=raw == null ? "" : raw.trim();
   if(s.isEmpty() || s.contains(" ") || s.startsWith("//")) return null;
   if(!s.matches("^[A-Za-z][A-Za-z0-9+.-]*://.*")) s="https://"+s;
   URI u=new URI(s);
   if(!"https".equalsIgnoreCase(u.getScheme()) && !"http".equalsIgnoreCase(u.getScheme())) return null;
   return u.getHost()==null ? null : u;
  } catch(Exception e) {return null;}
 }
 public static boolean isYouTube(String raw) {
  URI u=parse(raw); if(u==null)return false;
  String h=u.getHost().toLowerCase(Locale.ROOT);
  return h.equals("youtube.com") || h.endsWith(".youtube.com") || h.equals("youtu.be");
 }
 public static boolean isShorts(String raw) {
  URI u=parse(raw); if(u==null || !isYouTube(raw))return false;
  String h=u.getHost().toLowerCase(Locale.ROOT);
  if(h.equals("youtu.be"))return false;
  String p=u.getPath();
  return p!=null && (p.equals("/shorts") || p.startsWith("/shorts/"));
 }
}
