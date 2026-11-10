package app.bean.focus;

/**
 * Filter list served to Samsung Internet's content blocker (Adblock Plus syntax).
 * It names only YouTube Shorts pages and the Shorts player API ("reel" endpoints).
 * Normal videos (/watch, youtubei/v1/player, youtubei/v1/next), search, home and
 * other sites are never listed. No Android types, so the list is unit-tested.
 */
final class ShortsFilterRules {
 private ShortsFilterRules(){}
 // 실기기 확인 필요: rule support depends on the Samsung Internet version.
 static final String[] BLOCK={"||youtube.com/shorts","||youtube.com/youtubei/v1/reel/"};
 static final String PLACEHOLDER="||beanfocus.invalid^";
 static final String[] HIDE={"youtube.com##ytm-reel-shelf-renderer","youtube.com##ytd-reel-shelf-renderer","youtube.com##ytm-shorts-lockup-view-model"};
 static String text(boolean active){
  StringBuilder out=new StringBuilder();
  out.append("! Title: BeanFocus - YouTube Shorts only\n");
  out.append("! Blocks Shorts pages and Shorts player requests. Normal videos and other sites stay allowed.\n");
  // Never serve a list with no rules: Samsung Internet may drop an empty blocker from its list.
  // The reserved .invalid domain never resolves, so this rule blocks nothing.
  if(!active){out.append("! Shorts blocking is off in BeanFocus.\n").append(PLACEHOLDER).append('\n');return out.toString();}
  for(String rule:BLOCK)out.append(rule).append('\n');
  for(String rule:HIDE)out.append(rule).append('\n');
  return out.toString();
 }
 /** last: -1 never sent in this process, 0 off, 1 on. */
 static boolean changed(int last,boolean active){return last!=(active?1:0);}
 /** Repeat the update notice so Samsung Internet re-reads the list after its own updates or restarts. */
 static final long REANNOUNCE_MS=6L*60*60*1000;
 /** lastAt and now are elapsed-realtime millis of this process. */
 static boolean announce(int last,boolean active,long lastAt,long now){return changed(last,active)||now-lastAt>=REANNOUNCE_MS;}
}
