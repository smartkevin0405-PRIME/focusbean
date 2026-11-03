# 집중콩 소스 빌드

이 디렉터리는 0.13-beta 원본에서 이어서 수정한 0.14-beta(versionCode 14) 앱 소스다.
현재 변경 내역과 실기기 검증의 한계는 `REPORT-0.14.md`에 기록한다.
서명키·암호·토큰, APK, SDK, 빌드 산출물, 실행 로그, 기기의 개인 기록은 포함하지 않는다.

서명 설정은 외부 환경변수로만 전달한다. 원본 소스 공개 이후 브라우저 감지와 검사 주기를
수정했으며 Windows 빌드/테스트와 소스 검증 CI를 추가했다. `release/`의 기존 APK는 보존한다.

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
python3 tests/audit_014.py
```

기본 러너는 Java 테스트 20개와 Node 테스트 4개를 실행한다.
`audit_010.py`~`audit_013.py`는 이전 버전 전용 기록이며 현재 검증에는 `audit_014.py`를 사용한다.
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

결과: `BeanFocus-0.14-beta.apk`. 스크립트는 키를 새로 만들거나 업로드하지 않는다.
암호는 apksigner에 환경변수로 전달하며 저장소 파일·명령 인수에 직접 넣지 않는다.

## 파일 출처 확인

`SOURCE-MANIFEST.json`은 최초 공개한 **0.13 원본 스냅샷**의 SHA-256과
공개용 조정 파일 목록을 기록한다. 이후 수정 파일의 현재 해시 목록은 아니며,
0.14 이후 변경 내역은 Git 커밋으로 추적한다. 테스트 로그·개인 데이터는 포함하지 않는다.

## Windows에서 테스트·빌드

`tests/run-tests.ps1`과 `build.ps1`은 PowerShell용 러너다. JDK와 Node.js,
Android SDK를 사용하며 ECJ·Python·Bash를 별도로 설치하지 않아도 된다.
테스트는 `javac --release 8`, Android 앱은 Android의 `android.jar`를
boot classpath로 지정해 Java 8 바이트코드로 컴파일한 뒤 D8로 변환한다.
호스트 JDK의 최신 Java API를 Android API로 잘못 사용하는 일을 방지한다.

```powershell
cd android\BeanFocus
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_JAR = "$env:LOCALAPPDATA\Android\Sdk\platforms\android-35\android.jar"
$env:BUILD_TOOLS = "$env:LOCALAPPDATA\Android\Sdk\build-tools\35.0.0"
.\tests\run-tests.ps1
.\build.ps1
```

각 경로는 `-JdkHome`, `-AndroidJar`, `-BuildTools` 매개변수로도 전달할 수 있다.
현재 Windows 검증에는 설치되어 있던 JDK 25.0.2, API 37.0 `android.jar`,
Build Tools 36.0.0을 사용했다. `minSdkVersion=26`, `targetSdkVersion=35`는
Manifest 값 그대로이며, 다른 도구 버전으로 만든 APK와의 바이트 일치는 보장하지 않는다.
API 37을 쓰려면 위 `ANDROID_JAR` 경로를 `platforms\android-37.0\android.jar`로 바꾼다.

기본 결과는 **서명 없는** `build/windows/aligned.apk`다. 컴파일·DEX 생성·리소스
패키징·ZIP 정렬을 확인한 결과이며 설치나 실기기 차단 검증을 뜻하지 않는다.
이전 결과를 재사용하지 않도록 매 실행의 중간 파일을 별도의 `build/windows/run-*`,
`build/windows-tests/run-*` 폴더에 남긴다. 스크립트는 재귀 삭제를 실행하지 않는다.

동일한 기존 서명키가 저장소 밖에 있고 위 서명 절의 `SIGNING_*` 환경변수 네 개를
모두 설정한 경우에만 `.\build.ps1 -Sign`으로 서명한다. 새 키를 생성하지 않으며
암호를 명령 인수에 직접 넣지 않는다. 결과 이름은 Manifest의 버전을 읽어
`build/windows/BeanFocus-<versionName>.apk`로 정한다. 기본 빌드와 테스트에는 키가 필요 없다.
