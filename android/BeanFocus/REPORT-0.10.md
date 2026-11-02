# 집중콩 0.10-beta 구현·검증 보고서

2026-11-02 · 패키지 `app.bean.focus` · 실기기 검증 전

## 1. 기준 상태, 백업과 안전 우선 결정

작업 전 0.9 소스를 `backup-0.9-before-0.10.zip`으로 복제했다. 백업 SHA-256은 `6d5c839c58fcbfd8e27396ef4115e459b47dbf908ac3df84fe5e40e7be09af33`이며 기존 0.9 소스 ZIP과 동일하다. 배포용 백업 파일명은 `BeanFocus-backup-0.9.zip`이다. 관련 클래스 전체를 읽고 실제 소스 기준으로 수정했다. Java·Android SDK 빌드, minSdk 26/targetSdk 35, 기존 키스토어를 유지했다.

지시서와 실제 소스의 주요 차이는 `SettingsGuard.Match.management`였다. 새 `removalAllowable`을 추가하고 기존 필드를 호환 별칭으로 유지했다. 기존 `inspect`/`reason`은 수집기·순수 판정으로 위임하는 호환 진입점이다. `TimerRules.remaining`도 기존 인자 형식을 보존하고 네트워크 마감을 받는 오버로드를 추가했다. 기존 `YouTubeInspector.PACKAGE`는 기본 패키지 호환 상수로 남지만 실제 판정은 공통 집합을 사용한다.

안전 우선 결정:

- `activeRoot`의 포커스 창 대체 탐색만으로는 I2를 보장하기 어려워, 실제 활성 루트의 패키지를 먼저 확인하고 글로벌 동작 직전에 다시 확인한다. 집중콩·emergencySurface면 자동 HOME/BACK을 보내지 않는다. PiP 창에도 자동 HOME/BACK을 보내지 않는다.
- 집중콩·전화·SystemUI 영역을 가림막에서 제외한다. 보호 화면 전환 시 기존 가림막부터 지워 실패한 재계산으로 가림막이 남는 위험을 줄인다. 겹친 영역에서는 차단보다 폰 사용 가능성을 우선한다.
- PiP 닫기 요청 수락은 성공으로 세지 않는다. 이후 창이 실제 접근성 창 목록에서 사라졌을 때 이탈로 기록한다. 같은 창이 1.5초 이후에도 남으면 실패로 세고, 세 번 실패하면 60초 쉰다. 실패한 영역에는 지정 안내 문구를 작게 표시한다.
- 최근 앱 정리의 대상·시간·쿨다운을 유지했다. PiP와 유튜브에는 가림막 유예를 주지 않는다. 최근 앱 화면에서 닫기 작업을 할 때만 대상의 HOME을 잠시 억제하며, 차단 앱이 다시 활성화되면 취소한다.
- R2의 규칙 순서를 그대로 적용했다. 새로운 실기기 덤프를 받지 않았으므로 ID 후보를 확인된 값으로 표시하지 않았다.
- 다른 프로필은 요청한 `NameNotFoundException` 기준의 추정이다. 프로필 ID를 직접 검증했다는 의미가 아니다. 보이지 않는 창까지 막는다고 주장하지 않는다.
- 안전 모드·10분 조기 해제·삭제 대기·ADB·공장 초기화는 비상구로 남겼다. 차단의 영구 고정이나 접근성 자동 재활성화는 추가하지 않았다.

## 2. R1~R9 상태

‘완료’는 코드와 자동 검증 기준이다. 실제 One UI의 수용 기준은 6장의 빈 체크리스트로 별도 검증해야 한다.

