import com.google.gson.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import sun.misc.Unsafe;

/** Executes cached unchanged 1.7.10 bytecode for default, category and pack merge rules. */
public final class LegacyLoopSoundOracle {
    static int checks;
    static void check(boolean ok,String what){checks++;if(!ok)throw new AssertionError(what);}
    static Object instance(Class<?> c)throws Exception{Constructor<?> ctor=c.getDeclaredConstructor();ctor.setAccessible(true);return ctor.newInstance();}
    public static void main(String[] args)throws Exception{
        try(URLClassLoader loader=new URLClassLoader(new URL[]{new File(args[0]).toURI().toURL()},LegacyLoopSoundOracle.class.getClassLoader())){
            Class<?> list=Class.forName("bsp",true,loader),serializer=Class.forName("bss",true,loader),category=Class.forName("w",true,loader),location=Class.forName("bqx",true,loader),handler=Class.forName("btp",true,loader),registry=Class.forName("btt",true,loader);
            Gson gson=new GsonBuilder().registerTypeAdapter(list,instance(serializer)).create();Method getCategory=list.getMethod("c"),getName=category.getMethod("a");
            JsonObject original;try(Reader reader=new InputStreamReader(new FileInputStream(args[1]),StandardCharsets.UTF_8)){original=new JsonParser().parse(reader).getAsJsonObject();}
            for(Map.Entry<String,JsonElement> e:original.entrySet()){
                Object parsed=gson.fromJson(e.getValue(),list);String name=(String)getName.invoke(getCategory.invoke(parsed));check(name.equals(e.getValue().getAsJsonObject().get("category").getAsString()),"original declared category "+e.getKey());
            }
            check(original.size()==408,"all original 408 sound entries");Object omitted=gson.fromJson("{\"sounds\":[]}",list);check(getName.invoke(getCategory.invoke(omitted)).equals("master"),"original missing category defaults MASTER");
            boolean invalid=false;try{gson.fromJson("{\"category\":\"bogus\"}",list);}catch(RuntimeException expected){invalid=true;}check(invalid,"unknown legacy category is rejected");
            Field uf=Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);Unsafe unsafe=(Unsafe)uf.get(null);Object h=unsafe.allocateInstance(handler);Field rf=handler.getDeclaredField("e");rf.setAccessible(true);rf.set(h,instance(registry));
            Method load=handler.getDeclaredMethod("a",location,list);load.setAccessible(true);Method get=handler.getMethod("a",location);Object id=location.getConstructor(String.class,String.class).newInstance("hbm","probe");
            String[] definitions={"{\"category\":\"block\",\"sounds\":[]}","{\"category\":\"hostile\",\"sounds\":[]}","{\"category\":\"record\",\"replace\":true,\"sounds\":[]}","{\"replace\":true,\"sounds\":[]}"};String[] expected={"block","block","record","master"};
            for(int i=0;i<definitions.length;i++){load.invoke(h,id,gson.fromJson(definitions[i],list));Object accessor=get.invoke(h,id);Object cat=accessor.getClass().getMethod("d").invoke(accessor);check(getName.invoke(cat).equals(expected[i]),"original real handler merge/replace stage "+i);}
            Class<?> positioned=Class.forName("bsk",true,loader),record=Class.forName("bso",true,loader);Object sound=record.getMethod("a",location).invoke(null,id);
            Field volume=positioned.getDeclaredField("b"),pitch=positioned.getDeclaredField("c");volume.setAccessible(true);pitch.setAccessible(true);volume.setFloat(sound,-4.5F);pitch.setFloat(sound,.75F);
            check(positioned.getMethod("e").invoke(sound).equals(-4.5F),"original getter raw unclamped volume");check(positioned.getMethod("f").invoke(sound).equals(.75F),"original getter raw pitch independent of accessor");
            System.out.println("PASS legacy loop-sound bytecode oracle: "+checks+" checks; all 408 categories, omitted MASTER, unknown rejection, original actual handler append/replace and raw getters");
        }
    }
}
