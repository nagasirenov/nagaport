package com.hbm.entity.mob;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.ServerConfig;

import api.hbm.entity.IRadiationImmune;
import net.minecraft.block.Block;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.world.World;

public class EntityCreeperTainted extends EntityCreeper implements IRadiationImmune {

	public EntityCreeperTainted(World world) {
		super(world);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(15.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.35D);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(this.isEntityAlive()) {

			if(this.getHealth() < this.getMaxHealth() && this.ticksExisted % 10 == 0) {
				this.heal(1.0F);
			}
		}
	}

	@Override
	protected Item getDropItem() {
		return Item.getItemFromBlock(Blocks.TNT);
	}

	public void func_146077_cc() {
		if(!this.world.isRemote) {
			boolean griefing = this.world.getGameRules().getBoolean("mobGriefing");

			this.world.newExplosion(this, posX, posY, posZ, 5.0F, false, false);

			if(griefing) {
				if(this.getPowered()) {

					for(int i = 0; i < 255; i++) {
						int a = rand.nextInt(15) + (int) posX - 7;
						int b = rand.nextInt(15) + (int) posY - 7;
						int c = rand.nextInt(15) + (int) posZ - 7;
						net.minecraft.block.state.IBlockState nativeAirState = com.hbm.world.WorldBlockState.read(this.world, a, b, c);
						Block block = nativeAirState.getBlock();
						if(block.isNormalCube(nativeAirState) && !block.isAir(nativeAirState, this.world, new net.minecraft.util.math.BlockPos(a, b, c))) {
							if(!ServerConfig.TAINT_TRAILS.get()) {
								com.hbm.world.WorldBlockState.writeLegacyBlock(this.world, a, b, c, ModBlocks.taint, rand.nextInt(3) + 5, 2);
							} else {
								com.hbm.world.WorldBlockState.writeLegacyBlock(this.world, a, b, c, ModBlocks.taint, rand.nextInt(3), 2);
							}
						}
					}

				} else {

					for(int i = 0; i < 85; i++) {
						int a = rand.nextInt(7) + (int) posX - 3;
						int b = rand.nextInt(7) + (int) posY - 3;
						int c = rand.nextInt(7) + (int) posZ - 3;
						net.minecraft.block.state.IBlockState nativeAirState = com.hbm.world.WorldBlockState.read(this.world, a, b, c);
						Block block = nativeAirState.getBlock();
						if(block.isNormalCube(nativeAirState) && !block.isAir(nativeAirState, this.world, new net.minecraft.util.math.BlockPos(a, b, c))) {
							if(!ServerConfig.TAINT_TRAILS.get()) {
								com.hbm.world.WorldBlockState.writeLegacyBlock(this.world, a, b, c, ModBlocks.taint, rand.nextInt(6) + 10, 2);
							} else {
								com.hbm.world.WorldBlockState.writeLegacyBlock(this.world, a, b, c, ModBlocks.taint, rand.nextInt(3) + 4, 2);
							}
						}
					}
				}
			}

			this.setDead();
		}
	}

	public static boolean hasPosNeightbour(World world, int x, int y, int z) {
		net.minecraft.block.state.IBlockState s0 = com.hbm.world.WorldBlockState.read(world, x + 1, y, z);
		net.minecraft.block.state.IBlockState s1 = com.hbm.world.WorldBlockState.read(world, x, y + 1, z);
		net.minecraft.block.state.IBlockState s2 = com.hbm.world.WorldBlockState.read(world, x, y, z + 1);
		net.minecraft.block.state.IBlockState s3 = com.hbm.world.WorldBlockState.read(world, x - 1, y, z);
		net.minecraft.block.state.IBlockState s4 = com.hbm.world.WorldBlockState.read(world, x, y - 1, z);
		net.minecraft.block.state.IBlockState s5 = com.hbm.world.WorldBlockState.read(world, x, y, z - 1);
		return s0.isOpaqueCube() || s1.isOpaqueCube() || s2.isOpaqueCube() || s3.isOpaqueCube() || s4.isOpaqueCube() || s5.isOpaqueCube();
	}
}
