package com.hbm.blocks.machine.pile;

import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;

import api.hbm.block.IToolable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.item.Item;

public class BlockGraphiteBreedingProduct extends BlockGraphiteDrilledBase implements IToolable {
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		super.registerBlockIcons(iconRegister);
		this.blockIconAluminum = iconRegister.registerIcon(RefStrings.MODID + ":block_graphite_tritium_aluminum");
	}
	
	@Override
	protected Item getInsertedItem() {
		return ModItems.cell_tritium;
	}
}
