package com.hbm.items.tool;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.fluidmk2.IFillableItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;

import java.util.List;

public class ItemPipette extends Item implements IFillableItem {

	public ItemPipette() {
		this.canRepair = false;
		this.setMaxDamage(1);
	}

	@SideOnly(Side.CLIENT) protected IIcon overlayIcon;
	@SideOnly(Side.CLIENT) protected IIcon emptyIcon;
	@SideOnly(Side.CLIENT) protected IIcon itemIcon;

	public short getMaxFill() {
		if(this == ModItems.pipette_laboratory) return 50;
		else return 1_000;
	}

	public void initNBT(ItemStack stack) {
		stack.setTagCompound(new NBTTagCompound());
		this.setFill(stack, Fluids.NONE, (short) 0); // sets "type" and "fill" NBT
		stack.getTagCompound().setShort("capacity", this.getMaxFill()); // set "capacity"
	}

	public FluidType getType(ItemStack stack) {
		if(!stack.hasTagCompound()) {
			initNBT(stack);
		}

		return Fluids.fromID(stack.getTagCompound().getShort("type"));
	}

	public short getCapacity(ItemStack stack) {
		if(!stack.hasTagCompound()) {
			initNBT(stack);
		}

		return stack.getTagCompound().getShort("capacity");
	}

	public void setFill(ItemStack stack, FluidType type, short fill) {
		if(!stack.hasTagCompound()) {
			initNBT(stack);
		}

		stack.getTagCompound().setShort("type", (short) type.getID());
		stack.getTagCompound().setShort("fill", fill);
	}

	@Override
	public int getFill(ItemStack stack) {
		if(!stack.hasTagCompound()) {
			initNBT(stack);
		}

		return stack.getTagCompound().getShort("fill");
	}

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand nativeHand) {
		ItemStack stack = player.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(stack, world, player));
	}

	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(!stack.hasTagCompound()) {
			initNBT(stack);
		}

		if(!world.isRemote) {
			if(this.getFill(stack) == 0) {
				int a;
				if(this == ModItems.pipette_laboratory)
					a = !player.isSneaking() ? Math.min(this.getCapacity(stack) + 1, 50) : Math.max(this.getCapacity(stack) - 1, 1);
				else
					a = !player.isSneaking() ? Math.min(this.getCapacity(stack) + 50, 1_000) : Math.max(this.getCapacity(stack) - 50, 50);
				stack.getTagCompound().setShort("capacity", (short) a);
				player.sendMessage(new TextComponentString(a + "/" + this.getMaxFill() + "mB"));
			} else {
				player.sendMessage(new TextComponentTranslation("desc.item.pipette.noEmpty"));
			}
		}
		return stack;
	}

	@Override
	public void addInformation(ItemStack stack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {
		if(this == ModItems.pipette_laboratory) {
			list.add(I18nUtil.resolveKey("desc.item.pipette.corrosive"));
			list.add(I18nUtil.resolveKey("desc.item.pipette.laboratory"));
		}
		if(this == ModItems.pipette_boron)
			list.add(I18nUtil.resolveKey("desc.item.pipette.corrosive"));
		if(this == ModItems.pipette)
			list.add(I18nUtil.resolveKey("desc.item.pipette.noCorrosive"));
		list.add("Fluid: " + this.getType(stack).getLocalizedName());
		list.add("Amount: " + this.getFill(stack) + "/" + this.getCapacity(stack) + "mB (" + this.getMaxFill() + "mB)");
	}

	@Override
	public boolean acceptsFluid(FluidType type, ItemStack stack) {
		return (type == this.getType(stack) || this.getFill(stack) == 0) && (!type.isAntimatter());
	}

	@Override
	public int tryFill(FluidType type, int amount, ItemStack stack) {

		if(!acceptsFluid(type, stack))
			return amount;

		if(this.getFill(stack) == 0)
			this.setFill(stack, type, (short) 0);

		int req = this.getCapacity(stack) - this.getFill(stack);
		int toFill = Math.min(req, amount);

		this.setFill(stack, type, (short) (this.getFill(stack) + toFill));

		// fizzling checks
		if(this.getFill(stack) > 0 && willFizzle(type)) {
			stack.setCount(0);
		}

		return amount - toFill;
	}

	public boolean willFizzle(FluidType type) {
		if (this != ModItems.pipette) return false;
		return type.isCorrosive() && type != Fluids.PEROXIDE;
	}

	@Override
	public boolean providesFluid(FluidType type, ItemStack stack) {
		return this.getType(stack) == type;
	}

	@Override
	public int tryEmpty(FluidType type, int amount, ItemStack stack) {
		if(providesFluid(type, stack)) {
			int toUnload = Math.min(amount, this.getFill(stack));
			this.setFill(stack, type, (short) (this.getFill(stack) - toUnload));
			if(this.getFill(stack) == 0)
				this.setFill(stack, Fluids.NONE, (short) 0);
			return toUnload;
		}
		return amount;
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister icon) {
		this.itemIcon = icon.registerIcon(this.getRegistryName().toString());
		if(this == ModItems.pipette_laboratory)
			this.overlayIcon = icon.registerIcon("hbm:pipette_laboratory_overlay");
		else
			this.overlayIcon = icon.registerIcon("hbm:pipette_overlay");

		this.emptyIcon = icon.registerIcon("hbm:pipette_empty");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(ItemStack stack, int pass) {
		if(getFill(stack) == 0 && pass == 1) return this.emptyIcon;
		return pass == 1 ? this.overlayIcon : this.itemIcon;
	}

	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int pass) {
		if(pass == 0) {
			return 0xffffff;
		} else {
			int j = this.getType(stack).getColor();

			if(j < 0) {
				j = 0xffffff;
			}

			return j;
		}
	}

	@Override
	public FluidType getFirstFluidType(ItemStack stack) {
		return this.getType(stack);
	}
}
