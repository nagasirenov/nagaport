import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.*;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import io.netty.buffer.Unpooled;
import java.lang.reflect.*;
import java.util.*;

public class Probe {
    static int checks;
    static void check(boolean b,String why) { checks++; if(!b) throw new AssertionError(why); }
    @SuppressWarnings("unchecked")
    static DataParameter<Byte> key(Class<?> owner,String name) throws Exception { Field f=owner.getDeclaredField(name); f.setAccessible(true); return (DataParameter<Byte>)f.get(null); }
    static void transfer(Entity a,Entity b,boolean full) throws Exception {
        PacketBuffer buf=new PacketBuffer(Unpooled.buffer());
        new SPacketEntityMetadata(a.getEntityId(),a.getDataManager(),full).writePacketData(buf);
        SPacketEntityMetadata packet=new SPacketEntityMetadata(); packet.readPacketData(buf);
        check(buf.readableBytes()==0,"real native BYTE metadata framing"); b.getDataManager().setEntryValues(packet.getDataManagerEntries()); buf.release();
    }
    static void ownerBasics(Entity server,Entity client,int count) throws Exception {
        Set<Integer> ids=new HashSet<>();
        for(EntityDataManager.DataEntry<?> entry:server.getDataManager().getAll()) check(ids.add(entry.getKey().getId()),"unique owner/vanilla index");
        check(ids.size()==6+count,"six vanilla entries plus exact owner key count");
        transfer(server,client,true);
        for(Field field:server.getClass().getDeclaredFields()) if(field.getType()==DataParameter.class) {
            DataParameter<Byte> k=key(server.getClass(),field.getName());
            check(k.getSerializer()==DataSerializers.BYTE && k.getId()>=6,"native BYTE after vanilla slots");
            check(server.getDataManager().get(k)==0 && client.getDataManager().get(k)==0,"zero source and initial packet defaults");
        }
    }
    static void bitChannel(Entity server,Entity client,String field,String setter,String getter) throws Exception {
        DataParameter<Byte> k=key(server.getClass(),field); Method set=server.getClass().getMethod(setter,boolean.class), get=server.getClass().getMethod(getter);
        for(int raw=-128;raw<=127;raw++) {
            server.getDataManager().set(k,(byte)raw); server.getDataManager().setClean();
            set.invoke(server,true); byte on=(byte)(raw|1);
            check(server.getDataManager().get(k)==on && (Boolean)get.invoke(server),"bit0 set preserves seven upper bits");
            check(server.getDataManager().isDirty()==((raw&1)==0),"setter dirty only when bit changes");
            server.getDataManager().setClean(); set.invoke(server,false); byte off=(byte)(raw&-2);
            check(server.getDataManager().get(k)==off && !(Boolean)get.invoke(server),"bit0 clear preserves sign/upper bits");
            check(server.getDataManager().isDirty(),"on to off is dirty");
            server.getDataManager().setClean(); set.invoke(server,false); check(!server.getDataManager().isDirty(),"idempotent clear not dirty");
        }
        // One edge packet per channel; exhaustive packet roundtrip occurs once below.
        server.getDataManager().set(k,(byte)0x81); transfer(server,client,false);
        check(client.getDataManager().get(k)==(byte)0x81 && (Boolean)get.invoke(client),"channel native byte packet");
    }
    static void nbtDoesNotPersistFlags(Entity server,Entity client) throws Exception {
        NBTTagCompound tag=new NBTTagCompound(); server.getClass().getMethod("writeEntityToNBT",NBTTagCompound.class).invoke(server,tag);
        check(tag.getKeySet().equals(new HashSet<String>(Arrays.asList("xTile","yTile","zTile","life","inTile","inData","shake","inGround","pickup","damage"))),"exact old save tag set has no synchronized flags");
        java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream(); net.minecraft.nbt.CompressedStreamTools.write(tag,new java.io.DataOutputStream(bytes));
        NBTTagCompound binary=net.minecraft.nbt.CompressedStreamTools.read(new java.io.DataInputStream(new java.io.ByteArrayInputStream(bytes.toByteArray())));
        Entity fresh=(Entity)server.getClass().getConstructor(net.minecraft.world.World.class).newInstance(new Object[]{null});
        server.getClass().getMethod("readEntityFromNBT",NBTTagCompound.class).invoke(fresh,binary);
        for(Field field:server.getClass().getDeclaredFields()) if(field.getType()==DataParameter.class) {
            DataParameter<Byte> k=key(server.getClass(),field.getName()); check(fresh.getDataManager().get(k)==0,"fresh load retains zero flags");
            server.getDataManager().set(k,(byte)99);
        }
        server.getClass().getMethod("readEntityFromNBT",NBTTagCompound.class).invoke(server,binary);
        for(Field field:server.getClass().getDeclaredFields()) if(field.getType()==DataParameter.class) check(server.getDataManager().get(key(server.getClass(),field.getName()))==99,"in-place NBT read does not reset unsaved flags");
    }
    public static void main(String[] args) throws Exception {
        EntityBullet bullet=new EntityBullet(null), remoteBullet=new EntityBullet(null);
        EntityRocket rocket=new EntityRocket(null), remoteRocket=new EntityRocket(null);
        EntitySchrab schrab=new EntitySchrab(null), remoteSchrab=new EntitySchrab(null);
        EntityExplosiveBeam beam=new EntityExplosiveBeam(null), remoteBeam=new EntityExplosiveBeam(null);
        EntityShrapnel shrapnel=new EntityShrapnel(null), remoteShrapnel=new EntityShrapnel(null);
        Entity[] servers={bullet,rocket,schrab,beam,shrapnel}, clients={remoteBullet,remoteRocket,remoteSchrab,remoteBeam,remoteShrapnel};
        for(int i=0;i<servers.length;i++) ownerBasics(servers[i],clients[i],i==0?3:1);
        Set<DataParameter<Byte>> owners=Collections.newSetFromMap(new IdentityHashMap<DataParameter<Byte>,Boolean>());
        for(int i=0;i<4;i++) { check(owners.add(key(servers[i].getClass(),"CRITICAL_FLAGS")),"same numeric slot uses independent owner object"); bitChannel(servers[i],clients[i],"CRITICAL_FLAGS","setIsCritical","getIsCritical"); }
        bitChannel(bullet,remoteBullet,"TAU_FLAGS","setTau","getIsTau"); bitChannel(bullet,remoteBullet,"CHOPPER_FLAGS","setChopper","getIsChopper");
        DataParameter<Byte> critical=key(EntityBullet.class,"CRITICAL_FLAGS");
        for(int raw=-128;raw<=127;raw++) {
            bullet.getDataManager().set(critical,(byte)raw); transfer(bullet,remoteBullet,false);
            check(remoteBullet.getDataManager().get(critical)==(byte)raw,"all256 signed BYTE values survive packet");
            check(!bullet.getDataManager().isDirty() && bullet.getDataManager().getDirty()==null,"native dirty delta consumed");
        }
        bullet.getDataManager().setClean(); bullet.setTau(false); bullet.setChopper(true);
        check(bullet.getDataManager().get(critical)==127 && !bullet.getIsTau() && bullet.getIsChopper(),"bullet channels do not alias");
        for(int i=0;i<4;i++) nbtDoesNotPersistFlags(servers[i],clients[i]);
        check(!RendererRead.large(bullet),"shared renderer keeps non-Shrapnel guard before cast (Fireworks route)");
        DataParameter<Byte> mode=key(EntityShrapnel.class,"SHRAPNEL_TYPE");
        String[] setters={"setTrail","setVolcano","setWatz","setRadVolcano"};
        for(int i=0;i<setters.length;i++) {
            Method set=EntityShrapnel.class.getMethod(setters[i],boolean.class);
            shrapnel.getDataManager().set(mode,(byte)0x80); set.invoke(shrapnel,true);
            check(shrapnel.getShrapnelType()==i+1,"mode setter replaces whole byte, not a bit flag");
            transfer(shrapnel,remoteShrapnel,false); check(remoteShrapnel.getShrapnelType()==i+1,"shrapnel mode native sync");
            check(RendererRead.large(remoteShrapnel)==(i+1>=2),"exact renderer threshold");
            set.invoke(shrapnel,false); check(shrapnel.getShrapnelType()==0,"false resets full mode");
        }
        for(int raw=-128;raw<=127;raw++) { shrapnel.getDataManager().set(mode,(byte)raw); check(RendererRead.large(shrapnel)==(raw>=2),"renderer preserves signed byte comparison"); }
        NBTTagCompound n=new NBTTagCompound(); check(!shrapnel.writeToNBTOptional(n),"shrapnel optional save stays disabled");
        shrapnel.readEntityFromNBT(n); check(shrapnel.isDead,"actual vanilla throwable read plus original kill");
        System.out.println("PASS: "+checks+" native BYTE protocol, exhaustive bit-mask/mode, owner and NBT assertions");
    }
}
