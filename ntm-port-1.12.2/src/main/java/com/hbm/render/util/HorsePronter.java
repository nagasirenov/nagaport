package com.hbm.render.util;

import org.lwjgl.opengl.GL11;

import com.hbm.util.Vec3NT;

import com.hbm.lib.RefStrings;
import com.hbm.render.loader.HFRWavefrontObject;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.IModelCustom;

public class HorsePronter {
	
	public static final IModelCustom horse = new HFRWavefrontObject(new ResourceLocation(RefStrings.MODID, "models/mobs/horse.obj"), false).asVBO();

	public static final ResourceLocation tex_demohorse = new ResourceLocation(RefStrings.MODID, "textures/models/horse/horse_demo.png");

	private static Vec3NT[] pose = new Vec3NT[] {
			new Vec3NT(0, 0, 0), //head
			new Vec3NT(0, 0, 0), //left front leg
			new Vec3NT(0, 0, 0), //right front leg
			new Vec3NT(0, 0, 0), //left back leg
			new Vec3NT(0, 0, 0), //right back leg
			new Vec3NT(0, 0, 0), //tail
			new Vec3NT(0, 0, 0), //body
			new Vec3NT(0, 0, 0) //body offset
	};

	private static Vec3d[] offsets = new Vec3d[] {
			new Vec3d(0, 1.125, 0.375), //head
			new Vec3d(0.125, 0.75, 0.3125), //left front leg
			new Vec3d(-0.125, 0.75, 0.3125), //right front leg
			new Vec3d(0.125, 0.75, -0.25), //left back leg
			new Vec3d(-0.125, 0.75, -0.25), //right back leg
			new Vec3d(0, 1.125, -0.4375), //tail
			new Vec3d(0, 0, 0), //body
			new Vec3d(0, 0, 0) //body offset
	};

	public static final int id_head = 0;
	public static final int id_lfl = 1;
	public static final int id_rfl = 2;
	public static final int id_lbl = 3;
	public static final int id_rbl = 4;
	public static final int id_tail = 5;
	public static final int id_body = 6;
	public static final int id_position = 7;

	private static boolean wings = false;
	private static boolean horn = false;
	private static boolean maleSnoot = false;

	public static void reset() {
		
		wings = false;
		horn = false;
		
		for(Vec3NT angles : pose) {
			angles.x = 0;
			angles.y = 0;
			angles.z = 0;
		}
	}
	
	public static void enableHorn() { horn = true; }
	public static void enableWings() { wings = true; }
	public static void setMaleSnoot() { maleSnoot = true; }
	
	public static void setAlicorn() {
		enableHorn();
		enableWings();
	}
	
	public static void poseStandardSit() {
		double r = 60;
		pose(HorsePronter.id_body, 0, -r, 0);
		pose(HorsePronter.id_tail, 0, 45, 90);
		pose(HorsePronter.id_lbl, 0, -90 + r, 35);
		pose(HorsePronter.id_rbl, 0, -90 + r, -35);
		pose(HorsePronter.id_lfl, 0, r - 10, 5);
		pose(HorsePronter.id_rfl, 0, r - 10, -5);
		pose(HorsePronter.id_head, 0, r, 0);
	}
	
	public static void pose(int id, double yaw, double pitch, double roll) {
		pose[id].setComponents(yaw, pitch, roll);
	}
	
	public static void pront() {
		
		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_CULL_FACE);
		doTransforms(id_body);
		
		horse.renderPart("Body");
		
		if(horn) {
			renderWithTransform(id_head, "Head", "Mane", maleSnoot ? "NoseMale" : "NoseFemale", "HornPointy");
		} else {
			renderWithTransform(id_head, "Head", "Mane", maleSnoot ? "NoseMale" : "NoseFemale");
		}
		
		renderWithTransform(id_lfl, "LeftFrontLeg");
		renderWithTransform(id_rfl, "RightFrontLeg");
		renderWithTransform(id_lbl, "LeftBackLeg");
		renderWithTransform(id_rbl, "RightBackLeg");
		renderWithTransform(id_tail, "Tail");
		
		if(wings) {
			horse.renderPart("LeftWing");
			horse.renderPart("RightWing");
		}

		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glPopMatrix();
	}
	
	private static void doTransforms(int id) {
		Vec3NT rotation = pose[id];
		Vec3d offset = offsets[id];
		GL11.glTranslated(offset.x, offset.y, offset.z);
		GL11.glRotated(rotation.x, 0, 1, 0);
		GL11.glRotated(rotation.y, 1, 0, 0);
		GL11.glRotated(rotation.z, 0, 0, 1);
		GL11.glTranslated(-offset.x, -offset.y, -offset.z);
	}
	
	private static void renderWithTransform(int id, String... parts) {
		GL11.glPushMatrix();
		doTransforms(id);
		for(String part : parts) horse.renderPart(part);
		GL11.glPopMatrix();
	}
}
