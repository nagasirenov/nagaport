import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.*;
import net.minecraft.init.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import com.hbm.blocks.machine.BlockBeamBase;

public final class AirCaptureNativeTest {
    static int checks;static final StringBuilder trace=new StringBuilder();
    static void check(boolean ok,String label) { checks++;if(!ok)throw new AssertionError(label); }
    static final class Access implements IBlockAccess {
        IBlockState state=Blocks.STONE.getDefaultState();int reads;boolean poison;
        public IBlockState getBlockState(BlockPos pos){if(poison)throw new AssertionError("extra state read");reads++;trace.append('S');return state;}
        public TileEntity getTileEntity(BlockPos p){return null;}public int getCombinedLight(BlockPos p,int m){return m;}
        public boolean isAirBlock(BlockPos p){throw new AssertionError("unexpected replacement with world.isAirBlock");}
        public Biome getBiome(BlockPos p){return Biomes.PLAINS;}public int getStrongPower(BlockPos p,EnumFacing s){return 0;}
        public WorldType getWorldType(){return WorldType.DEFAULT;}public boolean isSideSolid(BlockPos p,EnumFacing s,boolean d){return d;}
    }
    static class Plain extends Block {Plain(Material m){super(m);}}
    static class NativeBeam extends BlockBeamBase {public TileEntity createNewTileEntity(World w,int m){return null;}}
    static final class StatefulSpy extends Block {
        static final PropertyInteger VALUE=PropertyInteger.create("value",0,15);
        IBlockState observed;IBlockAccess context;BlockPos position;int calls;
        StatefulSpy(){super(Material.ROCK);setDefaultState(blockState.getBaseState().withProperty(VALUE,0));}
        protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,VALUE);}
        public boolean isAir(IBlockState state,IBlockAccess world,BlockPos pos){trace.append('Q');observed=state;context=world;position=pos;calls++;return state.getValue(VALUE)==9;}
    }
    static IBlockAccess world(IBlockAccess w){trace.append('W');return w;}
    static int coordinate(char name,int value){trace.append(name);return value;}
    static IBlockState read(Access w,int x,int y,int z){return w.getBlockState(new BlockPos(x,y,z));}
    static int fail(){trace.append('X');throw new IllegalStateException();}
    public interface LegacyQuery {boolean isAir(IBlockAccess w,int x,int y,int z);}
    static final class Loader extends ClassLoader {Loader(){super(AirCaptureNativeTest.class.getClassLoader());}Class<?> define(String n,byte[] b){return defineClass(n,b,0,b.length);}}
    static LegacyQuery originalBeam(Path path)throws Exception {
        ClassWriter out=new ClassWriter(0);String n="OriginalBeamAirProjection";
        out.visit(Opcodes.V1_8,Opcodes.ACC_PUBLIC,n,null,"java/lang/Object",new String[]{"AirCaptureNativeTest$LegacyQuery"});
        MethodVisitor c=out.visitMethod(Opcodes.ACC_PUBLIC,"<init>","()V",null,null);c.visitCode();c.visitVarInsn(Opcodes.ALOAD,0);c.visitMethodInsn(Opcodes.INVOKESPECIAL,"java/lang/Object","<init>","()V",false);c.visitInsn(Opcodes.RETURN);c.visitMaxs(1,1);c.visitEnd();
        int[] count={0};try(JarFile jar=new JarFile(path.toFile());InputStream in=jar.getInputStream(jar.getJarEntry("com/hbm/blocks/machine/BlockBeamBase.class"))){
            new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM5){public MethodVisitor visitMethod(int a,String m,String d,String s,String[] e){if(m.equals("isAir")&&d.equals("(Lnet/minecraft/world/IBlockAccess;III)Z")){count[0]++;return out.visitMethod(a,m,d,s,e);}return null;}},0);
        }
        check(count[0]==1,"exact original Beam method");out.visitEnd();return(LegacyQuery)new Loader().define(n,out.toByteArray()).newInstance();
    }
    public static void main(String[] args)throws Exception {
        Bootstrap.register();Access access=new Access();BlockPos source=new BlockPos(7,64,8),query=new BlockPos(8,64,8);int registryStates=0;
        access.poison=true;
        for(Block block:Block.REGISTRY)for(IBlockState state:block.getBlockState().getValidStates()){
            check(state.getBlock()==block,"actual native state owner");
            check(block.isAir(state,access,query)==(state.getMaterial()==Material.AIR),"actual native registry air");registryStates++;
        }
        NativeBeam beam=new NativeBeam();LegacyQuery oldBeam=originalBeam(Paths.get(args[0]));
        for(int meta=0;meta<16;meta++)for(IBlockAccess context:new IBlockAccess[]{access,null}){
            IBlockState captured=beam.getStateFromMeta(meta);Block block=captured.getBlock();
            check(oldBeam.isAir(context,query.getX(),query.getY(),query.getZ())==block.isAir(captured,context,query),"actual native Beam / exact original method projection");
        }
        Block paint=new appeng.block.paint.BlockPaint();IBlockState paintState=paint.getDefaultState();
        check(paintState.getBlock()==paint,"actual AE2 state owner");check(paintState.getMaterial()!=Material.AIR,"actual AE2 non-air material");check(paint.isAir(paintState,access,query),"actual AE2 virtual native air override");
        // Original Forge base body executes on actual old Block/Material classes, not native stand-ins.
        URLClassLoader old=new URLClassLoader(new URL[]{Paths.get(args[1]).toUri().toURL()},AirCaptureNativeTest.class.getClassLoader());
        Class<?> material=old.loadClass("awt"),base=old.loadClass("aji"),accessClass=old.loadClass("ahl");base.getMethod("p").invoke(null);
        Class<?> projection=new URLClassLoader(new URL[]{Paths.get(args[2]).toUri().toURL()},old).loadClass("OriginalForgeAir");Constructor<?> constructor=projection.getConstructor(material);Method isAir=projection.getMethod("isAir",accessClass,int.class,int.class,int.class);
        for(String row:Files.readAllLines(Paths.get(args[3]))){String[] f=row.split("\t");Object legacyMaterial=material.getField(f[3]).get(null);Object oldBlock=constructor.newInstance(legacyMaterial);Material nativeMaterial=(Material)Material.class.getField(f[2]).get(null);Block b=new Plain(nativeMaterial);IBlockState state=b.getDefaultState();check((Boolean)isAir.invoke(oldBlock,null,7,64,8)==b.isAir(state,access,query),"actual old/native material with extracted Forge hook "+f[2]);}
        // Neighbor receiver and caller context deliberately remain different, exactly as DroneWaypoint.
        access.poison=false;access.state=Blocks.STONE.getDefaultState();IBlockState neighbor=access.getBlockState(source);Block block=neighbor.getBlock();access.state=Blocks.AIR.getDefaultState();access.poison=true;
        check(!block.isAir(neighbor,access,query),"receiver snapshot survives target mismatch and later world mutation");check(neighbor.getBlock()==Blocks.STONE,"state owns original receiver, not query-position air");
        // Stateful native override fixture proves non-default metadata, original coordinates and single capture.
        StatefulSpy spy=new StatefulSpy();access.poison=false;access.state=spy.getDefaultState().withProperty(StatefulSpy.VALUE,9);access.reads=0;trace.setLength(0);
        IBlockState nativeAirState=read(access,coordinate('x',source.getX()),coordinate('y',source.getY()),coordinate('z',source.getZ()));
        Block originalReceiver=nativeAirState.getBlock();access.state=Blocks.AIR.getDefaultState();access.poison=true;
        check(originalReceiver.isAir(nativeAirState,world(access),new BlockPos(coordinate('X',query.getX()),coordinate('Y',query.getY()),coordinate('Z',query.getZ()))),"stateful native dispatch receives captured metadata");
        check(trace.toString().equals("xyzSWXYZQ")&&access.reads==1&&spy.calls==1,"one unchanged receiver read and argument evaluation sequence");
        check(spy.observed==nativeAirState&&spy.context==access&&spy.position.equals(query),"state, context and mismatched position identities");
        check(!spy.isAir(spy.getDefaultState(),access,query),"default-state substitution would be wrong");
        int[][] points={{Integer.MIN_VALUE,Integer.MAX_VALUE,Integer.MIN_VALUE},{30000000,-1,-30000001},{-30000000,256,30000000},{Integer.MAX_VALUE,Integer.MIN_VALUE,Integer.MAX_VALUE}};
        for(int[] p:points){spy.isAir(nativeAirState,access,new BlockPos(p[0],p[1],p[2]));check(spy.position.getX()==p[0]&&spy.position.getY()==p[1]&&spy.position.getZ()==p[2],"raw integer context coordinates");}
        // Capture keeps the existing read even if a later condition short-circuits.
        access.poison=false;access.state=Blocks.STONE.getDefaultState();access.reads=0;IBlockState shortState=read(access,1,2,3);Block shortBlock=shortState.getBlock();trace.setLength(0);
        boolean skip=false&&shortBlock.isAir(shortState,world(access),new BlockPos(coordinate('X',1),coordinate('Y',2),coordinate('Z',3)));
        check(!skip&&access.reads==1&&trace.length()==0,"and short circuit preserves prior state read");
        skip=true||shortBlock.isAir(shortState,world(access),new BlockPos(coordinate('X',1),coordinate('Y',2),coordinate('Z',3)));
        check(skip&&trace.length()==0,"or short circuit");
        trace.setLength(0);try{shortBlock.isAir(shortState,world(access),new BlockPos(fail(),coordinate('Y',2),coordinate('Z',3)));throw new AssertionError();}catch(IllegalStateException expected){check(trace.toString().equals("WX"),"argument exception timing");}
        // Both original chained read and captured read fail before query arguments on a null state.
        access.state=null;trace.setLength(0);try{Block b=read(access,1,2,3).getBlock();b.isAir(null,world(access),query);throw new AssertionError();}catch(NullPointerException expected){check(trace.toString().equals("S"),"original read null timing");}
        trace.setLength(0);try{IBlockState state=read(access,1,2,3);Block b=state.getBlock();b.isAir(state,world(access),query);throw new AssertionError();}catch(NullPointerException expected){check(trace.toString().equals("S"),"captured read null timing");}
        System.out.println("PASS "+checks+" assertions; "+registryStates+" actual native registry states; actual BlockBeamBase and AE2 BlockPaint; original Beam bytecode and Forge-base method projections; capture/evaluation/mismatch fixtures");
    }
}
