import java.util.*;import net.minecraft.util.math.AxisAlignedBB;import net.minecraft.entity.item.EntityItem;import net.minecraft.world.World;
public class GrowthProbe {
 static long checks;static void ok(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static double[] old(azt b){return new double[]{b.a,b.b,b.c,b.d,b.e,b.f};}static double[] neo(AxisAlignedBB b){return new double[]{b.minX,b.minY,b.minZ,b.maxX,b.maxY,b.maxZ};}
 static azt old(double[] v){return azt.a(v[0],v[1],v[2],v[3],v[4],v[5]);}static AxisAlignedBB neo(double[] v){return new AxisAlignedBB(v[0],v[1],v[2],v[3],v[4],v[5]);}
 static boolean same(double[] a,double[] b){for(int i=0;i<6;i++)if(Double.doubleToRawLongBits(a[i])!=Double.doubleToRawLongBits(b[i]))return false;return true;}
 static void check(azt a,AxisAlignedBB b,int site){double[] o=old(a),n=neo(b);for(int i=0;i<6;i++)ok(Double.doubleToRawLongBits(o[i])==Double.doubleToRawLongBits(n[i]),"raw grow bits site="+site+" axis="+i);}
 static void geometry(azt a,AxisAlignedBB b,int site){check(a,b,site);double[] v=neo(b);for(double x:v)if(!Double.isFinite(x))return;
  double[][] probes={{v[0],v[1],v[2],v[3],v[4],v[5]},{v[3],v[4],v[5],v[3]+1,v[4]+1,v[5]+1},{v[0]-1,v[1]-1,v[2]-1,v[3]+1,v[4]+1,v[5]+1},{v[3]+2,v[4]+2,v[5]+2,v[3]+3,v[4]+3,v[5]+3}};
  for(double[] p:probes){azt o=old(p);AxisAlignedBB n=neo(p);ok(a.b(o)==b.intersects(n),"intersection");for(double step:new double[]{-10,-1,-0.0,0.0,1,10}){ok(Double.doubleToRawLongBits(a.a(o,step))==Double.doubleToRawLongBits(b.calculateXOffset(n,step)),"x-offset");ok(Double.doubleToRawLongBits(a.b(o,step))==Double.doubleToRawLongBits(b.calculateYOffset(n,step)),"y-offset");ok(Double.doubleToRawLongBits(a.c(o,step))==Double.doubleToRawLongBits(b.calculateZOffset(n,step)),"z-offset");}}
 }
 static String events="";static AxisAlignedBB rn(AxisAlignedBB b){events+="R";return b;}static azt ro(azt b){events+="R";return b;}static double arg(int i){events+=i;return i;}static double fail(int i){events+=i;throw new IllegalStateException();}
 static class OverrideBox extends AxisAlignedBB{OverrideBox(){super(0,0,0,1,1,1);}@Override public AxisAlignedBB expand(double a,double b,double c){return new AxisAlignedBB(99,99,99,100,100,100);}}
 static void controls(){
  azt o=azt.a(0,0,0,1,1,1);AxisAlignedBB n=new AxisAlignedBB(0,0,0,1,1,1);
  ok(!same(old(o.b(2,3,4)),neo(n.expand(2,3,4))),"directional expand is not old symmetric growth");check(o.b(2,3,4),n.grow(2,3,4),-1);
  ok(!same(old(o.b(-1,-1,-1)),neo(n.grow(-1,-1,-1))),"over-shrink crosses endpoints");
  o=azt.a(-0.0,-0.0,-0.0,-0.0,-0.0,-0.0);n=new AxisAlignedBB(-0.0,-0.0,-0.0,-0.0,-0.0,-0.0);ok(!same(old(o.b(-0.0,-0.0,-0.0)),neo(n.grow(-0.0,-0.0,-0.0))),"negative-zero growth can reorder raw zero signs");
  o=azt.a(4,4,4,-4,-4,-4);n=new AxisAlignedBB(4,4,4,-4,-4,-4);ok(!same(old(o.b(1,1,1)),neo(n.grow(1,1,1))),"raw reversed original receiver outside native invariant");
  double q1=Double.longBitsToDouble(0x7ff8000000000031L),q2=Double.longBitsToDouble(0x7ff8000000000094L);o=azt.a(q1,0,0,q2,1,1);n=new AxisAlignedBB(q1,0,0,q2,1,1);ok(!same(old(o.b(1,1,1)),neo(n.grow(1,1,1))),"different original endpoint NaN payloads not silently accepted");
  o=azt.a(Double.POSITIVE_INFINITY,0,0,Double.POSITIVE_INFINITY,1,1);n=new AxisAlignedBB(Double.POSITIVE_INFINITY,0,0,Double.POSITIVE_INFINITY,1,1);ok(!same(old(o.b(Double.POSITIVE_INFINITY,0,0)),neo(n.grow(Double.POSITIVE_INFINITY,0,0))),"infinite growth introduces one-sided NaN");
  AxisAlignedBB over=new OverrideBox();ok(!same(neo(over.expand(1,1,1)),neo(over.grow(1,1,1))),"unknown virtual subclass override is not equivalent");
  o=azt.a(0,0,0,1,1,1);n=new AxisAlignedBB(0,0,0,1,1,1);
  for(double x:new double[]{-4,-1,-0.0,0,1,4})for(double y:new double[]{-3,0,3})for(double z:new double[]{-2,0,2})check(o.a(x,y,z),n.expand(x,y,z),-2); // actual old addCoord confirms intentionally native directional dispatch
  events="";ro(o).b(arg(1),arg(2),arg(3));ok(events.equals("R123"),"old receiver/arg order");events="";rn(n).grow(arg(1),arg(2),arg(3));ok(events.equals("R123"),"native receiver/arg order");
  events="";try{ro(null).b(arg(1),arg(2),arg(3));throw new AssertionError();}catch(NullPointerException e){ok(events.equals("R123"),"old null after args");}events="";try{rn(null).grow(arg(1),arg(2),arg(3));throw new AssertionError();}catch(NullPointerException e){ok(events.equals("R123"),"native null after args");}
  events="";try{ro(o).b(arg(1),fail(2),arg(3));throw new AssertionError();}catch(IllegalStateException e){ok(events.equals("R12"),"old abrupt arg");}events="";try{rn(n).grow(arg(1),fail(2),arg(3));throw new AssertionError();}catch(IllegalStateException e){ok(events.equals("R12"),"native abrupt arg");}
 }
 static void site0(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(10, 10, 10),b.grow(10, 10, 10),0);}
static void site1(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(100, 100, 100),b.grow(100, 100, 100),1);}
static void site2(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(100, 100, 100),b.grow(100, 100, 100),2);}
static void site3(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(0.2D, 0.0D, 0.2D),b.grow(0.2D, 0.0D, 0.2D),3);}
static void site4(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(0.2D, 0.0D, 0.2D),b.grow(0.2D, 0.0D, 0.2D),4);}
static void site5(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(0.2D, 0.0D, 0.2D),b.grow(0.2D, 0.0D, 0.2D),5);}
static void site6(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(100, 50, 100),b.grow(100, 50, 100),6);}
static void site7(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(200, 200, 200),b.grow(200, 200, 200),7);}
static void site8(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(0.5D, 0.5D, 0.5D),b.grow(0.5D, 0.5D, 0.5D),8);}
static void site9(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(50, 50, 50),b.grow(50, 50, 50),9);}
static void site10(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(5, 5, 5),b.grow(5, 5, 5),10);}
static void site11(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(50, 50, 50),b.grow(50, 50, 50),11);}
static void site12(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(50, 50, 50),b.grow(50, 50, 50),12);}
static void site13(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(100, 50, 100),b.grow(100, 50, 100),13);}
static void site14(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(200, 200, 200),b.grow(200, 200, 200),14);}
static void site15(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(10, 10, 10),b.grow(10, 10, 10),15);}
static void site16(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(5.0D, 3.0D, 5.0D),b.grow(5.0D, 3.0D, 5.0D),16);}
static void site17(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(200, 200, 200),b.grow(200, 200, 200),17);}
static void site18(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(0.5D, 0.5D, 0.5D),b.grow(0.5D, 0.5D, 0.5D),18);}
static void site19(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(2.5, 2.5, 2.5),b.grow(2.5, 2.5, 2.5),19);}
static void site20(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(2.5, 2.5, 2.5),b.grow(2.5, 2.5, 2.5),20);}
static void site21(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(2D, 0D, 2D),b.grow(2D, 0D, 2D),21);}
static void site22(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(1, 1, 1),b.grow(1, 1, 1),22);}
static void site23(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(2, 2, 2),b.grow(2, 2, 2),23);}
static void site24(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(50, 50, 50),b.grow(50, 50, 50),24);}
static void site25(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(3, 0, 3),b.grow(3, 0, 3),25);}
static void site26(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(5, 5, 5),b.grow(5, 5, 5),26);}
static void site27(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(50, 50, 50),b.grow(50, 50, 50),27);}
static void site28(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(200, 200, 200),b.grow(200, 200, 200),28);}
static void site29(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(0.2, 0.2, 0.2),b.grow(0.2, 0.2, 0.2),29);}
static void site30(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(50, 50, 50),b.grow(50, 50, 50),30);}
static void site31(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(3, 3, 3),b.grow(3, 3, 3),31);}
static void sites(AxisAlignedBB b){site0(b);site1(b);site2(b);site3(b);site4(b);site5(b);site6(b);site7(b);site8(b);site9(b);site10(b);site11(b);site12(b);site13(b);site14(b);site15(b);site16(b);site17(b);site18(b);site19(b);site20(b);site21(b);site22(b);site23(b);site24(b);site25(b);site26(b);site27(b);site28(b);site29(b);site30(b);site31(b);}
static void centers(int x,int y,int z){geometry(azt.a(x + 0.5, y + 0.5, z + 0.5, x + 0.5, y + 0.5, z + 0.5).b(100, 100, 100),new AxisAlignedBB(x + 0.5, y + 0.5, z + 0.5, x + 0.5, y + 0.5, z + 0.5).grow(100, 100, 100),1);geometry(azt.a(x + 0.5, y + 0.5, z + 0.5, x + 0.5, y + 0.5, z + 0.5).b(100, 100, 100),new AxisAlignedBB(x + 0.5, y + 0.5, z + 0.5, x + 0.5, y + 0.5, z + 0.5).grow(100, 100, 100),2);}
 public static void main(String[] args){controls();
  double q1=Double.longBitsToDouble(0x7ff0000000000031L),q2=Double.longBitsToDouble(0xfff8000000000094L);
  double[][] pairs={{-1,1},{0,0},{-0.0,-0.0},{0.0,-0.0},{-Double.MAX_VALUE,Double.MAX_VALUE},{Double.MAX_VALUE,Double.MAX_VALUE},{Double.MIN_VALUE,Double.MIN_VALUE},{Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY},{Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY},{q1,q1},{q2,q2},{q1,1}};
  for(double[] x:pairs)for(double[] y:pairs)for(double[] z:pairs)sites(new AxisAlignedBB(x[0],y[0],z[0],x[1],y[1],z[1]));
  Random r=new Random(0x13579aceL);for(int i=0;i<1024;i++)sites(new AxisAlignedBB(Double.longBitsToDouble(r.nextLong()),Double.longBitsToDouble(r.nextLong()),Double.longBitsToDouble(r.nextLong()),Double.longBitsToDouble(r.nextLong()),Double.longBitsToDouble(r.nextLong()),Double.longBitsToDouble(r.nextLong())));
  int[] ie={Integer.MIN_VALUE,-30000000,-1,0,1,30000000,Integer.MAX_VALUE};for(int x:ie)for(int y:ie)for(int z:ie)centers(x,y,z);
  EntityItem entity=new EntityItem((World)null);for(int x:ie){entity.setPosition(x,64,-(double)x);AxisAlignedBB b=entity.getEntityBoundingBox();ok(b.getClass()==AxisAlignedBB.class,"actual native entity exact box implementation");sites(b);}
  System.out.println("PASS "+checks+" actual original/native checks; 32 exact growth-argument sites, 2 complete center expressions, normalized-box IEEE boundaries, native EntityItem getter, evaluation/null/exception, 7 negative semantic controls");
 }
}
