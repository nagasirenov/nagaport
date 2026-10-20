package com.hbm.main;

import java.util.*;
import java.util.function.Function;
import com.hbm.blocks.IBlockMultiPass;
import com.hbm.dim.SolarSystem;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.RenderBlockMultipass;
import com.hbm.render.block.multipass.MultipassMaterials;
import com.hbm.render.block.multipass.MultipassMaterials.Kind;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
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

/** Exact original multipass registrations, including ore subclasses and their resource aliases. */
@Mod.EventBusSubscriber(modid=RefStrings.MODID,value=Side.CLIENT)
public final class MultipassModelHandler {
    private static final Map<String,Profile> PROFILES=new LinkedHashMap<>();
    private static final Loader LOADER=new Loader();
    private static boolean registered;
    static {
        add("ore_iron","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_iron");
        add("ore_gold","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_gold");
        add("ore_redstone","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_redstone");
        add("ore_lapis","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_lapis");
        add("ore_emerald","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_emerald");
        add("ore_quartz","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_quartz");
        add("ore_diamond","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_diamond");
        add("ore_uranium","com.hbm.blocks.generic.BlockOreOutgas",Kind.ORE,"ore_uranium");
        add("ore_plutonium","com.hbm.blocks.generic.BlockOreOutgas",Kind.ORE,"ore_plutonium");
        add("ore_titanium","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_titanium");
        add("ore_sulfur","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_sulfur");
        add("ore_glowstone","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_glowstone");
        add("ore_thorium","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_thorium");
        add("ore_morkite","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_morkite");
        add("ore_niter","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_niter");
        add("ore_copper","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_copper");
        add("ore_nickel","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_nickel");
        add("ore_mineral","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_mineral");
        add("ore_zinc","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_zinc");
        add("ore_lithium","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_lithium");
        add("ore_niobium","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_niobium");
        add("ore_palladium","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_palladium");
        add("ore_iodine","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_iodine");
        add("ore_arsenic","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_arsenic");
        add("ore_cadmium","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_cadmium");
        add("ore_tungsten","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_tungsten");
        add("ore_aluminium","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_aluminium");
        add("ore_fluorite","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_fluorite");
        add("ore_lead","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_lead");
        add("ore_schrabidium","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_schrabidium");
        add("ore_beryllium","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_beryllium");
        add("ore_lignite","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_lignite");
        add("ore_asbestos","com.hbm.blocks.generic.BlockOreOutgas",Kind.ORE,"ore_asbestos");
        add("ore_silicon","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_silicon");
        add("ore_fire","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_fire");
        add("ore_shale","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_shale");
        add("cluster_iron","com.hbm.blocks.generic.BlockCluster",Kind.ORE,"cluster_iron");
        add("cluster_titanium","com.hbm.blocks.generic.BlockCluster",Kind.ORE,"cluster_titanium");
        add("cluster_aluminium","com.hbm.blocks.generic.BlockCluster",Kind.ORE,"cluster_aluminium");
        add("cluster_copper","com.hbm.blocks.generic.BlockCluster",Kind.ORE,"cluster_copper");
        add("ore_bedrock","com.hbm.blocks.generic.BlockBedrockOreTE",Kind.BEDROCK_ORE,"minecraft:bedrock");
        add("ore_volcano","com.hbm.blocks.generic.BlockFissure",Kind.FISSURE,"minecraft:bedrock");
        add("ore_australium","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_australium");
        add("ore_rare","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_rare");
        add("ore_cobalt","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_cobalt");
        add("ore_cinnebar","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_cinnebar");
        add("ore_coltan","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_coltan");
        add("ore_lanthanium","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_lanthanium");
        add("ore_oil_empty","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_oil_empty");
        add("ore_oil","com.hbm.blocks.generic.BlockOreFluid",Kind.ORE,"ore_oil");
        add("ore_bedrock_oil","com.hbm.blocks.generic.BlockOreFluid",Kind.ORE,"ore_bedrock_oil");
        add("ore_gas_empty","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_oil_empty");
        add("ore_gas","com.hbm.blocks.generic.BlockOreFluid",Kind.ORE,"ore_gas");
        add("ore_brine_empty","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_oil_empty");
        add("ore_brine","com.hbm.blocks.generic.BlockOreFluid",Kind.ORE,"ore_brine");
        add("ore_tekto_empty","com.hbm.blocks.generic.BlockOre",Kind.ORE,"ore_oil_empty");
        add("ore_tekto","com.hbm.blocks.generic.BlockOreFluid",Kind.ORE,"ore_tekto");
        add("deco_titanium","com.hbm.blocks.generic.BlockOre",Kind.ORE,"deco_titanium");
        add("ore_sellafield_diamond","com.hbm.blocks.generic.BlockSellafieldOre",Kind.SELLAFIELD,"ore_overlay_diamond");
        add("ore_sellafield_emerald","com.hbm.blocks.generic.BlockSellafieldOre",Kind.SELLAFIELD,"ore_overlay_emerald");
        add("ore_sellafield_uranium_scorched","com.hbm.blocks.generic.BlockSellafieldOre",Kind.SELLAFIELD,"ore_overlay_uranium_scorched");
        add("ore_sellafield_schrabidium","com.hbm.blocks.generic.BlockSellafieldOre",Kind.SELLAFIELD,"ore_overlay_schrabidium");
        add("ore_sellafield_radgem","com.hbm.blocks.generic.BlockSellafieldOre",Kind.SELLAFIELD,"ore_overlay_radgem");
        add("red_cable_paintable","com.hbm.blocks.network.BlockCablePaintable",Kind.CABLE,"red_cable_base");
        add("red_cable_gauge","com.hbm.blocks.network.BlockCableGauge",Kind.CABLE_GAUGE,"deco_red_copper");
        add("fluid_duct_paintable_block_exhaust","com.hbm.blocks.network.FluidDuctPaintableBlockExhaust",Kind.EXHAUST,"fluid_duct_paintable_block_exhaust");
        add("fluid_duct_paintable","com.hbm.blocks.network.FluidDuctPaintable",Kind.FLUID,"fluid_duct_paintable");
        add("fluid_duct_gauge","com.hbm.blocks.network.FluidDuctGauge",Kind.FLUID_GAUGE,"deco_steel");
        add("crane_router","com.hbm.blocks.network.CraneRouter",Kind.ROUTER,"crane_in");
        add("pneumatic_tube_paintable","com.hbm.blocks.network.PneumoTubePaintableBlock",Kind.PNEUMATIC,"pneumatic_tube_paintable");
        add("oc_cable_paintable","com.hbm.blocks.network.BlockOpenComputersCablePaintable",Kind.OC_CABLE,"oc_cable_base");
    }
    private MultipassModelHandler() { }
    private static void add(String name,String owner,Kind kind,String texture) { PROFILES.put(name,new Profile(name,owner,kind,texture)); }
    private static final class Profile {
        final String name,owner,texture; final Kind kind;
        Profile(String name,String owner,Kind kind,String texture) { this.name=name; this.owner=owner; this.kind=kind; this.texture=texture; }
        MultipassMaterials materials() {
            List<ResourceLocation> stones=new ArrayList<>();
            if(kind==Kind.ORE) for(SolarSystem.Body body:SolarSystem.Body.values()) {
                if(body.ordinal()==0) stones.add(MultipassMaterials.texture("minecraft:stone"));
                else stones.add(new ResourceLocation(body.getStoneTexture().toString().replace("textures/blocks/","blocks/").replace(".png","")));
            }
            return new MultipassMaterials(kind,texture,stones);
        }
    }
    private static Profile profile(Block block) {
        if(!(block instanceof IBlockMultiPass) || block.getRegistryName()==null || !RefStrings.MODID.equals(block.getRegistryName().getNamespace())) return null;
        String path=block.getRegistryName().getPath();
        Profile p=path.startsWith("tile.")?PROFILES.get(path.substring(5)):null;
        return p!=null && p.owner.equals(block.getClass().getName())?p:null;
    }
    public static boolean owns(Block block) { return profile(block)!=null; }
    @SubscribeEvent public static void registerModels(ModelRegistryEvent event) {
        if(!registered) { ModelLoaderRegistry.registerLoader(LOADER); registered=true; }
        for(Block block:ForgeRegistries.BLOCKS.getValuesCollection()) {
            Profile p=profile(block); if(p==null) continue;
            ModelLoader.setCustomStateMapper(block,owner->{
                Map<IBlockState,ModelResourceLocation> states=new IdentityHashMap<>();
                for(IBlockState state:owner.getBlockState().getValidStates()) states.put(state,
                    new ModelResourceLocation(RefStrings.MODID+":native_multipass/"+p.name+"/world_"+owner.getMetaFromState(state),"normal"));
                return states;
            });
            Item item=Item.getItemFromBlock(block);
            if(!(item instanceof ItemBlock) || ((ItemBlock)item).getBlock()!=block) throw new IllegalStateException("Missing multipass ItemBlock "+block.getRegistryName());
            for(int metadata=0;metadata<16;metadata++) ModelLoader.setCustomModelResourceLocation(item,metadata,
                new ModelResourceLocation(RefStrings.MODID+":native_multipass/"+p.name+"/item_"+metadata,"inventory"));
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
            return parts.length==3 && parts[0].equals("native_multipass") && PROFILES.containsKey(parts[1]) &&
                parts[2].matches("(world|item)_([0-9]|1[0-5])");
        }
        @Override public IModel loadModel(ResourceLocation location) {
            if(!accepts(location)) throw new IllegalArgumentException("Unknown multipass route "+location);
            String[] parts=path(location).split("/"); boolean item=parts[2].startsWith("item_");
            return new Model(PROFILES.get(parts[1]).materials(),item,Integer.parseInt(parts[2].substring(parts[2].indexOf('_')+1)));
        }
    }
    private static final class Model implements IModel {
        final MultipassMaterials materials; final boolean item; final int metadata;
        Model(MultipassMaterials materials,boolean item,int metadata) { this.materials=materials; this.item=item; this.metadata=metadata; }
        @Override public Collection<ResourceLocation> getTextures() { return materials.textures(); }
        @Override public IBakedModel bake(IModelState state,VertexFormat format,Function<ResourceLocation,TextureAtlasSprite> sprites) {
            return new RenderBlockMultipass(materials,sprites,paint->Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(paint),item,metadata);
        }
    }
}
