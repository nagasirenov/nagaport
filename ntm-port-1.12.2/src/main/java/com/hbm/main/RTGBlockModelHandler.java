package com.hbm.main;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import com.hbm.blocks.machine.MachineRTG;
import com.hbm.blocks.machine.MachineMiniRTG;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.RenderRTGBlock;
import com.hbm.render.block.nativeblock.AtlasObjBakedModel;
import com.hbm.render.mesh.ObjMesh;
import com.hbm.render.mesh.ObjMeshParser;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.*;
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

/** RTG inventory atlas mesh; placed generators retain their original tile-rendered model. */
@Mod.EventBusSubscriber(modid=RefStrings.MODID,value=Side.CLIENT)
public final class RTGBlockModelHandler {
    private RTGBlockModelHandler() { }
    private static final String PREFIX="native_rtg/";
    private static final ResourceLocation ASSET=new ResourceLocation(RefStrings.MODID,"models/machines/rtg.obj");
    private static final Loader LOADER=new Loader();
    private static boolean registered;
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) { ModelLoaderRegistry.registerLoader(LOADER); registered=true; }
        for(Block block:ForgeRegistries.BLOCKS.getValuesCollection()) {
            if(!(block instanceof MachineRTG || block instanceof MachineMiniRTG))continue;
            ResourceLocation name=Objects.requireNonNull(block.getRegistryName(),"RTG registry name");
            boolean mini="tile.machine_minirtg".equals(name.getPath()) || "tile.machine_powerrtg".equals(name.getPath());
            if(!RefStrings.MODID.equals(name.getNamespace()) || !(mini || "tile.machine_rtg_grey".equals(name.getPath()))
                    || block.getClass()!=(mini?MachineMiniRTG.class:MachineRTG.class))
                throw new IllegalArgumentException("Unspecified RTG model owner "+name);
            Item item=Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock)||((ItemBlock)item).getBlock()!=block)throw new IllegalStateException("Missing RTG ItemBlock "+name);
            for(int metadata=0;metadata<16;metadata++)ModelLoader.setCustomModelResourceLocation(item,metadata,
                    new ModelResourceLocation(RefStrings.MODID+":"+PREFIX+name.getPath()+"/item","inventory"));
        }
    }
    private static final class Loader implements ICustomModelLoader {
        private IResourceManager resources;
        private ObjMesh mesh;
        @Override public void onResourceManagerReload(IResourceManager resources) { this.resources=resources;mesh=null; }
        private static String normalized(ResourceLocation location) {
            String path=location.getPath();
            if(path.startsWith("models/"))path=path.substring(7);
            if(path.startsWith("block/"))path=path.substring(6);
            return path;
        }
        @Override public boolean accepts(ResourceLocation location) {
            return RefStrings.MODID.equals(location.getNamespace())&&normalized(location).startsWith(PREFIX);
        }
        @Override public IModel loadModel(ResourceLocation location)throws Exception {
            String[] fields=normalized(location).substring(PREFIX.length()).split("/");
            if(fields.length!=2 || !(fields[1].equals("item")||fields[1].equals("world")))throw new IllegalArgumentException("Invalid RTG route "+location);
            boolean power=fields[0].equals("tile.machine_powerrtg");
            boolean mini=fields[0].equals("tile.machine_minirtg") || power;
            if(!mini&&!fields[0].equals("tile.machine_rtg_grey"))throw new IllegalArgumentException("Unknown RTG owner "+location);
            if(mesh==null)try(IResource resource=resources.getResource(ASSET);
                    InputStreamReader reader=new InputStreamReader(resource.getInputStream(),StandardCharsets.UTF_8)) {
                mesh=ObjMeshParser.parse(reader,ASSET.toString(),false);
            }
            return new Model(mesh,new ResourceLocation(RefStrings.MODID,"blocks/"+(power?"rtg_polonium":mini?"rtg_cell":"rtg")),fields[1].equals("item"));
        }
    }
    private static final class Model implements IModel {
        final ObjMesh mesh;final ResourceLocation texture;final boolean item;
        Model(ObjMesh mesh,ResourceLocation texture,boolean item) {this.mesh=mesh;this.texture=texture;this.item=item;}
        @Override public Collection<ResourceLocation> getTextures() {return Collections.singleton(texture);}
        @Override public IBakedModel bake(IModelState state,VertexFormat format,Function<ResourceLocation,TextureAtlasSprite> sprites) {
            TextureAtlasSprite sprite=sprites.apply(texture);
            return new AtlasObjBakedModel(item?RenderRTGBlock.bake(mesh,sprite):Collections.emptyList(),sprite);
        }
    }
}
