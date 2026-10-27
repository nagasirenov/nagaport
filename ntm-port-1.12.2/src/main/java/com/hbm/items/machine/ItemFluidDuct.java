package com.hbm.items.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.tileentity.network.TileEntityPipeBaseNT;
import com.hbm.util.i18n.I18nUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;

import java.util.List;

public class ItemFluidDuct extends Item {

	protected IIcon itemIcon;
	protected IIcon overlayIcon;

	public ItemFluidDuct() {
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}

	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister reg) {
		String base = com.hbm.render.item.NativeItemTextureRegistry.texture(this);
		if(base == null) base = "hbm:duct";
		this.itemIcon = reg.registerIcon(base);
		this.overlayIcon = reg.registerIcon("hbm:duct_overlay");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(ItemStack stack, int pass) {
		return pass == 1 ? this.overlayIcon : this.itemIcon;
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamageForRenderPass(int p_77618_1_, int p_77618_2_) {
		return p_77618_2_ == 1 ? this.overlayIcon : this.itemIcon;
	}

	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int p_82790_2_) {
		if(p_82790_2_ == 0) {
			return 16777215;
		} else {
			int j = Fluids.fromID(stack.getItemDamage()).getColor();

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
				list.add(new ItemStack(item, 1, order[i].getID()));
			}
		}
	}

	public String getItemStackDisplayName(ItemStack stack) {
		String s = ("" + I18nUtil.resolveKey(this.getTranslationKey() + ".name")).trim();
		String s1 = ("" + I18nUtil.resolveKey(Fluids.fromID(stack.getItemDamage()).getConditionalName())).trim();

		if(s1 != null) {
			s = s + " " + s1;
		}

		return s;
	}



		@Override
	public net.minecraft.util.EnumActionResult onItemUse(net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
		net.minecraft.item.ItemStack stack = player.getHeldItem(hand);
		int side = facing != null ? facing.getIndex() : 0;
		return onItemUse(stack, player, world, pos.getX(), pos.getY(), pos.getZ(), side, hitX, hitY, hitZ) ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
	}

public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int i, float f0, float f1, float f2) {
		if(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() != Blocks.SNOW_LAYER) {
			if(i == 0) {
				--y;
			}

			if(i == 1) {
				++y;
			}

			if(i == 2) {
				--z;
			}

			if(i == 3) {
				++z;
			}

			if(i == 4) {
				--x;
			}

			if(i == 5) {
				++x;
			}

			if(!com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x, y, z))) {
				return false;
			}
		}

		if(!player.canPlayerEdit(new net.minecraft.util.math.BlockPos(x, y, z), net.minecraft.util.EnumFacing.byIndex(i), stack)) {
			return false;
		} else {
			stack.shrink(1);
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, ModBlocks.fluid_duct_neo);

			if(world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z)) instanceof TileEntityPipeBaseNT) {
				((TileEntityPipeBaseNT) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z))).setType(Fluids.fromID(stack.getItemDamage()));
			}

			net.minecraft.block.SoundType soundType = ModBlocks.fluid_duct_neo.getSoundType();
			world.playSound(null, x + 0.5, y + 0.5, z + 0.5, soundType.getPlaceSound(), net.minecraft.util.SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);

			return true;
		}
	}

}