| 요구 | 상태 | 구현 및 남은 조건 | 변경 파일 |
|---|---|---|---|
| R1 | 일부: 코드 완료, 기기 수용 검증 대기 | 자동 동작 헬퍼, 창 단위 순수 결정, PiP dismiss·1.5초 간격·3회/60초 제한, 300ms 이벤트 디바운스, 영역별 실패 안내 구현. 실제 PiP가 약 1초에 닫히는지는 기기 지원에 달림 | FocusService.java, GameShield.java, WindowPolicy.java, PipDismissTracker.java, DeviceConstants.java |
| R2 | 완료 | 최대 200개 노드 수집과 순수 decide 분리. 지정 1~8 순서, 체크형 컨트롤, 편집 노드 제외, 루틴·날짜/시간, 삭제 허용 범위 구현 | SettingsGuard.java, SettingsNodeCollector.java, DeviceConstants.java, FocusService.java, MainActivity.java, strings.xml |
| R3 | 완료 | UUID lock_id, 교체 설치 제외 패키지 브로드캐스트 기록, StringSet 복사·잠금 범위 초기화, 설치 시각 OR 기록, QUERY_ALL_PACKAGES와 안내 | FocusLock.java, NewPackageLedger.java, AppCatalog.java, FocusService.java, AndroidManifest.xml, MainActivity.java, strings.xml |
| R4 | 완료 | 세 Secure 키를 각각 예외 처리. 등록 발견은 시작 거부, 읽기 실패는 경고 후 시작 가능. 세션 내 한 번 경고하고 ‘다시 보지 않기’ 저장 지원. 점검 결과 저장·진단 | ShortcutCheck.java, DeviceConstants.java, MainActivity.java, FocusService.java |
| R5 | 완료 | 5분 허용 중 강제 중지·삭제·데이터 삭제를 하면 차단도 꺼진다는 안내로 정정 | MainActivity.java, strings.xml |
| R6 | 완료 | 같은 부팅은 elapsedRealtime, 재부팅은 저장/현재 네트워크 시각이 모두 있을 때 네트워크 우선, 없으면 기존 벽시계. lock·delay 모두 적용 | FocusLock.java, TimerRules.java, NetworkTime.java |
| R7 | 일부: 코드 완료, 프로필 노출 검증 대기 | 기본 켬·잠금 중 변경 불가 옵션, 조회 실패 캐시와 패키지 변경 무효화, 보호 패키지 제외. 보안 폴더의 창·이벤트가 실제 보이는지는 미검증 | MainActivity.java, AppCatalog.java, FocusService.java, BlockPolicy.java, strings.xml |
| R8 | 완료 | 지정 5종 패키지 공통 집합, 해당 패키지의 플레이어 ID 조회, 잠금 전체 제한·평소 플레이어·설정 가드에 적용, 10초 토스트 제한 | DeviceConstants.java, YouTubeInspector.java, FocusService.java, AppCatalog.java, SettingsGuard.java, MainActivity.java |
| R9 | 완료 | 창 결정·PiP 결과, 글로벌 요청 결과, 마지막 설정 이유, 현재 lock_id 새 앱 수, 네트워크 시각, 바로가기 점검 결과 표시 | FocusService.java, NetworkTime.java, ShortcutCheck.java |

OEM 값을 한 곳에 모으기 위해 BrowserAddress.java, Prefs.java, RecentsInspector.java의 기존 후보 값도 DeviceConstants.java를 참조하도록 변경했다. 관련 없는 통계·XP·내장 웹 가드는 변경하지 않았다. 새 prefs 변경과 기존 잠금/삭제 대기의 쓰기는 apply()를 사용하며, DB 쓰기는 기존 기록 스레드에 남겼다.

## 3. 실기기 확인 필요 상수와 덤프

모든 후보는 `BeanFocus/src/app/bean/focus/DeviceConstants.java`에 모았고 **실기기 확인 필요** 주석을 달았다. 새로 검증된 실기기 상수는 아직 없다.

