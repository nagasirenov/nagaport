package com.hbm.items.weapon.sedna.factory;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockNTMSand.EnumSandType;
import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockAllocatorBulkie;
import com.hbm.explosion.vanillant.standard.BlockAllocatorStandard;
import com.hbm.explosion.vanillant.standard.BlockMutatorDebris;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.explosion.vanillant.standard.EntityProcessorCrossSmooth;
import com.hbm.explosion.vanillant.standard.ExplosionEffectWeapon;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.Crosshair;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.ItemGunBaseNT.LambdaContext;
import com.hbm.items.weapon.sedna.ItemGunBaseNT.WeaponQuality;
import com.hbm.items.weapon.sedna.factory.GunFactory.EnumAmmo;
import com.hbm.items.weapon.sedna.impl.ItemGunChargeThrower;
import com.hbm.items.weapon.sedna.mags.MagazineFullReload;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.particle.helper.ExplosionCreator;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationSequence;
import com.hbm.render.anim.BusAnimationKeyframe.IType;
import com.hbm.tileentity.IRepairable;
import com.hbm.tileentity.IRepairable.EnumExtinguishType;
import com.hbm.util.CompatExternal;
import com.hbm.util.Vec3NT;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.ResourceLocation;

public class XFactoryTool {

	public static final ResourceLocation scope = new ResourceLocation(RefStrings.MODID, "textures/misc/scope_tool.png");

	public static BulletConfig fext_water;
	public static BulletConfig fext_foam;
	public static BulletConfig fext_sand;

	public static BulletConfig ct_hook;
	public static BulletConfig ct_mortar;
	public static BulletConfig ct_mortar_charge;

	public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_WATER_HIT = (bullet, mop) -> {
		if(!bullet.world.isRemote) {
			int ix = mop.getBlockPos().getX();
			int iy = mop.getBlockPos().getY();
			int iz = mop.getBlockPos().getZ();
			boolean fizz = false;
			for(int i = -1; i <= 1; i++) for(int j = -1; j <= 1; j++) for(int k = -1; k <= 1; k++) {
				Block block = com.hbm.world.WorldBlockState.read(bullet.world, ix + i, iy + j, iz + k).getBlock();
				if(block == Blocks.fire || block == ModBlocks.foam_layer || block == ModBlocks.block_foam) {
					bullet.world.setBlock(ix + i, iy + j, iz + k, Blocks.air);
					fizz = true;
				}
			}
			TileEntity core = CompatExternal.getCoreFromPos(bullet.world, ix, iy, iz);
			if(core instanceof IRepairable) ((IRepairable) core).tryExtinguish(bullet.world, ix, iy, iz, EnumExtinguishType.WATER);
			if(fizz) com.hbm.sound.VanillaWorldSoundEffects.play(bullet.world, bullet.posX, bullet.posY, bullet.posZ, "random.fizz", 1.0F, 1.5F + bullet.world.rand.nextFloat() * 0.5F);
			bullet.setDead();
		}
	};

	public static Consumer<Entity> LAMBDA_WATER_UPDATE = (bullet) -> {
		if(bullet.world.isRemote) {
			NBTTagCompound data = new NBTTagCompound();
			data.setString("type", "vanillaExt");
			data.setString("mode", "blockdust");
			data.setInteger("block", Block.getIdFromBlock(Blocks.water));
			data.setDouble("posX", bullet.posX); data.setDouble("posY", bullet.posY); data.setDouble("posZ", bullet.posZ);
			data.setDouble("mX", bullet.motionX + bullet.world.rand.nextGaussian() * 0.05);
			data.setDouble("mY", bullet.motionY - 0.2 + bullet.world.rand.nextGaussian() * 0.05);
			data.setDouble("mZ", bullet.motionZ + bullet.world.rand.nextGaussian() * 0.05);
			MainRegistry.proxy.effectNT(data);
		} else {
			int x = (int)Math.floor(bullet.posX);
			int y = (int)Math.floor(bullet.posY);
			int z = (int)Math.floor(bullet.posZ);
			if(com.hbm.world.WorldBlockState.read(bullet.world, x, y, z).getBlock() == ModBlocks.volcanic_lava_block && bullet.world.getBlockMetadata(x, y, z) == 0) {
				bullet.world.setBlock(x, y, z, Blocks.obsidian);
				bullet.setDead();
			}
		}
	};

