package com.hbm.entity.projectile;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.WorldConfig;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.main.MainRegistry;
import com.hbm.world.feature.Meteorite;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import com.hbm.sound.AudioWrapper;
import com.hbm.util.fauxpointtwelve.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class EntityMeteor extends Entity {

	public boolean safe = false;
	private AudioWrapper audioFly;

	public EntityMeteor(World world) {
		super(world);
		this.ignoreFrustumCheck = true;
		this.isImmuneToFire = true;
		this.setSize(4F, 4F);
	}

	public List<BlockPos> getBlocksInRadius(World world, int x, int y, int z, int radius) {
		List<BlockPos> foundBlocks = new ArrayList();

		int rSq = radius * radius;
		for(int dx = -radius; dx <= radius; dx++) {
			for(int dy = -radius; dy <= radius; dy++) {
				for(int dz = -radius; dz <= radius; dz++) {
					// Check if point (dx, dy, dz) lies inside the sphere
					if(dx * dx + dy * dy + dz * dz <= rSq) {
						foundBlocks.add(new BlockPos(x + dx, y + dy, z + dz));
					}
				}
			}
		}
		return foundBlocks;
	}

	public void damageOrDestroyBlock(World world, int blockX, int blockY, int blockZ) {
		if(safe) return;

		// Get current block info
		Block block = com.hbm.world.WorldBlockState.read(world, blockX, blockY, blockZ).getBlock();
		if(block == null) return;
		float hardness = block.getBlockHardness(world, blockX, blockY, blockZ);

		// Check if the block is weak and can be destroyed
		if(block == Blocks.LEAVES || block == Blocks.LOG || (hardness >= 0 && hardness <= 0.3F)) {
			// Destroy the block
			com.hbm.world.WorldAirRemoval.remove(world, blockX, blockY, blockZ);
		} else {
			// Found solid block
			if(hardness < 0 || hardness > 5F) return;

			if(rand.nextInt(6) == 1) {
				// Turn blocks into damaged variants
				if(block == Blocks.DIRT) {
					world.setBlock(blockX, blockY, blockZ, ModBlocks.dirt_dead);
				} else if(block == Blocks.SAND) {
					if(rand.nextInt(2) == 1) {
						{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = blockX; int nativePlacementY = blockY; int nativePlacementZ = blockZ; net.minecraft.block.Block nativePlacementBlock = Blocks.SANDSTONE; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
					} else {
						{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = blockX; int nativePlacementY = blockY; int nativePlacementZ = blockZ; net.minecraft.block.Block nativePlacementBlock = Blocks.GLASS; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
					}
				} else if(block == Blocks.STONE) {
					{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = blockX; int nativePlacementY = blockY; int nativePlacementZ = blockZ; net.minecraft.block.Block nativePlacementBlock = Blocks.COBBLESTONE; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
				} else if(block == Blocks.GRASS) {
					world.setBlock(blockX, blockY, blockZ, ModBlocks.waste_earth);
				}
			}
		}
	}

	public void clearMeteorPath(World world, int x, int y, int z) {
		for(BlockPos blockPos : getBlocksInRadius(world, x, y, z, 5)) {
			damageOrDestroyBlock(this.world, blockPos.getX(), blockPos.getY(), blockPos.getZ());
		}
	}

	@Override
	public void onUpdate() {
		if(!this.world.isRemote && !WorldConfig.enableMeteorStrikes) {
			this.setDead();
			return;
		}

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		this.motionY -= 0.03;
		if(motionY < -2.5) motionY = -2.5;

		this.moveEntity(motionX, motionY, motionZ);

		if(!this.world.isRemote && this.posY < 260) {
			clearMeteorPath(this.world, (int) this.posX, (int) this.posY, (int) this.posZ);
			if(this.onGround) {
				this.world.createExplosion(this, this.posX, this.posY, this.posZ, 5 + rand.nextFloat(), !safe);

				if(WorldConfig.enableMeteorTails) {
					ExplosionLarge.spawnRubble(this.world, this.posX, this.posY, this.posZ, 15);

					ExplosionLarge.spawnParticles(this.world, posX, posY + 5, posZ, 75);
					ExplosionLarge.spawnParticles(this.world, posX + 5, posY, posZ, 75);
					ExplosionLarge.spawnParticles(this.world, posX - 5, posY, posZ, 75);
					ExplosionLarge.spawnParticles(this.world, posX, posY, posZ + 5, 75);
					ExplosionLarge.spawnParticles(this.world, posX, posY, posZ - 5, 75);
				}

				// Bury the meteor into the ground
				int spawnPosX = (int) (Math.round(this.posX - 0.5D) + (safe ? 0 : (this.motionZ * 4)));
				int spawnPosY = (int) Math.round(this.posY - (safe ? 0 : 4));
				int spawnPosZ = (int) (Math.round(this.posZ - 0.5D) + (safe ? 0 : (this.motionZ * 4)));

				(new Meteorite()).generate(this.world, rand, spawnPosX, spawnPosY, spawnPosZ, safe, true, true);
				clearMeteorPath(this.world, spawnPosX, spawnPosY, spawnPosZ);

				com.hbm.sound.WorldSoundEffects.play(this.world, this.posX, this.posY, this.posZ, "hbm:entity.oldExplosion", 10000.0F, 0.5F + this.rand.nextFloat() * 0.1F);

				this.setDead();
			}
		}

		// Sound
		if(this.world.isRemote) {

			if(this.isDead) {
				if(this.audioFly != null) this.audioFly.stopSound();

			} else {

				if(this.audioFly == null) this.audioFly = MainRegistry.proxy.getLoopedSound("hbm:entity.meteoriteFallingLoop", 0, 0, 0, 1F, 200F, 0.9F + this.rand.nextFloat() * 0.2F, 10);

				if(this.audioFly.isPlaying()) {
					// Update sound
					this.audioFly.keepAlive();
					this.audioFly.updateVolume(1F);
					this.audioFly.updatePosition((float) this.posX, (float) (this.posY + this.height / 2), (float) this.posZ);
				} else {
					// Start playing the sound
					EntityPlayer player = MainRegistry.proxy.me();
					double distance = player.getDistanceSq(this.posX, this.posY, this.posZ);
					if(distance < 210 * 210) {
						this.audioFly.startSound();
					}
				}
			}

			if(WorldConfig.enableMeteorTails) {

				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "exhaust");
				data.setString("mode", "meteor");
				data.setInteger("count", 10);
				data.setDouble("width", 1);
				data.setDouble("posX", posX - motionX);
				data.setDouble("posY", posY - motionY);
				data.setDouble("posZ", posZ - motionZ);

				MainRegistry.proxy.effectNT(data);
			}
		}
	}

	@Override @SideOnly(Side.CLIENT) public boolean isInRangeToRenderDist(double distance) { return true; }
	@Override @SideOnly(Side.CLIENT) public int getBrightnessForRender(float f) { return 15728880; }
	@Override public float getBrightness(float f) { return 1.0F; }

	@Override protected void entityInit() { }
	@Override protected void readEntityFromNBT(NBTTagCompound nbt) { this.safe = nbt.getBoolean("safe"); }
	@Override protected void writeEntityToNBT(NBTTagCompound nbt) { nbt.setBoolean("safe", safe); }
}
