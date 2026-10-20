package com.hbm.render.block;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
/** Vanilla 1.7 drawCrossedSquares ordering, unlike the deliberately fixed vine/coral back UVs. */
public final class RenderReeds {
 private RenderReeds() { }
 public static int depth(IBlockAccess world,BlockPos pos,boolean enabled) {
  if(!enabled)return 1;int depth=0;
  for(int y=pos.getY()-1;y>0;y--){Block block=world.getBlockState(new BlockPos(pos.getX(),y,pos.getZ())).getBlock();depth=pos.getY()-y;if(block!=Blocks.WATER&&block!=Blocks.FLOWING_WATER)break;}
  return depth;
 }
 public static List<double[]> vertices(TextureAtlasSprite s,int depth) {
  double a=.5-.45,b=.5+.45,y=-depth,u=s.getMinU(),v=s.getMinV(),U=s.getMaxU(),V=s.getMaxV();
  return Arrays.asList(new double[][]{
   {a,y+1,a,u,v},{a,y,a,u,V},{b,y,b,U,V},{b,y+1,b,U,v},
   {b,y+1,b,u,v},{b,y,b,u,V},{a,y,a,U,V},{a,y+1,a,U,v},
   {a,y+1,b,u,v},{a,y,b,u,V},{b,y,a,U,V},{b,y+1,a,U,v},
   {b,y+1,a,u,v},{b,y,a,u,V},{a,y,b,U,V},{a,y+1,b,U,v}});
 }
}
