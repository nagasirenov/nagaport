package com.hbm.blocks.generic;

import java.util.List;

import com.hbm.dim.WorldProviderCelestial;
import com.hbm.extprop.HbmPlayerProps;
import com.hbm.inventory.recipes.PedestalRecipes;
import com.hbm.inventory.recipes.PedestalRecipes.PedestalRecipe;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.particle.helper.ExplosionSmallCreator;


import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class BlockPedestal extends BlockContainer {


	public BlockPedestal() {
		super(Material.ROCK);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityPedestal();
	}

	@Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
	@Override public boolean isOpaqueCube(IBlockState state) { return false; }
	@Override public boolean isFullCube(IBlockState state) { return false; }
	@Override public net.minecraft.block.state.BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) { return net.minecraft.block.state.BlockFaceShape.UNDEFINED; }
	@Override public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) { return true; }

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if(player.isSneaking()) return false;

		TileEntityPedestal pedestal = (TileEntityPedestal) world.getTileEntity(pos);

		if(pedestal.item == null && !player.getHeldItem(hand).isEmpty()) {
			if(world.isRemote) return true;
			pedestal.item = player.getHeldItem(hand).copy();
			player.setHeldItem(hand, ItemStack.EMPTY);
			pedestal.markDirty();
			sync(world, pos);
			return true;
		} else if(pedestal.item != null && player.getHeldItem(hand).isEmpty()) {
			if(world.isRemote) return true;
			player.setHeldItem(hand, pedestal.item.copy());
			pedestal.item = null;
			pedestal.markDirty();
			sync(world, pos);
			return true;
		}

		return false;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();

		if(!world.isRemote) {
			TileEntityPedestal entity = (TileEntityPedestal) world.getTileEntity(pos);
			if(entity != null && entity.item != null) {
				EntityItem item = new EntityItem(world, x + 0.5, y, z + 0.5, entity.item.copy());
				world.spawnEntity(item);
			}
		}

		super.breakBlock(world, pos, state);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block b, BlockPos fromPos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		if(!world.isRemote) {
			if(world.isBlockPowered(pos)) {

				TileEntityPedestal nw = castOrNull(loadedTile(world, new BlockPos(x + ForgeDirection.NORTH.offsetX * 2 + ForgeDirection.WEST.offsetX * 2, y, z + ForgeDirection.NORTH.offsetZ * 2 + ForgeDirection.WEST.offsetZ * 2)));
				TileEntityPedestal n = castOrNull(loadedTile(world, new BlockPos(x + ForgeDirection.NORTH.offsetX * 3, y, z + ForgeDirection.NORTH.offsetZ * 3)));
				TileEntityPedestal ne = castOrNull(loadedTile(world, new BlockPos(x + ForgeDirection.NORTH.offsetX * 2 + ForgeDirection.EAST.offsetX * 2, y, z + ForgeDirection.NORTH.offsetZ * 2 + ForgeDirection.EAST.offsetZ * 2)));
				TileEntityPedestal w = castOrNull(loadedTile(world, new BlockPos(x + ForgeDirection.WEST.offsetX * 3, y, z + ForgeDirection.WEST.offsetZ * 3)));
				TileEntityPedestal center = (TileEntityPedestal) world.getTileEntity(pos);
				TileEntityPedestal e = castOrNull(loadedTile(world, new BlockPos(x + ForgeDirection.EAST.offsetX * 3, y, z + ForgeDirection.EAST.offsetZ * 3)));
				TileEntityPedestal sw = castOrNull(loadedTile(world, new BlockPos(x + ForgeDirection.SOUTH.offsetX * 2 + ForgeDirection.WEST.offsetX * 2, y, z + ForgeDirection.SOUTH.offsetZ * 2 + ForgeDirection.WEST.offsetZ * 2)));
				TileEntityPedestal s = castOrNull(loadedTile(world, new BlockPos(x + ForgeDirection.SOUTH.offsetX * 3, y, z + ForgeDirection.SOUTH.offsetZ * 3)));
				TileEntityPedestal se = castOrNull(loadedTile(world, new BlockPos(x + ForgeDirection.SOUTH.offsetX * 2 + ForgeDirection.EAST.offsetX * 2, y, z + ForgeDirection.SOUTH.offsetZ * 2 + ForgeDirection.EAST.offsetZ * 2)));

				TileEntityPedestal[] tileArray = new TileEntityPedestal[] {nw, n, ne, w, center, e, sw, s, se};
				List<EntityPlayer> nearbyPlayers = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1).grow(20, 20, 20));

				outer: for(PedestalRecipe recipe : PedestalRecipes.recipes) {

					/// EXTRA CONDITIONS ///
					if(recipe.extra == recipe.extra.FULL_MOON) {
						if(world.getCelestialAngle(0) < 0.35 || world.getCelestialAngle(0) > 0.65) continue;
						if(world.provider.getMoonPhase(world.getWorldTime()) != 0) continue;
					}

					// Changed requirement to eclipse in spork
					if(recipe.extra == recipe.extra.NEW_MOON) {
						if(world.provider instanceof WorldProviderCelestial) {
							if(world.getCelestialAngle(0) > 0.15 && world.getCelestialAngle(0) < 0.85) continue;
							if(!((WorldProviderCelestial) world.provider).isEclipse()) continue;
						} else {
							if(world.getCelestialAngle(0) < 0.35 || world.getCelestialAngle(0) > 0.65) continue;
							if(world.provider.getMoonPhase(world.getWorldTime()) != 4) continue;
						}
					}

					if(recipe.extra == recipe.extra.SUN) {
						if(world.getCelestialAngle(0) > 0.15 && world.getCelestialAngle(0) < 0.85) continue;
					}

					if(recipe.extra == recipe.extra.BAD_KARMA) {
						boolean matches = false;
						for(EntityPlayer player : nearbyPlayers) if(HbmPlayerProps.getData(player).reputation <= -10) { matches = true; break; }
						if(!matches) continue;
					}

					if(recipe.extra == recipe.extra.GOOD_KARMA) {
						boolean matches = false;
						for(EntityPlayer player : nearbyPlayers) if(HbmPlayerProps.getData(player).reputation >= 10) { matches = true; break; }
						if(!matches) continue;
					}

					/// CHECK ITEMS ///
					for(int i = 0; i < 9; i++) {
						ItemStack pedestal = tileArray[i] != null ? tileArray[i].item : null;
						if(pedestal == null && recipe.input[i] != null) continue outer;
						if(pedestal != null && recipe.input[i] == null) continue outer;
						if(pedestal == null && recipe.input[i] == null) continue;

						if(!recipe.input[i].matchesRecipe(pedestal, true) || recipe.input[i].stacksize != pedestal.getCount()) continue outer;
					}

					/// REMOVE ITEMS ///
					for(int i = 0; i < 9; i++) {
						if(i == 4) continue;
						ItemStack pedestal = tileArray[i] != null ? tileArray[i].item : null;
						if(pedestal == null && recipe.input[i] == null) continue;
						tileArray[i].item = null;
						tileArray[i].markDirty();
						sync(world, tileArray[i].getPos());
					}

					/// PRODUCE RESULT ///
					center.item = recipe.output.copy();
					center.markDirty();
					sync(world, pos);
					ExplosionSmallCreator.composeEffect(world, x + 0.5, y + 1.5, z + 0.5, 10, 2.5F, 1F);

					List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(x + 0.5, y, z + 0.5, x + 0.5, y, z + 0.5).grow(50, 50, 50));
					for(EntityPlayer player : players) player.addStat(MainRegistry.statLegendary, 1);

					return;
				}
			}
		}
	}

	private static TileEntity loadedTile(World world, BlockPos pos) {
		return world.isBlockLoaded(pos, false) ? world.getTileEntity(pos) : null;
	}

	private static void sync(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		world.notifyBlockUpdate(pos, state, state, 3);
	}

	public static TileEntityPedestal castOrNull(TileEntity tile) {
		if(tile instanceof TileEntityPedestal) return (TileEntityPedestal) tile;
		return null;
	}

	public static class TileEntityPedestal extends TileEntity {

		public ItemStack item;


		@Override public NBTTagCompound getUpdateTag() { return writeToNBT(new NBTTagCompound()); }

		@Override
		public SPacketUpdateTileEntity getUpdatePacket() {
			NBTTagCompound nbt = new NBTTagCompound();
			this.writeToNBT(nbt);
			return new SPacketUpdateTileEntity(this.getPos(), 0, nbt);
		}

		@Override
		public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
			this.readFromNBT(pkt.getNbtCompound());
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			ItemStack loaded = new ItemStack(nbt.getCompoundTag("item"));
			this.item = loaded.isEmpty() ? null : loaded;
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			if(this.item != null) {
				NBTTagCompound stack = new NBTTagCompound();
				this.item.writeToNBT(stack);
				nbt.setTag("item", stack);
			} else { nbt.removeTag("item"); }
			return nbt;
		}
	}
}
