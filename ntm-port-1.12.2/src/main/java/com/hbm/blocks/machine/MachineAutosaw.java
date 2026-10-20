package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.machine.TileEntityMachineAutosaw;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.block.IToolable;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

public class MachineAutosaw extends BlockContainer implements ILookOverlay, ITooltipProvider, IToolable {

	public MachineAutosaw() {
		super(Material.IRON);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityMachineAutosaw();
	}

	@Override
	public int getRenderType() {
		return -1;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		
		if(!world.isRemote && !player.isSneaking()) {
				
			if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null && com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() instanceof IItemFluidIdentifier) {
				
				TileEntityMachineAutosaw saw = (TileEntityMachineAutosaw) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
				
				FluidType type = ((IItemFluidIdentifier) com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem()).getType(world, x, y, z, com.hbm.util.NativeHeldItem.getMainHandNullable(player));
				if(saw.acceptedFuels.contains(type)) {
					saw.tank.setTankType(type);
					saw.markDirty();
					player.sendMessage(new TextComponentString("Changed type to ").setStyle(new Style().setColor(TextFormatting.YELLOW)).appendSibling(new TextComponentTranslation(type.getConditionalName())).appendSibling(new TextComponentString("!")));
					return true;
				}
			}
			
			return false;
		}
		
		return true;
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {
		if(tool != ToolType.SCREWDRIVER)
			return false;

		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(!(te instanceof TileEntityMachineAutosaw))
			return false;
		
		TileEntityMachineAutosaw saw = (TileEntityMachineAutosaw) te;

		saw.isSuspended = !saw.isSuspended;
		saw.markDirty();

		return true;
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		
		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
		
		if(!(te instanceof TileEntityMachineAutosaw))
			return;
		
		TileEntityMachineAutosaw saw = (TileEntityMachineAutosaw) te;
		
		List<String> text = new ArrayList();
		text.add(saw.tank.getTankType().getLocalizedName() + ": " + saw.tank.getFill() + "/" + saw.tank.getMaxFill() + "mB");

		if (saw.isSuspended) {
			text.add(TextFormatting.RED + "! " + I18nUtil.resolveKey(getTranslationKey() + ".suspended") + " !");
		}
		
		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		this.addStandardInfo(stack, player, list, ext);
	}
}
