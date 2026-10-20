package com.hbm.tileentity.network;

import api.hbm.energymk2.IEnergyConductorMK2;
import api.hbm.energymk2.Nodespace;
import api.hbm.energymk2.Nodespace.PowerNode;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.lib.ForgeDirection;

public class TileEntityCableBaseNT extends TileEntityLoadedBase implements IEnergyConductorMK2, net.minecraft.util.ITickable {

    @Override public boolean canRenderBreaking() {
        return getWorld() != null && com.hbm.blocks.network.BoxDuctRenderState.isNativeBreakingOwner(this, getWorld().getBlockState(getPos()));
    }


	protected PowerNode node;

	@Override
	public void update() {

		if(!this.world.isRemote) {

			if(this.node == null || this.node.expired) {

				if(this.shouldCreateNode()) {
					this.node = Nodespace.getNode(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());

					if(this.node == null || this.node.expired) {
						this.node = this.createNode();
						Nodespace.createNode(this.world, this.node);
					}
				}
			}
		}
	}

	public boolean shouldCreateNode() {
		return true;
	}

	@Override
	public void invalidate() {
		super.invalidate();

		if(!this.world.isRemote) {
			if(this.node != null) {
				Nodespace.destroyNode(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());
			}
		}
	}

	@Override
	public boolean canConnect(ForgeDirection dir) {
		return dir != ForgeDirection.UNKNOWN;
	}
}
