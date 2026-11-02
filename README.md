# 집중콩 APK 배포

개인용 Android 집중 관리 앱의 서명된 베타 APK 배포 저장소입니다. 소스 코드·서명 개인키·사용자 기록은 이 저장소에 없습니다.

## 설치

[최신 APK 다운로드](https://github.com/smartkevin0405-PRIME/focusbean/releases/latest/download/BeanFocus-latest.apk)

기존 집중콩을 삭제하지 말고 업데이트 설치하세요. Android 설치 확인과 기기에서 허용되는 설치 출처 설정이 필요합니다. 집중 잠금이 끝난 뒤 설치하세요.

0.12-beta부터 앱의 **앱 업데이트 → 최신 버전 다운로드**에 위 주소가 기본 등록되어 있습니다. 0.11-beta에서는 같은 주소를 한 번 입력하면 사용할 수 있습니다. APK의 앱 ID·서명 인증서·상위 버전을 확인하며, 같은 버전이면 설치하지 않습니다. 설치는 Android의 확인 화면을 거칩니다.

- 패키지: `app.bean.focus`
- 인증서 SHA-256: `5E:73:25:B0:FB:B0:AF:5D:F2:C8:0C:30:0F:0A:C8:D7:E6:E1:CC:D1:D2:3B:AE:0F:8B:6E:2B:AE:C4:B7:48:F4`
- 갤럭시 실기기 동작은 추가 확인이 필요한 베타입니다.

## 게시

`release/BeanFocus.apk.b64`는 이미 서명된 APK의 전송용 인코딩이며 소스가 아닙니다. APK를 먼저 갱신하고, 마지막으로 `release/release.json`의 버전·SHA-256을 갱신하면 GitHub Actions가 체크섬을 검사하고 릴리스 APK와 SHA256SUMS를 게시합니다. 개인키는 게시 작업에 필요하지 않습니다. 같은 파일명 `BeanFocus-latest.apk`를 유지해 앱의 주소가 바뀌지 않게 합니다.
