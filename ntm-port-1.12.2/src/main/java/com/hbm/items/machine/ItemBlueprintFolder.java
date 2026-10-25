package com.hbm.items.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.recipes.loader.GenericRecipes;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;

public class ItemBlueprintFolder extends Item {

	protected IIcon itemIcon;

	@SideOnly(Side.CLIENT) protected IIcon iconDiscover;
	@SideOnly(Side.CLIENT) protected IIcon iconSecret;

	public ItemBlueprintFolder() {
		this.setHasSubtypes(true);
		this.setMaxStackSize(1);
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister reg) {
		this.itemIcon = reg.registerIcon(this.getRegistryName().toString());
		this.iconDiscover = reg.registerIcon(this.getRegistryName().toString() + "_discover");
		this.iconSecret = reg.registerIcon(this.getRegistryName().toString() + "_secret");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int meta) {
		return meta == 1 ? iconDiscover : meta == 2 ? iconSecret : itemIcon;
	}


		@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public void getSubItems(net.minecraft.creativetab.CreativeTabs tab, net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> list) {
		if(this.isInCreativeTab(tab)) {
			getSubItems(this, tab, list);
		}
	}

@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		for(int i = 0; i < 2; i++) list.add(new ItemStack(item, 1, i));
	}

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand nativeHand) {
		ItemStack stack = player.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(stack, world, player));
	}


	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if(world.isRemote) return stack;

		List<String> pools = new ArrayList();

		for(String pool : GenericRecipes.blueprintPools.keySet()) {
			if(stack.getItemDamage() == 0 && pool.startsWith(GenericRecipes.POOL_PREFIX_ALT)) pools.add(pool);
			if(stack.getItemDamage() == 1 && pool.startsWith(GenericRecipes.POOL_PREFIX_DISCOVER)) pools.add(pool);
			if(stack.getItemDamage() == 2 && pool.startsWith(GenericRecipes.POOL_PREFIX_SECRET)) pools.add(pool);
		}

		if(!pools.isEmpty()) {
			stack.shrink(1);

			String chosen = pools.get(player.getRNG().nextInt(pools.size()));
			ItemStack blueprint = ItemBlueprints.make(chosen);

			if(!player.inventory.addItemStackToInventory(blueprint))
				player.dropItem(blueprint, false);
		}

		return stack;
	}
}
