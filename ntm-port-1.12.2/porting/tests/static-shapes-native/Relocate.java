import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
/** Relocate unsigned fixture classes only; original signed engine JAR is untouched. */
public class Relocate {
 public static void main(String[] args)throws Exception{
  Path root=Paths.get(args[0]);Files.createDirectories(root.resolve("oracle"));
  for(int i=0;i<4;i++){String n="OldShape"+i;ClassReader r=new ClassReader(Files.readAllBytes(root.resolve(n+".class")));ClassWriter w=new ClassWriter(0);r.accept(new ClassRemapper(w,new Remapper(){public String map(String x){return x.startsWith("OldShape")?"oracle/"+x:x;}}),0);Files.write(root.resolve("oracle/"+n+".class"),w.toByteArray());}
 }
}
