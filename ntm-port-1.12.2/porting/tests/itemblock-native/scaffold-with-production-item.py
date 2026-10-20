#!/usr/bin/env python3
"""Compile complete scaffold production classes; run original-source-derived native oracle."""
import argparse,hashlib,json,os,re,subprocess,tempfile,zipfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--module',type=Path,required=True);p.add_argument('--source-root',type=Path);a=p.parse_args()
root_classes=tempfile.TemporaryDirectory(prefix='itemblock-model-root-');subprocess.run([__import__('sys').executable,str(Path(__file__).with_name('run.py')),'--module',str(a.module),'--source-root',str(a.source_root or a.module),'--java-home',str(a.java_home),'--classpath',a.classpath,'--classes-dir',root_classes.name,'--compile-only'],check=True);a.classpath=root_classes.name+os.pathsep+a.classpath
r=a.module.resolve();o=r.parent/'ntm-original-pristine';t=r/'porting/tests';s=r/'src/main/java'
m=json.loads((t/'scaffold-native-scope.json').read_text())
for f,h in m['production'].items():assert hashlib.sha256((r/f).read_bytes()).hexdigest()==h,f
for f,h in m['original'].items():assert hashlib.sha256((o/f).read_bytes()).hexdigest()==h,f
for f in m['assets']:assert (r/f).read_bytes()==(o/f).read_bytes(),f
print('Pinned original sources and unchanged assets verified',flush=True)
def body(text,name):
 q=list(re.finditer(r'(?:public|private) (?:static )?[\w<>]+ '+name+r'\([^)]*\)\s*\{',text))[-1];assert q,name
 end=q.end();d=1
 while d:d+=(text[end]=='{')-(text[end]=='}');end+=1
 return text[q.end():end-1]
render=(o/'src/main/java/com/hbm/render/block/RenderScaffoldBlock.java').read_text();block=(o/'src/main/java/com/hbm/blocks/generic/BlockScaffold.java').read_text();shader=(o/'src/main/java/com/hbm/render/util/ObjUtil.java').read_text()
transform=body(render,'renderWorldBlock');transform=transform[transform.index('float ox ='):transform.index('tessellator.addTranslation(ox')].replace('int meta = world.getBlockMetadata(x, y, z);','')
assert 'GL11.glTranslated(0, -0.5, 0);' in body(render,'renderInventoryBlock')
bounds=body(block,'setBlockBoundsBasedOnState').replace('int meta = world.getBlockMetadata(x, y, z);','').replace('this.setBlockBounds','return bounds')
placement=body(block,'onBlockPlacedBy').replace('ForgeDirection placed = ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z));','ForgeDirection placed = ForgeDirection.values()[face];').replace('int meta = stack.getItemDamage();','').replace('player.rotationYaw','yaw').replace('MathHelper.floor_double','net.minecraft.util.math.MathHelper.floor')
placement=re.sub(r'world.setBlockMetadataWithNotify\(x, y, z, ([^,]+), 2\);',r'return \1;',placement)
whole=body(shader,'renderWithIcon');whole=whole[whole.index('if(shadow)'):whole.index('for(int i = 0;')]
oracle='''final class OriginalScaffoldOracle {
 enum ForgeDirection {DOWN,UP,NORTH,SOUTH,WEST,EAST}
 static double[] transform(int meta,boolean item){if(item)return new double[]{0,0,.5,0,.5};int x=0,y=0,z=0;'''+transform+'''return new double[]{rotation,pitch,ox,oy,oz};}
 static double[] bounds(double... x){return x;}static double[] bounds(int meta){'''+bounds+'''}
 static int placement(int face,int meta,float yaw){'''+placement+'''}
 static final class Normal {final double xCoord,yCoord,zCoord;Normal(double[] v){xCoord=v[0];yCoord=v[1];zCoord=v[2];}}
 static final class Color {int value=0xffffffff;void setColorOpaque(int r,int g,int b){r=Math.max(0,Math.min(255,r));g=Math.max(0,Math.min(255,g));b=Math.max(0,Math.min(255,b));value=0xff000000|(b<<16)|(g<<8)|r;}void setColorOpaque_F(float r,float g,float b){setColorOpaque((int)(r*255F),(int)(g*255F),(int)(b*255F));}}
 static int color(double[] n,boolean shadow,boolean whole){Normal normal=new Normal(n);Color tes=new Color();'''+whole+'''return tes.value;}
}
'''
prod=m['production'];exe='.exe' if os.name=='nt' else ''
with tempfile.TemporaryDirectory(prefix='scaffold-native-') as tmp:
 out=Path(tmp);files=[]
 for f in prod:
  if f.endswith('.java'):files.append(str(r/f));print('PRODUCTION',f,hashlib.sha256((r/f).read_bytes()).hexdigest(),flush=True)
 for name,content in [('OriginalScaffoldOracle',oracle),('ScaffoldNativeTest',(t/'scaffold-native-test.java.in').read_text().replace('new ItemBlock(b){public int getMetadata(int damage){return damage;}}.setHasSubtypes(true)', 'new com.hbm.items.block.ItemBlockBase(b)'))]:
  f=out/(name+'.java');f.write_text(content);files.append(str(f))
 subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',tmp]+files,check=True)
 jar=out/'resources.jar'
 with zipfile.ZipFile(jar,'w') as z:
  for f in m['assets']+['src/main/resources/assets/hbm/blockstates/tile.steel_scaffold.json']:
   path=r/f;z.write(path,path.relative_to(r/'src/main/resources').as_posix())
 subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-Djava.awt.headless=true','-Xmx512m','-cp',os.pathsep.join([tmp,a.classpath]),'ScaffoldNativeTest',str(r/'src/main/resources'),str(jar),str(o/'src/main/resources')],cwd=tmp,check=True)
