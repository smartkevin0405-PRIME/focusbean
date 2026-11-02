package app.bean.focus;
import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.view.*;
import android.widget.Toast;

public class PetView extends View {
 private final Paint p=new Paint(3);private boolean running=false;private long happyUntil=0,lastXpRead=-2000,cachedXp=0;
 public PetView(Context c){super(c);setContentDescription("프라임콩. 궤도 고리와 연구 노트를 가진 초록색 콩 연구자. 터치하면 인사합니다.");setFocusable(true);setOnClickListener(v->{happyUntil=SystemClock.uptimeMillis()+1800;invalidate();Toast.makeText(c,"프라임콩 · 한 번에 한 문제씩 가보자.",Toast.LENGTH_SHORT).show();});}
 private int color(String value){return Color.parseColor(value);}
 private void fill(Canvas c,String value){p.setColor(color(value));p.setStyle(Paint.Style.FILL);}
 private void oval(Canvas c,float a,float b,float d,float e,String color){fill(c,color);c.drawOval(a,b,d,e,p);}
 private void line(Canvas c,float x,float y,float x2,float y2,String color,float w){p.setColor(color(color));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(w);p.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x,y,x2,y2,p);p.setStyle(Paint.Style.FILL);}
 @Override protected void onAttachedToWindow(){super.onAttachedToWindow();running=true;}
 @Override protected void onDetachedFromWindow(){running=false;super.onDetachedFromWindow();}
 @Override protected void onDraw(Canvas c){super.onDraw(c);float scale=Math.min(getWidth()/360f,getHeight()/230f);c.save();c.translate((getWidth()-360*scale)/2,(getHeight()-230*scale)/2);c.scale(scale,scale);
  float t=SystemClock.uptimeMillis()/1000f;float bob=(float)Math.sin(t*1.7)*3;
  if(SystemClock.elapsedRealtime()-lastXpRead>1000){cachedXp=GrowthTracker.xp(getContext());lastXpRead=SystemClock.elapsedRealtime();}long minutes=cachedXp;int level=GrowthRules.level(minutes);int style=Prefs.get(getContext()).getInt("pet_style",0);if(!GrowthRules.styleUnlocked(style,minutes))style=0;
  oval(c,18,16,342,220,"#EEF4EC");
  p.setColor(color("#DCE8DE"));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);
  for(int x=40;x<330;x+=24)c.drawLine(x,32,x,206,p);for(int y=40;y<215;y+=24)c.drawLine(32,y,328,y,p);
  p.setStyle(Paint.Style.FILL);oval(c,121,194,240,208,"#D5E1D2");
  p.setColor(color("#8EAFA9"));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);c.save();c.rotate(-18,182,110);c.drawOval(66,80,298,140,p);c.restore();p.setStyle(Paint.Style.FILL);
  float angle=t*.6f;oval(c,181+(float)Math.cos(angle)*112-5,108+(float)Math.sin(angle)*29-5,181+(float)Math.cos(angle)*112+5,108+(float)Math.sin(angle)*29+5,"#D5AA4C");
  c.save();c.translate(0,bob);
  line(c,150,176,142,195,"#28574A",9);line(c,205,176,214,195,"#28574A",9);
  Path bean=new Path();bean.moveTo(219,43);bean.cubicTo(264,68,247,133,225,168);bean.cubicTo(203,206,133,195,116,153);bean.cubicTo(92,96,128,38,171,42);bean.cubicTo(186,44,192,57,187,73);bean.cubicTo(182,90,194,99,205,92);bean.cubicTo(219,84,222,61,219,43);bean.close();fill(c,"#729A63");c.drawPath(bean,p);
  oval(c,130,62,171,99,"#99BA7B");
  if(style==1||(style==0&&level>=1)){Path coat=new Path();coat.moveTo(118,136);coat.lineTo(153,146);coat.lineTo(175,163);coat.lineTo(196,143);coat.lineTo(240,129);coat.lineTo(225,173);coat.quadTo(183,205,137,176);coat.close();fill(c,"#FAFCF5");c.drawPath(coat,p);line(c,175,163,175,188,"#C5D5C8",2);line(c,148,158,160,158,"#24745C",2);}
  boolean happy=SystemClock.uptimeMillis()<happyUntil;boolean blink=((long)(t*10)%53)<2;
  if(happy||blink){line(c,142,113,151,110,"#203F37",3);line(c,182,110,191,113,"#203F37",3);}else{oval(c,143,108,150,119,"#203F37");oval(c,184,108,191,119,"#203F37");}
  oval(c,130,122,145,130,"#DAB7A0");oval(c,191,122,206,130,"#DAB7A0");p.setColor(color("#203F37"));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.5f);c.drawArc(157,121,178,135,15,150,false,p);p.setStyle(Paint.Style.FILL);
  line(c,116,143,97,154,"#28574A",7);line(c,234,142,253,150,"#28574A",7);
  c.save();c.rotate(12,258,155);fill(c,"#213D36");c.drawRoundRect(242,136,276,179,4,4,p);fill(c,"#EAF1D4");c.drawRoundRect(245,139,273,176,2,2,p);p.setTextSize(14);p.setTypeface(Typeface.create("serif",Typeface.ITALIC));p.setColor(color("#376955"));c.drawText("ψ",254,156,p);line(c,249,164,269,164,"#9DB69B",1);line(c,249,169,265,169,"#9DB69B",1);c.restore();
  line(c,166,48,161,27,"#28574A",3);oval(c,160,15,179,32,"#92B97D");
  if(style==2||(style==0&&level>=2)){p.setColor(color("#81B1B4"));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);for(int k=0;k<3;k++)c.drawArc(62-k*8,120-k*8,104+k*8,164+k*8,220,100,false,p);p.setStyle(Paint.Style.FILL);}
  if(style==3||(style==0&&level>=3)){p.setColor(color("#8498B7"));p.setTextSize(14);p.setTypeface(Typeface.create("serif",Typeface.ITALIC));c.drawText("E = ℏω",244,57,p);c.drawText("∫",91,74,p);}
  if(level>=4){oval(c,276,174,300,198,"#E3C877");p.setColor(color("#685B2F"));p.setTextSize(17);p.setTypeface(Typeface.DEFAULT_BOLD);c.drawText("P",282,193,p);}
  c.restore();c.restore();if(running&&getWindowVisibility()==VISIBLE)postInvalidateDelayed(80);
 }
}
