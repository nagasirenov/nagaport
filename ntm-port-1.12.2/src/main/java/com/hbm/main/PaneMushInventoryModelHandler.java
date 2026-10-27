package com.hbm.main;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/** Explicit inventory routes for legacy blocks whose item assets exist but
 * otherwise never get registered with Forge's item model mesher. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class PaneMushInventoryModelHandler {

    private PaneMushInventoryModelHandler() { }

    @SubscribeEvent
    public static void register(ModelRegistryEvent event) {
        register(ModBlocks.reinforced_glass_pane);
        register(ModBlocks.reinforced_laminate_pane);
        register(ModBlocks.mush);
    }

    private static void register(Block block) {
        Item item = Item.getItemFromBlock(block);
        ResourceLocation name = item.getRegistryName();
        if(name == null) throw new IllegalStateException("Missing item registry name for " + block);
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(name, "inventory"));
    }
}
