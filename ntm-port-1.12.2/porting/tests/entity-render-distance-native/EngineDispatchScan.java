import java.io.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
public class EngineDispatchScan {
 static class Type {String parent;Set<String> fields=new HashSet<>();Set<String> methods=new HashSet<>();}
 static Map<String,Type> types=new HashMap<>();
 static String resolve(String owner,String name,String desc,boolean field){
  Set<String> seen=new HashSet<>();
  while(owner!=null&&seen.add(owner)) {
   Type t=types.get(owner);if(t==null)return owner;
   if((field?t.fields:t.methods).contains(name+desc))return owner;owner=t.parent;
  }
  return null;
 }
 public static void main(String[] args)throws Exception {
  String kind=args[0],target=kind.equals("legacy")?"sa":"net/minecraft/entity/Entity";
  String field=kind.equals("legacy")?"j":"renderDistanceWeight";
  String method=kind.equals("legacy")?"a":"isInRangeToRenderDist";
  String method3d=kind.equals("legacy")?"h":"isInRangeToRender3d";
  try(JarFile jar=new JarFile(args[1])) {
   List<String> entries=new ArrayList<>();Enumeration<JarEntry> it=jar.entries();
   while(it.hasMoreElements()){String n=it.nextElement().getName();if(n.endsWith(".class"))entries.add(n);}Collections.sort(entries);
   for(String name:entries)try(InputStream in=jar.getInputStream(jar.getJarEntry(name))){new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM5){Type t;
    public void visit(int v,int a,String n,String sig,String sup,String[] interfaces){t=new Type();t.parent=sup;types.put(n,t);}
    public FieldVisitor visitField(int a,String n,String d,String s,Object val){t.fields.add(n+d);return null;}
    public MethodVisitor visitMethod(int a,String n,String d,String s,String[] ex){t.methods.add(n+d);return null;}
   },ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);}
   List<String> rows=new ArrayList<>();
   for(String name:entries)try(InputStream in=jar.getInputStream(jar.getJarEntry(name))){new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM5){String owner;
    public void visit(int v,int a,String n,String sig,String sup,String[] interfaces){owner=n;}
    public MethodVisitor visitMethod(int a,String n,String d,String s,String[] ex){final String caller=owner+"."+n+d;return new MethodVisitor(Opcodes.ASM5){
     public void visitFieldInsn(int op,String o,String n,String d){if(n.equals(field)&&d.equals("D")&&target.equals(resolve(o,n,d,true)))rows.add("FIELD\t"+caller+"\t"+op+"\t"+o+"."+n+d);}
     public void visitMethodInsn(int op,String o,String n,String d,boolean iface){if(n.equals(method)&&d.equals("(D)Z")&&target.equals(resolve(o,n,d,false)))rows.add("RANGE_CALL\t"+caller+"\t"+op+"\t"+o+"."+n+d);if(n.equals(method3d)&&d.equals("(DDD)Z")&&target.equals(resolve(o,n,d,false)))rows.add("3D_CALL\t"+caller+"\t"+op+"\t"+o+"."+n+d);}
    };}
   },ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);}
   Collections.sort(rows);System.out.println("CLASSES\t"+entries.size());for(String row:rows)System.out.println(row);
  }
 }
}
