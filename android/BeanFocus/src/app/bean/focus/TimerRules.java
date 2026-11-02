package app.bean.focus;
public final class TimerRules {
 private TimerRules(){}
 public static long remaining(long endElapsed,long endWall,long duration,int boot,int currentBoot,long elapsed,long wall){
  return remaining(endElapsed,endWall,-1,duration,boot,currentBoot,elapsed,wall,-1);
 }
 public static long remaining(long endElapsed,long endWall,long endNet,long duration,int boot,int currentBoot,long elapsed,long wall,long net){
  if(duration<=0)return 0;
  long left=boot>=0&&boot==currentBoot ? endElapsed-elapsed : endNet>=0&&net>=0?endNet-net:endWall-wall;
  return Math.max(0,Math.min(duration,left));
 }
}
