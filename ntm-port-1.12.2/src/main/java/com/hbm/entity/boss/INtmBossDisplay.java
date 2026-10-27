package com.hbm.entity.boss;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

/** NTM display classification and immediate display values only.
 * Implementing this does not alter native portal/dimension or combat rules.
 * HUD arbitration/registration is a separate, currently pending port boundary.
 */
public interface INtmBossDisplay {

    default float getHealth() {
        return this instanceof EntityLivingBase ? ((EntityLivingBase) this).getHealth() : 0.0F;
    }

    default float getMaxHealth() {
        return this instanceof EntityLivingBase ? ((EntityLivingBase) this).getMaxHealth() : 1.0F;
    }

    default ITextComponent getDisplayName() {
        return this instanceof Entity ? ((Entity) this).getDisplayName() : new TextComponentString("");
    }

    /** Original thermal boss classification, including the original vanilla bosses. */
    static boolean isDisplayBoss(Entity entity) {
        return entity instanceof INtmBossDisplay || entity instanceof EntityDragon || entity instanceof EntityWither;
    }
}
