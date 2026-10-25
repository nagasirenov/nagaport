package com.hbm.items.food;

import java.util.Locale;

import com.hbm.extprop.HbmPlayerProps;
import com.hbm.items.ItemEnumMulti;
import com.hbm.lib.ModDamageSource;
import com.hbm.potion.HbmPotion;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;

public class ItemFlask extends ItemEnumMulti {

	public static enum EnumInfusion {
		SHIELD,
		NITAN
	}

	public ItemFlask() {
		super(EnumInfusion.class, true, true);
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister reg) {

		Enum[] enums = theEnum.getEnumConstants();
		this.icons = new IIcon[enums.length];

		for(int i = 0; i < icons.length; i++) {
			Enum num = enums[i];
			// The original built flask icons from the setTextureName base "flask".
			String base = com.hbm.render.item.NativeItemTextureRegistry.texture(this);
			if(base == null) base = this.getIconString();
			if(base == null) base = this.getRegistryName().toString();
			this.icons[i] = reg.registerIcon(base + "_" + num.name().toLowerCase(Locale.US));
		}
	}

	@Override
	public net.minecraft.item.ItemStack onItemUseFinish(ItemStack stack, World world, net.minecraft.entity.EntityLivingBase entity) {
		EntityPlayer player = (EntityPlayer) entity;
		HbmPlayerProps props = HbmPlayerProps.getData(player);
		if(!player.capabilities.isCreativeMode) {
			stack.shrink(1);
		}

		if(world.isRemote)
			return stack;

		if(stack.getItemDamage() == EnumInfusion.SHIELD.ordinal()) {
			float infusion = 5F;
			props.maxShield = Math.min(props.shieldCap, props.maxShield + infusion);
			props.shield = Math.min(props.shield + infusion, props.getEffectiveMaxShield());
		}

		if(stack.getItemDamage() == EnumInfusion.NITAN.ordinal()) {
			props.nitanCount = props.nitanCount + 1;
			player.addPotionEffect(new PotionEffect(HbmPotion.nitan, 60 * 20 * 3, 0));
			player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.RESISTANCE, 60 * 20 * 3, 3));

		}


		return stack;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack p_77626_1_) {
		return 32;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack p_77661_1_) {
		return EnumAction.DRINK;
	}

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand nativeHand) {
		ItemStack stack = player.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(stack, world, player));
	}


	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(stack.getItemDamage() == EnumInfusion.SHIELD.ordinal() && HbmPlayerProps.getData(player).maxShield >= HbmPlayerProps.shieldCap)
			return stack;

		player.setActiveHand(net.minecraft.util.EnumHand.MAIN_HAND);
		return stack;
	}
}
