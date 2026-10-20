import com.hbm.particle.*;
import java.lang.reflect.*;
import java.nio.*;
import java.util.*;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.vertex.*;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

/** Complete production particles against native APIs; no GL/game-acceptance claim. */
public final class NativeAtlasParticlesTest {
 static int checks;
 static void check(boolean b,String label){checks++;if(!b)throw new AssertionError(label);}
 static void close(double a,double b,String label){check(Math.abs(a-b)<1E-6,label+": "+a+" != "+b);}
 static Field field(Class<?> c,String name)throws Exception{while(c!=null){try{Field f=c.getDeclaredField(name);f.setAccessible(true);return f;}catch(NoSuchFieldException e){c=c.getSuperclass();}}throw new NoSuchFieldException(name);}
 static Object get(Object o,String name)throws Exception{return field(o.getClass(),name).get(o);}
 static double number(Object o,String name)throws Exception{return ((Number)get(o,name)).doubleValue();}
 static void set(Object o,String name,Object v)throws Exception{field(o.getClass(),name).set(o,v);}
 // Controlled light source using the real World API; native Particle's sampling implementation remains active.
 static final class LightWorld extends World {
  BlockPos sampled;
  LightWorld(){super(null,null,null,null,true);}
  protected IChunkProvider createChunkProvider(){return null;}
  protected boolean isChunkLoaded(int x,int z,boolean allowEmpty){return true;}
  public int getCombinedLight(BlockPos pos,int minimum){sampled=pos;return 0x00A00050;}
 }
 static LightWorld world()throws Exception{
  Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);
  LightWorld w=(LightWorld)((sun.misc.Unsafe)f.get(null)).allocateInstance(LightWorld.class);
  set(w,"rand",new Random(77));set(w,"profiler",new Profiler());return w;
 }
 static void sprites(String resourceRoot)throws Exception{
  TextureMap map=new TextureMap("textures");ParticleSprites.register(map);
  String[] fields={"base","leaf","swen","len","splash","ashes","flare"};
  String[] names={"particle_base","dead_leaf","particlenote2","particlenote1","particle_splash","particle_ashes","yelflare"};
  TextureAtlasSprite[] first=new TextureAtlasSprite[fields.length];
  for(int i=0;i<fields.length;i++){first[i]=(TextureAtlasSprite)ParticleSprites.class.getField(fields[i]).get(null);check(first[i]==map.getTextureExtry("hbm:blocks/particle/"+names[i]),"actual native atlas identity "+names[i]);}
  net.minecraft.client.resources.SimpleReloadableResourceManager resources = new net.minecraft.client.resources.SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer());
  resources.reloadResourcePack(new net.minecraft.client.resources.FolderResourcePack(new java.io.File(resourceRoot)));
  for(int i=0;i<fields.length;i++){
   ResourceLocation png=new ResourceLocation("hbm","textures/blocks/particle/"+names[i]+".png");
   try(net.minecraft.client.resources.IResource resource=resources.getResource(png)){first[i].loadSprite(net.minecraft.client.renderer.texture.PngSizeInfo.makeFromResource(resource),false);}
   check(first[i].getIconWidth()==first[i].getIconHeight(),"native PNG header and ordinary-sprite aspect validation "+names[i]);
   java.awt.image.BufferedImage image=javax.imageio.ImageIO.read(new java.io.File(resourceRoot,"assets/hbm/textures/blocks/particle/"+names[i]+".png"));
   check(first[i].getIconWidth()==image.getWidth()&&first[i].getIconHeight()==image.getHeight()&&image.getRGB(0,0,image.getWidth(),image.getHeight(),null,0,image.getWidth()).length==image.getWidth()*image.getHeight(),"actual PNG pixel decode matches native dimensions "+names[i]);
  }
  ParticleSprites.register(map);for(int i=0;i<fields.length;i++)check(first[i]==ParticleSprites.class.getField(fields[i]).get(null),"same map deduplicates");
  ParticleSprites.register(new TextureMap("textures"));for(int i=0;i<fields.length;i++)check(first[i]!=ParticleSprites.class.getField(fields[i]).get(null),"new map refreshes references");
  for(String name:fields){TextureAtlasSprite s=(TextureAtlasSprite)ParticleSprites.class.getField(name).get(null);s.setIconWidth(16);s.setIconHeight(16);s.initSprite(128,128,32,48,false);}
 }
 static Particle[] particles(LightWorld world){return new Particle[]{new ParticleGlow(world,2,64,3,.25,.5,-.75,4),new ParticleDigammaSmoke(null,world,2,64,3),new ParticleDust(world,2,64,3,.25,.5,-.75,4),new ParticleCoolingTower(null,world,2,64,3)};}
 static void lifecycle()throws Exception{
  LightWorld w=world();Particle[] ps=particles(w);
  for(Particle p:ps){check(p.getFXLayer()==1&&p.shouldDisableDepth(),"native no-depth atlas layer");check(!(Boolean)get(p,"canCollide"),"original noClip intent");close(number(p,"prevPosX"),2,"native first-frame x");close(number(p,"prevPosY"),64,"native first-frame y");close(number(p,"prevPosZ"),3,"native first-frame z");}
  ParticleGlow glow=(ParticleGlow)ps[0];int max=(int)number(glow,"particleMaxAge");check(max==50+new Random(77).nextInt(50),"world-RNG glow lifetime");glow.onUpdate();close(number(glow,"posX"),2.25,"glow movement before drag");close(number(glow,"motionX"),.25*.9800000190734863,"glow native drag");close(number(glow,"particleAge"),2,"glow starts age1");float t=2F/max;t=Math.abs(.5F-(t-.5F))*2;close(number(glow,"particleScale"),4*(float)Math.sin(t*Math.PI/2),"unchanged glow formula");check(glow.getBrightnessForRender(0)==15728880,"glow full bright");
  set(glow,"particleAge",max);glow.onUpdate();check(!glow.isAlive(),"glow postincrement expiry");
  ParticleDust dust=(ParticleDust)ps[2];dust.onUpdate();close(number(dust,"particleScale"),4,"dust original scale");close(number(dust,"posY"),64.5,"dust no-collision motion");check(dust.getBrightnessForRender(.5F)==0x00A00050,"native environmental brightness");check(w.sampled.equals(new BlockPos(2,64,2)),"approved native light sampling position");
  ParticleDigammaSmoke smoke=(ParticleDigammaSmoke)ps[1];smoke.maxAge=3;smoke.setMotion(2,3,4);smoke.onUpdate();close(number(smoke,"posX"),3.98,"digamma damp before move");close(number(smoke,"particleAlpha"),1,"digamma alpha before increment");check(smoke.isAlive(),"digamma alive age1");smoke.onUpdate();close(number(smoke,"particleAlpha"),1-1F/3,"digamma original alpha");smoke.onUpdate();check(!smoke.isAlive(),"digamma equality expiry");check(smoke.getBrightnessForRender(0)==240,"digamma block-only brightness");
  ParticleCoolingTower tower=(ParticleCoolingTower)ps[3];tower.setLife(3);tower.setLift(0);tower.setStrafe(0);tower.noWind();tower.setBaseScale(1);tower.setMaxScale(2);tower.alphaMod(.5F);tower.setMotion(1,2,3);tower.onUpdate();close(number(tower,"particleScale"),2,"tower original squared formula at0");close(number(tower,"particleAlpha"),.5,"tower alpha0");close(number(tower,"posX"),3,"tower move before drag");close(number(tower,"motionX"),.925,"tower drag");tower.onUpdate();tower.onUpdate();check(!tower.isAlive(),"tower equality expiry");
  ParticleCoolingTower wind=new ParticleCoolingTower(null,w,0,64,0);wind.setLife(10);set(wind,"particleAge",5);set(wind,"rand",new Random(9));Random oracle=new Random(9);double mx=oracle.nextGaussian()*.075F*.5+.02*.5,mz=oracle.nextGaussian()*.075F*.5-.01*.5;wind.onUpdate();close(number(wind,"posX"),mx,"tower strafe/wind x");close(number(wind,"posZ"),mz,"tower strafe/wind z");close(number(wind,"posY"),64+(double).01F,"tower lift");
 }
 static void vertices()throws Exception{
  LightWorld w=world();Particle[] ps=particles(w);BufferBuilder b=new BufferBuilder(1024);
  for(int j=0;j<ps.length;j++){
   Particle p=ps[j];set(p,"particleScale",4F);set(p,"particleRed",.7F);set(p,"particleGreen",.2F);set(p,"particleBlue",.6F);set(p,"particleAlpha",.8F);
   set(p,"prevPosX",1D);set(p,"prevPosY",63D);set(p,"prevPosZ",2D);p.setPosition(3,65,4);
   Particle.interpPosX=.5;Particle.interpPosY=60;Particle.interpPosZ=1;
   float t=.25F,s=j==0?.4F:4F;float x=1F,y=3.5F,z=1.5F;
   float[][] expected={{x-s,y,z-s},{x-s,y,z+s},{x+s,y,z+s},{x+s,y,z-s}};
   b.begin(7,DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);p.renderParticle(b,null,t,1,0,0,0,1);p.renderParticle(b,null,t,1,0,0,0,1);b.finishDrawing();
   check(b.getVertexCount()==8,"complete renderer appends without begin/draw");ByteBuffer data=b.getByteBuffer().order(ByteOrder.nativeOrder());TextureAtlasSprite sprite=(TextureAtlasSprite)get(p,"particleTexture");check(sprite==(j==0?ParticleSprites.flare:ParticleSprites.base),"production native sprite route");
   float[][] uv={{sprite.getMaxU(),sprite.getMaxV()},{sprite.getMaxU(),sprite.getMinV()},{sprite.getMinU(),sprite.getMinV()},{sprite.getMinU(),sprite.getMaxV()}};
   int brightness=p.getBrightnessForRender(t);
   for(int i=0;i<8;i++){int o=i*28;for(int axis=0;axis<3;axis++)close(data.getFloat(o+axis*4),expected[i%4][axis],"original effect scale/geometry");close(data.getFloat(o+12),uv[i%4][0],"u");close(data.getFloat(o+16),uv[i%4][1],"v");check((data.get(o+20)&255)==178&&(data.get(o+21)&255)==51&&(data.get(o+22)&255)==153&&(data.get(o+23)&255)==204,"per-vertex RGBA");check((data.getShort(o+24)&65535)==(brightness&65535)&&(data.getShort(o+26)&65535)==((brightness>>16)&65535),"per-vertex lightmap");}
   b.reset();
  }
 }
 static void manager()throws Exception{
  LightWorld w=world();ParticleManager manager=new ParticleManager(w,null);ParticleGlow p=new ParticleGlow(w,1,64,2,0,0,0,1);manager.addEffect(p);check(manager.getStatistics().equals("0"),"native queued admission");manager.updateEffects();check(manager.getStatistics().equals("1")&&number(p,"particleAge")==1,"first drain does not tick");manager.updateEffects();check(number(p,"particleAge")==2,"following native update ticks");p.setExpired();manager.updateEffects();check(manager.getStatistics().equals("0"),"native expiry removal");
  Particle oldest=new ParticleGlow(w,0,64,0,0,0,0,1);manager.addEffect(oldest);for(int i=0;i<16384;i++)manager.addEffect(new ParticleGlow(w,0,64,0,0,0,0,1));manager.updateEffects();Object[][] layers=(Object[][])get(manager,"fxLayers");ArrayDeque<?> bucket=(ArrayDeque<?>)layers[1][0];check(bucket.size()==16384&&!bucket.contains(oldest),"approved native cap and oldest eviction");check(((ArrayDeque<?>)layers[1][1]).isEmpty(),"explicit original no-depth route");
 }
 public static void main(String[] args)throws Exception{sprites(args[0]);lifecycle();vertices();manager();System.out.println("PASS "+checks+" full production native particle/sprite/buffer/lifecycle checks; no GL or full-mod acceptance.");}
}
