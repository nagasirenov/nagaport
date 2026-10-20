import com.hbm.particle.*;
import com.hbm.particle.helper.*;
import com.hbm.handler.CasingEjector;
import com.hbm.main.ModSoundEvents;
import com.hbm.wiaj.WorldInAJar;
import java.lang.reflect.*;
import java.util.*;
import java.nio.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.*;
import net.minecraft.world.storage.*;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.lwjgl.opengl.GL11;

/** Whole native production particles and original mesh assets; controlled NTM registry/transport boundaries. */
public class NativeRemainingParticlesTest {
 static int checks;static Minecraft mc;static TextureManager textures;static Framebuffer frame;
 static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 static Field field(Class<?> c,String name)throws Exception{while(c!=null){try{Field f=c.getDeclaredField(name);f.setAccessible(true);return f;}catch(NoSuchFieldException e){c=c.getSuperclass();}}throw new NoSuchFieldException(name);}
 static void set(Object o,String name,Object value)throws Exception{field(o.getClass(),name).set(o,value);}
 static Object get(Object o,String name)throws Exception{return field(o.getClass(),name).get(o);}
 static double number(Object o,String name)throws Exception{return ((Number)get(o,name)).doubleValue();}
 static void close(double a,double b,String message){check(Math.abs(a-b)<1e-5,message+": "+a+" != "+b);}
 static class NativeWorld extends World {
  final Map<Long,Chunk> chunks=new HashMap<>();Entity represented;int sounds;SoundEvent lastSound;SoundCategory lastCategory;float volume,pitch;
  NativeWorld(){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),true);provider.setWorld(this);chunkProvider=createChunkProvider();for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){Chunk c=new Chunk(this,x,z);c.markLoaded(true);chunks.put(key(x,z),c);}}
  static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
  protected IChunkProvider createChunkProvider(){return new IChunkProvider(){public Chunk getLoadedChunk(int x,int z){return chunks.get(key(x,z));}public Chunk provideChunk(int x,int z){return getLoadedChunk(x,z);}public boolean tick(){return false;}public String makeString(){return "native preloaded chunks";}public boolean isChunkGeneratedAt(int x,int z){return chunks.containsKey(key(x,z));}};}
  protected boolean isChunkLoaded(int x,int z,boolean empty){return chunks.containsKey(key(x,z));}
  public Entity getEntityByID(int id){return represented!=null&&represented.getEntityId()==id?represented:null;}
  public boolean checkLight(BlockPos p){return true;}
  public int getCombinedLight(BlockPos p,int min){return 0x00F000F0;}
  public void playSound(double x,double y,double z,SoundEvent sound,SoundCategory category,float volume,float pitch,boolean delay){sounds++;lastSound=sound;lastCategory=category;this.volume=volume;this.pitch=pitch;}
 }
 static TextureAtlasSprite sprite(){TextureAtlasSprite s=new TextureAtlasSprite("probe:particle"){};s.setIconWidth(16);s.setIconHeight(16);s.initSprite(16,16,0,0,false);return s;}
 static void setup(String resourcesPath)throws Exception {
  mc=Minecraft.getMinecraft();OpenGlHelper.initializeTextures();SimpleReloadableResourceManager resources=new SimpleReloadableResourceManager(new MetadataSerializer());java.net.URL nativeSource=TextureMap.class.getProtectionDomain().getCodeSource().getLocation();if(nativeSource.getProtocol().equals("jar"))nativeSource=((java.net.JarURLConnection)nativeSource.openConnection()).getJarFileURL();resources.reloadResourcePack(new FileResourcePack(new java.io.File(nativeSource.toURI())));resources.reloadResourcePack(new FolderResourcePack(new java.io.File(resourcesPath)));set(mc,"resourceManager",resources);textures=new TextureManager(resources);mc.renderEngine=textures;
  mc.fontRenderer=new net.minecraft.client.gui.FontRenderer(mc.gameSettings,new ResourceLocation("textures/font/ascii.png"),textures,false);mc.fontRenderer.onResourceManagerReload(resources);
  DynamicTexture image=new DynamicTexture(16,16);Arrays.fill(image.getTextureData(),0xFFFFFFFF);image.updateDynamicTexture();textures.loadTexture(TextureMap.LOCATION_BLOCKS_TEXTURE,image);
  TextureMap atlas=new TextureMap("textures");ParticleSprites.register(atlas);ParticleSprites.base=sprite();set(mc,"textureMapBlocks",atlas);
  TextureAtlasSprite sprite=ParticleSprites.base;List<BakedQuad> quads=new ArrayList<>();FaceBakery bakery=new FaceBakery();for(EnumFacing f:EnumFacing.values())quads.add(bakery.makeBakedQuad(new org.lwjgl.util.vector.Vector3f(0,0,0),new org.lwjgl.util.vector.Vector3f(16,16,16),new BlockPartFace(null,-1,"probe",new BlockFaceUV(new float[]{0,0,16,16},0)),sprite,f,ModelRotation.X0_Y0,null,false,true));
  IBakedModel cube=new SimpleBakedModel(quads,new EnumMap<>(EnumFacing.class),false,true,sprite,ItemCameraTransforms.DEFAULT,ItemOverrideList.NONE){public List<BakedQuad> getQuads(net.minecraft.block.state.IBlockState state,EnumFacing face,long seed){return face==null?super.getQuads(state,null,seed):Collections.emptyList();}};
  ModelManager manager=new ModelManager(atlas);set(manager,"defaultModel",cube);set(mc,"modelManager",manager);BlockColors colors=BlockColors.init();set(mc,"blockColors",colors);set(mc,"blockRenderDispatcher",new BlockRendererDispatcher(manager.getBlockModelShapes(),colors));frame=new Framebuffer(64,64,true);
 }
 static Particle[] roots(NativeWorld w){return new Particle[]{new ParticleMukeCloud(textures,w,2,4,3,.1,.2,.3),new ParticleMukeCloudBF(textures,w,2,4,3,.1,.2,.3),new ParticleNT(w,2,4,3,new ParticleDefinition()),new ParticleGiblet(textures,w,2,4,3,.1,.2,.3,2),new ParticleRift(textures,w,2,4,3),new ParticleRocketFlame(textures,w,2,4,3),new ParticleSpentCasing(textures,w,2,4,3,.1,.2,.3,2,3,new SpentCasing(SpentCasing.CasingType.STRAIGHT).setColor(0xffcc88).setSound(null),false,0,0,0),new ParticleSkeleton(textures,w,2,4,3,1,1,1,SkeletonCreator.EnumSkeletonType.SKULL),new ParticleDebris(w,2,4,3,.1,.2,.3),new ParticleFoam(textures,w,2,4,3),new ParticleSmokePlume(textures,w,2,4,3),new ParticleSpark(w,2,4,3,.1,.2,.3),new ParticleLetter(w,2,4,3,0xabcdef,'X'),new ParticleMukeFlash(textures,w,2,4,3,false)};}
 static void lifecycle()throws Exception {
  NativeWorld w=new NativeWorld();mc.effectRenderer=new ParticleManager(w,textures);Particle[] particles=roots(w);
  for(Particle p:particles){check(!(p instanceof Object&&Entity.class.isAssignableFrom(p.getClass())),"real Particle not removed EntityFX");close(number(p,"prevPosX"),2,"native initial interpolation x");close(number(p,"prevPosY"),4,"native initial interpolation y");if(p instanceof ParticleDebris)((ParticleDebris)p).world=new WorldInAJar(1,1,1);p.onUpdate();check(p.isAlive(),"representative first tick survives "+p.getClass().getSimpleName());}
  ParticleRocketFlame rocket=new ParticleRocketFlame(textures,w,0,4,0);rocket.setMotion(1,2,3);rocket.setMaxAge(2);rocket.onUpdate();close(number(rocket,"posX"),.91,"rocket damp-before-native move");close(number(rocket,"motionY"),1.82,"rocket vertical drag");rocket.onUpdate();check(!rocket.isAlive(),"rocket exact custom expiry");
  for(double pressure:new double[]{1,.1,.01}){ParticleRocketFlame r=new ParticleRocketFlame(textures,w,0,4,0);r.setMaxAge(100);r.setAtmosphericPressure(pressure);check(number(r,"maxAge")== (pressure<.05?25:pressure<.2?50:100),"original atmospheric lifetime threshold");}
  ParticleMukeCloud cloud=new ParticleMukeCloud(textures,w,0,4,0,1,0,0);cloud.onUpdate();close(number(cloud,"motionX"),.95F,"ground cloud original drag");check(!(Boolean)get(cloud,"canCollide"),"initial cloud collision grace");set(cloud,"particleAge",3);cloud.onUpdate();check((Boolean)get(cloud,"canCollide"),"cloud restores collision after initial grace");
  ParticleSkeleton skeleton=new ParticleSkeleton(textures,w,0,4,0,1,1,1,SkeletonCreator.EnumSkeletonType.LIMB);for(int i=0;i<20;i++)skeleton.onUpdate();close(number(skeleton,"posY"),4,"skeleton original delay");skeleton.onUpdate();check(number(skeleton,"posY")<4,"skeleton starts moving after delay");
  ParticleMukeFlash flash=new ParticleMukeFlash(textures,w,0,4,0,true);mc.effectRenderer=new ParticleManager(w,textures);set(flash,"particleAge",14);flash.onUpdate();check(mc.effectRenderer.getStatistics().equals("0"),"nested flash clouds use genuine native pending queue");mc.effectRenderer.updateEffects();check(Integer.parseInt(mc.effectRenderer.getStatistics())==193,"flash retains original stem/ground/mush count under native queue");
  ParticleNT nt=new ParticleNT(w,0,4,0,1,2,3,new ParticleDefinition());close(number(nt,"motionZ"),3,"NT exact supplied native motion");
 }
 static void collisions()throws Exception {
  NativeWorld w=new NativeWorld();for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)w.setBlockState(new BlockPos(x,0,z),Blocks.STONE.getDefaultState(),3);
  ParticleSpark spark=new ParticleSpark(w,0,1.1,0,0,0,0);set(spark,"motionX",0D);set(spark,"motionY",-.5D);set(spark,"motionZ",0D);spark.onUpdate();close(number(spark,"posY"),1,"spark actual block collision");check(number(spark,"motionY")>0&&!(Boolean)get(spark,"onGround"),"spark original rebound");
  ParticleFoam foam=new ParticleFoam(textures,w,0,1.1,0);foam.setMotion(0,-1,0);foam.setBuoyancy(0);foam.onUpdate();check(!foam.isAlive(),"foam expires on actual native ground collision");
  ParticleSmokePlume plume=new ParticleSmokePlume(textures,w,0,1.1,0);plume.setMotion(0,-1,0);plume.onUpdate();check(number(plume,"motionY")>0,"plume native vertical clipping restores upward speed");
  SpentCasing config=new SpentCasing(SpentCasing.CasingType.STRAIGHT).setColor(0xffdd88).setSound(null);ParticleSpentCasing casing=new ParticleSpentCasing(textures,w,0,1.1,0,0,-1,0,2,3,config,false,0,0,0);casing.onUpdate();close(number(casing,"posY"),1,"casing native immutable box clips to floor");check(number(casing,"motionY")>0,"casing preserves vertical bounce, not vanilla vertical stop");
  config.setSound("probe:unregistered");ParticleSpentCasing missing=new ParticleSpentCasing(textures,w,0,1.1,0,0,-1,0,0,0,config,false,0,0,0);missing.onUpdate();check(w.sounds==0&&missing.isAlive(),"missing custom sound remains silent, not fatal");
  SoundEvent external=new SoundEvent(new ResourceLocation("probe:registered")).setRegistryName("probe:registered");ForgeRegistries.SOUND_EVENTS.register(external);config.setSound("probe:registered");ParticleSpentCasing other=new ParticleSpentCasing(textures,w,0,1.1,0,0,-1,0,0,0,config,false,0,0,0);other.onUpdate();check(w.sounds==1&&w.lastSound==external&&w.volume==.5F,"registered foreign casing sound retained");
  ParticleSkeleton bone=new ParticleSkeleton(textures,w,0,1.1,0,1,1,1,SkeletonCreator.EnumSkeletonType.SKULL).makeGib();bone.setMotion(0,-1,0);bone.onUpdate();check(w.sounds==2&&w.lastSound==SoundEvents.ENTITY_SKELETON_HURT&&w.lastCategory==SoundCategory.HOSTILE&&w.volume==.25F,"original skeleton landing sound maps to actual vanilla event");
 }
 static void atlas()throws Exception {
  NativeWorld w=new NativeWorld();List<Particle> list=new ArrayList<>();list.add(new ParticleRocketFlame(textures,w,0,4,0));list.add(new ParticleRocketBF(textures,w,0,4,0));list.add(new ParticleRocketFusion(textures,w,0,4,0));list.add(new ParticleRocketMUD(textures,w,0,4,0));list.add(new ParticleRocketSCH(textures,w,0,4,0));list.add(new ParticleRocketUP(textures,w,0,4,0));list.add(new ParticleFoam(textures,w,0,4,0));
  for(Particle p:list){p.onUpdate();BufferBuilder buffer=new BufferBuilder(65536);buffer.begin(7,DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);p.renderParticle(buffer,null,.5F,1,0,0,0,1);p.renderParticle(buffer,null,.5F,1,0,0,0,1);buffer.finishDrawing();check(buffer.getVertexCount()>=32&&buffer.getVertexCount()%8==0,"actual atlas callback appends quads without taking batch ownership");ByteBuffer bytes=buffer.getByteBuffer().order(ByteOrder.nativeOrder());for(int i=0;i<buffer.getVertexCount();i++)for(int axis=0;axis<3;axis++)check(Float.isFinite(bytes.getFloat(i*28+axis*4)),"finite native atlas geometry");}
 }
 static void reset(){frame.bindFramebuffer(true);GL11.glViewport(0,0,64,64);GlStateManager.matrixMode(GL11.GL_PROJECTION);GlStateManager.loadIdentity();GL11.glOrtho(-8,8,-8,8,-100,100);GlStateManager.matrixMode(GL11.GL_MODELVIEW);GlStateManager.loadIdentity();GlStateManager.enableTexture2D();GlStateManager.disableCull();GlStateManager.disableLighting();GlStateManager.disableDepth();GlStateManager.depthMask(true);GlStateManager.color(1,1,1,1);GlStateManager.clearColor(0,0,0,0);GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);}
 static void ownedDraws()throws Exception {
  NativeWorld w=new NativeWorld();Entity camera=new EntityArmorStand(w);camera.setPosition(0,0,0);Particle.interpPosX=Particle.interpPosY=Particle.interpPosZ=0;
  for(Particle p:roots(w)){if(p.getFXLayer()!=3)continue;if(p instanceof ParticleDebris){((ParticleDebris)p).world=new WorldInAJar(1,1,1);((ParticleDebris)p).world.setBlock(0,0,0,Blocks.STONE,0);}if(p instanceof ParticleSpark){set(p,"motionX",1D);set(p,"motionY",0D);set(p,"motionZ",0D);p.onUpdate();p.onUpdate();}set(p,"particleAge",10);if(p instanceof ParticleGiblet)set(p,"particleScale",10F);reset();if(p instanceof ParticleSpentCasing){GlStateManager.scale(12,12,12);GlStateManager.rotate(45,1,0,0);GlStateManager.translate(-2,-4,-3);}int depth=GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH);p.renderParticle(Tessellator.getInstance().getBuffer(),camera,.5F,1,1,0,0,1);check(GL11.glGetError()==0,"actual native owned draw has no GL error: "+p.getClass().getSimpleName());check(GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH)==depth,"owned draw balances matrix "+p.getClass().getSimpleName());ByteBuffer pixels=org.lwjgl.BufferUtils.createByteBuffer(64*64*4);GL11.glReadPixels(0,0,64,64,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,pixels);boolean visible=false;for(int i=0;i<pixels.capacity();i+=4)visible|=pixels.get(i)!=0||pixels.get(i+1)!=0||pixels.get(i+2)!=0;check(visible,"real asset/native draw produces pixels "+p.getClass().getSimpleName());}
 }
 static List<Particle> admitted(ParticleManager manager)throws Exception {
  List<Particle> result=new ArrayList<>();Object[][] layers=(Object[][])get(manager,"fxLayers");for(Object[] layer:layers)for(Object queue:layer)for(Object value:(Iterable<?>)queue)result.add((Particle)value);return result;
 }
 static void creators()throws Exception {
  NativeWorld w=new NativeWorld();mc.effectRenderer=new ParticleManager(w,textures);
  for(int type:new int[]{0,1})new ParticleGiblet(textures,w,0,4,0,0,0,0,type).onUpdate();mc.effectRenderer.updateEffects();List<Particle> dust=admitted(mc.effectRenderer);check(dust.size()==2,"both actual giblet blood/slime native dust factories enqueue");for(Particle p:dust)check(p instanceof ParticleBlockDust&&number(p,"particleMaxAge")>=20&&number(p,"particleMaxAge")<40,"native block dust receives original lifetime without reflection");
  mc.effectRenderer=new ParticleManager(w,textures);net.minecraft.entity.monster.EntityZombie zombie=new net.minecraft.entity.monster.EntityZombie(w);zombie.setPosition(2,4,3);w.represented=zombie;NBTTagCompound data=new NBTTagCompound();data.setInteger("entityID",zombie.getEntityId());data.setFloat("brightness",.7F);new SkeletonCreator().makeParticle(w,null,textures,new Random(3),0,0,0,data);check(com.hbm.main.ClientProxy.vanished==zombie.getEntityId(),"whole creator retains exact original entity-vanish callback");mc.effectRenderer.updateEffects();List<Particle> bones=admitted(mc.effectRenderer);check(bones.size()==6,"whole original zombie bone layout creates six native parts");for(Particle bone:bones)check(bone instanceof ParticleSkeleton&&Math.abs(bone.getRedColorF()-.7F)<1e-6,"original bone tint and type");
  mc.effectRenderer=new ParticleManager(w,textures);SpentCasing config=new SpentCasing(SpentCasing.CasingType.STRAIGHT).setColor(0xffcc88).setSound(null).register("native-probe");data=new NBTTagCompound();data.setString("name","native-probe");data.setDouble("mX",1);data.setDouble("mY",2);data.setDouble("mZ",3);data.setFloat("yaw",37);data.setFloat("pitch",12);new CasingCreator().makeParticle(w,null,textures,new Random(2),2,4,3,data);mc.effectRenderer.updateEffects();ParticleSpentCasing casing=(ParticleSpentCasing)admitted(mc.effectRenderer).get(0);close(number(casing,"motionY"),2,"whole original casing helper forwards motion");check(casing.rotationYaw==37&&casing.prevRotationYaw==37&&casing.rotationPitch==12,"mesh-specific public orientation fields retain helper semantics");
  mc.effectRenderer=new ParticleManager(w,textures);new CasingEjector().setOffset(.4,.2,.6).setMotion(.1,.2,.3).spawnCasing(textures,config,w,2,4,3,0,0,false);mc.effectRenderer.updateEffects();Particle ejected=admitted(mc.effectRenderer).get(0);close(number(ejected,"posX"),2.4,"native ejector offset");close(number(ejected,"prevPosX"),2.4,"approved native initial interpolation after offset");
 }
 static class NativeSoundCapture extends net.minecraft.client.audio.SoundHandler {
  int count,delay;net.minecraft.client.audio.ISound sound;
  NativeSoundCapture(){super(mc.getResourceManager(),mc.gameSettings);}
  @Override public void playDelayedSound(net.minecraft.client.audio.ISound sound,int delay){this.sound=sound;this.delay=delay;count++;}
 }
 static void ownedSounds()throws Exception {
  net.minecraftforge.fml.common.eventhandler.EventBus bus=new net.minecraftforge.fml.common.eventhandler.EventBus();bus.register(ModSoundEvents.class);bus.post(new net.minecraftforge.event.RegistryEvent.Register<SoundEvent>(net.minecraftforge.registries.RegistryManager.ACTIVE.getName(ForgeRegistries.SOUND_EVENTS),ForgeRegistries.SOUND_EVENTS));bus.unregister(ModSoundEvents.class);
  SoundEvent[] sounds={ModSoundEvents.WEAPON_CASING_SMALL,ModSoundEvents.WEAPON_CASING_MEDIUM,ModSoundEvents.WEAPON_CASING_LARGE,ModSoundEvents.WEAPON_CASING_SHELL,ModSoundEvents.WEAPON_EXPLOSION_LARGE_NEAR,ModSoundEvents.WEAPON_EXPLOSION_LARGE_FAR};for(SoundEvent sound:sounds)check(ForgeRegistries.SOUND_EVENTS.getValue(sound.getRegistryName())==sound,"actual production sound event registered by genuine EventBus");
  NativeWorld w=new NativeWorld();w.setBlockState(new BlockPos(0,0,0),Blocks.STONE.getDefaultState(),3);
  for(int i=0;i<4;i++){SpentCasing config=new SpentCasing(SpentCasing.CasingType.STRAIGHT).setColor(0xffcc88).setSound(sounds[i].getRegistryName().toString());new ParticleSpentCasing(textures,w,.5,1.1,.5,0,-1,0,0,0,config,false,0,0,0).onUpdate();check(w.sounds==i+1&&w.lastSound==sounds[i]&&w.lastCategory==SoundCategory.PLAYERS,"known casing collision reaches exact native event/category");check(w.volume==(i==2?1F:.5F)&&w.pitch>=1F&&w.pitch<1.2F,"original known casing volume/pitch");}
  NativeSoundCapture audio=new NativeSoundCapture();set(mc,"soundHandler",audio);net.minecraft.entity.player.EntityPlayer player=new net.minecraft.entity.player.EntityPlayer(w,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"ParticleProbe")){public boolean isSpectator(){return false;}public boolean isCreative(){return false;}};
  for(int distance:new int[]{0,80,81,200,201}){player.setPosition(distance,4,0);mc.effectRenderer=new ParticleManager(w,textures);NBTTagCompound data=new NBTTagCompound();data.setFloat("soundRange",200);data.setFloat("waveScale",10);data.setByte("cloudCount",(byte)2);data.setFloat("cloudScale",2);data.setFloat("cloudSpeedMult",.5F);int before=audio.count;new ExplosionCreator().makeParticle(w,player,textures,new Random(3),0,4,0,data);check(audio.count==before+(distance<=200?1:0),"original explosion sound radius");if(distance<=200){check(audio.sound instanceof net.minecraft.client.audio.PositionedSoundRecord&&audio.sound.getSoundLocation().equals((distance<=80?sounds[4]:sounds[5]).getRegistryName()),"native positioned near/far event at exact threshold");check(audio.delay==(int)(distance/ExplosionCreator.speedOfSound)&&audio.sound.getCategory()==SoundCategory.BLOCKS&&number(audio.sound,"volume")==1000F&&number(audio.sound,"pitch")>=.9F&&number(audio.sound,"pitch")<1.1F,"original native delayed sound timing/volume/pitch/category");}mc.effectRenderer.updateEffects();List<Particle> particles=admitted(mc.effectRenderer);check(particles.size()==3,"whole explosion creator admits wave and exact cloud count");for(Particle p:particles)if(p instanceof ParticleRocketFlame)check(!(Boolean)get(p,"canCollide")&&number(p,"maxAge")>=70&&number(p,"maxAge")<90,"original explosion smoke no-collision/lifetime settings");}
 }
 static void packetComposition()throws Exception {
  NativeWorld w=new NativeWorld();w.provider.setDimension(37);ExplosionCreator.composeEffect(w,2,4,3,10,2,.5F,25,5,8,200,.75F,1,-2,150);com.hbm.packet.toclient.AuxParticlePacketNT packet=(com.hbm.packet.toclient.AuxParticlePacketNT)com.hbm.handler.threading.PacketThreading.lastPacket;net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint point=com.hbm.handler.threading.PacketThreading.lastPoint;check(point.dimension==37&&point.range==300&&point.x==2&&point.y==4&&point.z==3,"real native target point carries dimension/coordinates/minimum radius");check(packet.data.getString("type").equals("explosionLarge")&&packet.data.hasKey("cloudCount",1)&&packet.data.getByte("cloudCount")==10&&packet.data.hasKey("debrisRetry",2)&&packet.data.getShort("debrisRetry")==-56,"original packet NBT types and odd signed-byte-to-short retry cast retained");check(packet.data.getFloat("cloudScale")==2&&packet.data.getFloat("waveScale")==25&&packet.data.getFloat("soundRange")==150&&packet.x==2&&packet.y==4&&packet.z==3,"original explosion packet payload at explicit transport boundary");
  Entity entity=new EntityArmorStand(w);entity.setPosition(2,4,3);SkeletonCreator.composeEffectGib(w,entity,2.5F);packet=(com.hbm.packet.toclient.AuxParticlePacketNT)com.hbm.handler.threading.PacketThreading.lastPacket;check(packet.data.getInteger("entityID")==entity.getEntityId()&&packet.data.getBoolean("gib")&&packet.data.getFloat("force")==2.5F&&packet.data.getFloat("brightness")==1,"original skeleton gib packet fields retained");
 }
 static void managerRendering()throws Exception {
  NativeWorld w=new NativeWorld();ParticleManager manager=new ParticleManager(w,textures);for(Particle p:roots(w)){if(p instanceof ParticleDebris)((ParticleDebris)p).world=new WorldInAJar(1,1,1);if(p instanceof ParticleGiblet)set(p,"particleScale",10F);manager.addEffect(p);}manager.updateEffects();manager.updateEffects();Entity camera=new EntityArmorStand(w);camera.setPosition(0,0,0);reset();int depth=GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH);manager.renderParticles(camera,.5F);manager.renderLitParticles(camera,.5F);check(GL11.glGetError()==0,"genuine manager both render phases execute complete native callbacks");check(GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH)==depth,"actual manager phases preserve matrix depth");GlStateManager.disableLighting();GlStateManager.enableLighting();check(GL11.glIsEnabled(GL11.GL_LIGHTING),"following native lighting draw remains synchronized");GlStateManager.enableBlend();GlStateManager.disableBlend();check(!GL11.glIsEnabled(GL11.GL_BLEND),"following native blend draw remains synchronized");GlStateManager.disableTexture2D();GlStateManager.enableTexture2D();check(GL11.glIsEnabled(GL11.GL_TEXTURE_2D),"following native texture draw remains synchronized");
 }
 static void manager()throws Exception {
  NativeWorld w=new NativeWorld();ParticleManager manager=new ParticleManager(w,textures);Particle p=new ParticleNT(w,0,4,0,new ParticleDefinition());manager.addEffect(p);check(manager.getStatistics().equals("0"),"native admission delayed");manager.updateEffects();check(manager.getStatistics().equals("1")&&number(p,"particleAge")==0,"first queue drain does not update");manager.updateEffects();check(number(p,"particleAge")==1,"subsequent native tick advances");p.setExpired();manager.updateEffects();check(manager.getStatistics().equals("0"),"native manager removes expired particles");for(int i=0;i<16600;i++)manager.addEffect(new ParticleNT(w,0,4,0,new ParticleDefinition()));manager.updateEffects();check(manager.getStatistics().equals("16384"),"approved genuine per-layer native particle queue cap");
 }
 public static void main(String[] args)throws Exception {
  Bootstrap.register();FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());ModMetadata md=new ModMetadata();md.modId="hbm";md.name="Native particle probe";md.version="1";Loader.instance().setupTestHarness(new DummyModContainer(md));
  Constructor<?> ctor=Class.forName("MaterialTextureAtlasSpriteNativeTest$Context").getDeclaredConstructor();ctor.setAccessible(true);try(AutoCloseable context=(AutoCloseable)ctor.newInstance()){Method init=Class.forName("MaterialTextureAtlasSpriteNativeTest").getDeclaredMethod("initializeNativeClientSettings");init.setAccessible(true);init.invoke(null);setup(args[0]);lifecycle();collisions();atlas();ownedDraws();creators();ownedSounds();packetComposition();managerRendering();manager();}
  System.out.println("PASS "+checks+" actual native remaining particle lifecycle/collision/sound/atlas-buffer/owned GL/manager checks; real original OBJ/VBO/texture draws; controlled registry/math/transport boundaries, not whole-game acceptance.");
 }
}
