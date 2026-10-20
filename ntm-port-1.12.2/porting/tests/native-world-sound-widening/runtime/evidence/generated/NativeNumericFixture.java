import java.lang.reflect.*;
import java.util.*;
import sun.misc.Unsafe;
/** Generated paired fixture; actual original/native engine types, no World shim. */
public final class NativeNumericFixture {
 static final Unsafe U;
 static {try {Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);U=(Unsafe)f.get(null);}catch(Exception e){throw new ExceptionInInitializerError(e);}}
 static final class Context {
  net.minecraft.world.World world;
  Context member=this;
  int[] ints;long[] longs;float[] floats;double[] doubles;String name,fail;
  int nullMode;
  List<String> trace=new ArrayList<>();
  void mark(String s){trace.add(s);if(s.equals(fail))throw new IllegalStateException("scripted:"+s);}
  Context outer(){mark("receiver.outer");return nullMode==2?null:this;}
  Context nested(){mark("receiver.nested");return nullMode==3?null:this;}
  net.minecraft.world.World getWorld(){mark("receiver.getWorld");return world;}
  Context arg(String label){mark(label);return this;}
 }
  static void call0_0(Context c) { com.hbm.sound.WorldSoundEffects.play(c.outer().member.nested().world,c.arg("x").member.ints[0],c.arg("y").member.ints[1],c.arg("z").member.ints[2],c.arg("name").member.name,c.arg("volume").member.floats[0],c.arg("pitch").member.floats[1]); }
 static void call0_1(Context c) { com.hbm.sound.WorldSoundEffects.play(c.outer().member.nested().getWorld(),c.arg("x").member.ints[0],c.arg("y").member.ints[1],c.arg("z").member.ints[2],c.arg("name").member.name,c.arg("volume").member.floats[0],c.arg("pitch").member.floats[1]); }
 static void call1_0(Context c) { com.hbm.sound.WorldSoundEffects.play(c.outer().member.nested().world,c.arg("x").member.doubles[0],c.arg("y").member.ints[1],c.arg("z").member.doubles[2],c.arg("name").member.name,c.arg("volume").member.floats[0],c.arg("pitch").member.floats[1]); }
 static void call1_1(Context c) { com.hbm.sound.WorldSoundEffects.play(c.outer().member.nested().getWorld(),c.arg("x").member.doubles[0],c.arg("y").member.ints[1],c.arg("z").member.doubles[2],c.arg("name").member.name,c.arg("volume").member.floats[0],c.arg("pitch").member.floats[1]); }
 static void call2_0(Context c) { com.hbm.sound.WorldSoundEffects.play(c.outer().member.nested().world,c.arg("x").member.longs[0],c.arg("y").member.floats[2],c.arg("z").member.ints[2],c.arg("name").member.name,c.arg("volume").member.ints[0],c.arg("pitch").member.longs[1]); }
 static void call2_1(Context c) { com.hbm.sound.WorldSoundEffects.play(c.outer().member.nested().getWorld(),c.arg("x").member.longs[0],c.arg("y").member.floats[2],c.arg("z").member.ints[2],c.arg("name").member.name,c.arg("volume").member.ints[0],c.arg("pitch").member.longs[1]); }
 static void call3_0(Context c) { com.hbm.sound.WorldSoundEffects.play(c.outer().member.nested().world,c.arg("x").member.floats[0],c.arg("y").member.floats[1],c.arg("z").member.floats[2],c.arg("name").member.name,c.arg("volume").member.floats[3],c.arg("pitch").member.floats[4]); }
 static void call3_1(Context c) { com.hbm.sound.WorldSoundEffects.play(c.outer().member.nested().getWorld(),c.arg("x").member.floats[0],c.arg("y").member.floats[1],c.arg("z").member.floats[2],c.arg("name").member.name,c.arg("volume").member.floats[3],c.arg("pitch").member.floats[4]); }
 static void call4_0(Context c) { com.hbm.sound.WorldSoundEffects.play(c.outer().member.nested().world,c.arg("x").member.longs[0],c.arg("y").member.longs[1],c.arg("z").member.longs[2],c.arg("name").member.name,c.arg("volume").member.longs[3],c.arg("pitch").member.longs[4]); }
 static void call4_1(Context c) { com.hbm.sound.WorldSoundEffects.play(c.outer().member.nested().getWorld(),c.arg("x").member.longs[0],c.arg("y").member.longs[1],c.arg("z").member.longs[2],c.arg("name").member.name,c.arg("volume").member.longs[3],c.arg("pitch").member.longs[4]); }
 static void call5_0(Context c) { com.hbm.sound.WorldSoundEffects.play(c.outer().member.nested().world,c.arg("x").member.doubles[0],c.arg("y").member.doubles[1],c.arg("z").member.doubles[2],c.arg("name").member.name,c.arg("volume").member.floats[0],c.arg("pitch").member.floats[1]); }
 static void call5_1(Context c) { com.hbm.sound.WorldSoundEffects.play(c.outer().member.nested().getWorld(),c.arg("x").member.doubles[0],c.arg("y").member.doubles[1],c.arg("z").member.doubles[2],c.arg("name").member.name,c.arg("volume").member.floats[0],c.arg("pitch").member.floats[1]); }
 public static Object[] run(int shape,int receiver,int nullMode,String fail,String name,int[] ints,long[] longs,float[] floats,double[] doubles)throws Exception {
  Context c=new Context();c.ints=ints;c.longs=longs;c.floats=floats;c.doubles=doubles;c.name=name;c.fail=fail;c.nullMode=nullMode;
  List<Object[]> callbacks=new ArrayList<>();
  net.minecraft.world.World real=(net.minecraft.world.World)U.allocateInstance(net.minecraft.client.multiplayer.WorldClient.class);
  net.minecraft.world.IWorldEventListener observer=(net.minecraft.world.IWorldEventListener)Proxy.newProxyInstance(NativeNumericFixture.class.getClassLoader(),new Class[]{net.minecraft.world.IWorldEventListener.class},(p,m,a)->{
   if(m.getName().equals("playSoundToAllNearExcept")){
    net.minecraft.util.SoundEvent sound=(net.minecraft.util.SoundEvent)a[1];
    if(a[0]!=null||sound!=com.hbm.main.ModSoundEvents.worldEffect(new net.minecraft.util.ResourceLocation(name))||a[2]!=com.hbm.main.ModSoundEvents.worldEffectCategory(new net.minecraft.util.ResourceLocation(name)))throw new AssertionError("native callback metadata");
    callbacks.add(new Object[]{sound.getSoundName().toString(),a[3],a[4],a[5],a[6],a[7]});return null;
   }
   throw new AssertionError("unexpected native callback:"+m);
  });
  Field f=net.minecraft.world.World.class.getDeclaredField("eventListeners");f.setAccessible(true);f.set(real,new ArrayList<>(Arrays.asList(observer)));
  c.world=nullMode==1?null:real;
  Throwable failure=null;
  try {switch(shape*2+receiver){case 0:call0_0(c);break;case 1:call0_1(c);break;case 2:call1_0(c);break;case 3:call1_1(c);break;case 4:call2_0(c);break;case 5:call2_1(c);break;case 6:call3_0(c);break;case 7:call3_1(c);break;case 8:call4_0(c);break;case 9:call4_1(c);break;case 10:call5_0(c);break;case 11:call5_1(c);break;default:throw new AssertionError("bad shape");}}catch(Throwable e){failure=e;}
  String exception=failure==null?null:failure.getClass().getName()+(failure instanceof IllegalStateException?":"+failure.getMessage():"");
  return new Object[]{c.trace,callbacks,exception};
 }
 public static String origin(){return net.minecraft.world.World.class.getProtectionDomain().getCodeSource().getLocation().toString();}
}
