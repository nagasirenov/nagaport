package com.hbm.render.tileentity;

import com.hbm.blocks.machine.FoundryModelState;
import com.hbm.render.block.nativeblock.*;
import com.hbm.render.item.nativeitem.context.NativeProjectionScope;
import com.hbm.tileentity.machine.TileEntityFoundryBase;
import com.hbm.tileentity.machine.TileEntityFoundryTank;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BakedQuadRetextured;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/** Only the original channel/tank molten faces, using their exact packed light 240 rather than a chunk lighter's raised sky channel. */
public final class RenderFoundryFluid extends TileEntitySpecialRenderer<TileEntityFoundryBase> {
    // Original damage pass called Tessellator.disableColor: retain its neutral
    // current color rather than multiplying cracks by molten/shadow vertex colors.
    private static final net.minecraft.client.renderer.vertex.VertexFormat DAMAGE_FORMAT=
            new net.minecraft.client.renderer.vertex.VertexFormat()
                    .addElement(DefaultVertexFormats.POSITION_3F)
                    .addElement(DefaultVertexFormats.TEX_2F)
                    .addElement(DefaultVertexFormats.TEX_2S);
    @Override public void render(TileEntityFoundryBase tile,double x,double y,double z,float partial,int destroyStage,float alpha) {
        if(tile==null||tile.getWorld()==null)return;
        IBlockState state=tile.getWorld().getBlockState(tile.getPos());
        FoundryModelState data=FoundryModelState.get(state.getBlock().getExtendedState(state,tile.getWorld(),tile.getPos()));
        FoundryGeometry.Kind kind=tile instanceof TileEntityFoundryTank?FoundryGeometry.Kind.TANK:FoundryGeometry.Kind.CHANNEL;
        TextureMap atlas=Minecraft.getMinecraft().getTextureMapBlocks();
        FoundryMaterials materials=new FoundryMaterials(kind,key->atlas.getAtlasSprite(key.toString()));
        java.util.List<BakedQuad> quads=geometry(kind,state.getBlock().getMetaFromState(state),data,materials,
                destroyStage<0?null:atlas.getAtlasSprite("minecraft:blocks/destroy_stage_"+destroyStage),
                tile.getWorld().getCombinedLight(tile.getPos(),0));
        if(quads.isEmpty())return;
        try(NativeProjectionScope scope=new NativeProjectionScope()) {
            GlStateManager.translate(x,y,z);
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.disableLighting();
            // RenderGlobal owns the damage blend and polygon offset around its TESR pass.
            if(destroyStage<0)GlStateManager.disableBlend();
            GlStateManager.enableCull();GlStateManager.enableAlpha();GlStateManager.depthMask(true);
            GlStateManager.color(1,1,1,destroyStage<0?1:.5F);
            BufferBuilder buffer=Tessellator.getInstance().getBuffer();
            buffer.begin(GL11.GL_QUADS,destroyStage<0?DefaultVertexFormats.BLOCK:DAMAGE_FORMAT);
            for(BakedQuad quad:quads) {
                int[] vertices=quad.getVertexData();
                if(destroyStage<0)buffer.addVertexData(vertices);
                else for(int vertex=0;vertex<4;vertex++) {
                    int index=vertex*7,light=vertices[index+6];
                    buffer.pos(Float.intBitsToFloat(vertices[index]),Float.intBitsToFloat(vertices[index+1]),Float.intBitsToFloat(vertices[index+2]))
                            .tex(Float.intBitsToFloat(vertices[index+4]),Float.intBitsToFloat(vertices[index+5]))
                            .lightmap(light&65535,light>>>16).endVertex();
                }
            }
            Tessellator.getInstance().draw();
        }
    }
    /** Native crack retexturing preserves the original partial-face UV interval.
     * Exactly one full damage model is emitted; normal chunk housing is unchanged. */
    static java.util.List<BakedQuad> geometry(FoundryGeometry.Kind kind,int metadata,FoundryModelState data,
            FoundryMaterials materials,TextureAtlasSprite damage,int packedLight) {
        java.util.List<BakedQuad> result=new java.util.ArrayList<>();
        if(damage!=null)for(BakedQuad housing:FoundryGeometry.build(kind,false,false,metadata,data,materials)) {
            int[] vertices=housing.getVertexData().clone();
            // Native ITEM and BLOCK share XYZ/color/UV0; only the final int changes from normal to UV1.
            for(int vertex=0;vertex<4;vertex++)vertices[vertex*7+6]=packedLight;
            BakedQuad lit=new BakedQuad(vertices,-1,housing.getFace(),housing.getSprite(),false,DefaultVertexFormats.BLOCK);
            result.add(new BakedQuadRetextured(lit,damage));
        }
        for(BakedQuad fluid:FoundryGeometry.build(kind,false,true,metadata,data,materials))
            result.add(damage==null?fluid:new BakedQuadRetextured(fluid,damage));
        return result;
    }

}
