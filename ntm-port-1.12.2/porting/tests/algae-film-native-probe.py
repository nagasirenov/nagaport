#!/usr/bin/env python3
"""Compile complete algae production slice against real 1.12, with controlled NTM gameplay dependencies."""
import argparse,hashlib,json,subprocess,tempfile,zipfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);a=p.parse_args()
r=Path(__file__).resolve().parents[2];s=r/'src/main/java';t=r/'porting/tests';o=r.parent/'ntm-original-pristine'
manifest=json.loads((t/'algae-film-original-manifest.json').read_text())
for rel,digest in manifest.items():assert hashlib.sha256((o/rel).read_bytes()).hexdigest()==digest,rel
for rel in manifest:
 if '/resources/' in rel:assert (r/rel).read_bytes()==(o/rel).read_bytes(),rel
old=(o/'src/main/java/com/hbm/render/block/RenderAlgaeFilm.java').read_text()
for token in ['if(meta == 4) rotation = 90F / 180F * (float) Math.PI;', 'if(meta == 5) rotation = 270F / 180F * (float) Math.PI;', 'if(meta == 3) rotation = 180F / 180F * (float)Math.PI;', 'GL11.glTranslated(0, -0.5, 0);', 'tessellator.addTranslation(x + 0.5F, y, z + 0.5F);', 'iicon, tessellator, 0, false)', 'iicon, tessellator, rotation, true)']:
 assert token in old, 'Original selector/transform changed: '+token
print('PASS pinned original renderer, owner, shader and unchanged OBJ/texture',flush=True)
prod=['com/hbm/blocks/generic/BlockAlgaeFilm.java','com/hbm/render/block/RenderAlgaeFilm.java','com/hbm/main/AlgaeFilmModelHandler.java','com/hbm/blocks/ILookOverlay.java','com/hbm/blocks/ITooltipProvider.java','com/hbm/lib/RefStrings.java','com/hbm/render/mesh/ObjMesh.java','com/hbm/render/mesh/ObjMeshParser.java']+['com/hbm/render/block/nativeblock/'+x+'.java' for x in ['AtlasObjGeometry','AtlasObjBakedModel','BlockPositionLightQuad','ThermalModelTransforms']]
stubs={
'com/hbm/tileentity/machine/TileEntityAlgaeFilm.java':'''package com.hbm.tileentity.machine; public class TileEntityAlgaeFilm extends net.minecraft.tileentity.TileEntity {public boolean canOperate=true; public Tank[] tanks={new Tank(),new Tank()};public static class Tank{public Tank getTankType(){return this;}public String getLocalizedName(){return "test";}public int getFill(){return 0;}public int getMaxFill(){return 1;}}}''',
'com/hbm/util/BobMathUtil.java':'package com.hbm.util;public class BobMathUtil{public static boolean getBlink(){return false;}}',
'com/hbm/util/i18n/I18nUtil.java':'package com.hbm.util.i18n;public class I18nUtil{public static String resolveKey(String s,Object... args){return s;}public static String[] resolveKeyArray(String s){return new String[]{s};}}'
}
shader=(o/'src/main/java/com/hbm/render/util/ObjUtil.java').read_text();a0=shader.index('public static void renderWithIcon(');a1=shader.index('if(shadow)',a0);b=shader.index('for(int i = 0;',a1)
stubs['OriginalDecorationOracle.java']='''final class OriginalDecorationOracle {static final class Normal {final double xCoord,yCoord,zCoord;Normal(double[] v){xCoord=v[0];yCoord=v[1];zCoord=v[2];}}static final class Color {int value=0xffffffff;void setColorOpaque_F(float r,float g,float b){int c=Math.max(0,Math.min(255,(int)(r*255F)));value=0xff000000|(c<<16)|(c<<8)|c;}}static int color(double[] n,boolean shadow,boolean whole){Normal normal=new Normal(n);Color tes=new Color();'''+shader[a1:b]+'''return tes.value;}}'''
with tempfile.TemporaryDirectory(prefix='algae-native-') as temp:
 out=Path(temp);files=[]
 for rel,content in stubs.items():
  f=out/rel;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(content);files.append(str(f))
 f=out/'AlgaeFilmNativeTest.java';f.write_text((t/'algae-film-native-test.java.in').read_text());files.append(str(f))
 for rel in prod:files.append(str(s/rel));print('PRODUCTION',rel,hashlib.sha256((s/rel).read_bytes()).hexdigest(),flush=True)
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',temp]+files,check=True)
 jar=out/'algae-resources.jar'
 with zipfile.ZipFile(jar,'w') as z:
  for rel in ['models/blocks/algae_film.obj','textures/blocks/algae_film.png','blockstates/tile.algae_film.json']:
   f=r/'src/main/resources/assets/hbm'/rel;z.write(f,'assets/hbm/'+rel)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-cp',temp+':'+a.classpath,'AlgaeFilmNativeTest',str(r/'src/main/resources'),str(o/'src/main/resources/assets/hbm/models/blocks/algae_film.obj'),str(jar)],cwd=temp,check=True)
