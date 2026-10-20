#!/usr/bin/env python3
"""Genuine bounded notification proof. Does not invoke Gradle or emit the whole source tree."""
import argparse,subprocess,tempfile,os,struct,zipfile,json,hashlib,io,re
from pathlib import Path
import audit
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,default=Path('/workspace/shared/nagaport-toolchain/java8'));p.add_argument('--classpath-file',type=Path,default=Path('/workspace/shared/nagaport-toolchain/native-classpath.txt'));p.add_argument('--legacy-client',type=Path,default=Path('/tmp/nagaport-legacy-client.jar'));p.add_argument('--forge-userdev',type=Path,default=Path('/tmp/nagaport-forge1614-userdev.jar'));p.add_argument('--attribute',action='store_true');p.add_argument('--mutants',action='store_true');a=p.parse_args()
H=audit.HERE;R=audit.ROOT;m=audit.verify();cp=a.classpath_file.read_text().strip();java=a.java_home/'bin/java';javac=a.java_home/'bin/javac';tools=a.java_home/'lib/tools.jar'
def run(cmd,**kw):return subprocess.run(list(map(str,cmd)),check=True,**kw)
hashes=json.loads((H/'dependency-hashes.json').read_text())
for path in [a.legacy_client,a.forge_userdev]:
 expected=next(d for name,d in hashes.items()if Path(name).name==path.name);assert hashlib.sha256(path.read_bytes()).hexdigest()==expected,path
native=next(Path(s)for s in cp.split(os.pathsep)if Path(s).name.startswith('forgeBin-'))
assert hashlib.sha256(native.read_bytes()).hexdigest()==next(d for name,d in hashes.items()if Path(name).name.startswith('forgeBin-'))
print('PASS: original client, Forge1614 and actual native dependency SHA-256 pins')
def relocate(f,name):
 raw=f.read_bytes();count=struct.unpack('>H',raw[8:10])[0];out=bytearray(raw[:10]);i=1;pos=10
 while i<count:
  tag=raw[pos];out.append(tag);pos+=1
  if tag==1:
   size=struct.unpack('>H',raw[pos:pos+2])[0];pos+=2;s=raw[pos:pos+size].replace(name.encode(),('proof/'+name).encode());pos+=size;out+=struct.pack('>H',len(s))+s
  else:
   size={3:4,4:4,5:8,6:8,7:2,8:2,9:4,10:4,11:4,12:4,15:3,16:2,18:4}[tag];out+=raw[pos:pos+size];pos+=size
   if tag in(5,6):i+=1
  i+=1
 out+=raw[pos:];dest=f.parent/'proof'/f.name;dest.parent.mkdir(exist_ok=True);dest.write_bytes(out);f.unlink()
with zipfile.ZipFile(a.forge_userdev)as z:
 srg=z.read('conf/packaged.srg').decode();assert 'MD: ahb/g (III)V net/minecraft/world/World/func_147471_g (III)V' in srg
 for archive in ['forgepatches.zip','fmlpatches.zip']:
  with zipfile.ZipFile(io.BytesIO(z.read(archive)))as p:
   for suffix in ['world/World.java.patch','client/renderer/RenderGlobal.java.patch','world/WorldManager.java.patch','server/management/PlayerManager.java.patch','world/IWorldAccess.java.patch']:
    key='net/minecraft/'+suffix
    if key in p.namelist():
     data=p.read(key);target=H/(archive+'.'+suffix.replace('/','_'));assert target.read_bytes()==data,target
