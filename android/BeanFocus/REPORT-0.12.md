# 집중콩 0.12-beta · 웹 업데이트 연결

2026-11-02 · package `app.bean.focus` · versionCode 12 · minSdk 26 / targetSdk 35

## 결과

공개 APK 배포 저장소 `https://github.com/smartkevin0405-PRIME/focusbean`에 0.12-beta를 게시했다. 앱 업데이트 화면의 기본 주소를 아래 고정 주소로 연결했다.

`https://github.com/smartkevin0405-PRIME/focusbean/releases/latest/download/BeanFocus-latest.apk`

이번 APK를 기존 앱 위에 설치하면 다음 버전부터 **앱 업데이트 → 최신 버전 다운로드 → 확인한 업데이트 설치**로 진행한다. 0.11-beta에서는 같은 주소를 한 번 입력해 이번 0.12-beta를 받을 수도 있다. 집중 잠금이 끝난 뒤 Android 설치 확인을 눌러야 한다. 설정·통계·XP를 유지하려면 기존 앱을 삭제하지 않는다.

## 코드 변경

- `UpdateRules.java`: 공식 고정 주소, 비어 있는 저장 주소를 기본 주소로 복원하는 순수 함수 추가.
- `UpdateActivity.java`: 공식 주소 기본 등록, 다운로드 버튼·안내 문구 수정, 같은 인증서·같은 버전이면 최신 버전 사용 중으로 안내. HTTPS·앱 ID·인증서·상위 버전 확인과 Android 설치 확인 유지.
- `AndroidManifest.xml`, `build.sh`, `MainActivity.java`: 0.12-beta / versionCode 12.
- `UpdateRulesTest.java`: 기본 주소·빈 주소·사용자 주소 경계 4개 추가.
- `tests/audit_012.py`: 현재 버전의 안전 조건 점검. 기존 차단·PiP·전화·잠금 안전 예외는 변경하지 않았다.
- 수정 전 0.11 소스 ZIP 백업 보존.

## 웹 배포

공개 저장소에는 서명 완료 APK의 전송용 base64, 버전·체크섬 정보, 게시 workflow, 설치 설명만 넣었다. Java/JS 소스·서명 개인키·사용자 기록은 올리지 않았다. `release/BeanFocus.apk.b64` 갱신 후 `release/release.json`을 마지막으로 갱신하면 Actions가 APK 해시를 확인하고 초안 릴리스에 APK와 SHA256SUMS를 올린 뒤 최신 릴리스로 공개한다. 개인키를 GitHub에 전달하지 않고 이미 서명된 APK를 게시하는 작업이다.

자동 게시 workflow는 클라우드 앱 빌드와 다르다. 새 코드를 수정·테스트·빌드해서 게시할 때 실행하며, 대화가 끝난 뒤 스스로 새 버전을 만들지 않는다. 앱도 백그라운드에서 자동 다운로드하거나 무인 설치하지 않는다. 앱 버전 이름은 베타이며, GitHub 고정 latest 링크를 사용하기 위해 최신 일반 릴리스로 게시했다.

## 검증

- Android SDK 35 빌드 성공. Java/Android SDK만 사용.
- Java 테스트 실행 단위 18개 + Node 실행 단위 4개, 실패 0. 업데이트 URL·앱 식별 정책 16개 검사, XP 그리드 1,152,000개 조합 포함.
- `tests/audit_012.py` 통과. 자동 HOME/BACK·PiP·보호 UI·잠금 조건 유지.
- 기존 인증서 SHA-256 일치: `5E:73:25:B0:FB:B0:AF:5D:F2:C8:0C:30:0F:0A:C8:D7:E6:E1:CC:D1:D2:3B:AE:0F:8B:6E:2B:AE:C4:B7:48:F4`.
- APK SHA-256: `b370038362da3dbca9ecc021178bf7c8378524cb2672ec8ba13faeb4d0ccd07d`.
- GitHub Actions 실행 `41434893624` 성공, `v0.12-beta` 공개 릴리스의 APK 해시와 로컬 빌드 해시 일치.
- 고정 latest 다운로드 주소로 HTTP 200 응답을 받아 실제 APK 바이트를 내려받았고, 로컬 빌드와 바이트 단위로 일치했다. 다운로드한 APK 인증서도 재검증했다.
- 갤럭시 실기기의 다운로드·설치 출처 권한·설치 확인·업데이트 후 기록 유지 동작은 추가 확인이 필요하다.

## 폰에서 확인

| 시나리오 | 기대 결과 | 결과 |
|---|---|---|
| 최신 APK 링크에서 이번 버전 설치 | 0.12-beta로 업데이트, 기존 기록 유지 | |
| 앱 업데이트 화면 열기 | 공식 고정 주소가 기본 표시됨 | |
| 최신 버전 다운로드 다시 누르기 | 같은 버전이면 최신 버전 사용 중 안내 | |
| 다음 상위 버전 게시 후 다운로드 | 기존 인증서·앱 ID 확인 후 설치 준비 | |
| 집중 잠금 중 설치 누르기 | 잠금 종료/조기 해제 후 설치 안내 | |

브라우저 일반 유튜브 허용·분 단위 입력·통계 그래프는 REPORT-0.11.md, 차단 안전 조건과 실기기 체크리스트는 REPORT-0.10.md를 따른다.
