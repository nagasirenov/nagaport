import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import javax.imageio.ImageIO;

import com.google.common.collect.ImmutableMap;
import com.hbm.main.ElectricFurnaceModelHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistrySimple;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.animation.ModelBlockAnimation;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.tuple.Pair;
import sun.misc.Unsafe;

/** Headless native JSON/bake/mesher probe, not the real furnace lifecycle. */
public final class ElectricFurnaceModelNativeTest {
    private static int checks;
    private static final Map<ResourceLocation, TextureAtlasSprite> SPRITES = new HashMap<>();
    private static final RegistrySimple<ModelResourceLocation, IBakedModel> BAKED = new RegistrySimple<>();
    private static Unsafe unsafe;
    private static ModelLoader loader;
    private static Constructor<?> wrapperConstructor;
    private static Method bakeImpl;

    private static void check(boolean condition, String message) {
        checks++;
        if(!condition) throw new AssertionError(message);
    }
    private static void close(float actual, float expected, String message) {
        check(Math.abs(actual - expected) < 0.000002F, message + ": " + actual + " != " + expected);
    }
    private static void set(Object object, Class<?> owner, String name, Object value) throws Exception {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        field.set(object, value);
    }
    private static final class Sprite extends TextureAtlasSprite {
        Sprite(ResourceLocation name, int index) {
            super(name.toString());
            setIconWidth(16);
            setIconHeight(16);
            initSprite(128, 64, 16 * index, 16, false);
        }
    }
    // Controlled native-state fixture, not a replacement production block.
    private static final class StateFixture extends BlockContainer {
        private static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);
        StateFixture() { super(Material.IRON); setDefaultState(blockState.getBaseState().withProperty(METADATA, 0)); }
        @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, METADATA); }
        @Override public IBlockState getStateFromMeta(int value) { return getDefaultState().withProperty(METADATA, value); }
        @Override public int getMetaFromState(IBlockState state) { return state.getValue(METADATA); }
        @Override public TileEntity createNewTileEntity(World world, int metadata) { return null; }
    }
    private static Reader reader(IResourcePack pack, ResourceLocation resource) throws Exception {
        InputStream input;
        if("minecraft".equals(resource.getNamespace())) {
            input = ElectricFurnaceModelNativeTest.class.getResourceAsStream("/assets/minecraft/" + resource.getPath());
            check(input != null, "native parent exists " + resource);
        } else {
            check(pack.resourceExists(resource), "packaged resource exists " + resource);
            input = pack.getInputStream(resource);
        }
        return new InputStreamReader(input, StandardCharsets.UTF_8);
    }
    private static ModelBlock loadModel(IResourcePack pack, ResourceLocation name) throws Exception {
        ModelBlock model;
        try(Reader in = reader(pack, new ResourceLocation(name.getNamespace(), "models/" + name.getPath() + ".json"))) {
            model = ModelBlock.deserialize(in);
        }
        if(model.getParentLocation() != null) model.parent = loadModel(pack, model.getParentLocation());
        return model;
    }
    private static IBakedModel bake(IResourcePack pack, ResourceLocation name) throws Exception {
        ModelBlock model = loadModel(pack, name);
        IModel wrapper = (IModel) wrapperConstructor.newInstance(loader, name, model, false,
                new ModelBlockAnimation(ImmutableMap.of(), ImmutableMap.of()));
        Function<ResourceLocation, TextureAtlasSprite> textures = resource -> {
            TextureAtlasSprite sprite = SPRITES.get(resource);
            check(sprite != null, "texture dependency resolved " + resource);
            return sprite;
        };
        // Entire real Forge bakeImpl -> bakeNormal -> native FaceBakery -> native baked model.
        // Only GL-backed loader initialization and atlas upload are intentionally bypassed.
        return (IBakedModel) bakeImpl.invoke(wrapper, TRSRTransformation.identity(), DefaultVertexFormats.ITEM, textures);
    }
    private static String expectedTexture(String active, int metadata, int face) {
        String suffix = metadata == 0 && face == 3 ? "front_" + active : face == metadata ? "front_" + active :
                face == 0 ? "bottom" : face == 1 ? "top" : "side";
        return "hbm:blocks/machine_electric_furnace_" + suffix;
    }
    private static float[] oldUV(EnumFacing face, float x, float y, float z) {
        // Official 1.7.10 client blm.a..f (RenderBlocks) no-rotation full-cube output.
        switch(face) {
            case DOWN: case UP: return new float[] {x, z};
            case NORTH: return new float[] {1 - x, 1 - y};
            case SOUTH: return new float[] {x, 1 - y};
            case WEST: return new float[] {z, 1 - y};
            case EAST: return new float[] {1 - z, 1 - y};
            default: throw new AssertionError(face);
        }
    }
    private static void verify(IBakedModel model, String active, int metadata, String kind) {
        String context = kind + ":" + active + ":" + metadata;
        check(model.isAmbientOcclusion(), context + " ambient occlusion");
        check(model.isGui3d() && !model.isBuiltInRenderer(), context + " native 3D model");
        close(model.getItemCameraTransforms().gui.scale.x, 0.625F, context + " native block GUI scale");
        close(model.getItemCameraTransforms().gui.rotation.x, 30F, context + " native block GUI X rotation");
        close(model.getItemCameraTransforms().gui.rotation.y, 225F, context + " native block GUI Y rotation");
        check(model.getParticleTexture().getIconName().equals(expectedTexture(active, metadata, 1)), context + " particle");
        check(model.getOverrides().getOverrides().isEmpty(), context + " no override routing");
        check(model.getQuads(null, null, 0).isEmpty(), context + " no duplicate general quads");
        Set<BakedQuad> emitted = Collections.newSetFromMap(new IdentityHashMap<BakedQuad, Boolean>());
        int total = model.getQuads(null, null, 0).size();
        for(EnumFacing face : EnumFacing.values()) {
            List<BakedQuad> list = model.getQuads(null, face, 0);
            check(list.size() == 1, context + " exactly one culled face " + face);
            total += list.size();
            BakedQuad quad = list.get(0);
            check(emitted.add(quad), context + " distinct native face");
            check(quad.getFace() == face, context + " face direction " + face);
            check(quad.shouldApplyDiffuseLighting(), context + " diffuse lighting " + face);
            check(!quad.hasTintIndex(), context + " no invented tint " + face);
            check(quad.getSprite().getIconName().equals(expectedTexture(active, metadata, face.getIndex())), context + " sprite " + face);
            check(quad.getFormat().equals(DefaultVertexFormats.ITEM), context + " actual native baked vertex format");
            int[] data = quad.getVertexData();
            int stride = quad.getFormat().getIntegerSize();
            check(data.length == 4 * stride && stride == 7, context + " native four-vertex quad");
            Set<String> corners = new HashSet<>();
            float[][] xyz = new float[4][3];
            for(int vertex = 0; vertex < 4; vertex++) {
                int offset = vertex * stride;
                float x = Float.intBitsToFloat(data[offset]), y = Float.intBitsToFloat(data[offset+1]), z = Float.intBitsToFloat(data[offset+2]);
                xyz[vertex] = new float[] {x, y, z};
                for(float coord : xyz[vertex]) check(coord == 0 || coord == 1, context + " cube vertex bounds");
                check(corners.add(x + "," + y + "," + z), context + " unique corner");
                float plane = face.getAxis() == EnumFacing.Axis.X ? x : face.getAxis() == EnumFacing.Axis.Y ? y : z;
                close(plane, face.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? 1 : 0, context + " face plane");
                float[] uv = oldUV(face, x, y, z);
                // Native FaceBakery applies its standard 0.1% edge inset; orientation is legacy-exact.
                float u = quad.getSprite().getInterpolatedU(16 * (uv[0] * 0.998F + 0.001F));
                float v = quad.getSprite().getInterpolatedV(16 * (uv[1] * 0.998F + 0.001F));
                close(Float.intBitsToFloat(data[offset+4]), u, context + " legacy U " + face);
                close(Float.intBitsToFloat(data[offset+5]), v, context + " legacy V " + face);
                int normal = data[offset+6];
                check((byte) normal == face.getXOffset() * 127, context + " X normal");
                check((byte) (normal >> 8) == face.getYOffset() * 127, context + " Y normal");
                check((byte) (normal >> 16) == face.getZOffset() * 127, context + " Z normal");
            }
            float ax = xyz[1][0]-xyz[0][0], ay = xyz[1][1]-xyz[0][1], az = xyz[1][2]-xyz[0][2];
            float bx = xyz[2][0]-xyz[0][0], by = xyz[2][1]-xyz[0][1], bz = xyz[2][2]-xyz[0][2];
            float facingDot = (ay*bz-az*by)*face.getXOffset() + (az*bx-ax*bz)*face.getYOffset() + (ax*by-ay*bx)*face.getZOffset();
            close(facingDot, 1, context + " outward winding");
        }
        check(total == 6 && emitted.size() == 6, context + " native item traversal emits six, not twelve");
    }
    public static void main(String[] args) throws Exception {
        net.minecraft.init.Bootstrap.register();
        Field uf = Unsafe.class.getDeclaredField("theUnsafe"); uf.setAccessible(true); unsafe = (Unsafe) uf.get(null);
        loader = (ModelLoader) unsafe.allocateInstance(ModelLoader.class);
        set(loader, ModelBakery.class, "faceBakery", new FaceBakery());
        Class<?> wrapper = Class.forName("net.minecraftforge.client.model.ModelLoader$VanillaModelWrapper");
        wrapperConstructor = wrapper.getDeclaredConstructors()[0]; wrapperConstructor.setAccessible(true);
        bakeImpl = wrapper.getMethod("bakeImpl", IModelState.class, VertexFormat.class, Function.class); bakeImpl.setAccessible(true);
        Mod.EventBusSubscriber registration = ElectricFurnaceModelHandler.class.getAnnotation(Mod.EventBusSubscriber.class);
        check(registration != null && registration.modid().equals("hbm") && Arrays.equals(registration.value(), new Side[]{Side.CLIENT}), "automatic client-only subscriber");
        Method event = ElectricFurnaceModelHandler.class.getMethod("registerModels", ModelRegistryEvent.class);
        check(event.getAnnotation(SubscribeEvent.class) != null && Modifier.isStatic(event.getModifiers()), "static model registration event");
        ItemBlock[] items = new ItemBlock[2];
        StateFixture[] blocks = new StateFixture[2];
        String[] actives = {"off", "on"};
        for(int index = 0; index < 2; index++) {
            ResourceLocation id = new ResourceLocation("hbm", "tile.machine_electric_furnace_" + actives[index]);
            blocks[index] = new StateFixture(); blocks[index].setRegistryName(id);
            ForgeRegistries.BLOCKS.register(blocks[index]);
            items[index] = new ItemBlock(blocks[index]); items[index].setRegistryName(id);
            ForgeRegistries.ITEMS.register(items[index]);
        }
        ElectricFurnaceModelHandler.registerModels(new ModelRegistryEvent());
        Field cm = ModelLoader.class.getDeclaredField("customModels"); cm.setAccessible(true);
        Map<?, ?> custom = (Map<?, ?>) cm.get(null);
        check(custom.size() == 32, "exactly 32 native registered item metadata models");
        String[] textures = {"front_off", "front_on", "top", "bottom", "side"};
        for(int index = 0; index < textures.length; index++) {
            ResourceLocation id = new ResourceLocation("hbm", "blocks/machine_electric_furnace_" + textures[index]);
            SPRITES.put(id, new Sprite(id, index));
        }
        try(FileResourcePack zip = new FileResourcePack(new File(args[1]))) {
            for(IResourcePack pack : new IResourcePack[]{new FolderResourcePack(new File(args[0])), zip}) {
                for(ResourceLocation sprite : SPRITES.keySet()) {
                    ResourceLocation png = new ResourceLocation(sprite.getNamespace(), "textures/" + sprite.getPath() + ".png");
                    check(pack.resourceExists(png), "native packaged sprite lookup " + png);
                    try(InputStream in = pack.getInputStream(png)) { check(ImageIO.read(in) != null, "decode actual PNG " + png); }
                }
                for(int index = 0; index < 2; index++) {
                    String active = actives[index];
                    ResourceLocation id = blocks[index].getRegistryName();
                    ModelBlockDefinition definition;
                    try(Reader in = reader(pack, new ResourceLocation("hbm", "blockstates/" + id.getPath() + ".json"))) {
                        definition = ModelBlockDefinition.parseFromReader(in, id);
                    }
                    Map<IBlockState, ModelResourceLocation> states = new DefaultStateMapper().putStateModelLocations(blocks[index]);
                    check(states.size() == 16, "all native metadata states");
                    for(int metadata = 0; metadata < 16; metadata++) {
                        ModelResourceLocation stateLocation = states.get(blocks[index].getStateFromMeta(metadata));
                        check(stateLocation.equals(new ModelResourceLocation(id, "metadata=" + metadata)), "exact tile-prefixed state model ID");
                        check(definition.hasVariant(stateLocation.getVariant()), "all 16 JSON variants");
                        List<Variant> variants = definition.getVariant(stateLocation.getVariant()).getVariantList();
                        check(variants.size() == 1, "no weighted or duplicate variant");
                        Variant variant = variants.get(0);
                        check(variant.getRotation() == ModelRotation.X0_Y0 && !variant.isUvLock(), "no invented orientation rotation");
                        ResourceLocation modelId = new ResourceLocation("hbm", "block/" + id.getPath() + "_" + metadata);
                        check(variant.getModelLocation().equals(modelId), "native variant model path");
                        verify(bake(pack, modelId), active, metadata, "block");
                        ModelResourceLocation itemLocation = new ModelResourceLocation("hbm:" + id.getPath() + "_" + metadata, "inventory");
                        check(itemLocation.equals(custom.get(Pair.of(items[index].delegate, metadata))), "real native item registration map");
                        IBakedModel itemModel = bake(pack, new ResourceLocation("hbm", "item/" + id.getPath() + "_" + metadata));
                        verify(itemModel, active, metadata, "item");
                        BAKED.putObject(itemLocation, itemModel);
                    }
                }
            }
        }
        ModelManager manager = (ModelManager) unsafe.allocateInstance(ModelManager.class);
        set(manager, ModelManager.class, "modelRegistry", BAKED);
        ItemModelMesher mesher = new ItemModelMesher(manager);
        ModelLoader.onRegisterItems(mesher);
        mesher.rebuildCache();
        for(int index = 0; index < 2; index++) for(int metadata = 0; metadata < 16; metadata++) {
            ItemStack stack = new ItemStack(items[index], 1, metadata);
            ModelResourceLocation id = new ModelResourceLocation("hbm:tile.machine_electric_furnace_" + actives[index] + "_" + metadata, "inventory");
            check(mesher.getItemModel(stack) == BAKED.getObject(id), "actual ItemModelMesher preserves metadata " + metadata);
        }
        System.out.println("PASS electric-furnace native models: " + checks + " assertions; 32 states + 32 items baked through real Forge, folder and ZIP resources, exact 32 native mesher selections.");
        System.out.println("Boundaries: controlled BlockContainer metadata fixture and atlas sprites; bypassed GL loader initialization. No client rendering/reload, actual furnace gameplay or full-mod compilation claim.");
    }
}
