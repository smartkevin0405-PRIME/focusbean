package app.bean.focus;
import java.util.*;
public class RecentCloseTest {
 static int n;static void ok(boolean b){n++;if(!b)throw new AssertionError("case "+n);}
 public static void main(String[] args){
  long t=100000;Map<Integer,String> owner=new HashMap<>();owner.put(7,"game.one");RecentClose c=new RecentClose(-RecentClose.COOLDOWN);
  ok(c.request(t,owner));ok(!c.pause(t,"game.one",7,false,false)); // request alone never unmasks
  c.observe(t+100,true,false);ok(c.pause(t+100,"game.one",7,false,false));ok(c.suppressHome(t+100,"game.one"));
  ok(!c.pause(t+100,"game.other",7,false,false));ok(!c.pause(t+100,"game.one",8,false,false));ok(!c.pause(t+100,"game.one",7,true,false));ok(!c.pause(t+100,"game.one",7,false,true));
  c.observe(t+200,false,true);ok(!c.active(t+200));ok(!c.pause(t+201,"game.one",7,false,false));ok(!c.request(t+201,owner));
  ok(c.request(t+120000,owner));c.observe(t+120100,true,false);c.observe(t+120200,false,false);ok(!c.active(t+120200)); // Home is not Recents
  ok(c.request(t+240000,owner));c.observe(t+240700,false,false);ok(!c.active(t+240700)); // No recognized Recents: fail closed
  ok(c.request(t+360000,owner));c.observe(t+360001,true,false);ok(!c.pause(t+368000,"game.one",7,false,false));
  RecentClose restored=new RecentClose(c.lastUse());ok(!restored.request(t+360100,owner));ok(!restored.request(t+480000,Collections.emptyMap()));
  RecentClose pending=new RecentClose(-RecentClose.COOLDOWN);ok(pending.request(t,owner));pending.observe(t+100,false,true);ok(pending.active(t+100));ok(!pending.pause(t+100,"game.one",7,false,false));ok(!pending.suppressHome(t+100,"game.one"));pending.observe(t+601,false,true);ok(!pending.active(t+601));
  ok(InstallTimePolicy.afterStart(1000,1001));ok(!InstallTimePolicy.afterStart(1000,1000));ok(!InstallTimePolicy.afterStart(1000,999));ok(!InstallTimePolicy.afterStart(0,2000));ok(!InstallTimePolicy.afterStart(1000,0));
  System.out.println("Recent cleanup and install timestamps: "+n+" checks passed");
 }
}
