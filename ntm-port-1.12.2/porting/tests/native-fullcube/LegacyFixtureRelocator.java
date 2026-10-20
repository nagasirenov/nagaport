import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
/** Move only our test fixtures to a named package, avoiding signed original default-package collisions. */
public final class LegacyFixtureRelocator {
 public static void main(String[] args)throws Exception{
  Path root=Paths.get(args[0]);Files.createDirectories(root.resolve("oracle"));
  for(String name:new String[]{"OldFullCubeFixture","OldFullCubeFixture$SideEffect"}){
   ClassReader reader=new ClassReader(Files.readAllBytes(root.resolve(name+".class")));ClassWriter writer=new ClassWriter(0);
   reader.accept(new ClassRemapper(writer,new Remapper(){@Override public String map(String n){return n.startsWith("OldFullCubeFixture")?"oracle/"+n:n;}}),0);
   Files.write(root.resolve("oracle/"+name+".class"),writer.toByteArray());
  }
 }
}
