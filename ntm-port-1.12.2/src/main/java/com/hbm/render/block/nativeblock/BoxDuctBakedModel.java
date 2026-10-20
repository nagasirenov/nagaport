package com.hbm.render.block.nativeblock;

import java.util.*;
import javax.vecmath.Matrix4f;
import org.apache.commons.lang3.tuple.Pair;
import com.hbm.blocks.network.ConnectionState;
import com.hbm.blocks.network.FluidConnectionState;
import com.hbm.render.block.RenderBoxDuct;
import com.hbm.render.item.nativeitem.ItemTransformMatrix;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;

/** Per-reload bounded immutable cache: only connection bits and final RGB, never live worlds/tiles. */
public final class BoxDuctBakedModel implements IBakedModel {
    private static final Matrix4f FIXED = new ItemTransformMatrix().scale(2).translate(0,-.06,0)
            .rotateY(180).scale(1.25).translate(0,.05,0).rotateY(-90).scale(.25).build();
    private final BoxDuctMaterials materials;
    private final int metadata;
    private final boolean inventory;
    private final Map<Long,List<BakedQuad>> cache=new LinkedHashMap<Long,List<BakedQuad>>(128,.75F,true) {
        @Override protected boolean removeEldestEntry(Map.Entry<Long,List<BakedQuad>> entry) { return size()>128; }
    };
    public BoxDuctBakedModel(BoxDuctMaterials materials,int metadata,boolean inventory) {
        this.materials=materials;this.metadata=metadata;this.inventory=inventory;
    }
    @Override public List<BakedQuad> getQuads(IBlockState state,EnumFacing side,long seed) {
        if(side!=null) return Collections.emptyList();
        int mask=0,color=0xffffff;
        if(!inventory && state instanceof IExtendedBlockState) {
            IExtendedBlockState extended=(IExtendedBlockState)state;
            Integer connections=extended.getValue(ConnectionState.CONNECTIONS);if(connections!=null)mask=connections;
            if(materials.kind!=BoxDuctMaterials.Kind.CABLE) { Integer rgb=extended.getValue(FluidConnectionState.FLUID_COLOR);if(rgb!=null)color=rgb; }
        }
        long key=(long)mask<<32 | color&0xffffffffL;
        synchronized(cache) {
            List<BakedQuad> quads=cache.get(key);
            if(quads==null) { quads=RenderBoxDuct.bake(materials,metadata,mask,inventory,color);cache.put(key,quads); }
            return quads;
        }
    }
    @Override public boolean isAmbientOcclusion() { return true; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean isBuiltInRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleTexture() { return materials.particle; }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return ThermalModelTransforms.BLOCK; }
    @Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
    @Override public Pair<? extends IBakedModel,Matrix4f> handlePerspective(TransformType type) {
        return inventory&&type==TransformType.FIXED?Pair.of(this,new Matrix4f(FIXED)):IBakedModel.super.handlePerspective(type);
    }
}
