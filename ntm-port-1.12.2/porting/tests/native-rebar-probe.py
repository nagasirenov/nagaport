#!/usr/bin/env python3
"""Complete rebar owner/native model geometry + real node dispatcher; external NTM services are explicit fixtures."""
import argparse,pathlib,subprocess,tempfile,os,zipfile,hashlib,re
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
r=pathlib.Path(__file__).resolve().parents[2];s=r/'src/main/java';t=r/'porting/tests'
import runpy
spec=runpy.run_path(str(t/'native-rebar-fixtures.py'));collaborators=spec['collaborators'];paths=spec['paths']
original=r.parent/'ntm-original-pristine/src/main/java/com/hbm/blocks/generic/BlockRebar.java'
source=original.read_text();start=source.index('public long transferFluid(');brace=source.index('{',start);depth=1;end=brace+1
while depth:depth+=(source[end]=='{')-(source[end]=='}');end+=1
method=source[start:end].replace('worldObj.getTileEntity(node.positions[0].getX(), y, node.positions[0].getZ())','this.world.getTileEntity(new net.minecraft.util.math.BlockPos(node.positions[0].getX(), y, node.positions[0].getZ()))')
# One native World method adaptation; original complete distribution algorithm is otherwise unchanged.
oracle='''import java.util.*;import com.hbm.blocks.generic.BlockRebar.TileEntityRebar;import com.hbm.blocks.generic.BlockRebar.RebarNode;import com.hbm.inventory.fluid.*;import com.hbm.util.BobMathUtil;import net.minecraft.tileentity.TileEntity;public class LegacyRebarOracle extends TileEntityRebar {'''+method+'}'
with tempfile.TemporaryDirectory(prefix='native-rebar-') as d:
 out=pathlib.Path(d);files=[s/path for path in paths]+[t/'ForgeEventNativeLauncher.java',t/'MaterialTextureAtlasSpriteNativeTest.java']
 for path,body in collaborators.items():
  target=out/'fixtures'/path;target.parent.mkdir(parents=True,exist_ok=True);target.write_text(body);files.append(target)
 for name,body in [('NativeRebarTest.java',(t/'native-rebar-test.java.in').read_text()),('LegacyRebarOracle.java',oracle)]:
  target=out/name;target.write_text(body);files.append(target)
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for name in z.namelist():
     if name.endswith('.so') and '/' not in name:(out/name).write_bytes(z.read(name))
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(out/'fixtures'),'-cp',a.classpath,'-d',d]+list(map(str,files)),check=True)
 print('PASS complete BlockRebar including all native block callbacks, nested tile and both render-last branches compiles, plus actual node dispatcher/shared geometry',flush=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+d,'-cp',d+os.pathsep+a.classpath,'ForgeEventNativeLauncher','NativeRebarTest',str(r/'src/main/resources')],cwd=d,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)
 print('Original oracle source SHA256',hashlib.sha256(original.read_bytes()).hexdigest())
 for path in paths:print(hashlib.sha256((s/path).read_bytes()).hexdigest(),path)
