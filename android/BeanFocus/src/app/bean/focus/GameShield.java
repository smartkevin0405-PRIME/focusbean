package app.bean.focus;
import android.accessibilityservice.AccessibilityService;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Non-focusable touch shields clipped to exposed game regions, below system navigation. */
final class GameShield {
 private final AccessibilityService service;private final WindowManager manager;private final java.util.function.BooleanSupplier onClose;
 static final class Area {final Rect rect;final String message;Area(Rect r,String m){rect=new Rect(r);message=m;}}
 private final List<View> views=new ArrayList<>();private final List<Rect> bounds=new ArrayList<>();private final List<String> messages=new ArrayList<>();
 GameShield(AccessibilityService s,java.util.function.BooleanSupplier close){service=s;onClose=close;manager=(WindowManager)s.getSystemService(android.content.Context.WINDOW_SERVICE);}
 void show(List<Area> areas){
  List<Rect> nextBounds=new ArrayList<>();List<String> nextMessages=new ArrayList<>();for(Area a:areas){nextBounds.add(a.rect);nextMessages.add(a.message);}if(nextBounds.equals(bounds)&&nextMessages.equals(messages))return;hide();
  try{for(Area entry:areas){Rect area=entry.rect;if(area.isEmpty())continue;
   LinearLayout panel=new LinearLayout(service);panel.setOrientation(1);panel.setGravity(Gravity.CENTER);panel.setPadding(12,12,12,12);panel.setBackgroundColor(Color.rgb(238,246,240));
   TextView label=new TextView(service);label.setText(entry.message.isEmpty()?"집중콩 · 앱 차단 중":entry.message);label.setTextColor(Color.rgb(29,60,49));label.setTextSize(entry.message.isEmpty()?18:12);panel.addView(label);
   if(entry.message.isEmpty()){
   Button home=new Button(service);home.setText("홈으로 이동");home.setOnClickListener(v->((FocusService)service).userGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME));panel.addView(home);
   Button recents=new Button(service);recents.setText("최근 앱에서 닫기");recents.setOnClickListener(v->onClose.getAsBoolean());panel.addView(recents);
   TextView help=new TextView(service);help.setText("최근 앱에서 카드를 위로 밀어 닫으세요. 앱을 다시 열면 즉시 차단합니다. 정리는 2분에 한 번 가능합니다.");help.setTextColor(Color.DKGRAY);panel.addView(help);
   }else panel.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);
   WindowManager.LayoutParams p=new WindowManager.LayoutParams(area.width(),area.height(),WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.OPAQUE);
   p.gravity=Gravity.TOP|Gravity.LEFT;p.x=area.left;p.y=area.top;p.setTitle("집중콩 앱 차단");
   manager.addView(panel,p);views.add(panel);bounds.add(new Rect(area));messages.add(entry.message);
  }}catch(RuntimeException e){hide();throw e;}
 }
 void hide(){RuntimeException error=null;for(View v:views)try{manager.removeViewImmediate(v);}catch(RuntimeException e){error=e;}views.clear();bounds.clear();messages.clear();if(error!=null)throw error;}
}
