import java.lang.reflect.*;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.profiler.Profiler;
import sun.misc.Unsafe;
public class NativeFieldTest {
 static int count;static String trace="";static void ok(boolean b,String s){count++;if(!b)throw new AssertionError(s);}
 static Entity recv(Entity e){trace+="R";return e;}static boolean rhs(boolean b){trace+="V";return b;}
 static boolean assign(Entity e,boolean b){return recv(e).collided=rhs(b);}static boolean read(Entity e){return recv(e).collided;}
 static boolean fail(){trace+="X";throw new IllegalStateException();}
 public static void main(String[] args)throws Exception{
  Field ufield=Unsafe.class.getDeclaredField("theUnsafe");ufield.setAccessible(true);Unsafe u=(Unsafe)ufield.get(null);
  String[] names={"collidedHorizontally","collidedVertically","collided"},olds={"E","F","G"};
  NativeProjection n=(NativeProjection)u.allocateInstance(NativeProjection.class);LegacyProjection o=(LegacyProjection)u.allocateInstance(LegacyProjection.class);
  for(int k=0;k<3;k++){
   Field f=Entity.class.getField(names[k]),g=sa.class.getField(olds[k]);
   ok(f.getDeclaringClass()==Entity.class&&f.getType()==boolean.class&&Modifier.isPublic(f.getModifiers())&&!Modifier.isFinal(f.getModifiers()),"native field owner/type/mutability");
   ok(g.getDeclaringClass()==sa.class&&g.getType()==boolean.class&&Modifier.isPublic(g.getModifiers())&&!Modifier.isFinal(g.getModifiers()),"original field owner/type/mutability");
   for(boolean b:new boolean[]{false,true}){f.setBoolean(n,b);g.setBoolean(o,b);ok(f.getBoolean(n)==g.getBoolean(o),"direct actual-engine values");}
  }
  trace="";ok(assign(n,true)&&n.collided&&trace.equals("RV"),"receiver before RHS and result");trace="";ok(read(n)&&trace.equals("R"),"single receiver read");
  trace="";try{assign(null,false);throw new AssertionError();}catch(NullPointerException expected){ok(trace.equals("RV"),"null after RHS");}
  trace="";try{recv(null).collided=fail();throw new AssertionError();}catch(IllegalStateException expected){ok(trace.equals("RX"),"RHS failure first");}
  double[] v={0D,-0D,1D,-1D,Double.NaN,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY};
  for(double a:v)for(double b:v)for(double c:v)for(double d:v)for(double e:v)for(double f:v){
   n.update(a,b,c,d,e,f);o.update(a,b,c,d,e,f);
   ok(n.collidedHorizontally==o.E&&n.collidedVertically==o.F&&n.collided==o.G&&n.onGround==o.D,"exact four production assignments original/native");
  }
  Field nf=World.class.getField("profiler"),of=ahb.class.getField("C");
  ok(nf.getDeclaringClass()==World.class&&nf.getType()==Profiler.class&&Modifier.isFinal(nf.getModifiers()),"native public final profiler");
  ok(of.getDeclaringClass()==ahb.class&&of.getType()==qi.class&&Modifier.isFinal(of.getModifiers()),"original public final profiler");
  WorldServer nw=(WorldServer)u.allocateInstance(WorldServer.class);mt ow=(mt)u.allocateInstance(mt.class);Profiler np=new Profiler();qi op=new qi();nf.setAccessible(true);of.setAccessible(true);nf.set(nw,np);of.set(ow,op);
  ok(nw.profiler==np&&ow.C==op,"field returns stored exact instance");np.profilingEnabled=true;op.a=true;
  nw.profiler.startSection("outer");ow.C.a("outer");ok(np.getNameOfLastSection().equals(op.c()),"start section path");
  nw.profiler.startSection("inner");ow.C.a("inner");ok(np.getNameOfLastSection().equals(op.c()),"nested section path");
  nw.profiler.endStartSection("replacement");ow.C.c("replacement");ok(np.getNameOfLastSection().equals(op.c()),"end-start section path");
  nw.profiler.endSection();ow.C.b();ok(np.getNameOfLastSection().equals(op.c()),"end section path");
  nw.profiler.endSection();ow.C.b();ok(np.getNameOfLastSection().equals(op.c()),"final section path");
  System.out.println("PASS "+count+" actual original/native field and exact collision-assignment projection checks; no movement lifecycle claim");
 }
}
