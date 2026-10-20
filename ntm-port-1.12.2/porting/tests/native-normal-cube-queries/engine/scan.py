import struct, zipfile, json, pathlib, io
class Class:
 def __init__(self,b):
  self.b=b; self.p=8; self.cp=[None]; n=self.u2(); i=1
  while i<n:
   t=self.u1()
   if t==1:
    l=self.u2(); self.cp.append(self.take(l).decode('utf8','replace'))
   elif t in (7,8,16,19,20): self.cp.append((t,self.u2()))
   elif t in (3,4): self.cp.append((t,self.take(4)))
   elif t in (5,6): self.cp.append((t,self.take(8))); self.cp.append(None); i+=1
   elif t in (9,10,11,12,18,17): self.cp.append((t,self.u2(),self.u2()))
   elif t==15: self.cp.append((t,self.u1(),self.u2()))
   else: raise Exception(t)
   i+=1
  self.access=self.u2(); self.name=self.cls(self.u2()); self.sup=self.cls(self.u2()); self.itf=[self.cls(self.u2()) for _ in range(self.u2())]; self.fields=self.members(); self.methods=self.members()
 def take(self,n): v=self.b[self.p:self.p+n]; self.p+=n; return v
 def u1(self): return self.take(1)[0]
 def u2(self): return int.from_bytes(self.take(2),'big')
 def u4(self): return int.from_bytes(self.take(4),'big')
 def cls(self,i): return self.cp[self.cp[i][1]] if i else None
 def members(self):
  r=[]
  for _ in range(self.u2()):
   a,n,d=self.u2(),self.u2(),self.u2(); attrs={}
   for _ in range(self.u2()):
    k=self.cp[self.u2()]; attrs[k]=self.take(self.u4()).hex()
   r.append({'access':a,'name':self.cp[n],'desc':self.cp[d],'attrs':attrs})
  return r
import hashlib, subprocess, collections
OUT=pathlib.Path(__file__).resolve().parent
ROOT=OUT.parents[3]
import argparse
parser=argparse.ArgumentParser();parser.add_argument('--record',action='store_true');args=parser.parse_args()
def output(name,data):
 if isinstance(data,str):data=data.encode()
 target=OUT/name
 if args.record:target.write_bytes(data)
 else:assert target.read_bytes()==data,('engine artifact drift',name)
PIN='0675e599e5ee31a60776b560af792372df191790'
CPFILE=pathlib.Path('/workspace/shared/nagaport-toolchain/native-classpath.txt')
JAVA=pathlib.Path('/workspace/shared/nagaport-toolchain/java8/bin')
NTM=pathlib.Path('/workspace/shared/nagaport/mods1710/HBM-NTM-.1.0.27_X5645_H261.jar')
OLD=pathlib.Path('/tmp/nagaport-legacy-client.jar'); FORGE=pathlib.Path('/tmp/nagaport-forge1614-userdev.jar')
cp=CPFILE.read_text().strip()
def sha(p):
 h=hashlib.sha256()
 with open(p,'rb') as f:
  for chunk in iter(lambda:f.read(1024*1024),b''):h.update(chunk)
 return h.hexdigest()
manifest={'repository_pin':PIN,'native_classpath_file_sha256':sha(CPFILE),'jars':[],'directories':[],'absent_classpath_entries':[]}
allc={}; counts=collections.Counter(); duplicates=[]
def add(scope,b,path):
 c=Class(b);k=scope+':'+c.name
 if k in allc:duplicates.append([k,path])
 else:allc[k]={'name':c.name,'super':c.sup,'interfaces':c.itf,'path':str(path),'methods':[{k:v for k,v in m.items() if k!='attrs'} for m in c.methods]}
 counts[scope]+=1
