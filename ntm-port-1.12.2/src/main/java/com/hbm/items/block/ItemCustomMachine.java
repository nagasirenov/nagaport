package com.hbm.items.block;

import java.util.List;

import com.hbm.config.CustomMachineConfigJSON;
import com.hbm.config.CustomMachineConfigJSON.MachineConfiguration;
import com.hbm.main.MainRegistry;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemCustomMachine extends ItemBlock {

	public ItemCustomMachine(Block block) {
		super(block);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}

		@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public void getSubItems(net.minecraft.creativetab.CreativeTabs tab, net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> list) {
		if(this.isInCreativeTab(tab)) {
			getSubItems(this, tab, list);
		}
	}

@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {

		for(int i = 0; i < CustomMachineConfigJSON.niceList.size(); i++) {
			ItemStack stack = new ItemStack(item, 1, i + 100);
			list.add(stack);
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {

		int id = stack.getItemDamage() - 100;

		if(id >= 0 && id < CustomMachineConfigJSON.customMachines.size()) {
			MachineConfiguration conf = CustomMachineConfigJSON.niceList.get(id);

			if(conf != null) {
				String localized = conf.localization.get(MainRegistry.proxy.getLanguageCode());
				return localized != null ? localized : conf.localizedName;
			}
		}

		return "INVALID MACHINE CONTROLLER";
	}
}
