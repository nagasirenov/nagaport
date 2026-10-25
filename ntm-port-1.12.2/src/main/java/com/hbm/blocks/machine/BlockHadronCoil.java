package com.hbm.blocks.machine;

import java.util.List;
import java.util.Locale;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.render.block.ct.CTContext;
import com.hbm.render.block.ct.IBlockCT;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;

public class BlockHadronCoil extends Block implements IBlockCT, ITooltipProvider {

	public int factor;

	@Override
	public net.minecraft.block.Block setSoundType(net.minecraft.block.SoundType sound) {
		return super.setSoundType(sound);
	}

	public BlockHadronCoil(Material mat, int factor) {
		super(mat);
        setDefaultState(blockState.getBaseState().withProperty(CTContext.METADATA, 0));
		this.factor = factor;
	}

    @Override protected net.minecraft.block.state.BlockStateContainer createBlockState() { return CTContext.stateContainer(this); }
    @Override public net.minecraft.block.state.IBlockState getStateFromMeta(int metadata) { return getDefaultState().withProperty(CTContext.METADATA, metadata & 15); }
    @Override public int getMetaFromState(net.minecraft.block.state.IBlockState state) { return state.getValue(CTContext.METADATA); }
    @Override public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) { return net.minecraft.util.EnumBlockRenderType.MODEL; }
    @Override public net.minecraft.block.state.IBlockState getExtendedState(net.minecraft.block.state.IBlockState state, IBlockAccess world, net.minecraft.util.math.BlockPos pos) {
        return CTContext.extend(state, world, pos, this);
    }



	@Override
	public boolean canConnect(IBlockAccess world, int x, int y, int z, Block block) {
		return block instanceof BlockHadronCoil;
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		list.add(I18nUtil.resolveKey("info.coil") + ": " + String.format(Locale.US, "%,d", factor));
	}
}
