package com.hbm.items.food;

import java.util.List;

import com.hbm.items.ModItems;
import com.hbm.items.armor.ArmorFSB;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemPancake extends ItemFood {

	public ItemPancake(int p_i45339_1_, float p_i45339_2_, boolean p_i45339_3_) {
		super(p_i45339_1_, p_i45339_2_, p_i45339_3_);
		this.setAlwaysEdible();
	}


    @Override
	protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {

	for(ItemStack st : player.inventory.armorInventory) {
		if (st == null || st.isEmpty()) continue;

		if(st == null)
			continue;

		if(st.getItem() instanceof IBatteryItem) {
			((IBatteryItem)st.getItem()).setCharge(st, ((IBatteryItem)st.getItem()).getMaxCharge(st));
		}
	}
    }

    @Override
    public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand hand) {
	ItemStack stack = player.getHeldItem(hand);
	if(ArmorFSB.hasFSBArmorIgnoreCharge(player) && com.hbm.util.PlayerInventoryRead.get(player.inventory.armorInventory, 3).getItem() == ModItems.bj_helmet) {
	return super.onItemRightClick(world, player, hand);
	}

	if(!world.isRemote)
		player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Your teeth are too soft to eat this."));

	return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.FAIL, stack);
    }

	@Override
	public void addInformation(ItemStack itemstack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {
		list.add("Can be eaten to recharge lunar cybernetic armor");
		list.add("Not for people with weak molars");
		list.add("");
		list.add("Half burnt and smells horrible");
	}

}
