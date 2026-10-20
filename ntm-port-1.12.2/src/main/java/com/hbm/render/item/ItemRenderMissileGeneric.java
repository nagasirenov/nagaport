package com.hbm.render.item;

import java.util.HashMap;
import java.util.function.Consumer;

import org.lwjgl.opengl.GL11;

import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.items.ModItems;
import com.hbm.main.ResourceManager;

import net.minecraft.client.Minecraft;
import javax.vecmath.Matrix4f;
import com.hbm.render.item.nativeitem.ItemTransformMatrix;
import com.hbm.render.item.nativeitem.direct.DirectMeshItemRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import com.hbm.render.mesh.MeshModel;

public class ItemRenderMissileGeneric extends DirectMeshItemRenderer {
	
	public static HashMap<ComparableStack, Consumer<TextureManager>> renderers = new HashMap();
	
	protected RenderMissileType type;
	
	public static enum RenderMissileType {
		TYPE_TIER0,
		TYPE_TIER1,
		TYPE_TIER2,
		TYPE_TIER3,
		TYPE_STEALTH,
		TYPE_ABM,
		TYPE_NUCLEAR,
		TYPE_ROBIN
	}
	
	public ItemRenderMissileGeneric(RenderMissileType type) {
		this.type = type;
	}
	
    @Override public Matrix4f localTransform(TransformType perspective, long timeMillis) {
        ItemTransformMatrix matrix = new ItemTransformMatrix();
        switch(perspective) {
        case THIRD_PERSON_LEFT_HAND:
        case THIRD_PERSON_RIGHT_HAND: return matrix.translate(.5, -.25, 0).scale(.15).build();
        case FIRST_PERSON_LEFT_HAND:
        case FIRST_PERSON_RIGHT_HAND: return matrix.translate(.5, .25, 0).scale(.1).build();
        case GUI:
            double guiScale = 1, guiOffset = 0;
            switch(type) {
            case TYPE_TIER0: guiScale = 3.75D; guiOffset = 10.75D; break;
            case TYPE_TIER1: guiScale = 2.5D; guiOffset = 8.5D; break;
            case TYPE_TIER2: guiScale = 2D; guiOffset = 6.5D; break;
            case TYPE_TIER3: guiScale = 1.25D; guiOffset = 1D; break;
            case TYPE_STEALTH: guiScale = 1.75D; guiOffset = 4.75D; break;
            case TYPE_ABM: guiScale = 2.25D; guiOffset = 7D; break;
            case TYPE_NUCLEAR: guiScale = 1.375D; guiOffset = 1.5D; break;
            case TYPE_ROBIN: guiScale = 1.25D; guiOffset = 2D; break;
            }
            return matrix.scale(guiScale).rotateZ(135).rotateY(timeMillis / 15 % 360)
                    .translate(0, -16 + guiOffset, 0).build();
        default: return matrix.scale(.15).build();
        }
    }

    @Override public boolean usesGuiStandardLighting() { return true; }

    @Override protected void draw(ItemStack item) {
        Consumer<TextureManager> renderer = renderers.get(new ComparableStack(item).makeSingular());
        if(renderer == null) return;
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0F);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        renderer.accept(Minecraft.getMinecraft().getTextureManager());
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

	public static Consumer<TextureManager> generateStandard(ResourceLocation texture, MeshModel model) { return generateWithScale(texture, model, 1F); }
	public static Consumer<TextureManager> generateLarge(ResourceLocation texture, MeshModel model) { return generateWithScale(texture, model, 1.5F); }
	public static Consumer<TextureManager> generateDouble(ResourceLocation texture, MeshModel model) { return generateWithScale(texture, model, 2F); }
	
	public static Consumer<TextureManager> generateWithScale(ResourceLocation texture, MeshModel model, float scale) {
		return x -> {
			GL11.glScalef(scale, scale, scale);
			GL11.glShadeModel(GL11.GL_SMOOTH);
			x.bindTexture(texture); model.renderAll();
			GL11.glShadeModel(GL11.GL_FLAT);
		};
	}
	
