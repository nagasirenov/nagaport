package com.hbm.blocks;

import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.handler.ThreeInts;
import com.hbm.interfaces.ICopiable;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.world.gen.nbt.INBTBlockTransformable;

import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import com.hbm.lib.ForgeDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class BlockDummyable extends BlockContainer implements ICustomBlockHighlight, ICopiable, INBTBlockTransformable {

	public BlockDummyable(Material mat) {
		super(mat);
		this.setTickRandomly(true);
		setDefaultState(blockState.getBaseState().withProperty(METADATA, 0));
	}

	/** Native engine boundary for the existing NTM-owned virtual coordinate hooks. */
	public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);
	private AxisAlignedBB selectionBounds = FULL_BLOCK_AABB;
	@Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, METADATA); }
	@Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(METADATA, meta & 15); }
	@Override public int getMetaFromState(IBlockState state) { return state.getValue(METADATA); }
	@Override public void neighborChanged(IBlockState state, World world, BlockPos pos, Block changed, BlockPos fromPos) {
		onNeighborBlockChange(world, pos.getX(), pos.getY(), pos.getZ(), changed);
	}
	@Override public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
		updateTick(world, pos.getX(), pos.getY(), pos.getZ(), random);
	}
	@Override public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		return getStateFromMeta(onBlockPlaced(world, pos.getX(), pos.getY(), pos.getZ(), side.getIndex(), hitX, hitY, hitZ, meta));
	}
	@Override public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		onBlockPlacedBy(world, pos.getX(), pos.getY(), pos.getZ(), placer, stack);
	}
	@Override public void breakBlock(World world, BlockPos pos, IBlockState state) {
		breakBlock(world, pos.getX(), pos.getY(), pos.getZ(), state.getBlock(), state.getBlock().getMetaFromState(state));
	}
	@Override public EnumBlockRenderType getRenderType(IBlockState state) {
		// Positive NTM render IDs require their separately migrated native model binding.
		return getRenderType() < 0 ? EnumBlockRenderType.INVISIBLE : EnumBlockRenderType.MODEL;
	}
	@Override public boolean isOpaqueCube(IBlockState state) { return isOpaqueCube(); }
	@Override public boolean isFullCube(IBlockState state) { return renderAsNormalBlock(); }
	@Override public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		onBlockHarvested(world, pos.getX(), pos.getY(), pos.getZ(), getMetaFromState(state), player);
	}
	@Override public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity tile, ItemStack tool) {
		harvestBlock(world, player, pos.getX(), pos.getY(), pos.getZ(), getMetaFromState(state));
	}
	@Override public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB query, List<AxisAlignedBB> boxes, Entity entity, boolean actualState) {
		addCollisionBoxesToList(world, pos.getX(), pos.getY(), pos.getZ(), query, boxes, entity);
	}
	@Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		setBlockBoundsBasedOnState(world, pos.getX(), pos.getY(), pos.getZ());
		return selectionBounds;
	}
	/** Bounds are NTM's existing per-owner selection state, not mutable Minecraft bounds. */
	public void setBlockBounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		selectionBounds = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
	}
	private static int metadata(IBlockAccess world, int x, int y, int z) {
		IBlockState state = world.getBlockState(new BlockPos(x, y, z));
		return state.getBlock().getMetaFromState(state);
	}
	private static void setMetadata(World world, int x, int y, int z, int meta, int flags) {
		BlockPos pos = new BlockPos(x, y, z);
		world.setBlockState(pos, world.getBlockState(pos).getBlock().getStateFromMeta(meta), flags);
	}

	/// BLOCK METADATA ///

	// 0-5 dummy rotation (for dummy neighbor checks)
	// 6-11 extra (6 rotations with flag, for pipe connectors and the like)
	// 12-15 block rotation (for rendering the TE)

	// meta offset from dummy to TE rotation
	public static final int offset = 10;
	// meta offset from dummy to extra rotation
	public static final int extra = 6;

	/*
	 * An extra integer that can be set before block set operations (such as makeExtra) and intercepted in createNewTileEntity.
	 * This way we can inelegantly add variation to the tiles created even if the metadata would be the same.
	 * Why createNewTileEntity only takes two args or why it is used by the chunk's setBlock implementation is beyond me but any
	 * other solution feels like putting in way too much effort to achieve the same thing, really.
	 */
	public static int overrideTileMeta = 0;

	public static boolean safeRem = false;

	public static void setOverride(int i) {
		overrideTileMeta = i;
	}

	public static void resetOverride() {
		overrideTileMeta = 0;
	}

	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {

		// Native BlockContainer inherits the empty Block.neighborChanged implementation.

		if(safeRem)
			return;

		destroyIfOrphan(world, x, y, z);
	}

	public void updateTick(World world, int x, int y, int z, Random rand) {

		super.updateTick(world, new BlockPos(x, y, z), world.getBlockState(new BlockPos(x, y, z)), rand);

		if(!internalPlayers.isEmpty()) {
			boolean anyStillInside = false;
			for(EntityPlayer player : internalPlayers) {
				if(isPlayerInside(world, player)) {
					anyStillInside = true;
					break;
				}
			}

			if(anyStillInside) {
				world.scheduleUpdate(new BlockPos(x, y, z), this, 1);
			} else {
				internalPlayers.clear();
			}
		}

		destroyIfOrphan(world, x, y, z);
	}

	private void destroyIfOrphan(World world, int x, int y, int z) {
		if(world.isRemote)
			return;

		int metadata = metadata(world, x, y, z);

		// if it's an extra, remove the extra-ness
		if(metadata >= extra)
			metadata -= extra;

		ForgeDirection dir = ForgeDirection.getOrientation(metadata).getOpposite();
		Block b = world.getBlockState(new BlockPos(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ)).getBlock();

		// An extra precaution against multiblocks on chunk borders being erroneously deleted.
		// Technically, this might be used to persist ghost dummy blocks by manipulating
		// loaded chunks and block destruction, but this gives no benefit to the player,
		// cannot be done accidentally, and is definitely preferable to multiblocks
		// just vanishing when their chunks are unloaded in an unlucky way.
		if(b != this && world.isAreaLoaded(new BlockPos(x - 1, y - 1, z - 1), new BlockPos(x + 1, y + 1, z + 1))) {
			if(isLegacyMonoblock(world, x, y, z)) {
				fixLegacyMonoblock(world, x, y, z);
			} else {
				world.setBlockToAir(new BlockPos(x, y, z));
			}
		}
	}

	// Override this when turning a single block into a pseudo-multiblock.
	// If this returns true, instead of being deleted as an orphan, the block
	// will be promoted to a core of a dummyable, however without any dummies.
	// This is only called if the block is presumed an orphan, so you don't
	// need to check that here.
	protected boolean isLegacyMonoblock(World world, int x, int y, int z) {
		return false;
	}

	protected void fixLegacyMonoblock(World world, int x, int y, int z) {
		// Promote to a lone core block with the same effective rotation as before the change
		setMetadata(world, x, y, z, offset + metadata(world, x, y, z), 3);
	}

	public int[] findCore(IBlockAccess world, int x, int y, int z) {
		positions.clear();
		return findCoreRec(world, x, y, z);
	}

	List<ThreeInts> positions = new ArrayList<>();

	public int[] findCoreRec(IBlockAccess world, int x, int y, int z) {

		ThreeInts pos = new ThreeInts(x, y, z);

		int metadata = metadata(world, x, y, z);

		// if it's an extra, remove the extra-ness
		if(metadata >= extra)
			metadata -= extra;

		// if the block matches and the orientation is "UNKNOWN", it's the core
		if(world.getBlockState(new BlockPos(x, y, z)).getBlock() == this && ForgeDirection.getOrientation(metadata) == ForgeDirection.UNKNOWN)
			return new int[] { x, y, z };

		if(positions.contains(pos))
			return null;

		ForgeDirection dir = ForgeDirection.getOrientation(metadata).getOpposite();

		Block b = world.getBlockState(new BlockPos(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ)).getBlock();

		if(b != this) {
			return null;
		}

		positions.add(pos);

		return findCoreRec(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
	}

	public int onBlockPlaced(World world, int x, int y, int z, int side, float fX, float fY, float fZ, int meta) {
		return side;
	}

	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack) {

		if(!(player instanceof EntityPlayer))
			return;

		ForgeDirection placedSide = ForgeDirection.getOrientation(metadata(world, x, y, z));

		safeRem = true;
		world.setBlockToAir(new BlockPos(x, y, z));
		safeRem = false;

		EntityPlayer pl = (EntityPlayer) player;

		int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

		// The direction the player is facing, for offsetting the block away from the player
		ForgeDirection facingDir = ForgeDirection.NORTH;

		if(placedSide == ForgeDirection.UP || placedSide == ForgeDirection.DOWN) {
			if(i == 0) facingDir = ForgeDirection.getOrientation(2);
			if(i == 1) facingDir = ForgeDirection.getOrientation(5);
			if(i == 2) facingDir = ForgeDirection.getOrientation(3);
			if(i == 3) facingDir = ForgeDirection.getOrientation(4);
		} else {
			facingDir = placedSide;
		}

		// The direction the final multiblock will be facing
		ForgeDirection dir = getDirModified(facingDir);

		int o = -getOffset();

		int ox = x + facingDir.offsetX * o;
		int oy = y + getHeightOffset();
		int oz = z + facingDir.offsetZ * o;

		// Modify offsets to handle differing rotations and placing blocks on walls
		if(placedSide == ForgeDirection.DOWN) {
			int[] dim = getDimensions();
			oy -= dim[0] + dim[1];
		} else if(placedSide != ForgeDirection.UP) {
			int[] rotDim = MultiblockHandlerXR.rotate(getDimensions(), dir);
			if(rotDim != null) {
				ox = x + placedSide.offsetX * rotDim[placedSide.getOpposite().ordinal()];
				oz = z + placedSide.offsetZ * rotDim[placedSide.getOpposite().ordinal()];
			}
		}

		if(!checkRequirement(world, ox - dir.offsetX * o, oy, oz - dir.offsetZ * o, dir, o)) {

			if(!pl.capabilities.isCreativeMode) {
				EnumHand refundHand = itemStack == pl.getHeldItemOffhand() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
				ItemStack stack = pl.getHeldItem(refundHand);
				Item item = Item.getItemFromBlock(this);

				if(stack == null || stack.isEmpty()) {
					pl.setHeldItem(refundHand, new ItemStack(this));
				} else {
					if(stack.getItem() != item || stack.getCount() == stack.getMaxStackSize()) {
						pl.inventory.addItemStackToInventory(new ItemStack(this));
					} else {
						pl.getHeldItem(refundHand).grow(1);
					}
				}
			}

			return;
		}

		if(!world.isRemote) {
			//this is separate because the multiblock rotation and the final meta might not be the same
			int meta = getMetaForCore(world, ox, oy, oz, pl, dir.ordinal() + offset);

			world.setBlockState(new BlockPos(ox, oy, oz), this.getStateFromMeta(meta), 3);
			IPersistentNBT.restoreData(world, ox, oy, oz, itemStack);
			fillSpace(world, ox - dir.offsetX * o, oy, oz - dir.offsetZ * o, dir, o);
		}

		// Check if the placing player is inside the placed multiblock
		if(isPlayerInside(world, pl)) internalPlayers.add(pl);

		world.scheduleUpdate(new BlockPos(x, y, z), this, 1);
		world.scheduleUpdate(new BlockPos(x, y, z), this, 2);

		super.onBlockPlacedBy(world, new BlockPos(x, y, z), world.getBlockState(new BlockPos(x, y, z)), player, itemStack);
	}

	private boolean isPlayerInside(World world, EntityPlayer player) {
		int x = MathHelper.floor(player.posX);
		int y = MathHelper.floor(player.posY + 0.1D);
		int z = MathHelper.floor(player.posZ);
		return world.getBlockState(new BlockPos(x, y, z)).getBlock() == this || world.getBlockState(new BlockPos(x, y + 1, z)).getBlock() == this;
	}

	/**
	 * A bit more advanced than the dir modifier, but it is important that the resulting direction meta is in the core range.
	 * Using the "extra" metas is technically possible but requires a bit of tinkering, e.g. preventing a recursive loop
	 * in the core finder and making sure the TE uses the right metas.
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @param player
	 * @param original
	 * @return
	 */
	protected int getMetaForCore(World world, int x, int y, int z, EntityPlayer player, int original) {
		return original;
	}

	/**
	 * Allows to modify the general placement direction as if the player had another rotation.
	 * Quite basic due to only having 1 param but it's more meant to fix/limit the amount of directions
	 * @param dir
	 * @return
	 */
	protected ForgeDirection getDirModified(ForgeDirection dir) {
		return dir;
	}

	protected boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
		return MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o, y + dir.offsetY * o, z + dir.offsetZ * o, getDimensions(), x, y, z, dir);
	}

	protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * o, y + dir.offsetY * o, z + dir.offsetZ * o, getDimensions(), this, dir);
	}

	// "upgrades" regular dummy blocks to ones with the extra flag
	public void makeExtra(World world, int x, int y, int z) {

		if(world.getBlockState(new BlockPos(x, y, z)).getBlock() != this)
			return;

		int meta = metadata(world, x, y, z);

		if(meta > 5)
			return;

		// setMetadata(world, x, y, z, meta + extra, 3);
		safeRem = true;
		world.setBlockState(new BlockPos(x, y, z), this.getStateFromMeta(meta + extra), 3);
		safeRem = false;
	}

	public void removeExtra(World world, int x, int y, int z) {

		if(world.getBlockState(new BlockPos(x, y, z)).getBlock() != this)
			return;

		int meta = metadata(world, x, y, z);

		if(meta <= 5 || meta >= 12)
			return;

		// setMetadata(world, x, y, z, meta + extra, 3);
		safeRem = true;
		world.setBlockState(new BlockPos(x, y, z), this.getStateFromMeta(meta - extra), 3);
		safeRem = false;
	}

	// checks if the dummy metadata is within the extra range
	public boolean hasExtra(int meta) {
		return meta > 5 && meta < 12;
	}

	public void breakBlock(World world, int x, int y, int z, Block b, int i) {
		if(i >= 12) {
			// ForgeDirection d = ForgeDirection.getOrientation(metadata(world, x, y, z) - offset);
			// MultiblockHandler.emptySpace(world, x, y, z, getDimensions(), this, d);
		} else if(!safeRem) {

			if(i >= extra)
				i -= extra;

			// ForgeDirection dir = ForgeDirection.getOrientation(i).getOpposite();
			// int[] pos = findCore(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);

			// if(pos != null) {

			ForgeDirection d = ForgeDirection.getOrientation(i);

			if(world.getBlockState(new BlockPos(x - d.offsetX, y - d.offsetY, z - d.offsetZ)).getBlock() == this)
				world.setBlockToAir(new BlockPos(x - d.offsetX, y - d.offsetY, z - d.offsetZ));
			// }
		}

		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));

		if(te instanceof ISidedInventory) {

			ISidedInventory sidedinv = (ISidedInventory) te;

			if(sidedinv != null) {
				for(int i1 = 0; i1 < sidedinv.getSizeInventory(); ++i1) {
					ItemStack itemstack = sidedinv.getStackInSlot(i1);

					if(itemstack != null && !itemstack.isEmpty()) {
						float f = world.rand.nextFloat() * 0.8F + 0.1F;
						float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
						float f2 = world.rand.nextFloat() * 0.8F + 0.1F;

						while(itemstack.getCount() > 0) {
							int j1 = world.rand.nextInt(21) + 10;

							if(j1 > itemstack.getCount()) {
								j1 = itemstack.getCount();
							}

							// Capture before depletion: native empty stacks hide their item.
							net.minecraft.item.Item dropItem = itemstack.getItem();
							int dropMeta = itemstack.getItemDamage();
							NBTTagCompound dropTag = itemstack.getTagCompound();
							itemstack.shrink(j1);
							EntityItem entityitem = new EntityItem(world, x + f, y + f1, z + f2, new ItemStack(dropItem, j1, dropMeta));

							if(dropTag != null) {
								entityitem.getItem().setTagCompound(dropTag.copy());
							}

							float f3 = 0.05F;
							entityitem.motionX = (float) world.rand.nextGaussian() * f3;
							entityitem.motionY = (float) world.rand.nextGaussian() * f3 + 0.2F;
							entityitem.motionZ = (float) world.rand.nextGaussian() * f3;
							world.spawnEntity(entityitem);
						}
					}
				}

				world.updateComparatorOutputLevel(new BlockPos(x, y, z), b);
			}
		}

		super.breakBlock(world, new BlockPos(x, y, z), b.getStateFromMeta(i));
	}

	public int getRenderType() {
		return -1;
	}

	public boolean isOpaqueCube() {
		return false;
	}

	public boolean renderAsNormalBlock() {
		return false;
	}

	public abstract int[] getDimensions();

	public abstract int getOffset();

	public int getHeightOffset() {
		return 0;
	}

	protected boolean standardOpenBehavior(World world, int x, int y, int z, EntityPlayer player, int id) {

		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			int[] pos = this.findCore(world, x, y, z);

			if(pos == null)
				return false;

			FMLNetworkHandler.openGui(player, MainRegistry.instance, id, world, pos[0], pos[1], pos[2]);
			return true;
		} else {
			return true;
		}
	}

	public void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player) {

		if(!player.capabilities.isCreativeMode) {
			harvesters.set(player);
			this.dropBlockAsItem(world, new BlockPos(x, y, z), getStateFromMeta(meta), 0);
			harvesters.set(null);
		}
	}

	/*
	 * Called after the block and TE are already gone, so this method is of no use to us.
	 */
	public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int meta) {
		player.addStat(StatList.getBlockStats(this), 1);
		player.addExhaustion(0.025F);
	}

	public boolean useDetailedHitbox() {
		return !bounding.isEmpty();
	}

	public List<AxisAlignedBB> bounding = new ArrayList<>();

	// players currently inside instances of this block
	private List<EntityPlayer> internalPlayers = new ArrayList<>();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB entityBounding, List list, Entity entity) {
		if(!internalPlayers.isEmpty() && internalPlayers.contains(entity))
			return;

		if(!this.useDetailedHitbox()) {
			AxisAlignedBB box = getCollisionBoundingBoxFromPool(world, x, y, z);
			if(box != null && entityBounding.intersects(box)) list.add(box);
			return;
		}

		int[] pos = this.findCore(world, x, y, z);

		if(pos == null)
			return;

		x = pos[0];
		y = pos[1];
		z = pos[2];

		ForgeDirection rot = ForgeDirection.getOrientation(metadata(world, x, y, z) - offset).getRotation(ForgeDirection.UP);

		for(AxisAlignedBB aabb : this.bounding) {
			AxisAlignedBB boxlet = getAABBRotationOffset(aabb, x + 0.5, y, z + 0.5, rot);

			if(entityBounding.intersects(boxlet)) {
				list.add(boxlet);
			}
		}
	}

	/** Existing NTM collision hook uses world coordinates; preserve descendant virtual dispatch. */
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return selectionBounds.offset(x, y, z);
	}

	public static AxisAlignedBB getAABBRotationOffset(AxisAlignedBB aabb, double x, double y, double z, ForgeDirection dir) {

		AxisAlignedBB newBox = null;

		if(dir == ForgeDirection.NORTH) newBox = new AxisAlignedBB(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
		if(dir == ForgeDirection.EAST) newBox = new AxisAlignedBB(-aabb.maxZ, aabb.minY, aabb.minX, -aabb.minZ, aabb.maxY, aabb.maxX);
		if(dir == ForgeDirection.SOUTH) newBox = new AxisAlignedBB(-aabb.maxX, aabb.minY, -aabb.maxZ, -aabb.minX, aabb.maxY, -aabb.minZ);
		if(dir == ForgeDirection.WEST) newBox = new AxisAlignedBB(aabb.minZ, aabb.minY, -aabb.maxX, aabb.maxZ, aabb.maxY, -aabb.minX);

		if(newBox != null) {
			newBox = newBox.offset(x, y, z);
			return newBox;
		}

		return new AxisAlignedBB(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ).offset(x + 0.5, y + 0.5, z + 0.5);
	}

	// Don't mutate the xyz parameters, or the interaction max distance will bite you
	@Override
	public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d startVec, Vec3d endVec) {
		if(!this.useDetailedHitbox()) {
			return super.collisionRayTrace(state, world, pos, startVec, endVec);
		}

		int[] corePos = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());

		if(corePos == null)
			return super.collisionRayTrace(state, world, pos, startVec, endVec);

		ForgeDirection rot = ForgeDirection.getOrientation(metadata(world, corePos[0], corePos[1], corePos[2]) - offset).getRotation(ForgeDirection.UP);

		for(AxisAlignedBB aabb : this.bounding) {
			AxisAlignedBB boxlet = getAABBRotationOffset(aabb, corePos[0] + 0.5, corePos[1], corePos[2] + 0.5, rot);

			RayTraceResult intercept = boxlet.calculateIntercept(startVec, endVec);
			if(intercept != null) {
				return new RayTraceResult(intercept.hitVec, intercept.sideHit, pos);
			}
		}

		return null;
	}

	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		if(!this.useDetailedHitbox()) {
			// Original Block default was a no-op, retaining the owner's previous bounds.
		} else {
			this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.999F, 1.0F); //for some fucking reason setting maxY to something that isn't 1 magically fixes item collisions
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldDrawHighlight(World world, int x, int y, int z) {
		return !this.bounding.isEmpty();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void drawHighlight(DrawBlockHighlightEvent event, World world, int x, int y, int z) {

		int[] pos = this.findCore(world, x, y, z);
		if(pos == null) return;

		x = pos[0];
		y = pos[1];
		z = pos[2];

		EntityPlayer player = event.getPlayer();
		float interp = event.getPartialTicks();
		double dX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) interp;
		double dY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) interp;
		double dZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)interp;
		float exp = 0.002F;

		ForgeDirection rot = ForgeDirection.getOrientation(metadata(world, x, y, z) - offset).getRotation(ForgeDirection.UP);

		ICustomBlockHighlight.setup();
		for(AxisAlignedBB aabb : this.bounding) RenderGlobal.drawSelectionBoundingBox(getAABBRotationOffset(aabb.grow(exp, exp, exp), 0, 0, 0, rot).offset(x - dX + 0.5, y - dY, z - dZ + 0.5), 0F, 0F, 0F, 0.4F);
		ICustomBlockHighlight.cleanup();
	}

	@Override
	public NBTTagCompound getSettings(World world, int x, int y, int z) {
		int[] pos = findCore(world, x, y, z);
		TileEntity tile = world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));
		if (tile instanceof ICopiable)
			return ((ICopiable) tile).getSettings(world, pos[0], pos[1], pos[2]);
		else
			return null;
	}

	@Override
	public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, int x, int y, int z) {
		int[] pos = findCore(world, x, y, z);
		if(pos == null) return;
		TileEntity tile = world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));
		if (tile instanceof ICopiable)
			((ICopiable) tile).pasteSettings(nbt, index, world, player, pos[0], pos[1], pos[2]);
	}

	@Override
	public String[] infoForDisplay(World world, int x, int y, int z) {
		int[] pos = findCore(world, x, y, z);
		TileEntity tile = world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));
		if (tile instanceof ICopiable)
			return ((ICopiable) tile).infoForDisplay(world, x, y, z);
		return null;
	}

	@Override
	public int transformMeta(int meta, int coordBaseMode) {
		boolean isOffset = meta >= 12; // squishing causes issues
		boolean isExtra = !isOffset && meta >= extra;

		if(isOffset) {
			meta -= offset;
		} else if(isExtra) {
			meta -= extra;
		}

		meta = INBTBlockTransformable.transformMetaDeco(meta, coordBaseMode);

		if(isOffset) {
			meta += offset;
		} else if(isExtra) {
			meta += extra;
		}

		return meta;
	}

}
