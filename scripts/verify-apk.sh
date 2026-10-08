#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SDK="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}"
: "${SDK:?Set ANDROID_SDK_ROOT or ANDROID_HOME}"
APK="${1:?Pass the signed universal APK path}"
TOOLS="$SDK/build-tools/35.0.0"
"$TOOLS/apksigner" verify --verbose --print-certs "$APK"
"$TOOLS/zipalign" -c -P 16 -v 4 "$APK"
python3 "$ROOT/scripts/verify_apk.py" "$APK" "$TOOLS/aapt2"
python3 "$ROOT/scripts/verify-jni.py" "$APK"
