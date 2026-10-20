package com.hbm.render.block.nativeblock;

import java.util.*;
import com.hbm.blocks.network.PneumaticModelState;
import com.hbm.render.block.RenderPneumoTube;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;

/** Reload-scoped bounded cache. Extended state snapshots never retain a world or tile. */
public final class PneumaticBakedModel implements IBakedModel {
    private final TextureAtlasSprite[] materials;
    private final List<BakedQuad> inventory;
    private final Map<Integer,List<BakedQuad>> cache=new LinkedHashMap<Integer,List<BakedQuad>>(128,.75F,true){
        @Override protected boolean removeEldestEntry(Map.Entry<Integer,List<BakedQuad>> entry){return size()>128;}
    };
    public PneumaticBakedModel(boolean item,TextureAtlasSprite[] materials){this.materials=materials.clone();inventory=item?RenderPneumoTube.bake(PneumaticModelState.EMPTY,true,this.materials):null;}
    @Override public List<BakedQuad> getQuads(IBlockState state,EnumFacing side,long seed){
        if(side!=null)return Collections.emptyList();
        if(inventory!=null)return inventory;
        PneumaticModelState snapshot=state instanceof IExtendedBlockState?((IExtendedBlockState)state).getValue(PneumaticModelState.MODEL):null;
        if(snapshot==null)snapshot=PneumaticModelState.EMPTY;
        synchronized(cache){List<BakedQuad> quads=cache.get(snapshot.key());if(quads==null){quads=RenderPneumoTube.bake(snapshot,false,materials);cache.put(snapshot.key(),quads);}return quads;}
    }
    @Override public boolean isAmbientOcclusion(){return false;}
    @Override public boolean isGui3d(){return true;}
    @Override public boolean isBuiltInRenderer(){return false;}
    @Override public TextureAtlasSprite getParticleTexture(){return materials[0];}
    @Override public ItemCameraTransforms getItemCameraTransforms(){return ThermalModelTransforms.BLOCK;}
    @Override public ItemOverrideList getOverrides(){return ItemOverrideList.NONE;}
}
