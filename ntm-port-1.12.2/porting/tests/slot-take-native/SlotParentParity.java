import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
/** Executes the original and native parent callback implementations, not copied equivalents. */
public final class SlotParentParity {
 static int oldDirty,newDirty,checks;
 static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 static class Old extends aay {Old(){super(null,0,0,0);}@Override public void f(){oldDirty++;}}
 static class Native extends Slot {Native(){super(null,0,0,0);}@Override public void onSlotChanged(){newDirty++;}}
 static class OldChild extends Old {}
 static class NativeChild extends Native {}
 public static void main(String[] args){
  Bootstrap.register();
  for(int i=0;i<2;i++)for(ItemStack stack:new ItemStack[]{null,ItemStack.EMPTY,new ItemStack(Items.APPLE,0),new ItemStack(Items.APPLE,7)}){
   aay old=i==0?new Old():new OldChild();Slot now=i==0?new Native():new NativeChild();int a=oldDirty,b=newDirty;
   old.a((yz)null,(add)null);check(oldDirty==a+1,"original callback virtually dirties exactly once");
   check(now.onTake(null,stack)==stack,"native added return preserves exact input identity without normalization");check(newDirty==b+1,"native callback virtually dirties exactly once");
  }
  System.out.println("PASS "+checks+" actual original/native Slot parent checks, inherited virtual dispatch and native return identity");
 }
}
