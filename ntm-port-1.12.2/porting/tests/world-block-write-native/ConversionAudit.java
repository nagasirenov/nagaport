import java.io.*;import java.lang.reflect.*;import java.util.*;import org.objectweb.asm.*;
import net.minecraft.block.Block;import net.minecraft.init.Blocks;import net.minecraft.init.Bootstrap;
/** Fail closed on effects or new calls in the exact vanilla state-conversion graph. */
public class ConversionAudit {
 static int methods;
 static final Set<String> calls=new HashSet<>(Arrays.asList(
  "net/minecraft/block/Block.getDefaultState()Lnet/minecraft/block/state/IBlockState;",
  "net/minecraft/block/BlockFire.getDefaultState()Lnet/minecraft/block/state/IBlockState;",
  "net/minecraft/block/BlockDirt.getDefaultState()Lnet/minecraft/block/state/IBlockState;",
  "net/minecraft/block/BlockLiquid.getDefaultState()Lnet/minecraft/block/state/IBlockState;",
  "java/lang/Integer.valueOf(I)Ljava/lang/Integer;",
  "net/minecraft/block/BlockDirt$DirtType.byMetadata(I)Lnet/minecraft/block/BlockDirt$DirtType;",
  "net/minecraft/block/state/IBlockState.withProperty(Lnet/minecraft/block/properties/IProperty;Ljava/lang/Comparable;)Lnet/minecraft/block/state/IBlockState;",
  "com/google/common/collect/ImmutableMap.get(Ljava/lang/Object;)Ljava/lang/Object;",
  "com/google/common/collect/ImmutableTable.get(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
  // Error construction paths are unreachable for the exact allowed values tested.
  "java/lang/StringBuilder.<init>()V",
  "java/lang/StringBuilder.append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
  "java/lang/StringBuilder.append(Ljava/lang/Object;)Ljava/lang/StringBuilder;",
  "java/lang/StringBuilder.toString()Ljava/lang/String;",
  "java/lang/IllegalArgumentException.<init>(Ljava/lang/String;)V",
  "net/minecraft/block/Block.getBlockState()Lnet/minecraft/block/state/BlockStateContainer;",
  "net/minecraft/util/registry/RegistryNamespacedDefaultedByKey.getNameForObject(Ljava/lang/Object;)Ljava/lang/Object;"
 ));
 static void inspect(String owner,String name,String desc)throws Exception{
  final int[] found={0};try(InputStream in=ConversionAudit.class.getClassLoader().getResourceAsStream(owner+".class")){
   if(in==null)throw new AssertionError(owner);new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM5){
    public MethodVisitor visitMethod(int access,String n,String d,String sig,String[] ex){if(!name.equals(n)||!desc.equals(d))return null;found[0]++;if((access&(Opcodes.ACC_NATIVE|Opcodes.ACC_SYNCHRONIZED))!=0)throw new AssertionError("effectful method "+owner);
     return new MethodVisitor(Opcodes.ASM5){
      public void visitFieldInsn(int opcode,String o,String n,String d){if(opcode==Opcodes.PUTFIELD||opcode==Opcodes.PUTSTATIC)throw new AssertionError("conversion field write "+o+"."+n);}
      public void visitInsn(int op){if((op>=Opcodes.IASTORE&&op<=Opcodes.SASTORE)||op==Opcodes.MONITORENTER||op==Opcodes.MONITOREXIT)throw new AssertionError("conversion mutation/monitor "+op);}
      public void visitMethodInsn(int op,String o,String n,String d,boolean itf){if(!calls.contains(o+"."+n+d))throw new AssertionError("unreviewed conversion call "+o+"."+n+d);}
      public void visitInvokeDynamicInsn(String n,String d,Handle h,Object... args){throw new AssertionError("conversion dynamic invocation");}
     };
    }
   },ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
  }if(found[0]!=1)throw new AssertionError(owner+"."+name+" matches "+found[0]);methods++;System.out.println("Read-only reviewed conversion method: "+owner+"."+name+desc);
 }
 public static void main(String[] args)throws Exception{
  Bootstrap.register();Set<String> owners=new HashSet<>();for(Block b:new Block[]{Blocks.AIR,Blocks.FIRE,Blocks.GRASS,Blocks.DIRT,Blocks.MOSSY_COBBLESTONE,Blocks.FLOWING_LAVA,Blocks.LAVA})owners.add(b.getClass().getMethod("getStateFromMeta",int.class).getDeclaringClass().getName().replace('.','/'));
  if(!owners.equals(new HashSet<>(Arrays.asList("net/minecraft/block/Block","net/minecraft/block/BlockFire","net/minecraft/block/BlockDirt","net/minecraft/block/BlockLiquid"))))throw new AssertionError(owners);
  for(String owner:new TreeSet<>(owners))inspect(owner,"getStateFromMeta","(I)Lnet/minecraft/block/state/IBlockState;");
  inspect("net/minecraft/block/Block","getDefaultState","()Lnet/minecraft/block/state/IBlockState;");
  inspect("net/minecraft/block/BlockDirt$DirtType","byMetadata","(I)Lnet/minecraft/block/BlockDirt$DirtType;");
  inspect("net/minecraft/block/state/BlockStateContainer$StateImplementation","withProperty","(Lnet/minecraft/block/properties/IProperty;Ljava/lang/Comparable;)Lnet/minecraft/block/state/IBlockState;");
  System.out.println("PASS: "+methods+" actual conversion methods have no field/array writes, monitors, dynamic invocation or unreviewed calls; actual allowed-state totality/identity tested separately");
 }
}
