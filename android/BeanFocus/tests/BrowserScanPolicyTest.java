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
  // Home was just inspected. A click must allow fresh player evidence without
  // permitting two full traversals in the same shared 500ms slot.
  p.reset();p.scanned(0,"samsung",1);p.playerScanned(0);
  p.navigation(100,"samsung",1);
  ok(p.navigating(450));ok(!p.allowPlayer(450));ok(p.delay(450,"samsung",1)==50);
  ok(p.allowPlayer(500));p.scanned(500,"samsung",1);p.playerScanned(500);
  ok(p.retryDelay(500,"samsung",1)==500);
  ok(!p.allowPlayer(999));ok(p.allowPlayer(1000));
  // Repeated content events and watchdog reads do not extend a navigation burst.
  players=1;
  for(long now=501;now<=8000;now++)if(p.delay(now,"samsung",1)==0){
   p.scanned(now,"samsung",1);
   if(p.allowPlayer(now)){p.playerScanned(now);players++;}
  }
  ok(players==7); // six burst scans through 3000ms, one ordinary scan at 8000ms
  ok(!p.navigating(3101));ok(p.retryDelay(8000,"samsung",1)==-1);
  p.navigation(9000,"samsung",1);ok(p.navigating(9000));
  ok(p.retryDelay(9000,"samsung",2)==-1);
  p.scanned(9001,"samsung",2);ok(!p.navigating(9001));ok(p.allowPlayer(9001));
  p.navigation(9100,"samsung",2);p.endNavigation();ok(!p.navigating(9100));
  p.navigation(9200,"samsung",2);p.reset();ok(!p.navigating(9200));
  // An event without a window ID binds to the first real window and keeps its deadline.
  p.reset();p.navigation(0,"samsung",-1);ok(p.navigating(0));ok(p.matches("samsung",7));
  p.scanned(450,"samsung",7);ok(p.navigating(450));ok(p.matches("samsung",7));ok(!p.matches("samsung",8));
  ok(p.retryDelay(450,"samsung",7)==500);
  // Later -1 events in a bound scope keep the shared budget instead of resetting it.
  p.navigation(600,"samsung",-1);ok(p.delay(600,"samsung",-1)==350);p.scanned(950,"samsung",7);ok(p.navigating(950));
  // -1 followed by another real window: fresh budget for that window, deadline kept.
  p.navigation(1000,"samsung",-1);p.scanned(1100,"samsung",9);ok(p.navigating(1100));ok(p.matches("samsung",9));ok(!p.matches("samsung",7));
  p.navigation(1200,"chrome",-1);ok(!p.matches("samsung",9));ok(p.navigating(1200));
  // A briefly missing root is retried only inside the same scope's remaining deadline.
  p.reset();p.navigation(0,"samsung",3);ok(p.missingRootDelay(100,"samsung",3)==150);ok(p.missingRootDelay(100,"samsung",-1)==150);
  ok(p.missingRootDelay(100,"samsung",4)==-1);ok(p.missingRootDelay(100,"chrome",3)==-1);ok(p.missingRootDelay(2850,"samsung",3)==150);ok(p.missingRootDelay(2900,"samsung",3)==-1);
  p.endNavigation();ok(p.missingRootDelay(200,"samsung",3)==-1);
  // Host memory is a tri-state per window, cleared by any scope change.
  p.reset();p.scanned(0,"samsung",3);ok(p.host()==0);p.rememberHost(1);ok(p.host()==1);p.rememberHost(0);ok(p.host()==1);p.rememberHost(-1);ok(p.host()==-1);
  p.rememberHost(5);ok(p.host()==1);p.scanned(600,"samsung",4);ok(p.host()==0);p.rememberHost(1);p.scanned(1200,"chrome",4);ok(p.host()==0);p.rememberHost(1);p.reset();ok(p.host()==0);
  System.out.println("Browser scan budget: "+checks+" checks passed");
 }
}
