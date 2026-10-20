import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.*;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraft.util.math.*;
import net.minecraft.util.text.*;
import net.minecraft.world.*;
import io.netty.buffer.Unpooled;

public class Probe {
 static int checks;
 static void check(boolean v,String why){checks++;if(!v)throw new AssertionError(why);}
 static class W extends World {
  W(boolean remote){super(new net.minecraft.world.storage.SaveHandlerMP(),new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new net.minecraft.profiler.Profiler(),remote);}
  protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
  protected boolean isChunkLoaded(int x,int z,boolean allow){throw new AssertionError("Chunk operations outside scope");}
  public BlockPos getSpawnPoint(){return BlockPos.ORIGIN;}
 }
 static class P extends EntityPlayer {
  P(W w,String name){super(w,new com.mojang.authlib.GameProfile(UUID.randomUUID(),name));}
  public boolean isSpectator(){return false;}public boolean isCreative(){return false;}
  public ITextComponent getDisplayName(){return new TextComponentString("TEAM_PREFIX "+getName()+" TEAM_SUFFIX");}
  public Vec3d getLookVec(){return new Vec3d(0.25,-0.5,0.75);}
  public float getEyeHeight(){return 1.375F;}
 }
 static class Observed extends EntityBeamVortex {
  static final List<String> events=new ArrayList<>();
  Observed(World w){super(w);}Observed(World w,EntityPlayer p){super(w,p);}
  public void notifyDataManagerChange(DataParameter<?> key){super.notifyDataManagerChange(key);if(key==PLAYER_NAME)events.add("name:"+posX+":"+posY+":"+posZ+":"+getDataManager().get(PLAYER_NAME));}
  public void setPosition(double x,double y,double z){super.setPosition(x,y,z);events.add("position:"+x+":"+y+":"+z);}
 }
 static void cacheName(P p,String value)throws Exception {java.lang.reflect.Field f=EntityPlayer.class.getDeclaredField("displayname");f.setAccessible(true);f.set(p,value);}
 static void packet(EntityBeamVortex a,EntityBeamVortex b,boolean full,int expected)throws Exception {
  PacketBuffer buf=new PacketBuffer(Unpooled.buffer());new SPacketEntityMetadata(a.getEntityId(),a.getDataManager(),full).writePacketData(buf);SPacketEntityMetadata read=new SPacketEntityMetadata();read.readPacketData(buf);check(buf.readableBytes()==0,"all real packet bytes consumed");check(read.getEntityId()==a.getEntityId(),"entity ID in packet");List<EntityDataManager.DataEntry<?>> entries=read.getDataManagerEntries();check((entries==null?0:entries.size())==expected,"correct entry count");if(entries!=null)b.getDataManager().setEntryValues(entries);buf.release();
 }
 static NBTTagCompound binary(NBTTagCompound n)throws Exception {java.io.ByteArrayOutputStream b=new java.io.ByteArrayOutputStream();CompressedStreamTools.write(n,new java.io.DataOutputStream(b));return CompressedStreamTools.read(new java.io.DataInputStream(new java.io.ByteArrayInputStream(b.toByteArray())));}
 public static void main(String[] args)throws Exception {
  net.minecraft.init.Bootstrap.register();W server=new W(false),client=new W(true);P player=new P(server,"ProfileName");server.playerEntities.add(player);player.setPosition(12.5,44.25,-8.75);cacheName(player,"ForgeAlias");
  check(player.getName().equals("ProfileName")&&player.getDisplayNameString().equals("ForgeAlias")&&player.getDisplayName().getUnformattedText().startsWith("TEAM_PREFIX"),"real cached Forge string distinct from username/decorated text");
  Observed.events.clear();Observed beam=new Observed(server,player);DataParameter<String> key=EntityBeamBase.PLAYER_NAME;check(key.getSerializer()==DataSerializers.STRING&&key.getId()>5,"true-owner native STRING key");check(beam.getDataManager().get(key).equals("ForgeAlias"),"constructor uses actual native Forge display string");check(beam.ignoreFrustumCheck,"complete constructor preserves frustum flag");
  Vec3d v=player.getLookVec().rotateYaw(-90F).scale(0.075F),v0=player.getLookVec().scale(0.1F);double x=player.posX+v.x+v0.x,y=player.posY+player.getEyeHeight()+v0.y,z=player.posZ+v.z+v0.z;check(beam.posX==x&&beam.posY==y&&beam.posZ==z,"unchanged native constructor positioning expression");int name=-1,position=-1;for(int i=0;i<Observed.events.size();i++){if(Observed.events.get(i).startsWith("name:"))name=i;if(Observed.events.get(i).equals("position:"+x+":"+y+":"+z))position=i;}check(name>=0&&name<position&&Observed.events.get(name).startsWith("name:0.0:0.0:0.0:"),"metadata write precedes constructor final position");
  Observed receiving=new Observed(client);check(receiving.getDataManager().get(key).equals(""),"world constructor default empty string");Set<Integer> ids=new HashSet<>();for(EntityDataManager.DataEntry<?> e:beam.getDataManager().getAll())check(ids.add(e.getKey().getId()),"no native key collision");check(ids.size()==7,"six Entity keys plus one beam key");packet(beam,receiving,true,7);check(receiving.getDataManager().get(key).equals("ForgeAlias"),"full metadata constructor name snapshot");
  beam.getDataManager().setClean();beam.getDataManager().set(key,new String("ForgeAlias"));check(!beam.getDataManager().isDirty(),"equal separate String object remains clean");packet(beam,receiving,false,0);
  for(String s:new String[]{"","normal_name","\u00a7cColorName","空间名字","emoji_\ud83d\ude80","zero\u0000inside",new String(new char[32767]).replace('\0','x')}){beam.getDataManager().set(key,s);check(beam.getDataManager().isDirty(),"string change dirty");packet(beam,receiving,false,1);check(receiving.getDataManager().get(key).equals(s),"native UTF8/string packet roundtrip");check(!beam.getDataManager().isDirty()&&beam.getDataManager().getDirty()==null,"native extraction reset");beam.getDataManager().set(key,new String(s));check(!beam.getDataManager().isDirty(),"equal string contents clean");}
  beam.getDataManager().set(key,"before");List<EntityDataManager.DataEntry<?>> snapshot=beam.getDataManager().getAll();beam.getDataManager().set(key,"after");for(EntityDataManager.DataEntry<?> e:snapshot)if(e.getKey().getId()==key.getId())check(e.getValue().equals("before"),"immutable string snapshot not overwritten");beam.getDataManager().setClean();beam.getDataManager().setDirty(key);packet(beam,receiving,false,1);check(receiving.getDataManager().get(key).equals("after"),"force-dirty unchanged payload");
  for(boolean remote:new boolean[]{false,true}){W w=remote?client:server;if(remote)w.playerEntities.add(player);EntityBeamVortex e=new EntityBeamVortex(w);for(String s:new String[]{"ProfileName","ForgeAlias","profilename",""}){e.getDataManager().set(key,s);check(RendererLookup.lookup(e)==(s.equals("ProfileName")?player:null),"exact renderer owner lookup preserves username/alias behavior");for(int tick:new int[]{0,1,2,10,11}){e.ticksExisted=tick;e.isDead=false;LookupObservation.reset();e.lookupPrefix();check(LookupObservation.count==(tick<=1?1:0),"original lookup only at ticks<=1 on either side");check(e.isDead==(tick>10),"original death threshold before early-return gate");check(e.ticksExisted==tick,"no new super.onUpdate tick increment");if(tick<=1)check(LookupObservation.player==(s.equals("ProfileName")?player:null),"actual World lookup remains exact case-sensitive username; alias mismatch retained");}}}
  beam.getDataManager().set(key,"unsaved");NBTTagCompound n=new NBTTagCompound();n.setString("sentinel","keep");beam.writeEntityToNBT(n);check(n.getKeySet().equals(Collections.singleton("sentinel")),"empty NBT writer adds no owner data");receiving.readEntityFromNBT(binary(n));check(receiving.getDataManager().get(key).equals("after"),"empty NBT reader does not reset live owner");EntityBeamVortex fresh=new EntityBeamVortex(server);fresh.readEntityFromNBT(binary(n));check(fresh.getDataManager().get(key).equals(""),"fresh load empty owner remains");
  System.out.println("PASS "+checks+" native beam-owner assertions; complete constructors/init/NBT and exact update-prefix only");
 }
}
class LookupObservation {static int count;static EntityPlayer player;static void record(EntityPlayer p){count++;player=p;}static void reset(){count=0;player=null;}}
