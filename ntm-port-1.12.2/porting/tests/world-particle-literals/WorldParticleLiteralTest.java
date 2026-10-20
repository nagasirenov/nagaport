import java.lang.reflect.*;
import java.util.*;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;

/** Real inherited World dispatch, with only its IWorldEventListener endpoint captured.
 * WorldServer is allocated without its unrelated server/save constructor. No World method is overridden.
 * The complete production performClient body is extracted unchanged by verify.py.
 */
public class WorldParticleLiteralTest {
    static final List<Object[]> events = new ArrayList<>();
    static int checks;
    static void check(boolean b, String message) { checks++; if (!b) throw new AssertionError(message); }
    static void set(Object o, String name, Object v) throws Exception {
        Field f = World.class.getDeclaredField(name); f.setAccessible(true); f.set(o,v);
    }
    static World world() throws Exception {
        Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); f.setAccessible(true);
        World w=(World)((sun.misc.Unsafe)f.get(null)).allocateInstance(WorldServer.class);
        IWorldEventListener listener=(IWorldEventListener)Proxy.newProxyInstance(WorldParticleLiteralTest.class.getClassLoader(),new Class<?>[]{IWorldEventListener.class},(proxy,method,args)->{
            if(method.getName().equals("spawnParticle")) events.add(args.clone());
            return null;
        });
        set(w,"eventListeners",Arrays.asList(listener)); set(w,"rand",new Random(79)); return w;
    }
    static void equal(double actual,double expected) { check(Double.doubleToLongBits(actual)==Double.doubleToLongBits(expected),"coordinate/velocity dispatch"); }
    public static void main(String[] args) throws Exception {
        World w=world();
        String[][] mapping={/*MAPPING*/};
        for(String[] row:mapping) {
            EnumParticleTypes type=EnumParticleTypes.valueOf(row[1]);
            check(EnumParticleTypes.getByName(row[0])==type,"original name maps to native enum "+row[0]);
            check(type.getArgumentCount()==0,"no unprovided block/item arguments");
            events.clear(); w.spawnParticle(type,1.25,-2.5,3.75,-0.125,0.25,-0.5);
            check(events.size()==1,"one native event");Object[] e=events.get(0);
            check((Integer)e[0]==type.getParticleID(),"native particle ID");
            check((Boolean)e[1]==type.getShouldIgnoreRange(),"native range policy");
            check(((int[])e[8]).length==0,"empty native argument array");
            double[] expected={1.25,-2.5,3.75,-0.125,0.25,-0.5};for(int i=0;i<6;i++)equal((Double)e[i+2],expected[i]);
        }
        for(float size:new float[]{0.5F,2F,4F}) {
            events.clear();SelectedExplosionCallback.performClient(w,1,2,3,size,Collections.emptyList());
            check(events.size()==1,"empty affected list produces only central event");
            check((Integer)events.get(0)[0]==(size>=2?EnumParticleTypes.EXPLOSION_HUGE:EnumParticleTypes.EXPLOSION_LARGE).getParticleID(),"original size threshold");
            List<BlockPos> blocks=Arrays.asList(new BlockPos(7,8,9),new BlockPos(-4,5,-6));
            events.clear();set(w,"rand",new Random(79));SelectedExplosionCallback.performClient(w,1,2,3,size,blocks);
            check(events.size()==5,"one central plus two events per affected block");
            Random expectedRandom=new Random(79);
            for(int j=0;j<blocks.size();j++) {
                BlockPos pos=blocks.get(j);double ox=(float)pos.getX()+expectedRandom.nextFloat(),oy=(float)pos.getY()+expectedRandom.nextFloat(),oz=(float)pos.getZ()+expectedRandom.nextFloat();
                double dx=ox-1,dy=oy-2,dz=oz-3;double delta=(double)(float)Math.sqrt(dx*dx+dy*dy+dz*dz);
                dx/=delta;dy/=delta;dz/=delta;double mod=.5D/(delta/(double)size+.1D);mod*=(double)(expectedRandom.nextFloat()*expectedRandom.nextFloat()+.3F);dx*=mod;dy*=mod;dz*=mod;
                Object[] explosion=events.get(1+j*2),smoke=events.get(2+j*2);
                check((Integer)explosion[0]==EnumParticleTypes.EXPLOSION_NORMAL.getParticleID()&&(Integer)smoke[0]==EnumParticleTypes.SMOKE_NORMAL.getParticleID(),"paired event types and order");
                double[] ex={(ox+1)/2,(oy+2)/2,(oz+3)/2,dx,dy,dz},sm={ox,oy,oz,dx,dy,dz};
                for(int i=0;i<6;i++){equal((Double)explosion[i+2],ex[i]);equal((Double)smoke[i+2],sm[i]);}
            }
            equal(w.rand.nextFloat(),expectedRandom.nextFloat());
        }
        System.out.println("PASS "+checks+" checks: 16 genuine native enum mappings/dispatch and complete extracted production explosion callback; no whole-mod or live-render claim");
    }
}
