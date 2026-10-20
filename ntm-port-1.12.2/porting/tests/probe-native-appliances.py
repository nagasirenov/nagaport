#!/usr/bin/env python3
"""Compile full appliance owners and model consumer closure against native Forge 1.12.2."""
import argparse, pathlib, subprocess, tempfile, os
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
r=pathlib.Path(__file__).resolve().parents[2];s=r/'src/main/java'
production=['blocks/BlockEnumMulti','blocks/BlockEnums','util/EnumUtil','blocks/generic/BlockDecoModel','blocks/generic/BlockDecoContainer','render/block/RenderBlockDecoModel','blocks/generic/BlockDecoToaster','blocks/BlockMulti','blocks/BlockBase','blocks/IBlockMulti','world/gen/nbt/INBTBlockTransformable','lib/RefStrings','render/icon/IIcon','render/icon/IIconRegister','render/block/RenderToaster','render/block/nativeblock/AtlasObjGeometry','render/block/nativeblock/BlockPositionLightQuad','render/block/nativeblock/DecoApplianceBakedModel','render/block/nativeblock/ThermalModelTransforms','render/mesh/ObjMesh','render/mesh/ObjMeshParser','main/DecoApplianceModelHandler']
collaborators={
 'com/hbm/main/MainRegistry.java':'package com.hbm.main; public class MainRegistry {public static Object instance=new Object();public static org.apache.logging.log4j.Logger logger=org.apache.logging.log4j.LogManager.getLogger();}',
 'com/hbm/items/ModItems.java':'package com.hbm.items;public class ModItems {public static net.minecraft.item.Item key_kit=new net.minecraft.item.Item();}',
 'com/hbm/items/tool/ItemLock.java':'package com.hbm.items.tool;public class ItemLock extends net.minecraft.item.Item {}',
 'com/hbm/tileentity/machine/TileEntityLockableBase.java':'package com.hbm.tileentity.machine;public abstract class TileEntityLockableBase extends net.minecraft.tileentity.TileEntity { public boolean canAccess(net.minecraft.entity.player.EntityPlayer player){return false;} }'
}
with tempfile.TemporaryDirectory(prefix='native-appliances-') as d:
 files=[str(s/('com/hbm/'+x+'.java')) for x in production]+[str(r/'porting/tests/NativeApplianceTest.java')]
 for path,body in collaborators.items():
  target=pathlib.Path(d)/path;target.parent.mkdir(parents=True,exist_ok=True);target.write_text(body);files.append(str(target))
 subprocess.run([str(a.java_home/'bin/javac'),'-source','8','-target','8','-encoding','UTF-8','-sourcepath',d,'-cp',a.classpath,'-d',d]+files,check=True)
 print('PASS 22 complete production classes; four explicit NTM registry/lock collaborators, no Minecraft/Forge substitutes',flush=True)
 subprocess.run([str(a.java_home/'bin/java'),'-cp',d+os.pathsep+a.classpath,'NativeApplianceTest',str(r)],check=True)
