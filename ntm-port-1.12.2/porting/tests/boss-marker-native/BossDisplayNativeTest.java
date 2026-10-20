import com.hbm.entity.boss.INtmBossDisplay;
import com.hbm.render.util.ClientBossDisplay;
import net.minecraft.util.text.*;
import java.net.*;
import java.lang.reflect.*;
public class BossDisplayNativeTest {
 static int n, fail; static float health,max; static String title;
 static void check(boolean b,String s){n++;if(!b)throw new AssertionError(s);}
 static Object value(String method)throws Exception {
  if(method.equals("aS")){if(fail==1)throw new IllegalStateException();return health;}
  if(method.equals("aY")){if(fail==2)throw new IllegalStateException();return max;}
  if(method.equals("c_")){if(fail==3)throw new IllegalStateException();return null;}
  throw new AssertionError(method);
 }
 static final INtmBossDisplay boss=new INtmBossDisplay(){
  public float getHealth(){if(fail==1)throw new IllegalStateException();return health;}
  public float getMaxHealth(){if(fail==2)throw new IllegalStateException();return max;}
  public ITextComponent getDisplayName(){if(fail==3)throw new IllegalStateException();return title==null?null:new TextComponentString(title);}
 };
 static void compare(Class<?> old)throws Exception {
  ClientBossDisplay.Snapshot s=ClientBossDisplay.current();
  check(Float.floatToIntBits(s.healthRatio)==Float.floatToIntBits(old.getField("a").getFloat(null)),"ratio");
  check(s.framesRemaining==old.getField("b").getInt(null),"frames");
  check(java.util.Objects.equals(s.name,old.getField("c").get(null)),"formatted name");
  check(s.darkeningPulse==old.getField("d").getBoolean(null),"darkening");
 }
 public static void main(String[] args)throws Exception {
  try(URLClassLoader cl=new URLClassLoader(new URL[]{new java.io.File(args[0]).toURI().toURL()},BossDisplayNativeTest.class.getClassLoader())){
   Class<?> iface=cl.loadClass("ww"),old=cl.loadClass("bln");
   Constructor<?> text=cl.loadClass("fq").getConstructor(String.class);
   Object proxy=java.lang.reflect.Proxy.newProxyInstance(cl,new Class<?>[]{iface},(p,m,a)->{
    Object v=value(m.getName());return m.getName().equals("c_")&&title!=null?text.newInstance(title):v;
   });
   Method capture=old.getMethod("a",iface,boolean.class);
   float[] values={0,1,-1,20,Float.MAX_VALUE,Float.MIN_VALUE,Float.NaN,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY};
   for(float h:values)for(float x:values)for(int failure=0;failure<5;failure++){
    health=h;max=x;fail=failure;title=failure==4?null:"Boss \u00a7cname";
    boolean darken=(n&4)==0, a=false,b=false;
    try{capture.invoke(null,proxy,darken);}catch(InvocationTargetException e){a=true;}
    try{ClientBossDisplay.capture(boss,darken);}catch(RuntimeException e){b=true;}
    check(a==b,"same getter failure");compare(old);
   }
   fail=0;health=5;max=10;title="last rendered";ClientBossDisplay.capture(boss,true);
   ClientBossDisplay.Snapshot saved=ClientBossDisplay.current();
   for(int i=0;i<100;i++)check(ClientBossDisplay.beginHudDraw(),"100 eligible draws");
   check(!ClientBossDisplay.beginHudDraw(),"expires only after draws");
   check(saved.framesRemaining==100,"immutable snapshot");
   check(ClientBossDisplay.consumeDarkeningPulse(),"independent atmosphere pulse");
   check(!ClientBossDisplay.consumeDarkeningPulse(),"pulse consumed once");
   ClientBossDisplay.clear();check(!ClientBossDisplay.beginHudDraw(),"clear not drawable");
   check(ClientBossDisplay.current().name==null&&!ClientBossDisplay.current().darkeningPulse,"clear state");
   System.out.println("PASS "+n+" original BossStatus bytecode/native capture and isolated consumer-state assertions. No active HUD, atmosphere or disconnect event integration.");
  }
 }
}
