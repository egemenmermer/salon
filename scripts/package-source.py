#!/usr/bin/env python3
"""Archive materialized sources without Git, build outputs or local secrets."""
import pathlib,sys,zipfile
root=pathlib.Path(__file__).resolve().parent.parent
out=pathlib.Path(sys.argv[1]).resolve();out.parent.mkdir(parents=True,exist_ok=True)
excluded={'.git','.gradle','.cxx','build','dist','outputs','node_modules','.idea','.salon-signing'}
secrets={'local.properties','credentials.json'}
with zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as z:
 for path in sorted(root.rglob('*')):
  rel=path.relative_to(root)
  if any(p in excluded for p in rel.parts) or path.name in secrets or path.suffix in {'.jks','.p12','.keystore'}:continue
  if not path.is_file() or path.resolve()==out:continue
  info=zipfile.ZipInfo('Salon/'+rel.as_posix(),date_time=(2026,10,8,0,0,0))
  info.external_attr=((0o100755 if path.stat().st_mode&0o111 else 0o100644)<<16)
  info.compress_type=zipfile.ZIP_DEFLATED
  z.writestr(info,path.read_bytes())
print(out)
