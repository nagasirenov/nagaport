package com.hbm.blocks.generic;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ILookOverlay;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.dim.trait.CBT_Atmosphere.FluidEntry;
import com.hbm.handler.atmosphere.IBlockSealable;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.trait.FT_Gaseous;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityAirPump;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

public class BlockAirPump extends BlockContainer implements ILookOverlay, IBlockSealable {

	@SideOnly(Side.CLIENT)
	private IIcon iconTop;

	public BlockAirPump(Material p_i45386_1_) {
		super(p_i45386_1_);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {

		this.iconTop = iconRegister.registerIcon(RefStrings.MODID + ":vent_chlorine_seal_top");
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":vent_chlorine_seal_side");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {

		return side == 1 ? this.iconTop : this.blockIcon;
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityAirPump();
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {

		TileEntity tile = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(!(tile instanceof TileEntityAirPump)) return;

		TileEntityAirPump pump = (TileEntityAirPump) tile;

		CBT_Atmosphere atmosphere = pump.currentAtmosphere;

		List<String> text = new ArrayList<>();

		text.add(I18nUtil.resolveKey("hbmfluid." + pump.tank.getTankType().getName().toLowerCase()) + ": " + pump.tank.getFill() + "/" + pump.tank.getMaxFill() + "mB");

		if(pump.tank.getFill() <= 10) {
			text.add("&[" + (BobMathUtil.getBlink() ? 0xff0000 : 0xffff00) + "&]! ! ! " + I18nUtil.resolveKey("atmosphere.noTank") + " ! ! !");
		} else if(!pump.hasSeal()) {
			text.add("&[" + (BobMathUtil.getBlink() ? 0xff0000 : 0xffff00) + "&]! ! ! " + I18nUtil.resolveKey("atmosphere.noSeal") + " ! ! !");
		}

		text.add(I18nUtil.resolveKey("atmosphere.name") + ": ");

		boolean hasPressure = false;

		if(atmosphere != null) {
			for(FluidEntry entry : atmosphere.fluids) {
				if(entry.pressure > 0.01) {
					double pressure = BobMathUtil.roundDecimal(entry.pressure, 3);
					text.add(TextFormatting.AQUA + " - " + I18nUtil.resolveKey("hbmfluid." + entry.fluid.getName().toLowerCase()) + " - " + pressure + "atm");
					hasPressure = true;
				}
			}
		}

		if(!hasPressure) {
			text.add(TextFormatting.AQUA + " - " + I18nUtil.resolveKey("atmosphere.vacuum"));
		}

		if(pump.isRecycling()) {
			text.add(TextFormatting.GREEN + I18nUtil.resolveKey("atmosphere.recycling"));
		}

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if(world.isRemote) {
			return true;
		}

		TileEntity tile = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(!(tile instanceof TileEntityAirPump)) return false;

		TileEntityAirPump pump = (TileEntityAirPump) tile;

		if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null && com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() instanceof IItemFluidIdentifier) {
			FluidType type = ((IItemFluidIdentifier) com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem()).getType(world, x, y, z, com.hbm.util.NativeHeldItem.getMainHandNullable(player));
			if(type.hasTrait(FT_Gaseous.class)) {
				pump.tank.setTankType(type);
				pump.markDirty();
				player.sendMessage(new TextComponentString("Changed type to ").setStyle(new Style().setColor(TextFormatting.YELLOW)).appendSibling(new TextComponentTranslation(type.getConditionalName())).appendSibling(new TextComponentString("!")));
			}
		}

		return true;
	}

	@Override
	public boolean isSealed(World world, int x, int y, int z) {
		return false;
	}
}

