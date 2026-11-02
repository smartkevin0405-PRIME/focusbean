package app.bean.focus;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import org.json.*;
import java.time.*;
import java.util.*;
final class AnalyticsDb extends SQLiteOpenHelper {
 private static AnalyticsDb instance;
 private final Context context;
 static synchronized AnalyticsDb get(Context c){if(instance==null)instance=new AnalyticsDb(c.getApplicationContext());return instance;}
 private AnalyticsDb(Context c){super(c,"analytics.db",null,1);context=c;if(Prefs.get(c).getInt("xp_rules",1)<2){int next=Prefs.get(c).getInt("xp_next",0);Prefs.get(c).edit().putInt("xp_next",Math.floorMod(next,8)+8).putInt("xp_rules",2).commit();}}
 public void onCreate(SQLiteDatabase d){
  d.execSQL("CREATE TABLE spans(id INTEGER PRIMARY KEY,s INTEGER,e INTEGER,k TEXT,detail TEXT,src TEXT)");d.execSQL("CREATE INDEX span_time ON spans(s,e)");
  d.execSQL("CREATE TABLE blocks(t INTEGER,k TEXT,detail TEXT)");
  d.execSQL("CREATE TABLE channels(id TEXT PRIMARY KEY,name TEXT,kind TEXT)");d.execSQL("CREATE TABLE channel_alias(alias TEXT PRIMARY KEY,id TEXT)");
  d.execSQL("CREATE TABLE growth(day TEXT PRIMARY KEY,ms INTEGER,formula INTEGER,xp REAL)");
  d.execSQL("CREATE TABLE game_daily(day TEXT,pkg TEXT,ms INTEGER,PRIMARY KEY(day,pkg))");
  d.execSQL("CREATE TABLE game_coverage(day TEXT PRIMARY KEY)");
  d.execSQL("CREATE TABLE meta(k TEXT PRIMARY KEY,v TEXT)");
  ContentValues v=new ContentValues();v.put("k","legacy_xp");v.put("v",Double.toString(Prefs.get(context).getLong("pet_ms",0)/60000.0));d.insert("meta",null,v);
 }
 public void onUpgrade(SQLiteDatabase d,int a,int b){}
 static String day(long wall){return Instant.ofEpochMilli(wall).atZone(ZoneId.systemDefault()).toLocalDate().toString();}
 synchronized void span(long s,long e,String kind,String detail,String src){if(e<=s||e-s>90000)return;SQLiteDatabase d=getWritableDatabase();
  try(Cursor c=d.rawQuery("SELECT id,e,k,detail,src FROM spans ORDER BY id DESC LIMIT 1",null)){
   if(c.moveToFirst()&&c.getLong(1)==s&&kind.equals(c.getString(2))&&detail.equals(c.getString(3))&&src.equals(c.getString(4))){ContentValues v=new ContentValues();v.put("e",e);d.update("spans",v,"id=?",new String[]{c.getString(0)});return;}}
  ContentValues v=new ContentValues();v.put("s",s);v.put("e",e);v.put("k",kind);v.put("detail",detail);v.put("src",src);d.insert("spans",null,v);
 }
 synchronized void block(String k,String detail){blockAt(k,detail,System.currentTimeMillis());}
 synchronized void blockAt(String k,String detail,long now){SQLiteDatabase d=getWritableDatabase();try(Cursor c=d.rawQuery("SELECT t FROM blocks WHERE k=? AND detail=? ORDER BY t DESC LIMIT 1",new String[]{k,detail})){if(c.moveToFirst()&&now-c.getLong(0)<30000)return;}ContentValues v=new ContentValues();v.put("t",now);v.put("k",k);v.put("detail",detail);d.insert("blocks",null,v);}
 synchronized String resolveChannel(String id,String alias){SQLiteDatabase d=getWritableDatabase();try(Cursor c=d.rawQuery("SELECT id FROM channel_alias WHERE alias=?",new String[]{id})){if(c.moveToFirst())return c.getString(0);}if(id.startsWith("/channel/")&&!alias.isEmpty()&&!alias.equals(id)){if(!hasChannel(id)&&hasChannel(alias)){try(Cursor c=d.rawQuery("SELECT name,kind FROM channels WHERE id=?",new String[]{alias})){if(c.moveToFirst())channel(id,c.getString(0),c.getString(1));}ContentValues detail=new ContentValues();detail.put("detail",id);d.update("spans",detail,"detail=?",new String[]{alias});d.delete("channels","id=?",new String[]{alias});}ContentValues v=new ContentValues();v.put("alias",alias);v.put("id",id);d.insertWithOnConflict("channel_alias",null,v,SQLiteDatabase.CONFLICT_REPLACE);}return id;}
 synchronized boolean hasChannel(String id){try(Cursor c=getReadableDatabase().rawQuery("SELECT 1 FROM channels WHERE id=?",new String[]{id})){return c.moveToFirst();}}
 synchronized String channelKind(String id){try(Cursor c=getReadableDatabase().rawQuery("SELECT kind FROM channels WHERE id=?",new String[]{id})){return c.moveToFirst()?c.getString(0):"unknown";}}
 synchronized void channel(String id,String name,String kind){ContentValues v=new ContentValues();v.put("id",id);v.put("name",name);v.put("kind",kind);getWritableDatabase().insertWithOnConflict("channels",null,v,SQLiteDatabase.CONFLICT_REPLACE);}
 synchronized JSONArray channels(){JSONArray out=new JSONArray();try(Cursor c=getReadableDatabase().rawQuery("SELECT id,name,kind FROM channels ORDER BY name",null)){while(c.moveToNext()){JSONObject x=new JSONObject();try{x.put("id",c.getString(0));x.put("name",c.getString(1));x.put("kind",c.getString(2));out.put(x);}catch(JSONException ignored){}}}return out;}
 synchronized double xp(){SQLiteDatabase d=getReadableDatabase();double total=0;try(Cursor c=d.rawQuery("SELECT v FROM meta WHERE k='legacy_xp'",null)){if(c.moveToFirst())total=Double.parseDouble(c.getString(0));}try(Cursor c=d.rawQuery("SELECT COALESCE(SUM(xp),0) FROM growth",null)){if(c.moveToFirst())total+=c.getDouble(0);}return total;}
 synchronized long todayGrowth(){try(Cursor c=getReadableDatabase().rawQuery("SELECT ms FROM growth WHERE day=?",new String[]{day(System.currentTimeMillis())})){return c.moveToFirst()?c.getLong(0):0;}}
 synchronized int todayFormula(){try(Cursor c=getReadableDatabase().rawQuery("SELECT formula FROM growth WHERE day=?",new String[]{day(System.currentTimeMillis())})){return c.moveToFirst()?c.getInt(0):Prefs.get(context).getInt("xp_next",0);}}
 synchronized void growth(long ms,long now){long at=now-ms;while(at<now){LocalDate date=Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).toLocalDate();long end=Math.min(now,date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());growthDay(end-at,date.toString());at=end;}}
 private void growthDay(long ms,String key){SQLiteDatabase d=getWritableDatabase();long old=0;int formula=Prefs.get(context).getInt("xp_next",0);try(Cursor c=d.rawQuery("SELECT ms,formula FROM growth WHERE day=?",new String[]{key})){if(c.moveToFirst()){old=c.getLong(0);formula=c.getInt(1);}}ContentValues v=new ContentValues();v.put("day",key);v.put("ms",old+ms);v.put("formula",formula);v.put("xp",XpMath.value(formula,(old+ms)/60000.0));d.insertWithOnConflict("growth",null,v,SQLiteDatabase.CONFLICT_REPLACE);}
 synchronized void replaceGameDays(Map<String,Map<String,Long>> byDay,Set<String> covered){SQLiteDatabase d=getWritableDatabase();d.beginTransaction();try{for(String day:covered){d.delete("game_daily","day=?",new String[]{day});ContentValues cov=new ContentValues();cov.put("day",day);d.insertWithOnConflict("game_coverage",null,cov,SQLiteDatabase.CONFLICT_REPLACE);Map<String,Long> rows=byDay.get(day);if(rows!=null)for(Map.Entry<String,Long> row:rows.entrySet()){ContentValues v=new ContentValues();v.put("day",day);v.put("pkg",row.getKey());v.put("ms",row.getValue());d.insertWithOnConflict("game_daily",null,v,SQLiteDatabase.CONFLICT_REPLACE);}}d.setTransactionSuccessful();}finally{d.endTransaction();}}
 synchronized JSONObject snapshot(){JSONObject out=new JSONObject();SQLiteDatabase d=getReadableDatabase();long end=System.currentTimeMillis(),start=LocalDate.now().minusDays(62).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();try{
  out.put("now",end);JSONArray formulas=new JSONArray();for(int i=0;i<16;i++)formulas.put(XpMath.name(i));out.put("formulas",formulas);out.put("today",LocalDate.now().toString());out.put("selectedGames",Prefs.games(context).size());out.put("gameImportAt",Prefs.get(context).getLong("stats_game_import",0));out.put("xp",xp());
  JSONArray days=new JSONArray();for(int i=62;i>=0;i--){LocalDate date=LocalDate.now().minusDays(i);long s=date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),e=Math.min(end,date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());JSONObject row=new JSONObject();row.put("day",date.toString());for(String k:new String[]{"lecture","leisure","unknown","shorts","coverage"})row.put(k,0);JSONArray hours=new JSONArray();for(int h=0;h<24;h++)hours.put(0);row.put("hours",hours);JSONObject channeluse=new JSONObject();row.put("channeluse",channeluse);row.put("game",JSONObject.NULL);row.put("blocks",0);row.put("xp",JSONObject.NULL);row.put("growth",JSONObject.NULL);
   try(Cursor c=d.rawQuery("SELECT s,e,k,detail FROM spans WHERE s<? AND e>?",new String[]{Long.toString(e),Long.toString(s)})){while(c.moveToNext()){long a=Math.max(s,c.getLong(0)),b=Math.min(e,c.getLong(1));String k=c.getString(2);if(!row.has(k))continue;row.put(k,row.getLong(k)+b-a);String detail=c.getString(3);if(!k.equals("coverage")&&!detail.isEmpty())channeluse.put(detail,channeluse.optLong(detail)+b-a);if(!k.equals("coverage")){long at=a;while(at<b){ZonedDateTime z=Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault());long next=Math.min(b,z.truncatedTo(java.time.temporal.ChronoUnit.HOURS).plusHours(1).toInstant().toEpochMilli());int h=z.getHour();hours.put(h,hours.getLong(h)+next-at);at=next;}}}}
   try(Cursor c=d.rawQuery("SELECT COUNT(*) FROM game_coverage WHERE day=?",new String[]{date.toString()})){if(c.moveToFirst()&&c.getInt(0)>0){long ms=0;try(Cursor game=d.rawQuery("SELECT COALESCE(SUM(ms),0) FROM game_daily WHERE day=?",new String[]{date.toString()})){if(game.moveToFirst())ms=game.getLong(0);}row.put("game",ms);}}
   try(Cursor c=d.rawQuery("SELECT COUNT(*) FROM blocks WHERE t>=? AND t<?",new String[]{Long.toString(s),Long.toString(e)})){if(c.moveToFirst())row.put("blocks",c.getLong(0));}
   try(Cursor c=d.rawQuery("SELECT ms,xp,formula FROM growth WHERE day=?",new String[]{date.toString()})){if(c.moveToFirst()){row.put("growth",c.getLong(0));row.put("xp",c.getDouble(1));row.put("formula",c.getInt(2));}}
   days.put(row);
  }out.put("days",days);out.put("channels",channels());
  JSONArray apps=new JSONArray();try(Cursor c=d.rawQuery("SELECT day,pkg,ms FROM game_daily ORDER BY day DESC",null)){while(c.moveToNext()){JSONObject r=new JSONObject();r.put("day",c.getString(0));r.put("pkg",c.getString(1));String name=c.getString(1);try{name=context.getPackageManager().getApplicationLabel(context.getPackageManager().getApplicationInfo(name,0)).toString();}catch(Exception ignored){}r.put("name",name);r.put("ms",c.getLong(2));apps.put(r);}}out.put("apps",apps);
 }catch(JSONException ignored){}return out;}
}
