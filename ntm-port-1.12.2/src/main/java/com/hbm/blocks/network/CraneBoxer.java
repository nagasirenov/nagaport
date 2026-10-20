package com.hbm.blocks.network;

import api.hbm.conveyor.IConveyorItem;
import api.hbm.conveyor.IConveyorPackage;
import api.hbm.conveyor.IEnterableBlock;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.network.TileEntityCraneBase;
import com.hbm.tileentity.network.TileEntityCraneBoxer;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class CraneBoxer extends BlockCraneBase implements IEnterableBlock {

	public CraneBoxer() {
		super(Material.IRON);
		initModelTextures();
	}

	@Override
	public TileEntityCraneBase createNewTileEntity(World world, int meta) {
		return new TileEntityCraneBoxer();
	}
	
	@Override
	protected void initModelTextures() {
		super.initModelTextures();
		this.iconOut = new ResourceLocation(RefStrings.MODID, "blocks/crane_box");
		this.iconSideOut = new ResourceLocation(RefStrings.MODID, "blocks/crane_side_box");
		this.iconDirectional = new ResourceLocation(RefStrings.MODID, "blocks/crane_boxer_top");
		this.iconDirectionalUp = new ResourceLocation(RefStrings.MODID, "blocks/crane_boxer_side_up");
		this.iconDirectionalDown = new ResourceLocation(RefStrings.MODID, "blocks/crane_boxer_side_down");
		this.iconDirectionalTurnLeft = new ResourceLocation(RefStrings.MODID, "blocks/crane_boxer_top_left");
		this.iconDirectionalTurnRight = new ResourceLocation(RefStrings.MODID, "blocks/crane_boxer_top_right");
		this.iconDirectionalSideLeftTurnUp = new ResourceLocation(RefStrings.MODID, "blocks/crane_boxer_side_left_turn_up");
		this.iconDirectionalSideRightTurnUp = new ResourceLocation(RefStrings.MODID, "blocks/crane_boxer_side_right_turn_up");
		this.iconDirectionalSideLeftTurnDown = new ResourceLocation(RefStrings.MODID, "blocks/crane_boxer_side_left_turn_down");
		this.iconDirectionalSideRightTurnDown = new ResourceLocation(RefStrings.MODID, "blocks/crane_boxer_side_right_turn_down");
		this.iconDirectionalSideUpTurnLeft = new ResourceLocation(RefStrings.MODID, "blocks/crane_boxer_side_up_turn_left");
		this.iconDirectionalSideUpTurnRight = new ResourceLocation(RefStrings.MODID, "blocks/crane_boxer_side_up_turn_right");
		this.iconDirectionalSideDownTurnLeft = new ResourceLocation(RefStrings.MODID, "blocks/crane_boxer_side_down_turn_left");
		this.iconDirectionalSideDownTurnRight = new ResourceLocation(RefStrings.MODID, "blocks/crane_boxer_side_down_turn_right");
	}

    @Override
	public boolean canItemEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorItem entity) {
		return getInputSide(world, x, y, z) == dir;
	}

	@Override
	public void onItemEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorItem entity) {
		TileEntityCraneBoxer boxer = (TileEntityCraneBoxer) world.getTileEntity(new BlockPos(x, y, z));
		
		ItemStack remainder = CraneInserter.addToInventory(boxer, boxer.getSlotsForFace(com.hbm.lib.ForgeDirection.getOrientation(dir.ordinal()).toEnumFacing()), entity.getItemStack(), dir.ordinal());
		
		if(remainder != null && remainder.getCount() > 0) {
			EntityItem drop = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, remainder.copy());
			world.spawnEntity(drop);
		}
	}
	
	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}
	
	@Override
	public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		return Container.calcRedstoneFromInventory((TileEntityCraneBoxer)world.getTileEntity(new BlockPos(x, y, z)));
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		Block block = state.getBlock();
		int meta = getMetaFromState(state);
		this.dropContents(world, x, y, z, block, meta, 0, 21);
		super.breakBlock(world, pos, state);
	}

	@Override
	public boolean canPackageEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorPackage entity) {
		return true;
	}

	@Override
	public void onPackageEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorPackage entity) {
		TileEntityCraneBoxer unboxer = (TileEntityCraneBoxer) world.getTileEntity(new BlockPos(x, y, z));
		ForgeDirection accessedSide = getOutputSide(world, x, y, z).getOpposite();

		for(ItemStack stack : entity.getItemStacks()) {
			ItemStack remainder = CraneInserter.addToInventory(unboxer, unboxer.getSlotsForFace(com.hbm.lib.ForgeDirection.getOrientation(accessedSide.ordinal()).toEnumFacing()), stack, accessedSide.ordinal());
			
			if(remainder != null && remainder.getCount() > 0) {
				EntityItem drop = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, remainder.copy());
				world.spawnEntity(drop);
			}
		}
	}
}
