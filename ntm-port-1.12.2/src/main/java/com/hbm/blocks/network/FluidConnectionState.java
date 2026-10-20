package com.hbm.blocks.network;

import net.minecraftforge.common.property.IUnlistedProperty;

/** Fluid tint is render-only and must never overwrite persisted duct metadata. */
public final class FluidConnectionState {
    private FluidConnectionState() { }
    public static final IUnlistedProperty<Integer> FLUID_COLOR = new IUnlistedProperty<Integer>() {
        @Override public String getName() { return "fluid_color"; }
        @Override public boolean isValid(Integer value) { return value != null; }
        @Override public Class<Integer> getType() { return Integer.class; }
        @Override public String valueToString(Integer value) { return value.toString(); }
    };
}
