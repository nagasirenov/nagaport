package com.hbm.items.machine;

import java.util.List;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import com.hbm.render.icon.IIcon;

public class ItemFluidIcon extends Item {

	IIcon overlayIcon;

	public ItemFluidIcon() {
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tabs, List list) {
		FluidType[] order = Fluids.getInNiceOrder();
		for(int i = 1; i < order.length; ++i) {
			list.add(new ItemStack(item, 1, order[i].getID()));
		}
	}

	@Override
	public void addInformation(ItemStack stack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {
		if(stack.hasTagCompound()) {
			if(getQuantity(stack) > 0) {
				list.add(getQuantity(stack) + "mB");
			} else if(getAtmospheres(stack) > 0) {
				double pressure = BobMathUtil.roundDecimal(getAtmospheres(stack), 3);
				list.add(pressure + "atm");
			}

			if(getPressure(stack) > 0) {
				list.add(TextFormatting.RED + "" + getPressure(stack) + "PU");
				list.add((BobMathUtil.getBlink() ? TextFormatting.RED : TextFormatting.DARK_RED) + "Pressurized, use compressor!");
			}
		}

		Fluids.fromID(stack.getItemDamage()).addInfo(list);
	}

	public static ItemStack addQuantity(ItemStack stack, int i) {
		if(i <= 0) return stack;
		if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setInteger("fill", i);
		return stack;
	}

	public static ItemStack addPressure(ItemStack stack, int i) {
		if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setInteger("pressure", i);
		return stack;
	}

	public static ItemStack addAtmospheres(ItemStack stack, double atm) {
		if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setDouble("atm", atm);
		return stack;
	}

	public static ItemStack make(FluidStack stack) {
		return make(stack.type, stack.fill, stack.pressure);
	}

	public static ItemStack make(FluidType fluid, int i) {
		return make(fluid, i, 0);
	}

	public static ItemStack make(FluidType fluid, int i, int pressure) {
		return addPressure(addQuantity(new ItemStack(ModItems.fluid_icon, 1, fluid.ordinal()), i), pressure);
	}

	public static ItemStack make(FluidType fluid, double atm) {
		return addAtmospheres(new ItemStack(ModItems.fluid_icon, 1, fluid.getID()), atm);
	}

	public static int getQuantity(ItemStack stack) {
		if(!stack.hasTagCompound()) return 0;
		return stack.getTagCompound().getInteger("fill");
	}

	public static int getPressure(ItemStack stack) {
		if(!stack.hasTagCompound()) return 0;
		return stack.getTagCompound().getInteger("pressure");
	}

	public static double getAtmospheres(ItemStack stack) {
		if(!stack.hasTagCompound()) return 0;
		return stack.getTagCompound().getDouble("atm");
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String s = (I18nUtil.resolveKey(Fluids.fromID(stack.getItemDamage()).getConditionalName())).trim();

		if(s != null) {
			return s;
		}

		return "Unknown";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int p_82790_2_) {
		int j = Fluids.fromID(stack.getItemDamage()).getColor();

		if(j < 0) {
			j = 16777215;
		}

		return j;
	}

}
