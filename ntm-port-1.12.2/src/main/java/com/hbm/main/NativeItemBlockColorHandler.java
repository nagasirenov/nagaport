package com.hbm.main;

import com.hbm.blocks.generic.BlockMetalFence;
import com.hbm.blocks.generic.BlockScaffold;
import com.hbm.items.block.ItemBlockBase;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;

/** Native color dispatch for the audited native owners only. No legacy reflective color adapter. */
@Mod.EventBusSubscriber(modid = "hbm", value = Side.CLIENT)
public final class NativeItemBlockColorHandler {
    private NativeItemBlockColorHandler() { }
    @SubscribeEvent public static void register(ColorHandlerEvent.Item event) {
        for(Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
            if(!(item instanceof ItemBlockBase)) continue;
            Block block = ((ItemBlockBase)item).getBlock();
            if(block.getRegistryName() == null || !"hbm".equals(block.getRegistryName().getNamespace())) continue;
            // Other owners must first migrate their block render-color contract and model tint indices.
            if(block.getClass() != Block.class && block.getClass() != BlockScaffold.class && block.getClass() != BlockMetalFence.class) continue;
            event.getItemColors().registerItemColorHandler((stack, index) ->
                event.getBlockColors().colorMultiplier(block.getStateFromMeta(stack.getMetadata()), null, null, index), item);
        }
    }
}
