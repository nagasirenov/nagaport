package com.hbm.blocks.machine;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.IBomb;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityHatch;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import com.hbm.render.icon.IIcon;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BlockSeal extends Block implements IBomb {

	protected IIcon blockIcon;

	@SideOnly(Side.CLIENT)
	private IIcon iconTop;

	public BlockSeal(Material p_i45394_1_) {
		super(p_i45394_1_);
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.iconTop = iconRegister.registerIcon(RefStrings.MODID + ":seal_frame");
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":seal_controller");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		metadata = metadata % 4;
		if(metadata == 0 || metadata == 1)
			metadata += 4;
		return side == 1 ? this.iconTop : (side == 0 ? this.iconTop : (metadata == 0 && side == 3 ? this.blockIcon : (side == metadata ? this.blockIcon : this.iconTop)));
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState p_149650_1_, Random p_149650_2_, int p_149650_3_) {
		return Item.getItemFromBlock(this);
	}

	@Override
	public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos nativePlacementPos, net.minecraft.block.state.IBlockState nativePlacementState, EntityLivingBase player, ItemStack itemStack) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

		if(i == 0) {
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 2, 2);
		}
		if(i == 1) {
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 1, 2);
		}
		if(i == 2) {
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 3, 2);
		}
		if(i == 3) {
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 0, 2);
		}
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;

		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			int i = BlockSeal.getFrameSize(world, x, y, z);

			if(i != 0)
				if(BlockSeal.isSealClosed(world, x, y, z, i))
					BlockSeal.openSeal(world, x, y, z, i);
				else
					BlockSeal.closeSeal(world, x, y, z, i);

			return true;
		} else {
			return false;
		}
	}

	public static int getFrameSize(World world, int x, int y, int z) {

		int max = 7;

		for(int size = 1; size < max; size++) {

			boolean valid = true;

			int xOff = 0;
			int zOff = 0;
			if(com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) % 4 == 2)
				zOff += size;
			if(com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) % 4 == 3)
				zOff -= size;
			if(com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) % 4 == 0)
				xOff += size;
			if(com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) % 4 == 1)
				xOff -= size;

			for(int X = x - size; X <= x + size; X++) {
				if(com.hbm.world.WorldBlockState.read(world, X + xOff, y, z + size + zOff).getBlock() != ModBlocks.seal_frame && com.hbm.world.WorldBlockState.read(world, X + xOff, y, z + size + zOff).getBlock() != ModBlocks.seal_controller)
					valid = false;
			}
			for(int X = x - size; X <= x + size; X++) {
				if(com.hbm.world.WorldBlockState.read(world, X + xOff, y, z - size + zOff).getBlock() != ModBlocks.seal_frame && com.hbm.world.WorldBlockState.read(world, X + xOff, y, z - size + zOff).getBlock() != ModBlocks.seal_controller)
					valid = false;
			}
			for(int Z = z - size; Z <= z + size; Z++) {
				if(com.hbm.world.WorldBlockState.read(world, x - size + xOff, y, Z + zOff).getBlock() != ModBlocks.seal_frame && com.hbm.world.WorldBlockState.read(world, x - size + xOff, y, Z + zOff).getBlock() != ModBlocks.seal_controller)
					valid = false;
			}
			for(int Z = z - size; Z <= z + size; Z++) {
				if(com.hbm.world.WorldBlockState.read(world, x + size + xOff, y, Z + zOff).getBlock() != ModBlocks.seal_frame && com.hbm.world.WorldBlockState.read(world, x + size + xOff, y, Z + zOff).getBlock() != ModBlocks.seal_controller)
					valid = false;
			}
			/*
			 * for(int X = x - size + 1; X <= x + size - 1; X++) { for(int Z = z
			 * - size + 1; Z <= z + size - 1; Z++) { //if(world.getBlock(X +
			 * size + xOff, y, Z + zOff) != ModBlocks.block_steel && //
			 * world.getBlock(X + size + xOff, y, Z + zOff) != Blocks.AIR) //
			 * valid = false; com.hbm.world.WorldBlockState.writeLegacyBlock(world, X + xOff, y, Z + zOff,
			 * ModBlocks.block_steel); System.out.println(valid); } }
			 */

			if(valid)
				return size;
		}

		return 0;
	}

	public static void closeSeal(World world, int x, int y, int z, int size) {

		int xOff = 0;
		int zOff = 0;
		if(com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) % 4 == 2)
			zOff += size;
		if(com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) % 4 == 3)
			zOff -= size;
		if(com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) % 4 == 0)
			xOff += size;
		if(com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) % 4 == 1)
			xOff -= size;

		for(int X = x - size + 1; X <= x + size - 1; X++) {
			for(int Z = z - size + 1; Z <= z + size - 1; Z++) {
				if(com.hbm.world.WorldBlockState.read(world, X + xOff, y, Z + zOff).getBlock() == Blocks.AIR && !world.isRemote) {
					com.hbm.world.WorldBlockState.writeLegacyBlock(world, X + xOff, y, Z + zOff, ModBlocks.seal_hatch);
					TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(X + xOff, y, Z + zOff));
					if(te != null && te instanceof TileEntityHatch)
						((TileEntityHatch) te).setControllerPos(x, y, z);

				}
			}
		}
	}

	public static void openSeal(World world, int x, int y, int z, int size) {

		int xOff = 0;
		int zOff = 0;
		if(com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) % 4 == 2)
			zOff += size;
		if(com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) % 4 == 3)
			zOff -= size;
		if(com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) % 4 == 0)
			xOff += size;
		if(com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) % 4 == 1)
			xOff -= size;

		for(int X = x - size + 1; X <= x + size - 1; X++) {
			for(int Z = z - size + 1; Z <= z + size - 1; Z++) {
				if(com.hbm.world.WorldBlockState.read(world, X + xOff, y, Z + zOff).getBlock() == ModBlocks.seal_hatch && !world.isRemote) {
					{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = X + xOff; int nativePlacementY = y; int nativePlacementZ = Z + zOff; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
				}
			}
		}
	}

	public static boolean isSealClosed(World world, int x, int y, int z, int size) {

		int xOff = 0;
		int zOff = 0;
		if(com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) % 4 == 2)
			zOff += size;
		if(com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) % 4 == 3)
			zOff -= size;
		if(com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) % 4 == 0)
			xOff += size;
		if(com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) % 4 == 1)
			xOff -= size;

		for(int X = x - size + 1; X <= x + size - 1; X++) {
			for(int Z = z - size + 1; Z <= z + size - 1; Z++) {
				if(com.hbm.world.WorldBlockState.read(world, X + xOff, y, Z + zOff).getBlock() == ModBlocks.seal_hatch) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public BombReturnCode explode(World world, int x, int y, int z) {

		if(!world.isRemote) {
			int i = BlockSeal.getFrameSize(world, x, y, z);

			if(i != 0) {
				if(BlockSeal.isSealClosed(world, x, y, z, i)) {
					BlockSeal.openSeal(world, x, y, z, i);
				} else {
					BlockSeal.closeSeal(world, x, y, z, i);
				}

				return BombReturnCode.TRIGGERED;
			}

			return BombReturnCode.ERROR_INCOMPATIBLE;
		}

		return BombReturnCode.UNDEFINED;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		if(world.isBlockPowered(new net.minecraft.util.math.BlockPos(x, y, z))) {
			int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
			if(meta < 4) {
				com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, meta + 4, 2);

				int i = BlockSeal.getFrameSize(world, x, y, z);

				if(i != 0)
					if(BlockSeal.isSealClosed(world, x, y, z, i))
						BlockSeal.openSeal(world, x, y, z, i);
					else
						BlockSeal.closeSeal(world, x, y, z, i);
			}
		} else {
			int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
			if(meta >= 4) {
				com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, meta % 4, 2);
			}
		}
	}

}
