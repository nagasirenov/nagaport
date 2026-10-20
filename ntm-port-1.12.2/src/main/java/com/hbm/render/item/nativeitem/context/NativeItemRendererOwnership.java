package com.hbm.render.item.nativeitem.context;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;

/** One-shot FMLLoadComplete seam, after Forge's entity factories and before HUD/
 * initial-screen construction. Preflight every audited live native cache before
 * changing any identity. Unknown owner classes abort the whole transaction.
 */
public final class NativeItemRendererOwnership {
    private final ContextItemRegistration registration;
    private boolean attempted;
    private ContextRenderItem installed;
    public NativeItemRendererOwnership(ContextItemRegistration registration) {
        this.registration = Objects.requireNonNull(registration);
    }
    public ContextRenderItem getInstalled() { return installed; }
    public boolean install() {
        if(attempted) return installed != null;
        attempted = true;
        List<Slot> slots = new ArrayList<>();
        ContextItemRegistration.Activation activation = null;
        RenderItem original = null;
        ContextRenderItem replacement = null;
        int changed = 0;
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if(mc == null || mc.getClass() != Minecraft.class) throw new IllegalStateException("Minecraft owner is not exact native");
            original = mc.getRenderItem();
            if(original == null || original.getClass() != RenderItem.class) throw new IllegalStateException("RenderItem owner is not exact native");
            if(mc.currentScreen != null || mc.ingameGUI != null) throw new IllegalStateException("Installation is outside the pre-GUI load-complete window");
            if(mc.getItemRenderer() == null || mc.getItemRenderer().getClass() != ItemRenderer.class)
                throw new IllegalStateException("First-person ItemRenderer owner is not exact native");
            RenderManager manager = mc.getRenderManager();
            if(manager == null || manager.getClass() != RenderManager.class) throw new IllegalStateException("RenderManager owner is not exact native");
            if(manager.entityRenderMap.get(EntityItem.class) == null || manager.entityRenderMap.get(EntityItem.class).getClass() != RenderEntityItem.class
                    || manager.entityRenderMap.get(EntityItemFrame.class) == null || manager.entityRenderMap.get(EntityItemFrame.class).getClass() != RenderItemFrame.class)
                throw new IllegalStateException("Primary dropped-item/frame ownership is not exact native");
            slots.add(new Slot(mc, Minecraft.class));
            slots.add(new Slot(mc.getItemRenderer(), ItemRenderer.class));
            Set<Render<?>> seen = Collections.newSetFromMap(new IdentityHashMap<Render<?>, Boolean>());
            for(Render<?> render : manager.entityRenderMap.values()) {
                if(render == null || !seen.add(render)) continue;
                Class<?> kind = render.getClass();
                if(kind == RenderEntityItem.class) slots.add(new Slot(render, RenderEntityItem.class));
                else if(kind == RenderItemFrame.class) slots.add(new Slot(render, RenderItemFrame.class));
                else if(kind == RenderSnowball.class || kind == RenderPotion.class) slots.add(new Slot(render, RenderSnowball.class));
                else for(Class<?> owner = kind; owner != null; owner = owner.getSuperclass()) {
                    for(Field field : owner.getDeclaredFields()) {
                        if(!Modifier.isStatic(field.getModifiers()) && RenderItem.class.isAssignableFrom(field.getType()))
                            throw new IllegalStateException("Unknown entity renderer caches RenderItem: " + kind.getName());
                    }
                }
            }
            for(Slot slot : slots) if(slot.get() != original) throw new IllegalStateException("Native cache identity differs: " + slot.owner.getClass().getName());
            activation = registration.prepare();
            replacement = new ContextRenderItem(original);
            // No rendering/event callback runs between this preflight and commit.
            for(Slot slot : slots) if(slot.get() != original) throw new IllegalStateException("Native cache changed while preparing installation");
            for(Slot slot : slots) { slot.set(replacement); changed++; }
            activation.commit();
            installed = replacement;
            return true;
        } catch(RuntimeException | Error failure) {
            Throwable rollbackFailure = null;
            if(activation != null) try { activation.rollback(); } catch(RuntimeException | Error problem) { rollbackFailure = problem; }
            for(int i = changed - 1; i >= 0; i--) try { slots.get(i).set(original); }
            catch(RuntimeException | Error problem) { if(rollbackFailure == null) rollbackFailure = problem; else rollbackFailure.addSuppressed(problem); }
            if(rollbackFailure != null) {
                failure.addSuppressed(rollbackFailure);
                throw new IllegalStateException("Native item-render ownership rollback failed; stop client initialization rather than use partial ownership", failure);
            }
            registration.unsupported(failure.toString());
            return false;
        }
    }
    private static final class Slot {
        final Object owner;
        final Field field;
        Slot(Object owner, Class<?> declaration) { this.owner = owner; field = NativeItemFields.unique(declaration, RenderItem.class); }
        RenderItem get() { return (RenderItem)NativeItemFields.get(field, owner); }
        void set(RenderItem value) { NativeItemFields.set(field, owner, value); }
    }
}
