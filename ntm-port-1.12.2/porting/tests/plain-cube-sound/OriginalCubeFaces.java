import java.io.File;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import sun.misc.Unsafe;
/** Executes the unmodified original RenderBlocks six-face bytecode. */
final class OriginalCubeFaces {
 static final class LegacyFaces implements AutoCloseable {
  final URLClassLoader loader;
  final Class<?> renderer,block,iconType,tessType;
  final Object render,tess;
  final Field raw,count,ptr;
  static Field field(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
  LegacyFaces(File jar)throws Exception{
   loader=new URLClassLoader(new java.net.URL[]{jar.toURI().toURL()},OriginalCubeFaces.class.getClassLoader());
   renderer=Class.forName("blm",true,loader);block=Class.forName("aji",true,loader);iconType=Class.forName("rf",true,loader);tessType=Class.forName("bmh",true,loader);
   Unsafe unsafe=(Unsafe)field(Unsafe.class,"theUnsafe").get(null);render=unsafe.allocateInstance(renderer);tess=tessType.getField("a").get(null);raw=field(tessType,"f");count=field(tessType,"g");ptr=field(tessType,"p");
  }
  void plainBlockSemantics() throws Exception {
   Class<?> material=Class.forName("awt",true,loader);
   java.lang.reflect.Constructor<?> constructor=block.getDeclaredConstructor(material);constructor.setAccessible(true);
   Object owner=constructor.newInstance(material.getField("e").get(null));
   java.lang.reflect.Method setter=block.getDeclaredMethod("d",String.class);setter.setAccessible(true);
   String name=new String("hbm:ore_uranium_scorched");
   if(setter.invoke(owner,name)!=owner || field(block,"d").get(owner)!=name)throw new AssertionError("Original setter identity/string reference");
   Class<?> sound=Class.forName("ajm",true,loader);java.lang.reflect.Method setSound=block.getDeclaredMethod("a",sound);setSound.setAccessible(true);
   String[] soundFields={"l","k","m","f"};String[] breaks={"dig.cloth","dig.glass","dig.sand","dig.wood"};
   net.minecraft.block.SoundType[] nativeSounds={net.minecraft.block.SoundType.CLOTH,net.minecraft.block.SoundType.GLASS,net.minecraft.block.SoundType.SAND,net.minecraft.block.SoundType.WOOD};
   for(int i=0;i<soundFields.length;i++) {
    Object value=block.getField(soundFields[i]).get(null);
    if(setSound.invoke(owner,value)!=owner || field(block,"H").get(owner)!=value)throw new AssertionError("Original sound setter identity/reference");
    if(!sound.getMethod("a").invoke(value).equals(breaks[i]))throw new AssertionError("Original sound category");
    if((Float)sound.getMethod("c").invoke(value)!=nativeSounds[i].getVolume()||(Float)sound.getMethod("d").invoke(value)!=nativeSounds[i].getPitch())throw new AssertionError("Original/native sound volume/pitch");
   }
   if(setSound.invoke(owner,new Object[]{null})!=owner || field(block,"H").get(owner)!=null)throw new AssertionError("Original null sound reference");
   java.lang.reflect.Method setName=block.getMethod("c",String.class);
   java.lang.reflect.Method setTab=block.getMethod("a",Class.forName("abt",true,loader));
   if(setName.invoke(owner,"closure")!=owner || setTab.invoke(owner,new Object[]{null})!=owner)throw new AssertionError("Original preceding setters return same receiver");
   System.out.println("PASS actual original sound setter/null reference identity,preceding setter closure,four sound categories and native volume/pitch.");
   Object icon=java.lang.reflect.Proxy.newProxyInstance(loader,new Class<?>[]{iconType},(p,m,a)->null);
   field(block,"L").set(owner,icon);
   for(int meta=0;meta<16;meta++) {
    if(!block.getMethod("i",int.class).invoke(owner,meta).equals(0xffffff))throw new AssertionError("Original item color");
    for(int side=0;side<6;side++)if(block.getMethod("a",int.class,int.class).invoke(owner,side,meta)!=icon)throw new AssertionError("Original face/meta icon identity");
   }
   for(int meta:new int[]{Integer.MIN_VALUE,-32769,-32768,-1,16,32767,32768,65535,65536,Integer.MAX_VALUE}) {
    if(!block.getMethod("i",int.class).invoke(owner,meta).equals(0xffffff))throw new AssertionError("Original raw item color");
    for(int side=0;side<6;side++)if(block.getMethod("a",int.class,int.class).invoke(owner,side,meta)!=icon)throw new AssertionError("Original raw face/meta icon identity");
   }
   if(!block.getMethod("d",Class.forName("ahl",true,loader),int.class,int.class,int.class).invoke(owner,null,0,0,0).equals(0xffffff))throw new AssertionError("Original world color");
   if(!block.getMethod("b").invoke(owner).equals(0) || !block.getMethod("c").invoke(owner).equals(true) || !block.getMethod("d").invoke(owner).equals(true))throw new AssertionError("Original normal opaque cube owner");
   System.out.println("PASS actual original Block setter return/string identity, 156 side/meta icons including raw int edges,26 item colors, white world color and normal opaque cube methods.");
  }
  double[][] standardFace(double[] bounds,int side,final TextureAtlasSprite sprite)throws Exception{
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
