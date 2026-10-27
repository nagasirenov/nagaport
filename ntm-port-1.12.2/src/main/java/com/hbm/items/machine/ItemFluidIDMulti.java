package com.hbm.items.machine;

import java.util.List;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.gui.GUIScreenFluid;
import com.hbm.items.IItemControlReceiver;
import com.hbm.main.MainRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.PlayerInformPacket;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.ChatBuilder;
import com.hbm.util.i18n.I18nUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;

public class ItemFluidIDMulti extends ItemFluidIdentifier implements IItemControlReceiver, IGUIProvider {

	@Override
	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister reg) {
		String base = com.hbm.render.item.NativeItemTextureRegistry.texture(this);
		if(base == null) base = "hbm:fluid_identifier_multi";
		this.itemIcon = reg.registerIcon(base);
		this.overlayIcon = reg.registerIcon("hbm:fluid_identifier_overlay");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(ItemStack stack, int pass) {
		return pass == 1 ? this.overlayIcon : this.itemIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamageForRenderPass(int meta, int pass) {
		return pass == 1 ? this.overlayIcon : this.itemIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int pass) {
		if(pass == 0) {
			return 16777215;
		} else {
			FluidType primary = getType(stack, true);
			if(primary == Fluids.NONE && stack.getItemDamage() > 0) {
				primary = Fluids.fromID(stack.getItemDamage());
			}
			int j = primary.getColor();

			if(j < 0) {
				j = 16777215;
			}

			return j;
		}
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
			if(!order[i].hasNoID()) {
				ItemStack id = new ItemStack(item, 1, order[i].getID());
				setType(id, order[i], true);
				list.add(id);
			}
		}
	}

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand nativeHand) {
		ItemStack stack = player.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(stack, world, player));
	}


	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(!world.isRemote && !player.isSneaking()) {
			FluidType primary = getType(stack, true);
			FluidType secondary = getType(stack, false);
			setType(stack, secondary, true);
			setType(stack, primary, false);
			updateMeta(stack);
			world.playSound(null, player.posX, player.posY, player.posZ, net.minecraft.init.SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, net.minecraft.util.SoundCategory.PLAYERS, 0.25F, 1.25F);
			PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(ChatBuilder.startTranslation(secondary.getConditionalName()).flush(), /*MainRegistry.proxy.ID_DETONATOR*/ 7, 3000), (EntityPlayerMP) player);
		}

		if(world.isRemote && player.isSneaking()) {
			player.openGui(MainRegistry.instance, 0, world, 0, 0, 0);
		}

		return stack;
	}

	@Override
	public void receiveControl(ItemStack stack, NBTTagCompound data) {
		if(data.hasKey("primary")) {
			setType(stack, Fluids.fromID(data.getInteger("primary")), true);
		}
		if(data.hasKey("secondary")) {
			setType(stack, Fluids.fromID(data.getInteger("secondary")), false);
		}

		updateMeta(stack);
	}

	public static void updateMeta(ItemStack stack) {
		if(stack.hasTagCompound()) stack.setItemDamage(stack.getTagCompound().getInteger("fluid1"));
	}

	@Override
	public void addInformation(ItemStack stack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {
		list.add(I18nUtil.resolveKey(getTranslationKey() + ".info"));
		list.add("   " + getType(stack, true).getLocalizedName());
		list.add(I18nUtil.resolveKey(getTranslationKey() + ".info2"));
		list.add("   " + getType(stack, false).getLocalizedName());
	}

	@Override
	public ItemStack getContainerItem(ItemStack stack) {
		return stack.copy();
	}

	@Override
	public boolean hasContainerItem(ItemStack stack) {
		return true;
	}

	public boolean doesContainerItemLeaveCraftingGrid(ItemStack stack) {
		return false;
	}

	@Override
	public FluidType getType(World world, int x, int y, int z, ItemStack stack) {
		return getType(stack, true);
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos, EntityPlayer player) {
		return true;
	}

	public static void setType(ItemStack stack, FluidType type, boolean primary) {
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setInteger("fluid" + (primary ? 1 : 2), type.getID());
	}

	public static FluidType getType(ItemStack stack, boolean primary) {
		if(!stack.hasTagCompound())
			return Fluids.NONE;

		int type = stack.getTagCompound().getInteger("fluid" + (primary ? 1 : 2));
		return Fluids.fromID(type);
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIScreenFluid(player);
	}
}
