package com.hbm.blocks.network;

import com.hbm.lib.RefStrings;
import com.hbm.tileentity.network.TileEntityRadioTorchSender;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class RadioTorchSender extends RadioTorchRWBase {

	public RadioTorchSender() {
		super();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":rtty_sender_off");
		this.iconOn = iconRegister.registerIcon(RefStrings.MODID + ":rtty_sender_on");
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityRadioTorchSender();
	}
}
