package api.hbm.fluid;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.UniNodespace;

import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

@Deprecated
public interface IFluidStandardReceiver extends IFluidStandardReceiverMK2 {

	public default void subscribeToAllAround(FluidType type, TileEntity tile) {
		subscribeToAllAround(type, tile.getWorld(), tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ());
	}

	public default void subscribeToAllAround(FluidType type, World world, int x, int y, int z) {
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			trySubscribe(type, world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir);
		}
	}

	public default void tryUnsubscribe(FluidType type, World world, int x, int y, int z) {
		GenNode node = UniNodespace.getNode(world, x, y, z, type.getNetworkProvider());
		if(node != null && node.net != null) node.net.removeReceiver(this);
	}

	public default void unsubscribeToAllAround(FluidType type, TileEntity tile) {
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			tryUnsubscribe(type, tile.getWorld(), tile.getPos().getX() + dir.offsetX, tile.getPos().getY() + dir.offsetY, tile.getPos().getZ() + dir.offsetZ);
		}
	}
}
