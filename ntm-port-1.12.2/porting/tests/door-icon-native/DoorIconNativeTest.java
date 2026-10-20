package com.hbm.render.icon;
import java.net.*;
import java.lang.reflect.*;
import java.io.File;
import java.util.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
public final class DoorIconNativeTest {
    static int checks;
    static void check(boolean pass,String what){checks++;if(!pass)throw new AssertionError(what);}
    static void same(float a,float b){check(Float.floatToRawIntBits(a)==Float.floatToRawIntBits(b),"float bits "+a+" vs "+b);}
    static class Sprite extends TextureAtlasSprite {
        float loU,hiU,loV,hiV;
        Sprite(){super("hbm:door_metal_upper");}
        public float getMinU(){return loU;} public float getMaxU(){return hiU;}
        public float getMinV(){return loV;} public float getMaxV(){return hiV;}
        public int getIconWidth(){return 32;} public int getIconHeight(){return 64;}
    }
    public static void main(String[] args)throws Exception {
        URLClassLoader loader=new URLClassLoader(new URL[]{new File(args[0]).toURI().toURL()},null);
        Class<?> oldIcon=loader.loadClass("rf"),oldFlip=loader.loadClass("re");
        Sprite sprite=new Sprite();IIcon source=new LegacyTextureAtlasSprite(sprite);
        Object delegate=java.lang.reflect.Proxy.newProxyInstance(loader,new Class<?>[]{oldIcon},(proxy,method,values)->{
            String n=method.getName();
            if(n.equals("a")&&method.getParameterCount()==0)return source.getIconWidth();
            if(n.equals("b")&&method.getParameterCount()==0)return source.getIconHeight();
            if(n.equals("c"))return source.getMinU();if(n.equals("d"))return source.getMaxU();
            if(n.equals("e"))return source.getMinV();if(n.equals("f"))return source.getMaxV();
            if(n.equals("g"))return source.getIconName();
            throw new AssertionError("Unexpected source interpolation delegation "+method);
        });
        Object old=oldFlip.getConstructor(oldIcon,boolean.class,boolean.class).newInstance(delegate,true,false);
        HorizontallyFlippedIcon actual=new HorizontallyFlippedIcon(source);
        Random random=new Random(1039);
        for(int atlas=0;atlas<64;atlas++){
            sprite.loU=random.nextFloat();sprite.hiU=random.nextFloat();sprite.loV=random.nextFloat();sprite.hiV=random.nextFloat();
            same((Float)oldFlip.getMethod("c").invoke(old),actual.getMinU());same((Float)oldFlip.getMethod("d").invoke(old),actual.getMaxU());
            same((Float)oldFlip.getMethod("e").invoke(old),actual.getMinV());same((Float)oldFlip.getMethod("f").invoke(old),actual.getMaxV());
            for(double coordinate:new double[]{-256,-16,-.1,-0.0,0,0.1,1,7.33333333333333,15.999999999,16,32,256,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NaN}){
                same((Float)oldFlip.getMethod("a",double.class).invoke(old,coordinate),actual.getInterpolatedU(coordinate));
                same((Float)oldFlip.getMethod("b",double.class).invoke(old,coordinate),actual.getInterpolatedV(coordinate));
            }
        }
        check(actual.getIconWidth()==(Integer)oldFlip.getMethod("a").invoke(old),"width");
        check(actual.getIconHeight()==(Integer)oldFlip.getMethod("b").invoke(old),"height");
        check(actual.getIconName().equals(oldFlip.getMethod("g").invoke(old)),"name");
        DoorIconBinding door=new DoorIconBinding();List<String> registered=new ArrayList<>();
        door.registerBlockIcons(name->{registered.add(name);return source;});
        check(registered.equals(Arrays.asList("hbm:door_metal_upper","hbm:door_metal_lower")),"unchanged registration order/names");
        check(door.field_150017_a[0]==source&&door.field_150016_b[0]==source,"original unflipped identities");
        for(IIcon icon:new IIcon[]{door.field_150017_a[1],door.field_150016_b[1]}){same(icon.getMinU(),source.getMaxU());same(icon.getMaxU(),source.getMinU());same(icon.getMinV(),source.getMinV());same(icon.getMaxV(),source.getMaxV());}
        loader.close();System.out.println("PASS "+checks+" native atlas/actual original bytecode mirror checks and exact door registration hook; whole door lifecycle and in-game rendering not certified.");
    }
}
