#!/usr/bin/env python3
"""Bounded native five-renderer proof. Original files are pinned; OBJ oracle is independent."""
import argparse,hashlib,json,os,re,subprocess,tempfile,zipfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--original-root',type=Path);p.add_argument('--legacy-client',type=Path,help='Optional cached original client; never downloaded');a=p.parse_args()
r=Path(__file__).resolve().parents[2];s=r/'src/main/java';t=r/'porting/tests';o=a.original_root or r.parent/'ntm-original-pristine'
m=json.loads((t/'atlas-obj-model-native-original-manifest.json').read_text())
for f,want in m['sha256'].items():
 got=hashlib.sha256((o/f).read_bytes()).hexdigest();assert got==want, 'Changed original source/asset: '+f
for f in m['assets']:
 rel='src/main/resources/assets/hbm/'+f;assert (r/rel).read_bytes()==(o/rel).read_bytes(), 'Changed native source asset: '+f
print('PINNED ORIGINAL SOURCE/ASSETS',len(m['sha256']),'UNCHANGED NATIVE ASSETS',len(m['assets']),flush=True)
for alias in ['BrownBarrel','YellowBarrel','RedBarrel']:
 source=(o/('src/main/java/com/hbm/blocks/generic/'+alias+'.java')).read_text()
 assert re.search(r'public int getRenderType\(\)\s*\{\s*return BlockFluidBarrel.renderID;\s*\}',source), 'Original shared barrel renderer route changed: '+alias
def method(text,name):
 matches=list(re.finditer(r'(?:public|private) (?:static )?(?:void|boolean) '+name+r'\([^)]*\)\s*\{',text));assert matches,name
 q=matches[-1];end=q.end();d=1
 while d:d+=(text[end]=='{')-(text[end]=='}');end+=1
 return text[q.end():end-1]
def original(n):return (o/('src/main/java/com/hbm/render/block/Render'+n+'.java')).read_text()
pipe=method(original('Pipe'),'renderWorldBlock');pipe=pipe[pipe.index('float rot ='):pipe.index('tessellator.addTranslation(-x')].replace('pipe.rType','shape')
cap=method(original('Capacitor'),'renderWorldBlock');cap=cap[cap.index('float flip ='):cap.index('tessellator.addTranslation(-x')];cap=re.sub(r'int meta = world.getBlockMetadata\(x, y, z\);','',cap);cap=re.sub(r'tessellator.addTranslation\([^;]+;','',cap)
icons={'iiconTop':0,'iiconSide':1,'pipe.frameIcon':2,'pipe.meshIcon':3,'capacitor.iconTop':0,'capacitor.iconSide':1,'capacitor.iconBottom':2,'capacitor.iconInnerTop':3,'capacitor.iconInnerSide':4}
def calls(text):
 return re.sub(r'ObjUtil.renderPartWithIcon\(\(HFRWavefrontObject\) ResourceManager\.(\w+), "([^"]+)", ([\w.]+), tessellator, ([^,]+), ([^,]+), true\);',lambda z:'out.add(new Layer("'+z[1]+'","'+z[2]+'",'+str(icons[z[3]])+','+z[4]+','+z[5]+'));',text)
shader=(o/'src/main/java/com/hbm/render/util/ObjUtil.java').read_text()
whole=method(shader,'renderWithIcon');whole=whole[whole.index('if(shadow)'):whole.index('for(int i = 0;')]
part=method(shader,'renderPartWithIcon');part=part[part.index('if(shadow || hasColor)'):part.index('for(int i = 0;')]
# Exact original item group/order and GL transform facts are pinned and checked, not taken from native definitions.
for n in ['Pipe','Capacitor']:
 inv=method(original(n),'renderInventoryBlock')
 assert not re.search(r'GL11.gl(?:Translated|Scaled|Scalef|Rotated)',inv)
 group_call=r'ObjUtil.renderPartWithIcon\(\(HFRWavefrontObject\) ResourceManager\.(\w+), "([^"]+)", ([\w.]+), tessellator,'
 assert re.findall(group_call,inv)==re.findall(group_call,method(original(n),'renderWorldBlock')), 'Original item/world group/material order differs: '+n
for n,translate,scale in [('Barrel','-0.5',None),('Battery','-0.625','3.0D'),('AntennaTop','-0.5','0.75F')]:
 inv=method(original(n),'renderInventoryBlock');assert 'GL11.glTranslated(0, '+translate+', 0);' in inv
 if scale:assert re.search(r'GL11.glScale[df]\('+re.escape(scale)+r', '+re.escape(scale)+r', '+re.escape(scale)+r'\);',inv)
