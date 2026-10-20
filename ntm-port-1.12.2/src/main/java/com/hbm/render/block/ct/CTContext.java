package com.hbm.render.block.ct;

import java.util.Arrays;
import net.minecraft.block.Block;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.*;

/** Thread-safe value captured before baking. No static render context or world reference. */
public final class CTContext {
    public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);
    public static final IUnlistedProperty<CTContext> PROPERTY = new IUnlistedProperty<CTContext>() {
        public String getName() { return "connected_texture"; }
        public boolean isValid(CTContext value) { return value != null; }
        public Class<CTContext> getType() { return CTContext.class; }
        public String valueToString(CTContext value) { return value.toString(); }
    };
    private static final EnumFacing[] UP = {EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.UP, EnumFacing.UP, EnumFacing.UP, EnumFacing.UP};
    private static final EnumFacing[] LEFT = {EnumFacing.WEST, EnumFacing.WEST, EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH};
    private static final int[] HORIZONTAL = {1,0,-1,1,-1,1,0,-1};
    private static final int[] VERTICAL = {1,1,1,0,0,-1,-1,-1};
    private final long masks;
    private final int material;
    private CTContext(long masks, int material) { this.masks = masks; this.material = material; }
    public static CTContext fromMasks(int[] masks) { return fromMasks(masks, 0); }
    public static CTContext fromMasks(int[] masks, int material) {
        if(masks.length != 6) throw new IllegalArgumentException("Six CT face masks required");
        if(material < 0 || material > 1) throw new IllegalArgumentException("Unknown CT material " + material);
        long packed = 0;
        for(int face = 0; face < 6; face++) {
            if((masks[face] & ~255) != 0) throw new IllegalArgumentException("CT mask must fit eight neighbors");
            packed |= (long)masks[face] << (face * 8);
        }
        return new CTContext(packed, material);
    }
    public static CTContext capture(IBlockAccess world, BlockPos pos, IBlockCT owner) {
        int[] masks = new int[6];
        for(EnumFacing face : EnumFacing.values()) for(int neighbor = 0; neighbor < 8; neighbor++) {
            BlockPos at = pos.add(offset(face, neighbor));
            if(owner.canConnect(world, at.getX(), at.getY(), at.getZ(), world.getBlockState(at).getBlock())) masks[face.getIndex()] |= 1 << neighbor;
        }
        return fromMasks(masks, owner.getCTMaterial(world.getBlockState(pos)));
    }
    /** The original lexical TL,TC,TR,CL,CR,BL,BC,BR orientation, including top/bottom. */
    public static BlockPos offset(EnumFacing face, int neighbor) {
        if(neighbor < 0 || neighbor > 7) throw new IllegalArgumentException("CT neighbor must be 0..7");
        EnumFacing up = UP[face.getIndex()], left = LEFT[face.getIndex()];
        return new BlockPos(up.getXOffset()*VERTICAL[neighbor]+left.getXOffset()*HORIZONTAL[neighbor],
                up.getYOffset()*VERTICAL[neighbor]+left.getYOffset()*HORIZONTAL[neighbor],
                up.getZOffset()*VERTICAL[neighbor]+left.getZOffset()*HORIZONTAL[neighbor]);
    }
    public int mask(EnumFacing face) { return (int)(masks >>> (face.getIndex()*8)) & 255; }
    public int material() { return material; }
    public int fragment(EnumFacing face, int quadrant) { return fragment(mask(face), quadrant); }
    public static int fragment(int mask, int quadrant) {
        int horizontal, diagonal, vertical;
        switch(quadrant) {
            case 0: horizontal=3; diagonal=0; vertical=1; break;
            case 1: horizontal=4; diagonal=2; vertical=1; break;
            case 2: horizontal=3; diagonal=5; vertical=6; break;
            case 3: horizontal=4; diagonal=7; vertical=6; break;
            default: throw new IllegalArgumentException("CT quadrant must be 0..3");
        }
        return quadrant | cornerType((mask & (1 << horizontal)) != 0, (mask & (1 << diagonal)) != 0, (mask & (1 << vertical)) != 0);
    }
    public static int cornerType(boolean hor, boolean corner, boolean vert) {
        return vert && hor && corner ? CT.c : vert && hor ? CT.j : vert ? CT.v : hor ? CT.h : CT.f;
    }
    public static BlockStateContainer stateContainer(Block owner) {
        return new ExtendedBlockState(owner, new IProperty<?>[] {METADATA}, new IUnlistedProperty<?>[] {PROPERTY});
    }
    public static IBlockState extend(IBlockState state, IBlockAccess world, BlockPos pos, IBlockCT owner) {
        return ((IExtendedBlockState)state).withProperty(PROPERTY, capture(world, pos, owner));
    }
    @Override public boolean equals(Object other) { return other instanceof CTContext && ((CTContext)other).masks == masks && ((CTContext)other).material == material; }
    @Override public int hashCode() { return 31 * Long.hashCode(masks) + material; }
    @Override public String toString() { return Long.toHexString(masks) + ":" + material; }
}
