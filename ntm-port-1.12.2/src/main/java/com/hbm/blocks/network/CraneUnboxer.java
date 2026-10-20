package com.hbm.blocks.network;

import api.hbm.conveyor.IConveyorItem;
import api.hbm.conveyor.IConveyorPackage;
import api.hbm.conveyor.IEnterableBlock;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.network.TileEntityCraneBase;
import com.hbm.tileentity.network.TileEntityCraneUnboxer;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class CraneUnboxer extends BlockCraneBase implements IEnterableBlock {

	public CraneUnboxer() {
		super(Material.IRON);
		initModelTextures();
	}

	@Override
	public TileEntityCraneBase createNewTileEntity(World world, int meta) {
		return new TileEntityCraneUnboxer();
	}
	
	@Override
	protected void initModelTextures() {
		super.initModelTextures();
		this.iconOut = new ResourceLocation(RefStrings.MODID, "blocks/crane_box");
		this.iconSideOut = new ResourceLocation(RefStrings.MODID, "blocks/crane_side_box");
		this.iconDirectional = new ResourceLocation(RefStrings.MODID, "blocks/crane_unboxer_top");
		this.iconDirectionalUp = new ResourceLocation(RefStrings.MODID, "blocks/crane_unboxer_side_down");
		this.iconDirectionalDown = new ResourceLocation(RefStrings.MODID, "blocks/crane_unboxer_side_up");
		this.iconDirectionalTurnLeft = new ResourceLocation(RefStrings.MODID, "blocks/crane_unboxer_top_right");
		this.iconDirectionalTurnRight = new ResourceLocation(RefStrings.MODID, "blocks/crane_unboxer_top_left");
		this.iconDirectionalSideLeftTurnUp = new ResourceLocation(RefStrings.MODID, "blocks/crane_unboxer_side_up_turn_left");
		this.iconDirectionalSideRightTurnUp = new ResourceLocation(RefStrings.MODID, "blocks/crane_unboxer_side_up_turn_right");
		this.iconDirectionalSideLeftTurnDown = new ResourceLocation(RefStrings.MODID, "blocks/crane_unboxer_side_down_turn_left");
		this.iconDirectionalSideRightTurnDown = new ResourceLocation(RefStrings.MODID, "blocks/crane_unboxer_side_down_turn_right");
		this.iconDirectionalSideUpTurnLeft = new ResourceLocation(RefStrings.MODID, "blocks/crane_unboxer_side_left_turn_up");
		this.iconDirectionalSideUpTurnRight = new ResourceLocation(RefStrings.MODID, "blocks/crane_unboxer_side_right_turn_up");
		this.iconDirectionalSideDownTurnLeft = new ResourceLocation(RefStrings.MODID, "blocks/crane_unboxer_side_left_turn_down");
		this.iconDirectionalSideDownTurnRight = new ResourceLocation(RefStrings.MODID, "blocks/crane_unboxer_side_right_turn_down");
	}

	@Override
	public int getRotationFromSide(IBlockAccess world, int x, int y, int z, int side) {
		int meta = getMetaFromState(world.getBlockState(new BlockPos(x, y, z)));

		if(meta > 1 && side == 1) {
			// ok so i've been sitting around for 4-5 hours trying to come up with a
			// more elegant way to implement this and i have seriously no clue what
			// the guys at mojang did, but the uv rotation makes absolutely no sense
			// it's 2:30 am, please just accept this
			// - martin
			ForgeDirection leftHandDirection = getOutputSide(world, x, y, z).getRotation(getInputSide(world, x, y, z));
			if (leftHandDirection == ForgeDirection.UP) {
				if (meta == 2) return 2;
				if (meta == 3) return 1;
				if (meta == 4) return 3;
				if (meta == 5) return 0;
			}
			if (leftHandDirection == ForgeDirection.DOWN) {
				if (meta == 2) return 1;
				if (meta == 3) return 2;
				if (meta == 4) return 0;
				if (meta == 5) return 3;
			}

			if(meta == 2) return 0;
			if(meta == 3) return 3;
			if(meta == 4) return 2;
			if(meta == 5) return 1;
		}

		return 0;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		Block block = state.getBlock();
		int meta = getMetaFromState(state);
		this.dropContents(world, x, y, z, block, meta, 0, 23);
		super.breakBlock(world, pos, state);
	}

	@Override
	public boolean canItemEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorItem entity) {
		return false;
	}

	@Override
	public void onItemEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorItem entity) { }

	@Override
	public boolean canPackageEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorPackage entity) {
		return getOutputSide(world, x, y, z) == dir;
	}

	@Override
	public void onPackageEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorPackage entity) {
		TileEntityCraneUnboxer unboxer = (TileEntityCraneUnboxer) world.getTileEntity(new BlockPos(x, y, z));
		ForgeDirection accessedSide = getOutputSide(world, x, y, z).getOpposite();

		for(ItemStack stack : entity.getItemStacks()) {
			ItemStack remainder = CraneInserter.addToInventory(unboxer, unboxer.getSlotsForFace(com.hbm.lib.ForgeDirection.getOrientation(accessedSide.ordinal()).toEnumFacing()), stack, accessedSide.ordinal());
			
			if(remainder != null && remainder.getCount() > 0) {
				EntityItem drop = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, remainder.copy());
				world.spawnEntity(drop);
			}
		}
	}
	
	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}
	
	@Override
	public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		return Container.calcRedstoneFromInventory((TileEntityCraneUnboxer)world.getTileEntity(new BlockPos(x, y, z)));
	}
}
