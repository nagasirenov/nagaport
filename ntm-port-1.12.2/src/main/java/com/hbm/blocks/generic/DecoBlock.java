package com.hbm.blocks.generic;

import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumBlockRenderType;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.LegacyBlockTextures;
import com.hbm.tileentity.deco.TileEntityDecoBlock;
import com.hbm.world.gen.nbt.INBTBlockTransformable;

import api.hbm.block.IToolable;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class DecoBlock extends BlockContainer implements IToolable, INBTBlockTransformable {

	public DecoBlock withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return this;
	}

	Random rand = new Random();

	public DecoBlock(Material p_i45386_1_) {
		super(p_i45386_1_);
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {
		if(tool != ToolType.SCREWDRIVER) return false;
		if(this != ModBlocks.steel_wall && this != ModBlocks.steel_corner) return false;

		int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );

		if(!player.isSneaking()) {
			if(meta == 3) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 4, 3);
			else if(meta == 4) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 2, 3);
			else if(meta == 2) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 5, 3);
			else if(meta == 5) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 3, 3);
		} else {
			if(meta == 3) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 5, 3);
			else if(meta == 4) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 3, 3);
			else if(meta == 2) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 4, 3);
			else if(meta == 5) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 2, 3);
		}

		return true;
	}

	@Override @SideOnly(Side.CLIENT) public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) { return true; }

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		if(this == ModBlocks.steel_scaffold || this == ModBlocks.steel_beam || this == ModBlocks.steel_wall || this == ModBlocks.steel_corner) return null;
		return new TileEntityDecoBlock();
	}

	public static int renderIDBeam = 0;
	public static int renderIDWall = 0;
	public static int renderIDCorner = 0;

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		if(this == ModBlocks.steel_wall || this == ModBlocks.steel_corner || this == ModBlocks.steel_beam) {
			return EnumBlockRenderType.MODEL;
		}
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState p_149650_1_, Random p_149650_2_, int p_149650_3_) {
		return Item.getItemFromBlock(this);
	}

	@Override
	public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos nativePlacementPos, net.minecraft.block.state.IBlockState nativePlacementState, EntityLivingBase player, ItemStack itemStack) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

		if(i == 0) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 3, 2);
		if(i == 1) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 4, 2);
		if(i == 2) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 2, 2);
		if(i == 3) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 5, 2);
	}

	private static final AxisAlignedBB ROOF_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.0625D, 1.0D);
	private static final AxisAlignedBB BEAM_AABB = new AxisAlignedBB(7 * 0.0625D, 0.0D, 7 * 0.0625D, 9 * 0.0625D, 1.0D, 9 * 0.0625D);
	private static final AxisAlignedBB SCAFFOLD_DEFAULT = new AxisAlignedBB(0.0D, 0.0D, 2 * 0.0625D, 1.0D, 1.0D, 14 * 0.0625D);
	private static final AxisAlignedBB SCAFFOLD_SIDE = new AxisAlignedBB(2 * 0.0625D, 0.0D, 0.0D, 14 * 0.0625D, 1.0D, 1.0D);

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		int te = com.hbm.world.WorldBlockState.readMetadata(source, pos.getX(), pos.getY(), pos.getZ());
		double f = 0.0625D;

		if(this == ModBlocks.steel_wall) {
			switch(te) {
			case 4: return new AxisAlignedBB(14 * f, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
			case 2: return new AxisAlignedBB(0.0D, 0.0D, 14 * f, 1.0D, 1.0D, 1.0D);
			case 5: return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 2 * f, 1.0D, 1.0D);
			case 3: return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 2 * f);
			}
		}

		if(this == ModBlocks.steel_corner) {
			return FULL_BLOCK_AABB;
		}

		if(this == ModBlocks.steel_roof) {
			return ROOF_AABB;
		}

		if(this == ModBlocks.steel_beam) {
			return BEAM_AABB;
		}

		if(this == ModBlocks.steel_scaffold) {
			switch(te) {
			case 4:
			case 5:
				return SCAFFOLD_SIDE;
			case 2:
			case 3:
			default:
				return SCAFFOLD_DEFAULT;
			}
		}

		return FULL_BLOCK_AABB;
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entity, boolean actualState) {
		if(this == ModBlocks.steel_corner) {
			int meta = this.getMetaFromState(state);
			switch(meta) {
			case 2:
				addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.25D, 0D, 0.875D, 1D, 1D, 1D));
				addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0D, 0D, 0.75D, 0.25D, 1D, 1D));
				addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0D, 0D, 0D, 0.125D, 1D, 0.75D));
				break;
			case 3:
				addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0D, 0D, 0D, 0.75D, 1D, 0.125D));
				addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.75D, 0D, 0D, 1D, 1D, 0.25D));
				addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.875D, 0D, 0.25D, 1D, 1D, 1D));
				break;
			case 4:
				addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.875D, 0D, 0D, 1D, 1D, 0.75D));
				addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.75D, 0D, 0.75D, 1D, 1D, 1D));
				addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0D, 0D, 0.875D, 0.75D, 1D, 1D));
				break;
			case 5:
				addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0D, 0D, 0.25D, 0.125D, 1D, 1D));
				addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0D, 0D, 0D, 0.25D, 1D, 0.25D));
				addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.25D, 0D, 0D, 1D, 1D, 0.125D));
				break;
			default:
				super.addCollisionBoxToList(state, world, pos, entityBox, collidingBoxes, entity, actualState);
				break;
			}
		} else {
			super.addCollisionBoxToList(state, world, pos, entityBox, collidingBoxes, entity, actualState);
		}
	}

	@Override
	public int transformMeta(int meta, int coordBaseMode) {
		return INBTBlockTransformable.transformMetaDeco(meta, coordBaseMode);
	}

}