| 그룹 | 값/후보 | 확인 방법 |
|---|---|---|
| 삼성 설정 | com.android.settings, com.samsung.android.settings, com.samsung.accessibility | 해당 메뉴에서 ID 수집, 진단의 패키지·설정 이유 확인 |
| 디바이스 케어·루틴 | com.samsung.android.lool, com.samsung.android.sm, com.samsung.android.app.routines | 깊은 절전 앱 추가/루틴 앱 선택에서 수집 |
| 설치기 | com.android.packageinstaller, com.google.android.packageinstaller, com.samsung.android.packageinstaller 및 `.packageinstaller` 접미사 | 홈 아이콘 삭제 확인 대화상자에서 수집 |
| 보호 패키지 | com.android.dialer, com.android.incallui, com.samsung.android.incallui, com.android.phone, com.android.server.telecom, com.samsung.android.dialer, com.google.android.dialer, com.android.systemui 및 `emergency` 포함 이름 | 전화 수신·통화·긴급 UI가 가려지거나 자동 이동하지 않는지 확인 |
| 최근 앱 호스트 | com.sec.android.app.launcher, com.android.launcher3, com.google.android.apps.nexuslauncher, com.android.systemui | 최근 앱 정리 버튼을 누른 뒤 진단·가림막 재진입 동작 확인 |
| 최근 앱 ID | recents_view, overview_panel, overview_actions_view, recents_root, recents_container, recents_view_container, recent_apps_root | 실제 호스트의 접두사와 ID를 덤프/ADB UI 트리로 확인 |
| 최근 앱 클래스 | com.android.quickstep.views.RecentsView | 클래스 후보는 자동 테스트로만 확인됨. 필요 시 기기 접근성 트리/ADB 확인 |
| 제목 ID | entity_header_title, app_name, app_title, toolbar_title, action_bar_title, collapsing_toolbar_title, large_title, small_title, sub_appbar_title, header_title | 앱 정보·배터리·저장공간·접근성 상세 덤프와 대조 |
| 체크형 ID | switch_widget, switch_bar, switch_text, switch_main, switch_text_container, switchbar | 스위치/체크박스/바로가기 화면에서 확인. 표준 isCheckable과 Switch/CheckBox/RadioButton 클래스 접미사도 사용 |
| 유튜브 패키지 | com.google.android.youtube, app.revanced.android.youtube, app.rvx.android.youtube, app.morphe.android.youtube, com.vanced.android.youtube | 실제 설치 패키지와 루트 노드 리소스 접두사 확인 |
| 플레이어 ID | reel_recycler, reel_player_page_container, reel_watch_fragment_root | 일반 영상·추천 선반·쇼츠 플레이어 각각 수집 |
| 날짜·시간 문구 | 자동 날짜 및 시간 / Automatic date and time; 날짜 설정 / Set date; 시간 설정 / Set time | 화면에 보이는 정확한 문구와 대조. ‘일반 → 날짜 및 시간’ 메뉴 한 줄은 차단 안 함 |
| 강한 동작 문구 | 강제 중지, 강제 종료, force stop, 삭제, 제거, uninstall, 데이터 삭제, 모든 데이터 삭제, clear data, clear storage | 한국어/영어 앱 정보 버튼 문구 확인 |
| 넓은 동작 추가 | 사용 중지, 사용 안 함, 사용, 사용 설정, enable, disable | YouTube 사용 중지/사용 설정 화면 확인 |
| 바로가기 키 | accessibility_shortcut_target_service, accessibility_button_targets, accessibility_qs_targets | 등록 전후 잠금 시작을 시도해 거부·경고·진단 확인 |
| PiP 실패 안내 | PiP를 닫지 못했어요. 알림의 미디어 컨트롤로 정지하세요. | dismiss 미지원 기기에서 3회 실패/60초 중지와 영역별 문구 확인 |

ADDRESS_FIELDS·BROWSERS·fieldsFor의 삼성 인터넷/기존 브라우저 후보와 webHost의 인앱 호스트도 같은 파일에 모았다. 삼성 주소 후보는 `location_bar_edit_text`, `location_bar_text_view`, `custom_tab_toolbar_url_bar_text`, `url_bar`이며 기존 Chromium·Firefox·DuckDuckGo 후보를 유지했다.

**받아야 할 화면 ID 덤프:** 접근성 서비스 상세, 집중콩 앱 정보, 앱 정보의 배터리, 앱 정보의 저장공간, 날짜 및 시간, 디바이스 케어의 깊은 절전 앱 추가. 바로가기 지정 화면과 루틴의 집중콩 선택도 추가 검증 대상이다. 앱에서 ‘화면 view ID 수집 예약 · 30초’를 누르고 대상 화면에 갔다가 ‘수집된 view ID 보기’로 확인한다. 모든 surface 패키지는 수집 대상이다. 텍스트·URL은 덤프에 저장하지 않으므로 날짜/시간·버튼 문구는 보이는 정확한 메뉴 문구를 별도로 확인해야 한다. 덤프를 얻어야 하는 메뉴가 가드로 닫히면 집중 잠금이 꺼진 상태에서 수집한다.

