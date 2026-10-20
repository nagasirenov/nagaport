package com.hbm.tileentity;
/** Explicit outside-owner packet/base fixture; no SolarMirror owner is substituted. */
public abstract class TileEntityTickingBase extends net.minecraft.tileentity.TileEntity implements net.minecraft.util.ITickable {
    public int packets,range,dirty;
    public abstract String getInventoryName();
    public void serialize(io.netty.buffer.ByteBuf buffer) { }
    public void deserialize(io.netty.buffer.ByteBuf buffer) { }
    public void networkPackNT(int range){packets++;this.range=range;}
    @Override public void markDirty(){dirty++;}
}
