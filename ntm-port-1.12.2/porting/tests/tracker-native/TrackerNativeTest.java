import java.util.*;
import com.hbm.util.TrackerUtil;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.network.*;
import net.minecraft.network.play.server.SPacketEntityTeleport;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.*;
import net.minecraft.util.*;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
public class TrackerNativeTest {
 static int checks;static void check(boolean v,String m){checks++;if(!v)throw new AssertionError(m);}
 static sun.misc.Unsafe unsafe;static <T>T allocate(Class<T> c)throws Exception{return(T)unsafe.allocateInstance(c);}
 static class Target extends Entity {
  Target(){super(null);}protected void entityInit(){}protected void readEntityFromNBT(NBTTagCompound n){}protected void writeEntityToNBT(NBTTagCompound n){}
  public boolean isSpectatedByPlayer(EntityPlayerMP p){return true;}
 }
 public static class Connection extends NetHandlerPlayServer {
  List<Packet<?>> packets;Connection(){super(null,null,null);}public void sendPacket(Packet<?> p){packets.add(p);}
 }
 static class ClientWorld extends World {
  ClientWorld(){super(new net.minecraft.world.storage.SaveHandlerMP(),new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new net.minecraft.profiler.Profiler(),true);}
  protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}protected boolean isChunkLoaded(int x,int z,boolean e){return true;}
 }
 public static void main(String[] args)throws Exception {
  net.minecraft.init.Bootstrap.register();java.lang.reflect.Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);unsafe=(sun.misc.Unsafe)uf.get(null);
  WorldServer world=allocate(WorldServer.class);EntityTracker tracker=allocate(EntityTracker.class);IntHashMap<EntityTrackerEntry> map=new IntHashMap<>();ReflectionHelper.setPrivateValue(EntityTracker.class,tracker,map,"trackedEntityHashTable","field_72794_c");ReflectionHelper.setPrivateValue(WorldServer.class,world,tracker,"entityTracker","field_73062_L");
  Target entity=new Target();entity.setEntityId(300);entity.setPosition(-12.123456,256.000001,44.999999);entity.rotationYaw=-71.25F;entity.rotationPitch=31.5F;entity.onGround=true;
  EntityTrackerEntry entry=new EntityTrackerEntry(entity,10,32,1,true);map.addKey(300,entry);check(TrackerUtil.getTrackerEntry(world,300)==entry,"genuine native hash map lookup");check(TrackerUtil.getTrackerEntry(world,301)==null,"missing map identity");
  List<Connection> connections=new ArrayList<>();for(int i=0;i<2;i++){EntityPlayerMP player=allocate(EntityPlayerMP.class);Connection connection=allocate(Connection.class);connection.packets=new ArrayList<>();player.connection=connection;player.setEntityId(400+i);entry.trackingPlayers.add(player);connections.add(connection);}
  entry.updateCounter=137;TrackerUtil.sendTeleport(world,entity);check(entry.updateCounter==0,"original counter reset");
  for(Connection c:connections){check(c.packets.size()==1&&c.packets.get(0) instanceof SPacketEntityTeleport,"actual tracker broadcast to each player");SPacketEntityTeleport sent=(SPacketEntityTeleport)c.packets.get(0);check(sent.getEntityId()==300,"entity ID");check(sent.getX()==entity.posX&&sent.getY()==entity.posY&&sent.getZ()==entity.posZ,"native double logical position, not legacy fixedpoint wire");check(sent.getYaw()==(byte)net.minecraft.util.math.MathHelper.floor(entity.rotationYaw*256F/360F)&&sent.getPitch()==(byte)net.minecraft.util.math.MathHelper.floor(entity.rotationPitch*256F/360F),"rotation encoding");check(sent.getOnGround(),"native on-ground wire");PacketBuffer buf=new PacketBuffer(io.netty.buffer.Unpooled.buffer());sent.writePacketData(buf);SPacketEntityTeleport decoded=new SPacketEntityTeleport();decoded.readPacketData(buf);check(decoded.getX()==sent.getX()&&decoded.getY()==sent.getY()&&decoded.getZ()==sent.getZ()&&decoded.getYaw()==sent.getYaw()&&decoded.getPitch()==sent.getPitch()&&decoded.getOnGround()==sent.getOnGround(),"actual native packet round trip");buf.release();}
  for(float angle:new float[]{-720.1F,-360F,-1.5F,-0.01F,0F,0.01F,1.5F,359.99F,720.1F}){entity.rotationYaw=angle;entity.rotationPitch=-angle;float yaw=entity.rotationYaw,pitch=entity.rotationPitch;entry.updateCounter=12;TrackerUtil.sendTeleport(world,entity);SPacketEntityTeleport packet=(SPacketEntityTeleport)connections.get(0).packets.get(connections.get(0).packets.size()-1);check(packet.getYaw()==(byte)net.minecraft.util.math.MathHelper.floor(yaw*256F/360F)&&packet.getPitch()==(byte)net.minecraft.util.math.MathHelper.floor(pitch*256F/360F),"original negative/zero/wrapped floor angle");check(entity.rotationYaw==yaw&&entity.rotationPitch==pitch&&entry.updateCounter==0,"no entity mutation during native packet construction");}
  EntityPlayerMP probe=allocate(EntityPlayerMP.class);probe.posX=entity.posX+15;probe.posZ=entity.posZ;check(!entry.isVisibleTo(probe),"old ten block range");TrackerUtil.setTrackingRange(world,entity,20);check(entry.isVisibleTo(probe),"native visibility observes reflected final runtime range");check((Integer)ReflectionHelper.getPrivateValue(EntityTrackerEntry.class,entry,"range","field_73130_b")==20,"native underlying range changed");entry.setMaxRange(12);check(!entry.isVisibleTo(probe),"native server-view cap preserved");
  for(int range:new int[]{0,1,-5,1000}){TrackerUtil.setTrackingRange(world,entity,range);check((Integer)ReflectionHelper.getPrivateValue(EntityTrackerEntry.class,entry,"range","field_73130_b")==range,"no invented range clamp");}
  Target missing=new Target();missing.setEntityId(301);TrackerUtil.setTrackingRange(world,missing,3);check(map.lookup(301)==null,"missing set range is original no-op");boolean threw=false;try{TrackerUtil.sendTeleport(world,missing);}catch(NullPointerException expected){threw=true;}check(threw,"original missing teleport entry failure retained");
  entry.updateCounter=91;ClientWorld client=new ClientWorld();TrackerUtil.sendTeleport(client,entity);TrackerUtil.setTrackingRange(client,entity,77);check(entry.updateCounter==91&&connections.get(0).packets.size()==10,"client paths no-op");
  System.out.println("PASS "+checks+" native tracker assertions; actual broadcast/packet/visibility, no running server/network game claim.");
 }
}
