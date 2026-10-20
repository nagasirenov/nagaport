package com.hbm.render.item.weapon.sedna.nativeplayer;

import com.hbm.items.IAnimatedItem;
import com.hbm.items.armor.IArmorDisableModel;
import com.hbm.items.armor.IArmorDisableModel.EnumPlayerPart;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.packet.PermaSyncHandler;
import com.hbm.render.item.weapon.sedna.ItemRenderWeaponBase;
import com.hbm.render.item.weapon.sedna.nativehand.SednaHandHandler;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;

/** Original player-only gun pose and armor visibility, with explicit native entity arguments. */
public final class SednaPlayerPose {
    private SednaPlayerPose() { }
    public static boolean manly(EntityPlayer player) { return PermaSyncHandler.boykissers.contains(player.getEntityId()); }
    public static ItemRenderWeaponBase gun(EntityPlayer player) { return SednaHandHandler.renderer(player.getHeldItemMainhand()); }
    public static boolean manualArms(EntityPlayer player) {
        for(ItemStack stack : new ItemStack[]{player.getHeldItemMainhand(),player.getHeldItemOffhand()}) {
            ItemRenderWeaponBase gun = SednaHandHandler.renderer(stack);
            if(gun != null && (gun.isAkimbo(player) || gun.isLeftHanded())) return true;
        }
        return false;
    }
    private static boolean hidesGunArm(EntityPlayer player, EnumPlayerPart part) {
        ItemStack[] stacks = {player.getHeldItemMainhand(),player.getHeldItemOffhand()};
        for(int i=0;i<2;i++) {
            ItemRenderWeaponBase gun = SednaHandHandler.renderer(stacks[i]);
            if(gun == null || !gun.isAkimbo(player) && !gun.isLeftHanded()) continue;
            EnumHandSide primary = i == 0 ? player.getPrimaryHand() : player.getPrimaryHand().opposite();
            EnumPlayerPart second = primary == EnumHandSide.RIGHT ? EnumPlayerPart.LEFT_ARM : EnumPlayerPart.RIGHT_ARM;
            if(part == second || gun.isLeftHanded() && (part == EnumPlayerPart.LEFT_ARM || part == EnumPlayerPart.RIGHT_ARM)) return true;
        }
        return false;
    }
    private static boolean aims(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof ItemGunBaseNT
                || stack.getItem() instanceof IAnimatedItem && ((IAnimatedItem<?>)stack.getItem()).shouldPlayerModelAim(stack));
    }
    public static void pose(ModelBiped model, Entity entity, float age) { pose(model,entity,age,true); }
    public static void pose(ModelBiped model, Entity entity, float age, boolean manualArmOffsets) {
        if(!(entity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer)entity;
        ItemStack selected = player.getHeldItemMainhand();
        boolean off = !aims(selected) && aims(player.getHeldItemOffhand());
        if(off) selected = player.getHeldItemOffhand();
        if(!aims(selected)) return;
        ItemRenderWeaponBase gun = SednaHandHandler.renderer(selected);
        boolean mirror = (off ? player.getPrimaryHand().opposite() : player.getPrimaryHand()) == EnumHandSide.LEFT;
        // These are the original ModelBiped aimedBow equations, not a replacement
        // gameplay animation. Akimbo/left-handed adjustments match the old handler.
        float rightY = -.1F + model.bipedHead.rotateAngleY;
        float leftY = .5F + model.bipedHead.rotateAngleY;
        if(manualArmOffsets && gun != null && (gun.isAkimbo(player) || gun.isLeftHanded())) leftY = .1F + model.bipedHead.rotateAngleY;
        if(manualArmOffsets && gun != null && gun.isLeftHanded()) rightY = -.5F + model.bipedHead.rotateAngleY;
        float x = -(float)Math.PI / 2 + model.bipedHead.rotateAngleX;
        float z = MathHelper.cos(age * .09F) * .05F + .05F;
        float sway = MathHelper.sin(age * .067F) * .05F;
        if(!mirror) {
            model.bipedRightArm.rotateAngleY = rightY; model.bipedLeftArm.rotateAngleY = leftY;
            model.bipedRightArm.rotateAngleX = x + sway; model.bipedLeftArm.rotateAngleX = x - sway;
            model.bipedRightArm.rotateAngleZ = z; model.bipedLeftArm.rotateAngleZ = -z;
        } else {
            // Reflect the old offsets around the actual head yaw, not the world origin.
            model.bipedLeftArm.rotateAngleY = 2 * model.bipedHead.rotateAngleY - rightY;
            model.bipedRightArm.rotateAngleY = 2 * model.bipedHead.rotateAngleY - leftY;
            model.bipedLeftArm.rotateAngleX = x + sway; model.bipedRightArm.rotateAngleX = x - sway;
            model.bipedLeftArm.rotateAngleZ = -z; model.bipedRightArm.rotateAngleZ = z;
        }
        if(model instanceof ModelPlayer) {
            ModelPlayer playerModel = (ModelPlayer)model;
            net.minecraft.client.model.ModelBase.copyModelAngles(model.bipedLeftArm, playerModel.bipedLeftArmwear);
            net.minecraft.client.model.ModelBase.copyModelAngles(model.bipedRightArm, playerModel.bipedRightArmwear);
        }
    }
    public static ModelRenderer part(ModelBiped model, EnumPlayerPart part) {
        switch(part) {
        case HEAD: return model.bipedHead;
        case HAT: return model.bipedHeadwear;
        case BODY: return model.bipedBody;
        case LEFT_ARM: return model.bipedLeftArm;
        case RIGHT_ARM: return model.bipedRightArm;
        case LEFT_LEG: return model.bipedLeftLeg;
        case RIGHT_LEG: return model.bipedRightLeg;
        default: throw new AssertionError(part);
        }
    }
    public static void hide(ModelPlayer model, EntityPlayer player) { hide(model,player,true); }
    public static void hide(ModelPlayer model, EntityPlayer player, boolean hideGunArms) {
        boolean manly = manly(player);
        for(EnumPlayerPart part : EnumPlayerPart.values()) {
            boolean hide = manly;
            if(!manly) for(EntityEquipmentSlot slot : new EntityEquipmentSlot[]{EntityEquipmentSlot.FEET, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.HEAD}) {
                ItemStack armor = player.getItemStackFromSlot(slot);
                if(!armor.isEmpty() && armor.getItem() instanceof IArmorDisableModel
                        && ((IArmorDisableModel)armor.getItem()).disablesPart(player, armor, part)) hide = true;
            }
            if(hideGunArms && hidesGunArm(player,part)) hide = true;
            if(hide) {
                part(model, part).isHidden = true;
                switch(part) {
                case BODY: model.bipedBodyWear.isHidden = true; break;
                case LEFT_ARM: model.bipedLeftArmwear.isHidden = true; break;
                case RIGHT_ARM: model.bipedRightArmwear.isHidden = true; break;
                case LEFT_LEG: model.bipedLeftLegwear.isHidden = true; break;
                case RIGHT_LEG: model.bipedRightLegwear.isHidden = true; break;
                default: break;
                }
            }
        }
    }
}
