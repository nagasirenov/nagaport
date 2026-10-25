package com.hbm.items.machine;

import java.util.List;
import java.util.Map.Entry;

import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.items.ModItems;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;

public class ItemBlueprints extends Item {




		@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public void getSubItems(net.minecraft.creativetab.CreativeTabs tab, net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> list) {
		if(this.isInCreativeTab(tab)) {
			getSubItems(this, tab, list);
		}
	}

@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		for(Entry<String, List<String>> pool : GenericRecipes.blueprintPools.entrySet()) {
			String poolName = pool.getKey();
			if(!poolName.startsWith(GenericRecipes.POOL_PREFIX_SECRET)) list.add(make(poolName));
		}
	}

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand nativeHand) {
		ItemStack stack = player.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(stack, world, player));
	}


	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if(world.isRemote) return stack;
		if(!stack.hasTagCompound()) return stack;

		String poolName = stack.getTagCompound().getString("pool");

		if(poolName.startsWith(GenericRecipes.POOL_PREFIX_SECRET)) return stack;
		if(!player.inventory.hasItemStack(new ItemStack(Items.PAPER))) return stack;

		player.inventory.clearMatchingItems(Items.PAPER, -1, 1, null);
		player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);

		ItemStack copy = stack.copy();
		copy.setCount(1);

		if(!player.capabilities.isCreativeMode) {
			if(stack.getCount() < stack.getMaxStackSize()) {
				stack.grow(1);
				return stack;
			}

			if(!player.inventory.addItemStackToInventory(copy)) {
				copy = stack.copy();
				copy.setCount(1);
				player.dropItem(copy, false);
			}

			player.inventoryContainer.detectAndSendChanges();
		} else {
			player.dropItem(copy, false);
		}

		return stack;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {

		if(!stack.hasTagCompound()) {
			return;
		}

		String poolName = stack.getTagCompound().getString("pool");
		List<String> pool = GenericRecipes.blueprintPools.get(poolName);

		if(pool == null || pool.isEmpty()) {
			return;
		}
		if(poolName.startsWith(GenericRecipes.POOL_PREFIX_SECRET)) {
			list.add(TextFormatting.RED + "Cannot be copied!");
		} else {
			list.add(TextFormatting.YELLOW + "Right-click to copy (requires paper)");
		}

		for(String name : pool) {
			GenericRecipe recipe = GenericRecipes.pooledBlueprints.get(name);
			if(recipe != null) {
				list.add(recipe.getLocalizedName());
			}
		}
	}

	public static String grabPool(ItemStack stack) {
		if(stack == null) return null;
		if(stack.getItem() != ModItems.blueprints) return null;
		if(!stack.hasTagCompound()) return null;
		if(!stack.getTagCompound().hasKey("pool")) return null;
		return stack.getTagCompound().getString("pool");
	}

	public static ItemStack make(String pool) {
		ItemStack stack = new ItemStack(ModItems.blueprints);
		stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setString("pool", pool);
		return stack;
	}
}
