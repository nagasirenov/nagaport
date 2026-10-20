package com.hbm.blocks.network;

import com.hbm.blocks.ILookOverlay;
import com.hbm.inventory.gui.GUIScreenRadioTorchLogic;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.network.TileEntityRadioTorchLogic;
import com.hbm.util.i18n.I18nUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

import java.util.ArrayList;
import java.util.List;

public class RadioTorchLogic extends RadioTorchRWBase {

	public RadioTorchLogic() {
		super();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":rtty_logic_off");
		this.iconOn = iconRegister.registerIcon(RefStrings.MODID + ":rtty_logic_on");
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		TileEntityRadioTorchLogic tile = new TileEntityRadioTorchLogic();
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

		if(tile instanceof TileEntityRadioTorchLogic) {
			int state = ((TileEntityRadioTorchLogic) tile).lastState;
			return state;
		}

		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void printHook(Pre event, World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(te instanceof TileEntityRadioTorchLogic) {
			TileEntityRadioTorchLogic radio = (TileEntityRadioTorchLogic) te;
			List<String> text = new ArrayList();
			if(radio.channel != null && !radio.channel.isEmpty()) text.add(TextFormatting.AQUA + "Freq: " + radio.channel);
			text.add(TextFormatting.RED + "Signal: " + radio.lastState);
			ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(te instanceof TileEntityRadioTorchLogic)
			return new GUIScreenRadioTorchLogic((TileEntityRadioTorchLogic) te);

		return null;
	}
}
