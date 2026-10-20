#!/usr/bin/env python3
"""Complete native owners/model/loader. External NTM UI/tile domain collaborators are explicit; native platform classes are genuine."""
import argparse,hashlib,json,os,re,subprocess,tempfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--pristine',type=Path,required=True);p.add_argument('--legacy-client',type=Path,required=True);a=p.parse_args()
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce','Unexpected legacy client'
t=Path(__file__).resolve().parent;r=t.parents[2];s=r/'src/main/java'
production=['com/hbm/blocks/network/'+n+'.java' for n in ['RadioTorchBase','RadioTorchRWBase','RadioTorchSender','RadioTorchReceiver','RadioTorchLogic','RadioTorchReader','RadioTorchController','RadioTorchCounter','RadioTorchProgrammer','RadioTorchModelState']]
production+=['com/hbm/'+x+'.java' for x in ['blocks/BlockBase','blocks/ITooltipProvider','blocks/ILookOverlay','tileentity/IGUIProvider','lib/RefStrings','lib/ForgeDirection','lib/FacingCompat','render/icon/IIcon','render/icon/IIconRegister','render/block/RenderRTTY','render/block/nativeblock/RadioTorchGeometry','render/block/nativeblock/RadioTorchBakedModel','render/block/nativeblock/AtlasObjGeometry','render/block/nativeblock/BlockPositionLightQuad','render/mesh/ObjMesh','render/mesh/ObjMeshParser','main/RadioTorchModelHandler']]
production+=['api/hbm/redstoneoverradio/'+n+'.java' for n in ['IRORInfo','IRORInteractive','IRORValueProvider','RORFunctionException']]
fixtures={
'com/hbm/main/MainRegistry.java':'package com.hbm.main; public class MainRegistry { public static final Object instance=new Object(); }',
'com/hbm/util/i18n/I18nUtil.java':'package com.hbm.util.i18n; public class I18nUtil { public static String resolveKey(String s){return s;} public static String[] resolveKeyArray(String s){return new String[]{s};} }',
'com/hbm/util/Compat.java':'package com.hbm.util; public class Compat { public static net.minecraft.tileentity.TileEntity getTileStandard(net.minecraft.world.World w,int x,int y,int z){return w.getTileEntity(new net.minecraft.util.math.BlockPos(x,y,z));} }',
'com/hbm/inventory/container/ContainerCounterTorch.java':'package com.hbm.inventory.container; public class ContainerCounterTorch extends net.minecraft.inventory.Container { public ContainerCounterTorch(Object... a){} public boolean canInteractWith(net.minecraft.entity.player.EntityPlayer p){return true;} }'
}
for n in ['GUIScreenRadioTorch','GUIScreenRadioTorchLogic','GUIScreenRadioTorchReader','GUIScreenRadioTorchController','GUICounterTorch']:fixtures['com/hbm/inventory/gui/'+n+'.java']='package com.hbm.inventory.gui; public class '+n+' { public '+n+'(Object... a){} }'
for n in ['Base','Sender','Receiver','Logic','Reader','Controller','Counter']:
 parent='TileEntityRadioTorchBase' if n in ['Sender','Receiver'] else 'net.minecraft.tileentity.TileEntity'
 fields='' if n in ['Sender','Receiver'] else 'public volatile int lastState; public long lastUpdate; public String channel="";'
 if n=='Reader':fields+=' public String[] channels=new String[8],names=new String[8];'
 if n=='Counter':fields='public String[] channel={"","",""}; public int[] lastCount=new int[3];'
 fixtures['com/hbm/tileentity/network/TileEntityRadioTorch'+n+'.java']='package com.hbm.tileentity.network; public class TileEntityRadioTorch'+n+' extends '+parent+' {'+fields+'}'
