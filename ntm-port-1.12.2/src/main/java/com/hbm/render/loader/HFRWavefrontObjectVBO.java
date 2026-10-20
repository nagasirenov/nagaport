package com.hbm.render.loader;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.*;

import com.hbm.render.mesh.MeshModel;
import com.hbm.render.mesh.ObjMesh;
import com.hbm.render.mesh.ObjMeshVboPayload;

public class HFRWavefrontObjectVBO implements MeshModel {

	class VBOBufferData {

		String name;
		int vertices = 0;
		int vertexHandle;
		int uvHandle;
		int normalHandle;

	}

	List<VBOBufferData> groups = new ArrayList<VBOBufferData>();
	private ObjMesh mesh = ObjMesh.empty();

	static int VERTEX_SIZE = 3;
	static int UV_SIZE = 3;
	
	public HFRWavefrontObjectVBO(HFRWavefrontObject obj) {
		load(obj);
	}
	
	/**
	 * Retains the original append-only upload behavior; normal reload destroys first.
	 * Publishes the source snapshot only after all uploads complete. GL failures are
	 * not atomic: previously appended groups and partial handles can remain.
	 */
	public void load(HFRWavefrontObject obj) {
		ObjMesh loadedMesh = obj.getMesh();
		for(ObjMesh.Part part : loadedMesh.parts) {
			VBOBufferData data = new VBOBufferData();
			data.name = part.name;
			ObjMeshVboPayload payload = ObjMeshVboPayload.pack(part);
			data.vertices = payload.vertices;
			FloatBuffer vertexData = payload.positions;
			FloatBuffer uvData = payload.uvs;
			FloatBuffer normalData = payload.normals;

			data.vertexHandle = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, data.vertexHandle);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexData, GL15.GL_STATIC_DRAW);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			
			data.uvHandle = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, data.uvHandle);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, uvData, GL15.GL_STATIC_DRAW);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			
			data.normalHandle = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, data.normalHandle);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normalData, GL15.GL_STATIC_DRAW);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

			groups.add(data);
		}
		mesh = loadedMesh;
	}
	
	// truth be told, i have no fucking idea what i'm doing
	// i know the VBO sends data to the GPU to be saved there directly which is where the optimization comes from in the first place
	// so logically, if we want to get rid of this, we need to blow the data up
	// documentation on GL15 functions seems nonexistant so fuck it we ball i guess
	public void destroy() {
		for(VBOBufferData data : groups) {
			GL15.glDeleteBuffers(data.vertexHandle);
			GL15.glDeleteBuffers(data.uvHandle);
			GL15.glDeleteBuffers(data.normalHandle);
		}
		groups.clear();
		mesh = ObjMesh.empty();
	}

	/**
	 * Source snapshot from the latest completed load, empty after destroy. If load is
	 * called without destroy, this is not an aggregate of the appended GPU groups;
	 * after a failed upload it can still describe the previous successful load.
	 */
	@Override
	public ObjMesh getMesh() {
		return mesh;
	}

	private void renderVBO(VBOBufferData data) {
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, data.vertexHandle);
		GL11.glVertexPointer(VERTEX_SIZE, GL11.GL_FLOAT, 0, 0l);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, data.uvHandle);
		GL11.glTexCoordPointer(UV_SIZE, GL11.GL_FLOAT, 0, 0l);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, data.normalHandle);
		GL11.glNormalPointer(GL11.GL_FLOAT, 0, 0l);

		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
		
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, data.vertices);
		
		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	@Override
	public void renderAll() {
		for(VBOBufferData data : groups) {
			renderVBO(data);
		}
	}

	@Override
	public void renderOnly(String... groupNames) {
		for(VBOBufferData data : groups) {
			for(String name : groupNames) {
				if(data.name.equalsIgnoreCase(name)) {
					renderVBO(data);
				}
			}
		}
	}

	@Override
	public void renderPart(String partName) {
		for(VBOBufferData data : groups) {
			if(data.name.equalsIgnoreCase(partName)) {
				renderVBO(data);
			}
		}
	}

	@Override
	public void renderAllExcept(String... excludedGroupNames) {
		for(VBOBufferData data : groups) {
			boolean skip = false;
			for(String name : excludedGroupNames) {
				if(data.name.equalsIgnoreCase(name)) {
					skip = true;
					break;
				}
			}
			if(!skip) {
				renderVBO(data);
			}
		}
	}

	@Override
	public List<String> getPartNames() {
		List<String> names = new ArrayList<String>();
		for(VBOBufferData data : groups) {
			names.add(data.name);
		}
		return names;
	}

}