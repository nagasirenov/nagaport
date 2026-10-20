import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.InvocationTargetException;

/** Real Forge entry point runs with every appeng class absent and explicitly denied. */
public final class AE2WithoutModClassLoaderTest {
    private static final class WithoutAE2 extends URLClassLoader {
        int forbidden;
        WithoutAE2(URL[] urls) { super(urls, null); }
        @Override protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.startsWith("appeng.")) { forbidden++; throw new ClassNotFoundException(name); }
            return super.loadClass(name, resolve);
        }
    }
    public static void main(String[] args) throws Exception {
        if (args.length == 1) {
            net.minecraft.init.Bootstrap.register();
            net.minecraftforge.fml.common.ModMetadata meta = new net.minecraftforge.fml.common.ModMetadata();
            meta.modId="ntm_native_test";meta.name="Native AE2 absent test";meta.version="1";
            net.minecraftforge.fml.common.Loader.instance().setupTestHarness(new net.minecraftforge.fml.common.DummyModContainer(meta));
            if (net.minecraftforge.fml.common.Loader.isModLoaded("appliedenergistics2")) throw new AssertionError("AE2 should be absent");
            com.hbm.handler.ae2.AE2CompatHandler.init();
            return;
        }
        String[] paths=System.getProperty("java.class.path").split(java.util.regex.Pattern.quote(File.pathSeparator));
        URL[] urls=new URL[paths.length];for(int i=0;i<paths.length;i++)urls[i]=new File(paths[i]).toURI().toURL();
        try(WithoutAE2 loader=new WithoutAE2(urls)) {
            try {Class.forName("com.hbm.handler.ae2.MassStorageMEInventory",true,loader);throw new AssertionError("Negative control loaded AE2 adapter");}
            catch(NoClassDefFoundError expected){if(loader.forbidden==0)throw expected;}
            loader.forbidden=0;
            try {Class.forName("AE2WithoutModClassLoaderTest",true,loader).getMethod("main",String[].class).invoke(null,(Object)new String[]{"inner"});}
            catch(InvocationTargetException e){throw new AssertionError("No-AE2 initialization failed",e.getCause());}
            if(loader.forbidden!=0)throw new AssertionError("Guarded entry point requested AE2 classes");
        }
        System.out.println("PASS no-AE2 classloader: native Forge loader guard executes; zero appeng loads; negative adapter control fails.");
    }
}
