package com.hbm.tileentity.machine.fusion;

import java.util.Map.Entry;

import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.KlystronNetwork;
import com.hbm.uninos.networkproviders.KlystronNetworkProvider;
import com.hbm.uninos.networkproviders.PlasmaNetworkProvider;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.math.AxisAlignedBB;
import com.hbm.lib.ForgeDirection;

public class TileEntityFusionCoupler extends TileEntityLoadedBase implements IFusionPowerReceiver, net.minecraft.util.ITickable {

	protected GenNode klystronNode;
	protected GenNode plasmaNode;
	
	@Override
	public void update() {
		
		if(!this.world.isRemote) {
			
			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10).getOpposite();
			ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
			
			// christ on the cross why didn't i just make a baseclass to shove this crap into
			if(klystronNode == null || klystronNode.expired) {
				klystronNode = UniNodespace.getNode(this.world, this.pos.getX() + rot.offsetX, this.pos.getY() + 2, this.pos.getZ() + rot.offsetZ, KlystronNetworkProvider.THE_PROVIDER);
				
				if(klystronNode == null) {
					klystronNode = new GenNode(KlystronNetworkProvider.THE_PROVIDER,
							new BlockPos(this.pos.getX() + rot.offsetX, this.pos.getY() + 2, this.pos.getZ() + rot.offsetZ))
							.setConnections(new DirPos(this.pos.getX() + rot.offsetX * 2, this.pos.getY() + 2, this.pos.getZ() + rot.offsetZ * 2, rot));
					
					UniNodespace.createNode(this.world, klystronNode);
				}
			}
			
			if(plasmaNode == null || plasmaNode.expired) {
				plasmaNode = UniNodespace.getNode(this.world, this.pos.getX() - rot.offsetX, this.pos.getY() + 2, this.pos.getZ() - rot.offsetZ, PlasmaNetworkProvider.THE_PROVIDER);
				
				if(plasmaNode == null) {
					plasmaNode = new GenNode(PlasmaNetworkProvider.THE_PROVIDER,
							new BlockPos(this.pos.getX() - rot.offsetX, this.pos.getY() + 2, this.pos.getZ() - rot.offsetZ))
							.setConnections(new DirPos(this.pos.getX() - rot.offsetX * 2, this.pos.getY() + 2, this.pos.getZ() - rot.offsetZ * 2, rot.getOpposite()));
					
					UniNodespace.createNode(this.world, plasmaNode);
				}
			}
			
			if(klystronNode.net != null) klystronNode.net.addProvider(this);
			if(plasmaNode.net != null) plasmaNode.net.addReceiver(this);
		}
	}

	@Override public boolean receivesFusionPower() { return true; }

	@Override
	public void receiveFusionPower(long fusionPower, double neutronPower) {
		
		// more copy pasted crap code ! ! !
		if(klystronNode != null && klystronNode.net != null) {
			KlystronNetwork net = (KlystronNetwork) klystronNode.net;
			
			for(Object o : net.receiverEntries.entrySet()) {
				Entry e = (Entry) o;
				if(e.getKey() instanceof TileEntityFusionTorus) {
					TileEntityFusionTorus torus = (TileEntityFusionTorus) e.getKey();
					
					if(torus.isLoaded() && !torus.isInvalid()) {
						torus.klystronEnergy += fusionPower;
						break;
					}
				}
			}
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();

		if(!this.world.isRemote) {
			if(this.klystronNode != null) UniNodespace.destroyNode(this.world, klystronNode);
			if(this.plasmaNode != null) UniNodespace.destroyNode(this.world, plasmaNode);
		}
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					this.pos.getX() - 1,
					this.pos.getY(),
					this.pos.getZ() - 1,
					this.pos.getX() + 2,
					this.pos.getY() + 4,
					this.pos.getZ() + 2
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
