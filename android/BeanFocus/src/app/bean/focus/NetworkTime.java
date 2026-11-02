package app.bean.focus;
import android.os.*;
/** Reads Android's already-maintained network clock; does not make an app network request. */
final class NetworkTime {
 static String status="아직 네트워크 시각 확인 안 함";
 static long now(){if(Build.VERSION.SDK_INT<33){status="네트워크 시각 API 없음 · 벽시계 대체";return -1;}try{long value=SystemClock.currentNetworkTimeClock().millis();status="네트워크 시각 사용 가능";return value;}catch(RuntimeException e){status="네트워크 시각 없음 · 벽시계 대체";return -1;}}
}
