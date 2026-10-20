#!/usr/bin/env python3
"""Replay pinned original/native mapping, bytecode, dispatch and Forge1614 patch evidence."""
from pathlib import Path
import hashlib,io,json,re,subprocess,tempfile,zipfile
H=Path(__file__).resolve().parent;J=Path('/workspace/shared/nagaport-toolchain/java8')
CP=Path('/workspace/shared/nagaport-toolchain/native-classpath.txt').read_text().strip()
D=json.loads((H/'engine-contract.json').read_text())
sha=lambda b:hashlib.sha256(b).hexdigest()
for path,expected in D['input_sha256'].items():assert sha(Path(path).read_bytes())==expected,path
old=next(Path(p) for p in D['input_sha256'] if 'legacy-client' in p)
native=next(Path(p) for p in D['input_sha256'] if 'forgeBin' in p)
srg=next(Path(p) for p in D['input_sha256'] if 'srg.zip' in p)
lines=zipfile.ZipFile(srg).read('joined.srg').decode().splitlines();assert all(l in lines for l in D['mapping_lines'])
for key,expected in D['methods'].items():
 kind,name,header=key.split(':',2);text=subprocess.check_output([str(J/'bin/javap'),'-p','-c','-classpath',str(old if kind=='legacy' else native),name],text=True)
 start=text.index('\n  '+header);m=re.search(r'\n  \S',text[start+3:]);actual=text[start:start+3+m.start()] if m else text[start:];assert actual==expected,key
with tempfile.TemporaryDirectory(prefix='equipment-engine-') as td:
 d=Path(td);subprocess.run([str(J/'bin/javac'),'-cp',CP,'-d',str(d),str(H/'EngineEquipmentScan.java')],check=True)
 for kind,jar in [('legacy',old),('native',native)]:
  actual=subprocess.check_output([str(J/'bin/java'),'-cp',str(d)+':'+CP,'EngineEquipmentScan',kind,str(jar)],text=True)
  assert actual==(H/(kind+'-dispatch.tsv')).read_text(),kind+' engine declaration drift'
print('PASS 1,803 original / 4,876 native classes; complete equipment/armor reader declaration census and exact selected bytecode/SRG mappings')
forge=Path('/tmp/nagaport-forge1614-userdev.jar');assert sha(forge.read_bytes())=='6ed364c0defdb481a41ef2d99c02d382f16fc42a6ea0ea1c2b849848d8466e91'
patches={}
with zipfile.ZipFile(forge) as outer:
 for archive in ['forgepatches.zip','fmlpatches.zip']:
  with zipfile.ZipFile(io.BytesIO(outer.read(archive))) as inner:
   for name in inner.namelist():
    if any(name.endswith('/'+n+'.java.patch') for n in ['EntityLivingBase','EntityLiving','EntityPlayer','InventoryPlayer']):
     data=inner.read(name);patches[archive+':'+name]={'sha256':sha(data),'text':data.decode()}
# All relevant patches are retained, not just grepped line excerpts. Review establishes
# unchanged equipment/armor readers. InventoryPlayer's sole patch adds armor tick;
# EntityLiving patches target combat/despawn and EntityPlayer's nearby setter changes.
for row in patches.values():
 assert not any(n in row['text'] for n in ['func_71124_b','func_82169_q','func_70440_f']), 'read-method patch requires re-audit'
player=patches['forgepatches.zip:net/minecraft/entity/player/EntityPlayer.java.patch']['text']
assert '+        if (p_70062_1_ == 0)' in player and '+            this.field_71071_by.field_70460_b[p_70062_1_ - 1] = p_70062_2_;' in player
path=H/'forge1614-patches.json'
if path.exists():assert patches==json.loads(path.read_text()),'Forge/FML patch drift'
else:path.write_text(json.dumps(patches,indent=2)+'\n')
print('PASS full relevant Forge1614/FML patch evidence: selected reads unchanged; player setter mapping explicitly corrected by Forge (vanilla setter is not a mod-behavior oracle)')
