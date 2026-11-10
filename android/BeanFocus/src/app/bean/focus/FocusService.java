package app.bean.focus;
import android.accessibilityservice.*;
import android.app.KeyguardManager;
import android.content.*;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.RegionIterator;
import android.os.*;
import android.view.accessibility.*;
import android.widget.Toast;
import java.util.*;

public class FocusService extends AccessibilityService {
 private static FocusService current;
 private final Handler handler=new Handler(Looper.getMainLooper());
 private GrowthTracker growth;private TelemetrySession webSession;private AppCatalog catalog;private GameShield shield;
 private long beat,lastGame=-2000,lastAction=-2000,reportAt;private int checks,events,blocks,errors,clickToken;
 private final BrowserScanPolicy browserBudget=new BrowserScanPolicy();
 private String pendingBrowser="";private int pendingBrowserWindow=-1;
 private int browserScans,playerScans;private long lastBrowserMs,maxBrowserMs,totalBrowserMs;
 private final Runnable browserCheck=()->safe("브라우저 변경 점검",()->{
  String p=pendingBrowser;int window=pendingBrowserWindow;pendingBrowser="";pendingBrowserWindow=-1;
  if(p.isEmpty()||!Prefs.shorts(this)||!awake()){browserBudget.endNavigation();return;}
  AccessibilityNodeInfo root=activeRoot(p,window);if(root==null){retryMissingRoot(p,window);return;}
  try{if(browserBudget.delay(SystemClock.elapsedRealtime(),p,root.getWindowId())>0){queueBrowserScan(p,root.getWindowId());return;}captureIds(root,p);inspect(root,p,false);}finally{root.recycle();}
 });
 // During a page transition the provider may briefly expose no root. Keep only the
 // remaining navigation deadline, and stop at once when another app/window is in front.
 private void retryMissingRoot(String p,int window){
  long wait=browserBudget.missingRootDelay(SystemClock.elapsedRealtime(),p,window);
  if(wait<0||!frontMayBe(p,window)){browserBudget.endNavigation();return;}
  queueBrowserScan(p,window,wait);
 }
 private boolean frontMayBe(String p,int window){
  AccessibilityNodeInfo front=getRootInActiveWindow();if(front==null)return true;
  try{return p.equals(packageOf(front))&&(window<0||front.getWindowId()==window);}finally{front.recycle();}
 }
 private String browserState="아직 브라우저를 확인하지 못했어요",errorState="",youtubeState="",lastGamePackage="";
 private boolean receiverRegistered;
 private HandlerThread recordThread;private Handler recordHandler;
 private RecentClose recentClose=new RecentClose(-RecentClose.COOLDOWN);private final Map<Integer,String> shieldOwners=new HashMap<>();
 private String windowState="아직 창 결정 없음",guardState="아직 가드 판정 없음",globalState="자동 이동 없음";
 private final Map<String,PipDismissTracker> pipAttempts=new HashMap<>();
 private final Runnable windowCheck=()->safe("창 변경 점검",()->checkBlockedWindows());
 private long lastYoutubeToast=-10000;
 private long lastYouTubeScan=-1000,lastGuard=-2000,lastBlockerCheck=-1000,dumpUntil,dumpAt,dumpExpires;private String dump="아직 수집한 화면 ID가 없어요",dumpPackage="";
 static boolean requestDump(){if(current==null)return false;current.dumpUntil=SystemClock.elapsedRealtime()+30000;current.dumpAt=0;current.dumpExpires=0;current.dump="";current.dumpPackage="";return true;}
 static String viewIds(){if(current==null)return "서비스 연결이 필요해요";FocusService s=current;if(SystemClock.elapsedRealtime()>s.dumpExpires){s.dump="";s.dumpPackage="";return "수집한 ID가 없거나 5분이 지나 지워졌어요";}return "수집 앱: "+s.dumpPackage+"\n텍스트·주소는 포함하지 않아요.\n\n"+s.dump;}
 private void captureIds(AccessibilityNodeInfo root,String pkg){long now=SystemClock.elapsedRealtime();if(now>dumpUntil||now-dumpAt<1000||!(YouTubeInspector.isPackage(pkg)||catalog.browser(pkg)||AppCatalog.webHost(pkg)||SettingsGuard.surface(pkg)))return;dump=ViewIdSnapshot.collect(root);dumpPackage=pkg;dumpAt=now;dumpExpires=now+300000;}
 private void writeRecord(String phase,Runnable task){Handler h=recordHandler;if(h!=null)h.post(()->safe(phase,task));}
 private void growthTick(){if(growth==null)return;long e=SystemClock.elapsedRealtime(),w=System.currentTimeMillis();boolean on=GrowthTracker.eligible(this);GrowthTracker tracker=growth;writeRecord("성장 기록",()->tracker.tickAt(e,w,on));}
 private void sampleWeb(String kind){if(webSession==null)return;long e=SystemClock.elapsedRealtime(),w=System.currentTimeMillis();TelemetrySession session=webSession;writeRecord("웹 관측 기록",()->session.sampleAt(kind,"",e,w));}
 private void stopWeb(){TelemetrySession session=webSession;if(session!=null)writeRecord("관측 중지",()->session.stop());}

