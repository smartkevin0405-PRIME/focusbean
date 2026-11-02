package app.bean.focus;
import java.util.*;
/** Geometry and control-family policy; no Android objects, titles or URLs retained. */
final class WebShortsRules {
 static final int LIKE=1,COMMENT=2,SHARE=3;
 static final class Control {
  final int kind;final float x,y;
  Control(int kind,float x,float y){this.kind=kind;this.x=x;this.y=y;}
 }
 static int control(String raw){String s=normalize(raw);if(s.length()>160)return 0;
  if(matches(s,DeviceConstants.WEB_SHARE_LABELS))return SHARE;
  if(matches(s,DeviceConstants.WEB_COMMENT_LABELS))return COMMENT;
  if(!matches(s,DeviceConstants.WEB_DISLIKE_LABELS)&&matches(s,DeviceConstants.WEB_LIKE_LABELS))return LIKE;
  return 0;
 }
 static boolean subscribe(String raw){return DeviceConstants.WEB_SUBSCRIBE_LABELS.contains(normalize(raw));}
 private static boolean matches(String s,String[] words){for(String w:words)if(w.matches("[a-z]+")?s.matches(".*\\b"+w+"s?\\b.*"):s.contains(w))return true;return false;}
 static String normalize(String s){return s==null?"":s.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+"," ");}
 static boolean player(boolean portrait,boolean playerId,boolean authorOrSubscribe,List<Control> controls){
  if(!portrait&&!playerId)return false;
  for(Control a:controls)for(Control b:controls){
   if(a.kind==b.kind||Math.abs(a.x-b.x)>.10f||Math.abs(a.y-b.y)<.055f)continue;
   if(playerId)return true;
   if(!authorOrSubscribe)continue;
   for(Control c:controls)if(c.kind!=a.kind&&c.kind!=b.kind&&Math.max(Math.abs(c.x-a.x),Math.abs(c.x-b.x))<=.10f&&Math.min(Math.abs(c.y-a.y),Math.abs(c.y-b.y))>=.055f&&Math.max(a.y,Math.max(b.y,c.y))-Math.min(a.y,Math.min(b.y,c.y))>=.15f)return true;
  }return false;
 }
}
