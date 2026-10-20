package com.hbm.tileentity.network;

import com.hbm.blocks.network.IBlockFluidDuct;
import com.hbm.extprop.HbmPlayerProps;
import com.hbm.handler.HbmKeybinds;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;

import api.hbm.fluidmk2.FluidNode;
import api.hbm.fluidmk2.IFluidPipeMK2;

import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.uninos.UniNodespace;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import com.hbm.lib.ForgeDirection;

public class TileEntityPipeBaseNT extends TileEntityLoadedBase implements IFluidPipeMK2, IFluidCopiable, net.minecraft.util.ITickable {

    @Override public boolean canRenderBreaking() {
        return getWorld() != null && com.hbm.blocks.network.BoxDuctRenderState.isNativeBreakingOwner(this, getWorld().getBlockState(getPos()));
    }


	protected FluidNode node;
	protected FluidType type = Fluids.NONE;
	protected FluidType lastType = Fluids.NONE;

	@Override
	public void update() {

		if(this.world.isRemote && lastType != type) {
			this.world.markBlockForUpdate(this.pos.getX(), this.pos.getY(), this.pos.getZ());
			lastType = type;
		}

		if(!this.world.isRemote) {

			if(this.node == null || this.node.expired) {

				if(this.shouldCreateNode()) {
					this.node = (FluidNode) UniNodespace.getNode(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), type.getNetworkProvider());

					if(this.node == null || this.node.expired) {
						this.node = this.createNode(type);
						UniNodespace.createNode(this.world, this.node);
					}
				}
			}
		}
	}

	public boolean shouldCreateNode() {
		return true;
	}

	public FluidType getType() {
		return this.type;
	}

	public void setType(FluidType type) {
		FluidType prev = this.type;
		this.type = type;
		this.markDirty();

		if(this.world instanceof WorldServer) {
			WorldServer world = (WorldServer) this.world;
			world.getPlayerChunkMap().markBlockForUpdate(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ()));
		}
		
		UniNodespace.destroyNode(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), prev.getNetworkProvider());

		if(this.node != null) {
			this.node = null;
		}
	}

	@Override
	public boolean canConnect(FluidType type, ForgeDirection dir) {
		return dir != ForgeDirection.UNKNOWN && type == this.type;
	}

	@Override
	public void invalidate() {
		super.invalidate();

		if(!this.world.isRemote) {
			if(this.node != null) {
				UniNodespace.destroyNode(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), type.getNetworkProvider());
			}
		}
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		return new SPacketUpdateTileEntity(this.getPos(), 0, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		this.readFromNBT(pkt.getNbtCompound());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.type = Fluids.fromID(nbt.getInteger("type"));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("type", this.type.getID());
		return nbt;
	}

	public boolean isLoaded = true;

	@Override
	public boolean isLoaded() {
		return isLoaded;
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		this.isLoaded = false;
	}

	@Override
	public int[] getFluidIDToCopy() {
		return new int[]{ type.getID() };
	}

	@Override
	public FluidTank getTankToPaste() {
		return null;
	}

	@Override
	public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, int x, int y, int z) {
		int[] ids = nbt.getIntArray("fluidID");
		if(ids.length > 0) {
			int id;
			if (index < ids.length)
				id = ids[index];
			else
				id = 0;

			FluidType fluid = Fluids.fromID(id);

			if(HbmPlayerProps.getData(player).getKeyPressed(HbmKeybinds.EnumKeybind.TOOL_CTRL)){
				IBlockFluidDuct pipe = (IBlockFluidDuct)com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock();
				pipe.changeTypeRecursively(world, x, y, z, getType(), fluid, 64);
			} else {
				this.setType(fluid);
			}
		}

	}
}
