#!/usr/bin/env python3
"""Compile complete moving-item engine and frozen conveyor owners on native 1.12.2.
Only four unrelated NTM registry/recipe/i18n collaborators are bounded fixtures.
No Minecraft/Forge API, engine owner, block owner, or coordinate helper is stubbed.
"""
import argparse, hashlib, os, subprocess, tempfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);a=p.parse_args()
r=Path(__file__).resolve().parents[2];s=r/'src/main/java';exe='.exe' if os.name=='nt' else ''
collaborators={
 'com/hbm/blocks/ModBlocks.java':'package com.hbm.blocks; public final class ModBlocks {public static net.minecraft.block.Block conveyor, conveyor_lift, conveyor_chute;}',
 'com/hbm/items/ModItems.java':'package com.hbm.items; public final class ModItems {public static net.minecraft.item.Item conveyor_wand;}',
 'com/hbm/inventory/RecipesCommon.java':'package com.hbm.inventory; public class RecipesCommon {public static class ComparableStack {public ComparableStack(net.minecraft.item.ItemStack s){}}}',
 'com/hbm/util/i18n/I18nUtil.java':'package com.hbm.util.i18n; public class I18nUtil {public static String[] resolveKeyArray(String s){return new String[]{s};}}',
}
production=['com/hbm/entity/item/EntityMovingConveyorObject.java','com/hbm/entity/item/EntityMovingItem.java','com/hbm/util/fauxpointtwelve/BlockPos.java','com/hbm/util/fauxpointtwelve/Rotation.java','com/hbm/interfaces/Spaghetti.java','com/hbm/lib/ForgeDirection.java','com/hbm/blocks/network/ConveyorModelState.java','com/hbm/blocks/ITooltipProvider.java','api/hbm/block/IToolable.java']+['com/hbm/blocks/network/BlockConveyor'+n+'.java' for n in ['Base','Bendable','','Double','Triple','Express','Lift','Chute','Classic']]+['api/hbm/conveyor/'+n+'.java' for n in ['IConveyorBelt','IEnterableBlock','IConveyorItem','IConveyorPackage']]
with tempfile.TemporaryDirectory(prefix='native-moving-item-engine-') as d:
 out=Path(d);files=[]
 for rel,body in collaborators.items():
  f=out/rel;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(body);files.append(str(f));print('BOUNDED_NTM_COLLABORATOR',rel,flush=True)
 for rel in production:
  f=s/rel;print('PRODUCTION',rel,hashlib.sha256(f.read_bytes()).hexdigest(),flush=True);files.append(str(f))
 files.append(str(r/'porting/tests/MovingItemEngineNativeTest.java'))
 subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(out),'-cp',a.classpath,'-d',d]+files,check=True)
 print('PASS complete production moving-item engine, coordinate helper, nine frozen conveyor owners and native APIs compiled.',flush=True)
 subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-cp',os.pathsep.join([d,a.classpath]),'MovingItemEngineNativeTest'],cwd=d,check=True)
print('PASS bounded real-World/Chunk/Entity/DataManager/ItemStack engine proof; no complete package/crane/render/full-mod or live-server acceptance.',flush=True)
