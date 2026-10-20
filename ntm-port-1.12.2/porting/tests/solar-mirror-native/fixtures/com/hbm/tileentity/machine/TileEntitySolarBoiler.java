package com.hbm.tileentity.machine;
/** Explicit outside-owner destination fixture for mirror heat and beam collection. */
public class TileEntitySolarBoiler extends net.minecraft.tileentity.TileEntity {
    public int heat;
    public java.util.Set<SolarMirrorPosition> primary=new java.util.HashSet<>();
}
