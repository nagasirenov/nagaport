package oracle;
public class LegacyItemIds {
 static int checks;static void check(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
 public static void main(String[] args)throws Exception{
  Class<?> bootstrap=Class.forName("kl"),item=Class.forName("adb");java.io.File expected=new java.io.File(args[0]).getCanonicalFile();
  for(Class<?> c:new Class<?>[]{bootstrap,item})check(new java.io.File(c.getProtectionDomain().getCodeSource().getLocation().toURI()).getCanonicalFile().equals(expected),"actual pinned legacy class source");
  bootstrap.getMethod("b").invoke(null);java.lang.reflect.Method lookup=item.getMethod("d",int.class),id=item.getMethod("b",item);
  check(lookup.invoke(null,1)!=null,"actual vanilla registry bootstrapped");check(((Integer)id.invoke(null,new Object[]{null})).intValue()==0,"legacy null Item ID0");
  for(int value:new int[]{0,-1,Integer.MIN_VALUE,65535,Integer.MAX_VALUE})check(lookup.invoke(null,value)==null,"legacy zero/unknown IDs resolve null");
  System.out.println("PASS "+checks+" genuine legacy Item registry assertions; zero is null, not native ItemAir");
 }
}
