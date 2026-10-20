import com.hbm.blocks.BlockBase;
import com.hbm.blocks.generic.BlockGeneric;



import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.IntBuffer;
import java.util.*;
import javax.imageio.ImageIO;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.chunk.*;
import net.minecraft.world.chunk.storage.*;
import net.minecraft.world.storage.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class CTStateTest {
 static int checks;static void check(boolean ok,String s){checks++;if(!ok)throw new AssertionError(s);}
 static final class NativeWorld extends World {
        final Map<Long, Chunk> chunks = new HashMap<>();
        NativeWorld() {
            super(new SaveHandlerMP(), new WorldInfo(new NBTTagCompound()), new WorldProviderSurface(), new Profiler(), false);
            provider.setWorld(this);
            chunkProvider = createChunkProvider();
            for (int x=-1;x<=1;x++) for (int z=-1;z<=1;z++) {
                Chunk chunk = new Chunk(this,x,z); chunk.markLoaded(true); chunks.put(key(x,z),chunk);
            }
        }
        static long key(int x,int z) { return ((long)x<<32) ^ (z&0xffffffffL); }
        protected IChunkProvider createChunkProvider() { return new IChunkProvider() {
            public Chunk getLoadedChunk(int x,int z) { return chunks.get(key(x,z)); }
            public Chunk provideChunk(int x,int z) { return getLoadedChunk(x,z); }
            public boolean tick() { return false; }
            public String makeString() { return "preloaded real concrete chunks; no generation"; }
            public boolean isChunkGeneratedAt(int x,int z) { return getLoadedChunk(x,z)!=null; }
        }; }
        protected boolean isChunkLoaded(int x,int z,boolean allowEmpty) { return chunks.containsKey(key(x,z)); }
        // Lighting propagation needs loaded surrounding terrain; explicitly outside this storage probe.
        public boolean checkLight(BlockPos pos) { return true; }
    }
 static void originalDropOracle(File legacy,Block[] blocks) throws Exception {
        try(URLClassLoader loader=new URLClassLoader(new URL[]{legacy.toURI().toURL()},CTStateTest.class.getClassLoader())) {
            Class<?> block=Class.forName("aji",true,loader), material=Class.forName("awt",true,loader);
            Constructor<?> constructor=block.getDeclaredConstructor(material); constructor.setAccessible(true);
            Object original=constructor.newInstance(material.getField("e").get(null)); // original ROCK
            Method damage=block.getMethod("a",int.class), quantity=block.getMethod("a",Random.class);
            Method resistanceSetter=block.getDeclaredMethod("b",float.class); resistanceSetter.setAccessible(true);
            Method resistance=block.getMethod("a",Class.forName("sa",false,loader));
            for(int index=0;index<blocks.length;index++) {
                resistanceSetter.invoke(original,10.0F);
                check(((Float)resistance.invoke(original,new Object[]{null}))==blocks[index].getExplosionResistance(null), "actual original resistance setter/scaling");
            }
            for(int meta=0;meta<16;meta++) for(Block nativeBlock:blocks) {
                check(((Integer)damage.invoke(original,meta))==nativeBlock.damageDropped(nativeBlock.getStateFromMeta(meta)), "independent1.7 bytecode damageDropped oracle");
                check(((Integer)quantity.invoke(original,new Random(meta)))==nativeBlock.quantityDropped(new Random(meta)), "independent1.7 bytecode quantity oracle");
            }
        }
        // getDamageValue in original Block delegates world metadata to the same damageDropped method.
        ClassNode cls=new ClassNode();
        try(java.util.jar.JarFile jar=new java.util.jar.JarFile(legacy);InputStream in=jar.getInputStream(jar.getJarEntry("aji.class"))) { new ClassReader(in).accept(cls,0); }
        boolean verified=false;
        for(Object obj:cls.methods) {
            MethodNode method=(MethodNode)obj;
            if(method.name.equals("k") && method.desc.equals("(Lahb;III)I")) {
                List<String> calls=new ArrayList<>();
                for(AbstractInsnNode ins=method.instructions.getFirst();ins!=null;ins=ins.getNext()) if(ins instanceof MethodInsnNode) {
                    MethodInsnNode call=(MethodInsnNode)ins; calls.add(call.owner+"."+call.name+call.desc);
                }
                check(calls.equals(Arrays.asList("ahb.e(III)I","aji.a(I)I")), "original getDamageValue reads metadata then damageDropped, not raw metadata");
                verified=true;
            }
        }
        check(verified,"original getDamageValue method found in pinned jar");
        System.out.println("PASS pinned original1.7.10 bytecode oracle: quantity1/dropdamage0 all16; original pick damage delegates metadata to same damageDropped.");
    }
 public static void main(String[] args)throws Exception{
 Bootstrap.register();FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());ModMetadata md=new ModMetadata();md.modId="hbm";md.name="CT palette storage proof";md.version="1";DummyModContainer owner=new DummyModContainer(md);Loader.instance().setupTestHarness(owner);Loader.instance().setActiveModContainer(owner);ConcreteConstructionSlice.initialize();
 Block[] blocks=Arrays.copyOfRange(ConcreteConstructionSlice.blocks(),6,11);String[] names={"deco_steel","deco_tungsten","deco_red_copper","deco_lead","deco_rusty_steel"};NativeWorld world=new NativeWorld();
 for(int i=0;i<blocks.length;i++){Block b=blocks[i];check(b.getClass()==com.hbm.blocks.generic.BlockDecoCT.class,"complete real CT owner");check(b.getTranslationKey().equals("tile."+names[i]),"exact name");check(b.getBlockHardness(b.getDefaultState(),null,BlockPos.ORIGIN)==5&&b.getExplosionResistance(null)==6,"exact hardness/resistance");check(b.getDefaultState().getMaterial()==net.minecraft.block.material.Material.IRON,"exact iron material");check(b.getCreativeTab()==com.hbm.main.MainRegistry.blockTab,"exact tab identity");check(!((com.hbm.blocks.generic.BlockDecoCT)b).allowFortune,"exact noFortune constructor");check(b.getRegistryName().equals(new ResourceLocation("hbm","tile."+names[i])),"native name from original registration route");check(b.getMetaFromState(b.getDefaultState())==0,"constructor default metadata0");ForgeRegistries.BLOCKS.register(b);ForgeRegistries.ITEMS.register(new ItemBlock(b).setRegistryName(b.getRegistryName()));check(b.getBlockState().getValidStates().size()==16,"native16 states");
 for(int meta=0;meta<16;meta++){IBlockState state=b.getStateFromMeta(meta);BlockPos p=new BlockPos(i+2,64+meta,3);check(b.getMetaFromState(state)==meta,"native metadata roundtrip");check(Block.getStateById(Block.getStateId(state))==state,"actual global state IDs");check(world.setBlockState(p,state,3),"actual native World/Chunk write");check(world.getBlockState(p)==state,"actual state identity");check(!b.hasTileEntity(state)&&b.createTileEntity(world,state)==null&&world.getTileEntity(p)==null,"no introduced tile lifecycle");NonNullList<ItemStack> drops=NonNullList.create();b.getDrops(drops,world,p,state,4);check(drops.size()==1&&drops.get(0).getItem()==Item.getItemFromBlock(b)&&drops.get(0).getCount()==1&&drops.get(0).getItemDamage()==0,"real native drop identity/count/damage");ItemStack pick=b.getPickBlock(state,null,world,p,null);check(pick.getItem()==Item.getItemFromBlock(b)&&pick.getItemDamage()==0,"real native pick");for(int fortune=0;fortune<5;fortune++){Random r=new Random(812),original=new Random(812);check(b.quantityDroppedWithBonus(fortune,r)==1&&r.nextLong()==original.nextLong(),"noFortune quantity and no RNG draw");}check(!world.setBlockState(p,state,3),"native same-state no-op");}
 }
 AnvilChunkLoader loader=new AnvilChunkLoader(new File("chunk-storage"),new DataFixer(1343));Method write=AnvilChunkLoader.class.getDeclaredMethod("writeChunkToNBT",Chunk.class,World.class,NBTTagCompound.class),read=AnvilChunkLoader.class.getDeclaredMethod("readChunkFromNBT",World.class,NBTTagCompound.class);write.setAccessible(true);read.setAccessible(true);NBTTagCompound saved=new NBTTagCompound();write.invoke(loader,world.getChunk(0,0),world,saved);ByteArrayOutputStream bytes=new ByteArrayOutputStream();CompressedStreamTools.writeCompressed(saved,bytes);NBTTagCompound restored=CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes.toByteArray()));NativeWorld fresh=new NativeWorld();Chunk loaded=(Chunk)read.invoke(loader,fresh,restored);fresh.chunks.put(NativeWorld.key(0,0),loaded);loaded.markLoaded(true);
 for(int i=0;i<blocks.length;i++)for(int meta=0;meta<16;meta++){BlockPos p=new BlockPos(i+2,64+meta,3);check(fresh.getBlockState(p)==blocks[i].getStateFromMeta(meta),"real compressed Anvil state identity");check(fresh.getTileEntity(p)==null,"no tile NBT");check(fresh.setBlockState(p,blocks[(i+1)%blocks.length].getStateFromMeta(meta),3),"native replacement");check(fresh.setBlockToAir(p)&&fresh.getTileEntity(p)==null,"native removal");}
 originalDropOracle(new File(args[0]),blocks);System.out.println("PASS "+checks+" actual CT owner/state/drop/pick/WorldChunk/Anvil assertions; no graphics acceptance change.");
 }
}