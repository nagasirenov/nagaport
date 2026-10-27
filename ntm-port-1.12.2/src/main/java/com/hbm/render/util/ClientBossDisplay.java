package com.hbm.render.util;

import com.hbm.entity.boss.INtmBossDisplay;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** NTM's original last-rendered display state, independent of HUD arbitration.
 * No overlay, tick listener, packet or native gameplay boss flag is registered here.
 * The HUD consumer remains a separate pending integration boundary.
 */
@SideOnly(Side.CLIENT)
public final class ClientBossDisplay {
    private static float healthRatio;
    private static int framesRemaining;
    private static String name;
    private static boolean darkeningPulse;
    private ClientBossDisplay() { }

    public static void capture(INtmBossDisplay boss, boolean darken) {
        if(boss == null) return;
        if(boss instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase) boss;
            float max = living.getMaxHealth();
            healthRatio = max > 0.0F ? living.getHealth() / max : 0.0F;
            framesRemaining = 100;
            name = living.getDisplayName().getFormattedText();
            darkeningPulse = darken;
        } else {
            float max = boss.getMaxHealth();
            healthRatio = max > 0.0F ? boss.getHealth() / max : 0.0F;
            framesRemaining = 100;
            name = boss.getDisplayName() != null ? boss.getDisplayName().getFormattedText() : "";
            darkeningPulse = darken;
        }
    }

    public static Snapshot current() { return new Snapshot(healthRatio, framesRemaining, name, darkeningPulse); }

    /** Future HUD calls this only when it enters an uncanceled eligible bar draw, not a game tick. */
    public static boolean beginHudDraw() {
        if(name == null || framesRemaining <= 0) return false;
        --framesRemaining;
        return true;
    }

    /** Future unpaused renderer-update consumer; atmosphere lifetime is independent of bar lifetime. */
    public static boolean consumeDarkeningPulse() {
        boolean value = darkeningPulse;
        darkeningPulse = false;
        return value;
    }

    /** Explicit future world/disconnect cleanup; no event listener is activated by this class. */
    public static void clear() { healthRatio = 0; framesRemaining = 0; name = null; darkeningPulse = false; }

    public static final class Snapshot {
        public final float healthRatio;
        public final int framesRemaining;
        public final String name;
        public final boolean darkeningPulse;
        private Snapshot(float healthRatio, int framesRemaining, String name, boolean darkeningPulse) {
            this.healthRatio = healthRatio; this.framesRemaining = framesRemaining;
            this.name = name; this.darkeningPulse = darkeningPulse;
        }
    }
}
