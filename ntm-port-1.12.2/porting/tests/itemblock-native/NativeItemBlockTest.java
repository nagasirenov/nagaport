import java.util.*;
import java.lang.reflect.*;
import com.hbm.items.block.*;
import com.hbm.blocks.*;
import com.hbm.blocks.generic.*;
import com.hbm.main.NativeItemBlockColorHandler;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.*;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.color.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class NativeItemBlockTest {
 static int checks; static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 static ItemBlockBase register(Block b,String name){b.setTranslationKey(name);ForgeRegistries.BLOCKS.register(b.setRegistryName("hbm",name));ItemBlockBase i=new ItemBlockBase(b);ForgeRegistries.ITEMS.register(i.setRegistryName(b.getRegistryName()));check(Item.getItemFromBlock(b)==i,"native registry owns production ItemBlockBase");return i;}
 static class Provider extends Block implements IBlockMulti,ITooltipProvider,IPersistentInfoProvider {
  EntityPlayer last; boolean advanced; NBTTagCompound tag;
  Provider(){super(Material.ROCK);} public int getSubCount(){return 4;}
  public String getUnlocalizedName(ItemStack s){return "tile.variant_"+s.getMetadata();}
  public String getOverrideDisplayName(ItemStack s){return s.getMetadata()==3?"Original override":null;}
  public void addInformation(ItemStack s,EntityPlayer p,List list,boolean ext){last=p;advanced=ext;list.add("ordinary:"+s.getMetadata());}
  public void addInformation(ItemStack s,NBTTagCompound t,EntityPlayer p,List list,boolean ext){check(p==last,"same viewing player for persistent info");tag=t;list.add("persistent:"+t.getInteger("energy"));}
  public EnumRarity getRarity(ItemStack s){return EnumRarity.EPIC;}
 }
 static class Derived extends ItemBlockBase {Derived(Block b){super(b);} public void addInformation(ItemStack s,EntityPlayer p,List l,boolean a){super.addInformation(s,p,l,a);l.add("descendant");}}
 static ItemBlockLore lore(Block b,String key){b.setTranslationKey(key);ForgeRegistries.BLOCKS.register(b.setRegistryName("hbm",key));ItemBlockLore item=new ItemBlockLore(b);ForgeRegistries.ITEMS.register(item.setRegistryName(b.getRegistryName()));return item;}
 static void descendants(Minecraft mc,Block normal,ItemBlockBase ni,sun.misc.Unsafe u)throws Exception {
  Field lf=net.minecraft.client.resources.I18n.class.getDeclaredField("i18nLocale");lf.setAccessible(true);Object locale=lf.get(null);
  Field pf=net.minecraft.client.resources.Locale.class.getDeclaredField("properties");pf.setAccessible(true);Map<String,String> translations=(Map<String,String>)pf.get(locale);
  Provider provider=new Provider();ItemBlockLore standard=lore(provider,"lore_provider");translations.put("tile.lore_provider.desc","first$second");
  List<String> lines=new ArrayList<>();standard.addInformation(new ItemStack(standard),(World)null,lines,ITooltipFlag.TooltipFlags.NORMAL);check(lines.equals(Arrays.asList("ordinary:0","first","second")),"Lore real base provider plus split localization in order");check(standard.getRarity(new ItemStack(standard))==EnumRarity.COMMON,"Lore intentionally overrides provider rarity");
  ItemBlockLore barrel=lore(new RedBarrel(),"lore_barrel");lines.clear();barrel.addInformation(new ItemStack(barrel),(World)null,lines,ITooltipFlag.TooltipFlags.NORMAL);check(lines.equals(Arrays.asList("Static fluid barrel")),"Lore red barrel branch exact");
  ModBlocks.meteor_battery=new Block(Material.ROCK);ItemBlockLore meteor=lore(ModBlocks.meteor_battery,"lore_meteor");lines.clear();meteor.addInformation(new ItemStack(meteor),(World)null,lines,ITooltipFlag.TooltipFlags.NORMAL);check(lines.equals(Arrays.asList("Provides infinite charge to tesla coils")),"Lore meteor identity branch");
  ItemBlockLore ore=lore(new BlockOreFluid(),"lore_ore");lines.clear();ore.addInformation(new ItemStack(ore),(World)null,lines,ITooltipFlag.TooltipFlags.NORMAL);check(lines.equals(Arrays.asList("You weren't supposed to mine that.","Come on, get a derrick you doofus.")),"Lore ore branch exact");
  ModBlocks.gravel_diamond=new Block(Material.ROCK);ItemBlockLore gravel=lore(ModBlocks.gravel_diamond,"lore_gravel");lines.clear();gravel.addInformation(new ItemStack(gravel),(World)null,lines,ITooltipFlag.TooltipFlags.NORMAL);check(lines.equals(LoreExpected.GRAVEL),"all original gravel strings and empty separators in order");check(gravel.getRarity(new ItemStack(gravel))==EnumRarity.RARE,"Lore gravel rarity");
  ModBlocks.block_euphemium=new Block(Material.ROCK);ModBlocks.block_euphemium_cluster=new Block(Material.ROCK);ModBlocks.plasma=new Block(Material.ROCK);
  int n=0;for(Block b:Arrays.asList(ModBlocks.block_euphemium,ModBlocks.block_euphemium_cluster,ModBlocks.plasma)){ItemBlockLore item=lore(b,"lore_epic_"+n++);check(item.getRarity(new ItemStack(item))==EnumRarity.EPIC,"every Lore epic identity");}
  mc.player=(EntityPlayerSP)u.allocateInstance(EntityPlayerSP.class);mc.player.inventory=new net.minecraft.entity.player.InventoryPlayer(mc.player);
  BlockRemap rb=new BlockRemap(normal,7);ForgeRegistries.BLOCKS.register(rb.setRegistryName("hbm","remap_test"));ItemBlockRemap ri=new ItemBlockRemap(rb);ForgeRegistries.ITEMS.register(ri.setRegistryName(rb.getRegistryName()));
  ItemStack old=new ItemStack(ri,37,2);NBTTagCompound data=new NBTTagCompound();data.setString("original","not copied by original conversion");old.setTagCompound(data);ItemStack neighbor=new ItemStack(ni,4);mc.player.inventory.setInventorySlotContents(4,old);mc.player.inventory.setInventorySlotContents(5,neighbor);
  ri.onUpdate(old,null,mc.player,4,false);ItemStack converted=mc.player.inventory.getStackInSlot(4);check(converted.getItem()==ni&&converted.getCount()==37&&converted.getMetadata()==7,"Remap target item exact count and metadata");check(!converted.hasTagCompound()&&old.getCount()==37&&old.hasTagCompound(),"Remap replaces inventory stack, drops NBT as original without mutating input");check(mc.player.inventory.getStackInSlot(5)==neighbor,"Remap only addressed slot");
  ri.onUpdate(old,null,null,4,true);check(mc.player.inventory.getStackInSlot(4)==converted,"Remap ignores non-player entity");new ItemBlockRemap(normal).onUpdate(old,null,mc.player,4,true);check(mc.player.inventory.getStackInSlot(4)==converted,"Remap ignores non-remap block");
  lines.clear();ri.addInformation(old,(World)null,lines,ITooltipFlag.TooltipFlags.ADVANCED);check(lines.equals(Arrays.asList(net.minecraft.util.text.TextFormatting.RED+"Compatibility item, hold in inventory to convert!")),"native dispatch to exact Remap tooltip");
 }
 public static void main(String[] args)throws Exception {
  Bootstrap.register();
  Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);sun.misc.Unsafe u=(sun.misc.Unsafe)uf.get(null);
  Field locale=net.minecraft.client.resources.I18n.class.getDeclaredField("i18nLocale");locale.setAccessible(true);locale.set(null,new net.minecraft.client.resources.Locale());
  Minecraft mc=(Minecraft)u.allocateInstance(Minecraft.class);Field f=Minecraft.class.getDeclaredField("instance");f.setAccessible(true);f.set(null,mc);mc.player=(EntityPlayerSP)u.allocateInstance(EntityPlayerSP.class);
  BlockScaffold scaffold=new BlockScaffold();BlockMetalFence fence=new BlockMetalFence(Material.IRON);Block normal=new Block(Material.ROCK);
  ItemBlockBase si=register(scaffold,"tile.steel_scaffold"),fi=register(fence,"tile.fence_metal"),ni=register(normal,"normal_test");
  for(int m=0;m<16;m++){check(si.getMetadata(m)==m&&fi.getMetadata(m)==m,"subtype damage retained");check(ni.getMetadata(m)==0,"normal Item metadata behavior");check(si.getHasSubtypes()&&fi.getHasSubtypes()&&!ni.getHasSubtypes(),"subtype flags");check(fi.getTranslationKey(new ItemStack(fi,1,m)).equals("tile.tile.fence_metal"+(m==1?"_post":"")),"fence post key exact predicate");}
  check(ni.getTranslationKey(new ItemStack(ni)).equals("tile.normal_test"),"normal native translation key");
  check(ni.getRarity(new ItemStack(ni))==EnumRarity.COMMON,"default rarity");
  Provider p=new Provider();ItemBlockBase pi=register(p,"provider_test");ItemStack ps=new ItemStack(pi,1,3);
  check(pi.getItemStackDisplayName(ps).equals("Original override"),"custom display override");check(pi.getTranslationKey(ps).equals("tile.variant_3"),"multi internal key");check(pi.getRarity(ps)==EnumRarity.EPIC,"provider rarity");
  ps.setItemDamage(2);check(pi.getItemStackDisplayName(ps).equals("tile.variant_2.name"),"localization fallback key via real I18nUtil");
  NBTTagCompound outer=new NBTTagCompound(),payload=new NBTTagCompound();payload.setInteger("energy",42);outer.setTag("persistent",payload);ps.setTagCompound(outer);
  List<String> lines=new ArrayList<>();pi.addInformation(ps,(World)null,lines,ITooltipFlag.TooltipFlags.ADVANCED);check(lines.equals(Arrays.asList("ordinary:2","persistent:42")),"ordered native tooltip/persistent dispatch");check(p.last==mc.player&&p.advanced&&p.tag==payload,"player advanced and original NBT identity");
  mc.player=null;lines.clear();new Derived(p).addInformation(ps,(World)null,lines,ITooltipFlag.TooltipFlags.NORMAL);check(lines.get(2).equals("descendant")&&p.last==null&&!p.advanced,"descendant virtual callback and no-world null player");
  ps.setTagCompound(null);lines.clear();pi.addInformation(ps,(World)null,lines,ITooltipFlag.TooltipFlags.NORMAL);check(lines.size()==1,"no persistent callback without data");
  lines.clear();new ItemBlockBlastInfo(normal).addInformation(new ItemStack(ni),(World)null,lines,ITooltipFlag.TooltipFlags.NORMAL);check(lines.size()==1&&lines.get(0).contains("Blast Resistance:"),"complete production descendant virtual dispatch");
  BlockColors bc=new BlockColors();ItemColors ic=new ItemColors();NativeItemBlockColorHandler.register(new ColorHandlerEvent.Item(ic,bc));
  for(ItemBlockBase item:Arrays.asList(si,fi,ni))check(ic.colorMultiplier(new ItemStack(item),0)==-1,"original inherited white: native untinted sentinel");
  bc.registerBlockColorHandler((state,world,pos,index)->0x123400+scaffold.getMetaFromState(state)*16+index,scaffold);
  for(int m=0;m<16;m++)for(int t=0;t<3;t++)check(ic.colorMultiplier(new ItemStack(si,1,m),t)==0x123400+m*16+t,"real native registered metadata/tint callback, not constant");
  check(ItemBlockBase.class.getSuperclass()==ItemBlock.class,"actual native superclass");
  descendants(mc,normal,ni,u);
  System.out.println("PASS NativeItemBlockTest: "+checks+" assertions; complete production root and real native registries, player, NBT and color APIs.");
 }
}
