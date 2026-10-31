package com.hbm.items.weapon.sedna.factory;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.entity.projectile.EntityBulletBeamBase;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.Crosshair;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.ItemGunBaseNT.GunState;
import com.hbm.items.weapon.sedna.ItemGunBaseNT.LambdaContext;
import com.hbm.items.weapon.sedna.ItemGunBaseNT.WeaponQuality;
import com.hbm.items.weapon.sedna.factory.GunFactory.EnumAmmoSecret;
import com.hbm.items.weapon.sedna.mags.MagazineSingleReload;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationSequence;
import com.hbm.render.anim.BusAnimationKeyframe.IType;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.EntityDamageUtil;
import com.hbm.util.Vec3NT;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import com.hbm.util.DamageResistanceHandler.DamageClass;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;

public class XFactoryFolly {

	public static BulletConfig folly_sm;
	public static BulletConfig folly_nuke;

	public static Consumer<Entity> LAMBDA_SM_UPDATE = (entity) -> {
		if(entity.world.isRemote) return;
		EntityBulletBeamBase beam = (EntityBulletBeamBase) entity;
		Vec3NT dir = new Vec3NT(beam.headingX, beam.headingY, beam.headingZ).normalizeSelf();

		if(beam.ticksExisted < 50) {
			double spacing = 10;
			double dist = beam.ticksExisted * spacing;
			NBTTagCompound data = new NBTTagCompound();
			data.setString("type", "plasmablast");
			data.setFloat("r", 0.75F);
			data.setFloat("g", 0.75F);
			data.setFloat("b", 0.75F);
			data.setFloat("pitch", (float) beam.rotationPitch + 90);
			data.setFloat("yaw", (float) -beam.rotationYaw);
			data.setFloat("scale", 2F + beam.ticksExisted / (float)(beam.beamLength / spacing) * 3F);
			PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, beam.posX + dir.x * dist, beam.posY + dir.y * dist, beam.posZ + dir.z * dist), new TargetPoint(beam.dimension, beam.posX, beam.posY, beam.posZ, 250));
		}

		if(entity.ticksExisted != 2) return;

		if(beam.thrower != null) ContaminationUtil.contaminate(beam.thrower, HazardType.RADIATION, ContaminationType.CREATIVE, 150F);

		List<Entity> entities = beam.world.getEntitiesWithinAABBExcludingEntity(beam, beam.getEntityBoundingBox().expand(beam.headingX, beam.headingY, beam.headingZ).grow(1.0D, 1.0D, 1.0D));

		for(int i = 1; i < beam.beamLength; i += 2) {
			int x = (int) Math.floor(beam.posX + dir.x * i);
			int y = (int) Math.floor(beam.posY + dir.y * i);
			int z = (int) Math.floor(beam.posZ + dir.z * i);

			for(int ix = x - 1; ix <= x + 1; ix++) for(int iy = y - 1; iy <= y + 1; iy++) for(int iz = z - 1; iz <= z + 1; iz++) {
				if(iy > 0 && iy < 256) com.hbm.world.WorldBlockState.writeLegacyBlock(beam.world, ix, iy, iz, Blocks.AIR);
				AxisAlignedBB aabb = new AxisAlignedBB(ix - 1, iy - 1, iz - 1, ix + 2, iy + 2, iz + 2);
				for(Entity e : entities) if(e != beam.thrower && e.getEntityBoundingBox().intersects(aabb)) {
					if(e instanceof EntityLivingBase) EntityDamageUtil.attackEntityFromNT((EntityLivingBase) e, beam.config.getDamage(beam, beam.thrower, beam.config.dmgClass), beam.damage, true, false, 0D, 100F, 0.99F);
					else EntityDamageUtil.attackEntityFromIgnoreIFrame(e, beam.config.getDamage(beam, beam.thrower, beam.config.dmgClass), beam.damage);
				}
			}
		}
	};

	public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_NUKE_IMPACT = (bullet, mop) -> {
		if(mop.typeOfHit == RayTraceResult.Type.ENTITY && bullet.ticksExisted < 2) return;
		if(bullet.isDead) return;
		bullet.setDead();
		bullet.world.spawnEntity(EntityNukeExplosionMK5.statFac(bullet.world, 100, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z));
		EntityNukeTorex.statFacStandard(bullet.world, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, 100);
	};

	public static void init() {

		folly_sm = new BulletConfig().setItem(EnumAmmoSecret.FOLLY_SM).setupDamageClass(DamageClass.SUBATOMIC).setBeam().setLife(100).setVel(2F).setGrav(0.015D).setRenderRotations(false).setSpectral(true).setDoesPenetrate(true)
				.setOnUpdate(LAMBDA_SM_UPDATE);
		folly_nuke = new BulletConfig().setItem(EnumAmmoSecret.FOLLY_NUKE).setChunkloading().setLife(600).setVel(4F).setGrav(0.015D)
				.setOnImpact(LAMBDA_NUKE_IMPACT);

		ModItems.gun_folly = new ItemGunBaseNT(WeaponQuality.SECRET, new GunConfig()
				.dura(0).draw(40).crosshair(Crosshair.NONE)
				.rec(new Receiver(0)
						.dmg(1_000F).delay(26).dryfire(false).reload(160).jam(0).sound("hbm:weapon.fire.loudestNoiseOnEarth", 100.0F, 1.0F)
						.mag(new MagazineSingleReload(0, 1).addConfigs(folly_sm, folly_nuke))
						.offset(0.75, -0.0625, -0.1875D).offsetScoped(0.75, -0.0625, -0.125D)
						.canFire(LAMBDA_CAN_FIRE).fire(LAMBDA_FIRE).recoil(LAMBDA_RECOIL_FOLLY))
				.setupStandardConfiguration().pt(LAMBDA_TOGGLE_AIM)
				.anim(LAMBDA_FOLLY_ANIMS).orchestra(Orchestras.ORCHESTRA_FOLLY)
				).setTranslationKey("gun_folly");
	}

	public static BiConsumer<ItemStack, LambdaContext> LAMBDA_TOGGLE_AIM = (stack, ctx) -> {
		if(ItemGunBaseNT.getState(stack, ctx.configIndex) == GunState.IDLE) {
			boolean wasAiming = ItemGunBaseNT.getIsAiming(stack);
			ItemGunBaseNT.setIsAiming(stack, !wasAiming);
			if(!wasAiming) ItemGunBaseNT.playAnimation(ctx.getPlayer(), stack, GunAnimation.SPINUP, ctx.configIndex);
		}
	};

	public static BiConsumer<ItemStack, LambdaContext> LAMBDA_FIRE = (stack, ctx) -> {
		Lego.doStandardFire(stack, ctx, GunAnimation.CYCLE, 0, false);
	};

	public static BiFunction<ItemStack, LambdaContext, Boolean> LAMBDA_CAN_FIRE = (stack, ctx) -> {
		if(!ItemGunBaseNT.getIsAiming(stack)) return false;
		if(ItemGunBaseNT.getLastAnim(stack, ctx.configIndex) != GunAnimation.SPINUP) return false;
		if(ItemGunBaseNT.getAnimTimer(stack, ctx.configIndex) < 100) return false;
		return ctx.config.getReceivers(stack)[0].getMagazine(stack).getAmount(stack, ctx.inventory) > 0;
	};

	public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_FOLLY = (stack, ctx) -> {
		ItemGunBaseNT.setupRecoil(25, (float) (ctx.getPlayer().getRNG().nextGaussian() * 1.5));
	};

	@SuppressWarnings("incomplete-switch") public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_FOLLY_ANIMS = (stack, type) -> {
		switch(type) {
		case EQUIP: return new BusAnimation()
				.addBus("EQUIP", new BusAnimationSequence().addPos(-60, 0, 0, 0).addPos(5, 0, 0, 1500, IType.SIN_DOWN).addPos(0, 0, 0, 500, IType.SIN_FULL));
		case CYCLE: return new BusAnimation()
				.addBus("RECOIL", new BusAnimationSequence().addPos(0, 0, -4.5, 50).addPos(0, 0, -4.5, 500).addPos(0, 0, 0, 500, IType.SIN_UP))
				.addBus("LOAD", new BusAnimationSequence().addPos(0, 0, 0, 50).addPos(-25, 0, 0, 250, IType.SIN_DOWN).addPos(0, 0, 0, 1000, IType.SIN_FULL));
		case RELOAD: return new BusAnimation()
				.addBus("LOAD", new BusAnimationSequence().addPos(60, 0, 0, 1000, IType.SIN_FULL).addPos(60, 0, 0, 6000).addPos(0, 0, 0, 1000, IType.SIN_FULL))
				.addBus("SCREW", new BusAnimationSequence().addPos(0, 0, 0, 1000).addPos(0, 0, -135, 1000, IType.SIN_FULL).addPos(0, 0, -135, 4000).addPos(0, 0, 0, 1000, IType.SIN_FULL))
				.addBus("BREECH", new BusAnimationSequence().addPos(0, 0, 0, 1000).addPos(0, 0, -0.5, 1000, IType.SIN_FULL).addPos(0, -4, -0.5, 1000, IType.SIN_FULL).addPos(0, -4, -0.5, 2000).addPos(0, 0, -0.5, 1000, IType.SIN_FULL).addPos(0, 0, 0, 1000, IType.SIN_FULL))
				.addBus("SHELL", new BusAnimationSequence().addPos(0, -4, -4.5, 0).addPos(0, -4, -4.5, 3000).addPos(0, 0, -4.5, 1000, IType.SIN_FULL).addPos(0, 0, 0, 500, IType.SIN_UP));
		}

		return null;
	};
}
