package com.hbm.blocks.network;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.network.TileEntityFluidCounterValve;
import com.hbm.util.i18n.I18nUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

import java.util.ArrayList;
import java.util.List;

public class FluidCounterValve extends FluidDuctBase implements ILookOverlay, ITooltipProvider {

	@SideOnly(Side.CLIENT)
	private IIcon iconOn;

	public FluidCounterValve(Material mat) {
		super(mat);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.iconOn = iconRegister.registerIcon(RefStrings.MODID + ":fluid_counter_valve_on");
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":fluid_counter_valve_off");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return metadata == 1 ? iconOn : blockIcon;
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityFluidCounterValve();
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(!(te instanceof TileEntityFluidCounterValve))
			return;

		TileEntityFluidCounterValve duct = (TileEntityFluidCounterValve) te;

		List<String> text = new ArrayList<>();
		text.add("&[" + duct.getType().getColor() + "&]" + duct.getType().getLocalizedName());
		text.add("Counter: " + duct.getCounter());
		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		this.addStandardInfo(stack, player, list, ext);
	}
}
