import java.util.*;import net.minecraft.util.math.BlockPos;import net.minecraft.world.World;
public class ProductionMethodTest {
 static class Recording extends AirRemovalTest.NativeWorld {
  List<BlockPos> removed=new ArrayList<>();public boolean setBlockToAir(BlockPos p){removed.add(p);return true;}
 }
 public static void main(String[] a){net.minecraft.init.Bootstrap.register();int checks=0;
  for(int x:new int[]{0,-30000000,29999999,Integer.MIN_VALUE,Integer.MAX_VALUE})for(int y:new int[]{0,64,255,Integer.MAX_VALUE})for(int height:new int[]{0,-1,7,Integer.MAX_VALUE}){
   Recording w=new Recording();ProductionPlatform p=new ProductionPlatform();p.setWorld(w);p.setPos(new BlockPos(x,y,-x));p.deletePlatform(height);List<BlockPos> expected=new ArrayList<>();
   for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++){if(dx==0&&dz==0)continue;if(Math.abs(dx)==4&&Math.abs(dz)==4)continue;int xx=x+dx,yy=y+height,zz=-x+dz;if(AirRemovalTest.valid(xx,yy,zz))expected.add(new BlockPos(xx,yy,zz));}
   if(!w.removed.equals(expected))throw new AssertionError("complete production method coordinate/order/bounds");checks++;
  }System.out.println("PASS: "+checks+" complete actual deletePlatform method scenarios (not whole enclosing class)");
 }
}
