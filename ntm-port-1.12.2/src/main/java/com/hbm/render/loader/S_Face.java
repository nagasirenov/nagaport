package com.hbm.render.loader;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.obj.TextureCoordinate;
import net.minecraftforge.client.model.obj.Vertex;

@SideOnly(Side.CLIENT)
public class S_Face {
	
	public int[] verticesID;
	public Vertex[] vertices;
	public Vertex[] vertexNormals;
	public Vertex faceNormal;
	public TextureCoordinate[] textureCoordinates;
	private boolean smoothing;
	
	public S_Face(boolean smoothing) {
		this.smoothing = smoothing;
	}

	public void addFaceForRender(Tessellator tessellator) {
		addFaceForRender(tessellator, 0.0F);
	}

	public void addFaceForRender(Tessellator tessellator, float textureOffset) {
		
		if (this.faceNormal == null) {
			this.faceNormal = calculateFaceNormal();
		}
		
		if(!this.smoothing) {
			tessellator.setNormal(this.faceNormal.x, this.faceNormal.y, this.faceNormal.z);
		}

		float averageU = 0.0F;
		float averageV = 0.0F;
		
		if ((this.textureCoordinates != null) && (this.textureCoordinates.length > 0)) {
			
			for (int i = 0; i < this.textureCoordinates.length; i++) {
				averageU += this.textureCoordinates[i].u;
				averageV += this.textureCoordinates[i].v;
			}
			
			averageU /= this.textureCoordinates.length;
			averageV /= this.textureCoordinates.length;
		}
		
		for(int i = 0; i < this.vertices.length; i++) {

			if((this.textureCoordinates != null) && (this.textureCoordinates.length > 0)) {

				float offsetU = textureOffset;
				float offsetV = textureOffset;

				if(this.textureCoordinates[i].u > averageU) {
					offsetU = -offsetU;
				}
				if(this.textureCoordinates[i].v > averageV) {
					offsetV = -offsetV;
				}
				if(this.smoothing && (this.vertexNormals != null) && (i < this.vertexNormals.length)) {
					tessellator.setNormal(this.vertexNormals[i].x, this.vertexNormals[i].y, this.vertexNormals[i].z);
				}

				tessellator.addVertexWithUV(this.vertices[i].x, this.vertices[i].y, this.vertices[i].z, this.textureCoordinates[i].u + offsetU, this.textureCoordinates[i].v + offsetV);

			} else {
				tessellator.addVertex(this.vertices[i].x, this.vertices[i].y, this.vertices[i].z);
			}
		}
	}

	public Vertex calculateFaceNormal() {
		
		Vec3d v1 = new Vec3d(this.vertices[1].x - this.vertices[0].x, this.vertices[1].y - this.vertices[0].y, this.vertices[1].z - this.vertices[0].z);
		Vec3d v2 = new Vec3d(this.vertices[2].x - this.vertices[0].x, this.vertices[2].y - this.vertices[0].y, this.vertices[2].z - this.vertices[0].z);
		Vec3d normalVector = null;

		normalVector = v1.crossProduct(v2).normalize();

		return new Vertex((float) normalVector.x, (float) normalVector.y, (float) normalVector.z);
	}
}
