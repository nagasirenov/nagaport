import com.hbm.render.mesh.*;
import com.hbm.render.mesh.ObjMeshRenderer.Shading;
import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

/** Full production mesh classes and real native BufferBuilder; no GL context or substitute MC types. */
public final class ObjMeshMinecraftTest {
    static int assertions;
    static void check(boolean ok, String label) { assertions++; if(!ok) throw new AssertionError(label); }
    static void near(float a, float b, String label) { check(Math.abs(a-b) <= 1E-6F, label + ": " + a + " != " + b); }
    static ObjMesh parse(String text, boolean mixed) throws Exception {
        return ObjMeshParser.parse(new StringReader(text), "fixture", mixed);
    }
    static final String VERTICES = "v 0 0 0\nv 1 0 0\nv 0 1 0\nv 0 0 1\n"
            + "vt 0.0 0.0\nvt 2.0 0.0\nvt 0.0 -1.0\nvn 1 0 0\n";
    static final String TRI = "f 1/1/1 2/2/1 3/3/1\n";
    static ByteBuffer bytes(List<ObjMesh.Face> faces, Shading shading, int mode, float offset) {
        BufferBuilder buffer = new BufferBuilder(32);
        buffer.begin(mode, ObjMeshRenderer.format(faces, shading));
        ObjMeshRenderer.append(buffer, faces, shading, offset, new ObjMeshRenderer.Cursor());
        buffer.finishDrawing();
        ByteBuffer copy = ByteBuffer.allocate(buffer.getByteBuffer().remaining()).order(ByteOrder.nativeOrder());
        copy.put(buffer.getByteBuffer().duplicate()).flip();
        check(buffer.getVertexCount() == (mode == GL11.GL_QUADS ? 4 : 3) * faces.size(), "native vertex count");
        return copy;
    }
    static void fixtures() throws Exception {
        ObjMesh mesh = parse(VERTICES + "mtllib deliberately_absent.mtl\ng First\n" + TRI + "g second\n" + TRI + "g FIRST\n" + TRI, false);
        check(mesh.parts.size()==3 && mesh.parts.get(2).name.equals("FIRST"), "source group order and duplicate names");
        check(mesh.selectParts("first", "FIRST").size()==4, "duplicate/case-equivalent selectors retain emission multiplicity");
        check(mesh.selectParts("FIRST").size()==2, "part selector visits all duplicate groups");
        check(mesh.excludingParts("fIrSt").size()==1 && mesh.excludingParts("fIrSt").get(0).name.equals("second"), "case-insensitive exclusions");
        check(mesh.selectParts("missing").isEmpty(), "absent part remains empty");
        List<ObjMesh.Face> faces=mesh.parts.get(0).faces;
        ByteBuffer flat=bytes(faces,Shading.LEGACY_FLAT,GL11.GL_TRIANGLES,0);
        check(flat.remaining()==72,"three 24-byte native POSITION_TEX_NORMAL vertices");
        near(flat.getFloat(12),.0005F,"first U inset"); near(flat.getFloat(16),1.0005F,"flipped first V inset");
        near(flat.getFloat(24+12),1.9995F,"wrapping U preserved, inset inward");
        near(flat.getFloat(48+16),1.9995F,"negative source V flips above one, not clamped");
        check(flat.get(20)==0 && flat.get(21)==0 && flat.get(22)==127,"plain Forge normal is face +Z, ignores supplied +X vn");
        ByteBuffer smooth=bytes(faces,Shading.HFR_SMOOTH,GL11.GL_TRIANGLES,0);
        near(smooth.getFloat(12),0,"HFR has no UV inset");
        check(smooth.get(20)==127 && smooth.get(21)==0 && smooth.get(22)==0,"HFR uses supplied normal in UV branch");
        ByteBuffer quad=bytes(faces,Shading.LEGACY_FLAT,GL11.GL_QUADS,0);
        for(int i=0;i<23;i++) check(quad.get(48+i)==quad.get(72+i),"triangle quad expansion duplicates last corner attributes");
        float offset=ObjMeshRenderer.hmfOffset(54321.25F,50000D,2500D);
        ByteBuffer hmf=bytes(faces,Shading.HMF,GL11.GL_TRIANGLES,offset);
        for(int i=0;i<3;i++) near(hmf.getFloat(i*24+16),flat.getFloat(i*24+16)+offset,"HMF time/modulo/quotient and inset");
        List<ObjMesh.Face> untextured=parse(VERTICES+"f 1//1 2//1 3//1\n",false).parts.get(0).faces;
        check(ObjMeshRenderer.format(untextured,Shading.HFR_SMOOTH).equals(DefaultVertexFormats.POSITION),"smooth UV-absent branch does not set normals");
        check(ObjMeshRenderer.format(untextured,Shading.LEGACY_FLAT).equals(DefaultVertexFormats.POSITION_NORMAL),"flat UV-absent normal-only format");
        ObjMesh mixed=parse(VERTICES+TRI+"f 1/1/1 2/2/1 4/3/1 3/1/1\n",true);
        bytes(mixed.parts.get(0).faces,Shading.LEGACY_FLAT,GL11.GL_QUADS,0);
        boolean rejected=false;
        try { parse(VERTICES+TRI+"f 1 2 4 3\n",false); } catch(IOException e) { rejected=true; }
        check(rejected,"non-mixed parser rejects mixed face sizes");
        rejected=false;
        try { parse(VERTICES+"f 0 2 3\n",false); } catch(IOException e) { rejected=e.getMessage().contains("fixture:"); }
        check(rejected,"invalid indices produce source/line error");
        ObjMesh noNormals=parse(VERTICES+TRI+"f 1/1 2/2 3/3\nf 1 2 3\n",false);
        ByteBuffer carried=bytes(noNormals.parts.get(0).faces,Shading.HFR_SMOOTH,GL11.GL_TRIANGLES,0);
        check(carried.get(6*24+20)==127,"smooth missing-normal and missing-UV faces retain previous normal in batch");
        near(carried.getFloat(6*24+16),2,"missing UV retains previous emitted V in batch");
        BufferBuilder wrong=new BufferBuilder(32);wrong.begin(GL11.GL_TRIANGLES,DefaultVertexFormats.POSITION_TEX);
        rejected=false;
        try{ObjMeshRenderer.append(wrong,faces,Shading.LEGACY_FLAT,0,new ObjMeshRenderer.Cursor());}catch(IllegalArgumentException e){rejected=true;}
        check(rejected && wrong.getVertexCount()==0,"reject missing normal attribute before writing vertices");wrong.finishDrawing();
        for(float time:new float[]{0,49999.75F,50000F,54321.25F,100001F,-1})
            near(ObjMeshRenderer.hmfOffset(time,50000D,2500D),(float)(((double)time%50000D)/2500D),"HMF timestamp boundary "+time);
        // Non-planar quad: use first three corners, not Forge 1.12 diagonal-cross normal.
        ObjMesh nonplanar=parse(VERTICES+"f 1 2 3 4\n",false);
        near(nonplanar.parts.get(0).faces.get(0).normal.z,1,"quad first-three face normal");
        ObjMesh degenerate=parse("v 0 0 0\nv 0 0 0\nv 1 0 0\nf 1 2 3\n",false);
        near(degenerate.parts.get(0).faces.get(0).normal.x,0,"degenerate zero normal");
        BufferBuilder caller=new BufferBuilder(32); caller.begin(GL11.GL_TRIANGLES,DefaultVertexFormats.POSITION_TEX_NORMAL);
        ObjMeshRenderer.Cursor cursor=new ObjMeshRenderer.Cursor();
        ObjMeshRenderer.append(caller,faces,Shading.LEGACY_FLAT,0,cursor);
        ObjMeshRenderer.append(caller,faces,Shading.LEGACY_FLAT,0,cursor);
        check(caller.getVertexCount()==6,"append does not own or reset caller batch"); caller.finishDrawing();
    }

