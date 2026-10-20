#!/usr/bin/env python3
"""Complete native model + block owner compilation. Explicit NTM mechanical collaborators
are test doubles, NOT a pneumatic transport implementation. No Minecraft/Forge stubs.
"""
import argparse,hashlib,json,os,re,subprocess,tempfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--base',type=Path,required=True);p.add_argument('--pristine',type=Path,required=True);a=p.parse_args()
r=Path(__file__).resolve().parents[2];s=r/'src/main/java';t=r/'porting/tests'
collaborators={
'com/hbm/main/MainRegistry.java':'package com.hbm.main; public class MainRegistry { public static final Object instance=new Object(); }',
'com/hbm/util/i18n/I18nUtil.java':'package com.hbm.util.i18n; public class I18nUtil {public static String[] resolveKeyArray(String s){return new String[]{s};}}',
'com/hbm/inventory/RecipesCommon.java':'package com.hbm.inventory; public class RecipesCommon {public static class ComparableStack {public ComparableStack(net.minecraft.item.ItemStack s){}}}',
'com/hbm/inventory/fluid/FluidType.java':'package com.hbm.inventory.fluid; public class FluidType {public boolean hasTrait(Class<?> c){return false;}public String getConditionalName(){return "air";}}',
'com/hbm/inventory/fluid/Fluids.java':'package com.hbm.inventory.fluid; public class Fluids {public static final FluidType AIR=new FluidType();}',
'com/hbm/inventory/fluid/trait/FT_Corrosive.java':'package com.hbm.inventory.fluid.trait; public class FT_Corrosive{}',
'com/hbm/inventory/fluid/trait/FT_Gaseous.java':'package com.hbm.inventory.fluid.trait; public class FT_Gaseous{}',
'com/hbm/inventory/fluid/trait/FluidTraitSimple.java':'package com.hbm.inventory.fluid.trait; public class FluidTraitSimple{public static class FT_Amat{} public static class FT_Gaseous_ART{}}',
'com/hbm/tileentity/network/TileEntityPneumoTube.java':'''package com.hbm.tileentity.network;
public class TileEntityPneumoTube extends net.minecraft.tileentity.TileEntity {
 public com.hbm.lib.ForgeDirection insertionDir=com.hbm.lib.ForgeDirection.UNKNOWN,ejectionDir=com.hbm.lib.ForgeDirection.UNKNOWN;
 public final Tank compair=new Tank();public boolean isCompressor(){return insertionDir!=com.hbm.lib.ForgeDirection.UNKNOWN;}public boolean isEndpoint(){return ejectionDir!=com.hbm.lib.ForgeDirection.UNKNOWN;}
 public static class Tank {private com.hbm.inventory.fluid.FluidType type=com.hbm.inventory.fluid.Fluids.AIR;public com.hbm.inventory.fluid.FluidType getTankType(){return type;}public void setTankType(com.hbm.inventory.fluid.FluidType t){type=t;}}
}''',
'com/hbm/util/Compat.java':'''package com.hbm.util; public class Compat { public static net.minecraft.tileentity.TileEntity getTileStandard(net.minecraft.world.World w,int x,int y,int z){return w.getTileEntity(new net.minecraft.util.math.BlockPos(x,y,z));}}''',
'com/hbm/lib/Library.java':'''package com.hbm.lib; public class Library {
public static final ForgeDirection POS_X=ForgeDirection.EAST,NEG_X=ForgeDirection.WEST,POS_Y=ForgeDirection.UP,NEG_Y=ForgeDirection.DOWN,POS_Z=ForgeDirection.SOUTH,NEG_Z=ForgeDirection.NORTH;
public static final java.util.Set<net.minecraft.util.math.BlockPos> AIR_PORTS=new java.util.HashSet<>();
public static boolean canConnectFluid(net.minecraft.world.IBlockAccess w,int x,int y,int z,ForgeDirection d,com.hbm.inventory.fluid.FluidType f){return AIR_PORTS.contains(new net.minecraft.util.math.BlockPos(x,y,z));}
}'''
}
basefiles=['com/hbm/blocks/ITooltipProvider.java','api/hbm/block/IToolable.java','com/hbm/lib/ForgeDirection.java','com/hbm/lib/RefStrings.java','com/hbm/items/machine/IItemFluidIdentifier.java','com/hbm/render/block/nativeblock/ThermalModelTransforms.java']
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def method(text,name):
 m=re.search(r'(?:public|protected) (?:static )?(?:void|boolean) '+name+r'\([^)]*\)\s*\{',text);assert m,name
 i=m.end();d=1
 while d:d+=(text[i]=='{')-(text[i]=='}');i+=1
 return text[m.start():i]
