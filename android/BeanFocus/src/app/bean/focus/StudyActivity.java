package app.bean.focus;
import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import java.io.*;
import java.net.URI;

public class StudyActivity extends Activity {
 private WebView web;private EditText address;private LinearLayout root;private android.widget.FrameLayout full;
 private WebChromeClient.CustomViewCallback fullCallback;private boolean guardEnabled;
 private String guard="",probe="";private boolean active=false,asking=false;private AlertDialog channelDialog;private TelemetrySession session;private String video="",channel="",channelName="",kind="unknown",candidate="";private int candidateCount=0;private long lastProbe=0;
 private final Handler handler=new Handler(Looper.getMainLooper());
 private final Runnable poll=new Runnable(){public void run(){if(web==null)return;inspect(web.getUrl());track();handler.postDelayed(this,500);}};
 @Override public void onCreate(Bundle state){super.onCreate(state);
  session=new TelemetrySession(this,"internal");probe=readAsset("channel_probe.js");guardEnabled=Prefs.shorts(this);try{InputStream in=getAssets().open("shorts_guard.js");ByteArrayOutputStream b=new ByteArrayOutputStream();byte[] buf=new byte[4096];int n;while((n=in.read(buf))!=-1)b.write(buf,0,n);in.close();guard=b.toString("UTF-8");}catch(IOException ignored){}
  root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(Color.WHITE);
  root.setOnApplyWindowInsetsListener((v,i)->{v.setPadding(i.getSystemWindowInsetLeft(),i.getSystemWindowInsetTop(),i.getSystemWindowInsetRight(),i.getSystemWindowInsetBottom());return i.consumeSystemWindowInsets();});setContentView(root);
  LinearLayout toolbar=new LinearLayout(this);Button back=new Button(this);back.setText("‹");back.setOnClickListener(v->back());toolbar.addView(back,new LinearLayout.LayoutParams((int)(48*getResources().getDisplayMetrics().density),-2));
  address=new EditText(this);address.setSingleLine(true);address.setTextSize(13);address.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_URI);address.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_GO);address.setHint("유튜브 주소 또는 검색어");toolbar.addView(address,new LinearLayout.LayoutParams(0,-2,1));Button go=new Button(this);go.setText("이동");toolbar.addView(go,new LinearLayout.LayoutParams(-2,-2));root.addView(toolbar);
  TextView note=new TextView(this);note.setText(guardEnabled?"  쇼츠 차단 중 · 일반 영상 허용 · 로그인 없이 이용":"  차단 꺼짐 · 일반 웹 화면");note.setTextSize(12);note.setPadding(8,6,8,6);root.addView(note);
  web=new WebView(this);root.addView(web,new LinearLayout.LayoutParams(-1,0,1));WebSettings settings=web.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);settings.setAllowFileAccess(false);settings.setAllowContentAccess(false);settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);settings.setMediaPlaybackRequiresUserGesture(true);settings.setSupportMultipleWindows(false);
  CookieManager.getInstance().setAcceptThirdPartyCookies(web,false);
  web.setWebViewClient(new WebViewClient(){
   @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){return reject(r.getUrl().toString());}
   @Override public void onPageStarted(WebView v,String url,android.graphics.Bitmap icon){inspect(url);}
   @Override public void onPageFinished(WebView v,String url){inspect(url);}
   @Override public void doUpdateVisitedHistory(WebView v,String url,boolean reload){inspect(url);}
   @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest r){if(guardEnabled&&UrlRules.isShorts(r.getUrl().toString()))return new WebResourceResponse("text/plain","UTF-8",new ByteArrayInputStream(new byte[0]));return null;}
  });
  web.setWebChromeClient(new WebChromeClient(){
   @Override public void onShowCustomView(View view,CustomViewCallback cb){if(full!=null){cb.onCustomViewHidden();return;}fullCallback=cb;full=new FrameLayout(StudyActivity.this);full.setBackgroundColor(Color.BLACK);full.addView(view,new FrameLayout.LayoutParams(-1,-1));web.setVisibility(View.GONE);addContentView(full,new ViewGroup.LayoutParams(-1,-1));}
   @Override public void onHideCustomView(){hideFull();}
  });
  go.setOnClickListener(v->navigate());address.setOnEditorActionListener((v,action,event)->{navigate();return true;});
  web.loadUrl("https://m.youtube.com/");
 }
 private boolean reject(String url){
  if(guardEnabled&&UrlRules.isShorts(url)){AnalyticsDb.get(this).block("shorts","internal");Toast.makeText(this,"쇼츠는 차단했어요",Toast.LENGTH_SHORT).show();return true;}
  URI u=UrlRules.parse(url);if(u==null||!"https".equalsIgnoreCase(u.getScheme())||!UrlRules.isYouTube(url)){Toast.makeText(this,"이 화면에서는 HTTPS 유튜브 링크만 열 수 있어요",Toast.LENGTH_SHORT).show();return true;}return false;
 }
 private void navigate(){String s=address.getText().toString().trim();if(s.isEmpty())return;String url;
  if(UrlRules.isYouTube(s)){url=UrlRules.parse(s).toString();}else if(s.contains("://")||s.startsWith("www.")){Toast.makeText(this,"유튜브 주소를 입력하세요",Toast.LENGTH_SHORT).show();return;}else{try{url="https://m.youtube.com/results?search_query="+java.net.URLEncoder.encode(s,"UTF-8");}catch(Exception e){return;}}
  if(!reject(url)){web.loadUrl(url);((android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(address.getWindowToken(),0);address.clearFocus();web.requestFocus();}
 }
 private void inspect(String url){if(url==null)return;if(guardEnabled&&UrlRules.isShorts(url)){AnalyticsDb.get(this).block("shorts","internal");web.stopLoading();web.loadUrl("https://m.youtube.com/");return;}if(!address.hasFocus())address.setText(url);if(guardEnabled&&UrlRules.isYouTube(url))web.evaluateJavascript(guard,null);}
 private void hideFull(){if(full==null)return;((ViewGroup)full.getParent()).removeView(full);full=null;web.setVisibility(View.VISIBLE);if(fullCallback!=null){fullCallback.onCustomViewHidden();fullCallback=null;}}
 private void back(){if(full!=null)hideFull();else if(web.canGoBack())web.goBack();else finish();}
 @Override public void onBackPressed(){back();}
 @Override protected void onResume(){super.onResume();active=true;if(web!=null){boolean next=Prefs.shorts(this);if(next!=guardEnabled){guardEnabled=next;web.reload();}web.onResume();handler.removeCallbacks(poll);handler.post(poll);}}
 @Override protected void onPause(){active=false;if(session!=null)session.stop();handler.removeCallbacks(poll);if(web!=null){web.onPause();web.evaluateJavascript("document.querySelectorAll('video').forEach(v=>v.pause())",null);}super.onPause();}
 private String readAsset(String name){try{InputStream in=getAssets().open(name);ByteArrayOutputStream b=new ByteArrayOutputStream();byte[] bytes=new byte[4096];int n;while((n=in.read(bytes))!=-1)b.write(bytes,0,n);in.close();return b.toString("UTF-8");}catch(Exception e){return "({video:'',key:'',name:''})";}}
 private String currentVideo(){String u=web.getUrl();if(u==null)return "";android.net.Uri uri=android.net.Uri.parse(u);return "/watch".equals(uri.getPath())?(uri.getQueryParameter("v")==null?"":uri.getQueryParameter("v")):"";}
 private void track(){if(!active||web==null)return;String nowVideo=currentVideo();if(!video.equals(nowVideo)){video=nowVideo;channel="";kind="unknown";candidate="";candidateCount=0;session.stop();}String url=web.getUrl();String sampleKind=asking?null:UrlRules.isShorts(url)?"shorts":kind;session.sample(sampleKind,channel);long now=SystemClock.elapsedRealtime();if(video.isEmpty()||asking||now-lastProbe<2000)return;lastProbe=now;final String expected=video;web.evaluateJavascript(probe,result->{if(!active||web==null||!currentVideo().equals(expected))return;try{org.json.JSONObject data=new org.json.JSONObject(result);String key=data.optString("key"),name=data.optString("name");if(!expected.equals(data.optString("video"))||key.isEmpty())return;if(!key.equals(candidate)){candidate=key;candidateCount=1;return;}if(++candidateCount<2)return;key=AnalyticsDb.get(this).resolveChannel(key,data.optString("alias"));if(!key.equals(channel)){session.stop();channel=key;channelName=name.isEmpty()?key:name;}AnalyticsDb db=AnalyticsDb.get(this);if(db.hasChannel(key)){kind=db.channelKind(key);return;}askChannel(expected,key,channelName);}catch(Exception ignored){}});}
 private void askChannel(String expected,String key,String name){if(asking||!active)return;asking=true;session.stop();web.evaluateJavascript("document.querySelectorAll('video').forEach(v=>v.pause())",null);channelDialog=new AlertDialog.Builder(this).setTitle("처음 보는 채널이에요").setMessage(name+"\n이 채널은 강의인가요, 여가인가요? 선택을 저장합니다. 선택 후 재생을 눌러주세요.").setPositiveButton("강의",(d,w)->saveChannel(expected,key,name,"lecture")).setNegativeButton("여가",(d,w)->saveChannel(expected,key,name,"leisure")).setNeutralButton("미분류로 두기",(d,w)->saveChannel(expected,key,name,"unknown")).setOnCancelListener(d->saveChannel(expected,key,name,"unknown")).show();}
 private void saveChannel(String expected,String key,String name,String type){AnalyticsDb.get(this).channel(key,name,type);asking=false;if(web!=null&&expected.equals(currentVideo())){channel=key;kind=type;}session.stop();}
 @Override public void onDestroy(){active=false;if(channelDialog!=null)channelDialog.dismiss();handler.removeCallbacksAndMessages(null);hideFull();if(web!=null){root.removeView(web);web.destroy();web=null;}super.onDestroy();}
}
