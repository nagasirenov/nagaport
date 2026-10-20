#!/usr/bin/env python3
"""Native RTG owner/model/TESR probe; real resource/GL APIs, explicit external NTM collaborators."""
import argparse,hashlib,json,os,subprocess,tempfile,zipfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=Path,required=True);a=p.parse_args()
ROOT=Path(__file__).resolve().parents[2];src=ROOT/'src/main/java';t=ROOT/'porting/tests';resources=ROOT/'src/main/resources'
original=ROOT.parent/'ntm-original-pristine'
pinned=json.loads((t/'native-rtg-render-source-manifest.json').read_text())
for relative,want in dict(pinned['sources'],**pinned['unchanged_assets']).items():
 assert hashlib.sha256((original/relative).read_bytes()).hexdigest()==want,relative
for relative,want in pinned['unchanged_assets'].items():assert hashlib.sha256((ROOT/relative).read_bytes()).hexdigest()==want,relative
legacy=(original/'src/main/java/com/hbm/render/block/RenderRTGBlock.java').read_text()
assert '"Gen", iicon, tessellator, 0, false' in legacy and 'GL11.glTranslated(0, -0.5, 0)' in legacy
registry=(original/'src/main/java/com/hbm/blocks/ModBlocks.java').read_text()
for name,owner,material in [('machine_rtg_grey','MachineRTG','rtg'),('machine_minirtg','MachineMiniRTG','rtg_cell'),('machine_powerrtg','MachineMiniRTG','rtg_polonium')]:
 line=next(x for x in registry.splitlines() if name+' = new ' in x)
 assert 'new '+owner+'(' in line and 'setBlockName("'+name+'")' in line and 'RefStrings.MODID + ":'+material+'"' in line
print('PASS pinned original Gen/transform facts, all three registered RTG aliases and seven unchanged assets',flush=True)
source=(t/'probe-rtg-energy.py').read_text();scope={'src':src,'ROOT':ROOT};exec(compile(source[source.index('collaborators ='):source.index('test_name=')],str(t/'probe-rtg-energy.py'),'exec'),scope)
collaborators=scope['collaborators'];production=scope['production']
lib=(src/'com/hbm/lib/Library.java').read_text();start=lib.index('public static boolean canConnect(');end=lib.index('\n\t/** dir is',start)
collaborators['com/hbm/lib/Library.java']='package com.hbm.lib;import net.minecraft.world.IBlockAccess;import net.minecraft.block.Block;import net.minecraft.tileentity.TileEntity;import api.hbm.energymk2.*; public class Library {'+scope['constants']+lib[start:end]+'}'
collaborators['com/hbm/main/ResourceManager.java']='''package com.hbm.main;import com.hbm.render.mesh.MeshModel;import net.minecraft.util.ResourceLocation;public class ResourceManager {public static MeshModel rtg;public static final ResourceLocation rtg_tex=new ResourceLocation("hbm","textures/models/machines/rtg.png"),rtg_polonium_tex=new ResourceLocation("hbm","textures/models/machines/rtg_polonium.png"),rtg_cell_tex=new ResourceLocation("hbm","textures/models/machines/rtg_cell.png");}'''
production += ['com/hbm/blocks/machine/MachineRTG.java','com/hbm/blocks/machine/MachineMiniRTG.java','api/hbm/energymk2/IEnergyConnectorBlock.java','com/hbm/lib/RefStrings.java','com/hbm/main/RTGBlockModelHandler.java','com/hbm/render/block/RenderRTGBlock.java','com/hbm/render/tileentity/RenderRTG.java']
production += ['com/hbm/render/block/nativeblock/'+n+'.java' for n in ['AtlasObjGeometry','AtlasObjBakedModel','BlockPositionLightQuad','ThermalModelTransforms']]
production += ['com/hbm/render/mesh/'+n+'.java' for n in ['MeshModel','ObjMesh','ObjMeshParser','ObjMeshRenderer','ObjMeshVboPayload']]+['com/hbm/render/loader/HFRWavefrontObject.java','com/hbm/render/loader/HFRWavefrontObjectVBO.java']
production += ['com/hbm/render/item/nativeitem/context/'+n+'.java' for n in ['NativeProjectionScope','NativeItemFailureScope','NativeItemFields']]+['com/hbm/render/item/nativeitem/MaterialTextureAtlasSprite.java']
with tempfile.TemporaryDirectory(prefix='rtg-render-native-') as d:
 out=Path(d);files=[]
 for rel,body in collaborators.items():
  dest=out/'fixtures'/rel;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_text(body);files.append(dest)
 for rel in dict.fromkeys(production):
  f=src/rel;files.append(f);print('PRODUCTION',rel,hashlib.sha256(f.read_bytes()).hexdigest(),flush=True)
 files += [t/(n+'.java') for n in ['RTGEnergyNativeTest','RTGRenderNativeTest','ForgeEventNativeLauncher','MaterialTextureAtlasSpriteNativeTest']]
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for name in z.namelist():
     if name.endswith('.so') and '/' not in name:(out/name).write_bytes(z.read(name))
 jar=out/'rtg-resources.jar'
 with zipfile.ZipFile(jar,'w') as z:
  for f in resources.rglob('*'):
   if f.is_file() and ('rtg' in f.name or f.name=='pack.mcmeta'):z.write(f,f.relative_to(resources).as_posix())
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(out/'fixtures'),'-cp',a.classpath,'-d',d]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+d,'-cp',d+os.pathsep+a.classpath,'ForgeEventNativeLauncher','RTGRenderNativeTest',str(resources),str(jar)],cwd=d,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)
