import com.hbm.inventory.SlotCraftingOutput;
import com.hbm.inventory.container.*;
import com.hbm.inventory.recipes.anvil.*;
import com.mojang.authlib.GameProfile;
import java.util.*;
import java.lang.reflect.*;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.stats.StatBase;
import net.minecraft.world.*;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.*;
import probe.*;
/** Complete selected production callbacks on actual native parents; owner collaborators are explicit fixtures. */
public final class SlotTakeNativeTest {
 static int checks;
 static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s+" events="+ProbeState.events);}
 static void events(String...s){check(ProbeState.events.equals(Arrays.asList(s)),"event sequence "+Arrays.toString(s));}
 static class W extends World {
  W(){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),false);provider.setWorld(this);}
  protected IChunkProvider createChunkProvider(){return null;}
  protected boolean isChunkLoaded(int x,int z,boolean allow){return false;}
 }
 static class P extends EntityPlayer {
  int crafts,drops;
  P(){super(new W(),new GameProfile(new UUID(0,1),"SlotTest"));}
  public boolean isSpectator(){return false;}
  public boolean isCreative(){return false;}
  public void addStat(StatBase stat,int amount){crafts++;ProbeState.craftAmount=amount;ProbeState.events.add("craft:"+amount);}
  public EntityItem dropItem(ItemStack s,boolean random){drops++;ProbeState.events.add("drop");return null;}
 }
 static ItemStack stack(int n){return new ItemStack(Items.APPLE,n);}
 static void buffer(SlotCraftingOutput s,int n)throws Exception {Field f=SlotCraftingOutput.class.getDeclaredField("craftBuffer");f.setAccessible(true);f.setInt(s,n);}
 static int buffer(SlotCraftingOutput s)throws Exception {Field f=SlotCraftingOutput.class.getDeclaredField("craftBuffer");f.setAccessible(true);return f.getInt(s);}
 static class OneSlot extends Container {
  OneSlot(Slot s){int index=s.slotNumber;for(int i=0;i<index;i++)addSlotToContainer(new Slot(new InventoryBasic("padding",false,1),0,0,0));addSlotToContainer(s);}
  public boolean canInteractWith(EntityPlayer p){return true;}
 }
 static void click(P p,Slot s,ItemStack contents,ClickType type){
  s.inventory.setInventorySlotContents(s.getSlotIndex(),contents);p.inventory.setItemStack(ItemStack.EMPTY);ProbeState.reset();
  OneSlot c=new OneSlot(s);c.slotClick(s.slotNumber,0,type,p);
 }
 static void crafting(P p)throws Exception {
  TestInventory inv=new TestInventory("output",1);SlotCraftingOutput output=new SlotCraftingOutput(p,inv,0,0,0);P caller=new P();
  for(ItemStack value:new ItemStack[]{stack(3),stack(0),ItemStack.EMPTY}){
   buffer(output,7);ProbeState.reset();int owner=p.crafts,other=caller.crafts;
   check(((Slot)output).onTake(caller,value)==value,"crafting result identity including empty/zero");
   events("craft:7","achievement","dirty:output");check(buffer(output)==0,"craft buffer resets after callbacks");
   check(p.crafts==owner+1&&caller.crafts==other,"original captured owner remains craft recipient");
  }
  buffer(output,9);ProbeState.reset();ProbeState.failAchievement=true;
  try{output.onTake(caller,stack(1));throw new AssertionError("must throw");}catch(IllegalStateException expected){}
  events("craft:9","achievement");check(buffer(output)==9,"exception preserves original not-yet-reset buffer");
  for(ClickType mode:new ClickType[]{ClickType.PICKUP,ClickType.THROW}){
   buffer(output,4);click(p,output,stack(2),mode);
   check(ProbeState.achievements==1,"real native Container dispatches crafting once "+mode);
   int craft=ProbeState.events.indexOf("craft:4"),achievement=ProbeState.events.indexOf("achievement");
   check(craft>=0&&achievement==craft+1&&ProbeState.events.get(achievement+1).equals("dirty:output"),"craft->achievement->native dirty ordering "+mode);
   check(buffer(output)==0,"native dispatched crafting buffer reset");
  }
 }
 static void anvil(P p)throws Exception {
  ContainerAnvil c=new ContainerAnvil();Slot out=c.output(p);ItemStack value=stack(1);
  for(int absent:new int[]{0,1}){
   c.input.setInventorySlotContents(0,stack(8));c.input.setInventorySlotContents(1,stack(9));c.input.nullSlot=absent;
   AnvilRecipes.recipes.clear();AnvilRecipes.recipes.add(new AnvilSmithingRecipe("must-not-run",0,2,3));ProbeState.reset();
   check(out.onTake(p,value)==value,"anvil null input early return identity");events("craft:0","achievement","dirty:output");
  }
  c.input.nullSlot=-1;
  c.input.clear();AnvilRecipes.recipes.clear();AnvilRecipes.recipes.add(new AnvilSmithingRecipe("empty-boundary",-1,0,0));ProbeState.reset();out.onTake(p,value);
  check(ProbeState.events.contains("match:empty-boundary"),"EXCLUSION: native EMPTY inputs pass preserved null guard; real recipe empty handling not certified");
  for(int match:new int[]{-1,0,1}){
   c.input.setInventorySlotContents(0,stack(8));c.input.setInventorySlotContents(1,stack(9));c.updates=0;
   AnvilRecipes.recipes.clear();AnvilRecipes.recipes.add(new AnvilSmithingRecipe("first",-1,9,9));AnvilRecipes.recipes.add(new AnvilSmithingRecipe("selected",match,2,3));AnvilRecipes.recipes.add(new AnvilSmithingRecipe("later",0,9,9));
   if(match==-1)AnvilRecipes.recipes.remove(2);
   ProbeState.reset();check(out.onTake(p,value)==value,"anvil return identity "+match);
   check(ProbeState.events.subList(0,5).equals(Arrays.asList("craft:0","achievement","dirty:output","match:first","match:selected")),"anvil original leading side-effect order");
   if(match==-1){check(c.updates==0&&c.input.getStackInSlot(0).getCount()==8&&c.input.getStackInSlot(1).getCount()==9,"no match does not consume/update");}
   else {check(c.updates==1,"one smithing update");check(c.input.getStackInSlot(0).getCount()==(match==0?6:5)&&c.input.getStackInSlot(1).getCount()==(match==0?6:7),"swapped quantity argument forwarding");check(!ProbeState.events.contains("match:later"),"first match retains original early exit");check(ProbeState.events.get(ProbeState.events.size()-1).equals("smithing"),"update last");}
  }
  c.updates=0;ProbeState.reset();Slot smith=c.new SmithingSlot();check(smith.onTake(p,value)==value,"smithing input result identity");events("dirty:input","smithing","smithing");check(c.updates==2,"preserve native virtual onSlotChanged then explicit update, no deduplication");
  AnvilRecipes.recipes.clear();click(p,out,stack(2),ClickType.PICKUP);check(ProbeState.achievements==1,"native Container reaches anonymous anvil output override");
  c.updates=0;click(p,smith,stack(2),ClickType.PICKUP);check(c.updates>=2,"native Container reaches named smithing override");
 }
 static void armor(P p){
  ContainerArmorTable c=new ContainerArmorTable();Slot out=c.output();ItemStack armor=stack(1);ItemStack retained=new ItemStack(Items.STICK,4);
  c.upgrades.setInventorySlotContents(0,stack(3));c.upgrades.setInventorySlotContents(1,retained);c.upgrades.setInventorySlotContents(8,stack(5));ProbeState.reset();
  check(out.onTake(p,armor)==armor,"armor result identity");check(ProbeState.events.get(0).equals("dirty:armor"),"armor superclass precedes domain checks");
  check(Collections.frequency(ProbeState.events,"applicable:armor")==9,"all nine original armor slots tested");
  check(c.upgrades.getStackInSlot(0)==ItemStack.EMPTY&&c.upgrades.getStackInSlot(8)==ItemStack.EMPTY,"applicable mods removed");check(c.upgrades.getStackInSlot(1)==retained,"nonapplicable mod unchanged");
  c.armor.setInventorySlotContents(0,armor);Slot upgrade=c.new UpgradeSlot(6);ProbeState.reset();
  ItemStack upgradeTaken=stack(2);check(upgrade.onTake(p,upgradeTaken)==upgradeTaken,"upgrade result identity");events("dirty:upgrades","remove:6");check(ProbeState.lastStack==armor&&ProbeState.lastIndex==6,"remove exact armor/index");
  c.armor.clear();ProbeState.reset();upgrade.onTake(p,upgradeTaken);check(ProbeState.lastStack==ItemStack.EMPTY,"EXCLUSION: unchanged armor collaborator receives native EMPTY, not null");
  click(p,out,armor,ClickType.PICKUP);check(Collections.frequency(ProbeState.events,"applicable:armor")==9,"native Container reaches anonymous armor output override");
  click(p,upgrade,stack(2),ClickType.PICKUP);check(Collections.frequency(ProbeState.events,"remove:6")==1,"native Container reaches named upgrade override");
 }
 static void weapon(P p){
  ContainerWeaponTable c=new ContainerWeaponTable();Slot out=c.output();ItemStack gun=stack(1);ItemStack[] refs=new ItemStack[7];
  for(int i=0;i<7;i++){refs[i]=new ItemStack(i%2==0?Items.APPLE:Items.STICK,i+1);c.mods.setInventorySlotContents(i,refs[i]);}
  c.index=3;ProbeState.reset();check(out.onTake(p,gun)==gun,"weapon result identity");check(ProbeState.events.subList(0,2).equals(Arrays.asList("dirty:gun","install:3")),"native dirty precedes install");
  check(ProbeState.lastStack==gun&&ProbeState.lastIndex==3&&ProbeState.lastMods.length==7,"exact weapon and original selected index");
  for(int i=0;i<7;i++){check(ProbeState.lastMods[i]==refs[i],"original mod alias "+i);check(i%2==0?c.mods.getStackInSlot(i)==ItemStack.EMPTY:c.mods.getStackInSlot(i)==refs[i],"selective mod removal "+i);}
  check(Collections.frequency(ProbeState.events,"applicable:weapon:3:false")==7&&c.index==0,"all applicability checks precede final index reset");
  c.index=4;ProbeState.reset();ProbeState.failInstall=true;try{out.onTake(p,gun);throw new AssertionError("must throw");}catch(IllegalStateException expected){}
  events("dirty:gun","install:4");check(c.index==4,"failed install does not reset original index");
  c.gun.setInventorySlotContents(0,gun);Slot mod=c.new ModSlot(5);c.index=2;ProbeState.reset();ItemStack taken=stack(2);
  check(mod.onTake(p,taken)==taken,"mod result identity");events("dirty:mods","install:2");check(ProbeState.lastStack==gun,"refresh install gets stored gun, not taken mod");
  c.gun.nullSlot=0;ProbeState.reset();check(mod.onTake(p,taken)==taken,"null gun refresh result identity");events("dirty:mods");c.gun.nullSlot=-1;
  c.gun.clear();ProbeState.reset();mod.onTake(p,taken);check(ProbeState.lastStack==ItemStack.EMPTY,"EXCLUSION: unchanged weapon refresh collaborator receives native EMPTY, not null");
  c.index=3;click(p,out,gun,ClickType.PICKUP);check(Collections.frequency(ProbeState.events,"install:3")==1&&c.index==0,"native Container reaches anonymous weapon override");
  c.gun.setInventorySlotContents(0,gun);c.index=4;click(p,mod,stack(2),ClickType.PICKUP);check(Collections.frequency(ProbeState.events,"install:4")==1,"native Container reaches named mod override");
 }
 static void callEdges(P p)throws Exception {
  final ItemStack[] seen=new ItemStack[1];final EntityPlayer[] player=new EntityPlayer[1];final int[] count={0};
  Slot target=new Slot(null,0,0,0){@Override public ItemStack onTake(EntityPlayer q,ItemStack s){seen[0]=s;player[0]=q;count[0]++;return ItemStack.EMPTY;}};
  for(Method edge:CallEdges.class.getDeclaredMethods()){
   ItemStack value=stack(2);int n=count[0];edge.invoke(null,target,p,value);
   check(count[0]==n+1&&seen[0]==value&&player[0]==p,"exact invocation edge "+edge.getName());
  }
  check(count[0]==32,"all 32 ordinary edges compile and dispatch once");
 }
 public static void main(String[] args)throws Exception {
  Bootstrap.register();P p=new P();crafting(p);anvil(p);armor(p);weapon(p);callEdges(p);
  System.out.println("PASS "+checks+" native Slot/Container checks: all seven complete callback projections and 32 exact ordinary call edges; quantity accumulation and full owner gameplay excluded");
 }
}
