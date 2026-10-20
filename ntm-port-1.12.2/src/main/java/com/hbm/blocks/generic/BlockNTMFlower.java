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
	public boolean canBlockStay(World world, int x, int y, int z) {
		return canPlaceBlockOn(world.getBlockState(new BlockPos(x, y - 1, z)).getBlock());
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return null;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}
	
	@Override
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
	public void updateTick(World world, int x, int y, int z, Random rand) {

		if(world.isRemote) return; //not possible i believe, but better safe than sorry
		
		int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
		EnumFlowerType type = EnumFlowerType.values()[rectify(meta)];
		
		if(!(type == EnumFlowerType.WEED || type == EnumFlowerType.CD0 || type == EnumFlowerType.CD1)) return;
		
		if(func_149851_a(world, x, y, z, false) && func_149852_a(world, rand, x, y, z) && rand.nextInt(3) == 0) {
			func_149853_b(world, rand, x, y, z);
		}
	}

	/* grow condition */
	@Override
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
			return world.isAirBlock(x, y + 1, z);
		}
		return true;
	}

	/* chance */
	@Override
	public boolean func_149852_a(World world, Random rand, int x, int y, int z) {
		
		int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
		
		if(meta == EnumFlowerType.WEED.ordinal() || meta == EnumFlowerType.CD0.ordinal() || meta == EnumFlowerType.CD1.ordinal()) {
			return rand.nextFloat() < 0.33F;
		}
		
		return true;
	}

	/* grow */
	@Override
	public void func_149853_b(World world, Random rand, int x, int y, int z) {

		int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
		Block onTop = com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock();
		
		if(meta == EnumFlowerType.WEED.ordinal()) {
			if(onTop == ModBlocks.dirt_dead || onTop == ModBlocks.dirt_oily) {
				world.setBlock(x, y, z, ModBlocks.plant_dead, EnumDeadPlantType.GENERIC.ordinal(), 3);
				return;
			}
		}
		
		if(meta == EnumFlowerType.WEED.ordinal()) {
			world.setBlock(x, y, z, ModBlocks.plant_tall, EnumTallFlower.WEED.ordinal(), 3);
			world.setBlock(x, y + 1, z, ModBlocks.plant_tall, EnumTallFlower.WEED.ordinal() + 8, 3);
			return;
		}
		
		if(meta == EnumFlowerType.CD0.ordinal()) {
			world.setBlock(x, y, z, ModBlocks.plant_flower, EnumFlowerType.CD1.ordinal(), 3);
			return;
		}
		
		if(meta == EnumFlowerType.CD1.ordinal()) {
			world.setBlock(x, y, z, ModBlocks.plant_tall, EnumTallFlower.CD2.ordinal(), 3);
			world.setBlock(x, y + 1, z, ModBlocks.plant_tall, EnumTallFlower.CD2.ordinal() + 8, 3);
			return;
		}
		
		this.dropBlockAsItem(world, x, y, z, com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ), 0);
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
    	int l = 0;
        int i1 = 0;
        int j1 = 0;

        for (int k1 = -1; k1 <= 1; ++k1)
        {
            for (int l1 = -1; l1 <= 1; ++l1)
            {
                int i2 = world.getBiomeGenForCoords(x + l1, z + k1).getBiomeFoliageColor(x + l1, y, z + k1);
                l += (i2 & 16711680) >> 16;
                i1 += (i2 & 65280) >> 8;
                j1 += i2 & 255;
            }
        }
        int meta = world.getBlockMetadata(x, y, z);
        if (meta == 1 || meta == 3) {
        	return ((l / 9 & 255) << 16 | (i1 / 9 & 255) << 8 | j1 / 9 & 255);
        } else return 0xFFFFFF;
    }
    

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) { }

	@Override
	public Item getItemDropped(int meta, Random rand, int j) {
		if(meta == EnumFlowerType.STRAWBERRY.ordinal()) {
			return ModItems.strawberry;
		}

		if(meta == EnumFlowerType.MINT.ordinal()) {
			return ModItems.mint_leaves;
		}
		
		return super.getItemDropped(meta, rand, j);
	}


	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		if(metadata == EnumFlowerType.STRAWBERRY.ordinal()) return ModBlocks.getDropsWithoutDamage(world, this, metadata, fortune);
		return super.getDrops(world, x, y, z, metadata, fortune);
	}

}
