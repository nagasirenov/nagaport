import java.io.File;
import java.lang.reflect.*;
import java.net.*;
import java.nio.IntBuffer;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.RegistryManager;
import com.hbm.blocks.machine.*;
import com.hbm.main.*;
import com.hbm.registry.GameRegistry;
import com.hbm.render.icon.*;

/** Unchanged platform classes, real registry/model/atlas/GL lifecycle, not a game. */
public final class PillarNativeTest {
    static int checks;
    static void check(boolean value, String label) { checks++; if(!value) throw new AssertionError(label); }
    static Field field(Class<?> type, String name) throws Exception { Field f=type.getDeclaredField(name); f.setAccessible(true); return f; }
    static File sourceJar(Class<?> type) throws Exception {
        URL u=type.getProtectionDomain().getCodeSource().getLocation();
        if(u.getProtocol().equals("jar")) u=((JarURLConnection)u.openConnection()).getJarFileURL();
        return new File(u.toURI());
    }
    static String atlas(String legacy) { ResourceLocation r=new ResourceLocation(legacy); return r.getNamespace()+":blocks/"+r.getPath(); }
    static TextureAtlasSprite sprite(IIcon icon) { return ((LegacyTextureAtlasSprite)icon).getSprite(); }
    static final int[] METADATA={Integer.MIN_VALUE,-32769,-32768,-1,0,1,15,16,32767,32768,65535,65536,Integer.MAX_VALUE};

    static void ownerSemantics() throws Exception {
        BlockPillar defaults=new BlockPillar(Material.ROCK, "");
        String first=new String("hbm:gneiss_tile"), later=new String("hbm:basalt");
        check(defaults.setBlockTextureName(first)==defaults,"setter exact Block identity");
        check(defaults.getTextureName(0)==first && defaults.getAlternateTextureName()==first,"empty constructor slots take exact first String reference");
        check(defaults.setBlockTextureName(later)==defaults && defaults.getTextureName(2)==later,"last side texture wins without early parsing");
        check(defaults.getTextureName(0)==first && defaults.getAlternateTextureName()==first,"nonempty top/alt survive later side setter");
        BlockPillar explicit=new BlockPillar(Material.ROCK,"hbm:gneiss_tile","hbm:basalt_top");
        explicit.setBlockTextureName("hbm:gneiss_chiseled");
        TextureMap map=new TextureMap("textures");
        LegacyIconRegister register=new LegacyIconRegister(map);
        List<String> calls=new ArrayList<>();
        explicit.registerBlockIcons(name->{calls.add(name);return register.registerIcon(name);});
        check(calls.equals(Arrays.asList("hbm:blocks/basalt_top","hbm:blocks/gneiss_tile","hbm:blocks/gneiss_chiseled")),"original alternate/top/side callback order and native atlas root");
        IIcon alternate=(IIcon)field(BlockPillar.class,"iconAlt").get(explicit);
        check(alternate!=explicit.getIcon(0,0),"explicit alternate is registered but NOT selected as bottom");
        for(int metadata:METADATA) for(int side=0;side<6;side++)
            check(explicit.getIcon(side,metadata).getIconName().equals(side<2?"hbm:blocks/gneiss_tile":"hbm:blocks/gneiss_chiseled"),"original all-raw-metadata face selection");
        // Original empty-alt fallback remains the original hbm:code, never a new default.
        BlockPillar fallback=new BlockPillar(Material.ROCK,"hbm:gneiss_tile");
        field(BlockPillar.class,"textureName").set(fallback,"hbm:gneiss_chiseled");
        calls.clear();fallback.registerBlockIcons(name->{calls.add(name);return register.registerIcon(name);});
        check(calls.get(0).equals("hbm:blocks/code"),"original code fallback only for unfilled alternate");
        BlockPillar deferred=new BlockPillar(Material.ROCK,"hbm:gneiss_tile","hbm:basalt_top");
        check(deferred.setBlockTextureName(null)==deferred && field(BlockPillar.class,"textureName").get(deferred)==null,"original null stored until resource use");
        check(BlockPillar.class.getSuperclass()==Block.class,"no BlockBase inheritance substitution");
        check(Modifier.isProtected(field(BlockPillar.class,"textureName").getModifiers()) && Modifier.isProtected(field(BlockPillar.class,"blockIcon").getModifiers()),"original inherited field visibility remains protected");
        accessprobe.PillarAccess access=new accessprobe.PillarAccess();
        access.write("hbm:gneiss_chiseled",explicit.getIcon(2,0));
        check(access.readTexture().equals("hbm:gneiss_chiseled") && access.readIcon()==explicit.getIcon(2,0),"genuine native cross-package descendant protected read/write access");
        for(BlockPillar descendant:new BlockPillar[]{new BlockCMFlux(Material.ROCK,"hbm:gneiss_tile"),new BlockCMHeat(Material.ROCK,"hbm:gneiss_tile")}) {
            check(descendant.setBlockTextureName("hbm:gneiss_chiseled")==descendant,"unchanged complete descendant inherited setter identity");
            descendant.registerBlockIcons(register);
            check(descendant.getIcon(0,0).getIconName().equals("hbm:blocks/gneiss_tile"),"unchanged complete descendant inherited callback selection");
        }
        check(BlockPillarPWR.class.getSuperclass()==BlockPillar.class,"PWR retains real owner hierarchy");
    }

