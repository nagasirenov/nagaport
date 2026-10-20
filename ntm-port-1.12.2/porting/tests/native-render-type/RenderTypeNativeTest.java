import net.minecraft.block.*;
import net.minecraft.block.state.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.init.Bootstrap;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import java.net.*;
import java.io.*;
import java.lang.reflect.*;
public class RenderTypeNativeTest {
 static Object allocate(Class<?> c)throws Exception { Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);return ((sun.misc.Unsafe)f.get(null)).allocateInstance(c); }
 public static void main(String[] args)throws Exception {
  Bootstrap.register();
  net.minecraftforge.fml.common.FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
  BlockRendererDispatcher dispatcher=(BlockRendererDispatcher)allocate(BlockRendererDispatcher.class);
  int checked=0;
  for(Block b:CallbackBank.blocks()) for(IBlockState s:b.getBlockState().getValidStates()) {
   if(s.getRenderType()!=EnumBlockRenderType.INVISIBLE)throw new AssertionError("native state dispatch");
   if(dispatcher.renderBlock(s,BlockPos.ORIGIN,null,null))throw new AssertionError("actual native render dispatcher");
   checked++;
  }
  try(URLClassLoader loader=new URLClassLoader(new URL[]{new File(args[0]).toURI().toURL(),new File(args[1]).toURI().toURL()},RenderTypeNativeTest.class.getClassLoader())) {
   Class<?> old=Class.forName("oracle.OldRenderTypeFixture",true,loader), block=Class.forName("aji",true,loader),render=Class.forName("blm",true,loader);
   Object b=old.newInstance(), renderer=allocate(render);
   if(!render.getMethod("b",block,int.class,int.class,int.class).invoke(renderer,b,0,0,0).equals(false))throw new AssertionError("actual original render dispatcher");
  }
  System.out.println("PASS: "+checked+" genuine native state/renderer dispatches; actual original RenderBlocks negative dispatch. Exact callback slices only; renderer constructor, TESR bodies, item rendering and pixels not executed.");
 }
}
