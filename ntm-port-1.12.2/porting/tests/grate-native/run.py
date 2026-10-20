#!/usr/bin/env python3
import argparse,hashlib,json,os,subprocess,tempfile,zipfile
from pathlib import Path
import build_oracle,build_mechanics_oracle
p=argparse.ArgumentParser();p.add_argument('--module',type=Path,required=True);p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--legacy-client',type=Path,required=True);a=p.parse_args()
a.module=a.module.resolve();here=Path(__file__).resolve().parent;src=a.module/'src/main/java';res=a.module/'src/main/resources';exe='.exe' if os.name=='nt' else ''
production=['com/hbm/blocks/generic/BlockGrate.java','com/hbm/blocks/ITooltipProvider.java','com/hbm/util/i18n/I18nUtil.java','com/hbm/util/i18n/ITranslate.java','com/hbm/util/i18n/I18nServer.java','com/hbm/lib/RefStrings.java','com/hbm/render/block/RenderGrate.java','com/hbm/render/block/nativeblock/GrateBakedModel.java','com/hbm/render/block/nativeblock/GrateShiftedLightQuad.java','com/hbm/render/block/nativeblock/ConveyorCuboidGeometry.java','com/hbm/main/GrateModelHandler.java']
fixture='''package com.hbm.main;
public final class MainRegistry {
 public static final Proxy proxy = new Proxy();
 public static final class Proxy {
  public com.hbm.util.i18n.ITranslate translator = new com.hbm.util.i18n.I18nServer();
  public com.hbm.util.i18n.ITranslate getI18n(){return translator;}
 }
}'''
manifest=json.loads((here/'original-sha256.json').read_text())
for path,want in manifest.items():
 if '/resources/' in path:assert hashlib.sha256((a.module/path).read_bytes()).hexdigest()==want,'Original grate texture changed: '+path
with tempfile.TemporaryDirectory(prefix='grate-native-') as d:
 out=Path(d);files=[]
 f=out/'com/hbm/main/MainRegistry.java';f.parent.mkdir(parents=True);f.write_text(fixture);files.append(str(f))
 for path in production:
  f=src/path;print('PRODUCTION',path,hashlib.sha256(f.read_bytes()).hexdigest(),flush=True);files.append(str(f))
 f=out/'OriginalGrateRenderOracle.java';f.write_text(build_oracle.generate(a.legacy_client));files.append(str(f))
 f=out/'OriginalGrateMechanicsOracle.java';f.write_text(build_mechanics_oracle.generate());files.append(str(f))
 for name in ['GrateRenderTest','GrateMechanicsTest']:
  files.append(str(here/(name+'.java')))
 subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',d,'-cp',a.classpath,'-d',d]+files,check=True)
 bytecode=subprocess.run([str(a.java_home/'bin'/('javap'+exe)),'-c','-p','-classpath',d,'com.hbm.blocks.generic.BlockGrate','com.hbm.render.block.RenderGrate','com.hbm.render.block.nativeblock.GrateBakedModel','com.hbm.render.block.nativeblock.GrateShiftedLightQuad','com.hbm.main.GrateModelHandler'],capture_output=True,text=True,check=True).stdout
 for forbidden in ['ISimpleBlockRenderingHandler','net/minecraft/client/renderer/RenderBlocks','cpw/mods','getBlockMetadata','setBlockMetadataWithNotify','getCollisionBoundingBoxFromPool']:
  assert forbidden not in bytecode,'Legacy production bytecode reference: '+forbidden
 print('PASS complete grate production bytecode contains no removed rendering/world APIs',flush=True)
 jar=out/'grate-resources.jar'
 with zipfile.ZipFile(jar,'w') as z:
  for name in ['grate_top','grate_wide_top','grate_side']:
   rel='assets/hbm/textures/blocks/'+name+'.png';z.write(res/rel,rel)
  for name in ['steel_grate','steel_grate_wide']:
   rel='assets/hbm/blockstates/tile.'+name+'.json';z.write(res/rel,rel)
 for name in ['GrateMechanicsTest','GrateRenderTest']:
  subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-Xmx1G','-Djava.awt.headless=true','-cp',os.pathsep.join([d,a.classpath]),name,str(res),str(jar),str(a.legacy_client)],cwd=d,check=True)
