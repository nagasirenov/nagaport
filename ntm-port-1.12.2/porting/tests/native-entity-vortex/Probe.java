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
    static void check(boolean value,String reason) { checks++; if(!value) throw new AssertionError(reason); }
    static int bits(float value) { return Float.floatToRawIntBits(value); }
    static void transfer(EntityBlackHole a,EntityBlackHole b,boolean full) throws Exception {
        PacketBuffer buffer=new PacketBuffer(Unpooled.buffer());
        new SPacketEntityMetadata(a.getEntityId(),a.getDataManager(),full).writePacketData(buffer);
        SPacketEntityMetadata packet=new SPacketEntityMetadata(); packet.readPacketData(buffer);
        check(buffer.readableBytes()==0 && packet.getEntityId()==a.getEntityId(),"metadata framing");
        b.getDataManager().setEntryValues(packet.getDataManagerEntries()); buffer.release();
    }
    static void sync(EntityBlackHole a,EntityBlackHole b) throws Exception {
        DataParameter<Float> key=EntityBlackHole.BLACK_HOLE_SIZE;
        check(bits(a.getBlackHoleSize())==bits(0.5F),"inherited registration default half");
        check(key.getSerializer()==DataSerializers.FLOAT && key.getId()==6,"base allocates native FLOAT after six vanilla entries");
        Set<Integer> ids=new HashSet<>();
        for(EntityDataManager.DataEntry<?> e:a.getDataManager().getAll()) check(ids.add(e.getKey().getId()),"no duplicate vanilla/custom registration");
        check(ids.size()==7,"one inherited base entry only");
        transfer(a,b,true); check(bits(b.getBlackHoleSize())==bits(0.5F),"full default packet");
        a.getDataManager().setClean(); a.getDataManager().set(key,0.5F); check(!a.getDataManager().isDirty(),"same Float no dirty");
        for(float value:new float[]{0F,-0F,Float.MIN_VALUE,-Float.MIN_VALUE,3.25F,Float.MAX_VALUE,-Float.MAX_VALUE,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.intBitsToFloat(0x7fc01234)}) {
            a.getDataManager().set(key,value); check(a.getDataManager().isDirty(),"changed Float dirty");
            transfer(a,b,false); check(bits(b.getBlackHoleSize())==bits(value),"native FLOAT packet retains raw bits");
            check(bits(RendererReads.RenderBlackHole(b))==bits(value),"actual external BlackHole renderer read");
            check(bits(RendererReads.RenderQuasar(b))==bits(value),"actual external Quasar renderer read");
            check(!a.getDataManager().isDirty() && a.getDataManager().getDirty()==null,"delta consumed/reset");
        }
        a.getDataManager().set(key,Float.intBitsToFloat(0x7fc05678)); check(!a.getDataManager().isDirty(),"NaN equals ignores payload as legacy Float.equals");
        List<EntityDataManager.DataEntry<?>> snapshot=a.getDataManager().getAll();
        a.getDataManager().set(key,9F);
        for(EntityDataManager.DataEntry<?> e:snapshot) if(e.getKey().getId()==key.getId()) check(bits((Float)e.getValue())==0x7fc01234,"snapshot independent of subsequent scalar set");
        a.getDataManager().setClean(); a.getDataManager().setDirty(key); transfer(a,b,false); check(b.getBlackHoleSize()==9F,"native forced dirty characterization");
    }
    static void nbt(EntityBlackHole a,EntityBlackHole b) throws Exception {
        for(float value:new float[]{-0F,-12.25F,Float.MAX_VALUE,Float.POSITIVE_INFINITY,Float.intBitsToFloat(0x7fc01234)}) {
            a.getDataManager().set(EntityBlackHole.BLACK_HOLE_SIZE,value); a.noBreak();
            NBTTagCompound out=new NBTTagCompound(); a.writeEntityToNBT(out); b.readEntityFromNBT(out);
            check(bits(b.getBlackHoleSize())==bits(value),"explicit raw float NBT (no normalization)");
            check(!b.breaksBlocks && out.hasKey("size",5) && out.hasKey("breaksBlocks",1),"base NBT size float and breaksBlocks byte");
            java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();
            net.minecraft.nbt.CompressedStreamTools.write(out,new java.io.DataOutputStream(bytes));
            NBTTagCompound disk=net.minecraft.nbt.CompressedStreamTools.read(new java.io.DataInputStream(new java.io.ByteArrayInputStream(bytes.toByteArray())));
            b.readEntityFromNBT(disk);
            check(Float.floatToIntBits(b.getBlackHoleSize())==Float.floatToIntBits(value),"native NBT binary save/reload (NaNs canonically compared)");
            check(!b.breaksBlocks,"breaksBlocks survives actual binary NBT");
        }
        b.readEntityFromNBT(new NBTTagCompound()); check(bits(b.getBlackHoleSize())==0 && !b.breaksBlocks,"missing legacy NBT remains size0 and false");
    }
    public static void main(String[] args) throws Exception {
        if(args[0].equals("descendant-first")) { Class.forName("EntityQuasar"); Class.forName("EntityRagingVortex"); Class.forName("EntityVortex"); }
        else Class.forName("EntityBlackHole");
        EntityBlackHole[] servers={new EntityBlackHole(null),new EntityVortex(null),new EntityRagingVortex(null),new EntityQuasar(null)};
        EntityBlackHole[] clients={new EntityBlackHole(null),new EntityVortex(null),new EntityRagingVortex(null),new EntityQuasar(null)};
        for(int i=0;i<servers.length;i++) { sync(servers[i],clients[i]); nbt(servers[i],clients[i]); }
        for(Class<?> c:new Class<?>[]{EntityVortex.class,EntityRagingVortex.class,EntityQuasar.class})
            for(Field f:c.getDeclaredFields()) check(f.getType()!=DataParameter.class,"descendants do not shadow or allocate key");
        EntityBlackHole[] sized={new EntityBlackHole(null,-4.5F),new EntityVortex(null,-4.5F),new EntityRagingVortex(null,-4.5F),new EntityQuasar(null,-4.5F)};
        for(EntityBlackHole e:sized) check(e.getBlackHoleSize()==-4.5F && e.getDataManager().isDirty(),"actual size constructor sets inherited entry without clamp");
        EntityVortex vortex=new EntityVortex(null,1F); check(vortex.shrinkRate==0.0025F,"original shrink default");
        check(vortex.setShrinkRate(0.125F)==vortex,"fluent shrink setter");
        vortex.advanceSizeBeforeBaseUpdate(); check(vortex.getBlackHoleSize()==0.875F && !vortex.isDead,"exact pre-super shrink computation");
        EntityVortex remote=new EntityVortex(null); transfer(vortex,remote,false); check(remote.getBlackHoleSize()==0.875F,"shrunk size metadata");
        NBTTagCompound vn=new NBTTagCompound(); vortex.writeEntityToNBT(vn); remote.readEntityFromNBT(vn);
        check(remote.shrinkRate==0.125F && remote.getBlackHoleSize()==0.875F,"real subclass + base NBT chain");
        vortex.setShrinkRate(1F); vortex.advanceSizeBeforeBaseUpdate(); check(vortex.isDead && vortex.getBlackHoleSize()==-0.125F,"negative size retained before death, no cap");
        EntityRagingVortex raging=new EntityRagingVortex(null,10F);
        Field rand=Entity.class.getDeclaredField("rand"); rand.setAccessible(true); ((Random)rand.get(raging)).setSeed(0);
        float expected=10F;
        for(int timer:new int[]{-19,-38,-57}) {
            expected=expected-(float)(Math.sin(timer)*Math.PI/20D)*0.35F-0.0F;
            raging.advanceSizeBeforeBaseUpdate(); check(raging.timer==timer && bits(raging.getBlackHoleSize())==bits(expected),"original <=20 timer subtraction and pulse unchanged");
        }
        NBTTagCompound rn=new NBTTagCompound(); raging.writeEntityToNBT(rn); EntityRagingVortex loaded=new EntityRagingVortex(null); loaded.readEntityFromNBT(rn);
        check(loaded.timer==-57 && bits(loaded.getBlackHoleSize())==bits(expected),"raging timer and inherited size NBT");
        System.out.println("PASS: "+checks+" native inherited FLOAT/metadata/NBT/renderer-expression/prefix assertions ("+args[0]+")");
    }
}
