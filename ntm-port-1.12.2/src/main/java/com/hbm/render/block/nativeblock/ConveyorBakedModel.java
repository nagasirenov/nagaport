package com.hbm.render.block.nativeblock;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.hbm.blocks.network.ConveyorModelState;
import com.hbm.render.block.RenderConveyor;
import com.hbm.render.block.RenderConveyorChute;
import com.hbm.render.block.RenderConveyorLift;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

/** Native AO-enabled model. Only lift/chute geometry depends on the immutable neighbor snapshot. */
public final class ConveyorBakedModel implements IBakedModel {
    public enum Kind { BELT, CLASSIC, LIFT, CHUTE }
    private final Kind kind;
    private final int metadata;
    private final boolean inventory;
    private final ConveyorMaterials materials;
    private final List<BakedQuad> fixed;
    private final Map<Integer,List<BakedQuad>> neighbors = new ConcurrentHashMap<>();
    public ConveyorBakedModel(Kind kind, int metadata, boolean inventory, ConveyorMaterials materials) {
        this.kind = kind; this.metadata = metadata; this.inventory = inventory; this.materials = materials;
        if(kind == Kind.LIFT && inventory) throw new IllegalArgumentException("Lift inventory uses its original flat icon route");
        fixed = kind == Kind.BELT || kind == Kind.CLASSIC ? RenderConveyor.bake(metadata,inventory,kind == Kind.BELT,materials)
                : inventory ? RenderConveyorChute.bake(metadata,0,true,materials) : null;
    }
    @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long seed) {
        if(kind == Kind.BELT || kind == Kind.CLASSIC) {
            // Original quarter-height block culls bottom/horizontal faces, never its recessed top.
            // RenderItem requests all buckets; inventory geometry must appear exactly once.
            if(inventory) return side == null ? fixed : Collections.emptyList();
            return side == null ? Collections.singletonList(fixed.get(EnumFacing.UP.getIndex()))
                    : side == EnumFacing.UP ? Collections.emptyList() : Collections.singletonList(fixed.get(side.getIndex()));
        }
        if(side != null) return Collections.emptyList(); // Lift/chute explicitly rendered every original side.
        if(inventory) return fixed;
        int snapshot = ConveyorModelState.neighbors(state);
        return neighbors.computeIfAbsent(snapshot, value -> kind == Kind.LIFT ? RenderConveyorLift.bake(metadata,value,materials)
                : RenderConveyorChute.bake(metadata,value,false,materials));
    }
    @Override public boolean isAmbientOcclusion() { return true; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean isBuiltInRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleTexture() { return materials.beltFaces(metadata,kind == Kind.BELT)[0]; }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return ThermalModelTransforms.BLOCK; }
    @Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
}
