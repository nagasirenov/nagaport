package com.hbm.render.block.nativeblock;
import java.util.*;
import com.hbm.blocks.BlockCoral;
import com.hbm.blocks.generic.PlantBlockState;
import com.hbm.config.ClientConfig;
import com.hbm.render.block.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;

public final class PlantBakedModel implements IBakedModel {
 private final String kind;private final TextureAtlasSprite[] sprites;private final int variant;
 public PlantBakedModel(String kind,TextureAtlasSprite... sprites){this(kind,0,sprites);}
 public PlantBakedModel(String kind,int variant,TextureAtlasSprite... sprites){this.kind=kind;this.variant=variant;this.sprites=sprites;}
 private static final EnumFacing[] ATTACH={EnumFacing.NORTH,EnumFacing.SOUTH,EnumFacing.WEST,EnumFacing.EAST,EnumFacing.UP,EnumFacing.DOWN};
 // Original Block.isBlockSolid queried material solidity, not Forge face support.
 public static EnumFacing attachment(IBlockAccess world,BlockPos pos){
  for(EnumFacing f:ATTACH){BlockPos p=pos.offset(f);if(world.getBlockState(p).getMaterial().isSolid())return f;}
  return EnumFacing.DOWN;
 }
 @Override public List<BakedQuad> getQuads(IBlockState state,EnumFacing side,long seed){
  if(side!=null||!(state instanceof IExtendedBlockState))return Collections.emptyList();
  PlantBlockState.Context c=((IExtendedBlockState)state).getValue(PlantBlockState.CONTEXT);if(c==null)return Collections.emptyList();
  IBlockAccess world=c.world;BlockPos pos=c.pos;List<BakedQuad> result=new ArrayList<>();
  if(kind.equals("reeds")){
   int depth=RenderReeds.depth(world,pos,ClientConfig.RENDER_REEDS.get());
   for(int i=0;i<depth;i++){TextureAtlasSprite sprite=sprites[i==0?0:i==depth-1?2:1];PlantGeometry.add(result,sprite,RenderReeds.vertices(sprite,i),.75F,world,state.getPackedLightmapCoords(world,pos.down(i)));}
  }else if(kind.equals("coral")){
   TextureAtlasSprite sprite=sprites[state.getValue(BlockCoral.META)%5];
   PlantGeometry.add(result,sprite,RenderCoral.vertices(sprite,0,0,0,1,attachment(world,pos)),1,world,state.getPackedLightmapCoords(world,pos));
  }else{
   Block below=world.getBlockState(pos.down()).getBlock();
   // Intentionally retain the original current-position solidity and air queries.
   TextureAtlasSprite base=sprites[world.isSideSolid(pos,EnumFacing.UP,false)?1:below==state.getBlock()?0:2];
   TextureAtlasSprite glow=sprites[below.isAir(world.getBlockState(pos.down()),world,pos)?4:3];
   PlantGeometry.add(result,base,RenderHangingVine.vertices(base),1,world,state.getPackedLightmapCoords(world,pos));
   PlantGeometry.add(result,glow,RenderHangingVine.vertices(glow),1,world,240);
  }
  return result;
 }
 @Override public boolean isAmbientOcclusion(){return false;}
 @Override public boolean isGui3d(){return false;}
 @Override public boolean isBuiltInRenderer(){return false;}
 @Override public TextureAtlasSprite getParticleTexture(){return sprites[variant];}
 @Override public ItemCameraTransforms getItemCameraTransforms(){return ItemCameraTransforms.DEFAULT;}
 @Override public ItemOverrideList getOverrides(){return ItemOverrideList.NONE;}
}
