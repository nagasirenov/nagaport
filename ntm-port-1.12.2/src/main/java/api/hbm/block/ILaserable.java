package api.hbm.block;

import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

//applicable to blocks and tile entities
public interface ILaserable {
	
	public void addEnergy(World world, int x, int y, int z, long energy, ForgeDirection dir);

}
