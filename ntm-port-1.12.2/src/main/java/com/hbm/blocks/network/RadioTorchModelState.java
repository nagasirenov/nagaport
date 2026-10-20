package com.hbm.blocks.network;

import net.minecraft.block.properties.PropertyInteger;
import net.minecraftforge.common.property.IUnlistedProperty;

/** The immutable Boolean value snapshots volatile tile state for chunk workers. */
public final class RadioTorchModelState {
    private RadioTorchModelState() { }
    public static final PropertyInteger META = PropertyInteger.create("metadata", 0, 15);
    public static final IUnlistedProperty<Boolean> ACTIVE = new IUnlistedProperty<Boolean>() {
        public String getName() { return "radio_active"; }
        public boolean isValid(Boolean value) { return value != null; }
        public Class<Boolean> getType() { return Boolean.class; }
        public String valueToString(Boolean value) { return value.toString(); }
    };
}
