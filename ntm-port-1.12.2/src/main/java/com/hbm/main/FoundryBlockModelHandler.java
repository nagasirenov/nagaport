package com.hbm.main;

import java.util.*;
import java.util.function.Function;
import com.hbm.blocks.machine.*;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.nativeblock.*;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;

/** Native foundry models registered after actual block/item registry creation. */
@Mod.EventBusSubscriber(modid=RefStrings.MODID,value=Side.CLIENT)
public final class FoundryBlockModelHandler {
    private FoundryBlockModelHandler() { }
    private static final String PREFIX="native_foundry/";
    private static boolean registered;
    private static final Map<ResourceLocation,Class<? extends Block>> OWNERS=new LinkedHashMap<>();
    static {
        OWNERS.put(name("basin"),FoundryBasin.class);OWNERS.put(name("mold"),FoundryMold.class);
        OWNERS.put(name("channel"),FoundryChannel.class);OWNERS.put(name("tank"),FoundryTank.class);
        OWNERS.put(name("outlet"),FoundryOutlet.class);OWNERS.put(name("slagtap"),FoundrySlagtap.class);
    }
    private static ResourceLocation name(String kind) { return new ResourceLocation(RefStrings.MODID,"tile.foundry_"+kind); }
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) { ModelLoaderRegistry.registerLoader(new Loader());registered=true; }
        for(Block block:ForgeRegistries.BLOCKS.getValuesCollection()) if(block instanceof FoundryBlock) {
            ResourceLocation name=Objects.requireNonNull(block.getRegistryName());
            if(OWNERS.get(name)!=block.getClass())throw new IllegalArgumentException("Unknown foundry model owner "+name);
            Item item=Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock)||((ItemBlock)item).getBlock()!=block)throw new IllegalStateException("Missing foundry ItemBlock "+name);
            for(int meta=0;meta<16;meta++)ModelLoader.setCustomModelResourceLocation(item,meta,new ModelResourceLocation(new ResourceLocation(RefStrings.MODID,PREFIX+name.getPath()+"/"+meta+"/item"),"inventory"));
        }
    }
    private static final class Loader implements ICustomModelLoader {
        @Override public void onResourceManagerReload(IResourceManager manager) { }
        private String path(ResourceLocation location) {
            String path=location.getPath();if(path.startsWith("models/"))path=path.substring(7);if(path.startsWith("block/"))path=path.substring(6);return path;
        }
        @Override public boolean accepts(ResourceLocation location) { return RefStrings.MODID.equals(location.getNamespace())&&path(location).startsWith(PREFIX); }
        @Override public IModel loadModel(ResourceLocation location) {
            String[] fields=path(location).substring(PREFIX.length()).split("/");
            if(fields.length!=3||!OWNERS.containsKey(new ResourceLocation(RefStrings.MODID,fields[0]))||!(fields[2].equals("world")||fields[2].equals("item")))throw new IllegalArgumentException("Unknown foundry model route "+location);
            final FoundryGeometry.Kind kind=FoundryGeometry.Kind.valueOf(fields[0].substring("tile.foundry_".length()).toUpperCase(Locale.ROOT));
            final int metadata=Integer.parseInt(fields[1]);if(metadata<0||metadata>15)throw new IllegalArgumentException("Foundry metadata "+metadata);
            final boolean inventory=fields[2].equals("item");
            return new IModel() {
                @Override public Collection<ResourceLocation> getTextures() { return FoundryMaterials.textures(kind).values(); }
                @Override public IBakedModel bake(IModelState state,VertexFormat format,Function<ResourceLocation,TextureAtlasSprite> sprites) { return new FoundryBakedModel(kind,metadata,inventory,new FoundryMaterials(kind,sprites)); }
            };
        }
    }
}
