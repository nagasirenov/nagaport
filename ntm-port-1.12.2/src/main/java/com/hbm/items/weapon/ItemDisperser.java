package com.hbm.items.weapon;

import com.hbm.entity.grenade.EntityDisperserCanister;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFluidTank;
import com.hbm.util.i18n.I18nUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

public class ItemDisperser extends ItemFluidTank {

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand nativeHand) {
		ItemStack stack = player.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(stack, world, player));
	}

	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(!player.capabilities.isCreativeMode) {
			stack.shrink(1);
		}

		world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("random.bow"), net.minecraft.util.SoundCategory.PLAYERS, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

		if(!world.isRemote) {

			EntityDisperserCanister canister = new EntityDisperserCanister(world, player);
			canister.setType(Item.getIdFromItem(this));
			canister.setFluid(stack.getItemDamage());
			world.spawnEntity(canister);
		}
		return stack;
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

		FluidType[] order = Fluids.getInNiceOrder();

		for(int i = 1; i < order.length; ++i) {
			FluidType type = order[i];
			int id = type.getID();
			if(type.isDispersable() && this == ModItems.disperser_canister) {
				list.add(new ItemStack(item, 1, id));
			} else if(type == Fluids.PHEROMONE || type == Fluids.SULFURIC_ACID && this == ModItems.glyphid_gland) {
				list.add(new ItemStack(item, 1, id));
			}

		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {

		String s = ("" + I18nUtil.resolveKey(this.getTranslationKey() + ".name")).trim();
		String s1 = ("" + I18nUtil.resolveKey(Fluids.fromID(stack.getItemDamage()).getConditionalName())).trim();

		s = this == ModItems.glyphid_gland ? s1 + " " + s : s + " " + s1;
		return s;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister p_94581_1_) {
		super.registerIcons(p_94581_1_);
		this.overlayIcon = this == ModItems.disperser_canister ? p_94581_1_.registerIcon("hbm:disperser_canister_overlay") : p_94581_1_.registerIcon("hbm:fluid_identifier_overlay");
	}
}
