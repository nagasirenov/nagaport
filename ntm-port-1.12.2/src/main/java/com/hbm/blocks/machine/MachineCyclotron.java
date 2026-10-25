package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineCyclotron;

import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class MachineCyclotron extends BlockDummyable {

	public MachineCyclotron(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {

		if(meta >= 12)
			return new TileEntityMachineCyclotron();

		if(meta >= 6)
			return new TileEntityProxyCombo().inventory().power().fluid();

		return null;
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;

		if(world.isRemote)
		{
			return true;
		} else if(!player.isSneaking())
		{
			int[] pos = this.findCore(world, x, y, z);

			if(pos == null)
				return false;

			TileEntityMachineCyclotron cyc = (TileEntityMachineCyclotron)world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));

			if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null) {

				for(int i = 0; i < 4; i++) {

					if(com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() == TileEntityMachineCyclotron.getItemForPlug(i) && !cyc.getPlug(i)) {
						com.hbm.util.NativeHeldItem.getMainHandNullable(player).shrink(1);
						cyc.setPlug(i);
						world.playSound(null, x + 0.5, y + 0.5, z + 0.5, com.hbm.main.ModSoundEvents.resolve("hbm:item.upgradePlug"), net.minecraft.util.SoundCategory.BLOCKS, 1.5F, 1.0F);
						return true;
					}
				}
			}

			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos[0], pos[1], pos[2]);
			return true;
		} else {
			return false;
		}
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
	public int[] getDimensions() {
		return new int[] {2, 0, 2, 2, 2, 2};
	}

	@Override
	public int getOffset() {
		return 2;
	}

	protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);

		this.makeExtra(world, x + dir.offsetX * o + 2, y, z + dir.offsetZ * o + 1);
		this.makeExtra(world, x + dir.offsetX * o + 2, y, z + dir.offsetZ * o);
		this.makeExtra(world, x + dir.offsetX * o + 2, y, z + dir.offsetZ * o - 1);
		this.makeExtra(world, x + dir.offsetX * o - 2, y, z + dir.offsetZ * o + 1);
		this.makeExtra(world, x + dir.offsetX * o - 2, y, z + dir.offsetZ * o);
		this.makeExtra(world, x + dir.offsetX * o - 2, y, z + dir.offsetZ * o - 1);
		this.makeExtra(world, x + dir.offsetX * o + 1, y, z + dir.offsetZ * o + 2);
		this.makeExtra(world, x + dir.offsetX * o, y, z + dir.offsetZ * o + 2);
		this.makeExtra(world, x + dir.offsetX * o - 1, y, z + dir.offsetZ * o + 2);
		this.makeExtra(world, x + dir.offsetX * o + 1, y, z + dir.offsetZ * o - 2);
		this.makeExtra(world, x + dir.offsetX * o, y, z + dir.offsetZ * o - 2);
		this.makeExtra(world, x + dir.offsetX * o - 1, y, z + dir.offsetZ * o - 2);
	}
}
