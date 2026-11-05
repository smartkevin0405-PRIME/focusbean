package app.bean.focus;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import java.io.*;

/**
 * Read-only filter list for Samsung Internet (authority "<package>.contentBlocker.contentProvider").
 * It exposes only the static Shorts rules from ShortsFilterRules; no settings or usage data.
 */
public class ShortsFilterProvider extends ContentProvider {
 @Override public boolean onCreate(){return true;}
 @Override public ParcelFileDescriptor openFile(Uri uri,String mode) throws FileNotFoundException {
  if(mode==null||!mode.startsWith("r")||mode.indexOf('w')>=0||mode.indexOf('a')>=0||mode.indexOf('t')>=0)throw new FileNotFoundException("read-only filter list");
  Context c=getContext();if(c==null)throw new FileNotFoundException("not ready");
  try{
   File list=SamsungContentBlocker.write(c);
   String caller=null;try{caller=getCallingPackage();}catch(RuntimeException ignored){}
   SamsungContentBlocker.served(c,caller);
   return ParcelFileDescriptor.open(list,ParcelFileDescriptor.MODE_READ_ONLY);
  }catch(IOException e){throw new FileNotFoundException("filter list unavailable");}
 }
 @Override public String getType(Uri uri){return "text/plain";}
 @Override public Cursor query(Uri uri,String[] projection,String selection,String[] args,String sort){return null;}
 @Override public Uri insert(Uri uri,ContentValues values){return null;}
 @Override public int update(Uri uri,ContentValues values,String selection,String[] args){return 0;}
 @Override public int delete(Uri uri,String selection,String[] args){return 0;}
}
