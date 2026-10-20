package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityForceField;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;

public class RenderMachineForceField extends TileEntitySpecialRenderer {

	@Override
	public void render(TileEntity te, double x, double y, double z, float f, int nativeDestroyStage, float nativeAlpha) {
		
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5D, y, z + 0.5D);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glRotatef(180, 0F, 1F, 0F);

        bindTexture(ResourceManager.forcefield_base_tex);
        
        ResourceManager.radar_body.renderAll();
        
        TileEntityForceField ff = (TileEntityForceField)te;

        GL11.glTranslated(0, 0.5D, 0);
        
        //double rot = (System.currentTimeMillis() / 10D) % 360;
        
        int segments = (int)(16 + ff.radius * 0.125);
        
        bindTexture(ResourceManager.forcefield_top_tex);

        if(ff.isOn && ff.health > 0 && ff.power > 0 && ff.cooldown == 0) {
        	generateSphere(segments, segments * 2, ff.radius, ff.color);
            
            double rot = (System.currentTimeMillis() / 10D) % 360;
    		GL11.glRotated(-rot, 0F, 1F, 0F);
        }

        GL11.glTranslated(0, 0.5, 0);
    	ResourceManager.forcefield_top.renderAll();
        GL11.glTranslated(0, -0.5, 0);
        
        GL11.glPopMatrix();
	}
	
	private void generateSphere(int l, int s, float rad, int hex) {

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        
        float sRot = 360F / s;
        float lRot = (float)Math.PI / l;
        
        for(int k = 0; k < s; k++) {

    		GL11.glRotatef(sRot, 0F, 1F, 0F);
    		
	        com.hbm.util.Vec3NT vec = new com.hbm.util.Vec3NT(0, rad, 0);
	        
	        for(int i = 0; i < l; i++) {

	            Tessellator tessellator = Tessellator.instance;
	            
	            /*if((i < 2 || i > l - 2) && k % 10 == 0) {
		            tessellator.startDrawing(3);
		            tessellator.setColorRGBA_F(0F, 1F, 0F, 1.0F);
		            tessellator.addVertex(vec.x, vec.y, vec.z);
		            tessellator.addVertex(0, 0, 0);
		            tessellator.draw();
	            }*/
	            
	            tessellator.startDrawing(3);
	            //tessellator.setColorRGBA_F(0F, 1F, 0F, 1.0F);
	            tessellator.setColorOpaque_I(hex);
	            tessellator.addVertex(vec.x, vec.y, vec.z);
	            vec.rotateAroundXRad(lRot);
	            tessellator.addVertex(vec.x, vec.y, vec.z);
	            tessellator.draw();
	        }
        }

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
        
        generateSphere2(l, s, rad, hex);
	}
	
	private void generateSphere2(int l, int s, float rad, int hex) {

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        
        float sRot = (float)Math.PI * 2F / (float)(s);
        float lRot = (float)Math.PI / l;

	        com.hbm.util.Vec3NT vec2 = new com.hbm.util.Vec3NT(0, rad, 0);
        
	        for(int k = 0; k < l; k++) {
	            vec2.rotateAroundZRad(lRot);
        	
        	
	        for(int i = 0; i < s; i++) {

	            Tessellator tessellator = Tessellator.instance;
	            tessellator.startDrawing(3);
	            //tessellator.setColorRGBA_F(0F, 1F, 0F, 1.0F);
	            tessellator.setColorOpaque_I(hex);
	            tessellator.addVertex(vec2.x, vec2.y, vec2.z);
	            vec2.rotateAroundYRad(sRot);
	            tessellator.addVertex(vec2.x, vec2.y, vec2.z);
	            tessellator.draw();
	        }
        }

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
	}
}
