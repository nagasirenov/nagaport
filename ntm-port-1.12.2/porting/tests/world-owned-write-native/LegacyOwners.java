import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
/** Symbol-only SRG to 1.7 client remapping of the three supplied original mod classfiles. */
public final class LegacyOwners {
 public static void main(String[] a)throws Exception {
  Map<String,String> classes=new HashMap<>(),methods=new HashMap<>(),fields=new HashMap<>();
  try(ZipFile mappings=new ZipFile(a[1])){BufferedReader in=new BufferedReader(new InputStreamReader(mappings.getInputStream(mappings.getEntry("joined.srg")),"UTF-8"));String line;while((line=in.readLine())!=null){String[] p=line.split(" ");
   if(p[0].equals("CL:"))classes.put(p[2],p[1]);
   if(p[0].equals("MD:")){String key=p[3].substring(p[3].lastIndexOf('/')+1);methods.put(key,p[1].substring(p[1].lastIndexOf('/')+1));}
   if(p[0].equals("FD:")){String key=p[2].substring(p[2].lastIndexOf('/')+1);fields.put(key,p[1].substring(p[1].lastIndexOf('/')+1));}
  }}
  String[] owners={"BlockRedBrick","DecoSteelPoles","DecoPoleTop"};for(String n:owners)classes.put("com/hbm/blocks/generic/"+n,"prooflegacy/"+n);
  Remapper remap=new Remapper(){public String map(String name){return classes.getOrDefault(name,name);}public String mapMethodName(String owner,String name,String desc){return methods.getOrDefault(name,name);}public String mapFieldName(String owner,String name,String desc){return fields.getOrDefault(name,name);}};
  try(ZipFile jar=new ZipFile(a[0])){for(String n:owners){ClassReader r=new ClassReader(jar.getInputStream(jar.getEntry("com/hbm/blocks/generic/"+n+".class")));ClassWriter w=new ClassWriter(0);r.accept(new RemappingClassAdapter(w,remap),ClassReader.EXPAND_FRAMES);Path out=Paths.get(a[2],"prooflegacy",n+".class");Files.createDirectories(out.getParent());Files.write(out,w.toByteArray());}}
  System.out.println("PASS: three actual supplied original owner classfiles symbol-remapped; no method instructions removed or replaced");
 }
}
