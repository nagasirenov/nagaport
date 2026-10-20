package com.hbm.blocks.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.trait.CBT_Lights;

import java.util.Random;

import com.hbm.blocks.BlockEnums.LightType;
import com.hbm.blocks.ISpotlight;
import com.hbm.lib.RefStrings;
import com.hbm.world.gen.nbt.INBTBlockTransformable;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class Spotlight extends Block implements ISpotlight, INBTBlockTransformable {

	public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);

	public static final int META_YELLOW = 0;
	public static final int META_GREEN = 1;
	public static final int META_BLUE = 2;

	public static boolean disableOnGeneration = true;

	// I'd be extending the ReinforcedLamp class if it wasn't for the inverted behaviour of these specific lights
	// I want these blocks to be eminently useful, so removing the need for redstone by default is desired,
	// these act more like redstone torches, in that applying a signal turns them off
	public boolean isOn;

	public int beamLength;
	public LightType type;

	public Spotlight(Material mat, int beamLength, LightType type, boolean isOn) {
		super(mat);
		setDefaultState(blockState.getBaseState().withProperty(METADATA, 0));

		this.beamLength = beamLength;
		this.type = type;
		this.isOn = isOn;

		this.setHardness(0.5F);

		if(isOn) setLightLevel(1.0F);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return SpotlightModelState.create(this, METADATA);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(METADATA, meta & 15);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(METADATA);
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		return SpotlightModelState.extend(state, world, pos);
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	public ResourceLocation getModelResource() {
		switch(type) {
		case FLUORESCENT: return new ResourceLocation(RefStrings.MODID, "models/lights/fluorescent_lamp.obj");
		case HALOGEN: return new ResourceLocation(RefStrings.MODID, "models/lights/flood_lamp.obj");
		default: return new ResourceLocation(RefStrings.MODID, "models/lights/cage_lamp.obj");
		}
	}

	/** Atlas identifiers formerly assigned by the six registration texture setters. */
	public ResourceLocation getModelTexture() {
		String texture;
		switch(type) {
		case FLUORESCENT: texture = "fluorescent_lamp"; break;
		case HALOGEN: texture = "flood_lamp"; break;
		default: texture = "cage_lamp"; break;
		}
		return new ResourceLocation(RefStrings.MODID, "blocks/" + texture + (isOn ? "" : "_off"));
	}

	public String getPartName(int connectionCount) {
		switch(type) {
		case HALOGEN: return "FloodLamp";
		default: return "CageLamp";
		}
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	// Ah yes, I love methods named the literal opposite of what they do
	public boolean isPassable(IBlockAccess world, BlockPos pos) {
		return true;
	}

	@Override
	public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
        return MapColor.AIR;
    }

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return null;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		ForgeDirection dir = getDirection(world, pos.getX(), pos.getY(), pos.getZ());
		float[] bounds = swizzleBounds(dir);
		float[] offset = new float[] { 0.5F - dir.offsetX * (0.5F - bounds[0]), 0.5F - dir.offsetY * (0.5F - bounds[1]), 0.5F - dir.offsetZ * (0.5F - bounds[2]) };

		return new AxisAlignedBB(offset[0] - bounds[0], offset[1] - bounds[1], offset[2] - bounds[2], offset[0] + bounds[0], offset[1] + bounds[1], offset[2] + bounds[2]);
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) {
		return BlockFaceShape.UNDEFINED;
	}

	private float[] swizzleBounds(ForgeDirection dir) {
		float[] bounds = getBounds();
		switch(dir) {
		case EAST:
		case WEST: return new float[] { bounds[2], bounds[1], bounds[0] };
		case UP:
		case DOWN: return new float[] { bounds[1], bounds[2], bounds[0] };
		default: return bounds;
		}
	}

	// Returns an xyz (half-)size for a given object type
	private float[] getBounds() {
		switch(type) {
		case FLUORESCENT: return new float[] { 0.5F, 0.5F, 0.1F };
		case HALOGEN: return new float[] { 0.35F, 0.25F, 0.2F };
		default: return new float[] { 0.25F, 0.2F, 0.15F };
		}
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float hx, float hy, float hz, int initData, EntityLivingBase placer) {
		return getStateFromMeta(side.getIndex() << 1);
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		if(world.isRemote) return;
		if(updatePower(world, x, y, z)) return;
		updateBeam(world, x, y, z);
	}

	private boolean updatePower(World world, int x, int y, int z) {
		if(isBroken(metadata(world, x, y, z))) return false;

		boolean isPowered = world.isBlockPowered(new BlockPos(x, y, z));
		if(isOn && isPowered) {
			world.scheduleUpdate(new BlockPos(x, y, z), this, 4);
			return true;
		} else if(!isOn && !isPowered) {
			world.setBlockState(new BlockPos(x, y, z), getOn().getStateFromMeta(metadata(world, x, y, z)), 2);
			return true;
		}

		return false;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		ForgeDirection dir = getDirection(getMetaFromState(state));
		super.breakBlock(world, pos, state);

		if(world.isRemote) return;

		unpropagateBeam(world, x, y, z, dir);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		if (world.isRemote) return;

		if (isOn && world.isBlockPowered(new BlockPos(x, y, z))) {
			world.setBlockState(new BlockPos(x, y, z), getOff().getStateFromMeta(metadata(world, x, y, z)), 2);
		}
	}

	// Repropagate the beam if we've become unblocked
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		if(world.isRemote) return;
		if(neighborBlock instanceof SpotlightBeam) return;
		if(neighborBlock == Blocks.AIR) return;

		ForgeDirection dir = getDirection(world, x, y, z);

		if(!canPlace(world, x, y, z, dir)) {
			dropBlockAsItem(world, pos, getStateFromMeta(0), 0);
			world.setBlockToAir(new BlockPos(x, y, z));
			return;
		}

		if(updatePower(world, x, y, z)) return;

		updateBeam(world, x, y, z);
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		if(!super.canPlaceBlockOnSide(world, pos, side)) return false;

		ForgeDirection dir = ForgeDirection.fromEnumFacing(side);

		return canPlace(world, x, y, z, dir);
	}

	// BlockSlab doesn't actually properly return isSideSolid,
	// probably because MOJANK thought this would only ever be used for torches,
	// which can't be placed on ceilings...
	private boolean canPlace(World world, int x, int y, int z, ForgeDirection dir) {
		x -= dir.offsetX;
		y -= dir.offsetY;
		z -= dir.offsetZ;

		Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
		if(block instanceof BlockSlab) {
			int meta = metadata(world, x, y, z);
			return dir == ((meta & 8) == 8 ? ForgeDirection.UP : ForgeDirection.DOWN) || block.getDefaultState().isFullBlock();
		}

		return com.hbm.lib.FacingCompat.isBlockSideSolid(world, x, y, z, dir);
	}

	private void updateBeam(World world, int x, int y, int z) {
		if(!isOn) return;

		ForgeDirection dir = getDirection(world, x, y, z);
		propagateBeam(world, x, y, z, dir, beamLength, META_YELLOW);
	}

	private static int metadata(IBlockAccess world, int x, int y, int z) {
		IBlockState state = world.getBlockState(new BlockPos(x, y, z));
		return state.getBlock().getMetaFromState(state);
	}

	public ForgeDirection getDirection(IBlockAccess world, int x, int y, int z) {
		int metadata = metadata(world, x, y, z);
		return getDirection(metadata);
	}

	public ForgeDirection getDirection(int metadata) {
		return ForgeDirection.getOrientation(metadata >> 1);
	}

	// Replace bulbs on broken lights with a click
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		int meta = metadata(world, x, y, z);
		if(!isBroken(meta)) return false;

		repair(world, x, y, z);
		return true;
	}

	private void repair(World world, int x, int y, int z) {
		int meta = metadata(world, x, y, z);
		if(!isBroken(meta)) return;

		world.setBlockState(new BlockPos(x, y, z), getOn().getStateFromMeta(meta - 1), 2);

		if(!world.isRemote) {
			// gwa gwa
			CelestialBody body = CelestialBody.getBody(world);
			CBT_Lights lights = body.getTrait(CBT_Lights.class);

			if(lights == null) lights = new CBT_Lights();
			lights.addLight(getOn(), x, y, z);

			body.modifyTraits(lights);
		}

		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			int ox = x + dir.offsetX;
			int oy = y + dir.offsetY;
			int oz = z + dir.offsetZ;
			Block block = world.getBlockState(new BlockPos(ox, oy, oz)).getBlock();
			if(block == this) repair(world, ox, oy, oz);
		}
	}

	public boolean isBroken(int metadata) {
		return (metadata & 1) == 1;
	}

	@Override
	public Item getItemDropped(IBlockState state, Random r, int fortune) {
		return Item.getItemFromBlock(getOn());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
		return new ItemStack(getOn());
	}

	@Override
	protected ItemStack getSilkTouchDrop(IBlockState state) {
		return new ItemStack(getOn());
	}

	// Recursively add beam blocks, updating any that already exist with new incoming light directions
	public static void propagateBeam(World world, int x, int y, int z, ForgeDirection dir, int distance, int meta) {
		distance--;
		if(distance <= 0)
			return;

		x += dir.offsetX;
		y += dir.offsetY;
		z += dir.offsetZ;

		Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
		if(!block.isAir(world.getBlockState(new BlockPos(x, y, z)), world, new BlockPos(x, y, z)))
			return;

		if(!(block instanceof SpotlightBeam)) {
			world.setBlockState(new BlockPos(x, y, z), ModBlocks.spotlight_beam.getStateFromMeta(meta), 3);
		}

		// If we encounter an existing beam, add a new INCOMING direction to the
		// metadata, and cancel propagation if something goes wrong
		if (SpotlightBeam.setDirection(world, x, y, z, dir, true) == 0)
			return;

		propagateBeam(world, x, y, z, dir, distance, meta);
	}

	// Recursively delete beam blocks, if they aren't still illuminated from a different direction
	public static void unpropagateBeam(World world, int x, int y, int z, ForgeDirection dir) {
		x += dir.offsetX;
		y += dir.offsetY;
		z += dir.offsetZ;

		Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
		if(!(block instanceof SpotlightBeam))
			return;

		// Remove the metadata associated with this direction
		// If all directions are set to zero, delete the beam
		if(SpotlightBeam.setDirection(world, x, y, z, dir, false) == 0) {
			world.setBlockToAir(new BlockPos(x, y, z));
		}

		unpropagateBeam(world, x, y, z, dir);
	}

	// Travels back through a beam to the source, and if found, repropagates the beam
	public static void backPropagate(World world, int x, int y, int z, ForgeDirection dir, int meta) {
		x -= dir.offsetX;
		y -= dir.offsetY;
		z -= dir.offsetZ;

		Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
		if(block instanceof ISpotlight) {
			ISpotlight spot = (ISpotlight) block;
			propagateBeam(world, x, y, z, dir, spot.getBeamLength(), meta);
		} else if(!(block instanceof SpotlightBeam)) {
			return;
		}

		backPropagate(world, x, y, z, dir, meta);
	}

	protected Block getOff() {
		if(this == ModBlocks.spotlight_incandescent) return ModBlocks.spotlight_incandescent_off;
		if(this == ModBlocks.spotlight_fluoro) return ModBlocks.spotlight_fluoro_off;
		if(this == ModBlocks.spotlight_halogen) return ModBlocks.spotlight_halogen_off;

		return this;
	}

	protected Block getOn() {
		if(this == ModBlocks.spotlight_incandescent_off) return ModBlocks.spotlight_incandescent;
		if(this == ModBlocks.spotlight_fluoro_off) return ModBlocks.spotlight_fluoro;
		if(this == ModBlocks.spotlight_halogen_off) return ModBlocks.spotlight_halogen;

		return this;
	}

	@Override
	public int getBeamLength() {
		return this.beamLength;
	}

	@Override
	public int transformMeta(int meta, int coordBaseMode) {
		// +1 to set as broken, won't turn on until broken and replaced
		int disabled = disableOnGeneration ? 1 : 0;
		return (INBTBlockTransformable.transformMetaDeco(meta >> 1, coordBaseMode) << 1) + disabled;
	}

	@Override
	public Block transformBlock(Block block) {
		if(!disableOnGeneration) return block;
		if(block == ModBlocks.spotlight_incandescent) return ModBlocks.spotlight_incandescent_off;
		if(block == ModBlocks.spotlight_fluoro) return ModBlocks.spotlight_fluoro_off;
		if(block == ModBlocks.spotlight_halogen) return ModBlocks.spotlight_halogen_off;
		return block;
	}

}
