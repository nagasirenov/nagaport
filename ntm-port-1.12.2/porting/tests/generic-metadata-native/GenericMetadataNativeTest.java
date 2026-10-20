import com.hbm.blocks.BlockBase;
import com.hbm.blocks.generic.BlockGeneric;
import com.hbm.main.LegacyIconRegistrationHandler;
import com.hbm.render.icon.LegacyTextureAtlasSprite;
import com.hbm.render.model.LegacyBaseBakedModel;
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

/** Complete production BlockGeneric/Base and baked-model owners, not a replacement ModBlocks.
 * Standard native ItemBlock is an explicit isolated item boundary, not ItemBlockBlastInfo.
 * Native World/Chunk placement and Anvil serialization are real; worldgen/lighting/network are not.
 */
public final class GenericMetadataNativeTest {
    static int checks;
    static final String[] NAMES = {"brick_concrete", "brick_concrete_mossy", "brick_concrete_cracked", "brick_concrete_broken"};
    static void check(boolean ok, String label) { checks++; if (!ok) throw new AssertionError(label); }

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

    public static void main(String[] args) throws Exception {
        Bootstrap.register();
        FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
        ModMetadata metadata = new ModMetadata(); metadata.modId="hbm"; metadata.name="Concrete native owner probe"; metadata.version="1";
        DummyModContainer owner = new DummyModContainer(metadata);
        Loader.instance().setupTestHarness(owner); Loader.instance().setActiveModContainer(owner);
        ConcreteConstructionSlice.initialize();
        Block[] blocks = ConcreteConstructionSlice.blocks();
        for (int i=0;i<blocks.length;i++) {
            Block b = blocks[i];
            check(b.getClass()==BlockGeneric.class, "genuine complete production class");
            check(b.getRegistryName().equals(new ResourceLocation("hbm", "tile."+NAMES[i])), "exact original registry identity");
            check(b.getTranslationKey().equals("tile."+NAMES[i]), "exact translation identity");
            check(((BlockBase)b).getTextureName().equals("hbm:"+NAMES[i]), "explicit original texture");
            check(b.getBlockHardness(b.getDefaultState(),null,BlockPos.ORIGIN)==15.0F, "original hardness");
            check(b.getExplosionResistance(null)==new float[]{96,96,36,27}[i], "original resistance scaling");
            check(b.getCreativeTab()==com.hbm.main.MainRegistry.blockTab, "construction retains exact creative fixture object");
            ForgeRegistries.BLOCKS.register(b);
            // Explicit mechanics boundary. Production still registers ItemBlockBlastInfo, audited separately.
            ForgeRegistries.ITEMS.register(new ItemBlock(b).setRegistryName(b.getRegistryName()));
        }
        statesDropsAndStorage(blocks);
        originalDropOracle(new File(args[1]), blocks);
        if(args.length>2) archivedOwnerAndRandomOracle(new File(args[2]));
        randomSelection(blocks);
        atlasAndModels(new File(args[0]), blocks);
        System.out.println("PASS "+checks+" native assertions: exact four concrete constructions; metadata-zero state/storage/drop/pick; inherited nonzero collapse EXPLICIT; original RNG; actual atlas/GL and actual baked cubes.");
        System.out.println("NOT_RUN full ModBlocks/Library, production ItemBlockBlastInfo construction/tooltip, LegacyBaseModelHandler event integration, live gameplay/client/server/network, nonzero metadata parity.");
    }

