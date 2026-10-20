import java.lang.reflect.*;
import java.util.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.util.math.MathHelper;

/** Real native IProjectile invocation + source-exact complete owned callback bodies.
 * Models deliberately exclude complete NTM owners, constructors, worlds, flight and payloads. */
public final class Probe {
 static int checks;
 static void check(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
 static final class TraceRandom extends Random {
  final StringBuilder trace=new StringBuilder(); final boolean scripted; int g,b;
  TraceRandom(long seed,boolean scripted){super(seed);this.scripted=scripted;}
  public double nextGaussian(){trace.append('G');int i=g++;return scripted?new double[]{0.25,-0.5,1.75}[i%3]:super.nextGaussian();}
  public boolean nextBoolean(){trace.append('B');int i=b++;return scripted?(i%3!=1):super.nextBoolean();}
 }
 static void initial(Models.Boundary e,TraceRandom r){e.random(r);e.motionX=7.25;e.motionY=-11.5;e.motionZ=13.75;e.rotationYaw=23.5F;e.prevRotationYaw=-25.5F;e.rotationPitch=29.5F;e.prevRotationPitch=-31.5F;e.ticksInGround=97;}
 static long[] state(Models.Boundary e){return new long[]{Double.doubleToLongBits(e.motionX),Double.doubleToLongBits(e.motionY),Double.doubleToLongBits(e.motionZ),Float.floatToIntBits(e.rotationYaw),Float.floatToIntBits(e.prevRotationYaw),Float.floatToIntBits(e.rotationPitch),Float.floatToIntBits(e.prevRotationPitch),e.ticksInGround};}
 static void compare(Models.Boundary a,Models.Boundary b,String label){check(Arrays.equals(state(a),state(b)),label+" motion/rotation/reset bits differ: "+Arrays.toString(state(a))+" != "+Arrays.toString(state(b)));}
 static void legacy(Models.Boundary e,double[] input)throws Exception{e.getClass().getMethod("setThrowableHeading",double.class,double.class,double.class,float.class,float.class).invoke(e,input[0],input[1],input[2],(float)input[3],(float)input[4]);}
 static void nativeCall(Models.Boundary e,double[] input){((IProjectile)e).shoot(input[0],input[1],input[2],(float)input[3],(float)input[4]);}
 static final double[][] INPUTS={
  {1,2,3,1.5,0.75},{-17,23,-41,-2.25,1.0},{0,0,0,1,1},{0,-3,0,0,0},{-0.0,0.0,-0.0,-0.0,-0.0},
  {3,4,0,Float.NaN,2},{Double.NaN,2,3,1,0},{Double.POSITIVE_INFINITY,-2,3,2,1},
  {Double.MIN_VALUE,Double.MIN_VALUE,Double.MIN_VALUE,Float.MIN_VALUE,-1},{Double.MAX_VALUE,Double.MAX_VALUE,-Double.MAX_VALUE,1,Float.POSITIVE_INFINITY},
  {1.125,-2.25,7.875,25,0},{1e-20,-3e-21,5e-21,-8,-0.125},{1,0,0,Float.POSITIVE_INFINITY,1}
 };
 static final long[] SEEDS={0,1,-1,42,987654321,Long.MIN_VALUE,Long.MAX_VALUE};
 static Models.Boundary fresh(Class<?> c)throws Exception{return (Models.Boundary)c.getDeclaredConstructor().newInstance();}
 static void family()throws Exception{
  for(int i=0;i<Models.CLASSES.length;i++){
   Class<?> nc=Models.CLASSES[i],oc=Models.OLD_CLASSES[i];
   check(IProjectile.class.isAssignableFrom(nc),nc+" genuine interface");check(!IProjectile.class.isAssignableFrom(oc),oc+" explicit legacy test boundary, not a fake native implementation");
   Method m=nc.getMethod("shoot",double.class,double.class,double.class,float.class,float.class);check(m.getDeclaringClass()==Models.OWNERS[i],nc+" correct inherited callback owner");
   try{nc.getMethod("setThrowableHeading",double.class,double.class,double.class,float.class,float.class);throw new AssertionError("legacy bridge exists");}catch(NoSuchMethodException expected){checks++;}
   for(long seed:SEEDS)for(double[] input:INPUTS){
    Models.Boundary n=fresh(nc),o=fresh(oc);TraceRandom nr=new TraceRandom(seed,false),or=new TraceRandom(seed,false);initial(n,nr);initial(o,or);
    nativeCall(n,input);legacy(o,input);compare(n,o,nc+" seed "+seed);
    check(nr.trace.toString().equals(or.trace.toString()),"RNG order");check(nr.nextLong()==or.nextLong(),"RNG state including Gaussian cache consumption");
    nativeCall(n,input);legacy(o,input);compare(n,o,"second shot preserves Gaussian cached draw semantics");check(nr.nextLong()==or.nextLong(),"second-shot RNG state");
   }
   Models.Boundary n=fresh(nc);TraceRandom r=new TraceRandom(9,true);initial(n,r);double[] input=INPUTS[0];nativeCall(n,input);
   String owner=Models.OWNERS[i].getSimpleName();boolean noop=owner.equals("EntityChopperMine"),signed=owner.equals("EntityBullet")||owner.equals("EntityRocket")||owner.equals("EntityExplosiveBeam")||owner.equals("EntitySchrab");
   check(r.trace.toString().equals(noop?"":signed?"GBGBGB":"GGG"),nc+" exact scripted RNG sequence");
   Models.Boundary expected=new Models.Boundary();initial(expected,new TraceRandom(9,true));
   if(!noop){
    double x=input[0],y=input[1],z=input[2];float length=(float)Math.sqrt(x*x+y*y+z*z);x/=length;y/=length;z/=length;
    double spread=owner.equals("EntityExplosiveBeam")?0.002499999832361937D:owner.equals("EntityThrowableNT")?Models.MULTIPLIERS[i]:0.007499999832361937D;
    x+=0.25*(signed?-1:1)*spread*(double)(float)input[4];y+=-0.5*spread*(double)(float)input[4];z+=1.75*(signed?-1:1)*spread*(double)(float)input[4];
    x*=(double)(float)input[3];y*=(double)(float)input[3];z*=(double)(float)input[3];expected.motionX=x;expected.motionY=y;expected.motionZ=z;
    expected.prevRotationYaw=expected.rotationYaw=(float)(Math.atan2(x,z)*180.0D/Math.PI);
    if(!owner.equals("EntityGrenadeBouncyBase")){float h=(float)Math.sqrt(x*x+z*z);expected.prevRotationPitch=expected.rotationPitch=(float)(Math.atan2(y,(double)h)*180.0D/Math.PI);expected.ticksInGround=0;}
   }
   compare(n,expected,nc+" independent scripted golden formula");
  }
 }
 static final class Dispatch extends Models.EntityThrowableNT {
  int shots,helpers; final StringBuilder order=new StringBuilder();
  @Override public void shoot(double x,double y,double z,float v,float a){shots++;super.shoot(x,y,z,v,a);}
  @Override protected double headingForceMult(){helpers++;order.append('H');return new double[]{0.25,0.5,1.0}[(helpers-1)%3];}
 }
 static void virtualDispatch(){
  Dispatch e=new Dispatch();e.random(new Random(){int g;public double nextGaussian(){e.order.append('G');return ++g;}});
  IProjectile typed=e;typed.shoot(1,2,3,2F,0.5F);
  check(e.shots==1&&e.helpers==3,"one native virtual callback, three still-virtual heading helpers, no recursion/forwarding");check(e.order.toString().equals("GHGHGH"),"original RNG/helper interleaving");
  float len=MathHelper.sqrt(14D);check(Double.doubleToLongBits(e.motionX)==Double.doubleToLongBits((1D/(double)len+1D*0.25D*0.5D)*2D),"first dynamic helper result");
  check(Double.doubleToLongBits(e.motionZ)==Double.doubleToLongBits((3D/(double)len+3D*1D*0.5D)*2D),"third dynamic helper result");
 }
 public static void main(String[] args)throws Exception{family();virtualDispatch();System.out.println("PASS "+checks+" native interface/source-exact callback assertions; 90 modeled inheritance nodes, 7 complete callbacks, 3 complete heading helpers, seeded/scripted RNG and IEEE edges. No complete NTM owner/gameplay claim.");}
}
