"""Pure recording oracle from immutable original foundry callback bodies, not a platform substitute."""
import re,json,hashlib
from pathlib import Path
BASELINE='e8970b85e0c8fe46f17348330b3853bb32ffaa0b'
def body(source,name):
 start=source.index('public '+('void' if name=='renderInventoryBlock' else 'boolean')+' '+name);start=source.index('{',start)+1;end=start;depth=1
 while depth:depth+=(source[end]=='{')-(source[end]=='}');end+=1
 return source[start:end-1]
def generate(root):
 text='''import java.awt.Color;import java.util.*;import com.hbm.blocks.machine.FoundryModelState;import com.hbm.lib.ForgeDirection;import com.hbm.lib.Library;import net.minecraft.util.EnumFacing;
final class LegacyFoundryGeometryOracle {
 static final class Face {final double[] bounds,offset;final int side,color,light;final String texture;final boolean mirror;Face(Recorder recorder,int side,double x,double y,double z,String texture){bounds=recorder.bounds.clone();offset=new double[]{x,y,z};this.side=side;this.texture=texture;mirror=recorder.field_152631_f;color=Draw.instance.color;light=Draw.instance.light;}}
 static final class Draw {static final Draw instance=new Draw();int color=0xffffffff,light;void startDrawingQuads(){}void draw(){}void setNormal(float x,float y,float z){}void setBrightness(int light){this.light=light;}void setColorOpaque_F(float r,float g,float b){color=0xff000000|(int)(b*255)<<16|(int)(g*255)<<8|(int)(r*255);}}
 static final class Recorder {final List<Face> faces=new ArrayList<>();double[] bounds={0,0,0,1,1,1};boolean field_152631_f;void setRenderBounds(double...bounds){this.bounds=bounds;}
'''
 for side,name in enumerate(['YNeg','YPos','ZNeg','ZPos','XNeg','XPos']):text+=f' void renderFace{name}(OracleBlock block,double x,double y,double z,String texture){{faces.add(new Face(this,{side},x,y,z,texture));}}\n'
 text+=''' }
 static final class Mat {final int moltenColor;Mat(int color){moltenColor=color;}}
 static final class OracleTile {final int amount,capacity;final Mat type,filter;final boolean closed;OracleTile(FoundryModelState data){amount=data.amount;capacity=data.capacity;type=data.material?new Mat(data.color):null;filter=data.filter?new Mat(0):null;closed=data.closed;}int getCapacity(){return capacity;}boolean isClosed(){return closed;}}
 static final class OracleBlock {String iconTop="Top",iconSide="Side",iconBottom="Bottom",iconInner="Inner",iconLava="Lava",iconSideOutlet="SideOutlet",iconSideUpper="SideUpper",iconSideUpperOutlet="SideUpperOutlet",iconFront="Front",iconLock="Lock",iconFilter="Filter";int colorMultiplier(OracleWorld world,int x,int y,int z){return 0xffffff;}int getMixedBrightnessForBlock(OracleWorld world,int x,int y,int z){return 0x00400030;}boolean canConnectTo(OracleWorld world,int x,int y,int z,ForgeDirection side){return world.data.connects(side.toEnumFacing());}}
 static final class OracleBlocks {static final OracleBlock foundry_tank=new OracleBlock(),foundry_outlet=new OracleBlock();}
 static final class OracleWorld {final FoundryModelState data;final int meta;OracleWorld(FoundryModelState data,int meta){this.data=data;this.meta=meta;}OracleTile getTileEntity(int x,int y,int z){return new OracleTile(data);}int getBlockMetadata(int x,int y,int z){return x==0&&y==0&&z==0?meta:side(x,y,z).getIndex();}EnumFacing side(int x,int y,int z){return EnumFacing.getFacingFromVector(x,y,z);}OracleBlock getBlock(int x,int y,int z){EnumFacing side=side(x,y,z);return data.connects(side)?OracleBlocks.foundry_tank:data.outlet(side)?OracleBlocks.foundry_outlet:null;}}
 static List<Face> build(String kind,boolean inventory,int metadata,FoundryModelState data){Recorder recorder=new Recorder();if(!inventory&&kind.equals("Basin"))recorder.bounds[4]=(double).999F;if(!inventory&&kind.equals("Mold"))recorder.bounds[4]=.5;Draw.instance.color=0xffffffff;Draw.instance.light=0;OracleWorld world=new OracleWorld(data,metadata);OracleBlock block=new OracleBlock();switch(kind){
'''
 for kind in ['Basin','Mold','Channel','Tank','Outlet']:text+=f'case "{kind}":if(inventory){kind}Item(block,metadata,0,recorder);else {kind}World(world,0,0,0,block,0,recorder);break;\n'
 text+='default:throw new AssertionError(kind);}return recorder.faces;}\n'
 for kind in ['Basin','Mold','Channel','Tank','Outlet']:
  relative='src/main/java/com/hbm/render/block/RenderFoundry'+kind+'.java'
  raw=(Path(root)/'porting/tests/native-foundry-light-boundary-manifest.json').read_bytes()
  assert hashlib.sha256(raw).hexdigest()=='90875f86e84121222868eaa7fea537dbb11f4baf6e77c024d8e85365e111780a'
  manifest=json.loads(raw);assert manifest['baseline']==BASELINE
  source=manifest['deleted_files'][relative]['original'].replace('\r\n','\n')
  for inventory,name in [(True,'renderInventoryBlock'),(False,'renderWorldBlock')]:
   b=body(source,name)
   for old,new in [('Tessellator','Draw'),('TileEntityFoundryChannel','OracleTile'),('TileEntityFoundryTank','OracleTile'),('TileEntityFoundryOutlet','OracleTile'),('TileEntity','OracleTile'),('Foundry'+kind,'OracleBlock'),('ModBlocks.','OracleBlocks.'),('EntityRenderer.anaglyphEnable','false')]:b=b.replace(old,new)
   b=b.replace('getOracleTile(', 'getTileEntity(')
   b=re.sub(r'GL11\.glTranslatef\([^;]+\);','// Original inventory centering belongs to caller, not block-local vertices.',b)
   signature='static void '+kind+'Item(OracleBlock block,int metadata,int modelId,Recorder renderer)' if inventory else 'static boolean '+kind+'World(OracleWorld world,int x,int y,int z,OracleBlock block,int modelId,Recorder renderer)'
   text+=signature+'{'+b+'}\n'
 return text+'}\n'
