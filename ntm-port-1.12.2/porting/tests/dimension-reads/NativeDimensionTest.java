import java.lang.reflect.*;
import java.net.*;
import java.io.*;
import sun.misc.Unsafe;
import net.minecraft.world.*;
public class NativeDimensionTest {
 static int checks;
 static void check(boolean b){checks++;if(!b)throw new AssertionError("check "+checks);}
 static String trace="";
 static WorldProvider receive(WorldProvider p){trace+="R";return p;}
 static int rhs(){trace+="A";return 123;}
 static int compare(WorldProvider p){return receive(p).getDimension()+rhs();}
 static class Override extends WorldProviderSurface { public int getDimension(){return 900;} }
 public static void main(String[] args)throws Exception {
  Field uf=Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);Unsafe unsafe=(Unsafe)uf.get(null);
  URLClassLoader old=new URLClassLoader(new URL[]{new File(args[0]).toURI().toURL()},NativeDimensionTest.class.getClassLoader());
  Class<?> base=old.loadClass("aqo");Field id=base.getField("i");
  Field nativeId=WorldProvider.class.getDeclaredField("dimensionId");nativeId.setAccessible(true);
  check(Modifier.isPublic(id.getModifiers()));check(Modifier.isPrivate(nativeId.getModifiers()));
  Method getter=WorldProvider.class.getMethod("getDimension");check(!Modifier.isFinal(getter.getModifiers()));
  String[] types={"aqq","aqp","aqr"};WorldProvider[] providers={new WorldProviderSurface(),new WorldProviderHell(),new WorldProviderEnd()};
  int[] values={0,-1,1,-27,42,Integer.MIN_VALUE,Integer.MAX_VALUE,0};
  for(int k=0;k<types.length;k++) {
   Object legacy=old.loadClass(types[k]).newInstance();WorldProvider p=providers[k];check(id.getInt(legacy)==0);check(p.getDimension()==0);
   for(int v:values){id.setInt(legacy,v);p.setDimension(v);check(id.getInt(legacy)==p.getDimension());check(nativeId.getInt(p)==v);WorldProvider alias=p;check(alias.getDimension()==v);}
  }
  WorldProvider p=providers[0];p.setDimension(-9);trace="";check(compare(p)==114);check(trace.equals("RA"));
  trace="";try{compare(null);throw new AssertionError();}catch(NullPointerException expected){check(trace.equals("R"));}
  Override o=new Override();o.setDimension(-9);check(o.getDimension()!=nativeId.getInt(o)); // Demonstrates why closure proof is essential.
  World world=(World)unsafe.allocateInstance(WorldServer.class);Field provider=World.class.getDeclaredField("provider");provider.setAccessible(true);provider.set(world,p);
  Class<?> oldWorld=old.loadClass("ahb");Object ow=unsafe.allocateInstance(old.loadClass("mt"));Object op=old.loadClass("aqq").newInstance();Field oldProvider=oldWorld.getField("t");oldProvider.setAccessible(true);oldProvider.set(ow,op);
  Class<?> lp=Class.forName("LegacyProduction");Method oldCompare=lp.getDeclaredMethod("compare",base);oldCompare.setAccessible(true);Field oldTrace=lp.getDeclaredField("trace");oldTrace.setAccessible(true);
  id.setInt(op,-9);check((int)oldCompare.invoke(null,op)==114);check(oldTrace.get(null).equals("RA"));oldTrace.set(null,"");
  try{oldCompare.invoke(null,new Object[]{null});throw new AssertionError();}catch(InvocationTargetException expected){check(expected.getCause() instanceof NullPointerException);check(oldTrace.get(null).equals("R"));}
  Method oldPredicate=Class.forName("LegacyProduction").getDeclaredMethod("inOrbit",oldWorld);oldPredicate.setAccessible(true);
  Field oldOrbit=Class.forName("LegacyProduction$SpaceConfig").getDeclaredField("orbitDimension");oldOrbit.setAccessible(true);
  for(int v:values)for(int orbit:values){p.setDimension(v);id.setInt(op,v);GeneratedProduction.SpaceConfig.orbitDimension=orbit;oldOrbit.setInt(null,orbit);check(GeneratedProduction.inOrbit(world)==(boolean)oldPredicate.invoke(null,ow));check(GeneratedProduction.inOrbit(world)==(v==orbit));}
  oldProvider.set(ow,null);try{oldPredicate.invoke(null,ow);throw new AssertionError();}catch(InvocationTargetException expected){check(expected.getCause() instanceof NullPointerException);}
  try{oldPredicate.invoke(null,new Object[]{null});throw new AssertionError();}catch(InvocationTargetException expected){check(expected.getCause() instanceof NullPointerException);}
  provider.set(world,null);try{GeneratedProduction.inOrbit(world);throw new AssertionError();}catch(NullPointerException expected){checks++;}
  try{GeneratedProduction.inOrbit(null);throw new AssertionError();}catch(NullPointerException expected){checks++;}
  System.out.println("PASS "+checks+" actual original/native provider constructor, signed int, setter/getter, alias, evaluation/null, override counterexample and complete extracted inOrbit checks");
 }
}