## 4. 테스트와 자체 점검

| 단계 | 빌드 | 테스트 스위트 | 숫자로 집계된 검증 수 | 실패 |
|---|---|---:|---:|---:|
| P0 종료 | 성공 | 17 | 1,152,318 | 0 |
| P1 종료 | 성공 | 19 | 1,152,348 | 0 |
| P2 최종 | 성공 | 20 | 1,152,380 | 0 |

숫자 검증 수에는 1,152,000개 XP 수치 그리드가 포함된다. 개수를 출력하지 않는 주소/텔레메트리/JS 테스트의 개별 어설션은 위 합계에 더하지 않았다. 따라서 위 수는 전체 어설션의 하한이다. 20개 스위트는 Java main 기반 단위·SDK 대역 테스트 17개와 기존 JS 회귀 3개다. JUnit·AndroidX·외부 앱 의존성을 추가하지 않았다.

추가 테스트는 창 결정 64조합·글로벌 보호 패키지·PiP 간격/실패/중지(83개), 설정 규칙 22개 및 변형 앱 제목 5개(27개), 설치 기록/시계 역전/바로가기 읽기(8개), 네트워크 마감/실제 FocusLock·delay 복원/프로필 정책(17개), 유튜브 계열·노드 수집·개인정보 제외(49개)를 포함한다. 기존 RecentClose·RemovalAccess·confirmedHome·웹 쇼츠·채널·통계·XP 회귀도 통과했다.

`tests/audit_010.py` 소스 자체 점검도 통과했다:

1. performGlobalAction 직접 호출은 사용자 버튼용 userGlobalAction과 자동용 globalActionFor 내부 두 곳뿐이다. GameShield 직접 호출과 action(1)/action(2)는 없다.
2. checkBlockedWindows의 blockApp 호출은 순회가 끝난 뒤 activeBlockedPkg가 있는 경우 한 곳뿐이다. PiP는 SHIELD_DISMISS_PIP 경로이며 헬퍼도 PiP 자동 HOME/BACK을 거부한다.
3. 최근 앱 가림막 유예는 YouTube/PiP를 제외하고, 창·앱·최대 8초·쿨다운 조건을 유지한다.
4. 설정 가드는 잠금 활성·guard_locked_settings·surface 조건을 모두 통과해야 동작한다.
5. blockedApp의 집중콩/emergency/surface 제외, 가림막의 보호 영역 제외와 글로벌 동작의 실제 활성 창 검사를 확인했다.
6. 최종 APK v2/v3 서명, zipalign, minSdk/targetSdk/versionCode/권한을 확인했다.

빌드 오류 0개. SDK 호환용 deprecated API 등 경고 59개는 남아 있다. 실제 Android Binder·One UI 화면 전환·PiP 닫기 성능·통화 수신은 이 테스트가 재현하지 않으므로 실기기 결과로 채우지 않았다.

## 5. APK와 소스

- APK: `/workspace/scratch/cf405d63da12/BeanFocus-0.10-beta.apk`
- 소스: `/workspace/scratch/cf405d63da12/BeanFocus-source-0.10.zip`
- 0.9 백업: `/workspace/scratch/cf405d63da12/BeanFocus-backup-0.9.zip`
- versionCode: **10**, versionName: **0.10-beta**
- minSdk: **26**, targetSdk: **35**
- 서명 인증서 SHA-256: `5E:73:25:B0:FB:B0:AF:5D:F2:C8:0C:30:0F:0A:C8:D7:E6:E1:CC:D1:D2:3B:AE:0F:8B:6E:2B:AE:C4:B7:48:F4`

기존 키스토어와 동일한 인증서임을 실제 apksigner 결과로 확인한다. 새 키 생성은 하지 않는다. 기존 앱을 삭제하지 말고 업데이트 설치한다. 접근성 재연결은 집중 잠금이 꺼진 상태에서 진행한다. 최종 소스 ZIP에 단계별 테스트/빌드 로그, 자체 점검, 보고서를 포함한다.

## 6. 갤럭시 실기기 체크리스트

