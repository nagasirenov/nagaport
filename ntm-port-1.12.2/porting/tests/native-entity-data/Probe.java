import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.*;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import io.netty.buffer.Unpooled;
import java.lang.reflect.*;
import java.util.*;

public class Probe {
    private static int checks;
    static void check(boolean b, String why) { checks++; if (!b) throw new AssertionError(why); }
    static DataParameter<Integer> key(Class<?> c, String n) throws Exception {
        Field f=c.getDeclaredField(n); f.setAccessible(true); return (DataParameter<Integer>) f.get(null);
    }
    static void transfer(Entity from, Entity to, boolean full) throws Exception {
        PacketBuffer out=new PacketBuffer(Unpooled.buffer());
        new SPacketEntityMetadata(from.getEntityId(),from.getDataManager(),full).writePacketData(out);
        SPacketEntityMetadata packet=new SPacketEntityMetadata(); packet.readPacketData(out);
        check(out.readableBytes()==0,"packet fully consumed");
        to.getDataManager().setEntryValues(packet.getDataManagerEntries()); out.release();
    }
    static void sync(Entity a, Entity b, DataParameter<Integer> k) throws Exception {
        check(a.getDataManager().get(k)==0,"default remains 0 (not public maxAge=100)");
        check(k.getSerializer()==DataSerializers.VARINT,"native integer serializer");
        Set<Integer> ids=new HashSet<>();
        for(EntityDataManager.DataEntry<?> e:a.getDataManager().getAll()) check(ids.add(e.getKey().getId()),"no duplicate inherited vanilla key");
        check(k.getId()>5,"custom id allocated after vanilla Entity keys");
        transfer(a,b,true); check(b.getDataManager().get(k)==0,"full packet carries zero default");
        a.getDataManager().setClean();
        a.getDataManager().set(k,0); check(!a.getDataManager().isDirty(),"equal value not dirty");
        for(int v:new int[]{1,-1,255,256,16777215,Integer.MAX_VALUE,Integer.MIN_VALUE,0}) {
            a.getDataManager().set(k,v); check(a.getDataManager().isDirty(),"changed value dirty");
            transfer(a,b,false); check(!a.getDataManager().isDirty(),"dirty extraction clears manager");
            check(a.getDataManager().getDirty()==null,"second dirty extraction empty");
            check(b.getDataManager().get(k)==v,"native packet integer round trip");
        }
        java.util.List<EntityDataManager.DataEntry<?>> snapshot=a.getDataManager().getAll();
        a.getDataManager().set(k,73);
        for(EntityDataManager.DataEntry<?> e:snapshot) if(e.getKey().getId()==k.getId()) check(e.getValue().equals(0),"native snapshot does not alias later integer assignment");
        transfer(a,b,true);
        check(b.getDataManager().get(k)==73,"full packet initial snapshot");
        a.getDataManager().setClean(); a.getDataManager().setDirty(k);
        transfer(a,b,false); check(b.getDataManager().get(k)==73,"explicit force dirty");
    }
    public static void main(String[] args) throws Exception {
        EntityRBMKDebris r=new EntityRBMKDebris(null), rc=new EntityRBMKDebris(null);
        EntityZirnoxDebris z=new EntityZirnoxDebris(null), zc=new EntityZirnoxDebris(null);
        DataParameter<Integer> debris=key(EntityDebrisBase.class,"DEBRIS_TYPE");
        sync(r,rc,debris); sync(z,zc,debris);
        check(EntityRBMKDebris.class.getDeclaredFields().length==0,"RBMK does not shadow base key");
        for(EntityRBMKDebris.DebrisType type:EntityRBMKDebris.DebrisType.values()) {
            r.setType(type); check(r.getType()==type,"RBMK ordinal getter"); transfer(r,rc,false); check(rc.getType()==type,"RBMK client ordinal");
        }
        for(EntityZirnoxDebris.DebrisType type:EntityZirnoxDebris.DebrisType.values()) {
            z.setType(type); check(z.getType()==type,"Zirnox ordinal getter"); transfer(z,zc,false); check(zc.getType()==type,"Zirnox client ordinal");
        }
        for(EntityDebrisBase e:new EntityDebrisBase[]{r,z}) {
            for(int value:new int[]{-1,12,Integer.MAX_VALUE,Integer.MIN_VALUE}) {
                NBTTagCompound in=new NBTTagCompound(); in.setInteger("debtype",value); e.readEntityFromNBT(in);
                NBTTagCompound out=new NBTTagCompound(); e.writeEntityToNBT(out);
                check(out.getInteger("debtype")==value,"raw NBT integer preserved (not enum-normalized)");
            }
            e.readEntityFromNBT(new NBTTagCompound()); check(e.getDataManager().get(debris)==0,"missing NBT defaults zero");
        }
        Set<DataParameter<Integer>> cloudKeys=Collections.newSetFromMap(new IdentityHashMap<DataParameter<Integer>, Boolean>());
        for(Class<?> c:new Class<?>[]{EntityCloudFleija.class,EntityCloudFleijaRainbow.class,EntityCloudTom.class,EntityCloudSolinium.class}) {
            Entity a=(Entity)c.getConstructor(net.minecraft.world.World.class).newInstance(new Object[]{null});
            Entity b=(Entity)c.getConstructor(net.minecraft.world.World.class).newInstance(new Object[]{null});
            DataParameter<Integer> k=key(c,"MAX_AGE"); check(cloudKeys.add(k),"cloud sibling key object not reused"); sync(a,b,k);
            c.getMethod("setMaxAge",int.class).invoke(a,-123); check((Integer)c.getMethod("getMaxAge").invoke(a)==-123,"cloud setter/getter no clamp");
            NBTTagCompound n=new NBTTagCompound(); n.setShort("age",(short)37); n.setShort("scale",(short)8);
            Method read=c.getDeclaredMethod("readEntityFromNBT",NBTTagCompound.class); read.invoke(a,n);
            check((Integer)c.getMethod("getMaxAge").invoke(a)==-123,"NBT does not persist/reset maxAge");
            NBTTagCompound out=new NBTTagCompound(); c.getDeclaredMethod("writeEntityToNBT",NBTTagCompound.class).invoke(a,out);
            check(out.getShort("age")==37 && !out.hasKey("maxAge"),"cloud age NBT only");
            if(c!=EntityCloudTom.class) check(out.getShort("scale")==8,"cloud scale short NBT");
        }
        System.out.println("PASS: "+checks+" native manager, inherited allocation, packet, dirty, enum and NBT assertions");
    }
}
