package com.hbm.tileentity.network;

import api.hbm.conveyor.IConveyorBelt;
import api.hbm.conveyor.IEnterableBlock;

import com.hbm.entity.item.EntityMovingItem;
import com.hbm.inventory.container.ContainerCraneExtractor;
import com.hbm.inventory.gui.GUICraneExtractor;
import com.hbm.items.ModItems;
import com.hbm.module.ModulePatternMatcher;
import com.hbm.tileentity.IControlReceiverFilter;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.InventoryUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class TileEntityCraneExtractor extends TileEntityCraneBase implements IGUIProvider, IControlReceiverFilter {

	public boolean isWhitelist = false;
	public boolean maxEject = false;
	public ModulePatternMatcher matcher;

	public TileEntityCraneExtractor() {
		super(20);
		this.matcher = new ModulePatternMatcher(9);
	}

	@Override
	public String getDefaultName() {
		return "container.craneExtractor";
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack stack) {
		super.setInventorySlotContents(i, stack);

		if(stack != null && !stack.isEmpty()) {

			if((stack.getItem() == ModItems.upgrade_ejector && i == 19) || (stack.getItem() == ModItems.upgrade_stack && i == 18)) {
				this.world.playSound(null, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, com.hbm.main.ModSoundEvents.resolve("hbm:item.upgradePlug"), net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);
			}
		}
	}

	@Override
	public void update() {
		super.update();
		if(!this.world.isRemote) {

			int delay = 20;

			if(slots[19] != null && slots[19].getItem() == ModItems.upgrade_ejector) {
				switch(slots[19].getItemDamage()) {
				case 0: delay = 10; break;
				case 1: delay = 5; break;
				case 2: delay = 2; break;
				}
			}

			if(this.world.getTotalWorldTime() % delay == 0 && !this.world.isBlockPowered(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ()))) {
				int amount = 1;

				if(slots[18] != null && slots[18].getItem() == ModItems.upgrade_stack) {
					switch(slots[18].getItemDamage()) {
					case 0: amount = 4; break;
					case 1: amount = 16; break;
					case 2: amount = 64; break;
					}
				}

				ForgeDirection inputSide = getOutputSide(); // note the switcheroo!
				ForgeDirection outputSide = getInputSide();
				TileEntity te = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX() + inputSide.offsetX, this.pos.getY() + inputSide.offsetY, this.pos.getZ() + inputSide.offsetZ));
				Block b = com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + outputSide.offsetX, this.pos.getY() + outputSide.offsetY, this.pos.getZ() + outputSide.offsetZ).getBlock();

				int[] access = null;
				ISidedInventory sided = null;

				if(te instanceof ISidedInventory) {
					sided = (ISidedInventory) te;
					//access = sided.getAccessibleSlotsFromSide(dir.ordinal());
					access = masquerade(sided, inputSide.getOpposite().ordinal());
				}

				boolean hasSent = false;

				IConveyorBelt belt = null;

				if(b instanceof IConveyorBelt) {
					belt = (IConveyorBelt) b;
				}

				/* try to send items from a connected inv, if present */
				if(te instanceof IInventory) {

					IInventory inv = (IInventory) te;
					int size = access == null ? inv.getSizeInventory() : access.length;

					for(int i = 0; i < size; i++) {
						int index = access == null ? i : access[i];
						ItemStack stack = inv.getStackInSlot(index);

						if(stack != null && !stack.isEmpty() && (sided == null || sided.canExtractItem(index, stack, com.hbm.lib.ForgeDirection.getOrientation(inputSide.getOpposite().ordinal()).toEnumFacing()))) {

							int maxTarget = Math.min(amount, stack.getMaxStackSize());
							if(this.maxEject && stack.getCount() < maxTarget) continue;
							boolean match = this.matchesFilter(stack);

							if((isWhitelist && match) || (!isWhitelist && !match)) {
								stack = stack.copy();
								int toSend = Math.min(amount, stack.getCount());

								if (belt != null) {
									inv.decrStackSize(index, toSend);
									stack.setCount(toSend);
									sendItem(stack, belt, outputSide);
								} else {
									stack.setCount(toSend);
									ItemStack remaining = InventoryUtil.tryAddItemToInventory(this.slots, 9, 17, stack);
									inv.decrStackSize(index, toSend - (remaining == null ? 0 : remaining.getCount()));
								}
								hasSent = true;
								break;
							}
						}
					}
				}

				/* if no item has been sent, send buffered items while ignoring the filter */
				if(!hasSent && belt != null) {

					for(int i = 9; i < 18; i++) {
						ItemStack stack = slots[i];

						if(stack != null){
							stack = stack.copy();
							int toSend = Math.min(amount, stack.getCount());

							int maxTarget = Math.min(amount, stack.getMaxStackSize());
							if(this.maxEject && stack.getCount() < maxTarget) continue;

							decrStackSize(i, toSend);
							stack.setCount(toSend);
							sendItem(stack, belt, outputSide);

							break;
						}
					}
				}
			}

			this.networkPackNT(15);
		}
	}

	private void sendItem(ItemStack stack, IConveyorBelt belt, ForgeDirection outputSide) {
		EntityMovingItem moving = new EntityMovingItem(this.world);
		Vec3d pos = new Vec3d(this.pos.getX() + 0.5 + outputSide.offsetX * 0.55, this.pos.getY() + 0.5 + outputSide.offsetY * 0.55, this.pos.getZ() + 0.5 + outputSide.offsetZ * 0.55);
		Vec3d snap = belt.getClosestSnappingPosition(this.world, this.pos.getX() + outputSide.offsetX, this.pos.getY() + outputSide.offsetY, this.pos.getZ() + outputSide.offsetZ, pos);
		moving.setPosition(snap.x, snap.y, snap.z);
		moving.setItemStack(stack);
		this.world.spawnEntity(moving);

		if(belt instanceof IEnterableBlock) {
			IEnterableBlock enterable = (IEnterableBlock) belt;

			if(enterable.canItemEnter(this.world, this.pos.getX() + outputSide.offsetX, this.pos.getY() + outputSide.offsetY, this.pos.getZ() + outputSide.offsetZ, outputSide.getOpposite(), moving)) {
				enterable.onItemEnter(this.world, this.pos.getX() + outputSide.offsetX, this.pos.getY() + outputSide.offsetY, this.pos.getZ() + outputSide.offsetZ, outputSide.getOpposite(), moving);
				moving.setDead();
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(isWhitelist);
		buf.writeBoolean(maxEject);
		this.matcher.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		isWhitelist = buf.readBoolean();
		maxEject = buf.readBoolean();
		this.matcher.deserialize(buf);
	}

	public static int[] masquerade(ISidedInventory sided, int side) {

		if(sided instanceof TileEntityFurnace) {
			return new int[] {2};
		}

		return sided.getSlotsForFace(com.hbm.lib.ForgeDirection.getOrientation(side).toEnumFacing());
	}

	public boolean matchesFilter(ItemStack stack) {

		for(int i = 0; i < 9; i++) {
			ItemStack filter = slots[i];

			if(filter != null && this.matcher.isValidForFilter(filter, i, stack)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void nextMode(int i) {
		this.matcher.nextMode(this.world, slots[i], i);
	}

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int p_94128_1_ = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return new int[] { 9, 10, 11, 12, 13, 14, 15, 16, 17 };
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return i > 8 && i < 18;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return i > 8 && i < 18;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerCraneExtractor(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUICraneExtractor(player.inventory, this);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.isWhitelist = nbt.getBoolean("isWhitelist");
		this.maxEject = nbt.getBoolean("maxEject");
		this.matcher.readFromNBT(nbt);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setBoolean("isWhitelist", this.isWhitelist);
		nbt.setBoolean("maxEject", this.maxEject);
		this.matcher.writeToNBT(nbt);
		return nbt;
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return new Vec3d(this.pos.getX() - player.posX, this.pos.getY() - player.posY, this.pos.getZ() - player.posZ).length() < 20;
	}

	@Override
	public void receiveControl(NBTTagCompound data) {
		if(data.hasKey("whitelist")) {
			this.isWhitelist = !this.isWhitelist;
		}
		if(data.hasKey("maxEject")) {
			this.maxEject = !this.maxEject;
		}
		if(data.hasKey("slot")){
			setFilterContents(data);
		}
		this.markDirty();
	}

	@Override
	public int[] getFilterSlots() {
		return new int[] {0, 9};
	}
}

