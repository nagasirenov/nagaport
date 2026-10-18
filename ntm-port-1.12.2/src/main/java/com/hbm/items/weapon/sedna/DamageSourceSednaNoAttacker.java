package com.hbm.items.weapon.sedna;

import java.util.Locale;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class DamageSourceSednaNoAttacker extends DamageSource {

	public DamageSourceSednaNoAttacker(String type) {
		super(type.toLowerCase(Locale.US));
	}

	@Override
	public ITextComponent getDeathMessage(EntityLivingBase died) {
		ITextComponent diedName = died.getDisplayName();
		return new TextComponentTranslation("death.sedna." + this.damageType, diedName);
	}
}
