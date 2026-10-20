package com.hbm.tileentity.machine;

import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineElectricFurnace;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerElectricFurnace;
import com.hbm.inventory.gui.GUIMachineElectricFurnace;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class TileEntityMachineElectricFurnace extends TileEntityMachineBase implements ISidedInventory, IEnergyReceiverMK2, IGUIProvider, IUpgradeInfoProvider {

	// HOLY FUCKING SHIT I SPENT 5 DAYS ON THIS SHITFUCK CLASS FILE
	// thanks Martin, vaer and Bob for the help
	public int progress;
	public long power;
	public static final long maxPower = 100000;
	public int maxProgress = 100;
	public int consumption = 50;
	private int cooldown = 0;

	private static final int[] slots_io = new int[] { 0, 1, 2 };

	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT();

	public TileEntityMachineElectricFurnace() {
		super(4);
	}

	@Override
	public boolean shouldRefresh(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState oldState, net.minecraft.block.state.IBlockState newState) {
		// Legacy metadata-only facing changes retained this tile and its inventory.
		return oldState.getBlock() != newState.getBlock();
	}

	@Override
	public String getDefaultName() {
		return "container.electricFurnace";
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		if(i == 0) {
			return itemStack.getItem() instanceof IBatteryItem;
		}

		if(i == 1) {
			return !FurnaceRecipes.instance().getSmeltingResult(itemStack).isEmpty();
		}

		return false;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.power = nbt.getLong("power");
		this.progress = nbt.getInteger("progress");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", power);
		nbt.setInteger("progress", progress);
		return nbt;
	}

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return slots_io;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		if(i == 0)
			if(itemStack.getItem() instanceof IBatteryItem && ((IBatteryItem) itemStack.getItem()).getCharge(itemStack) == 0)
				return true;
		if(i == 2)
			return true;

		return false;
	}

	public int getProgressScaled(int i) {
		return (progress * i) / maxProgress;
	}

	public long getPowerScaled(long i) {
		return (power * i) / maxPower;
	}

	public boolean hasPower() {
		return power >= consumption;
	}

	public boolean isProcessing() {
		return this.progress > 0;
	}

	public boolean canProcess() {

		if(slots[1] == null || slots[1].isEmpty() || cooldown > 0) {
			return false;
		}
		ItemStack itemStack = FurnaceRecipes.instance().getSmeltingResult(this.slots[1]);

		if(itemStack.isEmpty()) {
			return false;
		}

		if(slots[2] == null || slots[2].isEmpty()) {
			return true;
		}

		if(!slots[2].isItemEqual(itemStack)) {
			return false;
		}

		if(slots[2].getCount() < getInventoryStackLimit() && slots[2].getCount() < slots[2].getMaxStackSize()) {
			return true;
		} else {
			return slots[2].getCount() < itemStack.getMaxStackSize();
		}
	}

	private void processItem() {
		if(canProcess()) {
			ItemStack itemStack = FurnaceRecipes.instance().getSmeltingResult(this.slots[1]);

			if(slots[2] == null || slots[2].isEmpty()) {
				slots[2] = itemStack.copy();
			} else if(slots[2].isItemEqual(itemStack)) {
				slots[2].grow(itemStack.getCount());
			}

			for(int i = 1; i < 2; i++) {
				if(slots[i].getCount() <= 0) {
					slots[i] = new ItemStack(slots[i].getItem());
				} else {
					slots[i].shrink(1);
				}
				if(slots[i].getCount() <= 0) {
					slots[i] = null;
				}
			}
		}
	}

	@Override
	public void update() {
		boolean markDirty = false;

		if(!world.isRemote) {

			if(cooldown > 0) {
				cooldown--;
			}

			power = Library.chargeTEFromItems(slots, 0, power, maxPower);

			if(world.getTotalWorldTime() % 40 == 0) this.updateConnections();

			this.consumption = 50;
			this.maxProgress = 100;

			upgradeManager.checkSlots(this, slots, 3, 3);

			int speedLevel = upgradeManager.getLevel(UpgradeType.SPEED);
			int powerLevel = upgradeManager.getLevel(UpgradeType.POWER);

			maxProgress -= speedLevel * 25;
			consumption += speedLevel * 50;
			maxProgress += powerLevel * 10;
			consumption -= powerLevel * 15;

			if(!hasPower()) {
				cooldown = 20;
			}

			if(hasPower() && canProcess()) {
				progress++;

				power -= consumption;

				if(world.getTotalWorldTime() % 20 == 0) PollutionHandler.incrementPollution(world, pos.getX(), pos.getY(), pos.getZ(), PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND);

				if(this.progress >= maxProgress) {
					this.progress = 0;
					this.processItem();
					markDirty = true;
				}
			} else {
				progress = 0;
			}

			boolean trigger = true;

			if(hasPower() && canProcess() && this.progress == 0) {
				trigger = false;
			}

			if(trigger) {
				markDirty = true;
				MachineElectricFurnace.updateBlockState(this.progress > 0, this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());
			}

			this.networkPackNT(50);


			if(markDirty) {
				this.markDirty();
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(maxProgress);
		buf.writeInt(progress);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		maxProgress = buf.readInt();
		progress = buf.readInt();
	}

	private void updateConnections() {

		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			this.trySubscribe(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);
	}

	@Override
	public void setPower(long i) {
		power = i;

	}

	@Override
	public long getPower() {
		return power;

	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerElectricFurnace(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineElectricFurnace(player.inventory, this);
	}

	@Override
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
		return type == UpgradeType.SPEED || type == UpgradeType.POWER;
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_electric_furnace_off));
		if(type == UpgradeType.SPEED) {
			info.add(TextFormatting.GREEN + I18nUtil.resolveKey(this.KEY_DELAY, "-" + (level * 25) + "%"));
			info.add(TextFormatting.RED + I18nUtil.resolveKey(this.KEY_CONSUMPTION, "+" + (level * 100) + "%"));
		}
		if(type == UpgradeType.POWER) {
			info.add(TextFormatting.GREEN + I18nUtil.resolveKey(this.KEY_CONSUMPTION, "-" + (level * 30) + "%"));
			info.add(TextFormatting.RED + I18nUtil.resolveKey(this.KEY_DELAY, "+" + (level * 10) + "%"));
		}
	}

	@Override
	public HashMap<UpgradeType, Integer> getValidUpgrades() {
		HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
		upgrades.put(UpgradeType.SPEED, 3);
		upgrades.put(UpgradeType.POWER, 3);
		return upgrades;
	}
}
