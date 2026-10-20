#!/usr/bin/env python3
"""Complete native CRT owner/model proof; no Gradle, downloads, game API or owner substitutes."""
import argparse,hashlib,json,os,subprocess,tempfile,zipfile
from pathlib import Path
import build_oracle
p=argparse.ArgumentParser();p.add_argument('--module',type=Path,required=True);p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--original',type=Path);p.add_argument('--legacy-client',type=Path,required=True);a=p.parse_args();a.module=a.module.resolve();a.java_home=a.java_home.resolve();a.legacy_client=a.legacy_client.resolve();here=Path(__file__).resolve().parent;src=a.module/'src/main/java';res=a.module/'src/main/resources';exe='.exe' if os.name=='nt' else ''
production=['com/hbm/blocks/generic/BlockDecoCRT.java','com/hbm/blocks/BlockMulti.java','com/hbm/blocks/BlockBase.java','com/hbm/blocks/IBlockMulti.java','com/hbm/world/gen/nbt/INBTBlockTransformable.java','com/hbm/lib/RefStrings.java','com/hbm/render/icon/IIcon.java','com/hbm/render/icon/IIconRegister.java','com/hbm/render/block/RenderCRT.java','com/hbm/main/CRTModelHandler.java','com/hbm/render/item/nativeitem/ItemTransformMatrix.java','com/hbm/render/item/nativeitem/AngelCoreFrameCorrection.java']+['com/hbm/render/block/nativeblock/'+n+'.java' for n in ['CRTBakedModel','CRTPackedLightQuad','AtlasObjGeometry','BlockPositionLightQuad','ThermalModelTransforms']]+['com/hbm/render/mesh/'+n+'.java' for n in ['ObjMesh','ObjMeshParser']]
manifest=json.loads((here/'original-sha256.json').read_text());assets=[]
for rel,want in manifest.items():
 if '/resources/' in rel:
  assert hashlib.sha256((a.module/rel).read_bytes()).hexdigest()==want,'Original CRT asset changed: '+rel
  assets.append(rel.split('src/main/resources/')[1])
oracle=build_oracle.generate(a.original,a.legacy_client)
with tempfile.TemporaryDirectory(prefix='crt-native-') as d:
 out=Path(d);files=[]
 for rel in production:
  f=src/rel;print('PRODUCTION',rel,hashlib.sha256(f.read_bytes()).hexdigest(),flush=True);files.append(str(f))
 f=out/'OriginalCRTOracle.java';f.write_text(oracle);files.append(str(f))
 for name in ['CRTNativeTest','LegacyCRTRotationTest']:files.append(str(here/(name+'.java')))
 subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',d,'-cp',a.classpath,'-d',d]+files,check=True)
 bytecode=subprocess.run([str(a.java_home/'bin'/('javap'+exe)),'-c','-p','-classpath',d,'com.hbm.blocks.generic.BlockDecoCRT','com.hbm.render.block.RenderCRT','com.hbm.main.CRTModelHandler','com.hbm.render.block.nativeblock.CRTBakedModel','com.hbm.render.block.nativeblock.CRTPackedLightQuad'],capture_output=True,text=True,check=True).stdout
 for forbidden in ['ISimpleBlockRenderingHandler','net/minecraft/client/renderer/RenderBlocks','cpw/mods','getBlockMetadata','setBlockMetadataWithNotify','getCollisionBoundingBoxFromPool']:assert forbidden not in bytecode,'Legacy CRT bytecode reference: '+forbidden
 print('PASS complete native CRT production bytecode has no removed rendering/world APIs',flush=True)
 jar=out/'crt-resources.jar'
 with zipfile.ZipFile(jar,'w') as z:
  for rel in assets+['assets/hbm/blockstates/tile.deco_crt.json']:z.write(res/rel,rel)
 # Extract the independent bundled assets for oracle geometry, not migrated output.
 original=out/'pristine';original.mkdir()
 with zipfile.ZipFile(here/'pristine-crt-sources.zip') as z:
  for rel in manifest:
   if '/resources/' in rel:z.extract(rel,original)
 cp=os.pathsep.join([d,a.classpath]);java=str(a.java_home/'bin'/('java'+exe))
 subprocess.run([java,'-Djava.awt.headless=true','-cp',cp,'LegacyCRTRotationTest',str(a.legacy_client)],cwd=d,check=True)
 args=[str(res),str(jar),str(original/'src/main/resources')]
 subprocess.run([java,'-Xmx1400m','-Djava.awt.headless=true','-cp',cp,'CRTNativeTest']+args,cwd=d,check=True)
 for mode in ['replacement','subclass']:subprocess.run([java,'-Djava.awt.headless=true','-cp',cp,'CRTNativeTest']+args+[mode],cwd=d,check=True)
