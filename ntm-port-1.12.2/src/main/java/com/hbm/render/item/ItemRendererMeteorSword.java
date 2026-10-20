package com.hbm.render.item;

import org.lwjgl.opengl.GL11;
import com.hbm.render.util.NativeIconExtrusion;
import com.hbm.render.util.RenderItemStack;
import com.hbm.render.util.NativeGuiItemDraw;
import com.hbm.render.util.NativeMachineItemRenderer;
import com.hbm.render.item.nativeitem.context.NativeItemContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;

/** Original meteor tools always carry their own colored HBM shimmer, even without enchantments. */
public class ItemRendererMeteorSword extends ItemRenderTransformer {
    private final float r, g, b;
    public ItemRendererMeteorSword(float r, float g, float b) {
        super(new double[]{0,180,-90},new double[]{.2,.55,0},new double[]{1.7,1.7,.85},
                new double[]{0,180,-90},new double[]{0,0,0},new double[]{1.36,1.36,.68},
                new double[]{0,0,0},new double[]{0,0,0},new double[]{1,1,1});
        this.r=r;this.g=g;this.b=b;
    }
    @Override protected void draw(ItemStack item, NativeItemContext context) {
        net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
        if(context.transform == TransformType.GUI) {
            RenderItemStack.renderItemStackNoEffect(0,0,0,item);
            renderGlintFlat();
        } else {
            IBakedModel base = NativeGuiItemDraw.baseModel(Minecraft.getMinecraft().getRenderItem(), item);
            java.util.List<BakedQuad> layers = NativeMachineItemRenderer.iconLayers(base);
            if(layers == null || layers.isEmpty()) {
                Minecraft.getMinecraft().getRenderItem().renderItem(item,base);
                return;
            }
            TextureAtlasSprite icon = layers.get(0).getSprite();
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            NativeIconExtrusion.draw(icon.getMaxU(),icon.getMinV(),icon.getMinU(),icon.getMaxV(),icon.getIconWidth(),icon.getIconHeight(),.0625F);
            renderGlint3D(Tessellator.getInstance(),.0625F);
        }
    }

	public void renderGlintFlat() {

		Minecraft mc = Minecraft.getMinecraft();
		mc.renderEngine.bindTexture(new net.minecraft.util.ResourceLocation("hbm", "textures/misc/glint.png"));

		GL11.glDepthFunc(GL11.GL_EQUAL);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_BLEND);

		for(int j1 = 0; j1 < 2; ++j1) {
			OpenGlHelper.glBlendFunc(772, 1, 0, 0);
			float scaleU = 0.00390625F;
			float scaleV = 0.00390625F;
			float anim = (float) (Minecraft.getSystemTime() % (long) (3000 + j1 * 1873)) / (3000.0F + (float) (j1 * 1873)) * 256.0F;
			float offsetV = 0.0F;
			Tessellator tessellator = Tessellator.getInstance();
			float sizeMultU = 4.0F;

			if(j1 == 1) {
				sizeMultU = -1.0F;
			}

			float in = 0.36F;
			GL11.glColor4f(r * in, g * in, b * in, 1.0F);

			int x = 0;
			int sizeX = 16;
			int y = 0;
			int sizeY = 16;
			int zLevel = 0;

			net.minecraft.client.renderer.BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX);
			buffer.pos(x + 0, y + sizeY, zLevel).tex((anim + sizeY * sizeMultU) * scaleU, (offsetV + sizeY) * scaleV).endVertex();
			buffer.pos(x + sizeX, y + sizeY, zLevel).tex((anim + sizeX + sizeY * sizeMultU) * scaleU, (offsetV + sizeY) * scaleV).endVertex();
			buffer.pos(x + sizeX, y + 0, zLevel).tex((anim + sizeX) * scaleU, (offsetV + 0.0F) * scaleV).endVertex();
			buffer.pos(x + 0, y + 0, zLevel).tex((anim + 0.0F) * scaleU, (offsetV + 0.0F) * scaleV).endVertex();
			tessellator.draw();
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
	}
	
	public void renderGlint3D(Tessellator tessellator, float depth) {

		Minecraft mc = Minecraft.getMinecraft();
		mc.renderEngine.bindTexture(new net.minecraft.util.ResourceLocation("hbm", "textures/misc/glint.png"));
		
		GL11.glDepthFunc(GL11.GL_EQUAL);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);

		float in = 0.36F;
		GL11.glColor4f(r * in, g * in, b * in, 1.0F);
		
		GL11.glMatrixMode(GL11.GL_TEXTURE);
		
		GL11.glPushMatrix();
		float scale = 0.125F;
		GL11.glScalef(scale, scale, scale);
		float offset = (float) (Minecraft.getSystemTime() % 3000L) / 3000.0F * 8.0F;
		GL11.glTranslatef(offset, 0.0F, 0.0F);
		GL11.glRotatef(-50.0F, 0.0F, 0.0F, 1.0F);
		NativeIconExtrusion.draw(0.0F, 0.0F, 1.0F, 1.0F, 255, 255, depth);
		GL11.glPopMatrix();
		GL11.glPushMatrix();
		GL11.glScalef(scale, scale, scale);
		offset = (float) (Minecraft.getSystemTime() % 4873L) / 4873.0F * 8.0F;
		GL11.glTranslatef(-offset, 0.0F, 0.0F);
		GL11.glRotatef(10.0F, 0.0F, 0.0F, 1.0F);
		NativeIconExtrusion.draw(0.0F, 0.0F, 1.0F, 1.0F, 255, 255, depth);
		GL11.glPopMatrix();
		
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
	}
}
