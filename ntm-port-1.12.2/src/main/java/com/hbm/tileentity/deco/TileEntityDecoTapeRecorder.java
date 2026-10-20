package com.hbm.tileentity.deco;

import com.hbm.blocks.generic.DecoTapeRecorder;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.tileentity.TileEntity;

public class TileEntityDecoTapeRecorder extends TileEntity {

	private int rot = 0;
	
	public int getRotation() {
		if(com.hbm.world.WorldBlockState.read(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ()).getBlock() instanceof DecoTapeRecorder)
		{
			if(this.world.isBlockPowered(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ())))
			{
				rot += 3;
				if(rot >= 360)
				{
					rot -=360;
				}
				return rot;
			}
			
			rot = 0;
		}
		
		return 0;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}
}
