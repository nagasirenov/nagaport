package com.hbm.main;

import java.util.Set;
import java.util.TreeSet;

import com.hbm.lib.RefStrings;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Logs, once per game start, every NTM item stack and block state whose baked model is the
 * missing model or uses the missing sprite. Built-in (TESR) models are not inspectable here.
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class MissingTextureAudit {
    private static boolean done;

    private MissingTextureAudit() { }

    @SubscribeEvent
    public static void onGui(GuiOpenEvent event) {
        if(done || !(event.getGui() instanceof GuiMainMenu)) return;
        done = true;
        Minecraft mc = Minecraft.getMinecraft();
        IBakedModel missingModel = mc.getRenderItem().getItemModelMesher().getModelManager().getMissingModel();
        TextureAtlasSprite missing = mc.getTextureMapBlocks().getMissingSprite();
        Set<String> items = new TreeSet<>(), blocks = new TreeSet<>();

        for(Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
            if(!RefStrings.MODID.equals(item.getRegistryName().getNamespace())) continue;
            NonNullList<ItemStack> stacks = NonNullList.create();
            try { item.getSubItems(CreativeTabs.SEARCH, stacks); } catch(RuntimeException ignored) { }
            if(stacks.isEmpty()) stacks.add(new ItemStack(item));
            for(ItemStack stack : stacks) {
                try {
                    IBakedModel model = mc.getRenderItem().getItemModelWithOverrides(stack, null, null);
                    if(model == missingModel || (!model.isBuiltInRenderer() && usesSprite(model, null, missing)))
                        items.add(item.getRegistryName() + "@" + stack.getMetadata());
                } catch(RuntimeException failure) {
                    items.add(item.getRegistryName() + "@" + stack.getMetadata() + " (" + failure.getClass().getSimpleName() + ")");
                }
            }
        }

        for(Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            if(!RefStrings.MODID.equals(block.getRegistryName().getNamespace())) continue;
            for(IBlockState state : block.getBlockState().getValidStates()) {
                if(state.getRenderType() != EnumBlockRenderType.MODEL) continue;
                try {
                    IBakedModel model = mc.getBlockRendererDispatcher().getModelForState(state);
                    if(model == missingModel || usesSprite(model, state, missing)) { blocks.add(block.getRegistryName().toString()); break; }
                } catch(RuntimeException ignored) {
                    // Extended-state models need a live world; they are not inspectable here.
                }
            }
        }
        MainRegistry.logger.warn("Missing texture audit: {} item stacks: {}", items.size(), items);
        MainRegistry.logger.warn("Missing texture audit: {} blocks: {}", blocks.size(), blocks);
    }

    private static boolean usesSprite(IBakedModel model, IBlockState state, TextureAtlasSprite sprite) {
        for(EnumFacing side : EnumFacing.values()) for(BakedQuad quad : model.getQuads(state, side, 0L)) if(quad.getSprite() == sprite) return true;
        for(BakedQuad quad : model.getQuads(state, null, 0L)) if(quad.getSprite() == sprite) return true;
        return false;
    }
}
