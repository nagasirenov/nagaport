import java.lang.reflect.*;
import java.util.*;
/** Real original client World/Chunk bytecode; this fixture block only records its drop hook. */
public class OriginalDestroyBlockTest extends aji {
 static final sun.misc.Unsafe U;static int checks;static StringBuilder trace=new StringBuilder();static boolean sabotage;static int wantedMeta;static OriginalDestroyBlockTest recording;
 static {try{Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);U=(sun.misc.Unsafe)f.get(null);}catch(Exception e){throw new RuntimeException(e);}}
 OriginalDestroyBlockTest(){super(aji.e(1).o());}
 public void a(ahb w,int x,int y,int z,int meta,float chance,int fortune){trace.append('d');check(meta==wantedMeta&&chance==1F&&fortune==0,"old metadata chance fortune");check(w.a(x,y,z)==(sabotage?ajn.a:this),"old event precedes drop before removal");}
 static Field field(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
 static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static boolean valid(int x,int y,int z){return x>=-30000000&&x<30000000&&z>=-30000000&&z<30000000&&y>=0&&y<256;}
 public static void main(String[] args)throws Exception{
  kl.b();ahb w=(ahb)U.allocateInstance(bjf.class);field(ahb.class,"t").set(w,aqo.a(0));field(ahb.class,"E").setBoolean(w,true);field(ahb.class,"C").set(w,new qi());field(ahb.class,"u").set(w,new ArrayList<>());
  final apx[] chunk={null};final int[] calls={0};apu provider=(apu)java.lang.reflect.Proxy.newProxyInstance(OriginalDestroyBlockTest.class.getClassLoader(),new Class[]{apu.class},(p,m,a)->{if(m.getReturnType()==apx.class){calls[0]++;return chunk[0];}if(m.getReturnType()==boolean.class)return false;throw new AssertionError(m.toString());});field(ahb.class,"v").set(w,provider);
  int[] h={Integer.MIN_VALUE,-30000001,-30000000,-17,-1,0,16,29999999,30000000,Integer.MAX_VALUE};
  for(boolean drop:new boolean[]{false,true})for(int x:h)for(int z:h)for(int y:new int[]{-1,0,64,255,256}){
   chunk[0]=new apx(w,x>>4,z>>4);if(y>=0&&y<256){apz st=new apz(y&~15,true);st.a(x&15,y&15,z&15,aji.e(1));((apz[])field(apx.class,"u").get(chunk[0]))[y>>4]=st;}calls[0]=0;
   check(w.a(x,y,z,drop)==valid(x,y,z),"old actual return");check((calls[0]>0)==valid(x,y,z),"old prechunk domain");if(valid(x,y,z)){check(chunk[0].a(x&15,y,z&15)==ajn.a,"old actual removed");check(!w.a(x,y,z,drop),"old repeated air false");}else if(y>=0&&y<256)check(chunk[0].a(x&15,y,z&15)==aji.e(1),"old invalid unchanged");
  }
  recording=new OriginalDestroyBlockTest();aji.c.a(3500,"proof:destruction",recording);final int x=-17,y=64,z=19;
  ahh listener=(ahh)java.lang.reflect.Proxy.newProxyInstance(OriginalDestroyBlockTest.class.getClassLoader(),new Class[]{ahh.class},(proxy,m,a)->{if(m.getParameterTypes().length==6&&m.getParameterTypes()[0]==yz.class){trace.append('e');check((Integer)a[1]==2001&&(Integer)a[2]==x&&(Integer)a[3]==y&&(Integer)a[4]==z,"old event coordinates");check((Integer)a[5]==3500+(wantedMeta<<12),"old event metadata");check(w.a(x,y,z)==recording,"old event before remove");if(sabotage)check(w.f(x,y,z),"old event callback preempts removal");}return null;});((List)field(ahb.class,"u").get(w)).add(listener);
  for(boolean preempt:new boolean[]{false,true})for(boolean drop:new boolean[]{false,true})for(int meta=0;meta<16;meta++){
   sabotage=preempt;wantedMeta=meta;chunk[0]=new apx(w,x>>4,z>>4);apz st=new apz(y&~15,true);st.a(x&15,y&15,z&15,recording);st.a(x&15,y&15,z&15,meta);((apz[])field(apx.class,"u").get(chunk[0]))[y>>4]=st;trace.setLength(0);
   check(w.a(x,y,z,drop)==!preempt,"old actual final mutation return");check(trace.toString().equals(drop?"ed":"e"),"old event/drop order "+trace);check(w.a(x,y,z)==ajn.a,"old final removal");
  }
  System.out.println("PASS: "+checks+" real original World/Chunk domain, event, drop, metadata, final-removal return assertions (vanilla original engine; authentic Forge patches audited separately)");
 }
}
