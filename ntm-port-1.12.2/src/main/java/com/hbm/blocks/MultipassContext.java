package com.hbm.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraftforge.common.property.*;

/** Immutable render snapshot, with no tile/world reference and no mutable current pass. */
public final class MultipassContext {
    public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);
    public static final IUnlistedProperty<MultipassContext> PROPERTY = new IUnlistedProperty<MultipassContext>() {
        public String getName() { return "multipass"; }
        public boolean isValid(MultipassContext value) { return value != null; }
        public Class<MultipassContext> getType() { return MultipassContext.class; }
        public String valueToString(MultipassContext value) { return value.toString(); }
    };
    public final int metadata, color, shape, ejection, insertion, variation;
    public final IBlockState paint;
    public final boolean tile;
    private MultipassContext(int metadata,IBlockState paint,boolean tile,int color,int shape,int ejection,int insertion,int variation) {
        this.metadata=metadata; this.paint=paint; this.tile=tile; this.color=color;
        this.shape=shape; this.ejection=ejection; this.insertion=insertion; this.variation=variation;
    }
    public static MultipassContext of(int metadata,IBlockState paint,boolean tile,int color,int shape,int ejection,int insertion,int variation) {
        return new MultipassContext(metadata,paint,tile,color,shape,ejection,insertion,variation);
    }
    public static MultipassContext basic(int metadata) { return of(metadata,null,false,0xffffff,0,6,6,0); }
    public static BlockStateContainer stateContainer(Block owner) {
        return new ExtendedBlockState(owner,new IProperty<?>[]{METADATA},new IUnlistedProperty<?>[]{PROPERTY});
    }
}
