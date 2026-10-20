import com.hbm.particle.*;
import com.hbm.particle.helper.*;
import com.hbm.main.ModSoundEvents;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.*;

/** Actual unchanged ClientProxy branches with native worlds, particles and manager admission. */
public class NativeVanillaParticlesTest extends NativeRemainingParticlesTest {
 static NBTTagCompound packet(String type,String mode){NBTTagCompound n=new NBTTagCompound();n.setString("type",type);n.setString("mode",mode);n.setDouble("posY",4);n.setInteger("count",3);n.setInteger("block",net.minecraft.block.Block.getIdFromBlock(Blocks.STONE));return n;}
 static List<Particle> run(NativeWorld w,EntityPlayer player,NBTTagCompound n)throws Exception {mc.effectRenderer=new ParticleManager(w,textures);NativeVanillaCaller.effect(w,player,n);check(mc.effectRenderer.getStatistics().equals("0"),"constructors do not eagerly enqueue/drain");mc.effectRenderer.updateEffects();List<Particle> ps=admitted(mc.effectRenderer);check(new HashSet<>(ps).size()==ps.size(),"no duplicate particle admission");return ps;}
 static void callers()throws Exception {
  NativeWorld w=new NativeWorld();EntityPlayer player=new EntityPlayer(w,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"probe")){public boolean isSpectator(){return false;}public boolean isCreative(){return false;}};player.setPosition(0,4,0);w.represented=player;
  for(String type:new String[]{"waterSplash","cloudFX2","fireworks","schrabfog","radiation","fluidfill"}){NBTTagCompound n=packet(type,"");List<Particle> ps=run(w,player,n);int expected=type.equals("waterSplash")?10:type.equals("fireworks")?51:type.equals("radiation")?3:1;check(ps.size()==expected,"original "+type+" count");}
  for(String mode:new String[]{"flame","cloud","reddust","bluedust","greendust","blockdust"})check(run(w,player,packet("vanillaburst",mode)).size()==3,"original burst mode/count "+mode);
  for(String mode:new String[]{"flame","smoke","volcano","cloud","reddust","bluedust","greendust","fireworks","largeexplode","townaura","blockdust","colordust"}){
   NBTTagCompound n=packet("vanillaExt",mode);n.setBoolean("noclip",true);n.setInteger("overrideAge",77);n.setDouble("mX",.2);n.setDouble("mY",.3);n.setDouble("mZ",.4);List<Particle> ps=run(w,player,n);check(ps.size()==(mode.equals("largeexplode")?4:1),"extended native mode and secondary count "+mode);Particle p=ps.get(ps.size()-1);check(number(p,"particleMaxAge")==77,"native age override "+mode);check(!(Boolean)get(p,"canCollide"),"packet collision decision "+mode);
   if(mode.equals("townaura")){close(number(p,"motionX"),.2,"aura supplied motion x");close(number(p,"motionY"),.3,"aura supplied motion y");}
   if(mode.equals("volcano")){check(number(p,"motionY")>=2.5&&number(p,"motionY")<3.5,"volcano preserved upward velocity");}
  }
  NBTTagCompound colored=packet("vanillaExt","cloud");colored.setFloat("r",.2F);List<Particle> ps=run(w,player,colored);close(number(ps.get(0),"motionX"),0,"colored cloud still motion");close(number(ps.get(0),"motionY"),0,"colored cloud still vertical");
  check(run(w,player,packet("vanillaExt","unknown")).isEmpty(),"unknown extended mode silent");
  for(String type:new String[]{"jetpack","bnuuy","jetpack_bj","jetpack_dns"})for(int setting=0;setting<3;setting++){mc.gameSettings.particleSetting=setting;NBTTagCompound n=packet(type,"");n.setInteger("player",player.getEntityId());ps=run(w,player,n);check(ps.size()==(setting==2?0:type.equals("jetpack")&&setting==0?4:2),"jetpack setting/count "+type+setting);if(setting!=2&&type.startsWith("jetpack_") )close(number(ps.get(0),"motionX"),player.motionX,"jetpack explicit motion");}
  for(String mode:new String[]{"normal","blood","smoke"})for(int setting=0;setting<3;setting++){mc.gameSettings.particleSetting=setting;NBTTagCompound n=packet("vomit",mode);n.setInteger("entity",player.getEntityId());check(run(w,player,n).size()==3/(setting+1),"vomit count/settings "+mode+setting);}
  NBTTagCompound sweat=packet("sweat","");sweat.setInteger("entity",player.getEntityId());check(run(w,player,sweat).size()==3,"sweat count");
  mc.gameSettings.particleSetting=0;
 }
 static class NamedWorld extends NativeWorld {
  int calls;net.minecraft.util.EnumParticleTypes type;int[] args;
  public void spawnParticle(net.minecraft.util.EnumParticleTypes t,double x,double y,double z,double mx,double my,double mz,int... a){calls++;type=t;args=a;}
 }
 static void named()throws Exception {
  NamedWorld w=new NamedWorld();for(String mode:new String[]{"smoke","flame","reddust","townaura","fireworksSpark"}){int before=w.calls;run(w,null,packet("vanilla",mode));check(w.calls==before+1,"actual vanilla caller resolves original name "+mode);}
  int id=net.minecraft.block.Block.getIdFromBlock(Blocks.WOOL);run(w,null,packet("vanilla","blockdust_"+id+"_5"));check(w.type==net.minecraft.util.EnumParticleTypes.BLOCK_DUST&&w.args[0]==net.minecraft.block.Block.getStateId(Blocks.WOOL.getStateFromMeta(5)),"named block/meta packet maps to native state id");
  run(w,null,packet("vanilla","iconcrack_1"));check(w.type==net.minecraft.util.EnumParticleTypes.ITEM_CRACK&&w.args[0]==1&&w.args[1]==0,"item packet optional metadata defaults to zero");
  int before=w.calls;for(String mode:new String[]{"unknown","blockdust","blockdust_bad"})run(w,null,packet("vanilla",mode));check(w.calls==before,"unknown/malformed optional names stay silent");
 }
 static void nativeEffects()throws Exception {
  NativeWorld w=new NativeWorld();w.setBlockState(new BlockPos(0,0,0),Blocks.STONE.getDefaultState(),3);
  for(boolean collision:new boolean[]{true,false}){Particle p=new NativeVanillaParticles.BlockDust(w,.5,1.1,.5,0,0,0,Blocks.STONE,0);NativeVanillaParticles.setCollision(p,collision);NativeVanillaParticles.setMotion(p,0,-.5,0);p.onUpdate();check(collision?number(p,"posY")>=1:number(p,"posY")<1,"real native floor honors collision "+collision);}
  ParticleManager manager=new ParticleManager(w,textures);mc.effectRenderer=manager;
  for(String mode:new String[]{"flame","smoke","volcano","cloud","reddust","bluedust","greendust","fireworks","largeexplode","townaura","blockdust","colordust"}){NBTTagCompound n=packet("vanillaExt",mode);n.setInteger("overrideAge",3);NativeVanillaCaller.effect(w,null,n);}
  manager.updateEffects();List<Particle> ps=admitted(manager);check(ps.size()==15&&new HashSet<>(ps).size()==15,"all native variants admitted exactly once");manager.updateEffects();net.minecraft.entity.Entity camera=new net.minecraft.entity.item.EntityArmorStand(w);camera.setPosition(0,0,0);reset();int depth=org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL11.GL_MODELVIEW_STACK_DEPTH);manager.renderParticles(camera,.5F);manager.renderLitParticles(camera,.5F);check(org.lwjgl.opengl.GL11.glGetError()==0,"actual native vanilla manager render phases have no GL errors");check(org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL11.GL_MODELVIEW_STACK_DEPTH)==depth,"native vanilla rendering balances matrix");for(int i=0;i<50;i++)manager.updateEffects();check(manager.getStatistics().equals("0"),"native lifecycle expires all variants and secondaries");
  for(int i=0;i<16600;i++)manager.addEffect(new NativeVanillaParticles.Cloud(w,0,4,0,0,0,0));manager.updateEffects();check(manager.getStatistics().equals("16384"),"actual native vanilla queue cap");
 }
 static void smallExplosion()throws Exception {
  NativeWorld w=new NativeWorld();w.setBlockState(new BlockPos(0,3,0),Blocks.STONE.getDefaultState(),3);NativeSoundCapture audio=new NativeSoundCapture();set(mc,"soundHandler",audio);EntityPlayer player=new EntityPlayer(w,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"probe")){public boolean isSpectator(){return false;}public boolean isCreative(){return false;}};
  for(int distance:new int[]{0,80,81,200,201}){player.setPosition(distance,4,0);mc.effectRenderer=new ParticleManager(w,textures);NBTTagCompound n=packet("explosionSmall","");n.setInteger("cloudCount",2);n.setInteger("debris",3);n.setFloat("cloudScale",2);n.setFloat("cloudSpeedMult",.5F);int before=audio.count;new ExplosionSmallCreator().makeParticle(w,player,textures,new Random(3),0,4,0,n);check(audio.count==before+(distance<=200?1:0),"small original sound radius");if(distance<=200){check(audio.sound.getSoundLocation().equals((distance<=80?ModSoundEvents.WEAPON_EXPLOSION_SMALL_NEAR:ModSoundEvents.WEAPON_EXPLOSION_SMALL_FAR).getRegistryName()),"small exact near/far native event");check(audio.delay==(int)(distance/ExplosionSmallCreator.speedOfSound)&&audio.sound.getCategory()==net.minecraft.util.SoundCategory.BLOCKS&&number(audio.sound,"volume")==100F&&number(audio.sound,"pitch")>=.9F&&number(audio.sound,"pitch")<1.1F,"small original sound category/delay/volume/pitch");}mc.effectRenderer.updateEffects();List<Particle> ps=admitted(mc.effectRenderer);check(ps.size()==5&&new HashSet<>(ps).size()==5,"small exact cloud/debris count and no double admission");for(Particle p:ps)if(p instanceof ParticleBlockDust)check(number(p,"particleMaxAge")>=50&&number(p,"particleMaxAge")<70,"small original debris lifetime");}
  w.provider.setDimension(37);ExplosionSmallCreator.composeEffect(w,2,4,3,7,2,.5F);com.hbm.packet.toclient.AuxParticlePacketNT packet=(com.hbm.packet.toclient.AuxParticlePacketNT)com.hbm.handler.threading.PacketThreading.lastPacket;check(packet.data.getString("type").equals("explosionSmall")&&packet.data.getInteger("cloudCount")==7&&packet.data.getInteger("debris")==15&&packet.data.getFloat("cloudScale")==2&&packet.data.getFloat("cloudSpeedMult")==.5F,"small original packet payload");check(com.hbm.handler.threading.PacketThreading.lastPoint.dimension==37&&com.hbm.handler.threading.PacketThreading.lastPoint.range==200,"small native target dimension/range");
 }
 public static void main(String[] args)throws Exception {
  Bootstrap.register();FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());ModMetadata md=new ModMetadata();md.modId="hbm";md.name="Native vanilla particle probe";md.version="1";Loader.instance().setupTestHarness(new DummyModContainer(md));
  Constructor<?> ctor=Class.forName("MaterialTextureAtlasSpriteNativeTest$Context").getDeclaredConstructor();ctor.setAccessible(true);try(AutoCloseable context=(AutoCloseable)ctor.newInstance()){Method init=Class.forName("MaterialTextureAtlasSpriteNativeTest").getDeclaredMethod("initializeNativeClientSettings");init.setAccessible(true);init.invoke(null);setup(args[0]);callers();named();nativeEffects();smallExplosion();}
  System.out.println("PASS "+checks+" actual native vanilla particle caller/admission/motion/collision checks; whole ClientProxy outside the extracted branches remains unproven.");
 }
}
