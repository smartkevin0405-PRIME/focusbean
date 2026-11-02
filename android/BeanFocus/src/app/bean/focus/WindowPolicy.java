package app.bean.focus;
final class WindowPolicy {
 enum Decision {NONE,SHIELD,SHIELD_DISMISS_PIP,SHIELD_HOME}
 static Decision decide(boolean blocked,boolean visible,boolean pip,boolean activeOrFocused,boolean recentPause,boolean youtube){
  if(!blocked||!visible)return Decision.NONE;
  if(pip)return Decision.SHIELD_DISMISS_PIP;
  if(recentPause&&!youtube)return Decision.NONE;
  return activeOrFocused?Decision.SHIELD_HOME:Decision.SHIELD;
 }
 static boolean globalAllowed(String active,String expected){return active!=null&&active.equals(expected)&&!DeviceConstants.OWN.equals(active)&&!DeviceConstants.emergency(active);}
}
