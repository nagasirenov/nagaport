package com.hbm.blocks.machine;

import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityMachineRadarNT;

import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class MachineRadar extends BlockContainer {

	public MachineRadar(Material p_i45386_1_) {
		super(p_i45386_1_);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityMachineRadarNT();
	}
	
	@Override
	public int getRenderType(){
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
		
		if(y < TileEntityMachineRadarNT.radarAltitude) {
			if(world.isRemote)
				player.sendMessage(new TextComponentString("[Radar] Error: Radar altitude not sufficient.").setStyle(new Style().setColor(TextFormatting.RED)));
			return true;
		}
		
		if(world.isRemote && !player.isSneaking()) {
			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, x, y, z);
			return true;
		} else if(!player.isSneaking()) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean canProvidePower() {
		return true;
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int m) {
		TileEntityMachineRadarNT entity = (TileEntityMachineRadarNT) world.getTileEntity(x, y, z);
		return entity.getRedPower();
	}

	@Override
	public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int m) {
		return isProvidingWeakPower(world, x, y, z, m);
	}
}
