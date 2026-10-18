package com.hbm.blocks.generic;

import com.hbm.blocks.BlockEnumMulti;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIcon;

public class BlockConcreteColoredExt extends BlockEnumMulti {

	public BlockConcreteColoredExt(Material mat) {
		super(mat, EnumConcreteType.class, true, true);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		
		if(meta == EnumConcreteType.MACHINE_STRIPE.ordinal() && (side == 0 || side == 1)) {
			return super.getIcon(side, EnumConcreteType.MACHINE.ordinal());
		}
		
		return super.getIcon(side, meta);
	}
	
	public enum EnumConcreteType {
		MACHINE,
		MACHINE_STRIPE,
		INDIGO,
		PURPLE,
		PINK,
		HAZARD,
		SAND,
		BRONZE
	}
}
