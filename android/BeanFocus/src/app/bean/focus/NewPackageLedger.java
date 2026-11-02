package app.bean.focus;
import java.util.*;
final class NewPackageLedger {
 static Set<String> add(Set<String> old,String oldId,String lockId,String pkg){Set<String> copy=oldId.equals(lockId)?new HashSet<>(old):new HashSet<>();copy.add(pkg);return copy;}
 static boolean contains(String id,String recordedId,Set<String> records,String pkg){return !id.isEmpty()&&id.equals(recordedId)&&records.contains(pkg);}
}
