package com.hbm.render.item.nativeitem.context;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import com.hbm.render.item.weapon.sedna.nativehand.SednaHandHandler;
import com.hbm.render.item.weapon.sedna.nativeplayer.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.common.MinecraftForge;

/** Separate transaction after the proven global RenderItem seam. Failure keeps
 * Boltgun active but leaves every Sedna model, TEISR, player pose and layer original. */
public final class SednaPlayerOwnership {
    private final ContextItemRegistration registration;
    private final ContextRenderItem expectedRenderer;
    private final Consumer<String> warning;
    private boolean attempted, installed;
    public SednaPlayerOwnership(ContextItemRegistration registration, ContextRenderItem expectedRenderer, Consumer<String> warning) {
        this.registration = Objects.requireNonNull(registration);
        this.expectedRenderer = Objects.requireNonNull(expectedRenderer);
        this.warning = Objects.requireNonNull(warning);
    }
    public boolean install() {
        if(attempted) return installed;
        attempted = true;
        List<Change> changes = new ArrayList<>();
        ContextItemRegistration.Activation activation = null;
        SednaHandHandler hands = new SednaHandHandler();
        int changed = 0; boolean subscribed = false;
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if(mc == null || mc.getClass() != Minecraft.class || mc.getRenderItem() != expectedRenderer
                    || expectedRenderer.getClass() != ContextRenderItem.class)
                throw new IllegalStateException("Exact installed contextual renderer identity is missing");
            if(mc.currentScreen != null || mc.ingameGUI != null) throw new IllegalStateException("Outside pre-GUI load-complete window");
            RenderManager manager = mc.getRenderManager();
            if(manager == null || manager.getClass() != RenderManager.class) throw new IllegalStateException("Unknown render manager");
            Set<RenderPlayer> players = Collections.newSetFromMap(new IdentityHashMap<RenderPlayer,Boolean>());
            Map<String,RenderPlayer> skins = new HashMap<>(manager.getSkinMap());
            if(!skins.containsKey("default") || !skins.containsKey("slim")) throw new IllegalStateException("Native player skins are missing");
            players.addAll(skins.values());
            players.add((RenderPlayer)NativeItemFields.get(NativeItemFields.unique(RenderManager.class,RenderPlayer.class),manager));
            for(Render<?> renderer : manager.entityRenderMap.values()) if(renderer instanceof RenderPlayer) players.add((RenderPlayer)renderer);
            for(RenderPlayer player : players) prepare(player,changes);
            activation = registration.prepare();
            if(mc.getRenderItem() != expectedRenderer || !skins.equals(manager.getSkinMap())) throw new IllegalStateException("Native player/item ownership changed during preparation");
            for(Change change : changes) if(change.current() != change.before) throw new IllegalStateException("Native pose/layer ownership changed during preparation");
            for(Change change : changes) { change.set(change.after); changed++; }
            activation.commit();
            // Registration itself has no render callback. Include subscription in
            // this transaction so failures cannot leave a half-activated family.
            subscribed = true; MinecraftForge.EVENT_BUS.register(hands);
            installed = true; return true;
        } catch(RuntimeException | Error failure) {
            Throwable rollback = null;
            if(subscribed) try { MinecraftForge.EVENT_BUS.unregister(hands); } catch(RuntimeException | Error problem) { rollback = problem; }
            if(activation != null) try { activation.rollback(); } catch(RuntimeException | Error problem) { if(rollback == null) rollback = problem; else rollback.addSuppressed(problem); }
            for(int i = changed - 1; i >= 0; i--) try { changes.get(i).set(changes.get(i).before); }
            catch(RuntimeException | Error problem) { if(rollback == null) rollback = problem; else rollback.addSuppressed(problem); }
            if(rollback != null) { failure.addSuppressed(rollback); throw new IllegalStateException("Native Sedna rollback failed; stop initialization rather than retain partial ownership",failure); }
            warning.accept("Native Sedna rendering is not activated: " + failure + ". Original Sedna models, player models and layers were preserved; unknown ownership is unsupported, not visual parity.");
            return false;
        }
    }
    private static void prepare(RenderPlayer renderer, List<Change> changes) {
        if(renderer == null || renderer.getClass() != RenderPlayer.class) throw new IllegalStateException("Unknown player renderer");
        ModelPlayer original = renderer.getMainModel();
        if(original == null || original.getClass() != ModelPlayer.class) throw new IllegalStateException("Unknown player model");
        boolean slim = (Boolean)NativeItemFields.get(NativeItemFields.unique(RenderPlayer.class,boolean.class),renderer);
        SednaPlayerModel replacement = new SednaPlayerModel(slim); copyModel(original,replacement);
        changes.add(new Change(renderer,NativeItemFields.unique(RenderLivingBase.class,ModelBase.class),original,replacement));
        Field layersField = NativeItemFields.unique(RenderLivingBase.class,List.class);
        @SuppressWarnings("unchecked") List<LayerRenderer<?>> layers = (List<LayerRenderer<?>>)NativeItemFields.get(layersField,renderer);
        if(layers == null) throw new IllegalStateException("Missing native player layers");
        List<LayerRenderer<?>> after = new ArrayList<>(layers);
        Set<Class<?>> nativeLayers = new HashSet<>(Arrays.asList(LayerBipedArmor.class,LayerHeldItem.class,LayerArrow.class,
                LayerDeadmau5Head.class,LayerCape.class,LayerCustomHead.class,LayerElytra.class,LayerEntityOnShoulder.class));
        int armorCount = 0, heldCount = 0, headCount = 0;
        SednaPlayerLayers companion = new SednaPlayerLayers(renderer);
        for(int i = 0; i < layers.size(); i++) {
            LayerRenderer<?> layer = layers.get(i);
            if(layer == null || (!nativeLayers.contains(layer.getClass()) && !isCompatibleOptifinePlayerItemsLayer(layer,renderer)))
                throw new IllegalStateException("Unknown player layer: " + (layer == null ? "null" : layer.getClass().getName()));
            if(layer.getClass() == LayerBipedArmor.class) {
                armorCount++;
                LayerBipedArmor armor = (LayerBipedArmor)layer;
                if(NativeItemFields.get(NativeItemFields.unique(LayerArmorBase.class,RenderLivingBase.class),armor) != renderer) throw new IllegalStateException("Armor layer renderer differs");
                for(EntityEquipmentSlot slot : new EntityEquipmentSlot[]{EntityEquipmentSlot.LEGS,EntityEquipmentSlot.CHEST}) {
                    ModelBiped old = armor.getModelFromSlot(slot);
                    if(old == null || old.getClass() != ModelBiped.class) throw new IllegalStateException("Unknown native armor model");
                    SednaArmorModel model = new SednaArmorModel(); copyModel(old,model);
                    Field field = identityField(LayerArmorBase.class,armor,old);
                    changes.add(new Change(armor,field,old,model));
                }
                after.set(i,companion.armor(armor));
            } else if(layer.getClass() == LayerHeldItem.class) {
                heldCount++;
                if(NativeItemFields.get(NativeItemFields.unique(LayerHeldItem.class,RenderLivingBase.class),layer) != renderer) throw new IllegalStateException("Held layer renderer differs");
                after.set(i,companion.held((LayerHeldItem)layer));
            } else if(layer.getClass() == LayerCustomHead.class) {
                headCount++;
                if(NativeItemFields.get(NativeItemFields.unique(LayerCustomHead.class,ModelRenderer.class),layer) != original.bipedHead) throw new IllegalStateException("Head layer caches a foreign model part");
                if(replacement.bipedHead != original.bipedHead) throw new AssertionError("Cached native head identity was not retained");
            }
        }
        if(armorCount != 1 || heldCount != 1 || headCount != 1) throw new IllegalStateException("Native armor/held/head layer ownership is not unique");
        for(int i=0;i<after.size();i++) after.set(i,companion.visibility(after.get(i)));
        changes.add(new Change(renderer,layersField,layers,after));
    }
    private static boolean isCompatibleOptifinePlayerItemsLayer(LayerRenderer<?> layer, RenderPlayer renderer) {
        if(!"net.optifine.player.PlayerItemsLayer".equals(layer.getClass().getName())) return false;
        Field rendererField = null;
        for(Class<?> owner = layer.getClass(); owner != null && owner != Object.class; owner = owner.getSuperclass()) {
            for(Field field : owner.getDeclaredFields()) if(!Modifier.isStatic(field.getModifiers()) && field.getType().isAssignableFrom(renderer.getClass())) {
                if(rendererField != null) throw new IllegalStateException("Ambiguous OptiFine player layer renderer ownership");
                rendererField = field;
            }
        }
        if(rendererField == null) throw new IllegalStateException("OptiFine player layer renderer ownership missing");
        rendererField.setAccessible(true);
        if(NativeItemFields.get(rendererField,layer) != renderer) throw new IllegalStateException("OptiFine player layer renderer differs");
        return true;
    }
    private static Field identityField(Class<?> owner, Object target, Object value) {
        Field found = null;
        for(Field field : owner.getDeclaredFields()) if(!Modifier.isStatic(field.getModifiers())) {
            field.setAccessible(true);
            if(NativeItemFields.get(field,target) == value) {
                if(found != null) throw new IllegalStateException("Ambiguous native model identity");
                found = field;
            }
        }
        if(found == null) throw new IllegalStateException("Native model identity missing");
        return found;
    }
    private static void copyModel(ModelBase original, ModelBase target) {
        for(Class<?> owner = original.getClass(); ModelBase.class.isAssignableFrom(owner); owner = owner.getSuperclass()) {
            for(Field field : owner.getDeclaredFields()) if(!Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                Object value = NativeItemFields.get(field,original);
                if(value instanceof ModelRenderer && value.getClass() != ModelRenderer.class) throw new IllegalStateException("Unknown player/armor model part");
                NativeItemFields.set(field,target,value);
            }
        }
    }
    private static final class Change {
        final Object owner, before, after; final Field field;
        Change(Object owner, Field field, Object before, Object after) { this.owner=owner;this.field=field;this.before=before;this.after=after; }
        Object current() { return NativeItemFields.get(field,owner); }
        void set(Object value) { NativeItemFields.set(field,owner,value); }
    }
}
