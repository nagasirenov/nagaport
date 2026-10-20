import java.lang.reflect.*;
import java.net.*;
import java.io.*;
import java.util.*;
import net.minecraft.util.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import com.hbm.main.ModSoundEvents;
/** Actual original World callback vs unchanged production native helper. */
public final class NumericWideningProbe {
 static int checks,migrationCases,robustnessCases,orderingCases,normalizedNames;
 static Method oldRun,newRun;
 static final String[] NAMES={"hbm:player.vomit","hbm:turret.jeremy_fire","hbm:turret.jeremy_reload","hbm:weapon.flamethrowerShoot","hbm:weapon.fstbmbPing","hbm:weapon.missileTakeOff"};
 static void check(boolean ok,String what){checks++;if(!ok)throw new AssertionError(what);}
 static Object[] call(Method method,int shape,int receiver,int nullMode,String failure,String name,int[] is,long[] ls,float[] fs,double[] ds)throws Exception {
  try{return (Object[])method.invoke(null,shape,receiver,nullMode,failure,name,is,ls,fs,ds);}catch(InvocationTargetException e){throw new AssertionError("fixture infrastructure",e.getCause());}
 }
 static List<String> expectedTrace(int receiver,int nullMode,String fail){
  List<String> all=new ArrayList<>(Arrays.asList("receiver.outer","receiver.nested"));
  if(receiver==1)all.add("receiver.getWorld");
  all.addAll(Arrays.asList("x","y","z","name","volume","pitch"));
  if(nullMode==2)return all.subList(0,1);
  if(nullMode==3)return all.subList(0,2);
  if(fail!=null&&all.contains(fail))return all.subList(0,all.indexOf(fail)+1);
  return all;
 }
 @SuppressWarnings("unchecked")
 static Object[] test(int shape,int receiver,int nullMode,String fail,String name,int[] is,long[] ls,float[] fs,double[] ds)throws Exception {
  Object[] a=call(oldRun,shape,receiver,nullMode,fail,name,is,ls,fs,ds),b=call(newRun,shape,receiver,nullMode,fail,name,is,ls,fs,ds);
  check(a[0].equals(b[0]),"identical receiver+argument order");check(a[0].equals(expectedTrace(receiver,nullMode,fail)),"independent expected evaluation trace: "+a[0]);
  check(Objects.equals(a[2],b[2]),"same exception class/scripted winner: "+a[2]+" vs "+b[2]);
  boolean throwsScript=fail!=null&&((List<String>)a[0]).contains(fail);
  String expected=throwsScript?"java.lang.IllegalStateException:scripted:"+fail:nullMode!=0?"java.lang.NullPointerException":null;
  check(Objects.equals(a[2],expected),"expected exception winner: "+a[2]+" vs "+expected);
  List<Object[]> ac=(List<Object[]>)a[1],bc=(List<Object[]>)b[1];
  check(ac.size()==bc.size()&&ac.size()==(expected==null?1:0),"single/no callback");
  if(expected==null){
   Object[] x=ac.get(0),y=bc.get(0);check(x[0].equals(name),"original raw name retained");
   check(y[0].equals(new ResourceLocation(name).toString()),"native ResourceLocation normalization, not raw String equivalence");
   if(!x[0].equals(y[0]))normalizedNames++;
   for(int i=1;i<=3;i++)check(Double.doubleToRawLongBits((Double)x[i])==Double.doubleToRawLongBits((Double)y[i]),"raw double callback "+i+" shape "+shape);
   for(int i=4;i<=5;i++)check(Float.floatToRawIntBits((Float)x[i])==Float.floatToRawIntBits((Float)y[i]),"raw float callback "+i+" shape "+shape);
  }
  if(nullMode!=0||fail!=null)orderingCases++;else if(shape<2)migrationCases++;else robustnessCases++;
  return a;
 }
 public static void main(String[] args)throws Exception {
  net.minecraft.init.Bootstrap.register();net.minecraftforge.fml.common.FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
  net.minecraftforge.fml.common.ModMetadata md=new net.minecraftforge.fml.common.ModMetadata();md.modId="hbm";md.name="Coordinate widening audit";md.version="1";
  net.minecraftforge.fml.common.DummyModContainer owner=new net.minecraftforge.fml.common.DummyModContainer(md);net.minecraftforge.fml.common.Loader.instance().setupTestHarness(owner);net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(owner);
  ModSoundEvents.registerSounds(new net.minecraftforge.event.RegistryEvent.Register<SoundEvent>(new ResourceLocation("minecraft","soundevents"),ForgeRegistries.SOUND_EVENTS));
  Class<?>[] signature={int.class,int.class,int.class,String.class,String.class,int[].class,long[].class,float[].class,double[].class};
  try(URLClassLoader legacy=new URLClassLoader(new URL[]{new File(args[0]).toURI().toURL()},NumericWideningProbe.class.getClassLoader());URLClassLoader fixture=new URLClassLoader(new URL[]{new File(args[1]).toURI().toURL()},legacy)){
   Class<?> old=Class.forName("LegacyNumericFixture",true,fixture);oldRun=old.getMethod("run",signature);newRun=NativeNumericFixture.class.getMethod("run",signature);
   check(old.getClassLoader()==fixture,"unsigned original fixture isolated from signed jar package");check(Class.forName("ahb",true,legacy).getClassLoader()==legacy,"actual signed original World isolated loader");
   System.out.println("LEGACY WORLD SOURCE "+old.getMethod("origin").invoke(null));System.out.println("NATIVE WORLD SOURCE "+NativeNumericFixture.origin());
   int[] iv={Integer.MIN_VALUE,Integer.MIN_VALUE+1,-16777217,-16777216,-1,0,1,16777215,16777216,16777217,Integer.MAX_VALUE};
   long[] lv={Long.MIN_VALUE,Long.MIN_VALUE+1,-9007199254740993L,-16777217L,-1L,0L,1L,16777217L,9007199254740991L,9007199254740992L,9007199254740993L,Long.MAX_VALUE};
   float[] fv={Float.NEGATIVE_INFINITY,-Float.MAX_VALUE,-16777216F,-1F,-Float.MIN_NORMAL,-Float.MIN_VALUE,-0F,0F,Float.MIN_VALUE,Float.MIN_NORMAL,.5F,.799999F,1F,Math.nextUp(1F),4.047619F,Float.MAX_VALUE,Float.POSITIVE_INFINITY,Float.NaN,Float.intBitsToFloat(0x7fa12345)};
   double[] dv={Double.NEGATIVE_INFINITY,-Double.MAX_VALUE,-9007199254740993D,-16777217.125D,-1D,-Double.MIN_NORMAL,-Double.MIN_VALUE,-0D,0D,Double.MIN_VALUE,Double.MIN_NORMAL,.5D,16777217.125D,9007199254740992D,Double.MAX_VALUE,Double.POSITIVE_INFINITY,Double.NaN,Double.longBitsToDouble(0x7ff123456789abcdL)};
   for(String name:NAMES)for(int shape=0;shape<6;shape++)for(int receiver=0;receiver<2;receiver++)for(int n=0;n<57;n++){
    int[] is=new int[5];long[] ls=new long[5];float[] fs=new float[5];double[] ds=new double[5];for(int j=0;j<5;j++){is[j]=iv[(n+j*3)%iv.length];ls[j]=lv[(n+j*5)%lv.length];fs[j]=fv[(n+j*7)%fv.length];ds[j]=dv[(n+j*5)%dv.length];}
    test(shape,receiver,0,null,name,is,ls,fs,ds);
   }
   Random r=new Random(1710112262L);
   for(int n=0;n<1200;n++){
    int[] is=new int[5];long[] ls=new long[5];float[] fs=new float[5];double[] ds=new double[5];for(int j=0;j<5;j++){is[j]=r.nextInt();ls[j]=r.nextLong();fs[j]=Float.intBitsToFloat(r.nextInt());ds[j]=Double.longBitsToDouble(r.nextLong());}
    for(int shape=0;shape<6;shape++)test(shape,n%2,0,null,NAMES[n%6],is,ls,fs,ds);
   }
   int[] is={16777217,-1,Integer.MAX_VALUE,1,1};long[] ls={9007199254740993L,Long.MAX_VALUE,Long.MIN_VALUE,16777217L,1};float[] fs={1,.799999F,Float.MIN_VALUE,Float.NaN,Float.NEGATIVE_INFINITY};double[] ds={-0D,1,Double.MAX_VALUE,1,1};
   for(int shape=0;shape<6;shape++)for(int receiver=0;receiver<2;receiver++){
    for(int nullMode=1;nullMode<=3;nullMode++)test(shape,receiver,nullMode,null,NAMES[shape],is,ls,fs,ds);
    for(String fail:Arrays.asList("receiver.outer","receiver.nested","receiver.getWorld","x","y","z","name","volume","pitch"))if(receiver==1||!fail.equals("receiver.getWorld"))for(int nullMode=0;nullMode<=1;nullMode++)test(shape,receiver,nullMode,fail,NAMES[shape],is,ls,fs,ds);
   }
   Object[] exact=test(0,0,0,null,NAMES[0],is,ls,fs,ds);Object[] callback=((List<Object[]>)exact[1]).get(0);
   check(((Double)callback[1])==16777217D,"int to double does not round through float");
   Object[] rounding=test(2,1,0,null,NAMES[1],is,ls,fs,ds);callback=((List<Object[]>)rounding[1]).get(0);
   check(((Double)callback[1])==9007199254740992D,"long to double exact JVM rounding");check(((Double)callback[2])==(double)Float.MIN_VALUE,"float subnormal to double");check(((Float)callback[4])==16777216F,"robustness int to float rounding");check(((Float)callback[5])==(float)Long.MAX_VALUE,"robustness long to float rounding");
   check(normalizedNames>0,"explicit mixed-case raw-name caveat exercised");
   System.out.println("PASS NumericWideningProbe: "+checks+" assertions; "+migrationCases+" migration-shape callback comparisons; "+robustnessCases+" separately labelled robustness/existing-shape comparisons; "+orderingCases+" null/exception order comparisons; "+normalizedNames+" observed raw-name normalization differences.");
  }
 }
}
