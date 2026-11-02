# 집중콩 0.13-beta 원본 소스 빌드

이 디렉터리는 작업 공간의 0.13-beta(versionCode 13) 전체 앱 소스다.
`src/`, `res/`, `assets/`, `design/`, `AndroidManifest.xml`과 테스트 코드는 원본 그대로다.
서명키·암호·토큰, APK, SDK, 빌드 산출물, 실행 로그, 기기의 개인 기록은 포함하지 않는다.

공개용 조정은 `build.sh`의 내장 서명 설정을 외부 환경변수로 변경하고
`UNSIGNED_ONLY=1` 옵션을 추가한 것, README의 서명 안내를 정정한 것뿐이다.
앱 기능·버전·리소스·테스트에는 변경이 없다. 이 커밋은 APK를 게시하지 않는다.
기존 저장소 루트의 README, `release/`, `.github/workflows/`는 그대로 보존한다.

## 도구

Linux x86-64와 Bash, OpenJDK 17, Python 3, Node.js가 필요하다.
Gradle이나 외부 Java 라이브러리는 사용하지 않는다.

- Android SDK Platform API 35 (`android.jar`)
- Android SDK Build Tools 35.0.0 (`aapt2`, `zipalign`, `lib/d8.jar`, `lib/apksigner.jar`)
- Eclipse ECJ 3.37.0 (`ecj-3.37.0.jar`)
- Node.js 24.22.1로 기본 JavaScript 테스트를 검증했다.

Android SDK 도구는 Android SDK Manager에서 설치한다.
ECJ는 Eclipse JDT의 ECJ 3.37.0을 별도로 준비한다.
이 프로젝트에는 Gradle wrapper가 없다.

```bash
sdkmanager 'platforms;android-35' 'build-tools;35.0.0'
cd android/BeanFocus
export ANDROID_JAR="$ANDROID_SDK_ROOT/platforms/android-35/android.jar"
export BUILD_TOOLS="$ANDROID_SDK_ROOT/build-tools/35.0.0"
export ECJ_JAR=/absolute/path/to/ecj-3.37.0.jar
```

원본 검증에 사용한 도구의 SHA-256(도구를 저장소에 포함하지 않음):

| 도구 | SHA-256 |
|---|---|
| ECJ 3.37.0 | `cde026ff966b48b5e5f148b6f041ceff3cf4f85cf75155f4ec0f40e4ee14b545` |
| API 35 android.jar (android-35-ext15 배포본) | `aec874bb691a604a64b93c907b65c26bcc1311050e09413cf050bc5bd9e68488` |
| Build Tools 35.0.0 aapt2 | `d1096e11aba9c974644369ee3c50d239acac3f3428ffa928e5b9c14dfb7a57de` |
| Build Tools 35.0.0 zipalign | `a205d94a45c8522ca1b571dcbe190df145c818f0a29dfb366e4dd78eef4f56a7` |
| Build Tools 35.0.0 d8.jar | `305622ad00535684534eb8f742cbf5e628a9abc09d8ea4d39d1babb95bf0cee5` |

## 테스트

```bash
bash tests/run-tests.sh
python3 tests/audit_013.py
```

기본 러너는 Java 테스트 19개와 Node 테스트 4개를 실행한다.
`audit_010.py`~`audit_012.py`는 이전 버전 전용 기록이며 0.13 검증에는 `audit_013.py`를 사용한다.
Android 대역 테스트이므로 실제 One UI 접근성·PiP·보안 폴더 동작 검증을 대신하지 않는다.

선택적인 화면 렌더링 검증은 Playwright와 Chromium을 별도로 준비한 뒤 실행한다.
이 스크립트는 명시적인 가상 통계 데이터만 사용한다.

```bash
# 의존성은 저장소 밖에 설치해도 된다.
npm install --no-save --package-lock=false playwright
npx playwright install chromium
node tests/render_stats.cjs
# Chromium 실행 파일을 따로 쓰려면 BEAN_CHROME=/absolute/path/to/chromium 지정
```

## 서명 없는 빌드

```bash
UNSIGNED_ONLY=1 bash build.sh
```

결과는 `build/aligned.apk`다. 서명 전에는 설치할 수 없다.
이는 원본 컴파일·리소스 패키징을 재현하는 절차다.
ZIP 타임스탬프·도구 배포본과 개인 서명키가 달라질 수 있으므로
배포 APK와의 바이트 단위 일치를 보장하지 않는다.

## 외부 개인키로 서명

기존 설치 위에 업데이트하려면 동일한 원래 서명키가 필요하다.
공개 저장소만으로 기존 베타 서명을 재생성할 수 없다.
다른 키로 서명하면 기존 앱 위에 덮어 설치할 수 없다.
개인키는 저장소 외부의 안전한 위치에 둔다.

```bash
export SIGNING_KEYSTORE=/private/path/to/existing-keystore.p12
read -r -p 'Key alias: ' SIGNING_KEY_ALIAS
read -r -s -p 'Keystore password: ' SIGNING_STORE_PASSWORD; printf '\n'
read -r -s -p 'Key password: ' SIGNING_KEY_PASSWORD; printf '\n'
export SIGNING_KEY_ALIAS SIGNING_STORE_PASSWORD SIGNING_KEY_PASSWORD
bash build.sh
unset SIGNING_STORE_PASSWORD SIGNING_KEY_PASSWORD
```

결과: `BeanFocus-0.13-beta.apk`. 스크립트는 키를 새로 만들거나 업로드하지 않는다.
암호는 apksigner에 환경변수로 전달하며 저장소 파일·명령 인수에 직접 넣지 않는다.

## 파일 출처 확인

`SOURCE-MANIFEST.json`은 이 디렉터리에 포함된 파일들의 SHA-256과
원본 대비 공개용 조정 파일 목록을 기록한다(자기 자신은 제외).
테스트 로그·개인 데이터는 포함하지 않는다.
