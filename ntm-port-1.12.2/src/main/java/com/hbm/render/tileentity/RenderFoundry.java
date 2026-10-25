package com.hbm.render.tileentity;

import java.awt.Color;
import java.util.List;
import com.hbm.lib.RefStrings;
import com.hbm.render.item.nativeitem.context.NativeProjectionScope;
import com.hbm.render.util.RenderDecoItem;
import com.hbm.tileentity.machine.IRenderFoundry;
import com.hbm.wiaj.WorldInAJar;
import com.hbm.wiaj.actors.ITileActorRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Native basin/mold TESR. Original item, output-top and two molten passes remain distinct. */
public class RenderFoundry extends TileEntitySpecialRenderer<TileEntity> implements ITileActorRenderer {
    private final RenderDecoItem itemRenderer=new RenderDecoItem();
    private static final java.util.Set<Block> NO_TOP_WARNED=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<Block,Boolean>());
    public static final ResourceLocation lava=new ResourceLocation(RefStrings.MODID,"textures/models/machines/lava_gray.png");
    private void drawItem(ItemStack stack,double height) {
        try(NativeProjectionScope scope=new NativeProjectionScope()) {
            GlStateManager.translate(.125D,height,.125D);
            double scale=.0625D*12D/16D;GlStateManager.scale(scale,scale,scale);GlStateManager.rotate(90,1,0,0);
            RenderHelper.enableGUIStandardItemLighting();
            Minecraft mc=Minecraft.getMinecraft();RenderItem renderer=mc.getRenderItem();
            IBakedModel selected=renderer.getItemModelWithOverrides(stack,null,null);
            AtlasFilterState filters=new AtlasFilterState(mc.getTextureManager());
            float previous=renderer.zLevel;renderer.zLevel=0;
            try {
                // Original Forge custom callback won; the ordinary fallback was the no-effect GUI entry.
                if(selected.isBuiltInRenderer())renderer.renderItemIntoGUI(stack,1,0);
                else itemRenderer.renderGui(stack,0,0,false,false);
            } finally { renderer.zLevel=previous;filters.restore(); }
        }
        RenderHelper.enableStandardItemLighting();GlStateManager.enableAlpha();
    }
    /** Vanilla GUI methods do not restore these native object caches when a custom draw throws. */
    private static final class AtlasFilterState {
        private static final java.lang.reflect.Field[] FIELDS={
            net.minecraftforge.fml.relauncher.ReflectionHelper.findField(net.minecraft.client.renderer.texture.AbstractTexture.class,"blur","field_174940_b"),
            net.minecraftforge.fml.relauncher.ReflectionHelper.findField(net.minecraft.client.renderer.texture.AbstractTexture.class,"mipmap","field_174941_c"),
            net.minecraftforge.fml.relauncher.ReflectionHelper.findField(net.minecraft.client.renderer.texture.AbstractTexture.class,"blurLast","field_174938_d"),
            net.minecraftforge.fml.relauncher.ReflectionHelper.findField(net.minecraft.client.renderer.texture.AbstractTexture.class,"mipmapLast","field_174939_e")};
        private final net.minecraft.client.renderer.texture.AbstractTexture texture;
        private final boolean[] values=new boolean[4];
        AtlasFilterState(net.minecraft.client.renderer.texture.TextureManager manager) {
            net.minecraft.client.renderer.texture.ITextureObject atlas=manager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            texture=atlas instanceof net.minecraft.client.renderer.texture.AbstractTexture?(net.minecraft.client.renderer.texture.AbstractTexture)atlas:null;
            try { if(texture!=null)for(int i=0;i<4;i++)values[i]=FIELDS[i].getBoolean(texture); }
            catch(IllegalAccessException e){throw new AssertionError(e);}
        }
        void restore() {
            if(texture==null)return;
            int previous=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            GlStateManager.bindTexture(texture.getGlTextureId());texture.setBlurMipmapDirect(values[0],values[1]);
            try { for(int i=2;i<4;i++)FIELDS[i].setBoolean(texture,values[i]); }
            catch(IllegalAccessException e){throw new AssertionError(e);}
            finally { GlStateManager.bindTexture(previous); }
        }
    }
    /** Select an actual upward native model surface, never a particle-icon replacement. */
    static TextureAtlasSprite topSprite(ItemStack stack) {
        Block block=((ItemBlock)stack.getItem()).getBlock();IBlockState state=block.getStateFromMeta(stack.getMetadata());
        IBakedModel model=Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
        List<BakedQuad> top=model.getQuads(state,EnumFacing.UP,0);
        if(!top.isEmpty())return top.get(0).getSprite();
        double highest=-Double.MAX_VALUE;TextureAtlasSprite sprite=null;
        for(BakedQuad quad:model.getQuads(state,null,0)) {
            int[] data=quad.getVertexData();int stride=quad.getFormat().getIntegerSize();
            float y=Float.intBitsToFloat(data[1]);boolean plane=true;for(int v=1;v<4;v++)plane&=Float.intBitsToFloat(data[v*stride+1])==y;
            double ax=Float.intBitsToFloat(data[stride])-Float.intBitsToFloat(data[0]),az=Float.intBitsToFloat(data[stride+2])-Float.intBitsToFloat(data[2]);
            double bx=Float.intBitsToFloat(data[2*stride])-Float.intBitsToFloat(data[0]),bz=Float.intBitsToFloat(data[2*stride+2])-Float.intBitsToFloat(data[2]);
            if(plane&&az*bx-ax*bz>0&&y>highest){highest=y;sprite=quad.getSprite();}
        }
        return sprite;
    }
    private void drawBlock(ItemStack stack,IRenderFoundry foundry) {
        TextureAtlasSprite icon=topSprite(stack);
        if(icon==null) {
            Block block=((ItemBlock)stack.getItem()).getBlock();
            if(NO_TOP_WARNED.add(block))org.apache.logging.log4j.LogManager.getLogger("hbm-foundry").warn(
                    "Foundry output {} has no upward native surface; rendering its actual item instead of the legacy flat top. Recipes and stack contents are unchanged.",block.getRegistryName());
            drawItem(stack,foundry.outHeight());return;
        }
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        BufferBuilder b=Tessellator.getInstance().getBuffer();b.begin(GL11.GL_QUADS,DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        double h=foundry.outHeight();
        vertex(b,foundry.minX(),h,foundry.minZ(),icon.getMinU(),icon.getMaxV(),1,1,1,.3F);
        vertex(b,foundry.minX(),h,foundry.maxZ(),icon.getMaxU(),icon.getMaxV(),1,1,1,.3F);
        vertex(b,foundry.maxX(),h,foundry.maxZ(),icon.getMaxU(),icon.getMinV(),1,1,1,.3F);
        vertex(b,foundry.maxX(),h,foundry.minZ(),icon.getMinU(),icon.getMinV(),1,1,1,.3F);
        Tessellator.getInstance().draw();
    }
    private static void vertex(BufferBuilder b,double x,double y,double z,double u,double v,float r,float g,float blue,float alpha) {
        b.pos(x,y,z).tex(u,v).color((int)(r*255),(int)(g*255),(int)(blue*255),(int)(alpha*255)).normal(0,1,0).endVertex();
    }
    private static void molten(IRenderFoundry foundry,float r,float g,float blue,float alpha) {
        BufferBuilder b=Tessellator.getInstance().getBuffer();b.begin(GL11.GL_QUADS,DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        vertex(b,foundry.minX(),foundry.getLevel(),foundry.minZ(),foundry.minZ(),foundry.maxX(),r,g,blue,alpha);
        vertex(b,foundry.minX(),foundry.getLevel(),foundry.maxZ(),foundry.maxZ(),foundry.maxX(),r,g,blue,alpha);
        vertex(b,foundry.maxX(),foundry.getLevel(),foundry.maxZ(),foundry.maxZ(),foundry.minX(),r,g,blue,alpha);
        vertex(b,foundry.maxX(),foundry.getLevel(),foundry.minZ(),foundry.minZ(),foundry.minX(),r,g,blue,alpha);
        Tessellator.getInstance().draw();
    }
    @Override public void render(TileEntity tile,double x,double y,double z,float partial,int destroyStage,float alpha) {
        IRenderFoundry foundry=(IRenderFoundry)tile;
        try(NativeProjectionScope scope=new NativeProjectionScope()) {
            GlStateManager.translate(x,y,z);
            if(foundry instanceof IInventory) {
                IInventory inventory=(IInventory)foundry;ItemStack mold=inventory.getStackInSlot(0);
                if(!mold.isEmpty())drawItem(mold,foundry.moldHeight());
                ItemStack output=inventory.getStackInSlot(1);
                if(!output.isEmpty()) { if(output.getItem() instanceof ItemBlock)drawBlock(output,foundry);else drawItem(output,foundry.outHeight()); }
            }
            if(foundry.shouldRender()) {
                Minecraft.getMinecraft().getTextureManager().bindTexture(lava);Color color=new Color(foundry.getMat().moltenColor);
                GlStateManager.disableLighting();GlStateManager.disableCull();OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,240,240);
                molten(foundry,color.getRed()/255F,color.getGreen()/255F,color.getBlue()/255F,1);
                GlStateManager.depthMask(false);GlStateManager.enableBlend();GlStateManager.blendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE);
                molten(foundry,1,1,1,.3F);
                GlStateManager.disableBlend();GlStateManager.enableLighting();GlStateManager.depthMask(true);
            }
        }
    }
    @Override public void renderActor(WorldInAJar world,int ticks,float partial,NBTTagCompound data) {
        int x=data.getInteger("x"),y=data.getInteger("y"),z=data.getInteger("z");render(world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z)),x,y,z,partial,-1,1);
    }
    @Override public void updateActor(int ticks,NBTTagCompound data) { }
}
