package com.hbm.render.block.nativeblock;

import java.util.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.model.pipeline.*;

/** Unculled crosses with the original non-directional color and exact packed light. */
public final class PlantGeometry {
 private PlantGeometry() { }
 public static void add(List<BakedQuad> output,TextureAtlasSprite sprite,List<double[]> vertices,float shade,IBlockAccess world,int light) {
  for(int q=0;q<vertices.size();q+=4) {
   VertexFormat format=DefaultVertexFormats.BLOCK;
   UnpackedBakedQuad.Builder b=new UnpackedBakedQuad.Builder(format);
   b.setTexture(sprite);b.setQuadTint(-1);b.setQuadOrientation(EnumFacing.WEST);b.setApplyDiffuseLighting(false);b.setContractUVs(false);
   for(int v=0;v<4;v++)for(int e=0;e<format.getElementCount();e++){
    double[] p=vertices.get(q+v);VertexFormatElement element=format.getElement(e);
    switch(element.getUsage()){
     case POSITION:b.put(e,(float)p[0],(float)p[1],(float)p[2],1);break;
     case COLOR:b.put(e,shade,shade,shade,1);break;
     case UV:if(element.getIndex()==0)b.put(e,(float)p[3],(float)p[4],0,1);else b.put(e,(light&65535)/65535F,(light>>>16)/65535F,0,1);break;
     default:b.put(e);
    }
   }
   output.add(new ExactLightQuad(b.build(),world,light));
  }
 }
 /** The Forge lighter normally replaces emitted light with max(world,vertex).
  * A scoped world view makes every sample the exact original sample, including
  * the vine's 240 (block=15, sky=0) glow pass and darker submerged reed levels.
  * Restore the lighter's original world even if a downstream consumer throws. */
 private static final class ExactLightQuad extends BakedQuad {
  final IBlockAccess world;final int light;
  ExactLightQuad(BakedQuad source,IBlockAccess world,int light){super(source.getVertexData(),-1,EnumFacing.WEST,source.getSprite(),false,source.getFormat());this.world=world;this.light=light;}
  @Override public void pipe(IVertexConsumer consumer) {
   VertexLighterFlat lighter=consumer instanceof VertexLighterFlat?(VertexLighterFlat)consumer:null;
   if(lighter!=null){lighter.setWorld(new LightWorld(world,light));lighter.updateBlockInfo();}
   try { new BlockPositionLightQuad(this).pipe(consumer); }
   finally {if(lighter!=null){lighter.setWorld(world);lighter.updateBlockInfo();}}
  }
 }
 private static final class LightWorld implements IBlockAccess {
  final IBlockAccess world;final int light;LightWorld(IBlockAccess world,int light){this.world=world;this.light=light;}
  public TileEntity getTileEntity(BlockPos p){return world.getTileEntity(p);}
  public int getCombinedLight(BlockPos p,int minimum){return light;}
  public IBlockState getBlockState(BlockPos p){return world.getBlockState(p);}
  public boolean isAirBlock(BlockPos p){return world.isAirBlock(p);}
  public Biome getBiome(BlockPos p){return world.getBiome(p);}
  public int getStrongPower(BlockPos p,EnumFacing f){return world.getStrongPower(p,f);}
  public WorldType getWorldType(){return world.getWorldType();}
  public boolean isSideSolid(BlockPos p,EnumFacing f,boolean fallback){return world.isSideSolid(p,f,fallback);}
 }
}
