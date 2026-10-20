package api.hbm.fluidmk2;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.lib.Library;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;

import net.minecraft.tileentity.TileEntity;

/**
 * IFluidConductorMK2 with added node creation method
 * @author hbm
 */
public interface IFluidPipeMK2 extends IFluidConnectorMK2 {
	
	public default FluidNode createNode(FluidType type) {
		TileEntity tile = (TileEntity) this;
		return new FluidNode(type.getNetworkProvider(), new BlockPos(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ())).setConnections(
				new DirPos(tile.getPos().getX() + 1, tile.getPos().getY(), tile.getPos().getZ(), Library.POS_X),
				new DirPos(tile.getPos().getX() - 1, tile.getPos().getY(), tile.getPos().getZ(), Library.NEG_X),
				new DirPos(tile.getPos().getX(), tile.getPos().getY() + 1, tile.getPos().getZ(), Library.POS_Y),
				new DirPos(tile.getPos().getX(), tile.getPos().getY() - 1, tile.getPos().getZ(), Library.NEG_Y),
				new DirPos(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ() + 1, Library.POS_Z),
				new DirPos(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ() - 1, Library.NEG_Z)
				);
	}
}
