package com.hbm.render.item;

import java.util.HashMap;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockBobble.BobbleType;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.ItemAmmoHIMARS;
import com.hbm.items.weapon.ItemAmmoHIMARS.HIMARSRocket;
import com.hbm.main.ResourceManager;
import com.hbm.render.tileentity.RenderBobble;
import com.hbm.render.tileentity.RenderDemonLamp;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@Deprecated // implement IItemRendererProvider for TESRs instead!
public class ItemRenderLibrary {

	public static HashMap<Item, ItemRenderBase> renderers = new HashMap();

	public static void init() {
		ItemRenderLibraryDoors.init();





















		renderers.put(Item.getItemFromBlock(ModBlocks.machine_fensu), new ItemRenderBase() {
			public void renderInventory() {
				GL11.glRotated(90, 0, 1, 0);
				GL11.glTranslated(0, -2, 0);
				GL11.glScaled(2.5, 2.5, 2.5);
			}
			public void renderCommon() {
		        GL11.glShadeModel(GL11.GL_SMOOTH);
		        bindTexture(ResourceManager.fensu_tex); ResourceManager.fensu.renderPart("Base"); ResourceManager.fensu.renderPart("Disc");
		        GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
		        GL11.glDisable(GL11.GL_LIGHTING);
		        GL11.glDisable(GL11.GL_CULL_FACE);
		        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
		        ResourceManager.fensu.renderPart("Lights");
		        GL11.glEnable(GL11.GL_LIGHTING);
		        GL11.glPopAttrib();
		        GL11.glShadeModel(GL11.GL_FLAT);
			}});





























		renderers.put(Item.getItemFromBlock(ModBlocks.mine_he), new ItemRenderBase() {
			public void renderInventory() {
				GL11.glScaled(6, 6, 6);
			}
			public void renderNonInv() {
				GL11.glTranslated(0.25, 0.625, 0);
				GL11.glRotated(45, 0, 1, 0);
				GL11.glRotated(-15, 0, 0, 1);
			}
			public void renderCommon() {
				GL11.glScaled(4, 4, 4);
				GL11.glDisable(GL11.GL_CULL_FACE);
				GL11.glShadeModel(GL11.GL_SMOOTH);
				bindTexture(ResourceManager.mine_marelet_tex); ResourceManager.mine_marelet.renderAll();
				GL11.glShadeModel(GL11.GL_FLAT);
				GL11.glEnable(GL11.GL_CULL_FACE);
			}
		});















		renderers.put(Item.getItemFromBlock(ModBlocks.launch_table), new ItemRenderBase() {
			public void renderInventory() {
				GL11.glTranslated(0, -2, 0);
				GL11.glScaled(2.5, 2.5, 2.5);
			}
			public void renderCommon() {
				GL11.glScaled(0.5, 0.5, 0.5);
				bindTexture(ResourceManager.launch_table_base_tex); ResourceManager.launch_table_base.renderAll();
				bindTexture(ResourceManager.launch_table_small_pad_tex); ResourceManager.launch_table_small_pad.renderAll();
				GL11.glTranslatef(0F, 0F, 2.5F);
				for(int i = 0; i < 8; i++) {
					GL11.glTranslatef(0F, 1F, 0.F);
					if(i < 6) {
						bindTexture(ResourceManager.launch_table_small_scaffold_base_tex); ResourceManager.launch_table_small_scaffold_base.renderAll();
					}
					if(i == 6) {
						bindTexture(ResourceManager.launch_table_small_scaffold_connector_tex); ResourceManager.launch_table_small_scaffold_connector.renderAll();
					}
					if(i > 6) {
						bindTexture(ResourceManager.launch_table_small_scaffold_base_tex); ResourceManager.launch_table_small_scaffold_empty.renderAll();
					}
				}
			}});








































		renderers.put(Item.getItemFromBlock(ModBlocks.lamp_demon), new ItemRenderBase() {
			public void renderInventory() {
				GL11.glTranslated(0, -3, 0);
				GL11.glScaled(8, 8, 8);
			}
			public void renderCommon() {
				GL11.glShadeModel(GL11.GL_SMOOTH);
				bindTexture(RenderDemonLamp.tex);
				RenderDemonLamp.demon_lamp.renderAll();
				GL11.glShadeModel(GL11.GL_FLAT);
			}});




















		renderers.put(Item.getItemFromBlock(ModBlocks.machine_tower_large), new ItemRenderBase() {
			public void renderInventory() {
				GL11.glTranslated(0, -3, 0);
				GL11.glScaled(4 * 0.95, 4 * 0.95, 4 * 0.95);
			}
			public void renderCommon() {
				GL11.glScaled(0.25, 0.25, 0.25);
				GL11.glShadeModel(GL11.GL_SMOOTH);
				bindTexture(ResourceManager.tower_large_tex); ResourceManager.tower_large.renderAll();
				GL11.glShadeModel(GL11.GL_FLAT);
			}});






		renderers.put(Item.getItemFromBlock(ModBlocks.bobblehead), new ItemRenderBase() {
			public void renderInventory() {
				GL11.glTranslated(0, -3.5, 0);
				GL11.glScaled(10, 10, 10);
			}
			public void renderCommonWithStack(ItemStack stack) {
				GL11.glScaled(0.5, 0.5, 0.5);
				RenderBobble.instance.renderBobble(BobbleType.values()[stack.getItemDamage()]);
			}});























		renderers.put(ModItems.gear_large, new ItemRenderBase( ) {
			public void renderInventory() {
				GL11.glTranslated(0, -7, 0);
				GL11.glScaled(6, 6, 6);
				GL11.glRotated(-45, 0, 1, 0);
				GL11.glRotated(30, 1, 0, 0);
				GL11.glTranslated(0, 1.375, 0);
				GL11.glRotated(System.currentTimeMillis() % 3600 * 0.1F, 0, 0, 1);
				GL11.glTranslated(0, -1.375, 0);
			}
			public void renderCommonWithStack(ItemStack item) {
				GL11.glTranslated(0, 0, -0.875);

				if(item.getItemDamage() == 0)
					bindTexture(ResourceManager.stirling_tex);
				else
					bindTexture(ResourceManager.stirling_steel_tex);
				ResourceManager.stirling.renderPart("Cog");
			}});

		renderers.put(ModItems.sawblade, new ItemRenderBase( ) {
			public void renderInventory() {
				GL11.glTranslated(0, -7, 0);
				GL11.glScaled(6, 6, 6);
				GL11.glRotated(-45, 0, 1, 0);
				GL11.glRotated(30, 1, 0, 0);
				GL11.glTranslated(0, 1.375, 0);
				GL11.glRotated(System.currentTimeMillis() % 3600 * 0.2F, 0, 0, 1);
				GL11.glTranslated(0, -1.375, 0);
			}
			public void renderCommonWithStack(ItemStack item) {
				GL11.glTranslated(0, 0, -0.875);
				bindTexture(ResourceManager.sawmill_tex);
				ResourceManager.sawmill.renderPart("Blade");
			}});
		renderers.put(ModItems.sat_war, new ItemRenderBase( ) {
			public void renderInventory() {
				GL11.glTranslated(3, -2, 0);
				GL11.glScaled(3, 3, 3);
				GL11.glRotated(-45, 0, 1, 0);
				GL11.glRotated(-45, 0, 0, 1);

			}
			public void renderCommonWithStack(ItemStack item) {
				GL11.glTranslated(0, 0, -0.875);
				bindTexture(ResourceManager.sat_rail_tex);
				ResourceManager.sat_rail.renderAll();;
			}});

		renderers.put(ModItems.swarm_member, new ItemRenderBase( ) {
			public void renderInventory() {
				GL11.glTranslated(0, 3, 0);
				GL11.glScaled(1.8D, 1.8D, 1.8D);
				GL11.glRotated(-45, 0, 0, 1);
				GL11.glRotated(30, 1, 0, 0);
				GL11.glRotated(System.currentTimeMillis() % 7200 * -0.05F, 0, 1, 0);
			}
			public void renderCommonWithStack(ItemStack item) {
				bindTexture(ResourceManager.dyson_swarm_member_tex);
				ResourceManager.dyson_swarm_member.renderAll();
			}});

		renderers.put(ModItems.ammo_himars, new ItemRenderBase( ) {
			public void renderInventory() {
				GL11.glTranslated(0, -2, 0);
				double scale = 2.75D;
				GL11.glScaled(scale, scale, scale);
				GL11.glRotated(System.currentTimeMillis() % 3600 / 10D, 0, 1, 0);
			}
			public void renderCommonWithStack(ItemStack item) {
				GL11.glTranslated(0, 1.5, 0);
				GL11.glRotated(-45, 0, 1, 0);
				GL11.glRotated(90, 1, 0, 0);
				HIMARSRocket type = ItemAmmoHIMARS.itemTypes[item.getItemDamage()];
				GL11.glShadeModel(GL11.GL_SMOOTH);
				bindTexture(type.texture);
				if(type.modelType == 0) {
					GL11.glTranslated(0.75, 0, 0);
					ResourceManager.turret_himars.renderPart("RocketStandard");
					GL11.glTranslated(-1.5, 0, 0);
					GL11.glTranslated(0, -3.375D, 0);
					ResourceManager.turret_himars.renderPart("TubeStandard");
				} else {
					GL11.glTranslated(0.75, 0, 0);
					ResourceManager.turret_himars.renderPart("RocketSingle");
					GL11.glTranslated(-1.5, 0, 0);
					GL11.glTranslated(0, -3.375D, 0);
					ResourceManager.turret_himars.renderPart("TubeSingle");
				}
				GL11.glShadeModel(GL11.GL_FLAT);
			}});

		//hi there! it seems you are trying to register a new item renderer, most likely for a tile entity.
		//please refer to the comment at the start of the file on how to do this without adding to this gigantic pile of feces.
	}

	private static void bindTexture(ResourceLocation res) {
		Minecraft.getMinecraft().renderEngine.bindTexture(res);
	}
}
