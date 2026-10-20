import java.lang.reflect.*;import java.net.*;import java.io.*;import java.util.*;
import com.hbm.world.WorldBlockState;import com.hbm.blocks.*;import com.hbm.blocks.generic.*;
import net.minecraft.block.*;import net.minecraft.block.material.Material;import net.minecraft.block.state.*;import net.minecraft.init.*;import net.minecraft.util.math.*;import net.minecraft.world.*;import net.minecraft.world.chunk.*;import net.minecraft.world.chunk.storage.*;
/** Genuine complete native owners / inheritance roots against actual original classfiles. Not a full target lifecycle proof. */
public class RegisteredWriteTest extends WorldWriteTest {
 public enum Kind { ZERO, ONE, TWO, THREE }
 static Block[] owned;
 static class Old implements AutoCloseable {
  URLClassLoader l;Class<?> world,block,chunk,storage;Object w,stone;Object[] current={null};Object[] owners=new Object[5];Method write,read,meta,shortWrite;int calls;
  Old(String jar,String ownersDir,String forge)throws Exception {
   l=new URLClassLoader(new URL[]{new File(ownersDir).toURI().toURL(),new File(jar).toURI().toURL(),new File(forge).toURI().toURL()},RegisteredWriteTest.class.getClassLoader());Class.forName("kl",true,l).getMethod("b").invoke(null);
   world=Class.forName("ahb",true,l);block=Class.forName("aji",true,l);chunk=Class.forName("apx",true,l);storage=Class.forName("apz",true,l);
   w=U.allocateInstance(Class.forName("proof.OriginalFlagWorld",true,l));field(world,"t").set(w,Class.forName("aqo",true,l).getMethod("a",int.class).invoke(null,0));field(world,"E").setBoolean(w,true);field(world,"C").set(w,Class.forName("qi",true,l).newInstance());field(world,"u").set(w,new ArrayList<>());w.getClass().getField("events").set(w,new StringBuilder());
   Class<?> provider=Class.forName("apu",true,l);Object proxy=java.lang.reflect.Proxy.newProxyInstance(l,new Class[]{provider},(p,m,a)->{if(m.getReturnType()==chunk){calls++;return current[0];}if(m.getReturnType()==boolean.class)return false;throw new AssertionError(m.toString());});field(world,"v").set(w,proxy);
   write=world.getMethod("d",int.class,int.class,int.class,block,int.class,int.class);shortWrite=world.getMethod("b",int.class,int.class,int.class,block);read=world.getMethod("a",int.class,int.class,int.class);meta=world.getMethod("e",int.class,int.class,int.class);stone=block.getMethod("e",int.class).invoke(null,1);
   Class<?> material=Class.forName("awt",true,l);Object rock=field(material,"e").get(null);Object registry=field(block,"c").get(null);Method register=registry.getClass().getMethod("a",int.class,String.class,Object.class);
   String[] names={"blocks/generic/BlockGeneric","blocks/generic/BlockNoDrop","blocks/generic/BlockDecoCT"};for(int i=0;i<3;i++)owners[i]=Class.forName("prooflegacy."+names[i].replace('/','.'),true,l).getConstructor(material).newInstance(rock);
   owners[3]=Class.forName("prooflegacy.blocks.BlockEnumMulti",true,l).getConstructor(material,Class.class,boolean.class,boolean.class).newInstance(rock,Kind.class,true,true);
   owners[4]=Class.forName("akx",true,l).getConstructor(material).newInstance(rock);
   for(int i=0;i<5;i++)register.invoke(registry,3000+i,"proof:registered_"+i,owners[i]);
  }
  void seed(int x,int y,int z)throws Exception {current[0]=chunk.getConstructor(world,int.class,int.class).newInstance(w,x>>4,z>>4);if(y>=0&&y<256){Object st=storage.getConstructor(int.class,boolean.class).newInstance(y&~15,true);storage.getMethod("a",int.class,int.class,int.class,block).invoke(st,x&15,y&15,z&15,stone);Array.set(field(chunk,"u").get(current[0]),y>>4,st);}calls=0;}
  boolean write(int i,int x,int y,int z,int m,int f)throws Exception{return (Boolean)write.invoke(w,x,y,z,owners[i],m,f);}
  int meta(int x,int y,int z)throws Exception{return (Integer)meta.invoke(w,x,y,z);}
  public void close()throws Exception{l.close();}
 }
 static NativeWorld fresh(int x,int y,int z,boolean remote){NativeWorld w=new NativeWorld(remote);Chunk c=new Chunk(w,x>>4,z>>4);w.chunks.put(ChunkPos.asLong(x>>4,z>>4),c);if(y>=0&&y<256){ExtendedBlockStorage st=new ExtendedBlockStorage(y&~15,true);st.set(x&15,y&15,z&15,Blocks.STONE.getDefaultState());c.getBlockStorageArray()[y>>4]=st;}w.requests=0;return w;}
 static void storage(Old o)throws Exception {
  int[][] positions={{0,64,0},{-17,0,16},{29999999,255,-30000000},{-30000000,64,29999999},{30000000,64,0},{-30000001,64,0},{0,64,30000000},{0,64,-30000001},{0,-1,0},{0,256,0},{Integer.MIN_VALUE,64,Integer.MAX_VALUE}};
  for(int i=0;i<5;i++)for(int m=0;m<(i==2||i==3?16:1);m++)for(int[]p:positions)for(int flags=0;flags<8;flags++){
   int x=p[0],y=p[1],z=p[2];Block b=owned[i];IBlockState s=b.getStateFromMeta(m);check(s.getBlock()==b&&b.getMetaFromState(s)==m,"root/complete owner roundtrip");for(int n=0;n<8;n++)check(s==b.getStateFromMeta(m),"stable immutable conversion");
   o.seed(x,y,z);NativeWorld w=fresh(x,y,z,true);boolean old=o.write(i,x,y,z,m,flags),now=WorldBlockState.write(w,x,y,z,s,flags);check(old==now&&now==valid(x,y,z),"actual original/native changed write");check((o.calls>0)==valid(x,y,z)&&(w.requests>0)==valid(x,y,z),"original/native bounds before chunk lookup");
   if(valid(x,y,z)){check(o.read.invoke(o.w,x,y,z)==o.owners[i]&&o.meta(x,y,z)==m,"original exact owner and metadata stored");check(w.getBlockState(new BlockPos(x,y,z))==s,"native real Chunk state stored");check(!o.write(i,x,y,z,m,flags)&&!WorldBlockState.write(w,x,y,z,s,flags),"canonical no-change returns");if(m==0)check(!(Boolean)o.shortWrite.invoke(o.w,x,y,z,o.owners[i]),"original four argument overload metadata0 flags3");}
  }
  // Raw 16 is intentionally outside the selected metadata domain, even for a nibble codec.
  o.seed(0,64,0);check(o.write(3,0,64,0,16,3)&&o.meta(0,64,0)==0,"original raw16 stores0");check(o.write(3,0,64,0,16,3),"raw16 repeated write remains changed, so no blanket masking equivalence");
 }
 static class FlagWorld extends NativeWorld {
  int added;Block expected;FlagWorld(boolean remote){super(remote);}public void notifyNeighborsRespectDebug(BlockPos p,Block b,boolean observers){events.append('n');check(observers&&b==Blocks.STONE,"original old owner notified");}
  public void scheduleUpdate(BlockPos p,Block b,int ticks){added++;check(p.equals(new BlockPos(0,64,0))&&b==expected&&ticks==2,"actual falling added callback schedules exact block/position/delay");}
 }
 static void callbackFlags(Old o)throws Exception{
  for(int i=0;i<5;i++)for(boolean populated:new boolean[]{false,true})for(boolean remote:new boolean[]{false,true})for(int f=0;f<8;f++){
   FlagWorld w=new FlagWorld(remote);w.expected=owned[i];Chunk c=new Chunk(w,0,0);w.chunks.put(0L,c);field(Chunk.class,"ticked").setBoolean(c,populated);c.setTerrainPopulated(populated);c.setLightPopulated(populated);ExtendedBlockStorage st=new ExtendedBlockStorage(64,true);st.set(0,0,0,Blocks.STONE.getDefaultState());c.getBlockStorageArray()[4]=st;
   check(WorldBlockState.write(w,0,64,0,owned[i].getStateFromMeta(0),f),"real native World.write result");String expected=((f&2)!=0&&populated&&(!remote||(f&4)==0)?"u":"")+(remote?"":((f&1)!=0?"n":"o"));check(w.events.toString().equals(expected),"real native write flags/callback order "+i+"/"+remote+"/"+f+":"+w.events);check(w.added==(!remote&&i==4?1:0),"real falling added callback vs tile-free inherited no-op");
   o.seed(0,64,0);field(o.world,"E").setBoolean(o.w,remote);for(String n:new String[]{"m","k","l"})field(o.chunk,n).setBoolean(o.current[0],populated);StringBuilder oldEvents=(StringBuilder)o.w.getClass().getField("events").get(o.w);oldEvents.setLength(0);o.w.getClass().getField("scheduled").setInt(o.w,0);o.w.getClass().getField("expected").set(o.w,o.owners[i]);check(o.write(i,0,64,0,0,f),"real original flags write result");check(oldEvents.toString().equals(expected.replace("o","")),"actual original flags differ only native observer notification");check(o.w.getClass().getField("scheduled").getInt(o.w)==w.added,"actual original/native falling add callback schedule count");
  }
 }
 static void roots()throws Exception {
  Block[] exact=RegisteredConstructionSlice.initialize();java.util.Set<Block> seen=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<Block,Boolean>());
  for(Block b:exact){check(seen.add(b),"verbatim constructors return distinct actual owners");check(b.getStateFromMeta(0).getBlock()==b&&b.getMetaFromState(b.getStateFromMeta(0))==0,"verbatim ModBlocks construction preserves zero state identity");NativeWorld w=fresh(0,64,0,true);check(WorldBlockState.write(w,0,64,0,b.getStateFromMeta(0),3)&&w.getBlockState(new BlockPos(0,64,0)).getBlock()==b,"exact constructed field real native storage");}
  check(exact.length==16,"sixteen exact constructor projections, not complete ModBlocks initialization");

