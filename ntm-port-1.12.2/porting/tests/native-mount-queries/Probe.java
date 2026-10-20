import java.lang.reflect.*;import java.util.*;import java.util.jar.*;import org.objectweb.asm.*;import net.minecraft.entity.Entity;import net.minecraft.entity.item.EntityItem;import net.minecraft.world.World;import net.minecraft.world.WorldServer;import sun.misc.Unsafe;
public class Probe {
 static Unsafe unsafe;static int checks;static Field riding,passengers;static World world;
 static void ok(boolean x,String why){checks++;if(!x)throw new AssertionError(why);}
 static Entity make()throws Exception{Entity e=(Entity)unsafe.allocateInstance(EntityItem.class);e.world=world;passengers.set(e,new ArrayList<Entity>());return e;}
 static String trace="";static Entity recv(Entity e){trace+="R";return e;}
 static Entity read(Entity e){return recv(e).getRidingEntity();}
 static boolean occupied(Entity e){return recv(e).isBeingRidden();}
 public static void main(String[] a)throws Exception{
 Field u=Unsafe.class.getDeclaredField("theUnsafe");u.setAccessible(true);unsafe=(Unsafe)u.get(null);world=(World)unsafe.allocateInstance(WorldServer.class);
 riding=Entity.class.getDeclaredField("ridingEntity");riding.setAccessible(true);passengers=Entity.class.getDeclaredField("riddenByEntities");passengers.setAccessible(true);
 ok(Modifier.isPrivate(riding.getModifiers()),"native riding state private");
 List<String> code=new ArrayList<>();new ClassReader(Entity.class.getResourceAsStream("Entity.class")).accept(new ClassVisitor(Opcodes.ASM5){public MethodVisitor visitMethod(int acc,String n,String d,String s,String[] ex){if(!n.equals("getRidingEntity"))return null;return new MethodVisitor(Opcodes.ASM5){public void visitVarInsn(int op,int var){code.add(op+":"+var);}public void visitFieldInsn(int op,String ow,String n,String d){code.add(op+":"+ow+":"+n+":"+d);}public void visitInsn(int op){code.add(""+op);}public void visitMethodInsn(int op,String ow,String n,String d,boolean i){throw new AssertionError("getter call");}};}},0);
 ok(code.equals(Arrays.asList("25:0","180:net/minecraft/entity/Entity:ridingEntity:Lnet/minecraft/entity/Entity;","176")),"exact getter bytecode");
 Entity vehicle=make(),p=make(),q=make(),other=make();
 ok(!Queries.has(false,null)&&Queries.reject(null),"complete production short circuits null");ok(!Queries.has(true,vehicle)&&!Queries.reject(vehicle)&&!Queries.skip(vehicle),"complete production empty queries");
 ok(read(p)==null&&trace.equals("R"),"null unmounted state / single receiver evaluation");trace="";ok(!occupied(vehicle)&&trace.equals("R"),"empty passenger list");
 ok(p.startRiding(vehicle),"actual native initial mount");ok(p.getRidingEntity()==vehicle&&vehicle.isBeingRidden(),"native reciprocal state");
 ok(!q.startRiding(vehicle),"native single default capacity");ok(q.getRidingEntity()==null,"rejected remains unmounted");
 ok(q.startRiding(vehicle,true),"forced second passenger fixture");ok(vehicle.getPassengers().size()==2&&vehicle.isBeingRidden(),"nonempty means any passengers, not selected rider");
 ok(Queries.has(true,vehicle)&&Queries.reject(vehicle)&&Queries.skip(vehicle),"complete production multipassenger queries");
 p.dismountRidingEntity();ok(p.getRidingEntity()==null&&q.getRidingEntity()==vehicle&&vehicle.isBeingRidden(),"one removed leaves occupancy");q.dismountRidingEntity();ok(!vehicle.isBeingRidden(),"last removed clears occupancy");
 ok(p.startRiding(other,true),"forced remount after cooldown");ok(p.getRidingEntity()==other,"fresh lookup not stale state");
 trace="";try{read(null);throw new AssertionError();}catch(NullPointerException expected){ok(trace.equals("R"),"null receiver order");}
 trace="";try{occupied(null);throw new AssertionError();}catch(NullPointerException expected){ok(trace.equals("R"),"null occupancy receiver order");}
 // Explicit external override negative control: mapping is virtual, unlike old public field.
 Custom custom=(Custom)unsafe.allocateInstance(Custom.class);custom.result=vehicle;ok(read(custom)==vehicle&&custom.calls==1,"virtual dispatch negative control");
 Map<String,String> parents=new HashMap<>();Set<String> overrides=new HashSet<>();
 for(String path:new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(a[0]))).trim().split(java.io.File.pathSeparator)){
 if(!path.endsWith(".jar"))continue;try(JarFile jar=new JarFile(path)){Enumeration<JarEntry> en=jar.entries();while(en.hasMoreElements()){JarEntry je=en.nextElement();if(!je.getName().endsWith(".class"))continue;ClassReader cr=new ClassReader(jar.getInputStream(je));parents.put(cr.getClassName(),cr.getSuperName());cr.accept(new ClassVisitor(Opcodes.ASM5){public MethodVisitor visitMethod(int ac,String n,String d,String sig,String[] ex){if(n.equals("getRidingEntity")||n.equals("getPassengers")||n.equals("isBeingRidden"))overrides.add(cr.getClassName());return null;}},ClassReader.SKIP_CODE);}}
 }
 for(String owner:overrides){String next=owner;Set<String> seen=new HashSet<>();while(next!=null&&seen.add(next)){if(next.equals("net/minecraft/entity/Entity")){ok(owner.equals(next),"resolved native Entity hierarchy has no query override: "+owner);break;}next=parents.get(next);}}
 System.out.println("PASS "+checks+" actual native getter, mount/capacity/forced-multipassenger/dismount/query and null/evaluation checks");
 }
 static class Custom extends EntityItem{Entity result;int calls;Custom(){super((World)null);}public Entity getRidingEntity(){calls++;return result;}}
}
