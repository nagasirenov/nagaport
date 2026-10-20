#!/usr/bin/env python3
"""Offline: run.py JAVA_HOME CLASSPATH_FILE LEGACY_CLIENT_JAR USERDEV_JAR SRG_ZIP OUTPUT_DIRECTORY [--attribution-only]"""
from pathlib import Path
import subprocess,sys,json,csv,collections,tempfile,hashlib,zipfile,importlib.util,io
sys.dont_write_bytecode=True
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[3];PROJECT=ROOT/'ntm-port-1.12.2'
java,cpf,legacy,userdev,srg,out=[Path(x).resolve() for x in sys.argv[1:7]];out.mkdir(parents=True,exist_ok=True);cp=cpf.read_text().strip()
spec=importlib.util.spec_from_file_location('boundary',HERE/'verify.py');boundary=importlib.util.module_from_spec(spec);spec.loader.exec_module(boundary);boundary.verify();manifest=boundary.MANIFEST

def run(args):
 p=subprocess.run([str(x) for x in args],stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True)
 if p.returncode:raise AssertionError(p.stdout)
 return p.stdout

def rows(tag,name):return list(csv.DictReader((out/tag/(name+'.tsv')).open(),delimiter='\t'))
classes=out/'compiler';classes.mkdir(exist_ok=True)
print(run([java/'bin/javac','-cp',java/'lib/tools.jar','-d',classes,HERE/'ScheduleAttribution.java']))
changed=run(['git','-C',ROOT,'diff',manifest['baseline'],'--name-only','--','ntm-port-1.12.2/src/main/java']).splitlines();assert set(changed)==set(boundary.FILES)
with tempfile.TemporaryDirectory(prefix='schedule-baseline-') as tmp:
 base=Path(tmp)
 for p in (PROJECT/'src/main/java').rglob('*.java'):
  relative=str(p.relative_to(ROOT));q=base/relative;q.parent.mkdir(parents=True,exist_ok=True)
  if relative in boundary.FILES:q.write_bytes(boundary.reverse_source(relative,p.read_bytes().decode()).encode())
  else:q.symlink_to(p)
 for tag,tree in [('before',base/'ntm-port-1.12.2'),('after',PROJECT)]:
  log=run([java/'bin/java','-Xmx4g','-cp',str(classes)+':'+str(java/'lib/tools.jar'),'ScheduleAttribution',tree,cpf,out/tag]);(out/(tag+'.log')).write_text(log);print(tag,log.strip(),flush=True)
before=rows('before','calls');after=rows('after','calls');expected={}
for f in manifest['files']:
 for c in f['calls']:expected[(f['path'].split('ntm-port-1.12.2/',1)[1],str(c['line']))]=c
assert len(expected)==37
unchanged=[]
for b,a in zip(before,after):
 assert (b['path'],b['line'])==(a['path'],a['line'])
 k=(b['path'],b['line'])
 if k in expected:
  c=expected[k];assert b['source']==c['before'] and a['source']==c['after']
  assert b['receiver_type']==a['receiver_type']=='net.minecraft.world.World'
  assert a['method_owner']=='net.minecraft.world.World' and a['method_kind']=='METHOD'
  assert a['method_signature']=='scheduleUpdate(net.minecraft.util.math.BlockPos,net.minecraft.block.Block,int)'
 else:assert b==a;unchanged.append(a)
assert len(before)==len(after)==len(unchanged)+37

def errs(tag):return collections.Counter((x[0],*x[2:])for x in csv.reader((out/tag/'errors.tsv').open(),delimiter='\t'))
b,a=errs('before'),errs('after');removed=b-a;assert not a-b,list((a-b).items())[:5];assert sum(removed.values())==37
assert all('scheduleBlockUpdate' in x[-1] and x[1]=='compiler.err.cant.apply.symbol' for x in removed)
summary=dict(baseline=manifest['baseline'],changed_files=21,migrated_calls=37,unchanged_native_calls=len(unchanged),before_errors=sum(b.values()),after_errors=sum(a.values()),removed_errors=sum(removed.values()),added_errors=0,attributed_calls=after,removed_diagnostics=[{'path':k[0],'code':k[1],'message':k[2],'count':v}for k,v in removed.items()])
(out/'summary.json').write_text(json.dumps(summary,indent=2)+'\n');print('PASS 37 native World.scheduleUpdate attributions; all previous native calls unchanged; 37 removed diagnostics; zero new diagnostics.',flush=True)
if '--attribution-only' in sys.argv:sys.exit(0)

# Every changed call is compiled and executed verbatim in a Block-derived expression fixture.
projection=(HERE/'NativeScheduleTest.java.in').read_text();methods=[];cases=[];n=0
for f in manifest['files']:
 for c in f['calls']:
  methods.append('void call%d(World world,int x,int y,int z,int iX,int iZ,Dir dir,Random rand){%s;}\n'%(n,c['after']))
  cases.append('case %d: call%d(world,x,y,z,iX,iZ,dir,rand); return;'%(n,n));n+=1
