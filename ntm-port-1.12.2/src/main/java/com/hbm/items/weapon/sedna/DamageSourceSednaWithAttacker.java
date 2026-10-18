package com.hbm.items.weapon.sedna;

import java.util.Locale;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class DamageSourceSednaWithAttacker extends DamageSourceSednaNoAttacker {

	public Entity projectile;
	public Entity shooter;
	
	public DamageSourceSednaWithAttacker(String type, Entity projectile, Entity shooter) {
		super(type.toLowerCase(Locale.US));
		this.projectile = projectile;
		this.shooter = shooter;
	}

	@Override public Entity getSourceOfDamage() { return this.projectile; } //what even uses this, except for the wackass "shot by bullet" death messages?
	@Override public Entity getEntity() { return this.shooter; }

	@Override
	public ITextComponent getDeathMessage(EntityLivingBase died) {
		ITextComponent diedName = died.getDisplayName();
		ITextComponent shooterName = shooter != null ? shooter.getDisplayName() : new TextComponentString("Unknown").setStyle(new Style().setObfuscated(true));
		return new TextComponentTranslation("death.sedna." + this.damageType + ".attacker", diedName, shooterName);
	}
}
