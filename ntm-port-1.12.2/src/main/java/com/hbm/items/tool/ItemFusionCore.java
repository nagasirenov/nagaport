package com.hbm.items.tool;

import java.util.List;

import com.hbm.items.armor.ArmorFSB;
import com.hbm.items.armor.ArmorFSBPowered;
import com.hbm.util.BobMathUtil;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemFusionCore extends Item {

	private int charge;

	public ItemFusionCore(int charge) {
		this.charge = charge;
	}

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand nativeHand) {
		ItemStack stack = player.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(stack, world, player));
	}

	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(ArmorFSB.hasFSBArmorIgnoreCharge(player) && com.hbm.util.PlayerInventoryRead.get(player.inventory.armorInventory, 2).getItem() instanceof ArmorFSBPowered) {

			for(ItemStack st : player.inventory.armorInventory) {
				if (st == null || st.isEmpty()) continue;

				if(st == null)
					continue;

				if(st.getItem() instanceof IBatteryItem) {

					long maxcharge = ((IBatteryItem) st.getItem()).getMaxCharge(st);
					long charge = ((IBatteryItem) st.getItem()).getCharge(st);
					long newcharge = Math.min(charge + this.charge, maxcharge);

					((IBatteryItem) st.getItem()).setCharge(st, newcharge);
				}
			}

			stack.shrink(1);

			world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:item.battery"), net.minecraft.util.SoundCategory.PLAYERS, 1.0F, 1.0F);
		}

		return stack;
	}

	@Override
	public void addInformation(ItemStack itemstack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {

		list.add(TextFormatting.YELLOW + "Charges all worn armor pieces by " + BobMathUtil.getShortNumber(charge) + "HE");
		list.add("[Requires full electric set to be worn]");
	}
}
