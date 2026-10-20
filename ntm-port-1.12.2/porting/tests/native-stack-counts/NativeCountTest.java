import java.lang.reflect.*;
import java.net.*;
import java.io.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
public final class NativeCountTest {
 static int checks; static String trace=""; static ItemStack current; static boolean throwsRhs, replaceReceiver;
 static void check(boolean b,String message){checks++;if(!b)throw new AssertionError(message);}
 static ItemStack receiver(){trace+="R";return current;}
 static int rhs(){trace+="V";if(throwsRhs)throw new IllegalArgumentException();if(replaceReceiver)current=new ItemStack(Items.APPLE,3);return 7;}
 static void assign(){receiver().setCount(rhs());}
 public static void main(String[] args)throws Exception {
  Bootstrap.register();Field raw=ItemStack.class.getDeclaredField("stackSize");raw.setAccessible(true);
  URLClassLoader old=new URLClassLoader(new URL[]{new File(args[0]).toURI().toURL()},NativeCountTest.class.getClassLoader());
  Class<?> block=old.loadClass("aji"),item=old.loadClass("adb"),stack=old.loadClass("add");block.getMethod("p").invoke(null);item.getMethod("l").invoke(null);
  Object apple=item.getMethod("d",int.class).invoke(null,260);Field oldCount=stack.getField("b");Constructor<?> construct=stack.getConstructor(item,int.class,int.class);
  int[] counts={Integer.MIN_VALUE,-100,-1,0,1,2,64,65,127,128,100000,Integer.MAX_VALUE};
  for(int initial:counts)for(int count:counts){
   ItemStack target=new ItemStack(Items.APPLE,initial);ItemStack alias=target;NBTTagCompound nbt=new NBTTagCompound();nbt.setString("marker","retain");target.setTagCompound(nbt);
   Object previous=construct.newInstance(apple,initial,0);oldCount.setInt(previous,count);target.setCount(count);
   ItemStack copied=new ItemStack(Items.APPLE,initial).copy();copied.setCount(count);check(raw.getInt(copied)==count && copied.getCount()==Math.max(0,count),"copy raw count and revival");
   if(count>0)check(copied.getItem()==Items.APPLE,"positive setter revives copied zero/negative item exactly as legacy field assignment");
   check(raw.getInt(target)==oldCount.getInt(previous),"raw count exact");check(alias==target&&raw.getInt(alias)==count,"alias mutation");
   check(target.getCount()==Math.max(0,count),"native normalized count");check(target.isEmpty()==(count<=0),"empty contract");check(target.getTagCompound()==nbt,"tag identity");
   if(count>0){check(target.getItem()==Items.APPLE,"item retained");check(target.copy().getCount()==count,"positive copy no clamp");}
  }
  ItemStack[] invalid={ItemStack.EMPTY,new ItemStack((Item)null,3),new ItemStack(Items.AIR,3),new ItemStack(Items.APPLE,3,65536),new ItemStack(Items.APPLE,3,-32769)};
  Field damage=ItemStack.class.getDeclaredField("itemDamage");damage.setAccessible(true);damage.setInt(invalid[3],65536);damage.setInt(invalid[4],-32769);
  int savedEmpty=raw.getInt(ItemStack.EMPTY);
  try {for(ItemStack s:invalid)for(int c:counts){s.setCount(c);check(raw.getInt(s)==c,"invalid raw retained");check(s.getCount()==0&&s.isEmpty(),"invalid remains EMPTY");check(s.getItem()==Items.AIR,"invalid public item is AIR");}}
  finally{ItemStack.EMPTY.setCount(savedEmpty);}
  current=new ItemStack(Items.APPLE,2);trace="";assign();check(trace.equals("RV")&&current.getCount()==7,"receiver/RHS each once in order");
  ItemStack saved=current;replaceReceiver=true;trace="";assign();check(saved.getCount()==7 && current.getCount()==3 && trace.equals("RV"),"receiver captured before RHS replaces alias");replaceReceiver=false;
  current=null;trace="";try{assign();throw new AssertionError("missing null failure");}catch(NullPointerException expected){}check(trace.equals("RV"),"RHS precedes null dereference");
  throwsRhs=true;trace="";try{assign();throw new AssertionError("missing RHS failure");}catch(IllegalArgumentException expected){}check(trace.equals("RV"),"throwing RHS precedes null failure");throwsRhs=false;
  ExtractedMassStorage m=new ExtractedMassStorage();m.slots[1]=new ItemStack(Items.APPLE,1);m.stack=130;m.output=false;check(m.quickExtract()==null&&m.stack==130&&m.dirty==0,"disabled extraction");
  m.output=true;m.stack=63;check(m.quickExtract()==null&&m.stack==63&&m.dirty==0,"insufficient stock");m.stack=130;ItemStack result=m.quickExtract();check(result.getCount()==64&&m.stack==66&&m.dirty==1,"exact production extraction output/decrement/dirty");check(m.slots[1].getCount()==1&&result!=m.slots[1],"template unmodified");
  for(Item i:new Item[]{Items.IRON_SWORD,Items.SNOWBALL}){m.slots[1]=new ItemStack(i,1);m.stack=130;int expected=m.slots[1].getMaxStackSize();int dirty=m.dirty;ItemStack out=m.quickExtract();check(out.getCount()==expected && out.getItem()==i && m.stack==130-expected && m.dirty==dirty+1,"production extraction native 1/16 max limits");}
  Object oldZero=construct.newInstance(apple,0,0);Object oldCopy=stack.getMethod("m").invoke(oldZero);check(stack.getMethod("b").invoke(oldCopy)==apple,"legacy zero sentinel copy keeps identity");
  ItemStack zero=new ItemStack(Items.APPLE,1);zero.setCount(0);check(zero.copy().isEmpty() && zero.copy().getItem()==Items.AIR,"native zero sentinel public identity incompatibility explicitly verified");
  old.close();System.out.println("PASS "+checks+" actual original/native count, EMPTY, alias, evaluation and deferred quickExtract candidate checks; zero-sentinel incompatibility is a deferred gate.");
 }
}
