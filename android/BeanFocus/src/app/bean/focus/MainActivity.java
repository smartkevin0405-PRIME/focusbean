package app.bean.focus;
import android.app.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.*;
import android.content.pm.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
 private LinearLayout body;
 private TextView status, gameSummary;
 private TextView lockStatus,removalStatus;
 private TextView petStatus,petSpeech;
 private ProgressBar petProgress;
 private PetView petView;
 private Switch enabledSwitch,shortsSwitch,strictSwitch,guardSwitch,newAppsSwitch,otherProfilesSwitch;
 private Button gamesButton,startLockButton,unlockButton,cancelUnlockButton,removalButton,cancelRemovalButton;
 private boolean syncing;
 private final Handler handler=new Handler(Looper.getMainLooper());
 private final Runnable tick=new Runnable(){public void run(){refresh();handler.postDelayed(this,1000);}};
 int dp(int n){return (int)(getResources().getDisplayMetrics().density*n+0.5f);}
 @Override public void onCreate(Bundle b){super.onCreate(b); build();}
 @Override public void onResume(){super.onResume();handler.removeCallbacks(tick);handler.post(tick);}
 @Override public void onPause(){handler.removeCallbacks(tick);super.onPause();}
 @Override public void onDestroy(){handler.removeCallbacksAndMessages(null);super.onDestroy();}
 private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(0,dp(6),0,dp(6));return t;}
 private LinearLayout card(){LinearLayout c=new LinearLayout(this);c.setOrientation(1);c.setPadding(dp(18),dp(12),dp(18),dp(16));GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(20));c.setBackground(bg);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(14);body.addView(c,p);return c;}
 private Button button(LinearLayout c,String s,View.OnClickListener click){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextColor(Color.rgb(36,116,92));b.setOnClickListener(click);c.addView(b,new LinearLayout.LayoutParams(-1,dp(52)));return b;}
 private void build(){
  ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.setBackgroundColor(Color.rgb(245,247,243));
  sc.setOnApplyWindowInsetsListener((v,i)->{v.setPadding(i.getSystemWindowInsetLeft(),i.getSystemWindowInsetTop(),i.getSystemWindowInsetRight(),i.getSystemWindowInsetBottom());return i.consumeSystemWindowInsets();});
  body=new LinearLayout(this);body.setOrientation(1);body.setPadding(dp(22),dp(22),dp(22),dp(30));sc.addView(body);setContentView(sc);
  TextView title=text("집중콩",32,Color.rgb(29,60,49));title.setTypeface(null,Typeface.BOLD);body.addView(title);
  body.addView(text("강의는 보고, 쇼츠는 멈추고.",17,Color.DKGRAY));
  body.addView(text("0.13 베타 · 삼성 인터넷 쇼츠 화면 감지",12,Color.GRAY));
  LinearLayout c=card();TextView petTitle=text("프라임콩  ·  PrimeBean",21,Color.rgb(29,60,49));petTitle.setTypeface(null,Typeface.BOLD);c.addView(petTitle);
  c.addView(text("네 집중 곁에서 자라는 콩 연구자",13,Color.GRAY));petView=new PetView(this);c.addView(petView,new LinearLayout.LayoutParams(-1,dp(230)));
  petStatus=text("",15,Color.DKGRAY);c.addView(petStatus);petProgress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);petProgress.setMax(1000);c.addView(petProgress,new LinearLayout.LayoutParams(-1,dp(12)));petSpeech=text("",14,Color.rgb(36,116,92));c.addView(petSpeech);
  button(c,"연구실 꾸미기 · 성장 보상",v->chooseStyle());
  button(c,"XP 수식 연구실",v->startActivity(new Intent(this,FormulaActivity.class)));
  button(c,"시간 관측소 · 상세 통계",v->startActivity(new Intent(this,StatsActivity.class)));
  c.addView(text("차단 연결 유지 시간을 선택한 수식으로 XP에 반영합니다. 수식 연구실에서 원하는 수식을 고를 수 있습니다. 접근성 연결과 차단이 켜져 있고, 쇼츠 또는 선택한 게임 차단이 설정돼 있어야 적립됩니다. 공부량을 측정하는 기능은 아닙니다. 화면이 꺼진 뒤 기기가 절전 상태라면 일부 시간이 적립되지 않을 수 있습니다.",12,Color.GRAY));
  c=card();Switch enabled=new Switch(this);enabledSwitch=enabled;enabled.setText("차단 켜기");enabled.setTextSize(19);enabled.setPadding(0,dp(12),0,dp(12));enabled.setChecked(Prefs.enabled(this));c.addView(enabled);enabled.setOnCheckedChangeListener((v,on)->{if(syncing)return;if(FocusLock.active(this)){refresh();return;}Prefs.get(this).edit().putBoolean("enabled",on).apply();FocusService.reload();refresh();});
  Switch shorts=new Switch(this);shortsSwitch=shorts;shorts.setText("유튜브 쇼츠 차단");shorts.setChecked(Prefs.get(this).getBoolean("shorts",true));shorts.setPadding(0,dp(12),0,dp(12));c.addView(shorts);shorts.setOnCheckedChangeListener((v,on)->{if(syncing)return;if(FocusLock.active(this)){refresh();return;}Prefs.get(this).edit().putBoolean("shorts",on).apply();FocusService.reload();});
  strictSwitch=new Switch(this);strictSwitch.setText("잠금 중 미지원 브라우저 제한");strictSwitch.setChecked(Prefs.get(this).getBoolean("strict_locked_web",true));c.addView(strictSwitch);strictSwitch.setOnCheckedChangeListener((v,on)->{if(syncing)return;if(FocusLock.active(this)){refresh();return;}Prefs.get(this).edit().putBoolean("strict_locked_web",on).apply();FocusService.reload();});
  guardSwitch=new Switch(this);guardSwitch.setText("잠금 중 차단 해제 설정 가드");guardSwitch.setChecked(Prefs.get(this).getBoolean("guard_locked_settings",true));c.addView(guardSwitch);guardSwitch.setOnCheckedChangeListener((v,on)->{if(syncing)return;if(FocusLock.active(this)){refresh();return;}Prefs.get(this).edit().putBoolean("guard_locked_settings",on).apply();FocusService.reload();});
  c.addView(text("잠금 중 집중콩의 접근성 해제·강제 중지·삭제 화면과 유튜브 앱 정보 화면에서 뒤로 이동합니다. 전화·긴급 통화는 그대로 사용할 수 있습니다. 잠금 중 접근성의 집중콩 항목, 집중콩·유튜브 앱 관리, 디바이스 케어·루틴의 집중콩 항목, 날짜·시간 수동 변경은 가드 대상입니다. 10분 조기 해제 대기와 안전 모드는 비상구로 남습니다. 설정 UI가 바뀌거나 서비스가 중단되면 가드를 놓칠 수 있습니다.",12,Color.GRAY));
  newAppsSwitch=new Switch(this);newAppsSwitch.setText("잠금 후 새로 설치된 앱도 차단");newAppsSwitch.setChecked(Prefs.get(this).getBoolean("block_new_apps",false));c.addView(newAppsSwitch);newAppsSwitch.setOnCheckedChangeListener((v,on)->{if(syncing)return;if(FocusLock.active(this)){refresh();return;}Prefs.get(this).edit().putBoolean("block_new_apps",on).apply();FocusService.reload();});
  otherProfilesSwitch=new Switch(this);otherProfilesSwitch.setText("잠금 중 다른 프로필 앱도 차단");otherProfilesSwitch.setChecked(Prefs.get(this).getBoolean("block_other_profiles",true));c.addView(otherProfilesSwitch);otherProfilesSwitch.setOnCheckedChangeListener((v,on)->{if(syncing)return;if(FocusLock.active(this)){refresh();return;}Prefs.get(this).edit().putBoolean("block_other_profiles",on).apply();FocusService.reload();});
  c.addView(text("선택 옵션입니다. 잠금 시작 시각과 앱의 최초 설치 시각을 비교해 새 일반 앱을 제한합니다. 시스템 앱과 전화·긴급 기능은 제외합니다. 조회할 수 없는 다른 프로필 앱 창은 선택한 옵션으로 차단하지만, 보안 폴더의 창·이벤트 자체가 보이지 않으면 차단할 수 없습니다.",12,Color.GRAY));
  c.addView(text("이 옵션은 미지원 브라우저만 제한합니다. 삼성 인터넷 등 지원 브라우저에서 유튜브 도메인만 읽히는 경우, 웹 문서 안의 쇼츠 플레이어 버튼 배치도 확인합니다. 주소·쇼츠 링크·플레이어 신호를 모두 읽지 못하면 감지가 누락될 수 있습니다. 일반 유튜브는 도메인만 보인다는 이유로 차단하지 않습니다.",12,Color.GRAY));
  c.addView(text("경로가 확인된 일반 영상은 허용합니다. 내장 웹 화면에서 새 채널을 만나면 강의/여가 분류를 물어봅니다.",14,Color.DKGRAY));
  c=card();c.addView(text("집중 잠금 · 조기 해제는 10분 뒤",20,Color.rgb(29,60,49)));
  lockStatus=text("",16,Color.DKGRAY);c.addView(lockStatus);
  c.addView(text("먼저 게임과 쇼츠 차단을 설정하세요. 잠금 중에는 차단 스위치와 게임 목록을 바꿀 수 없습니다. 정해진 시간이 끝나면 설정 잠금이 풀리며, 기존 차단은 유지됩니다.",14,Color.DKGRAY));
  startLockButton=button(c,"집중 잠금 시작",v->startLock());
  unlockButton=button(c,"조기 해제 요청 · 10분 대기",v->{if(!FocusLock.active(this)){refresh();return;}if(FocusLock.ready(this)){new AlertDialog.Builder(this).setTitle("설정 잠금을 해제할까요?").setMessage("차단은 그대로 켜져 있고, 스위치와 게임 목록을 다시 바꿀 수 있습니다.").setNegativeButton("유지",null).setPositiveButton("설정 잠금 해제",(d,w)->{FocusLock.release(this);FocusService.reload();refresh();}).show();}else if(!FocusLock.requested(this)){FocusLock.request(this);refresh();}});
  cancelUnlockButton=button(c,"해제 요청 취소",v->{FocusLock.cancel(this);refresh();});
  c.addView(text("해제 대기는 앱을 닫거나 화면을 꺼도 진행됩니다. 기다린 뒤 돌아와 해제를 눌러야 합니다. 설정 가드는 화면 인식에 의존하므로 완전한 삭제·해제 방지는 아닙니다. 재부팅 뒤에는 시스템 네트워크 시각을 우선하며, 없으면 기기 시각으로 복원합니다. 재부팅 직후 네트워크 시각이 없거나 서비스가 연결되기 전에는 짧은 보호 공백이 생길 수 있습니다.",12,Color.GRAY));
  c=card();c.addView(text("앱 삭제 · 10분 생각할 시간",20,Color.rgb(29,60,49)));removalStatus=text("",15,Color.DKGRAY);c.addView(removalStatus);
  c.addView(text("집중 잠금 중에도 삭제 허용을 요청할 수 있습니다. 10분 뒤 직접 진행하면 집중콩의 삭제·앱 정보 화면을 5분 동안 허용합니다. 허용된 5분 동안 앱 정보에서 강제 중지·삭제·데이터 삭제를 하면 차단도 함께 꺼져요(조기 해제와 같은 비용). 잠금이 끝나면 대기 없이 삭제할 수 있습니다. 재부팅하면 삭제 대기는 다시 요청해야 합니다.",13,Color.DKGRAY));
  removalButton=button(c,"삭제 허용 요청 · 10분 대기",v->removalAction());cancelRemovalButton=button(c,"삭제 허용 요청 취소",v->{RemovalAccess.cancel(this);FocusService.reload();refresh();});
  c=card();c.addView(text("유튜브 앱",20,Color.rgb(29,60,49)));
  c.addView(text("쇼츠 차단이 켜져 있으면 집중 잠금 중에는 유튜브 앱 전체를 홈으로 보냅니다. 일반 영상은 내장 웹 화면을 이용하세요. 잠금 밖에서는 쇼츠 플레이어 ID가 확인된 화면만 뒤로 보내고 남아 있으면 홈으로 이동합니다. 유튜브 업데이트로 ID가 바뀌면 감지가 누락될 수 있습니다.",14,Color.DKGRAY));
  c=card();c.addView(text("01  웹 유튜브",20,Color.rgb(29,60,49)));
  c.addView(text("주소창의 텍스트·설명, 클릭한 쇼츠 링크와 웹 쇼츠 플레이어의 버튼 배치를 확인합니다. 쇼츠에서 뒤로 이동한 뒤에도 쇼츠 주소나 화면 신호가 남으면 홈으로 이동합니다. 주소창 숨김·인앱 화면·브라우저 변경에 따라 감지가 누락될 수 있습니다.",14,Color.DKGRAY));
  button(c,"쇼츠 차단 웹 화면 열기",v->startActivity(new Intent(this,StudyActivity.class)));
  c.addView(text("앱 안의 웹 화면은 별도로 쇼츠 링크·화면을 차단합니다. 로그인 없이 사용하세요. 유튜브 화면 변경에 따라 보완이 필요할 수 있습니다.",12,Color.GRAY));
  c=card();c.addView(text("02  게임 차단",20,Color.rgb(29,60,49)));gameSummary=text("",14,Color.DKGRAY);c.addView(gameSummary);gamesButton=button(c,"차단할 게임 선택",v->chooseGames());
  c=card();c.addView(text("03  접근성 연결",20,Color.rgb(29,60,49)));status=text("",14,Color.DKGRAY);c.addView(status);
  c.addView(text("다른 앱의 게임·웹 쇼츠 차단에 필요합니다. 지원 브라우저 주소와 선택한 게임의 실행을 기기 안에서만 확인합니다. 런처 아이콘 없는 앱도 조회하기 위해 전체 패키지 조회 권한을 사용하며 설치 이벤트를 기기 안에 기록합니다. 전체 주소는 기록하지 않습니다. 이용 시간·채널 분류·차단 통계는 기기에 저장하고 서버로 전송하지 않습니다.",14,Color.DKGRAY));
  c.addView(text("삼성 기기: 설정 → 배터리 → 백그라운드 사용 제한 → 절전 예외 앱에 집중콩을 추가하세요. 앱 정보 → 배터리에서 ‘제한 없음’도 확인하세요. 접근성 서비스는 일반 앱의 실행 중 목록과 표시 방식이 다르므로 위의 연결·점검 시각으로 확인하세요.",12,Color.GRAY));
  button(c,"화면 view ID 수집 예약 · 30초",v->{boolean ready=FocusService.requestDump();Toast.makeText(this,ready?"30초 안에 유튜브·브라우저·설정 화면으로 이동하세요. 그동안 표시된 ID를 수집합니다.":"접근성을 먼저 연결하세요",Toast.LENGTH_LONG).show();});
  button(c,"수집된 view ID 보기",v->{TextView dump=text(FocusService.viewIds(),12,Color.DKGRAY);dump.setTextIsSelectable(true);ScrollView idScroll=new ScrollView(this);idScroll.addView(dump);new AlertDialog.Builder(this).setTitle("최근 화면 ID · 5분 뒤 지움").setView(idScroll).setPositiveButton("닫기",null).show();});
  button(c,"진단 복사 · 주소·제목 제외",v->{ClipboardManager clipboard=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);if(clipboard!=null){clipboard.setPrimaryClip(ClipData.newPlainText("집중콩 진단",FocusService.diagnostics(this)));Toast.makeText(this,"진단을 복사했어요",Toast.LENGTH_SHORT).show();}});
  button(c,"앱 업데이트 · 휴대폰에서 설치",v->startActivity(new Intent(this,UpdateActivity.class)));
  button(c,"접근성 설정 열기",v->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
  button(c,"이 앱의 정보 열기",v->startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,android.net.Uri.parse("package:"+getPackageName()))));
  c.addView(text("APK 설치 후 ‘제한된 설정’이 표시되면 앱 정보의 메뉴에서 허용할 수 있는지 확인하세요. 기기 보안 설정이 설치나 권한을 막으면 보안 기능을 해제하지 마세요.",12,Color.GRAY));
  body.addView(text("바로가기 설정을 읽지 못하는 기기에서는 등록 여부를 확실히 알 수 없습니다. 전화·긴급 통화·SystemUI는 차단하지 않습니다. 잠금 중에는 선택한 설정 가드가 적용됩니다. 루팅·유튜브 앱 수정 없이 동작하며 광고·추적 SDK는 없습니다.",12,Color.GRAY));
 }
 private void openAppInfo(){startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,android.net.Uri.parse("package:"+getPackageName())));}
 private void removalAction(){
  if(!FocusLock.active(this)||!Prefs.get(this).getBoolean("guard_locked_settings",true)||RemovalAccess.allowed(this)){openAppInfo();return;}
  if(RemovalAccess.ready(this)){if(RemovalAccess.grant(this)){FocusService.reload();openAppInfo();}refresh();return;}
  RemovalAccess.request(this);refresh();
 }
 private void refreshRemoval(){if(removalStatus==null)return;
  boolean guarded=FocusLock.active(this)&&Prefs.get(this).getBoolean("guard_locked_settings",true),pending=RemovalAccess.requested(this),allowed=RemovalAccess.allowed(this),ready=RemovalAccess.ready(this);
  removalStatus.setText(!guarded?"삭제 대기 없음 · 앱 정보에서 삭제 가능":allowed?"집중콩 삭제·앱 정보 허용 중 · "+FocusLock.format(RemovalAccess.allowedRemaining(this))+" 남음":ready?"대기 완료 · 아래 버튼을 눌러 5분간 허용하세요":pending?"삭제 허용까지 "+FocusLock.format(RemovalAccess.remaining(this)):"삭제를 원하면 10분 대기를 시작하세요");
  removalButton.setEnabled(!guarded||allowed||ready||!pending);removalButton.setText(!guarded||allowed?"앱 정보 열기 · 삭제 가능":ready?"삭제 진행 · 5분 허용":pending?"삭제 허용 대기 중":"삭제 허용 요청 · 10분 대기");cancelRemovalButton.setVisibility(guarded&&(pending||allowed)?View.VISIBLE:View.GONE);
 }
 private void refresh(){refreshRemoval();if(status!=null)status.setText(FocusService.diagnostics(this));if(gameSummary!=null)gameSummary.setText("선택한 앱 "+Prefs.games(this).size()+"개 · 잠금 중에는 Android가 게임으로 분류한 앱도 자동 차단합니다.");
  if(petStatus!=null){long xp=GrowthTracker.xp(this);int level=GrowthRules.level(xp);String next=level<4?"다음 성장까지 "+(GrowthRules.THRESHOLDS[level+1]-xp)+" XP":"모든 성장 단계 달성 · 연구는 계속!";petStatus.setText("Lv."+(level+1)+" "+GrowthRules.NAMES[level]+"  ·  "+xp+" XP\n"+next);long base=GrowthRules.THRESHOLDS[level];petProgress.setProgress(level==4?1000:(int)Math.min(1000,(xp-base)*1000/(GrowthRules.THRESHOLDS[level+1]-base)));petSpeech.setText(FocusService.connected()&&GrowthTracker.eligible(this)?"“한 번에 한 문제씩. 지금도 조금씩 자라고 있어.”":"“기다리고 있을게. 연결을 켜면 다시 함께 시작하자.”");}
  if(lockStatus==null)return;boolean locked=FocusLock.active(this),requested=FocusLock.requested(this);syncing=true;enabledSwitch.setChecked(Prefs.enabled(this));shortsSwitch.setChecked(Prefs.get(this).getBoolean("shorts",true));strictSwitch.setChecked(Prefs.get(this).getBoolean("strict_locked_web",true));guardSwitch.setChecked(Prefs.get(this).getBoolean("guard_locked_settings",true));newAppsSwitch.setChecked(Prefs.get(this).getBoolean("block_new_apps",false));otherProfilesSwitch.setChecked(Prefs.get(this).getBoolean("block_other_profiles",true));syncing=false;enabledSwitch.setEnabled(!locked);shortsSwitch.setEnabled(!locked);strictSwitch.setEnabled(!locked);guardSwitch.setEnabled(!locked);newAppsSwitch.setEnabled(!locked);otherProfilesSwitch.setEnabled(!locked);gamesButton.setEnabled(!locked);startLockButton.setVisibility(locked?View.GONE:View.VISIBLE);unlockButton.setVisibility(locked?View.VISIBLE:View.GONE);cancelUnlockButton.setVisibility(requested?View.VISIBLE:View.GONE);
  String message="설정 잠금 꺼짐 · 차단 설정 변경 가능";
  if(locked){message="집중 잠금 남은 시간 "+FocusLock.format(FocusLock.remaining(this));if(requested)message+="\n"+(FocusLock.ready(this)?"대기 완료 · 아래 버튼으로 해제 가능":"조기 해제까지 "+FocusLock.format(FocusLock.waitRemaining(this)));}
  lockStatus.setText(message);unlockButton.setEnabled(!requested||FocusLock.ready(this));unlockButton.setText(FocusLock.ready(this)?"설정 잠금 해제":requested?"해제 대기 중":"조기 해제 요청 · 10분 대기");
 }
 private void chooseStyle(){long xp=GrowthTracker.xp(this);String[] names={"성장에 맞춰 자동 꾸미기","연구복 · 50 XP","파동 고리 · 150 XP","양자 수식 · 400 XP"};for(int i=1;i<names.length;i++)if(!GrowthRules.styleUnlocked(i,xp))names[i]+=" · 아직 잠김";int selected=Prefs.get(this).getInt("pet_style",0);new AlertDialog.Builder(this).setTitle("프라임콩의 연구실").setSingleChoiceItems(names,selected,(d,i)->{if(!GrowthRules.styleUnlocked(i,GrowthTracker.xp(this))){Toast.makeText(this,"함께 쌓은 유지 시간으로 해금돼요",Toast.LENGTH_SHORT).show();return;}Prefs.get(this).edit().putInt("pet_style",i).apply();petView.invalidate();d.dismiss();}).setNegativeButton("닫기",null).show();}
 private void startLock(){if(FocusLock.active(this))return;if(!FocusService.connected()){new AlertDialog.Builder(this).setMessage("게임·외부 브라우저 차단을 위해 접근성을 먼저 켜주세요. 내장 웹 화면만 사용한다면 그대로 시작할 수도 있습니다.").setNegativeButton("취소",null).setPositiveButton("내장 웹 화면용으로 시작",(d,w)->pickDuration()).show();}else pickDuration();}
 private void pickDuration(){
  LinearLayout form=new LinearLayout(this);form.setOrientation(1);form.setPadding(dp(20),dp(8),dp(20),dp(8));form.addView(text("1~180분 사이에서 직접 입력하세요.",14,Color.DKGRAY));
  EditText minutes=new EditText(this);minutes.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);minutes.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(3)});minutes.setText("50");minutes.selectAll();minutes.setHint("집중 시간(분)");form.addView(minutes);
  AlertDialog dialog=new AlertDialog.Builder(this).setTitle("집중 잠금 시간 · 분").setView(form).setNegativeButton("취소",null).setPositiveButton("잠금 시작",null).create();
  dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{int value;try{value=Integer.parseInt(minutes.getText().toString().trim());}catch(NumberFormatException e){minutes.setError("1~180분을 입력하세요");return;}if(value<1||value>180){minutes.setError("1~180분을 입력하세요");return;}dialog.dismiss();attemptLock(value);}));dialog.show();
 }

 private void attemptLock(int minutes){
  ShortcutCheck.Result r=ShortcutCheck.check(this);
  if(r.registered){new AlertDialog.Builder(this).setMessage(DeviceConstants.SHORTCUT_NOTICE).setPositiveButton("접근성 설정 열기",(d,w)->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))).setNegativeButton("취소",null).show();return;}
  if(r.failed&&!ShortcutCheck.warned&&!Prefs.get(this).getBoolean("shortcut_warning_hidden",false)){
   ShortcutCheck.warned=true;CheckBox hide=new CheckBox(this);hide.setText("다시 보지 않기");new AlertDialog.Builder(this).setMessage("바로가기 설정을 읽지 못했어요. "+DeviceConstants.SHORTCUT_NOTICE).setView(hide).setPositiveButton("경고 확인 후 시작",(d,w)->{Prefs.get(this).edit().putBoolean("shortcut_warning_hidden",hide.isChecked()).apply();beginLock(minutes);}).setNegativeButton("취소",null).show();return;}
  beginLock(minutes);
 }
 private void beginLock(int minutes){FocusLock.start(this,minutes);FocusService.reload();refresh();}
 private void chooseGames(){
  if(FocusLock.active(this))return;
  PackageManager pm=getPackageManager();Intent q=new Intent(Intent.ACTION_MAIN);q.addCategory(Intent.CATEGORY_LAUNCHER);
  List<ResolveInfo> found=pm.queryIntentActivities(q,0);Map<String,String> labels=new HashMap<>();
  for(ResolveInfo r:found){String p=r.activityInfo.packageName;if(p.equals(getPackageName())||Prefs.browser(p))continue;
   if((r.activityInfo.applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM)!=0&&!YouTubeInspector.isPackage(p)&&!AppCatalog.declaredGame(r.activityInfo.applicationInfo))continue;
   labels.put(p,r.loadLabel(pm).toString()+(AppCatalog.declaredGame(r.activityInfo.applicationInfo)?" · 자동 감지됨":YouTubeInspector.isPackage(p)?" · 앱 전체 차단":" · 수동 선택"));}
  List<String> packages=new ArrayList<>(labels.keySet());Collections.sort(packages,(a,b)->labels.get(a).compareToIgnoreCase(labels.get(b)));
  String[] names=new String[packages.size()];boolean[] checked=new boolean[names.length];Set<String> selected=Prefs.games(this);
  for(int i=0;i<names.length;i++){names[i]=labels.get(packages.get(i));checked[i]=selected.contains(packages.get(i));}
  new AlertDialog.Builder(this).setTitle("차단할 게임 선택").setMultiChoiceItems(names,checked,(d,i,on)->checked[i]=on).setNegativeButton("취소",null).setPositiveButton("저장",(d,w)->{if(FocusLock.active(this))return;Set<String> out=new HashSet<>();for(int i=0;i<checked.length;i++)if(checked[i])out.add(packages.get(i));Prefs.get(this).edit().putStringSet("games",out).apply();FocusService.reload();refresh();}).show();
 }
}
