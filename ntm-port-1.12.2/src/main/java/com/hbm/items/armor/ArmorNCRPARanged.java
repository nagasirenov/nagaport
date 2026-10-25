package com.hbm.items.armor;

import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.ItemGunBaseNT.GunState;
import com.hbm.items.weapon.sedna.ItemGunBaseNT.LambdaContext;
import com.hbm.items.weapon.sedna.factory.XFactoryRocket;
import com.hbm.items.weapon.sedna.mags.MagazineBelt;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ArmorNCRPARanged implements IPARanged {

	public static MagazineBelt rocketSteerMag = new MagazineBelt();
	public static MagazineBelt rocketMag = new MagazineBelt();

	@Override public void clickPrimary(ItemStack stack, LambdaContext ctx) { fireRocket(stack, ctx, true); }
	@Override public void clickSecondary(ItemStack stack, LambdaContext ctx) { fireRocket(stack, ctx, false); }

	public static void fireRocket(ItemStack stack, LambdaContext ctx, boolean steer) {

		EntityPlayer player = ctx.getPlayer();
		GunState state = ItemGunBaseNT.getState(stack, 0);
		MagazineBelt mag = steer ? rocketSteerMag : rocketMag;

		if(state == GunState.IDLE) {
			if(mag.acceptedBullets.isEmpty()) {
				mag.addConfigs(steer ? XFactoryRocket.rocket_ncrpa_steer : XFactoryRocket.rocket_ncrpa);
			}
			BulletConfig cfg = mag.getType(stack, player.inventory);
			int amount = mag.getAmount(stack, player.inventory);

			if(amount > 0) {
				mag.useUpAmmo(stack, player.inventory, 1);
				EntityBulletBaseMK4 mk4 = new EntityBulletBaseMK4(player, cfg, 25, 0, 0.25F * (player.getRNG().nextBoolean() ? - 1 : 1), 0, 0);
				player.world.spawnEntity(mk4);
				ItemGunBaseNT.setState(stack, 0, GunState.COOLDOWN);
				ItemGunBaseNT.setTimer(stack, 0, 10);
				player.world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:weapon.rpgShoot"), net.minecraft.util.SoundCategory.PLAYERS, 0.5F, 0.9F + player.getRNG().nextFloat() * 0.2F);
				player.inventoryContainer.detectAndSendChanges();
			} else {
				ItemGunBaseNT.setState(stack, 0, GunState.COOLDOWN);
				ItemGunBaseNT.setTimer(stack, 0, 10);
				player.world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:weapon.reload.dryFireClick"), net.minecraft.util.SoundCategory.PLAYERS, 1F, 1F);
			}
		}
	}

}