    /** Independent resource oracle uses native Vec3d normal arithmetic, not production mesh normals. */
    static void resource(Path root,String name,int expected,Shading shading) throws Exception {
        Path file=root.resolve(name); ObjMesh mesh;
        try(Reader r=Files.newBufferedReader(file,StandardCharsets.UTF_8)) { mesh=ObjMeshParser.parse(r,name,false); }
        List<float[]> positions=new ArrayList<>(),uvs=new ArrayList<>(),normals=new ArrayList<>();
        List<int[][]> sourceFaces=new ArrayList<>(); List<String> groups=new ArrayList<>();
        for(String line:Files.readAllLines(file,StandardCharsets.UTF_8)) {
            String[] s=line.trim().split("\\s+");
            if(s[0].equals("v")) positions.add(new float[]{Float.parseFloat(s[1]),Float.parseFloat(s[2]),Float.parseFloat(s[3])});
            if(s[0].equals("vn")) normals.add(new float[]{Float.parseFloat(s[1]),Float.parseFloat(s[2]),Float.parseFloat(s[3])});
            if(s[0].equals("vt")) uvs.add(new float[]{Float.parseFloat(s[1]),1-Float.parseFloat(s[2])});
            if(s[0].equals("g")||s[0].equals("o")) groups.add(s[1]);
            if(s[0].equals("f")) {
                int[][] f=new int[s.length-1][];
                for(int i=1;i<s.length;i++) { String[] v=s[i].split("/"); f[i-1]=new int[]{Integer.parseInt(v[0])-1,Integer.parseInt(v[1])-1,Integer.parseInt(v[2])-1}; }
                sourceFaces.add(f);
            }
        }
        int total=0,faceIndex=0;
        for(int p=0;p<mesh.parts.size();p++) {
            ObjMesh.Part part=mesh.parts.get(p);check(part.name.equals(groups.get(p)),name+" group order");
            ByteBuffer emitted=bytes(part.faces,shading,GL11.GL_TRIANGLES,shading==Shading.HMF?1.25F:0);
            int vertex=0;
            for(ObjMesh.Face face:part.faces) {
                int[][] f=sourceFaces.get(faceIndex++); total++;
                float[] a=positions.get(f[0][0]),b=positions.get(f[1][0]),c=positions.get(f[2][0]);
                Vec3d normal=new Vec3d(b[0]-a[0],b[1]-a[1],b[2]-a[2]).crossProduct(new Vec3d(c[0]-a[0],c[1]-a[1],c[2]-a[2])).normalize();
                near(face.normal.x,(float)normal.x,name+" normal X");near(face.normal.y,(float)normal.y,name+" normal Y");near(face.normal.z,(float)normal.z,name+" normal Z");
                float meanU=0,meanV=0;for(int[] corner:f) { meanU+=uvs.get(corner[1])[0];meanV+=uvs.get(corner[1])[1]; }meanU/=f.length;meanV/=f.length;
                for(int i=0;i<f.length;i++,vertex++) {
                    float[] pos=positions.get(f[i][0]),uv=uvs.get(f[i][1]);int off=vertex*24;
                    for(int k=0;k<3;k++) near(emitted.getFloat(off+k*4),pos[k],name+" position/winding");
                    float inset=shading==Shading.HFR_SMOOTH||shading==Shading.HFR_FLAT?0:.0005F;
                    near(emitted.getFloat(off+12),uv[0]+(uv[0]>meanU?-inset:inset),name+" raw U/inset");
                    near(emitted.getFloat(off+16),uv[1]+(uv[1]>meanV?-inset:inset)+(shading==Shading.HMF?1.25F:0),name+" flipped V/inset/animation");
                    if(shading==Shading.HFR_SMOOTH) {
                        float[] vn=normals.get(f[i][2]);
                        for(int k=0;k<3;k++) check(emitted.get(off+20+k)==(byte)(vn[k]*127),name+" exact smooth normal byte");
                    } else {
                        check(emitted.get(off+20)==(byte)((float)normal.x*127),name+" native normal X");
                        check(emitted.get(off+21)==(byte)((float)normal.y*127),name+" native normal Y");
                        check(emitted.get(off+22)==(byte)((float)normal.z*127),name+" native normal Z");
                    }
                }
            }
        }
        check(total==expected && faceIndex==sourceFaces.size(),name+" face count "+total);
        System.out.println("PASS resource "+name+": "+total+" faces, "+mesh.parts.size()+" ordered parts; native emitted bytes checked");
    }

