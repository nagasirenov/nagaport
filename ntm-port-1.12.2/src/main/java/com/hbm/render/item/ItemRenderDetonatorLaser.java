package com.hbm.render.item;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.hbm.main.ResourceManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import javax.vecmath.Matrix4f;
import com.hbm.render.item.nativeitem.ItemTransformMatrix;
import com.hbm.render.item.nativeitem.context.ContextItemRenderer;
import com.hbm.render.item.nativeitem.context.NativeItemContext;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class ItemRenderDetonatorLaser extends ContextItemRenderer {
	
    @Override public Matrix4f localTransform(ItemStack stack, NativeItemContext context) {
        ItemTransformMatrix matrix = new ItemTransformMatrix();
        switch(context.transform) {
        case FIRST_PERSON_LEFT_HAND:
        case FIRST_PERSON_RIGHT_HAND:
            return matrix.scale(.25).rotateY(80).rotateX(-20).translate(1, .5, 3).build();
        case THIRD_PERSON_LEFT_HAND:
        case THIRD_PERSON_RIGHT_HAND:
            return matrix.scale(-.125).rotateY(85).rotateX(145).translate(-.5, -1, 6.5).build();
        case GUI:
            return matrix.scale(3.5, 3.5, -3.5).translate(1.5, 2.75, 0)
                    .rotateX(180).rotateY(-90).rotateX(-45).build();
        default:
            return matrix.rotateY(-90).scale(.25).build();
        }
    }

    @Override protected void draw(ItemStack item, NativeItemContext context) {
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        if(context.transform == net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType.GUI)
            GL11.glEnable(GL11.GL_LIGHTING);
        Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.detonator_laser_tex);
        float lightX = OpenGlHelper.lastBrightnessX, lightY = OpenGlHelper.lastBrightnessY;
        try {

		ResourceManager.detonator_laser.renderPart("Main");
		
		GL11.glPushMatrix();
		try {
		GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
		try {
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_CULL_FACE);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor3f(1F, 0F, 0F);
		ResourceManager.detonator_laser.renderPart("Lights");
		GL11.glColor3f(1F, 1F, 1F);
		
		GL11.glPushMatrix();
		try {
		float px = 0.0625F;
		GL11.glTranslatef(0.5626F, px * 18, -px * 14);
		
		Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		
		int sub = 32;
		double width = px * 8;
		double len = width / sub;
		double time = System.currentTimeMillis() / -100D;
		double amplitude = 0.075;
		
		
		for(int i = 0; i < sub; i++) {
			double h0 = Math.sin(i * 0.5 + time) * amplitude;
			double h1 = Math.sin((i + 1) * 0.5 + time) * amplitude;
			buffer.pos(0, -px * 0.25 + h1, len * (i + 1)).color(255, 255, 0, 255).endVertex();
			buffer.pos(0, px * 0.25 + h1, len * (i + 1)).color(255, 255, 0, 255).endVertex();
			buffer.pos(0, px * 0.25 + h0, len * i).color(255, 255, 0, 255).endVertex();
			buffer.pos(0, -px * 0.25 + h0, len * i).color(255, 255, 0, 255).endVertex();
		}
		
		tess.draw();
		} finally { GL11.glPopMatrix(); }
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);

		GL11.glPushMatrix();
		try {
		String s;
		Random rand = new Random(System.currentTimeMillis() / 500);
		FontRenderer font = Minecraft.getMinecraft().fontRenderer;
		float f3 = 0.01F;
		GL11.glTranslatef(0.5625F, 1.3125F, 0.875F);
		GL11.glScalef(f3, -f3, f3);
		GL11.glRotatef(90, 0, 1, 0);
		GL11.glNormal3f(0.0F, 0.0F, -1.0F * f3);

		GL11.glTranslatef(3F, -2F, 0.2F);
		
		for(int i = 0; i < 3; i++) {
			s = (rand.nextInt(900000) + 100000) + "";
			font.drawString(s, 0, 0, 0xff0000);
			GL11.glTranslatef(0F, 12.5F, 0F);
		}
		} finally { GL11.glPopMatrix(); }
		GL11.glEnable(GL11.GL_LIGHTING);
		} finally { GL11.glPopAttrib(); }
		} finally { GL11.glPopMatrix(); }

		GL11.glShadeModel(GL11.GL_FLAT);
		
        } finally {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightX, lightY);
        }
    }
}
