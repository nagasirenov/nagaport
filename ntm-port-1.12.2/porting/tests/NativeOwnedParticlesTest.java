import com.hbm.particle.*;
import java.lang.reflect.*;
import java.nio.*;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.VertexFormat;

/** Full production classes compile; owned GL/font calls are not invoked by this bounded test. */
public final class NativeOwnedParticlesTest {
    static int checks;
    static void check(boolean value,String label){checks++;if(!value)throw new AssertionError(label);}
    static void close(double a,double b,String label){check(Math.abs(a-b)<1e-6,label+": "+a+" != "+b);}
    static final class HazeWorld extends net.minecraft.world.World {
        int spawns; net.minecraft.util.EnumParticleTypes kind; double lastY;
        HazeWorld(){super(null,null,null,null,true);}
        protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
        protected boolean isChunkLoaded(int x,int z,boolean empty){return true;}
        public net.minecraft.util.math.BlockPos getHeight(net.minecraft.util.math.BlockPos position){return new net.minecraft.util.math.BlockPos(position.getX(),80,position.getZ());}
        public void spawnParticle(net.minecraft.util.EnumParticleTypes type,double x,double y,double z,double mx,double my,double mz,int... parameters){spawns++;kind=type;lastY=y;}
    }
    static void stationary()throws Exception {
        NativeAtlasParticlesTest.LightWorld world=NativeAtlasParticlesTest.world();
        ParticleFoundry foundry=new ParticleFoundry(null,world,2,64,3,0xffa040,2,3,1,.5);
        for(int i=0;i<20;i++)foundry.onUpdate();check(foundry.isAlive(),"foundry alive20");foundry.onUpdate();check(!foundry.isAlive(),"foundry expires21");
        close(NativeAtlasParticlesTest.number(foundry,"posX"),2,"foundry remains stationary");
        for(Particle p:new Particle[]{new ParticleRadiationFog(null,world,2,64,3),new ParticleProblem(null,world,2,64,3)}) {
            NativeAtlasParticlesTest.set(p,"particleAge",398);p.onUpdate();check(p.isAlive(),"fog original minimum400 lifetime");p.onUpdate();check(!p.isAlive(),"fog expires400");close(NativeAtlasParticlesTest.number(p,"posY"),64,"fog does not move");
        }
        ParticleRBMKMush mush=new ParticleRBMKMush(null,world,2,64,3,3);NativeAtlasParticlesTest.set(mush,"age",49);mush.onUpdate();check(!mush.isAlive(),"mush equality expiry50");
        ParticleContrail trail=new ParticleContrail(null,world,2,64,3);int max=(int)NativeAtlasParticlesTest.number(trail,"maxAge");trail.onUpdate();close(NativeAtlasParticlesTest.number(trail,"particleAlpha"),1,"trail alpha before increment");NativeAtlasParticlesTest.set(trail,"age",max-1);trail.onUpdate();check(!trail.isAlive(),"trail equality expiry");
        Field unsafeField=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");unsafeField.setAccessible(true);
        HazeWorld hazeWorld=(HazeWorld)((sun.misc.Unsafe)unsafeField.get(null)).allocateInstance(HazeWorld.class);
        NativeAtlasParticlesTest.set(hazeWorld,"rand",new java.util.Random(2));NativeAtlasParticlesTest.set(hazeWorld,"profiler",new net.minecraft.profiler.Profiler());
        ParticleHaze haze=new ParticleHaze(null,hazeWorld,2,64,3);NativeAtlasParticlesTest.set(haze,"maxAge",2);haze.onUpdate();check(haze.isAlive(),"haze first tick alive");haze.onUpdate();check(!haze.isAlive(),"haze expiry");check(hazeWorld.spawns==2&&hazeWorld.kind==net.minecraft.util.EnumParticleTypes.LAVA,"haze retains nested lava particle including expiry tick");close(hazeWorld.lastY,80.1,"native height plus original offset");
        BufferBuilder b=new BufferBuilder(128);b.begin(7,ParticleVertexFormats.LIT_TEXTURED);b.pos(1,2,3).tex(.25,.75).color(.5F,.25F,1F,.75F).lightmap(0,240).normal(0,1,0).endVertex();b.finishDrawing();check(b.getVertexFormat().getSize()==32,"native lightmapped normal vertex format");ByteBuffer v=b.getByteBuffer().order(ByteOrder.nativeOrder());check(v.get(28)==0&&v.get(29)==127&&v.get(30)==0,"native explicit normal");check((v.getShort(24)&65535)==240,"native explicit lightmap");
    }
    public static void main(String[] args)throws Exception {
        NativeAtlasParticlesTest.LightWorld world=NativeAtlasParticlesTest.world();
        ParticleGasFlame flame=new ParticleGasFlame(world,2,64,3,.1,.2,.3,2);
        check(NativeAtlasParticlesTest.number(flame,"particleMaxAge")>=30&&NativeAtlasParticlesTest.number(flame,"particleMaxAge")<=42,"gas original lifetime range");
        check(!(Boolean)NativeAtlasParticlesTest.get(flame,"canCollide")&&flame.shouldDisableDepth(),"gas no collision/depth writes");
        NativeAtlasParticlesTest.set(flame,"motionX",1D);NativeAtlasParticlesTest.set(flame,"motionY",2D);NativeAtlasParticlesTest.set(flame,"motionZ",3D);
        flame.onUpdate();close(NativeAtlasParticlesTest.number(flame,"motionY"),2.005,"gas restores pre-smoke Y before lift");
        close(NativeAtlasParticlesTest.number(flame,"motionX"),.96*.75,"gas native smoke drag then original drag");
        check(flame.getBrightnessForRender(0)==15728880,"gas original fixed light");
        ParticleDebug debug=new ParticleDebug(null,world,2,64,3,.1,.2,.3,0x4080c0);
        check(debug.getFXLayer()==3&&!(Boolean)NativeAtlasParticlesTest.get(debug,"canCollide"),"debug owned layer/no collision");
        close(debug.getRedColorF(),64/255F,"debug R");close(debug.getGreenColorF(),128/255F,"debug G");close(debug.getBlueColorF(),192/255F,"debug B");
        debug.onUpdate();close(NativeAtlasParticlesTest.number(debug,"posX"),2.1,"debug motion");
        ParticleText text=new ParticleText(world,2,64,3,0xabcdef,"power");
        check(text.getFXLayer()==3&&!(Boolean)NativeAtlasParticlesTest.get(text,"canCollide"),"text owned layer/no collision");
        text.onUpdate();close(NativeAtlasParticlesTest.number(text,"posY"),64.01,"text drift");
        ParticleDebugLine line=new ParticleDebugLine(world,2,64,3,4,5,6,0x123456);
        for(int i=0;i<60;i++){line.onUpdate();check(line.isAlive(),"line alive through60 updates");}
        line.onUpdate();check(!line.isAlive(),"line postincrement expiry61");close(NativeAtlasParticlesTest.number(line,"posX"),2,"line stationary");
        close(NativeAtlasParticlesTest.number(line,"motionX"),4,"line direction retained rather than moved");
        Field formatField=ParticleDebugLine.class.getDeclaredField("LINE_FORMAT");formatField.setAccessible(true);VertexFormat format=(VertexFormat)formatField.get(null);
        BufferBuilder buffer=new BufferBuilder(256);buffer.begin(1,format);
        Method append=ParticleDebugLine.class.getDeclaredMethod("appendLine",BufferBuilder.class,double.class,double.class,double.class,double.class,double.class,double.class,int.class,int.class);append.setAccessible(true);
        append.invoke(null,buffer,1D,2D,3D,5D,7D,9D,0x123456,0x00a00050);buffer.finishDrawing();
        check(buffer.getVertexCount()==2,"native line has two vertices");ByteBuffer data=buffer.getByteBuffer().order(ByteOrder.nativeOrder());
        for(int i=0;i<2;i++){int o=i*28;double[] xyz=i==0?new double[]{1,2,3}:new double[]{5,7,9};for(int j=0;j<3;j++)close(data.getFloat(o+4*j),xyz[j],"line position");check((data.get(o+20)&255)==0x12&&(data.get(o+21)&255)==0x34&&(data.get(o+22)&255)==0x56&&(data.get(o+23)&255)==255,"line color");check((data.getShort(o+24)&65535)==0x50&&(data.getShort(o+26)&65535)==0xa0,"line lightmap");}
        stationary();
        System.out.println("PASS "+checks+" native owned particle lifecycle/line-buffer assertions; full ten production classes compile. No owned GL/font rendering or game acceptance.");
    }
}
