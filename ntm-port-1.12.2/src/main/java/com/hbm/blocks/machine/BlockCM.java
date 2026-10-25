package com.hbm.blocks.machine;

import java.util.Locale;

import com.hbm.blocks.BlockEnumMulti;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import com.hbm.render.icon.IIcon;

public class BlockCM extends BlockEnumMulti {

	public BlockCM(Material mat, Class<? extends Enum> theEnum, boolean multiName, boolean multiTexture) {
		super(mat, theEnum, multiName, multiTexture);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		Enum[] enums = theEnum.getEnumConstants();
		this.icons = new IIcon[enums.length];
		
		for(int i = 0; i < icons.length; i++) {
			Enum num = enums[i];
			this.icons[i] = reg.registerIcon(this.getRegistryName().toString() + "_" + num.name().toLowerCase(Locale.US));
		}
	}
}
