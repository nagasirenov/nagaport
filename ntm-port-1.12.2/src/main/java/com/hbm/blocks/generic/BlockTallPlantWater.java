package com.hbm.blocks.generic;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import com.hbm.blocks.BlockEnumMulti;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockNTMFlower.EnumFlowerType;
import com.hbm.blocks.generic.BlockTallPlant.EnumTallFlower;
import com.hbm.inventory.OreDictManager.DictFrame;
import com.hbm.items.ModItems;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;

public class BlockTallPlantWater extends BlockEnumMulti implements IPlantable, IGrowable {

	//ngl i dont know why i did this....
	@Override
	public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
		return EnumPlantType.Water;
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

	public BlockTallPlantWater() {
		super(Material.WATER, EnumTallPlantWater.class, true, true);
		this.setTickRandomly(true);
	}

	public static enum EnumTallPlantWater {
		LAYTHE;
	}

	protected IIcon[] bottomIcons;

	@SuppressWarnings("rawtypes")
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {

		Enum[] enums = theEnum.getEnumConstants();
		this.icons = new IIcon[enums.length];
		this.bottomIcons = new IIcon[enums.length];

		for(int i = 0; i < icons.length; i++) {
			Enum num = enums[i];
			this.icons[i] = reg.registerIcon(this.getRegistryName().toString() + "." + num.name().toLowerCase(Locale.US) + ".upper");
			this.bottomIcons[i] = reg.registerIcon(this.getRegistryName().toString() + "." + num.name().toLowerCase(Locale.US) + ".lower");
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return meta > 7 ? this.icons[meta % this.icons.length] : this.bottomIcons[meta % this.icons.length];
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
		return 6;
	}

	public boolean canReplace(World world, int x, int y, int z, int side, ItemStack itemStack) {
		return com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() == Blocks.WATER && this.canBlockStay(world, x, y, z);
	}

	@Override
	public boolean canPlaceBlockAt(World world, net.minecraft.util.math.BlockPos nativePlacementPos) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		return super.canPlaceBlockAt(world, new net.minecraft.util.math.BlockPos(x, y, z)) && this.canBlockStay(world, x, y, z) && com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock().getDefaultState().getMaterial().isLiquid();
	}

	protected boolean canPlaceBlockOn(Block block) {
		return block == ModBlocks.laythe_silt;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		super.neighborChanged(state, world, pos, block, fromPos);
		this.checkAndDropBlock(world, pos.getX(), pos.getY(), pos.getZ());
	}

	public static boolean detectCut = true;
	protected void checkAndDropBlock(World world, int x, int y, int z) {
		if(!this.canBlockStay(world, x, y, z)) {
			if(com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) < 8) {
				this.dropBlockAsItem(world, new net.minecraft.util.math.BlockPos(x, y, z), world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)), 0);
			}
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.WATER; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		}

		if(!detectCut) return;

		Block block = com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock();
		int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y + 1, z) );
		int ownMeta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );

		if(ownMeta < 8 && (meta != ownMeta + 8 || block != this) && ((BlockNTMFlower) ModBlocks.plant_flower).canBlockStay(world, x, y, z)) {

			if(ownMeta == EnumTallPlantWater.LAYTHE.ordinal())
				{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.WATER; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			else
				com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, ModBlocks.plant_flower, EnumFlowerType.CD0.ordinal(), 3);
		}

	}


	public boolean canBlockStay(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {
		return canBlockStay(worldIn, pos.getX(), pos.getY(), pos.getZ());
	}

public boolean canBlockStay(World world, int x, int y, int z) {
		int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );

		if(meta > 7) {
			return com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() == this && com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y - 1, z) ) == meta - 8;
		}

		return canPlaceBlockOn(com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock());
	}

	public int getDamageValue(World world, int x, int y, int z) {
		return com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) % 8;
	}


		@Override
	public void onBlockHarvested(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, net.minecraft.entity.player.EntityPlayer player) {
		int meta = state.getBlock().getMetaFromState(state);
		onBlockHarvested(worldIn, pos.getX(), pos.getY(), pos.getZ(), meta, player);
	}

public void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player) {
		// Check if the top part of the plant is being broken
		if (meta > 7) {
			// Set the bottom part to air
			if (com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() == this) {
				com.hbm.world.WorldAirRemoval.remove(world, x, y - 1, z);
			}
		} else {
			// If the bottom part is being broken, check for the top part and set it to air
			if (com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() == this) {
				com.hbm.world.WorldAirRemoval.remove(world, x, y + 1, z);
			}

		}
		if(player.capabilities.isCreativeMode) {
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y + 1; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 2; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		} else {
			this.dropBlockAsItem(world, new net.minecraft.util.math.BlockPos(x, y + 1, z), world.getBlockState(new net.minecraft.util.math.BlockPos(x, y + 1, z)), 0);
		}

		super.onBlockHarvested(world, new net.minecraft.util.math.BlockPos(x, y, z), world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)), player);
	}
	@Override
	public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos nativePlacementPos, net.minecraft.block.state.IBlockState nativePlacementState, EntityLivingBase entity, ItemStack stack) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y + 1, z, this, stack.getItemDamage() + 8, 2);
	}

	@Override
	public void updateTick(World world, net.minecraft.util.math.BlockPos nativeTickPos, net.minecraft.block.state.IBlockState nativeTickState, Random rand) {
		int x = nativeTickPos.getX(), y = nativeTickPos.getY(), z = nativeTickPos.getZ();

	}

	/* grow condition */
	public boolean func_149851_a(World world, int x, int y, int z, boolean b) {
		return false;
	}

	/* chance */
	public boolean func_149852_a(World world, Random rand, int x, int y, int z) {
		int meta = rectify(com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ));
		if(meta == EnumTallFlower.CD3.ordinal()) {
			return true;
		}
		return rand.nextFloat() < 0.33F;
	}

	/* grow */
	public void func_149853_b(World world, Random rand, int x, int y, int z) {

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
	public int quantityDropped(int parMetadata, int parFortune, Random parRand) {
	   return (parRand.nextInt(4));
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState parMetadata, Random parRand, int parFortune) {
	   return (ModItems.saltleaf);
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

		if(metadata == EnumTallFlower.CD4.ordinal() + 8) {
			ret.add(DictFrame.fromOne(ModItems.plant_item, com.hbm.items.ItemEnums.EnumPlantType.MUSTARDWILLOW, 3 + world.rand.nextInt(4)));
		}

		return ret;
	}
}
