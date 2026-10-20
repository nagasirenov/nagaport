#!/usr/bin/env python3
"""Real native CRT frame draw, reusing unchanged audited native machine fixtures."""
import argparse,importlib.util,os,pathlib,subprocess,tempfile,zipfile
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
here=pathlib.Path(__file__).resolve().parent;r=here.parents[2];s=r/'src/main/java';t=r/'porting/tests'
with tempfile.TemporaryDirectory(prefix='crt-native-frames-') as d:
 out=pathlib.Path(d);files=[s/('com/hbm/render/util/'+n+'.java') for n in ['NativeItemBaseModel','NativeMachineItemRenderer','NativeGuiItemDraw','NativeIconExtrusion','NativeItemQuadRenderer','RenderDecoItem','RenderItemStack']]
 files += [s/'com/hbm/render/item/nativeitem/MaterialTextureAtlasSprite.java']
 files += [t/(n+'.java') for n in ['NativeMachineItemTest','NativeMachineDrawTest','NativeMachineConsumerTest','NativeBaseModelOwnershipTest','MaterialTextureAtlasSpriteNativeTest','ForgeEventNativeLauncher']]
 spec=importlib.util.spec_from_file_location('fixtures',t/'machine-renderer-fixtures.py');f=importlib.util.module_from_spec(spec);spec.loader.exec_module(f);f.add(r,out,files)
 production=['blocks/generic/BlockDecoCRT','blocks/BlockMulti','blocks/BlockBase','blocks/IBlockMulti','world/gen/nbt/INBTBlockTransformable','render/icon/IIcon','render/icon/IIconRegister','render/block/RenderCRT','render/block/nativeblock/AtlasObjGeometry','render/block/nativeblock/BlockPositionLightQuad','render/block/nativeblock/CRTPackedLightQuad','render/block/nativeblock/CRTBakedModel','render/block/nativeblock/ThermalModelTransforms','render/mesh/ObjMeshParser','main/CRTModelHandler']
 files += [s/('com/hbm/'+name+'.java') for name in production]+[here/'CRTFrameNativeTest.java']
 # Compile the exact shared registration expression, controlling only the unrelated creative-tab holder.
 registration=next(line for line in (s/'com/hbm/blocks/ModBlocks.java').read_text().splitlines() if 'deco_crt = new ' in line)
 fragment=out/'CRTRegistrationFragmentTest.java'
 fragment.write_text('import com.hbm.blocks.generic.BlockDecoCRT;import net.minecraft.block.Block;import net.minecraft.block.material.Material;\npublic class CRTRegistrationFragmentTest {\n static class MainRegistry {static net.minecraft.creativetab.CreativeTabs blockTab=net.minecraft.creativetab.CreativeTabs.BUILDING_BLOCKS;}\n static Block deco_crt;static void initialize(){'+registration+'}\n public static void main(String[] args){net.minecraft.init.Bootstrap.register();initialize();if(!(deco_crt instanceof BlockDecoCRT)||!deco_crt.getRegistryName().toString().equals("hbm:tile.deco_crt")||!deco_crt.getTranslationKey().equals("tile.deco_crt")||deco_crt.getBlockHardness(deco_crt.getDefaultState(),null,net.minecraft.util.math.BlockPos.ORIGIN)!=5F)throw new AssertionError("Exact native CRT registration");System.out.println("PASS exact shared CRT registration expression compiles and retains native identity/hardness");}}\n')
 files.append(fragment)
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for name in z.namelist():
     if name.endswith('.so') and '/' not in name:(out/name).write_bytes(z.read(name))
 assert (out/'liblwjgl64.so').exists()
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(out/'fixtures'),'-cp',a.classpath,'-d',d]+list(map(str,files)),check=True)
 print('PASS complete CRT owner/model/light/frame routes and unchanged native machine consumers compile',flush=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-cp',d+os.pathsep+a.classpath,'CRTRegistrationFragmentTest'],cwd=d,check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+d,'-cp',d+os.pathsep+a.classpath,'ForgeEventNativeLauncher','CRTFrameNativeTest',str(r/'src/main/resources')],cwd=d,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)