print('PASS: actual original mapping plus complete Forge1614/FML World/renderer/player-map patch bytes verified')
with tempfile.TemporaryDirectory(prefix='native-notification-')as tmp:
 t=Path(tmp);out=t/'classes';out.mkdir();runtime=str(out)+os.pathsep+cp
 # Exact complete selected contiguous branch statements, not a fabricated implementation.
 branch=[]
 for path in m['files']:
  s=(R/path).read_text();offset=s.index('public boolean onScrew(');match=re.search(r'(?:pipe|tube)\.block = null;\s*com\.hbm\.world\.TileEntityBlockUpdate\.notifyAt\(world, x, y, z\);\s*(?:pipe|tube)\.markDirty\(\);',s[offset:]);assert match,path
  branch.append(re.sub(r'\b(?:pipe|tube)\b','tile',match.group()).replace('tile.block','tile.paint'))
 assert len(set(re.sub(r'\s+',' ',x)for x in branch))==1
 selected=t/'SelectedBranch.java';selected.write_text('import net.minecraft.world.World;class SelectedBranch{static void apply(World world,int x,int y,int z,NotificationTest.TestTile tile){'+branch[0]+'}}')
 state_methods=[]
 for path in m['files']:
  text=(R/path).read_text();methods=[re.search(r'    @Override (?:protected|public) [^\n]+ '+name+r'\([^\n]+',text).group()for name in ['createBlockState','getStateFromMeta','getMetaFromState']];state_methods.append('\n'.join(methods))
 assert len(set(state_methods))==1
 state_owner=t/'SelectedOwnerState.java';state_owner.write_text('import com.hbm.blocks.MultipassContext; abstract class SelectedOwnerState extends net.minecraft.block.BlockContainer { SelectedOwnerState(){super(net.minecraft.block.material.Material.IRON);}'+state_methods[0]+'}')
 helper=R/m['helper'];run([javac,'-encoding','UTF-8','-cp',cp,'-d',out,helper,R/'src/main/java/com/hbm/world/WorldBlockState.java',R/'src/main/java/com/hbm/blocks/MultipassContext.java',H/'NotificationTest.java',selected,state_owner]);run([java,'-cp',runtime,'NotificationTest'],cwd=t)
 name='OriginalNotificationTest';run([javac,'-cp',str(a.legacy_client)+os.pathsep+cp,'-d',out,H/(name+'.java')]);
 for f in out.glob(name+'*.class'):relocate(f,name)
 run([java,'-cp',runtime+os.pathsep+str(a.legacy_client),'proof.'+name],cwd=t)
 run([javac,'-cp',tools,'-d',out,H/'NotificationScanner.java',H/'NativeNotificationScanner.java'])
 inputs=t/'inputs';cpf=t/'cp';cpf.write_text(cp)
 def scan(name,paths,log):
  inputs.write_text('\n'.join(map(str,paths))+'\n');r=run([java,'-Xmx3g','-cp',str(out)+os.pathsep+str(tools),name,inputs,cpf],stdout=subprocess.PIPE,stderr=subprocess.PIPE,universal_newlines=True);(H/log).write_text(r.stdout+r.stderr);return r.stdout
 control=t/'Controls.java';control.write_bytes((H/'controls.java.in').read_bytes());rows=scan('NotificationScanner',[control],'controls.log');assert len(rows.splitlines())==2,rows;print('PASS: two native World/int positives and eight foreign/subtype/boxing/arity/unresolved/effectful-receiver controls')
 if a.attribute:
  paths=sorted((R/'src/main/java').rglob('*.java'));rows=scan('NativeNotificationScanner',paths,'native-attribution.log');actual={(str(Path(l.split('\t')[0]).relative_to(R)),int(l.split('\t')[1]))for l in rows.splitlines()};expected={(f,len((R/f).read_bytes().decode()[:es[0]['start']].encode('utf-16-le'))//2)for f,es in m['files'].items()};assert actual==expected,(actual,expected)
  original=t/'original'
  with zipfile.ZipFile(H/'baseline.zip')as z:
   for f in paths:
    rel=str(f.relative_to(R));target=original/rel;target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(z.read(rel)if rel in m['files']else f.read_bytes())
  rows=scan('NotificationScanner',sorted(original.rglob('*.java')),'original-attribution.log');actual={(str(Path(l.split('\t')[0]).relative_to(original)),int(l.split('\t')[1]))for l in rows.splitlines()};assert expected<=actual and len(actual)==43,(len(actual),expected-actual)
  # Each chosen original expression must occur at the missing-method diagnostic position.
  log=(R/'porting/checkpoint87/full-compile.log').read_text()
  for f,edits in m['files'].items():
   for e in edits:assert '/build/sources/main/java/'+f.split('src/main/java/')[1]+':'+str(e['line'])+': error: cannot find symbol\n' in log,(f,e['line'])
  print('PASS: five exact old typed World/int positions among 43 match CP87 diagnostics and five native helper targets')
 if a.mutants:
  source=helper.read_text();mutants=[source.replace('new BlockPos(x, y, z)','new BlockPos(z, y, x)'),source.replace('world.getBlockState(pos)','net.minecraft.init.Blocks.AIR.getDefaultState()'),source.replace('state, state, 2','state, state, 10'),source.replace('state, state, 2','state, state, 3'),source.replace('world.notifyBlockUpdate(pos, state, state, 2);','world.setBlockState(pos, state, 2);'),source.replace('world.notifyBlockUpdate(pos, state, state, 2);',''),source.replace('world.notifyBlockUpdate(pos, state, state, 2);','world.notifyBlockUpdate(pos, state, state, 2); world.notifyBlockUpdate(pos, state, state, 2);'),source.replace('state, state, 2','state, world.getBlockState(pos), 2')]
  mutated=t/'TileEntityBlockUpdate.java'
  for i,s in enumerate(mutants):
   assert s!=source;mutated.write_text(s);run([javac,'-cp',cp,'-d',out,mutated]);r=subprocess.run([str(java),'-cp',runtime,'NotificationTest'],cwd=t,stdout=subprocess.PIPE,stderr=subprocess.STDOUT);assert r.returncode!=0,('survived helper mutant',i)
  print('PASS: eight coordinate/state/flag/write/noop/duplicate/read-count behavior mutants rejected')
  for f,es in m['files'].items():
   orig=(R/f).read_bytes()
   try:
    (R/f).write_bytes(orig.replace(es[0]['new'].encode(),b'',1))
    try:audit.verify();raise RuntimeError('source deletion mutant survived '+f)
    except AssertionError:pass
   finally:(R/f).write_bytes(orig)
  print('PASS: all five individual call deletion mutations rejected; source restored')
print('PASS: bounded proof complete; no whole-mod compile, game session, network transport or save-load acceptance claimed')
