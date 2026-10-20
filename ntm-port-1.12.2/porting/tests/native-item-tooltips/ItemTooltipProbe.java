import java.util.*;
import net.minecraft.item.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
/** Real native engine objects; generated subclasses contain complete unchanged production bodies. */
public class ItemTooltipProbe {
 static int assertions;
 static void check(boolean b,String message){assertions++;if(!b)throw new AssertionError(message);}
 static final String[] glyphs={"Hourglass","Eye","'Pillar'","IOI","Delta","VTPC","Cool S","Trefoil","Pony","Sparkle","PiP","Triangles","Linux Mint","13","Digamma","Celestial Altar"};
 static List<String> expected(String name,int meta,boolean nbt){
  switch(name){
   case "ItemGlyphBlock":return meta>=0&&meta<16?Arrays.asList(glyphs[meta]):Collections.emptyList();
   case "ItemMissileShuttle":return Arrays.asList("Tonite, on bo''om gear:","James huffs leaded gasoline and","goes insane, Richard spends the","entire budget on a broken .PNG,","And I forget to set the infinite","Water tanks on our RBMK to flow","out, blowing up our entire base");
   case "ItemTemFlakes":return meta==0?Arrays.asList("Heals 2HP DISCOUNT FOOD OF TEM!!!"):meta==1?Arrays.asList("Heals 2HP food of tem"):meta==2?Arrays.asList("Heals food of tem (expensiv)"):Collections.emptyList();
   case "ItemStarmetal":return meta>=1&&meta<=3?Arrays.asList("\u00a7o"+new String[]{"Astra","Ursa","Orion"}[meta-1]):Collections.emptyList();
   case "ItemReactorSensor":return nbt?Arrays.asList("x: -17","y: 255","z: 2147483647"):Arrays.asList("No reactor selected!");
   case "ItemTeleLink":return nbt?Arrays.asList("X: -17","Y: 255","Z: 2147483647","D: -1"):Arrays.asList("\u00a7cSelect exit location first!");
   case "ItemDetonator":return Arrays.asList("Shift right-click to set position,","right-click to detonate!",nbt?"\u00a7eLinked to -17, 255, 2147483647":"\u00a7cNo position set!");
   case "ItemWiring":return nbt?Arrays.asList("Wire start x: -17","Wire start y: 255","Wire start z: 2147483647"):Arrays.asList("Right-click poles to connect");
   default:throw new AssertionError(name);
  }
 }
 public static void main(String[] args)throws Exception {
  net.minecraft.init.Bootstrap.register();
  // Original and native base callbacks are actual engine methods, including null inputs.
  Class<?> old=Class.forName("adb");java.lang.reflect.Constructor<?> ctor=old.getDeclaredConstructor();ctor.setAccessible(true);Object oldItem=ctor.newInstance();
  java.lang.reflect.Method method=old.getMethod("a",Class.forName("add"),Class.forName("yz"),List.class,boolean.class);
  for(boolean advanced:new boolean[]{false,true}){List<String> before=new ArrayList<>(Arrays.asList("prefix"));method.invoke(oldItem,null,null,before,advanced);check(before.equals(Arrays.asList("prefix")),"original actual empty callback");}
  Item nativeItem=new Item();List<String> base=new ArrayList<>(Arrays.asList("prefix"));nativeItem.addInformation(null,null,base,null);check(base.equals(Arrays.asList("prefix")),"native actual empty callback");
  String[] names={"ItemGlyphBlock","ItemMissileShuttle","ItemTemFlakes","ItemStarmetal","ItemReactorSensor","ItemTeleLink","ItemDetonator","ItemWiring"};
  for(String name:names){Item item=(Item)Class.forName("RepresentativeItemTooltips$"+name).newInstance();
   for(int meta=0;meta<=17;meta++)for(boolean nbt:new boolean[]{false,true})for(int mode=0;mode<3;mode++){
    ItemStack stack=new ItemStack(item,1,meta);if(nbt){NBTTagCompound tag=new NBTTagCompound();tag.setInteger("x",-17);tag.setInteger("y",255);tag.setInteger("z",Integer.MAX_VALUE);tag.setInteger("dim",-1);stack.setTagCompound(tag);}
    final int selected=mode;final int[] reads={0};ITooltipFlag flag=mode==0?null:new ITooltipFlag(){public boolean isAdvanced(){reads[0]++;return selected==2;}};
    List<String> list=new ArrayList<>(Arrays.asList("prefix"));item.addInformation(stack,null,list,flag);
    List<String> want=new ArrayList<>(Arrays.asList("prefix"));want.addAll(expected(name,meta,nbt));check(list.equals(want),name+" meta="+meta+" nbt="+nbt+" mode="+mode+" actual="+list);check(reads[0]==0,"unused flag must not be evaluated");check(stack.getItemDamage()==meta&&stack.getCount()==1,"stack metadata/count unchanged");check(stack.hasTagCompound()==nbt,"NBT presence unchanged");
   }
   System.out.println("PASS complete body "+name+": 108 native dispatch cases (metadata 0..17, absent/present NBT, null/normal/advanced flag)");
  }
  // Actual native ItemStack callback route, not merely direct invocation.
  Item item=new RepresentativeItemTooltips.ItemMissileShuttle();net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.register(item.setRegistryName("tooltip_probe", "shuttle"));ItemStack stack=new ItemStack(item);
  for(ITooltipFlag flag:new ITooltipFlag[]{ITooltipFlag.TooltipFlags.NORMAL,ITooltipFlag.TooltipFlags.ADVANCED}){List<String> lines=stack.getTooltip(null,flag);List<String> want=expected("ItemMissileShuttle",0,false);int i=lines.indexOf(want.get(0));check(i>=0&&lines.subList(i,i+want.size()).equals(want),"actual ItemStack.getTooltip native dispatch and text order");}
  System.out.println("PASS "+assertions+" assertions; 864 complete-body cases; actual original/native base callbacks; actual native ItemStack route. Not whole-item or full-game execution.");
 }
}