 private final BroadcastReceiver packageReceiver=new BroadcastReceiver(){public void onReceive(Context c,Intent i){safe("패키지 변경",()->{if(catalog!=null)catalog.invalidate();if(Intent.ACTION_PACKAGE_ADDED.equals(i.getAction())&&i.getData()!=null)SamsungContentBlocker.browserChanged(i.getData().getSchemeSpecificPart());if(Intent.ACTION_PACKAGE_ADDED.equals(i.getAction())&&!i.getBooleanExtra(Intent.EXTRA_REPLACING,false)&&FocusLock.active(FocusService.this)&&i.getData()!=null){String pkg=i.getData().getSchemeSpecificPart();if(pkg!=null){android.content.SharedPreferences p=Prefs.get(FocusService.this);String id=p.getString("lock_id","");if(!id.isEmpty())p.edit().putStringSet("lock_new_pkgs",NewPackageLedger.add(p.getStringSet("lock_new_pkgs",Collections.emptySet()),p.getString("lock_new_pkgs_id",""),id,pkg)).putString("lock_new_pkgs_id",id).apply();}}});}};
 static boolean connected(){return current!=null;}
 static String diagnostics(Context c){
  if(current==null){String enabled=android.provider.Settings.Secure.getString(c.getContentResolver(),android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);boolean allowed=enabled!=null&&enabled.contains(c.getPackageName()+"/");return allowed?"접근성 설정은 켜짐 · 서비스 연결은 확인 안 됨. 접근성을 껐다 켜주세요.":"접근성 설정에서 집중콩을 켜주세요.";}
  FocusService s=current;long age=Math.max(0,(SystemClock.elapsedRealtime()-s.beat)/1000);
  return "서비스 연결됨 · 점검 "+age+"초 전\n마지막 감지: "+s.browserState+"\n설정 가드: "+s.guardState+"\n이번 잠금 새 앱: "+s.newPackageCount()+"개\n"+NetworkTime.status+"\n"+ShortcutCheck.last+"\n창: "+s.windowState+"\n이동: "+s.globalState+"\n점검 "+s.checks+"회 · 이벤트 "+s.events+"회 · 이동 요청 전달 "+s.blocks+"회\n브라우저 검사 "+s.browserScans+"회 · 웹 보조 검사 "+s.playerScans+"회\n검사 시간 최근/평균/최대 "+s.lastBrowserMs+"/"+(s.browserScans==0?0:s.totalBrowserMs/s.browserScans)+"/"+s.maxBrowserMs+"ms (페이지 로딩 시간 아님)\n"+s.youtubeState+"\n"+SamsungContentBlocker.status(c)+(s.errors>0?"\n복구한 오류 "+s.errors+"회 · "+s.errorState:"");
 }
 private int newPackageCount(){android.content.SharedPreferences p=Prefs.get(this);String id=p.getString("lock_id","");return FocusLock.active(this)&&!id.isEmpty()&&id.equals(p.getString("lock_new_pkgs_id",""))?p.getStringSet("lock_new_pkgs",Collections.emptySet()).size():0;}
 private void safe(String phase,Runnable work){try{work.run();}catch(RuntimeException e){String failure=phase+" · "+e.getClass().getSimpleName();if(Looper.myLooper()==Looper.getMainLooper()){errors++;errorState=failure;}else handler.post(()->{errors++;errorState=failure;});}}
 private final Runnable webBeat=new Runnable(){public void run(){try{safe("화면 점검",()->sampleExternal());}finally{if(current==FocusService.this)handler.postDelayed(this,2000);}}};
 private final Runnable growthBeat=new Runnable(){public void run(){try{safe("성장 관측",()->growthTick());}finally{if(current==FocusService.this)handler.postDelayed(this,30000);}}};
 static void reload(){if(current!=null){FocusService s=current;s.safe("설정 반영",()->s.configure());s.clickToken++;s.cancelBrowserScan();s.browserBudget.reset();s.recentClose.cancel();if(s.shield!=null)s.safe("가림 해제",()->s.shield.hide());if(s.growth!=null)s.safe("성장 관측",()->s.growthTick());s.handler.post(()->s.safe("설정 후 점검",()->s.sampleExternal()));}}
 @Override public void onServiceConnected(){safe("서비스 준비",()->connectService());}
 private void connectService(){
  current=this;ShortcutCheck.last=Prefs.get(this).getString("shortcut_last_result",ShortcutCheck.last);beat=SystemClock.elapsedRealtime();recordThread=new HandlerThread("BeanFocus-records");recordThread.start();recordHandler=new Handler(recordThread.getLooper());catalog=new AppCatalog(this);shield=new GameShield(this,()->{boolean[] result={false};safe("최근 앱 버튼",()->result[0]=requestRecentClose());return result[0];});
  int boot=android.provider.Settings.Global.getInt(getContentResolver(),android.provider.Settings.Global.BOOT_COUNT,-1);long previous=Prefs.get(this).getInt("recents_close_boot",-2)==boot?Prefs.get(this).getLong("recents_close_last",-RecentClose.COOLDOWN):-RecentClose.COOLDOWN;
  recentClose=new RecentClose(previous<=SystemClock.elapsedRealtime()?previous:-RecentClose.COOLDOWN);
  safe("접근성 설정",()->configure());safe("사용 기록 준비",()->{growth=new GrowthTracker(this);webSession=new TelemetrySession(this,"external");});
  safe("앱 변경 감지",()->{IntentFilter f=new IntentFilter();f.addAction(Intent.ACTION_PACKAGE_ADDED);f.addAction(Intent.ACTION_PACKAGE_REMOVED);f.addAction(Intent.ACTION_PACKAGE_CHANGED);f.addDataScheme("package");if(Build.VERSION.SDK_INT>=33)registerReceiver(packageReceiver,f,Context.RECEIVER_NOT_EXPORTED);else registerReceiver(packageReceiver,f);receiverRegistered=true;});
  handler.removeCallbacksAndMessages(null);handler.post(webBeat);handler.post(growthBeat);
 }
 private void configure(){
  AccessibilityServiceInfo info=getServiceInfo();if(info==null)return;
  info.flags|=AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS|AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS|AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
  info.eventTypes=AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED|AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED|AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED|AccessibilityEvent.TYPE_WINDOWS_CHANGED|AccessibilityEvent.TYPE_VIEW_SCROLLED|AccessibilityEvent.TYPE_VIEW_CLICKED;
  // New games/browsers and embedded web clicks need events before their package is known.
  // Most packages are rejected below without reading their tree. No text from events is recorded.
  info.packageNames=null;setServiceInfo(info);catalog.invalidate();catalog.refresh();
 }
 private boolean awake(){PowerManager p=(PowerManager)getSystemService(POWER_SERVICE);KeyguardManager k=(KeyguardManager)getSystemService(KEYGUARD_SERVICE);return p!=null&&p.isInteractive()&&(k==null||!k.isKeyguardLocked());}
 private boolean strict(){return Prefs.get(this).getBoolean("strict_locked_web",true);}
 private boolean blockedApp(String p){
  if(p.isEmpty()||p.equals(getPackageName())||emergencySurface(p)||SettingsGuard.surface(p)||!Prefs.enabled(this))return false;
  boolean selected=Prefs.games(this).contains(p),locked=FocusLock.active(this);
  if(locked&&Prefs.shorts(this)&&YouTubeInspector.isPackage(p))return true;
  if(BlockPolicy.otherProfile(locked,Prefs.get(this).getBoolean("block_other_profiles",true),locked&&catalog.unknownPackage(p),false))return true;
  if(locked&&Prefs.get(this).getBoolean("block_new_apps",false)&&!SettingsGuard.surface(p)&&!emergencySurface(p)&&catalog.newDuringLock(p))return true;
  return BlockPolicy.game(true,selected,locked,!selected&&locked&&catalog.game(p));
 }
 private static boolean emergencySurface(String p){return DeviceConstants.emergency(p);}

