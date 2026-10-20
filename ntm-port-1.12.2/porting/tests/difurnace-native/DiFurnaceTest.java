import com.hbm.tileentity.machine.TileEntityDiFurnace;
import com.hbm.inventory.recipes.BlastFurnaceRecipes;
import com.hbm.inventory.recipes.BlastFurnaceRecipes.AStack;
import com.hbm.util.Tuple.Triplet;
import com.hbm.items.ModItems;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.storage.*;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.profiler.Profiler;
import java.lang.reflect.*;
import io.netty.buffer.*;
public class DiFurnaceTest {
 static int checks;
 static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static final class W extends World {
  long time=1; boolean extension;
  W(boolean client){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),client);provider.setWorld(this);}
  protected IChunkProvider createChunkProvider(){return null;}
  protected boolean isChunkLoaded(int x,int z,boolean a){return false;}
  public long getTotalWorldTime(){return time;}
  public net.minecraft.block.state.IBlockState getBlockState(BlockPos p){return (extension?Blocks.IRON_BLOCK:Blocks.AIR).getDefaultState();}
 }
 public static final class F extends TileEntityDiFurnace {int dirty; public void markDirty(){dirty++;}}
 static F f(){F f=new F();f.setWorld(new W(false));f.setPos(new BlockPos(10,70,20));return f;}
 static W w(F f){return (W)f.getWorld();}
 static void recipe(int count){BlastFurnaceRecipes.blastFurnaceRecipes.clear();ItemStack out=new ItemStack(Items.IRON_INGOT,count);NBTTagCompound tag=new NBTTagCompound();tag.setString("recipe","preserved");out.setTagCompound(tag);BlastFurnaceRecipes.blastFurnaceRecipes.add(new Triplet<Object,Object,ItemStack>(new AStack(Items.COAL),new AStack(Items.DIAMOND),out));}
 static F ready(){F f=f();f.slots[0]=new ItemStack(Items.COAL,2);f.slots[1]=new ItemStack(Items.DIAMOND,2);f.fuel=1000;return f;}
 static void air(boolean a){com.hbm.dim.trait.CBT_Atmosphere x=new com.hbm.dim.trait.CBT_Atmosphere();x.breathable=a;com.hbm.dim.CelestialBody.atmosphere=x;}
 static final class Callback extends Item {int calls;ItemStack seen;public ItemStack getContainerItem(ItemStack s){calls++;seen=s;check(s.getCount()==0,"depleted callback");return new ItemStack(Items.BUCKET);}}
 public static void main(String[]args)throws Exception {
  Bootstrap.register();TileEntityDiFurnace.class.getName();net.minecraft.tileentity.TileEntity.register("difurnace_probe",F.class);
  for(Field x:ModItems.class.getFields())if(x.getType()==Item.class)x.set(null,new Item());
  air(true);recipe(3);
  F f=ready();for(int i=0;i<399;i++){f.update();check(f.progress==i+1,"400 tick progress");}ItemStack a=f.slots[0],b=f.slots[1];f.update();check(f.progress==0&&f.fuel==600,"exact400 energy");check(f.slots[0]==a&&a.getCount()==1&&f.slots[1]==b&&b.getCount()==1,"partial input aliases");check(f.slots[3].getCount()==3&&f.slots[3].getTagCompound().getString("recipe").equals("preserved"),"recipe output count tag");ItemStack output=f.slots[3];for(int i=0;i<400;i++)f.update();check(f.slots[0]==null&&f.slots[1]==null,"last inputs cleared");check(a.getCount()==1&&b.getCount()==1,"original decr returns last-unit alias without mutation");check(f.slots[3]==output&&output.getCount()==6,"output alias merge");check(f.fuel==200,"second400 energy");
  f=ready();w(f).extension=true;for(int i=0;i<134;i++)f.update();check(f.progress==2&&f.fuel==866&&f.slots[3].getCount()==3,"extension3 progression residual");check(f.smokeCalls==134*7,"extension smoke routing");
  f=ready();f.slots[3]=new ItemStack(Items.IRON_INGOT,62);f.progress=399;f.update();check(f.progress==0&&f.fuel==1000&&f.slots[0].getCount()==2,"capacity blocks without energy consumption");f.slots[3].setCount(61);f.progress=399;f.update();check(f.slots[3].getCount()==64&&f.fuel==999,"capacity exact64");
  f=ready();f.slots[3]=new ItemStack(Items.GOLD_INGOT);check(!f.canProcess(),"mismatch output");f.slots[3]=null;f.fuel=0;check(!f.canProcess(),"fuel required");
  for(Item fuel:new Item[]{Items.COAL,Items.BLAZE_ROD,Items.BLAZE_POWDER,Items.LAVA_BUCKET})for(int count:new int[]{1,2,64}) {
   f=f();ItemStack original=new ItemStack(fuel,count);f.slots[2]=original;f.update();int power=fuel==Items.COAL?200:fuel==Items.BLAZE_ROD?1000:fuel==Items.BLAZE_POWDER?300:12800;check(f.fuel==power,"fuel formula");check(original.getCount()==count-1,"fuel decrement once");check(count>1?f.slots[2]==original:fuel==Items.LAVA_BUCKET?f.slots[2].getItem()==Items.BUCKET:f.slots[2]==null,"fuel alias/residue");
  }
  Callback cb=new Callback();ModItems.lignite=cb;f=f();ItemStack original=new ItemStack(cb);f.slots[2]=original;f.update();check(cb.calls==1&&cb.seen==original&&f.slots[2].getItem()==Items.BUCKET&&f.fuel==150,"callback actual depleted identity");
  f=f();f.fuel=12601;f.slots[2]=new ItemStack(Items.COAL);f.update();check(f.fuel==12601&&f.slots[2].getCount()==1,"fuel capacity no overfill");f.fuel=12600;f.update();check(f.fuel==12800&&f.slots[2]==null,"fuel capacity exact");
  air(false);f=f();f.slots[2]=new ItemStack(Items.COAL);f.update();check(f.fuel==0&&f.slots[2].getCount()==1,"oxygen coal blocked");f.slots[2]=new ItemStack(Items.LAVA_BUCKET);f.update();check(f.fuel==12800&&f.slots[2].getItem()==Items.BUCKET,"lava oxygen independent");air(true);
  for(int n:new int[]{Integer.MIN_VALUE,-1,0}) {
   recipe(3);f=ready();f.slots[0]=new ItemStack(Items.COAL,n);f.progress=399;f.update();check(f.progress==0&&f.fuel==1000&&f.slots[3]==null,"approved empty input");
   f=ready();f.slots[3]=new ItemStack(Items.GOLD_INGOT,n);f.progress=399;f.update();check(f.slots[3].getItem()==Items.IRON_INGOT&&f.slots[3].getCount()==3,"approved empty output slot");
   f=f();f.slots[2]=new ItemStack(Items.COAL,n);f.update();check(f.fuel==0,"approved empty fuel");
   recipe(n);f=ready();f.progress=399;f.update();check(f.slots[3]==null&&f.fuel==1000,"approved empty recipe result");
  }
  // Positive quantities whose addition overflows remain Java int arithmetic.
  recipe(Integer.MAX_VALUE);f=ready();f.slots[3]=new ItemStack(Items.IRON_INGOT,1);original=f.slots[3];f.progress=399;check(f.canProcess(),"original overflowing capacity sum");f.update();Field raw=net.minecraftforge.fml.relauncher.ReflectionHelper.findField(ItemStack.class,"stackSize","field_77994_a");check(f.slots[3]==original&&raw.getInt(original)==Integer.MIN_VALUE,"raw overflowing result retained without clamp");check(raw.getName().equals(System.getProperty("expectedQuantityField","stackSize")),"actual runtime field layout");System.out.println("NATIVE_QUANTITY_FIELD "+raw.getName());
  // Aliasing remains observable; do not copy or independently normalize input slots.
  BlastFurnaceRecipes.blastFurnaceRecipes.clear();BlastFurnaceRecipes.blastFurnaceRecipes.add(new Triplet<Object,Object,ItemStack>(new AStack(Items.COAL),new AStack(Items.COAL),new ItemStack(Items.IRON_INGOT,3)));
  f=ready();original=new ItemStack(Items.COAL,2);f.slots[0]=original;f.slots[1]=original;f.progress=399;f.update();check(f.slots[0]==original&&f.slots[1]==null&&original.getCount()==1&&f.slots[3].getCount()==3,"shared input alias original sequential decrement");
  BlastFurnaceRecipes.blastFurnaceRecipes.clear();BlastFurnaceRecipes.blastFurnaceRecipes.add(new Triplet<Object,Object,ItemStack>(new AStack(Items.COAL),new AStack(Items.DIAMOND),new ItemStack(Items.COAL,3)));
  f=ready();original=new ItemStack(Items.COAL,1);f.slots[0]=original;f.slots[3]=original;f.progress=399;f.update();check(f.slots[0]==original&&f.slots[3]==original&&original.getCount()==3,"output-input alias grows before input consumption");
  // Repaired native caches must not bypass the original raw capacity test.
  recipe(3);f=ready();original=new ItemStack(Items.IRON_INGOT,64,65536);original.setItemDamage(0);f.slots[3]=original;f.progress=399;check(!original.isEmpty()&&original.getCount()==0,"stale repaired fixture");check(!f.canProcess(),"raw64 plus3 still rejects");f.update();check(raw.getInt(original)==64&&f.fuel==1000&&f.slots[0].getCount()==2,"stale repaired no overstack or consumption");
  Method quantity=TileEntityDiFurnace.class.getDeclaredMethod("quantity",ItemStack.class);quantity.setAccessible(true);
  for(int count:new int[]{Integer.MIN_VALUE,-1,0,1,64,Integer.MAX_VALUE}) {
   ItemStack q=new ItemStack(Items.COAL,count);check(((Integer)quantity.invoke(null,q))==count,"raw reader full int");
   q=new ItemStack(Items.COAL,count,65536);q.setItemDamage(0);check(((Integer)quantity.invoke(null,q))==count,"raw reader stale cache");
  }
  check(((Integer)quantity.invoke(null,ItemStack.EMPTY))==1,"reader does not mask EMPTY raw1");try{quantity.invoke(null,new Object[]{null});throw new AssertionError("reader null accepted");}catch(InvocationTargetException expected){check(expected.getCause() instanceof NullPointerException,"reader null exception");}
  // Genuine getOutput calls a recipe object's getZ at each lookup, so changing
  // results exercise the second production lookup without a fake return hook.
  for(final int secondCount:new int[]{0,-1,5}) {
   final int[] lookups={0};BlastFurnaceRecipes.blastFurnaceRecipes.clear();
   BlastFurnaceRecipes.blastFurnaceRecipes.add(new Triplet<Object,Object,ItemStack>(new AStack(Items.COAL),new AStack(Items.DIAMOND),new ItemStack(Items.IRON_INGOT,3)) {
    @Override public ItemStack getZ(){lookups[0]++;return new ItemStack(Items.IRON_INGOT,lookups[0]==2?secondCount:3);}
   });
   f=ready();f.progress=399;f.update();check(f.fuel==999&&f.progress==0,"second lookup preserves update timing");
   check(secondCount<=0?f.slots[3]==null&&f.slots[0].getCount()==2&&f.slots[1].getCount()==2:f.slots[3].getCount()==5&&f.slots[0].getCount()==1&&f.slots[1].getCount()==1,"changing second result");check(lookups[0]==3,"no additional recipe lookups");
  }
  final int[] calls={0};BlastFurnaceRecipes.blastFurnaceRecipes.clear();
  BlastFurnaceRecipes.blastFurnaceRecipes.add(new Triplet<Object,Object,ItemStack>(new AStack(Items.COAL){@Override public boolean matchesRecipe(ItemStack x,boolean ignore){calls[0]++;return calls[0]!=2&&super.matchesRecipe(x,ignore);}},new AStack(Items.DIAMOND),new ItemStack(Items.IRON_INGOT,3)));
  f=ready();f.progress=399;f.update();check(f.slots[3]==null&&f.slots[0].getCount()==2&&f.slots[1].getCount()==2&&f.fuel==999,"second null result no ingredient mutation");
  recipe(3);f=ready();w(f).time=20;f.update();check(f.polluteCalls==1&&f.amount==1F/25F,"pollution formula");f=ready();w(f).extension=true;w(f).time=20;f.update();check(f.amount==(1F/25F)*3,"extension pollution");
  f=ready();f.setWorld(new W(true));f.progress=399;f.update();check(f.fuel==1000&&f.progress==399&&f.slots[3]==null,"client no mutation");
  f=ready();f.progress=123;f.sideFuel=2;f.sideUpper=3;f.sideLower=4;NBTTagCompound nbt=f.writeToNBT(new NBTTagCompound());F loaded=f();loaded.readFromNBT(nbt);check(loaded.fuel==1000&&loaded.progress==123&&loaded.slots[0].getCount()==2&&loaded.sideFuel==2,"positive NBT lifecycle");ByteBuf buf=Unpooled.buffer();f.serialize(buf);loaded=f();loaded.deserialize(buf);check(loaded.fuel==1000&&loaded.progress==123&&loaded.sideLower==4,"network roundtrip");buf.release();
  check(f.getDiFurnaceProgressScaled(100)==30&&f.getPowerRemainingScaled(128)==10,"GUI formulas");
  System.out.println("PASS "+checks+" complete native DiFurnace logic checks");
 }
}
