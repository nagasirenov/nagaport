package solaroracle;

import java.io.*;
import java.util.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Test-only reference adapters. The actual pristine renderer and named-part helper
 * are compiled separately after package/import adaptation, never rewritten as an oracle. */
public final class OriginalMirrorOracle {
    public static final class CapturedVertex {
        public float x,y,z,u,v,nx,ny,nz;
        public int color,brightness;
    }
    public static final class Result {
        public final List<CapturedVertex> vertices;
        public final boolean rendered,inventory3d;
        public final int inventoryVertices;
        public final double tx,ty,tz;
        Result(List<CapturedVertex> v,boolean r,boolean inventory,int count,double x,double y,double z) {
            vertices=v;rendered=r;inventory3d=inventory;inventoryVertices=count;tx=x;ty=y;tz=z;
        }
    }
    public static Result render(String source,TextureAtlasSprite sprite,boolean available,int dx,int dy,int dz) throws Exception {
        ResourceManager.solar_mirror=parse(source);
        Tessellator.instance=new Tessellator();
        final TileEntitySolarMirror tile=new TileEntitySolarMirror();
        tile.tX=dx;tile.tY=dy;tile.tZ=dz;
        IBlockAccess world=new IBlockAccess(){public int getBlockMetadata(int x,int y,int z){return 0;} public TileEntity getTileEntity(int x,int y,int z){return available?tile:null;}};
        Block block=new Block(sprite);RenderMirror renderer=new RenderMirror();
        boolean rendered=renderer.renderWorldBlock(world,0,0,0,block,0,new RenderBlocks());
        Tessellator t=Tessellator.instance;
        int before=t.vertices.size();renderer.renderInventoryBlock(block,0,0,new RenderBlocks());
        return new Result(new ArrayList<>(t.vertices),rendered,renderer.shouldRender3DInInventory(0),t.vertices.size()-before,t.tx,t.ty,t.tz);
    }
    static HFRWavefrontObject parse(String source)throws Exception {
        HFRWavefrontObject result=new HFRWavefrontObject();List<Vertex> points=new ArrayList<>();List<TextureCoordinate> uv=new ArrayList<>();S_GroupObject part=null;
        BufferedReader reader=new BufferedReader(new StringReader(source));String line;
        while((line=reader.readLine())!=null){String[] p=line.trim().split("\\s+");
            if(p[0].equals("v"))points.add(new Vertex(Float.parseFloat(p[1]),Float.parseFloat(p[2]),Float.parseFloat(p[3])));
            else if(p[0].equals("vt"))uv.add(new TextureCoordinate(Float.parseFloat(p[1]),1-Float.parseFloat(p[2])));
            else if(p[0].equals("o")||p[0].equals("g")){part=new S_GroupObject(p[1]);result.groupObjects.add(part);}
            else if(p[0].equals("f")){
                if(part==null){part=new S_GroupObject("Default");result.groupObjects.add(part);}
                S_Face face=new S_Face();face.vertices=new Vertex[p.length-1];face.textureCoordinates=new TextureCoordinate[p.length-1];
                for(int i=1;i<p.length;i++){String[] k=p[i].split("/");face.vertices[i-1]=points.get(Integer.parseInt(k[0])-1);face.textureCoordinates[i-1]=uv.get(Integer.parseInt(k[1])-1);}
                Vertex a=face.vertices[0],b=face.vertices[1],c=face.vertices[2];
                Vec3 v1=Vec3.createVectorHelper(b.x-a.x,b.y-a.y,b.z-a.z),v2=Vec3.createVectorHelper(c.x-a.x,c.y-a.y,c.z-a.z);
                Vec3 n=v1.crossProduct(v2).normalize();face.faceNormal=new Vertex((float)n.xCoord,(float)n.yCoord,(float)n.zCoord);part.faces.add(face);
            }
        }
        return result;
    }
}
class Vertex {float x,y,z;Vertex(float x,float y,float z){this.x=x;this.y=y;this.z=z;}}
class TextureCoordinate {float u,v;TextureCoordinate(float u,float v){this.u=u;this.v=v;}}
class S_Face {Vertex[] vertices;TextureCoordinate[] textureCoordinates;Vertex faceNormal;}
class S_GroupObject {String name;List<S_Face> faces=new ArrayList<>();S_GroupObject(String n){name=n;}}
class HFRWavefrontObject {List<S_GroupObject> groupObjects=new ArrayList<>();}
class ResourceManager {static HFRWavefrontObject solar_mirror;}
class SolarMirror {static int renderID=37;}
class TileEntity {int xCoord,yCoord,zCoord;}
class TileEntitySolarMirror extends TileEntity {int tX,tY,tZ;}
interface IBlockAccess {int getBlockMetadata(int x,int y,int z);TileEntity getTileEntity(int x,int y,int z);}
interface IIcon {float getInterpolatedU(double u);float getInterpolatedV(double v);}
class Block {final IIcon icon;Block(final TextureAtlasSprite sprite){icon=new IIcon(){public float getInterpolatedU(double u){return sprite.getInterpolatedU(u);}public float getInterpolatedV(double v){return sprite.getInterpolatedV(v);}};}IIcon getIcon(int side,int meta){return icon;}int getMixedBrightnessForBlock(IBlockAccess world,int x,int y,int z){return 0x00e000a0;}}
class RenderBlocks {IIcon overrideBlockTexture;boolean hasOverrideBlockTexture(){return overrideBlockTexture!=null;}}
interface ISimpleBlockRenderingHandler {
 void renderInventoryBlock(Block block,int metadata,int modelId,RenderBlocks renderer);
 boolean renderWorldBlock(IBlockAccess world,int x,int y,int z,Block block,int modelId,RenderBlocks renderer);
 boolean shouldRender3DInInventory(int id);int getRenderId();
}
class Tessellator {
 static Tessellator instance;final List<OriginalMirrorOracle.CapturedVertex> vertices=new ArrayList<>();double tx,ty,tz;float nx,ny,nz;int color,brightness;
 void setBrightness(int v){brightness=v;}
 void setNormal(float x,float y,float z){nx=x;ny=y;nz=z;}
 void setColorOpaque_F(float r,float g,float b){setColorOpaque((int)(r*255F),(int)(g*255F),(int)(b*255F));}
 void setColorOpaque(int r,int g,int b){r=Math.max(0,Math.min(255,r));g=Math.max(0,Math.min(255,g));b=Math.max(0,Math.min(255,b));color=0xff000000|(b<<16)|(g<<8)|r;}
 void addTranslation(float x,float y,float z){tx+=x;ty+=y;tz+=z;}
 void addVertexWithUV(double x,double y,double z,double u,double v){OriginalMirrorOracle.CapturedVertex q=new OriginalMirrorOracle.CapturedVertex();q.x=(float)(x+tx);q.y=(float)(y+ty);q.z=(float)(z+tz);q.u=(float)u;q.v=(float)v;q.nx=nx;q.ny=ny;q.nz=nz;q.color=color;q.brightness=brightness;vertices.add(q);}
}
class Vec3 {
 double xCoord,yCoord,zCoord;static Vec3 createVectorHelper(double x,double y,double z){Vec3 v=new Vec3();v.xCoord=x;v.yCoord=y;v.zCoord=z;return v;}
 void rotateAroundX(float angle){double c=net.minecraft.util.math.MathHelper.cos(angle),s=net.minecraft.util.math.MathHelper.sin(angle),y=yCoord*c+zCoord*s,z=zCoord*c-yCoord*s;yCoord=y;zCoord=z;}
 void rotateAroundY(float angle){double c=net.minecraft.util.math.MathHelper.cos(angle),s=net.minecraft.util.math.MathHelper.sin(angle),x=xCoord*c+zCoord*s,z=zCoord*c-xCoord*s;xCoord=x;zCoord=z;}
 void rotateAroundZ(float angle){double c=net.minecraft.util.math.MathHelper.cos(angle),s=net.minecraft.util.math.MathHelper.sin(angle),x=xCoord*c+yCoord*s,y=yCoord*c-xCoord*s;xCoord=x;yCoord=y;}
 Vec3 crossProduct(Vec3 v){return createVectorHelper(yCoord*v.zCoord-zCoord*v.yCoord,zCoord*v.xCoord-xCoord*v.zCoord,xCoord*v.yCoord-yCoord*v.xCoord);}
 Vec3 normalize(){double length=(float)Math.sqrt(xCoord*xCoord+yCoord*yCoord+zCoord*zCoord);return length<1E-4D?createVectorHelper(0,0,0):createVectorHelper(xCoord/length,yCoord/length,zCoord/length);}
}