	public static void init() {

		renderers.put(new ComparableStack(ModItems.missile_test), generateStandard(ResourceManager.missileMicroTest_tex, ResourceManager.missileMicro));
		renderers.put(new ComparableStack(ModItems.missile_taint), generateStandard(ResourceManager.missileMicroTaint_tex, ResourceManager.missileMicro));
		renderers.put(new ComparableStack(ModItems.missile_micro), generateStandard(ResourceManager.missileMicro_tex, ResourceManager.missileMicro));
		renderers.put(new ComparableStack(ModItems.missile_bhole), generateStandard(ResourceManager.missileMicroBHole_tex, ResourceManager.missileMicro));
		renderers.put(new ComparableStack(ModItems.missile_schrabidium), generateStandard(ResourceManager.missileMicroSchrab_tex, ResourceManager.missileMicro));
		renderers.put(new ComparableStack(ModItems.missile_emp), generateStandard(ResourceManager.missileMicroEMP_tex, ResourceManager.missileMicro));
		
		renderers.put(new ComparableStack(ModItems.missile_stealth), x -> {
			GL11.glShadeModel(GL11.GL_SMOOTH);
			x.bindTexture(ResourceManager.missileStealth_tex); ResourceManager.missileStealth.renderAll();
			GL11.glShadeModel(GL11.GL_FLAT);
		});
		
		renderers.put(new ComparableStack(ModItems.missile_generic), generateStandard(ResourceManager.missileV2_HE_tex, ResourceManager.missileV2));
		renderers.put(new ComparableStack(ModItems.missile_incendiary), generateStandard(ResourceManager.missileV2_IN_tex, ResourceManager.missileV2));
		renderers.put(new ComparableStack(ModItems.missile_cluster), generateStandard(ResourceManager.missileV2_CL_tex, ResourceManager.missileV2));
		renderers.put(new ComparableStack(ModItems.missile_buster), generateStandard(ResourceManager.missileV2_BU_tex, ResourceManager.missileV2));
		renderers.put(new ComparableStack(ModItems.missile_decoy), generateStandard(ResourceManager.missileV2_decoy_tex, ResourceManager.missileV2));
		renderers.put(new ComparableStack(ModItems.missile_anti_ballistic), generateStandard(ResourceManager.missileAA_tex, ResourceManager.missileABM));
		
		renderers.put(new ComparableStack(ModItems.missile_strong), generateLarge(ResourceManager.missileStrong_HE_tex, ResourceManager.missileStrong));
		renderers.put(new ComparableStack(ModItems.missile_incendiary_strong), generateLarge(ResourceManager.missileStrong_IN_tex, ResourceManager.missileStrong));
		renderers.put(new ComparableStack(ModItems.missile_cluster_strong), generateLarge(ResourceManager.missileStrong_CL_tex, ResourceManager.missileStrong));
		renderers.put(new ComparableStack(ModItems.missile_buster_strong), generateLarge(ResourceManager.missileStrong_BU_tex, ResourceManager.missileStrong));
		renderers.put(new ComparableStack(ModItems.missile_emp_strong), generateLarge(ResourceManager.missileStrong_EMP_tex, ResourceManager.missileStrong));
		
		renderers.put(new ComparableStack(ModItems.missile_burst), generateStandard(ResourceManager.missileHuge_HE_tex, ResourceManager.missileHuge));
		renderers.put(new ComparableStack(ModItems.missile_inferno), generateStandard(ResourceManager.missileHuge_IN_tex, ResourceManager.missileHuge));
		renderers.put(new ComparableStack(ModItems.missile_rain), generateStandard(ResourceManager.missileHuge_CL_tex, ResourceManager.missileHuge));
		renderers.put(new ComparableStack(ModItems.missile_drill), generateStandard(ResourceManager.missileHuge_BU_tex, ResourceManager.missileHuge));

		renderers.put(new ComparableStack(ModItems.missile_nuclear), generateStandard(ResourceManager.missileNuclear_tex, ResourceManager.missileNuclear));
		renderers.put(new ComparableStack(ModItems.missile_nuclear_cluster), generateStandard(ResourceManager.missileThermo_tex, ResourceManager.missileNuclear));
		renderers.put(new ComparableStack(ModItems.missile_volcano), generateStandard(ResourceManager.missileVolcano_tex, ResourceManager.missileNuclear));
		renderers.put(new ComparableStack(ModItems.missile_doomsday), generateStandard(ResourceManager.missileDoomsday_tex, ResourceManager.missileNuclear));
		renderers.put(new ComparableStack(ModItems.missile_doomsday_rusted), generateStandard(ResourceManager.missileDoomsdayRusted_tex, ResourceManager.missileNuclear));

		renderers.put(new ComparableStack(ModItems.missile_shuttle), generateStandard(ResourceManager.missileShuttle_tex, ResourceManager.missileShuttle));
	}
}
