import java.nio.file.*;
import java.io.*;
import java.util.*;
import javax.vecmath.Matrix4f;
import com.hbm.render.item.nativeitem.*;
import com.hbm.render.mesh.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Independent double-precision original caller/draw oracle; production uses vecmath float matrices. */
public final class AngelCoreNativeTest {
    static int checks;
    static void check(boolean value, String label) { checks++; if(!value) throw new AssertionError(label); }
    static double[][] identity() { double[][] a = new double[4][4]; for(int i=0;i<4;i++)a[i][i]=1; return a; }
    static double[][] mul(double[][] a, double[][] b) {
        double[][] c=new double[4][4];
        for(int i=0;i<4;i++)for(int j=0;j<4;j++)for(int k=0;k<4;k++)c[i][j]+=a[i][k]*b[k][j];return c;
    }
    static double[][] seq(double[][]... ops) { double[][] a=identity();for(double[][] b:ops)a=mul(a,b);return a; }
    static double[][] t(double x,double y,double z) {double[][]a=identity();a[0][3]=x;a[1][3]=y;a[2][3]=z;return a;}
    static double[][] s(double x,double y,double z) {double[][]a=identity();a[0][0]=x;a[1][1]=y;a[2][2]=z;return a;}
    static double[][] s(double x) {return s(x,x,x);}
    static double[][] r(int axis,double angle) {
        double[][]a=identity();int p=(axis+1)%3,q=(axis+2)%3;double c=Math.cos(Math.toRadians(angle)),v=Math.sin(Math.toRadians(angle));
        a[p][p]=c;a[q][q]=c;a[p][q]=-v;a[q][p]=v;return a;
    }
    static double[][] from(Matrix4f m) {double[][]a=new double[4][4];for(int i=0;i<4;i++)for(int j=0;j<4;j++)a[i][j]=m.getElement(i,j);return a;}
    static void equal(double[][] a,double[][] b,String label) {
        // Compare transformed basis/corner vertices, not another invocation of production's factory.
        double[][] points={{0,0,0,1},{1,0,0,1},{0,1,0,1},{0,0,1,1},{-.31,2.7,-1.8,1}};
        for(double[] p:points)for(int row=0;row<4;row++) {double x=0,y=0;for(int k=0;k<4;k++){x+=a[row][k]*p[k];y+=b[row][k]*p[k];}
            check(Math.abs(x-y)<2E-4,label+" row "+row+": "+x+" != "+y);}
    }
    static double[][] h() {return seq(t(0,-.3,0),s(1.5),r(1,50),r(2,335),t(-.9375,-.0625,0));}
    static double[][] armSuffix() {return seq(r(0,-90),r(1,180),t(.0625,.125,-.625));}
    public static void main(String[] args)throws Exception {
        net.minecraft.client.resources.SimpleReloadableResourceManager resources =
                new net.minecraft.client.resources.SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer());
        resources.reloadResourcePack(new net.minecraft.client.resources.FolderResourcePack(new File(args[1])));
        MaterialTextureAtlasSprite particle = new MaterialTextureAtlasSprite(AngelCoreModelHandler.MATERIAL, 4);
        check(particle.hasCustomLoader(resources, AngelCoreModelHandler.MATERIAL), "real material custom loader");
        check(!particle.load(resources, AngelCoreModelHandler.MATERIAL, ignored -> null), "continue native stitching");
        check(particle.getSourceWidth()==116 && particle.getSourceHeight()==52, "actual rectangular core PNG dimensions");
        check(particle.getIconWidth()==128 && particle.getIconHeight()==128, "mip-aligned particle-only square canvas");
        java.awt.image.BufferedImage source=javax.imageio.ImageIO.read(new File(args[1], "assets/hbm/textures/entity/bfa_body.png"));
        int[] sourcePixels=source.getRGB(0,0,116,52,null,0,116), atlasPixels=particle.getFrameTextureData(0)[0];
        for(int y=0;y<52;y++)for(int x=0;x<116;x++)check(sourcePixels[y*116+x]==atlasPixels[y*128+x],"unchanged source material pixels");
        Map<TransformType,Matrix4f> p=AngelCorePerspectives.derived();double[][] d=t(-.5,-.5,-.5);
        equal(seq(t(10,20,-3),t(8,10,0),r(0,-30),r(1,45),s(-1),t(0,-3,0),s(4),s(4)),
                seq(t(18,28,100),s(16,-16,16),from(p.get(TransformType.GUI)),d),"GUI original vs native");
        for(int i=0;i<=200;i++) {
            double swing=i/200D,equip=1-i/200D;
            double a=Math.sin(Math.PI*swing*swing),b=Math.sin(Math.PI*Math.sqrt(swing));
            double[][] v=seq(t(-.4*b,.2*Math.sin(2*Math.PI*Math.sqrt(swing)),-.2*Math.sin(Math.PI*swing)),t(.56,-.52-.6*equip,-.72));
            double[][] w=seq(r(1,45-20*a),r(2,-20*b),r(0,-80*b));
            equal(seq(v,w,s(.4),h(),t(.5,.25,0),s(.25),r(1,90),s(4)),
                    seq(v,w,r(1,-45),from(p.get(TransformType.FIRST_PERSON_RIGHT_HAND)),d),"first person "+i);
            double[][] drop=seq(t(i/13D,.1*Math.sin(i),-.7),r(1,i*3.3));
            equal(seq(drop,s(.5),s(1.5),s(.25),r(1,90),s(4)),
                    seq(drop,t(0,.25,0),from(p.get(TransformType.GROUND)),d),"ground "+i);
            float x=(float)(i/113D),y=(float)(i/207D),z=(float)(i/331D);
            double[][] arm=seq(t(-.3125,.125,0),r(2,z*57.295776F),r(1,y*57.295776F),r(0,x*57.295776F));
            double[][] oldHeld=seq(arm,t(-.0625,.4375,.0625),t(.25,.1875,-.1875),s(.375),r(2,60),r(0,-90),r(2,20),h(),t(.5,.25,0),s(.25),s(4));
            equal(oldHeld,seq(arm,armSuffix(),from(p.get(TransformType.THIRD_PERSON_RIGHT_HAND)),d),"third person "+i);
            Matrix4f sneak=AngelCoreItemModel.compensateSneaking(p.get(TransformType.THIRD_PERSON_RIGHT_HAND),x,y,z,false);
            equal(oldHeld,seq(t(0,(double).2F,0),arm,armSuffix(),from(sneak),d),"sneak arm-local "+i);
        }
        double[][] offsets={{0,0,0},{-.16,-.16,0},{0,-.32,0},{.16,-.16,0}};
        for(int k=0;k<4;k++) {
            double[][] f=seq(t(2,7,-4),r(1,180-90*k),t(0,0,.4375));
            double[][] old=seq(f,t(0,-.18,.015625),r(1,180),r(2,-90*k),t(offsets[k][0],offsets[k][1],0),t(0,.1,0),s(.5),s(1.5),s(.25),r(1,90),s(4));
            double[] delta=AngelCoreFrameCorrection.translation(k*2);
            equal(old,seq(f,r(2,90*k),t(delta[0],delta[1],delta[2]),s(.5),from(p.get(TransformType.FIXED)),d),"frame "+k);
        }
        StaticMeshItemModel model=new StaticMeshItemModel(p,particle);
        check(model.isBuiltInRenderer(),"native TEISR dispatch");check(model.isGui3d(),"ground lift/copy policy");
        check(model.getQuads(null,null,1).isEmpty(),"complete geometry owned by TEISR");
        check(model.getOverrides()==ItemOverrideList.NONE,"static model has no mutable overrides");
        check(model.getParticleTexture().getIconName().equals(AngelCoreModelHandler.PARTICLE.toString()),"real texture particle");
        Matrix4f first=model.handlePerspective(TransformType.GROUND).getRight();first.m00=999;
        check(model.handlePerspective(TransformType.GROUND).getRight().m00!=999,"defensive returned matrix");
        p.get(TransformType.GROUND).m00=888;
        check(model.handlePerspective(TransformType.GROUND).getRight().m00!=888,"defensive input matrix");
        check(!model.hasEveryNativePerspective(),"unapproved new contexts stay gated");
        boolean rejected=false;try{StaticMeshItemRegistration.requireComplete(model);}catch(IllegalStateException e){rejected=true;}
        check(rejected,"incomplete model cannot activate registration");
        // Actual complete core model in the real native event registry and Item TEISR binding.
        net.minecraft.init.Bootstrap.register();
        net.minecraft.item.Item item = new net.minecraft.item.Item().setRegistryName("hbm", "native_core_probe");
        net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer renderer =
                new net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer();
        Map<TransformType,Matrix4f> complete = AngelCorePerspectives.complete();
        check(complete.size()==TransformType.values().length,"every approved native context present");
        equal(from(complete.get(TransformType.FIRST_PERSON_LEFT_HAND)),from(complete.get(TransformType.FIRST_PERSON_RIGHT_HAND)),"Forge-owned left-hand conjugation");
        equal(from(complete.get(TransformType.HEAD)),from(complete.get(TransformType.NONE)),"approved centered unusual contexts");
        for(int i=0;i<201;i++) {
            float x=(float)(i/117D),y=(float)(i/233D),z=(float)(i/319D);
            double[][] arm=seq(t(.3125,.125,0),r(2,z*57.295776F),r(1,y*57.295776F),r(0,x*57.295776F));
            double[][] leftSuffix=seq(r(0,-90),r(1,180),t(-.0625,.125,-.625));
            double[][] flip=s(-1,1,1);
            Matrix4f base=complete.get(TransformType.THIRD_PERSON_LEFT_HAND);
            Matrix4f corrected=AngelCoreItemModel.compensateSneaking(base,x,y,z,true);
            equal(seq(arm,leftSuffix,flip,from(base),flip,d),
                    seq(t(0,(double).2F,0),arm,leftSuffix,flip,from(corrected),flip,d),"left native sneak compensation "+i);
        }
        final int[] bakes={0};
        StaticMeshItemRegistration registration=new StaticMeshItemRegistration(item,renderer,()->{
            bakes[0]++;return new AngelCoreItemModel(complete,particle);},0);
        net.minecraft.util.registry.RegistrySimple<ModelResourceLocation,IBakedModel> registry=
                new net.minecraft.util.registry.RegistrySimple<>();
        net.minecraftforge.client.event.ModelBakeEvent event=new net.minecraftforge.client.event.ModelBakeEvent(null,registry,null);
        registration.bake(event);
        IBakedModel firstBake=registry.getObject(StaticMeshItemRegistration.modelLocation(item));
        check(firstBake!=null&&firstBake.isBuiltInRenderer(),"native event installs real built-in model");
        check(item.getTileEntityItemStackRenderer()==renderer,"actual native Item TEISR binding");
        registration.bake(event);
        check(bakes[0]==2&&registry.getObject(StaticMeshItemRegistration.modelLocation(item))!=firstBake,"reload re-bakes native model");
        check(registry.getKeys().size()==1,"reload retains ownership key");
        try(Reader in=Files.newBufferedReader(Paths.get(args[0]))) {
            ObjMesh mesh=ObjMeshParser.parse(in,"balefire_angel.obj",false);
            List<ObjMesh.Part> body=mesh.selectParts("body");check(!body.isEmpty(),"actual original body group");
            int faces=0,corners=0;for(ObjMesh.Part part:body)for(ObjMesh.Face face:part.faces){faces++;for(ObjMesh.Corner corner:face.corners){corners++;check(corner.uv!=null,"original body UV");check(corner.normal!=null,"original smooth normal");}}
            check(faces>0&&corners>0,"body nonempty");System.out.println("Actual body: "+faces+" faces, "+corners+" corners");
        }
        System.out.println("PASS "+checks+" independent matrix/native model/asset assertions; no GL or full-client parity claim.");
    }
}
