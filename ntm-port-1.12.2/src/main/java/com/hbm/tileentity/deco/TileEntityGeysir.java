package com.hbm.tileentity.deco;

import java.util.List;
import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.entity.particle.EntityOrangeFX;
import com.hbm.entity.projectile.EntityShrapnel;
import com.hbm.entity.projectile.EntityWaterSplash;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.packet.toclient.AuxParticlePacketNT;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityGeysir extends TileEntity implements net.minecraft.util.ITickable {

	int timer;

	@Override
	public void update() {

		if (!this.world.isRemote && com.hbm.world.WorldBlockState.read(this.world, this.pos.getX(), this.pos.getY() + 1, this.pos.getZ()).getBlock() == Blocks.AIR) {

			timer--;

			int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ()) );

			if(timer <= 0) {
				timer = getDelay();

				if(meta == 0)
					this.world.setBlockMetadataWithNotify(this.pos.getX(), this.pos.getY(), this.pos.getZ(), 1, 2);
				else
					this.world.setBlockMetadataWithNotify(this.pos.getX(), this.pos.getY(), this.pos.getZ(), 0, 2);
			}

			if(meta == 1) {
				perform();
			}
		}
	}

	private void water() {

		EntityWaterSplash fx = new EntityWaterSplash(this.world, this.pos.getX() + 0.5, this.pos.getY() + 1.5, this.pos.getZ() + 0.5);

		fx.motionX = this.world.rand.nextGaussian() * 0.35;
		fx.motionZ = this.world.rand.nextGaussian() * 0.35;
		fx.motionY = 2;

		this.world.spawnEntityInWorld(fx);
	}

	private void chlorine() {

		for(int i = 0; i < 3; i++) {
			EntityOrangeFX fx = new EntityOrangeFX(this.world, this.pos.getX() + 0.5, this.pos.getY() + 1.5, this.pos.getZ() + 0.5, 0.0, 0.0, 0.0);

			fx.motionX = this.world.rand.nextGaussian() * 0.45;
			fx.motionZ = this.world.rand.nextGaussian() * 0.45;
			fx.motionY = timer * 0.3;

			this.world.spawnEntityInWorld(fx);
		}

	}

	private void vapor() {

		List<Entity> entities = this.world.getEntitiesWithinAABB(Entity.class,
				AxisAlignedBB.getBoundingBox(this.pos.getX() - 0.5, this.pos.getY() + 0.5, this.pos.getZ() - 0.5, this.pos.getX() + 1.5,
						this.pos.getY() + 2, this.pos.getZ() + 1.5));

		if (!entities.isEmpty()) {
			for (Entity e : entities) {

				if(e instanceof EntityLivingBase)
				((EntityLivingBase)e).addPotionEffect(new PotionEffect(Potion.regeneration.id, 20, 0));
			}
		}
	}

	private void fire() {

		int range = 32;
		if(this.world.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5).expand(range, range, range)).isEmpty())
			return;

		if(this.world.rand.nextInt(3) == 0) {
			EntityShrapnel fx = new EntityShrapnel(this.world, this.pos.getX() + 0.5, this.pos.getY() + 1.5, this.pos.getZ() + 0.5);
			fx.motionX = this.world.rand.nextGaussian() * 0.05;
			fx.motionZ = this.world.rand.nextGaussian() * 0.05;
			fx.motionY = 0.5 + this.world.rand.nextDouble() * timer * 0.01;

			this.world.spawnEntityInWorld(fx);
		}

		if(timer % 2 == 0) {
			NBTTagCompound data = new NBTTagCompound();
			data.setString("type", "gasfire");
			data.setDouble("mX", this.world.rand.nextGaussian() * 0.05);
			data.setDouble("mY", 0.2);
			data.setDouble("mZ", this.world.rand.nextGaussian() * 0.05);
			PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, this.pos.getX() + 0.5F, this.pos.getY() + 1.1F, this.pos.getZ() + 0.5F), new TargetPoint(this.world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 75));
		}
	}

	private int getDelay() {

		Block b = com.hbm.world.WorldBlockState.read(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ()).getBlock();
		int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ()) );
		Random rand = this.world.rand;

		if(b == ModBlocks.geysir_water) {

			return (meta == 0 ? 30 : 100 + rand.nextInt(40));

		} else if(b == ModBlocks.geysir_chlorine) {

			return (meta == 0 ? 20 : 400 + rand.nextInt(100));

		} else if(b == ModBlocks.geysir_vapor) {

			return (meta == 0 ? 20 : 30 + rand.nextInt(20));

		} else if(b == ModBlocks.geysir_nether) {

			return (meta == 0 ? (rand.nextBoolean() ? 300 : 450) : 80 + rand.nextInt(60));

		}

		return 0;
	}

	private void perform() {

		Block b = com.hbm.world.WorldBlockState.read(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ()).getBlock();

		if(b == ModBlocks.geysir_water) {
			water();

		} else if(b == ModBlocks.geysir_chlorine) {
			chlorine();

		} else if(b == ModBlocks.geysir_vapor) {
			vapor();

		} else if(b == ModBlocks.geysir_nether) {
			fire();
		}
	}

}
