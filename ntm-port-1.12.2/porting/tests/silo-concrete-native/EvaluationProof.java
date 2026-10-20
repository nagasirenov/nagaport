import java.lang.reflect.*;
import java.util.*;
import net.minecraft.world.World;
import com.hbm.lib.Library;
import com.hbm.world.generator.DungeonToolbox;
/** Effectful receiver/coordinates and selector run against real production domain helper. */
public class EvaluationProof extends NativeProof {
 static StringBuilder order; static int fail; static boolean nil; static int[] pos;
 static RecordingWorld world,other; static World holder; static boolean mutate;
 static class TraceRandom extends Random {
  TraceRandom(){super(918273);}
  public int nextInt(int bound){step(4);return super.nextInt(bound);}
 }
 static void step(int n){order.append(n);if(fail==n)throw new IllegalStateException("fail"+n);}
 static World receiver(){step(0);return holder;}
 static int coord(int n){step(n);if(mutate&&n==1)holder=other;return pos[n-1];}
 static void original(){legacy(receiver(),coord(1),coord(2),coord(3),Library.getRandomConcrete(),0,3);}
 static void actual(){DungeonToolbox.placeRandomConcrete(receiver(),coord(1),coord(2),coord(3));}
 static String execute(Method mutant,boolean old)throws Exception {
  world=new RecordingWorld();other=new RecordingWorld();holder=nil?null:world;order=new StringBuilder();
  Field rng=Library.class.getDeclaredField("rand");rng.setAccessible(true);TraceRandom r=new TraceRandom();rng.set(null,r);
  String exception="";
  try {if(old)original();else if(mutant==null)actual();else mutant.invoke(null,receiver(),coord(1),coord(2),coord(3));}
  catch(InvocationTargetException ex){exception=ex.getCause().getClass().getName();}
  catch(RuntimeException ex){exception=ex.getClass().getName();}
  return exception+"/"+order+"/"+r.nextLong()+"/"+world.trace+"/"+other.trace;
 }
 static void suite(Method target)throws Exception {
  int[][] cases={{0,64,0},{-30000000,0,-30000000},{29999999,255,29999999},{30000000,64,0},{-30000001,64,0},{0,64,30000000},{0,64,-30000001},{0,-1,0},{0,256,0},{Integer.MAX_VALUE,64,Integer.MIN_VALUE}};
  for(int[] p:cases)for(int n=0;n<2;n++)for(int m=0;m<2;m++)for(int f=-1;f<=4;f++){
   pos=p;nil=n==1;mutate=m==1;fail=f;
   String expected=execute(null,true),actual=execute(target,false);
   check(expected.equals(actual),"single evaluation, receiver capture, null/bounds/exception/selector ordering: "+Arrays.toString(p)+" "+nil+" "+m+" "+f+" expected "+expected+" actual "+actual);
  }
 }
 static void run()throws Exception {
  suite(null);
  for(int n=0;n<6;n++){
   Method target=Class.forName("Mutant"+n).getMethod("placeRandomConcrete",World.class,int.class,int.class,int.class);
   boolean rejected=false;try{suite(target);}catch(AssertionError expected){rejected=true;}
   check(rejected,"mutant rejected "+n);
  }
 }
}
