package com.hbm.blocks.network;

import api.hbm.conveyor.IConveyorBelt;
import api.hbm.conveyor.IConveyorItem;
import api.hbm.conveyor.IConveyorPackage;
import api.hbm.conveyor.IEnterableBlock;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.entity.item.EntityMovingItem;
import com.hbm.inventory.recipes.CrystallizerRecipes;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.InventoryUtil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;

public class CranePartitioner extends BlockContainer implements IConveyorBelt, IEnterableBlock, ITooltipProvider {

    public CranePartitioner() { super(Material.IRON); setDefaultState(blockState.getBaseState().withProperty(METADATA, 0)); }

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityCranePartitioner();
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack itemStack) {
		int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		if(i == 0) world.setBlockState(pos, getStateFromMeta(2), 2);
		if(i == 1) world.setBlockState(pos, getStateFromMeta(5), 2);
		if(i == 2) world.setBlockState(pos, getStateFromMeta(3), 2);
		if(i == 3) world.setBlockState(pos, getStateFromMeta(4), 2);
	}

    public static boolean isNativeModelOwner(net.minecraft.block.Block block) {
        return block != null && block.getClass() == CranePartitioner.class
                && new ResourceLocation("hbm", "tile.crane_partitioner").equals(block.getRegistryName());
    }
    @Override public boolean hasCustomBreakingProgress(IBlockState state) { return isNativeModelOwner(this); }
    public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, METADATA); }
    @Override public IBlockState getStateFromMeta(int metadata) { return getDefaultState().withProperty(METADATA, metadata & 15); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(METADATA); }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
    @Override public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT_MIPPED; }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) { return BlockFaceShape.UNDEFINED; }
    @Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) { return new AxisAlignedBB(0, 0, 0, 1, .75, 1); }
    @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) { return getBoundingBox(state, world, pos); }

	@Override public boolean canItemEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorItem entity) { return getTravelDirection(world, x, y, z, null) == dir; }
	@Override public boolean canPackageEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorPackage entity) { return false; }
	@Override public void onPackageEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorPackage entity) { }

	@Override
	public boolean canItemStay(World world, int x, int y, int z, Vec3d itemPos) {
		return true;
	}

	@Override
	public Vec3d getTravelLocation(World world, int x, int y, int z, Vec3d itemPos, double speed) {
		ForgeDirection dir = this.getTravelDirection(world, x, y, z, itemPos);
		Vec3d snap = this.getClosestSnappingPosition(world, x, y, z, itemPos);
		itemPos = new Vec3d(MathHelper.clamp(itemPos.x, x, x + 1), itemPos.y, MathHelper.clamp(itemPos.z, z, z + 1));
		Vec3d dest = new Vec3d(snap.x - dir.offsetX * speed, snap.y - dir.offsetY * speed, snap.z - dir.offsetZ * speed);
		Vec3d motion = new Vec3d((dest.x - itemPos.x), (dest.y - itemPos.y), (dest.z - itemPos.z));
		double len = motion.length();
		Vec3d ret = new Vec3d(itemPos.x + motion.x / len * speed, itemPos.y + motion.y / len * speed, itemPos.z + motion.z / len * speed);
		return ret;
	}

	@Override
	public Vec3d getClosestSnappingPosition(World world, int x, int y, int z, Vec3d itemPos) {
		ForgeDirection dir = this.getTravelDirection(world, x, y, z, itemPos);
		double itemX = MathHelper.clamp(itemPos.x, x, x + 1);
		double itemZ = MathHelper.clamp(itemPos.z, z, z + 1);
		double posX = x + 0.5;
		double posZ = z + 0.5;
		if(dir.offsetX != 0) posX = itemX;
		if(dir.offsetZ != 0) posZ = itemZ;
		return new Vec3d(posX, y + 0.25, posZ);
	}

	public ForgeDirection getTravelDirection(World world, int x, int y, int z, Vec3d itemPos) {
		int meta = getMetaFromState(world.getBlockState(new BlockPos(x, y, z)));
		return ForgeDirection.getOrientation(meta);
	}

	@Override
	public void onItemEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorItem entity) {
		TileEntityCranePartitioner partitioner = (TileEntityCranePartitioner) world.getTileEntity(new BlockPos(x, y, z));
		ItemStack stack = entity.getItemStack();
		ItemStack remainder = null;
		if(CrystallizerRecipes.getAmount(stack) > 0) {
			remainder = InventoryUtil.tryAddItemToInventory(partitioner, 0, TileEntityCranePartitioner.INPUT_COUNT - 1, stack);
		} else {
			remainder = InventoryUtil.tryAddItemToInventory(partitioner, TileEntityCranePartitioner.INPUT_COUNT, TileEntityCranePartitioner.INPUT_COUNT + TileEntityCranePartitioner.OUTPUT_COUNT - 1, stack);
		}
		if(remainder != null && !remainder.isEmpty()) {
			EntityItem item = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, remainder.copy());
			world.spawnEntity(item);
		}
	}

	public static class TileEntityCranePartitioner extends TileEntityMachineBase {
        @Override public boolean canRenderBreaking() {
            return getClass() == TileEntityCranePartitioner.class && getWorld() != null && isNativeModelOwner(getWorld().getBlockState(getPos()).getBlock());
        }


		// 104 total, since it's the highest I've seen in the codebase
		// and desh crates haven't exploderoonied as far as I know
		public static final int INPUT_COUNT = 96;
		public static final int OUTPUT_COUNT = 8;
		
		public TileEntityCranePartitioner() {
			super(INPUT_COUNT + OUTPUT_COUNT);
		}

		@Override public String getDefaultName() { return "container.partitioner"; }

		@Override
		public void update() {

			if(!this.world.isRemote) {

				List<ItemStack> stacks = new ArrayList<>();
				for(int i = 0; i < INPUT_COUNT; i++) if(slots[i] != null && !slots[i].isEmpty()) stacks.add(slots[i]);
				stacks.sort(stackSizeComparator);
				boolean markDirty = false;

				for(ItemStack stack : stacks) {
					int amount = CrystallizerRecipes.getAmount(stack);
					if(amount == 0) amount = stack.getCount(); // eject full stack if invalid type somehow ended up in the queue
					
					while(stack.getCount() >= amount) {
						ItemStack entityStack = stack.copy();
						entityStack.setCount(amount);
						stack.shrink(amount);
						EntityMovingItem item = new EntityMovingItem(this.world);
						item.setItemStack(entityStack);
						item.setPosition(this.pos.getX() + 0.5, this.pos.getY() + 0.25, this.pos.getZ() + 0.5);
						this.world.spawnEntity(item);
					}
				}

				for(int i = 0; i < INPUT_COUNT; i++) if(slots[i] != null && slots[i].getCount() <= 0) slots[i] = null;
				if(markDirty) this.markDirty();
			}
		}

		public static Comparator<ItemStack> stackSizeComparator = new Comparator<ItemStack>() {

			@Override
			public int compare(ItemStack o1, ItemStack o2) {
				return (int) Math.signum(o1.getCount() - o2.getCount());
			}
		};

		@Override
		public boolean canExtractItem(int slot, ItemStack stack, net.minecraft.util.EnumFacing nativeFace) {
			int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
			return slot >= INPUT_COUNT; // declog
		}

		@Override
		public boolean isItemValidForSlot(int i, ItemStack stack) {
			return i <= (INPUT_COUNT - 1) && CrystallizerRecipes.getAmount(stack) >= 1;
		}

		protected int[] access;
		
		@Override
		public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
			int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
			
			if(access == null) {
				access = new int[INPUT_COUNT + OUTPUT_COUNT]; // writing this by hand is for chumps
				for(int i = 0; i < INPUT_COUNT + OUTPUT_COUNT; i++) access[i] = i;
			}
			
			return access;
		}
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		this.addStandardInfo(stack, player, list, ext);
	}

	private final Random dropRandom = new Random();

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
		if(tile instanceof IInventory) {
			IInventory battery = (IInventory) tile;
			for(int i = 0; i < battery.getSizeInventory(); ++i) {
				ItemStack itemstack = battery.getStackInSlot(i);
				if(itemstack != null && !itemstack.isEmpty()) {
					float f = this.dropRandom.nextFloat() * 0.8F + 0.1F;
					float f1 = this.dropRandom.nextFloat() * 0.8F + 0.1F;
					float f2 = this.dropRandom.nextFloat() * 0.8F + 0.1F;
					while(itemstack.getCount() > 0) {
						int j1 = this.dropRandom.nextInt(21) + 10;
						if(j1 > itemstack.getCount()) j1 = itemstack.getCount();
						// Capture before depletion: native empty stacks hide their item.
						net.minecraft.item.Item dropItem = itemstack.getItem();
						int dropMeta = itemstack.getItemDamage();
						NBTTagCompound dropTag = itemstack.getTagCompound();
						itemstack.shrink(j1);
						EntityItem entityitem = new EntityItem(world, x + f, y + f1, z + f2, new ItemStack(dropItem, j1, dropMeta));
						if(dropTag != null) entityitem.getItem().setTagCompound(dropTag.copy());
						float f3 = 0.05F;
						entityitem.motionX = (float) this.dropRandom.nextGaussian() * f3;
						entityitem.motionY = (float) this.dropRandom.nextGaussian() * f3 + 0.2F;
						entityitem.motionZ = (float) this.dropRandom.nextGaussian() * f3;
						world.spawnEntity(entityitem);
					}
				}
			}
			world.updateComparatorOutputLevel(pos, this);
		}
		super.breakBlock(world, pos, state);
	}
}
