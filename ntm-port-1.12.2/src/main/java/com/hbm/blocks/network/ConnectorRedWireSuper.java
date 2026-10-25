package com.hbm.blocks.network;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import com.hbm.lib.Library;
import java.util.List;

import com.hbm.lib.Library;
import com.hbm.tileentity.network.TileEntityConnectorSuper;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class ConnectorRedWireSuper extends PylonBase {

	public ConnectorRedWireSuper(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityConnectorSuper();
	}

	public int onBlockPlaced(World world, int x, int y, int z, int side, float fX, float fY, float fZ, int meta) { return side; }

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		int meta = com.hbm.world.WorldBlockState.readMetadata(source, pos.getX(), pos.getY(), pos.getZ());
		double pixel = 0.0625D;
		double min = pixel * 5.0D;
		double max = pixel * 11.0D;

		ForgeDirection dir = ForgeDirection.getOrientation(meta).getOpposite();

		double minX = dir == Library.NEG_X ? 0.0D : dir == Library.POS_X ? 0.0D : min;
		double maxX = dir == Library.POS_X ? 1.0D : dir == Library.NEG_X ? 1.0D : max;
		double minY = dir == Library.NEG_Y ? 0.0D : dir == Library.POS_Y ? 0.0D : min;
		double maxY = dir == Library.POS_Y ? 1.0D : dir == Library.NEG_Y ? 1.0D : max;
		double minZ = dir == Library.NEG_Z ? 0.0D : dir == Library.POS_Z ? 0.0D : min;
		double maxZ = dir == Library.POS_Z ? 1.0D : dir == Library.NEG_Z ? 1.0D : max;

		return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		list.add(TextFormatting.GOLD + "Connection Type: " + TextFormatting.YELLOW + "Single");
		list.add(TextFormatting.GOLD + "Connection Range: " + TextFormatting.YELLOW + "100m");
	}
}
