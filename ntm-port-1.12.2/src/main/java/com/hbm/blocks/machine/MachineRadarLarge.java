package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineRadarLarge;
import com.hbm.tileentity.machine.TileEntityMachineRadarNT;

import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class MachineRadarLarge extends BlockDummyable {

	public MachineRadarLarge(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityMachineRadarLarge();
		if(meta >= 6) return new TileEntityProxyCombo().power();
		return null;
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;


		if(y < TileEntityMachineRadarNT.radarAltitude) {
			if(world.isRemote)
				player.sendMessage(new TextComponentString("[Radar] Error: Radar altitude not sufficient.").setStyle(new Style().setColor(TextFormatting.RED)));
			return true;
		}

		if(world.isRemote && !player.isSneaking()) {
			int[] pos = this.findCore(world, x, y, z);
			if(pos == null) return false;
			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos[0], pos[1], pos[2]);
			return true;
		} else if(!player.isSneaking()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int[] getDimensions() {
		return new int[] {4, 0, 1, 1, 1, 1};
	}

	@Override
	public int getOffset() {
		return 1;
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);
		x += dir.offsetX * o;
		z += dir.offsetZ * o;
		this.makeExtra(world, x + 1, y, z);
		this.makeExtra(world, x - 1, y, z);
		this.makeExtra(world, x, y, z + 1);
		this.makeExtra(world, x, y, z - 1);
	}

	@Override
	public boolean canProvidePower(net.minecraft.block.state.IBlockState state) {
		return true;
	}

	@Override
	public int getWeakPower(net.minecraft.block.state.IBlockState state, IBlockAccess world, net.minecraft.util.math.BlockPos blockPos, net.minecraft.util.EnumFacing side) {
		int x = blockPos.getX();
		int y = blockPos.getY();
		int z = blockPos.getZ();
		int m = side.getIndex();
		int meta = com.hbm.world.WorldBlockState.metadata(world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)));
		if(meta >= 6) {
			ForgeDirection dir = ForgeDirection.getOrientation(m);
			TileEntity tile = world.getTileEntity(new net.minecraft.util.math.BlockPos(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ));
			if(tile instanceof TileEntityMachineRadarNT) {
				TileEntityMachineRadarNT entity = (TileEntityMachineRadarNT) tile;
				return entity.getRedPower();
			}
		}
		return 0;
	}

	@Override
	public int getStrongPower(net.minecraft.block.state.IBlockState state, IBlockAccess world, net.minecraft.util.math.BlockPos blockPos, net.minecraft.util.EnumFacing side) {
		return getWeakPower(state, world, blockPos, side);
	}
}
