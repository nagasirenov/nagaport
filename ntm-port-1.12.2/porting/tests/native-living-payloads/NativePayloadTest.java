import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

public final class NativePayloadTest {
    static int checks;
    static void ok(boolean value, String label) { checks++; if (!value) throw new AssertionError(label); }
    static void same(float a, float b, String label) { ok(Float.floatToRawIntBits(a) == Float.floatToRawIntBits(b), label); }
    static final String HURT = LivingHurtEvent.class.getName().replace('.', '/');
    static final String ATTACK = LivingAttackEvent.class.getName().replace('.', '/');
    static final Map<String, String> DESCRIPTORS = new HashMap<>();
    static { DESCRIPTORS.put("getAmount", "()F"); DESCRIPTORS.put("setAmount", "(F)V"); DESCRIPTORS.put("getSource", "()Lnet/minecraft/util/DamageSource;"); }
    static void audit(String classpathFile) throws Exception {
        Map<String,String> parents = new HashMap<>();
        Map<String,Set<String>> declarations = new HashMap<>();
        int jars = 0;
        for (String path : new String(Files.readAllBytes(Paths.get(classpathFile)), "UTF-8").trim().split(java.io.File.pathSeparator)) {
            if (!path.endsWith(".jar") || !Files.isRegularFile(Paths.get(path))) continue;
            jars++;
            try (JarFile jar = new JarFile(path)) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement(); if (!entry.getName().endsWith(".class")) continue;
                    ClassReader reader = new ClassReader(jar.getInputStream(entry));
                    String name = reader.getClassName();
                    if (parents.containsKey(name)) continue; // Match first classpath definition, not a later duplicate.
                    parents.put(name, reader.getSuperName());
                    reader.accept(new ClassVisitor(Opcodes.ASM5) {
                        public MethodVisitor visitMethod(int access, String method, String descriptor, String signature, String[] exceptions) {
                            if (descriptor.equals(DESCRIPTORS.get(method))) declarations.computeIfAbsent(name, k -> new HashSet<>()).add(method);
                            return null;
                        }
                    }, ClassReader.SKIP_CODE);
                }
            }
        }
        for (String root : Arrays.asList(HURT, ATTACK)) for (Map.Entry<String,Set<String>> entry : declarations.entrySet()) {
            String current = entry.getKey(); Set<String> seen = new HashSet<>();
            while (current != null && seen.add(current)) {
                if (current.equals(root)) { ok(entry.getKey().equals(root), "dependency override " + entry.getKey() + " " + entry.getValue()); break; }
                current = parents.get(current);
            }
        }
        System.out.println("PASS resolved binary override closure: " + jars + " actual classpath jars; no accessor overrides in either family");
    }
    static List<String> code(Class<?> owner, String method, String descriptor) throws Exception {
        List<String> result = new ArrayList<>();
        new ClassReader(owner.getResourceAsStream(owner.getSimpleName() + ".class")).accept(new ClassVisitor(Opcodes.ASM5) {
            public MethodVisitor visitMethod(int a, String n, String d, String s, String[] x) {
                if (!n.equals(method) || !d.equals(descriptor)) return null;
                return new MethodVisitor(Opcodes.ASM5) {
                    public void visitVarInsn(int op, int var) { result.add(op + ":" + var); }
                    public void visitFieldInsn(int op, String o, String f, String d) { result.add(op + ":" + o + ":" + f + ":" + d); }
                    public void visitInsn(int op) { result.add("" + op); }
                    public void visitMethodInsn(int op, String o, String n, String d, boolean itf) { throw new AssertionError("accessor invokes method"); }
                    public void visitJumpInsn(int op, Label label) { throw new AssertionError("accessor branches"); }
                    public void visitIntInsn(int op, int value) { throw new AssertionError("unexpected instruction"); }
                    public void visitLdcInsn(Object value) { throw new AssertionError("unexpected constant"); }
                    public void visitTypeInsn(int op, String t) { throw new AssertionError("unexpected type instruction"); }
                };
            }
        }, 0);
        return result;
    }
    static class ExternalOverride extends LivingHurtEvent {
        ExternalOverride(DamageSource source) { super(null, source, 2F); }
        public float getAmount() { return 71F; }
        public DamageSource getSource() { return null; }
        public void setAmount(float value) { }
    }
    public static void main(String[] args) throws Exception {
        audit(args[0]);
        if (args.length > 1 && args[1].equals("audit-only")) return;
        net.minecraft.init.Bootstrap.register();
        DamageSource source = new DamageSource("native-payload-proof");
        int[] bits = {0, 0x80000000, 1, 0x80000001, 0x3f800000, 0xc0a00000, 0x7f7fffff, 0xff7fffff, 0x7f800000, 0xff800000, 0x7fc12345, 0xffc54321};
        Field canceled = Event.class.getDeclaredField("isCanceled"); canceled.setAccessible(true);
        for (Class<?> owner : Arrays.asList(LivingHurtEvent.class, LivingAttackEvent.class)) {
            String name = owner.getName().replace('.', '/');
            Field amount = owner.getDeclaredField("amount"), src = owner.getDeclaredField("source");
            amount.setAccessible(true); src.setAccessible(true);
            ok(Modifier.isPrivate(amount.getModifiers()) && amount.getType() == float.class && Modifier.isFinal(amount.getModifiers()) == (owner == LivingAttackEvent.class), "native float visibility/mutability");
            ok(Modifier.isPrivate(src.getModifiers()) && Modifier.isFinal(src.getModifiers()) && src.getType() == DamageSource.class, "native source visibility/type/finality");
            ok(code(owner, "getAmount", "()F").equals(Arrays.asList("25:0", "180:" + name + ":amount:F", "174")), "exact getAmount bytecode");
            ok(code(owner, "getSource", "()Lnet/minecraft/util/DamageSource;").equals(Arrays.asList("25:0", "180:" + name + ":source:Lnet/minecraft/util/DamageSource;", "176")), "exact getSource bytecode");
            if (owner == LivingHurtEvent.class) ok(code(owner, "setAmount", "(F)V").equals(Arrays.asList("25:0", "23:1", "181:" + name + ":amount:F", "177")), "exact setAmount bytecode");
            for (int value : bits) {
                float input = Float.intBitsToFloat(value);
                Event event = (Event)owner.getConstructors()[0].newInstance(null, source, input);
                canceled.setBoolean(event, true);
                same(((Float)owner.getMethod("getAmount").invoke(event)), input, "constructor/getter raw float bits");
                ok(owner.getMethod("getSource").invoke(event) == source && src.get(event) == source, "source identity");
                if (owner == LivingHurtEvent.class) for (int replacement : bits) {
                    float next = Float.intBitsToFloat(replacement);
                    ((LivingHurtEvent)event).setAmount(next);
                    same(((LivingHurtEvent)event).getAmount(), next, "setter/getter raw float bits");
                    same(amount.getFloat(event), next, "setter directly stores native field");
                    ok(((LivingHurtEvent)event).getSource() == source && event.isCanceled(), "write leaves source and existing cancellation flag unchanged");
                }
                ok(event.isCanceled(), "getters leave existing cancellation flag unchanged");
                Object nil = owner.getConstructors()[0].newInstance(null, null, input);
                ok(owner.getMethod("getSource").invoke(nil) == null, "null source remains null");
            }
        }
        ExternalOverride external = new ExternalOverride(source);
        Field actual = LivingHurtEvent.class.getDeclaredField("amount"); actual.setAccessible(true);
        external.setAmount(19F);
        same(actual.getFloat(external), 2F, "unknown external override intercepts setter");
        same(external.getAmount(), 71F, "unknown external override changes read");
        ok(external.getSource() == null, "unknown external override changes source");
        System.out.println("PASS " + checks + " genuine native payload checks; test-only reflected cancellation state, not event-bus transformation or gameplay");
        System.exit(0);
    }
}
