package com.hbm.tileentity.machine;

import com.hbm.interfaces.IMultiblock;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityDummy extends TileEntity implements net.minecraft.util.ITickable {

	public int targetX;
	public int targetY;
	public int targetZ;
	
    @Override
	public void update() {
    	if(!this.world.isRemote) {
    		if(!(com.hbm.world.WorldBlockState.read(this.world, targetX, targetY, targetZ).getBlock() instanceof IMultiblock)) {
    			this.world.func_147480_a(this.pos.getX(), this.pos.getY(), this.pos.getZ(), false);
    		}
    	}
    }

    @Override
	public void readFromNBT(NBTTagCompound nbt)
    {
    	super.readFromNBT(nbt);
        this.targetX = nbt.getInteger("tx");
        this.targetY = nbt.getInteger("ty");
        this.targetZ = nbt.getInteger("tz");
    }

    @Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
    	super.writeToNBT(nbt);
    	nbt.setInteger("tx", this.targetX);
    	nbt.setInteger("ty", this.targetY);
    	nbt.setInteger("tz", this.targetZ);
	    return nbt;
    }
}
