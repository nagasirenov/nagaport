package com.hbm.entity.boss;

import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.util.text.ITextComponent;

/** NTM display classification and immediate display values only.
 * Implementing this does not alter native portal/dimension or combat rules.
 * HUD arbitration/registration is a separate, currently pending port boundary.
 */
public interface INtmBossDisplay {
    float getHealth();
    float getMaxHealth();
    ITextComponent getDisplayName();

    /** Original thermal boss classification, including the original vanilla bosses. */
    static boolean isDisplayBoss(Entity entity) {
        return entity instanceof INtmBossDisplay || entity instanceof EntityDragon || entity instanceof EntityWither;
    }
}
