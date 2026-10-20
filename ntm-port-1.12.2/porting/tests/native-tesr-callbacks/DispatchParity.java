import java.lang.reflect.*;
import java.util.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.client.renderer.tileentity.*;
/** Bounded callback fixtures, NOT substituted production renderer bodies. Genuine engine dispatchers. */
public final class DispatchParity {
 static int checks; static Object tile; static double x,y,z; static float partial,alpha; static int stage,calls;
 static void check(boolean c,String message){checks++;if(!c)throw new AssertionError(message);}
 static void record(Object t,double a,double b,double c,float f){tile=t;x=a;y=b;z=c;partial=f;calls++;}
 public static class OldTile extends aor {} public static class OldChild extends OldTile {}
 public static class OldRenderer extends bml { @Override public void a(aor t,double a,double b,double c,float f){record(t,a,b,c,f);} }
 public static class NewTile extends TileEntity {} public static class NewChild extends NewTile {}
 public static class NewRenderer extends TileEntitySpecialRenderer<TileEntity> {
  @Override public void render(TileEntity t,double a,double b,double c,float f,int destruction,float opacity){record(t,a,b,c,f);stage=destruction;alpha=opacity;}
 }
 public static class RawRenderer extends TileEntitySpecialRenderer {
  @Override public void render(TileEntity t,double a,double b,double c,float f,int destruction,float opacity){record(t,a,b,c,f);stage=destruction;alpha=opacity;}
 }
 public static class InheritedRenderer extends RawRenderer {}
 static void values(Object t,double a,double b,double c,float f){check(tile==t&&x==a&&y==b&&z==c&&Float.floatToIntBits(partial)==Float.floatToIntBits(f),"tile, coordinates and partial tick survive virtual dispatch");}
 public static void main(String[] ignored)throws Exception {
  bmk old=bmk.a;Field map=bmk.class.getDeclaredField("m");map.setAccessible(true);((Map)map.get(old)).put(OldTile.class,new OldRenderer());
  TileEntityRendererDispatcher current=TileEntityRendererDispatcher.instance;
  for(TileEntitySpecialRenderer renderer:new TileEntitySpecialRenderer[]{new NewRenderer(),new RawRenderer(),new InheritedRenderer()}){
   current.renderers.put(NewTile.class,renderer);current.renderers.remove(NewChild.class);
   for(boolean child:new boolean[]{false,true})for(float f:new float[]{0,.125F,.5F,1}){
    OldTile ot=child?new OldChild():new OldTile();TileEntity nt=child?new NewChild():new NewTile();
    double a=-19.75,b=0,c=183.125;int before=calls;old.a(ot,a,b,c,f);values(ot,a,b,c,f);check(calls==before+1,"legacy callback once");
    current.render(nt,a,b,c,f);values(nt,a,b,c,f);check(stage==-1&&alpha==1,"genuine native five-argument convenience supplies ordinary stage -1 and alpha 1");
    current.render(nt,a,b,c,f,.375F);values(nt,a,b,c,f);check(stage==-1&&alpha==.375F,"native opacity overload forwards requested alpha");
    for(int d:new int[]{-1,0,5,9}){current.render(nt,a,b,c,f,d,.625F);values(nt,a,b,c,f);check(stage==d&&alpha==.625F,"native destruction and alpha forwarded without fabrication");}
   }
  }
  System.out.println("PASS "+checks+" genuine original/native dispatcher checks; typed, raw, inherited callback fixtures; no production body or visual certification");
 }
}
