package com.hbm.blocks;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.block.Block;

/** Texture bindings for the audited plain BlockOutgas/Smolder/StepEffect cubes only.
 * No block inheritance, gameplay callbacks or legacy rendering APIs are replaced.
 * Strings remain unparsed until client model loading, as in the original setter.
 */
public final class PlainCubeTextures {
    private static final Map<Block, String> TEXTURES = new IdentityHashMap<>();
    private PlainCubeTextures() { }

    public static Block bind(Block block, String texture) {
        if(block == null) throw new NullPointerException();
        String owner = block.getClass().getName();
        if(!owner.equals("com.hbm.blocks.generic.BlockOutgas") &&
                !owner.equals("com.hbm.blocks.generic.BlockSmolder") &&
                !owner.equals("com.hbm.blocks.generic.BlockStepEffect"))
            throw new IllegalArgumentException("Unaudited plain cube owner " + owner);
        TEXTURES.put(block, texture);
        return block;
    }

    public static Map<Block, String> bindings() {
        return Collections.unmodifiableMap(TEXTURES);
    }
}
