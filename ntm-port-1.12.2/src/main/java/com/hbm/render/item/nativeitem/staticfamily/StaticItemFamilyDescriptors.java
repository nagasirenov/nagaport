package com.hbm.render.item.nativeitem.staticfamily;

import org.lwjgl.opengl.GL11;
import com.hbm.render.mesh.MeshModel;
import com.hbm.render.item.nativeitem.ItemTransformMatrix;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

/** Explicit ordered native mesh recipes. No legacy renderer callback or context emulation. */
public final class StaticItemFamilyDescriptors {
    private StaticItemFamilyDescriptors() { }
    private static void bindTexture(ResourceLocation texture) { Minecraft.getMinecraft().getTextureManager().bindTexture(texture); }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibraryDoors.java:81; effective item owner ModBlocks.sliding_blast_door. */
    public static StaticItemDescriptor sliding_blast_door(Item item, MeshModel m_pheo_blast_door, ResourceLocation t_pheo_blast_door_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2.75, 0).scale(2.5, 2.5, 2.5).build(), new ResourceLocation("hbm:blocks/sliding_blast_door"), () -> {
            bindTexture(t_pheo_blast_door_tex);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            m_pheo_blast_door.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibraryDoors.java:94; effective item owner ModBlocks.large_vehicle_door. */
    public static StaticItemDescriptor large_vehicle_door(Item item, MeshModel m_pheo_vehicle_door, ResourceLocation t_pheo_vehicle_door_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -4, 0).scale(1.8, 1.8, 1.8).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glRotated(90, 0, 1, 0);
            bindTexture(t_pheo_vehicle_door_tex);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            m_pheo_vehicle_door.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibraryDoors.java:122; effective item owner ModBlocks.silo_hatch. */
    public static StaticItemDescriptor silo_hatch(Item item, MeshModel m_silo_hatch, ResourceLocation t_silo_hatch_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2, 0).scale(2, 2, 2).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            bindTexture(t_silo_hatch_tex);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            GL11.glRotated(90, 0, 1, 0);
            m_silo_hatch.renderPart("Frame");
            GL11.glTranslated(0, 0.875, -1.875);
            GL11.glRotated(-120, 1, 0, 0);
            GL11.glTranslated(0, -0.875, 1.875);
            GL11.glTranslated(0, 0.25, 0);
            m_silo_hatch.renderPart("Hatch");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibraryDoors.java:141; effective item owner ModBlocks.silo_hatch_large. */
    public static StaticItemDescriptor silo_hatch_large(Item item, MeshModel m_silo_hatch_large, ResourceLocation t_silo_hatch_large_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2, 0).scale(1.5, 1.5, 1.5).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            bindTexture(t_silo_hatch_large_tex);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            GL11.glTranslated(1, 0, 0);
            GL11.glRotated(90, 0, 1, 0);
            m_silo_hatch_large.renderPart("Frame");
            GL11.glTranslated(0, 0.875, -2.875);
            GL11.glRotated(-120, 1, 0, 0);
            GL11.glTranslated(0, -0.875, 2.875);
            GL11.glTranslated(0, 0.25, 0);
            m_silo_hatch_large.renderPart("Hatch");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibraryDoors.java:174; effective item owner ModBlocks.qe_sliding_door. */
    public static StaticItemDescriptor qe_sliding_door(Item item, MeshModel m_pheo_sliding_door, ResourceLocation t_pheo_sliding_door_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -3, 0).scale(5, 5, 5).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            bindTexture(t_pheo_sliding_door_tex);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            m_pheo_sliding_door.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibraryDoors.java:200; effective item owner ModBlocks.sliding_seal_door. */
    public static StaticItemDescriptor sliding_seal_door(Item item, MeshModel m_pheo_seal_door, ResourceLocation t_pheo_seal_door_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -5, 0).scale(7, 7, 7).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            bindTexture(t_pheo_seal_door_tex);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            m_pheo_seal_door.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibraryDoors.java:213; effective item owner ModBlocks.blast_door. */
    public static StaticItemDescriptor blast_door(Item item, MeshModel m_blast_door_base, MeshModel m_blast_door_tooth, MeshModel m_blast_door_slider, MeshModel m_blast_door_block, ResourceLocation t_blast_door_base_tex, ResourceLocation t_blast_door_tooth_tex, ResourceLocation t_blast_door_slider_tex, ResourceLocation t_blast_door_block_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -3, 0).scale(3, 3, 3).build(), new ResourceLocation("hbm:blocks/blast_door"), () -> {
            bindTexture(t_blast_door_base_tex); m_blast_door_base.renderAll();
            bindTexture(t_blast_door_tooth_tex); m_blast_door_tooth.renderAll();
            bindTexture(t_blast_door_slider_tex); m_blast_door_slider.renderAll();
            bindTexture(t_blast_door_block_tex); m_blast_door_block.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:30; effective item owner ModBlocks.obj_tester. */
    public static StaticItemDescriptor obj_tester(Item item, MeshModel m_soyuz_module, ResourceLocation t_soyuz_module_dome_tex, ResourceLocation t_soyuz_module_lander_tex, ResourceLocation t_soyuz_module_propulsion_tex, ResourceLocation t_soyuz_module_solar_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -4, 0).scale(2, 2, 2).build(), new ResourceLocation("hbm:blocks/test_render"), () -> {
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_soyuz_module_dome_tex); m_soyuz_module.renderPart("Dome");
            bindTexture(t_soyuz_module_lander_tex); m_soyuz_module.renderPart("Capsule");
            bindTexture(t_soyuz_module_propulsion_tex); m_soyuz_module.renderPart("Propulsion");
            bindTexture(t_soyuz_module_solar_tex); m_soyuz_module.renderPart("Solar");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:44; effective item owner ModBlocks.machine_cyclotron. */
    public static StaticItemDescriptor machine_cyclotron(Item item, MeshModel m_cyclotron, ResourceLocation t_cyclotron_tex, ResourceLocation t_cyclotron_ashes, ResourceLocation t_cyclotron_book, ResourceLocation t_cyclotron_gavel, ResourceLocation t_cyclotron_coin) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().scale(2.25, 2.25, 2.25).build(), new ResourceLocation("hbm:blocks/cyclotron"), () -> {
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_cyclotron_tex); m_cyclotron.renderPart("Body");
            bindTexture(t_cyclotron_ashes);  m_cyclotron.renderPart("B1");
            bindTexture(t_cyclotron_book); m_cyclotron.renderPart("B2");
            bindTexture(t_cyclotron_gavel); m_cyclotron.renderPart("B3");
            bindTexture(t_cyclotron_coin); m_cyclotron.renderPart("B4");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:58; effective item owner ModBlocks.iter. */
    public static StaticItemDescriptor iter(Item item, MeshModel m_iter, ResourceLocation t_iter_glass, ResourceLocation t_iter_motor, ResourceLocation t_iter_rails, ResourceLocation t_iter_toroidal, ResourceLocation t_iter_torus, ResourceLocation t_iter_solenoid) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1, 0).scale(4.5, 4.5, 4.5).build(), new ResourceLocation("hbm:blocks/iter"), () -> {
            GL11.glScaled(0.25, 0.25, 0.25);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_iter_glass); m_iter.renderPart("Windows");
            bindTexture(t_iter_motor); m_iter.renderPart("Motors");
            bindTexture(t_iter_rails); m_iter.renderPart("Rails");
            bindTexture(t_iter_toroidal); m_iter.renderPart("Toroidal");
            bindTexture(t_iter_torus); m_iter.renderPart("Torus");
            bindTexture(t_iter_solenoid); m_iter.renderPart("Solenoid");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:75; effective item owner ModBlocks.machine_press. */
    public static StaticItemDescriptor machine_press(Item item, MeshModel m_press_body, MeshModel m_press_head, ResourceLocation t_press_body_tex, ResourceLocation t_press_head_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -4, 0).scale(4.5, 4.5, 4.5).build(), new ResourceLocation("hbm:blocks/machine_press"), () -> {
            bindTexture(t_press_body_tex); m_press_body.renderAll();
            GL11.glTranslated(0, 0.5, 0);
            bindTexture(t_press_head_tex); m_press_head.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:87; effective item owner ModBlocks.machine_epress. */
    public static StaticItemDescriptor machine_epress(Item item, MeshModel m_epress_body, MeshModel m_epress_head, ResourceLocation t_epress_body_tex, ResourceLocation t_epress_head_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -4, 0).scale(4.5, 4.5, 4.5).build(), new ResourceLocation("hbm:blocks/machine_epress"), () -> {
            bindTexture(t_epress_body_tex); m_epress_body.renderAll();
            GL11.glTranslated(0, 1.5, 0);
            bindTexture(t_epress_head_tex); m_epress_head.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:99; effective item owner ModBlocks.machine_reactor_breeding. */
    public static StaticItemDescriptor machine_reactor_breeding(Item item, MeshModel m_breeder, ResourceLocation t_breeder_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -4.5, 0).scale(4.5, 4.5, 4.5).build(), new ResourceLocation("hbm:blocks/machine_reactor"), () -> {
            GL11.glShadeModel(GL11.GL_SMOOTH);
            GL11.glDisable(GL11.GL_CULL_FACE);
            bindTexture(t_breeder_tex); m_breeder.renderAll();
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:112; effective item owner ModBlocks.machine_large_turbine. */
    public static StaticItemDescriptor machine_large_turbine(Item item, MeshModel m_turbine, ResourceLocation t_turbine_tex, ResourceLocation t_universal_bright) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1, 0).scale(2.5, 2.5, 2.5).build(), new ResourceLocation("hbm:blocks/machine_large_turbine"), () -> {
            GL11.glRotated(90, 0, 1, 0);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            GL11.glDisable(GL11.GL_CULL_FACE);
            bindTexture(t_turbine_tex); m_turbine.renderPart("Body");
            bindTexture(t_universal_bright); m_turbine.renderPart("Blades");
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:127; effective item owner ModBlocks.reactor_research. */
    public static StaticItemDescriptor reactor_research(Item item, MeshModel m_reactor_small_base, MeshModel m_reactor_small_rods, ResourceLocation t_reactor_small_base_tex, ResourceLocation t_reactor_small_rods_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -4, 0).scale(4, 4, 4).build(), new ResourceLocation("hbm:blocks/machine_reactor_small"), () -> {
            bindTexture(t_reactor_small_base_tex); m_reactor_small_base.renderAll();
            bindTexture(t_reactor_small_rods_tex); m_reactor_small_rods.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:137; effective item owner ModBlocks.machine_industrial_generator. */
    public static StaticItemDescriptor machine_industrial_generator(Item item, MeshModel m_igen, ResourceLocation t_igen_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1, 0).scale(4, 4, 4).rotateY(90).build(), new ResourceLocation("minecraft:blocks/gold_block"), () -> {
            GL11.glTranslated(0, 0, -0.5);
            GL11.glScaled(0.75, 0.75, 0.75);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            GL11.glDisable(GL11.GL_CULL_FACE);
            bindTexture(t_igen_tex);
            m_igen.renderPart("Body");
            m_igen.renderPart("Rotor");
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:155; effective item owner ModBlocks.machine_radgen. */
    public static StaticItemDescriptor machine_radgen(Item item, MeshModel m_radgen, ResourceLocation t_radgen_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1, 0).scale(4.5, 4.5, 4.5).build(), new ResourceLocation("hbm:blocks/machine_radgen"), () -> {
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glTranslated(0.5, 0, 0);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_radgen_tex);
            m_radgen.renderPart("Base");
            m_radgen.renderPart("Rotor");
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glColor3f(0F, 1F, 0F);
            m_radgen.renderPart("Light");
            GL11.glColor3f(1F, 1F, 1F);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:206; effective item owner ModBlocks.machine_pumpjack. */
    public static StaticItemDescriptor machine_pumpjack(Item item, MeshModel m_pumpjack, ResourceLocation t_pumpjack_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2, 0).rotateY(90).scale(4, 4, 4).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glTranslatef(0, 0, 3);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_pumpjack_tex); m_pumpjack.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
            GL11.glEnable(GL11.GL_CULL_FACE);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:222; effective item owner ModBlocks.machine_flare. */
    public static StaticItemDescriptor machine_flare(Item item, MeshModel m_oilflare, ResourceLocation t_oilflare_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -4, 0).scale(2.25, 2.25, 2.25).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_oilflare_tex); m_oilflare.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
            GL11.glEnable(GL11.GL_CULL_FACE);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:236; effective item owner ModBlocks.machine_refinery. */
    public static StaticItemDescriptor machine_refinery(Item item, MeshModel m_refinery, ResourceLocation t_refinery_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -4, 0).scale(3, 3, 3).build(), new ResourceLocation("hbm:blocks/machine_refinery"), () -> {
            GL11.glRotated(180, 0, 1, 0);
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_refinery_tex);  m_refinery.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:249; effective item owner ModBlocks.machine_mining_laser. */
    public static StaticItemDescriptor machine_mining_laser(Item item, MeshModel m_mining_laser, ResourceLocation t_mining_laser_base_tex, ResourceLocation t_mining_laser_pivot_tex, ResourceLocation t_mining_laser_laser_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -0.5, 0).scale(3, 3, 3).build(), new ResourceLocation("hbm:blocks/machine_mining_laser"), () -> {
            bindTexture(t_mining_laser_base_tex); m_mining_laser.renderPart("Base");
            bindTexture(t_mining_laser_pivot_tex); m_mining_laser.renderPart("Pivot");
            GL11.glTranslated(0, -1, 0.75);
            GL11.glRotated(90, 1, 0, 0);
            bindTexture(t_mining_laser_laser_tex); m_mining_laser.renderPart("Laser");
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:262; effective item owner ModBlocks.plasma_heater. */
    public static StaticItemDescriptor plasma_heater(Item item, MeshModel m_iter, ResourceLocation t_iter_microwave) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1, 0).rotateY(90).scale(2.5, 2.5, 2.5).build(), new ResourceLocation("hbm:blocks/plasma_heater"), () -> {
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glTranslatef(0, 0, 14);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_iter_microwave); m_iter.renderPart("Microwave");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:276; effective item owner ModBlocks.tesla. */
    public static StaticItemDescriptor tesla(Item item, MeshModel m_tesla, ResourceLocation t_tesla_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -3, 0).scale(6, 6, 6).build(), new ResourceLocation("hbm:blocks/tesla"), () -> {
            GL11.glDisable(GL11.GL_CULL_FACE);
            bindTexture(t_tesla_tex); m_tesla.renderAll();
            GL11.glEnable(GL11.GL_CULL_FACE);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:287; effective item owner ModBlocks.boxcar. */
    public static StaticItemDescriptor boxcar(Item item, MeshModel m_boxcar, ResourceLocation t_boxcar_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().rotateY(90).translate(0, -1, 0).scale(4, 4, 4).build(), new ResourceLocation("hbm:blocks/boxcar"), () -> {
            GL11.glScaled(0.5, 0.5, 0.5);
            bindTexture(t_boxcar_tex); m_boxcar.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:298; effective item owner ModBlocks.boat. */
    public static StaticItemDescriptor boat(Item item, MeshModel m_duchessgambit, ResourceLocation t_duchessgambit_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().rotateY(-90).translate(0, 1, 0).scale(1.75, 1.75, 1.75).build(), new ResourceLocation("hbm:blocks/asphalt"), () -> {
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glTranslatef(0, 0, -3);
            bindTexture(t_duchessgambit_tex); m_duchessgambit.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:310; effective item owner ModBlocks.nuke_boy. */
    public static StaticItemDescriptor nuke_boy(Item item, MeshModel m_bomb_boy, ResourceLocation t_bomb_boy_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().scale(5, 5, 5).build(), new ResourceLocation("hbm:blocks/lilboy"), () -> {
            GL11.glTranslated(-1, 0, 0);
            bindTexture(t_bomb_boy_tex);
            m_bomb_boy.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:320; effective item owner ModBlocks.nuke_fstbmb. */
    public static StaticItemDescriptor nuke_fstbmb(Item item, MeshModel m_fstbmb, ResourceLocation t_fstbmb_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().scale(2.25, 2.25, 2.25).build(), new ResourceLocation("hbm:blocks/nuke_fstbmb"), () -> {
            GL11.glTranslated(1, 0, 0);
            GL11.glRotated(90, 0, 1, 0);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_fstbmb_tex);
            m_fstbmb.renderPart("Body");
            m_fstbmb.renderPart("Balefire");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:334; effective item owner ModBlocks.nuke_custom. */
    public static StaticItemDescriptor nuke_custom(Item item, MeshModel m_bomb_boy, ResourceLocation t_bomb_custom_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().scale(5, 5, 5).build(), new ResourceLocation("hbm:blocks/custom"), () -> {
            GL11.glTranslated(-1, 0, 0);
            bindTexture(t_bomb_custom_tex);
            m_bomb_boy.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:344; effective item owner ModBlocks.bomb_multi. */
    public static StaticItemDescriptor bomb_multi(Item item, MeshModel m_bomb_multi, ResourceLocation t_bomb_multi_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1, 0).scale(4, 4, 4).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glTranslated(0.75, 0, 0);
            GL11.glScaled(3, 3, 3);
            GL11.glTranslated(0, 0.5, 0);
            GL11.glRotatef(180, 1F, 0F, 0F);
            GL11.glRotatef(90, 0F, 1F, 0F);
            GL11.glDisable(GL11.GL_CULL_FACE);
            bindTexture(t_bomb_multi_tex);
            m_bomb_multi.renderAll();
            GL11.glEnable(GL11.GL_CULL_FACE);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:361; effective item owner ModBlocks.mine_ap. */
    public static StaticItemDescriptor mine_ap(Item item, MeshModel m_mine_ap, ResourceLocation t_mine_ap_grass_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().scale(8, 8, 8).build(), new ResourceLocation("hbm:blocks/mine_ap"), () -> {
            GL11.glScaled(1.25, 1.25, 1.25);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_mine_ap_grass_tex); m_mine_ap.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
            GL11.glEnable(GL11.GL_CULL_FACE);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:393; effective item owner ModBlocks.mine_shrap. */
    public static StaticItemDescriptor mine_shrap(Item item, MeshModel m_mine_ap, ResourceLocation t_mine_shrap_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().scale(8, 8, 8).build(), new ResourceLocation("hbm:blocks/mine_shrap"), () -> {
            GL11.glScaled(1.25, 1.25, 1.25);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_mine_shrap_tex); m_mine_ap.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
            GL11.glEnable(GL11.GL_CULL_FACE);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:406; effective item owner ModBlocks.mine_naval. */
    public static StaticItemDescriptor mine_naval(Item item, MeshModel m_mine_naval, ResourceLocation t_mine_naval_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, 2, -1).scale(5, 5, 5).build(), new ResourceLocation("hbm:blocks/mine_naval"), () -> {
            GL11.glTranslated(0, 0, 0);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_mine_naval_tex);
            m_mine_naval.renderAll();
            GL11.glEnable(GL11.GL_CULL_FACE);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:420; effective item owner ModBlocks.mine_fat. */
    public static StaticItemDescriptor mine_fat(Item item, MeshModel m_mine_fat, ResourceLocation t_mine_fat_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1, 0).scale(7, 7, 7).build(), new ResourceLocation("hbm:blocks/mine_fat"), () -> {
            GL11.glTranslated(0.25, 0, 0);
            GL11.glRotated(90, 0, 1, 0);
            GL11.glDisable(GL11.GL_CULL_FACE);
            bindTexture(t_mine_fat_tex);
            m_mine_fat.renderAll();
            GL11.glEnable(GL11.GL_CULL_FACE);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:434; effective item owner ModBlocks.machine_forcefield. */
    public static StaticItemDescriptor machine_forcefield(Item item, MeshModel m_radar_body, MeshModel m_forcefield_top, ResourceLocation t_forcefield_base_tex, ResourceLocation t_forcefield_top_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -4, 0).scale(6, 6, 6).build(), new ResourceLocation("hbm:blocks/machine_forcefield"), () -> {
            bindTexture(t_forcefield_base_tex); m_radar_body.renderAll();
            GL11.glTranslated(0, 1D, 0);
            bindTexture(t_forcefield_top_tex); m_forcefield_top.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:445; effective item owner ModBlocks.machine_missile_assembly. */
    public static StaticItemDescriptor machine_missile_assembly(Item item, MeshModel m_missile_assembly, ResourceLocation t_missile_assembly_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2.5, 0).scale(10, 10, 10).build(), new ResourceLocation("hbm:blocks/machine_missile_assembly"), () -> {
            GL11.glDisable(GL11.GL_CULL_FACE);
            bindTexture(t_missile_assembly_tex); m_missile_assembly.renderAll();
            GL11.glEnable(GL11.GL_CULL_FACE);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:456; effective item owner ModBlocks.launch_pad. */
    public static StaticItemDescriptor launch_pad(Item item, MeshModel m_missile_pad, ResourceLocation t_missile_pad_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1, 0).scale(3, 3, 3).build(), new ResourceLocation("hbm:blocks/launch_pad"), () -> {
            bindTexture(t_missile_pad_tex); m_missile_pad.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:465; effective item owner ModBlocks.compact_launcher. */
    public static StaticItemDescriptor compact_launcher(Item item, MeshModel m_compact_launcher, ResourceLocation t_compact_launcher_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -4, 0).scale(3.5, 3.5, 3.5).build(), new ResourceLocation("hbm:blocks/compact_launcher"), () -> {
            GL11.glScaled(0.5, 0.5, 0.5);
            bindTexture(t_compact_launcher_tex); m_compact_launcher.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:499; effective item owner ModBlocks.soyuz_capsule. */
    public static StaticItemDescriptor soyuz_capsule(Item item, MeshModel m_soyuz_lander, ResourceLocation t_soyuz_lander_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -4, 0).scale(5, 5, 5).build(), new ResourceLocation("hbm:blocks/soyuz_capsule"), () -> {
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_soyuz_lander_tex); m_soyuz_lander.renderPart("Capsule");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:510; effective item owner ModBlocks.machine_radar. */
    public static StaticItemDescriptor machine_radar(Item item, MeshModel m_radar, ResourceLocation t_radar_base_tex, ResourceLocation t_radar_dish_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -4, 0).scale(5, 5, 5).build(), new ResourceLocation("hbm:blocks/machine_radar"), () -> {
            GL11.glDisable(GL11.GL_CULL_FACE);
            bindTexture(t_radar_base_tex); m_radar.renderPart("Base");
            GL11.glTranslated(-0.125, 0, 0);
            bindTexture(t_radar_dish_tex); m_radar.renderPart("Dish");
            GL11.glEnable(GL11.GL_CULL_FACE);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:523; effective item owner ModBlocks.machine_uf6_tank. */
    public static StaticItemDescriptor machine_uf6_tank(Item item, MeshModel m_tank, ResourceLocation t_uf6_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -4, 0).scale(6, 6, 6).build(), new ResourceLocation("hbm:blocks/machine_uf6_tank"), () -> {
            GL11.glRotated(90, 0, -1, 0);
            bindTexture(t_uf6_tex); m_tank.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:533; effective item owner ModBlocks.machine_puf6_tank. */
    public static StaticItemDescriptor machine_puf6_tank(Item item, MeshModel m_tank, ResourceLocation t_puf6_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -4, 0).scale(6, 6, 6).build(), new ResourceLocation("hbm:blocks/machine_puf6_tank"), () -> {
            GL11.glRotated(90, 0, -1, 0);
            bindTexture(t_puf6_tex); m_tank.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:543; effective item owner ModBlocks.sat_dock. */
    public static StaticItemDescriptor sat_dock(Item item, MeshModel m_satDock, ResourceLocation t_satdock_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().scale(3, 3, 3).build(), new ResourceLocation("hbm:blocks/sat_dock"), () -> {
            GL11.glRotated(90, 0, -1, 0);
            bindTexture(t_satdock_tex); m_satDock.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:552; effective item owner ModBlocks.gas_dock. */
    public static StaticItemDescriptor gas_dock(Item item, MeshModel m_satDock, ResourceLocation t_satdock_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().scale(3, 3, 3).build(), new ResourceLocation("hbm:blocks/sat_dock"), () -> {
            GL11.glRotated(90, 0, -1, 0);
            bindTexture(t_satdock_tex); m_satDock.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:560; effective item owner ModBlocks.machine_microwave. */
    public static StaticItemDescriptor machine_microwave(Item item, MeshModel m_microwave, ResourceLocation t_microwave_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -4, 4).scale(5, 5, 5).build(), new ResourceLocation("hbm:blocks/machine_microwave"), () -> {
            GL11.glTranslated(-2, -2, 1);
            GL11.glScaled(3, 3, 3);
            bindTexture(t_microwave_tex);
            m_microwave.renderPart("mainbody_Cube.001");
            m_microwave.renderPart("window_Cube.002");
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:573; effective item owner ModBlocks.machine_solar_boiler. */
    public static StaticItemDescriptor machine_solar_boiler(Item item, MeshModel m_solar_boiler, ResourceLocation t_solar_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2.5, 0).scale(3.25, 3.25, 3.25).build(), new ResourceLocation("hbm:blocks/machine_solar_boiler"), () -> {
            GL11.glScaled(1, 1, 1);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_solar_tex); m_solar_boiler.renderPart("Base");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:585; effective item owner ModBlocks.solar_mirror. */
    public static StaticItemDescriptor solar_mirror(Item item, MeshModel m_solar_mirror, ResourceLocation t_solar_mirror_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -3, 0).scale(8, 8, 8).build(), new ResourceLocation("hbm:blocks/solar_mirror"), true, () -> {
            bindTexture(t_solar_mirror_tex);
            m_solar_mirror.renderPart("Base");
            GL11.glTranslated(0, 1, 0);
            GL11.glRotated(45, 0, 0, -1);
            GL11.glTranslated(0, -1, 0);
            m_solar_mirror.renderPart("Mirror");
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:599; effective item owner ModBlocks.turret_chekhov. */
    public static StaticItemDescriptor turret_chekhov(Item item, MeshModel m_turret_chekhov, ResourceLocation t_turret_base_tex, ResourceLocation t_turret_carriage_tex, ResourceLocation t_turret_chekhov_tex, ResourceLocation t_turret_chekhov_barrels_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -3, 0).scale(4, 4, 4).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glTranslated(-0.75, 0, 0);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_turret_base_tex); m_turret_chekhov.renderPart("Base");
            bindTexture(t_turret_carriage_tex); m_turret_chekhov.renderPart("Carriage");
            bindTexture(t_turret_chekhov_tex); m_turret_chekhov.renderPart("Body");
            bindTexture(t_turret_chekhov_barrels_tex); m_turret_chekhov.renderPart("Barrels");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:614; effective item owner ModBlocks.turret_friendly. */
    public static StaticItemDescriptor turret_friendly(Item item, MeshModel m_turret_chekhov, ResourceLocation t_turret_base_friendly_tex, ResourceLocation t_turret_carriage_friendly_tex, ResourceLocation t_turret_chekhov_tex, ResourceLocation t_turret_chekhov_barrels_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -3, 0).scale(4, 4, 4).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glTranslated(-0.75, 0, 0);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_turret_base_friendly_tex); m_turret_chekhov.renderPart("Base");
            bindTexture(t_turret_carriage_friendly_tex); m_turret_chekhov.renderPart("Carriage");
            bindTexture(t_turret_chekhov_tex); m_turret_chekhov.renderPart("Body");
            bindTexture(t_turret_chekhov_barrels_tex); m_turret_chekhov.renderPart("Barrels");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:629; effective item owner ModBlocks.turret_jeremy. */
    public static StaticItemDescriptor turret_jeremy(Item item, MeshModel m_turret_chekhov, MeshModel m_turret_jeremy, ResourceLocation t_turret_base_tex, ResourceLocation t_turret_carriage_tex, ResourceLocation t_turret_jeremy_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2, 0).scale(2.5, 2.5, 2.5).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glTranslated(-0.5, 0, 0);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_turret_base_tex); m_turret_chekhov.renderPart("Base");
            bindTexture(t_turret_carriage_tex); m_turret_chekhov.renderPart("Carriage");
            bindTexture(t_turret_jeremy_tex); m_turret_jeremy.renderPart("Gun");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:643; effective item owner ModBlocks.turret_tauon. */
    public static StaticItemDescriptor turret_tauon(Item item, MeshModel m_turret_chekhov, MeshModel m_turret_tauon, ResourceLocation t_turret_base_tex, ResourceLocation t_turret_carriage_tex, ResourceLocation t_turret_tauon_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2, 0).scale(5, 5, 5).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_turret_base_tex); m_turret_chekhov.renderPart("Base");
            bindTexture(t_turret_carriage_tex); m_turret_chekhov.renderPart("Carriage");
            bindTexture(t_turret_tauon_tex); m_turret_tauon.renderPart("Cannon");
            m_turret_tauon.renderPart("Rotor");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:657; effective item owner ModBlocks.turret_richard. */
    public static StaticItemDescriptor turret_richard(Item item, MeshModel m_turret_chekhov, MeshModel m_turret_richard, ResourceLocation t_turret_base_tex, ResourceLocation t_turret_carriage_tex, ResourceLocation t_turret_richard_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2, 0).scale(5, 5, 5).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_turret_base_tex); m_turret_chekhov.renderPart("Base");
            bindTexture(t_turret_carriage_tex); m_turret_chekhov.renderPart("Carriage");
            bindTexture(t_turret_richard_tex); m_turret_richard.renderPart("Launcher");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:670; effective item owner ModBlocks.turret_howard. */
    public static StaticItemDescriptor turret_howard(Item item, MeshModel m_turret_chekhov, MeshModel m_turret_howard, ResourceLocation t_turret_base_tex, ResourceLocation t_turret_carriage_ciws_tex, ResourceLocation t_turret_howard_tex, ResourceLocation t_turret_howard_barrels_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -4.5, 0).scale(4, 4, 4).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glTranslated(-0.75, 0, 0);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_turret_base_tex); m_turret_chekhov.renderPart("Base");
            bindTexture(t_turret_carriage_ciws_tex); m_turret_howard.renderPart("Carriage");
            bindTexture(t_turret_howard_tex); m_turret_howard.renderPart("Body");
            bindTexture(t_turret_howard_barrels_tex); m_turret_howard.renderPart("BarrelsTop");
            bindTexture(t_turret_howard_barrels_tex); m_turret_howard.renderPart("BarrelsBottom");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:686; effective item owner ModBlocks.turret_howard_damaged. */
    public static StaticItemDescriptor turret_howard_damaged(Item item, MeshModel m_turret_chekhov, MeshModel m_turret_howard, MeshModel m_turret_howard_damaged, ResourceLocation t_turret_base_rusted, ResourceLocation t_turret_carriage_ciws_rusted, ResourceLocation t_turret_howard_rusted, ResourceLocation t_turret_howard_barrels_rusted) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -4.5, 0).scale(4, 4, 4).build(), new ResourceLocation("hbm:blocks/block_rust"), () -> {
            GL11.glTranslated(-0.75, 0, 0);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_turret_base_rusted); m_turret_chekhov.renderPart("Base");
            bindTexture(t_turret_carriage_ciws_rusted); m_turret_howard.renderPart("Carriage");
            bindTexture(t_turret_howard_rusted); m_turret_howard_damaged.renderPart("Body");
            bindTexture(t_turret_howard_barrels_rusted); m_turret_howard_damaged.renderPart("BarrelsTop");
            bindTexture(t_turret_howard_barrels_rusted); m_turret_howard_damaged.renderPart("BarrelsBottom");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:702; effective item owner ModBlocks.machine_silex. */
    public static StaticItemDescriptor machine_silex(Item item, MeshModel m_silex, ResourceLocation t_silex_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2.5, 0).scale(3.25, 3.25, 3.25).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_silex_tex); m_silex.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:713; effective item owner ModBlocks.machine_fel. */
    public static StaticItemDescriptor machine_fel(Item item, MeshModel m_fel, ResourceLocation t_fel_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1, 0).scale(2, 2, 2).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glTranslated(1, 0, 0);
            GL11.glRotated(90, 0, -1, 0);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_fel_tex); m_fel.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:726; effective item owner ModBlocks.rbmk_console. */
    public static StaticItemDescriptor rbmk_console(Item item, MeshModel m_rbmk_console, ResourceLocation t_rbmk_console_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -3, 0).scale(2.5, 2.5, 2.5).build(), new ResourceLocation("hbm:blocks/rbmk/rbmk_console"), () -> {
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_rbmk_console_tex);
            m_rbmk_console.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:738; effective item owner ModBlocks.rbmk_crane_console. */
    public static StaticItemDescriptor rbmk_crane_console(Item item, MeshModel m_rbmk_crane_console, ResourceLocation t_rbmk_crane_console_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -3, 0).scale(3.5, 3.5, 3.5).build(), new ResourceLocation("hbm:blocks/rbmk/rbmk_crane_console"), () -> {
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_rbmk_crane_console_tex);
            m_rbmk_crane_console.renderPart("Console_Coonsole");
            m_rbmk_crane_console.renderPart("JoyStick");
            m_rbmk_crane_console.renderPart("Meter1");
            m_rbmk_crane_console.renderPart("Meter2");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:765; effective item owner ModBlocks.machine_storage_drum. */
    public static StaticItemDescriptor machine_storage_drum(Item item, MeshModel m_waste_drum, ResourceLocation t_waste_drum_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -3, 0).scale(5, 5, 5).build(), new ResourceLocation("hbm:blocks/machine_storage_drum"), () -> {
            GL11.glScaled(2, 2, 2);
            bindTexture(t_waste_drum_tex);
            m_waste_drum.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:776; effective item owner ModBlocks.turret_maxwell. */
    public static StaticItemDescriptor turret_maxwell(Item item, MeshModel m_turret_chekhov, MeshModel m_turret_howard, MeshModel m_turret_maxwell, ResourceLocation t_turret_base_tex, ResourceLocation t_turret_carriage_ciws_tex, ResourceLocation t_turret_maxwell_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(-1, -3, 0).scale(4, 4, 4).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_turret_base_tex); m_turret_chekhov.renderPart("Base");
            bindTexture(t_turret_carriage_ciws_tex); m_turret_howard.renderPart("Carriage");
            bindTexture(t_turret_maxwell_tex); m_turret_maxwell.renderPart("Microwave");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:789; effective item owner ModBlocks.turret_fritz. */
    public static StaticItemDescriptor turret_fritz(Item item, MeshModel m_turret_chekhov, MeshModel m_turret_fritz, ResourceLocation t_turret_base_tex, ResourceLocation t_turret_carriage_tex, ResourceLocation t_turret_fritz_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2, 0).scale(4, 4, 4).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_turret_base_tex); m_turret_chekhov.renderPart("Base");
            bindTexture(t_turret_carriage_tex); m_turret_chekhov.renderPart("Carriage");
            bindTexture(t_turret_fritz_tex); m_turret_fritz.renderPart("Gun");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:802; effective item owner ModBlocks.machine_bat9000. */
    public static StaticItemDescriptor machine_bat9000(Item item, MeshModel m_bat9000, ResourceLocation t_bat9000_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -3, 0).scale(2, 2, 2).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_bat9000_tex); m_bat9000.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:813; effective item owner ModBlocks.machine_orbus. */
    public static StaticItemDescriptor machine_orbus(Item item, MeshModel m_orbus, ResourceLocation t_orbus_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -3, 0).scale(2, 2, 2).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_orbus_tex); m_orbus.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:823; effective item owner ModBlocks.nuke_antimatter. */
    public static StaticItemDescriptor nuke_antimatter(Item item, MeshModel m_Antimatter, ResourceLocation t_Antimatter_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, 7, 0).scale(3.25, 3.25, 3.25).rotateX(180).build(), new ResourceLocation("hbm:blocks/thegadget"), () -> {
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_Antimatter_tex); m_Antimatter.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:835; effective item owner ModBlocks.watz. */
    public static StaticItemDescriptor watz(Item item, MeshModel m_watz, ResourceLocation t_watz_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1, 0).scale(2, 2, 2).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_watz_tex); m_watz.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:846; effective item owner ModBlocks.machine_fraction_tower. */
    public static StaticItemDescriptor machine_fraction_tower(Item item, MeshModel m_fraction_tower, ResourceLocation t_fraction_tower_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2.5, 0).scale(3.25, 3.25, 3.25).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glScaled(1, 1, 1);
            bindTexture(t_fraction_tower_tex); m_fraction_tower.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:856; effective item owner ModBlocks.fraction_spacer. */
    public static StaticItemDescriptor fraction_spacer(Item item, MeshModel m_fraction_spacer, ResourceLocation t_fraction_spacer_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().scale(3.25, 3.25, 3.25).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glScaled(1, 1, 1);
            bindTexture(t_fraction_spacer_tex); m_fraction_spacer.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:865; effective item owner ModBlocks.machine_tower_small. */
    public static StaticItemDescriptor machine_tower_small(Item item, MeshModel m_tower_small, ResourceLocation t_tower_small_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -4, 0).scale(3, 3, 3).build(), new ResourceLocation("hbm:blocks/brick_concrete"), () -> {
            GL11.glScaled(0.25, 0.25, 0.25);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_tower_small_tex); m_tower_small.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:888; effective item owner ModBlocks.machine_atmo_tower. */
    public static StaticItemDescriptor machine_atmo_tower(Item item, MeshModel m_atmo_tower, ResourceLocation t_atmo_tower_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -5, 0).scale(4, 4, 4).build(), new ResourceLocation("hbm:blocks/concrete"), () -> {
            GL11.glRotated(180, 0, 1, 0);
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_atmo_tower_tex); m_atmo_tower.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:901; effective item owner ModBlocks.machine_atmo_vent. */
    public static StaticItemDescriptor machine_atmo_vent(Item item, MeshModel m_atmo_vent, ResourceLocation t_atmo_vent_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -4, 0).scale(6, 6, 6).build(), new ResourceLocation("hbm:blocks/concrete"), () -> {
            GL11.glRotated(180, 0, 1, 0);
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_atmo_vent_tex); m_atmo_vent.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:916; effective item owner ModBlocks.machine_fracking_tower. */
    public static StaticItemDescriptor machine_fracking_tower(Item item, MeshModel m_fracking_tower, ResourceLocation t_fracking_tower_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -4.5, 0).scale(2.5, 2.5, 2.5).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glScaled(0.25, 0.25, 0.25);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            GL11.glDisable(GL11.GL_CULL_FACE);
            bindTexture(t_fracking_tower_tex); m_fracking_tower.renderAll();
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:940; effective item owner ModBlocks.machine_deuterium_tower. */
    public static StaticItemDescriptor machine_deuterium_tower(Item item, MeshModel m_deuterium_tower, ResourceLocation t_deuterium_tower_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -5, 0).scale(3, 3, 3).build(), new ResourceLocation("hbm:blocks/concrete"), () -> {
            GL11.glRotated(180, 0, 1, 0);
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_deuterium_tower_tex); m_deuterium_tower.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:955; effective item owner ModBlocks.reactor_zirnox. */
    public static StaticItemDescriptor reactor_zirnox(Item item, MeshModel m_zirnox, ResourceLocation t_zirnox_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2, 0).scale(2.8, 2.8, 2.8).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glScaled(0.75, 0.75, 0.75);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_zirnox_tex); m_zirnox.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:967; effective item owner ModBlocks.machine_catalytic_cracker. */
    public static StaticItemDescriptor machine_catalytic_cracker(Item item, MeshModel m_cracking_tower, ResourceLocation t_cracking_tower_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -3.5, 0).scale(1.8, 1.8, 1.8).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_cracking_tower_tex); m_cracking_tower.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:979; effective item owner ModBlocks.machine_liquefactor. */
    public static StaticItemDescriptor machine_liquefactor(Item item, MeshModel m_liquefactor, ResourceLocation t_liquefactor_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2.5, 0).scale(3, 3, 3).build(), new ResourceLocation("hbm:blocks/block_steel_machine"), () -> {
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_liquefactor_tex); m_liquefactor.renderPart("Main");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:990; effective item owner ModBlocks.machine_solidifier. */
    public static StaticItemDescriptor machine_solidifier(Item item, MeshModel m_solidifier, ResourceLocation t_solidifier_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2.5, 0).scale(3, 3, 3).build(), new ResourceLocation("hbm:blocks/block_steel_machine"), () -> {
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_solidifier_tex); m_solidifier.renderPart("Main");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:1001; effective item owner ModBlocks.machine_radiolysis. */
    public static StaticItemDescriptor machine_radiolysis(Item item, MeshModel m_radiolysis, ResourceLocation t_radiolysis_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2.5, 0).scale(3, 3, 3).build(), new ResourceLocation("hbm:blocks/block_steel_machine"), () -> {
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_radiolysis_tex); m_radiolysis.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:1012; effective item owner ModBlocks.red_pylon_large. */
    public static StaticItemDescriptor red_pylon_large(Item item, MeshModel m_pylon_large, ResourceLocation t_pylon_large_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -5, 0).scale(2.25, 2.25, 2.25).build(), new ResourceLocation("hbm:blocks/red_pylon_large"), () -> {
            GL11.glScaled(0.5, 0.5, 0.5);
            bindTexture(t_pylon_large_tex); m_pylon_large.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:1022; effective item owner ModBlocks.substation. */
    public static StaticItemDescriptor substation(Item item, MeshModel m_substation, ResourceLocation t_substation_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2.5, 0).scale(4.5, 4.5, 4.5).build(), new ResourceLocation("hbm:blocks/substation"), () -> {
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_substation_tex); m_substation.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:1034; effective item owner ModBlocks.charger. */
    public static StaticItemDescriptor charger(Item item, MeshModel m_charger, ResourceLocation t_charger_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -7, 0).scale(10, 10, 10).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glScaled(2, 2, 2);
            GL11.glTranslated(0.5, 0, 0);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_charger_tex);
            m_charger.renderPart("Base");
            m_charger.renderPart("Slide");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:1049; effective item owner ModBlocks.furnace_iron. */
    public static StaticItemDescriptor furnace_iron(Item item, MeshModel m_furnace_iron, ResourceLocation t_furnace_iron_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2, 0).scale(5, 5, 5).build(), new ResourceLocation("hbm:blocks/block_aluminium"), () -> {
            GL11.glRotated(90, 0, 1, 0);
            bindTexture(t_furnace_iron_tex);
            m_furnace_iron.renderPart("Main");
            m_furnace_iron.renderPart("Off");
        });
    }

    /** Original src/main/java/com/hbm/render/item/ItemRenderLibrary.java:1061; effective item owner ModBlocks.turret_arty. */
    public static StaticItemDescriptor turret_arty(Item item, MeshModel m_turret_arty, ResourceLocation t_turret_arty_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(-3, -4, 0).scale(3.5, 3.5, 3.5).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glRotated(-90, 0, 1, 0);
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_turret_arty_tex);
            m_turret_arty.renderPart("Base");
            m_turret_arty.renderPart("Carriage");
            GL11.glTranslated(0, 3, 0);
            GL11.glRotated(45, 1, 0, 0);
            GL11.glTranslated(0, -3, 0);
            m_turret_arty.renderPart("Cannon");
            m_turret_arty.renderPart("Barrel");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderSolderingStation.java:76; effective item owner ModBlocks.machine_soldering_station. */
    public static StaticItemDescriptor machine_soldering_station(Item item, MeshModel m_soldering_station, ResourceLocation t_soldering_station_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1, 0).scale(5, 5, 5).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            bindTexture(t_soldering_station_tex);
            m_soldering_station.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderOilburner.java:38; effective item owner ModBlocks.heater_oilburner. */
    public static StaticItemDescriptor heater_oilburner(Item item, MeshModel m_heater_oilburner, ResourceLocation t_heater_oilburner_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1.5, 0).scale(3.25, 3.25, 3.25).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_heater_oilburner_tex);
            m_heater_oilburner.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderArcFurnace.java:125; effective item owner ModBlocks.machine_arc_furnace. */
    public static StaticItemDescriptor machine_arc_furnace(Item item, MeshModel m_arc_furnace, ResourceLocation t_arc_furnace_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -3, 0).scale(3.5, 3.5, 3.5).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_arc_furnace_tex);
            m_arc_furnace.renderPart("Furnace");
            m_arc_furnace.renderPart("Lid");
            m_arc_furnace.renderPart("Ring1");
            m_arc_furnace.renderPart("Ring2");
            m_arc_furnace.renderPart("Ring3");
            m_arc_furnace.renderPart("Electrode1");
            m_arc_furnace.renderPart("Electrode2");
            m_arc_furnace.renderPart("Electrode3");
            m_arc_furnace.renderPart("Cable1");
            m_arc_furnace.renderPart("Cable2");
            m_arc_furnace.renderPart("Cable3");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderNukeN2.java:46; effective item owner ModBlocks.nuke_n2. */
    public static StaticItemDescriptor nuke_n2(Item item, MeshModel m_n2, ResourceLocation t_n2_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -5, 0).scale(2.25, 2.25, 2.25).build(), new ResourceLocation("hbm:blocks/nuke_n2"), () -> {
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_n2_tex); m_n2.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
            GL11.glEnable(GL11.GL_CULL_FACE);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderMilkReformer.java:60; effective item owner ModBlocks.machine_milk_reformer. */
    public static StaticItemDescriptor machine_milk_reformer(Item item, MeshModel m_milk_reformer, ResourceLocation t_milk_reformer_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -3, 0).scale(3.5, 3.5, 3.5).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_milk_reformer_tex);
            m_milk_reformer.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderHTR3.java:133; effective item owner ModBlocks.machine_htr3. */
    public static StaticItemDescriptor machine_htr3(Item item, MeshModel m_htr3, ResourceLocation t_lpw2_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -3, 0).scale(2.5, 2.5, 2.5).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_lpw2_tex);
            m_htr3.renderAllExcept("ExhaustVacuum");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderMagma.java:100; effective item owner ModBlocks.machine_magma. */
    public static StaticItemDescriptor machine_magma(Item item, MeshModel m_magma_drill, ResourceLocation t_magma_drill_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2, 0).scale(3, 3, 3).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glRotatef(90, 0F, 1F, 0F);
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_magma_drill_tex); m_magma_drill.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderCrucible.java:87; effective item owner ModBlocks.machine_crucible. */
    public static StaticItemDescriptor machine_crucible(Item item, MeshModel m_crucible_heat, ResourceLocation t_crucible_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1.5, 0).scale(3.25, 3.25, 3.25).build(), new ResourceLocation("hbm:blocks/brick_fire"), () -> {
            bindTexture(t_crucible_tex);
            m_crucible_heat.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderHydrotreater.java:39; effective item owner ModBlocks.machine_hydrotreater. */
    public static StaticItemDescriptor machine_hydrotreater(Item item, MeshModel m_hydrotreater, ResourceLocation t_hydrotreater_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -4, 0).scale(4, 4, 4).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_hydrotreater_tex);
            m_hydrotreater.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderFusionCollector.java:46; effective item owner ModBlocks.fusion_collector. */
    public static StaticItemDescriptor fusion_collector(Item item, MeshModel m_fusion_collector, ResourceLocation t_fusion_collector_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2, 0).scale(5, 5, 5).rotateY(90).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glRotatef(90, 0F, 1F, 0F);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_fusion_collector_tex);
            m_fusion_collector.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderDysonConverterTU.java:45; effective item owner ModBlocks.dyson_converter_tu. */
    public static StaticItemDescriptor dyson_converter_tu(Item item, MeshModel m_dyson_tu_converter, ResourceLocation t_dyson_tu_converter_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(3, 1, 0).scale(3.0D, 3.0D, 3.0D).build(), new ResourceLocation("hbm:blocks/block_steel_machine"), () -> {
            GL11.glScaled(0.55, 0.55, 0.55);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_dyson_tu_converter_tex);
            m_dyson_tu_converter.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderFurnaceSteel.java:72; effective item owner ModBlocks.furnace_steel. */
    public static StaticItemDescriptor furnace_steel(Item item, MeshModel m_furnace_steel, ResourceLocation t_furnace_steel_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1.5, 0).scale(3.25, 3.25, 3.25).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            bindTexture(t_furnace_steel_tex);
            m_furnace_steel.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderHTRNeo.java:119; effective item owner ModBlocks.machine_htrf4neo. */
    public static StaticItemDescriptor machine_htrf4neo(Item item, MeshModel m_htrf4_neo, ResourceLocation t_htrf4_neo_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1, 0).scale(1.5, 1.5, 1.5).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_htrf4_neo_tex);
            m_htrf4_neo.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderCombustionEngine.java:69; effective item owner ModBlocks.machine_combustion_engine. */
    public static StaticItemDescriptor machine_combustion_engine(Item item, MeshModel m_combustion_engine, ResourceLocation t_combustion_engine_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1, 0).scale(2.75, 2.75, 2.75).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glRotatef(90, 0F, 1F, 0F);
            GL11.glTranslated(0, 0, 2.75);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_combustion_engine_tex);
            m_combustion_engine.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderGeiger.java:44; effective item owner ModBlocks.geiger. */
    public static StaticItemDescriptor geiger(Item item, MeshModel m_geiger, ResourceLocation t_geiger_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().scale(10, 10, 10).build(), new ResourceLocation("hbm:blocks/geiger"), true, () -> {
            GL11.glTranslated(0.2F, 0, 0);
            GL11.glRotated(90, 0, 1, 0);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            GL11.glDisable(GL11.GL_CULL_FACE);
            bindTexture(t_geiger_tex);
            m_geiger.renderAll();
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderHTRF4.java:45; effective item owner ModBlocks.machine_htrf4. */
    public static StaticItemDescriptor machine_htrf4(Item item, MeshModel m_htrf4, ResourceLocation t_lpw2_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1, 0).scale(1.5, 1.5, 1.5).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_lpw2_tex);
            m_htrf4.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderExcavator.java:153; effective item owner ModBlocks.machine_excavator. */
    public static StaticItemDescriptor machine_excavator(Item item, MeshModel m_mining_drill, ResourceLocation t_mining_drill_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2, 0).scale(3, 3, 3).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glRotatef(90, 0F, 1F, 0F);
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_mining_drill_tex); m_mining_drill.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderMixer.java:85; effective item owner ModBlocks.machine_mixer. */
    public static StaticItemDescriptor machine_mixer(Item item, MeshModel m_mixer, ResourceLocation t_mixer_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -5, 0).scale(5, 5, 5).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glRotated(180, 0, 1, 0);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_mixer_tex);
            m_mixer.renderPart("Main");
            m_mixer.renderPart("Mixer");
            GL11.glShadeModel(GL11.GL_FLAT);
            GL11.glEnable(GL11.GL_CULL_FACE);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderNukeFleija.java:48; effective item owner ModBlocks.nuke_fleija. */
    public static StaticItemDescriptor nuke_fleija(Item item, MeshModel m_bomb_fleija, ResourceLocation t_bomb_fleija_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().scale(6.8, 6.8, 6.8).build(), new ResourceLocation("hbm:blocks/fleija"), () -> {
            GL11.glTranslated(0.125, 0, 0);
            GL11.glRotatef(90, 0F, 1F, 0F);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_bomb_fleija_tex);
            m_bomb_fleija.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderXenonThruster.java:89; effective item owner ModBlocks.machine_xenon_thruster. */
    public static StaticItemDescriptor machine_xenon_thruster(Item item, MeshModel m_xenon_thruster, ResourceLocation t_xenon_thruster_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -4, 0).scale(4, 4, 4).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_xenon_thruster_tex);
            m_xenon_thruster.renderPart("Thruster");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderCatalyticReformer.java:95; effective item owner ModBlocks.machine_catalytic_reformer. */
    public static StaticItemDescriptor machine_catalytic_reformer(Item item, MeshModel m_catalytic_reformer, ResourceLocation t_catalytic_reformer_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -3, 0).scale(3.5, 3.5, 3.5).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_catalytic_reformer_tex);
            m_catalytic_reformer.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderFusionCoupler.java:48; effective item owner ModBlocks.fusion_coupler. */
    public static StaticItemDescriptor fusion_coupler(Item item, MeshModel m_fusion_coupler, ResourceLocation t_fusion_coupler_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -3, 0).scale(6, 6, 6).rotateY(90).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glRotatef(90, 0F, 1F, 0F);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_fusion_coupler_tex);
            m_fusion_coupler.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderOrbitalComputer.java:54; effective item owner ModBlocks.orbital_station_computer. */
    public static StaticItemDescriptor orbital_station_computer(Item item, MeshModel m_orbital_computer, ResourceLocation t_orbital_computer_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2.5, 0).scale(6, 6, 6).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_orbital_computer_tex);
            m_orbital_computer.renderAllExcept("Drive");
            GL11.glShadeModel(GL11.GL_FLAT);
            GL11.glEnable(GL11.GL_CULL_FACE);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderTurbofan.java:67; effective item owner ModBlocks.machine_turbofan. */
    public static StaticItemDescriptor machine_turbofan(Item item, MeshModel m_turbofan, ResourceLocation t_turbofan_tex, ResourceLocation t_turbofan_back_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().rotateY(90).scale(2.25, 2.25, 2.25).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_turbofan_tex);
            m_turbofan.renderPart("Body");
            m_turbofan.renderPart("Blades");
            bindTexture(t_turbofan_back_tex);
            m_turbofan.renderPart("Afterburner");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderDysonLauncher.java:81; effective item owner ModBlocks.dyson_launcher. */
    public static StaticItemDescriptor dyson_launcher(Item item, MeshModel m_dyson_spinlaunch, ResourceLocation t_dyson_spinlaunch_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2, 0).scale(3.0D, 3.0D, 3.0D).build(), new ResourceLocation("hbm:blocks/block_steel_machine"), () -> {
            GL11.glScaled(0.25, 0.25, 0.25);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_dyson_spinlaunch_tex);
            m_dyson_spinlaunch.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderNukeSolinium.java:56; effective item owner ModBlocks.nuke_solinium. */
    public static StaticItemDescriptor nuke_solinium(Item item, MeshModel m_bomb_solinium, ResourceLocation t_bomb_solinium_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -0.125, 0).scale(5, 5, 5).build(), new ResourceLocation("hbm:blocks/nuke_solinium"), () -> {
            GL11.glRotated(90, 0, 1, 0);
            GL11.glTranslated(0, -0.125, 0);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            GL11.glDisable(GL11.GL_CULL_FACE);
            bindTexture(t_bomb_solinium_tex);
            m_bomb_solinium.renderAll();
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderStardar.java:69; effective item owner ModBlocks.machine_stardar. */
    public static StaticItemDescriptor machine_stardar(Item item, MeshModel m_stardar, ResourceLocation t_stardar_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -5, 0).scale(1.5D, 1.5D, 1.5D).build(), new ResourceLocation("hbm:blocks/block_steel_machine"), () -> {
            GL11.glScaled(0.55, 0.55, 0.55);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_stardar_tex);
            m_stardar.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
            GL11.glEnable(GL11.GL_CULL_FACE);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderChimneyIndustrial.java:41; effective item owner ModBlocks.chimney_industrial. */
    public static StaticItemDescriptor chimney_industrial(Item item, MeshModel m_chimney_industrial, ResourceLocation t_chimney_industrial_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -5, 0).scale(2.75, 2.75, 2.75).build(), new ResourceLocation("hbm:blocks/concrete_colored_ext.machine"), () -> {
            GL11.glScaled(0.25, 0.25, 0.25);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_chimney_industrial_tex);
            m_chimney_industrial.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
            GL11.glEnable(GL11.GL_CULL_FACE);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderHydroponic.java:72; effective item owner ModBlocks.hydrobay. */
    public static StaticItemDescriptor hydrobay(Item item, MeshModel m_hydroponic, ResourceLocation t_hydroponic_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1.75, 0).scale(3, 3, 3).build(), new ResourceLocation("hbm:blocks/block_steel_machine"), () -> {
            bindTexture(t_hydroponic_tex);
            m_hydroponic.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderVacuumCircuit.java:80; effective item owner ModBlocks.machine_vacuum_circuit. */
    public static StaticItemDescriptor machine_vacuum_circuit(Item item, MeshModel m_vac_cir_station, ResourceLocation t_vac_cir_station_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1, 0).scale(3.5, 3.5, 3.5).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            bindTexture(t_vac_cir_station_tex);
            m_vac_cir_station.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderVacuumDistill.java:37; effective item owner ModBlocks.machine_vacuum_distill. */
    public static StaticItemDescriptor machine_vacuum_distill(Item item, MeshModel m_vacuum_distill, ResourceLocation t_vacuum_distill_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -4, 0).scale(3, 3, 3).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_vacuum_distill_tex);
            m_vacuum_distill.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderNukeMike.java:46; effective item owner ModBlocks.nuke_mike. */
    public static StaticItemDescriptor nuke_mike(Item item, MeshModel m_bomb_mike, ResourceLocation t_bomb_mike_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -5, 0).scale(2.25, 2.25, 2.25).build(), new ResourceLocation("hbm:blocks/ivymike"), () -> {
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_bomb_mike_tex);
            m_bomb_mike.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderAshpit.java:60; effective item owner ModBlocks.machine_ashpit. */
    public static StaticItemDescriptor machine_ashpit(Item item, MeshModel m_heater_oven, ResourceLocation t_ashpit_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1, 0).scale(3.25, 3.25, 3.25).build(), new ResourceLocation("minecraft:blocks/stonebrick"), () -> {
            bindTexture(t_ashpit_tex);
            m_heater_oven.renderPart("Main");
            m_heater_oven.renderPart("Door");
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderArcWelder.java:76; effective item owner ModBlocks.machine_arc_welder. */
    public static StaticItemDescriptor machine_arc_welder(Item item, MeshModel m_arc_welder, ResourceLocation t_arc_welder_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2, 0).scale(4, 4, 4).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            bindTexture(t_arc_welder_tex);
            m_arc_welder.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderBatterySocket.java:118; effective item owner ModBlocks.machine_battery_socket. */
    public static StaticItemDescriptor machine_battery_socket(Item item, MeshModel m_battery_socket, ResourceLocation t_battery_socket_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2, 0).scale(5, 5, 5).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_battery_socket_tex);
            m_battery_socket.renderPart("Socket");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderNukeMan.java:46; effective item owner ModBlocks.nuke_man. */
    public static StaticItemDescriptor nuke_man(Item item, MeshModel m_bomb_man, ResourceLocation t_bomb_man_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2, 0).scale(5, 5, 5).build(), new ResourceLocation("hbm:blocks/fatman"), () -> {
            GL11.glRotated(180, 0, 1, 0);
            GL11.glTranslated(-0.75, 0, 0);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            GL11.glDisable(GL11.GL_CULL_FACE);
            bindTexture(t_bomb_man_tex);
            m_bomb_man.renderAll();
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderCompressorCompact.java:67; effective item owner ModBlocks.machine_compressor_compact. */
    public static StaticItemDescriptor machine_compressor_compact(Item item, MeshModel m_condenser, ResourceLocation t_compressor_compact_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(-1, -1, 0).scale(2.75, 2.75, 2.75).build(), new ResourceLocation("hbm:blocks/block_steel_machine"), () -> {
            GL11.glScaled(0.75, 0.75, 0.75);
            GL11.glTranslated(0.5, 0, 0);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_compressor_compact_tex); m_condenser.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderNukePrototype.java:50; effective item owner ModBlocks.nuke_prototype. */
    public static StaticItemDescriptor nuke_prototype(Item item, MeshModel m_bomb_prototype, ResourceLocation t_bomb_prototype_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, 0.125, 0).scale(3, 3, 3).build(), new ResourceLocation("hbm:blocks/prototype"), () -> {
            GL11.glRotated(90, 0, 1, 0);
            GL11.glTranslated(0, 0.125, 0);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            GL11.glDisable(GL11.GL_CULL_FACE);
            bindTexture(t_bomb_prototype_tex);
            m_bomb_prototype.renderAll();
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderLPW2.java:294; effective item owner ModBlocks.machine_lpw2. */
    public static StaticItemDescriptor machine_lpw2(Item item, MeshModel m_lpw2, ResourceLocation t_lpw2_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(1, -1, 0).scale(1.6, 1.6, 1.6).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_lpw2_tex);
            m_lpw2.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderDysonConverterAnatmogenesis.java:56; effective item owner ModBlocks.dyson_converter_anatmogenesis. */
    public static StaticItemDescriptor dyson_converter_anatmogenesis(Item item, MeshModel m_dyson_anatmogenesis, ResourceLocation t_dyson_anatmogenesis_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2, 0).scale(2.5D, 2.5D, 2.5D).build(), new ResourceLocation("hbm:blocks/block_steel_machine"), () -> {
            GL11.glScaled(0.55, 0.55, 0.55);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_dyson_anatmogenesis_tex);
            m_dyson_anatmogenesis.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderTurbineGas.java:50; effective item owner ModBlocks.machine_turbinegas. */
    public static StaticItemDescriptor machine_turbinegas(Item item, MeshModel m_turbinegas, ResourceLocation t_turbinegas_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1, 1.5).scale(2.5, 2.5, 2.5).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glScaled(0.75, 0.75, 0.75);
            GL11.glRotated(90, 0, 1, 0);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_turbinegas_tex);
            m_turbinegas.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderFirebox.java:73; effective item owner ModBlocks.heater_firebox. */
    public static StaticItemDescriptor heater_firebox(Item item, MeshModel m_heater_firebox, ResourceLocation t_heater_firebox_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1, 0).scale(3.25, 3.25, 3.25).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            bindTexture(t_heater_firebox_tex);
            m_heater_firebox.renderPart("Main");
            m_heater_firebox.renderPart("Door");
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderDrain.java:46; effective item owner ModBlocks.machine_drain. */
    public static StaticItemDescriptor machine_drain(Item item, MeshModel m_drain, ResourceLocation t_drain_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(-1, -1, 0).scale(5, 5, 5).build(), new ResourceLocation("hbm:blocks/concrete"), () -> {
            GL11.glRotatef(180, 0F, 1F, 0F);
            GL11.glTranslated(0.75, 0, 0);
            bindTexture(t_drain_tex); m_drain.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderRotaryFurnace.java:57; effective item owner ModBlocks.machine_rotary_furnace. */
    public static StaticItemDescriptor machine_rotary_furnace(Item item, MeshModel m_rotary_furnace, ResourceLocation t_rotary_furnace_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2, 0).scale(3.5, 3.5, 3.5).build(), new ResourceLocation("hbm:blocks/brick_fire"), () -> {
            GL11.glScaled(0.625, 0.625, 0.625);
            GL11.glRotatef(90, 0F, 1F, 0F);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_rotary_furnace_tex);
            m_rotary_furnace.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderRocketAssembly.java:90; effective item owner ModBlocks.machine_rocket_assembly. */
    public static StaticItemDescriptor machine_rocket_assembly(Item item, MeshModel m_rocket_assembly, ResourceLocation t_rocket_assembly_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1, 0).scale(2.0D, 2.0D, 2.0D).build(), new ResourceLocation("hbm:blocks/machine_missile_assembly"), () -> {
            GL11.glScaled(0.55, 0.55, 0.55);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_rocket_assembly_tex);
            m_rocket_assembly.renderPart("Base");
            GL11.glShadeModel(GL11.GL_FLAT);
            GL11.glEnable(GL11.GL_CULL_FACE);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderDriveProcessor.java:53; effective item owner ModBlocks.machine_drive_processor. */
    public static StaticItemDescriptor machine_drive_processor(Item item, MeshModel m_drive_processor, ResourceLocation t_drive_processor_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().scale(7D, 7D, 7D).build(), new ResourceLocation("hbm:blocks/block_steel_machine"), () -> {
            GL11.glTranslated(0.5, 0, 0);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            
            bindTexture(t_drive_processor_tex);
            m_drive_processor.renderPart("Base");
            m_drive_processor.renderPart("Drive");
            
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderBatteryREDD.java:245; effective item owner ModBlocks.machine_battery_redd. */
    public static StaticItemDescriptor machine_battery_redd(Item item, MeshModel m_battery_redd, ResourceLocation t_battery_redd_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -3, 0).scale(2.5, 2.5, 2.5).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glRotated(-90, 0, 1, 0);
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_battery_redd_tex);
            m_battery_redd.renderPart("Base");
            m_battery_redd.renderPart("Wheel");
            GL11.glDisable(GL11.GL_LIGHTING);
            m_battery_redd.renderPart("Lights");
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderFusionBoiler.java:46; effective item owner ModBlocks.fusion_boiler. */
    public static StaticItemDescriptor fusion_boiler(Item item, MeshModel m_fusion_boiler, ResourceLocation t_fusion_boiler_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1, 0).scale(3.5, 3.5, 3.5).rotateY(90).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glRotatef(90, 0F, 1F, 0F);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_fusion_boiler_tex);
            m_fusion_boiler.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderRBMKAutoloader.java:45; effective item owner ModBlocks.rbmk_autoloader. */
    public static StaticItemDescriptor rbmk_autoloader(Item item, MeshModel m_rbmk_autoloader, ResourceLocation t_rbmk_autoloader_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -6, 0).scale(1.75, 1.75, 1.75).build(), new ResourceLocation("hbm:blocks/rbmk_autoloader"), () -> {
            GL11.glRotated(180, 0, 1, 0);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_rbmk_autoloader_tex);
            m_rbmk_autoloader.renderPart("Base");
            m_rbmk_autoloader.renderPart("Piston");
            GL11.glShadeModel(GL11.GL_FLAT);
            GL11.glEnable(GL11.GL_CULL_FACE);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderFusionBreeder.java:46; effective item owner ModBlocks.fusion_breeder. */
    public static StaticItemDescriptor fusion_breeder(Item item, MeshModel m_fusion_breeder, ResourceLocation t_fusion_breeder_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -3, 0).scale(5, 5, 5).rotateY(90).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glRotatef(90, 0F, 1F, 0F);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_fusion_breeder_tex);
            m_fusion_breeder.renderPart("Breeder");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderCryoDistill.java:50; effective item owner ModBlocks.machine_cryo_distill. */
    public static StaticItemDescriptor machine_cryo_distill(Item item, MeshModel m_cryo_distill, ResourceLocation t_cryodistill_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -3, 0).scale(3.5, 3.5, 3.5).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_cryodistill_tex);
            m_cryo_distill.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderTransporterRocket.java:89; effective item owner ModBlocks.transporter_rocket. */
    public static StaticItemDescriptor transporter_rocket(Item item, MeshModel m_transporter_pad, ResourceLocation t_transporter_pad_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().scale(3D, 3D, 3D).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glTranslated(0.5, 0, 0);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            
            bindTexture(t_transporter_pad_tex);
            m_transporter_pad.renderAll();
            
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderChimneyBrick.java:41; effective item owner ModBlocks.chimney_brick. */
    public static StaticItemDescriptor chimney_brick(Item item, MeshModel m_chimney_brick, ResourceLocation t_chimney_brick_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -5, 0).scale(2.25, 2.25, 2.25).build(), new ResourceLocation("hbm:blocks/brick_fire"), () -> {
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_chimney_brick_tex);
            m_chimney_brick.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
            GL11.glEnable(GL11.GL_CULL_FACE);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderDysonConverterHE.java:69; effective item owner ModBlocks.dyson_converter_he. */
    public static StaticItemDescriptor dyson_converter_he(Item item, MeshModel m_dyson_he_converter, ResourceLocation t_dyson_he_converter_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1, 0).scale(3.0D, 3.0D, 3.0D).build(), new ResourceLocation("hbm:blocks/block_steel_machine"), () -> {
            GL11.glScaled(0.55, 0.55, 0.55);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_dyson_he_converter_tex);
            m_dyson_he_converter.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderAirScrubber.java:56; effective item owner ModBlocks.air_scrubber. */
    public static StaticItemDescriptor air_scrubber(Item item, MeshModel m_air_scrubber, ResourceLocation t_air_scrubber_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -4, 0).scale(4, 4, 4).build(), new ResourceLocation("hbm:blocks/block_steel_machine"), () -> {
            GL11.glScaled(1.5, 1.5, 1.5);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_air_scrubber_tex);
            m_air_scrubber.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
            GL11.glEnable(GL11.GL_CULL_FACE);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderRadiator.java:51; effective item owner ModBlocks.machine_radiator. */
    public static StaticItemDescriptor machine_radiator(Item item, MeshModel m_radiator, ResourceLocation t_radiator_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -5, 0).scale(1.2, 1.2, 1.2).build(), new ResourceLocation("hbm:blocks/block_steel_machine"), () -> {
            GL11.glScaled(0.75, 0.75, 0.75);
            
            
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_radiator_tex);
            m_radiator.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderAtmosphericEmitter.java:59; effective item owner ModBlocks.machine_atmo_emitter. */
    public static StaticItemDescriptor machine_atmo_emitter(Item item, MeshModel m_atmo_emitter, ResourceLocation t_atmo_emitter_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().scale(4, 4, 4).translate(0, -1, 0).build(), new ResourceLocation("hbm:blocks/block_steel_machine"), () -> {
            bindTexture(t_atmo_emitter_tex); m_atmo_emitter.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderPyroOven.java:65; effective item owner ModBlocks.machine_pyrooven. */
    public static StaticItemDescriptor machine_pyrooven(Item item, MeshModel m_pyrooven, ResourceLocation t_pyrooven_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1, 0).scale(3.5, 3.5, 3.5).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glRotatef(90, 0F, 1F, 0F);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_pyrooven_tex);
            m_pyrooven.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderTurretSentry.java:79; effective item owner ModBlocks.turret_sentry. */
    public static StaticItemDescriptor turret_sentry(Item item, MeshModel m_turret_sentry, ResourceLocation t_turret_sentry_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -4, 0).scale(7, 7, 7).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glRotated(90, 0, 1, 0);
            bindTexture(t_turret_sentry_tex);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            m_turret_sentry.renderPart("Base");
            m_turret_sentry.renderPart("Pivot");
            m_turret_sentry.renderPart("Body");
            m_turret_sentry.renderPart("Drum");
            m_turret_sentry.renderPart("BarrelL");
            m_turret_sentry.renderPart("BarrelR");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderAlkylation.java:47; effective item owner ModBlocks.machine_alkylation. */
    public static StaticItemDescriptor machine_alkylation(Item item, MeshModel m_alkyl, ResourceLocation t_alkyl_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2, 0).scale(5, 5, 5).build(), new ResourceLocation("hbm:blocks/block_steel_machine"), () -> {
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_alkyl_tex);
            m_alkyl.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderLaunchPadRusted.java:63; effective item owner ModBlocks.launch_pad_rusted. */
    public static StaticItemDescriptor launch_pad_rusted(Item item, MeshModel m_missile_pad, ResourceLocation t_missile_pad_rusted_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1, 0).scale(3, 3, 3).build(), new ResourceLocation("hbm:blocks/block_rust"), () -> {
            bindTexture(t_missile_pad_rusted_tex); m_missile_pad.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderElectrolyser.java:51; effective item owner ModBlocks.machine_electrolyser. */
    public static StaticItemDescriptor machine_electrolyser(Item item, MeshModel m_electrolyser, ResourceLocation t_electrolyser_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(-1, -1, 0).scale(2.5, 2.5, 2.5).build(), new ResourceLocation("hbm:blocks/block_steel_machine"), () -> {
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_electrolyser_tex); m_electrolyser.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderHeaterHeatex.java:45; effective item owner ModBlocks.heater_heatex. */
    public static StaticItemDescriptor heater_heatex(Item item, MeshModel m_heater_heatex, ResourceLocation t_heater_heatex_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1, 0).scale(3.25, 3.25, 3.25).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_heater_heatex_tex);
            m_heater_heatex.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderAmmoPress.java:63; effective item owner ModBlocks.machine_ammo_press. */
    public static StaticItemDescriptor machine_ammo_press(Item item, MeshModel m_ammo_press, ResourceLocation t_ammo_press_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2.5, 0).scale(5, 5, 5).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glRotatef(90, 0F, 1F, 0F);
            bindTexture(t_ammo_press_tex);
            m_ammo_press.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderRefueler.java:87; effective item owner ModBlocks.refueler. */
    public static StaticItemDescriptor refueler(Item item, MeshModel m_refueler, ResourceLocation t_refueler_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -3, 0).scale(6, 6, 6).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glScaled(2, 2, 2);
            GL11.glTranslated(0.5, 0, 0);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_refueler_tex);
            m_refueler.renderPart("Fueler");
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderHeatingOven.java:71; effective item owner ModBlocks.heater_oven. */
    public static StaticItemDescriptor heater_oven(Item item, MeshModel m_heater_oven, ResourceLocation t_heater_oven_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1, 0).scale(3.25, 3.25, 3.25).build(), new ResourceLocation("hbm:blocks/brick_fire"), () -> {
            bindTexture(t_heater_oven_tex);
            m_heater_oven.renderPart("Main");
            m_heater_oven.renderPart("Door");
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderDishControl.java:57; effective item owner ModBlocks.machine_dish_controller. */
    public static StaticItemDescriptor machine_dish_controller(Item item, MeshModel m_dish_controller, ResourceLocation t_dish_controller_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -0.5, 0).scale(6D, 6D, 6D).build(), new ResourceLocation("hbm:blocks/block_steel_machine"), () -> {
            GL11.glTranslated(0.5, 0, 0);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_dish_controller_tex);
            m_dish_controller.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderIntake.java:59; effective item owner ModBlocks.machine_intake. */
    public static StaticItemDescriptor machine_intake(Item item, MeshModel m_intake, ResourceLocation t_intake_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().scale(5, 5, 5).build(), new ResourceLocation("hbm:blocks/block_steel_machine"), () -> {
            bindTexture(t_intake_tex); m_intake.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderNukeTsar.java:46; effective item owner ModBlocks.nuke_tsar. */
    public static StaticItemDescriptor nuke_tsar(Item item, MeshModel m_bomb_tsar, ResourceLocation t_bomb_tsar_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().scale(2.25, 2.25, 2.25).build(), new ResourceLocation("hbm:blocks/tsarbomba"), () -> {
            GL11.glTranslated(1.5, 0, 0);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_bomb_tsar_tex); m_bomb_tsar.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
            GL11.glEnable(GL11.GL_CULL_FACE);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderSolarPanel.java:77; effective item owner ModBlocks.machine_solar. */
    public static StaticItemDescriptor machine_solar(Item item, MeshModel m_solarp, ResourceLocation t_solarp_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, 0, 0).scale(3.75, 3.75, 3.75).build(), new ResourceLocation("hbm:blocks/block_steel_machine"), () -> {
            GL11.glScaled(1, 1, 1);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_solarp_tex);
            m_solarp.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
            GL11.glEnable(GL11.GL_CULL_FACE);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderFluidPump.java:45; effective item owner ModBlocks.fluid_pump. */
    public static StaticItemDescriptor fluid_pump(Item item, MeshModel m_fluid_pump, ResourceLocation t_fluid_pump_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2, 0).scale(5, 5, 5).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glScaled(2, 2, 2);
            GL11.glRotatef(90, 0F, 1F, 0F);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_fluid_pump_tex);
            m_fluid_pump.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderDysonReceiver.java:90; effective item owner ModBlocks.dyson_receiver. */
    public static StaticItemDescriptor dyson_receiver(Item item, MeshModel m_dyson_receiver, ResourceLocation t_dyson_receiver_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -4, 0).scale(1.5D, 1.5D, 1.5D).build(), new ResourceLocation("hbm:blocks/block_steel_machine"), () -> {
            GL11.glScaled(0.55, 0.55, 0.55);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_dyson_receiver_tex);
            m_dyson_receiver.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderTelex.java:45; effective item owner ModBlocks.radio_telex. */
    public static StaticItemDescriptor radio_telex(Item item, MeshModel m_telex, ResourceLocation t_telex_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2, 0).scale(6, 6, 6).build(), new ResourceLocation("hbm:blocks/radio_telex"), () -> {
            GL11.glTranslated(0, 0, -0.5);
            bindTexture(t_telex_tex);
            m_telex.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderExposureChamber.java:120; effective item owner ModBlocks.machine_exposure_chamber. */
    public static StaticItemDescriptor machine_exposure_chamber(Item item, MeshModel m_exposure_chamber, ResourceLocation t_exposure_chamber_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1.5, 0).scale(3, 3, 3).build(), new ResourceLocation("hbm:blocks/block_aluminium"), () -> {
            GL11.glTranslated(1.5, 0, 0);
            GL11.glRotated(90, 0, 1, 0);
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_exposure_chamber_tex);
            m_exposure_chamber.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
            GL11.glEnable(GL11.GL_CULL_FACE);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderWoodBurner.java:48; effective item owner ModBlocks.machine_wood_burner. */
    public static StaticItemDescriptor machine_wood_burner(Item item, MeshModel m_wood_burner, ResourceLocation t_wood_burner_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -4, 0).scale(3.5, 3.5, 3.5).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glRotatef(90, 0F, 1F, 0F);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_wood_burner_tex);
            m_wood_burner.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderConveyorPress.java:67; effective item owner ModBlocks.machine_conveyor_press. */
    public static StaticItemDescriptor machine_conveyor_press(Item item, MeshModel m_conveyor_press, ResourceLocation t_conveyor_press_tex, ResourceLocation t_conveyor_press_belt_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -4, 0).scale(4.5, 4.5, 4.5).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            bindTexture(t_conveyor_press_tex);
            m_conveyor_press.renderPart("Press");
            m_conveyor_press.renderPart("Piston");
            bindTexture(t_conveyor_press_belt_tex);
            m_conveyor_press.renderPart("Belt");
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderLaunchPadRocket.java:84; effective item owner ModBlocks.launch_pad_rocket. */
    public static StaticItemDescriptor launch_pad_rocket(Item item, MeshModel m_rocket_pad, ResourceLocation t_rocket_pad_tex, ResourceLocation t_rocket_pad_support_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -2, 0).scale(1.25D, 1.25D, 1.25D).build(), new ResourceLocation("hbm:blocks/launch_pad"), () -> {
            GL11.glScaled(0.55, 0.55, 0.55);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_rocket_pad_tex);
            m_rocket_pad.renderPart("Base");
            bindTexture(t_rocket_pad_support_tex);
            m_rocket_pad.renderAllExcept("Base");
            GL11.glShadeModel(GL11.GL_FLAT);
            GL11.glEnable(GL11.GL_CULL_FACE);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderDerrick.java:48; effective item owner ModBlocks.machine_well. */
    public static StaticItemDescriptor machine_well(Item item, MeshModel m_derrick, ResourceLocation t_derrick_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -4, 0).scale(3, 3, 3).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glRotatef(90, 0F, 1F, 0F);
            GL11.glScaled(0.5, 0.5, 0.5);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_derrick_tex); m_derrick.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderFurnaceCombination.java:97; effective item owner ModBlocks.furnace_combination. */
    public static StaticItemDescriptor furnace_combination(Item item, MeshModel m_combination_oven, ResourceLocation t_combination_oven_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -1.5, 0).scale(3.25, 3.25, 3.25).build(), new ResourceLocation("hbm:blocks/brick_light_alt"), () -> {
            bindTexture(t_combination_oven_tex);
            m_combination_oven.renderAll();
        });
    }

    /** Original src/main/java/com/hbm/render/tileentity/RenderCoker.java:39; effective item owner ModBlocks.machine_coker. */
    public static StaticItemDescriptor machine_coker(Item item, MeshModel m_coker, ResourceLocation t_coker_tex) {
        return new StaticItemDescriptor(item, new ItemTransformMatrix().translate(0, -5, 0).scale(2.75, 2.75, 2.75).build(), new ResourceLocation("hbm:blocks/block_steel"), () -> {
            GL11.glScaled(0.25, 0.25, 0.25);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            bindTexture(t_coker_tex);
            m_coker.renderAll();
            GL11.glShadeModel(GL11.GL_FLAT);
            GL11.glEnable(GL11.GL_CULL_FACE);
        });
    }
}