  for(Block b:owned)check(!b.hasTileEntity(b.getDefaultState()),"representatives are tile-free; crashed bomb tile lifecycle deliberately not represented");
  for(int i:new int[]{0,1,4}){check(owned[i].getClass().getMethod("getStateFromMeta",int.class).getDeclaringClass()==Block.class,"default-only codec is actual native Block");check(owned[i].getClass().getMethod("getMetaFromState",IBlockState.class).getDeclaringClass()==Block.class,"default-only encode is actual native Block");}
  check(owned[3].getClass().getMethod("getStateFromMeta",int.class).getDeclaringClass()==BlockMulti.class,"multi uses production codec root");
  for(int m=0;m<16;m++)check(owned[3].getMetaFromState(owned[3].getStateFromMeta(m))==m,"upper half and crashed metadata5 not creative-index normalized");
 }
 public static void main(String[]a)throws Exception {Bootstrap.register();owned=new Block[]{new BlockGeneric(Material.ROCK),new BlockNoDrop(Material.ROCK),new BlockDecoCT(Material.IRON),new BlockEnumMulti(Material.ROCK,Kind.class,true,true),new BlockFalling(Material.ROCK)};try(Old o=new Old(a[0],a[1],a[2])){storage(o);callbackFlags(o);}roots();System.out.println("PASS: "+checks+" genuine original/native owner/root state, real World/Chunk/storage, all flags, falling callback, bounds and return assertions; incomplete target lifecycles NOT certified");}
}
