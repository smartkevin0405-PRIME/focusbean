package app.bean.focus;
final class InstallTimePolicy {
 static boolean afterStart(long start,long firstInstall){return start>0&&firstInstall>0&&firstInstall>start;}
}
