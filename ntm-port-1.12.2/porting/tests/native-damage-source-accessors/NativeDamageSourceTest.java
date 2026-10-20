import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import sun.misc.Unsafe;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import com.hbm.items.weapon.sedna.DamageSourceSednaNoAttacker;
import com.hbm.items.weapon.sedna.DamageSourceSednaWithAttacker;
/** Genuine MC/Forge classes and both complete production Sedna classes only. */
public final class NativeDamageSourceTest {
 static int checks;static String trace="";
 static void ok(boolean b,String label){checks++;if(!b)throw new AssertionError(label);}
 static class VirtualSource extends DamageSource {Entity entity;VirtualSource(Entity e){super("virtual");entity=e;}@Override public Entity getTrueSource(){trace+="T";return entity;}}
 static DamageSource receiver(DamageSource source){trace+="R";return source;}
 static DamageSource failure(){trace+="X";throw new IllegalStateException();}
 static void sources(DamageSource source,Entity projectile,Entity shooter,String label){ok(source.getTrueSource()==shooter,label+" true identity");ok(source.getImmediateSource()==projectile,label+" immediate identity");}
 static void inspect(ClassReader r,Map<String,String> parents,Map<String,Set<String>> methods){
  parents.put(r.getClassName(),r.getSuperName());
  r.accept(new ClassVisitor(Opcodes.ASM5){public MethodVisitor visitMethod(int access,String name,String desc,String signature,String[] exceptions){if(Arrays.asList("getEntity","getSourceOfDamage","getTrueSource","getImmediateSource").contains(name)&&desc.equals("()Lnet/minecraft/entity/Entity;"))methods.computeIfAbsent(r.getClassName(),k->new TreeSet<>()).add(name);return null;}},ClassReader.SKIP_CODE);
 }
 public static void main(String[] args)throws Exception {
  net.minecraft.init.Bootstrap.register();Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);Unsafe u=(Unsafe)f.get(null);
  Entity projectile=(Entity)u.allocateInstance(EntityItem.class),shooter=(Entity)u.allocateInstance(EntityItem.class),replacement=(Entity)u.allocateInstance(EntityItem.class);
  sources(new DamageSource("probe"),null,null,"base");
  for(Entity attacker:new Entity[]{shooter,null})sources(new EntityDamageSource("probe",attacker),attacker,attacker,"direct");
  for(Entity p:new Entity[]{projectile,null})for(Entity s:new Entity[]{shooter,null})sources(new EntityDamageSourceIndirect("probe",p,s),p,s,"indirect including null without fallback");
  VirtualSource virtual=new VirtualSource(shooter);trace="";ok(receiver(virtual).getImmediateSource()==shooter&&trace.equals("RT"),"base immediate invokes custom true override with exactly one receiver evaluation");
  trace="";try{receiver(null).getTrueSource();throw new AssertionError();}catch(NullPointerException expected){ok(trace.equals("R"),"null receiver timing");}
  trace="";try{failure().getTrueSource();throw new AssertionError();}catch(IllegalStateException expected){ok(trace.equals("X"),"receiver exception timing");}
  sources(new DamageSourceSednaNoAttacker("PROBE"),null,null,"complete production no-attacker Sedna");
  for(Entity p:new Entity[]{projectile,null})for(Entity s:new Entity[]{shooter,null}){
   DamageSourceSednaWithAttacker sedna=new DamageSourceSednaWithAttacker("PROBE",p,s);DamageSource polymorphic=sedna;
   sources(polymorphic,p,s,"complete production Sedna via DamageSource");
   ok(sedna.getDamageType().equals("probe"),"existing Locale.US type normalization retained");
   sedna.projectile=replacement;sedna.shooter=replacement;sources(polymorphic,replacement,replacement,"mutable Sedna fields remain dynamically visible");
   ok(sedna.getClass().getDeclaredMethod("getTrueSource").getReturnType()==Entity.class,"actual Sedna true override present");
   ok(sedna.getClass().getDeclaredMethod("getImmediateSource").getReturnType()==Entity.class,"actual Sedna immediate override present");
  }
  for(String old:new String[]{"getEntity","getSourceOfDamage"})try{DamageSourceSednaWithAttacker.class.getDeclaredMethod(old);throw new AssertionError("Legacy non-overriding method remains");}catch(NoSuchMethodException expected){checks++;}
  Map<String,String> parents=new TreeMap<>();Map<String,Set<String>> methods=new TreeMap<>();int jars=0,directoryClasses=0;
  for(String path:new String(Files.readAllBytes(Paths.get(args[0])),"UTF-8").trim().split(java.io.File.pathSeparator)){
   if(Files.isDirectory(Paths.get(path))){
    try(java.util.stream.Stream<Path> entries=Files.walk(Paths.get(path))){for(Path entry:(Iterable<Path>)entries.filter(e->e.toString().endsWith(".class"))::iterator){inspect(new ClassReader(Files.readAllBytes(entry)),parents,methods);directoryClasses++;}}
    continue;
   }
   if(!path.endsWith(".jar")||!Files.isRegularFile(Paths.get(path)))continue;jars++;
   try(JarFile jar=new JarFile(path)){Enumeration<JarEntry> entries=jar.entries();while(entries.hasMoreElements()){
    JarEntry e=entries.nextElement();if(!e.getName().endsWith(".class"))continue;inspect(new ClassReader(jar.getInputStream(e)),parents,methods);
   }}
  }
  List<String> declarations=new ArrayList<>();int descendants=0;
  for(String candidate:parents.keySet()){
   String c=candidate;Set<String> seen=new HashSet<>();while(c!=null&&seen.add(c)){if(c.equals("net/minecraft/util/DamageSource")){descendants++;if(methods.containsKey(candidate))declarations.add(candidate+"="+methods.get(candidate));break;}c=parents.get(c);}
  }
  List<String> expected=Arrays.asList("net/minecraft/util/DamageSource=[getImmediateSource, getTrueSource]","net/minecraft/util/EntityDamageSource=[getTrueSource]","net/minecraft/util/EntityDamageSourceIndirect=[getImmediateSource, getTrueSource]");
  ok(declarations.equals(expected),"reviewed resolved binary accessor closure: "+declarations);
  System.out.println("BINARY_DESCENDANTS "+descendants+" across "+jars+" native dependency jars and "+directoryClasses+" directory classes; accessor declarations: "+declarations);
  System.out.println("PASS "+checks+" native runtime assertions, complete production Sedna classes; no full caller/game lifecycle claim");System.exit(0);
 }
}
