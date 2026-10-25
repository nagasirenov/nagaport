package com.hbm.items.special;

import java.util.List;

import com.hbm.inventory.gui.GUIBookLore;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.i18n.I18nUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;

/*players can have a lil lore, as a treat.
 * nothing super complex like the guidebooks, just some NBT IDs, a bit of I18n and a centered textbox.
 * oh, and also different textures for both the book, the gui, and maybe the 'turn page' button based on what type of 'book' it is.
 * no metadata, i want it to be fairly flexible. probably like the assembly templates
 */
public class ItemBookLore extends Item implements IGUIProvider {

	public ItemBookLore() {
		this.setMaxStackSize(1);
	}

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand nativeHand) {
		ItemStack stack = player.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(stack, world, player));
	}

	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(world.isRemote)
			player.openGui(MainRegistry.instance, 0, world, 0, 0, 0);

		return stack;
	}

	@Override
	public void addInformation(ItemStack stack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {
		if(!stack.hasTagCompound()) return;
		String key = stack.getTagCompound().getString("k");
		if(key.isEmpty()) return;

		key = "book_lore." + key + ".author";
		String loc = I18nUtil.resolveKey(key);
		if(!loc.equals(key))
			list.add(I18nUtil.resolveKey("book_lore.author", loc));
	}

	@Override
	public String getTranslationKey(ItemStack stack) {
		if(!stack.hasTagCompound()) return "book_lore.test";
		String key = stack.getTagCompound().getString("k");

		return "book_lore." + (key.isEmpty() ? "test" : key);
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIBookLore(player);
	}

	public static ItemStack createBook(String key, int pages, int colorCov, int colorTit) {
		ItemStack book = new ItemStack(ModItems.book_lore);
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("k", key);
		tag.setShort("p", (short)pages);
		tag.setInteger("cov_col", colorCov);
		tag.setInteger("tit_col", colorTit);

		book.setTagCompound(tag);
		return book;
	}

	public static void addArgs(ItemStack book, int page, String... args) {
		if(!book.hasTagCompound()) return;
		NBTTagCompound data = new NBTTagCompound();
		for(int i = 0; i < args.length; i++) {
			data.setString("a" + (i + 1), args[i]);
		}

		book.getTagCompound().setTag("p" + page, data);
	}
}
