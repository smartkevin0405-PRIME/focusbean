package app.bean.focus;
public class TimerRulesTest {
 static int n;
 static void eq(long expected,long actual,String name){n++;if(expected!=actual)throw new AssertionError(name+": "+actual+" expected "+expected);}
 public static void main(String[] args){
  long delay=600000,elapsed=100000,wall=1700000000000L;
  eq(delay,TimerRules.remaining(elapsed+delay,wall+delay,delay,5,5,elapsed,wall),"new request");
  eq(1,TimerRules.remaining(elapsed+delay,wall+delay,delay,5,5,elapsed+delay-1,wall),"not ready before deadline");
  eq(0,TimerRules.remaining(elapsed+delay,wall+delay,delay,5,5,elapsed+delay,wall),"ready exactly at deadline");
  eq(0,TimerRules.remaining(elapsed+delay,wall+delay,delay,5,5,elapsed+delay+1,wall),"after deadline");
  eq(300000,TimerRules.remaining(elapsed+delay,wall+delay,delay,5,5,elapsed+300000,wall+90000000),"forward clock change same boot");
  eq(300000,TimerRules.remaining(elapsed+delay,wall+delay,delay,5,5,elapsed+300000,wall-90000000),"backward clock change same boot");
  eq(300000,TimerRules.remaining(elapsed+delay,wall+delay,delay,5,6,1000,wall+300000),"reboot wall restoration");
  eq(delay,TimerRules.remaining(elapsed+delay,wall+delay,delay,5,6,1000,wall-90000000),"reboot backward clock duration cap");
  eq(0,TimerRules.remaining(0,0,0,-1,5,elapsed,wall),"no active lock");
  eq(0,TimerRules.remaining(elapsed+delay,wall+delay,delay,5,6,1000,wall+delay+1),"expired during reboot");
  eq(300000,TimerRules.remaining(elapsed+delay,wall+delay,delay,-1,-1,elapsed,wall+300000),"boot count unavailable");
  eq(5400000,TimerRules.remaining(elapsed+5400000,wall+5400000,5400000,5,5,elapsed,wall),"90 minute focus session");
  System.out.println("Focus timer: "+n+" cases passed");
 }
}
