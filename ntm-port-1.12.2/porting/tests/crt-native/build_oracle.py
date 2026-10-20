#!/usr/bin/env python3
"""Independent pinned pristine CRT callbacks; never reads migrated production code."""
import argparse, hashlib, json, re, zipfile
from pathlib import Path
HERE=Path(__file__).resolve().parent
LEGACY_SHA256='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
SOURCES={'render':'src/main/java/com/hbm/render/block/RenderCRT.java','owner':'src/main/java/com/hbm/blocks/generic/BlockDecoCRT.java','multi':'src/main/java/com/hbm/blocks/BlockMulti.java','transform':'src/main/java/com/hbm/world/gen/nbt/INBTBlockTransformable.java','shader':'src/main/java/com/hbm/render/util/ObjUtil.java','registry':'src/main/java/com/hbm/blocks/ModBlocks.java','client':'src/main/java/com/hbm/main/ClientProxy.java'}
def method(text,name,last=False):
 matches=list(re.finditer(r'public\s+(?:static\s+)?[\w<>]+\s+'+name+r'\([^)]*\)\s*\{',text));assert matches,name
 if not last:assert len(matches)==1,name
 start,end=matches[-1].start(),matches[-1].end();depth=1
 while depth:depth+=(text[end]=='{')-(text[end]=='}');end+=1
 return text[start:end]
def generate(pristine=None,legacy_client=None):
 manifest=json.loads((HERE/'original-sha256.json').read_text())
 if pristine is None:
  with zipfile.ZipFile(HERE/'pristine-crt-sources.zip') as z:data={name:z.read(name) for name in manifest}
 else:data={name:(Path(pristine)/name).read_bytes() for name in manifest}
 for name,want in manifest.items():assert hashlib.sha256(data[name]).hexdigest()==want,'Original CRT input changed: '+name
 if legacy_client:assert hashlib.sha256(Path(legacy_client).read_bytes()).hexdigest()==LEGACY_SHA256,'Unrecognized original client'
 s={key:data[name].decode() for key,name in SOURCES.items()}
 assert 'RenderingRegistry.registerBlockHandler(new RenderCRT());' in s['client']
 line=next(line for line in s['registry'].splitlines() if 'deco_crt = new ' in line)
 for fact in ['new BlockDecoCRT(Material.iron)','.setHardness(5.0F)','.setResistance(10.0F)','.setBlockName("deco_crt")']:assert fact in line,fact
 result=(HERE/'OriginalCRTOracle.java.in').read_text()
 for marker,key,names in [('RENDER','render',['renderInventoryBlock','renderWorldBlock','shouldRender3DInInventory']),('OWNER','owner',['damageDropped','onBlockPlacedBy','getSubCount','transformMeta']),('TRANSFORM','transform',['transformMetaDecoModel'])]:result=result.replace('/* '+marker+' */','\n'.join(method(s[key],n) for n in names))
 for marker,name,start in [('WHOLE','renderWithIcon','if(shadow)'),('PART','renderPartWithIcon','if(shadow || hasColor)')]:
  body=method(s['shader'],name,True);body=body[body.index(start):body.index('for(int i = 0;')];result=result.replace('/* '+marker+' */',body)
 assert not re.search(r'/\* [A-Z]+ \*/',result)
 return result
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--pristine',type=Path);p.add_argument('--legacy-client',type=Path);p.add_argument('--output',type=Path,required=True);a=p.parse_args();a.output.write_text(generate(a.pristine,a.legacy_client));print('PASS independent pinned original CRT oracle generated:',a.output)
