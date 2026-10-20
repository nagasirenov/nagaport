package com.hbm.blocks.fluid;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.init.Blocks;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class VolcanicBlock extends BlockFluidClassic {

	@SideOnly(Side.CLIENT)
	public static IIcon stillIcon;
	@SideOnly(Side.CLIENT)
	public static IIcon flowingIcon;

	public VolcanicBlock(Fluid fluid, Material material) {
		super(fluid, material);
		this.setTickRandomly(true);
		this.setQuantaPerBlock(4);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return (side == 0 || side == 1) ? stillIcon : flowingIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		stillIcon = register.registerIcon(RefStrings.MODID + ":volcanic_lava_still");
		flowingIcon = register.registerIcon(RefStrings.MODID + ":volcanic_lava_flowing");
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		super.onNeighborBlockChange(world, x, y, z, block);
		
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			Block b = getReaction(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
			
			if(b != null) world.setBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, b, b == ModBlocks.ore_basalt ? 3 : 0, 3);
		}
	}

	public Block getReaction(World world, int x, int y, int z) {
		
		Block b = com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock();
		if(b.getDefaultState().getMaterial() == Material.WATER) return Blocks.STONE;
		if(b == Blocks.LOG || b == Blocks.LOG2) return ModBlocks.waste_log;
		if(b == Blocks.PLANKS) return ModBlocks.waste_planks;
		if(b == Blocks.LEAVES || b == Blocks.LEAVES2) return Blocks.FIRE;
		if(b == Blocks.DIAMOND_ORE) return ModBlocks.ore_basalt;
		return null;
	}
	
	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {
		
		super.updateTick(world, x, y, z, rand);
		
		int lavaCount = 0;
		int basaltCount = 0;
		
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			Block b = com.hbm.world.WorldBlockState.read(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ).getBlock();
			
			if(b == this)
				lavaCount++;
			if(b == getBasaltForCheck()) {
				basaltCount++;
			}
		}
		
		if(!world.isRemote && ((!this.isSourceBlock(world, x, y, z) && lavaCount < 2) || (rand.nextInt(5) == 0) && lavaCount < 5) && com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() != this) {
			this.onSolidify(world, x, y, z, lavaCount, basaltCount, rand);
		}
	}
	
	public Block getBasaltForCheck() {
		return ModBlocks.basalt;
	}
	
	public void onSolidify(World world, int x, int y, int z, int lavaCount, int basaltCount, Random rand) {
		int r = rand.nextInt(200);
		
		Block above = com.hbm.world.WorldBlockState.read(world, x, y + 10, z).getBlock();
		boolean canMakeGem = lavaCount + basaltCount == 6 && lavaCount < 3 && (above == ModBlocks.basalt || above == ModBlocks.volcanic_lava_block);
		
		if(r < 2) world.setBlock(x, y, z, ModBlocks.ore_basalt, 0, 3);
		else if(r == 2) world.setBlock(x, y, z, ModBlocks.ore_basalt, 1, 3);
		else if(r == 3) world.setBlock(x, y, z, ModBlocks.ore_basalt, 2, 3);
		else if(r == 4) world.setBlock(x, y, z, ModBlocks.ore_basalt, 4, 3);
		else if(r < 15 && canMakeGem) world.setBlock(x, y, z, ModBlocks.ore_basalt, 3, 3);
		else world.setBlock(x, y, z, ModBlocks.basalt);
	}

	@Override
	public boolean canDisplace(IBlockAccess world, int x, int y, int z) {
		Block b = world.getBlock(x, y, z);
		
		if(Blocks.FIRE.getFlammability(b) > 0)
			return true;
		
		if(b.isReplaceable(world, x, y, z))
			return true;
		
		return super.canDisplace(world, x, y, z);
	}

	@Override
	public boolean displaceIfPossible(World world, int x, int y, int z) {
		return super.displaceIfPossible(world, x, y, z) || canDisplace(world, x, y, z);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderBlockPass() {
		return 0;
	}
	
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {

		double dx;
		double dy;
		double dz;

		if(com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock().getDefaultState().getMaterial() == Material.AIR && !com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock().isOpaqueCube()) {
			if(rand.nextInt(100) == 0) {
				dx = (double) ((float) x + rand.nextFloat());
				dy = (double) y + this.maxY;
				dz = (double) ((float) z + rand.nextFloat());
				world.spawnParticle("lava", dx, dy, dz, 0.0D, 0.0D, 0.0D);
				world.playSound(dx, dy, dz, "liquid.lavapop", 0.2F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.15F, false);
			}

			if(rand.nextInt(200) == 0) {
				world.playSound((double) x, (double) y, (double) z, "liquid.lava", 0.2F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.15F, false);
			}
		}

		if(rand.nextInt(10) == 0 && World.doesBlockHaveSolidTopSurface(world, x, y - 1, z) && !com.hbm.world.WorldBlockState.read(world, x, y - 2, z).getBlock().getDefaultState().getMaterial().blocksMovement()) {
			dx = (double) ((float) x + rand.nextFloat());
			dy = (double) y - 1.05D;
			dz = (double) ((float) z + rand.nextFloat());
			world.spawnParticle("dripLava", dx, dy, dz, 0.0D, 0.0D, 0.0D);
		}
	}
}
