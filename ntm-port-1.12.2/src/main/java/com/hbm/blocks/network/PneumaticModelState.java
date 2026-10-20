package com.hbm.blocks.network;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.property.IUnlistedProperty;

/** Immutable rendering snapshot. Direction 6 retains the original UNKNOWN sentinel. */
public final class PneumaticModelState {
    public final int tubes, air, insertion, ejection;
    public PneumaticModelState(int tubes, int air, int insertion, int ejection) {
        if((tubes & ~63) != 0 || (air & ~63) != 0 || insertion < 0 || insertion > 6 || ejection < 0 || ejection > 6)
            throw new IllegalArgumentException("Invalid pneumatic snapshot");
        this.tubes=tubes;this.air=air;this.insertion=insertion;this.ejection=ejection;
    }
    public static final PneumaticModelState EMPTY = new PneumaticModelState(0,0,6,6);
    public static final IUnlistedProperty<PneumaticModelState> MODEL = new IUnlistedProperty<PneumaticModelState>() {
        public String getName(){return "pneumatic";}
        public boolean isValid(PneumaticModelState v){return v!=null;}
        public Class<PneumaticModelState> getType(){return PneumaticModelState.class;}
        public String valueToString(PneumaticModelState v){return Integer.toString(v.key());}
    };
    public static int bit(int direction) {return direction==6?0:new int[]{4,8,1,2,16,32}[direction];}
    public boolean hasPorts(){return insertion!=6 || ejection!=6;}
    public int key(){return tubes | air<<6 | insertion<<12 | ejection<<15;}
    public AxisAlignedBB bounds() {
        int mask=tubes|air|bit(insertion)|bit(ejection);
        return new AxisAlignedBB((mask&16)!=0?0:.3125,(mask&4)!=0?0:.3125,(mask&1)!=0?0:.3125,
                (mask&32)!=0?1:.6875,(mask&8)!=0?1:.6875,(mask&2)!=0?1:.6875);
    }
    public static AxisAlignedBB arm(EnumFacing side, double lo, double hi, double from, double to) {
        switch(side) {
        case DOWN:return new AxisAlignedBB(lo,1-to,lo,hi,1-from,hi);
        case UP:return new AxisAlignedBB(lo,from,lo,hi,to,hi);
        case NORTH:return new AxisAlignedBB(lo,lo,1-to,hi,hi,1-from);
        case SOUTH:return new AxisAlignedBB(lo,lo,from,hi,hi,to);
        case WEST:return new AxisAlignedBB(1-to,lo,lo,1-from,hi,hi);
        case EAST:return new AxisAlignedBB(from,lo,lo,to,hi,hi);
        default:throw new AssertionError(side);
        }
    }
}
