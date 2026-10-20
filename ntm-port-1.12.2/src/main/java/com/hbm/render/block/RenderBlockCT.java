package com.hbm.render.block;

import java.util.*;
import com.hbm.render.block.ct.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.lwjgl.util.vector.Vector3f;

/** Native CT model. Each world request owns its context; baking never mutates a global renderer. */
public final class RenderBlockCT implements IBakedModel {
    private final TextureAtlasSprite full;
    private final List<BakedQuad> inventory;
    private final List<BakedQuad>[][][] faces;
    @SuppressWarnings("unchecked")
    public RenderBlockCT(TextureAtlasSprite full, TextureAtlasSprite connected, TextureAtlasSprite portFull, TextureAtlasSprite portConnected, boolean item) {
        this.full=Objects.requireNonNull(full,"full");
        this.inventory=item ? RenderBlocksCT.bakeInventory(full) : null;
        faces=new List[2][6][256];
        if(!item) for(int material=0;material<2;material++) for(EnumFacing side:EnumFacing.values()) {
            if(material==1 && portFull==null) { faces[1][side.getIndex()]=faces[0][side.getIndex()]; continue; }
            TextureAtlasSprite base=material==1?portFull:full, joined=material==1?portConnected:connected;
            BakedQuad reference=RenderBlocksCT.fullFace(base,side);
            BakedQuad[] fragments=new BakedQuad[20];
            for(int mask=0;mask<256;mask++) {
                List<BakedQuad> raw=RenderBlocksCT.bakeFace(base,joined,side,mask);
                List<BakedQuad> lit=new ArrayList<>(4);
                for(int quadrant=0;quadrant<4;quadrant++) {
                    int index=CTContext.fragment(mask,quadrant);
                    if(fragments[index]==null) fragments[index]=new CTLightQuad(raw.get(quadrant),reference,quadrant);
                    lit.add(fragments[index]);
                }
                faces[material][side.getIndex()][mask]=Collections.unmodifiableList(lit);
            }
        }
    }
    @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long seed) {
        if(inventory!=null) return side==null?inventory:Collections.emptyList();
        if(side==null) return Collections.emptyList();
        CTContext context=state instanceof IExtendedBlockState?((IExtendedBlockState)state).getValue(CTContext.PROPERTY):null;
        return faces[context==null?0:context.material()][side.getIndex()][context==null?0:context.mask(side)];
    }
    @Override public boolean isAmbientOcclusion() { return true; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean isBuiltInRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleTexture() { return full; }
    @Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
    private static ItemTransformVec3f transform(float x,float y,float z,float ty,float scale) {
        return new ItemTransformVec3f(new Vector3f(x,y,z),new Vector3f(0,ty/16F,0),new Vector3f(scale,scale,scale));
    }
    private static final ItemCameraTransforms TRANSFORMS=new ItemCameraTransforms(
        transform(75,45,0,2.5F,.375F),transform(75,45,0,2.5F,.375F),transform(0,225,0,0,.4F),transform(0,45,0,0,.4F),
        ItemTransformVec3f.DEFAULT,transform(30,225,0,0,.625F),transform(0,0,0,3,.25F),transform(0,0,0,0,.5F));
    @Override public ItemCameraTransforms getItemCameraTransforms() { return TRANSFORMS; }
}
