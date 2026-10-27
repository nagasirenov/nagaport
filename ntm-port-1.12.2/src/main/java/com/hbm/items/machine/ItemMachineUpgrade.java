package com.hbm.items.machine;

import java.util.List;

import com.hbm.items.ModItems;
import com.hbm.tileentity.IUpgradeInfoProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.world.World;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

public class ItemMachineUpgrade extends Item {
	
	public UpgradeType type;
	public int tier = 0;
	
	public ItemMachineUpgrade() {
		this.setMaxStackSize(1);
		this.type = UpgradeType.SPECIAL;
	}
	
	public ItemMachineUpgrade(UpgradeType type) {
		this.setMaxStackSize(1);
		this.type = type;
	}
	
	public ItemMachineUpgrade(UpgradeType type, int tier) {
		this(type);
		this.tier = tier;
	}
	
	@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
		
		GuiScreen open = Minecraft.getMinecraft().currentScreen;
		
		if(open != null && open instanceof GuiContainer) {
			GuiContainer guiContainer = (GuiContainer) open;
			Container container = guiContainer.inventorySlots;
			if(container.inventorySlots.size() > 0) {
				Slot first = container.getSlot(0);
				IInventory inv = (IInventory) first.inventory;
				if(inv instanceof IUpgradeInfoProvider) {
					IUpgradeInfoProvider provider = (IUpgradeInfoProvider) inv;
					if(provider.canProvideInfo(this.type, this.tier, flag.isAdvanced())) {
						provider.provideInfo(this.type, this.tier, list, flag.isAdvanced());
						return;
					}
				}
			}
		}
		
		if(this == ModItems.upgrade_radius) {
			list.add(TextFormatting.RED + "Forcefield Range Upgrade");
			list.add("Radius +16 / Consumption +500");
			list.add("Stacks to 16");
		}
		
		if(this == ModItems.upgrade_health) {
			list.add(TextFormatting.RED + "Forcefield Health Upgrade");
			list.add("Max. Health +50 / Consumption +250");
			list.add("Stacks to 16");
		}
		
		if(this == ModItems.upgrade_smelter) {
			list.add(TextFormatting.RED + "Mining Laser Upgrade");
			list.add("Smelts blocks. Easy enough.");
		}
		
		if(this == ModItems.upgrade_shredder) {
			list.add(TextFormatting.RED + "Mining Laser Upgrade");
			list.add("Crunches ores");
		}
		
		if(this == ModItems.upgrade_centrifuge) {
			list.add(TextFormatting.RED + "Mining Laser Upgrade");
			list.add("Hopefully self-explanatory");
		}
		
		if(this == ModItems.upgrade_crystallizer) {
			list.add(TextFormatting.RED + "Mining Laser Upgrade");
			list.add("Your new best friend");
		}
		
		if(this == ModItems.upgrade_screm) {
			list.add(TextFormatting.RED + "Mining Laser Upgrade");
			list.add("It's like in Super Mario where all blocks are");
			list.add("actually Toads, but here it's Half-Life scientists");
			list.add("and they scream. A lot.");
		}
		
		if(this == ModItems.upgrade_nullifier) {
			list.add(TextFormatting.RED + "Mining Laser Upgrade");
			list.add("50% chance to override worthless items with /dev/zero");
			list.add("50% chance to move worthless items to /dev/null");
		}
		
		if(this == ModItems.upgrade_gc_speed) {
			list.add(TextFormatting.RED + "Gas Centrifuge Upgrade");
			list.add("Allows for total isotopic separation of HEUF6");
			list.add(TextFormatting.YELLOW + "also your centrifuge goes sicko mode");
		}
	}
	
	public static enum UpgradeType {
		SPEED,
		EFFECT,
		POWER,
		FORTUNE,
		AFTERBURN,
		OVERDRIVE,
		SPECIAL,
		LM_DESROYER,
		LM_SCREM,
		LM_SMELTER(true),
		LM_SHREDDER(true),
		LM_CENTRIFUGE(true),
		LM_CRYSTALLIZER(true),
		GS_SPEED;
		
		public boolean mutex = false;
		
		private UpgradeType() { }
		
		private UpgradeType(boolean mutex) {
			this.mutex = mutex;
		}
	}

}