for scope,p in [('legacy_ntm',NTM),('legacy_engine',OLD)]+[('native',pathlib.Path(x)) for x in cp.split(':')]:
 if p.is_file():
  record={'scope':scope,'path':str(p)}
  if p.name in ('compileDummy.jar','providedDummy.jar'):
   with zipfile.ZipFile(p) as dummy:
    assert dummy.namelist()==['dummyThing']
    record['inert_zip_entry_sha256']={'dummyThing':hashlib.sha256(dummy.read('dummyThing')).hexdigest()}
  else:record['sha256']=sha(p)
  manifest['jars'].append(record)
  with zipfile.ZipFile(p) as z:
   for name in z.namelist():
    if name.endswith('.class'):add(scope,z.read(name),str(p)+'!'+name)
 elif p.is_dir():
  files=sorted(p.rglob('*.class')); manifest['directories'].append({'path':str(p),'class_count':len(files),'classes':[{'path':str(f),'sha256':sha(f)} for f in files]})
  for f in files:add(scope,f.read_bytes(),f)
 else:manifest['absent_classpath_entries'].append(str(p))
manifest['jars'].append({'scope':'legacy_forge','path':str(FORGE),'sha256':sha(FORGE)})
with zipfile.ZipFile(FORGE) as z:
 with zipfile.ZipFile(io.BytesIO(z.read('binaries.jar'))) as zz:
  for name in zz.namelist():
   if name.endswith('.class'):add('legacy_forge',zz.read(name),str(FORGE)+'!binaries.jar!'+name)
 with zipfile.ZipFile(io.BytesIO(z.read('forgepatches.zip'))) as zz:
  output('legacy-forge-block.patch', zz.read('net/minecraft/block/Block.java.patch'))
  patchhits=[]
  for name in zz.namelist():
   if name.endswith('.patch'):
    for line in zz.read(name).decode('utf8','replace').splitlines():
     if any(x in line for x in ['isNormalCube','func_149721_r']):patchhits.append(name+': '+line)
  output('legacy-forge-normalcube-patch-hits.txt', '\n'.join(patchhits)+'\n')
def closure(scope,initial):
 targets=set(initial)
 while True:
  nxt={d['name'] for k,d in allc.items() if k.startswith(scope+':') and (d['super'] in targets or any(x in targets for x in d['interfaces']))}
  if nxt<=targets:return sorted(targets)
  targets.update(nxt)
def owners(scope,names,desc=None,within=None):
 return [{'class':d['name'],'super':d['super'],'name':m['name'],'descriptor':m['desc'],'access':m['access']} for k,d in allc.items() if k.startswith(scope+':') and (within is None or d['name'] in within) for m in d['methods'] if m['name'] in names and (desc is None or m['desc']==desc)]
states=closure('native',['net/minecraft/block/state/IBlockState','net/minecraft/block/state/IBlockProperties'])
blocks=closure('native',['net/minecraft/block/Block'])
oldblocks=closure('legacy_engine',['aji'])
report={'counts_including_duplicate_classes':counts,'unique_counts':dict(collections.Counter(k.split(':',1)[0] for k in allc)),'duplicate_classes':duplicates,'original_normal_cube_owners':owners('legacy_ntm',{'isNormalCube','func_149721_r'}),'original_forge_normal_cube_owners':owners('legacy_forge',{'isNormalCube','func_149721_r'}),'original_engine_normal_cube_owners':owners('legacy_engine',{'r'},'()Z',oldblocks),'original_ntm_material_opacity_overrides':owners('legacy_ntm',{'getMaterial','func_149688_o','isOpaque','func_76218_k'}),'original_ntm_material_descendants':closure('legacy_ntm',['net/minecraft/block/material/Material']),'original_ntm_power_owners':owners('legacy_ntm',{'canProvidePower','func_149744_f'}),'original_powerdetector_closure':closure('legacy_ntm',['com/hbm/blocks/machine/PowerDetector']),'native_block_normal_cube_owners':owners('native',{'isNormalCube'},within=blocks),'native_states':states,'native_state_dispatch_owners':owners('native',{'getBlock','isNormalCube','getMaterial','isFullCube','canProvidePower'},within=states),'native_block_secondary_owners':owners('native',{'getMaterial','isFullCube','canProvidePower'},within=blocks),'original_render_normal_owners':owners('legacy_ntm',{'renderAsNormalBlock','func_149686_d'})}
# Exact pinned NTM source, not work-in-progress files.
import re, tarfile
archive=tarfile.open(fileobj=io.BytesIO(subprocess.check_output(['git','archive','--format=tar',PIN,'src/main/java'],cwd=ROOT)))
report['pinned_source_state_type_declarations']=[]
report['pinned_source_normal_cube_methods']=[]
report['pinned_source_power_methods']=[]
source={}
for member in archive.getmembers():
 path=member.name
 if not member.isfile() or not path.endswith('.java'):continue
 text=archive.extractfile(member).read().decode('utf8')
 source[path]=text
 for match in re.finditer(r'\b(?:class|interface|enum)\s+[A-Za-z_$][\w$]*[^\{]+\{',text):
  if re.search(r'\b(?:IBlockState|IBlockProperties|IExtendedBlockState|BlockStateBase|StateImplementation|ExtendedStateImplementation)\b',match.group()):report['pinned_source_state_type_declarations'].append([path,match.group()])
 for name,key in [('isNormalCube','pinned_source_normal_cube_methods'),('canProvidePower','pinned_source_power_methods')]:
  for match in re.finditer(r'\bboolean\s+'+name+r'\s*\([^)]*\)',text):report[key].append([path,match.group()])
