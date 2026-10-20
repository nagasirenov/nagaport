import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.io.*;
import org.objectweb.asm.*;

/** Reads genuine class artifacts without loading or initializing them. */
public final class AirBytecodeScan {
    public static void main(String[] args) throws Exception {
        TreeSet<String> rows = new TreeSet<>();
        for (String path : new String(Files.readAllBytes(Paths.get(args[0])), "UTF-8").trim().split(":|\\n")) {
            if (!path.endsWith(".jar") || !Files.exists(Paths.get(path))) continue;
            try (JarFile jar = new JarFile(path)) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (!entry.getName().endsWith(".class")) continue;
                    try (InputStream in = jar.getInputStream(entry)) {
                        new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM5) {
                            String owner;
                            public void visit(int version, int access, String name, String signature, String parent, String[] interfaces) {
                                owner = name;
                                if (name.startsWith("com/hbm/")) rows.add("CLASS\t" + name + "\t" + parent);
                            }
                            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                                if (!(name.equals("isAir") || (name.equals("getMaterial") || name.equals("func_149688_o")) && descriptor.endsWith("Lnet/minecraft/block/material/Material;"))) return null;
                                String prefix = "METHOD\t" + owner + "\t" + descriptor;
                                rows.add(prefix);
                                return new MethodVisitor(Opcodes.ASM5) {
                                    StringJoiner code = new StringJoiner(",");
                                    public void visitInsn(int op) { code.add(Integer.toString(op)); }
                                    public void visitIntInsn(int op, int operand) { code.add(op + ":" + operand); }
                                    public void visitTypeInsn(int op, String type) { code.add(op + ":" + type); }
                                    public void visitLdcInsn(Object value) { code.add("LDC:" + value); }
                                    public void visitJumpInsn(int op, Label target) { code.add("JUMP:" + op); }
                                    public void visitIincInsn(int var, int increment) { code.add("IINC:" + var + ":" + increment); }
                                    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) { code.add("TABLESWITCH"); }
                                    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) { code.add("LOOKUPSWITCH"); }
                                    public void visitMultiANewArrayInsn(String desc, int dimensions) { code.add("MULTIANEWARRAY"); }
                                    public void visitInvokeDynamicInsn(String n, String d, Handle h, Object... values) { code.add("INVOKEDYNAMIC"); }
                                    public void visitVarInsn(int op, int var) { code.add(op + ":" + var); }
                                    public void visitMethodInsn(int op, String o, String n, String d, boolean itf) { code.add(op + ":" + o + "." + n + d); }
                                    public void visitFieldInsn(int op, String o, String n, String d) { code.add(op + ":" + o + "." + n + d); }
                                    public void visitEnd() { rows.add("CODE\t" + owner + "\t" + descriptor + "\t" + code); }
                                };
                            }
                        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                    }
                }
            }
        }
        for (String row : rows) System.out.println(row);
    }
}
