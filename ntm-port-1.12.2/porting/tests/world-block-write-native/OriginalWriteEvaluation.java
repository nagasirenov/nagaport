/** Direct calls compile against unchanged original Minecraft classes. */
public class OriginalWriteEvaluation {
 static StringBuilder trace=new StringBuilder();static ahb selected;static int checks;
 static ahb receiver(){trace.append('r');return selected;}
 static int arg(int i){trace.append(i);return i==1?Integer.MAX_VALUE:i==2?64:0;}
 static aji block(){trace.append('b');return ajn.a;}static int metadata(){trace.append('m');return 0;}static int flags(){trace.append('f');return 3;}
 static void check(String expected){if(!trace.toString().equals(expected))throw new AssertionError(trace);checks++;}
 public static void main(String[] a)throws Exception{
  kl.b();java.lang.reflect.Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);sun.misc.Unsafe u=(sun.misc.Unsafe)f.get(null);selected=(ahb)u.allocateInstance(bjf.class);
  trace.setLength(0);if(receiver().d(arg(1),arg(2),arg(3),block(),metadata(),flags()))throw new AssertionError();check("r123bmf");
  trace.setLength(0);if(receiver().b(arg(1),arg(2),arg(3),block()))throw new AssertionError();check("r123b");
  selected=null;trace.setLength(0);try{receiver().d(arg(1),arg(2),arg(3),block(),metadata(),flags());throw new AssertionError();}catch(NullPointerException expected){check("r123bmf");}
  trace.setLength(0);try{receiver().b(arg(1),arg(2),arg(3),block());throw new AssertionError();}catch(NullPointerException expected){check("r123b");}
  trace.setLength(0);try{receiver().d(arg(1),(new int[0])[arg(2)],arg(3),block(),metadata(),flags());throw new AssertionError();}catch(ArrayIndexOutOfBoundsException expected){check("r12");}
  trace.setLength(0);try{receiver().b(arg(1),(new int[0])[arg(2)],arg(3),block());throw new AssertionError();}catch(ArrayIndexOutOfBoundsException expected){check("r12");}
  System.out.println("PASS: "+checks+" direct original World write null/argument/evaluation assertions");
 }
}
