package com.hbm.main;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import com.hbm.blocks.machine.MachineFunnel;
import com.hbm.blocks.rail.*;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.RenderRail.Kind;
import com.hbm.render.block.nativeblock.RailBakedModel;
import com.hbm.render.mesh.ObjMesh;
import com.hbm.render.mesh.ObjMeshParser;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;

/** Complete, finite native registration for the twelve original shared RenderRail owners. */
@Mod.EventBusSubscriber(modid=RefStrings.MODID, value=Side.CLIENT)
public final class RailModelHandler {
    private static final String PREFIX="native_rail/";
    private static final Map<String,Definition> DEFINITIONS=definitions();
    private static final Loader LOADER=new Loader();
    private static boolean registered;
    private RailModelHandler() { }
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) { ModelLoaderRegistry.registerLoader(LOADER); registered=true; }
        for(Block block:ForgeRegistries.BLOCKS.getValuesCollection()) {
            if(!(block instanceof IRenderBlock)) continue;
            ResourceLocation name=block.getRegistryName();
            if(name==null||!RefStrings.MODID.equals(name.getNamespace())) continue;
            Definition definition=DEFINITIONS.get(name.getPath());
            if(definition==null||block.getClass()!=definition.owner) throw new IllegalArgumentException("Unknown rail model owner "+name);
            Item item=Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock)||((ItemBlock)item).getBlock()!=block) throw new IllegalStateException("Missing rail ItemBlock "+name);
            for(int metadata=0;metadata<16;metadata++) ModelLoader.setCustomModelResourceLocation(item,metadata,
                    new ModelResourceLocation(new ResourceLocation(RefStrings.MODID,PREFIX+name.getPath()+"/"+metadata+"/item"),"inventory"));
        }
    }
    private static Map<String,Definition> definitions() {
        Map<String,Definition> out=new LinkedHashMap<>();
        define(out,"rail_narrow_straight",RailNarrowStraight.class,Kind.NARROW_STRAIGHT,"rail_narrow","rail_narrow_neo");
        define(out,"rail_narrow_curve",RailNarrowCurve.class,Kind.NARROW_CURVE,"rail_narrow_bend","rail_narrow_neo");
        define(out,"rail_large_straight",RailStandardStraight.class,Kind.STRAIGHT,"rail_standard","rail_standard_straight");
        define(out,"rail_large_straight_short",RailStandardStraightShort.class,Kind.SHORT,"rail_standard_short","rail_standard_straight");
        define(out,"rail_large_curve",RailStandardCurveBase.class,Kind.CURVE,"rail_standard_bend","rail_standard_straight");
        define(out,"rail_large_curve_7",RailStandardCurveWide7.class,Kind.CURVE7,"rail_standard_bend_wide","rail_standard_straight");
        define(out,"rail_large_curve_9",RailStandardCurveWide9.class,Kind.CURVE9,"rail_standard_bend_wide9","rail_standard_straight");
        define(out,"rail_large_ramp",RailStandardRamp.class,Kind.RAMP,"rail_standard_ramp","rail_standard_straight");
        define(out,"rail_large_buffer",RailStandardBuffer.class,Kind.BUFFER,"rail_standard_buffer","rail_standard_buffer");
        define(out,"rail_large_switch",RailStandardSwitch.class,Kind.SWITCH,"rail_standard_switch","rail_standard_straight","rail_switch_sign");
        define(out,"rail_large_switch_flipped",RailStandardSwitchFlipped.class,Kind.SWITCH_FLIPPED,"rail_standard_switch_flipped","rail_standard_straight","rail_switch_sign_flipped");
        define(out,"machine_funnel",MachineFunnel.class,Kind.FUNNEL,"funnel","machine_funnel_top","machine_funnel_bottom","machine_funnel_side");
        return Collections.unmodifiableMap(out);
    }
    private static void define(Map<String,Definition> out,String name,Class<? extends Block> owner,Kind kind,String mesh,String... textures) {
        out.put("tile."+name,new Definition(owner,kind,mesh,textures));
    }
    private static final class Definition {
        final Class<? extends Block> owner; final Kind kind; final String mesh; final String[] textures;
        Definition(Class<? extends Block> owner,Kind kind,String mesh,String[] textures) { this.owner=owner;this.kind=kind;this.mesh=mesh;this.textures=textures; }
        ResourceLocation texture(int index) { return new ResourceLocation(RefStrings.MODID,"blocks/"+textures[index]); }
    }
    private static String path(ResourceLocation location) {
        String path=location.getPath();if(path.startsWith("models/"))path=path.substring(7);if(path.startsWith("block/"))path=path.substring(6);return path;
    }
    private static final class Loader implements ICustomModelLoader {
        private IResourceManager resources;
        private final Map<String,ObjMesh> meshes=new HashMap<>();
        @Override public void onResourceManagerReload(IResourceManager resources) { this.resources=resources;meshes.clear(); }
        @Override public boolean accepts(ResourceLocation location) { return RefStrings.MODID.equals(location.getNamespace())&&path(location).startsWith(PREFIX); }
        @Override public IModel loadModel(ResourceLocation location) throws Exception {
            String normalized=path(location);
            if(!accepts(location))throw new IllegalArgumentException("Unknown rail model route "+location);
            String[] fields=normalized.substring(PREFIX.length()).split("/");
            if(fields.length!=3||!(fields[2].equals("item")||fields[2].equals("world")))throw new IllegalArgumentException("Invalid rail model route "+location);
            Definition definition=DEFINITIONS.get(fields[0]);
            if(definition==null)throw new IllegalArgumentException("Unknown rail model "+location);
            int metadata=Integer.parseInt(fields[1]);
            if(metadata<0||metadata>15)throw new IllegalArgumentException("Invalid rail metadata "+metadata);
            ObjMesh mesh=meshes.get(definition.mesh);
            if(mesh==null) {
                ResourceLocation asset=new ResourceLocation(RefStrings.MODID,"models/blocks/"+definition.mesh+".obj");
                try(IResource resource=resources.getResource(asset);InputStreamReader reader=new InputStreamReader(resource.getInputStream(),StandardCharsets.UTF_8)) {
                    mesh=ObjMeshParser.parse(reader,asset.toString(),false);
                }
                meshes.put(definition.mesh,mesh);
            }
            return new RailModel(definition,mesh,metadata,fields[2].equals("item"));
        }
    }
    private static final class RailModel implements IModel {
        private final Definition definition;private final ObjMesh mesh;private final int metadata;private final boolean inventory;
        RailModel(Definition definition,ObjMesh mesh,int metadata,boolean inventory) { this.definition=definition;this.mesh=mesh;this.metadata=metadata;this.inventory=inventory; }
        @Override public Collection<ResourceLocation> getTextures() {
            List<ResourceLocation> result=new ArrayList<>();for(int i=0;i<definition.textures.length;i++)result.add(definition.texture(i));return result;
        }
        @Override public IBakedModel bake(IModelState state,VertexFormat format,Function<ResourceLocation,TextureAtlasSprite> sprites) {
            TextureAtlasSprite[] textures=new TextureAtlasSprite[definition.textures.length];for(int i=0;i<textures.length;i++)textures[i]=sprites.apply(definition.texture(i));
            return new RailBakedModel(mesh,definition.kind,metadata,inventory,textures);
        }
    }
}
