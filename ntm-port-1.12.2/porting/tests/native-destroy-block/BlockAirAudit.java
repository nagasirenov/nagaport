import java.io.*;import java.util.*;import java.util.jar.*;import org.objectweb.asm.*;
/** Lists actual dependency isAir overrides; current NTM declarations are separately checked. */
public class BlockAirAudit {
 public static void main(String[] a)throws Exception{
  Set<String>seen=new HashSet<>();for(String path:a[0].split(File.pathSeparator)){File f=new File(path);if(!f.isFile()||!path.endsWith(".jar"))continue;
   try(JarFile jar=new JarFile(f)){Enumeration<JarEntry> es=jar.entries();while(es.hasMoreElements()){JarEntry e=es.nextElement();if(!e.getName().endsWith(".class")||e.getName().startsWith("META-INF/"))continue;try(InputStream in=jar.getInputStream(e)){ClassReader r=new ClassReader(in);String name=r.getClassName();if(!seen.add(name))continue;r.accept(new ClassVisitor(Opcodes.ASM5){public MethodVisitor visitMethod(int access,String n,String desc,String sig,String[] ex){if(n.equals("isAir")&&desc.equals("(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Z"))System.out.println(name+" extends "+r.getSuperName()+" declares "+n+desc);return null;}},ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);}}}
  }
 }
}
