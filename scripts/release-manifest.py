#!/usr/bin/env python3
import hashlib,json,pathlib,re,subprocess,sys,zipfile
apk,source,sdk,output=map(pathlib.Path,sys.argv[1:5])
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
tools=sdk/'build-tools/35.0.0'
badging=subprocess.check_output([str(tools/'aapt2'),'dump','badging',str(apk)],text=True)
cert=subprocess.check_output([str(tools/'apksigner'),'verify','--verbose','--print-certs',str(apk)],text=True)
pkg=re.search(r"package: name='([^']+)' versionCode='([^']+)' versionName='([^']+)'",badging)
root=pathlib.Path(__file__).resolve().parent.parent
metadata=json.loads((root/'SOURCE_METADATA.json').read_text())
with zipfile.ZipFile(apk) as z:abis=sorted({n.split('/')[1] for n in z.namelist() if n.startswith('lib/') and n.endswith('.so')})
result={'applicationId':pkg[1],'versionName':pkg[3],'versionCode':int(pkg[2]),
'minSdk':int(re.search(r"(?:minSdkVersion|sdkVersion):'(\d+)'",badging)[1]),'targetSdk':int(re.search(r"targetSdkVersion:'(\d+)'",badging)[1]),
'packagedAbis':abis,'apkFileName':apk.name,'apkSizeBytes':apk.stat().st_size,'apkSha256':digest(apk),
'signingCertificateSha256':re.search(r'Signer #1 certificate SHA-256 digest: ([a-fA-F0-9]+)',cert)[1].lower(),
'sourceArchiveSha256':digest(source),'sourceCommit':metadata['sourceCommit'],'sourceDirty':metadata['sourceDirty'],
'upstreamCommit':metadata['upstreamCommit']}
output.write_text(json.dumps(result,indent=2)+'\n')
checksums=output.parent/'CHECKSUMS.txt'
checksums.write_text(''.join(f'{digest(p)}  {p.name}\n' for p in [apk,source,output]))
print(json.dumps(result,indent=2))
