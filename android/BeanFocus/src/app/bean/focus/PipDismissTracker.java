package app.bean.focus;
final class PipDismissTracker {
 private long attempted=-1,coolUntil;private boolean pending;private int failures;
 boolean shouldAttempt(long now){
  if(now<coolUntil)return false;
  if(pending&&now-attempted>=1500){pending=false;failures++;if(failures>=3){coolUntil=now+60000;failures=0;return false;}}
  return !pending&&(attempted<0||now-attempted>=1500);
 }
 void attempted(long now){attempted=now;pending=true;}
 boolean failed(long now){return now<coolUntil;}
 boolean wasAttempted(){return attempted>=0;}
}
