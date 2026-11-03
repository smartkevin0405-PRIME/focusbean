package app.bean.focus;

public class BrowserScanPolicyTest {
 static int checks;
 static void ok(boolean value){checks++;if(!value)throw new AssertionError("case "+checks);}
 public static void main(String[] args){
  BrowserScanPolicy p=new BrowserScanPolicy();
  ok(p.delay(0,"samsung",1)==0);p.scanned(0,"samsung",1);
  ok(p.allowPlayer(0));p.playerScanned(0);
  ok(p.delay(100,"samsung",1)==400);
  ok(p.delay(499,"samsung",1)==1);
  ok(p.delay(500,"samsung",1)==0);
  // An event flood plus a watchdog cannot run more than one scan per 500ms.
  int scans=1,players=1;
  for(long now=1;now<10000;now++)if(p.delay(now,"samsung",1)==0){
   p.scanned(now,"samsung",1);scans++;
   if(p.allowPlayer(now)){p.playerScanned(now);players++;}
  }
  ok(scans==20);ok(players==2);
  // A real foreground/window change gets a fresh budget; stale scope isn't reusable.
  ok(p.delay(9999,"chrome",1)==0);p.scanned(9999,"chrome",1);
  ok(p.allowPlayer(9999));p.playerScanned(9999);
  ok(p.delay(10000,"chrome",2)==0);p.scanned(10000,"chrome",2);
  ok(p.allowPlayer(10000));ok(!p.matches("samsung",1));
  p.reset();ok(p.delay(10001,"chrome",2)==0);
  System.out.println("Browser scan budget: "+checks+" checks passed");
 }
}
