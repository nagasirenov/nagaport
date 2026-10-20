import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import net.minecraftforge.fml.relauncher.*;
import net.minecraftforge.fml.common.asm.transformers.SideTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Genuine Forge server stripping of the complete production provider interface.
 * Client-class denial models a dedicated server; no substitute platform classes.
 */
public final class ItemProviderSideNativeTest {
    static final String PROVIDER = "com.hbm.render.tileentity.IItemRendererProvider";
    static int checks;
    static void check(boolean value, String label) { if(!value) throw new AssertionError(label); checks++; }
    static final class ServerLoader extends ClassLoader {
        final byte[] bytes;
        ServerLoader(byte[] bytes) { super(ItemProviderSideNativeTest.class.getClassLoader()); this.bytes = bytes; }
        @Override protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if(name.startsWith("net.minecraft.client.") || name.equals("com.hbm.render.item.ItemRenderBase"))
                throw new ClassNotFoundException("Dedicated-server client boundary: " + name);
            if(name.equals(PROVIDER)) {
                Class<?> c = findLoadedClass(name);
                if(c == null) c = defineClass(name, bytes, 0, bytes.length);
                if(resolve) resolveClass(c);
                return c;
            }
            return super.loadClass(name, resolve);
        }
    }
    public static void main(String[] args) throws Exception {
        Field side = FMLLaunchHandler.class.getDeclaredField("side"); side.setAccessible(true); side.set(null, Side.SERVER);
        SideTransformer transformer = new SideTransformer();
        byte[] original = Files.readAllBytes(Paths.get(args[0]));
        ClassNode node = new ClassNode(); new ClassReader(original).accept(node, 0);
        boolean removed = false;
        for(MethodNode method : node.methods) if(method.name.equals("getRenderer")) {
            check(method.visibleAnnotations != null, "production factory has visible side annotation");
            removed = method.visibleAnnotations.removeIf(a -> a.desc.equals("Lnet/minecraftforge/fml/relauncher/SideOnly;"));
        }
        check(removed, "negative control removes only factory annotation");
        ClassWriter writer = new ClassWriter(0); node.accept(writer);
        byte[] unsafe = transformer.transform(PROVIDER, PROVIDER, writer.toByteArray());
        boolean failed = false;
        try { new ServerLoader(unsafe).loadClass(PROVIDER).getDeclaredMethods(); }
        catch(NoClassDefFoundError expected) { failed = true; }
        check(failed, "unannotated native renderer return type fails server reflection");
        byte[] safe = transformer.transform(PROVIDER, PROVIDER, original);
        Class<?> type = new ServerLoader(safe).loadClass(PROVIDER);
        Method[] methods = type.getDeclaredMethods();
        check(methods.length == 2, "server retains exactly two common provider methods");
        check(type.getMethod("getItemForRenderer").getReturnType().getName().equals("net.minecraft.item.Item"), "common single-item contract retained");
        check(type.getMethod("getItemsForRenderer").isDefault(), "common default array contract retained");
        Field transformerSide = SideTransformer.class.getDeclaredField("SIDE"); transformerSide.setAccessible(true); transformerSide.set(null, "CLIENT");
        ClassNode client = new ClassNode(); new ClassReader(transformer.transform(PROVIDER, PROVIDER, original)).accept(client, 0);
        check(client.methods.size() == 3, "client retains renderer factory and common methods");
        System.out.println("PASS " + checks + " genuine Forge side-transform boundary checks; not a dedicated-server launch or whole armor compile");
    }
}
