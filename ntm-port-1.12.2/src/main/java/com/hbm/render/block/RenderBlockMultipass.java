package com.hbm.render.block;

import java.util.*;
import java.util.function.Function;
import com.hbm.blocks.MultipassContext;
import com.hbm.render.block.ct.RenderBlocksCT;
import com.hbm.render.block.multipass.MultipassMaterials;
import com.hbm.render.block.nativeblock.ConveyorCuboidGeometry;
import com.hbm.render.util.NativeBlockMaterial;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.lwjgl.util.vector.Vector3f;

/** Native layered full-cube model. There is deliberately no mutable global render pass. */
public final class RenderBlockMultipass implements IBakedModel {
    private final MultipassMaterials materials;
    private final Function<ResourceLocation,TextureAtlasSprite> sprites;
    private final Function<IBlockState,IBakedModel> camouflageModels;
    private final boolean item;
    private final int metadata;
    public RenderBlockMultipass(MultipassMaterials materials,Function<ResourceLocation,TextureAtlasSprite> sprites,
            Function<IBlockState,IBakedModel> camouflageModels,boolean item,int metadata) {
        this.materials=Objects.requireNonNull(materials); this.sprites=Objects.requireNonNull(sprites);
        this.camouflageModels=Objects.requireNonNull(camouflageModels); this.item=item; this.metadata=metadata;
    }
    @Override public List<BakedQuad> getQuads(IBlockState state,EnumFacing face,long seed) {
        boolean unculled=materials.kind==MultipassMaterials.Kind.ROUTER;
        if(item && face!=null || !item && unculled && face!=null || !item && !unculled && face==null) return Collections.emptyList();
        MultipassContext context=state instanceof IExtendedBlockState?((IExtendedBlockState)state).getValue(MultipassContext.PROPERTY):null;
        if(item || context==null) context=MultipassContext.basic(item?metadata:state==null?metadata:state.getBlock().getMetaFromState(state));
        List<BakedQuad> result=new ArrayList<>();
        for(int pass=0;pass<materials.passes(item);pass++) for(EnumFacing side:EnumFacing.values()) {
            if(face!=null && face!=side || !materials.visible(pass,side.getIndex(),item)) continue;
            TextureAtlasSprite sprite;
            if(materials.camouflage(context,pass,item)) {
                String material;
                if(context.paint.getBlock() instanceof com.hbm.blocks.BlockBase) {
                    com.hbm.render.icon.IIcon icon=((com.hbm.blocks.BlockBase)context.paint.getBlock()).getIcon(side.getIndex(),context.paint.getBlock().getMetaFromState(context.paint));
                    material=icon==null?"minecraft:missingno":MultipassMaterials.texture(icon.getIconName()).toString();
                } else {
                    IBakedModel paintModel=camouflageModels.apply(context.paint);
                    // An NTM multipass block's old metadata-only getIcon observed pass zero here.
                    // Its other layers are not part of the camouflage material.
                    if(paintModel instanceof RenderBlockMultipass) material=((RenderBlockMultipass)paintModel).baseSprite(context.paint.getBlock().getMetaFromState(context.paint),side).getIconName();
                    else material=NativeBlockMaterial.faceSprite(paintModel,context.paint,side);
                }
                if(material==null) throw new IllegalStateException("Ambiguous camouflage face material: "+context.paint+" / "+side+". No particle-texture fallback is authorized.");
                sprite=sprites.apply(new ResourceLocation(material));
            } else sprite=sprites.apply(materials.material(context,pass,side.getIndex(),item));
            BakedQuad quad=item?RenderBlocksCT.bakeInventory(sprite).get(side.getIndex()):
                ConveyorCuboidGeometry.face(sprite,false,side,0,0,0,0,1,1,1,0);
            int tint=materials.color(context,pass,item);
            if(tint!=0xffffff) {
                int[] data=quad.getVertexData().clone(); int stride=quad.getFormat().getIntegerSize();
                int color=quad.getFormat().getColorOffset()/4;
                int packed=0xff000000|(tint>>16&255)|(tint&0xff00)|(tint&255)<<16;
                for(int vertex=0;vertex<4;vertex++) data[vertex*stride+color]=packed;
                quad=new BakedQuad(data,-1,quad.getFace(),sprite,true,quad.getFormat());
            }
            result.add(quad);
        }
        return Collections.unmodifiableList(result);
    }
    public TextureAtlasSprite baseSprite(int metadata,EnumFacing side) {
        return sprites.apply(materials.material(MultipassContext.basic(metadata),0,side.getIndex(),true));
    }
    @Override public boolean isAmbientOcclusion() { return true; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean isBuiltInRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleTexture() { return sprites.apply(materials.material(MultipassContext.basic(metadata),0,0,true)); }
    @Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
    private static ItemTransformVec3f transform(float x,float y,float z,float ty,float scale) {
        return new ItemTransformVec3f(new Vector3f(x,y,z),new Vector3f(0,ty/16F,0),new Vector3f(scale,scale,scale));
    }
    private static final ItemCameraTransforms TRANSFORMS=new ItemCameraTransforms(
        transform(75,45,0,2.5F,.375F),transform(75,45,0,2.5F,.375F),transform(0,225,0,0,.4F),transform(0,45,0,0,.4F),
        ItemTransformVec3f.DEFAULT,transform(30,225,0,0,.625F),transform(0,0,0,3,.25F),transform(0,0,0,0,.5F));
    @Override public ItemCameraTransforms getItemCameraTransforms() { return TRANSFORMS; }
}