 private SettingsGuard.Match guardMatch(AccessibilityNodeInfo root,String p){
  if(BlockerGuard.surface(p))return BlockerGuard.ownSwitch(root)==BlockerGuard.ON?new SettingsGuard.Match("삼성 인터넷 광고 차단기 · 켜진 집중콩 보호",true,false,false,8):new SettingsGuard.Match("",false,false,false,0);
  return SettingsGuard.decide(p,SettingsNodeCollector.collect(root),Prefs.shorts(this),RemovalAccess.allowed(this));
 }

 private boolean guard(AccessibilityNodeInfo root,String p){
  if(!FocusLock.active(this)||!Prefs.get(this).getBoolean("guard_locked_settings",true)||!(SettingsGuard.surface(p)||BlockerGuard.surface(p)))return false;
  SettingsGuard.Match m=guardMatch(root,p);guardState=m.reason.isEmpty()?"허용":m.reason;if(m.reason.isEmpty())return false;
  if(m.own&&m.installer){
   if(RemovalAccess.ready(this)&&RemovalAccess.grant(this))return false;
   if(RemovalAccess.request(this))Toast.makeText(this,"집중콩 · 삭제 허용 대기를 시작했어요. 10분 뒤 삭제 화면을 다시 열거나 앱에서 진행하세요.",Toast.LENGTH_LONG).show();
  }
  long now=SystemClock.elapsedRealtime();if(now-lastGuard<900)return true;lastGuard=now;int window=root.getWindowId();
  if(m.rule==8)Toast.makeText(this,"집중 잠금 중에는 삼성 인터넷 광고 차단기에서 집중콩을 끌 수 없어요",Toast.LENGTH_SHORT).show();
  boolean delivered=globalActionFor(GLOBAL_ACTION_BACK,p);browserState="잠금 설정 가드 · "+m.reason+" · 뒤로 요청 "+(delivered?"전달됨":"실패");
  handler.postDelayed(()->safe("설정 가드 재확인",()->{if(!FocusLock.active(this)||!Prefs.get(this).getBoolean("guard_locked_settings",true)||!awake())return;AccessibilityNodeInfo again=activeRoot(p,window);if(again!=null)try{if(!guardMatch(again,p).reason.isEmpty()){globalActionFor(GLOBAL_ACTION_HOME,p);browserState="잠금 설정 가드 · 보호 화면이 남아 홈 이동 요청";}}finally{again.recycle();}}),650);return true;
 }
 private boolean requestRecentClose(){
  long now=SystemClock.elapsedRealtime();if(!recentClose.request(now,shieldOwners)){Toast.makeText(this,"최근 앱 정리는 2분에 한 번 사용할 수 있어요.",Toast.LENGTH_SHORT).show();return false;}
  if(!userGlobalAction(GLOBAL_ACTION_RECENTS)){recentClose.cancel();Toast.makeText(this,"최근 앱 화면을 열지 못했어요.",Toast.LENGTH_SHORT).show();return false;}
  int boot=android.provider.Settings.Global.getInt(getContentResolver(),android.provider.Settings.Global.BOOT_COUNT,-1);Prefs.get(this).edit().putInt("recents_close_boot",boot).putLong("recents_close_last",now).apply();
  handler.postDelayed(()->safe("최근 앱 정리 확인",()->checkBlockedWindows()),100);return true;
 }
 private void observeRecent(AccessibilityNodeInfo root){
  long now=SystemClock.elapsedRealtime();if(!recentClose.active(now))return;String p=packageOf(root);boolean foreground=blockedApp(p);
  recentClose.observe(now,RecentsInspector.visible(root,p),foreground);if(foreground&&!recentClose.active(now))lastGame=-2000;
 }

