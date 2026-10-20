package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachinePress;

import api.hbm.block.IToolable;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class MachinePress extends BlockDummyable implements IToolable {

	public MachinePress(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityMachinePress();
		if(meta >= 6) return new TileEntityProxyCombo(true, false, false);
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {2, 0, 0, 0, 0, 0};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	protected void fixLegacyMonoblock(World world, int x, int y, int z) {
		net.minecraft.util.math.BlockPos pos = new net.minecraft.util.math.BlockPos(x, y, z);
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileEntityMachinePress) {
			((TileEntityMachinePress) tile).withLegacyPromotion(this, world, pos,
					() -> super.fixLegacyMonoblock(world, x, y, z));
		} else {
			super.fixLegacyMonoblock(world, x, y, z);
		}
	}

	@Override
	protected boolean isLegacyMonoblock(World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
		return te != null && te instanceof TileEntityMachinePress;
	}
	
	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(); int y = nativePos.getY(); int z = nativePos.getZ();
		return this.standardOpenBehavior(world, x, y, z, player, 0);
	}

	// Un-multiblickable with a hand drill for schenanigans
	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {
		
		if (tool != ToolType.HAND_DRILL) 
			return false;
		
		int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
		if (meta >= 12)
			return false;
		
		safeRem = true;
		com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
		safeRem = false;
		return true;
	}
	
}
