import com.hbm.particle.*;
import java.lang.reflect.*;
import java.nio.*;
import java.util.Random;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.PngSizeInfo;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/** Real native classes and buffers, with no GL context and no full-game acceptance claim. */
public final class NativeStationaryParticlesTest {
    static int checks;
    static void check(boolean b, String label) { checks++; if(!b) throw new AssertionError(label); }
    static void close(double a, double b, String label) { check(Math.abs(a-b)<1e-5, label+": "+a+" != "+b); }
    static Field field(Class<?> c, String name) throws Exception {
        while(c!=null) { try { Field f=c.getDeclaredField(name); f.setAccessible(true); return f; } catch(NoSuchFieldException e) { c=c.getSuperclass(); } }
        throw new NoSuchFieldException(name);
    }
    static Object get(Object o, String name) throws Exception { return field(o.getClass(),name).get(o); }
    static double number(Object o, String name) throws Exception { return ((Number)get(o,name)).doubleValue(); }
    static void set(Object o, String name, Object value) throws Exception { field(o.getClass(),name).set(o,value); }
    static final class NoContactWorld extends World {
        NoContactWorld() { super(null,null,null,null,true); }
        protected net.minecraft.world.chunk.IChunkProvider createChunkProvider() { throw new AssertionError("stationary particle requested chunks"); }
        protected boolean isChunkLoaded(int x, int z, boolean empty) { throw new AssertionError("stationary particle queried world"); }
        public java.util.List<net.minecraft.util.math.AxisAlignedBB> getCollisionBoxes(net.minecraft.entity.Entity entity, net.minecraft.util.math.AxisAlignedBB box) { throw new AssertionError("stationary particle queried collision"); }
    }
    static World world() throws Exception {
        Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); f.setAccessible(true);
        World world=(World)((sun.misc.Unsafe)f.get(null)).allocateInstance(NoContactWorld.class);
        set(world,"rand",new Random(77)); set(world,"profiler",new net.minecraft.profiler.Profiler()); return world;
    }
    static Particle[] particles(World w) {
        return new Particle[]{new ParticleAmatFlash(w,2,64,3,6),new ParticleHadron(null,w,2,64,3),new ParticleMukeWave(null,w,2,64,3),new ParticlePlasmaBlast(null,w,2,64,3,.2F,.4F,.8F,30,60),new ParticleRBMKFlame(null,w,2,64,3,60),new ParticleRBMKSteam(null,w,2,64,3)};
    }
    static void lifecycle() throws Exception {
        World w=world(); Particle[] ps=particles(w);
        for(Particle p:ps) {
            check(p.getClass().getSuperclass()==Particle.class,"direct native Particle"); check(p.getFXLayer()==3,"owned layer");
            check(!(Boolean)get(p,"canCollide"),"stationary visual has no entity contacts");
            close(number(p,"prevPosX"),2,"native first position");close(number(p,"prevPosY"),64,"native first height");
            int max=(int)number(p,"particleMaxAge");
            for(int i=0;i<max;i++) p.onUpdate();
            check(p.isAlive(),"original postincrement still alive at max age"); p.onUpdate(); check(!p.isAlive(),"original postincrement expiry");
            close(number(p,"posX"),2,"stationary X");close(number(p,"posY"),64,"stationary Y");close(number(p,"posZ"),3,"stationary Z");
        }
        ParticleMukeWave wave=new ParticleMukeWave(null,w,0,0,0);wave.setup(75,40);close(number(wave,"waveScale"),75,"wave caller scale");close(number(wave,"particleMaxAge"),40,"wave caller lifetime");
        ParticlePlasmaBlast plasma=new ParticlePlasmaBlast(null,w,0,0,0,.2F,.4F,.8F,30,60);plasma.setScale(80);plasma.setMaxAge(35);
        close(number(plasma,"particleScale"),80,"plasma caller scale");close(number(plasma,"particleMaxAge"),35,"plasma caller lifetime");close(number(plasma,"pitch"),30,"local pitch");close(number(plasma,"yaw"),60,"local yaw");
        check(number(ps[4],"particleScale")>=1&&number(ps[4],"particleScale")<2,"flame scale range");
        ParticleManager manager=new ParticleManager(w,null);Particle p=new ParticleAmatFlash(w,2,64,3,1);manager.addEffect(p);
        check(manager.getStatistics().equals("0"),"native queued admission");manager.updateEffects();check(number(p,"particleAge")==0,"native first admission does not tick");manager.updateEffects();check(number(p,"particleAge")==1,"native next update ticks");
    }
    static ByteBuffer vertices(Particle p, Object... extra) throws Exception {
        Class<?>[] types=new Class<?>[extra.length+1];Object[] args=new Object[extra.length+1];types[0]=BufferBuilder.class;
        BufferBuilder buffer=new BufferBuilder(256);args[0]=buffer;
        for(int i=0;i<extra.length;i++){types[i+1]=extra[i] instanceof Float?float.class:double.class;args[i+1]=extra[i];}
        Method m=p.getClass().getDeclaredMethod("appendVertices",types);m.setAccessible(true);
        buffer.begin(7,ParticleVertexFormats.LIT_TEXTURED);m.invoke(p,args);buffer.finishDrawing();
        check(buffer.getVertexCount()==4,"owned native quad");check(buffer.getVertexFormat().getSize()==32,"native lightmapped normal format");
        ByteBuffer result=buffer.getByteBuffer().order(ByteOrder.nativeOrder());
        for(int i=0;i<4;i++){int o=i*32;check((result.getShort(o+24)&65535)==240&&result.getShort(o+26)==0,"original block light 240");check(result.get(o+28)==0&&result.get(o+29)==127&&result.get(o+30)==0,"up normal");}
        return result;
    }
    static void quad(ByteBuffer b,double[][] xyz,double uMin,double uMax,float r,float g,float blue,float alpha) {
        double[][] uv={{uMax,1},{uMax,0},{uMin,0},{uMin,1}};
        for(int i=0;i<4;i++){int o=i*32;for(int j=0;j<3;j++)close(b.getFloat(o+4*j),xyz[i][j],"original quad coordinate");close(b.getFloat(o+12),uv[i][0],"U");close(b.getFloat(o+16),uv[i][1],"V");check((b.get(o+20)&255)==(int)(r*255)&&(b.get(o+21)&255)==(int)(g*255)&&(b.get(o+22)&255)==(int)(blue*255)&&(b.get(o+23)&255)==(int)(Math.max(0F, Math.min(1F, alpha))*255),"original vertex RGBA");}
    }
    static void geometry() throws Exception {
        Particle[] ps=particles(world());
        for(int age:new int[]{0,5,10}) {
            for(Particle p:ps) set(p,"particleAge",age);
            float t=.5F, scale=(age+t)*.15F;
            quad(vertices(ps[1],t,2D,3D,4D,1F,0F,0F,0F,1F),new double[][]{{2-scale,3,4-scale},{2-scale,3,4+scale},{2+scale,3,4+scale},{2+scale,3,4-scale}},0,1,1,1,1,1-(age+t)/10F);
            scale=(1-(float)Math.pow(Math.E,(age+t)*-.125))*45F;
            quad(vertices(ps[2],t,2F,3F,4F),new double[][]{{2-scale,2.75,4-scale},{2-scale,2.75,4+scale},{2+scale,2.75,4+scale},{2+scale,2.75,4-scale}},0,1,1,1,1,1-(age+t)/25F);
            ((ParticlePlasmaBlast)ps[3]).setScale(12);scale=(1-(float)Math.pow(Math.E,(age+t)*-.125))*12;
            quad(vertices(ps[3],t),new double[][]{{-scale,0,-scale},{-scale,0,scale},{scale,0,scale},{scale,0,-scale}},0,1,.2F,.4F,.8F,1-(age+t)/20F);
            set(ps[4],"particleScale",1.5F);float u=age*5%14%5*(1F/14F),alpha=age/20F*.5F;
            quad(vertices(ps[4]),new double[][]{{-2.5,-3,0},{-2.5,3,0},{.5,3,0},{.5,-3,0}},u,u+1F/14F,1,1,1,alpha);
            u=((int)((age/10D)*20)%20-1)*(1F/20F);
            quad(vertices(ps[5]),new double[][]{{-1.9375,-.25,0},{-1.9375,3.75,0},{.0625,3.75,0},{.0625,-.25,0}},u,u+1F/20F,1,1,1,.25F);
        }
        set(ps[3],"particleAge",21);set(ps[3],"particleRed",2F);set(ps[3],"particleGreen",-.5F);
        ByteBuffer clamped=vertices(ps[3],.5F);check((clamped.get(20)&255)==255&&clamped.get(21)==0&&clamped.get(23)==0,"legacy RGBA clamp avoids last-frame wraparound");
        set(ps[4],"particleAge",61);ByteBuffer end=vertices(ps[4]);close(number(ps[4],"particleAge"),60,"flame last-frame age clamp");check(end.get(23)==0,"flame final alpha zero");
        set(ps[5],"particleAge",11);vertices(ps[5]);close(number(ps[5],"particleAge"),10,"steam last-frame age clamp");
        BufferBuilder fan=new BufferBuilder(256);fan.begin(6,DefaultVertexFormats.POSITION_COLOR);
        Method m=ParticleAmatFlash.class.getDeclaredMethod("appendFan",BufferBuilder.class,float.class,float.class,float.class);m.setAccessible(true);m.invoke(null,fan,10F,2F,.75F);fan.finishDrawing();
        check(fan.getVertexCount()==5&&fan.getVertexFormat().getSize()==16,"native five-vertex triangle fan");ByteBuffer data=fan.getByteBuffer().order(ByteOrder.nativeOrder());
        double[][] expected={{0,0,0},{-1.732,10,-1},{1.732,10,-1},{0,10,2},{-1.732,10,-1}};
        for(int i=0;i<5;i++){for(int j=0;j<3;j++)close(data.getFloat(i*16+j*4),expected[i][j],"fan coordinate");check((data.get(i*16+15)&255)==(i==0?191:0),"fan center/edge fade");}
        fan.reset();fan.begin(6,DefaultVertexFormats.POSITION_COLOR);m.invoke(null,fan,10F,2F,-.2F);fan.finishDrawing();check(fan.getByteBuffer().get(15)==0,"fan negative alpha clamped");
    }
    static void textures(String root) throws Exception {
        SimpleReloadableResourceManager resources=new SimpleReloadableResourceManager(new MetadataSerializer());resources.reloadResourcePack(new FolderResourcePack(new java.io.File(root)));
        Class<?>[] owners={ParticleHadron.class,ParticleMukeWave.class,ParticleRBMKFlame.class,ParticleRBMKSteam.class};int index=0;
        for(String name:new String[]{"hadron","shockwave","rbmk_fire","rbmk_jet_steam"}) {
            ResourceLocation location=new ResourceLocation("hbm","textures/particle/"+name+".png");
            check(field(owners[index++],"texture").get(null).equals(location),"actual production texture route "+name);
            try(IResource resource=resources.getResource(location)){PngSizeInfo info=PngSizeInfo.makeFromResource(resource);java.awt.image.BufferedImage pixels=javax.imageio.ImageIO.read(new java.io.File(root,"assets/hbm/textures/particle/"+name+".png"));check(info.pngWidth==pixels.getWidth()&&info.pngHeight==pixels.getHeight(),"native resource/header and actual PNG decode "+name);}
        }
    }
    public static void main(String[] args) throws Exception { lifecycle();geometry();textures(args[0]);System.out.println("PASS "+checks+" native stationary particle lifecycle/manager/buffer/resource checks; six complete production classes compile. No GL/game acceptance."); }
}