original=a.pristine/'src/main/java/com/hbm/render/block/RenderPneumoTube.java'
body='\n'.join(method(original.read_text(),name) for name in ['renderInventoryBlock','renderWorldBlock','renderCon'])
for old,new in {'RenderBlocksNT':'RecorderFactory','RenderBlocks':'Recorder','Tessellator':'SilentTessellator','Block':'OracleBlock','PneumoTube':'OracleTube','TileEntityPneumoTube':'OracleTile','TileEntity':'Object','IBlockAccess':'OracleWorld','IIcon':'String','GL11':'SilentGL'}.items():body=re.sub(r'\b'+old+r'\b',new,body)
oracle=(t/'pneumatic-oracle.java.in').read_text().replace('/* METHODS */',body)
manifest={'scope':'Exposed model and complete block owner boundary only; NTM mechanical collaborators are test doubles. No functional network, paintable, GL or whole-mod claim.','production':{},'pristine':{},'assets':{}}
for rel in ['src/main/java/com/hbm/render/block/RenderPneumoTube.java','src/main/java/com/hbm/blocks/network/PneumoTube.java','src/main/java/com/hbm/tileentity/network/TileEntityPneumoTube.java','src/main/java/com/hbm/uninos/networkproviders/PneumaticNetwork.java','src/main/java/com/hbm/render/util/RenderBlocksNT.java']:manifest['pristine'][rel]=sha(a.pristine/rel)
for suffix in ['', '_in','_out','_connector','_straight']:
 rel='src/main/resources/assets/hbm/textures/blocks/pneumatic_tube'+suffix+'.png';assert sha(a.base/rel)==sha(a.pristine/rel);manifest['assets'][rel]=sha(a.pristine/rel)
with tempfile.TemporaryDirectory(prefix='native-pneumatic-') as d:
 out=Path(d);files=[]
 for rel,body in collaborators.items():
  f=out/rel;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(body);files.append(str(f))
 production=['com/hbm/blocks/network/PneumoTube.java','com/hbm/blocks/network/PneumaticModelState.java','com/hbm/render/block/RenderPneumoTube.java','com/hbm/render/block/nativeblock/PneumaticCuboidGeometry.java','com/hbm/render/block/nativeblock/PneumaticBakedModel.java','com/hbm/main/PneumaticModelHandler.java']
 for f in [s/rel for rel in production]+[a.base/'src/main/java'/rel for rel in basefiles]:
  files.append(str(f));manifest['production'][str(f.relative_to(r)) if f.is_relative_to(r) else 'base/'+str(f.relative_to(a.base))]=sha(f)
 f=out/'LegacyPneumaticOracle.java';f.write_text(oracle);files.append(str(f))
 files.append(str(t/'PneumaticNativeTest.java'))
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',d,'-cp',a.classpath,'-d',d]+files,check=True)
 print('PASS complete native PneumoTube, RenderPneumoTube, immutable snapshot, model, cuboid geometry and loader compiled.',flush=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Xmx1200m','-Djava.awt.headless=true','-cp',d+os.pathsep+a.classpath,'PneumaticNativeTest'],cwd=d,check=True)
manifest_path=t/'pneumatic-source-manifest.json'
if manifest_path.exists():
 previous=json.loads(manifest_path.read_text())
 assert previous['pristine']==manifest['pristine'],'Pinned pristine sources changed'
 assert previous['assets']==manifest['assets'],'Pinned original assets changed'
manifest_path.write_text(json.dumps(manifest,indent=2,sort_keys=True)+'\n')
print('PASS bounded native owner/model boundary; no functional pneumatic network acceptance.',flush=True)
