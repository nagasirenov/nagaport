package com.hbm.tileentity.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineDiFurnace;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.inventory.container.ContainerDiFurnace;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GUIDiFurnace;
import com.hbm.inventory.recipes.BlastFurnaceRecipes;
import com.hbm.items.ModItems;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachinePolluting;
import com.hbm.util.CompatEnergyControl;

import api.hbm.fluid.IFluidStandardSender;
import api.hbm.tile.IInfoProviderEC;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class TileEntityDiFurnace extends TileEntityMachinePolluting implements IFluidStandardSender, IGUIProvider, IInfoProviderEC {
    @Override
    public boolean shouldRefresh(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos,
            net.minecraft.block.state.IBlockState oldState, net.minecraft.block.state.IBlockState newState) {
        // Legacy metadata-only orientation changes retained inventory and all tile state.
        return oldState.getBlock() != newState.getBlock();
    }


	/**
	 * Native stack count only. Zero/negative counts are empty by the 1.12
	 * contract and are not a second quantity to be excavated through reflection;
	 * the furnace boundaries normalize such records before they reach this code.
	 */
	private static int quantity(ItemStack stack) {
		return stack == null || stack.isEmpty() ? 0 : stack.getCount();
	}

	public int progress;
	public int fuel;
	public static final int maxFuel = 12800;
	public static final int processingSpeed = 400;

	private static final int[] slots_io = new int[] { 0, 1, 2, 3 };
	public byte sideFuel = 1;
	public byte sideUpper = 1;
	public byte sideLower = 1;

	public TileEntityDiFurnace() {
		super(4, 50);
	}

	@Override
	public String getDefaultName() {
		return "container.diFurnace";
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		return i != 3;
	}

	public boolean hasItemPower(ItemStack stack, boolean canBreathe) {
		return getItemPower(stack, canBreathe) > 0;
	}

	//TODO: replace this terribleness
	private int getItemPower(ItemStack stack, boolean canBreathe) {
		if(stack == null || stack.isEmpty()) {
			return 0;
		} else {
			Item item = stack.getItem();

			if(item == Items.COAL) return canBreathe ? 200 : 0;
			if(item == Item.getItemFromBlock(Blocks.COAL_BLOCK)) return canBreathe ? 2000 : 0;
			if(item == Item.getItemFromBlock(ModBlocks.block_coke)) return canBreathe ? 4000 : 0;
			if(item == Items.LAVA_BUCKET) return 12800;
			if(item == Items.BLAZE_ROD) return canBreathe ? 1000 : 0;
			if(item == Items.BLAZE_POWDER) return canBreathe ? 300 : 0;
			if(item == ModItems.lignite) return canBreathe ? 150 : 0;
			if(item == ModItems.powder_lignite) return canBreathe ? 150 : 0;
			if(item == ModItems.powder_coal) return canBreathe ? 200 : 0;
			if(item == ModItems.briquette) return canBreathe ? 200 : 0;
			if(item == ModItems.coke) return canBreathe ? 400 : 0;
			if(item == ModItems.powder_coke) return canBreathe ? 400 : 0;
			if(item == ModItems.solid_fuel) return canBreathe ? 400 : 0;

			return 0;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.fuel = nbt.getInteger("powerTime");
		this.progress = nbt.getShort("cookTime");
		
		byte[] modes = nbt.getByteArray("modes");
		this.sideFuel = modes[0];
		this.sideUpper = modes[1];
		this.sideLower = modes[2];
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("powerTime", fuel);
		nbt.setShort("cookTime", (short) progress);
		nbt.setByteArray("modes", new byte[] {(byte) sideFuel, (byte) sideUpper, (byte) sideLower});
		return nbt;
	}

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return slots_io;
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();

		if(i == 0 && this.sideUpper != j) return false;
		if(i == 1 && this.sideLower != j) return false;
		if(i == 2 && this.sideFuel != j) return false;
		if(i == 3) return false;
		
		return this.isItemValidForSlot(i, itemStack);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return i == 3;
	}

	public int getDiFurnaceProgressScaled(int i) {
		return (progress * i) / processingSpeed;
	}

	public int getPowerRemainingScaled(int i) {
		return (fuel * i) / maxFuel;
	}

	public boolean canProcess() {
		if(slots[0] == null || slots[0].isEmpty() || slots[1] == null || slots[1].isEmpty()) return false;
		if(!this.hasPower()) return false;
		
		ItemStack output = BlastFurnaceRecipes.getOutput(slots[0], slots[1]);
		if(output == null || output.isEmpty()) return false;
		if(slots[3] == null || slots[3].isEmpty()) return true;
		if(!slots[3].isItemEqual(output)) return false;

		if(quantity(slots[3]) + quantity(output) <= slots[3].getMaxStackSize()) {
			return true;
		}
		
		return false;
	}

	private void processItem() {
		ItemStack itemStack = BlastFurnaceRecipes.getOutput(slots[0], slots[1]);
		if(itemStack == null || itemStack.isEmpty()) return;

		if(slots[3] == null || slots[3].isEmpty()) {
			slots[3] = itemStack.copy();
		} else if(slots[3].isItemEqual(itemStack)) {
			slots[3].grow(quantity(itemStack));
		}

		for(int i = 0; i < 2; i++) {
			this.decrStackSize(i, 1);
		}
	}

	public boolean hasPower() {
		return fuel > 0;
	}

	public boolean isProcessing() {
		return this.progress > 0;
	}

	@Override
	public void update() {

		if(!this.world.isRemote) {
			
			boolean extension = com.hbm.world.WorldBlockState.read(this.world, this.pos.getX(), this.pos.getY() + 1, this.pos.getZ()).getBlock() == ModBlocks.machine_difurnace_extension;
			
			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				this.sendSmoke(this.pos.getX() + dir.offsetX, this.pos.getY() + dir.offsetY, this.pos.getZ() + dir.offsetZ, dir);
			}
			
			if(extension) this.sendSmoke(this.pos.getX(), this.pos.getY() + 2, this.pos.getZ(), ForgeDirection.UP);

			boolean markDirty = false;
			boolean canOperate = breatheAir(0); // checks breathable but doesn't consume air
			
			if(this.hasItemPower(this.slots[2], canOperate) && this.fuel <= (TileEntityDiFurnace.maxFuel - getItemPower(this.slots[2], canOperate))) {
				this.fuel += getItemPower(this.slots[2], canOperate);
				if(this.slots[2] != null) {
					markDirty = true;
					Item fuelItem = this.slots[2].getItem();
					this.slots[2].shrink(1);
					if(this.slots[2].getCount() == 0) {
						ItemStack residue = fuelItem.getContainerItem(this.slots[2]);
						this.slots[2] = (residue == null || residue.isEmpty()) ? null : residue;
					}
				}
			}

			if(canProcess()) {

				//fuel -= extension ? 2 : 1;
				fuel -= 1; //switch it up on me, fuel efficiency, on fumes i'm running - running - running - running
				progress += extension ? 3 : 1;

				if(this.progress >= TileEntityDiFurnace.processingSpeed) {
					this.progress -= TileEntityDiFurnace.processingSpeed; // look mom, ive finally added something to a popular project
					this.processItem();
					markDirty = true;
				}
				
				if(fuel < 0) {
					fuel = 0;
				}

				if(this.world.getTotalWorldTime() % 20 == 0) this.pollute(PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND * (extension ? 3 : 1));
				
			} else {
				progress = 0;
			}

			boolean trigger = true;

			if(canProcess() && this.progress == 0) {
				trigger = false;
			}

			if(trigger) {
				markDirty = true;
				MachineDiFurnace.updateBlockState(this.progress > 0, this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());
			}

			networkPackNT(15);

			if(markDirty) {
				this.markDirty();
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeShort(this.progress);
		buf.writeShort(this.fuel);
		buf.writeBytes(new byte[] {
				this.sideFuel,
				this.sideUpper,
				this.sideLower});
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.progress = buf.readShort();
		this.fuel = buf.readShort();
		byte[] modes = new byte[3];
		buf.readBytes(modes);
		this.sideFuel = modes[0];
		this.sideUpper = modes[1];
		this.sideLower = modes[2];
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerDiFurnace(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIDiFurnace(player.inventory, this);
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[0];
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return this.getSmokeTanks();
	}

	@Override
	public void provideExtraInfo(NBTTagCompound data) {
		data.setLong(CompatEnergyControl.L_ENERGY_, this.fuel);
		data.setLong(CompatEnergyControl.L_CAPACITY_, this.maxFuel);
		data.setInteger(CompatEnergyControl.I_PROGRESS, this.progress);
	}
}
