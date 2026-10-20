import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.profiler.Profiler;
import net.minecraft.nbt.NBTTagCompound;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
/** Real native World/BlockPos/state types; explicit selected-expression fixture, not whole owners. */
public class NativeProof {
 static int checks,events; static Method legacyBounds;
 static void check(boolean ok,String s){checks++;if(!ok)throw new AssertionError(s);}
 static class RecordingWorld extends World {
  List<String> trace=new ArrayList<>();
  RecordingWorld(){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),false);}
  protected IChunkProvider createChunkProvider(){return null;}
  protected boolean isChunkLoaded(int x,int z,boolean allowEmpty){return false;}
  public boolean setBlockState(BlockPos p,IBlockState s,int f){trace.add(event(p.getX(),p.getY(),p.getZ(),s.getBlock(),s.getBlock().getMetaFromState(s),f));return trace.size()%2==0;}
 }
 static String event(int x,int y,int z,Block b,int m,int f){return x+","+y+","+z+","+Block.getIdFromBlock(b)+","+m+","+f;}
 static boolean valid(int x,int y,int z){try{return (Boolean)legacyBounds.invoke(null,null,x,y,z);}catch(Exception e){throw new AssertionError(e);}}
 static void legacy(World w,int x,int y,int z,Block b){legacy(w,x,y,z,b,0,3);}
 static void legacy(World w,int x,int y,int z,Block b,int m,int f){if(w==null)throw new NullPointerException();if(valid(x,y,z))((RecordingWorld)w).trace.add(event(x,y,z,b,m,f));}
 static void loadBounds(String jar)throws Exception {
  ClassNode original=new ClassNode();try(JarFile jf=new JarFile(jar);InputStream in=jf.getInputStream(jf.getJarEntry("ahb.class"))){new ClassReader(in).accept(original,0);}
  MethodNode source=null;for(Object o:original.methods){MethodNode m=(MethodNode)o;if(m.name.equals("d")&&m.desc.equals("(IIILaji;II)Z"))source=m;}check(source!=null,"actual original placement bytecode");
  ClassWriter cw=new ClassWriter(ClassWriter.COMPUTE_MAXS|ClassWriter.COMPUTE_FRAMES);cw.visit(Opcodes.V1_8,Opcodes.ACC_PUBLIC,"OriginalPlacementBounds",null,"java/lang/Object",null);
  MethodVisitor mv=cw.visitMethod(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"valid","(Ljava/lang/Object;III)Z",null,null);mv.visitCode();
  int n=0;boolean end=false;for(AbstractInsnNode i=source.instructions.getFirst();i!=null;i=i.getNext()){
   if(i instanceof VarInsnNode&&i.getOpcode()==Opcodes.ALOAD&&((VarInsnNode)i).var==0){end=true;break;}
   if(i instanceof FrameNode||i instanceof LineNumberNode)continue;i.accept(mv);if(i.getOpcode()>=0)n++;
  }
  check(end&&n==23,"exact original 23-instruction pre-chunk bounds prefix: "+n);mv.visitInsn(Opcodes.ICONST_1);mv.visitInsn(Opcodes.IRETURN);mv.visitMaxs(0,0);mv.visitEnd();cw.visitEnd();byte[] bytes=cw.toByteArray();
  class Loader extends ClassLoader{Class<?> load(){return defineClass("OriginalPlacementBounds",bytes,0,bytes.length);}}
  legacyBounds=new Loader().load().getMethod("valid",Object.class,int.class,int.class,int.class);
 }
 static final int[][] origins={{0,64,0},{-17,200,-31},{29999990,0,-29999990},{-30000000,255,29999999},{Integer.MAX_VALUE,64,Integer.MIN_VALUE},{0,-256,0},{0,256,0},{29999999,64,29999999},{-30000001,64,-30000001}};
 public static void main(String[] args)throws Exception {
  Bootstrap.register();loadBounds(args[0]);GeneratedProof.run();EvaluationProof.run();System.out.println("PASS "+checks+" native checks and "+events+" exact ordered original-expression/native state events; actual original bounds bytecode, null timing, argument exceptions, RNG state, receiver mutation, original flags and ignored return values.");
 }
}
