import com.hbm.blocks.*;
import com.hbm.items.block.ItemBlockBase;
import com.hbm.render.icon.*;
import com.hbm.render.model.LegacyBaseBakedModel;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.*;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.chunk.*;
import net.minecraft.world.chunk.storage.*;
import net.minecraft.world.storage.*;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** Real owners, native state/registry/world/chunk/Anvil/drop/item/baked-model mechanics.
 * No full startup, terrain lighting, disk region IO, atlas upload, or tile-provider claim. */
public final class BlockMultiStateNativeTest {
    static int checks;
    static void check(boolean ok, String label) { checks++; if(!ok) throw new AssertionError(label); }
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
        ModMetadata metadata = new ModMetadata(); metadata.modId="hbm"; metadata.name="BlockMulti native state probe"; metadata.version="1";
        DummyModContainer owner = new DummyModContainer(metadata);
        Loader.instance().setupTestHarness(owner); Loader.instance().setActiveModContainer(owner);
        MultiConstructionSlice.initialize();
        Block[] blocks=MultiConstructionSlice.blocks();
        String[] names={"ore_meteor","concrete_colored_ext","lightstone","vinyl_tile","cm_block","icf_laser_component"};
        Class<?>[] types={com.hbm.blocks.generic.BlockMeteorOre.class,com.hbm.blocks.generic.BlockConcreteColoredExt.class,
            com.hbm.blocks.generic.BlockLightstone.class,BlockEnumMulti.class,com.hbm.blocks.machine.BlockCM.class,
            com.hbm.blocks.machine.BlockICFLaserComponent.class};
        float[] hardness={5,15,2,10,5,5}, resistance={6,84,9,36,6,36};
        for(int i=0;i<blocks.length;i++) {
            Block b=blocks[i];
            check(b.getClass()==types[i],"complete production constructor and actual class");
            check(b.getRegistryName().equals(new ResourceLocation("hbm","tile."+names[i])),"verbatim registry constructor identity");
            check(b.getBlockHardness(b.getDefaultState(),null,BlockPos.ORIGIN)==hardness[i],"original hardness");
            check(b.getExplosionResistance(null)==resistance[i],"native original resistance scaling");
            ForgeRegistries.BLOCKS.register(b);
            ForgeRegistries.ITEMS.register(new ItemBlockBase(b).setRegistryName(b.getRegistryName()));
        }
        unchangedStateOwners();
        statesDropsPlacementAndStorage(blocks);
        models(blocks);
        System.out.println("PASS "+checks+" native assertions: six verbatim constructions, all sixteen metadata states, real ItemBlockBase placement, drops/picks, World/Chunk/Anvil persistence, actual metadata-selected baked faces.");
        System.out.println("NOT_RUN full startup/build, LegacyBaseModelHandler events, texture resource upload, inherited legacy callbacks, tile-provider descendant lifecycle, specialized state-owner runtime except actual toaster, server/network/gameplay.");
    }

    static void unchangedStateOwners() {
        Block base=new BlockBase(net.minecraft.block.material.Material.ROCK);
        Block generic=new com.hbm.blocks.generic.BlockGeneric(net.minecraft.block.material.Material.ROCK);
        for(Block b:new Block[]{base,generic}) {
            check(b.getBlockState().getValidStates().size()==1 && b.getDefaultState().getProperties().isEmpty(),"non-Multi base/generic remains unchanged propertyless");
            for(int meta=0;meta<16;meta++) check(b.getMetaFromState(b.getStateFromMeta(meta))==0,"non-Multi base does not gain invented metadata contract");
        }
        com.hbm.blocks.generic.BlockDecoToaster toaster=new com.hbm.blocks.generic.BlockDecoToaster(net.minecraft.block.material.Material.IRON);
        check(toaster.getDefaultState().getPropertyKeys().contains(com.hbm.blocks.generic.BlockDecoToaster.META),"actual specialized descendant constructor retains its own property");
        check(toaster.getBlockState().getProperty("metadata") == null,"new shared property is not imposed on specialized state container");
        for(int meta=0;meta<16;meta++) {
            IBlockState state=toaster.getStateFromMeta(meta);
            check(toaster.getMetaFromState(state)==meta,"actual existing specialized state codec unchanged");
            check(toaster.damageDropped(state)==(meta%12)/4,"actual specialized variant/yaw drop override remains authoritative");
            check(toaster.getBoundingBox(state,null,BlockPos.ORIGIN).equals(toaster.getBoundingBox(toaster.getStateFromMeta(meta&1),null,BlockPos.ORIGIN)),"specialized orientation remains authoritative");
        }
    }

    static void statesDropsPlacementAndStorage(Block[] blocks) throws Exception {
        NativeWorld world=new NativeWorld();
        for(int b=0;b<blocks.length;b++) {
            BlockMulti block=(BlockMulti)blocks[b];
            ItemBlockBase item=(ItemBlockBase)Item.getItemFromBlock(block);
            check(block.getBlockState().getValidStates().size()==16,"genuine sixteen-state container");
            check(block.getMetaFromState(block.getDefaultState())==0,"constructor base state zero, no virtual subtype access before enum init");
            check(block.getDefaultState().getProperties().size()==1 && block.getBlockState().getProperty("metadata") instanceof net.minecraft.block.properties.PropertyInteger,"only owned metadata property");
            check(item.getHasSubtypes() && item.getMaxDamage()==0,"actual ItemBlockBase subtype configuration");
            NonNullList<ItemStack> creative=NonNullList.create(); block.getSubBlocks(block.getCreativeTab(),creative);
            check(creative.size()==block.getSubCount(),"native creative list remains subtype count rather than sixteen raw states");
            for(int i=0;i<creative.size();i++) check(creative.get(i).getMetadata()==i && creative.get(i).getItem()==item,"creative item metadata");
            for(int input=-32;input<48;input++) check(block.getMetaFromState(block.getStateFromMeta(input))==(input&15),"four-bit codec does not rectify subtype or lose flag bits");
            OriginalMultiOracle oracle=new OriginalMultiOracle(block.getSubCount());
            for(int meta=0;meta<16;meta++) {
                IBlockState state=block.getStateFromMeta(meta);
                check(block.getMetaFromState(state)==meta,"all sixteen values roundtrip");
                check(Block.getStateById(Block.getStateId(state))==state,"native global state ID roundtrip");
                check(block.damageDropped(state)==oracle.damageDropped(meta),"verbatim pristine drop/rectify oracle");
                check(item.getMetadata(meta)==meta,"real ItemBlockBase forwards all metadata");
                BlockPos pos=new BlockPos(2+b,64+meta,3);
                IBlockState placed=block.getStateForPlacement(world,pos,EnumFacing.UP,0.5F,0.5F,0.5F,item.getMetadata(meta),null,EnumHand.MAIN_HAND);
                check(placed==state,"native placement callback preserves item metadata");
                check(item.placeBlockAt(new ItemStack(item,1,meta),null,world,pos,EnumFacing.UP,0.5F,0.5F,0.5F,placed),"real native ItemBlock.placeBlockAt");
                check(world.getBlockState(pos)==state && world.getChunk(pos).getBlockState(pos)==state,"actual World/Chunk retain raw metadata");
                check(!world.setBlockState(pos,state,2),"same native state no-op");
                check(world.setBlockState(pos,block.getStateFromMeta((meta+1)&15),2),"different metadata actual state update");
                check(world.setBlockState(pos,state,2),"restore exact original state");
                check(!block.hasTileEntity(state) && world.getTileEntity(pos)==null,"selected actual owners create no tile");
                NonNullList<ItemStack> drops=NonNullList.create(); block.getDrops(drops,world,pos,state,0);
                check(drops.size()==1 && drops.get(0).getItem()==item && drops.get(0).getCount()==1 && drops.get(0).getMetadata()==oracle.damageDropped(meta),"real native drop calls actual BlockMulti state damage bridge");
                ItemStack pick=block.getPickBlock(state,null,world,pos,null);
                check(pick.getItem()==item && pick.getCount()==1 && pick.getMetadata()==oracle.damageDropped(meta),"real pick damage retains original rectification");
            }
        }
        AnvilChunkLoader loader=new AnvilChunkLoader(new File("chunk-storage"),new DataFixer(1343));
        Method write=AnvilChunkLoader.class.getDeclaredMethod("writeChunkToNBT",Chunk.class,World.class,NBTTagCompound.class);
        Method read=AnvilChunkLoader.class.getDeclaredMethod("readChunkFromNBT",World.class,NBTTagCompound.class);
        write.setAccessible(true); read.setAccessible(true);
        NBTTagCompound saved=new NBTTagCompound(); write.invoke(loader,world.getChunk(0,0),world,saved);
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); CompressedStreamTools.writeCompressed(saved,bytes);
        NBTTagCompound restored=CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes.toByteArray()));
        NativeWorld fresh=new NativeWorld(); Chunk loaded=(Chunk)read.invoke(loader,fresh,restored);
        fresh.chunks.put(NativeWorld.key(0,0),loaded); loaded.markLoaded(true);
        check(restored.getTagList("Sections",10).tagCount()>0,"real native Anvil section encoding");
        check(restored.getTagList("TileEntities",10).tagCount()==0,"no invented tile persistence");
        for(int b=0;b<blocks.length;b++) for(int meta=0;meta<16;meta++) {
            BlockPos pos=new BlockPos(2+b,64+meta,3);
            check(fresh.getBlockState(pos)==blocks[b].getStateFromMeta(meta),"actual Anvil/NBT preserves every raw metadata and identity");
            check(fresh.setBlockToAir(pos) && fresh.getBlockState(pos).getBlock()==net.minecraft.init.Blocks.AIR,"actual removal after reload");
        }
        check(loaded.getTileEntityMap().isEmpty(),"no tile lifecycle invented for selected owners");
    }

    static void models(Block[] blocks) {
        TextureMap atlas=new TextureMap("textures");
        LegacyIconRegister register=new LegacyIconRegister(atlas);
        for(Block b:blocks) ((BlockBase)b).registerBlockIcons(register);
        Set<TextureAtlasSprite> sprites=Collections.newSetFromMap(new IdentityHashMap<TextureAtlasSprite,Boolean>());
        for(Block b:blocks) for(int meta=0;meta<16;meta++) for(EnumFacing side:EnumFacing.values()) {
            TextureAtlasSprite sprite=((LegacyTextureAtlasSprite)((BlockBase)b).getIcon(side.getIndex(),meta)).getSprite();
            if(sprites.add(sprite)) { sprite.setIconWidth(16); sprite.setIconHeight(16); sprite.initSprite(256,256,0,0,false); }
        }
        for(Block b:blocks) {
            BlockMulti block=(BlockMulti)b;
            Map<IBlockState,ModelResourceLocation> mapping=new DefaultStateMapper().putStateModelLocations(block);
            check(mapping.size()==16,"native mapper covers all sixteen states");
            LegacyBaseBakedModel model=LegacyBaseBakedModel.forBlock(block);
            for(int meta=0;meta<16;meta++) {
                IBlockState state=block.getStateFromMeta(meta);
                check(mapping.get(state).equals(new ModelResourceLocation(block.getRegistryName(),"metadata="+meta)),"native metadata model key");
                check(model.getQuads(state,null,0).isEmpty(),"block faces remain culled by side");
                for(EnumFacing side:EnumFacing.values()) {
                    List<BakedQuad> quads=model.getQuads(state,side,0);
                    check(quads.size()==1 && quads.get(0).getFace()==side,"real FaceBakery face orientation");
                    TextureAtlasSprite expected=((LegacyTextureAtlasSprite)block.getIcon(side.getIndex(),meta)).getSprite();
                    check(quads.get(0).getSprite()==expected,"actual model selects original polymorphic icon with nonzero raw metadata");
                    check(model.getQuads(state,side,123).get(0)==quads.get(0),"metadata-keyed quad cache stable");
                }
            }
            for(int meta=0;meta<block.getSubCount();meta++) {
                LegacyBaseBakedModel item=LegacyBaseBakedModel.forBlockItem(block,Item.getItemFromBlock(block),meta);
                List<BakedQuad> quads=item.getQuads(null,null,0);
                check(quads.size()==6,"actual inventory cube has six faces");
                for(BakedQuad quad:quads) check(quad.getSprite()==((LegacyTextureAtlasSprite)block.getIcon(quad.getFace().getIndex(),meta)).getSprite(),"inventory subtype selects actual icon");
            }
        }
        BlockBase colored=(BlockBase)blocks[1];
        check(colored.getIcon(0,1)==colored.getIcon(0,0) && colored.getIcon(2,1)!=colored.getIcon(2,0),"original striped concrete top/bottom versus side split");
        BlockBase light=(BlockBase)blocks[2];
        check(light.getIcon(0,3)!=light.getIcon(2,3),"original lightstone distinct top variant");
        BlockBase icf=(BlockBase)blocks[5];
        check(icf.getIcon(0,4)==icf.getIcon(0,5) && icf.getIcon(2,4)!=icf.getIcon(2,5),"original ICF shared capacitor top and distinct side variants");
    }
}