	public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_FOAM_HIT = (bullet, mop) -> {
		if(!bullet.world.isRemote) {
			int ix = mop.getBlockPos().getX();
			int iy = mop.getBlockPos().getY();
			int iz = mop.getBlockPos().getZ();
			boolean fizz = false;
			for(int i = -1; i <= 1; i++) for(int j = -1; j <= 1; j++) for(int k = -1; k <= 1; k++) {
				Block b = com.hbm.world.WorldBlockState.read(bullet.world, ix + i, iy + j, iz + k).getBlock();
				if(b.getDefaultState().getMaterial() == Material.FIRE) {
					bullet.world.setBlock(ix + i, iy + j, iz + k, Blocks.air);
					fizz = true;
				}
			}
			TileEntity core = CompatExternal.getCoreFromPos(bullet.world, ix, iy, iz);
			if(core instanceof IRepairable) { ((IRepairable) core).tryExtinguish(bullet.world, ix, iy, iz, EnumExtinguishType.FOAM); return; }
			if(bullet.world.rand.nextBoolean()) {
				net.minecraft.util.EnumFacing side = mop.sideHit;
				ix += side.getFrontOffsetX(); iy += side.getFrontOffsetY(); iz += side.getFrontOffsetZ();
			}
			Block b = com.hbm.world.WorldBlockState.read(bullet.world, ix, iy, iz).getBlock();
			if(b.isReplaceable(bullet.world, ix, iy, iz) && ModBlocks.foam_layer.canPlaceBlockAt(bullet.world, ix, iy, iz)) {
				if(b != ModBlocks.foam_layer) {
					bullet.world.setBlock(ix, iy, iz, ModBlocks.foam_layer);
				} else {
					int meta = bullet.world.getBlockMetadata(ix, iy, iz);
					if(meta < 6) bullet.world.setBlockMetadataWithNotify(ix, iy, iz, meta + 1, 3);
					else bullet.world.setBlock(ix, iy, iz, ModBlocks.block_foam);
				}
			}
			if(fizz) com.hbm.sound.VanillaWorldSoundEffects.play(bullet.world, bullet.posX, bullet.posY, bullet.posZ, "random.fizz", 1.0F, 1.5F + bullet.world.rand.nextFloat() * 0.5F);
		}
	};

	public static Consumer<Entity> LAMBDA_FOAM_UPDATE = (bullet) -> {
		if(bullet.world.isRemote) {
			NBTTagCompound data = new NBTTagCompound();
			data.setString("type", "vanillaExt");
			data.setString("mode", "blockdust");
			data.setInteger("block", Block.getIdFromBlock(ModBlocks.block_foam));
			data.setDouble("posX", bullet.posX); data.setDouble("posY", bullet.posY); data.setDouble("posZ", bullet.posZ);
			data.setDouble("mX", bullet.motionX + bullet.world.rand.nextGaussian() * 0.1);
			data.setDouble("mY", bullet.motionY - 0.2 + bullet.world.rand.nextGaussian() * 0.1);
			data.setDouble("mZ", bullet.motionZ + bullet.world.rand.nextGaussian() * 0.1);
			MainRegistry.proxy.effectNT(data);
		}
	};

