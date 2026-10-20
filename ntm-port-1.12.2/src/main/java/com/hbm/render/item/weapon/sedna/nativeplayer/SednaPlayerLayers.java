package com.hbm.render.item.weapon.sedna.nativeplayer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import com.hbm.render.item.nativeitem.context.NativeProjectionScope;
import com.hbm.render.item.weapon.sedna.ItemRenderWeaponBase;
import com.hbm.render.item.weapon.sedna.nativehand.SednaHandHandler;
import com.hbm.render.model.ModelMan;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/** Native slot/held layers retain their actual implementations. These wrappers
 * supply only the original NTM manual arm/manly/akimbo phases that 1.12 removed. */
public final class SednaPlayerLayers {
    private static final Method ARMOR_SLOT = method(LayerArmorBase.class, EntityLivingBase.class,
            float.class,float.class,float.class,float.class,float.class,float.class,float.class,EntityEquipmentSlot.class);
    private static final Method HELD_ITEM = method(LayerHeldItem.class, EntityLivingBase.class,
            ItemStack.class,TransformType.class,EnumHandSide.class);
    private final RenderPlayer playerRenderer;
    private ModelMan manlyModel;
    public SednaPlayerLayers(RenderPlayer renderer) { playerRenderer = renderer; }
    private static Method method(Class<?> owner, Class<?>... parameters) {
        Method found = null;
        for(Method method : owner.getDeclaredMethods()) if(method.getReturnType() == void.class && Arrays.equals(parameters, method.getParameterTypes())) {
            if(found != null) throw new IllegalStateException("Ambiguous native render boundary: " + owner);
            method.setAccessible(true); found = method;
        }
        if(found == null) throw new IllegalStateException("Missing native render boundary: " + owner);
        return found;
    }
    private static void invoke(Method method, Object target, Object... args) {
        try { method.invoke(target, args); }
        catch(IllegalAccessException failure) { throw new IllegalStateException(failure); }
        catch(InvocationTargetException failure) {
            Throwable cause = failure.getCause();
            if(cause instanceof RuntimeException) throw (RuntimeException)cause;
            if(cause instanceof Error) throw (Error)cause;
            throw new IllegalStateException(cause);
        }
    }
    /** Repeat the original pure visibility policy lexically for each native layer.
     * No Pre/Post frame state can leak when a player draw is canceled or reentered. */
    public LayerRenderer<EntityLivingBase> visibility(final LayerRenderer<?> original) {
        return new LayerRenderer<EntityLivingBase>() {
            @Override @SuppressWarnings("unchecked") public void doRenderLayer(EntityLivingBase entity, float limb, float amount, float partial,
                    float age, float yaw, float pitch, float scale) {
                net.minecraft.client.model.ModelPlayer model = playerRenderer.getMainModel();
                ModelRenderer[] parts = {model.bipedHead,model.bipedHeadwear,model.bipedBody,model.bipedLeftArm,model.bipedRightArm,model.bipedLeftLeg,model.bipedRightLeg,
                        model.bipedBodyWear,model.bipedLeftArmwear,model.bipedRightArmwear,model.bipedLeftLegwear,model.bipedRightLegwear};
                boolean[] hidden = new boolean[parts.length];
                for(int i=0;i<parts.length;i++) hidden[i]=parts[i].isHidden;
                try {
                    // Original manual armor draws already unhid the gun arms by
                    // the Specials phase. Keep only armor-disable/manly hiding here.
                    if(entity instanceof EntityPlayer) SednaPlayerPose.hide(model,(EntityPlayer)entity,false);
                    ((LayerRenderer<EntityLivingBase>)original).doRenderLayer(entity,limb,amount,partial,age,yaw,pitch,scale);
                } finally { for(int i=0;i<parts.length;i++) parts[i].isHidden=hidden[i]; }
            }
            @Override public boolean shouldCombineTextures() { return original.shouldCombineTextures(); }
        };
    }
    private ModelMan manly() { if(manlyModel == null) manlyModel = new ModelMan(); return manlyModel; }
    public LayerRenderer<EntityLivingBase> armor(final LayerBipedArmor original) {
        return new LayerRenderer<EntityLivingBase>() {
            @Override public void doRenderLayer(EntityLivingBase entity, float limb, float amount, float partial,
                    float age, float yaw, float pitch, float scale) {
                if(!(entity instanceof EntityPlayer)) { original.doRenderLayer(entity,limb,amount,partial,age,yaw,pitch,scale); return; }
                EntityPlayer player = (EntityPlayer)entity;
                SednaPlayerPose.pose(playerRenderer.getMainModel(), player, age);
                if(!SednaPlayerPose.manly(player) && !SednaPlayerPose.manualArms(player)) {
                    original.doRenderLayer(entity,limb,amount,partial,age,yaw,pitch,scale); return;
                }
                // Legacy RendererLivingEntity pass 0..3 corresponds to head,
                // chest, legs, feet. SetArmorModel fired even for an empty slot.
                for(EntityEquipmentSlot slot : new EntityEquipmentSlot[]{EntityEquipmentSlot.HEAD,EntityEquipmentSlot.CHEST,EntityEquipmentSlot.LEGS,EntityEquipmentSlot.FEET}) {
                    manualArmorPhase(player, partial);
                    invoke(ARMOR_SLOT, original, entity,limb,amount,partial,age,yaw,pitch,scale,slot);
                }
            }
            @Override public boolean shouldCombineTextures() { return original.shouldCombineTextures(); }
        };
    }
    private void manualArmorPhase(EntityPlayer player, float partial) {
        try(NativeProjectionScope scope = new NativeProjectionScope()) {
            ModelBiped model = playerRenderer.getMainModel();
            if(!SednaPlayerPose.manly(player)) {
                Minecraft.getMinecraft().getTextureManager().bindTexture(((AbstractClientPlayer)player).getLocationSkin());
                manualSkin(player,model,player.getHeldItemMainhand(),player.getPrimaryHand());
                manualSkin(player,model,player.getHeldItemOffhand(),player.getPrimaryHand().opposite());
            }
            if(SednaPlayerPose.manly(player)) {
                float yawHead = player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * partial;
                float yawOffset = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partial;
                float amount = player.prevLimbSwingAmount + (player.limbSwingAmount - player.prevLimbSwingAmount) * partial;
                if(amount > 1) amount = 1;
                float limb = player.limbSwing - player.limbSwingAmount * (1 - partial);
                manly().render(player,limb,amount,MathHelper.wrapDegrees(yawHead-yawOffset),yawHead-yawOffset,player.rotationPitch,.0625F,playerRenderer);
            }
        }
    }
    private static void manualSkin(EntityPlayer player, ModelBiped model, ItemStack stack, EnumHandSide primary) {
        ItemRenderWeaponBase gun = SednaHandHandler.renderer(stack);
        if(gun == null) return;
        if(gun.isAkimbo(player)) skinArm(model,primary.opposite());
        if(gun.isLeftHanded()) { skinArm(model,primary.opposite()); skinArm(model,primary); }
    }
    private static void skinArm(ModelBiped model, EnumHandSide side) {
        ModelRenderer arm = side == EnumHandSide.LEFT ? model.bipedLeftArm : model.bipedRightArm;
        boolean hidden = arm.isHidden;
        try { arm.isHidden = false; arm.render(.0625F); }
        finally { arm.isHidden = hidden; }
    }
    public LayerRenderer<EntityLivingBase> held(final LayerHeldItem original) {
        return new LayerRenderer<EntityLivingBase>() {
            @Override public void doRenderLayer(EntityLivingBase entity, float limb, float amount, float partial,
                    float age, float yaw, float pitch, float scale) {
                if(!(entity instanceof EntityPlayer)) { original.doRenderLayer(entity,limb,amount,partial,age,yaw,pitch,scale); return; }
                EntityPlayer player = (EntityPlayer)entity;
                SednaPlayerPose.pose(playerRenderer.getMainModel(), player, age);
                akimbo(player, player.getHeldItemMainhand(), player.getPrimaryHand());
                akimbo(player, player.getHeldItemOffhand(), player.getPrimaryHand().opposite());
                if(!SednaPlayerPose.manly(player)) { original.doRenderLayer(entity,limb,amount,partial,age,yaw,pitch,scale); return; }
                ModelMan manly = manly(); ModelBiped model = playerRenderer.getMainModel();
                manly.rightArm.copyRotationFrom(model.bipedRightArm); manly.leftArm.copyRotationFrom(model.bipedLeftArm);
                ManlyHeld nativeHeld = new ManlyHeld(manly);
                ItemStack main = player.getHeldItemMainhand();
                if(!main.isEmpty() && player.fishEntity != null) main = new ItemStack(Items.STICK);
                ItemStack right = player.getPrimaryHand() == EnumHandSide.RIGHT ? main : player.getHeldItemOffhand();
                ItemStack left = player.getPrimaryHand() == EnumHandSide.LEFT ? main : player.getHeldItemOffhand();
                try(NativeProjectionScope scope = new NativeProjectionScope()) {
                    if(model.isChild) { GlStateManager.translate(0,.75F,0); GlStateManager.scale(.5F,.5F,.5F); }
                    invoke(HELD_ITEM,nativeHeld,player,right,TransformType.THIRD_PERSON_RIGHT_HAND,EnumHandSide.RIGHT);
                    invoke(HELD_ITEM,nativeHeld,player,left,TransformType.THIRD_PERSON_LEFT_HAND,EnumHandSide.LEFT);
                }
            }
            @Override public boolean shouldCombineTextures() { return original.shouldCombineTextures(); }
        };
    }
    private final class ManlyHeld extends LayerHeldItem {
        private final ModelMan model;
        ManlyHeld(ModelMan model) { super(playerRenderer); this.model = model; }
        @Override protected void translateToHand(EnumHandSide side) {
            if(side == EnumHandSide.RIGHT) model.rightArm.postRender(.0625F); else model.leftArm.postRender(.0625F);
        }
    }
    private void akimbo(EntityPlayer player, ItemStack stack, EnumHandSide primary) {
        ItemRenderWeaponBase gun = SednaHandHandler.renderer(stack);
        if(gun == null || !gun.isAkimbo(player) && !gun.isLeftHanded()) return;
        ModelBiped model = playerRenderer.getMainModel();
        EnumHandSide side = primary.opposite();
        ModelRenderer arm = side == EnumHandSide.LEFT ? model.bipedLeftArm : model.bipedRightArm;
        boolean hidden = arm.isHidden;
        try(NativeProjectionScope scope = new NativeProjectionScope()) {
            arm.isHidden = false; arm.postRender(.0625F);
            if(side == EnumHandSide.RIGHT) { GL11.glScalef(-1,1,1); GlStateManager.cullFace(GlStateManager.CullFace.FRONT); }
            GL11.glTranslatef(-.0625F,.4375F,.0625F);
            GL11.glTranslatef(.25F,.1875F,-.1875F); GL11.glScalef(.375F,.375F,.375F);
            GL11.glRotatef(60,0,0,1); GL11.glRotatef(-90,1,0,0); GL11.glRotatef(20,0,0,1);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL); GL11.glTranslatef(0,-.3F,0); GL11.glScalef(1.5F,1.5F,1.5F);
            GL11.glRotatef(50,0,1,0); GL11.glRotatef(335,0,0,1); GL11.glTranslatef(-.9375F,-.0625F,0);
            if(gun.isLeftHanded()) { GL11.glTranslatef(.1875F,0,0); gun.setupThirdPerson(stack); }
            else gun.setupThirdPersonAkimbo(stack);
            gun.renderEquippedAkimbo(stack,player);
        } finally { arm.isHidden = hidden; }
    }
}
