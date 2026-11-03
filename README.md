# 집중콩 (FocusBean)

개인용 Android 집중 관리 앱의 소스 코드와 베타 APK 배포 저장소입니다.

- [앱 소스](android/BeanFocus/)
- [Linux / Windows 빌드와 테스트](android/BeanFocus/BUILDING.md)
- [0.14 소스 변경 및 검증 범위](android/BeanFocus/REPORT-0.14.md)
- [현재 배포 APK](https://github.com/smartkevin0405-PRIME/focusbean/releases/latest/download/BeanFocus-latest.apk)

## 소스와 설치 버전

현재 소스는 **0.14-beta**입니다. 삼성 인터넷의 문서 URL 수집과 브라우저 검사 비용을 개선했습니다.
주소 경로와 웹 노드가 모두 제공되지 않는 화면에서는 여전히 쇼츠를 구분할 수 없습니다.
유튜브 도메인만으로 일반 영상까지 차단하지 않으며 실기기 차단 성공이나 속도 향상을 보장하지 않습니다.

**배포 APK는 아직 0.13-beta입니다.** 0.14는 원래 개인키로 서명하고 실기기에서 확인한 뒤 별도로 게시해야 합니다.
소스 코드 업로드나 검증 CI 성공만으로 최신 APK가 교체되지는 않습니다.
기존 앱은 삭제하지 말고, 집중 잠금이 끝난 뒤 동일 서명의 새 APK로 업데이트하세요.

- 패키지: `app.bean.focus`
- 기존 인증서 SHA-256: `5E:73:25:B0:FB:B0:AF:5D:F2:C8:0C:30:0F:0A:C8:D7:E6:E1:CC:D1:D2:3B:AE:0F:8B:6E:2B:AE:C4:B7:48:F4`
- 개인 서명키, 암호, 토큰 및 기기 사용 기록은 저장소에 포함하지 않습니다.

## 검사와 게시

`validate.yml`은 소스 변경 시 테스트와 서명 없는 APK 빌드를 수행합니다.
`publish.yml`은 `release/release.json` 변경 시 기존에 서명된 `release/BeanFocus.apk.b64`의 체크섬을 확인해 APK를 게시합니다.
새 APK와 메타데이터는 같은 버전으로 갱신해야 합니다. 소스 검증 작업에서는 서명키를 사용하지 않습니다.
