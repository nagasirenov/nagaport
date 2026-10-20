package com.hbm.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.IProxyController;
import com.hbm.util.Compat;
import com.hbm.util.fauxpointtwelve.BlockPos;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityProxyBase extends TileEntityLoadedBase {
	
	public BlockPos cachedPosition;


	public TileEntity getTE() {
		
		if(this.world == null) return null;
		
		if(cachedPosition != null) {
			TileEntity te = Compat.getTileStandard(this.world, cachedPosition.getX(), cachedPosition.getY(), cachedPosition.getZ());
			if(te != null && !(te instanceof TileEntityProxyBase)) return te;
			cachedPosition = null;
			this.markDirty();
		}

		if(this.getBlockType() instanceof BlockDummyable) {

			BlockDummyable dummy = (BlockDummyable) this.getBlockType();

			int[] pos = dummy.findCore(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());

			if(pos != null) {

				TileEntity te = Compat.getTileStandard(this.world, pos[0], pos[1], pos[2]);
				if(te != null && !(te instanceof TileEntityProxyBase)) return te;
			}
		}

		if(this.getBlockType() instanceof IProxyController) {
			IProxyController controller = (IProxyController) this.getBlockType();
			TileEntity tile = controller.getCore(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());
			
			if(tile != null && !(tile instanceof TileEntityProxyBase)) return tile;
		}

		return null;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		
		if(nbt.getBoolean("hasPos")) cachedPosition = new BlockPos(nbt.getInteger("pX"), nbt.getInteger("pY"), nbt.getInteger("pZ"));
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		if(this.cachedPosition != null) {
			nbt.setBoolean("hasPos", true);
			nbt.setInteger("pX", this.cachedPosition.getX());
			nbt.setInteger("pY", this.cachedPosition.getY());
			nbt.setInteger("pZ", this.cachedPosition.getZ());
		}
		return nbt;
	}
}
