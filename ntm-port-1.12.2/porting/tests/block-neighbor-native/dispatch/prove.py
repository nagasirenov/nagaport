#!/usr/bin/env python3
"""Read-only javap/CFG assertions, no Minecraft loading or game execution.
All output is restricted to this script's directory. No downloads or installs.
"""
import hashlib,itertools,json,pathlib,re,subprocess,zipfile
ROOT=pathlib.Path(__file__).resolve().parent
CP_FILE=pathlib.Path('/tmp/nagaport-ae2-native-classpath.txt')
JAVA=pathlib.Path('/workspace/shared/nagaport-toolchain/java8/bin/javap')
LEGACY=pathlib.Path('/tmp/nagaport-legacy-client.jar')
SRG=ROOT/'legacy-identities.srg'
CP=CP_FILE.read_text().strip()
assert hashlib.sha256(LEGACY.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
BASE_COMMIT='175b014cbb5c5990107fced236f7131f234998b6' # task provenance only; no repository read
P='net/minecraft/util/math/BlockPos'
W='net/minecraft/world/World'
B='net/minecraft/block/Block'
S='net/minecraft/block/state/IBlockState'
F='net/minecraft/util/EnumFacing'
DIRECTIONS=['west','east','down','up','north','south']
POWER=['down','up','north','south','west','east']
DELTA={'west':(-1,0,0),'east':(1,0,0),'down':(0,-1,0),'up':(0,1,0),'north':(0,0,-1),'south':(0,0,1)}
SIDE=dict(zip(POWER,range(6)))
report=[]
def ok(label): report.append('PASS '+label)
def sha(data): return hashlib.sha256(data).hexdigest()
manifest={'baseline_commit_context':BASE_COMMIT,'inputs':{},'class_bytes':{}}
for path in [CP_FILE,JAVA,LEGACY,SRG]: manifest['inputs'][str(path)]=sha(path.read_bytes())
def owner(clazz,classpath):
 entry=clazz.replace('.','/')+'.class'
 for item in classpath.split(':'):
  p=pathlib.Path(item)
  if p.is_dir() and (p/entry).exists(): return str(p/entry),(p/entry).read_bytes()
  if p.is_file():
   with zipfile.ZipFile(p) as z:
    if entry in z.namelist():
     manifest['inputs'][str(p)]=sha(p.read_bytes())
     return str(p)+'!/'+entry,z.read(entry)
 raise AssertionError('Missing class '+clazz)
def dump(label,clazz,cp=CP):
 source,content=owner(clazz,cp)
 manifest['class_bytes'][clazz]={'source':source,'sha256':sha(content)}
 data=subprocess.check_output([str(JAVA),'-classpath',cp,'-p','-c','-s',clazz],text=True)
 # Full javap is inspected in memory; focused methods are persisted below.
 return data
world=dump('native-World','net.minecraft.world.World')
assert manifest['inputs'][manifest['class_bytes']['net.minecraft.world.World']['source'].split('!/',1)[0]]=='8c75037188f87bc62939eefa03c2480551509034f069728de012c09276f8f49e'
state=dump('native-StateImplementation','net.minecraft.block.state.BlockStateContainer$StateImplementation')
block=dump('native-Block','net.minecraft.block.Block')
iface=dump('native-IBlockState','net.minecraft.block.state.IBlockState')
behavior=dump('native-IBlockBehaviors','net.minecraft.block.state.IBlockBehaviors')
server=dump('native-WorldServer','net.minecraft.world.WorldServer')
client=dump('native-WorldClient','net.minecraft.client.multiplayer.WorldClient')
blockpos=dump('native-BlockPos','net.minecraft.util.math.BlockPos')
facing=dump('native-EnumFacing','net.minecraft.util.EnumFacing')
oldworld=dump('legacy-ahb','ahb',str(LEGACY))
oldblock=dump('legacy-aji','aji',str(LEGACY))
class Method:
 def __init__(self,data,signature):
  pieces=re.split(r'(?=^  (?:public|protected|private) )',data,flags=re.M)
  hits=[x for x in pieces if x.split('\n',1)[0].strip()==signature]
  assert len(hits)==1,(signature,len(hits))
  self.text=hits[0]; self.signature=signature; self.ins=[]
  for line in self.text.splitlines():
   m=re.match(r'\s*(\d+):\s+(\w+)(.*)',line)
   if not m: continue
   offset,op,rest=m.groups(); parts=rest.split('//',1)
   args=parts[0].strip(); comment=parts[1].strip() if len(parts)>1 else ''
   self.ins.append((int(offset),op,args,comment))
  self.index={i[0]:n for n,i in enumerate(self.ins)}
 def at(self,offset): return self.ins[self.index[offset]]
 def has(self,s): assert s in self.text,(self.signature,s)
 def seq(self,offset): return self.ins[self.index[offset]:]
 def opnames(self):return [i[1] for i in self.ins]
 def window(self,offset):
  return [(i[1], i[3] or i[2]) for i in self.seq(offset)]
N=Method(world,'public void neighborChanged(net.minecraft.util.math.BlockPos, net.minecraft.block.Block, net.minecraft.util.math.BlockPos);')
ALL=Method(world,'public void notifyNeighborsOfStateChange(net.minecraft.util.math.BlockPos, net.minecraft.block.Block, boolean);')
EX=Method(world,'public void notifyNeighborsOfStateExcept(net.minecraft.util.math.BlockPos, net.minecraft.block.Block, net.minecraft.util.EnumFacing);')
ST=Method(state,'public void neighborChanged(net.minecraft.world.World, net.minecraft.util.math.BlockPos, net.minecraft.block.Block, net.minecraft.util.math.BlockPos);')
BA=Method(block,'public void neighborChanged(net.minecraft.block.state.IBlockState, net.minecraft.world.World, net.minecraft.util.math.BlockPos, net.minecraft.block.Block, net.minecraft.util.math.BlockPos);')
ON=Method(oldworld,'public void e(int, int, int, aji);')
OA=Method(oldworld,'public void d(int, int, int, aji);')
OE=Method(oldworld,'public void b(int, int, int, aji, int);')
OB=Method(oldblock,'public void a(ahb, int, int, int, aji);')
NP=Method(world,'public boolean isBlockPowered(net.minecraft.util.math.BlockPos);')
OP=Method(oldworld,'public boolean v(int, int, int);')
NR=Method(world,'public int getRedstonePower(net.minecraft.util.math.BlockPos, net.minecraft.util.EnumFacing);')
OR=Method(oldworld,'public int g(int, int, int, int);')
NS=Method(world,'public int getStrongPower(net.minecraft.util.math.BlockPos);')
OS=Method(oldworld,'public int u(int, int, int);')
HOOK=Method(block,'public boolean shouldCheckWeakPower(net.minecraft.block.state.IBlockState, net.minecraft.world.IBlockAccess, net.minecraft.util.math.BlockPos, net.minecraft.util.EnumFacing);')
SELECTED=[N,ALL,EX,ST,BA,ON,OA,OE,OB,NP,OP,NR,OR,NS,OS,HOOK]
(ROOT/'selected-methods.javap').write_text('\n'.join(x.text for x in SELECTED).rstrip()+'\n')
# Check the directional helpers used as symbolic leaves: each calls offset(face, 1).
for direction in DIRECTIONS:
 d0=Method(blockpos,'public net.minecraft.util.math.BlockPos '+direction+'();')
 d1=Method(blockpos,'public net.minecraft.util.math.BlockPos '+direction+'(int);')
 assert d0.opnames()==['aload_0','iconst_1','invokevirtual','areturn']
 d0.has('Method '+direction+':(I)L'+P+';')
 assert d1.opnames()==['aload_0','getstatic','iload_1','invokevirtual','areturn']
 d1.has('Field '+F+'.'+direction.upper()+':L'+F+';')
 d1.has('Method offset:(L'+F+';I)L'+P+';')
# Enum construction explicitly identifies standard 0..5 sides, sign and axis.
init=facing.split('  static {};',1)[1]
for i,direction in enumerate(POWER):
 chunk=init.split('// String '+direction.upper()+'\n',1)[1].split('// Field '+direction.upper()+':',1)[0]
 constants=re.findall(r'\biconst_(m1|[0-5])\b',chunk)
 assert constants[:2]==[str(i),str(i)]
 axis=['Y','Y','Z','Z','X','X'][i]
 sign='NEGATIVE' if i%2==0 else 'POSITIVE'
 assert 'EnumFacing$Axis.'+axis+':' in chunk
 assert 'EnumFacing$AxisDirection.'+sign+':' in chunk
ok('BlockPos unit directional helpers and EnumFacing standard side index/axis/sign definitions')
# Fail closed on changed mappings, exact descriptors, dispatch exception ranges, and guards.
mapping=SRG.read_text()
for entry in ['CL: ahb net/minecraft/world/World','CL: aji net/minecraft/block/Block','CL: s net/minecraft/util/ReportedException','FD: ahb/E net/minecraft/world/World/field_72995_K','MD: ahb/e (IIILaji;)V net/minecraft/world/World/func_147460_e (IIILnet/minecraft/block/Block;)V','MD: aji/a (Lahb;IIILaji;)V net/minecraft/block/Block/func_149695_a (Lnet/minecraft/world/World;IIILnet/minecraft/block/Block;)V','MD: ahb/v (III)Z net/minecraft/world/World/func_72864_z (III)Z','MD: aji/r ()Z net/minecraft/block/Block/func_149721_r ()Z']:
 assert entry in mapping,entry
ok('legacy obfuscated class/field/neighbor/power SRG identities')
assert 'extends net.minecraft.block.state.IBlockBehaviors,' in iface
assert 'neighborChanged(net.minecraft.world.World, net.minecraft.util.math.BlockPos, net.minecraft.block.Block, net.minecraft.util.math.BlockPos)' in behavior
assert 'neighborChanged(' not in server and 'notifyNeighborsOfState' not in server
assert 'neighborChanged(' not in client and 'notifyNeighborsOfState' not in client
ok('IBlockState inherits callback; native WorldServer/WorldClient declare no overrides of these methods')
assert BA.opnames()==['return'] and OB.opnames()==['return']
assert ST.opnames()==['aload_0','getfield','aload_0','aload_1','aload_2','aload_3','aload','invokevirtual','return']
ST.has('Field block:L'+B+';')
ST.has('Method '+B+'.neighborChanged:(L'+S+';L'+W+';L'+P+';L'+B+';L'+P+';)V')
assert ST.at(8)[2]=='4'
assert N.at(4)[1:3]==('ifne','85') and N.at(85)[1]=='return'
assert [(x[1],x[2]) for x in N.ins[:2]]==[('aload_0',''),('getfield','#233')]
N.has('Field isRemote:Z')
assert N.at(9)[3]=='Method getBlockState:(L'+P+';)L'+S+';'
assert [x[1] for x in N.seq(14)[:6]]==['aload','aload_0','aload_1','aload_2','aload_3','invokeinterface']
assert N.at(14)[2]=='4'
N.has('InterfaceMethod '+S+'.neighborChanged:(L'+W+';L'+P+';L'+B+';L'+P+';)V')
assert re.search(r'14\s+25\s+28\s+Class java/lang/Throwable',N.text)
assert re.search(r'17\s+28\s+31\s+Class java/lang/Throwable',ON.text)
for m in [N,ON]:
 for s in ['Exception while updating neighbours','Block being updated','Source block type']:m.has(s)
assert 'new           #311' in N.text and 'class net/minecraft/util/ReportedException' in N.text
assert N.at(84)[1]=='athrow' and ON.at(106)[1]=='athrow'
assert [x[1] for x in N.seq(55)[:5]]==['new','dup','aload_0','aload_2','invokespecial']
assert [x[1] for x in N.seq(67)[:4]]==['aload','aload_1','aload','invokestatic']
assert 'CrashReportCategory.addBlockInfo:' in N.at(72)[3]
assert N.at(69)[1]=='aload_1' and N.at(70)[1:3]==('aload','4')
assert ON.at(1)[3]=='Field E:Z' and ON.at(4)[1:3]==('ifeq','8') and ON.at(7)[1]=='return'
ok('server gate, target-state lookup, exact native/legacy callback argument stack and virtual dispatch')
ok('both dispatchers wrap callback Throwable; lookup is outside try; native target state/pos and changedBlock feed crash report')
for m in [ALL,EX]:
 assert 'isRemote' not in m.text and 'Exception table:' not in m.text
 assert m.text.count('Method neighborChanged:')==6
 assert 'ForgeEventFactory.onNeighborNotify:' in m.text
 assert 'NeighborNotifyEvent.isCanceled:()Z' in m.text
assert ALL.at(20)[1:3]==('ifeq','24') and ALL.at(23)[1]=='return'
assert EX.at(31)[1:3]==('ifeq','35') and EX.at(34)[1]=='return'
assert ALL.at(85)[1:3]==('ifeq','94') and ALL.at(94)[1]=='return'
assert 'updateObservingBlocksAt' not in EX.text
ok('notify wrappers cancel before neighbors; no wrapper remote gate/catch; observer update follows all six only when requested')
# Small deterministic interpreter over actual javap instructions. This is a CFG proof,
# not Minecraft runtime: external methods are explicit symbolic leaves below.
def run(m,locals_,power=None,cancel=False,remote=False,normal=False):
 stack=[]; loc=dict(enumerate(locals_)); trace=[]; pc=0; steps=0
 def popn(n):
  vals=stack[-n:] if n else []
  if n:del stack[-n:]
  return vals
 def invoke(comment):
  target=comment.split(' ',1)[1]
  name=target.split(':',1)[0]
  if name==P+'.'+name.split('.')[-1] and name.split('.')[-1] in DELTA:
   pos=stack.pop();d=DELTA[name.split('.')[-1]];stack.append(tuple(a+b for a,b in zip(pos,d)));return
  if name=='java/util/EnumSet.allOf':stack.pop();stack.append(set(range(6)));return
  if name=='java/util/EnumSet.remove':s,v=popn(2);stack.append(v in s);s.discard(v);return
  if name.endswith('ForgeEventFactory.onNeighborNotify'):
   w,p,s,faces,flag=popn(5);trace.append(('event',w,p,s,tuple(sorted(faces)),flag));stack.append('event');return
  if name.endswith('NeighborNotifyEvent.isCanceled'):assert stack.pop()=='event';stack.append(cancel);return
  if name=='getBlockState':w,p=popn(2);trace.append(('state',w,p));stack.append('targetState');return
  if name=='a' and target.endswith('(III)Laji;'):w,x,y,z=popn(4);trace.append(('block',w,(x,y,z)));stack.append('targetBlock');return
  if name=='neighborChanged':w,p,b,f=popn(4);trace.append(('notify',w,p,b,f));return
  if name=='updateObservingBlocksAt':w,p,b=popn(3);trace.append(('observe',w,p,b));return
  if name==S+'.neighborChanged':s,w,p,b,f=popn(5);trace.append(('stateCallback',s,w,p,b,f));return
  if name==B+'.neighborChanged':b,s,w,p,c,f=popn(6);trace.append(('blockCallback',b,s,w,p,c,f));return
  if name=='aji.a':b,w,x,y,z,c=popn(6);trace.append(('oldCallback',b,w,(x,y,z),c));return
  if name=='e' and target.endswith('(IIILaji;)V'):w,x,y,z,b=popn(5);trace.append(('oldNotify',w,(x,y,z),b));return
  if name in ['getRedstonePower','getStrongPower'] and target.endswith('(L'+P+';L'+F+';)I'):
   w,p,f=popn(3);trace.append(('query',w,p,f));stack.append(power[f]);return
  if name in ['g','e'] and target.endswith('(IIII)I'):
   w,x,y,z,f=popn(5);trace.append(('query',w,(x,y,z),f));stack.append(power[f]);return
  if name=='java/lang/Math.max':a,b=popn(2);stack.append(max(a,b));return
  raise AssertionError(('unmodeled call',name,target))
 while True:
  steps+=1;assert steps<500
  off,op,arg,comment=m.ins[pc];pc+=1
  if re.match(r'[ai]load_[0-3]$',op):stack.append(loc[int(op[-1])])
  elif op in ['aload','iload']:stack.append(loc[int(arg)])
  elif re.match(r'[ai]store_[0-3]$',op):loc[int(op[-1])]=stack.pop()
  elif op in ['astore','istore']:loc[int(arg)]=stack.pop()
  elif op.startswith('iconst_'):stack.append(-1 if op=='iconst_m1' else int(op[-1]))
  elif op=='bipush':stack.append(int(arg))
  elif op in ['ldc','ldc_w']:assert comment=='class '+F;stack.append('EnumFacingClass')
  elif op=='getstatic':
   assert comment.startswith('Field '+F+'.');stack.append(SIDE[comment.split('.',1)[1].split(':')[0].lower()])
  elif op=='getfield':
   value=stack.pop()
   if comment in ['Field isRemote:Z','Field E:Z']:stack.append(remote)
   elif comment=='Field block:L'+B+';':stack.append('targetBlock')
   else:raise AssertionError(comment)
  elif op in ['invokevirtual','invokestatic','invokeinterface']:invoke(comment)
  elif op in ['iadd','isub']:a,b=popn(2);stack.append(a+b if op=='iadd' else a-b)
  elif op=='pop':stack.pop()
  elif op=='goto':pc=m.index[int(arg)]
  elif op in ['ifeq','ifne','ifle']:
   a=stack.pop();take={'ifeq':a==0,'ifne':a!=0,'ifle':a<=0}[op]
   if take:pc=m.index[int(arg)]
  elif op in ['if_acmpeq','if_icmpeq','if_icmplt']:
   a,b=popn(2);take=a<b if op=='if_icmplt' else a==b
   if take:pc=m.index[int(arg)]
  elif op=='return':assert not stack;return None,trace
  elif op=='ireturn':a=stack.pop();assert not stack;return a,trace
  else:raise AssertionError(('unmodeled opcode',op,off,m.signature))
pos=(-7,13,42)
def moved(d):return tuple(a+b for a,b in zip(pos,DELTA[d]))
for remote in [False,True]:
 _,t=run(N,['world',pos,'changedBlock','fromPos'],remote=remote)
 assert t==([] if remote else [('state','world',pos),('stateCallback','targetState','world',pos,'changedBlock','fromPos')])
 _,t=run(ON,['world',*pos,'changedBlock'],remote=remote)
 assert t==([] if remote else [('block','world',pos),('oldCallback','targetBlock','world',pos,'changedBlock')])
_,t=run(ST,['targetState','world',pos,'changedBlock','fromPos'])
assert t==[('blockCallback','targetBlock','targetState','world',pos,'changedBlock','fromPos')]
for canceled,flag in itertools.product([False,True],repeat=2):
 _,t=run(ALL,['world',pos,'changedBlock',flag],cancel=canceled)
 expected=[('state','world',pos),('event','world',pos,'targetState',tuple(range(6)),flag)]
 if not canceled:
  expected += [('notify','world',moved(d),'changedBlock',pos) for d in DIRECTIONS]
  if flag:expected += [('observe','world',pos,'changedBlock')]
 assert t==expected,(canceled,flag,t)
for excluded,canceled in itertools.product(range(6),[False,True]):
 _,t=run(EX,['world',pos,'changedBlock',excluded],cancel=canceled)
 expected=[('state','world',pos),('event','world',pos,'targetState',tuple(v for v in range(6) if v!=excluded),0)]
 if not canceled:expected += [('notify','world',moved(d),'changedBlock',pos) for d in DIRECTIONS if SIDE[d]!=excluded]
 assert t==expected,(excluded,canceled,t)
_,t=run(OA,['world',*pos,'changedBlock'])
assert t==[('oldNotify','world',moved(d),'changedBlock') for d in DIRECTIONS]
for excluded in range(6):
 _,t=run(OE,['world',*pos,'changedBlock',excluded])
 assert t==[('oldNotify','world',moved(d),'changedBlock') for d in DIRECTIONS if SIDE[d]!=excluded]
ok('CFG: all native cancellation/observer/excluded-face cases and old dispatcher cases preserve W/E/D/U/N/S order and arguments')
ok('CFG: both remote states and unchanged native fromPos; state-to-block dispatch passes target state, world, target pos, changed block, fromPos')
for power in itertools.product([-1,0,1],repeat=6):
 a,ta=run(NP,['world',pos],power=power);b,tb=run(OP,['world',*pos],power=power)
 end=next((i+1 for i,v in enumerate(power) if v>0),6)
 expected=[('query','world',moved(d),SIDE[d]) for d in POWER[:end]]
 assert a==b==int(any(v>0 for v in power)) and ta==tb==expected,(power,a,b,ta,tb)
ok('CFG: 729 six-side sign assignments prove boolean query >0, D/U/N/S/W/E order, side argument and short-circuit correspondence')
for power in itertools.product([0,1,15],repeat=6):
 a,ta=run(NS,['world',pos],power=power);b,tb=run(OS,['world',*pos],power=power)
 end=next((i+1 for i,v in enumerate(power) if v>=15),6)
 expected=[('query','world',moved(d),SIDE[d]) for d in POWER[:end]]
 assert a==b==max(power) and ta==tb==expected,(power,a,b,ta,tb)
ok('CFG: 729 six-side strong-power assignments prove max/15 early exit and direction/query correspondence')
# Leaf selection and default hook are bounded structural evidence, not all block semantics.
NR.has('Block.shouldCheckWeakPower:');NR.has('Method getStrongPower:(L'+P+';)I');NR.has('IBlockState.getWeakPower:')
assert NR.at(19)[1:3]==('ifeq','30')
OR.has('Method aji.r:()Z');OR.has('Method u:(III)I');OR.has('Method aji.b:(Lahl;IIII)I')
assert OR.at(10)[1:3]==('ifeq','21')
assert HOOK.opnames()==['aload_1','invokeinterface','ireturn'];HOOK.has('IBlockState.isNormalCube:()Z')
assert 'isBlockIndirectlyGettingPowered(' not in world
assert 'public int getRedstonePowerFromNeighbors(net.minecraft.util.math.BlockPos);' in world
ok('native boolean isBlockPowered vs integer getRedstonePowerFromNeighbors; no indirect-getting-powered name on this classpath')
ok('leaf structure: native Forge hook defaults to normal-cube; legacy normal-cube branch; both choose aggregate strong or direct weak power')
(ROOT/'input-sha256.json').write_text(json.dumps(manifest,indent=2)+'\n')
for line in report:print(line)
print('PASS total '+str(len(report))+' assertion groups')
print('BOUND: static bytecode/CFG proof only. No Minecraft launch, integration test, complete modded-world equivalence, or migrated block-body test.')
print('BOUND: native Forge cancellation/observer behavior and shouldCheckWeakPower override point differ from the supplied vanilla legacy client. Query parity is the boolean/aggregate dispatcher contract, not every block implementation.')
print('BOUND: isRemote guard belongs to World.neighborChanged. Direct state/block callback invocation has no added guard; wrapper events/observer update are not independently remote-gated here.')
print('SOURCES: input-sha256.json, selected-methods.javap, raw native/legacy javap regenerated in memory; baseline commit is provenance, not a repository audit.')
