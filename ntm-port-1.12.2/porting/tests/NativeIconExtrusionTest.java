import com.hbm.render.util.NativeIconExtrusion;
import java.nio.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.objectweb.asm.*;

/** Exact pinned old method bytecode, with only its output sink redirected to a test recorder.
 * This does not replace any native Minecraft class or claim whole legacy engine execution.
 */
public class NativeIconExtrusionTest {
    public static final class VertexOracle {
        final List<double[]> vertices=new ArrayList<>();float nx,ny,nz;int batches;
        public void b(){batches++;} public int a(){return 0;}
        public void c(float x,float y,float z){nx=x;ny=y;nz=z;}
        public void a(double x,double y,double z,double u,double v){vertices.add(new double[]{x,y,z,u,v,nx,ny,nz});}
    }
    static int checks;
    static void check(boolean result,String label){checks++;if(!result)throw new AssertionError(label);}
    static class OracleLoader extends ClassLoader { Class<?> define(byte[] bytes){return defineClass("LegacyIconOracle",bytes,0,bytes.length);} }
    static Method oracle(String jar)throws Exception {
        byte[] old;try(ZipFile zip=new ZipFile(jar)){java.io.InputStream input=zip.getInputStream(zip.getEntry("bly.class"));java.io.ByteArrayOutputStream data=new java.io.ByteArrayOutputStream();byte[] buf=new byte[4096];int n;while((n=input.read(buf))>=0)data.write(buf,0,n);old=data.toByteArray();}
        final ClassWriter out=new ClassWriter(ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS);out.visit(Opcodes.V1_8,Opcodes.ACC_PUBLIC,"LegacyIconOracle",null,"java/lang/Object",null);
        final int[] found={0};new ClassReader(old).accept(new ClassVisitor(Opcodes.ASM5) {
            @Override public MethodVisitor visitMethod(int access,String name,String desc,String sig,String[] exceptions) {
                if(!name.equals("a")||!desc.equals("(Lbmh;FFFFIIF)V"))return null;
                found[0]++;MethodVisitor target=out.visitMethod(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"render",desc.replace("bmh","NativeIconExtrusionTest$VertexOracle"),null,null);
                return new MethodVisitor(Opcodes.ASM5,target) {
                    @Override public void visitMethodInsn(int opcode,String owner,String name,String desc,boolean itf){check(owner.equals("bmh"),"oracle calls only original vertex sink");super.visitMethodInsn(opcode,"NativeIconExtrusionTest$VertexOracle",name,desc,itf);}
                };
            }
        },ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);check(found[0]==1,"one exact original method selected");out.visitEnd();
        return new OracleLoader().define(out.toByteArray()).getMethod("render",VertexOracle.class,float.class,float.class,float.class,float.class,int.class,int.class,float.class);
    }
    public static void main(String[] args)throws Exception {
        Method original=oracle(args[0]);
        for(int[] size:new int[][]{{1,1},{16,16},{8,32},{255,255},{0,0}})for(float[] uv:new float[][]{{1,0,0,1},{.75F,.125F,.25F,.875F}}) {
            VertexOracle expected=new VertexOracle();original.invoke(null,expected,uv[0],uv[1],uv[2],uv[3],size[0],size[1],.0625F);
            BufferBuilder actual=new BufferBuilder(1024);actual.begin(7,DefaultVertexFormats.POSITION_TEX_NORMAL);NativeIconExtrusion.append(actual,uv[0],uv[1],uv[2],uv[3],size[0],size[1],.0625F);
            check(expected.batches==6&&actual.getVertexCount()==expected.vertices.size(),"original six face groups/count");actual.finishDrawing();ByteBuffer bytes=actual.getByteBuffer().order(ByteOrder.nativeOrder());int stride=actual.getVertexFormat().getSize();
            for(int i=0;i<expected.vertices.size();i++) {
                double[] vertex=expected.vertices.get(i);
                for(int c=0;c<5;c++)check(Float.floatToIntBits(bytes.getFloat(i*stride+c*4))==Float.floatToIntBits((float)vertex[c]),"native position/UV matches exact old method");
                for(int c=0;c<3;c++)check(bytes.get(i*stride+20+c)==(byte)(vertex[c+5]*127),"native packed normal matches old face");
            }
        }
        System.out.println("PASS "+checks+" original-method/native-buffer extrusion comparisons. No full old engine or GL draw claim.");
    }
}
