package com.hbm.blocks.network;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ILookOverlay;
import com.hbm.inventory.gui.GUIScreenRadioTorchController;
import com.hbm.tileentity.network.TileEntityRadioTorchController;
import com.hbm.util.Compat;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.redstoneoverradio.IRORInteractive;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import com.hbm.lib.ForgeDirection;

public class RadioTorchController extends RadioTorchBase {

	public RadioTorchController() {
		super("rtty_controller");
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityRadioTorchController();
	}

	@Override
	public boolean canBlockStay(World world, int x, int y, int z, ForgeDirection dir, Block b) {
		TileEntity tile = Compat.getTileStandard(world, x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ);
		return tile instanceof IRORInteractive;
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(te instanceof TileEntityRadioTorchController) {
			TileEntityRadioTorchController radio = (TileEntityRadioTorchController) te;
			List<String> text = new ArrayList();
			text.add(TextFormatting.AQUA + "Freq: " + radio.channel);
			ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
		if(te instanceof TileEntityRadioTorchController) return new GUIScreenRadioTorchController((TileEntityRadioTorchController) te);
		return null;
	}
}
