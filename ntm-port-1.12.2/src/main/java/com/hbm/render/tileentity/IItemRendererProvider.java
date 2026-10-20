package com.hbm.render.tileentity;

import net.minecraft.item.Item;
import com.hbm.render.item.ItemRenderBase;

public interface IItemRendererProvider {

	public Item getItemForRenderer();
	
	public default Item[] getItemsForRenderer() {
		return new Item[] { this.getItemForRenderer() };
	}
	
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public ItemRenderBase getRenderer();
}
