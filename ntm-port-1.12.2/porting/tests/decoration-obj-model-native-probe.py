#!/usr/bin/env python3
"""Complete new native model sources, original geometry oracle, real Forge routes and light."""
import argparse,hashlib,json,os,re,subprocess,tempfile,zipfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--original-root',type=Path);a=p.parse_args()
r=Path(__file__).resolve().parents[2];s=r/'src/main/java';t=r/'porting/tests';o=a.original_root or r.parent/'ntm-original-pristine'
m=json.loads((t/'decoration-obj-model-native-original-manifest.json').read_text())
for f,want in m['sha256'].items():assert hashlib.sha256((o/f).read_bytes()).hexdigest()==want,'Original changed: '+f
for f in m['assets']:
 rel='src/main/resources/assets/hbm/'+f;assert (r/rel).read_bytes()==(o/rel).read_bytes(),'Asset changed: '+f
print('PASS pinned original source/assets',len(m['sha256']),'unchanged native assets',len(m['assets']),flush=True)
for name in ['SteelBeam','Conserve','Pribris']:
 old=(o/('src/main/java/com/hbm/render/block/Render'+name+'.java')).read_text()
 assert 'GL11.glTranslated(0, -0.5, 0);' in old and 'tessellator.addTranslation(x + 0.5F, y, z + 0.5F);' in old
 assert 'tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));' in old
 assert old.count('ObjUtil.renderWithIcon(')==2 and not re.search(r'GL11.glScale|GL11.glRotate',old)
 assert 'iicon, tessellator, 0, false);' in old and 'iicon, tessellator, 0, true);' in old
print('PASS three exact original renderer transform/light/material routes',flush=True)
shader=(o/'src/main/java/com/hbm/render/util/ObjUtil.java').read_text();a0=shader.index('public static void renderWithIcon(');a1=shader.index('if(shadow)',a0);b=shader.index('for(int i = 0;',a1);whole=shader[a1:b]
oracle='''final class OriginalDecorationOracle {
 static final class Normal {final double xCoord,yCoord,zCoord;Normal(double[] v){xCoord=v[0];yCoord=v[1];zCoord=v[2];}}
 static final class Color {int value=0xffffffff;void setColorOpaque(int r,int g,int b){r=Math.max(0,Math.min(255,r));g=Math.max(0,Math.min(255,g));b=Math.max(0,Math.min(255,b));value=0xff000000|(b<<16)|(g<<8)|r;}void setColorOpaque_F(float r,float g,float b){setColorOpaque((int)(r*255F),(int)(g*255F),(int)(b*255F));}}
 static int color(double[] n,boolean shadow,boolean whole){Normal normal=new Normal(n);Color tes=new Color();'''+whole+'''return tes.value;}
}
'''
base='''import net.minecraft.block.Block;import net.minecraft.block.material.Material;import net.minecraft.block.properties.*;import net.minecraft.block.state.*;
 public class CLASS extends Block {public static final PropertyInteger METADATA=PropertyInteger.create("metadata",0,15);
 public CLASS(){super(Material.IRON);setDefaultState(blockState.getBaseState().withProperty(METADATA,0));}
 protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,METADATA);}
 public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(METADATA,meta&15);}public int getMetaFromState(IBlockState state){return state.getValue(METADATA);}
 public boolean isOpaqueCube(IBlockState state){return false;}public boolean isFullCube(IBlockState state){return false;}}
'''
prod=['com/hbm/render/block/Render'+n+'.java' for n in ['SteelBeam','Conserve','Pribris']]+['com/hbm/render/block/nativeblock/'+n+'.java' for n in ['AtlasObjGeometry','AtlasObjBakedModel','ConnectionQuadFactory','BlockPositionLightQuad','ThermalModelTransforms']]+['com/hbm/main/DecorationObjBlockModelHandler.java','com/hbm/lib/RefStrings.java','com/hbm/render/mesh/ObjMesh.java','com/hbm/render/mesh/ObjMeshParser.java']
exe='.exe' if os.name=='nt' else ''
with tempfile.TemporaryDirectory(prefix='decoration-obj-native-') as tmp:
 out=Path(tmp);files=[]
 for owner in m['owners']:
  cl=owner['owner'];pkg='generic' if not cl.startswith('RBMK') else 'machine.rbmk';f=out/('com/hbm/blocks/'+pkg.replace('.','/')+'/'+cl+'.java');f.parent.mkdir(parents=True,exist_ok=True);f.write_text('package com.hbm.blocks.'+pkg+';'+(('public class '+cl+' extends RBMKDebris {}') if cl.startswith('RBMKDebris') and cl!='RBMKDebris' else base.replace('CLASS',cl)));files.append(str(f))
 for rel in prod:print('PRODUCTION',rel,hashlib.sha256((s/rel).read_bytes()).hexdigest(),flush=True);files.append(str(s/rel))
 for name,content in [('OriginalDecorationOracle',oracle),('DecorationObjModelNativeTest',(t/'decoration-obj-model-native-test.java.in').read_text())]:
  f=out/(name+'.java');f.write_text(content);files.append(str(f))
 subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',tmp]+files,check=True)
 jar=out/'decoration-family-resources.jar';resources=r/'src/main/resources'
 with zipfile.ZipFile(jar,'w') as z:
  for asset in m['assets']+['blockstates/'+owner['name']+'.json' for owner in m['owners']]:
   f=resources/'assets/hbm'/asset;z.write(f,f.relative_to(resources).as_posix())
 subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-Djava.awt.headless=true','-Xmx768m','-cp',os.pathsep.join([tmp,a.classpath]),'DecorationObjModelNativeTest',str(resources),str(jar),str(o/'src/main/resources'),str(t/'decoration-obj-model-native-original-manifest.json')],cwd=tmp,check=True)
