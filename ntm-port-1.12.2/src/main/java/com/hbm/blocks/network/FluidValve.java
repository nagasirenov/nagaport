package com.hbm.blocks.network;

import com.hbm.blocks.ILookOverlay;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.network.TileEntityFluidValve;
import com.hbm.util.i18n.I18nUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

import java.util.ArrayList;
import java.util.List;

public class FluidValve extends FluidDuctBase implements ILookOverlay {
	// 1.7.10 type 0 cube; BlockContainer would make it INVISIBLE in 1.12 (render-type parity audit).
	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.MODEL;
	}


	protected IIcon blockIcon;

	@SideOnly(Side.CLIENT)
	private IIcon iconOn;

	public static final PropertyBool OPEN = PropertyBool.create("open");

	public FluidValve(Material mat) {
		super(mat);
		this.setDefaultState(this.blockState.getBaseState().withProperty(OPEN, false));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, OPEN);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(OPEN, meta == 1);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(OPEN) ? 1 : 0;
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.iconOn = iconRegister.registerIcon(RefStrings.MODID + ":fluid_valve_on");
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":fluid_valve_off");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return metadata == 1 ? iconOn : blockIcon;
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityFluidValve();
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;


		if(world.isRemote) return true;

		if(super.onBlockActivated(world, nativePos, nativeState, player, nativeHand, nativeFacing, hitX, hitY, hitZ)) return true;

		if(!player.isSneaking()) {
			int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
			TileEntityFluidValve te = (TileEntityFluidValve) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
			if(meta == 0) {
				com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 1, 2);
				com.hbm.sound.WorldSoundEffects.play(world, x, y, z, "hbm:block.reactorStart", 1.0F, 1.0F);
			} else {
				com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 0, 2);
				com.hbm.sound.WorldSoundEffects.play(world, x, y, z, "hbm:block.reactorStart", 1.0F, 0.85F);
			}

			te.updateState();

			return true;
		} else {
			return false;
		}
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {

		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(!(te instanceof TileEntityFluidValve))
			return;

		TileEntityFluidValve duct = (TileEntityFluidValve) te;

		List<String> text = new ArrayList();
		text.add("&[" + duct.getType().getColor() + "&]" + duct.getType().getLocalizedName());
		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}
}