oracle='''import java.util.*;
final class OriginalAtlasOracle {
 static final class Layer {final String asset,group;final int sprite;final float yaw,pitch;Layer(String a,String g,int s,float y,float p){asset=a;group=g;sprite=s;yaw=y;pitch=p;}}
 static List<Layer> layers(String kind,int shape,int meta,boolean item){List<Layer> out=new ArrayList<>();
 if(kind.equals("BlockPipe")){if(item)meta=-1;'''+calls(pipe)+'''}else if(kind.equals("MachineCapacitor")){if(item)meta=-1;'''+calls(cap)+'''}else out.add(new Layer(kind.equals("HEVBattery")?"battery":kind.equals("DecoPoleTop")?"antenna_top":"barrel",null,0,0,0));return out;}
 static final class Normal {final double xCoord,yCoord,zCoord;Normal(double[] v){xCoord=v[0];yCoord=v[1];zCoord=v[2];}}
 static final class Color {int value=0xffffffff;void setColorOpaque(int r,int g,int b){r=Math.max(0,Math.min(255,r));g=Math.max(0,Math.min(255,g));b=Math.max(0,Math.min(255,b));value=0xff000000|(b<<16)|(g<<8)|r;}void setColorOpaque_F(float r,float g,float b){setColorOpaque((int)(r*255F),(int)(g*255F),(int)(b*255F));}}
 static int color(double[] n,boolean shadow,boolean whole){Normal normal=new Normal(n);Color tes=new Color();boolean hasColor=false;int red=255,green=255,blue=255;if(whole){'''+whole+'''}else{'''+part+'''}return tes.value;}
}
'''
collaborators={}
base='''import net.minecraft.block.Block;import net.minecraft.block.material.Material;import net.minecraft.block.properties.*;import net.minecraft.block.state.*;
 public class CLASS extends Block {public static final PropertyInteger METADATA=PropertyInteger.create("metadata",0,15);EXTRA
 public CLASS(){super(Material.IRON);setDefaultState(blockState.getBaseState().withProperty(METADATA,0));}
 protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,METADATA);}
 public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(METADATA,meta&15);}public int getMetaFromState(IBlockState state){return state.getValue(METADATA);}
 public boolean isOpaqueCube(IBlockState state){return false;}public boolean isFullCube(IBlockState state){return false;}}
'''
for cl,pkg,extra in [('BlockPipe','generic','public int rType;public String side;public String getSideTexture(){return side;}'),('MachineCapacitor','machine','public String material;public String getModelMaterial(){return material;}'),('BlockFluidBarrel','machine',''),('HEVBattery','generic',''),('DecoPoleTop','generic',''),('BrownBarrel','generic',''),('YellowBarrel','generic',''),('RedBarrel','generic','')]:
 collaborators['com/hbm/blocks/'+pkg+'/'+cl+'.java']='package com.hbm.blocks.'+pkg+';'+base.replace('CLASS',cl).replace('EXTRA',extra)
prod=['com/hbm/render/block/Render'+n+'.java' for n in ['Pipe','Capacitor','Barrel','Battery','AntennaTop']]+['com/hbm/render/block/nativeblock/'+n+'.java' for n in ['AtlasObjGeometry','AtlasObjBakedModel','ConnectionQuadFactory','BlockPositionLightQuad','ThermalModelTransforms']]+['com/hbm/main/AtlasObjBlockModelHandler.java','com/hbm/lib/RefStrings.java','com/hbm/render/mesh/ObjMesh.java','com/hbm/render/mesh/ObjMeshParser.java']
exe='.exe' if os.name=='nt' else ''
if not (a.java_home/'bin'/('javac'+exe)).is_file():a.java_home=a.java_home.parent
with tempfile.TemporaryDirectory(prefix='atlas-obj-model-native-') as tmp:
 out=Path(tmp);files=[]
 for rel,content in collaborators.items():
  assert rel.startswith('com/hbm/');f=out/rel;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(content);files.append(str(f))
 for rel in prod:print('PRODUCTION',rel,hashlib.sha256((s/rel).read_bytes()).hexdigest(),flush=True);files.append(str(s/rel))
 for name,content in [('OriginalAtlasOracle',oracle),('AtlasObjModelNativeTest',(t/'atlas-obj-model-native-test.java.in').read_text()),('LegacyAtlasRotationOracleTest',(t/'atlas-obj-model-native-legacy-rotation-test.java.in').read_text())]:
  f=out/(name+'.java');f.write_text(content);files.append(str(f))
 subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',tmp]+files,check=True)
 if a.legacy_client is not None:
  got=hashlib.sha256(a.legacy_client.read_bytes()).hexdigest();assert got=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce','Unrecognized original client';print('PINNED ORIGINAL CLIENT',got,flush=True)
  subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-Djava.awt.headless=true','-cp',os.pathsep.join([tmp,a.classpath]),'LegacyAtlasRotationOracleTest',str(a.legacy_client)],cwd=tmp,check=True)
 jar=out/'atlas-family-resources.jar';resources=r/'src/main/resources'
 with zipfile.ZipFile(jar,'w') as z:
  for asset in m['assets']+['blockstates/'+owner['name']+'.json' for owner in m['owners']]:
   f=resources/'assets/hbm'/asset;z.write(f,f.relative_to(resources).as_posix())
 subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-Djava.awt.headless=true','-Xmx768m','-cp',os.pathsep.join([tmp,a.classpath]),'AtlasObjModelNativeTest',str(resources),str(jar),str(o/'src/main/resources'),str(t/'atlas-obj-model-native-original-manifest.json')],cwd=tmp,check=True)
