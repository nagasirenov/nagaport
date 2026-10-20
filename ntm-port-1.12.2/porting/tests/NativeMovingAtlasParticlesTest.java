import com.hbm.particle.*;
import java.awt.Color;
import java.lang.reflect.*;
import java.nio.*;
import java.util.*;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.vertex.*;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

/** Real native collision/movement/buffer classes, with a controlled real World fixture. */
public final class NativeMovingAtlasParticlesTest {
 static int checks;
 static void check(boolean value,String label){checks++;if(!value)throw new AssertionError(label);}
 static void close(double value,double expected,String label){check(Math.abs(value-expected)<1E-5,label+": "+value+" != "+expected);}
 static Field field(Class<?> type,String name)throws Exception{while(type!=null){try{Field f=type.getDeclaredField(name);f.setAccessible(true);return f;}catch(NoSuchFieldException e){type=type.getSuperclass();}}throw new NoSuchFieldException(name);}
 static Object get(Object target,String name)throws Exception{return field(target.getClass(),name).get(target);}
 static double n(Object target,String name)throws Exception{return ((Number)get(target,name)).doubleValue();}
 static void set(Object target,String name,Object value)throws Exception{field(target.getClass(),name).set(target,value);}
 static final class FixtureWorld extends World {
  List<AxisAlignedBB> boxes; int collisionQueries, smoke; double[] smokeArgs;
  FixtureWorld(){super(null,null,null,null,true);}
  protected IChunkProvider createChunkProvider(){throw new AssertionError("unexpected chunks");}
  protected boolean isChunkLoaded(int x,int z,boolean allowEmpty){return true;}
  public int getCombinedLight(BlockPos pos,int minimum){return 0x00A00050;}
  public List<AxisAlignedBB> getCollisionBoxes(Entity entity,AxisAlignedBB box){check(entity==null,"native particle has no entity callback context");collisionQueries++;return boxes;}
  public void spawnParticle(EnumParticleTypes type,double x,double y,double z,double mx,double my,double mz,int...args){check(type==EnumParticleTypes.SMOKE_NORMAL,"original ash smoke type");smoke++;smokeArgs=new double[]{x,y,z,mx,my,mz};}
 }
 static FixtureWorld world()throws Exception{
  Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);FixtureWorld w=(FixtureWorld)((sun.misc.Unsafe)f.get(null)).allocateInstance(FixtureWorld.class);
  set(w,"rand",new Random(77));set(w,"profiler",new Profiler());set(w,"isRemote",true);w.boxes=new ArrayList<>();return w;
 }
 static void sprites()throws Exception{
  ParticleSprites.register(new TextureMap("textures"));
  for(String name:new String[]{"base","leaf","swen","len","splash"}){TextureAtlasSprite s=(TextureAtlasSprite)ParticleSprites.class.getField(name).get(null);s.setIconWidth(16);s.setIconHeight(16);s.initSprite(128,128,32,48,false);}
 }
 static Particle[] particles(FixtureWorld w){return new Particle[]{new ParticleBlackPowderSmoke(w,2,64,3,2),new ParticleExplosionSmall(w,2,64,3,2,1),new ParticleFlamethrower(w,2,64,3,0),new ParticleAshes(w,2,64,3,2),new ParticleDuoDec(null,w,2,64,3),new ParticleDuoDec2(null,w,2,64,3),new ParticleDeadLeaf(null,w,2,64,3),new ParticleSplash(null,w,2,64,3),new ParticleExSmoke(null,w,2,64,3),new ParticleBlackPowderSpark(w,2,64,3,.2,.3,.4)};}
 static void lifecycle()throws Exception{
  FixtureWorld w=world();Particle[] ps=particles(w);
  for(int i=0;i<ps.length;i++){Particle p=ps[i];check(p instanceof Particle,"native particle inheritance");check(p.getFXLayer()==(i==9?0:1)&&p.shouldDisableDepth(),"native atlas/depth route");close(n(p,"prevPosY"),64,"approved first position");check((Boolean)get(p,"canCollide")==!(i==0||i==1),"native collision intent");}
  // Original custom tick formulas and expiry order, evaluated with actual native movement.
  ParticleBlackPowderSmoke black=(ParticleBlackPowderSmoke)ps[0];black.setMotion(1,2,3);set(black,"particleAge",4);black.setMaxAge(10);set(black,"visualSeed",2);black.onUpdate();close(n(black,"posX"),2.65,"black damp-before-move");close(n(black,"posY"),65.3,"black vertical drag");close(n(black,"spinDegrees"),-.5,"black degree rotation");check(w.collisionQueries==0,"noclip smoke never queries world collision");set(black,"particleAge",9);black.onUpdate();check(!black.isAlive(),"black increment-before expiry");
  ParticleExplosionSmall explosion=(ParticleExplosionSmall)ps[1];explosion.setMotion(1,2,3);set(explosion,"particleGravity",-.02F);explosion.onUpdate();close(n(explosion,"posX"),2.65,"explosion horizontal damping");close(n(explosion,"posY"),66.02,"explosion no vertical damping");
  ParticleFlamethrower flame=(ParticleFlamethrower)ps[2];flame.setMotion(1,2,3);set(flame,"visualSeed",3);flame.onUpdate();close(n(flame,"posY"),65.83,"flame buoyancy after damping");close(n(flame,"spinDegrees"),15,"flame degree rotation");
  for(int i=4;i<=7;i++){Particle p=ps[i];set(p,"motionY",-1D);set(p,"rand",new Random(9));Random r=new Random(9);double x=r.nextGaussian()*.002,z=r.nextGaussian()*.002;p.onUpdate();close(n(p,"motionX"),x,"drift jitter x");close(n(p,"motionZ"),z,"drift jitter z");close(n(p,"motionY"),i==7?-.5:-.025,"fall speed clamp");p.setMaxAge(1);set(p,"particleAge",1);p.onUpdate();check(!p.isAlive(),"inherited postincrement lifetime");}
  ParticleExSmoke ex=(ParticleExSmoke)ps[8];ex.maxAge=2;ex.setMotion(1,2,3);ex.onUpdate();close(n(ex,"posX"),2+.7599999785423279D,"ex original damping");close(n(ex,"particleAlpha"),1,"ex alpha before increment");check(ex.isAlive(),"ex first tick alive");ex.onUpdate();close(n(ex,"particleAlpha"),.5,"ex second alpha");check(!ex.isAlive(),"ex equality expiry");
  ParticleBlackPowderSpark spark=(ParticleBlackPowderSpark)ps[9];set(spark,"particleMaxAge",0);spark.onUpdate();check(!spark.isAlive(),"spark countdown expiry");close(n(spark,"posY"),64+.3-(double).01F,"spark gravity-before-movement");close(n(spark,"motionY"),(.3-(double).01F)*.95,"spark damping");check(spark.getBrightnessForRender(0)==15728880,"spark fullbright");
 }
 static void collision()throws Exception{
  FixtureWorld w=world();w.boxes.add(new AxisAlignedBB(-100,0,-100,100,1,100));w.boxes.add(new AxisAlignedBB(1,1,-100,2,10,100));
  ParticleSplash splash=new ParticleSplash(null,w,0,1.1,0);set(splash,"motionY",-.5D);splash.onUpdate();check(!splash.isAlive()&&(Boolean)get(splash,"onGround"),"splash dies on actual floor clipping");close(n(splash,"posY"),1,"native box uses particle base y");
  ParticleDeadLeaf leaf=new ParticleDeadLeaf(null,w,0,1.1,0);set(leaf,"motionY",-.5D);leaf.onUpdate();check(leaf.isAlive()&&(Boolean)get(leaf,"onGround"),"leaf persists on floor");check(n(leaf,"motionY")<0,"explicitly accepted native clipped-Y behavior");
  ParticleBlackPowderSpark spark=new ParticleBlackPowderSpark(w,0,2,0,3,0,0);spark.onUpdate();close(n(spark,"posX"),1-n(spark,"width")/2,"spark clips against actual wall");close(n(spark,"motionX"),0,"native clipped horizontal velocity");
  ParticleAshes ash=new ParticleAshes(w,0,1.1,0,1);set(ash,"motionY",-.5D);set(ash,"visualSeed",5);set(ash,"rand",new Random(){public int nextInt(int bound){return 0;}public float nextFloat(){return .25F;}});ash.onUpdate();check((Boolean)get(ash,"onGround"),"ashes grounded native collision");close(n(ash,"spinDegrees"),90,"ash randomizes rotation on first landing");check(w.smoke==1,"grounded ash still emits smoke");close(w.smokeArgs[1],1.125,"ash smoke offset");close(w.smokeArgs[4],.05,"ash smoke lift");ash.setMaxAge((int)n(ash,"particleAge")+1);ash.onUpdate();check(!ash.isAlive()&&w.smoke==2,"original ash expiry tick still emits");
 }
 static ByteBuffer render(Particle p,int expected)throws Exception{
  BufferBuilder b=new BufferBuilder(4096);b.begin(7,DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);p.renderParticle(b,null,.25F,1,0,0,0,1);p.renderParticle(b,null,.25F,1,0,0,0,1);b.finishDrawing();check(b.getVertexCount()==expected*2,"actual callback appends without begin/draw: "+p.getClass().getSimpleName());ByteBuffer out=ByteBuffer.allocate(expected*2*28).order(ByteOrder.nativeOrder());ByteBuffer src=b.getByteBuffer().duplicate();src.position(0);src.limit(expected*2*28);out.put(src);out.flip();return out;
 }
 static void vertices()throws Exception{
  FixtureWorld w=world();Particle[] ps=particles(w);Particle.interpPosX=.5;Particle.interpPosY=60;Particle.interpPosZ=1;
  for(int j=0;j<ps.length;j++){
   Particle p=ps[j];set(p,"particleScale",2F);set(p,"particleAge",5);p.setMaxAge(20);set(p,"particleRed",.7F);set(p,"particleGreen",.2F);set(p,"particleBlue",.6F);set(p,"particleAlpha",.8F);set(p,"prevPosX",1D);set(p,"prevPosY",63D);set(p,"prevPosZ",2D);p.setPosition(3,65,4);
   if(j<4){set(p,"previousSpinDegrees",0F);set(p,"spinDegrees",0F);}if(j==6||j==7||j==8)set(p,"visualSeed",4);
   ByteBuffer data=render(p,j==8?24:4);int count=(j==8?24:4)*2;
   int brightness=j==2?240:(j==9?15728880:0x00A00050);
   for(int i=0;i<count;i++){int o=i*28;for(int axis=0;axis<3;axis++)check(Float.isFinite(data.getFloat(o+axis*4)),"finite geometry");check((data.getShort(o+24)&65535)==(brightness&65535)&&(data.getShort(o+26)&65535)==(brightness>>>16),"explicit native lightmap");}
   if(j>=4&&j<=7){float[][] expected={{-1,3.5F,-.5F},{-1,3.5F,3.5F},{3,3.5F,3.5F},{3,3.5F,-.5F}};TextureAtlasSprite sprite=(TextureAtlasSprite)get(p,"particleTexture");boolean flip=j==6||j==7;for(int i=0;i<4;i++){int o=i*28;for(int axis=0;axis<3;axis++)close(data.getFloat(o+axis*4),expected[i][axis],"original quad corners");float u=i<2?sprite.getMaxU():sprite.getMinU();float v=i==0||i==3?sprite.getMaxV():sprite.getMinV();if(flip){u=i<2?sprite.getMinU():sprite.getMaxU();v=i==0||i==3?sprite.getMinV():sprite.getMaxV();}close(data.getFloat(o+12),u,"atlas u/flip");close(data.getFloat(o+16),v,"atlas v/flip");check((data.get(o+20)&255)==178&&(data.get(o+21)&255)==51&&(data.get(o+22)&255)==153&&(data.get(o+23)&255)==204,"original quad RGBA");}}
  }
  // A real rotated quad: original degree-based Rodrigues formula, not native angle radians.
  ParticleBlackPowderSmoke r=new ParticleBlackPowderSmoke(w,0,0,0,1);set(r,"previousSpinDegrees",90F);set(r,"spinDegrees",90F);BufferBuilder b=new BufferBuilder(512);b.begin(7,DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);Particle.interpPosX=Particle.interpPosY=Particle.interpPosZ=0;r.renderParticleRotated(b,0,1,0,0,0,1,2,1.5F,-.5F,.5F,2F,240);b.finishDrawing();ByteBuffer d=b.getByteBuffer().order(ByteOrder.nativeOrder());close(d.getFloat(0),-2,"rotated corner x");close(d.getFloat(8),2,"rotated corner z");check((d.get(20)&255)==255&&(d.get(21)&255)==0&&(d.get(22)&255)==127&&(d.get(23)&255)==255,"RGBA clamps rather than wraps");
  for(int type=0;type<5;type++){ParticleFlamethrower p=new ParticleFlamethrower(w,0,0,0,type);p.setMaxAge(20);set(p,"particleAge",20);ByteBuffer end=render(p,4);for(int i=0;i<8;i++)check((end.get(i*28+23)&255)==0,"all flame variants fade to zero");}
  ParticleAshes ash=new ParticleAshes(w,0,1,0,1);set(ash,"onGround",true);set(ash,"spinDegrees",0F);set(ash,"particleScale",1F);set(ash,"particleAge",21);ash.setMaxAge(20);ByteBuffer d2=render(ash,4);close(d2.getFloat(4),1.05,"ash ground quad elevation");check((d2.get(23)&255)==0,"negative ash final-frame alpha clamps");
 }
 static void manager()throws Exception{
  Particle vanillaFlame = new net.minecraft.client.particle.ParticleFlame.Factory().createParticle(0, world(), 2, 64, 3, 0, 0, 0);
  check(vanillaFlame instanceof net.minecraft.client.particle.ParticleFlame && vanillaFlame.isAlive(), "AshesCreator native flame factory contract");
  check(Math.abs(n(vanillaFlame,"posX")-2)<.1 && Math.abs(n(vanillaFlame,"posY")-64)<.1 && Math.abs(n(vanillaFlame,"posZ")-3)<.1, "native ash companion flame remains at supplied effect position");
  FixtureWorld w=world();ParticleManager m=new ParticleManager(w,null);Particle p=new ParticleDuoDec(null,w,0,64,0);m.addEffect(p);check(m.getStatistics().equals("0"),"native admission queues");m.updateEffects();check(m.getStatistics().equals("1")&&n(p,"particleAge")==0,"first native drain only");m.updateEffects();check(n(p,"particleAge")==1,"next native update ticks");p.setExpired();m.updateEffects();check(m.getStatistics().equals("0"),"expired native removal");
 }
 public static void main(String[] args)throws Exception{sprites();lifecycle();collision();vertices();manager();System.out.println("PASS "+checks+" complete-class native moving-atlas particle checks; no GL/full-mod/game claim.");}
}
