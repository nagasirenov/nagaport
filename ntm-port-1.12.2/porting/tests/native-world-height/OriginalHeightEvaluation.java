public class OriginalHeightEvaluation {
 static StringBuilder trace=new StringBuilder();static ahb selected;static int checks;
 static ahb receiver(){trace.append('r');return selected;}static int x(){trace.append('x');return Integer.MAX_VALUE;}static int z(){trace.append('z');return 0;}
 static void check(String value){if(!trace.toString().equals(value))throw new AssertionError(trace);checks++;}
 public static void main(String[] a)throws Exception{
  kl.b();java.lang.reflect.Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);sun.misc.Unsafe u=(sun.misc.Unsafe)f.get(null);selected=(ahb)u.allocateInstance(bjf.class);
  trace.setLength(0);if(receiver().f(x(),z())!=64)throw new AssertionError();check("rxz");
  selected=null;trace.setLength(0);try{receiver().f(x(),z());throw new AssertionError();}catch(NullPointerException e){check("rxz");}
  trace.setLength(0);try{receiver().f(x(),(new int[0])[z()]);throw new AssertionError();}catch(ArrayIndexOutOfBoundsException e){check("rxz");}
  System.out.println("PASS: "+checks+" actual original height invocation receiver/argument/null evaluation assertions");
 }
}
