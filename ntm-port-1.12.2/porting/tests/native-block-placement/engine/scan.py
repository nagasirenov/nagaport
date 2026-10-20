#!/usr/bin/env python3
"""Record once, then strictly reproduce genuine placement owner/dependency evidence."""
from pathlib import Path
import argparse,hashlib,json,zipfile,io,subprocess,re
from classfile import Class
H=Path(__file__).resolve().parent;R=H.parents[3]
p=argparse.ArgumentParser();p.add_argument('--record',action='store_true');a=p.parse_args()
NTM=Path('/workspace/shared/nagaport/mods1710/HBM-NTM-.1.0.27_X5645_H261.jar');OLD=Path('/tmp/nagaport-legacy-client.jar');FORGE=Path('/tmp/nagaport-forge1614-userdev.jar');SRG=Path('/tmp/nagaport1710-srg.zip');CPF=Path('/workspace/shared/nagaport-toolchain/native-classpath.txt');JAVA=Path('/workspace/shared/nagaport-toolchain/java8/bin');cp=CPF.read_text().strip()
def digest(raw):return hashlib.sha256(raw).hexdigest()
def output(name,data):
 if not isinstance(data,bytes):data=data.encode()
 if a.record:(H/name).write_bytes(data)
 else:assert (H/name).read_bytes()==data,('engine evidence drift',name)
def classes(archive):
 with zipfile.ZipFile(archive) as z:return {c.name:c for c in (Class(z.read(n)) for n in z.namelist() if n.endswith('.class'))}
old=classes(NTM);vanilla=classes(OLD);native={};pins={};inert={};directories={}
for name in cp.split(':'):
 path=Path(name)
 if name=='/workspace/shared/nagaport/ntm-port-1.12.2/build/classes/java/api':
  expected_api=json.loads((H.parent/'compiled-api-hashes.json').read_text());actual_api={str(q.relative_to(path)):digest(q.read_bytes()) for q in sorted(path.rglob('*.class'))} if path.is_dir() else None
  assert actual_api in (None,expected_api),'generated genuine API class drift'
  for rel,sha in json.loads((H.parent/'api-source-hashes.json').read_text()).items():assert digest((R/rel).read_bytes())==sha
  directories[name]={'optional_checked_source_output':expected_api}
  if actual_api is not None:
   for q in sorted(path.rglob('*.class')):c=Class(q.read_bytes());native.setdefault(c.name,c)
  continue
 if name=='/workspace/shared/nagaport/ntm-port-1.12.2/build/resources/api':
  assert not path.exists() or not any(q.is_file() for q in path.rglob('*')),'API resource drift'
  directories[name]={'optional_empty_directory':True};continue
 if path.is_file():
  if path.name in ['compileDummy.jar','providedDummy.jar']:
   with zipfile.ZipFile(path) as z:assert z.namelist()==['dummyThing'];inert[name]={'dummyThing':digest(z.read('dummyThing'))}
  else:pins[name]=digest(path.read_bytes())
  for key,value in classes(path).items():native.setdefault(key,value)
 elif path.is_dir():
  directories[name]={str(q.relative_to(path)):digest(q.read_bytes()) for q in sorted(path.rglob('*.class'))}
  for q in sorted(path.rglob('*.class')):c=Class(q.read_bytes());native.setdefault(c.name,c)
 else:directories[name]=None
for path in [NTM,OLD,FORGE,SRG,CPF,JAVA/'java',JAVA/'javac',JAVA/'javap',JAVA.parent/'lib/tools.jar']:pins[str(path)]=digest(path.read_bytes())
output('input-pins.json',json.dumps({'files':pins,'inert_zip_entries':inert,'classpath_directories':directories},indent=2)+'\n')
def closure(mapping,roots):
 result=set(roots)
 while True:
  grown={n for n,c in mapping.items() if c.sup in result}
  if grown<=result:return sorted(result)
  result.update(grown)
def owners(mapping,names,within=None):
 return [{'owner':n,'parent':c.sup,'name':m['name'],'descriptor':m['desc'],'code':m['attrs'].get('Code')} for n,c in sorted(mapping.items()) if within is None or n in within for m in c.methods if m['name'] in names]
original=owners(old,{'canPlaceBlockAt','func_149742_c'});assert len(original)==22
selected=json.loads((H.parent/'manifest.json').read_text())['owners'];roots={o['owner'].replace('.','/') for o in selected};assert len(roots)==15
source_rows=[l.split('\t') for l in (H.parent/'source-before.tsv').read_text().splitlines()];sourceparents={r[2].replace('.','/'):r[3].replace('.','/') for r in source_rows if r[0]=='CLASS'}
def sourceclosure(roots):
 result=set(roots)
 while True:
  grown={n for n,parent in sourceparents.items() if parent in result}
  if grown<=result:return sorted(result)
  result.update(grown)
