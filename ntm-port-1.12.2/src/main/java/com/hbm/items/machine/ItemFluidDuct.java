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

	IIcon overlayIcon;

	public ItemFluidDuct() {
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}

	@Override
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
	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister p_94581_1_) {
		super.registerIcons(p_94581_1_);
		this.overlayIcon = p_94581_1_.registerIcon("hbm:duct_overlay");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamageForRenderPass(int p_77618_1_, int p_77618_2_) {
		return p_77618_2_ == 1 ? this.overlayIcon : super.getIconFromDamageForRenderPass(p_77618_1_, p_77618_2_);
	}

	@Override
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

	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int i, float f0, float f1, float f2) {
		if(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() != Blocks.snow_layer) {
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

			if(!com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock().isReplaceable(world, x, y, z)) {
				return false;
			}
		}

		if(!player.canPlayerEdit(x, y, z, i, stack)) {
			return false;
		} else {
			--stack.stackSize;
			world.setBlock(x, y, z, ModBlocks.fluid_duct_neo);

			if(world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z)) instanceof TileEntityPipeBaseNT) {
				((TileEntityPipeBaseNT) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z))).setType(Fluids.fromID(stack.getItemDamage()));
			}
			
			world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, ModBlocks.fluid_duct_neo.stepSound.func_150496_b(), (ModBlocks.fluid_duct_neo.stepSound.getVolume() + 1.0F) / 2.0F, ModBlocks.fluid_duct_neo.stepSound.getPitch() * 0.8F);

			return true;
		}
	}

}
