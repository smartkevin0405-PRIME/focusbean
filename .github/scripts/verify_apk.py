"""Check a signed release against its metadata, source revision and update identity."""
import argparse
import hashlib
import json
import re
import subprocess
import xml.etree.ElementTree as ET
from pathlib import Path

PACKAGE = "app.bean.focus"
CERTIFICATE = "5e7325b0fbb0af5df2c80c300f0ac8d7e6e1ccd1d23bae0f8b6e2baec4b748f4"
ANDROID = "{http://schemas.android.com/apk/res/android}"


def run(*command, cwd=None):
    return subprocess.run(command, check=True, capture_output=True, text=True,
                          encoding="utf-8", errors="replace", cwd=cwd).stdout


def require(condition, message):
    if not condition:
        raise ValueError(message)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--apk", required=True, type=Path)
    parser.add_argument("--metadata", required=True, type=Path)
    parser.add_argument("--build-tools", required=True, type=Path)
    parser.add_argument("--java", default="java")
    args = parser.parse_args()
    meta = json.loads(args.metadata.read_text(encoding="utf-8-sig"))
    require(meta.get("package") == PACKAGE, "Unexpected package in release metadata")
    require(type(meta.get("versionCode")) is int and meta["versionCode"] > 0,
            "Invalid release versionCode")
    require(re.fullmatch(r"0\.[0-9]+-beta", meta.get("versionName", "")), "Invalid versionName")
    require(meta.get("certificateSha256") == CERTIFICATE, "Metadata certificate changed")
    require(args.apk.stat().st_size < 25 * 1024 * 1024, "APK exceeds updater size limit")
    digest = hashlib.sha256(args.apk.read_bytes()).hexdigest()
    require(digest == meta.get("sha256"), "APK SHA-256 mismatch")

    aapt = args.build_tools / ("aapt.exe" if (args.build_tools / "aapt.exe").exists() else "aapt")
    # Older Windows SDK executables cannot reliably decode non-ASCII parent paths.
    # A Unicode-aware working-directory change plus the APK basename avoids that.
    apk = args.apk.resolve()
    badging = run(str(aapt.resolve()), "dump", "badging", apk.name, cwd=apk.parent)
    identity = re.search(r"package: name='([^']+)' versionCode='([0-9]+)' versionName='([^']+)'", badging)
    require(identity is not None, "Missing APK package identity")
    require(identity.groups() == (PACKAGE, str(meta["versionCode"]), meta["versionName"]),
            "APK package or version differs from metadata")
    require("sdkVersion:'26'" in badging and "targetSdkVersion:'35'" in badging,
            "Unexpected APK minSdk or targetSdk")
    signature = run(args.java, "-jar", str(args.build_tools / "lib/apksigner.jar"),
                    "verify", "--min-sdk-version", "26", "--verbose", "--print-certs", str(args.apk))
    signers = re.findall(r"Signer #[0-9]+ certificate SHA-256 digest: ([0-9a-fA-F]{64})", signature)
    require([s.lower() for s in signers] == [CERTIFICATE], "APK signer differs from the installed app")

    source = meta.get("sourceCommit", "")
    require(re.fullmatch(r"[0-9a-f]{40}", source), "Missing or invalid sourceCommit")
    run("git", "merge-base", "--is-ancestor", source, "HEAD")
    app = "android/BeanFocus/"
    run("git", "diff", "--exit-code", source, "HEAD", "--", app + "src", app + "res",
        app + "assets", app + "AndroidManifest.xml", app + "build.sh")
    manifest = ET.fromstring(run("git", "show", source + ":" + app + "AndroidManifest.xml"))
    require(manifest.get("package") == PACKAGE and
            manifest.get(ANDROID + "versionCode") == str(meta["versionCode"]) and
            manifest.get(ANDROID + "versionName") == meta["versionName"],
            "Source manifest differs from the release")
    print("Verified APK:", PACKAGE, meta["versionName"], "code", meta["versionCode"])
    print("SHA-256:", digest)
    print("Certificate SHA-256:", CERTIFICATE)
    print("Declared source revision:", source)
    print("Source ancestry and manifest checked; this is not a bit-for-bit rebuild comparison.")


if __name__ == "__main__":
    main()
