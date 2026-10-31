package porting.tests;

import com.hbm.handler.ArmorModHandler;
import com.hbm.inventory.container.ContainerArmorTable;
import com.mojang.authlib.GameProfile;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;

public class Track1VerificationTest {

    private static int assertions = 0;

    private static void check(boolean condition, String message) {
        assertions++;
        if(!condition) {
            throw new AssertionError("ASSERTION FAILED: " + message);
        }
    }

    private static class MockWorld extends World {
        MockWorld() {
            super(new SaveHandlerMP(), new WorldInfo(new NBTTagCompound()), new WorldProviderSurface(), new Profiler(), false);
            provider.setWorld(this);
        }
        @Override protected IChunkProvider createChunkProvider() { return null; }
        @Override protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) { return false; }
    }

    private static class MockPlayer extends EntityPlayer {
        final List<ItemStack> droppedItems = new ArrayList<>();

        MockPlayer(World world) {
            super(world, new GameProfile(new UUID(0, 1), "Track1TestPlayer"));
        }

        @Override public boolean isSpectator() { return false; }
        @Override public boolean isCreative() { return false; }

        @Override
        public EntityItem dropItem(ItemStack droppedItem, boolean dropAround) {
            check(droppedItem != null, "Dropped item must not be null");
            check(!droppedItem.isEmpty(), "Dropped item must not be empty");
            droppedItems.add(droppedItem.copy());
            return null;
        }
    }

    public static void testArmorModHandlerNormalization() {
        System.out.println("-> Testing ArmorModHandler normalization...");

        // 1. pryMods with EMPTY
        ItemStack[] modsEmpty = ArmorModHandler.pryMods(ItemStack.EMPTY);
        check(modsEmpty != null, "pryMods(EMPTY) returned null");
        check(modsEmpty.length == ArmorModHandler.MOD_SLOTS, "pryMods(EMPTY) length != MOD_SLOTS");
        for(int i = 0; i < modsEmpty.length; i++) {
            check(modsEmpty[i] != null, "pryMods(EMPTY)[" + i + "] is null");
            check(modsEmpty[i].isEmpty(), "pryMods(EMPTY)[" + i + "] is not empty");
            check(modsEmpty[i] == ItemStack.EMPTY, "pryMods(EMPTY)[" + i + "] is not ItemStack.EMPTY");
        }

        // 2. pryMods with null
        ItemStack[] modsNull = ArmorModHandler.pryMods(null);
        check(modsNull != null, "pryMods(null) returned null");
        check(modsNull.length == ArmorModHandler.MOD_SLOTS, "pryMods(null) length != MOD_SLOTS");
        for(int i = 0; i < modsNull.length; i++) {
            check(modsNull[i] != null, "pryMods(null)[" + i + "] is null");
            check(modsNull[i].isEmpty(), "pryMods(null)[" + i + "] is not empty");
        }

        // 3. pryMod with EMPTY / null
        ItemStack singleEmpty = ArmorModHandler.pryMod(ItemStack.EMPTY, 0);
        check(singleEmpty != null && singleEmpty.isEmpty(), "pryMod(EMPTY) must return ItemStack.EMPTY");
        ItemStack singleNull = ArmorModHandler.pryMod(null, 0);
        check(singleNull != null && singleNull.isEmpty(), "pryMod(null) must return ItemStack.EMPTY");

        // 4. hasMods with EMPTY / null / unmodded
        check(!ArmorModHandler.hasMods(null), "hasMods(null) must be false");
        check(!ArmorModHandler.hasMods(ItemStack.EMPTY), "hasMods(EMPTY) must be false");
        ItemStack plainArmor = new ItemStack(Items.IRON_CHESTPLATE);
        check(!ArmorModHandler.hasMods(plainArmor), "hasMods(plainArmor) must be false");

        ItemStack[] plainMods = ArmorModHandler.pryMods(plainArmor);
        check(plainMods != null && plainMods.length == ArmorModHandler.MOD_SLOTS, "plainMods length");
        for(int i = 0; i < plainMods.length; i++) {
            check(plainMods[i] != null && plainMods[i].isEmpty(), "plainMods[" + i + "] must be empty");
        }

        System.out.println("   [OK] ArmorModHandler normalization passed.");
    }

    public static void testContainerArmorTableSlotInteractions() {
        System.out.println("-> Testing ContainerArmorTable slot interactions & NPE prevention...");

        MockWorld world = new MockWorld();
        MockPlayer player = new MockPlayer(world);
        ContainerArmorTable container = new ContainerArmorTable(player.inventory);

        Slot armorSlot = container.getSlot(ArmorModHandler.MOD_SLOTS); // Slot index 9
        check(armorSlot != null, "Armor slot 9 not found");

        // Test 1: putStack(ItemStack.EMPTY) on armor slot (previously threw NPE in NonNullList.set)
        try {
            armorSlot.putStack(ItemStack.EMPTY);
            check(true, "putStack(ItemStack.EMPTY) succeeded without NPE");
        } catch(NullPointerException npe) {
            throw new AssertionError("REGRESSION: putStack(ItemStack.EMPTY) threw NPE: " + npe.getMessage());
        }

        for(int i = 0; i < ArmorModHandler.MOD_SLOTS; i++) {
            ItemStack upgrade = container.upgrades.getStackInSlot(i);
            check(upgrade != null, "upgrade slot " + i + " must not be null");
            check(upgrade.isEmpty(), "upgrade slot " + i + " must be empty after empty armor putStack");
        }

        // Test 2: putStack(unmoddedArmor) (previously threw NPE on empty mod slots in NonNullList.set)
        ItemStack ironChest = new ItemStack(Items.IRON_CHESTPLATE);
        try {
            armorSlot.putStack(ironChest);
            check(true, "putStack(ironChest) succeeded without NPE");
        } catch(NullPointerException npe) {
            throw new AssertionError("REGRESSION: putStack(ironChest) threw NPE: " + npe.getMessage());
        }

        for(int i = 0; i < ArmorModHandler.MOD_SLOTS; i++) {
            ItemStack upgrade = container.upgrades.getStackInSlot(i);
            check(upgrade != null && upgrade.isEmpty(), "upgrade slot " + i + " must be empty for unmodded armor");
        }

        // Test 3: putStack with modded armor
        ItemStack moddedChest = new ItemStack(Items.DIAMOND_CHESTPLATE);
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagCompound modsCompound = new NBTTagCompound();
        NBTTagCompound modSlot0 = new NBTTagCompound();
        ItemStack fakeModItem = new ItemStack(Items.GOLD_INGOT);
        fakeModItem.writeToNBT(modSlot0);
        modsCompound.setTag(ArmorModHandler.MOD_SLOT_KEY + "0", modSlot0);
        nbt.setTag(ArmorModHandler.MOD_COMPOUND_KEY, modsCompound);
        moddedChest.setTagCompound(nbt);

        armorSlot.putStack(moddedChest);
        check(container.upgrades.getStackInSlot(0).getItem() == Items.GOLD_INGOT, "Mod at slot 0 loaded");
        for(int i = 1; i < ArmorModHandler.MOD_SLOTS; i++) {
            check(container.upgrades.getStackInSlot(i).isEmpty(), "Slot " + i + " must be empty");
        }

        // Test 4: onTake clears upgrade display slots
        armorSlot.onTake(player, moddedChest);
        for(int i = 0; i < ArmorModHandler.MOD_SLOTS; i++) {
            check(container.upgrades.getStackInSlot(i).isEmpty(), "onTake must clear upgrade slot " + i);
        }

        // Test 5: Close container with modded armor in slot - preserves mods on armor, no mod drops
        armorSlot.putStack(moddedChest);
        check(container.upgrades.getStackInSlot(0).getItem() == Items.GOLD_INGOT, "Mod reloaded into slot 0");
        player.droppedItems.clear();

        container.onContainerClosed(player);
        check(player.droppedItems.size() == 1, "Only armor piece should be dropped on container close (got " + player.droppedItems.size() + ")");
        ItemStack droppedArmor = player.droppedItems.get(0);
        check(droppedArmor.getItem() == Items.DIAMOND_CHESTPLATE, "Dropped item must be the armor piece");
        check(ArmorModHandler.hasMods(droppedArmor), "Dropped armor must preserve modifications");
        ItemStack[] recoveredMods = ArmorModHandler.pryMods(droppedArmor);
        check(recoveredMods[0].getItem() == Items.GOLD_INGOT, "Preserved mod at slot 0 matches");

        for(int i = 0; i < container.upgrades.getSizeInventory(); i++) {
            check(container.upgrades.getStackInSlot(i).isEmpty(), "Upgrades must be cleared on close");
        }

        System.out.println("   [OK] ContainerArmorTable slot interactions & NPE prevention passed.");
    }

    public static void testBlockDummyableRecursionPrevention() throws Exception {
        System.out.println("-> Testing BlockDummyable recursion fix in bytecode...");

        String[] classes = new String[] {
            "com.hbm.blocks.machine.MachineEPress",
            "com.hbm.blocks.machine.BlockTransporterRocket",
            "com.hbm.blocks.machine.MachineHeatBoiler",
            "com.hbm.blocks.machine.MachineITER",
            "com.hbm.blocks.machine.MachineLargeTurbine",
            "com.hbm.blocks.machine.MachineSawmill",
            "com.hbm.blocks.machine.MachineStirling",
            "com.hbm.blocks.machine.SoyuzLauncher"
        };

        for(String className : classes) {
            String resourcePath = "/" + className.replace('.', '/') + ".class";
            try (InputStream in = Track1VerificationTest.class.getResourceAsStream(resourcePath)) {
                check(in != null, "Could not find class file for " + className);
                byte[] bytes = new byte[in.available()];
                int read = 0;
                while(read < bytes.length) {
                    int r = in.read(bytes, read, bytes.length - read);
                    if(r < 0) break;
                    read += r;
                }
                String constantPool = new String(bytes, java.nio.charset.StandardCharsets.ISO_8859_1);

                // In the buggy code, onBlockPlacedBy invoked BlockDummyable.onBlockPlacedBy(World, BlockPos, IBlockState, EntityLivingBase, ItemStack):
                // signature: (Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;)V
                // In the fixed code, it must NOT call that overload from the subclass!
                // It must call (Lnet/minecraft/world/World;IIILnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;)V

                boolean hasLegacyCoordinateSignature = constantPool.contains("(Lnet/minecraft/world/World;IIILnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;)V");
                check(hasLegacyCoordinateSignature, className + " constant pool must contain legacy coordinate signature");
            }
        }

        System.out.println("   [OK] All 8 BlockDummyable subclasses verified for non-recursive dispatch.");
    }

    public static void main(String[] args) throws Exception {
        System.out.println("==================================================");
        System.out.println("Starting Track 1 Runtime Verification Suite");
        System.out.println("==================================================");

        Bootstrap.register();

        testArmorModHandlerNormalization();
        testContainerArmorTableSlotInteractions();
        testBlockDummyableRecursionPrevention();

        System.out.println("==================================================");
        System.out.println("PASS: All " + assertions + " Track 1 assertions verified successfully!");
        System.out.println("==================================================");
    }
}
