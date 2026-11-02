package app.bean.focus;
public class UpdateRulesTest {
 public static void main(String[] a){int n=0;String[] good={"https://example.org/BeanFocus-latest.apk","https://example.org/path?token=one"};for(String url:good){if(!UpdateRules.https(url))throw new AssertionError(url);n++;}for(String url:new String[]{"http://example.org/x","file:///x","https://user:pass@example.org/x","https://example.org/x#f","not url"}){if(UpdateRules.https(url))throw new AssertionError(url);n++;}
  if(!UpdateRules.compatible(DeviceConstants.OWN,UpdateRules.CERT,12,11))throw new AssertionError();n++;if(UpdateRules.compatible("other.app",UpdateRules.CERT,12,11))throw new AssertionError();n++;if(UpdateRules.compatible(DeviceConstants.OWN,"wrong",12,11))throw new AssertionError();n++;if(UpdateRules.compatible(DeviceConstants.OWN,UpdateRules.CERT,11,11))throw new AssertionError();n++;if(UpdateRules.compatible(DeviceConstants.OWN,UpdateRules.CERT,10,11))throw new AssertionError();n++;
  if(!UpdateRules.https(UpdateRules.DEFAULT_APK_URL)||!UpdateRules.address(null).equals(UpdateRules.DEFAULT_APK_URL)||!UpdateRules.address("  ").equals(UpdateRules.DEFAULT_APK_URL)||!UpdateRules.address(" https://example.org/app.apk ").equals("https://example.org/app.apk"))throw new AssertionError("default and override URL");n+=4;
  System.out.println("Update URL and identity policy: "+n+" checks passed");
 }
}
