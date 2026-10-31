import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockBedrockOreTE.TileEntityBedrockOre;
import com.hbm.blocks.generic.BlockRebar.TileEntityRebar;
import com.hbm.blocks.machine.*;
import com.hbm.blocks.machine.BlockICF.TileEntityBlockICF;
import com.hbm.blocks.machine.BlockPWR.TileEntityBlockPWR;
import com.hbm.blocks.network.*;
import com.hbm.entity.cart.EntityMinecartContainerBase;
import com.hbm.inventory.container.*;
import com.hbm.inventory.container.ContainerCrateBase;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemRebarPlacer;
import com.hbm.items.tool.ItemRebarPlacer.ContainerRebar;
import com.hbm.items.tool.ItemRebarPlacer.InventoryRebar;
import com.hbm.items.weapon.ItemCustomMissile;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKSlottedBase;
import com.hbm.tileentity.machine.storage.TileEntityCrateBase;
import com.hbm.tileentity.network.TileEntityDroneCrate;
import com.hbm.util.PersistentRegistryRef;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;

import java.util.UUID;

public class Session2VerificationMinecraftTest {

	private static int assertions = 0;

	private static void check(boolean condition, String message) {
		assertions++;
		if(!condition) {
			throw new AssertionError("FAILED: " + message);
		}
	}

	static class DummyWorld extends World {
		DummyWorld() {
			super(new SaveHandlerMP(), new WorldInfo(new NBTTagCompound()), new WorldProviderSurface(), new Profiler(), false);
			provider.setWorld(this);
		}
		@Override protected IChunkProvider createChunkProvider() { return null; }
		@Override protected boolean isChunkLoaded(int x, int z, boolean allow) { return false; }
	}

	static class DummyPlayer extends EntityPlayer {
		DummyPlayer(World world) {
			super(world, new GameProfile(new UUID(0, 42), "Session2Worker"));
		}
		@Override public boolean isSpectator() { return false; }
		@Override public boolean isCreative() { return capabilities.isCreativeMode; }
	}

