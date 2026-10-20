#!/usr/bin/env python3
"""Exact CP75 yellow-barrel palette migration. No generic ModBlocks selection."""
import argparse,hashlib,json,re,subprocess,tempfile,zipfile
from pathlib import Path
H=Path(__file__).resolve().parent; M=H.parents[2]; PATHS=['src/main/java/com/hbm/world/dungeon/'+c+'.java' for c in ['DesertAtom001','DesertAtom002','DesertAtom003']]
def sha(b):return hashlib.sha256(b).hexdigest()
def selected(r):return r['world'] and r['vanilla'] and r['ints'] and r['statement'] and r['args'][4:]==['0','3']
def main():
 p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--apply',action='store_true');a=p.parse_args()
 with zipfile.ZipFile(H/'baseline.zip') as z:source={p:z.read(p) for p in PATHS}
 with tempfile.TemporaryDirectory() as td:
  t=Path(td);files=[]
  for path,b in source.items():
   f=t/Path(path).name;f.write_bytes(b);files.append(str(f))
  tools=a.java_home/'lib/tools.jar'
  subprocess.run([str(a.java_home/'bin/javac'),'-cp',str(tools),'-d',td,str(H/'PaletteOwner.java')],check=True)
  result=subprocess.run([str(a.java_home/'bin/java'),'-cp',td+':'+str(tools),'PaletteOwner',a.classpath_file.read_text().strip()]+files,text=True,capture_output=True,check=True)
  rows=[]
  for r in map(json.loads,result.stdout.splitlines()):
   if not selected(r):continue
   path=next(p for p in PATHS if Path(p).name==Path(r['file']).name);raw=source[path].decode()
   lo=len(raw.encode('utf-16-le')[:2*r['start']].decode('utf-16-le'));hi=len(raw.encode('utf-16-le')[:2*r['end']].decode('utf-16-le'))+1
   assert raw[hi-1]==';';r.update(file=path,start=lo,end=hi,old=raw[lo:hi],new='com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock('+', '.join([r['receiver']]+r['args'][:4])+');');rows.append(r)
  assert len(rows)==105
  manifest={'baseline':'d3e012b1ee72c0170cb8e4ff89bfabadb25ac553','placements':105,'files':{}}
  for path,before in source.items():
   out=before.decode();rs=[r for r in rows if r['file']==path]
   for r in reversed(rs):out=out[:r['start']]+r['new']+out[r['end']:]
   manifest['files'][path]={'before':sha(before),'after':sha(out.encode()),'placements':len(rs)}
   if a.apply:
    assert (M/path).read_bytes()==before
    (M/path).write_bytes(out.encode())
   else:assert (M/path).read_bytes()==out.encode()
  if a.apply:
   (H/'selection.json').write_text(json.dumps(rows,indent=2)+'\n');(H/'attribution.log').write_text(result.stderr);(H/'manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
  else:assert rows==json.loads((H/'selection.json').read_text());assert manifest==json.loads((H/'manifest.json').read_text())
  print('PASS 105 attributed current mutable Block2 placements; only literal metadata0/flags3, native World and exact declaring owners.')
if __name__=='__main__':main()
