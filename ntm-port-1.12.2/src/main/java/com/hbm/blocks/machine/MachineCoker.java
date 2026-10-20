package com.hbm.blocks.machine;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.oil.TileEntityMachineCoker;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class MachineCoker extends BlockDummyable implements ITooltipProvider {

	public MachineCoker(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		
		if(meta >= 12) return new TileEntityMachineCoker();
		if(meta >= extra) return new TileEntityProxyCombo().inventory().fluid();
		return null;
	}
	
	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(); int y = nativePos.getY(); int z = nativePos.getZ(); int side = nativeFacing.getIndex();
		return standardOpenBehavior(world, x, y, z, player, side);
	}

	@Override
	public int[] getDimensions() {
		return new int[] {22, 0, 1, 1, 1, 1};
	}

	@Override
	public int getOffset() {
		return 1;
	}

	@Override
	protected boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
		if(super.checkRequirement(world, x, y, z, dir, o)) {

			x += dir.offsetX * o;
			z += dir.offsetZ * o;
			
			return MultiblockHandlerXR.checkSpace(world, x, y + 1, z, new int[] {5, 0, 2, 2, 2, 2}, x, y, z, ForgeDirection.NORTH) &&
					MultiblockHandlerXR.checkSpace(world, x + 2, y + 1, z + 2, new int[] {0, 1, 0, 0, 0, 0}, x, y, z, ForgeDirection.NORTH) &&
					MultiblockHandlerXR.checkSpace(world, x + 2, y + 1, z - 2, new int[] {0, 1, 0, 0, 0, 0}, x, y, z, ForgeDirection.NORTH) &&
					MultiblockHandlerXR.checkSpace(world, x - 2, y + 1, z + 2, new int[] {0, 1, 0, 0, 0, 0}, x, y, z, ForgeDirection.NORTH) &&
					MultiblockHandlerXR.checkSpace(world, x - 2, y + 1, z - 2, new int[] {0, 1, 0, 0, 0, 0}, x, y, z, ForgeDirection.NORTH);
		}
		
		return false;
	}

	@Override
	protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);

		x += dir.offsetX * o;
		z += dir.offsetZ * o;

		MultiblockHandlerXR.fillSpace(world, x, y + 1, z, new int[] {5, 0, 2, 2, 2, 2}, this, ForgeDirection.NORTH);
		MultiblockHandlerXR.fillSpace(world, x + 2, y + 1, z + 2, new int[] {0, 1, 0, 0, 0, 0}, this, ForgeDirection.NORTH);
		MultiblockHandlerXR.fillSpace(world, x + 2, y + 1, z - 2, new int[] {0, 1, 0, 0, 0, 0}, this, ForgeDirection.NORTH);
		MultiblockHandlerXR.fillSpace(world, x - 2, y + 1, z + 2, new int[] {0, 1, 0, 0, 0, 0}, this, ForgeDirection.NORTH);
		MultiblockHandlerXR.fillSpace(world, x - 2, y + 1, z - 2, new int[] {0, 1, 0, 0, 0, 0}, this, ForgeDirection.NORTH);

		this.makeExtra(world, x + 1, y, z + 1);
		this.makeExtra(world, x + 1, y, z - 1);
		this.makeExtra(world, x - 1, y, z + 1);
		this.makeExtra(world, x - 1, y, z - 1);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		this.addStandardInfo(stack, player, list, ext);
	}
}