    public static void main(String[] args) throws Exception {
        Bootstrap.register();
        FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
        ModMetadata md=new ModMetadata();md.modId="hbm";md.name="Native pillar probe";md.version="1";
        DummyModContainer owner=new DummyModContainer(md);Loader.instance().setupTestHarness(owner);Loader.instance().setActiveModContainer(owner);
        Block[] blocks=PillarConstruction.create();check(blocks.length==11,"eleven actual production construction chains");
        float[] hardness={1.5F,5F,15F,-1F,15F,5F,5F,5F,5F,5F,5F};
        float[] resistance={6F,6F,360F,399999.6F,216F,6F,6F,6F,6F,6F,6F};
        for(int i=0;i<blocks.length;i++) {
            BlockPillar block=(BlockPillar)blocks[i];
            check(block.getClass().getSimpleName().equals(PillarConstruction.owners[i]),"exact production owner");
            check(block.getTranslationKey().equals("tile."+PillarConstruction.names[i]),"translation identity");
            check(block.getRegistryName()==null,"no premature registry name from texture setter");
            check(block.getTextureName(0).equals(PillarConstruction.tops[i]) && block.getTextureName(2).equals(PillarConstruction.sides[i]),"constructor/side texture precedence");
            check(block.getAlternateTextureName().equals(PillarConstruction.sides[i]),"actual default alternate captures original side");
            check(block.getBlockHardness(block.getDefaultState(),null,BlockPos.ORIGIN)==hardness[i],"hardness retained");
            check(Math.abs(block.getExplosionResistance(null)-resistance[i])<0.05F,"native resistance scaling retained");
            check(block.getSoundType()==SoundType.STONE,"original inherited stone sound");
            check(block.getCreativeTab()==(i==3||i==8?null:i<5?MainRegistry.blockTab:MainRegistry.machineTab),"unchanged creative tab selection");
            check(block.getBlockState().getValidStates().size()==16 && block.isFullCube(block.getDefaultState()),"original sixteen metadata values and full-cube bounds");
        }
        for(Block block:blocks) {
            for(int meta=0;meta<16;meta++)check(block.getMetaFromState(block.getStateFromMeta(meta))==meta,"all original nibble values represented without collapse");
            for(int raw:METADATA)check(block.getMetaFromState(block.getStateFromMeta(raw))==(raw&15),"native owner retains original nibble masking for arbitrary integers");
            check(block.getMetaFromState(block.getDefaultState())==0,"original default metadata zero");
        }
        GameRegistry.beginRegistrationTables();PillarConstruction.register();GameRegistry.finishRegistrationTables();
        MinecraftForge.EVENT_BUS.post(new RegistryEvent.Register<Block>(RegistryManager.ACTIVE.getName(ForgeRegistries.BLOCKS),ForgeRegistries.BLOCKS));
        for(Block b:blocks) check(Item.getItemFromBlock(b)==Items.AIR,"block event cannot publish late ItemBlock early");
        for(Block block:blocks) {
            net.minecraft.world.chunk.storage.ExtendedBlockStorage storage=new net.minecraft.world.chunk.storage.ExtendedBlockStorage(0,false);
            for(int meta=0;meta<16;meta++) {
                IBlockState state=block.getStateFromMeta(meta);storage.set(meta,0,0,state);
                check(storage.get(meta,0,0)==state,"real native chunk storage retains each owner state");
                net.minecraft.nbt.NBTTagCompound tag=net.minecraft.nbt.NBTUtil.writeBlockState(new net.minecraft.nbt.NBTTagCompound(),state);
                check(net.minecraft.nbt.NBTUtil.readBlockState(tag)==state,"real native named-state NBT round trip");
                check(block.damageDropped(state)==0,"unchanged inherited drop metadata despite retained world metadata");
            }
        }
        try(NativeAtlasEnvironment.Context context=new NativeAtlasEnvironment.Context()) {
            NativeAtlasEnvironment.initializeNativeClientSettings();ownerSemantics();
            SimpleReloadableResourceManager resources=new SimpleReloadableResourceManager(new MetadataSerializer());
            field(Minecraft.class,"resourceManager").set(Minecraft.getMinecraft(),resources);
            net.minecraftforge.fml.common.asm.FMLSanityChecker.fmlLocation=sourceJar(TextureMap.class);
            List<IResourcePack> packs=Arrays.asList(new FileResourcePack(sourceJar(TextureMap.class)),new FolderResourcePack(new File(args[0])));
            resources.reloadResources(packs);
            TextureMap map=new TextureMap("textures");map.setMipmapLevels(2);
            net.minecraft.launchwrapper.Launch.blackboard.put("fml.deobfuscatedEnvironment",Boolean.TRUE);
            ModelManager manager=new ModelManager(map);BlockModelShapes shapes=new BlockModelShapes(manager);new ModelLoader(resources,map,shapes);
            boolean missing=false;try{PillarModelHandler.register(new ModelRegistryEvent());}catch(IllegalStateException expected){missing=true;}
            check(missing,"missing ItemBlock fails explicitly rather than mapping AIR");
            MinecraftForge.EVENT_BUS.post(new RegistryEvent.Register<Item>(RegistryManager.ACTIVE.getName(ForgeRegistries.ITEMS),ForgeRegistries.ITEMS));
            for(int i=0;i<blocks.length;i++) {
                Item item=Item.getItemFromBlock(blocks[i]);
                check(item.getClass().getSimpleName().equals(PillarConstruction.items[i]),"exact original registered item class");
                check(((ItemBlock)item).getBlock()==blocks[i],"real Forge late item-to-block map");
                check(item.getRegistryName().equals(new ResourceLocation("hbm:tile."+PillarConstruction.names[i])),"original tile-prefixed block/item registry ID");
                check(!item.getHasSubtypes() && item.getMaxDamage()==0,"original nondamageable non-subtype item");
                for(int metadata:METADATA)check(item.getMetadata(metadata)==0,"unchanged placement metadata");
            }
            Block excluded=new BlockPillar(Material.ROCK,"hbm:gneiss_tile"){};
            ((BlockPillar)excluded).setBlockTextureName("hbm:gneiss_chiseled");
            ForgeRegistries.BLOCKS.register(excluded.setRegistryName("hbm","unaudited_pillar_subclass"));
            check(PillarModelHandler.class.getAnnotation(Mod.EventBusSubscriber.class).value()[0]==net.minecraftforge.fml.relauncher.Side.CLIENT,"native client-only subscriber discovery");
            MinecraftForge.EVENT_BUS.register(PillarModelHandler.class);
            MinecraftForge.EVENT_BUS.register(new LegacyIconRegistrationHandler());
            MinecraftForge.EVENT_BUS.post(new ModelRegistryEvent());ModelLoader.onRegisterAllBlocks(shapes);
            Map<?,?> custom=(Map<?,?>)field(ModelLoader.class,"customModels").get(null);
            check(custom.size()==11,"one inventory dependency per actual selected item, no unrelated subclasses");
            check(!shapes.getBlockStateMapper().getVariants(excluded).get(excluded.getDefaultState()).getPath().startsWith("native_pillar/"),"unaudited subclass not silently admitted");
            Object previousIcon=null;TextureAtlasSprite previousSprite=null;IModel previousModel=null;IBakedModel previousBaked=null;
            Path overlay=Files.createDirectories(Paths.get("original-asset-overlay/assets/hbm/textures/blocks"));
            Files.copy(Paths.get(args[0],"assets/hbm/textures/blocks/basalt.png"),overlay.resolve("gneiss_tile.png"));
            try(OriginalCubeFaces.LegacyFaces original=new OriginalCubeFaces.LegacyFaces(new File(args[1]))) {
                for(int reload=0;reload<2;reload++) {
                    List<IResourcePack> current=new ArrayList<>(packs);
                    if(reload==1)current.add(new FolderResourcePack(new File("original-asset-overlay")));
                    resources.reloadResources(current);ModelLoaderRegistry.clearModelCache(resources);
                    ((Map<?,?>)field(TextureMap.class,"mapRegisteredSprites").get(map)).clear();
                    List<IModel> models=new ArrayList<>();
                    Set<ResourceLocation> dependencies=new HashSet<>();
                    for(int i=0;i<blocks.length;i++) {
                        ModelResourceLocation route=shapes.getBlockStateMapper().getVariants(blocks[i]).get(blocks[i].getDefaultState());
                        check(shapes.getBlockStateMapper().getVariants(blocks[i]).size()==16,"native state mapper covers all original metadata values");
                        for(IBlockState state:blocks[i].getBlockState().getValidStates())check(shapes.getBlockStateMapper().getVariants(blocks[i]).get(state).equals(route),"all world metadata variants select original metadata-independent art");
                        check(route.equals(new ModelResourceLocation("hbm:native_pillar/tile."+PillarConstruction.names[i],"normal")),"exact native state mapper route");
                        IModel model=ModelLoaderRegistry.getModel(route);models.add(model);
                        check(model!=ModelLoaderRegistry.getMissingModel(),"genuine native pillar loader");
                        Set<ResourceLocation> expected=new HashSet<>(Arrays.asList(new ResourceLocation(atlas(PillarConstruction.tops[i])),new ResourceLocation(atlas(PillarConstruction.sides[i]))));
                        check(new HashSet<>(model.getTextures()).equals(expected),"only original top/side/default-alt dependencies");
                        dependencies.addAll(model.getTextures());
                        ModelResourceLocation inventory=(ModelResourceLocation)custom.get(org.apache.commons.lang3.tuple.Pair.of(Item.getItemFromBlock(blocks[i]).delegate,0));
                        check(ModelLoaderRegistry.getModel(inventory).getTextures().equals(model.getTextures()),"world and item material dependencies equal");
                    }
                    for(ResourceLocation texture:dependencies)map.registerSprite(texture);
                    MinecraftForge.EVENT_BUS.post(new TextureStitchEvent.Pre(map));
                    Method init=TextureMap.class.getDeclaredMethod("initMissingImage");init.setAccessible(true);init.invoke(map);map.loadTextureAtlas(resources);
                    GlStateManager.bindTexture(map.getGlTextureId());
                    int width=org.lwjgl.opengl.GL11.glGetTexLevelParameteri(3553,0,4096),height=org.lwjgl.opengl.GL11.glGetTexLevelParameteri(3553,0,4097);
                    IntBuffer pixels=org.lwjgl.BufferUtils.createIntBuffer(width*height);org.lwjgl.opengl.GL11.glGetTexImage(3553,0,32993,33639,pixels);
                    for(ResourceLocation texture:dependencies) {
                        TextureAtlasSprite sprite=map.getAtlasSprite(texture.toString());check(sprite!=map.getMissingSprite(),"all original PNGs stitched");
                        File pngFile=reload==1 && texture.toString().equals("hbm:blocks/gneiss_tile")?overlay.resolve("gneiss_tile.png").toFile():new File(args[0],"assets/"+texture.getNamespace()+"/textures/"+texture.getPath()+".png");
                        java.awt.image.BufferedImage png=ImageIO.read(pngFile);
                        check(sprite.getIconWidth()==16&&sprite.getIconHeight()==16&&sprite.getFrameCount()==1,"original 16-square static sprite dimensions");
                        for(int y=0;y<16;y++)for(int x=0;x<16;x++)check(pixels.get((sprite.getOriginY()+y)*width+sprite.getOriginX()+x)==png.getRGB(x,y),"exact native GL pixel upload / higher-priority resource-pack reload");
                    }
                    net.minecraft.util.registry.RegistrySimple<ModelResourceLocation,IBakedModel> bakedRegistry=new net.minecraft.util.registry.RegistrySimple<>();
                    List<IBakedModel> bakes=new ArrayList<>();
                    for(int i=0;i<blocks.length;i++) {
                        BlockPillar block=(BlockPillar)blocks[i];IModel model=models.get(i);
                        IBakedModel baked=model.bake(model.getDefaultState(),DefaultVertexFormats.ITEM,t->map.getAtlasSprite(t.toString()));bakes.add(baked);
                        bakedRegistry.putObject(new ModelResourceLocation("hbm:native_pillar/tile."+PillarConstruction.names[i],"inventory"),baked);
                        check(baked.isAmbientOcclusion()&&baked.isGui3d()&&!baked.isBuiltInRenderer(),"genuine ordinary cube model traits");
                        check(baked.getParticleTexture()==sprite(block.getIcon(0,0)),"top-icon particle material, not unused alternate");
                        check(baked.getQuads(block.getDefaultState(),null,0).isEmpty(),"native culling buckets retain no duplicate general faces");
                        IBakedModel reference=ModelLoaderRegistry.getModel(new ResourceLocation("minecraft:block/cube")).bake(model.getDefaultState(),DefaultVertexFormats.ITEM,t->map.getMissingSprite());
                        for(ItemCameraTransforms.TransformType type:ItemCameraTransforms.TransformType.values()) {
                            check(baked.handlePerspective(type).getLeft()==baked,"perspective retains original-UV wrapper");
                            check(Objects.equals(baked.handlePerspective(type).getRight(),reference.handlePerspective(type).getRight()),"unchanged native cube perspective");
                        }
                        for(EnumFacing face:EnumFacing.values()) {
                            for(int meta=0;meta<16;meta++)check(baked.getQuads(block.getStateFromMeta(meta),face,0)==baked.getQuads(block.getDefaultState(),face,0),"native world face ignores metadata without discarding state");
                            List<BakedQuad> quads=baked.getQuads(block.getDefaultState(),face,0);check(quads.size()==1,"one actual face per native culling bucket");BakedQuad quad=quads.get(0);
                            TextureAtlasSprite expected=map.getAtlasSprite(atlas(face.getIndex()<2?PillarConstruction.tops[i]:PillarConstruction.sides[i]));
                            check(quad.getFace()==face&&quad.getSprite()==expected&&!quad.hasTintIndex(),"original selected face, material and white tint");
                            for(int metadata:METADATA)check(sprite(block.getIcon(face.getIndex(),metadata))==expected,"legacy callback and native face remain identical for all metadata");
                            double[][] want=original.standardFace(new double[]{0,0,0,1,1,1},face.getIndex(),expected);int[] data=quad.getVertexData();int stride=quad.getFormat().getIntegerSize();boolean found=false;
                            for(int shift=0;shift<4;shift++){boolean equal=true;for(int v=0;v<4;v++)for(int axis=0;axis<5;axis++){float actual=Float.intBitsToFloat(data[v*stride+(axis<3?axis:axis+1)]);float target=(float)want[(v+shift)%4][axis];if(Float.floatToIntBits(actual)!=Float.floatToIntBits(target))equal=false;}if(equal)found=true;}
                            check(found,"unmodified original RenderBlocks XYZ/UV/winding, including both cap faces");
                        }
                        if(i==0) {
                            check(block.getIcon(0,0)!=previousIcon && sprite(block.getIcon(0,0))!=previousSprite,"reload refreshes legacy wrapper AND native atlas sprite");
                            check(model!=previousModel&&baked!=previousBaked,"reload invalidates native model and bake caches");
                            previousIcon=block.getIcon(0,0);previousSprite=sprite(block.getIcon(0,0));previousModel=model;previousBaked=baked;
                        }
                    }
                    IBakedModel missingModel=ModelLoaderRegistry.getMissingModel().bake(net.minecraftforge.common.model.TRSRTransformation.identity(),DefaultVertexFormats.ITEM,t->map.getMissingSprite());
                    field(ModelManager.class,"modelRegistry").set(manager,bakedRegistry);field(ModelManager.class,"defaultModel").set(manager,missingModel);
                    net.minecraftforge.client.ItemModelMesherForge mesher=new net.minecraftforge.client.ItemModelMesherForge(manager);ModelLoader.onRegisterItems(mesher);
                    for(int i=0;i<blocks.length;i++)for(int damage:METADATA) {
                        Item item=Item.getItemFromBlock(blocks[i]);ItemStack raw=new ItemStack(item,1,0);field(ItemStack.class,"itemDamage").setInt(raw,damage);
                        check(raw.getItem()==item&&raw.getMetadata()==damage,"raw item damage stays untouched");
                        check(mesher.getItemModel(raw)==bakes.get(i),"native item mesh routes all raw damage to original face model");
                        ItemStack constructed=new ItemStack(item,1,damage);
                        if(damage>65535)check(constructed.isEmpty()&&mesher.getItemModel(constructed)==missingModel,"invalid native item stays empty, not hijacked");
                        else check(mesher.getItemModel(constructed)==bakes.get(i),"native constructor metadata/clamp resolves original item model");
                    }
                    mesher.rebuildCache();check(mesher.getItemModel(new ItemStack(Item.getItemFromBlock(blocks[0]),1,32767))==bakes.get(0),"native mesh cache rebuild");
                    check(map.getMipmapLevels()==2&&org.lwjgl.opengl.GL11.glGetError()==0,"unchanged native mipmap level and clean GL");
                    System.out.println("PASS native registry/model/atlas/GL/old-face/item-metadata cycle "+reload+(reload==1?" with higher-priority original-asset overlay":""));
                }
            }
            map.deleteGlTexture();
        }
        System.out.println("PASS "+checks+" assertions: full pillar owners, eleven production calls, genuine Forge lifecycle and original bytecode face oracle. NOT a launched game or whole-mod build.");
    }
}
