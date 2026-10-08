#!/usr/bin/env python3
"""Inspect APK DEX definitions and exported ELF symbols after R8."""
import pathlib,re,struct,sys,zipfile
root=pathlib.Path(__file__).resolve().parent.parent
def uleb(data,pos):
 value=0;shift=0
 while True:
  b=data[pos];pos+=1;value|=(b&127)<<shift
  if b<128:return value,pos
  shift+=7

def methods(data):
 def u32(p):return struct.unpack_from('<I',data,p)[0]
 ns,so,nt,to,np,po,nm,mo,nc,co=[u32(p) for p in [56,60,64,68,72,76,88,92,96,100]]
 strings=[]
 for i in range(ns):
  _,p=uleb(data,u32(so+4*i));strings.append(data[p:data.index(b'\0',p)].decode('utf-8',errors='replace'))
 types=[strings[u32(to+4*i)] for i in range(nt)]
 protos=[]
 for i in range(np):
  p=po+12*i;params=u32(p+8)
  args=[] if not params else [types[struct.unpack_from('<H',data,params+4+2*j)[0]] for j in range(u32(params))]
  protos.append('('+''.join(args)+')'+types[u32(p+4)])
 ids=[]
 for i in range(nm):
  cls,proto,name=struct.unpack_from('<HHI',data,mo+8*i);ids.append((types[cls],strings[name],protos[proto]))
 declared={}
 for i in range(nc):
  p=u32(co+32*i+24)
  if not p:continue
  counts=[]
  for j in range(4):n,p=uleb(data,p);counts.append(n)
  for j in range(counts[0]+counts[1]):_,p=uleb(data,p);_,p=uleb(data,p)
  for count in counts[2:]:
   idx=0
   for j in range(count):
    diff,p=uleb(data,p);flags,p=uleb(data,p);_,p=uleb(data,p);idx+=diff;declared[ids[idx]]=flags
 return declared

def exports(data):
 wide=data[4]==2;h=struct.unpack_from('<16sHHIQQQIHHHHHH' if wide else '<16sHHIIIIIHHHHHH',data)
 sections=[struct.unpack_from('<IIQQQQIIQQ' if wide else '<IIIIIIIIII',data,h[6]+h[11]*i) for i in range(h[12])]
 result=set()
 for s in sections:
  if s[1]!=11:continue
  st=sections[s[6]];strings=data[st[4]:st[4]+st[5]]
  for p in range(s[4],s[4]+s[5],s[9]):
   sym=struct.unpack_from('<IBBHQQ' if wide else '<IIIBBH',data,p)
   name=sym[0];info=sym[1 if wide else 3];other=sym[2 if wide else 4];index=sym[3 if wide else 5]
   if index and info>>4 and other&3 in (0,3):result.add(strings[name:strings.index(b'\0',name)].decode())
 return result
with zipfile.ZipFile(sys.argv[1]) as z:
 defined={}
 for n in z.namelist():
  if re.fullmatch(r'classes\d*\.dex',n):defined.update(methods(z.read(n)))
 bridge='Lio/github/jqssun/airplay/bridge/NativeBridge;'
 natives=[name for (cls,name,sig),flags in defined.items() if cls==bridge and flags&0x100]
 assert len(natives)>=20,('Missing native definitions after R8',natives)
 callbacks=re.findall(r'GetMethodID\(env, cls, "([^"]+)", "([^"]+)"\)',(root/'app/src/main/cpp/android_raop_callbacks.c').read_text())
 handler='Lio/github/jqssun/airplay/bridge/RaopCallbackHandler;'
 for name,sig in callbacks:assert (handler,name,sig) in defined,('R8 removed callback',name,sig)
 assert ('Lio/github/jqssun/airplay/bridge/LogListener;', 'onLog', '(Ljava/lang/String;)V') in defined
 path_class='Landroidx/graphics/path/PathIteratorPreApi34Impl;'
 path_methods={'createInternalPathIterator':'(Landroid/graphics/Path;IF)J','destroyInternalPathIterator':'(J)V',
 'internalPathIteratorHasNext':'(J)Z','internalPathIteratorNext':'(J[FI)I','internalPathIteratorPeek':'(J)I',
 'internalPathIteratorRawSize':'(J)I','internalPathIteratorSize':'(J)I'}
 for name,sig in path_methods.items():assert defined.get((path_class,name,sig),0)&0x100,('Missing Graphics JNI',name)
 assert defined.get(('Landroidx/graphics/path/ConicConverter;','internalConicToQuadratics','([FI[FFF)I'),0)&0x100
 for abi in ['arm64-v8a','armeabi-v7a']:
  assert 'JNI_OnLoad' in exports(z.read(f'lib/{abi}/libandroidx.graphics.path.so'))
  symbols=exports(z.read(f'lib/{abi}/libairplay_native.so'))
  for name in natives:assert 'Java_io_github_jqssun_airplay_bridge_NativeBridge_'+name in symbols,('Missing JNI export',abi,name)
 print('JNI/R8 PASS:',len(natives),'native definitions and',len(callbacks),'callback signatures; both ARM libraries export the required JNI symbols')
