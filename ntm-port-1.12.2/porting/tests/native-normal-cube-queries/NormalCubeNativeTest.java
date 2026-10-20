import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.init.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraftforge.common.property.*;
import com.hbm.world.WorldBlockState;
import com.hbm.blocks.generic.BlockNTMGlass;
import com.hbm.blocks.machine.BlockBeamBase;

public final class NormalCubeNativeTest {
    static int checks; static final StringBuilder trace=new StringBuilder();
    static void check(boolean ok,String label){checks++;if(!ok)throw new AssertionError(label);}
    static class Matrix extends Block {
        boolean full,power; Matrix(Material m,boolean f,boolean p){super(m);full=f;power=p;}
        public boolean isFullCube(IBlockState s){trace.append('F');return full;}
        public boolean canProvidePower(IBlockState s){trace.append('P');return power;}
    }
    static class Beam extends BlockBeamBase {public TileEntity createNewTileEntity(World w,int m){throw new AssertionError("unused tile factory");}}
    static class AE extends appeng.block.AEBaseBlock {
        AE(boolean full,boolean opaque){super(Material.ROCK);setFullSize(full);setOpaque(opaque);}
    }
    static class Spy extends Block {
        static final PropertyInteger META=PropertyInteger.create("meta",0,15);
        static final IUnlistedProperty<Integer> EXTRA=new IUnlistedProperty<Integer>() { public String getName(){return "extra";} public boolean isValid(Integer value){return true;} public Class<Integer> getType(){return Integer.class;} public String valueToString(Integer value){return value.toString();} };
        IBlockState observed;int calls; boolean answer;
        Spy(){super(Material.ROCK);}
        protected BlockStateContainer createBlockState(){return new ExtendedBlockState(this,new IProperty[]{META},new IUnlistedProperty[]{EXTRA});}
        public boolean isNormalCube(IBlockState s){trace.append('Q');observed=s;calls++;return answer && s.getValue(META)==9;}
    }
    static class FixtureWorld extends World {
        IBlockState state;int reads;boolean poison;BlockPos observed;
        FixtureWorld(){super(new net.minecraft.world.storage.SaveHandlerMP(),new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new net.minecraft.profiler.Profiler(),false);}
        protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
        protected boolean isChunkLoaded(int x,int z,boolean empty){throw new AssertionError("unexpected chunk read");}
        public IBlockState getBlockState(BlockPos p){if(poison)throw new AssertionError("unexpected reread");reads++;observed=p;trace.append('S');return state;}
    }
    static World world(FixtureWorld w){trace.append('W');return w;}
    static int coord(char c,int v){trace.append(c);return v;}
    static int fail(){trace.append('X');throw new IllegalStateException();}
    public interface OriginalPower {boolean func_149744_f();}
    static class Loader extends ClassLoader {Loader(){super(NormalCubeNativeTest.class.getClassLoader());}Class<?> define(String n,byte[] b){return defineClass(n,b,0,b.length);}}
    static OriginalPower originalPower(Path path)throws Exception {
        String name="OriginalPowerDetectorCapability";ClassWriter out=new ClassWriter(0);
        out.visit(Opcodes.V1_8,Opcodes.ACC_PUBLIC,name,null,"java/lang/Object",new String[]{"NormalCubeNativeTest$OriginalPower"});
        MethodVisitor c=out.visitMethod(Opcodes.ACC_PUBLIC,"<init>","()V",null,null);c.visitCode();c.visitVarInsn(Opcodes.ALOAD,0);c.visitMethodInsn(Opcodes.INVOKESPECIAL,"java/lang/Object","<init>","()V",false);c.visitInsn(Opcodes.RETURN);c.visitMaxs(1,1);c.visitEnd();int[] count={0};
        try(JarFile jar=new JarFile(path.toFile());InputStream in=jar.getInputStream(jar.getJarEntry("com/hbm/blocks/machine/PowerDetector.class"))){new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM5){public MethodVisitor visitMethod(int a,String n,String d,String s,String[] e){if(n.equals("func_149744_f")&&d.equals("()Z")){count[0]++;return out.visitMethod(a,n,d,s,e);}return null;}},0);}
        check(count[0]==1,"one exact original power method");out.visitEnd();return(OriginalPower)new Loader().define(name,out.toByteArray()).newInstance();
    }
    public static void main(String[] args)throws Exception {
        Bootstrap.register();int registry=0;
        for(Block b:Block.REGISTRY)for(IBlockState s:b.getBlockState().getValidStates()){
            check(s.getBlock()==b,"actual registry state owner");
            check(s.isNormalCube()==b.isNormalCube(s),"actual live native Block/state dispatch");registry++;
        }
        // Compare the genuine original r() predicate to genuine native base dispatch.
        URLClassLoader old=new URLClassLoader(new URL[]{Paths.get(args[1]).toUri().toURL()},NormalCubeNativeTest.class.getClassLoader());
        Class<?> material=old.loadClass("awt"),base=old.loadClass("aji");base.getMethod("p").invoke(null);
        Class<?> fixture=new URLClassLoader(new URL[]{Paths.get(args[2]).toUri().toURL()},old).loadClass("OriginalCube");
        Constructor<?> ctor=fixture.getConstructor(material,boolean.class,boolean.class);Method normal=base.getMethod("r");
        for(String row:Files.readAllLines(Paths.get(args[3]))){String[] f=row.split("\t");for(boolean full:new boolean[]{false,true})for(boolean power:new boolean[]{false,true}){
            Object original=ctor.newInstance(material.getField(f[3]).get(null),full,power);Matrix nativeBlock=new Matrix((Material)Material.class.getField(f[2]).get(null),full,power);
            check((Boolean)normal.invoke(original)==nativeBlock.isNormalCube(nativeBlock.getDefaultState()),"actual old/native material/full/power matrix "+f[2]);
        }}
        Matrix matrix=new Matrix(Material.ROCK,false,false);trace.setLength(0);check(!matrix.getDefaultState().isNormalCube()&&trace.toString().equals("F"),"full false short-circuits power");
        matrix.full=true;trace.setLength(0);check(matrix.getDefaultState().isNormalCube()&&trace.toString().equals("FP"),"live full/power dispatch not cached");matrix.power=true;check(!matrix.getDefaultState().isNormalCube(),"power change observed live");
        Matrix transparent=new Matrix(Material.AIR,true,false);trace.setLength(0);check(!transparent.getDefaultState().isNormalCube()&&trace.length()==0,"material opacity short circuit");
        // This is the actual complete NTM class, not its extracted callback.
        for(Material m:new Material[]{Material.GLASS,Material.ROCK,Material.IRON}){BlockNTMGlass glass=new BlockNTMGlass(0,"test",m);check(!glass.getDefaultState().isNormalCube(),"full actual BlockNTMGlass full-cube exclusion");}
        Beam beam=new Beam();for(int meta=0;meta<16;meta++)check(!beam.getStateFromMeta(meta).isNormalCube(),"full actual Beam state "+meta);
        OriginalPower power=originalPower(Paths.get(args[0]));PowerDetectorCapability detector=new PowerDetectorCapability(Material.IRON);
        check(power.func_149744_f()==detector.canProvidePower(null),"exact original/current constant owner projections");
        check(detector.getDefaultState().getMaterial().isOpaque()&&detector.getDefaultState().isFullCube(),"PowerDetector material/full prerequisites");
        check(!detector.getDefaultState().isNormalCube(),"PowerDetector native virtual power exclusion");
        // Actual AEBaseBlock final one-arg method is distinct from its contextual method.
        for(boolean full:new boolean[]{false,true})for(boolean opaque:new boolean[]{false,true}){AE ae=new AE(full,opaque);IBlockState s=ae.getDefaultState();check(s.isNormalCube()==(full&&opaque),"actual AE2 one-arg dispatch");check(ae.isNormalCube(s,null,BlockPos.ORIGIN)==full,"actual AE2 three-arg boundary");}
        Spy spy=new Spy();spy.answer=true;IBlockState captured=spy.getDefaultState().withProperty(Spy.META,9);check(captured instanceof IExtendedBlockState,"actual Forge extended state");
        spy.calls=0;check(captured.isNormalCube()&&spy.observed==captured&&spy.calls==1,"extended state live dispatch retains state identity");spy.answer=false;check(!captured.isNormalCube()&&spy.calls==2,"extended state not cached");spy.answer=true;
        FixtureWorld w=new FixtureWorld();w.state=captured;trace.setLength(0);spy.calls=0;
        boolean inline=WorldBlockState.read(world(w),coord('x',7),coord('y',64),coord('z',8)).isNormalCube();
        check(inline&&trace.toString().equals("WxyzSQ")&&w.reads==1&&spy.calls==1,"actual WorldBlockState inline evaluation and dispatch");
        w.reads=0;trace.setLength(0);IBlockState nativeCubeState=WorldBlockState.read(world(w),coord('x',7),coord('y',64),coord('z',8));Block receiver=nativeCubeState.getBlock();
        w.state=Blocks.AIR.getDefaultState();w.poison=true;check(receiver.isNormalCube(nativeCubeState),"pre-mutation receiver state retained");check(w.reads==1&&trace.toString().equals("WxyzSQ")&&spy.observed==captured,"no reread or default-state substitution");
        check(!spy.isNormalCube(spy.getDefaultState()),"default-state substitution would change state-sensitive native result");
        trace.setLength(0);int saved=spy.calls;check(!(false&&receiver.isNormalCube(nativeCubeState)),"and branch");check(true||receiver.isNormalCube(nativeCubeState),"or branch");check(trace.length()==0&&spy.calls==saved,"local query short circuit");
        check(!(false&&WorldBlockState.read(world(w),coord('x',1),coord('y',2),coord('z',3)).isNormalCube()),"inline and branch");check(trace.length()==0,"inline read stays short-circuited");
        try{WorldBlockState.read(world(w),fail(),coord('y',2),coord('z',3)).isNormalCube();throw new AssertionError();}catch(IllegalStateException expected){check(trace.toString().equals("WX"),"throw preserves argument order");}
        w.poison=false;w.state=null;trace.setLength(0);try{WorldBlockState.read(world(w),1,2,3).getBlock();throw new AssertionError();}catch(NullPointerException expected){check(trace.toString().equals("WS"),"old null state timing");}
        trace.setLength(0);try{WorldBlockState.read(world(w),1,2,3).isNormalCube();throw new AssertionError();}catch(NullPointerException expected){check(trace.toString().equals("WS"),"new null state timing");}
        w.poison=true;for(int[] p:new int[][]{{Integer.MIN_VALUE,64,0},{0,-1,0},{0,256,0},{30000000,0,0},{0,0,Integer.MAX_VALUE}})check(!WorldBlockState.read(w,p[0],p[1],p[2]).isNormalCube(),"unchanged actual helper bounds");
        System.out.println("PASS "+checks+" assertions; "+registry+" actual native registry states; full actual BlockNTMGlass/BlockBeamBase; actual AEBaseBlock final dispatch; original engine matrix and exact PowerDetector capability projections; actual world-helper, extended-state, mutation and evaluation fixtures");
    }
}
