import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import sun.misc.Unsafe;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Executes pinned actual Minecraft 1.7.10 face bytecode, independent of native geometry. */
public final class OriginalWandOracle {
 static final class LegacyFaces implements AutoCloseable {
  final URLClassLoader loader;
  final Class<?> renderer,block,iconType,tessType;
  final Object render,tess;
  final Field raw,count,ptr;
  static Field field(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
  LegacyFaces(File jar)throws Exception{
   loader=new URLClassLoader(new java.net.URL[]{jar.toURI().toURL()},OriginalWandOracle.class.getClassLoader());
   renderer=Class.forName("blm",true,loader);block=Class.forName("aji",true,loader);iconType=Class.forName("rf",true,loader);tessType=Class.forName("bmh",true,loader);
   Unsafe unsafe=(Unsafe)field(Unsafe.class,"theUnsafe").get(null);render=unsafe.allocateInstance(renderer);tess=tessType.getField("a").get(null);raw=field(tessType,"f");count=field(tessType,"g");ptr=field(tessType,"p");
  }
  double[][] standardFace(double[] bounds,int side,final TextureAtlasSprite sprite,boolean inside)throws Exception{
   field(renderer,"h").setBoolean(render,inside);
   String[] names={"i","k","m","j","l","n"};for(int i=0;i<6;i++)field(renderer,names[i]).setDouble(render,bounds[i]);
   Object icon=java.lang.reflect.Proxy.newProxyInstance(loader,new Class<?>[]{iconType},(p,m,a)->{
    if(a!=null&&a.length==1)return m.getName().equals("a")?sprite.getInterpolatedU((Double)a[0]):sprite.getInterpolatedV((Double)a[0]);
    switch(m.getName()){case "a":case "b":return 16;case "c":return sprite.getMinU();case "d":return sprite.getMaxU();case "e":return sprite.getMinV();case "f":return sprite.getMaxV();default:return sprite.getIconName();}
   });
   count.setInt(tess,0);ptr.setInt(tess,0);renderer.getMethod("abcdef".substring(side,side+1),block,double.class,double.class,double.class,iconType).invoke(render,null,0D,0D,0D,icon);
   if(count.getInt(tess)!=4)throw new AssertionError("Legacy face vertex count");int[] data=(int[])raw.get(tess);double[][] v=new double[4][5];for(int i=0;i<4;i++)for(int j=0;j<5;j++)v[i][j]=Float.intBitsToFloat(data[i*8+j]);return v;
  }
  public void close()throws Exception{loader.close();}
 }
}