	static class ConcreteCrate extends TileEntityCrateBase {
		ConcreteCrate(int count) { super(count); }
		@Override public String getName() { return "container.testCrate"; }
		@Override public int[] getSlotsForFace(EnumFacing side) { return new int[0]; }
		@Override public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) { return true; }
		@Override public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) { return true; }
		@Override public net.minecraft.inventory.Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) { return null; }
		@Override public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) { return null; }
		@Override public int getField(int id) { return 0; }
		@Override public void setField(int id, int value) {}
		@Override public int getFieldCount() { return 0; }
	}

	static class ConcreteRBMKSlotted extends TileEntityRBMKSlottedBase {
		ConcreteRBMKSlotted(int count) { super(count); }
		@Override public String getName() { return "container.testRBMK"; }
		@Override public int[] getSlotsForFace(EnumFacing side) { return new int[0]; }
		@Override public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) { return true; }
		@Override public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) { return true; }
		@Override public net.minecraft.inventory.Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) { return null; }
		@Override public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) { return null; }
		@Override public com.hbm.tileentity.machine.rbmk.TileEntityRBMKConsole.ColumnType getConsoleType() { return com.hbm.tileentity.machine.rbmk.TileEntityRBMKConsole.ColumnType.BLANK; }
		@Override public int getField(int id) { return 0; }
		@Override public void setField(int id, int value) {}
		@Override public int getFieldCount() { return 0; }
	}

	static class ConcreteMinecartContainer extends EntityMinecartContainerBase {
		ConcreteMinecartContainer(World world) { super(world); }
		@Override public int getSizeInventory() { return 5; }
		@Override public net.minecraft.entity.item.EntityMinecart.Type getType() { return net.minecraft.entity.item.EntityMinecart.Type.CHEST; }
	}

	public static void main(String[] args) {
		Bootstrap.register();
		net.minecraft.tileentity.TileEntity.register("test_concrete_crate", ConcreteCrate.class);
		net.minecraft.tileentity.TileEntity.register("test_rbmk_slotted", ConcreteRBMKSlotted.class);
		net.minecraft.tileentity.TileEntity.register("hbm_test_rebar", TileEntityRebar.class);
		net.minecraft.tileentity.TileEntity.register("hbm_test_icf", TileEntityBlockICF.class);
		net.minecraft.tileentity.TileEntity.register("hbm_test_pwr", TileEntityBlockPWR.class);
		net.minecraft.tileentity.TileEntity.register("hbm_test_bedrock_ore", TileEntityBedrockOre.class);

		DummyWorld world = new DummyWorld();
		DummyPlayer player = new DummyPlayer(world);

		System.out.println("Running Session 2 verification suite...");

		testContainerRebarFix(player);
		testContainerDroneCrateFix(player);
		testTargetContainers(player, world);
		testMachineMetadataCodecs();
		testNetworkBlocks();
		testLegacyNumericPreservation();

		System.out.println("PASS: All " + assertions + " Session 2 assertions verified successfully!");
	}

	// 1. ContainerRebar Fix Verification
	private static void testContainerRebarFix(DummyPlayer player) {
		ItemStack box = new ItemStack(Items.STICK);
		box.setTagCompound(new NBTTagCompound());
		InventoryRebar invRebar = new InventoryRebar(player, box);
		ContainerRebar container = new ContainerRebar(player.inventory, invRebar);

		// Verified: transferStackInSlot returns ItemStack.EMPTY (never null)
		ItemStack shiftResult = container.transferStackInSlot(player, 0);
		check(shiftResult != null, "transferStackInSlot must not return null");
		check(shiftResult == ItemStack.EMPTY, "transferStackInSlot must return ItemStack.EMPTY");

		// Verified: slotClick on empty slot 0 returns ItemStack.EMPTY (never null)
		player.inventory.setItemStack(ItemStack.EMPTY);
		ItemStack clickEmpty = container.slotClick(0, 0, ClickType.PICKUP, player);
		check(clickEmpty != null, "slotClick on empty slot 0 must not return null");
		check(clickEmpty.isEmpty(), "slotClick on empty slot 0 must return empty stack");

		// Verified: placing item into slot 0
		ItemStack held = new ItemStack(Items.IRON_INGOT, 1);
		player.inventory.setItemStack(held);
		ItemStack placed = container.slotClick(0, 0, ClickType.PICKUP, player);
		check(placed != null && placed.isEmpty(), "slotClick placing item returns empty");
		check(invRebar.getStackInSlot(0).getItem() == Items.IRON_INGOT, "slot 0 received item");
		System.out.println("  [OK] ContainerRebar defect fix & empty-safe click verified");
	}

	// 2. ContainerDroneCrate Fix Verification
	private static void testContainerDroneCrateFix(DummyPlayer player) {
		TileEntityDroneCrate crate = new TileEntityDroneCrate();
		ContainerDroneCrate container = new ContainerDroneCrate(player.inventory, crate);

		// Populate player hotbar slot 0 with 10 apples
		ItemStack apples = new ItemStack(Items.APPLE, 10);
		player.inventory.setInventorySlotContents(0, apples);

		// Slot index for hotbar slot 0:
		// 18 crate slots (0..17) + 1 fluid id slot (18) + 27 player main inv (19..45) + hotbar (46..54)
		int hotbarSlot0 = 46;
		Slot hotbarSlot = container.getSlot(hotbarSlot0);
		check(hotbarSlot.getHasStack(), "hotbar slot 0 has stack initially");
		check(hotbarSlot.getStack().getCount() == 10, "hotbar slot 0 count is 10");

		// Shift-click player hotbar slot 0 into crate
		ItemStack transferred = container.transferStackInSlot(player, hotbarSlot0);

		// Assert that shift-click returned the transferred stack (10 apples)
		check(transferred != null && !transferred.isEmpty(), "transferStackInSlot returned transferred stack");
		check(transferred.getItem() == Items.APPLE && transferred.getCount() == 10, "transferred stack is 10 apples");

		// CRITICAL DEFECT VERIFICATION: player slot MUST be cleared to ItemStack.EMPTY
		check(!hotbarSlot.getHasStack(), "player hotbar slot was cleared by var4.putStack(ItemStack.EMPTY)");
		check(hotbarSlot.getStack().isEmpty(), "player hotbar slot stack is empty");

		// Verify crate received the 10 apples in slot 0
		check(crate.getStackInSlot(0).getItem() == Items.APPLE, "crate slot 0 has apples");
		check(crate.getStackInSlot(0).getCount() == 10, "crate slot 0 has 10 apples");
		System.out.println("  [OK] ContainerDroneCrate shift-click slot clearing fix verified");
	}

	// 3. Representative Containers Verification
	private static void testTargetContainers(DummyPlayer player, DummyWorld world) {
		// TileEntityCrateBase
		ConcreteCrate crate = new ConcreteCrate(18);
		check(crate.isEmpty(), "crate starts empty");
		check(crate.getStackInSlot(0) == ItemStack.EMPTY, "getStackInSlot returns EMPTY for null slot");

		ItemStack stick = new ItemStack(Items.STICK, 100);
		crate.setInventorySlotContents(0, stick);
		check(crate.getStackInSlot(0).getCount() == 64, "setInventorySlotContents clamped to 64");
		check(!crate.isEmpty(), "crate is not empty");

		ItemStack split = crate.decrStackSize(0, 14);
		check(split.getCount() == 14, "decrStackSize split 14");
		check(crate.getStackInSlot(0).getCount() == 50, "crate remaining is 50");

		// NBT omission test: writeToNBT only writes occupied slots
		NBTTagCompound crateNbt = new NBTTagCompound();
		crate.writeToNBT(crateNbt);
		NBTTagList itemsList = crateNbt.getTagList("items", 10);
		check(itemsList.tagCount() == 1, "only 1 occupied slot written to NBT");

		// Read back
		ConcreteCrate loadedCrate = new ConcreteCrate(18);
		loadedCrate.readFromNBT(crateNbt);
		check(loadedCrate.getStackInSlot(0).getCount() == 50, "loaded crate has 50 sticks");
		check(loadedCrate.getStackInSlot(1) == ItemStack.EMPTY, "unoccupied slot loads as EMPTY");

		// TileEntityRBMKSlottedBase
		ConcreteRBMKSlotted rbmk = new ConcreteRBMKSlotted(4);
		check(rbmk.isEmpty(), "rbmk starts empty");
		rbmk.setInventorySlotContents(0, new ItemStack(Items.COAL, 5));
		check(!rbmk.isEmpty() && rbmk.getStackInSlot(0).getCount() == 5, "rbmk slot 0 populated");
		rbmk.setInventorySlotContents(0, ItemStack.EMPTY);
		check(rbmk.isEmpty() && rbmk.getStackInSlot(0) == ItemStack.EMPTY, "rbmk EMPTY sets null and returns EMPTY");

		// ContainerAmmoBag self-lock
		ItemStack bag = new ItemStack(Items.LEATHER);
		player.inventory.currentItem = 2;
		player.inventory.setInventorySlotContents(2, bag);
		ContainerAmmoBag ammoBag = new ContainerAmmoBag(player.inventory, new com.hbm.items.tool.ItemAmmoBag.InventoryAmmoBag(bag));
		// Hotbar swap attempt on open bag slot
		ItemStack bagSwapResult = ammoBag.slotClick(0, 2, ClickType.SWAP, player);
		check(bagSwapResult == ItemStack.EMPTY, "ammo bag denies SWAP hotbar into current bag slot");

		// EntityMinecartContainerBase
		ConcreteMinecartContainer cart = new ConcreteMinecartContainer(world);
		cart.setInventorySlotContents(0, new ItemStack(Items.GOLD_INGOT, 3));
		NBTTagCompound cartNbt = new NBTTagCompound();
		cart.writeToNBT(cartNbt);
		ConcreteMinecartContainer loadedCart = new ConcreteMinecartContainer(world);
		loadedCart.readFromNBT(cartNbt);
		check(loadedCart.getStackInSlot(0).getItem() == Items.GOLD_INGOT && loadedCart.getStackInSlot(0).getCount() == 3, "minecart container persisted item correctly");
		check(loadedCart.getStackInSlot(1) == ItemStack.EMPTY, "empty minecart slot loaded as EMPTY");

		System.out.println("  [OK] Target containers (CrateBase, RBMK, AmmoBag, Minecart) verified");
	}

	// 4. Machine Metadata Codecs Verification
	private static void testMachineMetadataCodecs() {
		// MachineBattery: FACING 0..5
		MachineBattery battery = new MachineBattery(Material.IRON, 100_000);
		for(int meta = 0; meta <= 5; meta++) {
			IBlockState state = battery.getStateFromMeta(meta);
			check(battery.getMetaFromState(state) == meta, "MachineBattery meta " + meta + " roundtrip");
		}
		check(battery.getMetaFromState(battery.getStateFromMeta(99)) == 0, "MachineBattery out-of-range fallback to 0");

		// MachineBoiler: FACING 0..5
		MachineBoiler boiler = new MachineBoiler(false);
		for(int meta = 0; meta <= 5; meta++) {
			IBlockState state = boiler.getStateFromMeta(meta);
			check(boiler.getMetaFromState(state) == meta, "MachineBoiler meta " + meta + " roundtrip");
		}
		check(boiler.getMetaFromState(boiler.getStateFromMeta(99)) == 0, "MachineBoiler out-of-range fallback to 0");

		// BlockCustomMachine: FACING 0..5
		BlockCustomMachine customMachine = new BlockCustomMachine();
		for(int meta = 0; meta <= 5; meta++) {
			IBlockState state = customMachine.getStateFromMeta(meta);
			check(customMachine.getMetaFromState(state) == meta, "BlockCustomMachine meta " + meta + " roundtrip");
		}
		check(customMachine.getMetaFromState(customMachine.getStateFromMeta(99)) == 0, "BlockCustomMachine out-of-range fallback to 0");

		// MachineFan: 6-way FACING 0..5
		MachineFan fan = new MachineFan();
		for(int meta = 0; meta <= 5; meta++) {
			IBlockState state = fan.getStateFromMeta(meta);
			check(fan.getMetaFromState(state) == meta, "MachineFan meta " + meta + " roundtrip");
		}
		check(fan.getMetaFromState(fan.getStateFromMeta(-1)) == 0, "MachineFan negative fallback to 0");

		// BlockCMAnchor: FACING 0..5
		BlockCMAnchor anchor = new BlockCMAnchor();
		for(int meta = 0; meta <= 5; meta++) {
			IBlockState state = anchor.getStateFromMeta(meta);
			check(anchor.getMetaFromState(state) == meta, "BlockCMAnchor meta " + meta + " roundtrip");
		}
		check(anchor.getMetaFromState(anchor.getStateFromMeta(12)) == 0, "BlockCMAnchor out-of-range fallback to 0");

		// FluidPump: FACING 0..5
		FluidPump pump = new FluidPump(Material.IRON);
		for(int meta = 0; meta <= 5; meta++) {
			IBlockState state = pump.getStateFromMeta(meta);
			check(pump.getMetaFromState(state) == meta, "FluidPump meta " + meta + " roundtrip");
		}
		check(pump.getMetaFromState(pump.getStateFromMeta(7)) == 0, "FluidPump out-of-range fallback to 0");

		// BlockMassStorage: packed 0..15 META combining 4 subtypes and 4 facings
		BlockMassStorage massStorage = new BlockMassStorage();
		for(int meta = 0; meta <= 15; meta++) {
			IBlockState state = massStorage.getStateFromMeta(meta);
			check(massStorage.getMetaFromState(state) == meta, "BlockMassStorage meta " + meta + " roundtrip");
		}
		// Test capacity calculation per rectified subtype: 0->10k, 1->100k, 2->1M, 3->100
		check(massStorage.getCapacity(0) == 10_000, "mass storage iron capacity");
		check(massStorage.getCapacity(1) == 100_000, "mass storage steel capacity");
		check(massStorage.getCapacity(2) == 1_000_000, "mass storage titanium capacity");
		check(massStorage.getCapacity(3) == 100, "mass storage crate capacity");

		System.out.println("  [OK] Machine metadata codecs (Battery, Boiler, CustomMachine, Fan, Anchor, Pump, MassStorage) verified");
	}

	// 5. Network Blocks Verification
	private static void testNetworkBlocks() {
		// CableSwitch: ACTIVE boolean property
		CableSwitch cableSwitch = new CableSwitch(Material.IRON);
		IBlockState csOff = cableSwitch.getStateFromMeta(0);
		IBlockState csOn = cableSwitch.getStateFromMeta(1);
		check(!csOff.getValue(CableSwitch.ACTIVE), "CableSwitch meta 0 is inactive");
		check(csOn.getValue(CableSwitch.ACTIVE), "CableSwitch meta 1 is active");
		check(cableSwitch.getMetaFromState(csOff) == 0, "CableSwitch inactive maps to 0");
		check(cableSwitch.getMetaFromState(csOn) == 1, "CableSwitch active maps to 1");

		// CableDetector: ACTIVE boolean property
		CableDetector cableDetector = new CableDetector(Material.IRON);
		IBlockState cdOff = cableDetector.getStateFromMeta(0);
		IBlockState cdOn = cableDetector.getStateFromMeta(1);
		check(!cdOff.getValue(CableDetector.ACTIVE), "CableDetector meta 0 is inactive");
		check(cdOn.getValue(CableDetector.ACTIVE), "CableDetector meta 1 is active");
		check(cableDetector.getMetaFromState(cdOff) == 0, "CableDetector inactive maps to 0");
		check(cableDetector.getMetaFromState(cdOn) == 1, "CableDetector active maps to 1");

		// FluidValve: OPEN boolean property
		FluidValve fluidValve = new FluidValve(Material.IRON);
		IBlockState fvClosed = fluidValve.getStateFromMeta(0);
		IBlockState fvOpen = fluidValve.getStateFromMeta(1);
		check(!fvClosed.getValue(FluidValve.OPEN), "FluidValve meta 0 is closed");
		check(fvOpen.getValue(FluidValve.OPEN), "FluidValve meta 1 is open");
		check(fluidValve.getMetaFromState(fvClosed) == 0, "FluidValve closed maps to 0");
		check(fluidValve.getMetaFromState(fvOpen) == 1, "FluidValve open maps to 1");

		// FluidSwitch: OPEN boolean property
		FluidSwitch fluidSwitch = new FluidSwitch(Material.IRON);
		IBlockState fsClosed = fluidSwitch.getStateFromMeta(0);
		IBlockState fsOpen = fluidSwitch.getStateFromMeta(1);
		check(!fsClosed.getValue(FluidSwitch.OPEN), "FluidSwitch meta 0 is closed");
		check(fsOpen.getValue(FluidSwitch.OPEN), "FluidSwitch meta 1 is open");
		check(fluidSwitch.getMetaFromState(fsClosed) == 0, "FluidSwitch closed maps to 0");
		check(fluidSwitch.getMetaFromState(fsOpen) == 1, "FluidSwitch open maps to 1");

		System.out.println("  [OK] Network blocks (CableSwitch, CableDetector, FluidValve, FluidSwitch) verified");
	}

	// 6. Legacy Numeric ID Preservation Verification
	private static void testLegacyNumericPreservation() {
		// PersistentRegistryRef schema v2 and legacy numeric checks
		NBTTagCompound testTag = new NBTTagCompound();
		PersistentRegistryRef.writeBlock(testTag, "testBlock", Blocks.STONE);
		check(testTag.hasKey("testBlock_name", 8), "writeBlock writes _name string key");
		check(testTag.getString("testBlock_name").equals("minecraft:stone"), "correct registry name written");
		check(testTag.getByte("hbmRefSchema") == 2, "schema version 2 written");
		check(PersistentRegistryRef.readBlock(testTag, "testBlock") == Blocks.STONE, "readBlock retrieves stone");

		// Legacy numeric tag detection
		NBTTagCompound legacyTag = new NBTTagCompound();
		legacyTag.setInteger("testBlock", 42); // legacy numeric id
		check(PersistentRegistryRef.hasLegacyNumeric(legacyTag, "testBlock"), "detects legacy numeric tag");
		check(PersistentRegistryRef.readBlock(legacyTag, "testBlock") == null, "readBlock returns null without mapping legacy numeric id");

		// preserveLegacy
		NBTTagCompound destination = new NBTTagCompound();
		PersistentRegistryRef.preserveLegacy(legacyTag, destination, "testBlock");
		check(destination.getInteger("testBlock") == 42, "preserveLegacy preserved raw numeric tag");

		// BlockRebar (TileEntityRebar) legacy preservation
		TileEntityRebar rebar = new TileEntityRebar();
		NBTTagCompound rebarLegacyNbt = new NBTTagCompound();
		rebarLegacyNbt.setInteger("block", 98); // legacy stone brick
		rebarLegacyNbt.setInteger("meta", 1);
		rebarLegacyNbt.setInteger("progress", 250);
		rebarLegacyNbt.setBoolean("hasConnection", true);
		rebar.readFromNBT(rebarLegacyNbt);

		check(rebar.concrete == null, "legacy numeric rebar block is unresolved (null)");
		check(rebar.progress == 250, "rebar progress read correctly");

		NBTTagCompound rebarWritten = new NBTTagCompound();
		rebar.writeToNBT(rebarWritten);
		check(rebarWritten.getInteger("block") == 98, "rebar writeToNBT preserved legacy numeric block id");
		check(rebarWritten.getInteger("meta") == 1, "rebar writeToNBT preserved legacy meta");

		// BlockICF (TileEntityBlockICF) legacy preservation
		TileEntityBlockICF icf = new TileEntityBlockICF();
		NBTTagCompound icfLegacyNbt = new NBTTagCompound();
		icfLegacyNbt.setInteger("block", 152); // legacy redstone block
		icfLegacyNbt.setInteger("meta", 0);
		icfLegacyNbt.setInteger("cX", 100);
		icfLegacyNbt.setInteger("cY", 64);
		icfLegacyNbt.setInteger("cZ", -200);
		icf.readFromNBT(icfLegacyNbt);
		check(icf.block == null, "legacy ICF block is unresolved");

		NBTTagCompound icfWritten = new NBTTagCompound();
		icf.writeToNBT(icfWritten);
		check(icfWritten.getInteger("block") == 152, "ICF writeToNBT preserved legacy block");
		check(icfWritten.getInteger("cX") == 100 && icfWritten.getInteger("cY") == 64 && icfWritten.getInteger("cZ") == -200, "ICF preserved core coordinates");

		// BlockPWR (TileEntityBlockPWR) legacy preservation
		TileEntityBlockPWR pwr = new TileEntityBlockPWR();
		NBTTagCompound pwrLegacyNbt = new NBTTagCompound();
		pwrLegacyNbt.setInteger("block", 200);
		pwrLegacyNbt.setInteger("cX", 50);
		pwrLegacyNbt.setInteger("cY", 40);
		pwrLegacyNbt.setInteger("cZ", 30);
		pwr.readFromNBT(pwrLegacyNbt);
		check(pwr.block == null, "legacy PWR block is unresolved");

		NBTTagCompound pwrWritten = new NBTTagCompound();
		pwr.writeToNBT(pwrWritten);
		check(pwrWritten.getInteger("block") == 200, "PWR writeToNBT preserved legacy block");
		check(pwrWritten.getInteger("cX") == 50 && pwrWritten.getInteger("cY") == 40 && pwrWritten.getInteger("cZ") == 30, "PWR preserved core coordinates");

		// BlockBedrockOreTE (TileEntityBedrockOre) legacy preservation
		TileEntityBedrockOre bedrockOre = new TileEntityBedrockOre();
		NBTTagCompound oreLegacyNbt = new NBTTagCompound();
		oreLegacyNbt.setInteger("0id", 265); // iron ingot in 1.7
		oreLegacyNbt.setByte("size", (byte) 3);
		oreLegacyNbt.setShort("meta", (short) 0);
		oreLegacyNbt.setInteger("tier", 2);
		bedrockOre.readFromNBT(oreLegacyNbt);

		NBTTagCompound oreWritten = new NBTTagCompound();
		bedrockOre.writeToNBT(oreWritten);
		check(oreWritten.getInteger("0id") == 265, "BedrockOre writeToNBT preserved 0id");
		check(oreWritten.getByte("size") == 3, "BedrockOre preserved size");

		// ItemCustomMissile legacy part preservation
		ItemStack missile = new ItemStack(Items.STICK);
		NBTTagCompound missileNbt = new NBTTagCompound();
		missileNbt.setInteger("chip", 501);
		missileNbt.setInteger("warhead", 502);
		missileNbt.setInteger("fuselage", 503);
		missileNbt.setInteger("thruster", 504);
		missile.setTagCompound(missileNbt);

		check(ItemCustomMissile.hasUnresolvedLegacyPart(missile, "chip"), "detected unresolved legacy chip");
		check(ItemCustomMissile.hasUnresolvedLegacyPart(missile, "warhead"), "detected unresolved legacy warhead");
		check(ItemCustomMissile.readPart(missile, "chip") == null, "readPart returns null for legacy numeric id");

		System.out.println("  [OK] Legacy numeric ID preservation (BlockRebar, BlockICF, BlockPWR, BedrockOre, CustomMissile) verified");
	}
}
