package com.hbm.render.block.nativeblock;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.hbm.blocks.SideRotationModelState;
import com.hbm.blocks.SideRotationModelState.Snapshot;
import com.hbm.render.block.RenderBlockSideRotation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

/** Native smooth AO and face culling over immutable owner-selected atlas materials and UV rotations. */
public final class SideRotationBakedModel implements IBakedModel {
    private final boolean inventory;
    private final Map<ResourceLocation,TextureAtlasSprite> sprites;
    private final TextureAtlasSprite particle;
    private final List<BakedQuad> fixed;
    private final Map<Snapshot,List<BakedQuad>> appearances=new ConcurrentHashMap<>();
    public SideRotationBakedModel(Snapshot item,boolean inventory,Map<ResourceLocation,TextureAtlasSprite> sprites) {
        this.inventory=inventory;this.sprites=Collections.unmodifiableMap(new HashMap<>(sprites));
        particle=texture(item.texture(0));
        fixed=inventory ? RenderBlockSideRotation.bake(item,true,this::texture) : null;
    }
    private TextureAtlasSprite texture(ResourceLocation name) {
        TextureAtlasSprite sprite=sprites.get(name);
        if(sprite==null) throw new IllegalArgumentException("Undeclared rotated-cube atlas material "+name);
        return sprite;
    }
    @Override public List<BakedQuad> getQuads(IBlockState state,EnumFacing side,long seed) {
        if(inventory) return side==null ? fixed : Collections.emptyList();
        if(side==null) return Collections.emptyList();
        Snapshot snapshot=SideRotationModelState.appearance(state);
        List<BakedQuad> faces=appearances.computeIfAbsent(snapshot,value->RenderBlockSideRotation.bake(value,false,this::texture));
        return Collections.singletonList(faces.get(side.getIndex()));
    }
    @Override public boolean isAmbientOcclusion() { return true; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean isBuiltInRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleTexture() { return particle; }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return ThermalModelTransforms.BLOCK; }
    @Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
}
