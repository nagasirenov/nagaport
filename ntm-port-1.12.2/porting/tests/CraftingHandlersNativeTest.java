import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockStorageCrate;
import com.hbm.blocks.machine.BlockMassStorage;
import com.hbm.items.ModItems;
import com.hbm.crafting.handlers.MKUCraftingHandler;
import com.hbm.crafting.nativeapi.NtmCraftingRegistry;
import com.hbm.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.profiler.Profiler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.GameData;

/** Complete recipe owners; NTM block/item identities are fixtures, not world/storage implementations. */
public class CraftingHandlersNativeTest {
 static int checks;
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);checks++;System.out.println("PASS "+m);}
 static InventoryCrafting grid(){return new InventoryCrafting(new Container(){public boolean canInteractWith(EntityPlayer p){return true;}},3,3);}
 static InventoryCrafting ingredients(IRecipe recipe){
  InventoryCrafting g=grid(); int i=0;
  for(Ingredient ingredient:recipe.getIngredients()){
   ItemStack[] choices=ingredient.getMatchingStacks();
   if(choices.length>0){ItemStack s=choices[0].copy();s.setCount(1);if(s.getMetadata()==32767)s.setItemDamage(0);g.setInventorySlotContents(i,s);}i++;
  } return g;
 }
 public static class Events {@SubscribeEvent public void register(RegistryEvent.Register<IRecipe> e){ExactHandlerProducers.register();}}
 static class SeedWorld extends World {
  final long seed;
  SeedWorld(long s){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),false);seed=s;}
  public long getSeed(){return seed;}
  protected IChunkProvider createChunkProvider(){return null;}
  protected boolean isChunkLoaded(int x,int z,boolean allowEmpty){return true;}
 }
 public static void main(String[] args)throws Exception{
  Bootstrap.register();ModMetadata md=new ModMetadata();md.modId="hbm";md.name="Native handler probe";
  DummyModContainer owner=new DummyModContainer(md);Loader.instance().setupTestHarness(owner);Loader.instance().setActiveModContainer(owner);
  GameRegistry.beginRegistrationTables();
  for(java.lang.reflect.Field f:ModBlocks.class.getFields())GameRegistry.registerBlock(((Block)f.get(null)).setCreativeTab(net.minecraft.creativetab.CreativeTabs.MISC),f.getName());
  for(java.lang.reflect.Field f:ModItems.class.getFields()){Item item=new Item().setCreativeTab(net.minecraft.creativetab.CreativeTabs.MISC);f.set(null,item);GameRegistry.registerItem(item,f.getName());}
  GameRegistry.finishRegistrationTables();GameData.fireRegistryEvents(id->!id.equals(GameData.RECIPES));
  String[] ores={"blockTungsten","plateTripleCopper","plateLead","plateAdvancedAlloy","ingotDesh","ingotAnyResistantAlloy","nuggetZirconium","ingotBeryllium"};
  Item[] values={Items.IRON_INGOT,Items.GOLD_INGOT,Items.DIAMOND,Items.EMERALD,Items.COAL,Items.REDSTONE,Items.QUARTZ,Items.BRICK};
  for(int i=0;i<ores.length;i++)OreDictionary.registerOre(ores[i],values[i]);
  MinecraftForge.EVENT_BUS.register(new Events());GameData.fireRegistryEvents(id->id.equals(GameData.RECIPES));
  String[] names={"desh_crate","tungsten_crate","safe","mass_storage_1","mass_storage_2"};
  Block[] outputs={ModBlocks.crate_desh,ModBlocks.crate_tungsten,ModBlocks.safe,ModBlocks.mass_storage,ModBlocks.mass_storage};
  for(int n=0;n<names.length;n++){
   IRecipe recipe=ForgeRegistries.RECIPES.getValue(new ResourceLocation("hbm","crafting/container_upgrade/"+names[n]));
   InventoryCrafting g=ingredients(recipe);check(recipe.matches(g,null),names[n]+" exact extracted producer matches");
   ItemStack source=ItemStack.EMPTY;int sourceSlot=-1;
   for(int i=0;i<9;i++){ItemStack s=g.getStackInSlot(i);Block b=Block.getBlockFromItem(s.getItem());if(b instanceof BlockStorageCrate||b instanceof BlockMassStorage){source=s;sourceSlot=i;break;}}
   NBTTagCompound tag=new NBTTagCompound(),nested=new NBTTagCompound();nested.setString("item","retained inventory");tag.setTag("inventory",nested);tag.setString("owner","unchanged");source.setTagCompound(tag);
   ItemStack result=recipe.getCraftingResult(g);
   check(result.getItem()==Item.getItemFromBlock(outputs[n])&&result.getCount()==1&&result.getMetadata()==(n<3?0:n-2),names[n]+" exact output/count/metadata");
   check(result.getTagCompound()==tag,names[n]+" original NBT alias preserved");result.getTagCompound().setInteger("mutation",42);check(source.getTagCompound().getInteger("mutation")==42,names[n]+" original alias mutation visible");
   check(recipe.getCraftingResult(g)!=result,names[n]+" fresh result stack");
   source.setTagCompound(null);check(!recipe.getCraftingResult(g).hasTagCompound(),names[n]+" absent source NBT preserved");
   if(n>=3){int meta=source.getMetadata();source.setItemDamage(meta+1);check(!recipe.matches(g,null),names[n]+" incorrect source tier rejected");source.setItemDamage(meta);}
   g.setInventorySlotContents(sourceSlot,ItemStack.EMPTY);check(!recipe.matches(g,null),names[n]+" missing container rejected");
  }
  IRecipe rod=null;for(IRecipe r:ForgeRegistries.RECIPES.getValuesCollection())if(r.getRegistryName().toString().startsWith("hbm:crafting/rod/zirnox_empty/")){check(rod==null,"single rod producer");rod=r;}
  InventoryCrafting rg=ingredients(rod);int occupied=0;for(int i=0;i<9;i++)if(!rg.getStackInSlot(i).isEmpty())occupied++;
  check(occupied==7&&rod.matches(rg,null),"extracted rod shape uses six nuggets and one ingot");
  check(rod.getCraftingResult(rg).getItem()==ModItems.rod_zirnox_empty&&rod.getCraftingResult(rg).getCount()==4,"rod output four empty Zirnox rods");
  OreDictionary.registerOre("nuggetZirconium",Items.APPLE);rg.setInventorySlotContents(0,new ItemStack(Items.APPLE));check(rod.matches(rg,null),"rod accepts live late ore alternative");rg.setInventorySlotContents(4,ItemStack.EMPTY);check(!rod.matches(rg,null),"rod missing central ingot rejected");
  MKUCraftingHandler mku=(MKUCraftingHandler)ForgeRegistries.RECIPES.getValue(new ResourceLocation("hbm","crafting/special/mku"));
  for(long seed:new long[]{0,1,-1,Long.MAX_VALUE,0}){
   World world=new SeedWorld(seed);MKUCraftingHandler.generateRecipe(world);ItemStack[] identity=MKUCraftingHandler.MKURecipe;MKUCraftingHandler.generateRecipe(world);check(identity==MKUCraftingHandler.MKURecipe,"MKU same seed cache "+seed);
   InventoryCrafting g=grid();int used=0,filled=-1,empty=-1;for(int i=0;i<9;i++){if(identity[i]!=null){g.setInventorySlotContents(i,identity[i].copy());used++;filled=i;}else empty=i;}
   check(used==6&&mku.matches(g,world),"MKU actual seeded six-ingredient array matches "+seed);
   ItemStack s=g.getStackInSlot(filled);s.setCount(7);s.setTagCompound(new NBTTagCompound());check(mku.matches(g,world),"MKU original ignores input count/NBT");s.setItemDamage(1);check(!mku.matches(g,world),"MKU exact metadata required");s.setItemDamage(0);
   g.setInventorySlotContents(empty,new ItemStack(Items.APPLE));check(!mku.matches(g,world),"MKU extra occupied slot rejected");
  }
  check(!mku.matches(grid(),null)&&!mku.canFit(2,2)&&mku.canFit(3,3)&&mku.isDynamic(),"MKU null-world guard/native fit/dynamic policy");
  check(mku.getCraftingResult(grid()).getItem()==ModItems.syringe_mkunicorn&&mku.getCraftingResult(grid())!=mku.getCraftingResult(grid()),"MKU fresh original output");
  boolean rejected=false;try{NtmCraftingRegistry.special("special/mku",new MKUCraftingHandler());}catch(IllegalStateException expected){rejected=true;}
  check(rejected&&ForgeRegistries.RECIPES.getValue(mku.getRegistryName())==mku,"duplicate explicit identity rejected without override");
  System.out.println("PASS native handler checks="+checks);
 }
}
