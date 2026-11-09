package app.bean.focus;
import java.util.*;

/** Checks the Samsung Internet list with a small Adblock Plus "||host/path" matcher. */
public class ShortsFilterRulesTest {
 static int checks;static void ok(boolean b){checks++;if(!b)throw new AssertionError("case "+checks);}
 static List<String> rules(String text){List<String> out=new ArrayList<>();for(String line:text.split("\n")){line=line.trim();if(!line.isEmpty()&&!line.startsWith("!"))out.add(line);}return out;}
 // "||" anchors at a host or subdomain boundary; the remainder is a plain prefix.
 static boolean blocks(List<String> rules,String url){
  String rest=url.replaceFirst("^https?://","");
  for(String rule:rules){
   if(!rule.startsWith("||"))continue;String pattern=rule.substring(2);
   for(int i=0;i<rest.length();i++){
    if(i>0&&rest.charAt(i-1)!='.')continue;
    if(rest.startsWith(pattern,i)&&rest.indexOf('/')>=i)return true;
   }
  }return false;
 }
 public static void main(String[] args){
  List<String> on=rules(ShortsFilterRules.text(true)),off=rules(ShortsFilterRules.text(false));
  ok(off.isEmpty());ok(!on.isEmpty());
  for(String shorts:new String[]{"https://m.youtube.com/shorts/abc","https://www.youtube.com/shorts/abc?feature=share","https://youtube.com/shorts","https://m.youtube.com/youtubei/v1/reel/reel_item_watch?prettyPrint=false","https://www.youtube.com/youtubei/v1/reel/reel_watch_sequence"})ok(blocks(on,shorts));
  for(String allowed:new String[]{"https://m.youtube.com/","https://m.youtube.com/watch?v=lesson","https://m.youtube.com/results?search_query=shorts","https://m.youtube.com/@channel/shorts","https://m.youtube.com/youtubei/v1/player","https://m.youtube.com/youtubei/v1/next","https://m.youtube.com/youtubei/v1/browse","https://youtu.be/abc","https://example.org/youtube.com/shorts/x","https://notyoutube.com/shorts/x","https://www.google.com/search?q=youtube+shorts"})ok(!blocks(on,allowed));
  for(String off1:new String[]{"https://m.youtube.com/shorts/abc","https://m.youtube.com/youtubei/v1/reel/reel_item_watch"})ok(!blocks(off,off1));
  // Never a whole-domain or document-wide rule; cosmetic rules stay on YouTube.
  for(String rule:on){ok(!rule.equals("||youtube.com^")&&!rule.equals("||youtube.com/")&&!rule.contains("$"));if(rule.contains("##"))ok(rule.startsWith("youtube.com##")&&!rule.contains(":has"));}
  ok(ShortsFilterRules.changed(-1,true));ok(ShortsFilterRules.changed(-1,false));ok(ShortsFilterRules.changed(0,true));ok(ShortsFilterRules.changed(1,false));ok(!ShortsFilterRules.changed(1,true));ok(!ShortsFilterRules.changed(0,false));
  // Re-announce on state change, at first sync, and once the interval passes; not on every beat.
  long h=ShortsFilterRules.REANNOUNCE_MS;ok(ShortsFilterRules.announce(-1,true,0,5));ok(ShortsFilterRules.announce(1,false,1000,1001));
  ok(!ShortsFilterRules.announce(1,true,1000,1000+2000));ok(!ShortsFilterRules.announce(1,true,1000,1000+h-1));ok(ShortsFilterRules.announce(1,true,1000,1000+h));ok(ShortsFilterRules.announce(0,false,1000,1000+h+1));
  System.out.println("Samsung Internet Shorts filter list: "+checks+" checks passed");
 }
}
