import java.lang.reflect.*;import java.util.*;import net.minecraft.client.gui.inventory.GuiContainer;import sun.misc.Unsafe;
public class NativeHitRegionTest {
 public static class Current extends GuiContainer {public Current(){super(null);}protected void drawGuiContainerBackgroundLayer(float f,int x,int y){} public boolean hit(int a,int b,int c,int d,int e,int f){return isPointInRegion(a,b,c,d,e,f);}public void origin(int a,int b){guiLeft=a;guiTop=b;}}
 public static class Original extends bex {public Original(){super(null);}protected void a(float f,int x,int y){} public boolean hit(int a,int b,int c,int d,int e,int f){return c(a,b,c,d,e,f);}public void origin(int a,int b){i=a;r=b;}}
 static int checks;static void check(boolean x){checks++;if(!x)throw new AssertionError("hit region mismatch");}
 public static void main(String[]args)throws Exception{Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);Unsafe u=(Unsafe)f.get(null);Current n=(Current)u.allocateInstance(Current.class);Original o=(Original)u.allocateInstance(Original.class);
 int[] vals={Integer.MIN_VALUE,Integer.MIN_VALUE+1,-10000,-1,0,1,15,16,17,9999,Integer.MAX_VALUE-1,Integer.MAX_VALUE};
 for(int left:vals)for(int top:vals){n.origin(left,top);o.origin(left,top);for(int x:vals)for(int y:vals){check(n.hit(x,y,16,16,left+x,top+y)==o.hit(x,y,16,16,left+x,top+y));for(int delta:new int[]{-2,-1,0,15,16,17})check(n.hit(x,y,16,16,left+x+delta,top+y+delta)==o.hit(x,y,16,16,left+x+delta,top+y+delta));}}
 Random r=new Random(0x146978c);for(int j=0;j<20000;j++){int left=r.nextInt(),top=r.nextInt(),x=r.nextInt(),y=r.nextInt(),w=r.nextInt(),h=r.nextInt(),mx=r.nextInt(),my=r.nextInt();n.origin(left,top);o.origin(left,top);check(n.hit(x,y,w,h,mx,my)==o.hit(x,y,w,h,mx,my));}
 n.origin(10,20);check(n.hit(0,0,16,16,9,19));check(n.hit(0,0,16,16,26,36));check(!n.hit(0,0,16,16,27,36));check(!n.hit(0,0,16,16,26,37));check(!n.hit(0,0,16,16,8,19));
 System.out.println("PASS "+checks+" actual original/native hit-region checks including signed overflow, negative sizes and one-pixel boundary; no GUI draw/input-loop claim");}
}
