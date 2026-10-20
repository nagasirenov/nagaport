package com.hbm.tileentity.network;

import com.hbm.blocks.network.CraneInserter;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerCraneInserter;
import com.hbm.inventory.gui.GUICraneInserter;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.Compat;
import com.hbm.util.InventoryUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class TileEntityCraneInserter extends TileEntityCraneBase implements IGUIProvider, IControlReceiver {
	
	public boolean destroyer = true;
	public static final int[] access = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };

	public TileEntityCraneInserter() {
		super(21);
	}

	@Override
	public String getDefaultName() {
		return "container.craneInserter";
	}

	@Override
	public void update() {
		super.update();
		if(!this.world.isRemote) {

			if(!this.world.isBlockPowered(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ()))) {ForgeDirection outputSide = getOutputSide();
				TileEntity te = Compat.getTileStandard(this.world, this.pos.getX() + outputSide.offsetX, this.pos.getY() + outputSide.offsetY, this.pos.getZ() + outputSide.offsetZ);
				
				int[] access = null;
				
				if(te instanceof ISidedInventory) {
					ISidedInventory sided = (ISidedInventory) te;
					access = InventoryUtil.masquerade(sided, outputSide.getOpposite().ordinal());
				}
				
				boolean didSomething = false;
				
				if(te instanceof IInventory) {
					for(int i = 0; i < slots.length; i++) {
						
						ItemStack stack = slots[i];
						
						if(stack != null) {
							ItemStack ret = CraneInserter.addToInventory((IInventory) te, access, stack.copy(), outputSide.getOpposite().ordinal());
							
							if(ret == null || ret.stackSize != stack.stackSize) {
								slots[i] = ret;
								this.markDirty();
								didSomething = true;
								break;
							}
						}
					}
					
					//if the previous operation fails, repeat but use single items instead of the whole stack instead
					//this should fix cases where the inserter can't insert into something that has a stack size limitation
					if(!didSomething) for(int i = 0; i < slots.length; i++) {
						
						ItemStack stack = slots[i];
						
						if(stack != null) {
							stack = stack.copy();
							stack.setCount(1);
							
							ItemStack ret = CraneInserter.addToInventory((IInventory) te, access, stack.copy(), outputSide.getOpposite().ordinal());
							
							if(ret == null || ret.stackSize != stack.stackSize) {
								this.decrStackSize(i, 1);
								this.markDirty();
								didSomething = true;
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
		buf.writeBoolean(destroyer);
	}
	
	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		destroyer = buf.readBoolean();
	}

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return access;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return true;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return true;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerCraneInserter(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUICraneInserter(player.inventory, this);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.destroyer = nbt.getBoolean("destroyer");
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setBoolean("destroyer", this.destroyer);
		return nbt;
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return new Vec3d(this.pos.getX() - player.posX, this.pos.getY() - player.posY, this.pos.getZ() - player.posZ).length() < 20;
	}

	@Override
	public void receiveControl(NBTTagCompound data) {
		if(data.hasKey("destroyer")) this.destroyer = !this.destroyer;
	}
}
