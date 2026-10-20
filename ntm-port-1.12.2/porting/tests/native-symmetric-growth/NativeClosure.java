import java.util.*;import java.util.jar.*;import java.nio.file.*;import org.objectweb.asm.*;
/** Read actual dependency class hierarchies without loading/initializing them. */
public class NativeClosure {
 public static void main(String[] args)throws Exception{
  Map<String,String> parents=new HashMap<>();Set<String> declarations=new TreeSet<>();int jars=0,classes=0;
  for(String path:new String(Files.readAllBytes(Paths.get(args[0]))).trim().split(java.io.File.pathSeparator)){
   if(!path.endsWith(".jar")||!Files.isRegularFile(Paths.get(path)))continue;jars++;
   try(JarFile jar=new JarFile(path)){Enumeration<JarEntry> es=jar.entries();while(es.hasMoreElements()){
    JarEntry e=es.nextElement();if(!e.getName().endsWith(".class"))continue;classes++;
    ClassReader cr=new ClassReader(jar.getInputStream(e));parents.putIfAbsent(cr.getClassName(),cr.getSuperName());
    cr.accept(new ClassVisitor(Opcodes.ASM5){public MethodVisitor visitMethod(int a,String n,String d,String s,String[] x){if((n.equals("expand")||n.equals("grow"))&&d.equals("(DDD)Lnet/minecraft/util/math/AxisAlignedBB;"))declarations.add(cr.getClassName());return null;}},ClassReader.SKIP_CODE);
   }}
  }
  int descendants=0;
  for(String d:parents.keySet()){String c=d;Set<String> seen=new HashSet<>();while(c!=null&&seen.add(c)){if(c.equals("net/minecraft/util/math/AxisAlignedBB")){descendants++;if(!d.equals(c)&&declarations.contains(d))throw new AssertionError("foreign AABB override "+d);break;}c=parents.get(c);}}
  System.out.println("PASS "+jars+" actual dependency jars / "+classes+" classes / "+descendants+" AABB hierarchy members: no subclass expand/grow overrides; unknown external mods not certified");
 }
}
