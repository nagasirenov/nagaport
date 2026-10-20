package com.hbm.blocks.network;

import com.hbm.lib.RefStrings;
import com.hbm.tileentity.network.TileEntityCraneBase;
import com.hbm.tileentity.network.TileEntityCraneGrabber;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class CraneGrabber extends BlockCraneBase {

	public CraneGrabber() {
		super(Material.IRON);
		initModelTextures();
	}

	@Override
	public TileEntityCraneBase createNewTileEntity(World world, int meta) {
		return new TileEntityCraneGrabber();
	}
	
	@Override
	protected void initModelTextures() {
		super.initModelTextures();
		this.iconIn = new ResourceLocation(RefStrings.MODID, "blocks/crane_pull");
		this.iconSideIn = new ResourceLocation(RefStrings.MODID, "blocks/crane_side_pull");
		this.iconDirectional = new ResourceLocation(RefStrings.MODID, "blocks/crane_grabber_top");
		this.iconDirectionalUp = new ResourceLocation(RefStrings.MODID, "blocks/crane_grabber_side_up");
		this.iconDirectionalDown = new ResourceLocation(RefStrings.MODID, "blocks/crane_grabber_side_down");
		this.iconDirectionalTurnLeft = new ResourceLocation(RefStrings.MODID, "blocks/crane_grabber_top_left");
		this.iconDirectionalTurnRight = new ResourceLocation(RefStrings.MODID, "blocks/crane_grabber_top_right");
		this.iconDirectionalSideLeftTurnUp = new ResourceLocation(RefStrings.MODID, "blocks/crane_grabber_side_left_turn_up");
		this.iconDirectionalSideRightTurnUp = new ResourceLocation(RefStrings.MODID, "blocks/crane_grabber_side_right_turn_up");
		this.iconDirectionalSideLeftTurnDown = new ResourceLocation(RefStrings.MODID, "blocks/crane_grabber_side_left_turn_down");
		this.iconDirectionalSideRightTurnDown = new ResourceLocation(RefStrings.MODID, "blocks/crane_grabber_side_right_turn_down");
		this.iconDirectionalSideUpTurnLeft = new ResourceLocation(RefStrings.MODID, "blocks/crane_grabber_side_up_turn_left");
		this.iconDirectionalSideUpTurnRight = new ResourceLocation(RefStrings.MODID, "blocks/crane_grabber_side_up_turn_right");
		this.iconDirectionalSideDownTurnLeft = new ResourceLocation(RefStrings.MODID, "blocks/crane_grabber_side_down_turn_left");
		this.iconDirectionalSideDownTurnRight = new ResourceLocation(RefStrings.MODID, "blocks/crane_grabber_side_down_turn_right");
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		Block block = state.getBlock();
		int meta = getMetaFromState(state);
		this.dropContents(world, x, y, z, block, meta, 9, 11);
		super.breakBlock(world, pos, state);
	}
}