 // YouTube emits content events continuously during playback. Decide with cheap state
 // first; only blocked-app handling, explicit ID capture or a due Shorts check reads the tree.
 private boolean youtubeEventDue(String p){
  long now=SystemClock.elapsedRealtime();
  return blockedApp(p)||now<=dumpUntil||(Prefs.shorts(this)&&now-lastYouTubeScan>=250);
 }
 private void inspectYouTube(AccessibilityNodeInfo root){
  if(!Prefs.shorts(this))return;final String p=packageOf(root);
  if(FocusLock.active(this)){restrict(p,"잠금 중 유튜브 앱 · 내장 웹 화면을 사용하세요");return;}
  if(SystemClock.elapsedRealtime()-lastYouTubeScan<250)return;lastYouTubeScan=SystemClock.elapsedRealtime();String id=YouTubeInspector.player(root);browserState="YouTube 앱 · "+(id.isEmpty()?"쇼츠 플레이어 ID 없음 (화면 판별 보장 아님)":"쇼츠 플레이어 감지: "+id);
  if(id.isEmpty()||SystemClock.elapsedRealtime()-lastAction<1100)return;
  lastAction=SystemClock.elapsedRealtime();int window=root.getWindowId();boolean delivered=globalActionFor(GLOBAL_ACTION_BACK,p,window);browserState+=" · 뒤로 요청 "+(delivered?"전달됨":"실패");record("shorts",p);
  handler.postDelayed(()->safe("앱 쇼츠 이동 확인",()->{
   if(!Prefs.shorts(this)||!awake())return;AccessibilityNodeInfo again=activeRoot(p,window);if(again==null)return;
   try{String remains=YouTubeInspector.player(again);if(!remains.isEmpty()){lastAction=SystemClock.elapsedRealtime();globalActionFor(GLOBAL_ACTION_HOME,p,window);browserState="YouTube 쇼츠 플레이어가 남아 홈 이동 요청 · "+remains;}else browserState="YouTube 앱 · 뒤로 이동 후 플레이어 ID 없음";}finally{again.recycle();}
  }),850);
 }
 @Override public void onAccessibilityEvent(AccessibilityEvent event){safe("화면 이벤트",()->handleEvent(event));}
 private void handleEvent(AccessibilityEvent event){
  events++;if(event==null)return;
  if(event.getEventType()==AccessibilityEvent.TYPE_WINDOWS_CHANGED||event.getEventType()==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){handler.removeCallbacks(windowCheck);handler.postDelayed(windowCheck,300);}
  if(!awake()||!Prefs.enabled(this))return;
  if(recentClose.active(SystemClock.elapsedRealtime())){AccessibilityNodeInfo front=getRootInActiveWindow();if(front!=null)try{observeRecent(front);}finally{front.recycle();}else recentClose.cancel();}
  String p=event.getPackageName()==null?"":event.getPackageName().toString();if(p.isEmpty()||p.equals(getPackageName())||emergencySurface(p))return;
  if(SettingsGuard.surface(p)||YouTubeInspector.isPackage(p)){
   if(YouTubeInspector.isPackage(p)&&!youtubeEventDue(p))return;
   AccessibilityNodeInfo root=activeRoot(p,-1);if(root!=null)try{captureIds(root,p);if(guard(root,p))return;if(YouTubeInspector.isPackage(p)){if(blockedApp(p))checkBlockedWindows();else inspectYouTube(root);return;}}finally{root.recycle();}
  }
  // Samsung Internet's ad blocker list: leave it while BeanFocus's switch there is on (lock only).
  if(BlockerGuard.surface(p)&&FocusLock.active(this)&&(event.getEventType()==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED||SystemClock.elapsedRealtime()-lastBlockerCheck>=400)){
   lastBlockerCheck=SystemClock.elapsedRealtime();AccessibilityNodeInfo root=activeRoot(p,-1);if(root!=null)try{if(guard(root,p))return;}finally{root.recycle();}
  }
  if(blockedApp(p)){// Reject stale events if that app no longer owns an interactive window.
   AccessibilityNodeInfo root=activeRoot(p,-1);if(root!=null){try{checkBlockedWindows();}finally{root.recycle();}}return;
  }
  if(!Prefs.shorts(this))return;
  if(event.getEventType()==AccessibilityEvent.TYPE_VIEW_CLICKED){
   clickToken++;browserBudget.endNavigation(); // A newer click invalidates the earlier navigation.
   AccessibilityNodeInfo node=event.getSource();if(node!=null)try{
    boolean candidate=catalog.browser(p)||AppCatalog.webHost(p)||UrlRules.isShorts(String.valueOf(node.getExtras().getCharSequence(BrowserInspector.TARGET)));
    if(!candidate)return;
    boolean link=BrowserInspector.clickedShorts(node,false);
    // A tab ID is only a hint here. The delayed check must establish the current YouTube page.
    if(link||BrowserInspector.clickedShorts(node,true)){queueClickedShorts(p,event.getWindowId());return;}
   }finally{node.recycle();}
  }
  if(!(catalog.browser(p)||AppCatalog.webHost(p)))return;
  int type=event.getEventType();
  boolean navigation=type==AccessibilityEvent.TYPE_VIEW_CLICKED||type==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
  if(type==AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED){
   AccessibilityNodeInfo source=event.getSource();if(source!=null)try{navigation=BrowserAddress.field(source.getViewIdResourceName());}finally{source.recycle();}
  }
  if(navigation){
   browserBudget.navigation(SystemClock.elapsedRealtime(),p,event.getWindowId());
  }
  queueBrowserScan(p,event.getWindowId());
 }
 private void cancelBrowserScan(){handler.removeCallbacks(browserCheck);pendingBrowser="";pendingBrowserWindow=-1;}
 private void queueBrowserScan(String p,int window){
  queueBrowserScan(p,window,120);
 }
 private void queueBrowserScan(String p,int window,long minimumDelay){
  if(p.equals(pendingBrowser)&&window==pendingBrowserWindow)return; // Never postpone forever during scrolling.
  cancelBrowserScan();pendingBrowser=p;pendingBrowserWindow=window;
  long delay=browserBudget.delay(SystemClock.elapsedRealtime(),p,window);
  handler.postDelayed(browserCheck,Math.max(minimumDelay,delay));
 }
 private BrowserInspector.Result readBrowser(AccessibilityNodeInfo root,String p,boolean confirmAction){
  long started=SystemClock.elapsedRealtime();browserBudget.scanned(started,p,root.getWindowId());
  try{
   BrowserInspector.Result r=BrowserInspector.read(root,p,confirmAction||browserBudget.allowPlayer(started),confirmAction||browserBudget.navigating(started),browserBudget.host());
   if(r.playerChecked){playerScans++;browserBudget.playerScanned(SystemClock.elapsedRealtime());}
   browserBudget.rememberHost(r.hostEvidence());
   return r;
  }finally{lastBrowserMs=SystemClock.elapsedRealtime()-started;totalBrowserMs+=lastBrowserMs;maxBrowserMs=Math.max(maxBrowserMs,lastBrowserMs);browserScans++;}
 }
 private String packageOf(AccessibilityNodeInfo n){return n.getPackageName()==null?"":n.getPackageName().toString();}
 private AccessibilityNodeInfo activeRoot(String expected,int window){
  AccessibilityNodeInfo root=getRootInActiveWindow();if(root!=null){if((window<0?expected.equals(packageOf(root)):BlockPolicy.sameWindow(expected,window,packageOf(root),root.getWindowId())))return root;root.recycle();}
  List<AccessibilityWindowInfo> windows=getWindows();
  try{for(AccessibilityWindowInfo w:windows){
   if(w.getType()!=AccessibilityWindowInfo.TYPE_APPLICATION||(!w.isActive()&&!w.isFocused())||(window>=0&&w.getId()!=window))continue;
   AccessibilityNodeInfo r=w.getRoot();if(r!=null){if(expected.equals(packageOf(r)))return r;r.recycle();}
  }}finally{for(AccessibilityWindowInfo w:windows)w.recycle();}return null;
 }
 private void inspect(AccessibilityNodeInfo root,String p,boolean telemetry){
  if(!Prefs.shorts(this)){if(telemetry)sampleWeb(null);return;}
  if(browserBudget.delay(SystemClock.elapsedRealtime(),p,root.getWindowId())>0)return;
  cancelBrowserScan();BrowserInspector.Result result=readBrowser(root,p,false);browserState=p+"\n"+result.diagnostic();
  if(result.editing)browserBudget.endNavigation();
  if(Prefs.shorts(this)&&!result.editing){
   if(BlockPolicy.browserShorts(result.url,result.editing,result.playerShorts,result.confirmedHome()))backFromShorts(p,root.getWindowId());
   else if(BlockPolicy.unknownBrowser(true,FocusLock.active(this),strict(),catalog.browser(p),Prefs.browser(p),AppCatalog.webHost(p)))restrict(p,"잠금 중 미지원 브라우저 · 내장 웹 화면을 사용하세요");

  }
  // Event scans also sample the coarse classification. Otherwise a busy event stream
  // can win every shared budget slot and starve the two-second telemetry watchdog.
  String kind=result.youtube()&&!result.editing&&(result.knownLocation()||result.playerShorts)?(result.shorts()?"shorts":"unknown"):null;sampleWeb(kind);
  if(browserBudget.retryDelay(SystemClock.elapsedRealtime(),p,root.getWindowId())>=0)queueBrowserScan(p,root.getWindowId());
 }
 boolean userGlobalAction(int action){boolean[] result={false};safe("사용자 이동 버튼",()->{result[0]=performGlobalAction(action);if(result[0])blocks++;});return result[0];}
 private boolean globalActionFor(int action,String expectedPkg,int expectedWindow){
  AccessibilityNodeInfo front=getRootInActiveWindow();if(front==null){globalState="활성 창 확인 불가 · 자동 이동 생략";return false;}
  try{String active=packageOf(front);if(!WindowPolicy.globalAllowed(active,expectedPkg)||(expectedWindow>=0&&front.getWindowId()!=expectedWindow)){globalState="대상/활성 창 불일치 또는 보호 화면 · 자동 이동 생략";return false;}}finally{front.recycle();}
  AccessibilityNodeInfo expected=activeRoot(expectedPkg,expectedWindow);if(expected==null)return false;try{
   List<AccessibilityWindowInfo> windows=getWindows();try{for(AccessibilityWindowInfo w:windows)if(w.getId()==expected.getWindowId()&&w.isInPictureInPictureMode()){globalState="PiP · 자동 HOME/BACK 생략";return false;}}finally{for(AccessibilityWindowInfo w:windows)w.recycle();}
  }finally{expected.recycle();}
  AccessibilityNodeInfo latest=getRootInActiveWindow();if(latest==null)return false;try{if(!WindowPolicy.globalAllowed(packageOf(latest),expectedPkg)||(expectedWindow>=0&&latest.getWindowId()!=expectedWindow))return false;}finally{latest.recycle();}
  boolean delivered=performGlobalAction(action);if(delivered)blocks++;globalState=expectedPkg+" · "+(action==GLOBAL_ACTION_HOME?"HOME":"BACK")+" 요청 "+(delivered?"전달":"실패");return delivered;
 }
 private boolean globalActionFor(int action,String expectedPkg){return globalActionFor(action,expectedPkg,-1);}
 private void record(String kind,String p){long wall=System.currentTimeMillis();writeRecord("차단 기록",()->AnalyticsDb.get(this).blockAt(kind,p,wall));}
 private void blockApp(String p){long now=SystemClock.elapsedRealtime();if(YouTubeInspector.isPackage(p)&&FocusLock.active(this)&&now-lastYoutubeToast>=10000){lastYoutubeToast=now;Toast.makeText(this,DeviceConstants.YT_NOTICE,Toast.LENGTH_SHORT).show();}if(recentClose.suppressHome(now,p))return;if(p.equals(lastGamePackage)&&now-lastGame<1000)return;lastGame=now;lastGamePackage=p;boolean delivered=globalActionFor(GLOBAL_ACTION_HOME,p);browserState="차단 앱 감지 · 홈 이동 요청 "+(delivered?"전달됨":"실패");record(YouTubeInspector.isPackage(p)?"youtube_app":"game",p);handler.postDelayed(()->safe("게임 창 점검",()->checkBlockedWindows()),300);}
 private void restrict(String p,String reason){long now=SystemClock.elapsedRealtime();if(now-lastAction<1200)return;lastAction=now;boolean delivered=globalActionFor(GLOBAL_ACTION_HOME,p);browserState=reason+" · 홈 요청 "+(delivered?"전달됨":"실패");Toast.makeText(this,"집중콩 · "+reason,Toast.LENGTH_SHORT).show();}
 private void backFromShorts(String p,int window){
  long now=SystemClock.elapsedRealtime();if(now-lastAction<1100)return;lastAction=now;boolean delivered=globalActionFor(GLOBAL_ACTION_BACK,p,window);browserState+="\n쇼츠 감지 · 뒤로 요청 "+(delivered?"전달됨":"실패");record("shorts",p);
  browserBudget.endNavigation();cancelBrowserScan();
  final int token=clickToken;
  handler.postDelayed(()->safe("쇼츠 이동 확인",()->{if(token==clickToken)verifyExit(p,window,false);}),850);
 }
 private void queueClickedShorts(String p,int window){
  ++clickToken;browserState=p+" · 클릭한 쇼츠 링크 감지 · 현재 화면 확인 대기";
  browserBudget.navigation(SystemClock.elapsedRealtime(),p,window);
  cancelBrowserScan();queueBrowserScan(p,window,450);
 }
 private void verifyExit(String p,int window,boolean clicked){
  if(!Prefs.shorts(this)||!awake())return;AccessibilityNodeInfo root=activeRoot(p,window);if(root==null)return;
  try{BrowserInspector.Result r=readBrowser(root,p,true);if(r.editing)return;
   boolean remains=BlockPolicy.browserShorts(r.url,r.editing,r.playerShorts,r.confirmedHome());
   if(remains){lastAction=SystemClock.elapsedRealtime();boolean delivered=globalActionFor(GLOBAL_ACTION_HOME,p,window);browserState=p+" · 쇼츠 주소/화면 신호가 남음 · 홈 요청 "+(delivered?"전달됨":"실패");}
   else browserState=p+"\n"+r.diagnostic();
  }finally{root.recycle();}
 }
 private void sampleExternal(){
  beat=SystemClock.elapsedRealtime();checks++;if(dumpExpires>0&&beat>dumpExpires){dump="";dumpPackage="";dumpExpires=0;}safe("브라우저 목록",()->catalog.refresh());
  if(beat-reportAt>30000||youtubeState.isEmpty()){youtubeState=AppCatalog.youtubeStatus(this);NetworkTime.now();reportAt=beat;}
  safe("삼성 인터넷 차단 목록",()->SamsungContentBlocker.sync(this));
  if(!awake()){shield.hide();sampleWeb(null);return;}
  AccessibilityNodeInfo root=getRootInActiveWindow();boolean sampled=false;
  if(root!=null)try{observeRecent(root);String p=packageOf(root);captureIds(root,p);
   if(Prefs.enabled(this)&&guard(root,p)){}
   else if(YouTubeInspector.isPackage(p)){if(blockedApp(p))checkBlockedWindows();else inspectYouTube(root);}
   else if(Prefs.enabled(this)&&blockedApp(p))checkBlockedWindows();
   else if(catalog.browser(p)||AppCatalog.webHost(p)){inspect(root,p,true);sampled=true;}
  }finally{root.recycle();}
  safe("팝업 게임 점검",()->checkBlockedWindows());
  if(root==null)recentClose.cancel();if(!sampled)sampleWeb(null);
 }
 private void checkBlockedWindows(){
  if(shield==null)return;if(!Prefs.enabled(this)||!awake()){shield.hide();shieldOwners.clear();recentClose.cancel();pipAttempts.clear();return;}
  Rect protectedForeground=new Rect();AccessibilityNodeInfo active=getRootInActiveWindow();if(active!=null)try{String foreground=packageOf(active);if(foreground.equals(getPackageName())||emergencySurface(foreground)){shield.hide();active.getBoundsInScreen(protectedForeground);}observeRecent(active);}finally{active.recycle();}else recentClose.cancel();
  List<GameShield.Area> regions=new ArrayList<>();Map<Integer,String> owners=new HashMap<>();String activeBlockedPkg="";Set<Integer> seenWindows=new HashSet<>();Set<String> seenPips=new HashSet<>();List<AccessibilityWindowInfo> windows=getWindows();
  try{for(AccessibilityWindowInfo w:windows){
   seenWindows.add(w.getId());if(w.getType()!=AccessibilityWindowInfo.TYPE_APPLICATION)continue;AccessibilityNodeInfo r=w.getRoot();if(r==null)continue;
   try{String p=packageOf(r);boolean pip=w.isInPictureInPictureMode();WindowPolicy.Decision decision=WindowPolicy.decide(blockedApp(p),r.isVisibleToUser()||pip,pip,w.isActive()||w.isFocused(),recentClose.pause(SystemClock.elapsedRealtime(),p,w.getId(),pip,YouTubeInspector.isPackage(p)),YouTubeInspector.isPackage(p));
    if(decision==WindowPolicy.Decision.NONE)continue;String message="";
    if(decision==WindowPolicy.Decision.SHIELD_DISMISS_PIP){
     String key=p+":"+w.getId();seenPips.add(key);PipDismissTracker tracker=pipAttempts.get(key);if(tracker==null){tracker=new PipDismissTracker();pipAttempts.put(key,tracker);}long now=SystemClock.elapsedRealtime();
     if(tracker.shouldAttempt(now)){tracker.attempted(now);boolean[] sent={false};safe("PiP 닫기",()->sent[0]=r.performAction(AccessibilityNodeInfo.ACTION_DISMISS));windowState=p+" · PiP 닫기 요청 "+(sent[0]?"전달 · 이탈 확인 대기":"거절 · 재확인 대기");}
     if(tracker.failed(now)){message=DeviceConstants.PIP_FAILURE;windowState=p+" · PiP 닫기 실패 · 60초 재시도 중지";}
    }else if(decision==WindowPolicy.Decision.SHIELD_HOME)activeBlockedPkg=p;
    Rect area=new Rect();w.getBoundsInScreen(area);if(area.isEmpty())continue;Region visible=new Region(area);if(!protectedForeground.isEmpty())visible.op(protectedForeground,Region.Op.DIFFERENCE);
    for(AccessibilityWindowInfo front:windows){if(front.getId()==w.getId()||front.getType()==AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY)continue;
     boolean protectedWindow=false;AccessibilityNodeInfo f=front.getRoot();if(f!=null)try{String fp=packageOf(f);protectedWindow=fp.equals(getPackageName())||emergencySurface(fp);}finally{f.recycle();}
     if(front.getLayer()>w.getLayer()||protectedWindow){Rect cover=new Rect();front.getBoundsInScreen(cover);visible.op(cover,Region.Op.DIFFERENCE);}
    }
    RegionIterator iterator=new RegionIterator(visible);Rect part=new Rect();while(iterator.next(part)&&regions.size()<16)regions.add(new GameShield.Area(part,message));owners.put(w.getId(),p);
   }finally{r.recycle();}
  }}finally{for(AccessibilityWindowInfo w:windows)w.recycle();}
  for(Iterator<Map.Entry<String,PipDismissTracker>> it=pipAttempts.entrySet().iterator();it.hasNext();){Map.Entry<String,PipDismissTracker> e=it.next();if(!seenPips.contains(e.getKey())&&!seenWindows.contains(Integer.parseInt(e.getKey().substring(e.getKey().lastIndexOf(':')+1)))){if(e.getValue().wasAttempted())windowState=e.getKey()+" · PiP 닫기 성공(창 이탈 확인)";it.remove();}}
  shieldOwners.clear();shieldOwners.putAll(owners);if(regions.isEmpty())shield.hide();else{shield.show(regions);if(seenPips.isEmpty())windowState="가림막 "+regions.size()+"개 영역";}
  if(!activeBlockedPkg.isEmpty())blockApp(activeBlockedPkg);
 }

 @Override public void onInterrupt(){clickToken++;cancelBrowserScan();browserBudget.reset();stopWeb();if(shield!=null)safe("가림 해제",()->shield.hide());}
 private void cleanup(){recentClose.cancel();shieldOwners.clear();dump="";dumpPackage="";dumpExpires=0;dumpUntil=0;handler.removeCallbacksAndMessages(null);clickToken++;if(shield!=null)safe("가림 해제",()->shield.hide());GrowthTracker oldGrowth=growth;TelemetrySession oldSession=webSession;writeRecord("관측 정리",()->{if(oldGrowth!=null)oldGrowth.stop();if(oldSession!=null)oldSession.stop();});if(recordThread!=null){recordThread.quitSafely();recordThread=null;recordHandler=null;}if(receiverRegistered){safe("앱 감지 해제",()->unregisterReceiver(packageReceiver));receiverRegistered=false;}if(current==this)current=null;}
 @Override public boolean onUnbind(Intent i){cleanup();return super.onUnbind(i);}
 @Override public void onDestroy(){cleanup();super.onDestroy();}
}
