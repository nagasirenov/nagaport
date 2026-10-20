import java.lang.reflect.*;import java.util.*;
/** Invokes authentic 1.7.10 World, RenderGlobal, WorldManager and PlayerManager bodies. */
public class OriginalNotificationTest {
 static final sun.misc.Unsafe U;static int checks;static StringBuilder trace=new StringBuilder();static ahb selected;
 static{try{Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);U=(sun.misc.Unsafe)f.get(null);}catch(Exception e){throw new RuntimeException(e);}}
 static Field field(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
 static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 public static class Renderer extends bma {int calls;int[] last;Renderer(){super(null);}public void b(int a,int b,int c,int d,int e,int f){calls++;last=new int[]{a,b,c,d,e,f};}}
 static ahb recv(){trace.append('r');return selected;}static int arg(int n){trace.append(n);return n==2?64:n;}
 public static void main(String[] args)throws Exception{
  kl.b();ahb world=(ahb)U.allocateInstance(bjf.class);List<ahh> listeners=new ArrayList<>();field(ahb.class,"u").set(world,listeners);
  // A null chunk provider establishes that the old dispatch itself reads no block state.
  Renderer renderer=(Renderer)U.allocateInstance(Renderer.class);listeners.add(renderer);final int[] coord=new int[3],calls={0};
  ahh listener=(ahh)Proxy.newProxyInstance(OriginalNotificationTest.class.getClassLoader(),new Class[]{ahh.class},(proxy,m,a)->{check(m.getName().equals("a")&&a.length==3,"old exact listener hook");calls[0]++;for(int i=0;i<3;i++)check((Integer)a[i]==coord[i],"old listener coordinate "+i);return null;});listeners.add(listener);
  for(int x:new int[]{Integer.MIN_VALUE,-30000001,-30000000,-17,-1,0,16,29999999,30000000,Integer.MAX_VALUE})for(int y:new int[]{-1,0,64,255,256})for(int z:new int[]{-19,0,16}){
   coord[0]=x;coord[1]=y;coord[2]=z;int before=calls[0];world.g(x,y,z);check(calls[0]==before+1,"old dispatch once even outside block domain");check(Arrays.equals(renderer.last,new int[]{x-1,y-1,z-1,x+1,y+1,z+1}),"old renderer one block margin with int overflow");
  }
  mt server=(mt)U.allocateInstance(mt.class);mq map=(mq)U.allocateInstance(mq.class);field(mt.class,"L").set(server,map);field(mq.class,"b").set(map,server);qd entries=new qd();field(mq.class,"d").set(map,entries);List<Object> dirty=new ArrayList<>();field(mq.class,"e").set(map,dirty);
  Class<?> entryClass=Class.forName("mr");Object entry=U.allocateInstance(entryClass);field(entryClass,"a").set(entry,map);field(entryClass,"d").set(entry,new short[64]);int x=-17,y=80,z=19,cx=x>>4,cz=z>>4;long key=((long)cx+2147483647L)|(((long)cz+2147483647L)<<32);entries.a(key,entry);field(ahb.class,"u").set(server,new ArrayList<ahh>(Arrays.asList(new mp(null,server))));server.g(x,y,z);server.g(x,y,z);
  check(dirty.size()==1&&dirty.get(0)==entry,"old actual queue entry once");check(field(entryClass,"e").getInt(entry)==1,"old duplicate coordinate coalesced");check(field(entryClass,"f").getInt(entry)==1<<5,"old section bit");short packed=((short[])field(entryClass,"d").get(entry))[0];check(packed==(short)(((x&15)<<12)|((z&15)<<8)|y),"old packed coordinate");entries.d(key);server.g(x,y,z);check(entries.a()==0&&dirty.size()==1,"old unwatched no entry generation");
  selected=null;trace.setLength(0);try{recv().g(arg(1),arg(2),arg(3));throw new AssertionError();}catch(NullPointerException expected){check(trace.toString().equals("r123"),"old null receiver after coordinates");}trace.setLength(0);try{recv().g(arg(1),(new int[0])[arg(2)],arg(3));throw new AssertionError();}catch(ArrayIndexOutOfBoundsException expected){check(trace.toString().equals("r12"),"old argument failure before null");}
  System.out.println("PASS: "+checks+" authentic original dispatch/render/server-queue/evaluation assertions; Forge patches audited separately");
 }
}
