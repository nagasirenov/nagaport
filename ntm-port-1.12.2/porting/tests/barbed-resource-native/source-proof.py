#!/usr/bin/env python3
import hashlib,json,zipfile,subprocess,re
from pathlib import Path
H=Path(__file__).resolve().parent;R=H.parents[2];m=json.loads((H/'manifest.json').read_text())
with zipfile.ZipFile(H/'sources.zip') as z:
 for f,hashes in m['changes'].items():
  before=z.read('before/'+f);after=z.read('after/'+f);assert (R/f).read_bytes()==after,f
  assert hashlib.sha256(after).hexdigest()==hashes['after'];assert (hashlib.sha256(before).hexdigest() if before else None)==hashes['before']
  if f.endswith('/ModBlocks.java'):
   insertion=b'\t\tblock.setModelTexture(RefStrings.MODID + ":blocks/" + registryName + "_model");\n';assert after.count(insertion)==1;assert after.replace(insertion,b'')==before
  if f.endswith('/BarbedWire.java'):
   inverse=after.replace(b'\tprivate String modelTexture;\n',b'')
   method=b'\tpublic BarbedWire setModelTexture(String texture) {\n\t\tthis.modelTexture = texture;\n\t\treturn this;\n\t}\n\n'
   inverse=inverse.replace(method,b'').replace(b'import com.hbm.lib.ModDamageSource;\n',b'import com.hbm.lib.ModDamageSource;\nimport com.hbm.lib.RefStrings;\n')
   inverse=inverse.replace(b'\t\tblockIcon = register.registerIcon(modelTexture);',b'\t\tString path = getRegistryName().getPath();\n\t\tblockIcon = register.registerIcon(RefStrings.MODID + ":blocks/" + path + "_model");')
   assert inverse==before,'Exact owner inverse'
   tail=b'\t@SideOnly(Side.CLIENT)\n\tpublic IIcon getIcon';assert before[before.index(tail):]==after[after.index(tail):]
  if f.endswith('/BarbedWireObjModelHandler.java'):
   tail=b'\t@SubscribeEvent\n\tpublic void onModelBake';assert before[before.index(tail):]==after[after.index(tail):], 'All baking, geometry, UV, normals, shade and transforms must be identical'
  if '/blockstates/' in f:
   assert before==b'';d=json.loads(after);assert set(d['variants'])=={'inventory'}|{'orientation='+str(i) for i in range(6)};assert d['defaults']['model']=='hbm:barbed_wire'
 for f,want in m['unchanged_assets'].items():
  p=R/f;data=p.read_bytes() if p.exists() else subprocess.check_output(['git','show','HEAD:ntm-port-1.12.2/'+f],cwd=R);assert hashlib.sha256(data).hexdigest()==want,f
owners=[]
for p in (R/'src/main/java').rglob('*.java'):
 s=p.read_text();
 if re.search(r'new\s+BarbedWire\s*\(|extends\s+BarbedWire\b',s):owners.append(str(p.relative_to(R)))
assert owners==['src/main/java/com/hbm/blocks/ModBlocks.java'],owners
factory=(R/'src/main/java/com/hbm/blocks/ModBlocks.java').read_text();names=re.findall(r'createBarbedWire\("([^"]+)"\)',factory);assert sorted(names)==sorted(['barbed_wire'+x for x in ['', '_fire','_poison','_acid','_wither','_ultradeath']])
print('PASS nine exact changed source/resources; one-line factory inverse; complete gameplay and bake/transform tail unchanged; six owned constructor names; 13 original mesh/texture assets unchanged. Historical placement evidence remains baseline-specific.')
