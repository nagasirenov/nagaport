"""Generate pure recordings of exact supplied-original callbacks, not Minecraft API substitutes."""
import json,re,hashlib
from pathlib import Path

def body(source,name):
 start=source.index('public '+('boolean' if name=='renderWorldBlock' else 'void')+' '+name);start=source.index('{',start)+1;end=start;depth=1
 while depth:depth+=(source[end]=='{')-(source[end]=='}');end+=1
 return source[start:end-1]

def generate(root):
 raw=(Path(root)/'porting/tests/universal-original-geometry.json').read_text();inputs=json.loads(raw)
 for source in inputs.values():assert hashlib.sha256(source['source'].encode()).hexdigest()==source['sha256']
 out='''import java.util.*;import com.hbm.util.EnumUtil;
final class LegacyUniversalGeometryOracle {
 enum EnumWoodStructure {ROOF,SCAFFOLD,CEILING}
 enum Direction {NEG_Y,POS_Y,NEG_Z,POS_Z,NEG_X,POS_X}
 static final class Library {static final Direction NEG_X=Direction.NEG_X,POS_X=Direction.POS_X,NEG_Z=Direction.NEG_Z,POS_Z=Direction.POS_Z;}
 static boolean simple;
 static final class Shape {final double[] bounds;final double offset;Shape(double[] bounds,double offset){this.bounds=bounds.clone();this.offset=offset;}}
 static final class Matrix {static double y;static void glPushMatrix(){}static void glPopMatrix(){}static void glRotatef(float a,float x,float y,float z){}static void glTranslatef(float x,float y,float z){if(x==0&&z==0)Matrix.y+=y;}}
 static final class Recorder {static Recorder INSTANCE;final List<Shape> result=new ArrayList<>();double[] bounds={0,0,0,1,1,1};Recorder setWorld(OracleWorld w){return this;}void setRenderBounds(double... bounds){this.bounds=bounds;}void setRenderBoundsFromBlock(Owner block){this.bounds=block.bounds;}void renderStandardBlock(Owner b,int x,int y,int z){result.add(new Shape(bounds,Matrix.y));}static void renderStandardInventoryBlock(Owner b,int m,Recorder r){r.renderStandardBlock(b,0,0,0);}}
 static class Owner {double[] bounds={0,0,0,1,1,1};boolean solid;Owner(){}Owner(boolean s){solid=s;}void setBlockBounds(double... b){bounds=b;}boolean isOpaqueCube(){return solid;}boolean isNormalCube(){return solid;} }
 static final class OracleWorld {final Owner owner;final int metadata,mask;final boolean wood;OracleWorld(Owner o,int meta,int mask,boolean wood){owner=o;metadata=meta;this.mask=mask;this.wood=wood;}Owner getBlock(int x,int y,int z){if(x==0&&y==0&&z==0)return owner;int dir=x<0?4:x>0?5:y<0?0:y>0?1:z<0?2:3;return (mask&1<<dir)!=0?(wood?owner:new Owner(true)):new Owner(false);}int getBlockMetadata(int x,int y,int z){return x==0&&y==0&&z==0||getBlock(x,y,z)==owner?metadata:(metadata+1)&15;}}
 static List<Shape> build(int kind,int meta,int mask,boolean inventory,boolean simpleMode){simple=simpleMode;Matrix.y=0;Recorder.INSTANCE=new Recorder();Recorder r=Recorder.INSTANCE;switch(kind){
'''
 for i,name in enumerate(['BlockSandbags','BlockBarrier','BlockWoodStructure','BlockRebar','RBMKMiniPanelBase']):
  out+=f'case {i}:{{{name} b=new {name}();OracleWorld w=new OracleWorld(b,meta,mask,{str(i==2).lower()});'
  if i==0:out+='b.setBlockBoundsBasedOnState(w,0,0,0);'
  out+='if(inventory)b.renderInventoryBlock(b,meta,0,r);else b.renderWorldBlock(w,0,0,0,b,0,r);break;}\n'
 out+='default:throw new AssertionError(kind);}return r.result;}\n'
 for name in ['BlockSandbags','BlockBarrier','BlockWoodStructure','BlockRebar','RBMKMiniPanelBase']:
  source=next(v['source'] for k,v in inputs.items() if k.endswith('/'+name));out+='static final class '+name+' extends Owner {\n'
  methods=['renderInventoryBlock','renderWorldBlock']+(['setBlockBoundsBasedOnState'] if name=='BlockSandbags' else [])
  for method in methods:
   b=body(source,method);b=re.sub(r'/\*.*?\*/','',b,flags=re.S)
   b=re.sub(r'^\s*Tessellator tessellator[^\n]*\n','\n',b,flags=re.M);b=re.sub(r'^\s*tessellator\.[^\n]*\n','\n',b,flags=re.M)
   for old,new in [('GL11.','Matrix.'),('RenderBlocksNT','Recorder'),('RenderBlocks','Recorder'),('Block ','Owner '),('ClientConfig.RENDER_REBAR_SIMPLE.get()','simple')]:b=b.replace(old,new)
   if method=='renderInventoryBlock':sig='void renderInventoryBlock(Owner block,int meta,int modelId,Object renderBlocks)'
   elif method=='renderWorldBlock':sig='boolean renderWorldBlock(OracleWorld world,int x,int y,int z,Owner block,int modelId,Object renderBlocks)'
   else:sig='void setBlockBoundsBasedOnState(OracleWorld world,int x,int y,int z)'
   out+=sig+'{'+b+'}\n'
  out+='}\n'
 return out+'}\n'