    static void reload() throws Exception {
        final ResourceLocation location=new ResourceLocation("hbm","models/fixture.obj");
        class Resources implements IResourceManager {
            String text=VERTICES+TRI;int reads;boolean fail,failClose;
            public Set<String> getResourceDomains(){return Collections.singleton("hbm");}
            public IResource getResource(ResourceLocation requested) throws IOException {
                check(requested.equals(location),"exact requested resource");reads++;
                if(fail)throw new IOException("controlled failure");
                return new SimpleResource("fixture",location,new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)) {
                    public void close() throws IOException { if(failClose) throw new IOException("controlled close failure"); }
                },null,null);
            }
            public List<IResource> getAllResources(ResourceLocation requested) throws IOException {return Collections.singletonList(getResource(requested));}
        }
        Resources resources=new Resources();ReloadableObjMesh loaded=new ReloadableObjMesh(resources,location,false);
        check(resources.reads==1,"eager resource sampling");ObjMesh old=loaded.getMesh();resources.text=VERTICES+TRI+TRI;
        check(loaded.getMesh()==old && resources.reads==1,"no implicit reload or resampling");
        loaded.reload(resources);check(loaded.getMesh().parts.get(0).faces.size()==2,"explicit successful replacement");
        old=loaded.getMesh();resources.fail=true;boolean failed=false;
        try{loaded.reload(resources);}catch(IllegalStateException e){failed=e.getCause() instanceof IOException;}
        check(failed && loaded.getMesh()==old,"failed reload reports error and retains last complete snapshot");
        resources.fail=false;resources.failClose=true;failed=false;
        try{loaded.reload(resources);}catch(IllegalStateException e){failed=true;}
        check(failed && loaded.getMesh()==old,"close failure does not publish replacement");
    }
    public static void main(String[] args) throws Exception {
        fixtures();reload();Path root=Paths.get(args[0]);
        resource(root,"mobs/bot_prime_head.obj",680,Shading.LEGACY_FLAT);
        resource(root,"machines/stirling.obj",932,Shading.LEGACY_FLAT);
        resource(root,"machines/firebox.obj",348,Shading.HFR_SMOOTH);
        resource(root,"core_receiver.obj",Integer.parseInt(args[1]),Shading.LEGACY_FLAT);
        resource(root,"fluidtank.obj",Integer.parseInt(args[2]),Shading.LEGACY_FLAT);
        resource(root,"weapons/tom_flame.hmf",Integer.parseInt(args[3]),Shading.HMF);
        System.out.println("PASS "+assertions+" assertions: real BufferBuilder, full mesh core, eager/explicit reload, selectors, UV wrapping/material ignore, normals, primitives and HMF timing.");
        System.out.println("LIMIT: ModelWormHead compiled, not initialized/rendered. No GL draws, VBO upload/delete, live client, full-mod build or migration of existing HFR/HMF consumers.");
    }
}
