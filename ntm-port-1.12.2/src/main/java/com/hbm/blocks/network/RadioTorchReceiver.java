package com.hbm.blocks.network;

import com.hbm.lib.RefStrings;
import com.hbm.tileentity.network.TileEntityRadioTorchReceiver;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class RadioTorchReceiver extends RadioTorchRWBase {

	public RadioTorchReceiver() {
		super();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":rtty_rec_off");
		this.iconOn = iconRegister.registerIcon(RefStrings.MODID + ":rtty_rec_on");
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		TileEntityRadioTorchReceiver tile = new TileEntityRadioTorchReceiver();
		tile.lastUpdate = world.getTotalWorldTime();
		return tile;
	}

	@Override
	public boolean canProvidePower(net.minecraft.block.state.IBlockState state) {
		return true;
	}

	@Override
	public int getWeakPower(net.minecraft.block.state.IBlockState blockState, IBlockAccess world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumFacing side) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();

		TileEntity tile = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(tile instanceof TileEntityRadioTorchReceiver) {
			int state = ((TileEntityRadioTorchReceiver) tile).lastState;
			return state;
		}

		return 0;
	}
}
