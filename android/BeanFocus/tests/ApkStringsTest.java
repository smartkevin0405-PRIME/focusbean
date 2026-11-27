package app.bean.focus;
import java.io.ByteArrayOutputStream;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Hand-built binary manifest (UTF-8 and UTF-16 pools) and dex string tables. */
public class ApkStringsTest {
 static int checks;static void ok(boolean b){checks++;if(!b)throw new AssertionError("case "+checks);}
 static byte[] manifest(boolean utf8,String... strings){
  ByteArrayOutputStream data=new ByteArrayOutputStream();int[] offsets=new int[strings.length];
  for(int i=0;i<strings.length;i++){
   offsets[i]=data.size();
   if(utf8){byte[] b=strings[i].getBytes(StandardCharsets.UTF_8);data.write(strings[i].length());data.write(b.length);data.write(b,0,b.length);data.write(0);}
   else{byte[] b=strings[i].getBytes(StandardCharsets.UTF_16LE);data.write(strings[i].length()&0xff);data.write(strings[i].length()>>8);data.write(b,0,b.length);data.write(0);data.write(0);}
  }
  int header=28,start=header+4*strings.length,pool=start+data.size();
  ByteBuffer out=ByteBuffer.allocate(8+pool).order(ByteOrder.LITTLE_ENDIAN);
  out.putShort((short)3).putShort((short)8).putInt(8+pool);
  out.putShort((short)1).putShort((short)header).putInt(pool).putInt(strings.length).putInt(0).putInt(utf8?0x100:0).putInt(start).putInt(0);
  for(int o:offsets)out.putInt(o);out.put(data.toByteArray());return out.array();
 }
 static byte[] dex(String... strings){
  ByteArrayOutputStream data=new ByteArrayOutputStream();int base=0x70+4*strings.length;int[] offsets=new int[strings.length];
  for(int i=0;i<strings.length;i++){offsets[i]=base+data.size();byte[] b=strings[i].getBytes(StandardCharsets.UTF_8);data.write(strings[i].length());data.write(b,0,b.length);data.write(0);}
  ByteBuffer out=ByteBuffer.allocate(base+data.size()).order(ByteOrder.LITTLE_ENDIAN);
  out.put("dex\n035\0".getBytes(StandardCharsets.US_ASCII));out.position(0x38);out.putInt(strings.length).putInt(0x70);out.position(0x70);
  for(int o:offsets)out.putInt(o);out.put(data.toByteArray());return out.array();
 }
 public static void main(String[] args){
  String[] names={"android","com.samsung.android.sbrowser.contentBlocker.interfaceVersion","API_1.0","API_2.1","com.samsung.android.sbrowser.permission.CONTENTBLOCKER","광고 차단","AdBlockManager","normal.string","API_x"};
  for(boolean utf8:new boolean[]{true,false}){
   List<String> all=ApkStrings.manifest(manifest(utf8,names),s->true);ok(all.equals(Arrays.asList(names)));
   List<String> kept=ApkStrings.manifest(manifest(utf8,names),ApkStrings::interesting);
   ok(kept.equals(Arrays.asList("com.samsung.android.sbrowser.contentBlocker.interfaceVersion","API_1.0","API_2.1","com.samsung.android.sbrowser.permission.CONTENTBLOCKER","AdBlockManager")));
  }
  ok(ApkStrings.dex(dex(names),s->true).equals(Arrays.asList(names)));
  ok(ApkStrings.dex(dex(names),ApkStrings::interesting).size()==5);
  // Not a manifest or dex, truncated data, and overlong strings give nothing, never an exception.
  ok(ApkStrings.manifest(new byte[]{1,2,3},s->true).isEmpty());ok(ApkStrings.dex("not a dex file at all, just text".getBytes(StandardCharsets.US_ASCII),s->true).isEmpty());
  byte[] cut=Arrays.copyOf(dex(names),0x80);ok(ApkStrings.dex(cut,s->true).size()<names.length);
  char[] longName=new char[ApkStrings.MAX_LENGTH+1];Arrays.fill(longName,'a');ok(!ApkStrings.interesting("contentBlocker"+new String(longName)));ok(!ApkStrings.interesting(null));
  System.out.println("APK string pools: "+checks+" checks passed");
 }
}
