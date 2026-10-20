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
    static void check(boolean b, String why) { checks++; if (!b) throw new AssertionError(why); }
    @SuppressWarnings("unchecked")
    static <T> DataParameter<T> key(Class<?> c, String n) throws Exception {
        Field f=c.getDeclaredField(n); f.setAccessible(true); return (DataParameter<T>) f.get(null);
    }
    static void transfer(Entity from, Entity to, boolean full) throws Exception {
        PacketBuffer out=new PacketBuffer(Unpooled.buffer());
        new SPacketEntityMetadata(from.getEntityId(),from.getDataManager(),full).writePacketData(out);
        SPacketEntityMetadata packet=new SPacketEntityMetadata(); packet.readPacketData(out);
        check(packet.getEntityId()==from.getEntityId(),"metadata entity id retained");
        check(out.readableBytes()==0,"packet fully consumed");
        to.getDataManager().setEntryValues(packet.getDataManagerEntries()); out.release();
    }
    static void allocation(Entity e, int customCount) {
        Set<Integer> ids=new HashSet<>();
        for(EntityDataManager.DataEntry<?> entry:e.getDataManager().getAll()) check(ids.add(entry.getKey().getId()),"unique keys including vanilla");
        check(ids.size()==6+customCount,"expected vanilla Entity slots plus complete owner keys");
    }
    static void integers(Entity a, Entity b, DataParameter<Integer> k, int defaultValue) throws Exception {
        check(k.getId()>5,"allocated after vanilla Entity keys");
        check(k.getSerializer()==DataSerializers.VARINT,"native VARINT");
        check(a.getDataManager().get(k)==defaultValue,"original default");
        transfer(a,b,true); check(b.getDataManager().get(k)==defaultValue,"full default snapshot");
        a.getDataManager().setClean(); a.getDataManager().set(k,defaultValue);
        check(!a.getDataManager().isDirty(),"equal default no dirty");
        for(int v:new int[]{-1,0,255,256,Integer.MIN_VALUE,Integer.MAX_VALUE,42}) {
            a.getDataManager().set(k,v); check(a.getDataManager().isDirty(),"changed int dirty");
            transfer(a,b,false); check(b.getDataManager().get(k)==v,"signed raw integer roundtrip");
            check(!a.getDataManager().isDirty() && a.getDataManager().getDirty()==null,"delta resets dirty");
        }
        List<EntityDataManager.DataEntry<?>> snapshot=a.getDataManager().getAll();
        a.getDataManager().set(k,123);
        for(EntityDataManager.DataEntry<?> entry:snapshot) if(entry.getKey().getId()==k.getId()) check(entry.getValue().equals(42),"integer snapshot copy independent");
        a.getDataManager().setClean(); a.getDataManager().setDirty(k); transfer(a,b,false);
        check(b.getDataManager().get(k)==123,"force dirty native characterization");
    }
    static int bits(float f) { return Float.floatToRawIntBits(f); }
    static void floats(EntityFireLingering a, EntityFireLingering b, DataParameter<Float> k) throws Exception {
        check(k.getSerializer()==DataSerializers.FLOAT,"native FLOAT");
        check(bits(a.getDataManager().get(k))==0,"positive zero float default");
        transfer(a,b,true); check(bits(b.getDataManager().get(k))==0,"full zero float snapshot");
        a.getDataManager().setClean(); a.getDataManager().set(k,0.0F); check(!a.getDataManager().isDirty(),"equal float not dirty");
        for(float value:new float[]{-0.0F,Float.MIN_VALUE,-Float.MIN_VALUE,Float.MAX_VALUE,-Float.MAX_VALUE,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.intBitsToFloat(0x7fc01234)}) {
            a.getDataManager().set(k,value); check(a.getDataManager().isDirty(),"Float.equals change dirty including signed zero");
            transfer(a,b,false); check(bits(b.getDataManager().get(k))==bits(value),"FLOAT packet preserves raw IEEE bits");
            check(!a.getDataManager().isDirty() && a.getDataManager().getDirty()==null,"float delta reset");
        }
        a.getDataManager().set(k,Float.intBitsToFloat(0x7fc05678));
        check(!a.getDataManager().isDirty(),"different NaN payload compares equal, unchanged legacy Float.equals semantics");
        check(bits(a.getDataManager().get(k))==0x7fc01234,"equal NaN leaves previous value");
        List<EntityDataManager.DataEntry<?>> snapshot=a.getDataManager().getAll();
        a.getDataManager().set(k,7.25F);
        for(EntityDataManager.DataEntry<?> entry:snapshot) if(entry.getKey().getId()==k.getId()) check(bits((Float)entry.getValue())==0x7fc01234,"float snapshot independent");
        transfer(a,b,false);
    }
    public static void main(String[] args) throws Exception {
        if(args.length>0) { Class.forName("EntitySiegeLaser"); Class.forName("EntityWaypoint"); Class.forName("EntityFireLingering"); }
        EntityFireLingering fire=new EntityFireLingering(null), clientFire=new EntityFireLingering(null);
        EntityWaypoint waypoint=new EntityWaypoint(null), clientWaypoint=new EntityWaypoint(null);
        EntitySiegeLaser laser=new EntitySiegeLaser(null), clientLaser=new EntitySiegeLaser(null);
        DataParameter<Integer> fireType=key(EntityFireLingering.class,"FIRE_TYPE"), task=key(EntityWaypoint.class,"WAYPOINT_TYPE"), color=key(EntitySiegeLaser.class,"COLOR");
        DataParameter<Float> width=key(EntityFireLingering.class,"AREA_WIDTH"), height=key(EntityFireLingering.class,"AREA_HEIGHT");
        allocation(fire,3); allocation(waypoint,1); allocation(laser,1);
        check(fireType.getId()==6 && width.getId()==7 && height.getId()==8 && task.getId()==6 && color.getId()==6,"owner layouts stable in alternate class-load order");
        check(fireType!=task && task!=color && fireType!=color,"separate owner objects even when numeric indices coincide");
        check(width.getId()!=height.getId() && width.getId()!=fireType.getId(),"three owner-local unique keys");
        integers(fire,clientFire,fireType,0); integers(waypoint,clientWaypoint,task,0); integers(laser,clientLaser,color,0xffffff);
        floats(fire,clientFire,width); floats(fire,clientFire,height);
        check(fire.setArea(-12.5F,Float.POSITIVE_INFINITY)==fire,"fluent area setter retained");
        transfer(fire,clientFire,false);
        check(bits(clientFire.getDataManager().get(width))==bits(-12.5F) && Float.isInfinite(clientFire.getDataManager().get(height)),"setArea updates both independently with no caps");
        check(fire.setType(-77)==fire && fire.getType()==-77,"actual fire setter/getter");
        check(fire.maxAge==150 && fire.setDuration(-40)==fire && fire.maxAge==-40,"duration local field default/value unchanged");
        fire.getDataManager().setClean(); fire.setDuration(222); check(!fire.getDataManager().isDirty(),"duration not newly synchronized");
        NBTTagCompound empty=new NBTTagCompound(); fire.writeEntityToNBT(empty); check(empty.isEmpty(),"fire writes no persistent entity data");
        check(!fire.writeToNBTOptional(empty),"fire optional save suppressed");
        fire.readEntityFromNBT(empty); check(fire.isDead,"fire load still kills entity");
        for(int value:new int[]{0,-123,Integer.MIN_VALUE,Integer.MAX_VALUE}) {
            waypoint.setWaypointType(value); check(waypoint.getWaypointType()==value,"actual waypoint accessor");
            NBTTagCompound n=new NBTTagCompound(); waypoint.writeEntityToNBT(n);
            clientWaypoint.readEntityFromNBT(n); check(clientWaypoint.getWaypointType()==value && n.getInteger("type")==value,"waypoint raw integer NBT");
            check(laser.setColor(value)==laser && laser.getColor()==value,"laser fluent accessor no color mask");
            laser.setDamage(-5.25F).setExplosive(12.75F).setBreakChance(3.5F).setIncendiary();
            NBTTagCompound ln=new NBTTagCompound(); laser.writeEntityToNBT(ln); clientLaser.readEntityFromNBT(ln);
            NBTTagCompound copied=new NBTTagCompound(); clientLaser.writeEntityToNBT(copied);
            check(copied.getInteger("color")==value,"laser color raw integer NBT");
            check(copied.getFloat("damage")==-5.25F && copied.getFloat("explosive")==12.75F && copied.getFloat("breakChance")==3.5F && copied.getBoolean("incendiary"),"laser native superclass and own NBT values preserved");
        }
        clientWaypoint.readEntityFromNBT(new NBTTagCompound()); check(clientWaypoint.getWaypointType()==0,"missing task NBT zero");
        clientLaser.readEntityFromNBT(new NBTTagCompound()); check(clientLaser.getColor()==0,"missing color NBT remains zero, not registration white");
        System.out.println("PASS: "+checks+" native scalar metadata/dirty/ownership/NBT assertions");
    }
}
