import java.util.*;
import net.minecraft.util.math.AxisAlignedBB;
public class BoundProbe {
 static long checks;
 static void ok(boolean x,String why){checks++;if(!x)throw new AssertionError(why);}
 static double[] old(azt a){return new double[]{a.a,a.b,a.c,a.d,a.e,a.f};}
 static double[] neo(AxisAlignedBB a){return new double[]{a.minX,a.minY,a.minZ,a.maxX,a.maxY,a.maxZ};}
 static boolean same(double[] a,double[] b){for(int i=0;i<6;i++)if(Double.doubleToRawLongBits(a[i])!=Double.doubleToRawLongBits(b[i]))return false;return true;}
 static azt old(double[] a){return azt.a(a[0],a[1],a[2],a[3],a[4],a[5]);}
 static AxisAlignedBB neo(double[] a){return new AxisAlignedBB(a[0],a[1],a[2],a[3],a[4],a[5]);}
 static void check(azt a,AxisAlignedBB b,int site){double[] oa=old(a),nb=neo(b);for(int i=0;i<6;i++)ok(Double.doubleToRawLongBits(oa[i])==Double.doubleToRawLongBits(nb[i]),"raw coordinates site="+site+" axis="+i);}
 static void geometry(azt a,AxisAlignedBB b,int site){check(a,b,site);double[] v=old(a);
  double[][] probes={{v[0],v[1],v[2],v[3],v[4],v[5]},{v[3],v[4],v[5],v[3]+1,v[4]+1,v[5]+1},{v[0]-1,v[1]-1,v[2]-1,v[3]+1,v[4]+1,v[5]+1},{v[3]+2,v[4]+2,v[5]+2,v[3]+3,v[4]+3,v[5]+3}};
  for(double[] p:probes){azt o=old(p);AxisAlignedBB n=neo(p);ok(a.b(o)==b.intersects(n),"intersection site="+site);for(double step:new double[]{-100,-1,-0.0,0.0,1,100}){ok(Double.doubleToRawLongBits(a.a(o,step))==Double.doubleToRawLongBits(b.calculateXOffset(n,step)),"x-offset");ok(Double.doubleToRawLongBits(a.b(o,step))==Double.doubleToRawLongBits(b.calculateYOffset(n,step)),"y-offset");ok(Double.doubleToRawLongBits(a.c(o,step))==Double.doubleToRawLongBits(b.calculateZOffset(n,step)),"z-offset");}}
 }
 static String events="";static double arg(int i){events+=i;return i;}static double fail(int i){events+=i;throw new IllegalStateException();}
 static void controls(){
  double nan1=Double.longBitsToDouble(0x7ff8000000000031L),nan2=Double.longBitsToDouble(0x7ff8000000000094L);
  double[][] differences={{1,2,3,-1,-2,-3},{nan1,0,0,1,0,0},{0,0,0,-0.0,0,0},{Double.POSITIVE_INFINITY,0,0,Double.NEGATIVE_INFINITY,0,0},{nan1,0,0,nan2,0,0},{Integer.MAX_VALUE,0,0,Integer.MIN_VALUE,0,0}};
  for(double[] v:differences)ok(!same(old(old(v)),neo(neo(v))),"global-constructor rewrite negative control");
  int x=Integer.MAX_VALUE;ok(!same(old(azt.a(x,0,0,x+1,0,0)),neo(new AxisAlignedBB(x,0,0,x+1,0,0))),"actual Java int-overflow negative control");
  events="";azt.a(arg(0),arg(1),arg(2),arg(3),arg(4),arg(5));ok(events.equals("012345"),"old evaluation order");events="";new AxisAlignedBB(arg(0),arg(1),arg(2),arg(3),arg(4),arg(5));ok(events.equals("012345"),"native evaluation order");
  events="";try{azt.a(arg(0),arg(1),fail(2),arg(3),arg(4),arg(5));}catch(IllegalStateException e){}ok(events.equals("012"),"old abrupt evaluation");events="";try{new AxisAlignedBB(arg(0),arg(1),fail(2),arg(3),arg(4),arg(5));}catch(IllegalStateException e){}ok(events.equals("012"),"native abrupt evaluation");
 }
 static void site0(int x,int y,int z){geometry(azt.a(x + 0.25D, y + 0D, z + 0.875D, x + 1D, y + 1D, z + 1D),new AxisAlignedBB(x + 0.25D, y + 0D, z + 0.875D, x + 1D, y + 1D, z + 1D),0);}
static void site1(int x,int y,int z){geometry(azt.a(x + 0D, y + 0D, z + 0.75D, x + 0.25D, y + 1D, z + 1D),new AxisAlignedBB(x + 0D, y + 0D, z + 0.75D, x + 0.25D, y + 1D, z + 1D),1);}
static void site2(int x,int y,int z){geometry(azt.a(x + 0D, y + 0D, z + 0D, x + 0.125D, y + 1D, z + 0.75D),new AxisAlignedBB(x + 0D, y + 0D, z + 0D, x + 0.125D, y + 1D, z + 0.75D),2);}
static void site3(int x,int y,int z){geometry(azt.a(x + 0D, y + 0D, z + 0D, x + 0.75D, y + 1D, z + 0.125D),new AxisAlignedBB(x + 0D, y + 0D, z + 0D, x + 0.75D, y + 1D, z + 0.125D),3);}
static void site4(int x,int y,int z){geometry(azt.a(x + 0.75D, y + 0D, z + 0D, x + 1D, y + 1D, z + 0.25D),new AxisAlignedBB(x + 0.75D, y + 0D, z + 0D, x + 1D, y + 1D, z + 0.25D),4);}
static void site5(int x,int y,int z){geometry(azt.a(x + 0.875D, y + 0D, z + 0.25D, x + 1D, y + 1D, z + 1D),new AxisAlignedBB(x + 0.875D, y + 0D, z + 0.25D, x + 1D, y + 1D, z + 1D),5);}
static void site6(int x,int y,int z){geometry(azt.a(x + 0.875D, y + 0D, z + 0D, x + 1D, y + 1D, z + 0.75D),new AxisAlignedBB(x + 0.875D, y + 0D, z + 0D, x + 1D, y + 1D, z + 0.75D),6);}
static void site7(int x,int y,int z){geometry(azt.a(x + 0.75D, y + 0D, z + 0.75D, x + 1D, y + 1D, z + 1D),new AxisAlignedBB(x + 0.75D, y + 0D, z + 0.75D, x + 1D, y + 1D, z + 1D),7);}
static void site8(int x,int y,int z){geometry(azt.a(x + 0D, y + 0D, z + 0.875D, x + 0.75D, y + 1D, z + 1D),new AxisAlignedBB(x + 0D, y + 0D, z + 0.875D, x + 0.75D, y + 1D, z + 1D),8);}
static void site9(int x,int y,int z){geometry(azt.a(x + 0D, y + 0D, z + 0.25D, x + 0.125D, y + 1D, z + 1D),new AxisAlignedBB(x + 0D, y + 0D, z + 0.25D, x + 0.125D, y + 1D, z + 1D),9);}
static void site10(int x,int y,int z){geometry(azt.a(x + 0D, y + 0D, z + 0D, x + 0.25D, y + 1D, z + 0.25D),new AxisAlignedBB(x + 0D, y + 0D, z + 0D, x + 0.25D, y + 1D, z + 0.25D),10);}
static void site11(int x,int y,int z){geometry(azt.a(x + 0.25D, y + 0D, z + 0D, x + 1D, y + 1D, z + 0.125D),new AxisAlignedBB(x + 0.25D, y + 0D, z + 0D, x + 1D, y + 1D, z + 0.125D),11);}
static void site12(int x,int y,int z){geometry(azt.a(x + 0.5, y + 0.5, z + 0.5, x + 0.5, y + 0.5, z + 0.5),new AxisAlignedBB(x + 0.5, y + 0.5, z + 0.5, x + 0.5, y + 0.5, z + 0.5),12);}
static void site13(int x,int y,int z){geometry(azt.a(x + 0.5, y + 0.5, z + 0.5, x + 0.5, y + 0.5, z + 0.5),new AxisAlignedBB(x + 0.5, y + 0.5, z + 0.5, x + 0.5, y + 0.5, z + 0.5),13);}
static void site14(double x,double y,double z){check(azt.a(x, y, z, x, y, z),new AxisAlignedBB(x, y, z, x, y, z),14);}
static void site15(int x,int y,int z){geometry(azt.a(x, y, z, x, y, z),new AxisAlignedBB(x, y, z, x, y, z),15);}
static void site16(int x,int y,int z){geometry(azt.a(x + 0.5, y + 0.5, z + 0.5, x + 0.5, y + 0.5, z + 0.5),new AxisAlignedBB(x + 0.5, y + 0.5, z + 0.5, x + 0.5, y + 0.5, z + 0.5),16);}
static void site17(int x,int y,int z){geometry(azt.a(x + 0.5, y + 0.5, z + 0.5, x + 0.5, y + 0.5, z + 0.5),new AxisAlignedBB(x + 0.5, y + 0.5, z + 0.5, x + 0.5, y + 0.5, z + 0.5),17);}
static void site18(int x,int y,int z){geometry(azt.a(x + 0.5, y + 0.5, z + 0.5, x + 0.5, y + 0.5, z + 0.5),new AxisAlignedBB(x + 0.5, y + 0.5, z + 0.5, x + 0.5, y + 0.5, z + 0.5),18);}
static void site19(int x,int y,int z){geometry(azt.a(x - 7.5, y - 7.5, z - 7.5, x + 7.5, y + 7.5, z + 7.5),new AxisAlignedBB(x - 7.5, y - 7.5, z - 7.5, x + 7.5, y + 7.5, z + 7.5),19);}
static void ints(int x,int y,int z){site0(x,y,z);site1(x,y,z);site2(x,y,z);site3(x,y,z);site4(x,y,z);site5(x,y,z);site6(x,y,z);site7(x,y,z);site8(x,y,z);site9(x,y,z);site10(x,y,z);site11(x,y,z);site12(x,y,z);site13(x,y,z);site15(x,y,z);site16(x,y,z);site17(x,y,z);site18(x,y,z);site19(x,y,z);}
static void doubles(double x,double y,double z){site14(x,y,z);}
static List<azt> decoOld(int meta,int x,int y,int z){List<azt> bbs=new ArrayList<>();switch(meta) {
			case 2:
				bbs.add(azt.a(x + 0.25D, y + 0D, z + 0.875D, x + 1D, y + 1D, z + 1D));
				bbs.add(azt.a(x + 0D, y + 0D, z + 0.75D, x + 0.25D, y + 1D, z + 1D));
				bbs.add(azt.a(x + 0D, y + 0D, z + 0D, x + 0.125D, y + 1D, z + 0.75D));
				break;
			case 3:
				bbs.add(azt.a(x + 0D, y + 0D, z + 0D, x + 0.75D, y + 1D, z + 0.125D));
				bbs.add(azt.a(x + 0.75D, y + 0D, z + 0D, x + 1D, y + 1D, z + 0.25D));
				bbs.add(azt.a(x + 0.875D, y + 0D, z + 0.25D, x + 1D, y + 1D, z + 1D));
				break;
			case 4:
				bbs.add(azt.a(x + 0.875D, y + 0D, z + 0D, x + 1D, y + 1D, z + 0.75D));
				bbs.add(azt.a(x + 0.75D, y + 0D, z + 0.75D, x + 1D, y + 1D, z + 1D));
				bbs.add(azt.a(x + 0D, y + 0D, z + 0.875D, x + 0.75D, y + 1D, z + 1D));
				break;
			case 5:
				bbs.add(azt.a(x + 0D, y + 0D, z + 0.25D, x + 0.125D, y + 1D, z + 1D));
				bbs.add(azt.a(x + 0D, y + 0D, z + 0D, x + 0.25D, y + 1D, z + 0.25D));
				bbs.add(azt.a(x + 0.25D, y + 0D, z + 0D, x + 1D, y + 1D, z + 0.125D));
				break;
			} return bbs;}
static List<AxisAlignedBB> decoNew(int meta,int x,int y,int z){List<AxisAlignedBB> bbs=new ArrayList<>();switch(meta) {
			case 2:
				bbs.add(new AxisAlignedBB(x + 0.25D, y + 0D, z + 0.875D, x + 1D, y + 1D, z + 1D));
				bbs.add(new AxisAlignedBB(x + 0D, y + 0D, z + 0.75D, x + 0.25D, y + 1D, z + 1D));
				bbs.add(new AxisAlignedBB(x + 0D, y + 0D, z + 0D, x + 0.125D, y + 1D, z + 0.75D));
				break;
			case 3:
				bbs.add(new AxisAlignedBB(x + 0D, y + 0D, z + 0D, x + 0.75D, y + 1D, z + 0.125D));
				bbs.add(new AxisAlignedBB(x + 0.75D, y + 0D, z + 0D, x + 1D, y + 1D, z + 0.25D));
				bbs.add(new AxisAlignedBB(x + 0.875D, y + 0D, z + 0.25D, x + 1D, y + 1D, z + 1D));
				break;
			case 4:
				bbs.add(new AxisAlignedBB(x + 0.875D, y + 0D, z + 0D, x + 1D, y + 1D, z + 0.75D));
				bbs.add(new AxisAlignedBB(x + 0.75D, y + 0D, z + 0.75D, x + 1D, y + 1D, z + 1D));
				bbs.add(new AxisAlignedBB(x + 0D, y + 0D, z + 0.875D, x + 0.75D, y + 1D, z + 1D));
				break;
			case 5:
				bbs.add(new AxisAlignedBB(x + 0D, y + 0D, z + 0.25D, x + 0.125D, y + 1D, z + 1D));
				bbs.add(new AxisAlignedBB(x + 0D, y + 0D, z + 0D, x + 0.25D, y + 1D, z + 0.25D));
				bbs.add(new AxisAlignedBB(x + 0.25D, y + 0D, z + 0D, x + 1D, y + 1D, z + 0.125D));
				break;
			} return bbs;}
 public static void main(String[] args){controls();
  int[] edges={Integer.MIN_VALUE,Integer.MIN_VALUE+1,-30000000,-1,0,1,30000000,Integer.MAX_VALUE-1,Integer.MAX_VALUE};
  for(int x:edges)for(int y:edges)for(int z:edges)ints(x,y,z);
  Random r=new Random(0x1234abcdL);for(int i=0;i<256;i++)ints(r.nextInt(),r.nextInt(),r.nextInt());
  double[] de={-Double.MAX_VALUE,-Double.MIN_NORMAL,-Double.MIN_VALUE,-1,-0.0,0,Double.MIN_VALUE,Double.MIN_NORMAL,1,Double.MAX_VALUE,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY,Double.NaN,Double.longBitsToDouble(0x7ff0000000000031L),Double.longBitsToDouble(0xfff0000000000094L),Double.longBitsToDouble(0xfff80000000000b4L)};
  for(double x:de)for(double y:de)for(double z:de)doubles(x,y,z);
  for(int i=0;i<1024;i++)doubles(Double.longBitsToDouble(r.nextLong()),Double.longBitsToDouble(r.nextLong()),Double.longBitsToDouble(r.nextLong()));
  for(int x:edges)for(int y:edges)for(int z:edges)for(int meta:new int[]{Integer.MIN_VALUE,-1,0,1,2,3,4,5,6,Integer.MAX_VALUE}){List<azt>a=decoOld(meta,x,y,z);List<AxisAlignedBB>b=decoNew(meta,x,y,z);ok(a.size()==b.size(),"DecoBlock branch cardinality");ok(a.size()==(meta>=2&&meta<=5?3:0),"DecoBlock original branch count");for(int k=0;k<a.size();k++)check(a.get(k),b.get(k),100+meta);}
  System.out.println("PASS "+checks+" actual original/native checks; 20 exact expressions, full 12-piece DecoBlock switch, 7 semantic differences retained; not complete gameplay classes");
 }
}
