package com.hbm.render.block.nativeblock;

import java.awt.Color;
import java.util.*;
import com.hbm.blocks.machine.FoundryModelState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

/** Pure native faces. Bounds, material, offsets and branches retain the original foundry geometry. */
public final class FoundryGeometry {
    public enum Kind { BASIN, MOLD, CHANNEL, TANK, OUTLET, SLAGTAP }
    private FoundryGeometry() { }
    public static List<BakedQuad> build(Kind kind, boolean inventory, boolean fluid, int meta, FoundryModelState data, FoundryMaterials materials) {
        List<BakedQuad> quads=new ArrayList<>();
        switch(kind) {
        case BASIN: if(inventory) basinItem(quads,true,fluid,meta,data,materials); else basinWorld(quads,false,fluid,meta,data,materials); break;
        case MOLD: if(inventory) moldItem(quads,true,fluid,meta,data,materials); else moldWorld(quads,false,fluid,meta,data,materials); break;
        case CHANNEL: if(inventory) channelItem(quads,true,fluid,meta,data,materials); else channelWorld(quads,false,fluid,meta,data,materials); break;
        case TANK: if(inventory) tankItem(quads,true,fluid,meta,data,materials); else tankWorld(quads,false,fluid,meta,data,materials); break;
        case SLAGTAP:
        case OUTLET: if(inventory) outletItem(quads,true,fluid,meta,data,materials); else outletWorld(quads,false,fluid,meta,data,materials); break;
        default: throw new AssertionError(kind);
        }
        return Collections.unmodifiableList(quads);
    }
    private static void emit(List<BakedQuad> quads, boolean inventory, boolean fluid, AxisAlignedBB b, EnumFacing face, TextureAtlasSprite sprite,
            double x, double y, double z, boolean mirror, float r, float g, float blue, boolean emissive) {
        if(fluid != emissive) return;
        BakedQuad source=ConveyorCuboidGeometry.face(sprite,inventory,face,0,b.minX,b.minY,b.minZ,b.maxX,b.maxY,b.maxZ,0);
        VertexFormat format=fluid?DefaultVertexFormats.BLOCK:DefaultVertexFormats.ITEM;
        UnpackedBakedQuad.Builder builder=new UnpackedBakedQuad.Builder(format);
        builder.setTexture(sprite);builder.setQuadTint(-1);builder.setQuadOrientation(face);builder.setApplyDiffuseLighting(inventory);builder.setContractUVs(false);
        int[] packed=source.getVertexData();int stride=source.getFormat().getIntegerSize();int uv=source.getFormat().getUvOffsetById(0)/4;
        for(int v=0;v<4;v++) {
            float u=Float.intBitsToFloat(packed[v*stride+uv]);
            float texV=Float.intBitsToFloat(packed[v*stride+uv+1]);
            // Original RenderBlocks reset the complete side-V interval when an
            // overfilled tank extended outside its unit block; do not clamp the fill.
            if(face.getAxis()!=EnumFacing.Axis.Y && (b.minY<0 || b.maxY>1))
                texV=Math.abs(Float.intBitsToFloat(packed[v*stride+1])-b.minY)<1E-6?sprite.getMaxV():sprite.getMinV();
            if(mirror && (face==EnumFacing.NORTH || face==EnumFacing.EAST)) {
                double shift=face==EnumFacing.NORTH?1-b.minX-b.maxX:1-b.minZ-b.maxZ;
                u+=(float)shift*(sprite.getMaxU()-sprite.getMinU());
            }
            for(int e=0;e<format.getElementCount();e++) {
                VertexFormatElement element=format.getElement(e);
                switch(element.getUsage()) {
                case POSITION:builder.put(e,Float.intBitsToFloat(packed[v*stride])+(float)x,Float.intBitsToFloat(packed[v*stride+1])+(float)y,Float.intBitsToFloat(packed[v*stride+2])+(float)z,1);break;
                case COLOR:builder.put(e,(int)(r*255)/255F,(int)(g*255)/255F,(int)(blue*255)/255F,1);break;
                case UV:
                    if(element.getIndex()==0) builder.put(e,u,texV,0,1);
                    else builder.put(e,240F/32767F,0,0,1);
                    break;
                case NORMAL:builder.put(e,face.getXOffset(),face.getYOffset(),face.getZOffset(),0);break;
                default:builder.put(e);
                }
            }
        }
        BakedQuad quad=builder.build();quads.add(inventory||fluid?quad:new BlockPositionLightQuad(quad));
    }
    private static void basinItem(List<BakedQuad> quads, boolean inventory, boolean fluid, int meta, FoundryModelState data, FoundryMaterials materials) {
        double x=0,y=0,z=0;
        AxisAlignedBB bounds=new AxisAlignedBB(0,0,0,1,1D,1);
        boolean mirror=false, emissive=false;
        float r=1,g=1,b=1,red=1,green=1,blue=1;
        float mulBottom=.5F,mulTop=1,mulZ=.8F,mulX=.6F;
		emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Bottom"), x, y - 0.875D, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.DOWN, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Inner"), x - 0.875D, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Inner"), x + 0.875D, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Inner"), x, y, z - 0.875D, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Inner"), x, y, z + 0.875D, mirror, red, green, blue, emissive);
    }
    private static void basinWorld(List<BakedQuad> quads, boolean inventory, boolean fluid, int meta, FoundryModelState data, FoundryMaterials materials) {
        double x=0,y=0,z=0;
        AxisAlignedBB bounds=new AxisAlignedBB(0,0,0,1,(double)0.999F,1);
        boolean mirror=false, emissive=false;
        float r=1,g=1,b=1,red=1,green=1,blue=1;
        float mulBottom=.5F,mulTop=1,mulZ=.8F,mulX=.6F;
		red = r * mulTop; green = g * mulTop; blue = b * mulTop;
		emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Bottom"), x, y - 0.875D, z, mirror, red, green, blue, emissive);
		red = r * mulBottom; green = g * mulBottom; blue = b * mulBottom;
		emit(quads, inventory, fluid, bounds, EnumFacing.DOWN, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
		red = r * mulX; green = g * mulX; blue = b * mulX;
		emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Inner"), x - 0.875D, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Inner"), x + 0.875D, y, z, mirror, red, green, blue, emissive);
		red = r * mulZ; green = g * mulZ; blue = b * mulZ;
		emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Inner"), x, y, z - 0.875D, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Inner"), x, y, z + 0.875D, mirror, red, green, blue, emissive);
    }
    private static void moldItem(List<BakedQuad> quads, boolean inventory, boolean fluid, int meta, FoundryModelState data, FoundryMaterials materials) {
        double x=0,y=0,z=0;
        AxisAlignedBB bounds=new AxisAlignedBB(0,0,0,1,.5D,1);
        boolean mirror=false, emissive=false;
        float r=1,g=1,b=1,red=1,green=1,blue=1;
        float mulBottom=.5F,mulTop=1,mulZ=.8F,mulX=.6F;
		bounds = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
		emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Bottom"), x, y - 0.375D, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.DOWN, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Inner"), x - 0.875D, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Inner"), x + 0.875D, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Inner"), x, y, z - 0.875D, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Inner"), x, y, z + 0.875D, mirror, red, green, blue, emissive);
    }
    private static void moldWorld(List<BakedQuad> quads, boolean inventory, boolean fluid, int meta, FoundryModelState data, FoundryMaterials materials) {
        double x=0,y=0,z=0;
        AxisAlignedBB bounds=new AxisAlignedBB(0,0,0,1,0.5D,1);
        boolean mirror=false, emissive=false;
        float r=1,g=1,b=1,red=1,green=1,blue=1;
        float mulBottom=.5F,mulTop=1,mulZ=.8F,mulX=.6F;
		red = r * mulTop; green = g * mulTop; blue = b * mulTop;
		emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Bottom"), x, y - 0.375D, z, mirror, red, green, blue, emissive);
		red = r * mulBottom; green = g * mulBottom; blue = b * mulBottom;
		emit(quads, inventory, fluid, bounds, EnumFacing.DOWN, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
		red = r * mulX; green = g * mulX; blue = b * mulX;
		emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Inner"), x - 0.875D, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Inner"), x + 0.875D, y, z, mirror, red, green, blue, emissive);
		red = r * mulZ; green = g * mulZ; blue = b * mulZ;
		emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Inner"), x, y, z - 0.875D, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Inner"), x, y, z + 0.875D, mirror, red, green, blue, emissive);
    }
    private static void channelItem(List<BakedQuad> quads, boolean inventory, boolean fluid, int meta, FoundryModelState data, FoundryMaterials materials) {
        double x=0,y=0,z=0;
        AxisAlignedBB bounds=new AxisAlignedBB(0,0,0,1,1D,1);
        boolean mirror=false, emissive=false;
        float r=1,g=1,b=1,red=1,green=1,blue=1;
        float mulBottom=.5F,mulTop=1,mulZ=.8F,mulX=.6F;
		double height = 0.5D;
		//center
		bounds = new AxisAlignedBB(0.375D, 0D, 0.375D, 0.625D, 0.125D, 0.625D);
		emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
		bounds = new AxisAlignedBB(0.3125D, 0D, 0.3125D, 0.6875D, 0.125D, 0.6875D);
		emit(quads, inventory, fluid, bounds, EnumFacing.DOWN, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
		//pos X bottom
		bounds = new AxisAlignedBB(0.625D, 0D, 0.3125D, 1D, 0.125D, 0.6875D);
		emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.DOWN, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		//pos X
		bounds = new AxisAlignedBB(0.625D, 0D, 0.3125D, 1D, height, 0.375D);
		emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		bounds = new AxisAlignedBB(0.625D, 0D, 0.625D, 1D, height, 0.6875D);
		emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		//neg x bottom
		bounds = new AxisAlignedBB(0D, 0D, 0.3125D, 0.375D, 0.125D, 0.6875D);
		emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.DOWN, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		//neg x
		bounds = new AxisAlignedBB(0D, 0D, 0.3125D, 0.375D, height, 0.375D);
		emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		bounds = new AxisAlignedBB(0D, 0D, 0.625D, 0.375D, height, 0.6875D);
		emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		//pos Z bottom
		bounds = new AxisAlignedBB(0.3125D, 0D, 0.625D, 0.6875D, 0.125D, 1D);
		emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.DOWN, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		//pos z
		bounds = new AxisAlignedBB(0.3125D, 0D, 0.625D, 0.375D, height, 1D);
		emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		bounds = new AxisAlignedBB(0.625D, 0D, 0.625D, 0.6875D, height, 1D);
		emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		//neg z bottom
		bounds = new AxisAlignedBB(0.3125D, 0D, 0D, 0.6875D, 0.125D, 0.375D);
		emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.DOWN, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		//neg z
		bounds = new AxisAlignedBB(0.3125D, 0D, 0D, 0.375D, height, 0.375D);
		emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		bounds = new AxisAlignedBB(0.625D, 0D, 0D, 0.6875D, height, 0.375D);
		emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
    }
    private static void channelWorld(List<BakedQuad> quads, boolean inventory, boolean fluid, int meta, FoundryModelState data, FoundryMaterials materials) {
        double x=0,y=0,z=0;
        AxisAlignedBB bounds=new AxisAlignedBB(0,0,0,1,1D,1);
        boolean mirror=false, emissive=false;
        float r=1,g=1,b=1,red=1,green=1,blue=1;
        float mulBottom=.5F,mulTop=1,mulZ=.8F,mulX=.6F;
boolean doRender = data.amount > 0 && data.material;
		double level = doRender ? data.amount * 0.25D / data.capacity : 0;
		Color color = doRender ? new Color(data.color).brighter() : null;
		if(color != null) {
			double brightener = 0.7D;
			int nr = (int) (255D - (255D - color.getRed()) * brightener);
			int ng = (int) (255D - (255D - color.getGreen()) * brightener);
			int nb = (int) (255D - (255D - color.getBlue()) * brightener);
			color = new Color(nr, ng, nb);
		}
		boolean posX = data.connects(EnumFacing.EAST);
		boolean negX = data.connects(EnumFacing.WEST);
		boolean posZ = data.connects(EnumFacing.SOUTH);
		boolean negZ = data.connects(EnumFacing.NORTH);
		double height = 0.5D;
		bounds = new AxisAlignedBB(0.375D, 0D, 0.375D, 0.625D, 0.125D, 0.625D);
		red = r * mulTop; green = g * mulTop; blue = b * mulTop;
		emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
		bounds = new AxisAlignedBB(0.3125D, 0D, 0.3125D, 0.6875D, 0.125D, 0.6875D);
		red = r * mulBottom; green = g * mulBottom; blue = b * mulBottom;
		emit(quads, inventory, fluid, bounds, EnumFacing.DOWN, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
		if(doRender) {
			bounds = new AxisAlignedBB(0.375D, 0.125D, 0.375D, 0.625D, 0.125D + level, 0.625D);
			red = color.getRed() / 255F; green = color.getGreen() / 255F; blue = color.getBlue() / 255F;
			emissive = true;
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Lava"), x, y, z, mirror, red, green, blue, emissive);
			emissive = false;
		}
		if(posX) {
			bounds = new AxisAlignedBB(0.625D, 0D, 0.3125D, 1D, 0.125D, 0.6875D);
			red = r * mulTop; green = g * mulTop; blue = b * mulTop;
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
			red = r * mulBottom; green = g * mulBottom; blue = b * mulBottom;
			emit(quads, inventory, fluid, bounds, EnumFacing.DOWN, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
			bounds = new AxisAlignedBB(0.625D, 0D, 0.3125D, 1D, height, 0.375D);
			red = r * mulTop; green = g * mulTop; blue = b * mulTop;
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
			red = r * mulZ; green = g * mulZ; blue = b * mulZ;
			emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
			bounds = new AxisAlignedBB(0.625D, 0D, 0.625D, 1D, height, 0.6875D);
			red = r * mulTop; green = g * mulTop; blue = b * mulTop;
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
			red = r * mulZ; green = g * mulZ; blue = b * mulZ;
			emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
			if(doRender) {
				bounds = new AxisAlignedBB(0.625D, 0.125D, 0.3125D, 1D, 0.125D + level, 0.6875D);
				red = color.getRed() / 255F; green = color.getGreen() / 255F; blue = color.getBlue() / 255F;
				emissive = true;
				emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Lava"), x, y, z, mirror, red, green, blue, emissive);
				emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Lava"), x, y, z, mirror, red, green, blue, emissive);
				emissive = false;
			}
		} else {
			bounds = new AxisAlignedBB(0.625, 0D, 0.3125D, 0.6875D, height, 0.6875D);
			red = r * mulTop; green = g * mulTop; blue = b * mulTop;
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
			red = r * mulX; green = g * mulX; blue = b * mulX;
			emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		}
		if(negX) {
			bounds = new AxisAlignedBB(0D, 0D, 0.3125D, 0.375D, 0.125D, 0.6875D);
			red = r * mulTop; green = g * mulTop; blue = b * mulTop;
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
			red = r * mulBottom; green = g * mulBottom; blue = b * mulBottom;
			emit(quads, inventory, fluid, bounds, EnumFacing.DOWN, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
			bounds = new AxisAlignedBB(0D, 0D, 0.3125D, 0.375D, height, 0.375D);
			red = r * mulTop; green = g * mulTop; blue = b * mulTop;
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
			red = r * mulZ; green = g * mulZ; blue = b * mulZ;
			emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
			bounds = new AxisAlignedBB(0D, 0D, 0.625D, 0.375D, height, 0.6875D);
			red = r * mulTop; green = g * mulTop; blue = b * mulTop;
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
			red = r * mulZ; green = g * mulZ; blue = b * mulZ;
			emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
			if(doRender) {
				bounds = new AxisAlignedBB(0D, 0.125D, 0.3125D, 0.375D, 0.125D + level, 0.6875D);
				red = color.getRed() / 255F; green = color.getGreen() / 255F; blue = color.getBlue() / 255F;
				emissive = true;
				emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Lava"), x, y, z, mirror, red, green, blue, emissive);
				emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Lava"), x, y, z, mirror, red, green, blue, emissive);
				emissive = false;
			}
		} else {
			bounds = new AxisAlignedBB(0.3125D, 0D, 0.3125D, 0.375D, height, 0.6875D);
			red = r * mulTop; green = g * mulTop; blue = b * mulTop;
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
			red = r * mulX; green = g * mulX; blue = b * mulX;
			emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
		}
		if(posZ) {
			bounds = new AxisAlignedBB(0.3125D, 0D, 0.625D, 0.6875D, 0.125D, 1D);
			red = r * mulTop; green = g * mulTop; blue = b * mulTop;
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
			red = r * mulBottom; green = g * mulBottom; blue = b * mulBottom;
			emit(quads, inventory, fluid, bounds, EnumFacing.DOWN, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
			bounds = new AxisAlignedBB(0.3125D, 0D, 0.625D, 0.375D, height, 1D);
			red = r * mulTop; green = g * mulTop; blue = b * mulTop;
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
			red = r * mulX; green = g * mulX; blue = b * mulX;
			emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
			bounds = new AxisAlignedBB(0.625D, 0D, 0.625D, 0.6875D, height, 1D);
			red = r * mulTop; green = g * mulTop; blue = b * mulTop;
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
			red = r * mulX; green = g * mulX; blue = b * mulX;
			emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
			if(doRender) {
				bounds = new AxisAlignedBB(0.3125D, 0.125D, 0.625D, 0.6875D, 0.125D + level, 1D);
				red = color.getRed() / 255F; green = color.getGreen() / 255F; blue = color.getBlue() / 255F;
				emissive = true;
				emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Lava"), x, y, z, mirror, red, green, blue, emissive);
				emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Lava"), x, y, z, mirror, red, green, blue, emissive);
				emissive = false;
			}
		} else {
			bounds = new AxisAlignedBB(0.3125D, 0D, 0.625D, 0.6875D, height, 0.6875D);
			red = r * mulTop; green = g * mulTop; blue = b * mulTop;
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
			red = r * mulZ; green = g * mulZ; blue = b * mulZ;
			emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		}
		if(negZ) {
			bounds = new AxisAlignedBB(0.3125D, 0D, 0D, 0.6875D, 0.125D, 0.375D);
			red = r * mulTop; green = g * mulTop; blue = b * mulTop;
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
			red = r * mulBottom; green = g * mulBottom; blue = b * mulBottom;
			emit(quads, inventory, fluid, bounds, EnumFacing.DOWN, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
			bounds = new AxisAlignedBB(0.3125D, 0D, 0D, 0.375D, height, 0.375D);
			red = r * mulTop; green = g * mulTop; blue = b * mulTop;
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
			red = r * mulX; green = g * mulX; blue = b * mulX;
			emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
			bounds = new AxisAlignedBB(0.625D, 0D, 0D, 0.6875D, height, 0.375D);
			red = r * mulTop; green = g * mulTop; blue = b * mulTop;
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
			red = r * mulX; green = g * mulX; blue = b * mulX;
			emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
			if(doRender) {
				bounds = new AxisAlignedBB(0.3125D, 0.125D, 0D, 0.6875D, 0.125D + level, 0.375D);
				red = color.getRed() / 255F; green = color.getGreen() / 255F; blue = color.getBlue() / 255F;
				emissive = true;
				emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Lava"), x, y, z, mirror, red, green, blue, emissive);
				emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Lava"), x, y, z, mirror, red, green, blue, emissive);
				emissive = false;
			}
		} else {
			bounds = new AxisAlignedBB(0.3125D, 0D, 0.3125D, 0.6875D, height, 0.375D);
			red = r * mulTop; green = g * mulTop; blue = b * mulTop;
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
			red = r * mulZ; green = g * mulZ; blue = b * mulZ;
			emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
		}
    }
    private static void tankItem(List<BakedQuad> quads, boolean inventory, boolean fluid, int meta, FoundryModelState data, FoundryMaterials materials) {
        double x=0,y=0,z=0;
        AxisAlignedBB bounds=new AxisAlignedBB(0,0,0,1,1D,1);
        boolean mirror=false, emissive=false;
        float r=1,g=1,b=1,red=1,green=1,blue=1;
        float mulBottom=.5F,mulTop=1,mulZ=.8F,mulX=.6F;
		emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Bottom"), x, y - 0.875D, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.DOWN, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Inner"), x - 0.875D, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Inner"), x + 0.875D, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Inner"), x, y, z - 0.875D, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Inner"), x, y, z + 0.875D, mirror, red, green, blue, emissive);
    }
    private static void tankWorld(List<BakedQuad> quads, boolean inventory, boolean fluid, int meta, FoundryModelState data, FoundryMaterials materials) {
        double x=0,y=0,z=0;
        AxisAlignedBB bounds=new AxisAlignedBB(0,0,0,1,1D,1);
        boolean mirror=false, emissive=false;
        float r=1,g=1,b=1,red=1,green=1,blue=1;
        float mulBottom=.5F,mulTop=1,mulZ=.8F,mulX=.6F;
boolean conPosX = data.connects(EnumFacing.EAST);
		boolean conNegX = data.connects(EnumFacing.WEST);
		boolean conPosZ = data.connects(EnumFacing.SOUTH);
		boolean conNegZ = data.connects(EnumFacing.NORTH);
		boolean conPosY = data.connects(EnumFacing.UP);
		boolean conNegY = data.connects(EnumFacing.DOWN);
		boolean outPosX = data.outlet(EnumFacing.EAST);
		boolean outNegX = data.outlet(EnumFacing.WEST);
		boolean outPosZ = data.outlet(EnumFacing.SOUTH);
		boolean outNegZ = data.outlet(EnumFacing.NORTH);
		boolean doRender = data.amount > 0 && data.material;
		double max = 0.75D + (conNegY ? 0.125D : 0) + (conPosY ? 0.125D : 0);
		double level = doRender ? data.amount * max / data.capacity : 0;
		Color color = doRender ? new Color(data.color).brighter() : null;
		if(color != null) {
			double brightener = 0.7D;
			int nr = (int) (255D - (255D - color.getRed()) * brightener);
			int ng = (int) (255D - (255D - color.getGreen()) * brightener);
			int nb = (int) (255D - (255D - color.getBlue()) * brightener);
			color = new Color(nr, ng, nb);
		}
		bounds = new AxisAlignedBB(0D, 0D, 0D, 1D, 1D, 1D);
		if(!conNegY) {
			bounds = new AxisAlignedBB(0D, 0D, 0D, 1D, 0.125D, 1D);
			red = r * mulTop; green = g * mulTop; blue = b * mulTop;
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
			red = r * mulBottom; green = g * mulBottom; blue = b * mulBottom;
			emit(quads, inventory, fluid, bounds, EnumFacing.DOWN, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
		}
		if(!conPosX) {
			bounds = new AxisAlignedBB(0.875D, 0D, 0D, 1D, 1D, 1D);
			red = r * mulX; green = g * mulX; blue = b * mulX;
			emit(quads, inventory, fluid, bounds, EnumFacing.EAST, conNegY ? (outPosX ? materials.get("SideUpperOutlet") : materials.get("SideUpper")) : (outPosX ? materials.get("SideOutlet") : materials.get("Side")), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.WEST, conPosY ? materials.get("Bottom") : materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
			red = r * mulZ; green = g * mulZ; blue = b * mulZ;
			if(conPosZ) emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, conPosY ? materials.get("Bottom") : materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
			if(conNegZ) emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, conPosY ? materials.get("Bottom") : materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
			red = r * mulTop; green = g * mulTop; blue = b * mulTop;
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
		}
		if(!conNegX) {
			bounds = new AxisAlignedBB(0D, 0D, 0D, 0.125D, 1D, 1D);
			red = r * mulX; green = g * mulX; blue = b * mulX;
			emit(quads, inventory, fluid, bounds, EnumFacing.EAST, conPosY ? materials.get("Bottom") : materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.WEST, conNegY ? (outNegX ? materials.get("SideUpperOutlet") : materials.get("SideUpper")) : (outNegX ? materials.get("SideOutlet") : materials.get("Side")), x, y, z, mirror, red, green, blue, emissive);
			red = r * mulZ; green = g * mulZ; blue = b * mulZ;
			if(conPosZ) emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, conPosY ? materials.get("Bottom") : materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
			if(conNegZ) emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, conPosY ? materials.get("Bottom") : materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
			red = r * mulTop; green = g * mulTop; blue = b * mulTop;
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
		}
		if(!conPosZ) {
			bounds = new AxisAlignedBB(0D, 0D, 0.875D, 1D, 1D, 1D);
			red = r * mulZ; green = g * mulZ; blue = b * mulZ;
			emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, conNegY ? (outPosZ ? materials.get("SideUpperOutlet") : materials.get("SideUpper")) : (outPosZ ? materials.get("SideOutlet") : materials.get("Side")), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, conPosY ? materials.get("Bottom") : materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
			red = r * mulX; green = g * mulX; blue = b * mulX;
			if(conPosX) emit(quads, inventory, fluid, bounds, EnumFacing.EAST, conPosY ? materials.get("Bottom") : materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
			if(conNegX) emit(quads, inventory, fluid, bounds, EnumFacing.WEST, conPosY ? materials.get("Bottom") : materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
			red = r * mulTop; green = g * mulTop; blue = b * mulTop;
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
		}
		if(!conNegZ) {
			bounds = new AxisAlignedBB(0D, 0D, 0D, 1D, 1D, 0.125D);
			red = r * mulZ; green = g * mulZ; blue = b * mulZ;
			emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, conPosY ? materials.get("Bottom") : materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, conNegY ? (outNegZ ? materials.get("SideUpperOutlet") : materials.get("SideUpper")) : (outNegZ ? materials.get("SideOutlet") : materials.get("Side")), x, y, z, mirror, red, green, blue, emissive);
			red = r * mulX; green = g * mulX; blue = b * mulX;
			if(conPosX) emit(quads, inventory, fluid, bounds, EnumFacing.EAST, conPosY ? materials.get("Bottom") : materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
			if(conNegX) emit(quads, inventory, fluid, bounds, EnumFacing.WEST, conPosY ? materials.get("Bottom") : materials.get("Inner"), x, y, z, mirror, red, green, blue, emissive);
			red = r * mulTop; green = g * mulTop; blue = b * mulTop;
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
		}
		if(doRender) {
			double height = conNegY ? 0D : 0.125D;
			bounds = new AxisAlignedBB(0D, height, 0D, 1D, height + level, 1D);
			red = color.getRed() / 255F; green = color.getGreen() / 255F; blue = color.getBlue() / 255F;
			emissive = true;
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Lava"), x, y, z, mirror, red, green, blue, emissive);
			if(conPosX) emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Lava"), x, y, z, mirror, red, green, blue, emissive);
			if(conNegX) emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Lava"), x, y, z, mirror, red, green, blue, emissive);
			if(conPosZ) emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Lava"), x, y, z, mirror, red, green, blue, emissive);
			if(conNegZ) emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Lava"), x, y, z, mirror, red, green, blue, emissive);
			emissive = false;
		}
    }
    private static void outletItem(List<BakedQuad> quads, boolean inventory, boolean fluid, int meta, FoundryModelState data, FoundryMaterials materials) {
        double x=0,y=0,z=0;
        AxisAlignedBB bounds=new AxisAlignedBB(0,0,0,1,1D,1);
        boolean mirror=false, emissive=false;
        float r=1,g=1,b=1,red=1,green=1,blue=1;
        float mulBottom=.5F,mulTop=1,mulZ=.8F,mulX=.6F;
		double bot = 0.0D;
		double top = 0.5D;
		bounds = new AxisAlignedBB(0.3125D, bot, 0D, 0.6875D, top, 0.375D);
		emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Bottom"), x, y - 0.375D, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.DOWN, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
		mirror = true;
		emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Inner"), x - 0.3125D, y, z, mirror, red, green, blue, emissive);
		mirror = false;
		emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Inner"), x + 0.3125D, y, z, mirror, red, green, blue, emissive);
		emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Front"), x, y, z, mirror, red, green, blue, emissive);
    }
    private static void outletWorld(List<BakedQuad> quads, boolean inventory, boolean fluid, int meta, FoundryModelState data, FoundryMaterials materials) {
        double x=0,y=0,z=0,bot=0,top=.5;
        AxisAlignedBB bounds=new AxisAlignedBB(0,0,0,1,1D,1);
        boolean mirror=false, emissive=false;
        float r=1,g=1,b=1,red=1,green=1,blue=1;
        float mulBottom=.5F,mulTop=1,mulZ=.8F,mulX=.6F;
		emissive = false;
		if(meta == 4) {
			bounds = new AxisAlignedBB(0.625D, bot, 0.3125D, 1D, top, 0.6875D);
			red = r * mulBottom; green = g * mulBottom; blue = b * mulBottom;
			emit(quads, inventory, fluid, bounds, EnumFacing.DOWN, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
			red = r * mulTop; green = g * mulTop; blue = b * mulTop;
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Bottom"), x, y - 0.375D, z, mirror, red, green, blue, emissive);
			red = r * mulZ; green = g * mulZ; blue = b * mulZ;
			emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Inner"), x, y, z - 0.3125D, mirror, red, green, blue, emissive);
			mirror = true;
			emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Inner"), x, y, z + 0.3125D, mirror, red, green, blue, emissive);
			mirror = false;
			red = r * mulX; green = g * mulX; blue = b * mulX;
			emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Front"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Front"), x, y, z, mirror, red, green, blue, emissive);
			if(data.filter) {
				bounds = new AxisAlignedBB(0.96875D, 0.0625, 0.375D, 0.96875D, top, 0.625D);
				emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Filter"), x, y, z, mirror, red, green, blue, emissive);
				emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Filter"), x, y, z, mirror, red, green, blue, emissive);
			}
			if(data.closed) {
				bounds = new AxisAlignedBB(0.9375D, 0.0625, 0.375D, 0.9375D, top, 0.625D);
				emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Lock"), x, y, z, mirror, red, green, blue, emissive);
				emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Lock"), x, y, z, mirror, red, green, blue, emissive);
			}
		}
		if(meta == 5) {
			bounds = new AxisAlignedBB(0D, bot, 0.3125D, 0.375D, top, 0.6875D);
			red = r * mulBottom; green = g * mulBottom; blue = b * mulBottom;
			emit(quads, inventory, fluid, bounds, EnumFacing.DOWN, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
			red = r * mulTop; green = g * mulTop; blue = b * mulTop;
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Bottom"), x, y - 0.375D, z, mirror, red, green, blue, emissive);
			red = r * mulZ; green = g * mulZ; blue = b * mulZ;
			emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Inner"), x, y, z - 0.3125D, mirror, red, green, blue, emissive);
			mirror = true;
			emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Inner"), x, y, z + 0.3125D, mirror, red, green, blue, emissive);
			mirror = false;
			red = r * mulX; green = g * mulX; blue = b * mulX;
			emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Front"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Front"), x, y, z, mirror, red, green, blue, emissive);
			if(data.filter) {
				bounds = new AxisAlignedBB(0.03125D, 0.0625, 0.375D, 0.03125D, top, 0.625D);
				emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Filter"), x, y, z, mirror, red, green, blue, emissive);
				emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Filter"), x, y, z, mirror, red, green, blue, emissive);
			}
			if(data.closed) {
				bounds = new AxisAlignedBB(0.0625D, 0.0625, 0.375D, 0.0625D, top, 0.625D);
				emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Lock"), x, y, z, mirror, red, green, blue, emissive);
				emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Lock"), x, y, z, mirror, red, green, blue, emissive);
			}
		}
		if(meta == 2) {
			bounds = new AxisAlignedBB(0.3125D, bot, 0.625D, 0.6875D, top, 1D);
			red = r * mulBottom; green = g * mulBottom; blue = b * mulBottom;
			emit(quads, inventory, fluid, bounds, EnumFacing.DOWN, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
			red = r * mulTop; green = g * mulTop; blue = b * mulTop;
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Bottom"), x, y - 0.375D, z, mirror, red, green, blue, emissive);
			red = r * mulX; green = g * mulX; blue = b * mulX;
			mirror = true;
			emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Inner"), x - 0.3125D, y, z, mirror, red, green, blue, emissive);
			mirror = false;
			emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Inner"), x + 0.3125D, y, z, mirror, red, green, blue, emissive);
			red = r * mulZ; green = g * mulZ; blue = b * mulZ;
			emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Front"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Front"), x, y, z, mirror, red, green, blue, emissive);
			if(data.filter) {
				bounds = new AxisAlignedBB(0.375D, 0.0625, 0.96875D, 0.625D, top, 0.96875D);
				emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Filter"), x, y, z, mirror, red, green, blue, emissive);
				emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Filter"), x, y, z, mirror, red, green, blue, emissive);
			}
			if(data.closed) {
				bounds = new AxisAlignedBB(0.375D, 0.0625, 0.9375D, 0.625D, top, 0.9375D);
				emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Lock"), x, y, z, mirror, red, green, blue, emissive);
				emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Lock"), x, y, z, mirror, red, green, blue, emissive);
			}
		}
		if(meta == 3) {
			bounds = new AxisAlignedBB(0.3125D, bot, 0D, 0.6875D, top, 0.375D);
			red = r * mulBottom; green = g * mulBottom; blue = b * mulBottom;
			emit(quads, inventory, fluid, bounds, EnumFacing.DOWN, materials.get("Bottom"), x, y, z, mirror, red, green, blue, emissive);
			red = r * mulTop; green = g * mulTop; blue = b * mulTop;
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Top"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.UP, materials.get("Bottom"), x, y - 0.375D, z, mirror, red, green, blue, emissive);
			red = r * mulX; green = g * mulX; blue = b * mulX;
			mirror = true;
			emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.EAST, materials.get("Inner"), x - 0.3125D, y, z, mirror, red, green, blue, emissive);
			mirror = false;
			emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Side"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.WEST, materials.get("Inner"), x + 0.3125D, y, z, mirror, red, green, blue, emissive);
			red = r * mulZ; green = g * mulZ; blue = b * mulZ;
			emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Front"), x, y, z, mirror, red, green, blue, emissive);
			emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Front"), x, y, z, mirror, red, green, blue, emissive);
			if(data.filter) {
				bounds = new AxisAlignedBB(0.375D, 0.0625D, 0.03125, 0.625D, top, 0.03125D);
				emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Filter"), x, y, z, mirror, red, green, blue, emissive);
				emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Filter"), x, y, z, mirror, red, green, blue, emissive);
			}
			if(data.closed) {
				bounds = new AxisAlignedBB(0.375D, 0.0625, 0.0625D, 0.625D, top, 0.0625D);
				emit(quads, inventory, fluid, bounds, EnumFacing.SOUTH, materials.get("Lock"), x, y, z, mirror, red, green, blue, emissive);
				emit(quads, inventory, fluid, bounds, EnumFacing.NORTH, materials.get("Lock"), x, y, z, mirror, red, green, blue, emissive);
			}
		}
    }
}
