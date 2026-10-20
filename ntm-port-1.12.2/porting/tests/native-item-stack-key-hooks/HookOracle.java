import java.lang.reflect.*;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;

/** Both sides execute complete hook bodies on real platform Item/ItemStack classes.
 * Naming-only owner projections do not certify unrelated constructors/gameplay. */
public final class HookOracle {
 static int checks; static Field nativeDamage,oldDamage; static sun.misc.Unsafe unsafe;
 static final String[] NAMES={"ItemModMinecart","ItemBlockNamedMeta","ItemBlockColoredConcrete","ItemAmmoArty","ItemAmmoHIMARS","ItemMineralOre","ItemAutogen","ItemMetaUpgrade","ItemGear","ItemStampBook","ItemCrayon","ItemScraps"};
 static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 static Object result(Object owner,Object stack,boolean old) throws Exception {
  try{return owner.getClass().getMethod(old?"a":"getTranslationKey",old?add.class:ItemStack.class).invoke(owner,stack);}
  catch(InvocationTargetException e){return e.getCause().getClass().getName();}
 }
 static ItemStack ns(Item item,int count,int meta)throws Exception{ItemStack s=new ItemStack(item,count,0);nativeDamage.setInt(s,meta);return s;}
 static add os(adb item,int count,int meta)throws Exception{add s=new add(item,count,0);oldDamage.setInt(s,meta);return s;}
 static Object item(String prefix,String name,Block b,aji ob)throws Exception{
  Class<?> c=Class.forName(prefix+name);boolean old=prefix.equals("Legacy");
  return name.startsWith("ItemBlock")?c.getConstructor(old?aji.class:Block.class).newInstance(old?ob:b):c.newInstance();
 }
 static void equal(Object n,Object o,String m){check(Objects.equals(n,o),m+" native="+n+" legacy="+o);}
 public static class NativeGearVirtual extends NativeItemGear { @Override public String getTranslationKey(){return "virtual.key";} }
 public static class LegacyGearVirtual extends LegacyItemGear { @Override public String a(){return "virtual.key";} }
 public static class NativeAutogenVirtual extends NativeItemAutogen { @Override public String getTranslationKey(){return "must.not.replace.base";} }
 public static class LegacyAutogenVirtual extends LegacyItemAutogen { @Override public String a(){return "must.not.replace.base";} }
 public static class NativeStackVirtual extends NativeItemGear { @Override public String getTranslationKey(ItemStack s){return "intercept:"+super.getTranslationKey(s);} }
 public static class LegacyStackVirtual extends LegacyItemGear { @Override public String a(add s){return "intercept:"+super.a(s);} }
 public static class NativeStampVirtual extends NativeItemStampBook { public Item seen; public int meta; @Override public StampType getStampType(Item item,int damage){seen=item;meta=damage;return super.getStampType(item,damage);} }
 public static class LegacyStampVirtual extends LegacyItemStampBook { public adb seen; public int meta; @Override public StampType getStampType(adb item,int damage){seen=item;meta=damage;return super.getStampType(item,damage);} }
 public static class VirtualBlock extends Block { VirtualBlock(){super(Material.ROCK);} @Override public String getTranslationKey(){return "tile.block.virtual";} }
 public static class LegacyVirtualBlock extends aji { LegacyVirtualBlock(){super(awt.a);} @Override public String a(){return "tile.block.virtual";} }
 public static void main(String[] args)throws Exception {
  Bootstrap.register(); aji.p();adb.l();
  Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);unsafe=(sun.misc.Unsafe)uf.get(null);
  nativeDamage=ItemStack.class.getDeclaredField("itemDamage");nativeDamage.setAccessible(true);oldDamage=add.class.getDeclaredField("f");oldDamage.setAccessible(true);
  Block block=new Block(Material.ROCK);aji oldBlock=(aji)unsafe.allocateInstance(aji.class);
  String[] keys={null,"","gear","item.cart","tile.name","hbm:raw","UPPER.Mixed","ñ漢字","x.name","white space"};
  int[] edge={Integer.MIN_VALUE,Integer.MIN_VALUE+1,-65537,-32768,-257,-17,-16,-15,-8,-7,-1,0,1,2,7,8,15,16,17,127,255,32767,65535,Integer.MAX_VALUE};
  List<Integer> metas=new ArrayList<>();for(int n=-40;n<=40;n++)metas.add(n);for(int n:edge)metas.add(n);
  for(String name:NAMES){
   Item n=(Item)item("Native",name,block,oldBlock);adb o=(adb)item("Legacy",name,block,oldBlock);
   check(n.getClass().getMethod("getTranslationKey",ItemStack.class).getDeclaringClass().getName().equals("Native"+(name.equals("ItemScraps")?"ItemAutogen":name)),"real override/inheritance "+name);
   for(String key:keys){n.setTranslationKey(key);o.c(key);block.setTranslationKey(key);oldBlock.c(key);
    for(int meta:metas){ItemStack s=ns(n,1,meta);add t=os(o,1,meta);
     equal(result(n,s,false),result(o,t,true),name+" key="+key+" meta="+meta);
     // Actual entry points on positive-count fixtures. Values above 65535 are synthetic raw-field fixtures, NOT normal caller-identity evidence.
     if(meta>=0){equal(s.getTranslationKey(),t.a(),"actual positive-count fixture stack dispatch "+name+" "+meta);}
    }
    equal(result(n,null,false),result(o,null,true),"null hook behavior "+name+" "+key);
   }
   // Empty count/invalid metadata callers are not migrated: only explicit hook bodies are tested here.
   for(int count:new int[]{0,-1})for(int meta:edge)equal(result(n,ns(n,count,meta),false),result(o,os(o,count,meta),true),"explicit hook only count="+count+" "+name+" "+meta);
  }
  // Rejection control: the untouched BookLore hook cannot yet be certified for malformed legacy tags.
  NBTTagCompound bad=new NBTTagCompound(); dh oldBad=new dh();bad.setInteger("k",73);oldBad.a("k",73);
  equal(bad.getString("k"),"","BookLore exclusion: genuine native getString numeric NBT");equal(oldBad.j("k"),"73","BookLore exclusion: genuine archived-client getString numeric NBT");
  for(String name:new String[]{"ItemAutogen","ItemScraps"})for(String key:keys){NativeItemAutogen n=(NativeItemAutogen)item("Native",name,block,oldBlock);LegacyItemAutogen o=(LegacyItemAutogen)item("Legacy",name,block,oldBlock);n.setTranslationKey("raw");o.c("raw");n.oun(key);o.oun(key);equal(result(n,null,false),result(o,null,true),"autogen null/override "+name+" "+key);equal(result(n,ns(n,1,0),false),result(o,os(o,1,0),true),"autogen inherited override "+name+" "+key);}
  for(String name:new String[]{"ItemAmmoArty","ItemAmmoHIMARS","ItemMineralOre"}){
   Item n=(Item)item("Native",name,block,oldBlock);adb o=(adb)item("Legacy",name,block,oldBlock);Field nf=n.getClass().getField("itemTypes"),of=o.getClass().getField("itemTypes");Object na=nf.get(null),oa=of.get(null);
   for(int mode=0;mode<3;mode++){
    Object nv=mode==0?null:Array.newInstance(na.getClass().getComponentType(),mode==1?0:1);Object ov=mode==0?null:Array.newInstance(oa.getClass().getComponentType(),mode==1?0:1);nf.set(null,nv);of.set(null,ov);
    equal(result(n,ns(n,1,0),false),result(o,os(o,1,0),true),"array null/empty/null-element error "+name+" "+mode);
   }nf.set(null,na);of.set(null,oa);
  }
  NativeGearVirtual ng=new NativeGearVirtual();LegacyGearVirtual og=new LegacyGearVirtual();equal(result(ng,ns(ng,1,1),false),result(og,os(og,1,1),true),"this zeroarg virtual dispatch");equal(ng.getTranslationKey(ns(ng,1,1)),"virtual.key_steel","this is not super bypass");
  NativeAutogenVirtual na=new NativeAutogenVirtual();LegacyAutogenVirtual oa=new LegacyAutogenVirtual();na.setTranslationKey("base");oa.c("base");equal(result(na,null,false),result(oa,null,true),"Autogen explicit super overload");equal(result(na,null,false),"item.base","Autogen avoids virtual zeroarg substitution");
  NativeStackVirtual nv=new NativeStackVirtual();LegacyStackVirtual ov=new LegacyStackVirtual();nv.setTranslationKey("gear");ov.c("gear");equal(ns(nv,1,1).getTranslationKey(),os(ov,1,1).a(),"actual stack selects most-derived hook");
  NativeStampVirtual st=new NativeStampVirtual();LegacyStampVirtual ost=new LegacyStampVirtual();ItemStack ss=ns(st,1,3);add ots=os(ost,1,3);equal(result(st,ss,false),result(ost,ots,true),"stamp helper remains virtual");check(st.seen==st&&ost.seen==ost&&st.meta==3&&ost.meta==3,"stamp helper exact live receiver/meta");
  for(int index=0;index<16;index++)equal(EnumDyeColor.byDyeDamage(index).getTranslationKey(),acj.a[index],"actual native enum vs original dye array "+index);
  for(int meta:edge)check(NativeBlockConcreteColored.func_150032_b(meta)==LegacyBlockConcreteColored.func_150032_b(meta)&&NativeBlockConcreteColored.func_150032_b(meta)>=0&&NativeBlockConcreteColored.func_150032_b(meta)<16,"unchanged mask all integer edges "+meta);
  com.hbm.items.block.ItemBlockNamedMeta complete=new com.hbm.items.block.ItemBlockNamedMeta(new VirtualBlock());
  for(int meta:edge)equal(complete.getTranslationKey(ns(complete,1,meta)),"tile.block.virtual."+meta,"complete owner native compile and block virtual delegation "+meta);
  check(complete.getClass().getSuperclass()==com.hbm.items.block.ItemBlockMeta.class,"complete owner real parent");
  System.out.println("PASS "+checks+" native/original assertions: 11 complete hook projections, 12-class dispatch family, actual Item/ItemStack, all variants/edge metadata/null/errors; BookLore NBT divergence excluded, original 16 dye strings, and complete ItemBlockNamedMeta + ItemBlockMeta; no full gameplay/build claim");
 }
}
