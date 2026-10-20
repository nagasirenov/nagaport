package com.hbm.main;

import java.util.*;
import java.util.function.Function;
import com.hbm.blocks.IBlockSideRotation;
import com.hbm.blocks.SideRotationModelState;
import com.hbm.blocks.generic.*;
import com.hbm.blocks.network.*;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.nativeblock.SideRotationBakedModel;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
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

/** Complete native owner registration for the former side-rotation render ID, after queued registry flush. */
@Mod.EventBusSubscriber(modid=RefStrings.MODID,value=Side.CLIENT)
public final class SideRotationBlockModelHandler {
    private SideRotationBlockModelHandler() { }
    private static final String PREFIX="native_side_rotation/";
    private static final Map<String,Class<? extends Block>> OWNERS=owners();
    private static final Map<String,IBlockSideRotation> DEFINITIONS=new HashMap<>();
    private static final Loader LOADER=new Loader();
    private static boolean registered;
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) {ModelLoaderRegistry.registerLoader(LOADER);registered=true;}
        for(Block block:ForgeRegistries.BLOCKS.getValuesCollection()) {
            if(!(block instanceof IBlockSideRotation)) continue;
            ResourceLocation name=Objects.requireNonNull(block.getRegistryName(),"Side-rotation registry name");
            if(!RefStrings.MODID.equals(name.getNamespace()) || block.getClass()!=OWNERS.get(name.getPath()))
                throw new IllegalArgumentException("Unspecified side-rotation owner "+name);
            IBlockSideRotation owner=(IBlockSideRotation)block;
            if(owner.getModelTextures().isEmpty()) throw new IllegalArgumentException("No atlas materials for "+name);
            DEFINITIONS.put(name.getPath(),owner);
            Item item=Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock) || ((ItemBlock)item).getBlock()!=block)
                throw new IllegalStateException("Missing side-rotation ItemBlock "+name);
            for(int metadata=0;metadata<16;metadata++) ModelLoader.setCustomModelResourceLocation(item,metadata,
                    new ModelResourceLocation(RefStrings.MODID+":"+PREFIX+name.getPath()+"/"+metadata+"/item","inventory"));
        }
    }
    private static Map<String,Class<? extends Block>> owners() {
        Map<String,Class<? extends Block>> result=new LinkedHashMap<>();
        result.put("tile.crane_extractor",CraneExtractor.class);result.put("tile.crane_inserter",CraneInserter.class);
        result.put("tile.crane_grabber",CraneGrabber.class);result.put("tile.crane_boxer",CraneBoxer.class);
        result.put("tile.crane_unboxer",CraneUnboxer.class);result.put("tile.wand_jigsaw",BlockWandJigsaw.class);
        result.put("tile.wand_tandem",BlockWandTandem.class);result.put("tile.wand_logic",BlockWandLogic.class);
        result.put("tile.wand_loot",BlockWandLoot.class);
        return Collections.unmodifiableMap(result);
    }
    private static final class Loader implements ICustomModelLoader {
        @Override public void onResourceManagerReload(IResourceManager resources) { }
        @Override public boolean accepts(ResourceLocation location) {
            return RefStrings.MODID.equals(location.getNamespace()) && normalized(location).startsWith(PREFIX);
        }
        @Override public IModel loadModel(ResourceLocation location) {
            String[] fields=normalized(location).substring(PREFIX.length()).split("/");
            if(fields.length!=3 || !(fields[2].equals("world")||fields[2].equals("item")))
                throw new IllegalArgumentException("Invalid side-rotation route "+location);
            IBlockSideRotation owner=DEFINITIONS.get(fields[0]);
            if(owner==null) throw new IllegalArgumentException("Unregistered side-rotation owner "+location);
            int metadata=Integer.parseInt(fields[1]);
            if(metadata<0||metadata>15) throw new IllegalArgumentException("Side-rotation metadata "+metadata);
            return new CubeModel(owner,metadata,fields[2].equals("item"));
        }
        private static String normalized(ResourceLocation location) {
            String path=location.getPath();
            if(path.startsWith("models/")) path=path.substring(7);
            if(path.startsWith("block/")) path=path.substring(6);
            return path;
        }
    }
    private static final class CubeModel implements IModel {
        final IBlockSideRotation owner;
        final int metadata;
        final boolean inventory;
        final Collection<ResourceLocation> textures;
        CubeModel(IBlockSideRotation owner,int metadata,boolean inventory) {
            this.owner=owner;this.metadata=metadata;this.inventory=inventory;
            textures=Collections.unmodifiableSet(new LinkedHashSet<>(owner.getModelTextures()));
        }
        @Override public Collection<ResourceLocation> getTextures() { return textures; }
        @Override public IBakedModel bake(IModelState state,VertexFormat format,Function<ResourceLocation,TextureAtlasSprite> sprites) {
            Map<ResourceLocation,TextureAtlasSprite> atlas=new HashMap<>();
            for(ResourceLocation texture:textures) atlas.put(texture,Objects.requireNonNull(sprites.apply(texture)));
            return new SideRotationBakedModel(SideRotationModelState.inventory(owner,metadata),inventory,atlas);
        }
    }
}
