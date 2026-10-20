import com.hbm.render.item.NativeItemTextureModelHandler;
import com.hbm.render.item.NativeItemTextureRegistry;
import com.hbm.render.item.nativeitem.context.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.vecmath.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.*;
import net.minecraft.entity.Entity;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.registry.RegistrySimple;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.*;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.objectweb.asm.*;

/** Native registration/lifecycle/model/GPU slice, not full mod initialization.
 * Isolated application entity tokens/items are supplied by the generated fixture.
 * Every Forge/Minecraft API and all production statements/providers are unchanged.
 */
public final class SnowballFactoriesNativeTest {
    static int checks;
    static Minecraft mc;
    static void check(boolean ok, String message) { checks++; if(!ok) throw new AssertionError(message); }
    static Field field(Class<?> c, String name) throws Exception { Field f=c.getDeclaredField(name); f.setAccessible(true); return f; }
    static Object get(Object target, Class<?> c, String name) throws Exception { return field(c,name).get(target); }
    static void set(Object target, Class<?> c, String name, Object value) throws Exception { field(c,name).set(target,value); }
    static Matrix4f matrix() {
        java.nio.FloatBuffer b=BufferUtils.createFloatBuffer(16); GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX,b);
        float[] v=new float[16]; b.get(v); Matrix4f m=new Matrix4f(v); m.transpose(); return m;
    }
    public static Item registerItem(String name, String texture) {
        Item item=NativeItemTextureRegistry.texture(new Item(),texture);
        item.setRegistryName("hbm","snowball_probe_"+name); ForgeRegistries.ITEMS.register(item); return item;
    }
    static final class RecordingModel implements IBakedModel {
        final IBakedModel model;
        final List<Matrix4f> matrices=new ArrayList<>();
        final List<TransformType> transforms=new ArrayList<>();
        int quads;
        RecordingModel(IBakedModel model) { this.model=model; }
        public List<BakedQuad> getQuads(net.minecraft.block.state.IBlockState state,EnumFacing side,long seed) {
            List<BakedQuad> result=model.getQuads(state,side,seed); quads+=result.size();
            if(!result.isEmpty()) matrices.add(matrix()); return result;
        }
        public boolean isAmbientOcclusion() { return model.isAmbientOcclusion(); }
        public boolean isGui3d() { return model.isGui3d(); }
        public boolean isBuiltInRenderer() { return model.isBuiltInRenderer(); }
        public TextureAtlasSprite getParticleTexture() { return model.getParticleTexture(); }
        public ItemCameraTransforms getItemCameraTransforms() { return model.getItemCameraTransforms(); }
        public ItemOverrideList getOverrides() { return model.getOverrides(); }
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType type) {
            transforms.add(type); return Pair.of(this,model.handlePerspective(type).getRight());
        }
        void clear() { matrices.clear(); transforms.clear(); quads=0; }
    }
    static void auditLifecycle() throws Exception {
        final List<String> init=new ArrayList<>(),finish=new ArrayList<>(),begin=new ArrayList<>(),managerCtor=new ArrayList<>();
        for(String name:new String[]{"net/minecraft/client/Minecraft","net/minecraftforge/fml/client/FMLClientHandler","net/minecraft/client/renderer/entity/RenderManager"}) {
            try(InputStream in=SnowballFactoriesNativeTest.class.getClassLoader().getResourceAsStream(name+".class")) {
                new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM5) {
                    public MethodVisitor visitMethod(int access,String method,String desc,String signature,String[] exceptions) {
                        final List<String> target=method.equals("init")?init:method.equals("finishMinecraftLoading")?finish:method.equals("beginMinecraftLoading")?begin:method.equals("<init>")&&name.endsWith("RenderManager")?managerCtor:null;
                        return target==null?null:new MethodVisitor(Opcodes.ASM5) {
                            public void visitMethodInsn(int opcode,String owner,String name,String descriptor,boolean itf) { target.add(owner+"."+name); if(owner.equals("net/minecraftforge/fml/client/registry/RenderingRegistry")) target.add(descriptor); }
                        };
                    }
                },ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
            }
        }
        int item=init.indexOf("net/minecraft/client/renderer/RenderItem.<init>"),manager=init.indexOf("net/minecraft/client/renderer/entity/RenderManager.<init>"),loading=init.indexOf("net/minecraftforge/fml/client/FMLClientHandler.finishMinecraftLoading");
        check(init.indexOf("net/minecraftforge/fml/client/FMLClientHandler.beginMinecraftLoading")<item&&item>=0&&item<manager&&manager<loading,"actual native Minecraft preinitializes mods then constructs RenderItem before RenderManager");
        check(begin.contains("net/minecraftforge/fml/common/Loader.preinitializeMods"),"native Forge begin-loading includes preinitialization registration window");
        check(managerCtor.contains("net/minecraftforge/fml/client/registry/RenderingRegistry.loadEntityRenderers")&&managerCtor.contains("(Lnet/minecraft/client/renderer/entity/RenderManager;Ljava/util/Map;)V"),"actual RenderManager constructor invokes the typed factory overload before global manager assignment");
        int mods=finish.indexOf("net/minecraftforge/fml/common/Loader.initializeMods"),factories=finish.indexOf("net/minecraftforge/fml/client/registry/RenderingRegistry.loadEntityRenderers"),complete=finish.indexOf("net/minecraftforge/fml/common/Loader.loadingComplete");
        check(mods>=0&&mods<factories&&factories<complete,"later Forge legacy renderer map load follows mod init and precedes load-complete item ownership");
    }
    @SuppressWarnings("unchecked") static void run(String[] args) throws Exception {
        check(Minecraft.getMinecraft()==null,"registration begins before Minecraft singleton is supplied");
        SnowballRegistrationFixture.register();
        Object registry=get(null,RenderingRegistry.class,"INSTANCE");
        check(((Map<?,?>)get(registry,RenderingRegistry.class,"entityRenderers")).size()==42,"all 42 entries use actual IRenderFactory overload");
        check(((Map<?,?>)get(registry,RenderingRegistry.class,"entityRenderersOld")).isEmpty(),"legacy eager renderer registry remains empty");
        check(Minecraft.getMinecraft()==null,"registration never eagerly resolves RenderItem");
        for(Field slot:SnowballRegistrationFixture.ModItems.class.getDeclaredFields()) check(slot.get(null)==null,"mod item slots remain uninitialized at registration");
        Method initialize=Class.forName("MaterialTextureAtlasSpriteNativeTest").getDeclaredMethod("initializeNativeClientSettings"); initialize.setAccessible(true); initialize.invoke(null);
        mc=Minecraft.getMinecraft(); Bootstrap.register(); OpenGlHelper.initializeTextures();
        net.minecraftforge.fml.common.ModMetadata metadata=new net.minecraftforge.fml.common.ModMetadata(); metadata.modId="hbm"; metadata.name="Snowball native proof"; metadata.version="probe";
        net.minecraftforge.fml.common.DummyModContainer container=new net.minecraftforge.fml.common.DummyModContainer(metadata);
        net.minecraftforge.fml.common.Loader.instance().setupTestHarness(container); net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(container);
        if(net.minecraft.launchwrapper.Launch.blackboard==null) net.minecraft.launchwrapper.Launch.blackboard=new HashMap<>();
        net.minecraft.launchwrapper.Launch.blackboard.put("fml.deobfuscatedEnvironment",Boolean.TRUE);
        SnowballRegistrationFixture.initializeItems();
        SimpleReloadableResourceManager resources=new SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer());
        try(FileResourcePack vanilla=new FileResourcePack(new File(args[1]))) {
            resources.reloadResources(Arrays.asList(vanilla,new FolderResourcePack(new File(args[0]))));
            set(mc,Minecraft.class,"resourceManager",resources);
            TextureMap atlas=new TextureMap("textures"); atlas.setMipmapLevels(0); set(mc,Minecraft.class,"textureMapBlocks",atlas);
            TextureManager textures=new TextureManager(resources); set(mc,Minecraft.class,"renderEngine",textures);
            ModelLoaderRegistry.clearModelCache(resources);
            new ModelLoader(resources,atlas,new BlockModelShapes(new ModelManager(atlas)));
            NativeItemTextureModelHandler foundation=new NativeItemTextureModelHandler();
            foundation.register(new ModelRegistryEvent()); foundation.stitch(new TextureStitchEvent.Pre(atlas));
            atlas.registerSprite(new ResourceLocation("minecraft:items/slimeball"));
            net.minecraftforge.fml.common.asm.FMLSanityChecker.fmlLocation=new File(TextureMap.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Method missing=TextureMap.class.getDeclaredMethod("initMissingImage"); missing.setAccessible(true); missing.invoke(atlas);
            atlas.loadTextureAtlas(resources);
            // TextureManager ownership is real, but avoid its loadTexture reload of
            // the just-built controlled atlas, which would invoke unrelated global listeners.
            ((Map<ResourceLocation,ITextureObject>)get(textures,TextureManager.class,"mapTextureObjects")).put(TextureMap.LOCATION_BLOCKS_TEXTURE,atlas);
            RegistrySimple<ModelResourceLocation,IBakedModel> baked=new RegistrySimple<>();
            ModelManager models=new ModelManager(atlas);
            foundation.bake(new ModelBakeEvent(models,baked,null));
            IModel slime=ModelLoaderRegistry.getModel(new ResourceLocation("minecraft:item/slime_ball"));
            ModelResourceLocation slimeLocation=new ModelResourceLocation("minecraft:slime_ball","inventory");
            baked.putObject(slimeLocation,slime.bake(slime.getDefaultState(),net.minecraft.client.renderer.vertex.DefaultVertexFormats.ITEM,id->atlas.getAtlasSprite(id.toString())));
            set(models,ModelManager.class,"modelRegistry",baked); set(models,ModelManager.class,"defaultModel",baked.getObject(slimeLocation));
            ItemColors colors=new ItemColors(); set(mc,Minecraft.class,"itemColors",colors);
            RenderItem itemRenderer=new RenderItem(textures,models,colors); set(mc,Minecraft.class,"renderItem",itemRenderer);
            Class<?>[] types=SnowballRegistrationFixture.entities(); Item[] items=SnowballRegistrationFixture.items();
            List<RecordingModel> records=new ArrayList<>();
            for(Item item:items) {
                ModelResourceLocation location=item==Items.SLIME_BALL?slimeLocation:new ModelResourceLocation(item.getRegistryName(),"inventory");
                IBakedModel model=baked.getObject(location); check(model!=null,"actual native foundation supplies original item model");
                check(model.getQuads(null,null,0).size()>2,"native alpha-edge geometry retained, not placeholder quad");
                String texture=item==Items.SLIME_BALL?"minecraft:items/slimeball":NativeItemTextureRegistry.atlasLocation(NativeItemTextureRegistry.texture(item)).toString();
                check(model.getParticleTexture().getIconName().equals(texture),"each model owns its exact original texture");
                RecordingModel record=new RecordingModel(model); records.add(record); baked.putObject(location,record);
                itemRenderer.getItemModelMesher().register(item,stack->location);
            }
            itemRenderer.getItemModelMesher().rebuildCache();
            check(mc.getRenderManager()==null,"global manager is absent while native factory constructor will execute");
            RenderManager manager=new RenderManager(textures,itemRenderer); manager.options=mc.gameSettings;
            check(mc.getRenderManager()==null,"factory construction does not require or assign the global manager");
            for(Class<?> type:types) check(manager.entityRenderMap.containsKey(type),"actual native RenderManager constructor invokes typed factories");
            set(mc,Minecraft.class,"renderManager",manager);
            for(int i=0;i<types.length;i++) {
                Render<?> render=manager.entityRenderMap.get(types[i]);
                check(render!=null&&render.getClass()==RenderSnowball.class,"factory creates exact native RenderSnowball for each token");
                check(get(render,Render.class,"renderManager")==manager,"native registry passes actual supplied manager identity");
                check(get(render,RenderSnowball.class,"itemRenderer")==itemRenderer,"factory captures actual initialized global RenderItem");
                ItemStack stack=((RenderSnowball<Entity>)render).getStackToRender(null);
                check(stack.getItem()==items[i]&&stack.getCount()==1&&stack.getMetadata()==0&&!stack.hasTagCompound(),"original displayed item, count one, metadata zero and no injected NBT");
                Entity entity=(Entity)types[i].getDeclaredConstructor().newInstance(); RecordingModel record=records.get(i);
                for(int view:new int[]{0,1,2}) {
                    manager.options.thirdPersonView=view; manager.playerViewX=27; manager.playerViewY=41;
                    GL11.glMatrixMode(GL11.GL_MODELVIEW); GL11.glLoadIdentity(); Matrix4f before=matrix(); int depth=GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH);
                    ((RenderSnowball<Entity>)render).doRender(entity,3,5,7,0,0);
                    check(record.transforms.equals(Collections.singletonList(TransformType.GROUND)),"real snowball dispatches actual GROUND native model perspective "+types[i]+" view="+view+" observed="+record.transforms);
                    check(record.quads>2&&!record.matrices.isEmpty(),"original textured native geometry reaches real RenderItem draw");
                    check(matrix().equals(before)&&depth==GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH),"real draw restores matrix and stack depth");
                    GL11.glPushMatrix(); GL11.glTranslatef(3,5,7); GL11.glRotatef(-41,0,1,0); GL11.glRotatef(view==2?-27:27,1,0,0); GL11.glRotatef(180,0,1,0);
                    GL11.glTranslatef(0,.125F,0); GL11.glScalef(.5F,.5F,.5F); GL11.glTranslatef(-.5F,-.5F,-.5F); Matrix4f centered=matrix(); GL11.glPopMatrix();
                    Matrix4f actual=record.matrices.get(0); // Native generated vertices are in [0,1], centered by RenderItem.
                    check(actual.epsilonEquals(centered,.00001F),"native generated perspective preserves original 0.5 scale / 0.125 center at all camera views");
                    check(GL11.glGetError()==0,"actual native textured GL draw is error-free"); record.clear();
                }
            }
            // Replay the genuine loader with a replacement manager and RenderItem:
            // confirms factories did not capture stale owner objects at registration.
            RenderItem later=new RenderItem(textures,models,colors); set(mc,Minecraft.class,"renderItem",later);
            RenderManager other=new RenderManager(textures,later);
            for(Class<?> type:types) {
                Render<?> render=other.entityRenderMap.get(type);
                check(get(render,Render.class,"renderManager")==other&&get(render,RenderSnowball.class,"itemRenderer")==later,"each factory resolves live owners on invocation, not stale registration owners");
                check(render!=manager.entityRenderMap.get(type),"factory produces new owned instance for another manager");
            }
            set(mc,Minecraft.class,"renderManager",other); set(mc,Minecraft.class,"itemRenderer",new ItemRenderer(mc));
            List<String> warnings=new ArrayList<>();
            ContextItemRegistration contextual=new ContextItemRegistration(Collections.emptyMap(),warnings::add);
            contextual.bake(new ModelBakeEvent(models,baked,null));
            NativeItemRendererOwnership ownership=new NativeItemRendererOwnership(contextual);
            check(ownership.install(),"real existing load-complete ownership transaction accepts all factory-born native caches: "+warnings);
            check(mc.getRenderItem()==ownership.getInstalled(),"native global RenderItem participates in ownership commit");
            for(Class<?> type:types) check(get(other.entityRenderMap.get(type),RenderSnowball.class,"itemRenderer")==ownership.getInstalled(),"all 42 factory-created native caches follow existing load-complete ownership");
        }
        auditLifecycle();
    }
    public static void main(String[] args) throws Exception {
        Constructor<?> ctor=Class.forName("MaterialTextureAtlasSpriteNativeTest$Context").getDeclaredConstructor(); ctor.setAccessible(true);
        try(AutoCloseable context=(AutoCloseable)ctor.newInstance()) {
            run(args); System.out.println("PASS "+checks+" actual Forge factory/lifecycle/native stack/original-texture/generated-model/EGL draw checks. Controlled application tokens; not full ClientProxy or in-game acceptance.");
        }
    }
}
