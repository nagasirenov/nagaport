package com.hbm.tileentity.machine.fusion;

import com.hbm.uninos.GenNode;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.PlasmaNetworkProvider;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import com.hbm.lib.ForgeDirection;

public class TileEntityFusionCollector extends TileEntity implements net.minecraft.util.ITickable {

	protected GenNode plasmaNode;

	@Override
	public void update() {

		if(!this.world.isRemote) {

			if(plasmaNode == null || plasmaNode.expired) {
				ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10).getOpposite();
				plasmaNode = UniNodespace.getNode(this.world, this.pos.getX() + dir.offsetX * 2, this.pos.getY() + 2, this.pos.getZ() + dir.offsetZ * 2, PlasmaNetworkProvider.THE_PROVIDER);

				if(plasmaNode == null) {
					plasmaNode = new GenNode(PlasmaNetworkProvider.THE_PROVIDER,
							new BlockPos(this.pos.getX() + dir.offsetX * 2, this.pos.getY() + 2, this.pos.getZ() + dir.offsetZ * 2))
							.setConnections(new DirPos(this.pos.getX() + dir.offsetX * 3, this.pos.getY() + 2, this.pos.getZ() + dir.offsetZ * 3, dir));

					UniNodespace.createNode(this.world, plasmaNode);
				}
			}

			if(plasmaNode != null && plasmaNode.hasValidNet()) plasmaNode.net.addReceiver(this);
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();

		if(!this.world.isRemote) {
			if(this.plasmaNode != null) UniNodespace.destroyNode(this.world, plasmaNode);
		}
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = new AxisAlignedBB(
					this.pos.getX() - 2,
					this.pos.getY(),
					this.pos.getZ() - 2,
					this.pos.getX() + 3,
					this.pos.getY() + 4,
					this.pos.getZ() + 3
					);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}
