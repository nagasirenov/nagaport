import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;

/** Load the complete scoped plugin graph with no NEI jar and reject NEI references. */
public final class JeiWithoutNeiClassLoaderTest {
    private static final class WithoutNei extends URLClassLoader {
        int forbiddenRequests;
        WithoutNei(URL[] urls) { super(urls, null); }
        @Override protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.startsWith("codechicken.nei.")) {
                forbiddenRequests++;
                throw new ClassNotFoundException("NEI is deliberately absent: " + name);
            }
            return super.loadClass(name, resolve);
        }
    }
    public static void main(String[] args) throws Exception {
        String[] paths = args[0].split(java.util.regex.Pattern.quote(File.pathSeparator));
        URL[] urls = new URL[paths.length];
        for (int i = 0; i < paths.length; i++) urls[i] = new File(paths[i]).toURI().toURL();
        try (WithoutNei loader = new WithoutNei(urls)) {
            // Negative control: the optional adapter must really fail here. This
            // would catch the old plugin's indirect IConfigureNEI dependency.
            try {
                Class.forName("com.hbm.main.NEIConfig", true, loader);
                throw new AssertionError("NEI adapter unexpectedly loads without NEI");
            } catch (NoClassDefFoundError expected) {
                if (!expected.getMessage().contains("codechicken/nei/api/IConfigureNEI")) throw expected;
            }
            if (loader.forbiddenRequests == 0) throw new AssertionError("Negative control did not request NEI");
            loader.forbiddenRequests = 0;
            try {
                Class.forName("JeiOptionalBrowserNativeTest", true, loader).getMethod("main", String[].class)
                        .invoke(null, (Object) new String[] {"no-nei"});
            } catch (InvocationTargetException failure) {
                throw new AssertionError("No-NEI plugin execution failed", failure.getCause());
            }
            if (loader.forbiddenRequests != 0) throw new AssertionError("Plugin graph requested optional NEI classes");
            System.out.println("PASS: isolated no-NEI classloader; negative adapter control fails as expected;"
                    + " complete scoped plugin/helper execution requests zero NEI classes.");
        }
    }
}
