package com.hbm.blocks.machine;

import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing;
import net.minecraft.block.state.IBlockState;
import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.items.machine.ItemScraps;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.TileEntityFoundryChannel;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.INetworkProvider;
import com.hbm.uninos.networkproviders.FoundryNetwork;
import net.minecraft.util.math.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.block.ICrucibleAcceptor;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class FoundryChannel extends FoundryBlock implements ICrucibleAcceptor {


	public FoundryChannel() {
		super();
	}





    /** The native damage TESR owns housing and molten faces together. */
    @Override public boolean hasCustomBreakingProgress(IBlockState state) { return true; }

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityFoundryChannel();
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBounding, List<AxisAlignedBB> list, Entity entity, boolean actualState) {
		int x=pos.getX(), y=pos.getY(), z=pos.getZ();

		List<AxisAlignedBB> bbs = new ArrayList();

		bbs.add(new AxisAlignedBB(x + 0.3125D, y, z + 0.3125D, x + 0.6875D, y + 0.5D, z + 0.6875D));

		if(canConnectTo(world, x, y, z, Library.POS_X)) bbs.add(new AxisAlignedBB(x + 0.6875D, y, z + 0.3125D, x + 1D, y + 0.5D, z + 0.6875D));
		if(canConnectTo(world, x, y, z, Library.NEG_X)) bbs.add(new AxisAlignedBB(x, y, z + 0.3125D, x + 0.3125D, y + 0.5D, z + 0.6875D));
		if(canConnectTo(world, x, y, z, Library.POS_Z)) bbs.add(new AxisAlignedBB(x + 0.3125D, y, z + 0.6875D, x + 0.6875D, y + 0.5D, z + 1D));
		if(canConnectTo(world, x, y, z, Library.NEG_Z)) bbs.add(new AxisAlignedBB(x + 0.3125D, y, z, x + 0.6875D, y + 0.5D, z + 0.3125D));

		for(AxisAlignedBB bb : bbs) {
			if(entityBounding.intersects(bb)) {
				list.add(bb);
			}
		}
	}







	@Override
	public boolean canAcceptPartialPour(World world, int x, int y, int z, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) {
		return ((ICrucibleAcceptor) world.getTileEntity(new BlockPos(x, y, z))).canAcceptPartialPour(world, x, y, z, dX, dY, dZ, side, stack);
	}

	@Override
	public MaterialStack pour(World world, int x, int y, int z, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) {
		return ((ICrucibleAcceptor) world.getTileEntity(new BlockPos(x, y, z))).pour(world, x, y, z, dX, dY, dZ, side, stack);
	}

	@Override
	public boolean canAcceptPartialFlow(World world, int x, int y, int z, ForgeDirection side, MaterialStack stack) {
		return ((ICrucibleAcceptor) world.getTileEntity(new BlockPos(x, y, z))).canAcceptPartialFlow(world, x, y, z, side, stack);
	}

	@Override
	public MaterialStack flow(World world, int x, int y, int z, ForgeDirection side, MaterialStack stack) {
		return ((ICrucibleAcceptor) world.getTileEntity(new BlockPos(x, y, z))).flow(world, x, y, z, side, stack);
	}

	public boolean canConnectTo(IBlockAccess world, int x, int y, int z, ForgeDirection dir) {

		if(dir == ForgeDirection.UP || dir == ForgeDirection.DOWN || dir == ForgeDirection.UNKNOWN)
			return false;

		Block b = world.getBlockState(new BlockPos(x + dir.offsetX, y, z + dir.offsetZ)).getBlock();
		int meta = world.getBlockState(new BlockPos(x + dir.offsetX, y, z + dir.offsetZ)).getBlock().getMetaFromState(world.getBlockState(new BlockPos(x + dir.offsetX, y, z + dir.offsetZ)));

		if((b == ModBlocks.foundry_outlet || b == ModBlocks.foundry_slagtap) && meta == dir.ordinal())
			return true;

		return b == ModBlocks.foundry_channel || b == ModBlocks.foundry_mold;
	}








	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		int x=pos.getX(), y=pos.getY(), z=pos.getZ();
		if(world.isRemote) {
			return true;
		}

		TileEntityFoundryChannel cast = (TileEntityFoundryChannel) world.getTileEntity(new BlockPos(x, y, z));

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

		TileEntityFoundryChannel channel = (TileEntityFoundryChannel) world.getTileEntity(new BlockPos(x, y, z));
		if(channel.amount > 0) {
			ItemStack scrap = ItemScraps.create(new MaterialStack(channel.type, channel.amount));
			EntityItem item = new EntityItem(world, x + 0.5, y + getBoundingBox(world.getBlockState(new BlockPos(x,y,z)),world,new BlockPos(x,y,z)).maxY, z + 0.5, scrap);
			world.spawnEntity(item);
			channel.amount = 0;
		}

		super.breakBlock(world, pos, state);
	}

	public static class FoundryNode extends GenNode<FoundryNetwork> {

		public NTMMaterial type;

		public FoundryNode(INetworkProvider<FoundryNetwork> provider, com.hbm.util.fauxpointtwelve.BlockPos... positions) {
			super(provider, positions);
		}

		@Override
		public FoundryNode setConnections(DirPos... connections) {
			super.setConnections(connections);
			return this;
		}

	}


	@Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        int x=pos.getX(), y=pos.getY(), z=pos.getZ();
        return new AxisAlignedBB(canConnectTo(world,x,y,z,Library.NEG_X)?0:.3125,0,canConnectTo(world,x,y,z,Library.NEG_Z)?0:.3125,canConnectTo(world,x,y,z,Library.POS_X)?1:.6875,.5,canConnectTo(world,x,y,z,Library.POS_Z)?1:.6875);
    }
    @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) { return getBoundingBox(state,world,pos); }
}
