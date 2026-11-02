package app.bean.focus;
public class GrowthRulesTest {
 static int n;
 static void eq(long e,long a){n++;if(e!=a)throw new AssertionError("expected "+e+" got "+a);}
 public static void main(String[] args){
  eq(0,GrowthRules.credit(30000,false,true));eq(0,GrowthRules.credit(30000,true,false));eq(0,GrowthRules.credit(30000,false,false));eq(30000,GrowthRules.credit(30000,true,true));eq(90000,GrowthRules.credit(90000,true,true));eq(0,GrowthRules.credit(90001,true,true));eq(0,GrowthRules.credit(-1,true,true));eq(0,GrowthRules.credit(0,true,true));
  eq(0,GrowthRules.level(49));eq(1,GrowthRules.level(50));eq(1,GrowthRules.level(149));eq(2,GrowthRules.level(150));eq(3,GrowthRules.level(400));eq(4,GrowthRules.level(1000));eq(4,GrowthRules.level(10000));
  eq(0,GrowthRules.styleUnlocked(1,49)?1:0);eq(1,GrowthRules.styleUnlocked(1,50)?1:0);eq(0,GrowthRules.styleUnlocked(3,399)?1:0);eq(1,GrowthRules.styleUnlocked(3,400)?1:0);eq(0,GrowthRules.styleUnlocked(5,9999)?1:0);
  System.out.println("Growth rules: "+n+" checks passed");
 }
}
