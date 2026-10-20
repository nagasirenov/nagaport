package com.hbm.tileentity.deco;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityObjTester extends TileEntity {
	
	@Override
	public int getBlockMetadata()
    {
        if (this.blockMetadata == -1)
        {
            this.blockMetadata = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ()) );
        }

        return this.blockMetadata;
    }
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}
}
