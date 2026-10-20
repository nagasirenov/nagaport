import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

/** Rehosts the unmodified original ranged decision bytecode on actual native entities.
 * Only class/member API names and the now-private bounding-box getter are translated.
 * No branch, arithmetic, constant, timer, radius or visibility instruction is replaced.
 */
public final class OriginalRangedOracle {
    static final Map<String,String> TYPES = new HashMap<>();
    static {
        TYPES.put("vd","LegacyRangedOracle");TYPES.put("ui","net/minecraft/entity/ai/EntityAIBase");
        TYPES.put("sw","net/minecraft/entity/EntityLiving");TYPES.put("sv","net/minecraft/entity/EntityLivingBase");TYPES.put("sa","net/minecraft/entity/Entity");
        TYPES.put("yi","net/minecraft/entity/IRangedAttackMob");TYPES.put("vv","net/minecraft/pathfinding/PathNavigate");
        TYPES.put("vw","net/minecraft/entity/ai/EntitySenses");TYPES.put("tu","net/minecraft/entity/ai/EntityLookHelper");
        TYPES.put("azt","net/minecraft/util/math/AxisAlignedBB");TYPES.put("qh","net/minecraft/util/math/MathHelper");
    }
    static class Names extends Remapper {
        public String map(String name) { return TYPES.containsKey(name)?TYPES.get(name):name; }
        public String mapFieldName(String owner,String name,String desc) {
            if(owner.equals("sv")) { if(name.equals("s")) return "posX";if(name.equals("u")) return "posZ"; }
            if(owner.equals("azt")&&name.equals("b")) return "minY";
            return name;
        }
        public String mapMethodName(String owner,String name,String desc) {
            if(name.equals("<init>")) return name;
            if(owner.equals("vd")||owner.equals("ui")) {
                if(desc.equals("(I)V")) return "setMutexBits";
                if(name.equals("a")) return "shouldExecute";if(name.equals("b")) return "shouldContinueExecuting";
                if(name.equals("d")) return "resetTask";if(name.equals("e")) return "updateTask";
            }
            if(owner.equals("sw")) {
                if(name.equals("o"))return "getAttackTarget";if(name.equals("m"))return "getNavigator";
                if(name.equals("n"))return "getEntitySenses";if(name.equals("j"))return "getLookHelper";
                if(name.equals("e"))return "getDistanceSq";
            }
            if(owner.equals("vv")) { if(name.equals("a"))return "tryMoveToEntityLiving";if(name.equals("g"))return "noPath";if(name.equals("h"))return "clearPath"; }
            if(owner.equals("vw")&&name.equals("a"))return "canSee";
            if(owner.equals("tu")&&name.equals("a"))return "setLookPositionWithEntity";
            if(owner.equals("yi")&&name.equals("a"))return "attackEntityWithRangedAttack";
            if(owner.equals("qh")) { if(name.equals("a"))return "sqrt";if(name.equals("d"))return "floor"; }
            if(owner.startsWith("java/"))return name;
            throw new AssertionError("Unmapped original method: "+owner+"."+name+desc);
        }
    }
    public static void main(String[] args) throws Exception {
        byte[] original;
        try(JarFile jar=new JarFile(args[0]);InputStream in=jar.getInputStream(jar.getJarEntry("vd.class"));ByteArrayOutputStream out=new ByteArrayOutputStream()) {
            byte[] buffer=new byte[4096];int n;while((n=in.read(buffer))>=0)out.write(buffer,0,n);original=out.toByteArray();
        }
        final int[] boxReads={0};
        // Getter is deliberately admitted explicitly, not guessed by generic remapping.
        Names mapping=new Names() {
            public String mapMethodName(String owner,String name,String desc) { return name.equals("getEntityBoundingBox")?name:super.mapMethodName(owner,name,desc); }
        };
        // Rebuild the chain with the explicit getter admission.
        ClassWriter output=new ClassWriter(0);
        final ClassVisitor sink=new ClassRemapper(output,mapping);
        new ClassReader(original).accept(new ClassVisitor(Opcodes.ASM5,sink) {
            public MethodVisitor visitMethod(int access,String name,String desc,String sig,String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM5,super.visitMethod(access,name,desc,sig,exceptions)) {
                    public void visitFieldInsn(int op,String owner,String field,String type) {
                        if(op==Opcodes.GETFIELD&&owner.equals("sv")&&field.equals("C")&&type.equals("Lazt;")) {
                            boxReads[0]++;super.visitMethodInsn(Opcodes.INVOKEVIRTUAL,"sv","getEntityBoundingBox","()Lazt;",false);
                        } else super.visitFieldInsn(op,owner,field,type);
                    }
                };
            }
        },0);
        if(boxReads[0]!=1)throw new AssertionError("Original box-read shape changed");
        Files.write(Paths.get(args[1],"LegacyRangedOracle.class"),output.toByteArray());
        System.out.println("PASS: original vd.class rehosted; one field-to-getter boundary, all original decisions retained.");
    }
}
