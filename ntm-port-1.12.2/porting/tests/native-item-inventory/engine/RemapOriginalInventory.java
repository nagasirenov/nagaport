package inventoryengine;

import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

/** Name-only relocation of complete original classfiles to genuine notch classes. */
public final class RemapOriginalInventory {
    static String member(String value) { return value.substring(value.lastIndexOf('/') + 1); }
    public static void main(String[] args) throws Exception {
        final Map<String,String> types = new HashMap<>(), fields = new HashMap<>(), exactFields = new HashMap<>(), methods = new HashMap<>();
        for (String line : Files.readAllLines(Paths.get(args[0]))) {
            String[] p = line.split(" ");
            if (p[0].equals("CL:")) types.put(p[2], p[1]);
            if (p[0].equals("FD:")) { fields.put(member(p[2]), member(p[1])); exactFields.put(p[2], member(p[1])); }
            if (p[0].equals("MD:")) methods.put(member(p[3]) + p[4], member(p[1]));
        }
        Remapper names = new Remapper() {
            public String map(String name) { return types.containsKey(name) ? types.get(name) : name; }
            public String mapFieldName(String owner, String name, String desc) {
                if (exactFields.containsKey(owner + "/" + name)) return exactFields.get(owner + "/" + name);
                return name.startsWith("field_") && fields.containsKey(name) ? fields.get(name) : name;
            }
            public String mapMethodName(String owner, String name, String desc) {
                String key = name + desc;
                return name.startsWith("func_") && methods.containsKey(key) ? methods.get(key) : name;
            }
        };
        Path out = Paths.get(args[2]);
        try (JarFile jar = new JarFile(args[1])) {
            for (String name : new String[]{"com/hbm/items/ItemInventory", "com/hbm/util/ItemStackUtil",
                    "com/hbm/items/tool/ItemLeadBox$InventoryLeadBox",
                    "com/hbm/items/tool/ItemToolBox$InventoryToolBox",
                    "com/hbm/items/tool/ItemRebarPlacer$InventoryRebar"}) {
                ClassReader reader = new ClassReader(jar.getInputStream(jar.getJarEntry(name + ".class")));
                ClassWriter writer = new ClassWriter(0);
                reader.accept(new ClassRemapper(writer, names), 0);
                Path target = out.resolve(name + ".class"); Files.createDirectories(target.getParent()); Files.write(target, writer.toByteArray());
            }
        }
        // Only test-owned abstract hooks are supplied. All inventory algorithms,
        // serialization, ItemStack and platform methods are original classfiles.
        ClassWriter w = new ClassWriter(0);
        String fixture = "inventoryengine/OriginalInventoryFixture", base = "com/hbm/items/ItemInventory";
        w.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, fixture, null, base, null);
        MethodVisitor m = w.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Ladd;)V", null, null); m.visitCode();
        m.visitVarInsn(Opcodes.ALOAD,0); m.visitMethodInsn(Opcodes.INVOKESPECIAL,base,"<init>","()V",false);
        m.visitVarInsn(Opcodes.ALOAD,0); m.visitInsn(Opcodes.ICONST_3); m.visitTypeInsn(Opcodes.ANEWARRAY,"add"); m.visitFieldInsn(Opcodes.PUTFIELD,base,"slots","[Ladd;");
        m.visitVarInsn(Opcodes.ALOAD,0); m.visitVarInsn(Opcodes.ALOAD,1); m.visitFieldInsn(Opcodes.PUTFIELD,base,"target","Ladd;");
        m.visitInsn(Opcodes.RETURN); m.visitMaxs(2,2); m.visitEnd();
        m = w.visitMethod(Opcodes.ACC_PUBLIC,"a","()I",null,null); m.visitCode(); m.visitInsn(Opcodes.ICONST_3); m.visitInsn(Opcodes.IRETURN); m.visitMaxs(1,1); m.visitEnd();
        m = w.visitMethod(Opcodes.ACC_PUBLIC,"b","()Ljava/lang/String;",null,null); m.visitCode(); m.visitLdcInsn("oracle"); m.visitInsn(Opcodes.ARETURN); m.visitMaxs(1,1); m.visitEnd();
        m = w.visitMethod(Opcodes.ACC_PUBLIC,"k_","()Z",null,null); m.visitCode(); m.visitInsn(Opcodes.ICONST_0); m.visitInsn(Opcodes.IRETURN); m.visitMaxs(1,1); m.visitEnd();
        w.visitEnd(); Path target = out.resolve(fixture + ".class"); Files.createDirectories(target.getParent()); Files.write(target,w.toByteArray());
    }
}
