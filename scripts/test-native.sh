#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LIB="$ROOT/app/src/main/cpp/third_party/UxPlay/lib"
TEMP="$(mktemp -d "${TMPDIR:-/tmp}/salon-native-test.XXXXXX")"
trap 'rm -rf "$TEMP"' EXIT
"${CC:-cc}" -std=c17 -fsanitize=address,undefined -fno-omit-frame-pointer -I "$LIB" -I "$LIB/llhttp" \
  "$ROOT/tests/native/http_request_limits.c" "$LIB/http_request.c" \
  "$LIB/llhttp/api.c" "$LIB/llhttp/http.c" "$LIB/llhttp/llhttp.c" -o "$TEMP/http_request_limits"
"$TEMP/http_request_limits"
