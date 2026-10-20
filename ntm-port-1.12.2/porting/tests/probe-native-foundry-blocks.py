#!/usr/bin/env python3
"""Whole foundry owners/tiles/models on cached native APIs, with explicit shared NTM collaborators."""
import argparse,pathlib,subprocess,tempfile,os,hashlib,zipfile,runpy
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
r=pathlib.Path(__file__).resolve().parents[2];s=r/'src/main/java';t=r/'porting/tests'
# Reuse the sibling tile probe's bounded NTM collaborators, not its compiler or tests.
source=(t/'probe-native-foundry-tiles.py').read_text();shared={'s':s,'t':t}
exec(compile(source[source.index('collaborators='):source.index('with tempfile.')],str(t/'probe-native-foundry-tiles.py'),'exec'),shared)
collaborators=shared['collaborators'];paths=shared['paths']
collaborators.pop('com/hbm/blocks/machine/FoundryChannel.java')
collaborators['com/hbm/blocks/ModBlocks.java']='package com.hbm.blocks;public class ModBlocks {public static net.minecraft.block.Block foundry_basin,foundry_mold,foundry_channel,foundry_tank,foundry_outlet,foundry_slagtap,slag;}'
collaborators['com/hbm/items/ModItems.java']='package com.hbm.items;public class ModItems {public static net.minecraft.item.Item mold,scraps;}'
collaborators['com/hbm/items/machine/ItemMold.java']=collaborators['com/hbm/items/machine/ItemMold.java'].replace('public int getCost()', 'public String getTitle(){return "fixture mold";}public int getCost()')
collaborators['com/hbm/items/machine/ItemScraps.java']='''package com.hbm.items.machine;public class ItemScraps {public static com.hbm.inventory.material.Mats.MaterialStack value;public static net.minecraft.item.ItemStack create(com.hbm.inventory.material.Mats.MaterialStack material){value=material;return new net.minecraft.item.ItemStack(com.hbm.items.ModItems.scraps,material.amount);}public static com.hbm.inventory.material.Mats.MaterialStack getMats(net.minecraft.item.ItemStack stack){return value;}}'''
collaborators['com/hbm/util/i18n/I18nUtil.java']='package com.hbm.util.i18n;public class I18nUtil{public static String resolveKey(String key,Object... args){return key;}}'
collaborators['com/hbm/wiaj/WorldInAJar.java']='package com.hbm.wiaj;public class WorldInAJar {public net.minecraft.tileentity.TileEntity getTileEntity(int x,int y,int z){throw new AssertionError("Actor world is not exercised");}}'
collaborators['com/hbm/inventory/RecipesCommon.java']='package com.hbm.inventory;public class RecipesCommon {public static class ComparableStack {public ComparableStack(net.minecraft.item.ItemStack stack){}}}'
paths+=['com/hbm/blocks/machine/Foundry'+n+'.java' for n in ['Block','ModelState','CastingBase','Basin','Mold','Channel','Tank','Outlet','Slagtap']]
paths+=['com/hbm/render/block/nativeblock/'+n+'.java' for n in ['FoundryGeometry','FoundryMaterials','FoundryBakedModel','ConveyorCuboidGeometry','BlockPositionLightQuad','ThermalModelTransforms']]
paths+=['com/hbm/main/FoundryBlockModelHandler.java','com/hbm/main/ModSoundEvents.java','com/hbm/blocks/ILookOverlay.java','api/hbm/block/IToolable.java','com/hbm/lib/RefStrings.java','com/hbm/render/tileentity/RenderFoundry.java','com/hbm/render/tileentity/RenderFoundryFluid.java','com/hbm/wiaj/actors/ITileActorRenderer.java']
paths+=['com/hbm/render/util/'+n+'.java' for n in ['RenderDecoItem','NativeGuiItemDraw','NativeItemQuadRenderer','NativeItemBaseModel','NativeMachineItemRenderer','NativeIconExtrusion']]
paths+=['com/hbm/render/item/nativeitem/ItemTransformMatrix.java','com/hbm/render/item/nativeitem/direct/PlainItemPerspectives.java']
paths+=['com/hbm/render/item/nativeitem/context/'+n+'.java' for n in ['NativeProjectionScope','NativeItemFailureScope','NativeItemFields']]+['com/hbm/render/item/nativeitem/MaterialTextureAtlasSprite.java']
with tempfile.TemporaryDirectory(prefix='foundry-block-native-') as d:
 out=pathlib.Path(d);files=[s/path for path in dict.fromkeys(paths)]+[t/'NativeFoundryBlocksTest.java',t/'FoundryRenderNativeTest.java',t/'ForgeEventNativeLauncher.java',t/'MaterialTextureAtlasSpriteNativeTest.java']
 oracle=out/'LegacyFoundryGeometryOracle.java';oracle.write_text(runpy.run_path(str(t/'foundry-legacy-geometry-oracle.py'))['generate'](r));files.append(oracle)
 for path,body in collaborators.items():
  target=out/'fixtures'/path;target.parent.mkdir(parents=True,exist_ok=True);target.write_text(body);files.append(target)
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for name in z.namelist():
     if name.endswith('.so') and '/' not in name:(out/name).write_bytes(z.read(name))
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(out/'fixtures'),'-cp',a.classpath,'-d',d]+list(map(str,files)),check=True)
 print('PASS full seven foundry block owners, eight production tiles, native models/TESRs and existing native preview helpers compile',flush=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+d,'-cp',d+os.pathsep+a.classpath,'ForgeEventNativeLauncher','NativeFoundryBlocksTest',str(r/'src/main/resources')],cwd=d,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+d,'-cp',d+os.pathsep+a.classpath,'ForgeEventNativeLauncher','FoundryRenderNativeTest',str(r/'src/main/resources')],cwd=d,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)
 for path in dict.fromkeys(paths):print(hashlib.sha256((s/path).read_bytes()).hexdigest(),path)
