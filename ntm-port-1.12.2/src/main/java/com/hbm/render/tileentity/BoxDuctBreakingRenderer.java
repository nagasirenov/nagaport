package com.hbm.render.tileentity;

import java.nio.ByteBuffer;
import java.util.List;
import com.hbm.blocks.network.BoxDuctRenderState;
import com.hbm.render.block.nativeblock.AtlasObjBakedModel;
import com.hbm.render.block.nativeblock.BoxDuctBakedModel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.vertex.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

/** Original box-duct damage ignored the incoming override texture, but retained standard-cube AO light. */
public class BoxDuctBreakingRenderer<T extends TileEntity> extends NetworkBreakingRenderer<T> {
    public BoxDuctBreakingRenderer() { super(false); }
    @Override public boolean owns(TileEntity tile,IBlockState state) { return BoxDuctRenderState.isNativeBreakingOwner(tile,state); }
    @Override public void render(T tile,double x,double y,double z,float partialTicks,int destroyStage,float alpha) {
        if(destroyStage<0||destroyStage>9||tile==null||tile.getWorld()==null)return;
        BlockPos pos=tile.getPos();IBlockState state=tile.getWorld().getBlockState(pos);if(!owns(tile,state))return;
        Minecraft minecraft=Minecraft.getMinecraft();BlockRendererDispatcher dispatcher=minecraft.getBlockRendererDispatcher();
        IBakedModel model=dispatcher.getModelForState(state);
        if(!(model instanceof BoxDuctBakedModel))throw new IllegalStateException("Missing native box-duct breaking model");
        IBlockState extended=state.getBlock().getExtendedState(state,tile.getWorld(),pos);
        BufferBuilder buffer=buildBuffer(tile.getWorld(),state,pos,model.getQuads(extended,null,0),Minecraft.isAmbientOcclusionEnabled(),
                dispatcher.getBlockModelRenderer(),x-pos.getX(),y-pos.getY(),z-pos.getZ());
        draw(buffer,minecraft.getTextureMapBlocks());
    }
    /** Actual native cube lighting first, then original damage Tessellator.noColor upload. */
    public static BufferBuilder buildBuffer(IBlockAccess world,IBlockState state,BlockPos pos,List<BakedQuad> quads,
            boolean ambient,BlockModelRenderer renderer,double x,double y,double z) {
        BufferBuilder source=new BufferBuilder(65536);source.begin(GL11.GL_QUADS,DefaultVertexFormats.BLOCK);source.setTranslation(x,y,z);
        if(!quads.isEmpty()) {
            IBakedModel model=new AtlasObjBakedModel(quads,quads.get(0).getSprite());
            if(ambient)renderer.renderModelSmooth(world,model,state,pos,source,true,0);
            else renderer.renderModelFlat(world,model,state,pos,source,true,0);
        }
        source.finishDrawing();
        VertexFormat format=new VertexFormat().addElement(DefaultVertexFormats.POSITION_3F).addElement(DefaultVertexFormats.TEX_2F).addElement(DefaultVertexFormats.TEX_2S);
        BufferBuilder output=new BufferBuilder(65536);output.begin(GL11.GL_QUADS,format);ByteBuffer data=source.getByteBuffer();
        for(int vertex=0;vertex<source.getVertexCount();vertex++) {
            int at=vertex*28;output.pos(data.getFloat(at),data.getFloat(at+4),data.getFloat(at+8)).tex(data.getFloat(at+16),data.getFloat(at+20))
                    .lightmap(data.getShort(at+26)&65535,data.getShort(at+24)&65535).endVertex();
        }
        output.finishDrawing();return output;
    }
}
