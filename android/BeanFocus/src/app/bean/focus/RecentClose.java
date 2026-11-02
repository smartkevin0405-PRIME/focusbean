package app.bean.focus;
import java.util.*;
/** Narrow cleanup session: only recorded windows while a verified Recents UI has focus. */
final class RecentClose {
 static final long WINDOW=8000,COOLDOWN=120000,TRANSITION=600;
 private long started=-1,lastUse=-COOLDOWN;private boolean verified;
 private final Map<Integer,String> targets=new HashMap<>();
 RecentClose(long previousUse){lastUse=previousUse;}
 long cooldown(long now){return Math.max(0,COOLDOWN-(now-lastUse));}
 boolean request(long now,Map<Integer,String> windows){if(cooldown(now)>0||windows.isEmpty())return false;targets.clear();targets.putAll(windows);started=now;lastUse=now;verified=false;return true;}
 boolean active(long now){if(started<0)return false;if(now-started>=WINDOW||now<started){cancel();return false;}return true;}
 void observe(long now,boolean recents,boolean blockedForeground){if(!active(now))return;if(blockedForeground){if(verified||now-started>=TRANSITION)cancel();return;}if(!recents&&(verified||now-started>=TRANSITION)){cancel();return;}verified=recents;}
 boolean pause(long now,String pkg,int window,boolean pip,boolean youtube){return active(now)&&verified&&!pip&&!youtube&&pkg.equals(targets.get(window));}
 boolean suppressHome(long now,String pkg){return active(now)&&verified&&targets.containsValue(pkg);}
 void cancel(){started=-1;verified=false;targets.clear();}
 long lastUse(){return lastUse;}
}
