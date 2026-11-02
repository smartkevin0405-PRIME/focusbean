package app.bean.focus;
import android.content.*;import android.database.*;import android.net.Uri;import android.os.ParcelFileDescriptor;import java.io.*;
public class UpdateFileProvider extends ContentProvider {
 static File apk(Context c){return new File(c.getFilesDir(),"beanfocus-update.apk");}
 private void validate(Uri uri){if(!"/beanfocus-update.apk".equals(uri.getPath()))throw new IllegalArgumentException("Unknown update file");}
 public boolean onCreate(){return true;}
 public String getType(Uri uri){validate(uri);return "application/vnd.android.package-archive";}
 public Cursor query(Uri uri,String[] projection,String selection,String[] args,String order){validate(uri);String[] cols=projection==null?new String[]{"_display_name","_size"}:projection;MatrixCursor cursor=new MatrixCursor(cols);Object[] values=new Object[cols.length];for(int i=0;i<cols.length;i++)values[i]="_display_name".equals(cols[i])?"BeanFocus-update.apk":"_size".equals(cols[i])?apk(getContext()).length():null;cursor.addRow(values);return cursor;}
 public ParcelFileDescriptor openFile(Uri uri,String mode)throws FileNotFoundException{validate(uri);if(!"r".equals(mode))throw new FileNotFoundException("Read only");return ParcelFileDescriptor.open(apk(getContext()),ParcelFileDescriptor.MODE_READ_ONLY);}
 public Uri insert(Uri u,ContentValues v){throw new UnsupportedOperationException();}public int delete(Uri u,String s,String[] a){throw new UnsupportedOperationException();}public int update(Uri u,ContentValues v,String s,String[] a){throw new UnsupportedOperationException();}
}
