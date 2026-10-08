#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LIB="$ROOT/app/src/main/cpp/third_party/UxPlay/lib"
: "${SALON_HOST_OPENSSL:?Set SALON_HOST_OPENSSL to the host OpenSSL prefix}"
TEMP="$(mktemp -d "${TMPDIR:-/tmp}/salon-pairing-test.XXXXXX")"
trap 'rm -rf "$TEMP"' EXIT
FLAGS=(-std=c17 -D_GNU_SOURCE -fsanitize=address,undefined -fno-omit-frame-pointer -I "$LIB" -I "$SALON_HOST_OPENSSL/include")
"${CC:-cc}" "${FLAGS[@]}" -DRAND_bytes=salon_test_RAND_bytes -c "$LIB/crypto.c" -o "$TEMP/crypto.o"
"${CC:-cc}" "${FLAGS[@]}" "$ROOT/tests/native/pairing_fail_closed.c" "$LIB/pairing.c" "$LIB/srp.c" "$LIB/utils.c" "$TEMP/crypto.o" -L "$SALON_HOST_OPENSSL/lib" -Wl,-rpath,"$SALON_HOST_OPENSSL/lib" -lcrypto -o "$TEMP/pairing"
"$TEMP/pairing"
