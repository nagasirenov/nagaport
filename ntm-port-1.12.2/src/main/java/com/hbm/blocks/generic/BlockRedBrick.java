package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.lib.RefStrings;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;

public class BlockRedBrick extends Block {

	/** All original raw nibble values are meaningful, including 6–15 with no front face. */
	public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);
	@Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, METADATA); }
	@Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(METADATA, meta & 15); }
	@Override public int getMetaFromState(IBlockState state) { return state.getValue(METADATA); }

	protected IIcon blockIcon;
	protected IIcon iconFront;
	protected IIcon iconTop;

	public BlockRedBrick(Material material) {
		super(material);
		setDefaultState(blockState.getBaseState().withProperty(METADATA, 0));
	}
	
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.iconFront = iconRegister.registerIcon(RefStrings.MODID + ":brick_red");
		this.iconTop = iconRegister.registerIcon(RefStrings.MODID + ":brick_red_top");
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":brick_base");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return side == meta ? (side == 0 || side == 1 ? this.iconTop : this.iconFront) : this.blockIcon;
	}
	
	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState i, Random rand, int j) {
		return null;
	}

	@Override
	public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos nativePlacementPos, net.minecraft.block.state.IBlockState nativePlacementState, EntityLivingBase player, ItemStack stack) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		int l = determinePlacementOrientation(nativePlacementPos, player);
		if(world == null) throw new NullPointerException();
		if(x >= -30000000 && x < 30000000 && z >= -30000000 && z < 30000000 && y >= 0 && y < 256) {
			IBlockState current = world.getBlockState(nativePlacementPos);
			com.hbm.world.WorldBlockState.write(world, x, y, z, current.getBlock().getStateFromMeta(l), 2);
		}
	}

	/** Original piston orientation, deliberately not native half-block/eye-height orientation. */
	private static int determinePlacementOrientation(BlockPos pos, EntityLivingBase player) {
		if(MathHelper.abs((float) player.posX - pos.getX()) < 2.0F && MathHelper.abs((float) player.posZ - pos.getZ()) < 2.0F) {
			// Native posY is feet-relative; the original posY - yOffset is that same position.
			double height = player.posY + 1.82D;
			if(height - pos.getY() > 2.0D) return 1;
			if(pos.getY() - height > 0.0D) return 0;
		}
		int yaw = MathHelper.floor((double) (player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		return yaw == 0 ? 2 : (yaw == 1 ? 5 : (yaw == 2 ? 3 : (yaw == 3 ? 4 : 0)));
	}

}
