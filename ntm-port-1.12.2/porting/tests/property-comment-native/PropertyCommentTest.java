import com.hbm.config.BombConfig;
import com.hbm.config.CommonConfig;
import com.hbm.config.WeaponConfig;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

/** Runs against genuine Forge 1614 or 2768 classes; no Forge/Minecraft test doubles. */
public final class PropertyCommentTest {
    static int checks;
    static boolean legacy;
    static File directory;
    static final StringBuilder snapshots = new StringBuilder();
    static final Field COMMENT;
    static {
        try { COMMENT = Property.class.getDeclaredField("comment"); COMMENT.setAccessible(true); }
        catch (Exception ex) { throw new ExceptionInInitializerError(ex); }
    }
    static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
    static Object comment(Property p) throws Exception { return COMMENT.get(p); }
    static void assign(Property p, String text) throws Exception {
        if (legacy) COMMENT.set(p, text);
        else Property.class.getMethod("setComment", String.class).invoke(p, text);
    }
    static String text(Object value) {
        if (value == null) return "<null>";
        if (value instanceof Object[]) return Arrays.deepToString((Object[]) value);
        return String.valueOf(value).replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r");
    }
    static Map<String, String> fields(Property p) throws Exception {
        Map<String, String> fields = new TreeMap<>();
        for (Field f : Property.class.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) || f.getName().equals("comment")) continue;
            f.setAccessible(true); fields.put(f.getName(), text(f.get(p)));
        }
        return fields;
    }
    static void directAssignment() throws Exception {
        for (Property.Type type : new Property.Type[]{Property.Type.INTEGER, Property.Type.BOOLEAN, Property.Type.DOUBLE, Property.Type.STRING}) {
            for (boolean list : new boolean[]{false, true}) {
                Property p = list ? new Property("sentinel", new String[]{"1", "2"}, type)
                                  : new Property("sentinel", "1", type);
                for (boolean changed : new boolean[]{false, true}) {
                    if (changed) p.setValue("2");
                    Map<String, String> before = fields(p);
                    for (String value : new String[]{null, "", "verbatim", "line 1\nline 2", "\n leading\t\n\ntrailing\n"}) {
                        assign(p, value);
                        check(comment(p) == value, "comment identity/null preserved");
                        check(fields(p).equals(before), "only comment may change");
                        check(p.hasChanged() == changed, "changed flag must be preserved");
                    }
                }
            }
        }
    }
    static Object[] helpers(Configuration c, String value) throws Exception {
        Object[] out = {
            CommonConfig.createConfigInt(c, "helpers", "int", value, 11),
            CommonConfig.createConfigInt(c, "helpers", "bounded", value, 5, 1, 9),
            CommonConfig.createConfigDouble(c, "helpers", "double", value, 1.5),
            CommonConfig.createConfigBool(c, "helpers", "bool", value, true),
            CommonConfig.createConfigString(c, "helpers", "string", value, "default text"),
            Arrays.toString(CommonConfig.createConfigIntList(c, "helpers", "ints", value, new int[]{1, -2, 3})),
            Arrays.toString(CommonConfig.createConfigStringList(c, "helpers", "strings", value, new String[]{"alpha", "beta"}))
        };
        for (Property p : c.getCategory("helpers").values()) {
            check(Objects.equals(comment(p), value), "all seven helpers retain exact comment");
        }
        Property bounded = c.getCategory("helpers").get("bounded");
        check(bounded.getMinValue().equals("1") && bounded.getMaxValue().equals("9"), "bounds metadata preserved");
        return out;
    }
    static void seedHelpers(Configuration c, boolean malformed) {
        c.getCategory("helpers").get("int").setValue(malformed ? "not an int" : "-17");
        c.getCategory("helpers").get("bounded").setValue(malformed ? "bad" : "45");
        c.getCategory("helpers").get("double").setValue(malformed ? "bad" : "2.25");
        c.getCategory("helpers").get("bool").setValue(malformed ? "bad" : "false");
        c.getCategory("helpers").get("string").setValue("saved text");
        c.getCategory("helpers").get("ints").setValues(malformed ? new String[]{"bad", "12"} : new String[]{"42", "-1"});
        c.getCategory("helpers").get("strings").setValues(new String[]{"persisted", "two"});
    }
    static void snapshot(Configuration c, String label) throws Exception {
        snapshots.append(label).append(" changed=").append(c.hasChanged()).append('\n');
        for (String category : c.getCategoryNames()) {
            for (Map.Entry<String, Property> e : c.getCategory(category).entrySet()) {
                Property p = e.getValue();
                snapshots.append(category).append('/').append(e.getKey()).append('|').append(p.getType()).append('|')
                    .append(text(p.isList() ? p.getStringList() : p.getString())).append('|')
                    .append(text(p.isList() ? p.getDefaults() : p.getDefault())).append('|')
                    .append(text(comment(p))).append('|').append(p.getMinValue()).append('|').append(p.getMaxValue())
                    .append('|').append(p.hasChanged()).append('\n');
            }
        }
    }
    static void saveReload(Configuration c, File file, String label, String comment, boolean owners) throws Exception {
        snapshot(c, label + " pre-save");
        boolean changedBeforeSave = c.hasChanged();
        c.save();
        check(file.isFile() && file.length() > 0, "native config save produced bytes");
        check(c.hasChanged() == changedBeforeSave, "save preserves changed flags");
        snapshot(c, label + " post-save");
        byte[] saved = Files.readAllBytes(file.toPath());
        c.save();
        check(Arrays.equals(saved, Files.readAllBytes(file.toPath())), "repeat save exact bytes");
        Configuration reloaded = new Configuration(file);
        if (owners) { BombConfig.loadFromConfig(reloaded); WeaponConfig.loadFromConfig(reloaded); }
        else helpers(reloaded, comment);
        snapshot(reloaded, label + " reloaded");
        reloaded.save();
        check(Arrays.equals(saved, Files.readAllBytes(file.toPath())), "load/apply comments/save exact bytes");
    }
    static void helperCases() throws Exception {
        String[] comments = {null, "", "one line", "line 1\nline 2", "\nleading\n\ntrailing\n", "unicode caf\u00e9\tindent"};
        for (int i = 0; i < comments.length; i++) {
            for (int mode = 0; mode < 3; mode++) {
                File file = new File(directory, "helpers-" + i + "-" + mode + ".cfg");
                Configuration c = new Configuration(file);
                Object[] values = helpers(c, comments[i]);
                check(Arrays.deepEquals(values, new Object[]{11, 5, 1.5, true, "default text", "[1, -2, 3]", "[alpha, beta]"}), "helper defaults");
                if (mode > 0) {
                    seedHelpers(c, mode == 2);
                    values = helpers(c, comments[i]);
                    if (mode == 1) check(Arrays.deepEquals(values, new Object[]{-17, 45, 2.25, false, "saved text", "[42, -1]", "[persisted, two]"}), "saved values are not clamped or replaced");
                }
                snapshots.append("returned ").append(Arrays.deepToString(values)).append('\n');
                saveReload(c, file, "helpers-" + i + "-" + mode, comments[i], false);
            }
        }
    }
    static void staticValues(Class<?> type) throws Exception {
        List<Field> fields = new ArrayList<>(Arrays.asList(type.getDeclaredFields()));
        Collections.sort(fields, new Comparator<Field>() { public int compare(Field a, Field b) { return a.getName().compareTo(b.getName()); } });
        for (Field f : fields) if (Modifier.isStatic(f.getModifiers())) snapshots.append(type.getSimpleName()).append('.').append(f.getName()).append('=').append(f.get(null)).append('\n');
    }
    static void ownerCases() throws Exception {
        for (int mode = 0; mode < 3; mode++) {
            File file = new File(directory, "owners-" + mode + ".cfg");
            Configuration c = new Configuration(file);
            BombConfig.loadFromConfig(c); WeaponConfig.loadFromConfig(c);
            check(BombConfig.gadgetRadius == 150 && BombConfig.boyRadius == 120 && BombConfig.manRadius == 175 && BombConfig.tsarRadius == 500, "selected radius defaults");
            check(BombConfig.chunkloading && BombConfig.explosionAlgorithm == 2 && WeaponConfig.ciwsHitrate == 50 && !WeaponConfig.linearAnimations, "other owner defaults");
            if (mode > 0) {
                for (String category : c.getCategoryNames()) for (Property p : c.getCategory(category).values()) {
                    p.setValue(mode == 2 ? "malformed" : p.getType() == Property.Type.BOOLEAN ? "false" : "-34");
                }
                BombConfig.loadFromConfig(c); WeaponConfig.loadFromConfig(c);
                if (mode == 1) check(BombConfig.gadgetRadius == -34 && BombConfig.mk5 == -34 && WeaponConfig.ciwsHitrate == -34 && !BombConfig.chunkloading, "owner saved values preserved");
            }
            check(comment(c.getCategory(CommonConfig.CATEGORY_NUKES).get("3.00_gadgetRadius")).equals("Radius of the Gadget"), "exact literal comment");
            staticValues(BombConfig.class); staticValues(WeaponConfig.class);
            saveReload(c, file, "owners-" + mode, null, true);
            staticValues(BombConfig.class); staticValues(WeaponConfig.class);
        }
    }
    public static void main(String[] args) throws Exception {
        legacy = args[0].equals("legacy"); directory = new File(args[1]); directory.mkdirs();
        // Bootstrap only the genuine FML home field, no forged Configuration/Property classes.
        Class<?> injection = Class.forName((legacy ? "cpw.mods.fml" : "net.minecraftforge.fml") + ".relauncher.FMLInjectionData");
        Field home = injection.getDeclaredField("minecraftHome"); home.setAccessible(true); home.set(null, directory);
        directAssignment(); helperCases(); ownerCases();
        Files.write(new File(directory, "snapshots.txt").toPath(), snapshots.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println("PASS " + args[0] + ": " + checks + " assertions; 21 native configuration files saved/reloaded");
    }
}
