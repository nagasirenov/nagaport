package com.hbm.render.item.nativeitem.context;

import java.util.Objects;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.EntityLivingBase;

/** Immutable native draw inputs, never published through global or thread-local state. */
public final class NativeItemContext {
    public final TransformType transform;
    public final EntityLivingBase holder;
    public NativeItemContext(TransformType transform, EntityLivingBase holder) {
        this.transform = Objects.requireNonNull(transform, "transform");
        this.holder = holder;
    }
    public boolean isFirstPerson() {
        return transform == TransformType.FIRST_PERSON_LEFT_HAND || transform == TransformType.FIRST_PERSON_RIGHT_HAND;
    }
}
