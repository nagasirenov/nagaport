package com.hbm.items.food;

import java.util.List;

import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;
import com.hbm.potion.HbmPotion;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class ItemAppleSchrabidium extends ItemFood {

	public ItemAppleSchrabidium(int p_i45339_1_, float p_i45339_2_, boolean p_i45339_3_) {
		super(p_i45339_1_, p_i45339_2_, p_i45339_3_);
        this.setHasSubtypes(true);
        this.setAlwaysEdible();
	}

    @Override
	@SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack p_77636_1_)
    {
        return p_77636_1_.getItemDamage() == 2;
    }

    @Override
	protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {

		if (!world.isRemote) {

			if(stack.getItem() == ModItems.apple_schrabidium) {
				if (stack.getItemDamage() == 0) {
					player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.REGENERATION, 600, 4));
					player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.RESISTANCE, 6000, 0));
					player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.FIRE_RESISTANCE, 6000, 0));
				}

				if (stack.getItemDamage() == 1) {

					player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.REGENERATION, 1200, 4));
					player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.RESISTANCE, 1200, 4));
					player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.FIRE_RESISTANCE, 1200, 0));
					player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.STRENGTH, 1200, 4));
					player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.HASTE, 1200, 2));
					player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.SPEED, 1200, 2));
					player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.JUMP_BOOST, 1200, 4));
					player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.HEALTH_BOOST, 1200, 9));
					player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.ABSORPTION, 1200, 4));
					player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.SATURATION, 1200, 9));
				}

				if (stack.getItemDamage() == 2) {

					player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.REGENERATION, 2147483647, 4));
					player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.RESISTANCE, 2147483647, 1));
					player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.FIRE_RESISTANCE, 2147483647, 0));
					player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.STRENGTH, 2147483647, 9));
					player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.HASTE, 2147483647, 4));
					player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.SPEED, 2147483647, 3));
					player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.JUMP_BOOST, 2147483647, 4));
					player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.HEALTH_BOOST, 2147483647, 24));
					player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.ABSORPTION, 2147483647, 14));
					player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.SATURATION, 2147483647, 99));
				}
			}

			if(stack.getItem() == ModItems.apple_lead) {

				if (stack.getItemDamage() == 0) {
					player.addPotionEffect(new PotionEffect(HbmPotion.lead, 15 * 20, 2));
				}

				if (stack.getItemDamage() == 1) {

					player.addPotionEffect(new PotionEffect(HbmPotion.lead, 60 * 20, 4));
				}

				if (stack.getItemDamage() == 2) {

					player.attackEntityFrom(ModDamageSource.lead, 500F);
				}
			}
		}
    }

    @Override
	public EnumRarity getRarity(ItemStack p_77613_1_)
    {
    	if(p_77613_1_.getItemDamage() == 0)
    	{
		return EnumRarity.UNCOMMON;
    	}

    	if(p_77613_1_.getItemDamage() == 1)
    	{
		return EnumRarity.RARE;
    	}

    	if(p_77613_1_.getItemDamage() == 2)
    	{
		return EnumRarity.EPIC;
    	}

		return EnumRarity.COMMON;
    }


		@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public void getSubItems(net.minecraft.creativetab.CreativeTabs tab, net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> list) {
		if(this.isInCreativeTab(tab)) {
			getSubItems(this, tab, list);
		}
	}

@SideOnly(Side.CLIENT)
    public void getSubItems(Item p_150895_1_, CreativeTabs p_150895_2_, List p_150895_3_)
    {
        p_150895_3_.add(new ItemStack(p_150895_1_, 1, 0));
        p_150895_3_.add(new ItemStack(p_150895_1_, 1, 1));
        p_150895_3_.add(new ItemStack(p_150895_1_, 1, 2));
    }

}
