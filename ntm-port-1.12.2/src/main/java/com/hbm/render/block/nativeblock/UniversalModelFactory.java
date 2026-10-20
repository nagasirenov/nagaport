package com.hbm.render.block.nativeblock;
import com.hbm.render.block.RenderISBRHUniversal;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
public final class UniversalModelFactory {
    private UniversalModelFactory() { }
    public static RenderISBRHUniversal bake(Block block,int metadata,boolean inventory,TextureAtlasSprite sprite) {
        return new RenderISBRHUniversal(block,metadata,inventory,sprite,ThermalModelTransforms.BLOCK);
    }
}