assert closure(old,roots)==sourceclosure(roots) and len(closure(old,roots))==17
assert not any(m['name'] in {'canPlaceBlockAt','func_149742_c'} for m in native['net/minecraft/block/BlockVine'].methods)
assert not any(m['name']=='c' and m['desc']=='(Lahb;III)Z' for m in vanilla['aoh'].methods)
report={'original_owners':original,'selected_owner_descendants':closure(old,roots),'all_original_owner_descendants':closure(old,{o['owner'] for o in original}),'native_placement_owners':owners(native,{'canPlaceBlockAt'},closure(native,{'net/minecraft/block/Block'})),'original_leaves_owners':owners(old,{'isLeaves'}),'original_side_solid_owners':owners(old,{'isSideSolid'}),'original_opaque_owners':owners(old,{'func_149662_c'}),'native_leaves_owners':owners(native,{'isLeaves'},closure(native,{'net/minecraft/block/Block'}))}
output('owners.json',json.dumps(report,indent=2)+'\n')
with zipfile.ZipFile(FORGE) as f:
 with zipfile.ZipFile(io.BytesIO(f.read('forgepatches.zip'))) as z:
  assert not any(x.endswith('/BlockFence.java.patch') for x in z.namelist())
  block=z.read('net/minecraft/block/Block.java.patch').decode();world=z.read('net/minecraft/world/World.java.patch').decode()
  assert 'func_149742_c' not in z.read('net/minecraft/block/BlockVine.java.patch').decode(), 'Forge added a legacy Vine placement override'
  def patch(s,term):
   lines=s.splitlines();i=next(i for i,l in enumerate(lines) if term in l);return '\n'.join(lines[max(0,i-3):i+12])+'\n'
  output('original-base-placement.patch',patch(block,'public boolean func_149742_c'))
  output('original-solid-top.patch',patch(world,'public static boolean func_147466_a'))
  output('original-default-replaceability.patch',patch(block,'boolean isReplaceable('))
  assert '+        return block.isSideSolid(p_147466_0_, p_147466_1_, p_147466_2_, p_147466_3_, ForgeDirection.UP);' in world
  assert '+        return p_149742_1_.func_147439_a(p_149742_2_, p_149742_3_, p_149742_4_).isReplaceable(p_149742_1_, p_149742_2_, p_149742_3_, p_149742_4_);' in block
with zipfile.ZipFile(SRG) as z:
 s=z.read('joined.srg').decode();mapping=[l for l in s.splitlines() if l.startswith(('CL: aoh ','FD: ajn/aJ ','FD: ajn/bk ','MD: akz/a (Laji;)Z'))];assert len(mapping)==4;output('original-mapping.txt','\n'.join(mapping)+'\n')
def javap(cp,cl):return subprocess.check_output([str(JAVA/'javap'),'-classpath',cp,'-c','-p',cl]).decode()
def selected_methods(s,terms):
 # javap method sections run from a two-space declaration to the next declaration.
 declarations=list(re.finditer(r'^  [^ \n].*;\n',s,re.M));parts=[]
 for i,m in enumerate(declarations):
  if any(t in m[0] for t in terms):parts.append(s[m.start():declarations[i+1].start() if i+1<len(declarations) else len(s)].rstrip()+'\n')
 assert parts,terms
 return '\n'.join(parts)
for owner in sorted(roots):output(owner.split('/')[-1]+'.original-placement.javap',selected_methods(javap(str(NTM),owner.replace('/','.')),['func_149742_c(']))
output('native-base-placement.javap',selected_methods(javap(cp,'net.minecraft.block.Block'),['canPlaceBlockAt(','isReplaceable(']))
output('native-world-state-read.javap',selected_methods(javap(cp,'net.minecraft.world.World'),['getBlockState(','isOutsideBuildHeight(']))
output('original-world-block-read.javap',selected_methods(javap(str(OLD),'ahb'),['aji a(int, int, int)']))
output('original-fence-predicate.javap',selected_methods(javap(str(OLD),'akz'),['static boolean a(aji)']))
output('native-vine-signatures.txt',subprocess.check_output([str(JAVA/'javap'),'-classpath',cp,'net.minecraft.block.BlockVine']))
output('original-vine-signatures.txt',subprocess.check_output([str(JAVA/'javap'),'-classpath',str(OLD),'aoh']))
print('PASS original 22 placement owners; exact 15 selected +2 descendants; actual BlockVine inherits Block placement in both engines; native placement/leaves inventory, original Forge base/support hooks and two-identity fence predicate; all inputs pinned')
