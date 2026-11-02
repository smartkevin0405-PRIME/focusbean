package app.bean.focus;
import java.util.*;
public class LedgerShortcutTest {
 public static void main(String[] a){int n=0;Set<String> initial=new HashSet<>();initial.add("old");Set<String> records=NewPackageLedger.add(initial,"previous","current","new.game");if(!initial.contains("old")||records.contains("old"))throw new AssertionError();n++;
  if(!NewPackageLedger.contains("current","current",records,"new.game"))throw new AssertionError();n++;if(NewPackageLedger.contains("different","current",records,"new.game"))throw new AssertionError();n++;
  if(!(NewPackageLedger.contains("current","current",records,"new.game")||InstallTimePolicy.afterStart(2000,1000)))throw new AssertionError("clock rollback bypass");n++;
  ShortcutCheck.Result r=ShortcutCheck.inspect(k->k.equals(DeviceConstants.SHORTCUT_KEYS[1])?DeviceConstants.OWN+"/.FocusService":"");if(!r.registered||r.failed)throw new AssertionError();n++;
  r=ShortcutCheck.inspect(k->{throw new SecurityException();});if(r.registered||!r.failed)throw new AssertionError();n++;
  r=ShortcutCheck.inspect(k->null);if(r.registered||r.failed)throw new AssertionError();n++;
  r=ShortcutCheck.inspect(k->{if(k.equals(DeviceConstants.SHORTCUT_KEYS[0]))throw new SecurityException();return DeviceConstants.OWN;});if(!r.registered||!r.failed)throw new AssertionError();n++;
  System.out.println("New-package ledger and shortcuts: "+n+" checks passed");
 }
}
