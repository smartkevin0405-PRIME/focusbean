package app.bean.focus;
public final class GrowthRules {
 public static final long[] THRESHOLDS={0,50,150,400,1000};
 public static final String[] NAMES={"씨앗 연구자","탐구 연구자","파동 연구자","양자 연구자","PrimeBean"};
 private GrowthRules(){}
 public static long credit(long elapsed,boolean previous,boolean current){return previous&&current&&elapsed>0&&elapsed<=90000?elapsed:0;}
 public static int level(long minutes){int l=0;for(int i=1;i<THRESHOLDS.length;i++)if(minutes>=THRESHOLDS[i])l=i;return l;}
 public static boolean styleUnlocked(int style,long minutes){return style==0||style==1&&minutes>=50||style==2&&minutes>=150||style==3&&minutes>=400;}
}
