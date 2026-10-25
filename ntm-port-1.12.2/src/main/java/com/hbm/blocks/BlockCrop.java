package com.hbm.blocks;

import net.minecraft.block.state.IBlockState;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Predicate;

import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.handler.atmosphere.IPlantableBreathing;
import com.hbm.items.ItemEnums.EnumTarType;
import com.hbm.items.ModItems;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import com.hbm.render.icon.IIcon;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BlockCrop extends BlockBush implements IGrowable, IPlantableBreathing {
	public BlockCrop withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return this;
	}


	private final net.minecraft.util.math.AxisAlignedBB shape;

	protected int maxGrowthStage = 7;
	protected Block soilBlock;
	private String textureName;

	@SideOnly(Side.CLIENT)
	protected IIcon[] blockIcons;

	private Predicate<CBT_Atmosphere> atmospherePredicate;

	public boolean canHydro;

	public BlockCrop(Block block, Predicate<CBT_Atmosphere> atmospherePredicate, boolean canHydro) {
		setTickRandomly(true);
		float f = 0.5F;
		this.shape = new net.minecraft.util.math.AxisAlignedBB(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 0.25F, 0.5F + f);
		setHardness(0.0F);
		setSoundType(net.minecraft.block.SoundType.PLANT);
		disableStats();

		this.soilBlock = block;
		this.atmospherePredicate = atmospherePredicate;
		this.canHydro = canHydro;
	}

	public BlockCrop setBlockTextureName(String texture) {
		this.textureName = texture;
		return this;
	}

	@Override
	public boolean canBreathe(CBT_Atmosphere atmosphere) {
	    if(atmosphere == null) {
	        return false;
	    }
	    return this.atmospherePredicate.test(atmosphere);
	}

	/**
	 * is the block grass, dirt or farmland
	 */
	protected boolean canPlaceBlockOn(Block block) {
		return block == this.soilBlock;
	}

	public void incrementGrowStage(World world, Random rand, int x, int y, int z) {
		int growStage = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) + MathHelper.getInt(rand, 2, 5);

		if(growStage > maxGrowthStage) {
			growStage = maxGrowthStage;
		}

		com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, growStage, 2);
	}

	public Item getItemDropped(int meta, Random rand, int fortune) {
		if(this == ModBlocks.crop_strawberry) {
			return ModItems.strawberry;
		}

		if(this == ModBlocks.crop_mint) {
			return ModItems.mint_leaves;
		}

		if(this == ModBlocks.crop_coffee) {
			return ModItems.bean_raw;
		}
		if(this == ModBlocks.crop_tea) {
			return meta == 7 ? ModItems.tea_leaf : ModItems.teaseeds;
		}
		if(this == ModBlocks.crop_paraffin) {
			return ModItems.paraffin_seeds;
		}

		return Item.getItemFromBlock(this);
	}

	/**
	 * The type of render function that is called for this block
	 */
	public int getRenderType() {
		return 1; // Cross like flowers
	}

	/**
	 * Gets the block's texture. Args: side, meta
	 */
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int growthStage) {
		return blockIcons[growthStage];
	}

	protected void checkAndDropBlock(World world, int x, int y, int z) {
		if(!this.canBlockStay(world, x, y, z)) {
			this.dropBlockAsItem(world, new net.minecraft.util.math.BlockPos(x, y, z), world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)), 0);
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, getBlockById(0), 0, 2);
		}
	}


		@Override
	public boolean canBlockStay(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {
		return canBlockStay(worldIn, pos.getX(), pos.getY(), pos.getZ());
	}

public boolean canBlockStay(World world, int x, int y, int z) {
		Block soil = com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock();
		return soil.canSustainPlant(soil.getDefaultState(), world, new BlockPos(x, y - 1, z), EnumFacing.UP, this);
	}

	/*
	 * Need to implement the IGrowable interface methods
	 */
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
	public void updateTick(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, java.util.Random rand) {
		updateTick(worldIn, pos.getX(), pos.getY(), pos.getZ(), rand);
	}

public void updateTick(World world, int x, int y, int z, Random rand) {
		super.updateTick(world, new net.minecraft.util.math.BlockPos(x, y, z), world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)), rand);
		int growStage = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) + 1;

		if(growStage > 7) {
			growStage = 7;
		}

		com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, growStage, 2);
	}

	// checks if finished growing (a grow stage of 7 is final stage)
	public boolean func_149851_a(World world, int x, int y, int z, boolean p_149851_5_) {
		return com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) != 7;
	}

	public boolean func_149852_a(World world, Random rand, int x, int y, int z) {
		return true;
	}

	public void func_149853_b(World world, Random rand, int x, int y, int z) {
		incrementGrowStage(world, rand, x, y, z);
	}

	public int quantityDropped(int meta, int fortune, Random rand) {
		if(meta == 7) { //dividing is probably better, but thats the point?? plus i want players to fully grow their crops
			return 4;
		} else {
			return meta / 2;
		}

	}


		@Override
	public void getDrops(net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> drops, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, int fortune) {
		int meta = state.getBlock().getMetaFromState(state);
		net.minecraft.world.World w = world instanceof net.minecraft.world.World ? (net.minecraft.world.World) world : null;
		java.util.ArrayList<net.minecraft.item.ItemStack> ret = getDrops(w, pos.getX(), pos.getY(), pos.getZ(), meta, fortune);
		if(ret != null) drops.addAll(ret);
	}

public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		net.minecraft.util.NonNullList<ItemStack> nativeDrops = net.minecraft.util.NonNullList.create();
		super.getDrops(nativeDrops, world, new net.minecraft.util.math.BlockPos(x, y, z), this.getStateFromMeta(metadata), fortune);
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>(nativeDrops);

		// Adds seeds to the drops
		if(this == ModBlocks.crop_tea && metadata >= 7) {
			for(int i = 0; i < 3 + fortune; ++i) {
				if(world.rand.nextInt(15) <= metadata) {
					ret.add(new ItemStack(ModItems.teaseeds, 1, 0));
				}
			}
		}

		if(this == ModBlocks.crop_paraffin && metadata >= 7) {
			for(int i = 0; i < 3 + fortune; ++i) {
				if(world.rand.nextInt(15) <= metadata) {
					ret.add(new ItemStack(ModItems.paraffin_seeds));
					ret.add(new ItemStack(ModItems.oil_tar, 1, EnumTarType.WAX.ordinal()));
				}
			}
		}

		return ret;
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister parIIconRegister) {
		blockIcons = new IIcon[maxGrowthStage+2];
		// seems that crops like to have 8 growth icons, but okay to repeat actual texture if you want
		// to make generic should loop to maxGrowthStage
		blockIcons[0] = parIIconRegister.registerIcon(textureName + "_1");
		blockIcons[1] = parIIconRegister.registerIcon(textureName + "_1");
		blockIcons[2] = parIIconRegister.registerIcon(textureName + "_2");
		blockIcons[3] = parIIconRegister.registerIcon(textureName + "_2");
		blockIcons[4] = parIIconRegister.registerIcon(textureName + "_3");
		blockIcons[5] = parIIconRegister.registerIcon(textureName + "_3");
		blockIcons[6] = parIIconRegister.registerIcon(textureName + "_4");
		blockIcons[7] = parIIconRegister.registerIcon(textureName + "_5");
		blockIcons[8] = parIIconRegister.registerIcon(textureName + "_5");
	}



	public net.minecraft.util.math.AxisAlignedBB getBoundingBox(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos) {
		return shape;
	}
}
