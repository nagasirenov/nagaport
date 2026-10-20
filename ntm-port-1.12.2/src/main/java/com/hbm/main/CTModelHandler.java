package com.hbm.main;

import java.util.*;
import java.util.function.Function;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.RenderBlockCT;
import com.hbm.render.block.ct.IBlockCT;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
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

/** Exact original CT owners and materials. No inferred joins, resource aliases or tile substitutions. */
@Mod.EventBusSubscriber(modid=RefStrings.MODID, value=Side.CLIENT)
public final class CTModelHandler {
    private static final Map<String, Profile> PROFILES=new LinkedHashMap<>();
    private static final Loader LOADER=new Loader();
    private static boolean registered;
    static {
        add("deco_red_copper", "com.hbm.blocks.generic.BlockDecoCT", null);
        add("deco_tungsten", "com.hbm.blocks.generic.BlockDecoCT", null);
        add("deco_aluminium", "com.hbm.blocks.generic.BlockDecoCT", null);
        add("deco_steel", "com.hbm.blocks.generic.BlockDecoCT", null);
        add("deco_rusty_steel", "com.hbm.blocks.generic.BlockDecoCT", null);
        add("deco_lead", "com.hbm.blocks.generic.BlockDecoCT", null);
        add("deco_beryllium", "com.hbm.blocks.generic.BlockDecoCT", null);
        add("deco_stainless", "com.hbm.blocks.generic.BlockDecoCT", null);
        add("reinforced_glass", "com.hbm.blocks.generic.BlockNTMGlassCT", null);
        add("reinforced_laminate", "com.hbm.blocks.generic.BlockNTMGlassCT", null);
        add("glass_boron", "com.hbm.blocks.generic.BlockNTMGlassCT", null);
        add("glass_lead", "com.hbm.blocks.generic.BlockNTMGlassCT", null);
        add("glass_uranium", "com.hbm.blocks.generic.BlockNTMGlassCT", null);
        add("glass_trinitite", "com.hbm.blocks.generic.BlockNTMGlassCT", null);
        add("glass_polonium", "com.hbm.blocks.generic.BlockNTMGlassCT", null);
        add("glass_ash", "com.hbm.blocks.generic.BlockNTMGlassCT", null);
        add("glass_quartz", "com.hbm.blocks.generic.BlockNTMGlassCT", null);
        add("glass_polarized", "com.hbm.blocks.generic.BlockNTMGlassCT", null);
        add("hadron_coil_alloy", "com.hbm.blocks.machine.BlockHadronCoil", null);
        add("hadron_coil_gold", "com.hbm.blocks.machine.BlockHadronCoil", null);
        add("hadron_coil_neodymium", "com.hbm.blocks.machine.BlockHadronCoil", null);
        add("hadron_coil_magtung", "com.hbm.blocks.machine.BlockHadronCoil", null);
        add("hadron_coil_schrabidium", "com.hbm.blocks.machine.BlockHadronCoil", null);
        add("hadron_coil_schrabidate", "com.hbm.blocks.machine.BlockHadronCoil", null);
        add("hadron_coil_starmetal", "com.hbm.blocks.machine.BlockHadronCoil", null);
        add("hadron_coil_chlorophyte", "com.hbm.blocks.machine.BlockHadronCoil", null);
        add("hadron_coil_mese", "com.hbm.blocks.machine.BlockHadronCoil", null);
        add("red_wire_coated", "com.hbm.blocks.network.WireCoated", null);
        add("pwr_block", "com.hbm.blocks.machine.BlockPWR", "pwr_casing_port");
        add("icf_block", "com.hbm.blocks.machine.BlockICF", "icf_block_port");
    }
    private CTModelHandler() { }
    private static void add(String name,String owner,String port) { PROFILES.put(name,new Profile(name,owner,port)); }
    private static final class Profile {
        final String name,owner;
        final ResourceLocation full,connected,portFull,portConnected;
        Profile(String name,String owner,String port) {
            this.name=name; this.owner=owner;
            full=texture(name); connected=texture(name+"_ct");
            portFull=port==null?null:texture(port); portConnected=port==null?null:texture(port+"_ct");
        }
        private static ResourceLocation texture(String path) { return new ResourceLocation(RefStrings.MODID,"blocks/"+path); }
    }
    public static boolean owns(Block block) { return profile(block)!=null; }
    private static Profile profile(Block block) {
        if(!(block instanceof IBlockCT) || block.getRegistryName()==null || !RefStrings.MODID.equals(block.getRegistryName().getNamespace())) return null;
        String path=block.getRegistryName().getPath();
        Profile profile=path.startsWith("tile.")?PROFILES.get(path.substring(5)):null;
        return profile!=null && profile.owner.equals(block.getClass().getName())?profile:null;
    }
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) { ModelLoaderRegistry.registerLoader(LOADER); registered=true; }
        for(Block block:ForgeRegistries.BLOCKS.getValuesCollection()) {
            Profile profile=profile(block); if(profile==null) continue;
            ModelLoader.setCustomStateMapper(block,owner->{
                Map<IBlockState,ModelResourceLocation> states=new IdentityHashMap<>();
                ModelResourceLocation location=new ModelResourceLocation(RefStrings.MODID+":native_ct/"+profile.name+"/world","normal");
                for(IBlockState state:owner.getBlockState().getValidStates()) states.put(state,location);
                return states;
            });
            Item item=Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock)||((ItemBlock)item).getBlock()!=block) throw new IllegalStateException("Missing CT ItemBlock "+block.getRegistryName());
            for(int metadata=0;metadata<16;metadata++) ModelLoader.setCustomModelResourceLocation(item,metadata,
                    new ModelResourceLocation(RefStrings.MODID+":native_ct/"+profile.name+"/item","inventory"));
        }
    }
    private static String path(ResourceLocation location) {
        String path=location.getPath(); if(path.startsWith("models/")) path=path.substring(7); if(path.startsWith("block/")) path=path.substring(6); return path;
    }
    private static final class Loader implements ICustomModelLoader {
        @Override public void onResourceManagerReload(IResourceManager manager) { }
        @Override public boolean accepts(ResourceLocation location) {
            if(!RefStrings.MODID.equals(location.getNamespace())) return false;
            String[] parts=path(location).split("/",-1);
            return parts.length==3 && parts[0].equals("native_ct") && PROFILES.containsKey(parts[1]) && (parts[2].equals("world")||parts[2].equals("item"));
        }
        @Override public IModel loadModel(ResourceLocation location) {
            if(!accepts(location)) throw new IllegalArgumentException("Unknown CT model route "+location);
            String[] parts=path(location).split("/"); return new Model(PROFILES.get(parts[1]),parts[2].equals("item"));
        }
    }
    private static final class Model implements IModel {
        final Profile profile; final boolean item;
        Model(Profile profile,boolean item) { this.profile=profile; this.item=item; }
        @Override public Collection<ResourceLocation> getTextures() {
            return profile.portFull==null?Arrays.asList(profile.full,profile.connected):Arrays.asList(profile.full,profile.connected,profile.portFull,profile.portConnected);
        }
        @Override public IBakedModel bake(IModelState state,VertexFormat format,Function<ResourceLocation,TextureAtlasSprite> sprites) {
            return new RenderBlockCT(sprites.apply(profile.full),sprites.apply(profile.connected),profile.portFull==null?null:sprites.apply(profile.portFull),
                    profile.portConnected==null?null:sprites.apply(profile.portConnected),item);
        }
    }
}
