#!/usr/bin/env bash
# NirixX — hermetic build pipeline (no Gradle / no network)
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
TOOLS=/home/user/tooling
JAVA=/home/user/venv/lib/python3.11/site-packages/jdk4py/java-runtime/bin/java
KEYTOOL=/home/user/venv/lib/python3.11/site-packages/jdk4py/java-runtime/bin/keytool
AAPT2=$TOOLS/bin/aapt2
ANDROID_JAR=/home/user/android-sdk/platform/android.jar
ECJ=$TOOLS/lib/ecj.jar
DX=$TOOLS/lib/dx.jar
APKSIGNER=$TOOLS/lib/apksigner.jar
KS=$ROOT/keystore/nirixx.jks
KSPASS=android123

APP=$ROOT/app
OUT=$ROOT/out
rm -rf "$OUT"; mkdir -p "$OUT/compiled" "$OUT/gen" "$OUT/classes"

echo "== [1/6] aapt2 compile =="
$AAPT2 compile --dir "$APP/res" -o "$OUT/compiled/res.zip"

echo "== [2/6] aapt2 link =="
$AAPT2 link \
  -o "$OUT/app-unsigned.apk" \
  -I "$ANDROID_JAR" \
  --manifest "$ROOT/manifest/AndroidManifest.xml" \
  --java "$OUT/gen" \
  -A "$APP/assets" \
  --min-sdk-version 24 \
  --target-sdk-version 34 \
  --version-code 5 \
  --version-name "1.3.1" \
  "$OUT/compiled/res.zip"

echo "== [3/6] javac (ecj) =="
find "$OUT/gen" "$APP/src" -name "*.java" > "$OUT/sources.txt"
$JAVA -jar "$ECJ" -1.8 -nowarn \
  -bootclasspath "$ANDROID_JAR" -classpath "$ANDROID_JAR" \
  -d "$OUT/classes" @"$OUT/sources.txt"

echo "== [4/6] dx =="
(cd "$OUT/classes" && find . -name "*.class" -exec $JAVA -jar "$DX" --output="$OUT/classes.dex" {} +)

echo "== [5/6] package dex =="
python3 - "$OUT/app-unsigned.apk" "$OUT/classes.dex" <<'PY'
import zipfile, sys, shutil
apk, dex = sys.argv[1], sys.argv[2]
tmp = apk + ".tmp"
with zipfile.ZipFile(apk) as zin, zipfile.ZipFile(tmp,"w",zipfile.ZIP_DEFLATED) as zout:
    for item in zin.infolist():
        zout.writestr(item, zin.read(item.filename))
    zout.write(dex, "classes.dex")
shutil.move(tmp, apk)
print("dex added")
PY

if [ ! -f "$KS" ]; then
  echo "== generating keystore =="
  mkdir -p "$ROOT/keystore"
  $KEYTOOL -genkeypair -alias nirixx -keyalg RSA -keysize 2048 -validity 10950 \
    -keystore "$KS" -storepass $KSPASS -keypass $KSPASS \
    -dname "CN=NirixX, OU=Engineering, O=NirixX Mobility, L=Chennai, ST=Tamil Nadu, C=IN" >/dev/null 2>&1
fi

echo "== [6/6] sign (v1 + v2 + v3) =="
$JAVA -jar "$APKSIGNER" sign \
  --ks "$KS" --ks-pass pass:$KSPASS --key-pass pass:$KSPASS \
  --min-sdk-version 24 \
  --out "$ROOT/NirixX.apk" \
  "$OUT/app-unsigned.apk" 2>/dev/null

echo
echo "✔ Built: $ROOT/NirixX.apk ($(du -h "$ROOT/NirixX.apk" | cut -f1))"
