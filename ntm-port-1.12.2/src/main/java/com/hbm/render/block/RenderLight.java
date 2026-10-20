package com.hbm.render.block;

import java.util.List;
import com.hbm.blocks.BlockEnums.LightType;
import com.hbm.lib.ForgeDirection;
import com.hbm.render.block.nativeblock.SpotlightGeometry;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Original spotlight part/connection/rotation rules expressed as native immutable geometry. */
public final class RenderLight {
    private RenderLight() { }
    public static List<BakedQuad> bake(ObjMesh mesh, LightType type, boolean modular, int metadata,
            int connections, boolean inventory, TextureAtlasSprite sprite) {
        ForgeDirection dir = ForgeDirection.getOrientation(metadata >> 1);
        float yaw = dir == ForgeDirection.NORTH ? (float)Math.PI * .5F
                : dir == ForgeDirection.SOUTH ? (float)Math.PI * -.5F
                : dir == ForgeDirection.WEST ? (float)Math.PI : 0;
        float pitch = dir == ForgeDirection.UP ? (float)Math.PI * -.5F
                : dir == ForgeDirection.DOWN ? (float)Math.PI * .5F : 0;
        float roll = 0;
        int count = 0;
        if(modular && !inventory) {
            ForgeDirection connection = null;
            for(ForgeDirection available : ForgeDirection.VALID_DIRECTIONS) {
                if(available == dir || available == dir.getOpposite()) continue;
                if((connections & available.flag) != 0) { count++; connection = available; break; }
            }
            if(connection != null) {
                if((connections & connection.getOpposite().flag) != 0) count++;
                roll = rotation(connection, dir);
            }
        }
        String part = modular ? count == 0 ? "FluoroSingle" : count == 1 ? "FluoroCap" : "FluoroMid"
                : type == LightType.HALOGEN ? "FloodLamp" : "CageLamp";
        return SpotlightGeometry.bake(mesh, part, sprite, inventory, yaw, pitch, roll,
                .5F - dir.offsetX * .5F, .5F - dir.offsetY * .5F, .5F - dir.offsetZ * .5F);
    }
    private static float rotation(ForgeDirection dir, ForgeDirection axis) {
        float flipX = axis == ForgeDirection.DOWN || axis == ForgeDirection.NORTH || axis == ForgeDirection.WEST ? -.5F : .5F;
        float addX = axis == ForgeDirection.NORTH || axis == ForgeDirection.SOUTH ? -.5F : 0;
        boolean flipNS = axis == ForgeDirection.WEST;
        switch(dir) {
            case NORTH: return flipNS ? (float)Math.PI : 0;
            case SOUTH: return !flipNS ? (float)Math.PI : 0;
            case EAST: return (float)Math.PI * (flipX + addX);
            case WEST: return (float)Math.PI * (-flipX + addX);
            case UP: return (float)Math.PI * -.5F;
            case DOWN: return (float)Math.PI * .5F;
            default: return 0;
        }
    }
}
