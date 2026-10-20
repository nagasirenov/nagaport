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
			this.icons[i] = reg.registerIcon(this.getTextureName() + "." + num.name().toLowerCase(Locale.US) + ".upper");
			this.bottomIcons[i] = reg.registerIcon(this.getTextureName() + "." + num.name().toLowerCase(Locale.US) + ".lower");
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return meta > 7 ? this.icons[meta % this.icons.length] : this.bottomIcons[meta % this.icons.length];
	}

	@Override
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

	@Override
	public int getRenderType() {
		return 6;
	}

	@Override
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
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		super.onNeighborBlockChange(world, x, y, z, block);
		this.checkAndDropBlock(world, x, y, z);
	}
	
	public static boolean detectCut = true;
	protected void checkAndDropBlock(World world, int x, int y, int z) {
		if(!this.canBlockStay(world, x, y, z)) {
			if(com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) < 8) {
				this.dropBlockAsItem(world, x, y, z, com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ), 0);
			}
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.WATER; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		}
		
		if(!detectCut) return;
		
		Block block = com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock();
		int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y + 1, z) );
		int ownMeta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
		
		if(ownMeta < 8 && (meta != ownMeta + 8 || block != this) && ModBlocks.plant_flower.canBlockStay(world, x, y, z)) {

			if(ownMeta == EnumTallPlantWater.LAYTHE.ordinal())
				{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.WATER; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			else
				world.setBlock(x, y, z, ModBlocks.plant_flower, EnumFlowerType.CD0.ordinal(), 3);
		}
		
	}

	@Override
	public boolean canBlockStay(World world, int x, int y, int z) {
		int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
		
		if(meta > 7) {
			return com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() == this && com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y - 1, z) ) == meta - 8;
		}
		
		return canPlaceBlockOn(com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock());
	}
	
	@Override
	public int getDamageValue(World world, int x, int y, int z) {
		return com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) % 8;
	}
	
	@Override
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
			this.dropBlockAsItem(world, x, y + 1, z, com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y + 1, z) ), 0);
		}

		super.onBlockHarvested(world, x, y, z, meta, player);
	}
	@Override
	public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos nativePlacementPos, net.minecraft.block.state.IBlockState nativePlacementState, EntityLivingBase entity, ItemStack stack) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		world.setBlock(x, y + 1, z, this, stack.getItemDamage() + 8, 2);
	}

	@Override
	public void updateTick(World world, net.minecraft.util.math.BlockPos nativeTickPos, net.minecraft.block.state.IBlockState nativeTickState, Random rand) {
		int x = nativeTickPos.getX(), y = nativeTickPos.getY(), z = nativeTickPos.getZ();

	}

	/* grow condition */
	@Override
	public boolean func_149851_a(World world, int x, int y, int z, boolean b) {
		return false;
	}

	/* chance */
	@Override
	public boolean func_149852_a(World world, Random rand, int x, int y, int z) {
		int meta = rectify(com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ));
		if(meta == EnumTallFlower.CD3.ordinal()) {
			return true;
		}
		return rand.nextFloat() < 0.33F;
	}

	/* grow */
	@Override
	public void func_149853_b(World world, Random rand, int x, int y, int z) {

	}

	@Override
	public EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z) {
		return EnumPlantType.Plains;
	}

	@Override
	public Block getPlant(IBlockAccess world, int x, int y, int z) {
		return this;
	}

	@Override
	public int getPlantMetadata(IBlockAccess world, int x, int y, int z) {
		return world.getBlockMetadata(x, y, z);
	}
	@Override
	public int quantityDropped(int parMetadata, int parFortune, Random parRand) {
	   return (parRand.nextInt(4));
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState parMetadata, Random parRand, int parFortune) {
	   return (ModItems.saltleaf);
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		ArrayList<ItemStack> ret = super.getDrops(world, x, y, z, metadata, fortune);
		
		if(metadata == EnumTallFlower.CD4.ordinal() + 8) {
			ret.add(DictFrame.fromOne(ModItems.plant_item, com.hbm.items.ItemEnums.EnumPlantType.MUSTARDWILLOW, 3 + world.rand.nextInt(4)));
		}
		
		return ret;
	}
}
