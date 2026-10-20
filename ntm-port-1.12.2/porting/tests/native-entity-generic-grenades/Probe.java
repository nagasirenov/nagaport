import java.util.*;
import java.lang.reflect.*;
import com.hbm.entity.grenade.*;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.ItemGenericGrenade;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.nbt.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.*;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.*;
import io.netty.buffer.Unpooled;

public class Probe {
 static int checks;static void check(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
 static class W extends World {W(){super(new net.minecraft.world.storage.SaveHandlerMP(),new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new net.minecraft.profiler.Profiler(),false);}protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}protected boolean isChunkLoaded(int x,int z,boolean a){throw new AssertionError("Chunk access excluded");}}
 @SuppressWarnings("unchecked")static DataParameter<Integer> key(Class<?> c)throws Exception{Field f=c.getDeclaredField("GRENADE_ID");f.setAccessible(true);return (DataParameter<Integer>)f.get(null);}
 static void set(Entity e,ItemGenericGrenade item){if(e instanceof EntityGrenadeImpactGeneric)check(((EntityGrenadeImpactGeneric)e).setType(item)==e,"impact fluent setter");else check(((EntityGrenadeBouncyGeneric)e).setType(item)==e,"bouncy fluent setter");}
 static ItemGenericGrenade get(Entity e){return ((IGenericGrenade)e).getGrenade();}
 static NBTTagCompound save(Entity e)throws Exception{NBTTagCompound n=new NBTTagCompound();Method m=e.getClass().getDeclaredMethod("writeEntityToNBT",NBTTagCompound.class);m.setAccessible(true);m.invoke(e,n);return n;}
 static void load(Entity e,NBTTagCompound n)throws Exception{Method m=e.getClass().getDeclaredMethod("readEntityFromNBT",NBTTagCompound.class);m.setAccessible(true);m.invoke(e,n);}
 static NBTTagCompound binary(NBTTagCompound n)throws Exception{java.io.ByteArrayOutputStream b=new java.io.ByteArrayOutputStream();CompressedStreamTools.write(n,new java.io.DataOutputStream(b));return CompressedStreamTools.read(new java.io.DataInputStream(new java.io.ByteArrayInputStream(b.toByteArray())));}
 static void packet(Entity a,Entity b,boolean full,int count)throws Exception{PacketBuffer buf=new PacketBuffer(Unpooled.buffer());new SPacketEntityMetadata(a.getEntityId(),a.getDataManager(),full).writePacketData(buf);SPacketEntityMetadata p=new SPacketEntityMetadata();p.readPacketData(buf);check(buf.readableBytes()==0&&p.getEntityId()==a.getEntityId(),"native packet bytes and entity ID");List<EntityDataManager.DataEntry<?>> es=p.getDataManagerEntries();check((es==null?0:es.size())==count,"native exact entry count");if(es!=null)b.getDataManager().setEntryValues(es);buf.release();}
 static ItemGenericGrenade register(String path){ItemGenericGrenade g=new ItemGenericGrenade();g.setRegistryName(new ResourceLocation("minecraft",path));net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.register(g);return g;}
 static void exercise(Entity a,Entity b,ItemGenericGrenade zero,ItemGenericGrenade one)throws Exception{
  DataParameter<Integer> k=key(a.getClass());check(k.getSerializer()==DataSerializers.VARINT&&k.getId()>5,"native owner-local VARINT allocation");check(a.getDataManager().get(k)==0,"original literal integer0 default");Set<Integer> ids=new HashSet<>();for(EntityDataManager.DataEntry<?> e:a.getDataManager().getAll())check(ids.add(e.getKey().getId()),"unique native key");check(ids.size()==7,"native six keys plus independent owner key");check(get(a)==ModItems.grenade_kyiv,"zero default retains original fallback instead of native ItemAir cast");packet(a,b,true,7);a.getDataManager().setClean();set(a,null);check(a.getDataManager().get(k)==0&&!a.getDataManager().isDirty(),"null Item ID0 and equal write");packet(a,b,false,0);
  for(ItemGenericGrenade item:new ItemGenericGrenade[]{zero,one,zero}){set(a,item);check(a.getDataManager().get(k)==Item.getIdFromItem(item)&&get(a)==item,"genuine native registry ID setter/getter");packet(a,b,false,1);check(get(b)==item,"packet receiver resolves exact registered item");set(a,item);check(!a.getDataManager().isDirty(),"equal item ID stays clean");}
  for(int id:new int[]{-1,Integer.MIN_VALUE,65535,Integer.MAX_VALUE,0}){a.getDataManager().set(k,id);check(get(a)==ModItems.grenade_kyiv,"only null/unmapped IDs use original fallback");packet(a,b,false,1);check(b.getDataManager().get(k)==id,"signed raw item ID unchanged across metadata");NBTTagCompound n=binary(save(a));check(n.getInteger("grenade")==id,"binary NBT raw item ID not normalized");load(b,n);check(b.getDataManager().get(k)==id&&get(b)==ModItems.grenade_kyiv,"load retains raw invalid ID and domain fallback");}
  a.getDataManager().set(k,Item.getIdFromItem(net.minecraft.init.Items.DIAMOND));boolean threw=false;try{get(a);}catch(ClassCastException ex){threw=true;}check(threw,"nonzero registered wrong-type cast still throws; no broad instanceof fallback");
  set(a,one);NBTTagCompound saved=binary(save(a));load(b,saved);check(get(b)==one,"registered grenade survives full owner NBT hook");check(saved.hasKey("grenade",3),"same NBT integer tag type");
  if(a instanceof EntityGrenadeBouncyGeneric){Field timer=EntityGrenadeBouncyBase.class.getDeclaredField("timer");timer.setAccessible(true);timer.setInt(a,-123);saved=binary(save(a));check(saved.getInteger("timer")==-123&&saved.getKeySet().equals(new HashSet<String>(Arrays.asList("timer","grenade"))),"actual bounded Bouncy parent timer hook followed by original grenade tag");load(b,saved);check(timer.getInt(b)==-123,"parent read executes before owner ID read");}else{check(saved.hasKey("xTile")&&saved.hasKey("inTile")&&saved.hasKey("ownerName"),"genuine native EntityThrowable NBT chain executed, not a no-op parent");}
  a.getDataManager().setClean();load(a,new NBTTagCompound());check(a.getDataManager().get(k)==0&&get(a)==ModItems.grenade_kyiv,"missing grenade tag resets ID0/fallback");check(a.getDataManager().isDirty(),"changed NBT ID dirties metadata");packet(a,b,false,1);check(get(b)==ModItems.grenade_kyiv,"missing-tag load fallback reaches receiving manager");
  ModItems.grenade_kyiv=null;check(get(a)==null,"null fallback remains null");ModItems.grenade_kyiv=net.minecraft.init.Items.DIAMOND;threw=false;try{get(a);}catch(ClassCastException ex){threw=true;}check(threw,"invalid fallback type still throws");ModItems.grenade_kyiv=zero;
  a.getDataManager().setClean();a.getDataManager().setDirty(k);packet(a,b,false,1);check(b.getDataManager().get(k)==0,"explicit forced-dirty zero snapshot");
 }
 public static void main(String[] args)throws Exception{
  net.minecraft.init.Bootstrap.register();check(Item.getItemById(0)==net.minecraft.init.Items.AIR,"actual native ID0 is ItemAir, proving compatibility branch needed");check(Item.getItemById(-1)==null&&Item.getItemById(Integer.MAX_VALUE)==null,"actual unknown native IDs are null");ItemGenericGrenade first=register("probe_grenade_first"),second=register("probe_grenade_second");ModItems.grenade_kyiv=first;check(Item.getIdFromItem(first)!=Item.getIdFromItem(second)&&Item.getIdFromItem(first)>0,"distinct genuine Forge item IDs");W w=new W();
  if(args[0].equals("impact-first"))new EntityGrenadeImpactGeneric(w);else new EntityGrenadeBouncyGeneric(w);
  EntityGrenadeImpactGeneric i=new EntityGrenadeImpactGeneric(w),ic=new EntityGrenadeImpactGeneric(w);EntityGrenadeBouncyGeneric b=new EntityGrenadeBouncyGeneric(w),bc=new EntityGrenadeBouncyGeneric(w);
  check(key(i.getClass())!=key(b.getClass())&&key(i.getClass()).getId()==key(b.getClass()).getId(),"independent owner key objects may share allocated numeric slot");check(i.width==0.25F&&b.width==0.25F,"real native Throwable and exact Bouncy World constructor sizes");exercise(i,ic,first,second);exercise(b,bc,first,second);
  System.out.println("PASS "+checks+" native generic-grenade assertions, "+args[0]+"; whole owners, explicit bounded parents, no explosion/flight acceptance");
 }
}
