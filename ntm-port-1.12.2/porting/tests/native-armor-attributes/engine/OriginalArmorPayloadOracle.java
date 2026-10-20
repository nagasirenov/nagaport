import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.*;
import com.google.common.collect.Multimap;
import com.google.gson.GsonBuilder;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import sun.misc.Unsafe;

/** Loads complete original mod classfiles, translating symbols only to the actual old client. */
public final class OriginalArmorPayloadOracle {
    static final class OriginalLoader extends URLClassLoader {
        final ZipFile mod;
        final Map<String, byte[]> forge = new HashMap<>();
        final Map<String, String> classes = new HashMap<>(), methods = new HashMap<>(), fields = new HashMap<>();
        final Remapper names = new Remapper() {
            @Override public String map(String name) { return classes.containsKey(name) ? classes.get(name) : name; }
            @Override public String mapMethodName(String owner, String name, String desc) { return name.startsWith("func_") && methods.containsKey(name) ? methods.get(name) : name; }
            @Override public String mapFieldName(String owner, String name, String desc) { return name.startsWith("field_") && fields.containsKey(name) ? fields.get(name) : name; }
        };
        OriginalLoader(String legacy, String modPath, String forgePath) throws Exception {
            super(new URL[] { new File(legacy).toURI().toURL() }, OriginalArmorPayloadOracle.class.getClassLoader());
            mod = new ZipFile(modPath);
            try (ZipFile userdev = new ZipFile(forgePath)) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(userdev.getInputStream(userdev.getEntry("conf/packaged.srg")), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] words = line.split(" ");
                        if (words[0].equals("CL:")) classes.put(words[2], words[1]);
                        if (words[0].equals("MD:")) methods.put(simple(words[3]), simple(words[1]));
                        if (words[0].equals("FD:")) fields.put(simple(words[2]), simple(words[1]));
                    }
                }
                try (ZipInputStream zip = new ZipInputStream(userdev.getInputStream(userdev.getEntry("binaries.jar")))) {
                    ZipEntry entry;
                    while ((entry = zip.getNextEntry()) != null) if (entry.getName().endsWith(".class")) forge.put(entry.getName(), read(zip));
                }
            }
        }
        @Override protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.startsWith("com.hbm.") || name.startsWith("api.hbm.") || name.startsWith("cpw.mods.") || name.startsWith("net.minecraftforge.")) {
                Class<?> found = findLoadedClass(name);
                if (found == null) found = findClass(name);
                if (resolve) resolveClass(found);
                return found;
            }
            return super.loadClass(name, resolve);
        }
        @Override protected Class<?> findClass(String name) throws ClassNotFoundException {
            String path = name.replace('.', '/') + ".class";
            ZipEntry entry = mod.getEntry(path);
            byte[] bytes = forge.get(path);
            try {
                if (entry != null) try (InputStream stream = mod.getInputStream(entry)) { bytes = read(stream); }
                if (bytes == null) return super.findClass(name);
                ClassWriter writer = new ClassWriter(0);
                new ClassReader(bytes).accept(new ClassRemapper(writer, names), 0);
                byte[] mapped = writer.toByteArray();
                return defineClass(name, mapped, 0, mapped.length);
            } catch (IOException error) { throw new ClassNotFoundException(name, error); }
        }
        @Override public void close() throws IOException { mod.close(); super.close(); }
    }
    static String simple(String name) { return name.substring(name.lastIndexOf('/') + 1); }
    static byte[] read(InputStream stream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(); byte[] buffer = new byte[8192]; int count;
        while ((count = stream.read(buffer)) >= 0) out.write(buffer, 0, count);
        return out.toByteArray();
    }
    static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe"); unsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe) unsafeField.get(null);
        List<Map<String, Object>> payloads = new ArrayList<>();
        try (OriginalLoader loader = new OriginalLoader(args[0], args[1], args[2])) {
            loader.loadClass("kl").getMethod("b").invoke(null);
            Class<?> oldItem = loader.loadClass("adb"), oldArmor = loader.loadClass("abb");
            java.lang.reflect.Method dispatch = oldItem.getMethod(loader.methods.get("func_111205_h"));
            Field armorType = oldArmor.getField(loader.fields.get("field_77881_a"));
            armorType.setAccessible(true);
            String[] owners = {"ArmorDiesel", "ArmorDesh", "ArmorLiquidator"};
            String[] uuids = {"e572caf4-3e65-4152-bc79-c4d4048cbd29", "bed30902-8a6a-4769-9f65-2a9b67469fff", "baebf7b3-1eda-4a14-b233-068e2493e9a2", "28016c1b-d992-4324-9409-a9f9f0ffb85c"};
            for (String owner : owners) {
                Class<?> originalClass = loader.loadClass("com.hbm.items.armor." + owner);
                check(originalClass.getClassLoader() == loader && oldArmor.isAssignableFrom(originalClass), "actual old ItemArmor ancestry " + owner);
                Object instance = unsafe.allocateInstance(originalClass);
                for (int index = 0; index < 4; index++) {
                    armorType.setInt(instance, index);
                    Multimap<?, ?> map = (Multimap<?, ?>) dispatch.invoke(instance);
                    check(map.size() == (owner.equals("ArmorLiquidator") ? 2 : 1), "original exact custom map size " + owner);
                    for (Map.Entry<?, ?> entry : map.entries()) {
                        Object value = entry.getValue(); Class<?> type = value.getClass();
                        String id = type.getMethod(loader.methods.get("func_111167_a")).invoke(value).toString();
                        String name = (String) type.getMethod(loader.methods.get("func_111166_b")).invoke(value);
                        double amount = (Double) type.getMethod(loader.methods.get("func_111164_d")).invoke(value);
                        int operation = (Integer) type.getMethod(loader.methods.get("func_111169_c")).invoke(value);
                        String attribute = (String) entry.getKey();
                        check(id.equals(uuids[index]) && name.equals("Armor modifier"), "original name/UUID " + owner + '/' + index);
                        boolean speed = attribute.equals("generic.movementSpeed");
                        check(speed || attribute.equals("generic.knockbackResistance"), "original attribute key");
                        double expected = owner.equals("ArmorDiesel") ? 0.25 : owner.equals("ArmorDesh") ? -0.025 : speed ? -0.1 : 100;
                        int expectedOperation = owner.equals("ArmorLiquidator") && !speed ? 0 : 1;
                        check(amount == expected && operation == expectedOperation, "original amount/operation " + owner);
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("owner", owner); row.put("armor_type", index); row.put("attribute", attribute); row.put("uuid", id);
                        row.put("name", name); row.put("amount", amount); row.put("operation", operation); payloads.add(row);
                    }
                }
            }
        }
        Collections.sort(payloads, Comparator.comparing(row -> row.get("owner") + "/" + row.get("armor_type") + "/" + row.get("attribute")));
        try (Writer output = new OutputStreamWriter(new FileOutputStream(args[3]), StandardCharsets.UTF_8)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(payloads, output);
        }
        System.out.println("PASS complete original Diesel/Desh/Liquidator classfiles, actual old Item.k virtual dispatch: 12 maps / 16 exact payload tuples");
        System.out.println("SCOPE: symbols only remapped from supplied SRG to official old client names; every original class/method body retained. Unsafe bypasses unrelated constructors; no complete mod lifecycle startup/gameplay claimed.");
    }
}
