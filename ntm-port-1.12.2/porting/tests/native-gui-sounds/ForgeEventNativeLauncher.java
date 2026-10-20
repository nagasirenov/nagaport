import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

/**
 * Runs an isolated native test with Forge's genuine event-subscription transformer.
 * Invoke with the test's main class followed by its arguments. The test initializes
 * Minecraft/Forge itself, inside this loader, before registering its subscribers.
 * No Minecraft/Forge class may be initialized by this outer launcher.
 */
public final class ForgeEventNativeLauncher {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            throw new IllegalArgumentException("Usage: ForgeEventNativeLauncher <test main class> [arguments]");
        }
        List<URL> urls = new ArrayList<>();
        for (String path : System.getProperty("java.class.path").split(
                java.util.regex.Pattern.quote(File.pathSeparator))) {
            urls.add(new File(path).toURI().toURL());
        }
        Launch.minecraftHome = new File(".");
        Launch.assetsDir = new File(".");
        Launch.blackboard = new HashMap<>();
        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        try (LaunchClassLoader loader = new LaunchClassLoader(urls.toArray(new URL[0]))) {
            Launch.classLoader = loader;
            // The transformer itself must load without recursively transforming itself.
            // It must still belong to this loader, because it resolves event parents
            // through its own classloader while constructing native listener lists.
            loader.addTransformerExclusion("net.minecraftforge.fml.common.asm.transformers.");
            loader.registerTransformer("net.minecraftforge.fml.common.asm.transformers.EventSubscriptionTransformer");
            if (loader.getTransformers().size() != 1) {
                throw new AssertionError("Genuine Forge event transformer did not register");
            }
            Thread.currentThread().setContextClassLoader(loader);
            try {
                Class.forName(args[0], true, loader).getMethod("main", String[].class)
                        .invoke(null, (Object) Arrays.copyOfRange(args, 1, args.length));
            } catch (InvocationTargetException failure) {
                throw new AssertionError("Native event test failed: " + args[0], failure.getCause());
            } finally {
                Thread.currentThread().setContextClassLoader(previous);
            }
        }
    }
}
