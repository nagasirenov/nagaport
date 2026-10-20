/** Compiled against and invokes actual original Minecraft 1.7.10 World methods. */
public class OriginalAirEvaluation {
 static StringBuilder trace=new StringBuilder();static ahb selected;static int checks;
 static ahb receiver(){trace.append('r');return selected;}
 static int arg(int i){trace.append(i);return i==1?Integer.MAX_VALUE:i==2?64:0;}
 static void check(String expected){if(!trace.toString().equals(expected))throw new AssertionError(trace);checks++;}
 public static void main(String[] a)throws Exception{
  kl.b();java.lang.reflect.Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);sun.misc.Unsafe u=(sun.misc.Unsafe)f.get(null);selected=(ahb)u.allocateInstance(bjf.class);
  trace.setLength(0);if(receiver().f(arg(1),arg(2),arg(3))!=false)throw new AssertionError();check("r123");
  trace.setLength(0);if(receiver().f(arg(1),arg(2),arg(3))!=false)throw new AssertionError();check("r123");
  selected=null;trace.setLength(0);try{receiver().f(arg(1),arg(2),arg(3));throw new AssertionError();}catch(NullPointerException expected){check("r123");}
  trace.setLength(0);try{receiver().f(arg(1),arg(2),arg(3));throw new AssertionError();}catch(NullPointerException expected){check("r123");}
  trace.setLength(0);try{receiver().f(arg(1),(new int[0])[arg(2)],arg(3));throw new AssertionError();}catch(ArrayIndexOutOfBoundsException expected){check("r12");}
  trace.setLength(0);try{receiver().f(arg(1),(new int[0])[arg(2)],arg(3));throw new AssertionError();}catch(ArrayIndexOutOfBoundsException expected){check("r12");}
  System.out.println("PASS: "+checks+" direct original World invocation null/argument exception/evaluation assertions");
 }
}
