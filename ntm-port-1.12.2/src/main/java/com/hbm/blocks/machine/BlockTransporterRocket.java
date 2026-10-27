package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.items.ModItems;
import com.hbm.main.ChunkLoaderManager;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityTransporterBase;
import com.hbm.tileentity.machine.TileEntityTransporterRocket;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class BlockTransporterRocket extends BlockDummyable {

	public BlockTransporterRocket(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityTransporterRocket();
		if(meta >= 6) return new TileEntityProxyCombo().fluid();
		return null;
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;

		if(world.isRemote)
			return com.hbm.util.NativeHeldItem.getMainHandNullable(player) == null || com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() != ModItems.transporter_linker;

		if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) == null || com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() != ModItems.transporter_linker) {
			return standardOpenBehavior(world, x, y, z, player, 0);
		}

		return false;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack) {
		super.onBlockPlacedBy(world, x, y, z, player, itemStack);
		if(world.isRemote) return;

		// If we don't have a position, we failed to place, and should skip chunkloading
		int[] pos = findCore(world, x, y, z);
		if(pos != null) {
			ChunkLoaderManager.forceChunk(world, pos[0], pos[1], pos[2]);
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block b, int meta) {
		if(meta >= 12) {
			ChunkLoaderManager.unforceChunk(world, x, y, z);

			TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
			if(te != null && te instanceof TileEntityTransporterBase) {
				((TileEntityTransporterBase) te).unlinkTransporter();
			}
		}

		super.breakBlock(world, x, y, z, b, meta);
	}

	@Override
	public int[] getDimensions() {
		return new int[] {1, 0, 1, 1, 1, 2};
	}

	@Override
	public int getOffset() {
		return 1;
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		x += dir.offsetX * o;
		z += dir.offsetZ * o;

		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {0, 0, 1, 1, 1, 2}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {1, 0, 1, 1, 0, 0}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {1, 0, 1, 1, -2, 2}, this, dir);

		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);


		this.makeExtra(world, x + 1, y, z + 1);
		this.makeExtra(world, x + 1, y, z - 1);
		this.makeExtra(world, x - 1, y, z + 1);
		this.makeExtra(world, x - 1, y, z - 1);

		this.makeExtra(world, x + dir.offsetX - rot.offsetX * 2, y, z + dir.offsetZ - rot.offsetZ * 2);
		this.makeExtra(world, x - dir.offsetX - rot.offsetX * 2, y, z - dir.offsetZ - rot.offsetZ * 2);
	}

}
