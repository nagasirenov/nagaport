import java.io.*;
import java.net.*;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.block.state.*;
import net.minecraft.init.*;
import net.minecraft.nbt.*;
import net.minecraft.profiler.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.*;
import net.minecraft.world.storage.*;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.registry.*;

/** Exact production constructor/callback projections, real native World/Chunk/state consumers,
 * and separate actual signed 1.7.10 Block/BlockBush method execution. No full-owner claim. */
public class ShapeNativeTest {
 static int checks;
 static double worldRayTolerance;
 static void check(boolean value,String why){checks++;if(!value)throw new AssertionError(why);}
 static void eq(double a,double b,String why){check(Double.doubleToLongBits(a)==Double.doubleToLongBits(b),why+": "+a+" != "+b);}
 static final class NativeWorld extends World {
  final Map<Long,Chunk> chunks=new HashMap<>();
  NativeWorld(){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),false);provider.setWorld(this);chunkProvider=createChunkProvider();}
  static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
  void around(BlockPos p){chunks.clear();for(int x=(p.getX()>>4)-1;x<=(p.getX()>>4)+1;x++)for(int z=(p.getZ()>>4)-1;z<=(p.getZ()>>4)+1;z++){Chunk c=new Chunk(this,x,z);c.markLoaded(true);chunks.put(key(x,z),c);}}
  protected IChunkProvider createChunkProvider(){return new IChunkProvider(){public Chunk getLoadedChunk(int x,int z){return chunks.get(key(x,z));}public Chunk provideChunk(int x,int z){return getLoadedChunk(x,z);}public boolean tick(){return false;}public String makeString(){return "static shape proof";}public boolean isChunkGeneratedAt(int x,int z){return getLoadedChunk(x,z)!=null;}};}
  protected boolean isChunkLoaded(int x,int z,boolean empty){return chunks.containsKey(key(x,z));}
  public boolean checkLight(BlockPos p){return true;}
  void put(BlockPos p,IBlockState s){getChunk(p).setBlockState(p,s);}
 }
 static final class Original implements AutoCloseable {
  final URLClassLoader loader;final Class<?> block,world,vec,aabb,entity;final Object[] owners=new Object[4];
  final Method selected,collision,add,ray,makeVec,makeBox;
  Original(String jar,String classes)throws Exception{
   loader=new URLClassLoader(new URL[]{new File(jar).toURI().toURL(),new File(classes).toURI().toURL()},ShapeNativeTest.class.getClassLoader());
   block=Class.forName("aji",true,loader);world=Class.forName("ahb",true,loader);vec=Class.forName("azw",true,loader);aabb=Class.forName("azt",true,loader);entity=Class.forName("sa",true,loader);
   for(int i=0;i<4;i++)owners[i]=Class.forName("oracle.OldShape"+i,true,loader).newInstance();
   selected=block.getMethod("g",world,int.class,int.class,int.class);collision=block.getMethod("a",world,int.class,int.class,int.class);add=block.getMethod("a",world,int.class,int.class,int.class,aabb,List.class,entity);ray=block.getMethod("a",world,int.class,int.class,int.class,vec,vec);makeVec=vec.getMethod("a",double.class,double.class,double.class);makeBox=aabb.getMethod("a",double.class,double.class,double.class,double.class,double.class,double.class);
  }
  Object invoke(Method m,int i,BlockPos p,Object... tail)throws Exception{Object[] args=new Object[4+tail.length];args[0]=null;args[1]=p.getX();args[2]=p.getY();args[3]=p.getZ();System.arraycopy(tail,0,args,4,tail.length);return m.invoke(owners[i],args);}
  Object vector(Vec3d v)throws Exception{return makeVec.invoke(null,v.x,v.y,v.z);}
  Object box(AxisAlignedBB b)throws Exception{return makeBox.invoke(null,b.minX,b.minY,b.minZ,b.maxX,b.maxY,b.maxZ);}
  double[] values(Object b)throws Exception{if(b==null)return null;double[] result=new double[6];for(int j=0;j<6;j++)result[j]=aabb.getField("abcdef".substring(j,j+1)).getDouble(b);return result;}
  public void close()throws Exception{loader.close();}
 }
 static void bounds(AxisAlignedBB actual,double[] expected,String why){check((actual==null)==(expected==null),why+" null");if(actual==null)return;double[] a={actual.minX,actual.minY,actual.minZ,actual.maxX,actual.maxY,actual.maxZ};for(int j=0;j<6;j++)eq(a[j],expected[j],why+" component "+j);}
 static void ray(RayTraceResult actual,Object old,Original o,String label)throws Exception{
  check((actual==null)==(old==null),label+" hit/null");if(actual==null)return;
  Class<?> c=old.getClass();check(actual.sideHit.getIndex()==c.getField("e").getInt(old),label+" face");
  check(actual.getBlockPos().equals(new BlockPos(c.getField("b").getInt(old),c.getField("c").getInt(old),c.getField("d").getInt(old))),label+" block position");
  Object v=c.getField("f").get(old);point(actual.hitVec.x,o.vec.getField("a").getDouble(v),label+" hit x");point(actual.hitVec.y,o.vec.getField("b").getDouble(v),label+" hit y");point(actual.hitVec.z,o.vec.getField("c").getDouble(v),label+" hit z");
 }
 static void point(double a,double b,String label){if(label.startsWith("actual World"))check(Math.abs(a-b)<=worldRayTolerance,label+" DDA point rounding "+a+" != "+b);else eq(a,b,label);}
 public static void main(String[] args)throws Exception{
  Bootstrap.register();ModMetadata md=new ModMetadata();md.modId="hbm";md.name="Static shape proof";md.version="test";DummyModContainer container=new DummyModContainer(md);Loader.instance().setupTestHarness(container);Loader.instance().setActiveModContainer(container);
  Block[] blocks={new com.hbm.blocks.BlockCrop(Blocks.DIRT,a->true,true),new com.hbm.blocks.generic.BlockMush(Material.PLANTS),new com.hbm.blocks.generic.BlockFallout(Material.SAND),new com.hbm.blocks.fluid.MeltedFlesh(Material.SNOW)};
  for(int i=0;i<4;i++){blocks[i].setRegistryName("hbm","shape_"+i);ForgeRegistries.BLOCKS.register(blocks[i]);}
  NativeWorld w=new NativeWorld();Random random=new Random(0x17101122L);
  try(Original old=new Original(args[0],args[1])){
   for(int i=0;i<4;i++)for(BlockPos p:new BlockPos[]{new BlockPos(8,64,8),new BlockPos(-19,1,-33),new BlockPos(170002,254,-990001)}){
    worldRayTolerance=32*Math.ulp(Math.max(Math.max(Math.abs((double)p.getX()),Math.abs((double)p.getY())),Math.abs((double)p.getZ()))+3)+1e-12;
    Block b=blocks[i];IBlockState state=b.getDefaultState();w.around(p);w.put(p,state);check(w.getBlockState(p)==state,"actual Chunk owner state identity");
    AxisAlignedBB selected=b.getSelectedBoundingBox(state,w,p);bounds(selected,old.values(old.invoke(old.selected,i,p)),"selection exact old instance fields");
    bounds(state.getBoundingBox(w,p).offset(p),old.values(old.invoke(old.selected,i,p)),"native state-local geometry");
    AxisAlignedBB collision=state.getCollisionBoundingBox(w,p);bounds(collision==null?null:collision.offset(p),old.values(old.invoke(old.collision,i,p)),"collision exact old Block/BlockBush/null override");
    check((collision==null)==(i<2),"crop and mushroom remain noncollidable");
    // Native callbacks remain stable across null/foreign arguments and interleaved owner/position queries.
    bounds(b.getBoundingBox(null,null,null),old.values(old.invoke(old.selected,i,BlockPos.ORIGIN)),"no state or position dependence");
    for(int j=0;j<16;j++)bounds(b.getBoundingBox(b.getStateFromMeta(j),w,p),old.values(old.invoke(old.selected,i,BlockPos.ORIGIN)),"all original metadata geometry independent");
    for(int q=0;q<48;q++){
     double x=q<3?selected.minX+(q==0?-1:q==1?0:1)*0.01:p.getX()+random.nextDouble()*2-0.5;
     double y=q<3?selected.maxY+(q==0?0:q==1?-0.000001:0.000001):p.getY()+random.nextDouble()*2-0.5;
     double z=q<3?selected.minZ:p.getZ()+random.nextDouble()*2-0.5;
     AxisAlignedBB query=new AxisAlignedBB(x,y,z,x+0.1,y+0.1,z+0.1);
     List<Object> expected=new ArrayList<>();old.invoke(old.add,i,p,old.box(query),expected,null);
     List<AxisAlignedBB> direct=new ArrayList<>();state.addCollisionBoxToList(w,p,query,direct,null,false);
     check(direct.size()==expected.size(),"actual native state collision list count");for(int j=0;j<direct.size();j++)bounds(direct.get(j),old.values(expected.get(j)),"direct collision list bounds");
     List<AxisAlignedBB> worldHits=w.getCollisionBoxes(null,query);check(worldHits.size()==expected.size(),"actual World collision traversal count");for(int j=0;j<worldHits.size();j++)bounds(worldHits.get(j),old.values(expected.get(j)),"World collision traversal bounds");
    }
    List<Vec3d[]> rays=new ArrayList<>();double cx=(selected.minX+selected.maxX)/2,cy=(selected.minY+selected.maxY)/2,cz=(selected.minZ+selected.maxZ)/2;
    rays.add(new Vec3d[]{new Vec3d(cx,p.getY()+2,cz),new Vec3d(cx,p.getY()-1,cz)});rays.add(new Vec3d[]{new Vec3d(cx,p.getY()-1,cz),new Vec3d(cx,p.getY()+2,cz)});
    rays.add(new Vec3d[]{new Vec3d(p.getX()-1,cy,cz),new Vec3d(p.getX()+2,cy,cz)});rays.add(new Vec3d[]{new Vec3d(p.getX()+2,cy,cz),new Vec3d(p.getX()-1,cy,cz)});
    rays.add(new Vec3d[]{new Vec3d(cx,cy,p.getZ()-1),new Vec3d(cx,cy,p.getZ()+2)});rays.add(new Vec3d[]{new Vec3d(cx,cy,p.getZ()+2),new Vec3d(cx,cy,p.getZ()-1)});
    rays.add(new Vec3d[]{new Vec3d(selected.minX-1,selected.minY-1,selected.minZ-1),new Vec3d(selected.minX,selected.minY,selected.minZ)});
    rays.add(new Vec3d[]{new Vec3d(selected.maxX+1,selected.maxY+1,selected.maxZ+1),new Vec3d(selected.maxX,selected.maxY,selected.maxZ)});
    rays.add(new Vec3d[]{new Vec3d(cx,cy,cz),new Vec3d(cx,cy,cz)});
    for(int j=0;j<300;j++)rays.add(new Vec3d[]{new Vec3d(p.getX()+random.nextDouble()*3-1,p.getY()+random.nextDouble()*3-1,p.getZ()+random.nextDouble()*3-1),new Vec3d(p.getX()+random.nextDouble()*3-1,p.getY()+random.nextDouble()*3-1,p.getZ()+random.nextDouble()*3-1)});
    int rayIndex=0;for(Vec3d[] r:rays){Object expected=old.invoke(old.ray,i,p,old.vector(r[0]),old.vector(r[1]));ray(state.collisionRayTrace(w,p,r[0],r[1]),expected,old,"native state ray vs original Block");if(rayIndex<6||rayIndex>=9)ray(w.rayTraceBlocks(r[0],r[1],false,false,false),expected,old,"actual World ray traversal");rayIndex++;}
    // Above shape is not a full cube; explicit miss rejects full-cube fallbacks.
    Vec3d above0=new Vec3d(p.getX()-1,p.getY()+0.9,p.getZ()+0.5),above1=new Vec3d(p.getX()+2,p.getY()+0.9,p.getZ()+0.5);check(w.rayTraceBlocks(above0,above1,false,false,false)==null,"ray above short shape misses");
    Vec3d top=new Vec3d(cx,p.getY()+2,cz),bottom=new Vec3d(cx,p.getY()-1,cz);check((w.rayTraceBlocks(top,bottom,false,true,false)==null)==(i<2),"native ignore-null-collision flag preserves plant omission");
    check(b.getBoundingBox(state,w,p)==b.getBoundingBox(state,w,p.add(9,0,8)),"immutable owner-local box reused without coordinate leakage");
   }
  }
  check(blocks[0].getTickRandomly()&&blocks[1].getTickRandomly()&&blocks[3].getTickRandomly(),"complete native constructors retain existing random ticks");check(!blocks[2].getTickRandomly(),"original salted identity comparison in constructor unchanged");
  System.out.println("PASS "+checks+" original/native exact geometry, state, World/Chunk collision and ray-trace assertions across 4 genuine scoped owners");
 }
}
