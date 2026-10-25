package com.hbm.blocks;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.block.Block;

/** Texture bindings for the audited plain Outgas/Smolder/StepEffect, Hazard/HotHazard and Depth/DepthOre cubes, plus the explicitly audited remainder only.
 * No block inheritance, gameplay callbacks or legacy rendering APIs are replaced.
 * Strings remain unparsed until client model loading, as in the original setter.
 */
public final class PlainCubeTextures {
    private static final Map<Block, String> TEXTURES = new IdentityHashMap<>();
    private PlainCubeTextures() { }

    public static <T extends Block> T bind(T block, String texture) {
        if(block == null) throw new NullPointerException();
        String owner = block.getClass().getName();
        if(!owner.equals("com.hbm.blocks.generic.BlockOutgas") &&
                !owner.equals("com.hbm.blocks.generic.BlockSmolder") &&
                !owner.equals("com.hbm.blocks.generic.BlockStepEffect") &&
                !owner.equals("com.hbm.blocks.generic.BlockHazard") &&
                !owner.equals("com.hbm.blocks.generic.BlockHotHazard") &&
                !owner.equals("com.hbm.blocks.generic.BlockDepth") &&
                !owner.equals("com.hbm.blocks.generic.BlockDepthOre") &&
                !owner.equals("net.minecraft.block.BlockFalling") &&
                !owner.equals("com.hbm.blocks.generic.BlockFallingLegacy") &&
                !owner.equals("com.hbm.blocks.generic.BlockAshes") &&
                !owner.equals("com.hbm.blocks.generic.TritiumLamp") &&
                !owner.equals("com.hbm.blocks.generic.BlockDirt") &&
                !owner.equals("com.hbm.blocks.generic.BlockNTMDirt") &&
                !owner.equals("com.hbm.blocks.generic.BlockJungleCrate") &&
                !owner.equals("com.hbm.blocks.generic.BlockHazardFalling") &&
                !owner.equals("com.hbm.blocks.generic.BlockMeteoriteTreasure") &&
                !owner.equals("com.hbm.blocks.generic.BlockNetherCoal") &&
                !owner.equals("com.hbm.blocks.generic.BlockNoDrop") &&
                !owner.equals("com.hbm.blocks.generic.BlockNuclearWaste") &&
                !owner.equals("com.hbm.blocks.generic.BlockRadResistant") &&
                !owner.equals("com.hbm.blocks.generic.BlockSpeedy") &&
                !owner.equals("com.hbm.blocks.generic.FragileBrick"))
            throw new IllegalArgumentException("Unaudited plain cube owner " + owner);
        TEXTURES.put(block, texture);
        return block;
    }

    public static Map<Block, String> bindings() {
        return Collections.unmodifiableMap(TEXTURES);
    }
}
