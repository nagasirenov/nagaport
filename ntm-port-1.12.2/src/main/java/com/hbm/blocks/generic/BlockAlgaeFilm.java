package com.hbm.blocks.generic;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.tileentity.machine.TileEntityAlgaeFilm;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

public class BlockAlgaeFilm extends BlockContainer implements ILookOverlay, ITooltipProvider {

	public BlockAlgaeFilm(Material mat) {
		super(mat);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, FACING); }
    @Override public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = meta >= 2 && meta <= 5 ? EnumFacing.byIndex(meta) : EnumFacing.NORTH;
        return getDefaultState().withProperty(FACING, facing.getAxis().isHorizontal() ? facing : EnumFacing.NORTH);
    }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(FACING).getIndex(); }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
    @Override public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT; }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
        world.setBlockState(pos, state.withProperty(FACING, player.getHorizontalFacing().getOpposite()), 2);
    }

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityAlgaeFilm();
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {

		TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));

		if(!(tile instanceof TileEntityAlgaeFilm)) return;

		TileEntityAlgaeFilm film = (TileEntityAlgaeFilm) tile;

		List<String> text = new ArrayList<>();

		if(!film.canOperate) {
			text.add("&[" + (BobMathUtil.getBlink() ? 0xff0000 : 0xffff00) + "&]! ! ! " + I18nUtil.resolveKey("atmosphere.noGravity") + " ! ! !");
		}

		text.add(TextFormatting.GREEN + "-> " + TextFormatting.RESET + film.tanks[0].getTankType().getLocalizedName() + ": " + film.tanks[0].getFill() + "/" + film.tanks[0].getMaxFill() + "mB");
		text.add(TextFormatting.RED + "<- " + TextFormatting.RESET + film.tanks[1].getTankType().getLocalizedName() + ": " + film.tanks[1].getFill() + "/" + film.tanks[1].getMaxFill() + "mB");

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		addStandardInfo(stack, player, list, ext);
	}

}
