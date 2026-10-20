package com.hbm.render.block;

import java.util.*;
import com.hbm.blocks.network.PneumaticModelState;
import com.hbm.render.block.nativeblock.PneumaticCuboidGeometry;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

/** Native atlas successor to the exposed tube ISBRH. No shared mutable block render state. */
public final class RenderPneumoTube {
    private RenderPneumoTube() { }
    /** Materials: body, insertion collar, ejection collar, air collar, straight body. */
    public static List<BakedQuad> bake(PneumaticModelState state, boolean inventory, TextureAtlasSprite[] materials) {
        if(materials.length!=5)throw new IllegalArgumentException("Five pneumatic materials required");
        List<BakedQuad> out=new ArrayList<>();
        int mask=state.tubes;
        if(inventory){
            AxisAlignedBB box=new AxisAlignedBB(.3125,.3125,0,.6875,.6875,1);
            for(EnumFacing face:EnumFacing.values()) out.add(PneumaticCuboidGeometry.face(materials[face.getAxis()==EnumFacing.Axis.Z?3:4],true,face,face==EnumFacing.UP?2:face==EnumFacing.DOWN?1:0,box.minX,box.minY,box.minZ,box.maxX,box.maxY,box.maxZ,0));
        }else if(!state.hasPorts() && (mask==48 || mask==3 || mask==12)){
            AxisAlignedBB box=mask==48?new AxisAlignedBB(0,.3125,.3125,1,.6875,.6875):mask==3?new AxisAlignedBB(.3125,.3125,0,.6875,.6875,1):new AxisAlignedBB(.3125,0,.3125,.6875,1,.6875);
            for(EnumFacing face:EnumFacing.values()) if((mask&PneumaticModelState.bit(face.getIndex()))==0){
                int rotation=mask==3?(face==EnumFacing.UP?2:face==EnumFacing.DOWN?1:0):mask==12?2:0;
                out.add(PneumaticCuboidGeometry.face(materials[4],false,face,rotation,box.minX,box.minY,box.minZ,box.maxX,box.maxY,box.maxZ,0));
            }
        }else{
            append(out,new AxisAlignedBB(.3125,.3125,.3125,.6875,.6875,.6875),materials[0],mask);
            for(EnumFacing face:EnumFacing.values()) if((mask&PneumaticModelState.bit(face.getIndex()))!=0)
                append(out,PneumaticModelState.arm(face,.3125,.6875,.6875,1),materials[0],axisMask(face));
        }
        if(!inventory){
            connector(out,state.insertion,materials[0],materials[1]);
            connector(out,state.ejection,materials[0],materials[2]);
            for(EnumFacing face:EnumFacing.values())if((state.air&PneumaticModelState.bit(face.getIndex()))!=0)connector(out,face.getIndex(),materials[0],materials[3]);
        }
        return Collections.unmodifiableList(out);
    }
    private static int axisMask(EnumFacing face){return PneumaticModelState.bit(face.getIndex())|PneumaticModelState.bit(face.getOpposite().getIndex());}
    private static void connector(List<BakedQuad> out,int direction,TextureAtlasSprite base,TextureAtlasSprite collar){
        if(direction==6)return;
        EnumFacing side=EnumFacing.byIndex(direction);
        append(out,PneumaticModelState.arm(side,.3125,.6875,.6875,.75),base,axisMask(side));
        append(out,PneumaticModelState.arm(side,.25,.75,.75,1),collar,0);
    }
    private static void append(List<BakedQuad> out,AxisAlignedBB box,TextureAtlasSprite sprite,int hidden){
        for(EnumFacing face:EnumFacing.values())if((hidden&PneumaticModelState.bit(face.getIndex()))==0)
            out.add(PneumaticCuboidGeometry.face(sprite,false,face,0,box.minX,box.minY,box.minZ,box.maxX,box.maxY,box.maxZ,0));
    }
}