# The shared production breaking renderer contains two unrelated network branches.
# Their class-token predicates are bounded collaborators; radio routing uses real owners.
production+=['com/hbm/render/tileentity/NetworkBreakingRenderer.java','com/hbm/render/tileentity/RadioTorchBreakingRenderer.java','com/hbm/render/block/nativeblock/AtlasObjBakedModel.java','com/hbm/render/block/nativeblock/ThermalModelTransforms.java']
fixtures['com/hbm/blocks/network/CableDiode.java']='package com.hbm.blocks.network; public class CableDiode {public static class TileEntityDiode extends net.minecraft.tileentity.TileEntity {} public static boolean isNativeModelOwner(net.minecraft.block.Block b){return false;}}'
fixtures['com/hbm/blocks/network/CranePartitioner.java']='package com.hbm.blocks.network; public class CranePartitioner {public static class TileEntityCranePartitioner extends net.minecraft.tileentity.TileEntity {} public static boolean isNativeModelOwner(net.minecraft.block.Block b){return false;}}'
fixtures['com/hbm/render/block/nativeblock/DiodeBakedModel.java']='package com.hbm.render.block.nativeblock; public abstract class DiodeBakedModel implements net.minecraft.client.renderer.block.model.IBakedModel {public java.util.List<net.minecraft.client.renderer.block.model.BakedQuad> getAllQuads(net.minecraft.block.state.IBlockState state){return java.util.Collections.emptyList();}}'
# Use the tile proof's explicit external collaborators, replacing every radio-tile
# fixture above with the complete real tile/base implementation in this combined run.
from types import SimpleNamespace
scope={'a':SimpleNamespace(module=r),'src':s,'Path':Path}
tile_script=(t/'tile-probe.py').read_text()
exec(tile_script[tile_script.index("script=(a.module/"):tile_script.index("with tempfile.TemporaryDirectory")],scope)
fixtures.update(scope['collaborators'])
production=list(dict.fromkeys(production+scope['production']))
for rel in production:fixtures.pop(rel,None)
def method(text,name):
 m=re.search(r'public (?:static )?[\w<>]+ '+name+r'\([^)]*\)\s*\{',text);assert m,name;e=m.end();d=1
 while d:d+=(text[e]=='{')-(text[e]=='}');e+=1
 return text[m.start():e]
orig=a.pristine/'src/main/java/com/hbm/render/block/RenderRTTY.java'
oracle=(t/'OriginalRTTYOracle.java.in').read_text().replace('/* BODY */',method(orig.read_text(),'renderWorldBlock'))
manifest={}
for f in list((a.pristine/'src/main/java/com/hbm/blocks/network').glob('RadioTorch*.java'))+[orig,a.pristine/'src/main/java/com/hbm/render/util/ObjUtil.java']:
 manifest[str(f.relative_to(a.pristine))]=hashlib.sha256(f.read_bytes()).hexdigest()
for f in (a.pristine/'src/main/resources/assets/hbm').glob('**/rtty*'):
 if f.is_file():
  rel=f.relative_to(a.pristine);assert f.read_bytes()==(r/rel).read_bytes(),rel;manifest[str(rel)]=hashlib.sha256(f.read_bytes()).hexdigest()
mp=t/'original-manifest.json'
if mp.exists():assert json.loads(mp.read_text())==manifest,'Pinned original mismatch'
else:mp.write_text(json.dumps(manifest,sort_keys=True,indent=2)+'\n')
with tempfile.TemporaryDirectory(prefix='rtty-owner-model-') as d:
 out=Path(d);files=[s/x for x in production]
 for rel,body in fixtures.items():
  assert rel.startswith(('com/hbm/','api/hbm/'));f=out/rel;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(body);files.append(f)
 f=out/'OriginalRTTYOracle.java';f.write_text(oracle);files.append(f)
 files += [t/'RadioTorchOwnerModelTest.java',r/'porting/tests/ForgeEventNativeLauncher.java']
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',d,'-cp',a.classpath,'-d',d]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-cp',d+os.pathsep+a.classpath,'ForgeEventNativeLauncher','RadioTorchOwnerModelTest',str(r/'src/main/resources'),str(a.pristine/'src/main/resources'),str(a.legacy_client)],cwd=d,check=True)
print('PASS genuine native owner/model/loader compile and tests. Complete family owners and tiles/bases are compiled together; only external NTM domain collaborators are bounded.')
