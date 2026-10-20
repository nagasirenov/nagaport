package api.hbm.energymk2;

import com.hbm.lib.Library;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.energymk2.Nodespace.PowerNode;
import net.minecraft.tileentity.TileEntity;

public interface IEnergyConductorMK2 extends IEnergyConnectorMK2 {
	
	public default PowerNode createNode() {
		TileEntity tile = (TileEntity) this;
		return new PowerNode(new BlockPos(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ())).setConnections(
				new DirPos(tile.getPos().getX() + 1, tile.getPos().getY(), tile.getPos().getZ(), Library.POS_X),
				new DirPos(tile.getPos().getX() - 1, tile.getPos().getY(), tile.getPos().getZ(), Library.NEG_X),
				new DirPos(tile.getPos().getX(), tile.getPos().getY() + 1, tile.getPos().getZ(), Library.POS_Y),
				new DirPos(tile.getPos().getX(), tile.getPos().getY() - 1, tile.getPos().getZ(), Library.NEG_Y),
				new DirPos(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ() + 1, Library.POS_Z),
				new DirPos(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ() - 1, Library.NEG_Z)
				);
	}
}
