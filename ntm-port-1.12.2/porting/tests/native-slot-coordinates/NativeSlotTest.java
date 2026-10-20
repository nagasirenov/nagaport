import com.hbm.inventory.SlotLayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
public class NativeSlotTest {
 static int n;static void eq(boolean b,String m){n++;if(!b)throw new AssertionError(m);}
 static int calls;static Slot get(Slot s){calls++;return s;}
 public static void main(String[]a)throws Exception{
  int[] vs={Integer.MIN_VALUE,-10000,-1,0,1,17,9999,10000,Integer.MAX_VALUE};
  for(int x:vs)for(int y:vs)for(int layer:vs){
   SlotLayer now=new SlotLayer(null,7,x,y,layer); LegacySlotLayer old=new LegacySlotLayer(null,7,x,y,layer);
   eq(now.xPos==old.h&&now.yPos==old.i,"constructor positions");eq(now.isVisible()==old.isVisible(),"constructor visible");eq(now.isItemValid(ItemStack.EMPTY)==old.a((add)null),"validity constructor");
   for(int selected:vs){now.setLayer(selected);old.setLayer(selected);eq(now.xPos==old.h&&now.yPos==old.i,"selection coordinates");eq(now.isVisible()==old.isVisible(),"selection visible");eq(now.isItemValid(ItemStack.EMPTY)==old.a((add)null),"selection validity");}
   now.yPos=y+1;old.i=y+1;now.setLayer(layer);old.setLayer(layer);eq(now.yPos==y&&old.i==y,"original y restored rather than mutated y");
   now.xPos=x+1;old.h=x+1;now.setLayer(layer+1);old.setLayer(layer+1);eq(now.xPos==old.h&&now.yPos==old.i,"hide preserves external x");
  }
  Slot s=new Slot(null,0,13,27);calls=0;get(s).xPos+=3;eq(calls==1&&s.xPos==16,"receiver compound once");
  calls=0;try{get(null).yPos=2;throw new AssertionError();}catch(NullPointerException expected){eq(calls==1,"null receiver once");}
  for(int x:vs)for(int y:vs){Slot s1=new Slot(null,1,x,y);aay s2=new aay(null,1,x,y);eq(s1.xPos==s2.h&&s1.yPos==s2.i,"real engine constructor fields");}
  System.out.println("PASS "+n+" actual original/native Slot and complete SlotLayer checks; no GUI render/game claim");
 }
}
