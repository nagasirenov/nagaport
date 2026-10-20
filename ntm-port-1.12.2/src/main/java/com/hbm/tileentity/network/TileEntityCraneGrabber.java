package com.hbm.tileentity.network;

import api.hbm.conveyor.IConveyorBelt;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.network.CraneInserter;
import com.hbm.entity.item.EntityMovingItem;
import com.hbm.inventory.container.ContainerCraneGrabber;
import com.hbm.inventory.gui.GUICraneGrabber;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

import java.util.List;

public class TileEntityCraneGrabber extends TileEntityCraneBase implements IGUIProvider, IControlReceiverFilter {

	public boolean isWhitelist = false;
	public ModulePatternMatcher matcher;
	public long lastGrabbedTick = 0;

	public TileEntityCraneGrabber() {
		super(11);
		this.matcher = new ModulePatternMatcher(9);
	}

	@Override
	public void nextMode(int i) {
		this.matcher.nextMode(this.world, slots[i], i);
	}

	@Override
	public String getDefaultName() {
		return "container.craneGrabber";
	}

	@Override
	public void update() {
		super.update();
		if(!this.world.isRemote) {

			int delay = 20;

			if(slots[10] != null && slots[10].getItem() == ModItems.upgrade_ejector) {
				switch(slots[10].getItemDamage()) {
				case 0: delay = 10; break;
				case 1: delay = 5; break;
				case 2: delay = 2; break;
				}
			}

			if(this.world.getTotalWorldTime() >= lastGrabbedTick + delay && !this.world.isBlockIndirectlyGettingPowered(this.pos.getX(), this.pos.getY(), this.pos.getZ())) {
				int amount = 1;

				if(slots[9] != null && slots[9].getItem() == ModItems.upgrade_stack) {
					switch(slots[9].getItemDamage()) {
					case 0: amount = 4; break;
					case 1: amount = 16; break;
					case 2: amount = 64; break;
					}
				}

				ForgeDirection inputSide = getInputSide();
				ForgeDirection outputSide = getOutputSide();
				Block beltBlock = com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + outputSide.offsetX, this.pos.getY() + outputSide.offsetY, this.pos.getZ() + outputSide.offsetZ).getBlock();

				//unholy copy paste bullshit because i can't be assed to rework the entire thing
				if(beltBlock instanceof IConveyorBelt) {
					IConveyorBelt belt = (IConveyorBelt) beltBlock;

					double reach = 1D;
					if(this.getBlockMetadata() > 1) { //ignore if pointing up or down
						Block b = com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + inputSide.offsetX, this.pos.getY() + inputSide.offsetY, this.pos.getZ() + inputSide.offsetZ).getBlock();
						if(b == ModBlocks.conveyor_double) reach = 0.5D;
						if(b == ModBlocks.conveyor_triple) reach = 0.33D;
					}

					double x = this.pos.getX() + inputSide.offsetX * reach;
					double y = this.pos.getY() + inputSide.offsetY * reach;
					double z = this.pos.getZ() + inputSide.offsetZ * reach;

					List<EntityMovingItem> items = this.world.getEntitiesWithinAABB(EntityMovingItem.class, AxisAlignedBB.getBoundingBox(x + 0.1875D, y + 0.1875D, z + 0.1875D, x + 0.8125D, y + 0.8125D, z + 0.8125D));

					for(EntityMovingItem item : items) {
						if(item.isDead) continue;
						ItemStack stack = item.getItemStack();
						boolean match = this.matchesFilter(stack);
						if(this.isWhitelist && !match || !this.isWhitelist && match) continue;

						lastGrabbedTick = this.world.getTotalWorldTime();

						Vec3d pos = new Vec3d(this.pos.getX() + 0.5 + outputSide.offsetX * 0.55, this.pos.getY() + 0.5 + outputSide.offsetY * 0.55, this.pos.getZ() + 0.5 + outputSide.offsetZ * 0.55);
						Vec3d snap = belt.getClosestSnappingPosition(this.world, this.pos.getX() + outputSide.offsetX, this.pos.getY() + outputSide.offsetY, this.pos.getZ() + outputSide.offsetZ, pos);
						EntityMovingItem newItem = new EntityMovingItem(this.world);
						newItem.setItemStack(item.getItemStack().copy());
						newItem.setPosition(snap.x, snap.y, snap.z);
						item.setDead();
						this.world.spawnEntityInWorld(newItem);
						break;
					}

				} else {

					TileEntity te = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX() + outputSide.offsetX, this.pos.getY() + outputSide.offsetY, this.pos.getZ() + outputSide.offsetZ));

