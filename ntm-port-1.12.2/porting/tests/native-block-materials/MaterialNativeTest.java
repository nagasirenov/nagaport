import java.net.*;import java.nio.file.*;import java.lang.reflect.*;import java.util.*;
import net.minecraft.block.material.Material;
public class MaterialNativeTest {
 public static void main(String[] a)throws Exception{
  URLClassLoader loader=new URLClassLoader(new URL[]{Paths.get(a[0]).toUri().toURL()},null);
  Class<?> old=loader.loadClass("awt");int checks=0;
  String[] om={"d","a","b","c","h","j","k","l"};
  String[] nm={"isLiquid","isSolid","blocksLight","blocksMovement","getCanBurn","isReplaceable","isOpaque","isToolNotRequired"};
  List<Object> os=new ArrayList<>(),ns=new ArrayList<>();
  for(String line:Files.readAllLines(Paths.get(a[1]))){
   String[] r=line.split("\t");for(Field f:new Field[]{old.getField(r[3]),Material.class.getField(r[2])}){if(!Modifier.isStatic(f.getModifiers())||!Modifier.isFinal(f.getModifiers()))throw new AssertionError(r[2]+" not static final");checks++;}Object x=old.getField(r[3]).get(null);Material y=(Material)Material.class.getField(r[2]).get(null);os.add(x);ns.add(y);
   for(int i=0;i<om.length;i++){Object v=old.getMethod(om[i]).invoke(x),w=Material.class.getMethod(nm[i]).invoke(y);if(!v.equals(w))throw new AssertionError(r[2]+" "+nm[i]+" "+v+" != "+w);checks++;}
   int mobility=(Integer)old.getMethod("m").invoke(x);if(mobility!=y.getPushReaction().ordinal())throw new AssertionError(r[2]+" push "+mobility+" != "+y.getPushReaction());checks++;
   Object c=old.getMethod("r").invoke(x);Class<?> mc=loader.loadClass("awv");
   int rgb=mc.getField("L").getInt(c),index=mc.getField("M").getInt(c);
   if(rgb!=y.getMaterialMapColor().colorValue||index!=y.getMaterialMapColor().colorIndex)throw new AssertionError("MAP_DIFFERENCE "+r[2]+" "+rgb+"/"+index+" != "+y.getMaterialMapColor().colorValue+"/"+y.getMaterialMapColor().colorIndex);checks+=2;
  }
  for(int i=0;i<os.size();i++)for(int j=0;j<os.size();j++){if((os.get(i)==os.get(j))!=(ns.get(i)==ns.get(j)))throw new AssertionError("identity");checks++;}
  System.out.println("PASS actual legacy/native material predicates, push, map color, singleton identity: "+checks);
 }
}
