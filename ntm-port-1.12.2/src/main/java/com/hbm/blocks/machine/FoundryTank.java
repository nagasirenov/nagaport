package com.hbm.blocks.machine;

import net.minecraft.util.EnumHand;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.items.machine.ItemScraps;
import com.hbm.tileentity.machine.TileEntityFoundryTank;

import api.hbm.block.ICrucibleAcceptor;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class FoundryTank extends FoundryBlock implements ICrucibleAcceptor {


	public FoundryTank() {
		super();
	}
	




    /** The native damage TESR owns housing and molten faces together. */
    @Override public boolean hasCustomBreakingProgress(IBlockState state) { return true; }

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityFoundryTank();
	}

	@Override
	public boolean canAcceptPartialPour(World world, int x, int y, int z, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) {
		return ((ICrucibleAcceptor) world.getTileEntity(new BlockPos(x, y, z))).canAcceptPartialPour(world, x, y, z, dX, dY, dZ, side, stack);
	}

	@Override
	public MaterialStack pour(World world, int x, int y, int z, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) {
		return ((ICrucibleAcceptor) world.getTileEntity(new BlockPos(x, y, z))).pour(world, x, y, z, dX, dY, dZ, side, stack);
	}

	@Override public boolean canAcceptPartialFlow(World world, int x, int y, int z, ForgeDirection side, MaterialStack stack) { return false; }
	@Override public MaterialStack flow(World world, int x, int y, int z, ForgeDirection side, MaterialStack stack) { return stack; }



	

	

	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		int x=pos.getX(), y=pos.getY(), z=pos.getZ();
		if(world.isRemote) {
			return true;
		}
		
		TileEntityFoundryTank cast = (TileEntityFoundryTank) world.getTileEntity(new BlockPos(x, y, z));
		
		if(!player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() instanceof ItemTool && ((ItemTool) player.getHeldItem(hand).getItem()).getToolClasses(player.getHeldItem(hand)).contains("shovel")) {
			if(cast.amount > 0) {
				ItemStack scrap = ItemScraps.create(new MaterialStack(cast.type, cast.amount));
				if(!player.inventory.addItemStackToInventory(scrap)) {
					EntityItem item = new EntityItem(world, x + 0.5, y + getBoundingBox(world.getBlockState(new BlockPos(x,y,z)),world,new BlockPos(x,y,z)).maxY, z + 0.5, scrap);
					world.spawnEntity(item);
				} else {
					player.inventoryContainer.detectAndSendChanges();
				}
				cast.amount = 0;
				cast.type = null;
				cast.markDirty();
				notifyChange(world, new BlockPos(x, y, z));
			}
			return true;
		}
		
		return false;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		int x=pos.getX(), y=pos.getY(), z=pos.getZ();
		
		TileEntityFoundryTank tank = (TileEntityFoundryTank) world.getTileEntity(new BlockPos(x, y, z));
		if(tank.amount > 0) {
			ItemStack scrap = ItemScraps.create(new MaterialStack(tank.type, tank.amount));
			EntityItem item = new EntityItem(world, x + 0.5, y + getBoundingBox(world.getBlockState(new BlockPos(x,y,z)),world,new BlockPos(x,y,z)).maxY, z + 0.5, scrap);
			world.spawnEntity(item);
			tank.amount = 0;
		}
		
		super.breakBlock(world, pos, state);
	}
	
	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return side != EnumFacing.UP;
	}
}
