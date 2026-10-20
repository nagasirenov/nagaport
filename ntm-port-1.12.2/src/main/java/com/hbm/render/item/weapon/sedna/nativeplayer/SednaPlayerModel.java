package com.hbm.render.item.weapon.sedna.nativeplayer;

import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

/** Full-body lexical phase only. Native renderRightArm/renderLeftArm never enter
 * this phase, so an ordinary first-person empty hand is not given a third-person pose. */
public final class SednaPlayerModel extends ModelPlayer {
    private int bodyDepth;
    public SednaPlayerModel(boolean slim) { super(0, slim); }
    @Override public void render(Entity entity, float limb, float amount, float age, float yaw, float pitch, float scale) {
        ModelRenderer[] parts = {bipedHead,bipedHeadwear,bipedBody,bipedLeftArm,bipedRightArm,bipedLeftLeg,bipedRightLeg,
                bipedBodyWear,bipedLeftArmwear,bipedRightArmwear,bipedLeftLegwear,bipedRightLegwear};
        boolean[] hidden = new boolean[parts.length];
        for(int i = 0; i < parts.length; i++) hidden[i] = parts[i].isHidden;
        bodyDepth++;
        try {
            if(entity instanceof EntityPlayer) SednaPlayerPose.hide(this, (EntityPlayer)entity);
            super.render(entity, limb, amount, age, yaw, pitch, scale);
        } finally {
            bodyDepth--;
            for(int i = 0; i < parts.length; i++) parts[i].isHidden = hidden[i];
        }
    }
    @Override public void setRotationAngles(float limb, float amount, float age, float yaw, float pitch, float scale, Entity entity) {
        super.setRotationAngles(limb, amount, age, yaw, pitch, scale, entity);
        if(bodyDepth > 0) SednaPlayerPose.pose(this, entity, age);
    }
}
