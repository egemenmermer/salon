#!/usr/bin/env python3
"""Validate actual APK bytes, packaged ELF alignments and native dependency closure."""
import hashlib,json,re,struct,subprocess,sys,zipfile
apk,aapt=sys.argv[1:3]
badging=subprocess.check_output([aapt,'dump','badging',apk],text=True)
assert "package: name='app.salon.receiver'" in badging, badging
assert "versionCode='1'" in badging and "versionName='1.0.0'" in badging
assert ("minSdkVersion:'24'" in badging or "sdkVersion:'24'" in badging) and "targetSdkVersion:'36'" in badging
assert 'application-debuggable' not in badging
assert 'android.hardware.touchscreen' not in '\n'.join(x for x in badging.splitlines() if x.startswith('uses-feature:'))
manifest=subprocess.check_output([aapt,'dump','xmltree',apk,'--file','AndroidManifest.xml'],text=True)
assert 'android.intent.category.LEANBACK_LAUNCHER' in manifest
assert 'android.intent.action.MAIN' in manifest
assert 'android:banner' in manifest and 'android:icon' in manifest
for banned in ['SYSTEM_ALERT_WINDOW','RECORD_AUDIO','MEDIA_PROJECTION','ACCESS_FINE_LOCATION','RECEIVE_BOOT_COMPLETED','FOREGROUND_SERVICE','ACCESS_LOCAL_NETWORK']:
 assert banned not in manifest,banned
system={'libc.so','libm.so','libdl.so','liblog.so','libandroid.so','libmediandk.so','libOpenSLES.so','libaaudio.so','libz.so','libEGL.so','libGLESv2.so','libGLESv3.so'}
with zipfile.ZipFile(apk) as z:
 libraries=[n for n in z.namelist() if n.startswith('lib/') and n.endswith('.so')]
 abis=sorted({n.split('/')[1] for n in libraries})
 assert abis==['arm64-v8a','armeabi-v7a'],abis
 for abi in abis:
  assert f'lib/{abi}/libairplay_native.so' in libraries
  names={n.split('/')[-1] for n in libraries if n.split('/')[1]==abi}
  for n in [x for x in libraries if x.split('/')[1]==abi]:
   data=z.read(n); assert data[:4]==b'\x7fELF' and data[5]==1,n
   wide=data[4]==2
   header=struct.unpack_from('<16sHHIQQQIHHHHHH' if wide else '<16sHHIIIIIHHHHHH',data)
   assert wide==(abi=='arm64-v8a'),(n,'wrong ELF class')
   assert header[2]==(183 if wide else 40),(n,'wrong machine',header[2])
   phoff,phsize,phnum=header[5],header[9],header[10]
   loads=[]; dyn=None; relro=[]
   for i in range(phnum):
    ph=struct.unpack_from('<IIQQQQQQ' if wide else '<IIIIIIII',data,phoff+i*phsize)
    if wide:typ,flags,offset,va,pa,filesz,memsz,align=ph
    else:typ,offset,va,pa,filesz,memsz,flags,align=ph
    if typ==1:loads.append((offset,va,filesz,align))
    if typ==2:dyn=(offset,filesz)
    if typ==0x6474e552:relro.append((va,memsz))
   assert loads,n
   for offset,va,filesz,align in loads:
    # Android's 16KB requirement applies to 64-bit processes. NDK's ARM32 libc++ uses 4KB.
    page=16384 if wide else 4096
    assert align>=page,(n,align)
    assert offset%page==va%page,(n,offset,va)
   if wide:
    assert all((va+size)%16384==0 for va,size in relro),(n,'unaligned RELRO',relro)
   needed=[];strva=None
   if dyn:
    off,size=dyn; entry=16 if wide else 8
    for j in range(off,off+size,entry):
     tag,value=struct.unpack_from('<qQ' if wide else '<iI',data,j)
     if tag==0:break
     if tag==1:needed.append(value)
     if tag==5:strva=value
   if needed:
    assert strva is not None
    stroff=next(off+strva-va for off,va,sz,a in loads if va<=strva<va+sz)
    for index in needed:
     begin=stroff+index; dep=data[begin:data.index(b'\0',begin)].decode()
     assert dep in names|system,(n,'missing dependency',dep)
   print('ELF PASS',n,'LOAD alignments',sorted({x[3] for x in loads}))
print('APK PASS',json.dumps({'packagedAbis':abis,'apkSha256':hashlib.sha256(open(apk,'rb').read()).hexdigest()}))
