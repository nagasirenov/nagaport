import net.minecraft.block.BlockDispenser;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.dispenser.*;
public class DispenserRegistryParity {
 static int checks;
 static void ok(boolean v,String m){checks++;if(!v)throw new AssertionError(m);}
 public static void main(String[] args)throws Exception {
  net.minecraft.init.Bootstrap.register();
  Object oldKey=new Object(); Item key=new Item();
  Object oldDefault=akm.a.a(oldKey); IBehaviorDispenseItem fallback=BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(key);
  ok(oldDefault!=null && fallback!=null,"default behaviors exist");
  ok(akm.a.a(new Object())==oldDefault,"old stable default");
  ok(BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(new Item())==fallback,"native stable default");
  cp oldA=new cp(){public add a(ck src,add stack){return stack;}};
  cp oldB=new cp(){public add a(ck src,add stack){return stack;}};
  IBehaviorDispenseItem a=(src,stack)->stack,b=(src,stack)->stack;
  akm.a.a(oldKey,oldA);BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(key,a);
  ok(akm.a.a(oldKey)==oldA,"old exact callback identity");ok(BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(key)==a,"native exact callback identity");
  Object captured=akm.a.a(oldKey); IBehaviorDispenseItem capturedNew=BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(key);
  akm.a.a(oldKey,oldB);BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(key,b);
  ok(akm.a.a(oldKey)==oldB && captured==oldA,"old replacement and captured delegate");
  ok(BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(key)==b && capturedNew==a,"native replacement and captured delegate");
  ok(capturedNew.dispense(null,ItemStack.EMPTY)==ItemStack.EMPTY,"actual registered native invocation");
  ok(((cp)captured).a(null,null)==null,"actual registered original invocation");
  ok(BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(new Item())==fallback,"unrelated default unchanged");
  System.out.println("PASS "+checks+" real original/native registry assertions; full dispenser behavior and mod initialization NOT tested");
 }
}
