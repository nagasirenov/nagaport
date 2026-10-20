import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.lang.reflect.*;
import org.objectweb.asm.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import com.hbm.blocks.generic.BlockLayering;
import com.hbm.blocks.ModBlocks;

/** Genuine native API and BlockLayering; the six isolated owner methods are explicitly projections. */
public final class ReplaceabilityNativeTest {
    public interface LegacyQuery { boolean isReplaceable(IBlockAccess world, int x, int y, int z); }
    static int checks;
    static void check(boolean condition, String label) { checks++; if (!condition) throw new AssertionError(label); }
    static final StringBuilder trace = new StringBuilder();
    static Block receiver(Block b) { trace.append('R'); return b; }
    static LegacyQuery receiver(LegacyQuery b) { trace.append('R'); return b; }
    static IBlockAccess access(IBlockAccess w) { trace.append('W'); return w; }
    static int coordinate(char name, int value) { trace.append(name); return value; }
    static int failCoordinate() { trace.append('X'); throw new IllegalStateException(); }
    static class Access implements IBlockAccess {
        IBlockState state = Blocks.STONE.getDefaultState();
        int reads; boolean poison; BlockPos queried;
        public IBlockState getBlockState(BlockPos pos) { if (poison) throw new AssertionError("unexpected world read"); reads++; queried=pos; return state; }
        public TileEntity getTileEntity(BlockPos pos) { return null; }
        public int getCombinedLight(BlockPos pos,int min) { return min; }
        public boolean isAirBlock(BlockPos pos) { IBlockState s=getBlockState(pos); return s.getBlock().isAir(s,this,pos); }
        public Biome getBiome(BlockPos pos) { return Biomes.PLAINS; }
        public int getStrongPower(BlockPos pos,EnumFacing side) { return 0; }
        public WorldType getWorldType() { return WorldType.DEFAULT; }
        public boolean isSideSolid(BlockPos pos,EnumFacing side,boolean def) { return def; }
    }
    static class Spy extends Block {
        IBlockAccess observed; BlockPos position; int calls;
        Spy() { super(Material.ROCK); }
        @Override public boolean isReplaceable(IBlockAccess world,BlockPos pos) { trace.append('Q'); observed=world;position=pos;calls++;return true; }
    }
    static class DefinitionLoader extends ClassLoader {
        DefinitionLoader() { super(ReplaceabilityNativeTest.class.getClassLoader()); }
        Class<?> define(String name,byte[] bytes) { return defineClass(name,bytes,0,bytes.length); }
    }
    static LegacyQuery originalMethod(JarFile original,String owner) throws Exception {
        String name="OriginalProjection_"+owner.substring(owner.lastIndexOf('/')+1);
        ClassWriter out=new ClassWriter(0);
        out.visit(Opcodes.V1_8,Opcodes.ACC_PUBLIC,name,null,"net/minecraft/block/Block",new String[]{"ReplaceabilityNativeTest$LegacyQuery"});
        MethodVisitor constructor=out.visitMethod(Opcodes.ACC_PUBLIC,"<init>","()V",null,null);
        constructor.visitCode();constructor.visitVarInsn(Opcodes.ALOAD,0);
        constructor.visitFieldInsn(Opcodes.GETSTATIC,"net/minecraft/block/material/Material","ROCK","Lnet/minecraft/block/material/Material;");
        constructor.visitMethodInsn(Opcodes.INVOKESPECIAL,"net/minecraft/block/Block","<init>","(Lnet/minecraft/block/material/Material;)V",false);
        constructor.visitInsn(Opcodes.RETURN);constructor.visitMaxs(2,1);constructor.visitEnd();
        int[] found={0};
        try(InputStream in=original.getInputStream(original.getJarEntry(owner+".class"))) {
            new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM5) {
                public MethodVisitor visitMethod(int flags,String method,String descriptor,String signature,String[] exceptions) {
                    if (!method.equals("isReplaceable") || !descriptor.equals("(Lnet/minecraft/world/IBlockAccess;III)Z")) return null;
                    found[0]++; return out.visitMethod(flags,method,descriptor,signature,exceptions);
                }
            },0);
        }
        check(found[0]==1,"exact original method copied");out.visitEnd();
        return (LegacyQuery)new DefinitionLoader().define(name,out.toByteArray()).newInstance();
    }
    public static void main(String[] args) throws Exception {
        Bootstrap.register();
        int[][] points={{0,0,0},{1,255,-1},{-30000000,-1,30000000},{30000000,256,-30000001},
                {Integer.MIN_VALUE,Integer.MAX_VALUE,Integer.MIN_VALUE},{Integer.MAX_VALUE,Integer.MIN_VALUE,Integer.MAX_VALUE}};
        Access world=new Access();world.poison=true;
        try(JarFile original=new JarFile(args[0])) {
            for(String row:Files.readAllLines(Paths.get(args[1]))) {
                String[] fields=row.split("\t");String owner=fields[0];boolean expected=Boolean.parseBoolean(fields[1]);
                LegacyQuery old=originalMethod(original,owner.replace('.','/'));
                Block nativeOwner=(Block)Class.forName("OwnerProjections$"+owner.substring(owner.lastIndexOf('.')+1)).newInstance();
                Block inherited=(Block)Class.forName("OwnerProjections$Descendant_"+owner.substring(owner.lastIndexOf('.')+1)).newInstance();
                for(IBlockAccess context:new IBlockAccess[]{world,null}) for(int[] p:points) {
                    check(old.isReplaceable(context,p[0],p[1],p[2])==expected,"original actual method projection");
                    check(nativeOwner.isReplaceable(context,new BlockPos(p[0],p[1],p[2]))==expected,"native complete method projection");
                    check(inherited.isReplaceable(context,new BlockPos(p[0],p[1],p[2]))==expected,"virtual inherited native method projection");
                }
                trace.setLength(0);
                receiver(old).isReplaceable(access(world),coordinate('X',1),coordinate('Y',2),coordinate('Z',3));
                check(trace.toString().equals("RWXYZ"),"original evaluation order");
                trace.setLength(0);
                receiver(nativeOwner).isReplaceable(access(world),new BlockPos(coordinate('X',1),coordinate('Y',2),coordinate('Z',3)));
                check(trace.toString().equals("RWXYZ"),"native evaluation order");
            }
        }
        Spy spy=new Spy();
        for(int[] p:points) {
            trace.setLength(0);int prior=spy.calls;
            check(receiver(spy).isReplaceable(access(world),new BlockPos(coordinate('X',p[0]),coordinate('Y',p[1]),coordinate('Z',p[2]))),"dynamic override");
            check(trace.toString().equals("RWXYZQ") && spy.calls==prior+1 && spy.observed==world,"single evaluation and dispatch");
            check(spy.position.getX()==p[0] && spy.position.getY()==p[1] && spy.position.getZ()==p[2],"raw integer coordinate preservation");
        }
        trace.setLength(0);try { receiver((Block)null).isReplaceable(access(world),new BlockPos(coordinate('X',1),coordinate('Y',2),coordinate('Z',3))); throw new AssertionError(); }
        catch(NullPointerException expected) { check(trace.toString().equals("RWXYZ"),"native null receiver after arguments"); }
        trace.setLength(0);try { receiver((LegacyQuery)null).isReplaceable(access(world),coordinate('X',1),coordinate('Y',2),coordinate('Z',3)); throw new AssertionError(); }
        catch(NullPointerException expected) { check(trace.toString().equals("RWXYZ"),"old null receiver after arguments"); }
        trace.setLength(0);try { receiver(spy).isReplaceable(access(world),new BlockPos(failCoordinate(),coordinate('Y',2),coordinate('Z',3))); throw new AssertionError(); }
        catch(IllegalStateException expected) { check(trace.toString().equals("RWX"),"argument exception stops later evaluation"); }
        trace.setLength(0);boolean skip=false && receiver(spy).isReplaceable(access(world),new BlockPos(coordinate('X',1),coordinate('Y',2),coordinate('Z',3)));
        check(!skip && trace.length()==0,"and short circuit");
        skip=true || receiver(spy).isReplaceable(access(world),new BlockPos(coordinate('X',1),coordinate('Y',2),coordinate('Z',3)));
        check(skip && trace.length()==0,"or short circuit");
        world.poison=false;BlockPos pos=new BlockPos(8,64,8);int baseStates=0;
        for(Block block:Block.REGISTRY) if(block.getClass().getMethod("isReplaceable",IBlockAccess.class,BlockPos.class).getDeclaringClass()==Block.class) {
            for(IBlockState state:block.getBlockState().getValidStates()) {
                world.state=state;world.reads=0;
                check(block.isReplaceable(world,pos)==state.getMaterial().isReplaceable(),"actual registry same-target base query");
                check(world.reads==1 && world.queried.equals(pos),"actual base world lookup");baseStates++;
            }
        }
        world.state=Blocks.AIR.getDefaultState();
        check(Blocks.STONE.isReplaceable(world,pos),"native mismatched receiver observes target air");
        check(!Blocks.STONE.getDefaultState().getMaterial().isReplaceable(),"legacy base receiver material differs (OilWell exclusion)");
        for(Block block:new Block[]{Blocks.AIR,Blocks.DEADBUSH,Blocks.TALLGRASS,Blocks.VINE}) {
            world.poison=true;check(block.isReplaceable(world,pos),"actual constant native override");world.poison=false;
        }
        for(int layers=1;layers<=8;layers++) {
            world.state=Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS,layers);
            check(Blocks.SNOW_LAYER.isReplaceable(world,pos)==(layers==1),"actual native snow contextual override");
        }
        for(BlockDoublePlant.EnumPlantType plant:BlockDoublePlant.EnumPlantType.values()) {
            world.state=Blocks.DOUBLE_PLANT.getDefaultState().withProperty(BlockDoublePlant.HALF,BlockDoublePlant.EnumBlockHalf.LOWER).withProperty(BlockDoublePlant.VARIANT,plant);
            check(Blocks.DOUBLE_PLANT.isReplaceable(world,pos)==(plant==BlockDoublePlant.EnumPlantType.GRASS || plant==BlockDoublePlant.EnumPlantType.FERN),"actual native double plant contextual override");
        }
        BlockLayering layer=new BlockLayering(Material.AIR);Block receiverLayer=layer;
        for(int meta=0;meta<16;meta++) {world.state=layer.getStateFromMeta(meta);check(receiverLayer.isReplaceable(world,pos)==(meta<7),"actual NTM BlockLayering full metadata");}
        for(int identity=0;identity<3;identity++) {
            ModBlocks.leaves_layer=identity==0?layer:null;ModBlocks.oil_spill=identity==1?layer:null;ModBlocks.foam_layer=identity==2?layer:null;
            world.poison=true;check(receiverLayer.isReplaceable(world,pos),"actual NTM layer exception bypasses world");world.poison=false;
        }
        System.out.println("PASS "+checks+" native checks; "+baseStates+" actual registry base states; actual BlockLayering; six complete source/original-bytecode method projections; evaluation and exclusion counterexample");
    }
}
