import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.lang.reflect.*;
import com.google.gson.*;
import javax.vecmath.Matrix4f;
import com.hbm.render.item.nativeitem.*;
import com.hbm.render.item.nativeitem.staticfamily.*;
import com.hbm.render.mesh.*;
import net.minecraft.item.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

/** Independent original caller oracle and complete production recipe/model registration probe. No GL. */
public final class StaticItemFamilyNativeTest {
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

    static class Sprite extends TextureAtlasSprite { Sprite(String name) {super(name);} }
    static class RealMeshData implements MeshModel {
        final ObjMesh mesh;
        RealMeshData(ObjMesh mesh) { this.mesh=mesh; }
        public ObjMesh getMesh(){return mesh;}
        public List<String> getPartNames(){List<String> names=new ArrayList<>();for(ObjMesh.Part part:mesh.parts)names.add(part.name);return names;}
        public void renderAll(){throw new AssertionError("Probe must not pretend to render GL");}
        public void renderPart(String name){renderAll();}
        public void renderOnly(String... names){renderAll();}
        public void renderAllExcept(String... names){renderAll();}
    }
    static double literal(String value) {return Double.parseDouble(value.trim().replaceAll("[fFdD]$", ""));}
    static double[][] originalInventory(String body) {
        double[][] value=identity();
        Matcher matcher=Pattern.compile("GL11\\.(gl\\w+)\\((.*?)\\);").matcher(body);
        while(matcher.find()) {
            String op=matcher.group(1);String[] args=matcher.group(2).split(",");double[] n=new double[args.length];
            for(int i=0;i<n.length;i++)n[i]=literal(args[i]);
            if(op.contains("Translate")) value=mul(value,t(n[0],n[1],n[2]));
            else if(op.contains("Scale"))value=mul(value,s(n[0],n[1],n[2]));
            else if(op.contains("Rotate")) {
                int axis=n[1]!=0?0:n[2]!=0?1:2; value=mul(value,r(axis,n[0]*Math.signum(n[axis+1])));
            } else throw new AssertionError(op);
        }
        return value;
    }
    static void matrices(StaticItemDescriptor descriptor,String inventory) {
        Map<TransformType,Matrix4f> p=StaticItemPerspectives.complete(descriptor.inventory());double[][] d=t(-.5,-.5,-.5);
        equal(originalInventory(inventory),from(descriptor.inventory()),"literal inventory "+descriptor.item().getRegistryName());
        equal(seq(t(10,20,-3),t(8,10,0),r(0,-30),r(1,45),s(-1),originalInventory(inventory)),
                seq(t(18,28,100),s(16,-16,16),from(p.get(TransformType.GUI)),d),"GUI original vs native");
        for(int i=0;i<=200;i++) {
            double swing=i/200D,equip=1-i/200D,a=Math.sin(Math.PI*swing*swing),b=Math.sin(Math.PI*Math.sqrt(swing));
            double[][] v=seq(t(-.4*b,.2*Math.sin(2*Math.PI*Math.sqrt(swing)),-.2*Math.sin(Math.PI*swing)),t(.56,-.52-.6*equip,-.72));
            double[][] w=seq(r(1,45-20*a),r(2,-20*b),r(0,-80*b));
            equal(seq(v,w,s(.4),h(),t(.5,.25,0),s(.25),r(1,90)),seq(v,w,r(1,-45),from(p.get(TransformType.FIRST_PERSON_RIGHT_HAND)),d),"first "+i);
            double[][] drop=seq(t(i/13D,.1*Math.sin(i),-.7),r(1,i*3.3));
            equal(seq(drop,s(.5),s(1.5),s(.25),r(1,90)),seq(drop,t(0,.25,0),from(p.get(TransformType.GROUND)),d),"drop "+i);
            float x=(float)(i/113D),y=(float)(i/207D),z=(float)(i/331D);
            double[][] arm=seq(t(-.3125,.125,0),r(2,z*57.295776F),r(1,y*57.295776F),r(0,x*57.295776F));
            double[][] old=seq(arm,t(-.0625,.4375,.0625),t(.25,.1875,-.1875),s(.375),r(2,60),r(0,-90),r(2,20),h(),t(.5,.25,0),s(.25));
            equal(old,seq(arm,armSuffix(),from(p.get(TransformType.THIRD_PERSON_RIGHT_HAND)),d),"third "+i);
            Matrix4f sneak=OrdinaryMeshItemModel.compensateSneaking(p.get(TransformType.THIRD_PERSON_RIGHT_HAND),x,y,z,false);
            equal(old,seq(t(0,(double).2F,0),arm,armSuffix(),from(sneak),d),"sneak "+i);
            double[][] leftArm=seq(t(.3125,.125,0),r(2,z*57.295776F),r(1,y*57.295776F),r(0,x*57.295776F));
            double[][] leftSuffix=seq(r(0,-90),r(1,180),t(-.0625,.125,-.625)),flip=s(-1,1,1);
            Matrix4f left=p.get(TransformType.THIRD_PERSON_LEFT_HAND),leftSneak=OrdinaryMeshItemModel.compensateSneaking(left,x,y,z,true);
            equal(seq(leftArm,leftSuffix,flip,from(left),flip,d),seq(t(0,(double).2F,0),leftArm,leftSuffix,flip,from(leftSneak),flip,d),"left sneak "+i);
        }
        double[][] offsets={{0,0,0},{-.16,-.16,0},{0,-.32,0},{.16,-.16,0}};
        for(int k=0;k<4;k++) {
            double[][] f=seq(t(2,7,-4),r(1,180-90*k),t(0,0,.4375));
            double[][] old=seq(f,t(0,-.18,.015625),r(1,180),r(2,-90*k),t(offsets[k][0],offsets[k][1],0),t(0,.1,0),s(.5),s(1.5),s(.25),r(1,90));
            double[] delta=AngelCoreFrameCorrection.translation(k*2);
            equal(old,seq(f,r(2,90*k),t(delta[0],delta[1],delta[2]),s(.5),from(p.get(TransformType.FIXED)),d),"old frame "+k);
        }
        for(int k=0;k<8;k++) {
            double[] delta=AngelCoreFrameCorrection.translation(k);
            check(Math.abs(Math.hypot(delta[0],delta[1])-.02)<1E-12,"approved continuous pivot");
        }
        equal(from(p.get(TransformType.FIRST_PERSON_LEFT_HAND)),from(p.get(TransformType.FIRST_PERSON_RIGHT_HAND)),"approved Forge flipX");
        equal(seq(s(.1875),r(1,90)),seq(from(p.get(TransformType.HEAD)),d),"approved centered HEAD");
        equal(from(p.get(TransformType.HEAD)),from(p.get(TransformType.NONE)),"approved NONE");
    }
    static Object allocate(Class<?> type)throws Exception {Field field=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");field.setAccessible(true);return ((sun.misc.Unsafe)field.get(null)).allocateInstance(type);}
    static void set(Object target,String name,Object value)throws Exception {Field field=target.getClass().getDeclaredField(name);field.setAccessible(true);field.set(target,value);}
    public static void main(String[] args)throws Exception {
        net.minecraft.init.Bootstrap.register();
        JsonObject data=new JsonParser().parse(new FileReader(args[0])).getAsJsonObject();
        List<StaticItemDescriptor> descriptors=new ArrayList<>();Map<String,RealMeshData> meshes=new HashMap<>();
        JsonObject assets=data.getAsJsonObject("assets"); Path root=Paths.get(args[1]);
        for(Map.Entry<String,JsonElement> entry:assets.entrySet()) if(entry.getValue().getAsString().endsWith(".obj")) {
            try(Reader reader=Files.newBufferedReader(root.resolve("src/main/resources/assets/hbm").resolve(entry.getValue().getAsString()))) {
                ObjMesh mesh=ObjMeshParser.parse(reader,entry.getValue().getAsString(),false);
                check(!mesh.parts.isEmpty(),"real mesh "+entry.getKey());
                int faceCount=0,cornerCount=0;for(ObjMesh.Part part:mesh.parts)for(ObjMesh.Face face:part.faces){faceCount++;for(ObjMesh.Corner corner:face.corners){cornerCount++;check(Float.isFinite(corner.position.x)&&Float.isFinite(corner.position.y)&&Float.isFinite(corner.position.z),"finite original mesh vertex");}}
                check(faceCount>0&&cornerCount>0,"real complete asset has faces/corners "+entry.getKey());meshes.put(entry.getKey(),new RealMeshData(mesh));
            }
        }
        for(JsonElement element:data.getAsJsonArray("recipes")) {
            JsonObject recipe=element.getAsJsonObject();String name=recipe.get("method").getAsString();
            Item item=new Item().setRegistryName("hbm","static_native_probe_"+name);net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.register(item);List<Object> values=new ArrayList<>();List<Class<?>> types=new ArrayList<>();values.add(item);types.add(Item.class);
            for(JsonElement mesh:recipe.getAsJsonArray("mesh_fields")){values.add(meshes.get(mesh.getAsString()));types.add(MeshModel.class);}
            for(JsonElement material:recipe.getAsJsonArray("texture_fields")){values.add(new ResourceLocation("hbm",assets.get(material.getAsString()).getAsString()));types.add(ResourceLocation.class);}
            StaticItemDescriptor descriptor=(StaticItemDescriptor)StaticItemFamilyDescriptors.class.getMethod(name,types.toArray(new Class<?>[0])).invoke(null,values.toArray());
            descriptors.add(descriptor);matrices(descriptor,recipe.get("inventory").getAsString());
            Matrix4f changed=descriptor.inventory();changed.m00=987;check(descriptor.inventory().m00!=987,"inventory snapshot");
            String common=recipe.get("common").getAsString();Matcher parts=Pattern.compile("ResourceManager\\.(\\w+)\\.(renderPart|renderOnly)\\(([^)]*)\\)").matcher(common);
            while(parts.find()){Matcher names=Pattern.compile("\"([^\"]+)\"").matcher(parts.group(3));while(names.find())check(!meshes.get(parts.group(1)).mesh.selectParts(names.group(1)).isEmpty(),"actual selected part "+name+":"+names.group(1));}
        }
        StaticItemFamilyRegistration family=new StaticItemFamilyRegistration(descriptors, location->new Sprite(location.toString()));
        check(family.items().size()==descriptors.size(),"all exact identity owners");
        boolean duplicate=false;try{new StaticItemFamilyRegistration(Arrays.asList(descriptors.get(0),descriptors.get(0)));}catch(IllegalArgumentException expected){duplicate=true;}check(duplicate,"duplicate owners rejected");
        family.register(new net.minecraftforge.client.event.ModelRegistryEvent());
        net.minecraft.util.registry.RegistrySimple<ModelResourceLocation,IBakedModel> registry=new net.minecraft.util.registry.RegistrySimple<>();
        net.minecraftforge.client.event.ModelBakeEvent event=new net.minecraftforge.client.event.ModelBakeEvent(null,registry,null);
        family.bake(event);List<IBakedModel> first=new ArrayList<>();
        // Failure before publication must leave an existing native registry and all item bindings untouched.
        final int[] lookups={0};StaticItemFamilyRegistration invalid=new StaticItemFamilyRegistration(descriptors, location->{if(++lookups[0]==2)return null;return new Sprite(location.toString());});
        IBakedModel previous=registry.getObject(descriptors.get(0).modelLocation());boolean rejected=false;
        try{invalid.bake(event);}catch(IllegalArgumentException expected){rejected=true;}
        check(rejected&&registry.getObject(descriptors.get(0).modelLocation())==previous,"invalid bake validates before publication");
        for(StaticItemDescriptor descriptor:descriptors){IBakedModel model=registry.getObject(descriptor.modelLocation());first.add(model);check(model!=null&&model.isBuiltInRenderer(),"actual native model publication");check(model.isGui3d(),"native 3d ground path");check(descriptor.item().getTileEntityItemStackRenderer()==descriptor.renderer(),"actual TEISR ownership");check(model.getParticleTexture().getIconName().equals(descriptor.particle().toString()),"real material sprite identity");}
        ModelManager manager=(ModelManager)allocate(ModelManager.class);set(manager,"modelRegistry",registry);set(manager,"defaultModel",first.get(0));
        net.minecraftforge.client.ItemModelMesherForge mesher=new net.minecraftforge.client.ItemModelMesherForge(manager);
        net.minecraftforge.client.model.ModelLoader.onRegisterItems(mesher);
        for(StaticItemDescriptor descriptor:descriptors)for(int damage:new int[]{0,1,3,32767}) {
            ItemStack stack=new ItemStack(descriptor.item(),63,damage);net.minecraft.nbt.NBTTagCompound nbt=new net.minecraft.nbt.NBTTagCompound();nbt.setString("keep","unchanged");stack.setTagCompound(nbt);
            check(mesher.getItemModel(stack)==registry.getObject(descriptor.modelLocation()),"actual Forge mesher all damage "+damage);
            check(stack.getCount()==63&&stack.getMetadata()==damage&&stack.getTagCompound()==nbt,"lookup preserves stack and NBT");
        }
        family.bake(event);mesher.rebuildCache();
        for(int i=0;i<descriptors.size();i++){StaticItemDescriptor descriptor=descriptors.get(i);check(registry.getObject(descriptor.modelLocation())!=first.get(i),"reload new immutable model");check(mesher.getItemModel(new ItemStack(descriptor.item()))==registry.getObject(descriptor.modelLocation()),"native mesher reload identity");}
        check(registry.getKeys().size()==descriptors.size(),"reload retains exact ownership keys");
        System.out.println("PASS "+checks+" assertions for "+descriptors.size()+" complete static recipes/matrices/native model ownership/real asset groups; no GL or client parity claim.");
    }
}
