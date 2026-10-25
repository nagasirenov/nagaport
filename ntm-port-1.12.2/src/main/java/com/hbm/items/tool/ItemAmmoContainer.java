package com.hbm.items.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.lib.RefStrings;
import com.hbm.util.InventoryUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;

public class ItemAmmoContainer extends Item {

	protected IIcon itemIcon;

	@SideOnly(Side.CLIENT) protected IIcon altIcon;

	public ItemAmmoContainer() {
		this.setMaxStackSize(1);
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
	public void getSubItems(Item item, CreativeTabs tabs, List list) {
		list.add(new ItemStack(item, 1, 0));
		list.add(new ItemStack(item, 1, 1));
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister icon) {
		this.itemIcon = icon.registerIcon(this.getRegistryName().toString());
		this.altIcon = icon.registerIcon(RefStrings.MODID + ":ammo_container_alt");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int meta) {
		return meta == 1 ? this.altIcon : this.itemIcon;
	}

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand nativeHand) {
		ItemStack stack = player.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(stack, world, player));
	}

	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		boolean makeshift = stack.getItemDamage() == 1;

		List<ItemStack> stacks = new ArrayList();

		for(ItemStack inv : player.inventory.mainInventory) {
			if (inv == null || inv.isEmpty()) continue;
			if(inv != null && inv.getItem() instanceof ItemGunBaseNT) {
				ItemGunBaseNT gun = (ItemGunBaseNT) inv.getItem();
				if(gun.defaultAmmo != null && !(makeshift && gun.isDefaultExpensive)) stacks.add(inv);
			}
		}

		if(stacks.size() <= 0) return stack;

		Collections.shuffle(stacks);

		int maxGunCount = 3;

		for(int i = 0; i < maxGunCount && i < stacks.size(); i++) {
			ItemStack gunStack = stacks.get(i);
			ItemGunBaseNT gun = (ItemGunBaseNT) gunStack.getItem();
			ItemStack ammo = gun.defaultAmmo.copy();
			if(makeshift) ammo.setCount((int) Math.ceil(ammo.getCount() / 2D));
			ItemStack remainder = InventoryUtil.tryAddItemToInventory(player.inventory.mainInventory, ammo);
			if(remainder != null && remainder.getCount() > 0) player.dropItem(remainder, false);
		}

		world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:item.unpack"), net.minecraft.util.SoundCategory.PLAYERS, 1.0F, 1.0F);
		player.inventoryContainer.detectAndSendChanges();
		stack.shrink(1);

		return stack;
	}

	@Override
	public void addInformation(ItemStack stack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {
		String[] lines = I18nUtil.resolveKeyArray(this.getTranslationKey() + (stack.getItemDamage() == 1 ? ".1" : "") + ".desc");
		for(String line : lines) list.add(TextFormatting.YELLOW + line);
	}
}
