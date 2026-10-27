package com.hbm.items.machine;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.render.icon.IIcon;
import com.hbm.render.icon.IIconRegister;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemFluidTemplate extends Item implements IItemFluidIdentifier {

	protected IIcon itemIcon;
	protected IIcon overlayIcon;

	public ItemFluidTemplate() {
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, net.minecraft.util.NonNullList<ItemStack> list) {
		if(this.isInCreativeTab(tab)) {
			FluidType[] order = Fluids.getInNiceOrder();
			for(int i = 1; i < order.length; ++i) {
				if(!order[i].hasNoID()) {
					ItemStack stack = new ItemStack(this, 1, order[i].getID());
					setType(stack, order[i]);
					list.add(stack);
				}
			}
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String s = ("" + I18nUtil.resolveKey(this.getTranslationKey() + ".name")).trim();
		FluidType type = getType(stack);
		if(type != Fluids.NONE) {
			s = s + " - " + type.getLocalizedName();
		}
		return s;
	}

	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister reg) {
		String base = com.hbm.render.item.NativeItemTextureRegistry.texture(this);
		if(base == null) base = "hbm:fluid_identifier";
		this.itemIcon = reg.registerIcon(base);
		this.overlayIcon = reg.registerIcon("hbm:fluid_identifier_overlay");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(ItemStack stack, int pass) {
		return pass == 1 ? this.overlayIcon : this.itemIcon;
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamageForRenderPass(int meta, int pass) {
		return pass == 1 ? this.overlayIcon : this.itemIcon;
	}

	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int pass) {
		if(pass == 0) {
			return 0xFFFFFF;
		} else {
			FluidType type = getType(stack);
			int color = type.getColor();
			if(color < 0) color = 0xFFFFFF;
			return color;
		}
	}

	@Override
	public FluidType getType(World world, int x, int y, int z, ItemStack stack) {
		return getType(stack);
	}

	public static FluidType getType(ItemStack stack) {
		if(stack == null || stack.isEmpty()) return Fluids.NONE;
		if(stack.hasTagCompound() && stack.getTagCompound().hasKey("fluid")) {
			return Fluids.fromID(stack.getTagCompound().getInteger("fluid"));
		}
		if(stack.hasTagCompound() && stack.getTagCompound().hasKey("fluid1")) {
			return Fluids.fromID(stack.getTagCompound().getInteger("fluid1"));
		}
		if(stack.getItemDamage() > 0) {
			return Fluids.fromID(stack.getItemDamage());
		}
		return Fluids.NONE;
	}

	public static void setType(ItemStack stack, FluidType type) {
		if(stack == null || stack.isEmpty()) return;
		if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setInteger("fluid", type.getID());
		stack.setItemDamage(type.getID());
	}
}
