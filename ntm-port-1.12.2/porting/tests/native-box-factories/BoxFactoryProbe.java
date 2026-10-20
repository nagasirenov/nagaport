import net.minecraft.util.math.AxisAlignedBB;
public class BoxFactoryProbe {
 static int checks; static void ok(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static double[] old(azt b){return new double[]{b.a,b.b,b.c,b.d,b.e,b.f};}
 static double[] neo(AxisAlignedBB b){return new double[]{b.minX,b.minY,b.minZ,b.maxX,b.maxY,b.maxZ};}
 static boolean same(double[] a,double[] b){for(int i=0;i<6;i++)if(Double.doubleToRawLongBits(a[i])!=Double.doubleToRawLongBits(b[i]))return false;return true;}
 static azt old(double[] v){return azt.a(v[0],v[1],v[2],v[3],v[4],v[5]);}
 static AxisAlignedBB neo(double[] v){return new AxisAlignedBB(v[0],v[1],v[2],v[3],v[4],v[5]);}
 static void candidate(azt a,AxisAlignedBB b,int id){double[] x=old(a);for(double v:x)ok(Double.isFinite(v),"finite "+id);ok(same(x,neo(b)),"raw coordinates "+id);for(int j=0;j<3;j++)ok(x[j]<=x[j+3],"ordered "+id);
  double[][] probes={{-10,-10,-10,10,10,10},{0,0,0,0,0,0},{0,0,0,1,1,1},{1,1,1,2,2,2},{-2,-2,-2,-1,-1,-1}};
  for(double[] p:probes){azt o=old(p);AxisAlignedBB n=neo(p);ok(a.b(o)==b.intersects(n),"intersection "+id);for(double step:new double[]{-20,-1,-0.0,0,1,20}){ok(Double.doubleToLongBits(a.a(o,step))==Double.doubleToLongBits(b.calculateXOffset(n,step)),"x offset "+id);ok(Double.doubleToLongBits(a.b(o,step))==Double.doubleToLongBits(b.calculateYOffset(n,step)),"y offset "+id);ok(Double.doubleToLongBits(a.c(o,step))==Double.doubleToLongBits(b.calculateZOffset(n,step)),"z offset "+id);}}
 }
 static String events="";static double arg(int i){events+=i;return i;}static double fail(int i){events+=i;throw new IllegalStateException();}
 static void boundaries(){
 double[][] equal={{-1,-2,-3,1,2,3},{0,0,0,0,0,0},{-0.0,-0.0,-0.0,0,0,0},{Double.NEGATIVE_INFINITY,0,0,Double.POSITIVE_INFINITY,0,0},{Double.NaN,0,0,Double.NaN,0,0}};
 for(double[] v:equal)ok(same(old(old(v)),neo(neo(v))),"accepted boundary");
 double[][] different={{1,2,3,-1,-2,-3},{Double.NaN,0,0,1,0,0},{0.0,0,0,-0.0,0,0},{Double.POSITIVE_INFINITY,0,0,Double.NEGATIVE_INFINITY,0,0}};
 for(double[] v:different)ok(!same(old(old(v)),neo(neo(v))),"sorting difference must remain visible");
 events="";azt.a(arg(0),arg(1),arg(2),arg(3),arg(4),arg(5));ok(events.equals("012345"),"old order");events="";new AxisAlignedBB(arg(0),arg(1),arg(2),arg(3),arg(4),arg(5));ok(events.equals("012345"),"native order");
 events="";try{azt.a(arg(0),arg(1),fail(2),arg(3),arg(4),arg(5));}catch(IllegalStateException e){}ok(events.equals("012"),"old failure order");events="";try{new AxisAlignedBB(arg(0),arg(1),fail(2),arg(3),arg(4),arg(5));}catch(IllegalStateException e){}ok(events.equals("012"),"native failure order");
 // Exact EntityBoxcar search-box expression only, not a compiled/executed entity or world query.
 for(double posX:new double[]{-Double.MAX_VALUE,-1,-0.0,0,1,Double.MAX_VALUE,Double.NaN,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY}){double posY=posX,posZ=posX;ok(same(old(azt.a(posX - 2, posY - 2, posZ - 2, posX + 2, posY + 2, posZ + 2)),neo(new AxisAlignedBB(posX - 2, posY - 2, posZ - 2, posX + 2, posY + 2, posZ + 2))),"deferred EntityBoxcar expression");}
 }
 public static void main(String[] args){boundaries();
 candidate(azt.a(0, 0, 0, 1, 1, 1),new AxisAlignedBB(0, 0, 0, 1, 1, 1),0); // src/main/java/com/hbm/items/weapon/sedna/factory/XFactoryDrill.java
 candidate(azt.a(0, 0, 0, 1, 1, 1),new AxisAlignedBB(0, 0, 0, 1, 1, 1),1); // src/main/java/com/hbm/tileentity/DoorDecl.java
 candidate(azt.a(0, 0, 0, 1, 1, 1),new AxisAlignedBB(0, 0, 0, 1, 1, 1),2); // src/main/java/com/hbm/tileentity/DoorDecl.java
 candidate(azt.a(0.5, 0, 0, 1, 1, 1),new AxisAlignedBB(0.5, 0, 0, 1, 1, 1),3); // src/main/java/com/hbm/tileentity/DoorDecl.java
 candidate(azt.a(0, 0, 0, 0.5, 1, 1),new AxisAlignedBB(0, 0, 0, 0.5, 1, 1),4); // src/main/java/com/hbm/tileentity/DoorDecl.java
 candidate(azt.a(0, 0.75, 0, 1, 1, 1),new AxisAlignedBB(0, 0.75, 0, 1, 1, 1),5); // src/main/java/com/hbm/tileentity/DoorDecl.java
 candidate(azt.a(0, 0.5, 0, 1, 1, 1),new AxisAlignedBB(0, 0.5, 0, 1, 1, 1),6); // src/main/java/com/hbm/tileentity/DoorDecl.java
 candidate(azt.a(0, 0, 0, 0, 0, 0),new AxisAlignedBB(0, 0, 0, 0, 0, 0),7); // src/main/java/com/hbm/tileentity/DoorDecl.java
 candidate(azt.a(0, 0, 1 - 0.25, 1, 1, 1),new AxisAlignedBB(0, 0, 1 - 0.25, 1, 1, 1),8); // src/main/java/com/hbm/tileentity/DoorDecl.java
 candidate(azt.a(0, 0, 0.375, 1, 1, 0.625),new AxisAlignedBB(0, 0, 0.375, 1, 1, 0.625),9); // src/main/java/com/hbm/tileentity/DoorDecl.java
 candidate(azt.a(0, 0, 0, 1, 1, 1),new AxisAlignedBB(0, 0, 0, 1, 1, 1),10); // src/main/java/com/hbm/tileentity/DoorDecl.java
 candidate(azt.a(0, 0.5, 0.15, 1, 1, 0.85),new AxisAlignedBB(0, 0.5, 0.15, 1, 1, 0.85),11); // src/main/java/com/hbm/tileentity/DoorDecl.java
 candidate(azt.a(0, 0, 0, 1, 1, 1),new AxisAlignedBB(0, 0, 0, 1, 1, 1),12); // src/main/java/com/hbm/tileentity/DoorDecl.java
 candidate(azt.a(0.4, 0, 0, 1, 1, 1),new AxisAlignedBB(0.4, 0, 0, 1, 1, 1),13); // src/main/java/com/hbm/tileentity/DoorDecl.java
 candidate(azt.a(0, 0, 0, 0.6, 1, 1),new AxisAlignedBB(0, 0, 0, 0.6, 1, 1),14); // src/main/java/com/hbm/tileentity/DoorDecl.java
 candidate(azt.a(0, 0.5, 0, 1, 1, 1),new AxisAlignedBB(0, 0.5, 0, 1, 1, 1),15); // src/main/java/com/hbm/tileentity/DoorDecl.java
 candidate(azt.a(1 - 0.125, 0, 1 - 0.1875, 1, 1, 1),new AxisAlignedBB(1 - 0.125, 0, 1 - 0.1875, 1, 1, 1),16); // src/main/java/com/hbm/tileentity/DoorDecl.java
 candidate(azt.a(0, 0, 1 - 0.1875, 0.125, 1, 1),new AxisAlignedBB(0, 0, 1 - 0.1875, 0.125, 1, 1),17); // src/main/java/com/hbm/tileentity/DoorDecl.java
 candidate(azt.a(0, 0, 1 - 0.1875, 1, 1, 1),new AxisAlignedBB(0, 0, 1 - 0.1875, 1, 1, 1),18); // src/main/java/com/hbm/tileentity/DoorDecl.java
 candidate(azt.a(0, 0, 0.5, 1, 1, 1),new AxisAlignedBB(0, 0, 0.5, 1, 1, 1),19); // src/main/java/com/hbm/tileentity/DoorDecl.java
 candidate(azt.a(0, 0.25, 0.5, 1, 1, 1),new AxisAlignedBB(0, 0.25, 0.5, 1, 1, 1),20); // src/main/java/com/hbm/tileentity/DoorDecl.java
 candidate(azt.a(0, 0, 0.75, 1, 1, 1),new AxisAlignedBB(0, 0, 0.75, 1, 1, 1),21); // src/main/java/com/hbm/tileentity/DoorDecl.java
 candidate(azt.a(0, 0.85, 0.75, 1, 1, 1),new AxisAlignedBB(0, 0.85, 0.75, 1, 1, 1),22); // src/main/java/com/hbm/tileentity/DoorDecl.java
 candidate(azt.a(0.4, 0, 0, 1, 1, 1),new AxisAlignedBB(0.4, 0, 0, 1, 1, 1),23); // src/main/java/com/hbm/tileentity/DoorDecl.java
 candidate(azt.a(0, 0, 0, 0.6, 1, 1),new AxisAlignedBB(0, 0, 0, 0.6, 1, 1),24); // src/main/java/com/hbm/tileentity/DoorDecl.java
 candidate(azt.a(0, 0, 0, 0, 0, 0),new AxisAlignedBB(0, 0, 0, 0, 0, 0),25); // src/main/java/com/hbm/tileentity/DoorDecl.java
 candidate(azt.a(0, 0, 0, 1, 1, 1),new AxisAlignedBB(0, 0, 0, 1, 1, 1),26); // src/main/java/com/hbm/tileentity/DoorDecl.java
 candidate(azt.a(-1D, 0.375D, -1D, -0.875, 2.375D, 1D),new AxisAlignedBB(-1D, 0.375D, -1D, -0.875, 2.375D, 1D),27); // src/main/java/com/hbm/tileentity/machine/TileEntitySawmill.java
 candidate(azt.a(-4.5D, 0D, -4.5D, 4.5D, 1D, -0.5D),new AxisAlignedBB(-4.5D, 0D, -4.5D, 4.5D, 1D, -0.5D),28); // src/main/java/com/hbm/blocks/bomb/LaunchPadLarge.java
 candidate(azt.a(-4.5D, 0D, 0.5D, 4.5D, 1D, 4.5D),new AxisAlignedBB(-4.5D, 0D, 0.5D, 4.5D, 1D, 4.5D),29); // src/main/java/com/hbm/blocks/bomb/LaunchPadLarge.java
 candidate(azt.a(-4.5D, 0.875D, -0.5D, 4.5D, 1D, 0.5D),new AxisAlignedBB(-4.5D, 0.875D, -0.5D, 4.5D, 1D, 0.5D),30); // src/main/java/com/hbm/blocks/bomb/LaunchPadLarge.java
 candidate(azt.a(-1.5D, 0D, -1.5D, -0.5D, 1D, -0.5D),new AxisAlignedBB(-1.5D, 0D, -1.5D, -0.5D, 1D, -0.5D),31); // src/main/java/com/hbm/blocks/bomb/LaunchPadRusted.java
 candidate(azt.a(0.5D, 0D, -1.5D, 1.5D, 1D, -0.5D),new AxisAlignedBB(0.5D, 0D, -1.5D, 1.5D, 1D, -0.5D),32); // src/main/java/com/hbm/blocks/bomb/LaunchPadRusted.java
 candidate(azt.a(-1.5D, 0D, 0.5D, -0.5D, 1D, 1.5D),new AxisAlignedBB(-1.5D, 0D, 0.5D, -0.5D, 1D, 1.5D),33); // src/main/java/com/hbm/blocks/bomb/LaunchPadRusted.java
 candidate(azt.a(0.5D, 0D, 0.5D, 1.5D, 1D, 1.5D),new AxisAlignedBB(0.5D, 0D, 0.5D, 1.5D, 1D, 1.5D),34); // src/main/java/com/hbm/blocks/bomb/LaunchPadRusted.java
 candidate(azt.a(-0.5D, 0.5D, -1.5D, 0.5D, 1D, 1.5D),new AxisAlignedBB(-0.5D, 0.5D, -1.5D, 0.5D, 1D, 1.5D),35); // src/main/java/com/hbm/blocks/bomb/LaunchPadRusted.java
 candidate(azt.a(-1.5D, 0.5D, -0.5D, 1.5D, 1D, 0.5D),new AxisAlignedBB(-1.5D, 0.5D, -0.5D, 1.5D, 1D, 0.5D),36); // src/main/java/com/hbm/blocks/bomb/LaunchPadRusted.java
 candidate(azt.a(-1.5D, 0D, -1.5D, -0.5D, 1D, -0.5D),new AxisAlignedBB(-1.5D, 0D, -1.5D, -0.5D, 1D, -0.5D),37); // src/main/java/com/hbm/blocks/bomb/LaunchPad.java
 candidate(azt.a(0.5D, 0D, -1.5D, 1.5D, 1D, -0.5D),new AxisAlignedBB(0.5D, 0D, -1.5D, 1.5D, 1D, -0.5D),38); // src/main/java/com/hbm/blocks/bomb/LaunchPad.java
 candidate(azt.a(-1.5D, 0D, 0.5D, -0.5D, 1D, 1.5D),new AxisAlignedBB(-1.5D, 0D, 0.5D, -0.5D, 1D, 1.5D),39); // src/main/java/com/hbm/blocks/bomb/LaunchPad.java
 candidate(azt.a(0.5D, 0D, 0.5D, 1.5D, 1D, 1.5D),new AxisAlignedBB(0.5D, 0D, 0.5D, 1.5D, 1D, 1.5D),40); // src/main/java/com/hbm/blocks/bomb/LaunchPad.java
 candidate(azt.a(-0.5D, 0.5D, -1.5D, 0.5D, 1D, 1.5D),new AxisAlignedBB(-0.5D, 0.5D, -1.5D, 0.5D, 1D, 1.5D),41); // src/main/java/com/hbm/blocks/bomb/LaunchPad.java
 candidate(azt.a(-1.5D, 0.5D, -0.5D, 1.5D, 1D, 0.5D),new AxisAlignedBB(-1.5D, 0.5D, -0.5D, 1.5D, 1D, 0.5D),42); // src/main/java/com/hbm/blocks/bomb/LaunchPad.java
 candidate(azt.a(-0.5D, 0D, -0.5D, 0.5D, 1D, 0.5D),new AxisAlignedBB(-0.5D, 0D, -0.5D, 0.5D, 1D, 0.5D),43); // src/main/java/com/hbm/blocks/machine/MachineCentrifuge.java
 candidate(azt.a(-0.375D, 1D, -0.375D, 0.375D, 4D, 0.375D),new AxisAlignedBB(-0.375D, 1D, -0.375D, 0.375D, 4D, 0.375D),44); // src/main/java/com/hbm/blocks/machine/MachineCentrifuge.java
 candidate(azt.a(-1.5D, 0D, -1.5D, 1.5D, 1D, 1.5D),new AxisAlignedBB(-1.5D, 0D, -1.5D, 1.5D, 1D, 1.5D),45); // src/main/java/com/hbm/blocks/machine/MachineSawmill.java
 candidate(azt.a(-1.25D, 1D, -0.5D, -0.625D, 1.875D, 0.5D),new AxisAlignedBB(-1.25D, 1D, -0.5D, -0.625D, 1.875D, 0.5D),46); // src/main/java/com/hbm/blocks/machine/MachineSawmill.java
 candidate(azt.a(-0.625D, 1D, -1D, 1.375D, 2D, 1D),new AxisAlignedBB(-0.625D, 1D, -1D, 1.375D, 2D, 1D),47); // src/main/java/com/hbm/blocks/machine/MachineSawmill.java
 candidate(azt.a(-0.125, 0, -0.125, 0.125, 4, 0.125),new AxisAlignedBB(-0.125, 0, -0.125, 0.125, 4, 0.125),48); // src/main/java/com/hbm/blocks/machine/rbmk/RBMKAutoloader.java
 candidate(azt.a(-0.5, 4, -0.5, 0.5, 9, 0.5),new AxisAlignedBB(-0.5, 4, -0.5, 0.5, 9, 0.5),49); // src/main/java/com/hbm/blocks/machine/rbmk/RBMKAutoloader.java
 candidate(azt.a(-0.5, 0, -1.5, 0.5, 3, 1.5),new AxisAlignedBB(-0.5, 0, -1.5, 0.5, 3, 1.5),50); // src/main/java/com/hbm/blocks/machine/MachineHydroponic.java
 candidate(azt.a(-1.125, 1, -2.5, 1.125, 3, -1.5),new AxisAlignedBB(-1.125, 1, -2.5, 1.125, 3, -1.5),51); // src/main/java/com/hbm/blocks/machine/MachineHydroponic.java
 candidate(azt.a(-1.125, 1, 1.5, 1.125, 3, 2.5),new AxisAlignedBB(-1.125, 1, 1.5, 1.125, 3, 2.5),52); // src/main/java/com/hbm/blocks/machine/MachineHydroponic.java
 candidate(azt.a(-0.75, 0, -2.5, 0.75, 1, -1.5),new AxisAlignedBB(-0.75, 0, -2.5, 0.75, 1, -1.5),53); // src/main/java/com/hbm/blocks/machine/MachineHydroponic.java
 candidate(azt.a(-0.75, 0, 1.5, 0.75, 1, 2.5),new AxisAlignedBB(-0.75, 0, 1.5, 0.75, 1, 2.5),54); // src/main/java/com/hbm/blocks/machine/MachineHydroponic.java
 candidate(azt.a(-0.5D, 0D, -0.5D, 0.5D, 1D, 0.5D),new AxisAlignedBB(-0.5D, 0D, -0.5D, 0.5D, 1D, 0.5D),55); // src/main/java/com/hbm/blocks/machine/MachineGasCent.java
 candidate(azt.a(-0.4375D, 1D, -0.4375D, 0.4375D, 4D, 0.4375D),new AxisAlignedBB(-0.4375D, 1D, -0.4375D, 0.4375D, 4D, 0.4375D),56); // src/main/java/com/hbm/blocks/machine/MachineGasCent.java
 candidate(azt.a(-1.5D, 0D, -1.5D, 1.5D, 0.5D, 1.5D),new AxisAlignedBB(-1.5D, 0D, -1.5D, 1.5D, 0.5D, 1.5D),57); // src/main/java/com/hbm/blocks/machine/MachineCrucible.java
 candidate(azt.a(-1.25D, 0.5D, -1.25D, 1.25D, 1.5D, -1D),new AxisAlignedBB(-1.25D, 0.5D, -1.25D, 1.25D, 1.5D, -1D),58); // src/main/java/com/hbm/blocks/machine/MachineCrucible.java
 candidate(azt.a(-1.25D, 0.5D, -1.25D, -1D, 1.5D, 1.25D),new AxisAlignedBB(-1.25D, 0.5D, -1.25D, -1D, 1.5D, 1.25D),59); // src/main/java/com/hbm/blocks/machine/MachineCrucible.java
 candidate(azt.a(-1.25D, 0.5D, 1D, 1.25D, 1.5D, 1.25D),new AxisAlignedBB(-1.25D, 0.5D, 1D, 1.25D, 1.5D, 1.25D),60); // src/main/java/com/hbm/blocks/machine/MachineCrucible.java
 candidate(azt.a(1D, 0.5D, -1.25D, 1.25D, 1.5D, 1.25D),new AxisAlignedBB(1D, 0.5D, -1.25D, 1.25D, 1.5D, 1.25D),61); // src/main/java/com/hbm/blocks/machine/MachineCrucible.java
 candidate(azt.a(-1.5D, 0D, -1.5D, 1.5D, 3.875D, 1.5D),new AxisAlignedBB(-1.5D, 0D, -1.5D, 1.5D, 3.875D, 1.5D),62); // src/main/java/com/hbm/blocks/machine/MachineGasFlare.java
 candidate(azt.a(-0.75D, 3.875D, -0.75D, 0.75D, 9, 0.75D),new AxisAlignedBB(-0.75D, 3.875D, -0.75D, 0.75D, 9, 0.75D),63); // src/main/java/com/hbm/blocks/machine/MachineGasFlare.java
 candidate(azt.a(-1.5D, 9D, -1.5D, 1.5D, 9.375D, 1.5D),new AxisAlignedBB(-1.5D, 9D, -1.5D, 1.5D, 9.375D, 1.5D),64); // src/main/java/com/hbm/blocks/machine/MachineGasFlare.java
 candidate(azt.a(-0.75D, 9.375D, -0.75D, 0.75D, 12, 0.75D),new AxisAlignedBB(-0.75D, 9.375D, -0.75D, 0.75D, 12, 0.75D),65); // src/main/java/com/hbm/blocks/machine/MachineGasFlare.java
 candidate(azt.a(-3.5, 0, -1.5, 3.5, 1, 1.5),new AxisAlignedBB(-3.5, 0, -1.5, 3.5, 1, 1.5),66); // src/main/java/com/hbm/blocks/machine/MachineOreSlopper.java
 candidate(azt.a(0.5, 1, -1.5, 3.5, 3.25, 1.5),new AxisAlignedBB(0.5, 1, -1.5, 3.5, 3.25, 1.5),67); // src/main/java/com/hbm/blocks/machine/MachineOreSlopper.java
 candidate(azt.a(-2.25, 1, -1.5, 0.25, 3.25, -0.75),new AxisAlignedBB(-2.25, 1, -1.5, 0.25, 3.25, -0.75),68); // src/main/java/com/hbm/blocks/machine/MachineOreSlopper.java
 candidate(azt.a(-2.25, 1, 0.75, 0.25, 3.25, 1.5),new AxisAlignedBB(-2.25, 1, 0.75, 0.25, 3.25, 1.5),69); // src/main/java/com/hbm/blocks/machine/MachineOreSlopper.java
 candidate(azt.a(-2.25, 1, -1.5, -2, 3.25, 1.5),new AxisAlignedBB(-2.25, 1, -1.5, -2, 3.25, 1.5),70); // src/main/java/com/hbm/blocks/machine/MachineOreSlopper.java
 candidate(azt.a(0, 1, -1.5, 0.25, 3.25, 1.5),new AxisAlignedBB(0, 1, -1.5, 0.25, 3.25, 1.5),71); // src/main/java/com/hbm/blocks/machine/MachineOreSlopper.java
 candidate(azt.a(-2, 1, -0.75, 0, 2, 0.75),new AxisAlignedBB(-2, 1, -0.75, 0, 2, 0.75),72); // src/main/java/com/hbm/blocks/machine/MachineOreSlopper.java
 candidate(azt.a(-3.25, 1, -1, -2.25, 3, 1),new AxisAlignedBB(-3.25, 1, -1, -2.25, 3, 1),73); // src/main/java/com/hbm/blocks/machine/MachineOreSlopper.java
System.out.println("PASS "+checks+" actual old/native assertions; 74 literal factory expressions; no whole-class claim.");}}
