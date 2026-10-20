import api.hbm.energymk2.*;
import api.hbm.energymk2.Nodespace.*;
import api.hbm.tile.ILoadedTile.TileAccessCache;
import com.hbm.lib.ForgeDirection;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.init.Bootstrap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.*;

/** Real TileEntity/World subclasses; only NTM topology and packet collaborators controlled. */
public class EnergyConnectorNativeTest {
 static int checks; static String events="";
 static void check(boolean ok,String label){checks++;if(!ok)throw new AssertionError(label);}
 static class W extends World {
  TileEntity tile; boolean loaded=true; int gets,guards; BlockPos last; long time;
  W(){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),false);provider.setWorld(this);provider.setDimension(91);}
  protected IChunkProvider createChunkProvider(){return null;}
  protected boolean isChunkLoaded(int x,int z,boolean allow){guards++;return loaded;}
  public TileEntity getTileEntity(BlockPos p){gets++;last=p;return tile;}
  public long getTotalWorldTime(){return time;}
 }
 static class P extends TileEntity implements IEnergyProviderMK2 {
  long power,max=100,speed=Long.MIN_VALUE; public long getPower(){return power;} public long getMaxPower(){return max;} public void setPower(long p){events+="debit;";power=p;} public boolean isLoaded(){return true;}
  public long getProviderSpeed(){return speed==Long.MIN_VALUE?IEnergyProviderMK2.super.getProviderSpeed():speed;}
 }
 static class R extends TileEntity implements IEnergyReceiverMK2 {
  long power,max=100,speed=Long.MIN_VALUE; boolean allowed=true,direct=true,loaded=true; int calls; ForgeDirection face;
  public long getPower(){return power;}public long getMaxPower(){return max;}public void setPower(long p){events+="credit;";power=p;}public boolean isLoaded(){return loaded;}
  public boolean canConnect(ForgeDirection d){face=d;return allowed;}public boolean allowDirectProvision(){return direct;}
  public long getReceiverSpeed(){return speed==Long.MIN_VALUE?IEnergyReceiverMK2.super.getReceiverSpeed():speed;}
  public long transferPower(long p){calls++;return IEnergyReceiverMK2.super.transferPower(p);}
 }
 static class Partial extends R {public long transferPower(long p){super.transferPower(p-3);return 3;}}
 static class Both extends P implements IEnergyReceiverMK2 {int calls;public long transferPower(long p){calls++;return IEnergyReceiverMK2.super.transferPower(p);}}
 static class C extends TileEntity implements IEnergyConductorMK2 {
  boolean allowed=true; ForgeDirection face; PowerNode custom; int creates;
  public boolean canConnect(ForgeDirection d){face=d;return allowed;}
  public PowerNode createNode(){creates++;return custom==null?IEnergyConductorMK2.super.createNode():custom;}
 }
 static class Hybrid extends R implements IEnergyConductorMK2 {}
 static void reset(W w,TileEntity t){TileAccessCache.cache.clear();w.tile=t;Nodespace.selected=null;Nodespace.lookups=0;events="";}
 static void arithmetic(){
  R r=new R();check(r.getReceiverSpeed()==100&&r.allowDirectProvision(),"receiver default capacity rate/direct");P p=new P();check(p.getProviderSpeed()==100,"provider default rate");
  long[][] cases={{0,100,0,0,0},{20,100,30,50,0},{90,100,30,100,20},{100,100,9,100,9},{20,100,-5,15,0},{Long.MAX_VALUE-2,Long.MAX_VALUE,10,Long.MIN_VALUE+7,0},{110,100,1,100,11}};
  for(long[] c:cases){r.power=c[0];r.max=c[1];long left=r.transferPower(c[2]);check(r.power==c[3]&&left==c[4],"transfer exact arithmetic "+java.util.Arrays.toString(c));}
  p.power=7;p.usePower(10);check(p.power==-3,"usePower intentionally unchecked");p.usePower(-4);check(p.power==1,"negative usePower increases");p.power=Long.MIN_VALUE;p.usePower(1);check(p.power==Long.MAX_VALUE,"usePower wraps long");
  NBTTagCompound n=new NBTTagCompound();r.power=41;r.max=82;r.provideInfoForECMK2(n);check(n.getLong("energy")==41&&n.getLong("capacity")==82,"EC exact long values");
 }
 static void direct(W w){
  long[][] cases={{70,0,100,100,100,0,70},{70,90,100,100,100,60,100},{70,0,100,12,100,58,12},{70,0,100,100,9,61,9},{70,100,100,100,100,70,100},{0,0,100,100,100,0,0},{-5,20,100,100,100,0,15},{20,0,100,-3,100,23,-3},{20,0,100,100,-4,24,-4},{20,Long.MIN_VALUE,Long.MAX_VALUE,100,100,21,Long.MIN_VALUE}};
  // Last case: capacity subtraction wraps to -1, transfer sum wraps to MAX and is accepted.
  cases[cases.length-1][6]=Long.MAX_VALUE;
  for(long[] c:cases){P p=new P();R r=new R();p.power=c[0];r.power=c[1];r.max=c[2];p.speed=c[3];r.speed=c[4];reset(w,r);p.tryProvide(w,4,64,-17,ForgeDirection.EAST);check(p.power==c[5]&&r.power==c[6],"direct exact "+java.util.Arrays.toString(c));check(events.equals("credit;debit;")&&r.calls==1,"credit precedes debit including zero/negative");check(r.face==ForgeDirection.WEST,"opposite receiver face");}
  P p=new P();p.power=50;Partial r=new Partial();reset(w,r);p.tryProvide(w,4,64,-17,ForgeDirection.UP);check(p.power==3&&r.power==47,"leftover returned then subtracted before debit");
  R denied=new R();denied.allowed=false;reset(w,denied);p.tryProvide(w,4,64,-17,ForgeDirection.DOWN);check(denied.calls==0&&events.isEmpty(),"receiver connection denial");denied.allowed=true;denied.direct=false;reset(w,denied);p.tryProvide(w,4,64,-17,ForgeDirection.DOWN);check(denied.calls==0,"direct provision opt out");
  Both self=new Both();self.power=20;reset(w,self);self.tryProvide(w,4,64,-17,ForgeDirection.NORTH);check(self.calls==0&&self.power==20,"self receiver excluded");reset(w,new TileEntity(){});p.tryProvide(w,4,64,-17,ForgeDirection.UNKNOWN);check(events.isEmpty(),"ordinary tile ignored");reset(w,null);p.tryProvide(w,4,64,-17,ForgeDirection.EAST);check(events.isEmpty(),"missing tile ignored");
 }
 static void registration(W w){
  P p=new P();R r=new R();C c=new C();PowerNode node=new PowerNode();node.net=new Net();reset(w,c);Nodespace.selected=node;
  p.tryProvide(w,4,64,-17,ForgeDirection.SOUTH);r.trySubscribe(w,new DirPos(4,64,-17,ForgeDirection.NORTH));check(node.net.providers==1&&node.net.receivers==1,"provider and receiver registration");check(Nodespace.lookups==2&&Nodespace.lastWorld==w&&Nodespace.x==4&&Nodespace.y==64&&Nodespace.z==-17,"nodespace exact arguments");check(c.face==ForgeDirection.SOUTH&&c.creates==0,"subscribe uses opposite face and lookup not create");
  c.allowed=false;int before=Nodespace.lookups;p.tryProvide(w,4,64,-17,ForgeDirection.UP);r.trySubscribe(w,4,64,-17,ForgeDirection.UP);check(Nodespace.lookups==before,"denied conductor skips node lookup");
  c.allowed=true;Nodespace.selected=null;p.tryProvide(w,4,64,-17,ForgeDirection.UP);r.trySubscribe(w,4,64,-17,ForgeDirection.UP);Nodespace.selected=new PowerNode();p.tryProvide(w,4,64,-17,ForgeDirection.UP);r.trySubscribe(w,4,64,-17,ForgeDirection.UP);check(node.net.providers==1&&node.net.receivers==1,"null node and null net ignored");
  c.custom=node;c.allowed=false;w.loaded=false;before=w.guards;int gets=w.gets;r.tryUnsubscribe(w,Integer.MAX_VALUE,-1,Integer.MIN_VALUE);check(node.net.removed==1&&node.net.last==r&&c.creates==1,"unsubscribe uses createNode regardless connection");check(w.guards==before&&w.gets==gets+1&&w.last.equals(new BlockPos(Integer.MAX_VALUE,-1,Integer.MIN_VALUE)),"unsubscribe direct native lookup no loaded guard/cache");w.loaded=true;
  Hybrid h=new Hybrid();h.allowed=false;p.power=20;reset(w,h);Nodespace.selected=node;p.tryProvide(w,4,64,-17,ForgeDirection.UP);check(h.calls==0&&Nodespace.lookups==0,"hybrid denied both branches");h.allowed=true;reset(w,h);Nodespace.selected=node;p.tryProvide(w,4,64,-17,ForgeDirection.UP);check(node.net.providers==2&&h.power==20&&p.power==0,"hybrid registers AND transfers independently");
 }
 static void cache(W w){P p=new P();p.power=20;R first=new R(),second=new R();reset(w,first);int before=w.gets;p.tryProvide(w,4,64,-17,ForgeDirection.EAST);w.tile=second;p.power=10;p.tryProvide(w,4,64,-17,ForgeDirection.EAST);check(w.gets==before+1&&first.power==30&&second.power==0,"provider retains production cached identity");first.loaded=false;p.power=10;p.tryProvide(w,4,64,-17,ForgeDirection.EAST);check(second.power==10&&w.gets==before+2,"unloaded cached tile refreshes");reset(w,second);w.loaded=false;before=w.gets;p.tryProvide(w,4,64,-17,ForgeDirection.EAST);check(w.gets==before,"provider unloaded chunk does not request tile");w.loaded=true;}
 static void coordinates(){
  int[][] coords={{0,64,0},{-17,-1,33},{Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MIN_VALUE}};
  for(int[] a:coords){C c=new C();c.setPos(new BlockPos(a[0],a[1],a[2]));PowerNode n=c.createNode();check(n.positions.length==1&&n.positions[0].getX()==a[0]&&n.positions[0].getY()==a[1]&&n.positions[0].getZ()==a[2],"node exact center");ForgeDirection[] order={ForgeDirection.EAST,ForgeDirection.WEST,ForgeDirection.UP,ForgeDirection.DOWN,ForgeDirection.SOUTH,ForgeDirection.NORTH};check(n.connections.length==6,"six constructor connections");for(int i=0;i<6;i++){DirPos d=n.connections[i];ForgeDirection f=order[i];check(d.getDir()==f&&d.getX()==a[0]+f.offsetX&&d.getY()==a[1]+f.offsetY&&d.getZ()==a[2]+f.offsetZ,"ordered adjacent coordinates including int wrap");}P p=new P();p.setPos(c.getPos());Vec3d d=p.getDebugParticlePosMK2();check(d.x==a[0]+0.5&&d.y==(double)(a[1]+1)&&d.z==a[2]+0.5,"debug position preserves integer Y overflow");}
 }
 public static void main(String[] args){Bootstrap.register();arithmetic();W w=new W();direct(w);registration(w);cache(w);coordinates();System.out.println("PASS: "+checks+" whole-interface/native tile-world checks; controlled NTM topology, debug packets disabled as production");}
}
