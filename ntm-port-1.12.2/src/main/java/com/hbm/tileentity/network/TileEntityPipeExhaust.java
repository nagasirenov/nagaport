package com.hbm.tileentity.network;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.uninos.UniNodespace;

import api.hbm.fluidmk2.FluidNode;
import api.hbm.fluidmk2.IFluidPipeMK2;
import net.minecraft.tileentity.TileEntity;
import com.hbm.lib.ForgeDirection;

public class TileEntityPipeExhaust extends TileEntity implements IFluidPipeMK2, net.minecraft.util.ITickable {

    @Override public boolean canRenderBreaking() {
        return getWorld() != null && com.hbm.blocks.network.BoxDuctRenderState.isNativeBreakingOwner(this, getWorld().getBlockState(getPos()));
    }

	
	protected FluidNode[] nodes = new FluidNode[3];
	protected FluidType[] smokes = new FluidType[] {Fluids.SMOKE, Fluids.SMOKE_LEADED, Fluids.SMOKE_POISON};
	
	public FluidType[] getSmokes() {
		return smokes;
	}

	@Override
	public void update() {

		if(!this.world.isRemote) {

			for(int i = 0; i < getSmokes().length; i++) {
				if(this.nodes[i] == null || this.nodes[i].expired) {
					this.nodes[i] = (FluidNode) UniNodespace.getNode(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), getSmokes()[i].getNetworkProvider());
						
					if(this.nodes[i] == null || this.nodes[i].expired) {
						this.nodes[i] = this.createNode(getSmokes()[i]);
						UniNodespace.createNode(this.world, this.nodes[i]);
					}
				}
			}
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();

		if(!this.world.isRemote) {
			for(int i = 0; i < getSmokes().length; i++) {
				if(this.nodes[i] != null) {
					UniNodespace.destroyNode(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), getSmokes()[i].getNetworkProvider());
				}
			}
		}
	}
	
	@Override
	public boolean canConnect(FluidType type, ForgeDirection dir) {
		return dir != ForgeDirection.UNKNOWN && (type == Fluids.SMOKE || type == Fluids.SMOKE_LEADED || type == Fluids.SMOKE_POISON);
	}
}
