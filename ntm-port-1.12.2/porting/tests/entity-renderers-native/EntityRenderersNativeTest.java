import com.hbm.render.entity.mob.*;
import com.hbm.render.entity.projectile.*;
import com.hbm.render.util.ClientBossDisplay;
import com.hbm.render.mesh.MeshModel;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.vecmath.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.objectweb.asm.*;

/** Complete chicken/Beam5/ChopperMine/Coin/WormBody production classes; the
 * other sixteen named renderers are explicitly constructor+field projections.
 * Entity tokens are controlled fixtures, never evidence of full entity logic.
 */
public final class EntityRenderersNativeTest {
    static int checks;
    static void check(boolean ok,String why) { checks++; if(!ok)throw new AssertionError(why); }
    static Field field(Class<?> type,String name)throws Exception {Field f=type.getDeclaredField(name);f.setAccessible(true);return f;}
    static Object get(Object value,Class<?> type,String name)throws Exception {return field(type,name).get(value);}
    static void set(Object value,Class<?> type,String name,Object next)throws Exception {field(type,name).set(value,next);}
    static Matrix4f matrix() {java.nio.FloatBuffer buffer=BufferUtils.createFloatBuffer(16);GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX,buffer);float[] raw=new float[16];buffer.get(raw);Matrix4f result=new Matrix4f(raw);result.transpose();return result;}
    static int primitives(Runnable draw) {int query=GL15.glGenQueries();GL15.glBeginQuery(GL30.GL_PRIMITIVES_GENERATED,query);try{draw.run();}finally{GL15.glEndQuery(GL30.GL_PRIMITIVES_GENERATED);}int result=GL15.glGetQueryObjecti(query,GL15.GL_QUERY_RESULT);GL15.glDeleteQueries(query);return result;}
    static void auditLifecycle()throws Exception {
        List<String> events=new ArrayList<>();
        try(InputStream in=EntityRenderersNativeTest.class.getClassLoader().getResourceAsStream("net/minecraft/client/Minecraft.class")) {
            new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM5){public MethodVisitor visitMethod(int access,String name,String desc,String sig,String[] ex){
                if(!name.equals("init"))return null;
                return new MethodVisitor(Opcodes.ASM5){
                    public void visitMethodInsn(int op,String owner,String name,String desc,boolean itf){events.add(owner+"."+name);}
                    public void visitFieldInsn(int op,String owner,String name,String desc){if(op==Opcodes.PUTFIELD)events.add("assign "+name);}
                };}},ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        }
        int item=events.indexOf("assign renderItem"),ctor=events.indexOf("net/minecraft/client/renderer/entity/RenderManager.<init>"),assign=events.indexOf("assign renderManager");
        check(item>=0&&item<ctor&&ctor<assign,"actual Minecraft bytecode assigns RenderItem, constructs manager, then publishes manager");
        List<String> loads=new ArrayList<>();
        try(InputStream in=EntityRenderersNativeTest.class.getClassLoader().getResourceAsStream("net/minecraft/client/renderer/entity/RenderManager.class")){
            new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM5){public MethodVisitor visitMethod(int access,String name,String desc,String sig,String[] ex){if(!name.equals("<init>"))return null;return new MethodVisitor(Opcodes.ASM5){public void visitMethodInsn(int op,String owner,String name,String desc,boolean itf){if(owner.equals("net/minecraftforge/fml/client/registry/RenderingRegistry"))loads.add(name+desc);}};}},ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        }
        check(loads.contains("loadEntityRenderers(Lnet/minecraft/client/renderer/entity/RenderManager;Ljava/util/Map;)V"),"real manager constructor invokes typed factories before global publication");
    }
    @SuppressWarnings("unchecked") static void directDraw(RenderManager manager,TextureManager textures)throws Exception {
        Object[][] cases={
            {RendererRegistrationFixture.EntityExplosiveBeam.class,null},
            {RendererRegistrationFixture.EntityChopperMine.class,"hbm:textures/models/chopperBomb.png"},
            {RendererRegistrationFixture.EntityCoin.class,"hbm:textures/models/trinkets/chip_gold.png"},
            {RendererRegistrationFixture.EntityBOTPrimeBody.class,"hbm:textures/entity/mark_zero_body.png"}};
        for(Object[] row:cases){
            Class<?> type=(Class<?>)row[0];Entity e=(Entity)type.getDeclaredConstructor().newInstance();
            e.prevRotationYaw=11;e.rotationYaw=29;e.prevRotationPitch=7;e.rotationPitch=19;e.ticksExisted=13;
            Render<Entity> renderer=(Render<Entity>)manager.entityRenderMap.get(type);
            for(float partial:new float[]{0,.375F,1}){
                GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();Matrix4f before=matrix();int depth=GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH);
                int emitted=primitives(()->renderer.doRender(e,3,5,7,29,partial));
                check(emitted>0,"full production "+renderer.getClass().getSimpleName()+" emits actual native GPU primitives, partial="+partial);
                check(matrix().equals(before)&&depth==GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH),"full production renderer restores matrix/stack");
                if(row[1]!=null){ITextureObject texture=textures.getTexture(new ResourceLocation((String)row[1]));check(texture!=null&&texture.getGlTextureId()==GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D),"original production texture owns actual GL binding");}
                check(GL11.glGetError()==GL11.GL_NO_ERROR,"actual full-class native draw has no GL error");
            }
        }
        check(RenderCoin.coin.getMesh().parts.size()>0&&RenderWormBody.body.getMesh().parts.size()>0,"real coin VBO and body loader retain original nonempty meshes");
    }
    static void chickenPhases(RenderManager manager,TextureManager textures)throws Exception {
        Method getTexture=Render.class.getDeclaredMethod("getEntityTexture",Entity.class);getTexture.setAccessible(true);
        Method rotation=RenderLivingBase.class.getDeclaredMethod("handleRotationFloat",EntityLivingBase.class,float.class);rotation.setAccessible(true);
        Method renderModel=RenderLivingBase.class.getDeclaredMethod("renderModel",EntityLivingBase.class,float.class,float.class,float.class,float.class,float.class,float.class);renderModel.setAccessible(true);
        RendererRegistrationFixture.EntityQuackos boss=new RendererRegistrationFixture.EntityQuackos();boss.setCustomNameTag("Factory duck");boss.setHealth(boss.getMaxHealth()/2);
        EntityChicken plain=new RendererRegistrationFixture.EntityDuck();
        RenderChicken[] renderers={(RenderChicken)manager.entityRenderMap.get(RendererRegistrationFixture.EntityDuck.class),(RenderChicken)manager.entityRenderMap.get(RendererRegistrationFixture.EntityQuackos.class),new RenderQuackos(manager,new ModelChicken(),.3F)};
        float[] shadows={.3F,7.5F,.3F};
        for(int i=0;i<renderers.length;i++){
            RenderChicken renderer=renderers[i];EntityChicken entity=i==1?boss:plain;
            check(get(renderer,Render.class,"renderManager")==manager,"chicken owner callback retained");
            check(((Float)get(renderer,Render.class,"shadowSize")).floatValue()==shadows[i],"original chicken shadow radius retained");
            check(renderer.getMainModel().getClass()==ModelChicken.class,"original actual ModelChicken geometry retained");
            check(getTexture.invoke(renderer,entity).equals(new ResourceLocation("hbm:textures/entity/duck.png")),"native erased texture bridge reaches duck texture, not vanilla chicken");
            entity.oFlap=.2F;entity.wingRotation=1.1F;entity.oFlapSpeed=.3F;entity.destPos=.9F;
            for(float partial:new float[]{0,.375F,1}){
                float expected=(net.minecraft.util.math.MathHelper.sin(.2F+(.9F)*partial)+1)*(.3F+.6F*partial);
                check(Math.abs(((Float)rotation.invoke(renderer,entity,partial))-expected)<.00001F,"native inherited chicken flap interpolation retained");
                GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();GL11.glPushMatrix();
                ClientBossDisplay.clear();
                check(renderer.prepareScale(entity,partial)==.0625F,"actual native prepareScale model units retained");
                Matrix4f actual=matrix();GL11.glPushMatrix();GL11.glLoadIdentity();GL11.glScalef(-1,-1,1);if(i==1)GL11.glScaled(25,25,25);GL11.glTranslatef(0,-1.501F,0);Matrix4f expectedMatrix=matrix();GL11.glPopMatrix();
                check(actual.epsilonEquals(expectedMatrix,.00001F),"actual native pre-render bridge preserves "+(i==1?"25x boss":"unit duck")+" transform");
                ClientBossDisplay.Snapshot snapshot=ClientBossDisplay.current();
                if(i==1)check(snapshot.framesRemaining==100&&snapshot.healthRatio==.5F&&snapshot.name.equals(boss.getDisplayName().getFormattedText())&&!snapshot.darkeningPulse,"real boss display capture retains call, health/name and no darkening");
                else check(snapshot.framesRemaining==0,"ordinary duck variants do not steal boss display");
                int emitted=primitives(()->{try{renderModel.invoke(renderer,entity,0F,0F,0F,0F,0F,.0625F);}catch(Exception ex){throw new RuntimeException(ex);}});
                check(emitted>0,"actual native renderModel phase renders complete supplied ModelChicken");
                check(textures.getTexture(new ResourceLocation("hbm:textures/entity/duck.png")).getGlTextureId()==GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D),"actual chicken draw binds original duck texture");
                GL11.glPopMatrix();check(GL11.glGetError()==0,"native chicken phases have no GL errors");
            }
        }
        // Supplied ownership must survive native RenderChicken's fixed default constructor.
        ModelBase custom=new ModelChicken();
        for(RenderChicken renderer:new RenderChicken[]{new RenderDuck(manager,custom,2.25F),new RenderQuacc(manager,custom,2.25F),new RenderQuackos(manager,custom,2.25F)}){
            check(renderer.getMainModel()==custom,"caller model identity preserved, no silent native-default replacement");
            check(((Float)get(renderer,Render.class,"shadowSize")).floatValue()==2.25F,"caller shadow value preserved");
        }
    }
    static void auditCaseSensitivePack(String[] args)throws Exception {
        ResourceLocation location=new ResourceLocation("hbm:textures/models/chopperBomb.png");
        check(location.getPath().equals("textures/models/chopperbomb.png"),"native ResourceLocation normalizes the existing original literal");
        try(FileResourcePack before=new FileResourcePack(new File(args[2]));FileResourcePack after=new FileResourcePack(new File(args[3]))){
            check(!before.resourceExists(location),"case-sensitive native ZIP lookup reproduces original mixed-case asset failure on any host OS");
            check(after.resourceExists(location),"native ZIP lookup finds the byte-identical lower-case production resource");
            try(InputStream in=after.getInputStream(location)){check(javax.imageio.ImageIO.read(in)!=null,"actual native case-correct ZIP stream decodes original PNG");}
        }
    }
    static void run(String[] args)throws Exception {
        auditCaseSensitivePack(args);
        check(Minecraft.getMinecraft()==null,"registration before client initialization");RendererRegistrationFixture.register();
        Object registry=get(null,RenderingRegistry.class,"INSTANCE");
        check(((Map<?,?>)get(registry,RenderingRegistry.class,"entityRenderers")).size()==24,"all 24 exact extracted registrations use typed factory map");
        check(((Map<?,?>)get(registry,RenderingRegistry.class,"entityRenderersOld")).isEmpty(),"legacy eager registration map remains empty");
        check(Minecraft.getMinecraft()==null,"registration requires no eager global owner/resource lookup");
        Method init=Class.forName("MaterialTextureAtlasSpriteNativeTest").getDeclaredMethod("initializeNativeClientSettings");init.setAccessible(true);init.invoke(null);
        Minecraft mc=Minecraft.getMinecraft();Bootstrap.register();OpenGlHelper.initializeTextures();
        SimpleReloadableResourceManager resources=new SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer());
        try(FileResourcePack vanilla=new FileResourcePack(new File(args[1]))){
            resources.reloadResources(Arrays.asList(vanilla,new FolderResourcePack(new File(args[0]))));set(mc,Minecraft.class,"resourceManager",resources);
            TextureManager textures=new TextureManager(resources);set(mc,Minecraft.class,"renderEngine",textures);
            TextureMap atlas=new TextureMap("textures");set(mc,Minecraft.class,"textureMapBlocks",atlas);
            net.minecraft.client.renderer.block.model.ModelManager models=new net.minecraft.client.renderer.block.model.ModelManager(atlas);
            set(models,net.minecraft.client.renderer.block.model.ModelManager.class,"modelRegistry",new net.minecraft.util.registry.RegistrySimple<>());
            RenderItem item=new RenderItem(textures,models,new ItemColors());set(mc,Minecraft.class,"renderItem",item);
            check(mc.getRenderManager()==null,"native callback occurs before global manager publication");
            RenderManager manager=new RenderManager(textures,item);manager.options=mc.gameSettings;
            check(mc.getRenderManager()==null,"constructor/factory closure does not secretly publish or read global owner");
            Class<?>[] entities=RendererRegistrationFixture.entities(),renderers=RendererRegistrationFixture.renderers();
            for(int i=0;i<entities.length;i++){
                Render<?> renderer=manager.entityRenderMap.get(entities[i]);
                check(renderer!=null&&renderer.getClass()==renderers[i],"factory preserves exact per-entity renderer ownership");
                check(get(renderer,Render.class,"renderManager")==manager,"native inherited manager is exact callback identity");
                // All source-derived constructor/field projections execute their original
                // model and mesh initialization, never fake placeholder initializers.
                for(Field f:renderer.getClass().getDeclaredFields()){
                    f.setAccessible(true);Object v=f.get(renderer);
                    if(MeshModel.class.isAssignableFrom(f.getType()))check(v!=null&&!((MeshModel)v).getMesh().parts.isEmpty(),"actual projected/full constructor loads nonempty original mesh "+f);
                    if(ModelBase.class.isAssignableFrom(f.getType()))check(v!=null,"actual projected/full constructor owns original model "+f);
                }
            }
            set(mc,Minecraft.class,"renderManager",manager);
            directDraw(manager,textures);chickenPhases(manager,textures);
            RenderManager other=new RenderManager(textures,item);
            for(Class<?> type:entities){Render<?> first=manager.entityRenderMap.get(type),second=other.entityRenderMap.get(type);check(first!=second&&get(second,Render.class,"renderManager")==other,"factory replays with fresh renderer and current callback, not stale global manager");}
            // This class has no live registration; constructor remains valid without inventing one.
            RenderBalls balls=new RenderBalls(other);check(get(balls,Render.class,"renderManager")==other,"unregistered Balls constructor projection threads owner");
            check(((Float)get(balls,Render.class,"shadowOpaque")).floatValue()==0,"unregistered original shadow opacity retained");
        }
        auditLifecycle();
    }
    public static void main(String[] args)throws Exception {
        Constructor<?> ctor=Class.forName("MaterialTextureAtlasSpriteNativeTest$Context").getDeclaredConstructor();ctor.setAccessible(true);
        try(AutoCloseable context=(AutoCloseable)ctor.newInstance()){
            run(args);System.out.println("PASS "+checks+" native factory/constructor/ownership/bridge/EGL phase checks. Seven complete production renderer classes, sixteen constructor+field projections; no full ClientProxy, entity simulation, client boot, or visual-parity claim.");
        }
    }
}
