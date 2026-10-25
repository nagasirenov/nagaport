package com.hbm.main;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.hbm.lib.RefStrings;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;

/** Composes native state owners; only already non-model world states become empty. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class InvisibleWorldModelHandler {
    private InvisibleWorldModelHandler() { }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void register(ModelRegistryEvent event) {
        Map<?, ?> existing = stateMappers();
        int blocks = 0, states = 0;
        for(Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            if(block.getRegistryName() == null || !RefStrings.MODID.equals(block.getRegistryName().getNamespace())) continue;
            if(BakeOwnedModelLoader.hasBlockstate(block.getRegistryName())) continue;
            boolean needsRoute = false;
            for(IBlockState state : block.getBlockState().getValidStates()) {
                if(nonModel(state)) { needsRoute = true; break; }
            }
            if(!needsRoute) continue;
            IStateMapper previous = (IStateMapper) existing.get(block.delegate);
            IStateMapper original = previous == null ? new StateMap.Builder().build() : previous;
            // Keep the prior owner's MODEL mappings verbatim, including shape/metadata.
            Map<IBlockState, ModelResourceLocation> routes = new HashMap<>(original.putStateModelLocations(block));
            for(IBlockState state : block.getBlockState().getValidStates()) {
                if(nonModel(state)) {
                    routes.put(state, FusionCouplerModelHandler.emptyWorldLocation());
                    states++;
                }
            }
            ModelLoader.setCustomStateMapper(block, owner -> new HashMap<>(routes));
            blocks++;
        }
        MainRegistry.logger.info("Pre-load empty world routes: {} blocks, {} already invisible/tile-rendered states", blocks, states);
    }

    private static boolean nonModel(IBlockState state) {
        EnumBlockRenderType type = state.getBlock().getRenderType(state);
        return type == EnumBlockRenderType.INVISIBLE || type == EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    private static Map<?, ?> stateMappers() {
        try {
            // Forge implementation field, not a Minecraft obfuscated name. Read only;
            // all writes still go through Forge's public setCustomStateMapper API.
            Field field = ModelLoader.class.getDeclaredField("customStateMappers");
            field.setAccessible(true);
            return (Map<?, ?>) field.get(null);
        } catch(ReflectiveOperationException failure) {
            throw new IllegalStateException("Cannot preserve existing world state model owners", failure);
        }
    }
}
