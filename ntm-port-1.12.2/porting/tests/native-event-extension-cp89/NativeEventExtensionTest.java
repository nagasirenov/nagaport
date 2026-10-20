import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import sun.misc.Unsafe;
import net.minecraft.entity.player.*;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.world.*;
import net.minecraft.util.math.*;
import net.minecraft.util.*;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.terraingen.*;
import net.minecraftforge.event.world.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.oredict.OreDictionary;

/** Actual Forge classes; Unsafe and reflection are confined to this isolated test. */
public class NativeEventExtensionTest {
 static Unsafe u;static int checks;static String trace="";
 static void ok(boolean value,String why){checks++;if(!value)throw new AssertionError(why);}
 static Class<?> cls(String n)throws Exception {if(n.equals("float"))return float.class;if(n.equals("int"))return int.class;if(n.equals("java.lang.String[]"))return String[].class;while(true){try{return Class.forName(n);}catch(ClassNotFoundException e){int i=n.lastIndexOf('.');if(i<0)throw e;n=n.substring(0,i)+'$'+n.substring(i+1);}}}
 static Object sample(Class<?> t)throws Exception {
  if(t==float.class)return Float.valueOf(-12.75F);if(t==int.class)return Integer.valueOf(-29);
  if(t==String.class)return new String("identity-value");if(t==String[].class)return new String[]{"first","second"};
  if(t==java.util.List.class)return new ArrayList<>();if(t==Random.class)return new Random(1234);
  if(t==net.minecraft.world.biome.Biome.class)return net.minecraft.init.Biomes.PLAINS;
  if(t==GuiScreen.class)return u.allocateInstance(net.minecraft.client.gui.GuiIngameMenu.class);
  if(t==World.class)return u.allocateInstance(WorldServer.class);if(t==EntityPlayer.class)return u.allocateInstance(EntityPlayerMP.class);
  if(t==ItemStack.class)return new ItemStack(Items.APPLE,3);if(t.isEnum())return t.getEnumConstants()[1];
  if(t.isInterface())return Proxy.newProxyInstance(t.getClassLoader(),new Class<?>[]{t},(p,m,a)->null);
  return u.allocateInstance(t);
 }
 static List<String> code(Class<?> owner,String name,String descriptor)throws Exception {
  List<String> out=new ArrayList<>();ClassReader cr=new ClassReader(owner.getResourceAsStream('/'+owner.getName().replace('.','/')+".class"));
  cr.accept(new ClassVisitor(Opcodes.ASM5){public MethodVisitor visitMethod(int access,String n,String d,String signature,String[] exceptions){if(!n.equals(name)||!d.equals(descriptor))return null;
   return new MethodVisitor(Opcodes.ASM5){public void visitVarInsn(int op,int slot){out.add(op+":"+slot);}public void visitFieldInsn(int op,String o,String n,String d){out.add(op+":"+o+":"+n+":"+d);}public void visitInsn(int op){out.add(""+op);}public void visitMethodInsn(int op,String o,String n,String d,boolean itf){out.add(op+":"+o+":"+n+":"+d);}public void visitIntInsn(int o,int x){out.add("unexpected-int");}public void visitLdcInsn(Object o){out.add("unexpected-ldc");}public void visitJumpInsn(int o,Label l){out.add("unexpected-jump");}public void visitTypeInsn(int o,String t){out.add("unexpected-type");}};
  }},ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);return out;
 }
 static Object getter(Object e,String n)throws Exception{return e.getClass().getMethod(n).invoke(e);}
 static void same(Object e,String n,Object wanted)throws Exception{ok(getter(e,n)==wanted,"constructor identity "+n);}
 static float rhs(){trace+="R";return 0.5F;}
 static float fail(){trace+="X";throw new IllegalStateException("rhs");}
 static LivingFallEvent receiver(LivingFallEvent e){trace+="E";return e;}
 static void constructors()throws Exception {
  World world=(World)sample(World.class);EntityPlayer player=(EntityPlayer)sample(EntityPlayer.class);EntityPig pig=(EntityPig)u.allocateInstance(EntityPig.class);Random rand=new Random(42);ItemStack input=new ItemStack(Items.BUCKET),output=new ItemStack(Items.APPLE);RayTraceResult target=new RayTraceResult(new Vec3d(1,2,3),EnumFacing.UP,new BlockPos(1,2,3));
  same(new PlayerEvent(player),"getEntityPlayer",player);same(new PlayerEvent(null),"getEntityPlayer",null);
  same(new BlockEvent(world,new BlockPos(2,3,4),net.minecraft.init.Blocks.STONE.getDefaultState()),"getWorld",world);
  DecorateBiomeEvent.Decorate decorate=new DecorateBiomeEvent.Decorate(world,rand,new ChunkPos(-4,7),new BlockPos(-56,8,112),DecorateBiomeEvent.Decorate.EventType.TREE);
  same(decorate,"getWorld",world);same(decorate,"getRand",rand);same(decorate,"getType",DecorateBiomeEvent.Decorate.EventType.TREE);
  PopulateChunkEvent.Populate pop=new PopulateChunkEvent.Populate(null,world,rand,-9,17,true,PopulateChunkEvent.Populate.EventType.ANIMALS);
  same(pop,"getWorld",world);same(pop,"getRand",rand);same(pop,"getType",PopulateChunkEvent.Populate.EventType.ANIMALS);ok(pop.getChunkX()==-9&&pop.getChunkZ()==17,"populate chunk coordinates not block-coordinate substitution");
  same(new LivingDeathEvent(pig,DamageSource.FALL),"getSource",DamageSource.FALL);
  FillBucketEvent bucket=new FillBucketEvent(player,input,world,target);same(bucket,"getWorld",world);same(bucket,"getEmptyBucket",input);same(bucket,"getTarget",target);
  bucket.setFilledBucket(output);same(bucket,"getFilledBucket",output);bucket.setFilledBucket(null);same(bucket,"getFilledBucket",null);
  OreDictionary.OreRegisterEvent ore=new OreDictionary.OreRegisterEvent("example",output);same(ore,"getOre",output);same(ore,"getName","example");
  List<EntityItem> drops=new ArrayList<>();LivingDropsEvent drop=new LivingDropsEvent(pig,DamageSource.FALL,drops,3,true);same(drop,"getDrops",drops);drop.getDrops().add((EntityItem)u.allocateInstance(EntityItem.class));ok(drops.size()==1,"live native drops alias, no copied list");
  List<String> lines=new ArrayList<>();ItemTooltipEvent tip=new ItemTooltipEvent(input,player,lines,net.minecraft.client.util.ITooltipFlag.TooltipFlags.ADVANCED);same(tip,"getEntityPlayer",player);same(tip,"getItemStack",input);same(tip,"getToolTip",lines);tip.getToolTip().add("line");ok(lines.size()==1,"tooltip alias mutable");
  LivingSpawnEvent spawn=new LivingSpawnEvent(pig,world,-1.5F,44F,Float.NaN);same(spawn,"getWorld",world);ok(spawn.getX()==-1.5F&&spawn.getY()==44F&&Float.isNaN(spawn.getZ()),"spawn floating positions unchanged");
  Explosion explosion=(Explosion)sample(Explosion.class);ExplosionEvent boom=new ExplosionEvent(world,explosion);same(boom,"getWorld",world);same(boom,"getExplosion",explosion);
  LivingFallEvent fall=new LivingFallEvent(pig,8F,2F);trace="";receiver(fall).setDistance(rhs());ok(trace.equals("ER")&&fall.getDistance()==0.5F&&fall.getDamageMultiplier()==2F,"receiver once then RHS, unrelated payload unchanged");
  trace="";try{receiver(null).setDistance(rhs());throw new AssertionError();}catch(NullPointerException e){ok(trace.equals("ER"),"simple setter null check follows RHS as field assignment");}
  trace="";try{receiver(null).setDistance(fail());throw new AssertionError();}catch(IllegalStateException e){ok(trace.equals("EX"),"RHS exception before null setter receiver");}
  fall.setDistance(8F);trace="";fall.setDistance((float)(fall.getDistance()*(rhs())));ok(trace.equals("R")&&fall.getDistance()==4F,"compound old-value read before RHS");
  trace="";LivingFallEvent nil=null;try{nil.setDistance((float)(nil.getDistance()*(rhs())));throw new AssertionError();}catch(NullPointerException e){ok(trace.isEmpty(),"compound null getter before RHS");}
  for(float value:new float[]{0F,-0F,Float.NaN,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.MIN_VALUE,Float.MAX_VALUE}){fall.setDistance(value);fall.setDistance((float)(fall.getDistance()*(-0.25D)));float expected=(float)(value*(-0.25D));ok(Float.floatToRawIntBits(fall.getDistance())==Float.floatToRawIntBits(expected),"float compound narrowing including nonfinite and signed zero");}
 }
 public static void main(String[] args)throws Exception {
  net.minecraft.init.Bootstrap.register();Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);u=(Unsafe)f.get(null);
  List<String[]> rules=new ArrayList<>();for(String row:Files.readAllLines(Paths.get(args[0])))rules.add(row.split("\\t"));
  for(String[] r:rules){Class<?> owner=cls(r[0]),type=cls(r[3]);Field field=owner.getDeclaredField(r[1]);field.setAccessible(true);Method get=owner.getMethod(r[2]);String desc=org.objectweb.asm.Type.getDescriptor(type);int ret=type==float.class?Opcodes.FRETURN:type==int.class?Opcodes.IRETURN:Opcodes.ARETURN;
   ok(field.getType()==type&&Modifier.isPrivate(field.getModifiers()),"native exact private field type "+r[0]+'.'+r[1]);ok(get.getReturnType()==type,"native getter actual return type");ok(Modifier.isFinal(field.getModifiers())==r[5].equals("final"),"storage mutability verified");
   ok(code(owner,r[2],"()"+desc).equals(Arrays.asList("25:0","180:"+owner.getName().replace('.','/')+":"+r[1]+":"+desc,""+ret)),"exact direct getter bytecode "+r[0]+'.'+r[2]);
   Object event=u.allocateInstance(owner),value=sample(type);field.set(event,value);Object actual=get.invoke(event);ok(type.isPrimitive()?value.equals(actual):value==actual,"actual native field/getter identity "+r[0]+'.'+r[2]);
   if(!type.isPrimitive()){field.set(event,null);ok(get.invoke(event)==null,"null identity, not copy/normalization");}
   if(!r[4].equals("-")){Method set=owner.getMethod(r[4],type);int load=type==float.class?Opcodes.FLOAD:type==int.class?Opcodes.ILOAD:Opcodes.ALOAD;
    ok(code(owner,r[4],"("+desc+")V").equals(Arrays.asList("25:0",load+":1","181:"+owner.getName().replace('.','/')+":"+r[1]+":"+desc,"177")),"exact direct native setter "+r[4]);set.invoke(event,value);actual=get.invoke(event);ok(type.isPrimitive()?value.equals(actual):value==actual,"setter affects matching native payload");
   }
  }
  Class<?> tooltip=ItemTooltipEvent.class;ok(code(tooltip,"getEntityPlayer","()Lnet/minecraft/entity/player/EntityPlayer;").equals(Arrays.asList("25:0","183:net/minecraftforge/event/entity/player/PlayerEvent:getEntityPlayer:()Lnet/minecraft/entity/player/EntityPlayer;","176")),"only allowed override is exact native super forwarding");
  constructors();
  for(int i=0;i<SelectedReads.OWNERS.length;i++){Class<?> owner=cls(SelectedReads.OWNERS[i]),receiver=SelectedReads.RECEIVERS[i];Field field=owner.getDeclaredField(SelectedReads.FIELDS[i]);field.setAccessible(true);Object event=u.allocateInstance(receiver),value=sample(field.getType());field.set(event,value);Method method=SelectedReads.class.getDeclaredMethod("read"+i,receiver);Object result=method.invoke(null,event);ok(field.getType().isPrimitive()?value.equals(result):value==result,"selected production read "+i);try{method.invoke(null,new Object[]{null});throw new AssertionError("missing NPE");}catch(InvocationTargetException e){ok(e.getCause() instanceof NullPointerException,"selected receiver NPE "+i);}}
  SelectedWrites.test();
  Map<String,String> parents=new HashMap<>();Map<String,Set<String>> methods=new HashMap<>();int jars=0;
  for(String path:new String(Files.readAllBytes(Paths.get(args[1]))).trim().split(java.io.File.pathSeparator))if(path.endsWith(".jar")&&Files.isRegularFile(Paths.get(path))){jars++;try(JarFile jar=new JarFile(path)){Enumeration<JarEntry> es=jar.entries();while(es.hasMoreElements()){JarEntry e=es.nextElement();if(!e.getName().endsWith(".class"))continue;ClassReader cr=new ClassReader(jar.getInputStream(e));parents.put(cr.getClassName(),cr.getSuperName());cr.accept(new ClassVisitor(Opcodes.ASM5){public MethodVisitor visitMethod(int a,String n,String d,String s,String[] x){methods.computeIfAbsent(cr.getClassName(),k->new HashSet<>()).add(n+d);return null;}},ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);}}}
  for(String[] r:rules){String root=cls(r[0]).getName().replace('.','/'),desc=org.objectweb.asm.Type.getDescriptor(cls(r[3]));for(String key:Arrays.asList(r[2]+"()"+desc,r[4]+"("+desc+")V"))for(Map.Entry<String,Set<String>> candidate:methods.entrySet())if(candidate.getValue().contains(key)){String walk=candidate.getKey();Set<String> seen=new HashSet<>();while(walk!=null&&seen.add(walk)){if(walk.equals(root)){boolean forwarding=candidate.getKey().equals("net/minecraftforge/event/entity/player/ItemTooltipEvent")&&root.equals("net/minecraftforge/event/entity/player/PlayerEvent")&&r[2].equals("getEntityPlayer");ok(candidate.getKey().equals(root)||forwarding,"dependency override excluded "+candidate.getKey()+'.'+key);break;}walk=parents.get(walk);}}}
  System.out.println("PASS "+checks+" native payload/constructor/bytecode/selected-read/write/evaluation checks; "+jars+" resolved dependency JARs checked for overrides. No whole handler, event-bus or game lifecycle claim.");System.exit(0);
 }
}
