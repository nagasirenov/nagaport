package com.hbm.main;
import java.util.*;
import java.util.function.Function;
import com.hbm.blocks.BlockCoral;
import com.hbm.blocks.generic.BlockReeds;
import com.hbm.blocks.generic.BlockHangingVine;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.nativeblock.PlantBakedModel;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
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

/** Exact owned registrations only; addon replacement classes keep their own routes. */
@Mod.EventBusSubscriber(modid=RefStrings.MODID,value=Side.CLIENT)
public final class PlantBlockModelHandler {
 private PlantBlockModelHandler(){}
 private static final String PREFIX="native_plants/";
 private static final Loader LOADER=new Loader();private static boolean registered;
 public static String[] textures(String kind){
  if(kind.equals("reeds"))return new String[]{"reeds_top","reeds_mid","reeds_bottom"};
  if(kind.equals("vine"))return new String[]{"vine_phosphor","vine_phosphor_ground","vine_phosphor_hang","vine_phosphor_spots","vine_phosphor_spots_hang","vine_phosphor_item"};
  if(kind.equals("coral"))return new String[]{"laythe_coral.tube","laythe_coral.brain","laythe_coral.bubble","laythe_coral.fire","laythe_coral.horn"};
  throw new IllegalArgumentException("Unknown plant kind "+kind);
 }
 private static ResourceLocation texture(String name){return new ResourceLocation(RefStrings.MODID,"blocks/"+name);}
 @SubscribeEvent public static void registerModels(ModelRegistryEvent event){
  if(!registered){ModelLoaderRegistry.registerLoader(LOADER);registered=true;}
  for(Block block:ForgeRegistries.BLOCKS.getValuesCollection()){
   ResourceLocation name=block.getRegistryName();if(name==null||!name.getNamespace().equals(RefStrings.MODID))continue;
   final String kind;
   if(name.getPath().equals("tile.plant_reeds")&&block.getClass()==BlockReeds.class)kind="reeds";
   else if(name.getPath().equals("tile.laythe_coral")&&block.getClass()==BlockCoral.class)kind="coral";
   else if(name.getPath().equals("tile.vine_phosphor")&&block.getClass()==BlockHangingVine.class)kind="vine";
   else continue;
   ModelLoader.setCustomStateMapper(block,new StateMapperBase(){@Override protected ModelResourceLocation getModelResourceLocation(IBlockState state){return new ModelResourceLocation(name,"variant="+(kind.equals("coral")?state.getValue(BlockCoral.META)%5:0));}});
   Item item=Item.getItemFromBlock(block);
   if(!(item instanceof ItemBlock)||((ItemBlock)item).getBlock()!=block)throw new IllegalStateException("Missing plant ItemBlock "+name);
   for(int meta=0;meta<16;meta++)ModelLoader.setCustomModelResourceLocation(item,meta,route(kind,"item",kind.equals("coral")?meta%5:0));
  }
  Item saplingItem = Item.getItemFromBlock(com.hbm.blocks.ModBlocks.sapling_pvc);
  if(saplingItem instanceof ItemBlock) {
   ModelLoader.setCustomModelResourceLocation(saplingItem, 0, new ModelResourceLocation(RefStrings.MODID + ":sapling_vinyl", "inventory"));
   for(int meta = 1; meta < 16; meta++) {
    ModelLoader.setCustomModelResourceLocation(saplingItem, meta, new ModelResourceLocation(RefStrings.MODID + ":sapling_pvc", "inventory"));
   }
  }
 }
 private static ModelResourceLocation route(String kind,String mode,int variant){return new ModelResourceLocation(new ResourceLocation(RefStrings.MODID,PREFIX+kind+"/"+mode+"/"+variant),mode.equals("item")?"inventory":"normal");}
 private static final class Loader implements ICustomModelLoader {
  public void onResourceManagerReload(IResourceManager manager){}
  private String path(ResourceLocation l){String p=l.getPath();if(p.startsWith("models/"))p=p.substring(7);if(p.startsWith("block/"))p=p.substring(6);return p;}
  public boolean accepts(ResourceLocation l){return l.getNamespace().equals(RefStrings.MODID)&&path(l).startsWith(PREFIX);}
  public IModel loadModel(ResourceLocation l){
   String[] p=path(l).substring(PREFIX.length()).split("/");
   if(p.length!=3)throw new IllegalArgumentException("Invalid plant model "+l);
   String[] names=textures(p[0]);int variant=Integer.parseInt(p[2]);
   if(variant<0||variant>=(p[0].equals("coral")?5:1))throw new IllegalArgumentException("Invalid plant variant "+l);
   if(p[1].equals("item"))return new ItemLayerModel(com.google.common.collect.ImmutableList.of(texture(names[p[0].equals("vine")?5:variant])));
   if(!p[1].equals("world"))throw new IllegalArgumentException("Invalid plant mode "+l);
   return new PlantModel(p[0],variant);
  }
 }
 private static final class PlantModel implements IModel {
  final String kind;final int variant;PlantModel(String kind,int variant){this.kind=kind;this.variant=variant;}
  public Collection<ResourceLocation> getTextures(){List<ResourceLocation> result=new ArrayList<>();for(String t:textures(kind))result.add(texture(t));return result;}
  public IBakedModel bake(IModelState state,VertexFormat format,Function<ResourceLocation,TextureAtlasSprite> getter){String[] names=textures(kind);TextureAtlasSprite[] sprites=new TextureAtlasSprite[names.length];for(int i=0;i<names.length;i++)sprites[i]=getter.apply(texture(names[i]));return new PlantBakedModel(kind,variant,sprites);}
 }
}