    static void statesDropsAndStorage(Block[] blocks) throws Exception {
        NativeWorld world = new NativeWorld();
        Set<Integer> ids = new HashSet<>();
        for (int b=0;b<blocks.length;b++) {
            Block block=blocks[b]; IBlockState zero=block.getDefaultState();
            check(block.getBlockState().getValidStates().size()==1 && zero.getProperties().isEmpty(), "ordinary block has one no-property state");
            check(block.getStateFromMeta(0)==zero && block.getMetaFromState(zero)==0, "zero metadata lossless");
            check(ids.add(Block.getIdFromBlock(block)), "four distinct real registered identities");
            check(Block.getStateById(Block.getStateId(zero))==zero, "native global state-id roundtrip");
            Map<IBlockState,ModelResourceLocation> mapping = new DefaultStateMapper().putStateModelLocations(block);
            check(mapping.size()==1, "native state mapper has exactly one state");
            check(mapping.get(zero).equals(new ModelResourceLocation(block.getRegistryName(), "normal")), "metadata-zero normal model key");
            for(int meta=0;meta<16;meta++) {
                IBlockState decoded=block.getStateFromMeta(meta);
                check(decoded==zero && block.getMetaFromState(decoded)==0, "document existing nonzero collapse, never claim roundtrip for "+meta);
                check(!block.hasTileEntity(decoded) && block.createTileEntity(world,decoded)==null, "no invented tile entity");
                check(block.damageDropped(decoded)==0, "ordinary original drop damage zero");
                BlockPos p=new BlockPos(2+b,64+meta,3);
                check(world.setBlockState(p,decoded,3), "real World.setBlockState and Chunk placement");
                check(world.getBlockState(p)==zero && world.getChunk(p).getBlockState(p)==zero, "real world/chunk retrieves actual block identity");
                check(world.getTileEntity(p)==null && world.getChunk(p).getTileEntityMap().isEmpty(), "actual native placement creates no tile");
                NonNullList<ItemStack> drops=NonNullList.create();
                block.getDrops(drops,world,p,decoded,3);
                check(drops.size()==1 && drops.get(0).getCount()==1 && drops.get(0).getItem()==Item.getItemFromBlock(block) && drops.get(0).getItemDamage()==0, "actual block drops exactly one ordinary damage-zero item");
                ItemStack pick=block.getPickBlock(decoded,null,world,p,null);
                check(pick.getCount()==1 && pick.getItem()==Item.getItemFromBlock(block) && pick.getItemDamage()==0, "native pick preserves original damage zero");
                check(((ItemBlock)pick.getItem()).getMetadata(meta)==0, "explicit native non-subtype item boundary");
                check(!world.setBlockState(p,block.getStateFromMeta((meta+1)&15),2), "equivalent state is genuine native no-op, no fake metadata update");
            }
        }
        System.out.println("PASS all 16 inputs per actual concrete block: zero roundtrip; inputs1..15 collapse to0 and are NOT selected for migration.");
        // Run actual Anvil writer, NBT compressor, NBT reader and Anvil chunk decoder.
        // This avoids pretending a serialized state string is native chunk persistence.
        AnvilChunkLoader loader=new AnvilChunkLoader(new File("chunk-storage"),new DataFixer(1343));
        Method write=AnvilChunkLoader.class.getDeclaredMethod("writeChunkToNBT",Chunk.class,World.class,NBTTagCompound.class);
        Method read=AnvilChunkLoader.class.getDeclaredMethod("readChunkFromNBT",World.class,NBTTagCompound.class);
        write.setAccessible(true); read.setAccessible(true);
        NBTTagCompound saved=new NBTTagCompound();
        write.invoke(loader,world.getChunk(0,0),world,saved);
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); CompressedStreamTools.writeCompressed(saved,bytes);
        NBTTagCompound restored=CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes.toByteArray()));
        NativeWorld fresh=new NativeWorld(); Chunk loaded=(Chunk)read.invoke(loader,fresh,restored);
        fresh.chunks.put(NativeWorld.key(0,0),loaded); loaded.markLoaded(true);
        check(restored.getTagList("Sections",10).tagCount()>0, "actual section list persisted");
        check(restored.getTagList("TileEntities",10).tagCount()==0 && loaded.getTileEntityMap().isEmpty(), "no tile NBT invented");
        for(int b=0;b<blocks.length;b++) for(int meta=0;meta<16;meta++) {
            BlockPos p=new BlockPos(2+b,64+meta,3);
            check(fresh.getBlockState(p)==blocks[b].getDefaultState(), "native Anvil/NBT roundtrip identity");
            check(blocks[b].getMetaFromState(fresh.getBlockState(p))==0 && fresh.getTileEntity(p)==null, "native Anvil roundtrip zero/no tile");
        }
        for(int b=0;b<blocks.length;b++) {
            BlockPos p=new BlockPos(2+b,64,3);
            check(fresh.setBlockState(p,blocks[(b+1)%4].getDefaultState(),3), "native concrete owner replacement");
            check(fresh.getTileEntity(p)==null, "replacement has no tile refresh behavior to invent");
            check(fresh.setBlockToAir(p) && fresh.getTileEntity(p)==null, "native removal leaves no tile");
        }
        System.out.println("PASS native World/Chunk writes and actual Anvil write/read + compressed NBT persistence for64 concrete positions; no tile entity lifecycle invented.");
    }

    static void originalDropOracle(File legacy,Block[] blocks) throws Exception {
        try(URLClassLoader loader=new URLClassLoader(new URL[]{legacy.toURI().toURL()},GenericMetadataNativeTest.class.getClassLoader())) {
            Class<?> block=Class.forName("aji",true,loader), material=Class.forName("awt",true,loader);
            Constructor<?> constructor=block.getDeclaredConstructor(material); constructor.setAccessible(true);
            Object original=constructor.newInstance(material.getField("e").get(null)); // original ROCK
            Method damage=block.getMethod("a",int.class), quantity=block.getMethod("a",Random.class);
            Method resistanceSetter=block.getDeclaredMethod("b",float.class); resistanceSetter.setAccessible(true);
            Method resistance=block.getMethod("a",Class.forName("sa",false,loader));
            for(int index=0;index<blocks.length;index++) {
                resistanceSetter.invoke(original,new float[]{160,160,60,45}[index]);
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

    static ClassNode readClass(InputStream input) throws IOException {
        try(InputStream in=input) { ClassNode node=new ClassNode(); new ClassReader(in).accept(node,0); return node; }
    }
    static MethodNode randomMethod(ClassNode cls) {
        for(Object obj:cls.methods) { MethodNode method=(MethodNode)obj; if(method.name.equals("getRandomConcrete")) return method; }
        throw new AssertionError("archived/current getRandomConcrete method missing");
    }
    static String fixtureOwner(String owner) {
        return owner.replace("com/hbm/lib/Library","ConcreteRandomSlice").replace("com/hbm/blocks/ModBlocks","ConcreteConstructionSlice");
    }
    static List<String> fingerprint(MethodNode method) {
        Map<LabelNode,Integer> labels=new IdentityHashMap<>(); int position=0;
        for(AbstractInsnNode ins=method.instructions.getFirst();ins!=null;ins=ins.getNext()) {
            if(ins instanceof LabelNode) labels.put((LabelNode)ins,position);
            if(ins.getOpcode()>=0) position++;
        }
        List<String> result=new ArrayList<>();
        for(AbstractInsnNode ins=method.instructions.getFirst();ins!=null;ins=ins.getNext()) {
            if(ins.getOpcode()<0) continue;
            String detail="";
            if(ins instanceof FieldInsnNode) { FieldInsnNode f=(FieldInsnNode)ins; detail=fixtureOwner(f.owner)+"."+f.name+f.desc; }
            else if(ins instanceof MethodInsnNode) { MethodInsnNode m=(MethodInsnNode)ins; detail=fixtureOwner(m.owner)+"."+m.name+m.desc; }
            else if(ins instanceof VarInsnNode) detail="v"+((VarInsnNode)ins).var;
            else if(ins instanceof IntInsnNode) detail="i"+((IntInsnNode)ins).operand;
            else if(ins instanceof JumpInsnNode) detail="j"+labels.get(((JumpInsnNode)ins).label);
            else check(ins instanceof InsnNode,"RNG fingerprint covers every actual opcode kind");
            result.add(ins.getOpcode()+":"+detail);
        }
        return result;
    }
    static void archivedOwnerAndRandomOracle(File originalJar) throws Exception {
        try(java.util.jar.JarFile jar=new java.util.jar.JarFile(originalJar)) {
            ClassNode original=readClass(jar.getInputStream(jar.getJarEntry("com/hbm/lib/Library.class")));
            ClassNode generated=readClass(GenericMetadataNativeTest.class.getClassLoader().getResourceAsStream("ConcreteRandomSlice.class"));
            check(fingerprint(randomMethod(original)).equals(fingerprint(randomMethod(generated))), "actual archived1.7 Library RNG bytecode identical except named fixture owners/debug labels");
            ClassNode generic=readClass(jar.getInputStream(jar.getJarEntry("com/hbm/blocks/generic/BlockGeneric.class")));
            check(generic.superName.equals("com/hbm/blocks/BlockBase"), "archived genuine generic parent");
            for(Object obj:generic.methods) check(((MethodNode)obj).name.equals("<init>"), "archived generic has only constructor, not metadata/drop overrides");
            ClassNode base=readClass(jar.getInputStream(jar.getJarEntry("com/hbm/blocks/BlockBase.class")));
            check(base.superName.equals("net/minecraft/block/Block"), "archived genuine base parent");
            Set<String> expected=new HashSet<>(Arrays.asList("<init>","func_149663_c","setBeaconable","noMobSpawn","canCreatureSpawn","isBeaconBase","dismantle"));
            for(Object obj:base.methods) check(expected.contains(((MethodNode)obj).name), "archived base declares no unexpected drop/pick/tile override");
        }
        System.out.println("PASS archived HBM bytecode: original Generic/Base inheritance and entire original RNG instruction stream match, only explicit slice-owner names normalized.");
    }

    static void randomSelection(Block[] blocks) {
        int[] counts=new int[4]; boolean[] outcomes=new boolean[20];
        for(long seed=0;seed<200;seed++) {
            ConcreteRandomSlice.seed(seed); Random original=new Random(seed);
            for(int n=0;n<100;n++) {
                int value=original.nextInt(20); outcomes[value]=true;
                int index=value<=1?3:value<=4?2:value<=10?1:0;
                Block selected=ConcreteRandomSlice.getRandomConcrete();
                check(selected==blocks[index], "exact RNG branch selects genuine registered owner"); counts[index]++;
            }
            check(original.nextLong()==ConcreteRandomSlice.nextLong(), "exact original RNG consumption once per selection");
        }
        for(boolean hit:outcomes) check(hit,"all20 nextInt outcomes covered");
        for(int count:counts) check(count>0,"every genuine concrete owner selected");
        System.out.println("PASS exact Library method slice20000 selections, all20 branches, post-state equality200 seeds; only static field-owner qualifier changes.");
    }

    static void atlasAndModels(File resourceRoot,Block[] blocks) throws Exception {
        MinecraftForge.EVENT_BUS.register(new LegacyIconRegistrationHandler());
        SimpleReloadableResourceManager resources=new SimpleReloadableResourceManager(new MetadataSerializer());
        resources.reloadResourcePack(new FolderResourcePack(resourceRoot));
        try(NativeAtlasEnvironment.Context context=new NativeAtlasEnvironment.Context()) {
            NativeAtlasEnvironment.initializeNativeClientSettings();
            URL location=TextureMap.class.getProtectionDomain().getCodeSource().getLocation();
            if(location.getProtocol().equals("jar")) location=((JarURLConnection)location.openConnection()).getJarFileURL();
            net.minecraftforge.fml.common.asm.FMLSanityChecker.fmlLocation=new File(location.toURI());
            TextureMap map=new TextureMap("textures"); map.setMipmapLevels(2);
            Object[] previous=new Object[4];
            for(int reload=0;reload<2;reload++) {
                MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.TextureStitchEvent.Pre(map));
                Method init=TextureMap.class.getDeclaredMethod("initMissingImage"); init.setAccessible(true); init.invoke(map);
                map.loadTextureAtlas(resources);
                GlStateManager.bindTexture(map.getGlTextureId());
                int width=GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D,0,GL11.GL_TEXTURE_WIDTH);
                int height=GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D,0,GL11.GL_TEXTURE_HEIGHT);
                IntBuffer pixels=BufferUtils.createIntBuffer(width*height); GL11.glGetTexImage(GL11.GL_TEXTURE_2D,0,32993,33639,pixels);
                for(int b=0;b<blocks.length;b++) {
                    BlockBase block=(BlockBase)blocks[b];
                    TextureAtlasSprite sprite=((LegacyTextureAtlasSprite)block.getIcon(0,0)).getSprite();
                    check(sprite==map.getAtlasSprite("hbm:blocks/"+NAMES[b]) && sprite!=map.getMissingSprite(), "real atlas concrete texture without missing fallback");
                    check(block.getIcon(0,0)!=previous[b], "fresh icon wrapper on reload, native sprite may be reused"); previous[b]=block.getIcon(0,0);
                    BufferedImage png=ImageIO.read(new File(resourceRoot,"assets/hbm/textures/blocks/"+NAMES[b]+".png"));
                    check(sprite.getIconWidth()==png.getWidth() && sprite.getIconHeight()==png.getHeight(), "original concrete image dimensions");
                    for(int y=0;y<png.getHeight();y++) for(int x=0;x<png.getWidth();x++) check(pixels.get((sprite.getOriginY()+y)*width+sprite.getOriginX()+x)==png.getRGB(x,y), "actual original concrete texel uploaded");
                    LegacyBaseBakedModel world=LegacyBaseBakedModel.forBlock(block);
                    LegacyBaseBakedModel item=LegacyBaseBakedModel.forBlockItem(block,Item.getItemFromBlock(block),0);
                    check(world.getBlock()==block && world.getParticleTexture()==sprite && world.isGui3d() && world.isAmbientOcclusion() && !world.isBuiltInRenderer(), "actual production cube model properties");
                    check(world.getQuads(block.getDefaultState(),null,0).isEmpty(), "world cube uses culled face lists");
                    check(item.getQuads(null,null,0).size()==6, "actual inventory cube has six faces");
                    for(EnumFacing face:EnumFacing.values()) {
                        List<BakedQuad> quads=world.getQuads(block.getDefaultState(),face,0);
                        check(quads.size()==1 && quads.get(0).getFace()==face && quads.get(0).getSprite()==sprite, "actual FaceBakery native cube face/sprite");
                        check(quads.get(0).getVertexData().length==28, "actual vanilla packed quad layout");
                        for(int vertex=0;vertex<4;vertex++) for(int axis=0;axis<3;axis++) {
                            float coordinate=Float.intBitsToFloat(quads.get(0).getVertexData()[vertex*7+axis]);
                            check(coordinate==0.0F || coordinate==1.0F, "actual full cube original unit bounds");
                        }
                    }
                    for(int meta=0;meta<16;meta++) for(int face=0;face<6;face++) check(((LegacyTextureAtlasSprite)block.getIcon(face,meta)).getSprite()==sprite, "original uniform side/meta icon unaffected");
                }
                check(GL11.glGetError()==GL11.GL_NO_ERROR,"actual atlas/model path has no GL error");
                System.out.println("PASS concrete atlas reload"+reload+": real production stitch callback, PNG/mipmap/GL pixel readback and actual LegacyBaseBakedModel cube faces.");
                resources.reloadResourcePack(new FolderResourcePack(resourceRoot));
            }
            map.deleteGlTexture();
        }
    }
}
