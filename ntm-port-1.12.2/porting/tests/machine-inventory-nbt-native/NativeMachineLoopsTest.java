import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
public class NativeMachineLoopsTest {
    static int checks;
    static void check(boolean ok,String label){checks++;if(!ok)throw new AssertionError(label);}
    static NBTTagCompound record(int slot,String id,int count){NBTTagCompound n=new NBTTagCompound();n.setByte("slot",(byte)slot);n.setString("id",id);n.setByte("Count",(byte)count);n.setShort("Damage",(short)0);return n;}
    public static void main(String[] args) throws Exception {
        Bootstrap.register();
        for(String method:new String[]{"slots","processing","locks"}) {
            java.lang.reflect.Method load=ExtractedMachineLoops.class.getMethod(method,ItemStack[].class,NBTTagList.class);
            ItemStack untouched=new ItemStack(Items.GOLD_INGOT,4);
            ItemStack[] slots={untouched,new ItemStack(Items.GOLD_INGOT),new ItemStack(Items.GOLD_INGOT)};
            NBTTagList list=new NBTTagList();
            list.appendTag(record(-1,"minecraft:diamond",1));list.appendTag(record(3,"minecraft:diamond",1));list.appendTag(record(128,"minecraft:diamond",1));
            list.appendTag(record(1,"minecraft:iron_ingot",3));list.appendTag(record(2,"notregistered:missing",1));
            load.invoke(null,slots,list);
            check(slots[0]==untouched,method+" sparse entries and signed out-of-bounds ignored");
            check(slots[1].getItem()==Items.IRON_INGOT&&slots[1].getCount()==3,method+" bounded valid slot loaded");
            check(slots[2]==null,method+" invalid registry overwrites previous record with vacancy");
            list=new NBTTagList();
            list.appendTag(record(1,"notregistered:missing",1));list.appendTag(record(1,"minecraft:diamond",2));
            list.appendTag(record(2,"minecraft:diamond",1));list.appendTag(record(2,"notregistered:missing",1));
            load.invoke(null,slots,list);
            check(slots[1].getItem()==Items.DIAMOND&&slots[1].getCount()==2&&slots[2]==null,method+" duplicate records remain last-write-wins including null");
            list=new NBTTagList();NBTTagCompound missingSlot=record(0,"minecraft:iron_ingot",2);missingSlot.removeTag("slot");list.appendTag(missingSlot);list.appendTag(record(1,"minecraft:iron_ingot",0));
            load.invoke(null,slots,list);
            check(slots[0].getItem()==Items.IRON_INGOT&&slots[0].getCount()==2,method+" missing slot keeps NBT byte default zero");
            check(slots[1]!=null&&slots[1]!=ItemStack.EMPTY&&slots[1].isEmpty(),method+" registered zero-count not conflated with missing item");
            ItemStack[] fresh=new ItemStack[3];load.invoke(null,fresh,new NBTTagList());
            check(fresh[0]==null&&fresh[1]==null&&fresh[2]==null,method+" fresh empty array stays vacant");
        }
        System.out.println("PASS exact production load-loop slices with real native ItemStack/NBT: "+checks+" checks");
    }
}
