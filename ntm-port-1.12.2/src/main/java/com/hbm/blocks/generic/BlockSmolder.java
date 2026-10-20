package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.items.ModItems;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.world.World;

public class BlockSmolder extends Block {

	public BlockSmolder(Material mat) {
		super(mat);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		super.randomDisplayTick(world, x, y, z, rand);

		if(com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock().getDefaultState().getMaterial() == Material.AIR) {

			world.spawnParticle(net.minecraft.util.EnumParticleTypes.LAVA, x + 0.25 + rand.nextDouble() * 0.5, y + 1.1, z + 0.25 + rand.nextDouble() * 0.5, 0.0, 0.0, 0.0);
			world.spawnParticle(net.minecraft.util.EnumParticleTypes.FLAME, x + 0.25 + rand.nextDouble() * 0.5, y + 1.1, z + 0.25 + rand.nextDouble() * 0.5, 0.0, 0.0, 0.0);
		}
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState i, Random rand, int j) {
		return ModItems.powder_fire;
	}

	@Override
	public void onEntityWalking(World world, int x, int y, int z, Entity entity) {
		entity.setFire(3);
	}
}