expected=[]
for f in manifest['files']:
 for c in f['calls']:
  a=c['args'];coords='dir' if 'dir.' in a[0] else 'below' if a[1]=='y - 1' else 'other' if a[0]=='iX' else 'same';owner='this' if a[3]=='this' else 'slag' if a[3]=='ModBlocks.slag' else 'live'
  kinds={'this.tickRate(world)':'rate','tickRate(world)':'rate','getDelay(world)':'delay','10 + rand.nextInt(40)':'digamma','this.tickRate(world) + rand.nextInt(10)':'balefire','world.rand.nextInt(4) + 8':'fragile'}
  delay=kinds.get(a[4],a[4]);assert delay in kinds.values() or delay.isdigit();expected.append('\t'.join([coords,owner,delay,Path(f['path']).name]))
assert (HERE/'expected-calls.tsv').read_text()=='\n'.join(expected)+'\n'
projection=projection.replace('@CALLS@',''.join(methods)).replace('@CASES@','\n'.join(cases))
generated=out/'NativeScheduleTest.java';generated.write_text(projection)
log=run([java/'bin/javac','-source','8','-target','8','-cp',cp,'-d',classes,PROJECT/'src/main/java/com/hbm/world/WorldBlockState.java',generated]);(out/'runtime-compile.log').write_text(log)
log=run([java/'bin/java','-cp',str(classes)+':'+cp,'NativeScheduleTest',HERE/'expected-calls.tsv']);(out/'runtime.log').write_text(log);print(log.strip(),flush=True)

# Record only narrowly relevant actual engine bytecode, never binary copies.
srgtext=zipfile.ZipFile(srg).read('joined.srg').decode()
for line in ['MD: mt/a (IIILaji;I)V net/minecraft/world/WorldServer/func_147464_a (IIILnet/minecraft/block/Block;I)V','MD: mt/a (IIILaji;II)V net/minecraft/world/WorldServer/func_147454_a (IIILnet/minecraft/block/Block;II)V','MD: mt/b (IIILaji;II)V net/minecraft/world/WorldServer/func_147446_b (IIILnet/minecraft/block/Block;II)V']:
 assert line in srgtext,line
excerpts=[]
for name,cpath,cls,headers in [
 ('original-world',str(legacy),'ahb',['  public void a(int, int, int, aji, int);','  public boolean b(int, int, int, int, int, int);']),
 ('original-entry',str(legacy),'ahs',['  public boolean equals(','  public int a(ahs);']),
 ('original-worldserver',str(legacy),'mt',['  public void a(int, int, int, aji, int);','  public void a(int, int, int, aji, int, int);','  public void b(int, int, int, aji, int, int);']),
 ('native-worldserver',cp,'net.minecraft.world.WorldServer',['  public void scheduleUpdate(','  public void updateBlockTick(','  public void scheduleBlockUpdate(','  protected boolean isChunkLoaded(','  public boolean tickUpdates(']),
 ('native-world',cp,'net.minecraft.world.World',['  public void scheduleUpdate(','  public boolean isBlockLoaded(','  private boolean isAreaLoaded(']),
 ('native-provider',cp,'net.minecraft.world.gen.ChunkProviderServer',['  public boolean chunkExists(','  public net.minecraft.world.chunk.Chunk getLoadedChunk('])]:
 text=run([java/'bin/javap','-p','-c','-classpath',cpath,cls]);(out/(name+'.javap')).write_text(text);lines=text.splitlines(True)
 for header in headers:
  starts=[i for i,l in enumerate(lines)if l.startswith(header)]
  assert starts,(name,header)
  for start in starts:
   end=start+1
   while end<len(lines)and not(lines[end].startswith('  ')and not lines[end].startswith('    ')and '('in lines[end]):end+=1
   excerpts.append('SOURCE '+name+'\n'+''.join(lines[start:end]))
forge=zipfile.ZipFile(io.BytesIO(zipfile.ZipFile(userdev).read('forgepatches.zip'))).read('net/minecraft/world/WorldServer.java.patch').decode();hunks=forge.split('@@')
relevant=[h for h in hunks if 'public void func_147454_a' in h]
assert len(relevant)==1 and 'byte b0 = 0;' in relevant[0] and '+        //byte b0 = isForced ? 0 : 8;'in relevant[0]
mapping=zipfile.ZipFile(userdev).read('conf/methods.csv').decode()
for line in ['func_147454_a,scheduleBlockUpdateWithPriority,2,','func_147464_a,scheduleBlockUpdate,2,Schedules a tick to a block with a delay (Most commonly the tick rate)']:assert line in mapping
excerpts.append('SOURCE Forge 1614 conf/methods.csv:\n'+'\n'.join(line for line in mapping.splitlines() if line.startswith(('func_147454_a,','func_147464_a,')))+'\n')
excerpts.append('SOURCE Forge 1614 WorldServer patch (scheduling hunk):\n'+relevant[0])
(out/'engine-evidence.txt').write_text('\n'.join(line.rstrip() for line in '\n'.join(excerpts).splitlines())+'\n')
print('INPUT HASHES',json.dumps({p.name:hashlib.sha256(p.read_bytes()).hexdigest()for p in(legacy,userdev,srg)}))
print('PASS bounded scheduling API migration. Native platform behavior accepted; no full-owner/gameplay/lifecycle parity claim.')