	public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_SAND_HIT = (bullet, mop) -> {
		if(!bullet.world.isRemote) {
			int ix = mop.getBlockPos().getX();
			int iy = mop.getBlockPos().getY();
			int iz = mop.getBlockPos().getZ();
			TileEntity core = CompatExternal.getCoreFromPos(bullet.world, ix, iy, iz);
			if(core instanceof IRepairable) { ((IRepairable) core).tryExtinguish(bullet.world, ix, iy, iz, EnumExtinguishType.SAND); return; }
			if(bullet.world.rand.nextBoolean()) {
				net.minecraft.util.EnumFacing side = mop.sideHit;
				ix += side.getFrontOffsetX(); iy += side.getFrontOffsetY(); iz += side.getFrontOffsetZ();
			}
			Block b = com.hbm.world.WorldBlockState.read(bullet.world, ix, iy, iz).getBlock();
			if((b.isReplaceable(bullet.world, ix, iy, iz) || b == ModBlocks.sand_boron_layer) && ModBlocks.sand_boron_layer.canPlaceBlockAt(bullet.world, ix, iy, iz)) {
				if(b != ModBlocks.sand_boron_layer) {
					bullet.world.setBlock(ix, iy, iz, ModBlocks.sand_boron_layer);
				} else {
					int meta = bullet.world.getBlockMetadata(ix, iy, iz);
					if(meta < 6) bullet.world.setBlockMetadataWithNotify(ix, iy, iz, meta + 1, 3);
					else bullet.world.setBlock(ix, iy, iz, ModBlocks.sand_mix, EnumSandType.BORON.ordinal(), 3);
				}
				if(b.getDefaultState().getMaterial() == Material.FIRE) com.hbm.sound.VanillaWorldSoundEffects.play(bullet.world, bullet.posX, bullet.posY, bullet.posZ, "random.fizz", 1.0F, 1.5F + bullet.world.rand.nextFloat() * 0.5F);
			}
		}
	};

	public static Consumer<Entity> LAMBDA_SAND_UPDATE = (bullet) -> {
		if(bullet.world.isRemote) {
			NBTTagCompound data = new NBTTagCompound();
			data.setString("type", "vanillaExt");
			data.setString("mode", "blockdust");
			data.setInteger("block", Block.getIdFromBlock(ModBlocks.sand_mix));
			data.setInteger("meta", EnumSandType.BORON.ordinal());
			data.setDouble("posX", bullet.posX); data.setDouble("posY", bullet.posY); data.setDouble("posZ", bullet.posZ);
			data.setDouble("mX", bullet.motionX + bullet.world.rand.nextGaussian() * 0.1);
			data.setDouble("mY", bullet.motionY - 0.2 + bullet.world.rand.nextGaussian() * 0.1);
			data.setDouble("mZ", bullet.motionZ + bullet.world.rand.nextGaussian() * 0.1);
			MainRegistry.proxy.effectNT(data);
		}
	};

