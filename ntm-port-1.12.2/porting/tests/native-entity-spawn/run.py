#!/usr/bin/env python3
import argparse,os,subprocess,tempfile,json
from pathlib import Path
import check_source
H=Path(__file__).resolve().parent;R=H.parents[2]
a=argparse.ArgumentParser();a.add_argument('--java-home',type=Path,required=True);a.add_argument('--classpath-file',type=Path,required=True);a.add_argument('--legacy-client',type=Path,required=True);a.add_argument('--attribute',action='store_true');a=a.parse_args();m=check_source.verify();cp=a.classpath_file.read_text().strip();java=a.java_home/'bin/java';javac=a.java_home/'bin/javac';tools=a.java_home/'lib/tools.jar'
def run(cmd,**kw):return subprocess.run(list(map(str,cmd)),check=True,**kw)
with tempfile.TemporaryDirectory(prefix='entity-spawn-')as td:
 t=Path(td);out=t/'classes';out.mkdir();runtime=str(out)+os.pathsep+cp
 source=(R/'src/main/java/com/hbm/blocks/machine/BlockMassStorage.java').read_text()
 call=next(line.strip()for line in source.splitlines()if 'world.spawnEntity(new EntityItem(' in line)
 production=t/'ProductionSpawn.java';production.write_text('import net.minecraft.world.World;import net.minecraft.item.ItemStack;import net.minecraft.entity.item.EntityItem;class ProductionSpawn {static void spawn(World world,double x,double y,double z,ItemStack drop){'+call+'}}')
 run([javac,'-cp',cp+os.pathsep+str(tools),'-d',out,production,H/'SpawnScanner.java' ,H/'SpawnNativeTest.java',H/'OriginalSpawnTest.java',H.parent/'ForgeEventNativeLauncher.java'])
 run([java,'-cp',runtime,'ForgeEventNativeLauncher','SpawnNativeTest'],cwd=t)
 run([java,'-cp',runtime,'OriginalSpawnTest',a.legacy_client],cwd=t)
 def scan(paths,native=False):
  inputs=t/'inputs';inputs.write_text('\n'.join(map(str,paths))+'\n');cmd=[java,'-Xmx1800m','-cp',str(out)+os.pathsep+str(tools),'SpawnScanner',inputs,a.classpath_file]
  if native:cmd.append('native')
  return run(cmd,stdout=subprocess.PIPE,universal_newlines=True).stdout.splitlines()
 controls=t/'Controls.java';controls.write_bytes((H/'Controls.java.in').read_bytes());rows=scan([controls]);assert len(rows)==2,rows
 print('PASS two typed World/vanilla-constructor positives; six non-World/subclass/reuse/anonymous/arity/shadow negatives')
 if a.attribute:
  rows=scan(sorted((R/'src/main/java').rglob('*.java')),True);actual={(str(Path(r.split('\t')[0]).relative_to(R)),int(r.split('\t')[1]))for r in rows};expected=set()
  for rel,info in m['files'].items():
   s=(R/rel).read_bytes().decode();delta=0
   for e in sorted(info['edits'],key=lambda x:x['start']):
    pos=e['start']+delta;expected.add((rel,len(s[:pos].encode('utf-16-le'))//2));delta+=len(e['new'])-len(e['old'])
  preexisting={('src/main/java/com/hbm/entity/item/EntityMovingItem.java',2402),('src/main/java/com/hbm/blocks/generic/BlockSupplyCrate.java',3365)}
  assert actual==expected|preexisting,(len(actual),len(expected),actual^expected)
  print('PASS all 43 transformed invocations resolve to actual World.spawnEntity and vanilla EntityItem constructor')
