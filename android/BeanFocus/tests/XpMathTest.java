package app.bean.focus;
public class XpMathTest {
 public static void main(String[] args){int count=0;double[] previous=new double[8];double worst=1;
  for(int step=1;step<=144000;step++){double t=step*.01,min=Double.POSITIVE_INFINITY,max=0;for(int i=0;i<8;i++){double v=XpMath.value(i+8,t);if(!Double.isFinite(v)||v<=previous[i])throw new AssertionError("not increasing "+i+" at "+t);previous[i]=v;min=Math.min(min,v);max=Math.max(max,v);count++;}worst=Math.max(worst,max/min);if(max/min>2)throw new AssertionError("pairwise balance exceeds 2 at "+t);}
  for(int i=0;i<16;i++){double split=XpMath.increment(i,0,30)+XpMath.increment(i,30,75)+XpMath.increment(i,75,120);if(Math.abs(split-XpMath.value(i,120))>1e-8)throw new AssertionError("split session exploit");}
  if(Math.abs(XpMath.value(11,120)-XpMath.value(3,120))<1)throw new AssertionError("old/new epochs not distinct");
  if(Math.abs(XpMath.value(3,120)-116.16)>.01)throw new AssertionError("legacy formula changed");
  System.out.println("XP grid: "+count+" checks; monotonic, pairwise ratio <=2, legacy epoch preserved, split-session invariant passed");
 }
}