					int[] access = null;
					ISidedInventory sided = null;

					if(te instanceof ISidedInventory) {
						sided = (ISidedInventory) te;
						access = InventoryUtil.masquerade(sided, outputSide.getOpposite().ordinal());
					}

					if(te instanceof IInventory) {

						/*
						 * due to this really primitive way of just offsetting the AABB instead of contracting it, there's a wacky
						 * edge-case where it's possible to feed the grabber by inserting items from the side if there's a triple
						 * lane conveyor in front of the grabbing end. this is such a non-issue that i'm not going to bother trying
						 * to fuck with the AABB further, since that's just a major headache for no practical benefit
						*/
						double reach = 1D;
						if(this.getBlockMetadata() > 1) { //ignore if pointing up or down
							Block b = com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + inputSide.offsetX, this.pos.getY() + inputSide.offsetY, this.pos.getZ() + inputSide.offsetZ).getBlock();
							if(b == ModBlocks.conveyor_double) reach = 0.5D;
							if(b == ModBlocks.conveyor_triple) reach = 0.33D;
						}

						double x = this.pos.getX() + inputSide.offsetX * reach;
						double y = this.pos.getY() + inputSide.offsetY * reach;
						double z = this.pos.getZ() + inputSide.offsetZ * reach;
						List<EntityMovingItem> items = this.world.getEntitiesWithinAABB(EntityMovingItem.class, AxisAlignedBB.getBoundingBox(x + 0.1875D, y + 0.1875D, z + 0.1875D, x + 0.8125D, y + 0.8125D, z + 0.8125D));

						for(EntityMovingItem item : items) {
							ItemStack stack = item.getItemStack();
							boolean match = this.matchesFilter(stack);
							if(this.isWhitelist && !match || !this.isWhitelist && match) continue;

							lastGrabbedTick = this.world.getTotalWorldTime();

							ItemStack copy = stack.copy();
							int toAdd = Math.min(stack.stackSize, amount);
							copy.stackSize = toAdd;
							ItemStack ret = CraneInserter.addToInventory((IInventory) te, access, copy, outputSide.getOpposite().ordinal());
							int didAdd = toAdd - (ret != null ? ret.stackSize : 0);
							stack.stackSize -= didAdd;

							if(stack.stackSize <= 0) {
								item.setDead();
							}

							amount -= didAdd;
							if(amount <= 0) {
								break;
							}
						}
					}
				}
			}

			this.networkPackNT(15);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(this.isWhitelist);
		this.matcher.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.isWhitelist = buf.readBoolean();
		this.matcher.deserialize(buf);
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
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerCraneGrabber(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUICraneGrabber(player.inventory, this);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.isWhitelist = nbt.getBoolean("isWhitelist");
		this.matcher.readFromNBT(nbt);
		this.lastGrabbedTick = nbt.getLong("lastGrabbedTick");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setBoolean("isWhitelist", this.isWhitelist);
		this.matcher.writeToNBT(nbt);
		nbt.setLong("lastGrabbedTick", lastGrabbedTick);
		return nbt;
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return new Vec3d(this.pos.getX() - player.posX, this.pos.getY() - player.posY, this.pos.getZ() - player.posZ).length() < 20;
	}

	@Override
	public int[] getFilterSlots() {
		return new int[]{0,9};
	}

	@Override
	public void receiveControl(NBTTagCompound data) {
		if(data.hasKey("whitelist")) {
			this.isWhitelist = !this.isWhitelist;
		}
		if(data.hasKey("slot")){
			setFilterContents(data);
		}
	}
}