	public static Consumer<Entity> LAMBDA_SET_HOOK = (entity) -> {
		EntityBulletBaseMK4 bullet = (EntityBulletBaseMK4) entity;
		if(!bullet.world.isRemote && bullet.ticksExisted < 2 && bullet.getThrower() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) bullet.getThrower();
			if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null && com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() == ModItems.gun_charge_thrower) {
				ItemGunChargeThrower.setLastHook(com.hbm.util.NativeHeldItem.getMainHandNullable(player), bullet.getEntityId());
			}
		}
		bullet.ignoreFrustumCheck = true;
	};

	public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_HOOK = (bullet, mop) -> {
		if(mop.typeOfHit == RayTraceResult.Type.BLOCK) {
			Vec3NT vec = new Vec3NT(-bullet.motionX, -bullet.motionY, -bullet.motionZ).normalizeSelf().multiply(0.05);
			bullet.setPosition(mop.hitVec.x + vec.x, mop.hitVec.y + vec.y, mop.hitVec.z + vec.z);
			bullet.getStuck(mop.getBlockPos().getX(), mop.getBlockPos().getY(), mop.getBlockPos().getZ(), mop.sideHit.getIndex());
		}
	};

	public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_MORTAR = (bullet, mop) -> {
		if(mop.typeOfHit == RayTraceResult.Type.ENTITY && bullet.ticksExisted < 3 && mop.entityHit == bullet.getThrower()) return;
		ExplosionVNT vnt = new ExplosionVNT(bullet.world, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, 5, bullet.getThrower());
		vnt.setBlockAllocator(new BlockAllocatorBulkie(60, 8));
		vnt.setBlockProcessor(new BlockProcessorStandard());
		vnt.setEntityProcessor(new EntityProcessorCrossSmooth(1, bullet.damage).setupPiercing(bullet.config.armorThresholdNegation, bullet.config.armorPiercingPercent));
		vnt.setPlayerProcessor(new PlayerProcessorStandard());
		vnt.setSFX(new ExplosionEffectWeapon(10, 2.5F, 1F));
		vnt.explode();
		bullet.setDead();
	};

	public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_MORTAR_CHARGE = (bullet, mop) -> {
		if(mop.typeOfHit == RayTraceResult.Type.ENTITY && bullet.ticksExisted < 3 && mop.entityHit == bullet.getThrower()) return;
		ExplosionVNT vnt = new ExplosionVNT(bullet.world, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, 15, bullet.getThrower());
		vnt.setBlockAllocator(new BlockAllocatorStandard());
		vnt.setBlockProcessor(new BlockProcessorStandard().setNoDrop().withBlockEffect(new BlockMutatorDebris(ModBlocks.block_slag, 1)));
		vnt.setEntityProcessor(new EntityProcessorCrossSmooth(1, bullet.damage).setupPiercing(bullet.config.armorThresholdNegation, bullet.config.armorPiercingPercent));
		vnt.setPlayerProcessor(new PlayerProcessorStandard());
		ExplosionCreator.composeEffectSmall(bullet.world, mop.hitVec.x, mop.hitVec.y + 0.5, mop.hitVec.z);
		vnt.explode();
		bullet.setDead();
	};

	public static void init() {

		fext_water = new BulletConfig().setItem(new ItemStack(ModItems.ammo_fireext, 1, 0)).setReloadCount(300).setLife(100).setVel(0.75F).setGrav(0.04D).setSpread(0.025F)
				.setOnUpdate(LAMBDA_WATER_UPDATE)
				.setOnEntityHit((bulletEntity, target) -> { if(target.entityHit != null) target.entityHit.extinguish(); })
				.setOnRicochet(LAMBDA_WATER_HIT);
		fext_foam = new BulletConfig().setItem(new ItemStack(ModItems.ammo_fireext, 1, 1)).setReloadCount(300).setLife(100).setVel(0.75F).setGrav(0.04D).setSpread(0.05F)
				.setOnUpdate(LAMBDA_FOAM_UPDATE)
				.setOnEntityHit((bulletEntity, target) -> { if(target.entityHit != null) target.entityHit.extinguish(); })
				.setOnRicochet(LAMBDA_FOAM_HIT);
		fext_sand = new BulletConfig().setItem(new ItemStack(ModItems.ammo_fireext, 1, 2)).setReloadCount(300).setLife(100).setVel(0.75F).setGrav(0.04D).setSpread(0.05F)
				.setOnUpdate(LAMBDA_SAND_UPDATE)
				.setOnEntityHit((bulletEntity, target) -> { if(target.entityHit != null) target.entityHit.extinguish(); })
				.setOnRicochet(LAMBDA_SAND_HIT);

		ct_hook = new BulletConfig().setItem(EnumAmmo.CT_HOOK).setRenderRotations(false).setLife(6_000).setVel(3F).setGrav(0.035D).setDoesPenetrate(true).setDamageFalloffByPen(false)
				.setOnUpdate(LAMBDA_SET_HOOK).setOnImpact(LAMBDA_HOOK);
		ct_mortar = new BulletConfig().setItem(EnumAmmo.CT_MORTAR).setDamage(2.5F).setLife(200).setVel(3F).setGrav(0.035D)
				.setOnImpact(LAMBDA_MORTAR);
		ct_mortar_charge = new BulletConfig().setItem(EnumAmmo.CT_MORTAR_CHARGE).setDamage(5F).setLife(200).setVel(3F).setGrav(0.035D)
				.setOnImpact(LAMBDA_MORTAR_CHARGE);

		ModItems.gun_fireext = new ItemGunBaseNT(WeaponQuality.UTILITY, new GunConfig()
				.dura(5_000).draw(10).inspect(55).reloadChangeType(true).hideCrosshair(false).crosshair(Crosshair.L_CIRCLE)
				.rec(new Receiver(0)
						.dmg(0F).delay(1).dry(0).auto(true).spread(0F).spreadHipfire(0F).reload(20).jam(0).sound("hbm:weapon.extinguisher", 1.0F, 1.0F)
						.mag(new MagazineFullReload(0, 300).addConfigs(fext_water, fext_foam, fext_sand))
						.offset(1, -0.0625 * 2.5, -0.25D)
						.setupStandardFire())
				.setupStandardConfiguration()
				.orchestra(Orchestras.ORCHESTRA_FIREEXT)
				).setTranslationKey("gun_fireext");

		ModItems.gun_charge_thrower = new ItemGunChargeThrower(WeaponQuality.UTILITY, new GunConfig()
				.dura(3_000).draw(10).inspect(55).reloadChangeType(true).hideCrosshair(false).crosshair(Crosshair.L_CIRCUMFLEX)
				.rec(new Receiver(0)
						.dmg(10F).delay(4).dry(10).auto(true).spread(0F).spreadHipfire(0F).reload(60).jam(0).sound("hbm:weapon.fire.grenade", 1.0F, 1.0F)
						.mag(new MagazineFullReload(0, 1).addConfigs(ct_hook, ct_mortar, ct_mortar_charge))
						.offset(1, -0.0625 * 2.5, -0.25D)
						.setupStandardFire().recoil(LAMBDA_RECOIL_CT))
				.setupStandardConfiguration()
				.anim(LAMBDA_CT_ANIMS).orchestra(Orchestras.ORCHESTRA_CHARGE_THROWER)
				).setDefaultAmmo(EnumAmmo.CT_MORTAR, 3).setUnlocalizedName("gun_charge_thrower");
	}

	public static BiConsumer<ItemStack, LambdaContext> LAMBDA_RECOIL_CT = (stack, ctx) -> {
		ItemGunBaseNT.setupRecoil(10, (float) (ctx.getPlayer().getRNG().nextGaussian() * 1.5));
	};

	@SuppressWarnings("incomplete-switch") public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_CT_ANIMS = (stack, type) -> {
		switch(type) {
		case EQUIP: return new BusAnimation()
				.addBus("EQUIP", new BusAnimationSequence().addPos(-45, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
		case CYCLE: return new BusAnimation()
				.addBus("RECOIL", new BusAnimationSequence().addPos(0, 0, -1, 100, IType.SIN_DOWN).addPos(0, 0, 0, 250, IType.SIN_FULL));
		case RELOAD: return new BusAnimation()
				.addBus("RAISE", new BusAnimationSequence().addPos(-45, 0, 0, 500, IType.SIN_FULL).hold(2000).addPos(0, 0, 0, 500, IType.SIN_FULL))
				.addBus("AMMO", new BusAnimationSequence().setPos(0, -10, -5).hold(500).addPos(0, 0, 5, 750, IType.SIN_FULL).addPos(0, 0, 0, 500, IType.SIN_UP).hold(4000))
				.addBus("TWIST", new BusAnimationSequence().setPos(0, 0, 25).hold(2000).addPos(0, 0, 0, 150));
		case INSPECT: return new BusAnimation()
				.addBus("TURN", new BusAnimationSequence().addPos(0, 60, 0, 500, IType.SIN_FULL).hold(1750).addPos(0, 0, 0, 500, IType.SIN_FULL))
				.addBus("ROLL", new BusAnimationSequence().hold(750).addPos(0, 0, -90, 500, IType.SIN_FULL).hold(1000).addPos(0, 0, 0, 500, IType.SIN_FULL));
		}

		return null;
	};
}
