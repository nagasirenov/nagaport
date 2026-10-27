package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityITER;
import com.hbm.tileentity.machine.TileEntityITERStruct;

import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

@Deprecated
public class MachineITER extends BlockDummyable {

	public MachineITER() {
		super(Material.IRON);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {

		if(meta >= 12)
			return new TileEntityITER();

		if(meta >= 6)
			return new TileEntityProxyCombo(true, true, true);

		return null;
	}

	@Override
	public int[] getDimensions() {
		//because we'll implement our own gnarly behavior here
		return new int[] { 0, 0, 0, 0, 0, 0 };
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(); int y = nativePos.getY(); int z = nativePos.getZ();
		if(world.isRemote)
		{
			return true;
		} else if(!player.isSneaking())
		{
			int[] pos = this.findCore(world, x, y, z);

			if(pos == null)
				return false;

			TileEntityITER entity = (TileEntityITER) world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));
			if(entity != null)
			{
				FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos[0], pos[1], pos[2]);
			}
			return true;
		} else {
			return false;
		}
	}

	public static final int height = 2;

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack) {

		if(!(player instanceof EntityPlayer))
			return;

		int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		EntityPlayer pl = (EntityPlayer) player;

		int o = getOffset();

		ForgeDirection dir = ForgeDirection.NORTH;

		if(i == 0)
		{
			dir = ForgeDirection.getOrientation(2);
		}
		if(i == 1)
		{
			dir = ForgeDirection.getOrientation(5);
		}
		if(i == 2)
		{
			dir = ForgeDirection.getOrientation(3);
		}
		if(i == 3)
		{
			dir = ForgeDirection.getOrientation(4);
		}

		dir = dir.getOpposite();

		com.hbm.world.WorldAirRemoval.remove(world, x, y, z);

		if(!checkRequirement(world, x, y, z, dir, o)) {

			if(!pl.capabilities.isCreativeMode) {
				ItemStack stack = com.hbm.util.PlayerInventoryRead.get(pl.inventory.mainInventory, pl.inventory.currentItem);
				Item item = Item.getItemFromBlock(this);

				if(stack == null) {
					pl.inventory.mainInventory.set(pl.inventory.currentItem, new ItemStack(this));
				} else {
					if(stack.getItem() != item || stack.getCount() == stack.getMaxStackSize()) {
						pl.inventory.addItemStackToInventory(new ItemStack(this));
					} else {
						com.hbm.util.NativeHeldItem.getMainHandNullable(pl).grow(1);
					}
				}
			}

			return;
		}

		com.hbm.util.NativeHeldItem.getMainHandNullable(pl).shrink(1);

		com.hbm.world.WorldBlockState.writeLegacyBlock(world, x + dir.offsetX * o , y + dir.offsetY * o + height, z + dir.offsetZ * o, this, dir.ordinal() + offset, 3);
		this.safeRem = true;
		fillSpace(world, x, y, z, dir, o);
		this.safeRem = false;
		world.scheduleUpdate(new net.minecraft.util.math.BlockPos(x, y, z), this, 1);
		world.scheduleUpdate(new net.minecraft.util.math.BlockPos(x, y, z), this, 2);

		super.onBlockPlacedBy(world, x, y, z, player, itemStack);
	}

	@Override
	public boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {

		x = x + dir.offsetX * o;
		z = z + dir.offsetZ * o;

		int[][][] layout = TileEntityITERStruct.collisionMask;

		for(int iy = 0; iy < 5; iy++) {

			int l = iy > 2 ? 4 - iy : iy;
			int[][] layer = layout[l];

			for(int ix = 0; ix < layer.length; ix++) {

				for(int iz = 0; iz < layer.length; iz++) {

					int ex = ix - layer.length / 2;
					int ez = iz - layer.length / 2;

					if(ex == 0 && y == 2 && ez == 0)
						continue;

					if(!com.hbm.world.WorldBlockState.read(world, x + ex, y + iy, z + ez).getBlock().canPlaceBlockAt(world, new net.minecraft.util.math.BlockPos(x + ex, y + iy, z + ez))) {
						return false;
					}
				}
			}
		}

		return true;
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {

		x = x + dir.offsetX * o;
		z = z + dir.offsetZ * o;

		int[][][] layout = TileEntityITERStruct.collisionMask;

		for(int iy = 0; iy < 5; iy++) {

			int l = iy > 2 ? 4 - iy : iy;
			int[][] layer = layout[l];

			for(int ix = 0; ix < layer.length; ix++) {

				for(int iz = 0; iz < layer[0].length; iz++) {

					int ex = ix - layer.length / 2;
					int ez = iz - layer.length / 2;

					int meta = 0;

					if(iy < 2) {
						meta = ForgeDirection.DOWN.ordinal();
					} else if(iy > 2) {
						meta = ForgeDirection.UP.ordinal();
					} else if(ex < 0) {
						meta = ForgeDirection.WEST.ordinal();
					} else if(ex > 0) {
						meta = ForgeDirection.EAST.ordinal();
					} else if(ez < 0) {
						meta = ForgeDirection.NORTH.ordinal();
					} else if(ez > 0) {
						meta = ForgeDirection.SOUTH.ordinal();
					} else {
						continue;
					}

					if(layout[l][ix][iz] > 0)
						com.hbm.world.WorldBlockState.writeLegacyBlock(world, x + ex, y + iy, z + ez, this, meta, 3);
				}
			}
		}

		this.makeExtra(world, x, y, z);
		this.makeExtra(world, x, y + 4, z);

		Vec3d vec = new Vec3d(5.75, 0, 0);

		for(int i = 0; i < 16; i++) {
			vec = vec.rotateYaw((float) (Math.PI / 8));
			this.makeExtra(world, x + (int)vec.x, y, z + (int)vec.z);
			this.makeExtra(world, x + (int)vec.x, y + 4, z + (int)vec.z);
		}
	}

	@Override
	public int getOffset() {
		return 7;
	}

}
