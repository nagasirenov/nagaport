package com.hbm.render.item;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.NotableComments;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.DoorDecl;
import com.hbm.util.Clock;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

// the ItemRenderLibrary system sucks and is ugly as hell but it's the quickest way of doing it
// due to the sheer size if this stupid fucking class i just janked this part out into its own thing
// that way, code hotswap doesn't take 5 years to work while vomitting out error messages like
// i vomitted out that 20 pack of soggy chicken nuggets last year
// still a better use of €15 than hollow knight
@NotableComments
public class ItemRenderLibraryDoors {

	public static void init() {

		ItemRenderLibrary.renderers.put(Item.getItemFromBlock(ModBlocks.vault_door), new ItemRenderBase() {
			public void renderInventory() {
				GL11.glTranslated(0, -3, 0);
				GL11.glScaled(3.5, 3.5, 3.5);
			}
			public void renderCommon() {
				GL11.glTranslated(0, -1, 0);
				int index = (int) ((Clock.get_ms() % (DoorDecl.VAULT_DOOR.getSkinCount() * 1000)) / 1000);

				ResourceLocation doorTex = ResourceManager.pheo_vault_door_3;
				ResourceLocation labelTex = ResourceManager.pheo_label_101;
				
				switch(index) {
				case 1: labelTex = ResourceManager.pheo_label_87; break;
				case 2: labelTex = ResourceManager.pheo_label_106; break;
				case 3: doorTex = ResourceManager.pheo_vault_door_4; labelTex = ResourceManager.pheo_label_81; break;
				case 4: doorTex = ResourceManager.pheo_vault_door_4; labelTex = ResourceManager.pheo_label_111; break;
				case 5: doorTex = ResourceManager.pheo_vault_door_s; labelTex = ResourceManager.pheo_label_2; break;
				case 6: doorTex = ResourceManager.pheo_vault_door_s; labelTex = ResourceManager.pheo_label_99; break;
				}
				
				bindTexture(doorTex);
				ResourceManager.pheo_vault_door.renderPart("Door");
				bindTexture(labelTex);
				ResourceManager.pheo_vault_door.renderPart("Label");
			}
		});
		
		ItemRenderLibrary.renderers.put(Item.getItemFromBlock(ModBlocks.secure_access_door), new ItemRenderBase(){
			public void renderInventory() {
				GL11.glTranslated(0, -3.75, 0);
				GL11.glScaled(2.4, 2.4, 2.4);
			}
			public void renderCommon() {
				GL11.glTranslated(0, 1, 0);
				Minecraft.getMinecraft().getTextureManager().bindTexture(DoorDecl.SECURE_ACCESS_DOOR.getCyclingSkins());
				GL11.glShadeModel(GL11.GL_SMOOTH);
				ResourceManager.pheo_secure_door.renderAll();
				GL11.glShadeModel(GL11.GL_FLAT);
			}
		});
		
		ItemRenderLibrary.renderers.put(Item.getItemFromBlock(ModBlocks.fire_door), new ItemRenderBase(){
			public void renderInventory() {
				GL11.glTranslated(0, -3, 0);
				GL11.glScaled(3.5, 3.5, 3.5);
			}
			public void renderCommon() {
				Minecraft.getMinecraft().getTextureManager().bindTexture(DoorDecl.FIRE_DOOR.getCyclingSkins());
				GL11.glRotated(90, 0, 1, 0);
				GL11.glShadeModel(GL11.GL_SMOOTH);
				ResourceManager.pheo_fire_door.renderAll();
				GL11.glShadeModel(GL11.GL_FLAT);
			}
		});
		



		
		ItemRenderLibrary.renderers.put(Item.getItemFromBlock(ModBlocks.water_door), new ItemRenderBase(){
			public void renderInventory() {
				GL11.glTranslated(0, -4, 0);
				GL11.glScaled(4, 4, 4);
			}
			public void renderCommon() {
				GL11.glRotated(90, 0, 1, 0);
				Minecraft.getMinecraft().getTextureManager().bindTexture(DoorDecl.WATER_DOOR.getCyclingSkins());
				GL11.glShadeModel(GL11.GL_SMOOTH);
				ResourceManager.pheo_water_door.renderAll();
				GL11.glShadeModel(GL11.GL_FLAT);
			}
		});
		

		

		
		ItemRenderLibrary.renderers.put(Item.getItemFromBlock(ModBlocks.qe_containment), new ItemRenderBase(){
			public void renderInventory() {
				GL11.glTranslated(0, -3.5, 0);
				GL11.glScaled(3.8, 3.8, 3.8);
			}
			public void renderCommon() {
				Minecraft.getMinecraft().getTextureManager().bindTexture(DoorDecl.QE_CONTAINMENT.getCyclingSkins());
				GL11.glShadeModel(GL11.GL_SMOOTH);
				ResourceManager.pheo_containment_door.renderAll();
				GL11.glShadeModel(GL11.GL_FLAT);
			}
		});
		


		ItemRenderLibrary.renderers.put(Item.getItemFromBlock(ModBlocks.round_airlock_door), new ItemRenderBase(){
			public void renderInventory() {
				GL11.glTranslated(0, -3.75, 0);
				GL11.glScaled(3, 3, 3);
			}
			public void renderCommon() {
				Minecraft.getMinecraft().getTextureManager().bindTexture(DoorDecl.ROUND_AIRLOCK_DOOR.getCyclingSkins());
				GL11.glShadeModel(GL11.GL_SMOOTH);
				ResourceManager.pheo_airlock_door.renderAll();
				GL11.glShadeModel(GL11.GL_FLAT);
			}
		});




	}

	private static void bindTexture(ResourceLocation res) {
		Minecraft.getMinecraft().renderEngine.bindTexture(res);
	}
}