def face_oracle(root):
 inputs=json.loads((Path(root)/'porting/tests/universal-original-geometry.json').read_text());source=inputs['render/util/RenderBlocksNT']['source']
 out='''import java.util.*;import net.minecraft.client.renderer.texture.TextureAtlasSprite;
final class LegacyUniversalFaceOracle {
 double renderMinX,renderMinY,renderMinZ,renderMaxX,renderMaxY,renderMaxZ;
 boolean field_152631_f,flipTexture,renderFromInside,enableAO;
 int uvRotateEast,uvRotateSouth,brightnessTopLeft,brightnessBottomLeft,brightnessBottomRight,brightnessTopRight;
 float colorRedTopLeft,colorGreenTopLeft,colorBlueTopLeft,colorRedBottomLeft,colorGreenBottomLeft,colorBlueBottomLeft,colorRedBottomRight,colorGreenBottomRight,colorBlueBottomRight,colorRedTopRight,colorGreenTopRight,colorBlueTopRight;
 TextureAtlasSprite overrideBlockTexture;boolean hasOverrideBlockTexture(){return false;}
 static final class Draw {static final Draw instance=new Draw();final List<double[]> vertices=new ArrayList<>();void addVertexWithUV(double x,double y,double z,double u,double v){vertices.add(new double[]{x,y,z,u,v});}void setColorOpaque_F(float r,float g,float b){}void setBrightness(int b){}}
 static List<double[]> build(double[] bounds,TextureAtlasSprite sprite,int side){LegacyUniversalFaceOracle r=new LegacyUniversalFaceOracle();r.renderMinX=bounds[0];r.renderMinY=bounds[1];r.renderMinZ=bounds[2];r.renderMaxX=bounds[3];r.renderMaxY=bounds[4];r.renderMaxZ=bounds[5];Draw.instance.vertices.clear();if(side==2)r.renderFaceZNeg(null,0,0,0,sprite);else if(side==5)r.renderFaceXPos(null,0,0,0,sprite);else throw new AssertionError(side);return new ArrayList<>(Draw.instance.vertices);}
'''
 for name in ['renderFaceZNeg','renderFaceXPos']:
  start=source.index('public void '+name);start=source.index('{',start)+1;end=start;depth=1
  while depth:depth+=(source[end]=='{')-(source[end]=='}');end+=1
  b=source[start:end-1].replace('Tessellator','Draw')
  out+='void '+name+'(Object block,double x,double y,double z,TextureAtlasSprite icon){'+b+'}\n'
 return out+'}\n'