결과 칸은 의도적으로 비워 두었다.

| # | 시나리오 | 기대 결과 | 결과 |
|---|---|---|---|
| 1 | 유튜브 영상 재생 중 잠금 시작(PiP 되는 계정) | PiP가 닫힘. 안 닫혀도 홈 반복 없음, 집중콩·설정 정상 | |
| 2 | 잠금 중 카톡으로 받은 유튜브 링크 열기 | 유튜브가 홈으로, PiP가 생기면 닫힘, 안내 토스트 | |
| 3 | 잠금 중 팝업 게임을 띄운 채 설정·집중콩 사용 | 팝업만 가려짐, 홈으로 안 튕김 | |
| 4 | 팝업 게임 직접 터치 | 즉시 홈 + 가림막 | |
| 5 | 차단 팝업이 떠 있을 때 전화 수신 | 통화 화면 정상 | |
| 6 | 분할 화면(게임 + 크롬) | 게임 쪽만 가려짐, 크롬 사용 가능, 게임 터치 시 홈 | |
| 7 | 접근성 → 설치된 앱 → 집중콩 | 뒤로 튕김 | |
| 8 | 앱 정보(배터리·저장공간 포함)에서 강제 중지·삭제·제한 설정, 홈 화면 아이콘 삭제 | 막힘, 삭제 대기 후 5분은 허용 | |
| 9 | 디바이스 케어 → 깊은 절전 앱에 집중콩 추가 | 막힘 | |
| 10 | 날짜 및 시간 → 자동 끄기 | 막힘. ‘일반’ 화면은 열림 | |
| 11 | 시계를 하루 전으로 돌리고 게임 설치(새 앱 차단 켬) | 차단 | |
| 12 | 접근성 바로가기에 집중콩 등록 후 잠금 시작 | 시작 거부 + 안내 | |
| 13 | 보안 폴더에만 설치한 게임 실행 | 차단(안 되면 한계로 기록) | |
| 14 | 잠금 중 재부팅 뒤 날짜 앞당기기 시도 | 날짜 화면 막힘, 잠금 유지 | |
| 15 | 회귀: 잠금 밖 유튜브 쇼츠 탭, 브라우저 쇼츠, 유튜브 웹 홈, 내장 웹 화면 | 쇼츠만 막힘, 나머지 정상 | |
| 16 | 알림 앱 목록(집중콩·유튜브가 한 줄로 보임) | 열림 | |

## 7. 남은 한계와 확인 요청

실제 갤럭시에서 PiP ACTION_DISMISS가 동작하는지, 보안 폴더 이벤트/창을 서비스가 볼 수 있는지, 후보 ID·문구가 맞는지는 확인하지 못했다. 보안 폴더의 패키지가 기본 프로필에도 설치돼 있으면 조회 성공만으로 다른 프로필임을 구분할 수 없다. QUERY_ALL_PACKAGES는 다른 프로필 창 노출이나 프로필 식별 권한 자체를 주는 기능이 아니다.

접근성 바로가기 설정을 읽을 수 없는 기기는 경고 후 시작할 수 있다. 재부팅 직후 시스템 네트워크 시각이 없으면 벽시계로 대체하며, 접근성 서비스가 재연결되기 전에는 보호 공백이 남는다. 앱이 강제 중지되거나 삭제되면 보호는 끝난다. ‘우회가 전혀 없음’이나 ‘모든 One UI에서 1초 내 PiP 닫힘’을 검증한 결과로 표현하지 않는다.

구현을 마치는 데 필요한 추가 선택 질문은 없다. 실기기 검증용으로는 3장의 여섯 종류 화면 ID 덤프와 6장의 실제 결과가 필요하다. 특히 먼저 1·3·5번으로 HOME 반복과 통화 화면 방해가 없는지 확인하고, 그다음 설정 가드·삭제 대기를 확인한다.

공식 API 참고: [SystemClock.currentNetworkTimeClock](https://developer.android.com/reference/android/os/SystemClock#currentNetworkTimeClock()) · [AccessibilityNodeInfo.ACTION_DISMISS](https://developer.android.com/reference/android/view/accessibility/AccessibilityNodeInfo#ACTION_DISMISS).
