import java.lang.reflect.*;
import java.util.*;
import sun.misc.Unsafe;
/** Generated paired fixture; actual original/native engine types, no World shim. */
public final class LegacyNumericFixture {
 static final Unsafe U;
 static {try {Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);U=(Unsafe)f.get(null);}catch(Exception e){throw new ExceptionInInitializerError(e);}}
 static final class Context {
  ahb world;
  Context member=this;
  int[] ints;long[] longs;float[] floats;double[] doubles;String name,fail;
  int nullMode;
  List<String> trace=new ArrayList<>();
  void mark(String s){trace.add(s);if(s.equals(fail))throw new IllegalStateException("scripted:"+s);}
  Context outer(){mark("receiver.outer");return nullMode==2?null:this;}
  Context nested(){mark("receiver.nested");return nullMode==3?null:this;}
  ahb getWorld(){mark("receiver.getWorld");return world;}
  Context arg(String label){mark(label);return this;}
 }
  static void call0_0(Context c) { c.outer().member.nested().world.a(c.arg("x").member.ints[0],c.arg("y").member.ints[1],c.arg("z").member.ints[2],c.arg("name").member.name,c.arg("volume").member.floats[0],c.arg("pitch").member.floats[1]); }
 static void call0_1(Context c) { c.outer().member.nested().getWorld().a(c.arg("x").member.ints[0],c.arg("y").member.ints[1],c.arg("z").member.ints[2],c.arg("name").member.name,c.arg("volume").member.floats[0],c.arg("pitch").member.floats[1]); }
 static void call1_0(Context c) { c.outer().member.nested().world.a(c.arg("x").member.doubles[0],c.arg("y").member.ints[1],c.arg("z").member.doubles[2],c.arg("name").member.name,c.arg("volume").member.floats[0],c.arg("pitch").member.floats[1]); }
 static void call1_1(Context c) { c.outer().member.nested().getWorld().a(c.arg("x").member.doubles[0],c.arg("y").member.ints[1],c.arg("z").member.doubles[2],c.arg("name").member.name,c.arg("volume").member.floats[0],c.arg("pitch").member.floats[1]); }
 static void call2_0(Context c) { c.outer().member.nested().world.a(c.arg("x").member.longs[0],c.arg("y").member.floats[2],c.arg("z").member.ints[2],c.arg("name").member.name,c.arg("volume").member.ints[0],c.arg("pitch").member.longs[1]); }
 static void call2_1(Context c) { c.outer().member.nested().getWorld().a(c.arg("x").member.longs[0],c.arg("y").member.floats[2],c.arg("z").member.ints[2],c.arg("name").member.name,c.arg("volume").member.ints[0],c.arg("pitch").member.longs[1]); }
 static void call3_0(Context c) { c.outer().member.nested().world.a(c.arg("x").member.floats[0],c.arg("y").member.floats[1],c.arg("z").member.floats[2],c.arg("name").member.name,c.arg("volume").member.floats[3],c.arg("pitch").member.floats[4]); }
 static void call3_1(Context c) { c.outer().member.nested().getWorld().a(c.arg("x").member.floats[0],c.arg("y").member.floats[1],c.arg("z").member.floats[2],c.arg("name").member.name,c.arg("volume").member.floats[3],c.arg("pitch").member.floats[4]); }
 static void call4_0(Context c) { c.outer().member.nested().world.a(c.arg("x").member.longs[0],c.arg("y").member.longs[1],c.arg("z").member.longs[2],c.arg("name").member.name,c.arg("volume").member.longs[3],c.arg("pitch").member.longs[4]); }
 static void call4_1(Context c) { c.outer().member.nested().getWorld().a(c.arg("x").member.longs[0],c.arg("y").member.longs[1],c.arg("z").member.longs[2],c.arg("name").member.name,c.arg("volume").member.longs[3],c.arg("pitch").member.longs[4]); }
 static void call5_0(Context c) { c.outer().member.nested().world.a(c.arg("x").member.doubles[0],c.arg("y").member.doubles[1],c.arg("z").member.doubles[2],c.arg("name").member.name,c.arg("volume").member.floats[0],c.arg("pitch").member.floats[1]); }
 static void call5_1(Context c) { c.outer().member.nested().getWorld().a(c.arg("x").member.doubles[0],c.arg("y").member.doubles[1],c.arg("z").member.doubles[2],c.arg("name").member.name,c.arg("volume").member.floats[0],c.arg("pitch").member.floats[1]); }
 public static Object[] run(int shape,int receiver,int nullMode,String fail,String name,int[] ints,long[] longs,float[] floats,double[] doubles)throws Exception {
  Context c=new Context();c.ints=ints;c.longs=longs;c.floats=floats;c.doubles=doubles;c.name=name;c.fail=fail;c.nullMode=nullMode;
  List<Object[]> callbacks=new ArrayList<>();
  ahb real=(ahb)U.allocateInstance(bjf.class);
  if(real.getClass().getMethod("a",double.class,double.class,double.class,String.class,float.class,float.class).getDeclaringClass()!=ahb.class)throw new AssertionError("not genuine inherited World coordinate method");
  ahh observer=(ahh)Proxy.newProxyInstance(ahh.class.getClassLoader(),new Class[]{ahh.class},(p,m,a)->{
   if(m.getName().equals("a")&&a!=null&&a.length==6&&a[0] instanceof String){callbacks.add(a.clone());return null;}
   throw new AssertionError("unexpected original callback:"+m);
  });
  Field f=ahb.class.getDeclaredField("u");f.setAccessible(true);f.set(real,new ArrayList<>(Arrays.asList(observer)));
  c.world=nullMode==1?null:real;
  Throwable failure=null;
  try {switch(shape*2+receiver){case 0:call0_0(c);break;case 1:call0_1(c);break;case 2:call1_0(c);break;case 3:call1_1(c);break;case 4:call2_0(c);break;case 5:call2_1(c);break;case 6:call3_0(c);break;case 7:call3_1(c);break;case 8:call4_0(c);break;case 9:call4_1(c);break;case 10:call5_0(c);break;case 11:call5_1(c);break;default:throw new AssertionError("bad shape");}}catch(Throwable e){failure=e;}
  String exception=failure==null?null:failure.getClass().getName()+(failure instanceof IllegalStateException?":"+failure.getMessage():"");
  return new Object[]{c.trace,callbacks,exception};
 }
 public static String origin(){return ahb.class.getProtectionDomain().getCodeSource().getLocation().toString();}
}
