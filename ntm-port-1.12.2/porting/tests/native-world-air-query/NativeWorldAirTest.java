import java.lang.reflect.Field;import java.util.*;
import net.minecraft.world.*;import net.minecraft.world.chunk.IChunkProvider;import net.minecraft.block.*;import net.minecraft.block.material.Material;import net.minecraft.block.state.*;import net.minecraft.init.*;import net.minecraft.util.math.BlockPos;
public class NativeWorldAirTest {
 static int checks; static String trace=""; static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static class W extends World {
  IBlockState first,second; int reads; BlockPos position;
  W(){super(null,null,null,null,false);}
  protected IChunkProvider createChunkProvider(){throw new AssertionError("unused fixture boundary");}
  protected boolean isChunkLoaded(int x,int z,boolean a){throw new AssertionError("unused fixture boundary");}
  public IBlockState getBlockState(BlockPos p){trace+="S";reads++;position=p;return reads==1?first:second;}
 }
 static class Spy extends Block {
  IBlockState state;IBlockAccess access;BlockPos pos;int calls;
  Spy(){super(Material.ROCK);}
  public boolean isAir(IBlockState s,IBlockAccess w,BlockPos p){trace+="Q";state=s;access=w;pos=p;calls++;return s.getMaterial()==Material.AIR;}
 }
 static W world(W w){trace+="W";return w;}static int arg(char c,int v){trace+=c;return v;}static int fail(){trace+="Y";throw new IllegalStateException();}
 static void reset(W w,IBlockState a,IBlockState b){w.first=a;w.second=b;w.reads=0;trace="";}
 public static void main(String[]args)throws Exception{
  Bootstrap.register();Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);sun.misc.Unsafe unsafe=(sun.misc.Unsafe)uf.get(null);W w=(W)unsafe.allocateInstance(W.class);
  int[][] points={{0,0,0},{-1,-1,-1},{30000001,256,-30000001},{Integer.MIN_VALUE,Integer.MAX_VALUE,Integer.MIN_VALUE},{Integer.MAX_VALUE,Integer.MIN_VALUE,Integer.MAX_VALUE}};
  int states=0;for(Block b:Block.REGISTRY)for(IBlockState state:b.getBlockState().getValidStates()){
   for(int[]p:points){reset(w,state,state);boolean actual=w.isAirBlock(new BlockPos(p[0],p[1],p[2]));check(actual==(state.getMaterial()==Material.AIR),"registry state result");check(w.reads==2,"actual native two-read contract");check(w.position.getX()==p[0]&&w.position.getY()==p[1]&&w.position.getZ()==p[2],"raw integer coordinates");}states++;
  }
  Spy spy=new Spy();reset(w,spy.getDefaultState(),Blocks.AIR.getDefaultState());check(w.isAirBlock(new BlockPos(1,2,3)),"native first block / second state dispatch");check(spy.state==w.second&&spy.access==w&&spy.pos==w.position&&spy.calls==1,"exact native hook arguments");check(trace.equals("SSQ"),"two reads then virtual hook");
  reset(w,Blocks.AIR.getDefaultState(),Blocks.AIR.getDefaultState());check(world(w).isAirBlock(new BlockPos(arg('X',1),arg('Y',2),arg('Z',3))),"ordered success");check(trace.equals("WXYZSS"),"receiver then coordinates then world");
  reset(w,Blocks.AIR.getDefaultState(),Blocks.AIR.getDefaultState());try{world(null).isAirBlock(new BlockPos(arg('X',1),arg('Y',2),arg('Z',3)));throw new AssertionError("null not rejected");}catch(NullPointerException expected){check(trace.equals("WXYZ"),"null receiver arguments evaluated");}
  reset(w,Blocks.AIR.getDefaultState(),Blocks.AIR.getDefaultState());try{world(w).isAirBlock(new BlockPos(arg('X',1),fail(),arg('Z',3)));throw new AssertionError("argument exception lost");}catch(IllegalStateException expected){check(trace.equals("WXY")&&w.reads==0,"argument failure stops query");}
  reset(w,Blocks.AIR.getDefaultState(),Blocks.AIR.getDefaultState());boolean x=false&&world(w).isAirBlock(new BlockPos(arg('X',1),arg('Y',2),arg('Z',3)));check(!x&&trace.isEmpty()&&w.reads==0,"short circuit");
  System.out.println("PASS "+checks+" native checks across "+states+" actual registry states; genuine World.isAirBlock dispatch, state identity, two-read platform contract, raw integer coordinates and evaluation boundaries. Fixture skips unrelated World constructor; no real chunk lifecycle claim.");
 }
}
