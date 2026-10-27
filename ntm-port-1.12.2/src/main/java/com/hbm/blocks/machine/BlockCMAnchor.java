package com.hbm.blocks.machine;

import com.hbm.lib.RefStrings;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import com.hbm.render.icon.IIcon;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BlockCMAnchor extends Block {

	protected IIcon blockIcon;

	@SideOnly(Side.CLIENT)
	private IIcon iconFront;

	public static final PropertyInteger FACING = PropertyInteger.create("facing", 0, 5);

	public BlockCMAnchor() {
		super(Material.IRON);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, 0));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(FACING, meta >= 0 && meta <= 5 ? meta : 0);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING);
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.iconFront = iconRegister.registerIcon(RefStrings.MODID + ":cmt_terminal_front");
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":cmt_terminal_side");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return metadata == 0 && side == 3 ? this.iconFront : (side == metadata ? this.iconFront : this.blockIcon);
	}

	@Override
	public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos nativePlacementPos, net.minecraft.block.state.IBlockState nativePlacementState, EntityLivingBase player, ItemStack itemStack) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

		if(i == 0) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 2, 2);
		if(i == 1) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 5, 2);
		if(i == 2) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 3, 2);
		if(i == 3) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 4, 2);
	}
}
