import com.hbm.items.ModItems;
import com.hbm.items.machine.*;
import com.hbm.inventory.material.*;
import com.hbm.crafting.handlers.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.GameData;
import net.minecraft.item.crafting.IRecipe;
public class DynamicCraftingNativeTest {
 static int checks;
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);checks++;System.out.println("PASS "+s);}
 static InventoryCrafting grid(ItemStack... stacks){InventoryCrafting g=new InventoryCrafting(new Container(){public boolean canInteractWith(EntityPlayer p){return true;}},3,3);for(int i=0;i<stacks.length;i++)g.setInventorySlotContents(i,stacks[i]);return g;}
 public static class Events {@SubscribeEvent public void recipes(RegistryEvent.Register<IRecipe> e){ExactDynamicProducers.register();}}
 static Item item(String id,Item item){item.setRegistryName("hbm",id);ForgeRegistries.ITEMS.register(item);return item;}
 static ItemStack rod(ItemRBMKRod item,double enrichment,double poison,double hull,double core){ItemStack s=new ItemStack(item);NBTTagCompound tag=new NBTTagCompound();tag.setDouble("yield",enrichment*item.yield);tag.setDouble("xenon",poison*100);tag.setDouble("hull",hull);tag.setDouble("core",core);s.setTagCompound(tag);return s;}
 public static void main(String[] args){
  Bootstrap.register();ModMetadata md=new ModMetadata();md.modId="hbm";md.name="Dynamic crafting probe";DummyModContainer owner=new DummyModContainer(md);Loader.instance().setupTestHarness(owner);Loader.instance().setActiveModContainer(owner);
  ModItems.ammo_arty=item("ammo_arty",new Item());ModItems.ammo_shell=item("ammo_shell",new Item());
  ModItems.scraps=item("scraps",new ItemScraps());ModItems.nothing=item("nothing",new Item());
  Item pellet=item("pellet",new Item());Item emptyRod=item("empty_rod",new Item());ItemRBMKRod fuel=new ItemRBMKRod();fuel.pellet=pellet;fuel.yield=100;fuel.setContainerItem(emptyRod);item("fuel_rod",fuel);
  ItemRBMKRod noPellet=new ItemRBMKRod();noPellet.yield=100;item("no_pellet",noPellet);
  Mats.matById.put(7,new NTMMaterial(7));
  MinecraftForge.EVENT_BUS.register(new Events());GameData.fireRegistryEvents(id->id.equals(GameData.RECIPES));
  RBMKFuelCraftingHandler r=(RBMKFuelCraftingHandler)ForgeRegistries.RECIPES.getValue(new ResourceLocation("hbm","crafting/special/rbmk_fuel"));
  check(!r.matches(grid(),null)&&r.getCraftingResult(grid()).isEmpty(),"RBMK empty native grid rejected");
  check(!r.matches(grid(new ItemStack(Items.APPLE)),null)&&r.getCraftingResult(grid(new ItemStack(Items.APPLE))).isEmpty(),"RBMK wrong item rejected");
  check(!r.matches(grid(new ItemStack(noPellet)),null)&&r.getCraftingResult(grid(new ItemStack(noPellet))).isEmpty(),"RBMK null pellet rejected");
  double[] enrich={0,0.2,0.200001,0.4,0.6,0.8,0.99};int[] meta={4,4,3,3,2,1,0};
  for(int i=0;i<enrich.length;i++)for(double poison:new double[]{0.4999,0.5}){
   ItemStack input=rod(fuel,enrich[i],poison,49.999,49.999);input.setCount(12);input.setItemDamage(13);InventoryCrafting g=grid(input);
   check(r.matches(g,null),"RBMK ignores input count/metadata with cold rod");ItemStack result=r.getCraftingResult(g);
   check(result.getItem()==pellet&&result.getCount()==8&&result.getMetadata()==meta[i]+(poison>=0.5?5:0),"RBMK exact enrichment/poison metadata "+enrich[i]+"/"+poison);
   check(!result.hasTagCompound()&&r.getCraftingResult(g)!=result,"RBMK fresh untagged eight-pellet result");
  }
  for(double[] heat:new double[][]{{50,20},{20,50},{51,51}}){InventoryCrafting g=grid(rod(fuel,0.5,0,heat[0],heat[1]));check(!r.matches(g,null)&&r.getCraftingResult(g).isEmpty(),"RBMK exact heat cutoff");}
  InventoryCrafting fresh=grid(rod(fuel,0.990001,0,20,20));check(r.matches(fresh,null)&&r.getCraftingResult(fresh).isEmpty(),"original RBMK matches/result asymmetry above 0.99 retained");
  ItemStack defaults=new ItemStack(fuel);check(!defaults.hasTagCompound()&&r.matches(grid(defaults),null)&&defaults.getTagCompound().getDouble("core")==20&&defaults.getTagCompound().getDouble("yield")==100,"actual extracted RBMK default-NBT mutation preserved");
  check(r.getCraftingResult(grid(defaults)).isEmpty(),"new default full-enrichment rod has no pellet result");
  InventoryCrafting two=grid(rod(fuel,0.5,0,20,20),new ItemStack(Items.APPLE));check(!r.matches(two,null)&&r.getCraftingResult(two).isEmpty(),"RBMK second stack rejected");
  InventoryCrafting remaining=grid(rod(fuel,0.5,0,20,20));check(r.getRemainingItems(remaining).get(0).getItem()==emptyRod,"native remaining payload is configured empty fuel hull (not placement parity)");
  ScrapsCraftingHandler s=(ScrapsCraftingHandler)ForgeRegistries.RECIPES.getValue(new ResourceLocation("hbm","crafting/special/scraps"));
  for(int amount:new int[]{2,3,4,71,72,73,Integer.MAX_VALUE}){
   ItemStack input=ItemScraps.create(new Mats.MaterialStack(Mats.matById.get(7),amount),true);input.setCount(64);input.getTagCompound().setString("extra","discarded originally");InventoryCrafting g=grid(input);check(s.matches(g,null),"scraps valid single stack amount "+amount);ItemStack out=s.getCraftingResult(g);
   check(out.getItem()==ModItems.scraps&&out.getCount()==2&&out.getMetadata()==7&&out.getTagCompound().getInteger("amount")==amount/2,"scraps exact integer-halving output "+amount);
   check(!out.getTagCompound().hasKey("liquid")&&!out.getTagCompound().hasKey("extra")&&input.getTagCompound().getInteger("amount")==amount,"scraps original tag rebuilding/loss and input preservation");
  }
  for(int amount:new int[]{-1,0,1}){InventoryCrafting g=grid(ItemScraps.create(new Mats.MaterialStack(Mats.matById.get(7),amount)));check(!s.matches(g,null)&&s.getCraftingResult(g).isEmpty(),"scraps sub-two amount rejected");}
  InventoryCrafting untagged=grid(new ItemStack(ModItems.scraps,1,7));check(s.matches(untagged,null)&&s.getCraftingResult(untagged).getTagCompound().getInteger("amount")==36,"scraps original default ingot quantity 72");
  check(!s.matches(grid(),null)&&s.getCraftingResult(grid()).isEmpty(),"scraps empty grid rejected");
  check(!s.matches(grid(new ItemStack(Items.APPLE)),null)&&s.getCraftingResult(grid(new ItemStack(Items.APPLE))).isEmpty(),"scraps wrong item rejected");
  check(!s.matches(grid(new ItemStack(ModItems.scraps,1,7),new ItemStack(ModItems.scraps,1,7)),null),"scraps second stack rejected");
  boolean originalNpe=false;try{s.matches(grid(new ItemStack(ModItems.scraps,1,999)),null);}catch(NullPointerException expected){originalNpe=true;}check(originalNpe,"original malformed-material null dereference remains explicit, not silently repaired");
  check(r.isDynamic()&&s.isDynamic()&&r.canFit(1,1)&&s.canFit(1,1)&&r.getRecipeOutput().isEmpty(),"native dynamic classification and unknown static RBMK output");
  CargoShellCraftingHandler cargo=(CargoShellCraftingHandler)ForgeRegistries.RECIPES.getValue(new ResourceLocation("hbm","crafting/special/cargo_shell"));
  ItemStack shell=new ItemStack(ModItems.ammo_arty,31,8),payload=new ItemStack(Items.DYE,64,3);NBTTagCompound payloadTag=new NBTTagCompound();payloadTag.setString("name","cargo metadata retained");payload.setTagCompound(payloadTag);
  InventoryCrafting cg=grid(shell,payload);check(cargo.matches(cg,null),"cargo original clean meta-eight arty shell plus one payload");ItemStack packed=cargo.getCraftingResult(cg);ItemStack unpacked=new ItemStack(packed.getTagCompound().getCompoundTag("cargo"));
  check(packed.getItem()==ModItems.ammo_arty&&packed.getCount()==1&&packed.getMetadata()==8,"cargo original actual output arty item/count/meta");
  check(unpacked.getItem()==Items.DYE&&unpacked.getMetadata()==3&&unpacked.getCount()==1&&unpacked.getTagCompound().getString("name").equals("cargo metadata retained"),"cargo payload exact item/meta/NBT and normalized unit count");
  check(!shell.hasTagCompound()&&shell.getCount()==31&&payload.getCount()==64,"cargo original input stacks unmodified");
  packed.getTagCompound().getCompoundTag("cargo").getCompoundTag("tag").setString("name","changed");check(payload.getTagCompound().getString("name").equals("cargo metadata retained"),"cargo result serialized payload deep-independent from input");
  check(cargo.getRecipeOutput().getItem()==ModItems.ammo_shell&&cargo.getRecipeOutput().getMetadata()==8,"cargo original display/actual item discrepancy conserved");
  check(!cargo.matches(grid(shell),null)&&cargo.getCraftingResult(grid(shell)).isEmpty(),"cargo missing payload rejected");
  check(!cargo.matches(grid(shell,new ItemStack(ModItems.ammo_arty,1,8)),null),"cargo two clean shells rejected");
  check(!cargo.matches(grid(shell,payload,new ItemStack(Items.APPLE)),null),"cargo third occupied slot rejected");
  ItemStack wrongMeta=shell.copy();wrongMeta.setItemDamage(7);check(!cargo.matches(grid(wrongMeta,payload),null),"cargo wrong shell metadata rejected");
  ItemStack taggedShell=shell.copy();taggedShell.setTagCompound(new NBTTagCompound());check(!cargo.matches(grid(taggedShell,payload),null),"cargo even empty shell NBT prevents selecting shell");
  check(cargo.matches(grid(shell,taggedShell),null),"cargo tagged shell may be payload beside one clean shell, as original");
  Item stackContainer=item("stack_container",new ExtractedContainerPredicates.StackSensitive());Item noArgContainer=item("noarg_container",new ExtractedContainerPredicates.NoArg());
  for(Item container:new Item[]{Items.WATER_BUCKET,stackContainer,noArgContainer})check(!cargo.matches(grid(shell,new ItemStack(container)),null),"cargo rejects genuine native or exact extracted NTM container predicate "+container.getRegistryName());
  check(cargo.isDynamic()&&cargo.canFit(2,2)&&!cargo.canFit(1,1),"cargo native dynamic classification and two-slot fit");
  System.out.println("PASS dynamic crafting checks="+checks);
 }
}
