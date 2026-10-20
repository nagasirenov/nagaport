package com.hbm.tileentity.machine;

import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerFurnaceIron;
import com.hbm.inventory.gui.GUIFurnaceIron;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.module.ModuleBurnTime;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.i18n.I18nUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class TileEntityFurnaceIron extends TileEntityMachineBase implements IGUIProvider, IUpgradeInfoProvider {

	public int maxBurnTime;
	public int burnTime;
	public boolean wasOn = false;

	public int progress;
	public int processingTime;
	public static final int baseTime = 160;

	public ModuleBurnTime burnModule;

	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT();

	public TileEntityFurnaceIron() {
		super(5);

		burnModule = new ModuleBurnTime()
				.setLigniteTimeMod(1.25)
				.setCoalTimeMod(1.25)
				.setCokeTimeMod(1.5)
				.setSolidTimeMod(2)
				.setRocketTimeMod(2)
				.setBalefireTimeMod(2);
	}

	@Override
	public String getDefaultName() {
		return "container.furnaceIron";
	}

	@Override
	public void update() {

		if(!world.isRemote) {

			upgradeManager.checkSlots(this, slots, 4, 4);
			this.processingTime = baseTime - ((baseTime / 2) * upgradeManager.getLevel(UpgradeType.SPEED) / 3);

			wasOn = false;

			if(burnTime <= 0) {

				for(int i = 1; i < 3; i++) {
					if(slots[i] != null && !slots[i].isEmpty()) {

						int fuel = burnModule.getBurnTime(slots[i]);

						if(fuel > 0) {
							this.maxBurnTime = this.burnTime = fuel;
							// Invoke the captured item with the original, already-decremented stack.
							Item fuelItem = slots[i].getItem();
							slots[i].shrink(1);

							if(slots[i].getCount() == 0) {
								slots[i] = fuelItem.getContainerItem(slots[i]);
								if(slots[i] != null && slots[i].isEmpty()) slots[i] = null;
							}

							break;
						}
					}
				}
			}

			if(canSmelt() && breatheAir(world.getTotalWorldTime() % 5 == 0 ? 1 : 0)) {
				wasOn = true;
				this.progress++;
				this.burnTime--;

				if(this.progress % 15 == 0 && !this.muffled) {
					world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 1.0F, 0.5F + world.rand.nextFloat() * 0.5F);
				}

				if(this.progress >= this.processingTime) {
					ItemStack result = FurnaceRecipes.instance().getSmeltingResult(slots[0]);

					if((slots[3] == null || slots[3].isEmpty())) {
						slots[3] = result.copy();
					} else {
						slots[3].grow(result.getCount());
					}

					this.decrStackSize(0, 1);

					this.progress = 0;
					this.markDirty();
				}
				if(world.getTotalWorldTime() % 20 == 0) PollutionHandler.incrementPollution(world, pos.getX(), pos.getY(), pos.getZ(), PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND);
			} else {
				this.progress = 0;
			}

			this.networkPackNT(50);
		} else {

			if(this.progress > 0) {
				ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
				ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

				double offset = this.progress % 2 == 0 ? 1 : 0.5;
				world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + 0.5 - dir.offsetX * offset - rot.offsetX * 0.1875, pos.getY() + 2, pos.getZ() + 0.5 - dir.offsetZ * offset - rot.offsetZ * 0.1875, 0.0, 0.01, 0.0);

				if(this.progress % 5 == 0) {
					double rand = world.rand.nextDouble();
					world.spawnParticle(EnumParticleTypes.FLAME, pos.getX() + 0.5 + dir.offsetX * 0.25 + rot.offsetX * rand, pos.getY() + 0.25 + world.rand.nextDouble() * 0.25, pos.getZ() + 0.5 + dir.offsetZ * 0.25 + rot.offsetZ * rand, 0.0, 0.0, 0.0);
				}
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(this.maxBurnTime);
		buf.writeInt(this.burnTime);
		buf.writeInt(this.progress);
		buf.writeInt(this.processingTime);
		buf.writeBoolean(this.wasOn);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.maxBurnTime = buf.readInt();
		this.burnTime = buf.readInt();
		this.progress = buf.readInt();
		this.processingTime = buf.readInt();
		this.wasOn = buf.readBoolean();
	}

	public boolean canSmelt() {

		if(this.burnTime <= 0) return false;
		if((slots[0] == null || slots[0].isEmpty())) return false;

		ItemStack result = FurnaceRecipes.instance().getSmeltingResult(slots[0]);

		if(result.isEmpty()) return false;
		if((slots[3] == null || slots[3].isEmpty())) return true;

		if(!result.isItemEqual(slots[3])) return false;
		if(result.getCount() + slots[3].getCount() > slots[3].getMaxStackSize()) return false;

		return true;
	}

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int meta = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return new int[] { 0, 1, 2, 3 };
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {

		if(i == 0)
			return !FurnaceRecipes.instance().getSmeltingResult(itemStack).isEmpty();

		if(i < 3)
			return burnModule.getBurnTime(itemStack) > 0;

		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return i == 3;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.maxBurnTime = nbt.getInteger("maxBurnTime");
		this.burnTime = nbt.getInteger("burnTime");
		this.progress = nbt.getInteger("progress");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setInteger("maxBurnTime", maxBurnTime);
		nbt.setInteger("burnTime", burnTime);
		nbt.setInteger("progress", progress);
		return nbt;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerFurnaceIron(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIFurnaceIron(player.inventory, this);
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = new AxisAlignedBB(
					pos.getX() - 1,
					pos.getY(),
					pos.getZ() - 1,
					pos.getX() + 2,
					pos.getY() + 3,
					pos.getZ() + 2
					);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
		return type == UpgradeType.SPEED;
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.furnace_iron));
		if(type == UpgradeType.SPEED) {
			info.add(TextFormatting.GREEN + I18nUtil.resolveKey(this.KEY_DELAY, "-" + (level * 50 / 3) + "%"));
		}
	}

	@Override
	public HashMap<UpgradeType, Integer> getValidUpgrades() {
		HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
		upgrades.put(UpgradeType.SPEED, 3);
		return upgrades;
	}
}
