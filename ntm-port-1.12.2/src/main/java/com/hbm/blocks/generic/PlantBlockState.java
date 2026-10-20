package com.hbm.blocks.generic;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.*;
/** Render-only world context; never stored in metadata or serialized. */
public final class PlantBlockState {
 private PlantBlockState() { }
 public static final IUnlistedProperty<Context> CONTEXT=new IUnlistedProperty<Context>() {
  public String getName(){return "plant_context";} public boolean isValid(Context c){return c!=null;}
  public Class<Context> getType(){return Context.class;} public String valueToString(Context c){return c.pos.toString();}
 };
 public static class Context { public final IBlockAccess world;public final BlockPos pos;public Context(IBlockAccess w,BlockPos p){world=w;pos=p.toImmutable();} }
 public static BlockStateContainer create(Block block,IProperty<?>... properties){return new ExtendedBlockState(block,properties,new IUnlistedProperty<?>[]{CONTEXT});}
 public static IBlockState extend(IBlockState state,IBlockAccess world,BlockPos pos){return ((IExtendedBlockState)state).withProperty(CONTEXT,new Context(world,pos));}
}
