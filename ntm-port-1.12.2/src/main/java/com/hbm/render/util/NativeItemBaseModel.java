package com.hbm.render.util;

import net.minecraft.client.renderer.block.model.IBakedModel;

/** Original baked geometry retained by an NTM custom item model. This is model
 * ownership metadata, never a last stack, holder, or global render context. */
public interface NativeItemBaseModel {
    IBakedModel getBaseModel();
}
