package app.bean.focus;
import android.app.*;import android.os.*;import android.graphics.Color;import android.view.*;import android.widget.*;
public class FormulaActivity extends Activity {
 private LinearLayout body;
 public void onCreate(Bundle b){super.onCreate(b);ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(Color.rgb(245,247,243));body=new LinearLayout(this);body.setOrientation(1);body.setPadding(24,32,24,40);scroll.addView(body);scroll.setOnApplyWindowInsetsListener((v,i)->{v.setPadding(i.getSystemWindowInsetLeft(),i.getSystemWindowInsetTop(),i.getSystemWindowInsetRight(),i.getSystemWindowInsetBottom());return i.consumeSystemWindowInsets();});setContentView(scroll);draw();}
 private void text(String s,int size){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(Color.rgb(29,60,49));v.setPadding(8,14,8,14);body.addView(v);}
 private void draw(){body.removeAllViews();text("XP 수식 연구실",26);text("x = 오늘의 차단 유지 시간(분) ÷ 50\nC = 1 + .64 g(1)\n수식의 모양을 보고 골라보세요. 보유 XP는 유지됩니다. 오늘 적립을 시작했다면 새 선택은 다음 날부터 적용됩니다.",14);
  int current=AnalyticsDb.get(this).todayFormula(),next=Prefs.get(this).getInt("xp_next",8);text("오늘: "+XpMath.name(current)+"\n다음 적립일: "+XpMath.name(next),16);
  for(int i=0;i<XpMath.NAMES.length;i++){final int choice=i+8;LinearLayout card=new LinearLayout(this);card.setOrientation(1);card.setPadding(16,18,16,18);card.setBackgroundColor(Color.WHITE);LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,-2);params.bottomMargin=16;body.addView(card,params);TextView formula=new TextView(this);formula.setText(XpMath.NAMES[i]+"\n"+XpMath.EXPR[i]);formula.setTextSize(18);formula.setTextColor(Color.rgb(29,60,49));card.addView(formula);
   Button select=new Button(this);select.setText(choice==next?"선택됨":"이 수식 고르기");select.setEnabled(choice!=next);select.setOnClickListener(v->{Prefs.get(this).edit().putInt("xp_next",choice).apply();draw();});card.addView(select);
  }text("Γ: 감마 함수 · erf: 오차 함수 · asinh: 역쌍곡사인\n∫: 1/(1+u⁴)의 누적 적분. 시간에 따라 증가하는 모양이 서로 다릅니다. 실제 획득한 XP는 캐릭터와 통계에서 확인할 수 있습니다.",13);
 }
}
