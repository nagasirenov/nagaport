package com.hbm.handler.neutron;

import com.hbm.handler.neutron.NeutronNodeWorld.StreamWorld;
import com.hbm.util.fauxpointtwelve.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Iterator;

public abstract class NeutronStream {

	public enum NeutronType {
		DUMMY, // Dummy streams for node decaying
		RBMK,  // RBMK neutron streams
		PILE   // Chicago pile streams
	}

	public NeutronNode origin;

	// doubles!!
	public double fluxQuantity;
	// Hey, new implementation! Basically a ratio for slow flux to fast flux
	// 0 = all slow flux
	// 1 = all fast flux
	public double fluxRatio;

	public NeutronType type = NeutronType.DUMMY;

	// Vector for direction of neutron flow.
	public Vec3d vector;

	// Primarily used as a "dummy stream", not to be added to the streams list.
	public NeutronStream(NeutronNode origin, Vec3d vector) {
		this.origin = origin;
		this.vector = vector;
		posInstance = origin.pos.clone();
	}

	public NeutronStream(NeutronNode origin, Vec3d vector, double flux, double ratio, NeutronType type) {
		this.origin = origin;
		this.vector = vector;
		posInstance = origin.pos.clone();
		this.fluxQuantity = flux;
		this.fluxRatio = ratio;
		this.type = type;

		NeutronNodeWorld.getOrAddWorld(origin.tile.getWorld()).addStream(this);
	}

	protected BlockPos posInstance;

	private int i;

	// USES THE CACHE!!!
	public Iterator<BlockPos> getBlocks(int range) {

		i = 1;

		return new Iterator<BlockPos>() {
			@Override
			public boolean hasNext() {
				return i <= range;
			}

			@Override
			public BlockPos next() {
				int x = (int) Math.floor(0.5 + vector.x * i);
				int z = (int) Math.floor(0.5 + vector.z * i);

				i++;
				return posInstance.mutate(origin.tile.getPos().getX() + x, origin.tile.getPos().getY(), origin.tile.getPos().getZ() + z);
			}
		};
	}

	public abstract void runStreamInteraction(World worldObj, StreamWorld streamWorld);
}
