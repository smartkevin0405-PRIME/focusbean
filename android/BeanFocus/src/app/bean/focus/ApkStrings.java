package app.bean.focus;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;

/**
 * String pools of a binary AndroidManifest.xml and of a classes.dex, used only by the
 * Samsung Browser check to see which content blocker names the browser still carries.
 * No Android types, so it is unit-tested.
 */
final class ApkStrings {
 private ApkStrings(){}
 static final int MAX_LENGTH=160;

 /** Strings worth reporting: content blocker names, API versions and the browser's permission. */
 static boolean interesting(String s){
  if(s==null||s.isEmpty()||s.length()>MAX_LENGTH)return false;
  String l=s.toLowerCase(Locale.ROOT);
  return l.contains("contentblocker")||l.contains("content_blocker")||l.contains("adblock")||l.contains("ad_block")||l.contains("sbrowser.permission")||s.matches("API_[0-9]+(\\.[0-9]+)*");
 }

 /** Binary XML: chunk 0x0003, then a string pool chunk 0x0001 (UTF-8 or UTF-16). */
 static List<String> manifest(byte[] d,Predicate<String> keep){
  List<String> out=new ArrayList<>();
  try{
   if(d==null||d.length<36||u16(d,0)!=0x0003)return out;
   int pool=u16(d,2);if(u16(d,pool)!=0x0001)return out;
   int header=u16(d,pool+2),count=i32(d,pool+8),flags=i32(d,pool+16),start=i32(d,pool+20);
   boolean utf8=(flags&0x100)!=0;int base=pool+start;
   for(int i=0;i<count&&i<200000;i++){
    int q=base+i32(d,pool+header+i*4);String s;
    if(utf8){int[] chars=len8(d,q);int[] bytes=len8(d,chars[1]);s=new String(d,bytes[1],bytes[0],StandardCharsets.UTF_8);}
    else{int n=u16(d,q);q+=2;if((n&0x8000)!=0){n=((n&0x7fff)<<16)|u16(d,q);q+=2;}s=new String(d,q,n*2,StandardCharsets.UTF_16LE);}
    if(keep.test(s))out.add(s);
   }
  }catch(RuntimeException ignored){}
  return out;
 }

 /** Dex: string_ids_size at 0x38, string_ids_off at 0x3C; each entry points at uleb128 length + MUTF-8 bytes. */
 static List<String> dex(byte[] d,Predicate<String> keep){
  List<String> out=new ArrayList<>();
  try{
   if(d==null||d.length<0x70||d[0]!='d'||d[1]!='e'||d[2]!='x')return out;
   int size=i32(d,0x38),off=i32(d,0x3C);
   for(int i=0;i<size;i++){
    int q=uleb(d,i32(d,off+i*4));int end=q;while(end<d.length&&d[end]!=0)end++;
    // Modified UTF-8 differs from UTF-8 only for NUL and supplementary characters.
    if(end-q>MAX_LENGTH*3)continue;String s=new String(d,q,end-q,StandardCharsets.UTF_8);
    if(keep.test(s))out.add(s);
   }
  }catch(RuntimeException ignored){}
  return out;
 }

 private static int u16(byte[] d,int at){return (d[at]&0xff)|(d[at+1]&0xff)<<8;}
 private static int i32(byte[] d,int at){return (d[at]&0xff)|(d[at+1]&0xff)<<8|(d[at+2]&0xff)<<16|(d[at+3]&0xff)<<24;}
 /** {length, next offset}: one byte, or two when the high bit is set. */
 private static int[] len8(byte[] d,int at){int n=d[at]&0xff;if((n&0x80)!=0)return new int[]{((n&0x7f)<<8)|(d[at+1]&0xff),at+2};return new int[]{n,at+1};}
 /** Skips a uleb128 value and returns the next offset. */
 private static int uleb(byte[] d,int at){int i=0;while((d[at+i]&0x80)!=0&&i<4)i++;return at+i+1;}
}
