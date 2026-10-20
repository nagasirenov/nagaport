package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.hbm.render.util.RenderDecoItem;
import com.hbm.tileentity.machine.storage.TileEntityMassStorage;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

@SideOnly(Side.CLIENT)
public class RenderMassStorage extends TileEntitySpecialRenderer<TileEntity> {

	private final RenderDecoItem itemRenderer = new RenderDecoItem();

	@Override
	public void render(TileEntity tile, double x, double y, double z, float interp, int destroyStage, float alpha) {
		if(!(tile instanceof TileEntityMassStorage)) return;
		TileEntityMassStorage storage = (TileEntityMassStorage) tile;

		if(storage.type == null) return;

		Minecraft mc = Minecraft.getMinecraft();
		int dir = storage.getBlockMetadata() / 4;

		// fuck this shit, push pop the whole ass lighting state then for all I fucken care
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LIGHTING_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_TRANSFORM_BIT);

		GlStateManager.disableRescaleNormal();
		GlStateManager.disableNormalize();

		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);

		GL11.glPushMatrix();
		{

			// align to block
			GL11.glTranslated(x, y, z);

			// align item (and flip)
			GL11.glTranslatef(0.5F, 0.5F, 0.5F);
			GL11.glRotatef(180.0F, 0, 0, 1);
			switch(dir) {
			case 1: GL11.glRotatef(180.0F, 0, 1, 0); break;
			case 2: GL11.glRotatef(-90.0F, 0, 1, 0); break;
			case 3: GL11.glRotatef(90.0F, 0, 1, 0); break;
			}
			GL11.glTranslatef(-0.5F, -0.5F, -0.5F);


			GL11.glTranslatef(0, 0, -0.005F); // offset to prevent z-fighting
			GL11.glScalef(1.0F / 16.0F, 1.0F / 16.0F, -0.0001F); // scale to block size


			GL11.glPushMatrix();
			{

				GL11.glTranslatef(4.0F, 2.5F, 0); // adjust to centered location
				GL11.glScalef(8.0F / 16.0F, 8.0F / 16.0F, 1); // scale to 8 pixels across

				if(mc.gameSettings.fancyGraphics) {
					itemRenderer.renderGui(storage.type, 0, 0, true, true);
				} else {
					itemRenderer.renderGui(storage.type, 0, 0, false, true);
				}

			}
			GL11.glPopMatrix();

			GL11.glColor3f(1, 1, 1);

			String text = getTextForCount(storage.getStockpile(), mc.fontRenderer.getUnicodeFlag());

			int textX = 32 - mc.fontRenderer.getStringWidth(text) / 2;
			int textY = 44;

			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glPushMatrix();
			{

				GL11.glScalef(4.0F / 16.0F, 4.0F / 16.0F, 4.0F / 16.0F);

				int fontColor = 0x00FF00;

				// funky text shadow rendering with no z-fighting and alpha testing still enabled
				mc.fontRenderer.drawString(text, textX + 1, textY + 1, (fontColor & 16579836) >> 2 | fontColor & -16777216);
				GL11.glTranslatef(0, 0, 1);
				mc.fontRenderer.drawString(text, textX, textY, 0x00FF00);

			}
			GL11.glPopMatrix();

			GL11.glDisable(GL11.GL_TEXTURE_2D);

			double fraction = (double) storage.getStockpile() / (double) storage.getCapacity();

			GL11.glColor3d(1.0 - fraction, fraction, 0.0);

			double bMinX = 2;
			double bMaxX = 2 + fraction * 12;
			double bMinY = 13.5;
			double bMaxY = 14;

			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
			buffer.pos(bMinX, bMaxY, 0).endVertex();
			buffer.pos(bMaxX, bMaxY, 0).endVertex();
			buffer.pos(bMaxX, bMinY, 0).endVertex();
			buffer.pos(bMinX, bMinY, 0).endVertex();
			tessellator.draw();

			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_LIGHTING);

		}
		GL11.glPopMatrix();

		GL11.glPopAttrib();
		synchronizeNativeCaches();
	}

    /** This legacy scope restores raw attribs behind native item/font caches.
     * Reconcile the flags and scalar state those draws touch, without changing
     * the restored GL values or assuming a new default render state. */
    private static void synchronizeNativeCaches() {
        sync(GL11.GL_ALPHA_TEST, GlStateManager::enableAlpha, GlStateManager::disableAlpha);
        sync(GL11.GL_BLEND, GlStateManager::enableBlend, GlStateManager::disableBlend);
        sync(GL11.GL_LIGHTING, GlStateManager::enableLighting, GlStateManager::disableLighting);
        sync(GL11.GL_CULL_FACE, GlStateManager::enableCull, GlStateManager::disableCull);
        sync(GL12.GL_RESCALE_NORMAL, GlStateManager::enableRescaleNormal, GlStateManager::disableRescaleNormal);
        sync(GL11.GL_NORMALIZE, GlStateManager::enableNormalize, GlStateManager::disableNormalize);
        sync(GL11.GL_DEPTH_TEST, GlStateManager::enableDepth, GlStateManager::disableDepth);
        sync(GL11.GL_COLOR_MATERIAL, GlStateManager::enableColorMaterial, GlStateManager::disableColorMaterial);
        sync(GL11.GL_TEXTURE_2D, GlStateManager::enableTexture2D, GlStateManager::disableTexture2D);
        sync(GL11.GL_POLYGON_OFFSET_FILL, GlStateManager::enablePolygonOffset, GlStateManager::disablePolygonOffset);
        for(int i = 0; i < 8; i++) {
            final int light = i;
            sync(GL11.GL_LIGHT0 + i, () -> GlStateManager.enableLight(light), () -> GlStateManager.disableLight(light));
        }
        int function = GL11.glGetInteger(GL11.GL_ALPHA_TEST_FUNC);
        float reference = GL11.glGetFloat(GL11.GL_ALPHA_TEST_REF);
        GlStateManager.alphaFunc(function == GL11.GL_ALWAYS ? GL11.GL_NEVER : GL11.GL_ALWAYS, 0);
        GlStateManager.alphaFunc(function, reference);
        int src = GL11.glGetInteger(org.lwjgl.opengl.GL14.GL_BLEND_SRC_RGB), dst = GL11.glGetInteger(org.lwjgl.opengl.GL14.GL_BLEND_DST_RGB);
        int srcAlpha = GL11.glGetInteger(org.lwjgl.opengl.GL14.GL_BLEND_SRC_ALPHA), dstAlpha = GL11.glGetInteger(org.lwjgl.opengl.GL14.GL_BLEND_DST_ALPHA);
        GlStateManager.tryBlendFuncSeparate(src == GL11.GL_ONE ? GL11.GL_ZERO : GL11.GL_ONE, 0, 1, 0);
        GlStateManager.tryBlendFuncSeparate(src, dst, srcAlpha, dstAlpha);
        int shade = GL11.glGetInteger(GL11.GL_SHADE_MODEL);
        GlStateManager.shadeModel(shade == GL11.GL_FLAT ? GL11.GL_SMOOTH : GL11.GL_FLAT);
        GlStateManager.shadeModel(shade);
        int face = GL11.glGetInteger(GL11.GL_COLOR_MATERIAL_FACE), parameter = GL11.glGetInteger(GL11.GL_COLOR_MATERIAL_PARAMETER);
        GlStateManager.colorMaterial(face == GL11.GL_FRONT ? GL11.GL_BACK : GL11.GL_FRONT, GL11.GL_AMBIENT);
        GlStateManager.colorMaterial(face, parameter);
        java.nio.FloatBuffer color = org.lwjgl.BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_CURRENT_COLOR, color);
        GlStateManager.resetColor();
        GlStateManager.color(color.get(0), color.get(1), color.get(2), color.get(3));
    }
    private static void sync(int capability, Runnable enable, Runnable disable) {
        if(GL11.glIsEnabled(capability)) { disable.run(); enable.run(); }
        else { enable.run(); disable.run(); }
    }

	private String getTextForCount(int stackSize, boolean isUnicode) {
		if(stackSize >= 100000000 || (stackSize >= 1000000 && isUnicode)) return String.format("%.0fM", stackSize / 1000000f);
		if(stackSize >= 1000000) return String.format("%.1fM", stackSize / 1000000f);
		if(stackSize >= 100000 || (stackSize >= 10000 && isUnicode)) return String.format("%.0fK", stackSize / 1000f);
		if(stackSize >= 10000) return String.format("%.1fK", stackSize / 1000f);
		return String.valueOf(stackSize);
	}

}
