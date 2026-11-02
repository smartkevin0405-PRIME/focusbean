package app.bean.focus;
import java.util.*;
/** OEM/UI/package values below: 실기기 확인 필요. No device dump has yet verified these candidates. */
final class DeviceConstants {
 static final String[] ADDRESS_FIELDS={"url_bar","location_bar_edit_text","location_bar_text_view","custom_tab_toolbar_url_bar_text","omnibarTextInput","mozac_browser_toolbar_url_view","mozac_browser_toolbar_url_edit_view","mozac_browser_toolbar_origin_view","toolbar_url","url","url_field","url_view","url_text","address_bar","address_field","address_text","omnibox"};
 static String[] fieldsFor(String pkg){
  if(pkg.startsWith("com.sec.android.app.sbrowser"))return new String[]{"location_bar_edit_text","location_bar_text_view","custom_tab_toolbar_url_bar_text","url_bar"};
  if(pkg.startsWith("org.mozilla."))return new String[]{"mozac_browser_toolbar_url_view","mozac_browser_toolbar_url_edit_view","mozac_browser_toolbar_origin_view","toolbar_url","url_bar"};
  if(pkg.equals("com.duckduckgo.mobile.android"))return new String[]{"omnibarTextInput"};
  if(pkg.equals("com.naver.whale")||pkg.startsWith("com.android.chrome")||pkg.startsWith("com.chrome.")||pkg.startsWith("com.brave.")||pkg.equals("com.microsoft.emmx"))return new String[]{"url_bar","url"};
  return ADDRESS_FIELDS;
 }

 static final String[] BROWSERS={"com.sec.android.app.sbrowser","com.sec.android.app.sbrowser.beta","com.android.chrome","com.chrome.beta","com.chrome.dev","com.chrome.canary","com.microsoft.emmx","com.brave.browser","org.mozilla.firefox","org.mozilla.firefox_beta","org.mozilla.fenix","org.mozilla.focus","com.naver.whale","com.opera.browser","com.opera.mini.native","com.kiwibrowser.browser","com.vivaldi.browser","com.duckduckgo.mobile.android"};
 static boolean webHost(String pkg){return "com.kakao.talk".equals(pkg)||"com.nhn.android.search".equals(pkg)||"com.naver.android.main".equals(pkg)||"com.google.android.googlequicksearchbox".equals(pkg)||"com.instagram.android".equals(pkg)||"com.discord".equals(pkg)||"com.everytime.v2".equals(pkg)||"com.everytime".equals(pkg);}
 static final String OWN="app.bean.focus",SETTINGS="com.android.settings",SAMSUNG_ACCESS="com.samsung.accessibility";
 static final Set<String> SURFACES=set(SETTINGS,"com.samsung.android.settings",SAMSUNG_ACCESS,"com.samsung.android.lool","com.samsung.android.sm","com.samsung.android.app.routines","com.android.packageinstaller","com.google.android.packageinstaller","com.samsung.android.packageinstaller");
 static final Set<String> CARE=set("com.samsung.android.lool","com.samsung.android.sm","com.samsung.android.app.routines");
 static final Set<String> EMERGENCY=set("com.android.dialer","com.android.incallui","com.samsung.android.incallui","com.android.phone","com.android.server.telecom","com.samsung.android.dialer","com.google.android.dialer","com.android.systemui");
 static final Set<String> RECENTS_HOSTS=set("com.sec.android.app.launcher","com.android.launcher3","com.google.android.apps.nexuslauncher","com.android.systemui");
 static final String[] RECENTS_IDS={"recents_view","overview_panel","overview_actions_view","recents_root","recents_container","recents_view_container","recent_apps_root"};
 static final String RECENTS_CLASS="com.android.quickstep.views.RecentsView";
 static final Set<String> YOUTUBE=set("com.google.android.youtube","app.revanced.android.youtube","app.rvx.android.youtube","app.morphe.android.youtube","com.vanced.android.youtube");
 static final String[] PLAYERS={"reel_recycler","reel_player_page_container","reel_watch_fragment_root"};
 // 실기기 확인 필요: Samsung Internet/YouTube web accessibility labels and HTML IDs; screenshots alone do not confirm node values.
 static final Set<String> WEB_PLAYER_IDS=set("shorts-player","reel-player");
 static final String[] WEB_LIKE_LABELS={"좋아요","like"},WEB_COMMENT_LABELS={"댓글","comment"},WEB_SHARE_LABELS={"공유","share"};
 static final String[] WEB_DISLIKE_LABELS={"dislike","싫어요"};
 static final Set<String> WEB_SUBSCRIBE_LABELS=set("구독","구독중","구독 중","subscribe","subscribed");
 static final Set<String> TITLE_IDS=set("entity_header_title","app_name","app_title","toolbar_title","action_bar_title","collapsing_toolbar_title","large_title","small_title","sub_appbar_title","header_title");
 static final Set<String> SWITCH_IDS=set("switch_widget","switch_bar","switch_text","switch_main","switch_text_container","switchbar");
 static final Set<String> STRONG=set("강제 중지","강제 종료","force stop","삭제","제거","uninstall","데이터 삭제","모든 데이터 삭제","clear data","clear storage");
 static final Set<String> BROAD=set("강제 중지","강제 종료","force stop","삭제","제거","uninstall","데이터 삭제","모든 데이터 삭제","clear data","clear storage","사용 중지","사용 안 함","사용","사용 설정","enable","disable");
 static final Set<String> AUTO_TIME=set("자동 날짜 및 시간","automatic date and time");
 static final Set<String> SET_DATE=set("날짜 설정","set date"),SET_TIME=set("시간 설정","set time");
 static final String[] SHORTCUT_KEYS={"accessibility_shortcut_target_service","accessibility_button_targets","accessibility_qs_targets"};
 static final String PIP_FAILURE="PiP를 닫지 못했어요. 알림의 미디어 컨트롤로 정지하세요.";
 static final String YT_NOTICE="잠금 중 유튜브 앱 · 내장 웹 화면을 사용하세요";
 static final String SHORTCUT_NOTICE="접근성 바로가기에 집중콩이 등록돼 있으면 잠금 중에도 볼륨 키 등으로 바로 꺼져요. 바로가기에서 뺀 뒤 다시 시작하세요.";
 static boolean emergency(String p){return p!=null&&(EMERGENCY.contains(p)||p.contains("emergency"));}
 static Set<String> set(String... v){return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(v)));}
}
