package com.hbm.blocks.generic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.hbm.blocks.BlockEnumMulti;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockDeadPlant.EnumDeadPlantType;
import com.hbm.blocks.generic.BlockTallPlant.EnumTallFlower;
import com.hbm.items.ModItems;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;

public class BlockNTMFlower extends BlockEnumMulti implements IPlantable, IGrowable, ITooltipProvider {

	// Keep the complete legacy metadata nibble, including unused values from saves.
	public static final PropertyInteger VARIANT = PropertyInteger.create("variant", 0, 15);

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, VARIANT);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(VARIANT, meta & 15);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(VARIANT);
	}

	@Override
	public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
		return EnumPlantType.Plains;
	}

	@Override
	public IBlockState getPlant(IBlockAccess world, BlockPos pos) {
		return world.getBlockState(pos);
	}

	@Override
	public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
		return func_149851_a(worldIn, pos.getX(), pos.getY(), pos.getZ(), isClient);
	}

	@Override
	public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
		return func_149852_a(worldIn, rand, pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
		func_149853_b(worldIn, rand, pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return NULL_AABB;
	}

	public BlockNTMFlower() {
		super(Material.PLANTS, EnumFlowerType.class, true, true);
		setDefaultState(blockState.getBaseState().withProperty(VARIANT, 0));
		this.setTickRandomly(true);
	}

	public static enum EnumFlowerType {
		FOXGLOVE(false),
		TOBACCO(false),
		NIGHTSHADE(false),
		WEED(false),
		CD0(true),
		CD1(true),
		STRAWBERRY(false),
		MINT(false);

		public boolean needsOil;
		private EnumFlowerType(boolean needsOil) {
			this.needsOil = needsOil;
		}
	}

	public EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z) {
		return EnumPlantType.Plains;
	}

	public Block getPlant(IBlockAccess world, int x, int y, int z) {
		return this;
	}

	public int getPlantMetadata(IBlockAccess world, int x, int y, int z) {
		return com.hbm.world.WorldBlockState.readMetadata(world, x, y, z);
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos) {
		return super.canPlaceBlockAt(world, pos) && this.canBlockStay(world, pos.getX(), pos.getY(), pos.getZ());
	}

	protected boolean canPlaceBlockOn(Block block) {
		return block == Blocks.GRASS || block == Blocks.DIRT || block == Blocks.FARMLAND || block == ModBlocks.dirt_dead || block == ModBlocks.dirt_oily;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		super.neighborChanged(state, world, pos, block, fromPos);
		this.checkAndDropBlock(world, pos.getX(), pos.getY(), pos.getZ());
	}

	protected void checkAndDropBlock(World world, int x, int y, int z) {
		if(!this.canBlockStay(world, x, y, z)) {
			BlockPos pos = new BlockPos(x, y, z);
			this.dropBlockAsItem(world, pos, world.getBlockState(pos), 0);
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
		}
	}

	// Legacy callers still use coordinates; placement routes through this same rule.
	public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state) {
		return canBlockStay(worldIn, pos.getX(), pos.getY(), pos.getZ());
	}

	public boolean canBlockStay(World world, int x, int y, int z) {
		return canPlaceBlockOn(world.getBlockState(new BlockPos(x, y - 1, z)).getBlock());
	}

	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return null;
	}

	@Override
	public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	public int getRenderType() {
		return 1;
	}

	@Override
	public int damageDropped(int meta) {
		if(meta == EnumFlowerType.CD1.ordinal()) {
			return EnumFlowerType.CD0.ordinal();
		}

		return meta;
	}

	@Override
	public void updateTick(World world, net.minecraft.util.math.BlockPos nativeTickPos, net.minecraft.block.state.IBlockState nativeTickState, Random rand) {
		int x = nativeTickPos.getX(), y = nativeTickPos.getY(), z = nativeTickPos.getZ();

		if(world.isRemote) return; //not possible i believe, but better safe than sorry

		int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
		EnumFlowerType type = EnumFlowerType.values()[rectify(meta)];

		if(!(type == EnumFlowerType.WEED || type == EnumFlowerType.CD0 || type == EnumFlowerType.CD1)) return;

		if(func_149851_a(world, x, y, z, false) && func_149852_a(world, rand, x, y, z) && rand.nextInt(3) == 0) {
			func_149853_b(world, rand, x, y, z);
		}
	}

	/* grow condition */
	public boolean func_149851_a(World world, int x, int y, int z, boolean b) {

		int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );

		//cadmium willows can only grow with water
		if(meta == EnumFlowerType.CD0.ordinal() || meta == EnumFlowerType.CD1.ordinal()) {

			if(com.hbm.world.WorldBlockState.read(world, x + 1, y - 1, z).getBlock().getDefaultState().getMaterial() != Material.WATER &&
					com.hbm.world.WorldBlockState.read(world, x - 1, y - 1, z).getBlock().getDefaultState().getMaterial() != Material.WATER &&
					com.hbm.world.WorldBlockState.read(world, x, y - 1, z + 1).getBlock().getDefaultState().getMaterial() != Material.WATER &&
					com.hbm.world.WorldBlockState.read(world, x, y - 1, z - 1).getBlock().getDefaultState().getMaterial() != Material.WATER) {
				return false;
			}
		}

		if(meta == EnumFlowerType.WEED.ordinal() ||  meta == EnumFlowerType.CD1.ordinal()) {
			return world.isAirBlock(new net.minecraft.util.math.BlockPos(x, y + 1, z));
		}
		return true;
	}

	/* chance */
	public boolean func_149852_a(World world, Random rand, int x, int y, int z) {

		int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );

		if(meta == EnumFlowerType.WEED.ordinal() || meta == EnumFlowerType.CD0.ordinal() || meta == EnumFlowerType.CD1.ordinal()) {
			return rand.nextFloat() < 0.33F;
		}

		return true;
	}

	/* grow */
	public void func_149853_b(World world, Random rand, int x, int y, int z) {

		int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
		Block onTop = com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock();

		if(meta == EnumFlowerType.WEED.ordinal()) {
			if(onTop == ModBlocks.dirt_dead || onTop == ModBlocks.dirt_oily) {
				{ net.minecraft.world.World nativeRegisteredWriteWorld = world; int nativeRegisteredWriteX = x; int nativeRegisteredWriteY = y; int nativeRegisteredWriteZ = z; net.minecraft.block.Block nativeRegisteredWriteBlock = ModBlocks.plant_dead; int nativeRegisteredWriteMeta = EnumDeadPlantType.GENERIC.ordinal(); int nativeRegisteredWriteFlags = 3; if (nativeRegisteredWriteWorld == null) throw new NullPointerException(); if (nativeRegisteredWriteX >= -30000000 && nativeRegisteredWriteX < 30000000 && nativeRegisteredWriteZ >= -30000000 && nativeRegisteredWriteZ < 30000000 && nativeRegisteredWriteY >= 0 && nativeRegisteredWriteY < 256) com.hbm.world.WorldBlockState.write(nativeRegisteredWriteWorld, nativeRegisteredWriteX, nativeRegisteredWriteY, nativeRegisteredWriteZ, nativeRegisteredWriteBlock.getStateFromMeta(nativeRegisteredWriteMeta), nativeRegisteredWriteFlags); }
				return;
			}
		}

		if(meta == EnumFlowerType.WEED.ordinal()) {
			{ net.minecraft.world.World nativeRegisteredWriteWorld = world; int nativeRegisteredWriteX = x; int nativeRegisteredWriteY = y; int nativeRegisteredWriteZ = z; net.minecraft.block.Block nativeRegisteredWriteBlock = ModBlocks.plant_tall; int nativeRegisteredWriteMeta = EnumTallFlower.WEED.ordinal(); int nativeRegisteredWriteFlags = 3; if (nativeRegisteredWriteWorld == null) throw new NullPointerException(); if (nativeRegisteredWriteX >= -30000000 && nativeRegisteredWriteX < 30000000 && nativeRegisteredWriteZ >= -30000000 && nativeRegisteredWriteZ < 30000000 && nativeRegisteredWriteY >= 0 && nativeRegisteredWriteY < 256) com.hbm.world.WorldBlockState.write(nativeRegisteredWriteWorld, nativeRegisteredWriteX, nativeRegisteredWriteY, nativeRegisteredWriteZ, nativeRegisteredWriteBlock.getStateFromMeta(nativeRegisteredWriteMeta), nativeRegisteredWriteFlags); }
			{ net.minecraft.world.World nativeRegisteredWriteWorld = world; int nativeRegisteredWriteX = x; int nativeRegisteredWriteY = y + 1; int nativeRegisteredWriteZ = z; net.minecraft.block.Block nativeRegisteredWriteBlock = ModBlocks.plant_tall; int nativeRegisteredWriteMeta = EnumTallFlower.WEED.ordinal() + 8; int nativeRegisteredWriteFlags = 3; if (nativeRegisteredWriteWorld == null) throw new NullPointerException(); if (nativeRegisteredWriteX >= -30000000 && nativeRegisteredWriteX < 30000000 && nativeRegisteredWriteZ >= -30000000 && nativeRegisteredWriteZ < 30000000 && nativeRegisteredWriteY >= 0 && nativeRegisteredWriteY < 256) com.hbm.world.WorldBlockState.write(nativeRegisteredWriteWorld, nativeRegisteredWriteX, nativeRegisteredWriteY, nativeRegisteredWriteZ, nativeRegisteredWriteBlock.getStateFromMeta(nativeRegisteredWriteMeta), nativeRegisteredWriteFlags); }
			return;
		}

		if(meta == EnumFlowerType.CD0.ordinal()) {
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, ModBlocks.plant_flower, EnumFlowerType.CD1.ordinal(), 3);
			return;
		}

		if(meta == EnumFlowerType.CD1.ordinal()) {
			{ net.minecraft.world.World nativeRegisteredWriteWorld = world; int nativeRegisteredWriteX = x; int nativeRegisteredWriteY = y; int nativeRegisteredWriteZ = z; net.minecraft.block.Block nativeRegisteredWriteBlock = ModBlocks.plant_tall; int nativeRegisteredWriteMeta = EnumTallFlower.CD2.ordinal(); int nativeRegisteredWriteFlags = 3; if (nativeRegisteredWriteWorld == null) throw new NullPointerException(); if (nativeRegisteredWriteX >= -30000000 && nativeRegisteredWriteX < 30000000 && nativeRegisteredWriteZ >= -30000000 && nativeRegisteredWriteZ < 30000000 && nativeRegisteredWriteY >= 0 && nativeRegisteredWriteY < 256) com.hbm.world.WorldBlockState.write(nativeRegisteredWriteWorld, nativeRegisteredWriteX, nativeRegisteredWriteY, nativeRegisteredWriteZ, nativeRegisteredWriteBlock.getStateFromMeta(nativeRegisteredWriteMeta), nativeRegisteredWriteFlags); }
			{ net.minecraft.world.World nativeRegisteredWriteWorld = world; int nativeRegisteredWriteX = x; int nativeRegisteredWriteY = y + 1; int nativeRegisteredWriteZ = z; net.minecraft.block.Block nativeRegisteredWriteBlock = ModBlocks.plant_tall; int nativeRegisteredWriteMeta = EnumTallFlower.CD2.ordinal() + 8; int nativeRegisteredWriteFlags = 3; if (nativeRegisteredWriteWorld == null) throw new NullPointerException(); if (nativeRegisteredWriteX >= -30000000 && nativeRegisteredWriteX < 30000000 && nativeRegisteredWriteZ >= -30000000 && nativeRegisteredWriteZ < 30000000 && nativeRegisteredWriteY >= 0 && nativeRegisteredWriteY < 256) com.hbm.world.WorldBlockState.write(nativeRegisteredWriteWorld, nativeRegisteredWriteX, nativeRegisteredWriteY, nativeRegisteredWriteZ, nativeRegisteredWriteBlock.getStateFromMeta(nativeRegisteredWriteMeta), nativeRegisteredWriteFlags); }
			return;
		}

		this.dropBlockAsItem(world, new net.minecraft.util.math.BlockPos(x, y, z), world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)), 0);
	}



    @SideOnly(Side.CLIENT)
    public int getRenderColor(int meta)
    {
	if (meta == 1 || meta == 3) {
		return ColorizerGrass.getGrassColor(0.5D, 1.0D);
        } else return 0xFFFFFF;
    }
    // if you need to make another tinted plant just throw the metadata value
    // into the if statements above and below i really do not want to make this more
    // complicated than it needs to be

    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess world, int x, int y, int z)
    {
        int meta = com.hbm.world.WorldBlockState.readMetadata(world, x, y, z);
        if (meta == 1 || meta == 3) {
	return net.minecraft.world.biome.BiomeColorHelper.getFoliageColorAtPos(world, new BlockPos(x, y, z));
        } else return 0xFFFFFF;
    }


	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) { }

	public Item getItemDropped(int meta, Random rand, int j) {
		if(meta == EnumFlowerType.STRAWBERRY.ordinal()) {
			return ModItems.strawberry;
		}

		if(meta == EnumFlowerType.MINT.ordinal()) {
			return ModItems.mint_leaves;
		}

		return super.getItemDropped(this.getStateFromMeta(meta), rand, j);
	}



		@Override
	public void getDrops(net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> drops, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, int fortune) {
		int meta = state.getBlock().getMetaFromState(state);
		net.minecraft.world.World w = world instanceof net.minecraft.world.World ? (net.minecraft.world.World) world : null;
		java.util.ArrayList<net.minecraft.item.ItemStack> ret = getDrops(w, pos.getX(), pos.getY(), pos.getZ(), meta, fortune);
		if(ret != null) drops.addAll(ret);
	}

public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		if(metadata == EnumFlowerType.STRAWBERRY.ordinal()) return ModBlocks.getDropsWithoutDamage(world, this, metadata, fortune);
		net.minecraft.util.NonNullList<ItemStack> nativeDrops = net.minecraft.util.NonNullList.create();
		super.getDrops(nativeDrops, world, new net.minecraft.util.math.BlockPos(x, y, z), this.getStateFromMeta(metadata), fortune);
		return new ArrayList<ItemStack>(nativeDrops);
	}

}
