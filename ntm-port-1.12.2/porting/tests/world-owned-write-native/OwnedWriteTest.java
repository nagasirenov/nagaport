import java.lang.reflect.*;import java.net.*;import java.io.*;import java.util.*;
import com.hbm.world.WorldBlockState;import com.hbm.blocks.generic.*;import com.hbm.render.icon.*;
import net.minecraft.block.*;import net.minecraft.block.material.Material;import net.minecraft.block.state.*;import net.minecraft.init.*;import net.minecraft.entity.EntityLivingBase;import net.minecraft.entity.passive.EntityPig;import net.minecraft.item.ItemStack;import net.minecraft.util.math.*;import net.minecraft.world.chunk.*;import net.minecraft.world.chunk.storage.*;
public class OwnedWriteTest extends WorldWriteTest {
 static Block[] owned;
 static class Old implements AutoCloseable{
  URLClassLoader l;Class<?> world,block,chunk,storage,living,entity;Object w,stone;Object[] current={null};Object[] owners=new Object[3];Method write,read,meta,shortWrite,place;int calls;
  Old(String jar,String ownersDir,String forge)throws Exception{
   l=new URLClassLoader(new URL[]{new File(ownersDir).toURI().toURL(),new File(jar).toURI().toURL(),new File(forge).toURI().toURL()},OwnedWriteTest.class.getClassLoader());Class.forName("kl",true,l).getMethod("b").invoke(null);
   world=Class.forName("ahb",true,l);block=Class.forName("aji",true,l);chunk=Class.forName("apx",true,l);storage=Class.forName("apz",true,l);living=Class.forName("sv",true,l);entity=Class.forName("sa",true,l);
   w=U.allocateInstance(Class.forName("bjf",true,l));field(world,"t").set(w,Class.forName("aqo",true,l).getMethod("a",int.class).invoke(null,0));field(world,"E").setBoolean(w,true);field(world,"C").set(w,Class.forName("qi",true,l).newInstance());field(world,"u").set(w,new ArrayList<>());
   Class<?> provider=Class.forName("apu",true,l);Object proxy=java.lang.reflect.Proxy.newProxyInstance(l,new Class[]{provider},(p,m,a)->{if(m.getReturnType()==chunk){calls++;return current[0];}if(m.getReturnType()==boolean.class)return false;throw new AssertionError(m.toString());});field(world,"v").set(w,proxy);
   write=world.getMethod("d",int.class,int.class,int.class,block,int.class,int.class);shortWrite=world.getMethod("b",int.class,int.class,int.class,block);read=world.getMethod("a",int.class,int.class,int.class);meta=world.getMethod("e",int.class,int.class,int.class);stone=block.getMethod("e",int.class).invoke(null,1);
   Class<?> material=Class.forName("awt",true,l);Object registry=field(block,"c").get(null);Method register=registry.getClass().getMethod("a",int.class,String.class,Object.class);
   String[] names={"BlockRedBrick","DecoSteelPoles","DecoPoleTop"};for(int i=0;i<3;i++){owners[i]=Class.forName("prooflegacy."+names[i],true,l).getConstructor(material).newInstance(field(material,"e").get(null));register.invoke(registry,3000+i,"proof:"+names[i].toLowerCase(),owners[i]);}
  }
  void seed(int x,int y,int z)throws Exception {current[0]=chunk.getConstructor(world,int.class,int.class).newInstance(w,x>>4,z>>4);if(y>=0&&y<256){Object st=storage.getConstructor(int.class,boolean.class).newInstance(y&~15,true);storage.getMethod("a",int.class,int.class,int.class,block).invoke(st,x&15,y&15,z&15,stone);Array.set(field(chunk,"u").get(current[0]),y>>4,st);}calls=0;}
  boolean write(int i,int x,int y,int z,int m,int f)throws Exception{return (Boolean)write.invoke(w,x,y,z,owners[i],m,f);}
  int meta(int x,int y,int z)throws Exception{return (Integer)meta.invoke(w,x,y,z);}
  public void close()throws Exception{l.close();}
 }
 static NativeWorld fresh(int x,int y,int z){NativeWorld w=new NativeWorld(true);Chunk c=new Chunk(w,x>>4,z>>4);w.chunks.put(ChunkPos.asLong(x>>4,z>>4),c);if(y>=0&&y<256){ExtendedBlockStorage st=new ExtendedBlockStorage(y&~15,true);st.set(x&15,y&15,z&15,Blocks.STONE.getDefaultState());c.getBlockStorageArray()[y>>4]=st;}w.requests=0;return w;}
 static void storage(Old o)throws Exception{
  int[][] positions={{0,64,0},{-17,0,16},{29999999,255,-30000000},{-30000000,64,29999999},{30000000,64,0},{-30000001,64,0},{0,64,30000000},{0,64,-30000001},{0,-1,0},{0,256,0},{Integer.MIN_VALUE,64,Integer.MAX_VALUE}};
  for(int i=0;i<3;i++)for(int m=0;m<16;m++)for(int[]p:positions)for(int flags:new int[]{0,2,3,4,7}){
   int x=p[0],y=p[1],z=p[2];Block b=owned[i];IBlockState s=b.getStateFromMeta(m);check(s.getBlock()==b&&b.getMetaFromState(s)==m,"actual owner all nibble roundtrip");for(int n=0;n<8;n++)check(s==b.getStateFromMeta(m),"stable immutable conversion");
   o.seed(x,y,z);NativeWorld w=fresh(x,y,z);boolean old=o.write(i,x,y,z,m,flags),now=WorldBlockState.write(w,x,y,z,s,flags);check(old==now&&now==valid(x,y,z),"actual original/native changed write");check((o.calls>0)==valid(x,y,z)&&(w.requests>0)==valid(x,y,z),"original/native bounds before chunk lookup");
   if(valid(x,y,z)){check(o.read.invoke(o.w,x,y,z)==o.owners[i]&&o.meta(x,y,z)==m,"original custom owner and metadata stored");check(w.getBlockState(new BlockPos(x,y,z))==s,"native actual custom state stored");check(!o.write(i,x,y,z,m,flags)&&!WorldBlockState.write(w,x,y,z,s,flags),"canonical no-change returns");Block alias=b;check(!WorldBlockState.write(w,x,y,z,alias.getStateFromMeta(m),flags),"aliased owner does not collapse states");}
  }
  o.seed(0,64,0);check(o.write(0,0,64,0,16,3)&&o.meta(0,64,0)==0,"legacy raw16 stores0");check(o.write(0,0,64,0,16,3),"legacy repeated raw16 is NOT canonical unchanged");
 }
 static IIcon icon(final String name){return new IIcon(){public String getIconName(){return name;}public int getIconWidth(){return 16;}public int getIconHeight(){return 16;}public float getMinU(){return 0;}public float getMaxU(){return 1;}public float getInterpolatedU(double u){return(float)u/16;}public float getMinV(){return 0;}public float getMaxV(){return 1;}public float getInterpolatedV(double v){return(float)v/16;}};}
 static void icons(Old o)throws Exception{
  Map<Object,String> names=new IdentityHashMap<>();Class<?> register=Class.forName("rg",true,o.l),icon=Class.forName("rf",true,o.l);
  Object r=java.lang.reflect.Proxy.newProxyInstance(o.l,new Class[]{register},(p,m,a)->{Object result=java.lang.reflect.Proxy.newProxyInstance(o.l,new Class[]{icon},(q,n,b)->{throw new AssertionError(n);});names.put(result,(String)a[0]);return result;});o.owners[0].getClass().getMethod("a",register).invoke(o.owners[0],r);
  BlockRedBrick b=(BlockRedBrick)owned[0];b.registerBlockIcons(OwnedWriteTest::icon);Method getter=o.owners[0].getClass().getMethod("a",int.class,int.class);
  for(int m=0;m<16;m++)for(int side=-2;side<9;side++)check(b.getIcon(side,m).getIconName().equals(names.get(getter.invoke(o.owners[0],side,m))),"exact original brick icon identity for metadata/side");
  check(b.getItemDropped(b.getStateFromMeta(6),new Random(0),10)==null,"no drop remains no drop");
 }
 static void placement(Old o)throws Exception{
  EntityLivingBase player=(EntityLivingBase)U.allocateInstance(EntityPig.class);Object oldPlayer=U.allocateInstance(Class.forName("wo",true,o.l));Method piston=Class.forName("app",true,o.l).getMethod("a",o.world,int.class,int.class,int.class,o.living);
  Method compute=BlockRedBrick.class.getDeclaredMethod("determinePlacementOrientation",BlockPos.class,EntityLivingBase.class);compute.setAccessible(true);
  double[] dx={-2.000001,-2,-1.999999,0,0.5,1.999999,2,2.000001,Double.NaN};double[] height={-2,-1.82,0,0.18,Math.nextUp(0.18),3};float[] yaws={-720,-225,-180,-135,-90,-45,0,45,90,135,180,225,720,Float.NaN,Float.POSITIVE_INFINITY};
  for(int origin:new int[]{0,16777217,29999999,-30000000})for(double d:dx)for(double dy:height)for(float yaw:yaws){
   BlockPos pos=new BlockPos(origin,64,-origin);player.posX=origin+d;player.posY=64+dy;player.posZ=-origin;player.rotationYaw=yaw;field(o.entity,"s").setDouble(oldPlayer,player.posX);field(o.entity,"t").setDouble(oldPlayer,player.posY);field(o.entity,"u").setDouble(oldPlayer,player.posZ);field(o.entity,"y").setFloat(oldPlayer,yaw);field(o.entity,"L").setFloat(oldPlayer,0);
   int expected=(Integer)piston.invoke(null,o.w,pos.getX(),pos.getY(),pos.getZ(),oldPlayer);check((Integer)compute.invoke(null,pos,player)==expected,"actual old piston float/corner orientation vs native feet coordinates");
  }
  for(float offset:new float[]{0,1.62F,0.5F})for(double y:new double[]{-100,0,64,65,100}){
   BlockPos p=new BlockPos(0,64,0);field(o.entity,"s").setDouble(oldPlayer,0);field(o.entity,"u").setDouble(oldPlayer,0);field(o.entity,"t").setDouble(oldPlayer,y+(double)offset);field(o.entity,"L").setFloat(oldPlayer,offset);field(o.entity,"y").setFloat(oldPlayer,90);player.posX=0;player.posZ=0;player.posY=y;player.rotationYaw=90;
   check(((Integer)compute.invoke(null,p,player)).intValue()==((Integer)piston.invoke(null,o.w,0,64,0,oldPlayer)).intValue(),"legacy offset-relative Y translates to native feet-relative Y");
  }
  for(int i:new int[]{0,1})for(float yaw:yaws)for(double y:new double[]{60,64,67}){
   BlockPos pos=new BlockPos(0,64,0);player.posX=0;player.posY=y;player.posZ=0;player.rotationYaw=yaw;field(o.entity,"s").setDouble(oldPlayer,0);field(o.entity,"t").setDouble(oldPlayer,y);field(o.entity,"u").setDouble(oldPlayer,0);field(o.entity,"y").setFloat(oldPlayer,yaw);field(o.entity,"L").setFloat(oldPlayer,0);
   o.seed(0,64,0);o.write(i,0,64,0,6,3);o.owners[i].getClass().getMethod("a",o.world,int.class,int.class,int.class,o.living,Class.forName("add",true,o.l)).invoke(o.owners[i],o.w,0,64,0,oldPlayer,null);
   NativeWorld w=fresh(0,64,0);WorldBlockState.write(w,0,64,0,owned[i].getStateFromMeta(6),3);owned[i].onBlockPlacedBy(w,pos,w.getBlockState(pos),player,ItemStack.EMPTY);check(owned[i].getMetaFromState(w.getBlockState(pos))==o.meta(0,64,0),"actual original/native placement callback state storage");
  }
 }
 static void builderIdentity()throws Exception{
  for(Block b:owned){
   check(b.getClass().getMethod("setTranslationKey",String.class).getDeclaringClass()==Block.class,"identity builder is inherited native Block implementation");
   check(b.setTranslationKey("proof")==b&&b.setHardness(5F)==b&&b.setResistance(10F)==b&&b.setCreativeTab(null)==b,"all exact ModBlocks builder methods return original concrete owner");
   check(b.getDefaultState().getBlock()==b,"identity builder preserves constructed state owner");
  }
 }
 static void actualOwnerCallback(){
  EntityLivingBase player;try{player=(EntityLivingBase)U.allocateInstance(EntityPig.class);}catch(Exception e){throw new RuntimeException(e);}player.posX=20;player.posY=64;player.rotationYaw=90;
  NativeWorld w=fresh(0,64,0);BlockPos p=new BlockPos(0,64,0);WorldBlockState.write(w,0,64,0,owned[1].getStateFromMeta(6),3);owned[0].onBlockPlacedBy(w,p,owned[0].getStateFromMeta(0),player,ItemStack.EMPTY);
  check(w.getBlockState(p).getBlock()==owned[1]&&owned[1].getMetaFromState(w.getBlockState(p))==5,"placement metadata follows actual stored state owner, not callback receiver");
  w.requests=0;owned[0].onBlockPlacedBy(w,new BlockPos(30000000,64,0),owned[0].getStateFromMeta(0),player,ItemStack.EMPTY);check(w.requests==0,"placement outside old domain never looks up chunks");
 }
 static class Subclass extends DecoSteelPoles{Subclass(){super(Material.IRON);}public IBlockState getStateFromMeta(int m){return super.getStateFromMeta(15-m);}}
 static void subclasses(){Block b=new Subclass();NativeWorld w=fresh(0,64,0);IBlockState s=b.getStateFromMeta(4);check(WorldBlockState.write(w,0,64,0,s,2)&&w.getBlockState(new BlockPos(0,64,0))==s&&b.getMetaFromState(s)==11,"native virtual subclass conversion retains actual state, never caller-class recast");}
 public static void main(String[]a)throws Exception{Bootstrap.register();owned=new Block[]{new BlockRedBrick(Material.ROCK),new DecoSteelPoles(Material.IRON),new DecoPoleTop(Material.IRON)};try(Old o=new Old(a[0],a[1],a[2])){storage(o);icons(o);placement(o);actualOwnerCallback();subclasses();builderIdentity();}System.out.println("PASS: "+checks+" actual original/native owned state/storage/bounds/returns/aliases/icons/placement/subclass assertions");}
}
