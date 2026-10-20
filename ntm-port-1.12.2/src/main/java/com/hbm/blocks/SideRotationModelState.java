package com.hbm.blocks;

import java.util.Arrays;
import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

/** Immutable six-face material/rotation snapshot; no live tile or world reaches the baked model. */
public final class SideRotationModelState {
    private SideRotationModelState() { }
    public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);
    public static final IUnlistedProperty<Snapshot> APPEARANCE = new IUnlistedProperty<Snapshot>() {
        @Override public String getName() { return "side_rotation_appearance"; }
        @Override public boolean isValid(Snapshot value) { return value != null; }
        @Override public Class<Snapshot> getType() { return Snapshot.class; }
        @Override public String valueToString(Snapshot value) { return value.toString(); }
    };
    public static final class Snapshot {
        private final ResourceLocation[] textures;
        private final int[] rotations;
        public Snapshot(ResourceLocation[] textures, int[] rotations) {
            if(textures.length != 6 || rotations.length != 6) throw new IllegalArgumentException("Six cube faces required");
            this.textures = textures.clone(); this.rotations = rotations.clone();
            for(ResourceLocation texture : this.textures) Objects.requireNonNull(texture, "Cube atlas texture");
        }
        public ResourceLocation texture(int side) { return textures[side]; }
        public int rotation(int side) { return rotations[side]; }
        @Override public boolean equals(Object other) {
            return other instanceof Snapshot && Arrays.equals(textures,((Snapshot)other).textures)
                    && Arrays.equals(rotations,((Snapshot)other).rotations);
        }
        @Override public int hashCode() { return 31 * Arrays.hashCode(textures) + Arrays.hashCode(rotations); }
        @Override public String toString() { return Arrays.toString(textures) + Arrays.toString(rotations); }
    }
    public static BlockStateContainer create(Block block) {
        return new ExtendedBlockState(block,new IProperty<?>[] {METADATA},new IUnlistedProperty<?>[] {APPEARANCE});
    }
    public static IBlockState extend(IBlockState state,IBlockAccess world,BlockPos pos) {
        IBlockSideRotation block=(IBlockSideRotation)state.getBlock();
        ResourceLocation[] textures=new ResourceLocation[6];int[] rotations=new int[6];
        for(int side=0;side<6;side++) {
            textures[side]=block.getModelTexture(world,pos.getX(),pos.getY(),pos.getZ(),side);
            rotations[side]=block.getRotationFromSide(world,pos.getX(),pos.getY(),pos.getZ(),side);
        }
        return ((IExtendedBlockState)state).withProperty(APPEARANCE,new Snapshot(textures,rotations));
    }
    public static Snapshot inventory(IBlockSideRotation block,int metadata) {
        ResourceLocation[] textures=new ResourceLocation[6];
        for(int side=0;side<6;side++) textures[side]=block.getModelTexture(side,metadata);
        // Original inventory callback never assigned any uvRotate fields.
        return new Snapshot(textures,new int[6]);
    }
    public static Snapshot appearance(IBlockState state) {
        if(!(state instanceof IExtendedBlockState)) throw new IllegalArgumentException("Rotated cube requires its extended state");
        Snapshot snapshot=((IExtendedBlockState)state).getValue(APPEARANCE);
        if(snapshot == null) throw new IllegalArgumentException("Rotated cube requires a material/rotation snapshot");
        return snapshot;
    }
}
