package app.bean.focus;
/** Explicit policy boundaries, shared by polling and event handling. */
final class BlockPolicy {
 static boolean game(boolean enabled,boolean selected,boolean locked,boolean categoryGame){return enabled&&(selected||(locked&&categoryGame));}
 static boolean unknownBrowser(boolean shorts,boolean locked,boolean strict,boolean browser,boolean supported,boolean embeddedHost){return !embeddedHost&&unknownBrowser(shorts,locked,strict,browser,supported);}
 static boolean unknownBrowser(boolean shorts,boolean locked,boolean strict,boolean browser,boolean supported){return shorts&&locked&&strict&&browser&&!supported;}
 static boolean webShorts(String url,boolean editing){return !editing&&UrlRules.isShorts(url);}
 // A click or a shortened YouTube host alone never proves what is currently open.
 static boolean browserShorts(String url,boolean editing,boolean playerShorts,boolean confirmedHome){return !editing&&!confirmedHome&&(UrlRules.isShorts(url)||playerShorts);}

 static boolean homeAfterBack(String url,boolean clicked,boolean editing,boolean confirmedHome){return !confirmedHome&&homeAfterBack(url,clicked,editing);}
 static boolean homeAfterBack(String url,boolean clicked,boolean editing){
  return webShorts(url,editing);
 }
 static boolean newApp(boolean enabled,boolean locked,boolean option,boolean matchingBaseline,boolean existed,boolean system,boolean essential){return enabled&&locked&&option&&matchingBaseline&&!existed&&!system&&!essential;}
 static boolean otherProfile(boolean locked,boolean option,boolean missing,boolean essential){return locked&&option&&missing&&!essential;}
 static boolean sameWindow(String expected,int expectedWindow,String actual,int actualWindow){return expected.equals(actual)&&expectedWindow==actualWindow;}
}
