package com.hbm.render.entity.effect;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderFlare extends Render {

	public RenderFlare(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(Entity p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_) {
		
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder tessellatorBuffer = tessellator.getBuffer();
		RenderHelper.disableStandardItemLighting();
        float f1 = (p_76986_1_.ticksExisted + 2.0F) / 200.0F;
        float f2 = 0.0F;
        int count = 250;
        
        if(p_76986_1_.ticksExisted < 250)
        {
        	count = p_76986_1_.ticksExisted * 3;
        }

        if (f1 > 0.8F)
        {
            f2 = (f1 - 0.8F) / 0.2F;
        }

        Random random = new Random(432L);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDepthMask(false);
        GL11.glPushMatrix();
        //GL11.glTranslatef(0.0F, -1.0F, -2.0F);

        GL11.glTranslatef((float)p_76986_2_, (float)p_76986_4_, (float)p_76986_6_);
        
        //for (int i = 0; (float)i < (f1 + f1 * f1) / 2.0F * 60.0F; ++i)
        for(int i = 0; i < count; i++)
        {
            GL11.glRotatef(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(random.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(random.nextFloat() * 360.0F + f1 * 90.0F, 0.0F, 0.0F, 1.0F);
            tessellatorBuffer.begin(6, DefaultVertexFormats.POSITION_COLOR);
            float f3 = random.nextFloat() * 20.0F + 5.0F + f2 * 10.0F;
            float f4 = random.nextFloat() * 2.0F + 1.0F + f2 * 2.0F;
            //tessellator.setColorRGBA_I(16777215, (int)(255.0F * (1.0F - f2)));
            int tessellatorColor0Packed = 59345715;
            int tessellatorColor0Alpha = Math.max(0, Math.min(255, (int)(255.0F * (1.0F - f2))));
            tessellatorBuffer.pos(0.0D, 0.0D, 0.0D).color((tessellatorColor0Packed >> 16) & 255, (tessellatorColor0Packed >> 8) & 255, tessellatorColor0Packed & 255, tessellatorColor0Alpha).endVertex();
            //tessellator.setColorRGBA_I(16711935, 0);
            int tessellatorColor1Packed = 59345735;
            int tessellatorColor1Alpha = Math.max(0, Math.min(255, 0));
            tessellatorBuffer.pos(-0.866D * f4, f3, -0.5F * f4).color((tessellatorColor1Packed >> 16) & 255, (tessellatorColor1Packed >> 8) & 255, tessellatorColor1Packed & 255, tessellatorColor1Alpha).endVertex();
            tessellatorBuffer.pos(0.866D * f4, f3, -0.5F * f4).color((tessellatorColor1Packed >> 16) & 255, (tessellatorColor1Packed >> 8) & 255, tessellatorColor1Packed & 255, tessellatorColor1Alpha).endVertex();
            tessellatorBuffer.pos(0.0D, f3, 1.0F * f4).color((tessellatorColor1Packed >> 16) & 255, (tessellatorColor1Packed >> 8) & 255, tessellatorColor1Packed & 255, tessellatorColor1Alpha).endVertex();
            tessellatorBuffer.pos(-0.866D * f4, f3, -0.5F * f4).color((tessellatorColor1Packed >> 16) & 255, (tessellatorColor1Packed >> 8) & 255, tessellatorColor1Packed & 255, tessellatorColor1Alpha).endVertex();
    		GL11.glScalef(0.99F, 0.99F, 0.99F);
            tessellator.draw();
        }

        GL11.glPopMatrix();
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        RenderHelper.enableStandardItemLighting();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity p_110775_1_) {
		return null;
	}

}
