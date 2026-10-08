#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SDK="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}"
: "${SDK:?Set ANDROID_SDK_ROOT or ANDROID_HOME}"
: "${JAVA_HOME:?Set JAVA_HOME to JDK 17}"
: "${SALON_KEYSTORE:?Set SALON_KEYSTORE to the private release keystore}"
: "${SALON_STORE_PASSWORD:?Set SALON_STORE_PASSWORD}"
: "${SALON_KEY_PASSWORD:?Set SALON_KEY_PASSWORD}"
export SALON_STORE_PASSWORD SALON_KEY_PASSWORD
DEST="${SALON_DIST_DIR:-$ROOT/dist}"
TOOLS="$SDK/build-tools/35.0.0"
mkdir -p "$DEST"
cd "$ROOT"
./gradlew :app:testDebugUnitTest :app:assembleRelease --no-daemon --max-workers=1
UNSIGNED="$ROOT/app/build/outputs/apk/release/app-release-unsigned.apk"
[[ -f "$UNSIGNED" ]] || { echo 'Unsigned release APK missing; remove unrelated local signing configuration.' >&2; exit 1; }
ALIGNED="$(mktemp "${TMPDIR:-/tmp}/salon-aligned.XXXXXX")"
trap 'rm -f "$ALIGNED"' EXIT
"$TOOLS/zipalign" -f -P 16 4 "$UNSIGNED" "$ALIGNED"
"$TOOLS/apksigner" sign --v4-signing-enabled false --ks "$SALON_KEYSTORE" --ks-key-alias "${SALON_KEY_ALIAS:-salon}" \
  --ks-pass env:SALON_STORE_PASSWORD --key-pass env:SALON_KEY_PASSWORD \
  --out "$DEST/Salon-v1.0.0-universal.apk" "$ALIGNED"
"$ROOT/scripts/verify-apk.sh" "$DEST/Salon-v1.0.0-universal.apk"
