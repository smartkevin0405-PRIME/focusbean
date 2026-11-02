package app.bean.focus;
public class BlockPolicyTest {
 static int n;static void ok(boolean v){n++;if(!v)throw new AssertionError("case "+n);}
 public static void main(String[] a){
  ok(BlockPolicy.game(true,true,false,false));ok(!BlockPolicy.game(false,true,true,true));ok(BlockPolicy.game(true,false,true,true));ok(!BlockPolicy.game(true,false,false,true));ok(!BlockPolicy.game(true,false,true,false));
  ok(BlockPolicy.unknownBrowser(true,true,true,true,false));ok(!BlockPolicy.unknownBrowser(true,false,true,true,false));ok(!BlockPolicy.unknownBrowser(true,true,false,true,false));ok(!BlockPolicy.unknownBrowser(true,true,true,true,true));ok(!BlockPolicy.unknownBrowser(false,true,true,true,false));
  ok(!BlockPolicy.webShorts("m.youtube.com",false));ok(!BlockPolicy.webShorts("https://youtube.com/",false));ok(!BlockPolicy.webShorts("https://youtube.com/watch?v=x",false));ok(!BlockPolicy.webShorts("https://example.com",false));ok(BlockPolicy.webShorts("youtube.com/shorts/x",false));ok(!BlockPolicy.webShorts("youtube.com/shorts/x",true));
  ok(BlockPolicy.sameWindow("chrome",2,"chrome",2));ok(!BlockPolicy.sameWindow("chrome",2,"settings",2));ok(!BlockPolicy.sameWindow("chrome",2,"chrome",3));
  ok(BlockPolicy.homeAfterBack("youtube.com/shorts/x",false,false));ok(!BlockPolicy.homeAfterBack("youtube.com",false,false));ok(!BlockPolicy.homeAfterBack("",true,false));ok(!BlockPolicy.homeAfterBack("",false,false));ok(!BlockPolicy.homeAfterBack("example.com",true,false));ok(!BlockPolicy.homeAfterBack("youtube.com/watch?v=x",true,false));ok(!BlockPolicy.homeAfterBack("youtube.com/shorts/x",true,true));
  ok(!BlockPolicy.homeAfterBack("https://youtube.com/",true,false,true));ok(!BlockPolicy.homeAfterBack("youtube.com",true,false,false));ok(!BlockPolicy.unknownBrowser(true,true,true,true,false,true));ok(BlockPolicy.unknownBrowser(true,true,true,true,false,false));
  ok(BlockPolicy.newApp(true,true,true,true,false,false,false));ok(!BlockPolicy.newApp(true,true,false,true,false,false,false));ok(!BlockPolicy.newApp(true,true,true,false,false,false,false));ok(!BlockPolicy.newApp(true,true,true,true,true,false,false));ok(!BlockPolicy.newApp(true,true,true,true,false,true,false));ok(!BlockPolicy.newApp(true,true,true,true,false,false,true));ok(!BlockPolicy.newApp(true,false,true,true,false,false,false));ok(!BlockPolicy.newApp(false,true,true,true,false,false,false));
  System.out.println("Block policy: "+n+" checks passed");
 }
}
