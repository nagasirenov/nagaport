from pathlib import Path
import argparse, subprocess, tempfile, os
p=argparse.ArgumentParser();p.add_argument('--module',type=Path,required=True);p.add_argument('--source-root',type=Path,required=True);p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);a=p.parse_args();t=Path(__file__).resolve().parent
production=['com/hbm/blocks/BlockDummyable.java','com/hbm/handler/MultiblockHandlerXR.java','com/hbm/interfaces/ICopiable.java']
unchanged=['com/hbm/blocks/ICustomBlockHighlight.java','com/hbm/handler/ThreeInts.java','com/hbm/util/Either.java','com/hbm/tileentity/IPersistentNBT.java','com/hbm/world/gen/nbt/INBTBlockTransformable.java','com/hbm/lib/ForgeDirection.java']
fixtures={'com.hbm.main.MainRegistry':'public class MainRegistry {public static Object instance;}','com.hbm.util.CompatExternal':'public class CompatExternal {public static net.minecraft.tileentity.TileEntity getCoreFromPos(net.minecraft.world.World w,int x,int y,int z){return w.getTileEntity(new net.minecraft.util.math.BlockPos(x,y,z));}}'}
def method(s,signature):
 start=s.index(signature);end=s.index('{',start);depth=1;end+=1
 while depth:
  depth+=(s[end]=='{')-(s[end]=='}');end+=1
 return s[start:end]
with tempfile.TemporaryDirectory(prefix='dummyable-native-') as d:
 root=Path(d);files=[a.source_root/'src/main/java'/q for q in production]+[a.module/'src/main/java'/q for q in unchanged]+[t/'DummyableNativeTest.java']
 for name,body in fixtures.items():
  f=root/(name.replace('.','/')+'.java');f.parent.mkdir(parents=True,exist_ok=True);f.write_text('package '+name.rsplit('.',1)[0]+';'+body);files.append(f)
 source=(a.module/'src/main/java/com/hbm/blocks/machine/MachineHeatBoiler.java').read_text()
 hooks='\n'.join(method(source,sig) for sig in ['public void fillSpace(', 'public int[] getDimensions(', 'public int getOffset('])
 f=root/'RealBoilerHooks.java';f.write_text('import net.minecraft.world.World;import com.hbm.lib.ForgeDirection;public class RealBoilerHooks extends DummyableNativeTest.B {\n'+hooks+'\n}');files.append(f)
 subprocess.run([str(a.java_home/'bin/javac'),'-source','8','-target','8','-cp',a.classpath,'-d',str(root)]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-cp',str(root)+os.pathsep+a.classpath,'DummyableNativeTest'],cwd=root,check=True)
 print('Whole native base/helper, six real dependencies, two explicit external collaborators; three exact production boiler domain hooks, not the complete boiler owner.')
