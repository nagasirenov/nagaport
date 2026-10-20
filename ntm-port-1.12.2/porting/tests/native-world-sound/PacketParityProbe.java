import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomSound;
import net.minecraft.util.SoundCategory;
import java.util.Random;
import java.net.URLClassLoader;
import java.net.URL;
import java.io.File;
import java.lang.reflect.*;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** Real 1.7.10 packet vs native 1.12.2 packet, not a model-only comparison. */
public final class PacketParityProbe {
    private static Class<?> oldType, oldBufferType;
    private static SoundEvent event;
    private static float legacyPitch(float value) {
        return Math.max(0, Math.min(255, (int)(value * 63.0F))) / 63.0F;
    }
    private static void same(double a, double b, String field) {
        if (Double.doubleToLongBits(a) != Double.doubleToLongBits(b)) throw new AssertionError(field+": "+a+" != "+b);
    }
    private static void same(float a, float b, String field) {
        if (Float.floatToIntBits(a) != Float.floatToIntBits(b)) throw new AssertionError(field+": "+a+" != "+b);
    }
    private static void test(double x, double y, double z, float volume, float pitch) throws Exception {
        Object legacy = oldType.getConstructor(String.class,double.class,double.class,double.class,float.class,float.class).newInstance("hbm:block.crateOpen", x,y,z,volume,pitch);
        io.netty.buffer.ByteBuf oldBytes=new io.netty.buffer.UnpooledByteBufAllocator(false).heapBuffer();
        Object oldBuffer=oldBufferType.getConstructor(io.netty.buffer.ByteBuf.class).newInstance(oldBytes);
        oldType.getMethod("b",oldBufferType).invoke(legacy,oldBuffer);
        Object oldReceived=oldType.newInstance();
        oldType.getMethod("a",oldBufferType).invoke(oldReceived,oldBuffer);
        oldBytes.release();
        SPacketCustomSound nativePacket = new SPacketCustomSound("hbm:block.crateOpen", SoundCategory.MASTER, x, y, z, volume, legacyPitch(pitch));
        PacketBuffer newBuffer = new PacketBuffer(new io.netty.buffer.UnpooledByteBufAllocator(false).heapBuffer());
        nativePacket.writePacketData(newBuffer);
        SPacketCustomSound newReceived = new SPacketCustomSound();
        newReceived.readPacketData(newBuffer);
        newBuffer.release();
        if(!oldType.getMethod("c").invoke(oldReceived).equals(newReceived.getSoundName())) throw new AssertionError("name");
        same((double)oldType.getMethod("d").invoke(oldReceived), newReceived.getX(), "x");
        same((double)oldType.getMethod("e").invoke(oldReceived), newReceived.getY(), "y");
        same((double)oldType.getMethod("f").invoke(oldReceived), newReceived.getZ(), "z");
        same((float)oldType.getMethod("g").invoke(oldReceived), newReceived.getVolume(), "volume");
        same((float)oldType.getMethod("h").invoke(oldReceived), newReceived.getPitch(), "pitch");
        SPacketSoundEffect registered=new SPacketSoundEffect(event,SoundCategory.MASTER,x,y,z,volume,legacyPitch(pitch));
        PacketBuffer registeredBytes=new PacketBuffer(new io.netty.buffer.UnpooledByteBufAllocator(false).heapBuffer());
        registered.writePacketData(registeredBytes);
        SPacketSoundEffect registeredReceived=new SPacketSoundEffect();
        registeredReceived.readPacketData(registeredBytes);
        registeredBytes.release();
        if(registeredReceived.getSound()!=event)throw new AssertionError("registered SoundEvent identity");
        if(registeredReceived.getCategory()!=SoundCategory.MASTER)throw new AssertionError("category");
        same(newReceived.getX(),registeredReceived.getX(),"registered x");
        same(newReceived.getY(),registeredReceived.getY(),"registered y");
        same(newReceived.getZ(),registeredReceived.getZ(),"registered z");
        same(newReceived.getVolume(),registeredReceived.getVolume(),"registered volume");
        same(newReceived.getPitch(),registeredReceived.getPitch(),"registered pitch");
    }
    public static void main(String[] args) throws Exception {
        net.minecraft.init.Bootstrap.register();
        event=new SoundEvent(new ResourceLocation("hbm", "audit_world_sound")).setRegistryName("hbm", "audit_world_sound");
        ForgeRegistries.SOUND_EVENTS.register(event);
        URLClassLoader legacyLoader=new URLClassLoader(new URL[]{new File(args[0]).toURI().toURL()},PacketParityProbe.class.getClassLoader());
        oldType=Class.forName("hc",true,legacyLoader);
        oldBufferType=Class.forName("et",true,legacyLoader);
        double[] positions = {0.0D, -0.0D, 0.124999D, -0.124999D, 0.125D, -0.125D, 0.5D, -0.5D, 1.99D, -1.99D, 16777217.125D, -16777217.125D, 3E7D, -3E7D, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
        float[] values = {0F, -0F, 0.5F, 0.7F, 0.7999999F, 1F, 1.01F, 4F, 255F/63F, 5F, -1F, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
        int n=0;
        for(double p:positions) for(float volume:values) for(float pitch:values) {test(p,p-1.125D,-p,volume,pitch);n++;}
        Random random=new Random(17101122L);
        for(int i=0;i<10000;i++) {test(Double.longBitsToDouble(random.nextLong()),Double.longBitsToDouble(random.nextLong()),Double.longBitsToDouble(random.nextLong()),Float.intBitsToFloat(random.nextInt()),Float.intBitsToFloat(random.nextInt()));n++;}
        System.out.println("PASS: "+n+" actual legacy/custom/registered native packet encode-decode parity cases (names, all coordinates, volume, quantized pitch), including negative fractions, large coordinates, NaN and infinities.");
    }
}
