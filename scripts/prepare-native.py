#!/usr/bin/env python3
"""Idempotent preparation of the materialized native tree; never resets sources."""
from pathlib import Path
import hashlib
root = Path(__file__).resolve().parent.parent / 'app/src/main/cpp'
required = {
    'third_party/UxPlay/lib/pairing.c': ['proof_validated', 'if (!verifier)'],
    'third_party/UxPlay/lib/raop.h': ['salon_authenticated', 'salon_handshake'],
    'third_party/UxPlay/lib/raop_handlers.h': ['salon_pin_valid', 'salon_verified'],
    'third_party/UxPlay/lib/http_request.c': ['salon_header_bytes', 'salon_version_started'],
    'android_raop_callbacks.c': ['onAuthenticated', 'onHandshake'],
    'native_bridge.cpp': ['random_pin'],
    'third_party/oboe/src/common/Version.cpp': [],
    'third_party/androidx-graphics-path/pathway.cpp': [],
    'third_party/androidx-graphics-path/CMakeLists.txt': ['common-page-size=16384'],
    'third_party/ffmpeg/configure': [],
    'third_party/libplist/src/plist.c': [],
    'third_party/openssl-cmake/cmake/BuildOpenSSL.cmake': ['../sources/openssl-'],
}
for relative, markers in required.items():
    path = root / relative
    if not path.is_file():
        raise SystemExit(f'Missing materialized native source: {relative}. Extract the full Salon source archive.')
    source = path.read_text()
    if any(marker not in source for marker in markers):
        raise SystemExit(f'Native preparation marker missing: {relative}. Use the corresponding Salon source archive.')
archive = root / 'third_party/sources/openssl-3.4.4.tar.gz'
if hashlib.sha256(archive.read_bytes()).hexdigest() != '7bdf55ac20f2779e99e5eca306f824fad2b37dee5a06cc35ed5a8b85a6060010':
    raise SystemExit('Vendored OpenSSL source checksum mismatch')
print('Prepared native sources verified; no reset, download or Git operation performed.')
