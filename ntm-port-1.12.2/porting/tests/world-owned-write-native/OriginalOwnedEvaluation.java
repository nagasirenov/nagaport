/** Compiled direct calls against the unchanged supplied 1.7 client classes. */
public class OriginalOwnedEvaluation {
 static StringBuilder trace=new StringBuilder();static ahb selected;static aji selectedBlock;static int checks;static int failure;
 static ahb receiver(){trace.append('r');return selected;}
 static int arg(int i){trace.append(i);return i==1?Integer.MAX_VALUE:i==2?64:0;}
 static aji block(){trace.append('b');return selectedBlock;}static int metadata(){trace.append('m');if(failure==1)throw new IllegalStateException();return 6;}static int flags(){trace.append('f');if(failure==2)throw new IllegalStateException();return 2;}
 static void check(String expected){if(!trace.toString().equals(expected))throw new AssertionError(trace);checks++;}
 static void call(){receiver().d(arg(1),arg(2),arg(3),block(),metadata(),flags());}
 public static void main(String[] a)throws Exception{
  kl.b();java.lang.reflect.Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);sun.misc.Unsafe u=(sun.misc.Unsafe)f.get(null);selected=(ahb)u.allocateInstance(bjf.class);
  selectedBlock=null;trace.setLength(0);call();check("r123bmf");
  selectedBlock=ajn.b;trace.setLength(0);call();check("r123bmf");
  selected=null;trace.setLength(0);try{call();throw new AssertionError();}catch(NullPointerException expected){check("r123bmf");}
  for(int fail:new int[]{1,2}){failure=fail;trace.setLength(0);try{call();throw new AssertionError();}catch(IllegalStateException expected){check(fail==1?"r123bm":"r123bmf");}}failure=0;
  trace.setLength(0);try{receiver().d(arg(1),(new int[0])[arg(2)],arg(3),block(),metadata(),flags());throw new AssertionError();}catch(ArrayIndexOutOfBoundsException expected){check("r12");}
  System.out.println("PASS: "+checks+" original owned-write null-field/null-world/argument-failure/evaluation cases");
 }
}
