package com.hbm.blocks.bomb;

import org.apache.logging.log4j.Level;

import com.hbm.config.GeneralConfig;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.bomb.TileEntityFireworks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;

public class BlockFireworks extends BlockContainer {

	protected IIcon blockIcon;

	@SideOnly(Side.CLIENT)
	private IIcon iconTop;
	@SideOnly(Side.CLIENT)
	private IIcon iconBottom;

	public BlockFireworks(Material mat) {
		super(mat);
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.iconTop = iconRegister.registerIcon(RefStrings.MODID + ":fireworks_top");
		this.iconBottom = iconRegister.registerIcon(RefStrings.MODID + ":fireworks_bottom");
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":fireworks_side");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return side == 1 ? this.iconTop : (side == 0 ? this.iconBottom : this.blockIcon);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityFireworks();
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float fx, float fy, float fz) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int i = nativeFacing != null ? nativeFacing.getIndex() : 0;


		if(world.isRemote)
			return true;

		TileEntityFireworks te = (TileEntityFireworks)world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null) {

			if(com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() == Items.GUNPOWDER) {
				te.charges += com.hbm.util.NativeHeldItem.getMainHandNullable(player).getCount() * 3;
				te.markDirty();
				com.hbm.util.NativeHeldItem.getMainHandNullable(player).setCount(0);
				return true;
			}

			if(com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() == ModItems.sulfur) {
				te.charges += com.hbm.util.NativeHeldItem.getMainHandNullable(player).getCount();
				te.markDirty();
				com.hbm.util.NativeHeldItem.getMainHandNullable(player).setCount(0);
				return true;
			}

			if(com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() instanceof ItemDye) {
				te.color = ItemDye.DYE_COLORS[com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItemDamage()];
				te.markDirty();
				com.hbm.util.NativeHeldItem.getMainHandNullable(player).shrink(1);
				return true;
			}

			if(com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() == Items.NAME_TAG) {
				te.message = com.hbm.util.NativeHeldItem.getMainHandNullable(player).getDisplayName();
				te.markDirty();
				com.hbm.util.NativeHeldItem.getMainHandNullable(player).shrink(1);
				return true;
			}
		}

		player.sendMessage(new TextComponentTranslation(this.getTranslationKey() + ".name").setStyle(new Style().setColor(TextFormatting.GOLD)));
		player.sendMessage(new TextComponentTranslation(this.getTranslationKey() + ".charges", te.charges).setStyle(new Style().setColor(TextFormatting.YELLOW)));
		player.sendMessage(new TextComponentTranslation(this.getTranslationKey() + ".color", Integer.toHexString(te.color)).setStyle(new Style().setColor(TextFormatting.YELLOW)));
		player.sendMessage(new TextComponentTranslation(this.getTranslationKey() + ".message", te.message).setStyle(new Style().setColor(TextFormatting.YELLOW)));

		return true;
	}
	@Override
	public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos nativePlacementPos, net.minecraft.block.state.IBlockState nativePlacementState, EntityLivingBase player, ItemStack itemStack) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
	if(!world.isRemote) {
			if(GeneralConfig.enableExtendedLogging) {
			MainRegistry.logger.log(Level.INFO, "[BOMBPL]" + this.getLocalizedName() + " placed at " + x + " / " + y + " / " + z + "! " + "by "+ player.getName());
		}
	}
	}

}
