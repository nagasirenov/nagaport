package com.hbm.blocks.network;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.IPersistentInfoProvider;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.storage.TileEntityBatteryREDD;
import com.hbm.util.BobMathUtil;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class MachineBatteryREDD extends BlockDummyable implements IPersistentInfoProvider {

	public MachineBatteryREDD() {
		super(Material.IRON);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityBatteryREDD();
		if(meta >= 6) return new TileEntityProxyCombo().power().conductor();
		return null;
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(); int y = nativePos.getY(); int z = nativePos.getZ(); int side = nativeFacing.getIndex();
		return standardOpenBehavior(world, x, y, z, player, side);
	}

	@Override public int[] getDimensions() { return new int[] {9, 0, 2, 2, 4, 4}; }
	@Override public int getOffset() { return 2; }

	@Override
	protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);

		x += dir.offsetX * o;
		z += dir.offsetZ * o;

		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		this.makeExtra(world, x + dir.offsetX * 2 + rot.offsetX * 2, y, z + dir.offsetZ * 2 + rot.offsetZ * 2);
		this.makeExtra(world, x + dir.offsetX * 2 - rot.offsetX * 2, y, z + dir.offsetZ * 2 - rot.offsetZ * 2);
		this.makeExtra(world, x - dir.offsetX * 2 + rot.offsetX * 2, y, z - dir.offsetZ * 2 + rot.offsetZ * 2);
		this.makeExtra(world, x - dir.offsetX * 2 - rot.offsetX * 2, y, z - dir.offsetZ * 2 - rot.offsetZ * 2);
		this.makeExtra(world, x + rot.offsetX * 4, y, z + rot.offsetZ * 4);
		this.makeExtra(world, x - rot.offsetX * 4, y, z - rot.offsetZ * 4);
	}


		@Override
	public void getDrops(net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> drops, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, int fortune) {
		int meta = state.getBlock().getMetaFromState(state);
		net.minecraft.world.World w = world instanceof net.minecraft.world.World ? (net.minecraft.world.World) world : null;
		java.util.ArrayList<net.minecraft.item.ItemStack> ret = getDrops(w, pos.getX(), pos.getY(), pos.getZ(), meta, fortune);
		if(ret != null) drops.addAll(ret);
	}

public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		return IPersistentNBT.getDrops(world, x, y, z, this);
	}

	@Override
	public void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player) {

		if(!player.capabilities.isCreativeMode) {
			harvesters.set(player);
			this.dropBlockAsItem(world, new net.minecraft.util.math.BlockPos(x, y, z), com.hbm.world.WorldBlockState.read(world, x, y, z), 0);
			harvesters.set(null);
		}
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, net.minecraft.tileentity.TileEntity te, net.minecraft.item.ItemStack tool) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ(); int meta = state.getBlock().getMetaFromState(state);
		player.addStat(net.minecraft.stats.StatList.getBlockStats(this), 1);
		player.addExhaustion(0.025F);
	}

	@Override
	public void addInformation(ItemStack stack, NBTTagCompound persistentTag, EntityPlayer player, List list, boolean ext) {
		if(persistentTag != null && persistentTag.hasKey("power"))
			list.add(TextFormatting.YELLOW + "" + BobMathUtil.format(new BigInteger(persistentTag.getByteArray("power"))) + " HE");
	}
}