report['pinned_original_render_override_source_coverage']=[]
for owner in report['original_render_normal_owners']:
 path='src/main/java/'+owner['class'].split('$')[0]+'.java';text=source.get(path,'')
 names=[m.group() for m in re.finditer(r'\bboolean\s+(?:isFullCube|renderAsNormalBlock)\s*\([^)]*\)',text)]
 report['pinned_original_render_override_source_coverage'].append({'class':owner['class'],'path':path,'methods':names})
output('manifest.json', json.dumps(manifest,indent=2)+'\n')
output('inventory.json', json.dumps(report,indent=2)+'\n')
for fname,classpath,cls in [('native-block.javap',cp,'net.minecraft.block.Block'),('native-state.javap',cp,'net.minecraft.block.state.BlockStateContainer$StateImplementation'),('native-extended-state.javap',cp,'net.minecraftforge.common.property.ExtendedBlockState$ExtendedStateImplementation'),('native-ae-block.javap',cp,'appeng.block.AEBaseBlock'),('native-fence.javap',cp,'net.minecraft.block.BlockFence'),('legacy-block.javap',str(OLD),'aji'),('legacy-powerdetector.javap',str(NTM),'com.hbm.blocks.machine.PowerDetector'),('legacy-material.javap',str(OLD),'awt'),('legacy-materialgas.javap',str(NTM),'com.hbm.blocks.MaterialGas'),('native-material.javap',cp,'net.minecraft.block.material.Material')]:
 output(fname, subprocess.check_output([str(JAVA/'javap'),'-classpath',classpath,'-c','-p',cls]))
assert not report['original_normal_cube_owners']
assert not report['original_forge_normal_cube_owners']
assert not report['pinned_source_state_type_declarations']
assert not report['pinned_source_normal_cube_methods']
assert report['original_powerdetector_closure']==['com/hbm/blocks/machine/PowerDetector']
assert {m['class'] for m in report['native_block_normal_cube_owners']}=={'net/minecraft/block/Block','appeng/block/AEBaseBlock'}
assert {m['class'] for m in report['native_state_dispatch_owners'] if m['name']=='isNormalCube' and not m['access'] & 1024}=={'net/minecraft/block/state/BlockStateContainer$StateImplementation'}
print(json.dumps({k:report[k] for k in ['counts_including_duplicate_classes','original_normal_cube_owners','original_engine_normal_cube_owners','original_powerdetector_closure','native_states','pinned_source_state_type_declarations','pinned_source_normal_cube_methods','pinned_source_power_methods']},indent=2))
