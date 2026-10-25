package com.hbm.items.machine;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.world.World;
import java.util.List;
import java.util.Locale;

import com.hbm.items.ItemEnumMulti;
import com.hbm.util.BobMathUtil;
import com.hbm.util.EnumUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import com.hbm.render.icon.IIcon;

public class ItemZirnoxRod extends ItemEnumMulti {

	public ItemZirnoxRod() {
		super(EnumZirnoxType.class, true, true);
		this.setMaxStackSize(1);
		this.canRepair = false;
	}
	
	public static void incrementLifeTime(ItemStack stack) {
		
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		int time = stack.getTagCompound().getInteger("life");
		
		stack.getTagCompound().setInteger("life", time + 1);
	}
	
	public static void setLifeTime(ItemStack stack, int time) {
		
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		stack.getTagCompound().setInteger("life", time);
	}
	
	public static int getLifeTime(ItemStack stack) {
		
		if(!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
			return 0;
		}
		
		return stack.getTagCompound().getInteger("life");
	}

	public boolean showDurabilityBar(ItemStack stack) {
		return getDurabilityForDisplay(stack) > 0D;
	}

	public double getDurabilityForDisplay(ItemStack stack) {
		EnumZirnoxType num = EnumUtil.grabEnumSafely(theEnum, stack.getItemDamage());
		return (double) getLifeTime(stack) / (double) num.maxLife;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		
		
		EnumZirnoxType num = EnumUtil.grabEnumSafely(theEnum, stack.getItemDamage());
		list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("trait.rbmk.depletion", ((int)((((double)getLifeTime(stack)) / (double)num.maxLife) * 100000)) / 1000D + "%"));
		String[] loc = I18nUtil.resolveKeyArray("desc.item.zirnox" + (num.breeding ? "BreedingRod" : "Rod"), BobMathUtil.getShortNumber(num.maxLife));

		if(num.breeding)
			loc = I18nUtil.resolveKeyArray("desc.item.zirnoxBreedingRod", BobMathUtil.getShortNumber(num.maxLife));
		else
			loc = I18nUtil.resolveKeyArray("desc.item.zirnoxRod", num.heat, BobMathUtil.getShortNumber(num.maxLife));
		
		for(String s : loc) {
			list.add(s);
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister reg) {
		Enum[] enums = theEnum.getEnumConstants();
		this.icons = new IIcon[enums.length];
		
		for(int i = 0; i < icons.length; i++) {
			Enum num = enums[i];
			this.icons[i] = reg.registerIcon(this.getRegistryName().toString() + "_" + num.name().toLowerCase(Locale.US));
		}
	}

	@Override
	public String getTranslationKey(ItemStack stack) {
		Enum num = EnumUtil.grabEnumSafely(theEnum, stack.getItemDamage());
		return super.getTranslationKey() + "_" + num.name().toLowerCase(Locale.US);
	}

	public static enum EnumZirnoxType {
		NATURAL_URANIUM_FUEL(250_000, 30),
		URANIUM_FUEL(200_000, 50),
		TH232(20_000, 0, true),
		THORIUM_FUEL(200_000, 40),
		MOX_FUEL(165_000, 75),
		PLUTONIUM_FUEL(175_000, 65),
		U233_FUEL(150_000, 100),
		U235_FUEL(165_000, 85),
		LES_FUEL(150_000, 150),
		LITHIUM(20_000, 0, true),
		ZFB_MOX(50_000, 35);
		
		public final int maxLife;
		public final int heat;
		public final boolean breeding;
		
		private EnumZirnoxType(int life, int heat, boolean breeding) {
			this.maxLife = life;
			this.heat = heat;
			this.breeding = breeding;
		}
		
		private EnumZirnoxType(int life, int heat) {
			this.maxLife = life;
			this.heat = heat;
			this.breeding = false;
		}
	}
}
