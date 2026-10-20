package com.hbm.render.util;

import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.mesh.ObjMesh;

import net.minecraft.client.renderer.Tessellator;
import com.hbm.render.icon.IIcon;
import net.minecraft.util.math.Vec3d;
import com.hbm.lib.ForgeDirection;

public class ObjUtil {

	public static void renderWithIcon(HFRWavefrontObject model, IIcon icon, Tessellator tes, float rot, boolean shadow) {
		renderWithIcon(model, icon, tes, rot, 0, 0, shadow);
	}

	public static void renderWithIcon(HFRWavefrontObject model, IIcon icon, Tessellator tes, float rot, float pitch, boolean shadow) {
		renderWithIcon(model, icon, tes, rot, pitch, 0, shadow);
	}

	public static void renderWithIcon(HFRWavefrontObject model, IIcon icon, Tessellator tes, float rot, float pitch, float roll, boolean shadow) {
		for(ObjMesh.Part go : model.getMesh().parts) {
			
			for(ObjMesh.Face f : go.faces) {

				ObjMesh.Vec n = f.normal;

				com.hbm.util.Vec3NT normal = new com.hbm.util.Vec3NT(n.x, n.y, n.z);
				normal.rotateAroundZRad(pitch);
				normal.rotateAroundYRad(rot);
				tes.setNormal((float)normal.x, (float)normal.y, (float)normal.z);

				if(shadow) {
					float brightness = ((float)normal.y + 0.7F) * 0.9F - (float)Math.abs(normal.x) * 0.1F + (float)Math.abs(normal.z) * 0.1F;

					if(brightness < 0.45F)
						brightness = 0.45F;

					tes.setColorOpaque_F(brightness, brightness, brightness);
				}

				for(int i = 0; i < f.corners.size(); i++) {

					ObjMesh.Vec v = f.corners.get(i).position;

					com.hbm.util.Vec3NT vec = new com.hbm.util.Vec3NT(v.x, v.y, v.z);
					vec.rotateAroundXRad(roll);
					vec.rotateAroundZRad(pitch);
					vec.rotateAroundYRad(rot);

					float x = (float) vec.x;
					float y = (float) vec.y;
					float z = (float) vec.z;

					ObjMesh.Vec t = f.corners.get(i).uv;
					tes.addVertexWithUV(x, y, z, icon.getInterpolatedU(t.x * 16), icon.getInterpolatedV(t.y * 16));

					// The shoddy way of rendering a tringulated model with a
					// quad tessellator
					if(i % 3 == 2)
						tes.addVertexWithUV(x, y, z, icon.getInterpolatedU(t.x * 16), icon.getInterpolatedV(t.y * 16));
				}
			}
		}
	}

	public static void renderPartWithIcon(HFRWavefrontObject model, String name, IIcon icon, Tessellator tes, float rot, boolean shadow) {
		renderPartWithIcon(model, name, icon, tes, rot, 0, 0, shadow);
	}

	public static void renderPartWithIcon(HFRWavefrontObject model, String name, IIcon icon, Tessellator tes, float rot, float pitch, boolean shadow) {
		renderPartWithIcon(model, name, icon, tes, rot, pitch, 0, shadow);
	}

	public static void renderPartWithIcon(HFRWavefrontObject model, String name, IIcon icon, Tessellator tes, float rot, float pitch, float roll, boolean shadow) {

		ObjMesh.Part go = null;

		for(ObjMesh.Part obj : model.getMesh().parts) {
			if(obj.name.equals(name))
				go = obj;
		}

		if(go == null)
			return;

		for(ObjMesh.Face f : go.faces) {

			ObjMesh.Vec n = f.normal;

			com.hbm.util.Vec3NT normal = new com.hbm.util.Vec3NT(n.x, n.y, n.z);
			normal.rotateAroundZRad(pitch);
			normal.rotateAroundYRad(rot);
			tes.setNormal((float)normal.x, (float)normal.y, (float)normal.z);

			if(shadow || hasColor) {
				
				float brightness = 1.0F;
				
				if(shadow) {
					brightness = ((float)normal.y * 0.3F + 0.7F) - (float)Math.abs(normal.x) * 0.1F + (float)Math.abs(normal.z) * 0.1F;
	
					if(brightness < 0.45F)
						brightness = 0.45F;
				}

				if(hasColor) {
					tes.setColorOpaque((int)(red * brightness), (int)(green * brightness), (int)(blue * brightness));
				} else {
					tes.setColorOpaque_F(brightness, brightness, brightness);
				}
			}

			for(int i = 0; i < f.corners.size(); i++) {

				ObjMesh.Vec v = f.corners.get(i).position;

				com.hbm.util.Vec3NT vec = new com.hbm.util.Vec3NT(v.x, v.y, v.z);
				vec.rotateAroundXRad(roll);
				vec.rotateAroundZRad(pitch);
				vec.rotateAroundYRad(rot);

				float x = (float) vec.x;
				float y = (float) vec.y;
				float z = (float) vec.z;

				ObjMesh.Vec t = f.corners.get(i).uv;
				tes.addVertexWithUV(x, y, z, icon.getInterpolatedU(t.x * 16D), icon.getInterpolatedV(t.y * 16D));

				// The shoddy way of rendering a tringulated model with a quad
				// tessellator
				if(f.corners.size() == 3 && i % 3 == 2)
					tes.addVertexWithUV(x, y, z, icon.getInterpolatedU(t.x * 16D), icon.getInterpolatedV(t.y * 16D));
			}
		}
	}
	
	private static int red;
	private static int green;
	private static int blue;
	private static boolean hasColor = false;
	
	public static void setColor(int color) {
		red = (color & 0xff0000) >> 16;
		green = (color & 0x00ff00) >> 8;
		blue = color & 0x0000ff;
		hasColor = true;
	}
	
	public static void setColor(int r, int g, int b) {
		red = r;
		green = g;
		blue = b;
		hasColor = true;
	}
	
	public static void clearColor() {
		hasColor = false;
	}

	// Both methods assume model is facing towards +X (EAST)
	// Why not +Z (NORTH)? Pitch doesn't rotate as you would expect in that case using the (current) draw methods
	public static float getPitch(ForgeDirection dir) {
		if (dir == ForgeDirection.UP) return (float)Math.PI * -0.5F;
		if (dir == ForgeDirection.DOWN) return (float)Math.PI * 0.5F;
		return 0;
	}

	public static float getYaw(ForgeDirection dir) {
		if (dir == ForgeDirection.NORTH) return (float)Math.PI * 0.5f;;
		if (dir == ForgeDirection.SOUTH) return (float)Math.PI * -0.5f;
		if (dir == ForgeDirection.WEST) return (float)Math.PI;
		return 0;
	}

}
