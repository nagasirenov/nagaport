package com.hbm.blocks.network;

import com.hbm.lib.RefStrings;
import com.hbm.tileentity.network.TileEntityCableSwitch;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;

public class CableDetector extends BlockContainer {

	protected IIcon blockIcon;

	@SideOnly(Side.CLIENT)
	private IIcon iconOn;

	public static final PropertyBool ACTIVE = PropertyBool.create("active");

	public CableDetector(Material p_i45386_1_) {
		super(p_i45386_1_);
		this.setDefaultState(this.blockState.getBaseState().withProperty(ACTIVE, false));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, ACTIVE);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(ACTIVE, meta == 1);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(ACTIVE) ? 1 : 0;
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.iconOn = iconRegister.registerIcon(RefStrings.MODID + ":cable_detector_on");
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":cable_detector_off");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return metadata == 1 ? iconOn : blockIcon;
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityCableSwitch();
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();

		boolean on = world.isBlockPowered(new net.minecraft.util.math.BlockPos(x, y, z));
		int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );

		boolean update = false;

		if(on && meta == 0) {
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 1, 2);
			com.hbm.sound.WorldSoundEffects.play(world, x, y, z, "hbm:block.reactorStart", 1.0F, 1.0F);
			update = true;
		}

		if(!on && meta == 1) {
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 0, 2);
			com.hbm.sound.WorldSoundEffects.play(world, x, y, z, "hbm:block.reactorStart", 1.0F, 0.85F);
			update = true;
		}

		if(update) {
			TileEntityCableSwitch te = (TileEntityCableSwitch) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
			te.updateState();
		}
	}

	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.MODEL;
	}
}
