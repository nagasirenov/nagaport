package com.hbm.blocks.fluid;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.ModDamageSource;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class SchrabidicBlock extends BlockFluidClassic {

	@SideOnly(Side.CLIENT)
	public static IIcon stillIcon;
	@SideOnly(Side.CLIENT)
	public static IIcon flowingIcon;
	public Random rand = new Random();

	public static DamageSource damageSource;

	public SchrabidicBlock(Fluid fluid, Material material, DamageSource damage) {
		super(fluid, material);
		damageSource = damage;
		//setQuantaPerBlock(4);
		setCreativeTab(null);
		displacements.put(this, false);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return (side == 0 || side == 1) ? stillIcon : flowingIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		stillIcon = register.registerIcon(RefStrings.MODID + ":schrabidic_acid_still");
		flowingIcon = register.registerIcon(RefStrings.MODID + ":schrabidic_acid_flowing");
	}

	@Override
	public boolean canDisplace(IBlockAccess world, int x, int y, int z) {

		if (world.getBlock(x, y, z).getMaterial().isLiquid()) {
			return false;
		}
		return super.canDisplace(world, x, y, z);
	}

	@Override
	public boolean displaceIfPossible(World world, int x, int y, int z) {

		if (com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock().getDefaultState().getMaterial().isLiquid()) {
			return false;
		}
		return super.displaceIfPossible(world, x, y, z);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		
		if(entity instanceof EntityLivingBase)
		{
			if(entity.motionY < -0.2)
				entity.motionY *= 0.5;
			entity.attackEntityFrom(ModDamageSource.acid, 10F);
			ContaminationUtil.contaminate((EntityLivingBase)entity, HazardType.RADIATION, ContaminationType.CREATIVE, 1.0F);	
		}
	}

	/*@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		super.onNeighborBlockChange(world, x, y, z, block);
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			//Block b = getReaction(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
			getReaction(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
		}
	}*/
	
	public void getReaction(World world, int x, int y, int z) {
		Block b = com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock();
		double dx= (double) ((float) x + rand.nextFloat());
		double dy= (double) ((float) y + rand.nextFloat());
		double dz= (double) ((float) z + rand.nextFloat());
		if(b.getDefaultState().getMaterial() == Material.WATER && b!=this) {
			world.setBlock(x, y, z, ModBlocks.sellafield_slaked);
		}
		else if((b == Blocks.LOG || b == Blocks.LOG2) && rand.nextInt(4) == 0) {
		    world.playSound((double)x,(double)y,(double)z,"random.fizz", 0.2F, 1F,false);
		    world.setBlock(x, y, z, ModBlocks.waste_log);
		}
		else if(b == Blocks.PLANKS && rand.nextInt(4) == 0) {
		    world.playSound((double)x,(double)y,(double)z,"random.fizz", 0.2F, 1F,false);
		    world.setBlock(x, y, z, ModBlocks.waste_planks);
		}
		else if((b == Blocks.LEAVES || b == Blocks.LEAVES2 || b.getDefaultState().getMaterial() == Material.IRON || (b.getDefaultState().getMaterial() == Material.GLASS && b != ModBlocks.reinforced_glass)) && rand.nextInt(4) == 0) {
			world.spawnParticle("cloud", dx, dy, dz, 0.0, 0.0, 0.0);
		    world.playSound((double)x,(double)y,(double)z,"random.fizz", 0.2F, 1F,false);
		    { net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		}
		else if ((b == Blocks.STONE || 
				b == Blocks.STONE_BRICK_STAIRS || 
				b == Blocks.STONEBRICK || 
				b == Blocks.STONE_SLAB || 
				b == Blocks.STONE) && rand.nextInt(4) == 0) {
			world.playSound((double)x,(double)y,(double)z,"random.fizz", 0.2F, 1F,false);
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.COBBLESTONE; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		}
		else if(b == Blocks.COBBLESTONE && rand.nextInt(4) == 0) {
			world.playSound((double)x,(double)y,(double)z,"random.fizz", 0.2F, 1F,false);
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.GRAVEL; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		}
		else if(b == Blocks.GRAVEL && rand.nextInt(4) == 0) {
			world.playSound((double)x,(double)y,(double)z,"random.fizz", 0.2F, 1F,false);
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.CLAY; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		}
		else if((b == Blocks.OBSIDIAN || b == ModBlocks.brick_obsidian || b == ModBlocks.brick_obsidian_stairs) && rand.nextInt(4) == 0) {
			world.playSound((double)x,(double)y,(double)z,"random.fizz", 0.2F, 1F,false);
			world.setBlock(x, y, z, ModBlocks.gravel_obsidian);
		}
		else if((b == Blocks.SANDSTONE || b == Blocks.END_STONE) && rand.nextInt(4) == 0) {
			world.playSound((double)x,(double)y,(double)z,"random.fizz", 0.2F, 1F,false);
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.SAND; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		} 
		else if (b.getExplosionResistance(null) < 1.2F && b != ModBlocks.block_polymer && b != ModBlocks.gravel_obsidian && b != Blocks.SAND && b != Blocks.CLAY) {
			world.playSound((double)x,(double)y,(double)z,"random.fizz", 0.2F, 1F,false);
			world.spawnParticle("cloud", dx, dy, dz, 0.0, 0.0, 0.0);
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		}
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {
		
		super.updateTick(world, x, y, z, rand);
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			getReaction(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
		}
	}
	
	@Override
	public int tickRate(World p_149738_1_) {
		return 15;
	}

	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		super.randomDisplayTick(world, x, y, z, rand);

		double ix = x + 0.5F + rand.nextDouble() * 2 - 1D;
		double iy = y + 0.5F + rand.nextDouble() * 2 - 1D;
		double iz = z + 0.5F + rand.nextDouble() * 2 - 1D;

		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "schrabfog");
		data.setDouble("posX", ix);
		data.setDouble("posY", iy);
		data.setDouble("posZ", iz);
		MainRegistry.proxy.effectNT(data);
	}
}
