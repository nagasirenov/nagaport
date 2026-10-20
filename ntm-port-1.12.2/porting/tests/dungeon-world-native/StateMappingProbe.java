import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import java.io.*;
import java.net.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
/** Uses actual registered 1.7.10 block IDs and actual native 1.12.2 states. */
public final class StateMappingProbe {
 public static void main(String[] args)throws Exception {
  Bootstrap.register();
  try(URLClassLoader legacy=new URLClassLoader(new URL[]{new File(args[0]).toURI().toURL()},StateMappingProbe.class.getClassLoader())) {
   Class<?> oldBlock=Class.forName("aji",true,legacy);oldBlock.getMethod("p").invoke(null);
   Method byName=oldBlock.getMethod("b",String.class),id=oldBlock.getMethod("b",oldBlock);
   for(String row:Files.readAllLines(Paths.get(args[1]))) {
    String[] a=row.split("\t");String name=a[0],field=a[1];int meta=Integer.parseInt(a[2]);
    Block block=(Block)Blocks.class.getField(field).get(null);Object old=byName.invoke(null,name);
    int oldId=(Integer)id.invoke(null,old),newId=Block.getIdFromBlock(block);
    if(oldId!=newId||(!name.equals("air")&&oldId==0))throw new AssertionError("ID mismatch "+name+" "+oldId+" "+newId);
    try { IBlockState state=block.getStateFromMeta(meta);int roundtrip=block.getMetaFromState(state);System.out.println("MAP\t"+name+"\t"+field+"\t"+meta+"\t"+oldId+"\t"+newId+"\t"+roundtrip+"\t"+state); }
    catch(IllegalArgumentException ex){System.out.println("REJECT\t"+name+"\t"+field+"\t"+meta+"\t"+ex.getClass().getSimpleName());}
   }
  }
 }
}
