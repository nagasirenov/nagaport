import java.io.*;
import java.net.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.jar.*;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.discovery.*;
import net.minecraftforge.fml.common.discovery.asm.ASMModParser;
import net.minecraftforge.fml.common.asm.transformers.ModAPITransformer;

/** Apply Forge's real optional-API transformer to AE2, as the actual mod loader does.
 * This is not a replacement AE2/Forge implementation or a patched dependency jar.
 */
public final class AE2NativeLauncher {
    private static final class NativeLoader extends URLClassLoader {
        final ModAPITransformer optional;
        NativeLoader(URL[] urls,ModAPITransformer optional){super(urls,AE2NativeLauncher.class.getClassLoader());this.optional=optional;}
        @Override protected synchronized Class<?> loadClass(String name,boolean resolve) throws ClassNotFoundException {
            if(name.startsWith("appeng.")||name.startsWith("com.hbm.")||name.equals("AE2StorageNativeTest")||name.startsWith("AE2StorageNativeTest$")){
                Class<?> type=findLoadedClass(name);if(type==null)type=findClass(name);if(resolve)resolveClass(type);return type;
            }
            return super.loadClass(name,resolve);
        }
        @Override protected Class<?> findClass(String name) throws ClassNotFoundException {
            String resource=name.replace('.','/')+".class";
            try(InputStream in=getResourceAsStream(resource)){
                if(in==null)throw new ClassNotFoundException(name);
                ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int count;
                while((count=in.read(buffer))!=-1)bytes.write(buffer,0,count);
                byte[] data=optional.transform(name,name,bytes.toByteArray());
                return defineClass(name,data,0,data.length);
            }catch(IOException e){throw new ClassNotFoundException(name,e);}
        }
    }
    public static void main(String[] args) throws Exception {
        net.minecraft.init.Bootstrap.register();
        net.minecraftforge.fml.common.FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
        ModMetadata meta=new ModMetadata();meta.modId="appliedenergistics2";meta.name="AE2 native test bootstrap";meta.version="rv6-stable-7";
        Loader.instance().setupTestHarness(new DummyModContainer(meta));
        String[] paths=System.getProperty("java.class.path").split(java.util.regex.Pattern.quote(File.pathSeparator));
        List<URL> urls=new ArrayList<>();ASMDataTable table=new ASMDataTable();
        for(String path:paths){
            File file=new File(path);urls.add(file.toURI().toURL());
            if(!file.getName().startsWith("appliedenergistics2-")||!file.getName().endsWith(".jar"))continue;
            ModCandidate candidate=new ModCandidate(file,file,ContainerType.JAR);
            candidate.explore(table);
        }
        net.minecraftforge.fml.common.ModAPIManager.INSTANCE.registerDataTableAndParseAPI(table);
        ModAPITransformer optional=new ModAPITransformer();optional.initTable(table);
        try(NativeLoader loader=new NativeLoader(urls.toArray(new URL[0]),optional)){
            java.util.concurrent.atomic.AtomicReference<Throwable> failure=new java.util.concurrent.atomic.AtomicReference<>();
            Thread server=new Thread(net.minecraftforge.fml.common.thread.SidedThreadGroups.SERVER,()->{
                try{Class.forName("AE2StorageNativeTest",true,loader).getMethod("main",String[].class).invoke(null,(Object)new String[0]);}
                catch(Throwable e){failure.set(e instanceof InvocationTargetException ? e.getCause() : e);}
            },"AE2 native storage server test");
            server.setContextClassLoader(loader);server.start();server.join();
            if(failure.get()!=null)throw new AssertionError("Native AE2 test failed",failure.get());
        }
    }
}
