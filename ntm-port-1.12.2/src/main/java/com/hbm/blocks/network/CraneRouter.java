package com.hbm.blocks.network;

import api.hbm.conveyor.IConveyorBelt;
import api.hbm.conveyor.IConveyorItem;
import api.hbm.conveyor.IConveyorPackage;
import api.hbm.conveyor.IEnterableBlock;
import com.hbm.blocks.IBlockMultiPass;
import com.hbm.blocks.MultipassContext;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.entity.item.EntityMovingItem;
import com.hbm.entity.item.EntityMovingPackage;
import com.hbm.items.tool.ItemConveyorWand;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.module.ModulePatternMatcher;
import com.hbm.tileentity.network.TileEntityCraneRouter;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

public class CraneRouter extends BlockContainer implements IBlockMultiPass, IEnterableBlock, ITooltipProvider {

    @Override protected net.minecraft.block.state.BlockStateContainer createBlockState() { return MultipassContext.stateContainer(this); }
    @Override public net.minecraft.block.state.IBlockState getStateFromMeta(int metadata) { return getDefaultState().withProperty(MultipassContext.METADATA, metadata & 15); }
    @Override public int getMetaFromState(net.minecraft.block.state.IBlockState state) { return state.getValue(MultipassContext.METADATA); }
    @Override public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) { return net.minecraft.util.EnumBlockRenderType.MODEL; }
    @Override public net.minecraft.util.BlockRenderLayer getRenderLayer() { return net.minecraft.util.BlockRenderLayer.CUTOUT_MIPPED; }
    @Override public net.minecraft.block.state.IBlockState getExtendedState(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos) {
        return ((net.minecraftforge.common.property.IExtendedBlockState) state).withProperty(MultipassContext.PROPERTY, getMultipassContext(state, world, pos));
    }
    @Override public boolean shouldSideBeRendered(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumFacing side) { return true; }



	public CraneRouter() {
		super(Material.IRON);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityCraneRouter();
	}





	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null && com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() instanceof ItemConveyorWand) {
			return false;
		} else if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, x, y, z);
			return true;
		} else {
			return false;
		}
	}





	/*
	 * arg arg arg spongeboy me bob i have fooled the system that only allows one tint per pass by disabling all rendered sides except one and rendering multiple passes arg arg arg
	 */


	@Override
	public int getPasses() {
		return 7;
	}

	@Override public boolean canItemEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorItem entity) { return true; }
	@Override public boolean canPackageEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorPackage entity) { return true; }

	@Override
	public void onItemEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorItem entity) {
		List<ItemStack>[] sort = this.sort(world, x, y, z, entity.getItemStack());

		for(int i = 0; i < 7; i++) {
			ForgeDirection d = ForgeDirection.getOrientation(i);
			List<ItemStack> list = sort[i];
			
			if(d == ForgeDirection.UNKNOWN) {
				for(ItemStack stack : list) world.spawnEntity(new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, stack));
			} else {
				for(ItemStack stack : list) sendOnRoute(world, x, y, z, stack, d);
			}
		}
		
	}

	protected void sendOnRoute(World world, int x, int y, int z, ItemStack item, ForgeDirection dir) {
		IConveyorBelt belt = null;
		Block block = com.hbm.world.WorldBlockState.read(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ).getBlock();

		if(block instanceof IConveyorBelt) {
			belt = (IConveyorBelt) block;
		}

		if(belt != null) {
			EntityMovingItem moving = new EntityMovingItem(world);
			Vec3d pos = new Vec3d(x + 0.5 + dir.offsetX * 0.55, y + 0.5 + dir.offsetY * 0.55, z + 0.5 + dir.offsetZ * 0.55);
			Vec3d snap = belt.getClosestSnappingPosition(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, pos);
			moving.setPosition(snap.x, snap.y, snap.z);
			moving.setItemStack(item);
			world.spawnEntityInWorld(moving);
		} else {
			world.spawnEntity(new EntityItem(world, x + 0.5 + dir.offsetX * 0.55, y + 0.5 + dir.offsetY * 0.55, z + 0.5 + dir.offsetZ * 0.55, item));
		}
	}

	@Override
	public void onPackageEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorPackage entity) {
		List<ItemStack>[] sort = this.sort(world, x, y, z, entity.getItemStacks());

		for(int i = 0; i < 7; i++) {
			ForgeDirection d = ForgeDirection.getOrientation(i);
			List<ItemStack> list = sort[i];
			if(list.isEmpty()) continue;

			if(d == ForgeDirection.UNKNOWN) {
				for(ItemStack stack : list) world.spawnEntity(new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, stack));
			} else {
				
				IConveyorBelt belt = null;
				Block block = com.hbm.world.WorldBlockState.read(world, x + d.offsetX, y + d.offsetY, z + d.offsetZ).getBlock();
				if(block instanceof IConveyorBelt) belt = (IConveyorBelt) block;

				if(belt != null) {
					EntityMovingPackage moving = new EntityMovingPackage(world);
					Vec3d pos = new Vec3d(x + 0.5 + d.offsetX * 0.55, y + 0.5 + d.offsetY * 0.55, z + 0.5 + d.offsetZ * 0.55);
					Vec3d snap = belt.getClosestSnappingPosition(world, x + d.offsetX, y + d.offsetY, z + d.offsetZ, pos);
					moving.setPosition(snap.x, snap.y, snap.z);
					moving.setItemStacks(list.toArray(new ItemStack[0]));
					world.spawnEntityInWorld(moving);
				} else {
					for(ItemStack stack : list) world.spawnEntity(new EntityItem(world, x + 0.5 + d.offsetX * 0.55, y + 0.5 + d.offsetY * 0.55, z + 0.5 + d.offsetZ * 0.55, stack));
				}
			}
		}
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		this.addStandardInfo(stack, player, list, ext);
	}
	
	/** Accepts an arbitrary amount of ItemStacks (either solo or from boxes) and returns an array corresponding with ForgeDirections,
	 * with each direction having a list of items being output in that direction. Index 6 is used for UNKNOWN, i.e. unsortable items.
	 * Returned lists are populated with COPIES of the original stacks. */
	public static List<ItemStack>[] sort(World world, int x, int y, int z, ItemStack... stacks) {
		TileEntityCraneRouter router = (TileEntityCraneRouter) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
		List<ItemStack>[] output = new List[7];
		for(int i = 0; i < 7; i++) output[i] = new ArrayList();
		
		for(ItemStack stack : stacks) {
			if(stack == null) continue;
			ForgeDirection dir = getOutputDir(router, stack.copy());
			output[dir.ordinal()].add(stack);
		}
		
		return output;
	}
	
	public static ForgeDirection getOutputDir(TileEntityCraneRouter router, ItemStack stack) {
		List<ForgeDirection> validDirs = new ArrayList();

		//check filters for all sides
		for(int side = 0; side < 6; side++) {

			ModulePatternMatcher matcher = router.patterns[side];
			int mode = router.modes[side];

			//if the side is disabled or wildcard, skip
			if(mode == router.MODE_NONE || mode == router.MODE_WILDCARD)
				continue;

			boolean matchesFilter = false;

			for(int slot = 0; slot < 5; slot++) {
				ItemStack filter = router.slots[side * 5 + slot];

				if(filter == null)
					continue;

				//the filter kicks in so long as one entry matches
				if(matcher.isValidForFilter(filter, slot, stack)) {
					matchesFilter = true;
					break;
				}
			}

			//add dir if matches with whitelist on or doesn't match with blacklist on
			if((mode == router.MODE_WHITELIST && matchesFilter) || (mode == router.MODE_BLACKLIST && !matchesFilter)) {
				validDirs.add(ForgeDirection.getOrientation(side));
			}
		}

		//if no valid dirs have yet been found, use wildcard
		if(validDirs.isEmpty()) {
			for(int side = 0; side < 6; side++) {
				if(router.modes[side] == router.MODE_WILDCARD) {
					validDirs.add(ForgeDirection.getOrientation(side));
				}
			}
		}

		if(validDirs.isEmpty()) {
			return ForgeDirection.UNKNOWN;
		}

		int i = router.getWorld().rand.nextInt(validDirs.size());
		return validDirs.get(i);
	}
}
