import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import cofh.api.energy.ItemEnergyContainer;
import com.hbm.items.ISatChip;
import net.minecraft.item.Item;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/** Actual mapped Forge ItemStack/NBT and unextracted production ItemEnergyContainer. */
public final class NativeStackNbtTest {
    private static int assertions;
    private static StringBuilder trace = new StringBuilder();
    private static final RuntimeException MARKER = new IllegalStateException("controlled RHS failure");
    private static void check(boolean result, String name) {
        assertions++;
        if (!result) throw new AssertionError(name);
    }
    private static ItemStack receiver(ItemStack stack) { trace.append('R'); return stack; }
    private static ItemStack failReceiver() { trace.append('R'); throw MARKER; }
    private static NBTTagCompound rhs(NBTTagCompound tag) { trace.append('V'); return tag; }
    private static NBTTagCompound failRhs() { trace.append('V'); throw MARKER; }
    private static ItemStack[] array(ItemStack[] array) { trace.append('A'); return array; }
    private static int index(int value) { trace.append('I'); return value; }
    private static void throwsType(Class<? extends Throwable> type, Runnable action, String name) {
        Throwable result = null;
        try { action.run(); } catch (Throwable error) { result=error; }
        check(result != null && type.isInstance(result), name);
    }
    // A language-level direct-field oracle, NOT a replacement Minecraft/platform class.
    private static final class Cell { NBTTagCompound tag; }
    private static Cell cellReceiver(Cell cell) { trace.append('R'); return cell; }
    private static void rawAndSerialization() throws Exception {
        check(Modifier.isFinal(ItemStack.class.getModifiers()), "ItemStack final prevents changed virtual dispatch");
        Field raw=ItemStack.class.getDeclaredField("stackTagCompound"); raw.setAccessible(true);
        ItemStack stack=new ItemStack(Items.IRON_INGOT, 3, 0);
        check(stack.getTagCompound()==null && raw.get(stack)==null, "absent raw nullable tag");
        NBTTagCompound shared=new NBTTagCompound();
        shared.setString("unknown", "kept"); shared.setInteger("value", 7);
        NBTTagCompound nested=new NBTTagCompound(); nested.setLong("large", 9876543210L);
        NBTTagList list=new NBTTagList(); list.appendTag(nested); shared.setTag("nested", list);
        stack.setTagCompound(shared);
        check(stack.getTagCompound()==shared && raw.get(stack)==shared, "setter and getter preserve raw reference");
        shared.setInteger("value", 8);
        check(stack.getTagCompound().getInteger("value")==8, "external alias mutation remains visible");
        stack.getTagCompound().setString("fromGetter", "visible");
        check(shared.getString("fromGetter").equals("visible"), "getter mutation reaches original alias");
        ItemStack twin=new ItemStack(Items.GOLD_INGOT); twin.setTagCompound(stack.getTagCompound());
        check(twin.getTagCompound()==shared, "assignment-style sharing is not copied");
        NBTTagCompound serialized=stack.writeToNBT(new NBTTagCompound());
        ItemStack restored=new ItemStack(serialized);
        check(restored.getItem()==Items.IRON_INGOT && restored.getCount()==3, "native stack identity/count serialization");
        check(restored.getTagCompound().equals(shared), "native tag round trip preserves nested and unknown fields");
        check(restored.getTagCompound()==shared, "native in-memory write/read preserves tag alias");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); CompressedStreamTools.writeCompressed(serialized, bytes);
        ItemStack diskRestored=new ItemStack(CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes.toByteArray())));
        check(diskRestored.getTagCompound().equals(shared) && diskRestored.getTagCompound()!=shared, "native compressed binary round trip preserves value, not in-memory alias");
        ItemStack copied=stack.copy();
        check(copied.getTagCompound().equals(shared) && copied.getTagCompound()!=shared, "native copy remains deep");
        stack.setTagCompound(null);
        check(stack.getTagCompound()==null && raw.get(stack)==null, "setter null clears raw reference");
        check(twin.getTagCompound()==shared, "clearing one stack does not clear shared peer");
        ItemStack absent=new ItemStack(stack.writeToNBT(new NBTTagCompound()));
        check(absent.getTagCompound()==null, "absent tag remains absent in round trip");
        NBTTagCompound emptyTag=new NBTTagCompound(); stack.setTagCompound(emptyTag);
        check(stack.getTagCompound()==emptyTag && stack.hasTagCompound(), "empty tag is not normalized to absent");
        NBTTagCompound saved=ItemStack.EMPTY.getTagCompound();
        try {
            ItemStack.EMPTY.setTagCompound(shared);
            check(ItemStack.EMPTY.getTagCompound()==shared && raw.get(ItemStack.EMPTY)==shared, "EMPTY getter/setter retain raw field behavior");
            ItemStack.EMPTY.setTagCompound(null);
            check(ItemStack.EMPTY.getTagCompound()==null, "EMPTY null is not allocated or normalized");
        } finally { ItemStack.EMPTY.setTagCompound(saved); }
        check(ItemStack.EMPTY.isEmpty(), "EMPTY sentinel remains empty");
    }
    private static void orderAndExceptions() {
        ItemStack stack=new ItemStack(Items.IRON_INGOT); NBTTagCompound tag=new NBTTagCompound();
        trace.setLength(0); receiver(stack).setTagCompound(rhs(tag));
        check(trace.toString().equals("RV") && stack.getTagCompound()==tag, "receiver once before RHS");
        trace.setLength(0); Cell cell=new Cell(); cellReceiver(cell).tag=rhs(tag);
        check(trace.toString().equals("RV") && cell.tag==tag, "direct field oracle has same receiver/RHS order");
        trace.setLength(0); throwsType(NullPointerException.class, () -> receiver(null).setTagCompound(rhs(tag)), "null receiver setter throws NPE");
        check(trace.toString().equals("RV"), "null receiver still evaluates RHS before NPE");
        trace.setLength(0); throwsType(NullPointerException.class, () -> { cellReceiver(null).tag=rhs(tag); }, "direct field null oracle");
        check(trace.toString().equals("RV"), "direct field null oracle also evaluates RHS");
        trace.setLength(0); throwsType(IllegalStateException.class, () -> failReceiver().setTagCompound(rhs(tag)), "receiver failure propagated");
        check(trace.toString().equals("R"), "receiver failure skips RHS");
        trace.setLength(0); throwsType(IllegalStateException.class, () -> receiver(stack).setTagCompound(failRhs()), "RHS failure propagated");
        check(trace.toString().equals("RV") && stack.getTagCompound()==tag, "RHS failure preserves tag");
        trace.setLength(0); throwsType(IllegalStateException.class, () -> receiver(null).setTagCompound(failRhs()), "RHS failure precedes null receiver check");
        check(trace.toString().equals("RV"), "failed RHS evaluated once");
        trace.setLength(0); NBTTagCompound read=receiver(stack).getTagCompound();
        check(trace.toString().equals("R") && read==tag, "getter receiver evaluated once");
        trace.setLength(0); throwsType(NullPointerException.class, () -> receiver(null).getTagCompound(), "getter null receiver throws NPE");
        check(trace.toString().equals("R"), "getter null receiver evaluated once");
        ItemStack[] slots={stack};
        trace.setLength(0); array(slots)[index(0)].setTagCompound(rhs(null));
        check(trace.toString().equals("AIV") && stack.getTagCompound()==null, "array receiver and index before RHS");
        trace.setLength(0); throwsType(ArrayIndexOutOfBoundsException.class, () -> array(slots)[index(2)].setTagCompound(rhs(tag)), "array bound failure retained");
        check(trace.toString().equals("AI"), "array bound failure skips RHS");
        trace.setLength(0); throwsType(NullPointerException.class, () -> array(null)[index(0)].setTagCompound(rhs(tag)), "null array failure retained");
        check(trace.toString().equals("AI"), "null array evaluates index but skips RHS");
    }
    private static void productionEnergyContainer() {
        ItemEnergyContainer item=new ItemEnergyContainer(100, 30, 20);
        ItemStack stack=new ItemStack(Items.IRON_INGOT);
        check(item.getEnergyStored(stack)==0 && stack.getTagCompound()==null, "actual production read leaves absent tag absent");
        check(item.extractEnergy(stack, 10, false)==0 && stack.getTagCompound()==null, "actual production extraction preserves absent tag");
        check(item.receiveEnergy(stack, 50, true)==30, "actual production simulated receive amount");
        check(stack.getTagCompound()!=null && !stack.getTagCompound().hasKey("Energy"), "preserved existing behavior: simulation allocates empty tag");
        NBTTagCompound shared=stack.getTagCompound(); shared.setString("unknown", "kept");
        check(item.receiveEnergy(stack, 50, false)==30 && item.getEnergyStored(stack)==30, "actual production receive writes energy");
        check(stack.getTagCompound()==shared && shared.getInteger("Energy")==30, "actual production receive preserves alias");
        check(item.extractEnergy(stack, 50, true)==20 && item.getEnergyStored(stack)==30, "actual production simulated extract does not debit");
        check(item.extractEnergy(stack, 50, false)==20 && item.getEnergyStored(stack)==10, "actual production extract debits only transferred amount");
        check(shared.getString("unknown").equals("kept"), "actual production leaves unrelated payload intact");
        shared.setInteger("Energy", 95);
        check(item.receiveEnergy(stack, 100, false)==5 && item.getEnergyStored(stack)==100, "actual production capacity boundary");
        ItemStack restored=new ItemStack(stack.writeToNBT(new NBTTagCompound()));
        check(item.getEnergyStored(restored)==100 && restored.getTagCompound().getString("unknown").equals("kept"), "actual production energy survives native stack round trip");
        shared.removeTag("Energy");
        check(item.extractEnergy(stack, 10, false)==0 && item.getEnergyStored(stack)==0, "actual production missing energy key behavior");
        throwsType(NullPointerException.class, () -> item.getEnergyStored(null), "actual production null argument exception unchanged");
    }
    private static final class NativeChip extends Item implements ISatChip { }
    private static void productionSatChip() {
        ISatChip chip=new NativeChip();
        ItemStack stack=new ItemStack((Item)chip);
        check(chip.getFreq(stack)==0 && stack.getTagCompound()!=null, "actual ISatChip read lazily creates tag exactly as before");
        NBTTagCompound alias=stack.getTagCompound(); alias.setString("unknown", "retained");
        chip.setFreq(stack, -77);
        check(chip.getFreq(stack)==-77 && stack.getTagCompound()==alias, "actual ISatChip write preserves same tag and negative frequency");
        check(alias.getInteger("freq")==-77 && alias.getString("unknown").equals("retained"), "actual ISatChip payload and unrelated data");
        ISatChip.setFreqS(stack, 12345);
        check(ISatChip.getFreqS(stack)==12345, "actual ISatChip static dispatch through native ItemStack item");
        stack.setTagCompound(null); chip.setFreq(stack, 0);
        check(stack.getTagCompound()!=null && stack.getTagCompound().hasKey("freq") && chip.getFreq(stack)==0, "actual ISatChip absent-tag write creates explicit zero frequency");
        check(ISatChip.getFreqS(null)==0, "actual ISatChip static null read remains guarded");
        ISatChip.setFreqS(null, 3);
        check(ISatChip.getFreqS(new ItemStack(Items.IRON_INGOT))==0, "actual ISatChip static non-chip remains guarded");
        throwsType(NullPointerException.class, () -> chip.getFreq(null), "actual ISatChip direct null read exception unchanged");
    }
    public static void main(String[] args) throws Exception {
        Bootstrap.register();
        rawAndSerialization(); orderAndExceptions(); productionEnergyContainer(); productionSatChip();
        System.out.println("PASS native ItemStack/NBT + actual production ItemEnergyContainer + ISatChip: "+assertions+" assertions");
    }
}
