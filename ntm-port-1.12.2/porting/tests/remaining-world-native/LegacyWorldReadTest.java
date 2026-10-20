import java.lang.reflect.*;
import sun.misc.Unsafe;
public class LegacyWorldReadTest {
 static Unsafe unsafe;static int checks;static String trace="";
 static void ok(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 interface Eval{Object get();}static void npe(Eval e){try{e.get();throw new AssertionError("expected null failure");}catch(NullPointerException expected){checks++;}}
 static apx receiver(apx c){trace+="R";return c;}static int arg(){trace+="A";return 1;}static Object sink(ahb w,int n){trace+="S";return w;}
 static ahb plain(apx chunk){return chunk.e;}
 static class P{S parent;}static class S{R parent;}static class R{apx chunk;}static ahb chain(P p){return p.parent.parent.chunk.e;}
 static class Tile extends aor {ahb read(ahb world){return this.b;}void bind(ahb w){this.b=w;}class Inner {ahb world;ahb read(ahb world){return Tile.this.b;}}}
 static class Map extends aqx {ahb read(ahb world){return this.c;}void bind(ahb w){this.c=w;}}
 public static void main(String[] args)throws Exception{
  Field uf=Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);unsafe=(Unsafe)uf.get(null);ahb a=(ahb)unsafe.allocateInstance(mt.class),b=(ahb)unsafe.allocateInstance(mt.class);
  Field old=apx.class.getDeclaredField("e");ok(old.getType()==ahb.class&&Modifier.isPublic(old.getModifiers())&&!Modifier.isFinal(old.getModifiers()),"original Chunk public mutable World field");
  apx ca=(apx)unsafe.allocateInstance(apx.class),cb=(apx)unsafe.allocateInstance(apx.class);ca.e=a;cb.e=b;ok(plain(ca)==a&&plain(cb)==b,"original exact stored world identity");ca.e=null;ok(plain(ca)==null,"original null world");ca.e=a;npe(()->plain(null));trace="";ok(receiver(ca).e==a&&trace.equals("R"),"original receiver once");trace="";ok(sink(receiver(ca).e,arg())==a&&trace.equals("RAS"),"original access before later argument");trace="";npe(()->sink(receiver(null).e,arg()));ok(trace.equals("R"),"original null timing");
  P p=new P();p.parent=new S();p.parent.parent=new R();p.parent.parent.chunk=ca;ok(chain(p)==a,"original nested receiver");p.parent.parent.chunk=cb;ok(chain(p)==b,"original changed chunk pointer");p.parent.parent.chunk=null;npe(()->chain(p));p.parent.parent=null;npe(()->chain(p));p.parent=null;npe(()->chain(p));npe(()->chain(null));
  Tile t=new Tile();t.bind(a);ok(t.read(b)==a,"original direct tile field");Tile.Inner inner=t.new Inner();inner.world=b;ok(inner.read(b)==a,"original outer tile field");t.bind(null);ok(inner.read(b)==null,"original null outer tile field");Map m=new Map();m.bind(a);ok(m.read(b)==a,"original MapGen field");m.bind(null);ok(m.read(a)==null,"original MapGen null");
  sa entity=(sa)unsafe.allocateInstance(xk.class);entity.o=a;ok(entity.o==a,"original Entity World field");entity.o=null;ok(entity.o==null,"original Entity null World field");
  System.out.println("PASS "+checks+" actual original field and receiver-evaluation controls; original Chunk allocation bypasses constructor, no chunk lifecycle claim");
 }
}
