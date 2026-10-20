package com.hbm.items;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import com.hbm.render.icon.IIcon;
import com.hbm.util.i18n.I18nUtil;

public class BrokenItem extends Item {

	@Override
	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@Override
	public int getRenderPasses(int meta) {
		return 2;
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(ItemStack stack, int pass) {
		if(pass == 1) return this.itemIcon;
		if(stack.getTagCompound() == null) return this.itemIcon;

		String id = stack.getTagCompound().getString("itemID");
		int meta = stack.getTagCompound().getInteger("itemMeta");
		
		Item item = (Item) Item.itemRegistry.getObject(id);
		if(item == null) return this.itemIcon;
		
		return item.getIconFromDamage(meta);
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		if(stack.getTagCompound() == null) return super.getItemStackDisplayName(stack);

		String id = stack.getTagCompound().getString("itemID");
		int meta = stack.getTagCompound().getInteger("itemMeta");
		
		Item item = (Item) Item.itemRegistry.getObject(id);
		if(item == null) return super.getItemStackDisplayName(stack);
		
		ItemStack sta = new ItemStack(item, 1, meta);
		return I18nUtil.format(this.getUnlocalizedNameInefficiently(stack) + ".prefix", sta.getDisplayName());
	}
	
	public static ItemStack make(ItemStack stack) {		return make(stack.getItem(), stack.stackSize, stack.getItemDamage()); }
	public static ItemStack make(Item item) {			return make(item, 1, 0); }
	public static ItemStack make(Item item, int meta) {	return make(item, 1, meta); }
	
	public static ItemStack make(Item item, int stacksize, int meta) {
		ItemStack stack = new ItemStack(ModItems.broken_item, stacksize);
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("itemID", java.util.Objects.toString(Item.REGISTRY.getNameForObject(item), null));
		nbt.setInteger("itemMeta", meta);
		stack.setTagCompound(nbt);
		return stack;
	}
}
