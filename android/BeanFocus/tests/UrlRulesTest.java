package app.bean.focus;
public class UrlRulesTest {
 public static void main(String[] args){
  String[] yes={"https://m.youtube.com/shorts/abc","youtube.com/shorts/abc?x=1","https://www.youtube.com/shorts","http://youtube.com/shorts/x","HTTPS://YOUTUBE.COM/shorts/x","https://youtube.com/%73horts/abc"};
  String[] no={null,"","https://m.youtube.com/watch?v=abc","https://youtube.com/results?search_query=shorts","https://youtube.com/watch?v=abc&next=/shorts/x","https://youtube.com.evil.example/shorts/x","https://evilyoutube.com/shorts/x","https://youtube.com@evil.example/shorts/x","https://youtube.com/shortstuff","https://youtu.be/abc","javascript:alert(1)","//evil.example/shorts/x","Shorts"};
  for(String s:yes)if(!UrlRules.isShorts(s))throw new AssertionError("Miss: "+s);
  for(String s:no)if(UrlRules.isShorts(s))throw new AssertionError("False block: "+s);
  System.out.println("URL rules: "+(yes.length+no.length)+" cases passed");
 }
}
