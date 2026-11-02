package app.bean.focus;
public class WindowPolicyTest {
 static int n;static void ok(boolean b){n++;if(!b)throw new AssertionError("case "+n);}
 public static void main(String[] a){
  for(int v=0;v<64;v++){boolean blocked=(v&1)>0,visible=(v&2)>0,pip=(v&4)>0,active=(v&8)>0,pause=(v&16)>0,yt=(v&32)>0;WindowPolicy.Decision d=WindowPolicy.decide(blocked,visible,pip,active,pause,yt);
   ok((!blocked||!visible)?d==WindowPolicy.Decision.NONE:pip?d==WindowPolicy.Decision.SHIELD_DISMISS_PIP:pause&&!yt?d==WindowPolicy.Decision.NONE:active?d==WindowPolicy.Decision.SHIELD_HOME:d==WindowPolicy.Decision.SHIELD);
  }
  ok(!WindowPolicy.globalAllowed(DeviceConstants.OWN,"game"));for(String p:DeviceConstants.EMERGENCY)ok(!WindowPolicy.globalAllowed(p,p));ok(!WindowPolicy.globalAllowed("settings","game"));ok(WindowPolicy.globalAllowed("game","game"));
  PipDismissTracker p=new PipDismissTracker();ok(p.shouldAttempt(0));p.attempted(0);ok(!p.shouldAttempt(100));ok(p.shouldAttempt(1500));p.attempted(1500);ok(p.shouldAttempt(3000));p.attempted(3000);ok(!p.shouldAttempt(4500));ok(p.failed(4500));ok(!p.shouldAttempt(64499));ok(p.shouldAttempt(64500));
  System.out.println("Window and PiP policy: "+n+" checks passed");
 }
}
