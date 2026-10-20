package com.hbm.blocks.machine;

import net.minecraft.util.EnumHand;
import java.util.List;

import com.hbm.tileentity.machine.TileEntityFoundryMold;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class FoundryMold extends FoundryCastingBase {


	public FoundryMold() {
		super();
	}
	


	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityFoundryMold();
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBounding, List<AxisAlignedBB> list, Entity entity, boolean actualState) {
		int x=pos.getX(), y=pos.getY(), z=pos.getZ();
		
		AxisAlignedBB[] bbs = new AxisAlignedBB[] {
				new AxisAlignedBB(x, y, z, x + 1D, y + 0.125D, z + 1D),
				new AxisAlignedBB(x, y, z, x + 1D, y + 0.5D, z + 0.125D),
				new AxisAlignedBB(x, y, z, x + 0.125D, y + 0.5D, z + 1D),
				new AxisAlignedBB(x + 0.875D, y, z, x + 1D, y + 0.5D, z + 1D),
				new AxisAlignedBB(x, y, z + 0.875, x + 1D, y + 0.5D, z + 1D),
		};
		
		for(AxisAlignedBB bb : bbs) {
			if(entityBounding.intersects(bb)) {
				list.add(bb);
			}
		}
	}









	
	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return side == EnumFacing.DOWN;
	}

	@Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) { return new AxisAlignedBB(0,0,0,1,0.5D,1); }
	@Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) { return getBoundingBox(state,world,pos); }
}
