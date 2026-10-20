package com.hbm.render.item.weapon.sedna.nativehand;

import java.lang.reflect.Field;
import com.hbm.render.item.nativeitem.context.NativeProjectionScope;
import com.hbm.render.item.weapon.sedna.ItemRenderWeaponBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.opengl.GL11;

/** Actual cached hand stacks and native hand selection remain owned by ItemRenderer.
 * No last context or global hand flag connects these two synchronous events. */
public final class SednaHandHandler {
    private static final Field MAIN = ReflectionHelper.findField(ItemRenderer.class, "itemStackMainHand", "field_187467_d");
    private static final Field OFF = ReflectionHelper.findField(ItemRenderer.class, "itemStackOffHand", "field_187468_e");
    private static ItemStack cached(ItemRenderer renderer, Field field) {
        try { return (ItemStack)field.get(renderer); }
        catch(IllegalAccessException failure) { throw new IllegalStateException("Cannot read native cached hand", failure); }
    }
    public static ItemRenderWeaponBase renderer(ItemStack stack) {
        if(stack == null || stack.isEmpty()) return null;
        return stack.getItem().getTileEntityItemStackRenderer() instanceof ItemRenderWeaponBase
                ? (ItemRenderWeaponBase)stack.getItem().getTileEntityItemStackRenderer() : null;
    }
    @SubscribeEvent public void renderHands(RenderHandEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        ItemRenderer nativeHands = mc.getItemRenderer();
        ItemStack main = cached(nativeHands, MAIN), off = cached(nativeHands, OFF);
        ItemRenderWeaponBase mainRenderer = renderer(main), offRenderer = renderer(off);
        if(mainRenderer == null && offRenderer == null) return;
        event.setCanceled(true);
        // This is the original render-frame interpolation clock, not a held item context.
        ItemRenderWeaponBase.interp = event.getPartialTicks();
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        if(mc.gameSettings.thirdPersonView == 0 && !(mc.getRenderViewEntity() instanceof net.minecraft.entity.EntityLivingBase && ((net.minecraft.entity.EntityLivingBase)mc.getRenderViewEntity()).isPlayerSleeping())
                && !mc.gameSettings.hideGUI && !mc.playerController.isSpectator()) {
            try(NativeProjectionScope scope = new NativeProjectionScope()) {
                mc.entityRenderer.enableLightmap();
                try { nativeHands.renderItemInFirstPerson(event.getPartialTicks()); }
                finally { mc.entityRenderer.disableLightmap(); }
            }
        }
        // 1.12 still draws overlays after a canceled RenderHandEvent. Preserve
        // the original custom projection for that one native pass, not a second
        // manual overlay. Main hand retains precedence when both are custom.
        if(mainRenderer != null) mainRenderer.applyHandProjection(main, event.getPartialTicks(), EnumHand.MAIN_HAND);
        else offRenderer.applyHandProjection(off, event.getPartialTicks(), EnumHand.OFF_HAND);
        GL11.glLoadIdentity();
    }
    @SubscribeEvent public void renderHand(RenderSpecificHandEvent event) {
        ItemRenderWeaponBase renderer = renderer(event.getItemStack());
        if(renderer == null) return;
        Minecraft mc = Minecraft.getMinecraft();
        EnumHandSide side = event.getHand() == EnumHand.MAIN_HAND ? mc.player.getPrimaryHand() : mc.player.getPrimaryHand().opposite();
        event.setCanceled(true);
        renderer.renderNativeHand(event.getItemStack(), event.getPartialTicks(), event.getHand(), side);
    }
}
