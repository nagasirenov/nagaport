package com.hbm.render.item.weapon.sedna.nativeplayer;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;

/** Native armor still owns slot visibility, texture, dye, hooks and glint. */
public final class SednaArmorModel extends ModelBiped {
    public SednaArmorModel() { super(); }
    @Override public void setRotationAngles(float limb, float amount, float age, float yaw, float pitch, float scale, Entity entity) {
        super.setRotationAngles(limb, amount, age, yaw, pitch, scale, entity);
        // Vanilla ModelBiped.render recomputed the old temporary SetArmorModel Y
        // adjustments. Keep its bow pose, unlike manually drawn skin/NTM armor.
        SednaPlayerPose.pose(this, entity, age, false);
    }
}
