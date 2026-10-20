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
public final class OriginalToolPayloadOracle {
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
            super(new URL[] { new File(legacy).toURI().toURL() }, OriginalToolPayloadOracle.class.getClassLoader());
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
    static void field(Object target, String name, Object value) throws Exception {
        Class<?> owner = target.getClass(); Field f = null;
        while (owner != null) { try { f = owner.getDeclaredField(name); break; } catch (NoSuchFieldException e) { owner = owner.getSuperclass(); } }
        if (f == null) throw new NoSuchFieldException(name); f.setAccessible(true); f.set(target, value);
    }
    static List<Map<String,Object>> payload(Multimap<?,?> map, OriginalLoader loader) throws Exception {
        List<Map<String,Object>> result = new ArrayList<>();
        for (Map.Entry<?,?> e : map.entries()) {
            Object m=e.getValue(); Class<?> t=m.getClass(); Map<String,Object> row=new LinkedHashMap<>();
            row.put("attribute",e.getKey()); row.put("uuid",t.getMethod("a").invoke(m).toString()); row.put("name",t.getMethod("b").invoke(m)); row.put("amount",t.getMethod("d").invoke(m)); row.put("operation",t.getMethod("c").invoke(m));
            check(row.get("uuid").equals("cb3f55d3-645c-4f38-a497-9c13a33db5cf"),"exact old UUID");
            boolean movement=row.get("attribute").equals("generic.movementSpeed");
            check(movement||row.get("attribute").equals("generic.attackDamage"),"exact old key");
            check(row.get("operation").equals(movement?1:0),"exact old operation"); result.add(row);
        }
        Collections.sort(result,Comparator.comparing(x -> x.get("attribute").toString())); return result;
    }
    public static void main(String[] args) throws Exception {
        Field uf=Unsafe.class.getDeclaredField("theUnsafe"); uf.setAccessible(true); Unsafe unsafe=(Unsafe)uf.get(null);
        Map<String,Object> results=new LinkedHashMap<>();
        try (OriginalLoader loader=new OriginalLoader(args[0],args[1],args[2])) {
            loader.loadClass("kl").getMethod("b").invoke(null);
            Class<?> item=loader.loadClass("adb"), stackClass=loader.loadClass("add"); java.lang.reflect.Method dispatch=item.getMethod("k");
            for (String owner : new String[]{"com.hbm.items.tool.ItemSwordAbility", "com.hbm.items.weapon.GunB92", "com.hbm.items.weapon.ItemCrucible", "com.hbm.items.tool.ItemToolAbility"}) {
                try {
                    Class<?> c=loader.loadClass(owner); Object instance=unsafe.allocateInstance(c); check(c.getClassLoader()==loader,"original owner class loader");
                    if (owner.endsWith("GunB92")) {
                        Multimap<?,?> map=(Multimap<?,?>)dispatch.invoke(instance); check(map.size()==1,"gun original cardinality");
                        List<Map<String,Object>> rows=payload(map,loader); check(rows.get(0).get("amount").equals(3.5D)&&rows.get(0).get("name").equals("Weapon modifier"),"gun original exact amount/name"); results.put(owner,rows);
                    } else {
                        float configuredDamage=owner.endsWith("ItemCrucible")?5000F:250F; double configuredMovement=owner.endsWith("ItemCrucible")?1D:1.5D;
                        field(instance,"damage",configuredDamage); field(instance,"movement",configuredMovement);
                        if (owner.endsWith("ItemCrucible")) {
                            java.lang.reflect.Method maxDamage=item.getDeclaredMethod("f",int.class); maxDamage.setAccessible(true); maxDamage.invoke(instance,3);
                            Object stack=stackClass.getConstructor(item).newInstance(instance);
                            java.lang.reflect.Method m=c.getMethod("getAttributeModifiers",stackClass); java.lang.reflect.Method setDamage=stackClass.getMethod(loader.methods.get("func_77964_b"),int.class);
                            for(int damage:new int[]{0,2,3,4}) {
                                setDamage.invoke(stack,damage); Multimap<?,?> map=(Multimap<?,?>)m.invoke(instance,stack); check(map.size()==(damage<3?2:0),"crucible active/spent cardinality"); List<Map<String,Object>> rows=payload(map,loader);
                                if(damage<3) check(rows.get(0).get("amount").equals((double)configuredDamage)&&rows.get(1).get("amount").equals(configuredMovement)&&rows.get(0).get("name").equals("Tool modifier")&&rows.get(1).get("name").equals("Tool modifier"),"crucible exact active payload");
                                results.put(owner+"/damage="+damage,rows);
                            }
                        } else {
                            Multimap<?,?> map=(Multimap<?,?>)dispatch.invoke(instance); check(map.size()==2,"ability original cardinality"); List<Map<String,Object>> rows=payload(map,loader);
                            check(rows.get(0).get("amount").equals((double)configuredDamage)&&rows.get(1).get("amount").equals(configuredMovement)&&rows.get(0).get("name").equals("Tool modifier")&&rows.get(1).get("name").equals("Tool modifier"),"ability original explicit payload"); results.put(owner,rows);
                        }
                    }
                    System.out.println("PASS complete original classfile map " + owner);
                } catch (Throwable failure) {
                    if(failure instanceof AssertionError) throw (AssertionError)failure;
                    Throwable cause=failure; while(cause.getCause()!=null) cause=cause.getCause();
                    if(cause instanceof AssertionError) throw (AssertionError)cause;
                    check(owner.endsWith("ItemToolAbility")&&cause instanceof NoSuchMethodException&&"aji.captureDrops(boolean)".equals(cause.getMessage()),"unexpected original-class runtime failure: "+owner+": "+cause);
                    String message=cause.getClass().getName()+": "+cause.getMessage(); results.put(owner+"/runtime_limitation",message); System.out.println("STATIC-ONLY " + owner + ": " + message);
                }
            }
        }
        try(Writer w=new OutputStreamWriter(new FileOutputStream(args[3]),StandardCharsets.UTF_8)){new GsonBuilder().setPrettyPrinting().create().toJson(results,w);}
        System.out.println("SCOPE: complete original classfiles, symbol remapping only. Unsafe skips unrelated constructors, not static initializers. Forge-dependent unavailable classes are explicit limitations; no stub or replacement bytecode.");
    }
}
