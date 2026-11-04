#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
: "${ANDROID_JAR:?Set ANDROID_JAR to Android API 35 android.jar}"
: "${BUILD_TOOLS:?Set BUILD_TOOLS to Android build-tools 35 directory}"
: "${ECJ_JAR:?Set ECJ_JAR to Eclipse ECJ 3.37 jar}"
rm -rf -- build/gen build/classes build/dex
mkdir -p build/gen build/classes build/dex signing
"$BUILD_TOOLS/aapt2" compile --dir res -o build/resources.zip
"$BUILD_TOOLS/aapt2" link -o build/resources.apk -I "$ANDROID_JAR" --manifest AndroidManifest.xml --java build/gen -A assets build/resources.zip
mapfile -t sources < <(find src build/gen -name '*.java' -print)
java -jar "$ECJ_JAR" -encoding UTF-8 -source 1.8 -target 1.8 -classpath "$ANDROID_JAR" -d build/classes "${sources[@]}"
python3 - <<'PY'
import zipfile,pathlib
with zipfile.ZipFile('build/classes.jar','w') as z:
 for f in pathlib.Path('build/classes').rglob('*.class'):z.write(f,f.relative_to('build/classes'))
PY
java -cp "$BUILD_TOOLS/lib/d8.jar" com.android.tools.r8.D8 --release --min-api 26 --lib "$ANDROID_JAR" --output build/dex build/classes.jar
cp build/resources.apk build/unsigned.apk
python3 - <<'PY'
import zipfile
with zipfile.ZipFile('build/unsigned.apk','a') as z:z.write('build/dex/classes.dex','classes.dex')
PY
"$BUILD_TOOLS/zipalign" -f -p 4 build/unsigned.apk build/aligned.apk
if [[ "${UNSIGNED_ONLY:-0}" == "1" ]]; then
 echo "Unsigned, aligned APK: build/aligned.apk (not installable until signed)."
 exit 0
fi
: "${SIGNING_KEYSTORE:?Set SIGNING_KEYSTORE to a private keystore outside the repository}"
: "${SIGNING_STORE_PASSWORD:?Set SIGNING_STORE_PASSWORD outside source control}"
: "${SIGNING_KEY_PASSWORD:?Set SIGNING_KEY_PASSWORD outside source control}"
: "${SIGNING_KEY_ALIAS:?Set SIGNING_KEY_ALIAS to the private key alias}"
export SIGNING_STORE_PASSWORD SIGNING_KEY_PASSWORD
if [[ ! -f "$SIGNING_KEYSTORE" ]]; then
 echo "Existing signing key is required; refusing to generate a replacement key." >&2
 exit 1
fi
java -jar "$BUILD_TOOLS/lib/apksigner.jar" sign --ks "$SIGNING_KEYSTORE" --ks-key-alias "$SIGNING_KEY_ALIAS" --ks-pass env:SIGNING_STORE_PASSWORD --key-pass env:SIGNING_KEY_PASSWORD --out BeanFocus-0.15-beta.apk build/aligned.apk
java -jar "$BUILD_TOOLS/lib/apksigner.jar" verify --verbose BeanFocus-0.15-beta.apk
