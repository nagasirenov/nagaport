import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

/** Native MC/Forge test; extracted methods with declared fixture data, NOT full tiles. */
public final class TileNbtMinecraftTest {
    private static void require(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }
    private static final class Payload implements ICapabilitySerializable<NBTTagCompound> {
        int value = 73;
        public boolean hasCapability(Capability<?> capability, EnumFacing facing) { return false; }
        public <T> T getCapability(Capability<T> capability, EnumFacing facing) { return null; }
        public NBTTagCompound serializeNBT() {
            NBTTagCompound result = new NBTTagCompound(); result.setInteger("value", value); return result;
        }
        public void deserializeNBT(NBTTagCompound value) { this.value = value.getInteger("value"); }
    }
    private static Payload attach(TileEntity tile) throws Exception {
        Payload payload = new Payload();
        Map<ResourceLocation, ICapabilityProvider> providers = new LinkedHashMap<>();
        providers.put(new ResourceLocation("nbt_probe", "payload"), payload);
        Field field = TileEntity.class.getDeclaredField("capabilities");
        field.setAccessible(true);
        field.set(tile, new CapabilityDispatcher(providers));
        return payload;
    }
    private static void nativeTags(NBTTagCompound tag, String id) {
        require(tag.getString("id").equals(id), "registered concrete tile identity");
        require(tag.getInteger("x") == -9 && tag.getInteger("y") == 42 && tag.getInteger("z") == 19, "native BlockPos");
    }
    public static void main(String[] args) throws Exception {
        Bootstrap.register();
        TileEntity.register("nbt_probe:furnace", ExtractedTileWriters.Furnace.class);
        TileEntity.register("nbt_probe:early", ExtractedTileWriters.EarlyBranch.class);
        TileEntity.register("nbt_probe:hatch", ExtractedTileWriters.Hatch.class);
        ExtractedTileWriters.Furnace furnace = new ExtractedTileWriters.Furnace();
        furnace.setPos(new BlockPos(-9, 42, 19));
        furnace.muffled = true; furnace.power = 9876543210L; furnace.progress = 57;
        furnace.customName = "NBT native test";
        furnace.slots[1] = new ItemStack(Items.IRON_INGOT, 3, 0);
        NBTTagCompound itemTag = new NBTTagCompound(); itemTag.setString("marker", "kept");
        furnace.slots[1].setTagCompound(itemTag);
        furnace.getTileData().setString("custom", "preserved");
        attach(furnace);
        NBTTagCompound supplied = new NBTTagCompound(); supplied.setString("existing", "retained");
        TileEntity polymorphic = furnace;
        require(polymorphic.writeToNBT(supplied) == supplied, "same instance through inherited virtual dispatch");
        nativeTags(supplied, "nbt_probe:furnace");
        require(supplied.getString("existing").equals("retained"), "retained caller tags");
        require(supplied.getCompoundTag("ForgeData").getString("custom").equals("preserved"), "native ForgeData");
        require(supplied.getCompoundTag("ForgeCaps").getCompoundTag("nbt_probe:payload").getInteger("value") == 73, "native capability serialization");
        require(supplied.getBoolean("muffled") && supplied.getLong("power") == furnace.power && supplied.getInteger("progress") == 57, "all extracted inheritance layers");
        require(supplied.getString("name").equals(furnace.customName), "custom name");
        NBTTagList items = supplied.getTagList("items", 10);
        require(items.tagCount() == 1 && items.getCompoundTagAt(0).getByte("slot") == 1, "null slots skipped; original slot index");
        ItemStack restored = new ItemStack(items.getCompoundTagAt(0));
        require(restored.getItem() == Items.IRON_INGOT && restored.getCount() == 3 && restored.getTagCompound().getString("marker").equals("kept"), "real ItemStack payload round trip");
        require(!supplied.hasKey("cooldown"), "no added cooldown persistence");
        ExtractedTileWriters.Furnace nativeRead = new ExtractedTileWriters.Furnace();
        Payload restoredCap = attach(nativeRead); restoredCap.value = 0;
        nativeRead.readFromNBT(supplied); // Native TileEntity only; no claim about mod reads.
        require(nativeRead.getPos().equals(furnace.getPos()) && restoredCap.value == 73, "native base position/capability read");
        require(nativeRead.getTileData().getString("custom").equals("preserved"), "native ForgeData read");
        ExtractedTileWriters.EarlyBranch early = new ExtractedTileWriters.EarlyBranch();
        early.setPos(new BlockPos(-9, 42, 19)); early.muffled = true;
        NBTTagCompound earlyTag = new NBTTagCompound();
        require(early.writeToNBT(earlyTag) == earlyTag, "null type early return identity");
        nativeTags(earlyTag, "nbt_probe:early");
        require(earlyTag.getBoolean("muffled") && earlyTag.getTagList("items", 10).tagCount() == 0 && !earlyTag.hasKey("name") && !earlyTag.hasKey("machineType"), "early branch retains parent payload and omission");
        early.machineType = "fixture";
        NBTTagCompound configTag = new NBTTagCompound();
        require(early.writeToNBT(configTag) == configTag, "null config early branch identity");
        nativeTags(configTag, "nbt_probe:early");
        require(configTag.getBoolean("muffled") && configTag.getTagList("items", 10).tagCount() == 0 && !configTag.hasKey("name") && !configTag.hasKey("machineType"), "null config preserves parent payload and omissions");
        ExtractedTileWriters.Hatch hatch = new ExtractedTileWriters.Hatch();
        hatch.x = 1; hatch.y = 2; hatch.z = 3;
        NBTTagCompound hatchTag = new NBTTagCompound();
        require(hatch.writeToNBT(hatchTag) == hatchTag && hatchTag.getInteger("x1") == 1 && hatchTag.getInteger("y1") == 2 && hatchTag.getInteger("z1") == 3 && !hatchTag.hasKey("id"), "Hatch legacy no-super behavior retained");
        System.out.println("PASS: native registered identity/position/ForgeData/ForgeCaps, retained input tags and object identity");
        System.out.println("PASS: exact extracted LoadedBase/MachineBase/ElectricFurnace/Hatch writers; CustomMachine early fragment");
        System.out.println("PASS: real ItemStack payload; native-only read roundtrip; missing name and cooldown remain absent");
        System.out.println("LIMIT: method fixtures, NOT full production tiles, CustomMachine normal path, inventory EMPTY migration, or gameplay");
    }
}
