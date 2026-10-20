import java.lang.reflect.*;
import java.util.*;
import com.hbm.entity.mob.*;
import com.hbm.entity.boss.INtmBossDisplay;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.nbt.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.*;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraft.world.*;
import io.netty.buffer.Unpooled;

public class Probe {
 static int checks;
 static void check(boolean ok,String why){checks++;if(!ok)throw new AssertionError(why);}
 @SuppressWarnings("unchecked") static <T> DataParameter<T> key(Class<?> owner,String name)throws Exception{Field f=owner.getDeclaredField(name);f.setAccessible(true);return (DataParameter<T>)f.get(null);}
 static class W extends World {
  W(boolean remote){super(new net.minecraft.world.storage.SaveHandlerMP(),new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new net.minecraft.profiler.Profiler(),remote);}
  protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
  protected boolean isChunkLoaded(int x,int z,boolean allow){throw new AssertionError("No chunks in bounded metadata proof");}
 }
 static List<EntityDataManager.DataEntry<?>> packet(Entity a,Entity b,boolean full,int count)throws Exception{
  PacketBuffer buf=new PacketBuffer(Unpooled.buffer());new SPacketEntityMetadata(a.getEntityId(),a.getDataManager(),full).writePacketData(buf);
  SPacketEntityMetadata p=new SPacketEntityMetadata();p.readPacketData(buf);check(p.getEntityId()==a.getEntityId(),"native entity id");check(buf.readableBytes()==0,"native framing consumed");
  List<EntityDataManager.DataEntry<?>> es=p.getDataManagerEntries();check((es==null?0:es.size())==count,"exact metadata count expected="+count+" actual="+(es==null?0:es.size()));if(es!=null)b.getDataManager().setEntryValues(es);buf.release();return es;
 }
 static NBTTagCompound binary(NBTTagCompound n)throws Exception{java.io.ByteArrayOutputStream b=new java.io.ByteArrayOutputStream();CompressedStreamTools.write(n,new java.io.DataOutputStream(b));return CompressedStreamTools.read(new java.io.DataInputStream(new java.io.ByteArrayInputStream(b.toByteArray())));}
 static void layout(Entity e){Set<Integer> ids=new HashSet<>();for(EntityDataManager.DataEntry<?> v:e.getDataManager().getAll())check(ids.add(v.getKey().getId()),"unique native inherited entry");check(ids.size()==16,"real EntityFlying ancestors own twelve, exactly four more");}
 static void ints(Entity a,Entity b,String name)throws Exception{
  DataParameter<Integer> k=key(a.getClass(),name);check(k.getSerializer()==DataSerializers.VARINT,"native VARINT");a.getDataManager().setClean();packet(a,b,false,0);
  for(int n:new int[]{Integer.MIN_VALUE,-65536,-1,0,1,127,128,255,65535,Integer.MAX_VALUE}){
   int before=a.getDataManager().get(k);a.getDataManager().set(k,n);check(a.getDataManager().isDirty()==(before!=n),"integer equality dirty");packet(a,b,false,before==n?0:1);check(b.getDataManager().get(k)==n,"signed int native packet");check(!a.getDataManager().isDirty()&&a.getDataManager().getDirty()==null,"native dirty reset");a.getDataManager().set(k,n);check(!a.getDataManager().isDirty(),"equal int clean");
  }
  List<EntityDataManager.DataEntry<?>> snapshot=a.getDataManager().getAll();a.getDataManager().set(k,-17);for(EntityDataManager.DataEntry<?> e:snapshot)if(e.getKey().getId()==k.getId())check(((Integer)e.getValue())==Integer.MAX_VALUE,"immutable integer snapshot");packet(a,b,false,1);a.getDataManager().setDirty(k);packet(a,b,false,1);check(b.getDataManager().get(k)==-17,"forced native dirty packet");
 }
 static void bools(Entity a,Entity b,String name,String getter,String setter)throws Exception{
  DataParameter<Byte> k=key(a.getClass(),name);check(k.getSerializer()==DataSerializers.BYTE,"native BYTE");Method get=a.getClass().getMethod(getter),set=a.getClass().getMethod(setter,boolean.class);a.getDataManager().setClean();
  for(int raw=-128;raw<=127;raw++){
   a.getDataManager().set(k,(byte)raw);a.getDataManager().setDirty(k);packet(a,b,false,1);check(b.getDataManager().get(k)==(byte)raw,"all256 signed BYTE native roundtrips");check((Boolean)get.invoke(b)==(raw==1),"boolean equals1, not bit/nonzero");
   a.getDataManager().setClean();set.invoke(a,true);check(a.getDataManager().get(k)==1&&a.getDataManager().isDirty()==(raw!=1),"true replaces entire byte");a.getDataManager().setClean();set.invoke(a,true);check(!a.getDataManager().isDirty(),"true unchanged clean");set.invoke(a,false);check(a.getDataManager().get(k)==0&&a.getDataManager().isDirty(),"false replaces byte0");a.getDataManager().setClean();set.invoke(a,false);check(!a.getDataManager().isDirty(),"false unchanged clean");
  }
  a.getDataManager().set(k,(byte)37);List<EntityDataManager.DataEntry<?>> snap=a.getDataManager().getAll();a.getDataManager().set(k,(byte)-1);for(EntityDataManager.DataEntry<?> e:snap)if(e.getKey().getId()==k.getId())check(((Byte)e.getValue())==37,"immutable byte snapshot");a.getDataManager().setClean();
 }
 static void floats(EntityHunterChopper a,EntityHunterChopper b,String name)throws Exception{
  DataParameter<Float> k=key(EntityHunterChopper.class,name);check(k.getSerializer()==DataSerializers.FLOAT,"native FLOAT not dropped/repurposed");a.getDataManager().setClean();
  for(float n:new float[]{0F,-0F,Float.MIN_VALUE,-Float.MIN_VALUE,1F/3F,Float.MAX_VALUE,-Float.MAX_VALUE,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.NaN}){
   Float before=a.getDataManager().get(k);a.getDataManager().set(k,n);boolean changed=!before.equals(n);check(a.getDataManager().isDirty()==changed,"Float.equals changed detection");packet(a,b,false,changed?1:0);check(Float.floatToIntBits(b.getDataManager().get(k))==Float.floatToIntBits(n),"native float extremes and signedzero");a.getDataManager().set(k,n);check(!a.getDataManager().isDirty(),"equal FLOAT clean");
  }
  a.getDataManager().set(k,Float.intBitsToFloat(0x7fc01234));check(!a.getDataManager().isDirty(),"NaN payloads equal under Float.equals");a.getDataManager().setDirty(k);packet(a,b,false,1);check(Float.isNaN(b.getDataManager().get(k)),"forced NaN packet");a.getDataManager().set(k,23.5F);List<EntityDataManager.DataEntry<?>> snap=a.getDataManager().getAll();a.getDataManager().set(k,-3.25F);for(EntityDataManager.DataEntry<?> e:snap)if(e.getKey().getId()==k.getId())check(((Float)e.getValue())==23.5F,"immutable Float snapshot");a.getDataManager().setClean();
 }
 static void waypoint(Entity a,Entity b)throws Exception{
  Class<?> c=a.getClass();Method set=c.getMethod("setWaypoint",int.class,int.class,int.class);for(String axis:new String[]{"X","Y","Z"})ints(a,b,"WAYPOINT_"+axis);
  a.getDataManager().setClean();set.invoke(a,Integer.MIN_VALUE,42,Integer.MAX_VALUE);packet(a,b,false,3);check((Integer)c.getMethod("getX").invoke(b)==Integer.MIN_VALUE&&(Integer)c.getMethod("getY").invoke(b)==42&&(Integer)c.getMethod("getZ").invoke(b)==Integer.MAX_VALUE,"exact XYZ getter/setter");set.invoke(a,Integer.MIN_VALUE,42,Integer.MAX_VALUE);check(!a.getDataManager().isDirty(),"identical XYZ clean");set.invoke(a,Integer.MIN_VALUE,43,Integer.MAX_VALUE);packet(a,b,false,1);check((Integer)c.getMethod("getY").invoke(b)==43,"one-axis update isolated");
 }
 static void attack(EntityHunterChopper a,EntityHunterChopper b)throws Exception{
  DataParameter<Byte> k=key(EntityHunterChopper.class,"ATTACKING");a.getDataManager().setClean();
  for(int raw=-128;raw<=127;raw++){
   a.getDataManager().set(k,(byte)raw);a.getDataManager().setDirty(k);packet(a,b,false,1);check(b.func_110182_bF()==(raw!=0),"chopper attacking nonzero, distinct from dying equals1");
   for(int counter:new int[]{Integer.MIN_VALUE,-1,0,10,11,Integer.MAX_VALUE}){
    a.getDataManager().set(k,(byte)raw);a.getDataManager().setClean();a.attackCounter=counter;a.updateActionMetadataBlockOnly();byte expected=(byte)(counter>10?1:0);check(a.getDataManager().get(k)==expected,"exact original threshold and entire byte replacement");check(a.getDataManager().isDirty()==(raw!=expected),"original explicit inequality and native dirty semantics");a.getDataManager().setClean();a.updateActionMetadataBlockOnly();check(!a.getDataManager().isDirty(),"identical attack block clean");
    b.getDataManager().set(k,(byte)raw);b.getDataManager().setClean();b.attackCounter=counter;b.updateActionMetadataBlockOnly();check(b.getDataManager().get(k)==(byte)raw&&!b.getDataManager().isDirty(),"exact server guard leaves client byte untouched");
   }
   a.getDataManager().setClean();
  }
  a.isDying=false;b.isDying=false;a.setIsDying(true);packet(a,b,false,1);check(a.getIsDying()&&b.getIsDying()&&!a.isDying&&!b.isDying,"dying metadata setter/packet does not repair independent ordinary isDying");
  check(Consumers.stopMovingSoundChopper(b)&&!Consumers.stopMovingSoundCrashing(b),"exact dying sound conditions");b.setIsDying(false);check(!Consumers.stopMovingSoundChopper(b)&&Consumers.stopMovingSoundCrashing(b),"exact alive sound conditions");
 }
 static void nbt(EntityFlying a,W world,String[] fields)throws Exception{
  Class<?> c=a.getClass();Map<String,Object> old=new HashMap<>();for(String name:fields)old.put(name,a.getDataManager().get(key(c,name)));
  NBTTagCompound n=new NBTTagCompound();c.getMethod("writeEntityToNBT",NBTTagCompound.class).invoke(a,n);n=binary(n);
  check(n.hasKey("Health")&&n.hasKey("Attributes"),"complete writer calls genuine native EntityLivingBase NBT");
  for(String tag:new String[]{"beam","Beam","waypointX","waypointY","waypointZ","isDying","IsDying","attackCounter","ATTACKING","DYING","LEGACY_UNUSED_21","LEGACY_UNUSED_22"})check(!n.hasKey(tag),"custom metadata remains unpersisted: "+tag);
  EntityFlying fresh=(EntityFlying)c.getConstructor(World.class).newInstance(world);c.getMethod("readEntityFromNBT",NBTTagCompound.class).invoke(fresh,n);
  for(String name:fields){Object value=fresh.getDataManager().get(key(c,name));check(value instanceof Float?Float.floatToRawIntBits((Float)value)==0:((Number)value).intValue()==0,"fresh binary NBT load retains registration default");}
  c.getMethod("readEntityFromNBT",NBTTagCompound.class).invoke(a,n);for(String name:fields)check(old.get(name).equals(a.getDataManager().get(key(c,name))),"loading saved NBT into existing owner does not reset unsaved metadata");
  c.getMethod("readEntityFromNBT",NBTTagCompound.class).invoke(a,new NBTTagCompound());for(String name:fields)check(old.get(name).equals(a.getDataManager().get(key(c,name))),"empty NBT does not reset custom metadata");
  if(a instanceof EntityHunterChopper){EntityHunterChopper ch=(EntityHunterChopper)a;ch.isDying=true;ch.attackCounter=777;c.getMethod("readEntityFromNBT",NBTTagCompound.class).invoke(ch,n);check(ch.isDying&&ch.attackCounter==777,"ordinary chopper fields likewise not repaired by read");check(!((EntityHunterChopper)fresh).isDying&&((EntityHunterChopper)fresh).attackCounter==0,"fresh ordinary chopper fields default");}
 }
 public static void main(String[] args)throws Exception{
  net.minecraft.init.Bootstrap.register();for(String name:args[0].split(","))Class.forName("com.hbm.entity.mob."+name);W server=new W(false),client=new W(true);
  EntityUFO u=new EntityUFO(server),uc=new EntityUFO(client);EntityBFAngel a=new EntityBFAngel(server),ac=new EntityBFAngel(client);EntityHunterChopper h=new EntityHunterChopper(server),hc=new EntityHunterChopper(client);
  EntityFlying[] es={u,a,h},cs={uc,ac,hc};String[][] names={{"BEAM","WAYPOINT_X","WAYPOINT_Y","WAYPOINT_Z"},{"BEAM","WAYPOINT_X","WAYPOINT_Y","WAYPOINT_Z"},{"ATTACKING","LEGACY_UNUSED_21","LEGACY_UNUSED_22","DYING"}};
  for(int i=0;i<es.length;i++){
   EntityFlying e=es[i],r=cs[i];layout(e);layout(r);check(e instanceof INtmBossDisplay&&INtmBossDisplay.isDisplayBoss(e),"exact interface declaration preserves boss classification only");check(e.getHealth()==20F,"native inherited HEALTH not overwritten (production attributes deliberately excluded)");
   for(int j=0;j<4;j++){DataParameter<?> k=key(e.getClass(),names[i][j]);check(k.getId()==12+j,"actual native EntityFlying ancestor allocation");Object v=e.getDataManager().get(k),w=r.getDataManager().get(k);check(v.equals(w)&&(v instanceof Float?Float.floatToRawIntBits((Float)v)==0:((Number)v).intValue()==0),"hardcoded defaults both sides including +0F");}
   packet(e,r,true,16);packet(e,r,false,0);for(int j=0;j<i;j++)check(key(e.getClass(),names[i][0])!=key(es[j].getClass(),names[j][0]),"sibling static keys independently owned despite same native index");
  }
  check(u.width==15F&&u.height==4F&&u.deathTime==-30&&a.width==5F&&a.height==4F&&a.deathTime==-30&&h.width==8.25F&&h.height==3F,"complete exact production constructors");
  bools(u,uc,"BEAM","getBeam","setBeam");bools(a,ac,"BEAM","getBeam","setBeam");bools(h,hc,"DYING","getIsDying","setIsDying");waypoint(u,uc);waypoint(a,ac);floats(h,hc,"LEGACY_UNUSED_21");floats(h,hc,"LEGACY_UNUSED_22");attack(h,hc);
  u.setBeam(true);a.setBeam(false);check(Consumers.beam(u)&&!Consumers.beam(a),"exact renderer beam conditions stay typed and independent");check(!Consumers.stopMovingSoundChopper(u)&&!Consumers.stopMovingSoundCrashing(a),"sound instanceof guards reject other family owners");
  for(int i=0;i<es.length;i++)nbt(es[i],server,names[i]);
  System.out.println("PASS "+checks+" native flying-boss assertions ("+args[0]+"); bounded method projections, not full owners, attribute overrides, AI/death progression, HUD, sound playback, render GL or game startup");
 }
}
