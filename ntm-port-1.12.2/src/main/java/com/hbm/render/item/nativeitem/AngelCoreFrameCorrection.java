package com.hbm.render.item.nativeitem;

import org.lwjgl.opengl.GL11;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.RenderItemInFrameEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Original four frame rotations; intervening native angles extend the same off-center pivot. */
public final class AngelCoreFrameCorrection {
    private final Item item;
    public AngelCoreFrameCorrection(Item item) { this.item = item; }
    @SubscribeEvent public void renderFrame(RenderItemInFrameEvent event) {
        if(!event.getItem().isEmpty() && event.getItem().getItem() == item) {
            double[] delta = translation(event.getEntityItemFrame().getRotation());
            // Native caller already owns a matrix scope. Cancellation or local push/pop
            // would discard the correction before its subsequent FIXED item render.
            GL11.glTranslated(delta[0], delta[1], delta[2]);
        }
    }
    public static double[] translation(int rotation) {
        double angle = Math.toRadians(45D * (rotation & 7));
        return new double[] { -.02D * Math.sin(angle), -.02D * Math.cos(angle), .015625D };
    }
}
