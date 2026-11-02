package app.bean.focus;
public final class XpMath {
 public static final String[] NAMES={"등속 · Linear","근호 · √","로그 · ln","파동 · sin","가우스 · erf","쌍곡선 · asinh","감마 · Γ","누적 · ∫"};
 public static final String[] EXPR={"50x","50(x+.64(√(1+x)−1))/C","50(x+.64 ln(1+x))/C","50(x+.64 sin x)/C","50(x+.64 erf x)/C","50(x+.64 asinh x)/C","50(x+.64 lnΓ(1+x)/(1+x))","50(x+.64 ∫₀ˣ du/(1+u⁴))/C"};
 private XpMath(){}
 public static double erf(double x){double z=Math.abs(x),t=1/(1+.3275911*z);double y=1-(((((1.061405429*t-1.453152027)*t)+1.421413741)*t-.284496736)*t+.254829592)*t*Math.exp(-z*z);return x<0?-y:y;}
 public static double logGamma(double z){double[] p={676.5203681218851,-1259.1392167224028,771.32342877765313,-176.61502916214059,12.507343278686905,-.13857109526572012,9.9843695780195716e-6,1.5056327351493116e-7};z-=1;double a=.99999999999980993;for(int i=0;i<p.length;i++)a+=p[i]/(z+i+1);double t=z+7.5;return .9189385332046727+(z+.5)*Math.log(t)-t+Math.log(a);}
 private static double integral(double x){int n=256;double h=x/n,sum=1+1/(1+x*x*x*x);for(int i=1;i<n;i++){double u=i*h;sum+=(i%2==0?2:4)/(1+u*u*u*u);}return sum*h/3;}
 private static double g(int mode,double x){switch(mode){case 1:return Math.sqrt(1+x)-1;case 2:return Math.log1p(x);case 3:return Math.sin(x);case 4:return erf(x);case 5:return Math.log(x+Math.sqrt(x*x+1));case 6:return logGamma(1+x)/(1+x);case 7:return integral(x);default:return 0;}}
 public static String name(int mode){return NAMES[Math.floorMod(mode,8)]+(mode<8?" · 기존 수식":"");}
 public static double value(int mode,double minutes){if(minutes<=0)return 0;double weight=mode>=8?.64:.06;mode=Math.floorMod(mode,8);double x=minutes/50.0;if(mode==0)return minutes;return Math.max(0,50*(x+weight*g(mode,x))/(1+weight*g(mode,1)));}
 public static double increment(int mode,double before,double after){return Math.max(0,value(mode,after)-value(mode,before));}
}
