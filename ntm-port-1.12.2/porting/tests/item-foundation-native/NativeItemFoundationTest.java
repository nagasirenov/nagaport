import com.hbm.items.ItemEnumMulti;
import com.hbm.items.ItemEnums;
import com.hbm.items.ItemGenericPart;
import com.hbm.items.machine.ItemDrillbit;
import com.hbm.items.machine.ItemCircuit;
import com.hbm.interfaces.IOrderedEnum;
import com.hbm.render.icon.*;
import com.hbm.render.item.*;
import com.hbm.render.item.nativeitem.callback.*;
import com.hbm.render.util.NativeItemBaseModel;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.registry.RegistrySimple;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

/** Complete production texture/enum/native wrapper slice on real Forge; no mod stubs. */
public final class NativeItemFoundationTest {
 static int checks;
 static sun.misc.Unsafe unsafe;
 static SimpleReloadableResourceManager resources;
 static TextureMap atlas;
 static Item plain, custom, protectedModel, foreign, dynamic, orphan, unknown, handheld, plainSubtype, fancy;
 static final ModelResourceLocation UNKNOWN = new ModelResourceLocation("hbm:unknown_owner", "inventory");
 static ItemEnumMulti coke;
 static ItemGenericPart parts;
 static ItemDrillbit drill;
 static NativeItemTextureModelHandler foundation;
 static CallbackItemRegistration callback;
 static ItemRenderBase renderer;
 static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
 static Field field(Class<?> cls,String name)throws Exception{Field f=cls.getDeclaredField(name);f.setAccessible(true);return f;}
 static Object get(Object target,Class<?> cls,String name)throws Exception{return field(cls,name).get(target);}
 static void set(Object target,Class<?> cls,String name,Object value)throws Exception{field(cls,name).set(target,value);}
 static ModelResourceLocation location(Item item){return new ModelResourceLocation(item.getRegistryName(),"inventory");}
 public static final class DynamicItem extends Item {public void registerIcons(IIconRegister reg){reg.registerIcon("hbm:ingot_steel");}}
 enum Ordered implements IOrderedEnum { A,B,C;public Enum[] getOrder(){return new Enum[]{C,A,B};}}
 static void setup()throws Exception {
  Bootstrap.register();
  net.minecraftforge.fml.common.ModMetadata md=new net.minecraftforge.fml.common.ModMetadata();md.modId="hbm";md.name="Native item foundation proof";md.version="probe";
  net.minecraftforge.fml.common.DummyModContainer container=new net.minecraftforge.fml.common.DummyModContainer(md);
  net.minecraftforge.fml.common.Loader.instance().setupTestHarness(container);net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(container);
  unsafe=(sun.misc.Unsafe)field(sun.misc.Unsafe.class,"theUnsafe").get(null);
  resources=new SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer());
  Minecraft minecraft=(Minecraft)unsafe.allocateInstance(Minecraft.class);set(null,Minecraft.class,"instance",minecraft);set(minecraft,Minecraft.class,"resourceManager",resources);set(minecraft,Minecraft.class,"gameSettings",new net.minecraft.client.settings.GameSettings());
  net.minecraft.launchwrapper.Launch.blackboard.put("fml.deobfuscatedEnvironment",Boolean.TRUE);
  plain=register(new Item().setTranslationKey("test_registry_texture_mismatch"),"item.test_registry_texture_mismatch");
  check(NativeItemTextureRegistry.texture(plain,"hbm:ingot_steel")==plain,"texture helper preserves exact identity");
  check(NativeItemTextureRegistry.texture(plain).equals("hbm:ingot_steel"),"raw intent separate from registry/translation name");
  Map<Item,String> snapshot=NativeItemTextureRegistry.textures();
  NativeItemTextureRegistry.texture(plain,"hbm:ingot_copper");check(snapshot.get(plain).equals("hbm:ingot_steel"),"configuration snapshot immutable across redeclaration");NativeItemTextureRegistry.texture(plain,"hbm:ingot_steel");
  boolean immutable=false;try{snapshot.put(plain,"wrong");}catch(UnsupportedOperationException expected){immutable=true;}check(immutable,"snapshot cannot rewrite declarations");
  fancy=register(NativeItemTextureRegistry.texture(new Item(),"hbm:ingot_steel"),"item.fancy_missing");
  plainSubtype=register(NativeItemTextureRegistry.texture(new Item().setHasSubtypes(true),"hbm:ingot_steel"),"item.plain_subtype");
  handheld=register(NativeItemTextureRegistry.texture(new Item().setFull3D(),"hbm:ingot_steel"),"item.handheld");
  custom=register(NativeItemTextureRegistry.texture(new Item(),"hbm:ingot_copper"),"item.custom_mesh");
  protectedModel=register(NativeItemTextureRegistry.texture(new Item(),"hbm:ingot_steel"),"item.protected_model");
  orphan=NativeItemTextureRegistry.texture(new Item(),"hbm:must_not_be_stitched_or_registered");
  unknown=register(NativeItemTextureRegistry.texture(new Item(),"hbm:ingot_steel"),"item.unknown_owner");ModelLoader.setCustomModelResourceLocation(unknown,7,UNKNOWN);
  foreign=new Item().setRegistryName("foreign","item.foreign");ForgeRegistries.ITEMS.register(foreign);NativeItemTextureRegistry.texture(foreign,"hbm:ingot_steel");
  dynamic=register(NativeItemTextureRegistry.texture(new DynamicItem(),"hbm:ingot_steel"),"item.dynamic");
  coke=(ItemEnumMulti)register(new ItemEnumMulti(ItemEnums.EnumCokeType.class,true,true).setTranslationKey("coke").setCreativeTab(CreativeTabs.MATERIALS),"item.coke");
  NativeItemTextureRegistry.texture(coke,"hbm:wrong_separate_default");check(coke.getIconString().equals("hbm:coke"),"enum iconString independent of configured Item texture");
  parts=(ItemGenericPart)register(new ItemGenericPart().setTranslationKey("generic_part"),"item.generic_part");
  drill=(ItemDrillbit)register(new ItemDrillbit().setTranslationKey("drillbit"),"item.drillbit");
  check(NativeItemTextureRegistry.atlasLocation("hbm:ingot_steel").toString().equals("hbm:items/ingot_steel"),"native item prefix");
  check(NativeItemTextureRegistry.atlasLocation("hbm:items/ingot_steel").toString().equals("hbm:items/ingot_steel"),"idempotent item prefix");
  check(NativeItemTextureRegistry.atlasLocation("other:blocks/face").toString().equals("other:blocks/face"),"explicit blocks prefix and foreign namespace preserved");
  foundation=new NativeItemTextureModelHandler();renderer=new ItemRenderBase(){};callback=new CallbackItemRegistration(Collections.singletonMap(custom,renderer));
  // Register owner before foundation deliberately: priority, not accidental bus order, supplies its base.
  MinecraftForge.EVENT_BUS.register(callback);MinecraftForge.EVENT_BUS.register(foundation);
 }
 static Item register(Item item,String name){item.setRegistryName("hbm",name);ForgeRegistries.ITEMS.register(item);return item;}
 static void enumBehavior() {
  Item base=coke;
  for(int meta:new int[]{0,1,2,3,-1,Integer.MIN_VALUE,Integer.MAX_VALUE}) {
   ItemStack stack=new ItemStack(coke,1,meta);int index=Math.abs(stack.getItemDamage()%3);String suffix=ItemEnums.EnumCokeType.values()[index].name().toLowerCase(java.util.Locale.US);
   check(base.getTranslationKey(stack).equals("item.coke."+suffix),"native virtual enum name "+meta);
  }
  ItemEnumMulti ordered=new ItemEnumMulti(Ordered.class,true,true);ordered.setCreativeTab(CreativeTabs.MATERIALS);
  NonNullList<ItemStack> items=NonNullList.create();ordered.getSubItems(CreativeTabs.MATERIALS,items);
  check(items.size()==3&&items.get(0).getMetadata()==2&&items.get(1).getMetadata()==0&&items.get(2).getMetadata()==1,"original IOrderedEnum creative order");
  items.clear();ordered.getSubItems(CreativeTabs.COMBAT,items);check(items.isEmpty(),"native creative tab filtering");
  ItemCircuit circuit=new ItemCircuit();circuit.setCreativeTab(CreativeTabs.MATERIALS);items.clear();((Item)circuit).getSubItems(CreativeTabs.MATERIALS,items);
  String[] circuitOrder={"VACUUM_TUBE","NUMITRON","CAPACITOR","CAPACITOR_TANTALIUM","ATOMIC_CLOCK","PCB","SILICON","CHIP","CHIP_BISMOID","CHIP_QUANTUM","ANALOG","BASIC","ADVANCED","CAPACITOR_BOARD","BISMOID","AVIONICS","AERO","GAAS","QUANTUM","CONTROLLER_CHASSIS","CONTROLLER","CONTROLLER_ADVANCED","CONTROLLER_QUANTUM","CAPACITOR_LANTHANIUM","PROCESST1","PROCESST2","PROCESST3","GASCHIP","HFCHIP","MOLYCHIP"};
  check(items.size()==circuitOrder.length,"actual native circuit creative size");for(int i=0;i<circuitOrder.length;i++)check(ItemCircuit.EnumCircuitType.values()[items.get(i).getMetadata()].name().equals(circuitOrder[i]),"original circuit creative order "+i);
  items.clear();circuit.getSubItems(CreativeTabs.COMBAT,items);check(items.isEmpty(),"circuit foreign creative tab filtered");items.clear();circuit.getSubItems(CreativeTabs.SEARCH,items);check(items.size()==30,"native search exposes original circuit variants");
  ItemEnumMulti hidden=new ItemEnumMulti(Ordered.class,true,true);hidden.setCreativeTab(null);items.clear();hidden.getSubItems(CreativeTabs.SEARCH,items);check(items.isEmpty(),"hidden enum not leaked into creative search");
  check(coke.stackFromEnum(4,ItemEnums.EnumCokeType.PETROLEUM).getCount()==4,"enum stack count");check(coke.stackFromEnum(Ordered.A)==null,"wrong enum rejection unchanged");
  Item drillBase=drill;check(drillBase.getTranslationKey(new ItemStack(drill,1,3)).equals("item.drillbit_hss_diamond"),"real descendant underscore naming native virtual dispatch");
  List<String> tooltip=new ArrayList<>();drillBase.addInformation(new ItemStack(drill,1,3),null,tooltip,ITooltipFlag.TooltipFlags.NORMAL);
  check(tooltip.equals(Arrays.asList("\u00a7eSpeed: 120%","\u00a7eTier: 2","\u00a7dFortune 3","\u00a7aVein miner","\u00a7aSilk touch")),"real descendant native tooltip dispatch and null world");
 }
 @SuppressWarnings("unchecked") static void loadSprites()throws Exception {
  Map<String,TextureAtlasSprite> registered=(Map<String,TextureAtlasSprite>)get(atlas,TextureMap.class,"mapRegisteredSprites");
  Map<String,TextureAtlasSprite> uploaded=(Map<String,TextureAtlasSprite>)get(atlas,TextureMap.class,"mapUploadedSprites");
  int offset=0;
  for(TextureAtlasSprite sprite:registered.values()) {
   ResourceLocation id=new ResourceLocation(sprite.getIconName());
   try(IResource resource=resources.getResource(new ResourceLocation(id.getNamespace(),"textures/"+id.getPath()+".png"))) {
    java.awt.image.BufferedImage image=ImageIO.read(resource.getInputStream());check(image!=null,"original PNG decodes "+id);
    sprite.setIconWidth(image.getWidth());sprite.setIconHeight(image.getHeight());sprite.setFramesTextureData(Collections.singletonList(new int[][]{image.getRGB(0,0,image.getWidth(),image.getHeight(),null,0,image.getWidth())}));
    sprite.initSprite(2048,2048,(offset++%16)*64,(offset/16)*64,false);uploaded.put(sprite.getIconName(),sprite);
   }
  }
 }
 static IBakedModel round(IResourcePack pack,File vanilla,IBakedModel previous)throws Exception {
  try(FileResourcePack vanillaPack=new FileResourcePack(vanilla)) {
   resources.reloadResources(Arrays.asList(vanillaPack,pack));ModelLoaderRegistry.clearModelCache(resources);
   atlas=new TextureMap("textures");set(Minecraft.getMinecraft(),Minecraft.class,"textureMapBlocks",atlas);
   new ModelLoader(resources,atlas,new BlockModelShapes(new ModelManager(atlas)));
   MinecraftForge.EVENT_BUS.post(new ModelRegistryEvent());
   Map<?,?> routes=(Map<?,?>)get(null,ModelLoader.class,"customModels");Map<?,?> meshes=(Map<?,?>)get(null,ModelLoader.class,"customMeshDefinitions");
   check(meshes.containsKey(plain.delegate)&&!routes.containsKey(Pair.of(plain.delegate,0)),"actual native metadata-insensitive plain mesh registered");
   check(!routes.containsKey(Pair.of(custom.delegate,0))&&meshes.containsKey(custom.delegate),"specialized custom mesh owner preserved");
   check(!routes.containsKey(Pair.of(orphan.delegate,0))&&orphan.getRegistryName()==null,"unregistered overwritten construction identity safely skipped");
   check(routes.get(Pair.of(unknown.delegate,7))==UNKNOWN&&!routes.containsKey(Pair.of(unknown.delegate,0)),"unknown metadata route preserved without default override");
   check(!routes.containsKey(Pair.of(coke.delegate,0))&&!routes.containsKey(Pair.of(dynamic.delegate,0))&&!routes.containsKey(Pair.of(foreign.delegate,0)),"enum dynamic and foreign owners not claimed");
   MinecraftForge.EVENT_BUS.post(new TextureStitchEvent.Pre(atlas));
   check(atlas.getTextureExtry("hbm:items/must_not_be_stitched_or_registered")==null,"orphan texture not stitched");
   LegacyIconRegister itemIcons=new LegacyIconRegister(atlas,true);coke.registerIcons(itemIcons);parts.registerIcons(itemIcons);drill.registerIcons(itemIcons);
   check(coke.getIconFromDamage(-1).getIconName().equals("hbm:items/coke.lignite"),"direct enum negative metadata safely wraps");
   check(coke.getIconFromDamage(Integer.MIN_VALUE).getIconName().equals("hbm:items/coke.petroleum"),"direct minimum metadata safely wraps");
   check(coke.getIconFromDamage(1).getIconName().equals("hbm:items/coke.lignite"),"enum actual native atlas sprite");
   check(parts.getIconFromDamage(3).getIconName().equals("hbm:items/low_density_element"),"custom explicit original enum icon dispatch");
   check(drill.getIconFromDamage(3).getIconName().equals("hbm:items/drillbit_hss_diamond"),"descendant underscore atlas name");
   check(itemIcons.registerIcon("hbm:items/ingot_steel").getIconName().equals("hbm:items/ingot_steel"),"qualified item callback idempotence");
   TextureMap blocks=new TextureMap("textures");check(new LegacyIconRegister(blocks).registerIcon("hbm:original_block_name").getIconName().equals("hbm:original_block_name"),"default block callback unchanged");
   TextureAtlasSprite generated=new TextureAtlasSprite("hbm:generated_fixture"){};check(((LegacyTextureAtlasSprite)itemIcons.registerGeneratedSprite(generated)).getSprite()==generated,"generated sprite concrete adapter/identity unaffected");
   ((Map<?,?>)get(atlas,TextureMap.class,"mapRegisteredSprites")).remove("hbm:generated_fixture");
   loadSprites();
   RegistrySimple<ModelResourceLocation,IBakedModel> registry=new RegistrySimple<>();
   TextureAtlasSprite particle=atlas.getAtlasSprite("hbm:items/ingot_steel");
   IBakedModel missing=new SimpleBakedModel(Collections.emptyList(),new EnumMap<>(EnumFacing.class),false,false,atlas.getMissingSprite(),ItemCameraTransforms.DEFAULT,ItemOverrideList.NONE);
   IBakedModel owned=new SimpleBakedModel(Collections.emptyList(),new EnumMap<>(EnumFacing.class),false,false,particle,ItemCameraTransforms.DEFAULT,ItemOverrideList.NONE);
   Method missingFactory=ModelLoaderRegistry.class.getDeclaredMethod("getMissingModel",ResourceLocation.class,Throwable.class);missingFactory.setAccessible(true);
   IModel missingDescriptor=(IModel)missingFactory.invoke(null,new ResourceLocation("hbm:absent_item_foundation_probe"),new FileNotFoundException("intentional missing model probe"));
   IBakedModel fancyMissing=missingDescriptor.bake(missingDescriptor.getDefaultState(),net.minecraft.client.renderer.vertex.DefaultVertexFormats.ITEM,id->particle);
   check(fancyMissing.getClass().getName().equals("net.minecraftforge.client.model.FancyMissingModel$BakedModel"),"genuine Forge per-resource missing wrapper fixture");
   registry.putObject(location(fancy),fancyMissing);registry.putObject(UNKNOWN,owned);registry.putObject(ModelBakery.MODEL_MISSING,missing);registry.putObject(location(plain),missing);registry.putObject(location(custom),missing);registry.putObject(location(protectedModel),owned);
   MinecraftForge.EVENT_BUS.post(new ModelBakeEvent(null,registry,null));
   IBakedModel result=registry.getObject(location(plain));check(result!=null&&result!=missing&&result.getQuads(null,null,0).size()>2,"native generated alpha-edge extrusion, not flat placeholder");
   check(registry.getObject(location(fancy))!=fancyMissing&&registry.getObject(location(fancy)).getQuads(null,null,0).size()>2,"genuine Forge fancy missing resource receives declared native geometry");
   check(result.getParticleTexture()==particle,"explicit original texture wins over mismatched registry name");
   IBakedModel tool=registry.getObject(location(handheld));check(tool!=null&&!tool.handlePerspective(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND).getRight().equals(result.handlePerspective(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND).getRight()),"native full3D declaration uses genuine handheld transforms");
   check(registry.getObject(UNKNOWN)==owned,"unknown native model route identity untouched");
   check(registry.getObject(location(protectedModel))==owned,"nonmissing resource/custom model identity preserved");
   IBakedModel wrapped=registry.getObject(location(custom));check(wrapped instanceof CallbackItemModel,"actual specialized wrapper retains final ownership");
   IBakedModel wrappedBase=((NativeItemBaseModel)wrapped).getBaseModel();check(wrappedBase!=missing&&wrappedBase.getQuads(null,null,0).size()>2,"priority seeds actual native base before wrapper bake");
   check(custom.getTileEntityItemStackRenderer()==renderer,"specialized TEISR identity retained");
   check(wrappedBase.getParticleTexture().getIconName().equals("hbm:items/ingot_copper"),"specialized wrapper receives actual declared particle");
   ModelManager manager=(ModelManager)unsafe.allocateInstance(ModelManager.class);set(manager,ModelManager.class,"modelRegistry",registry);set(manager,ModelManager.class,"defaultModel",missing);
   ItemModelMesher mesher=new ItemModelMesher(manager);ModelLoader.onRegisterItems(mesher);mesher.rebuildCache();
   for(int meta:new int[]{0,1,5,99,32767})check(mesher.getItemModel(new ItemStack(plainSubtype,1,meta))==registry.getObject(location(plainSubtype)),"plain original metadata-insensitive icon route "+meta);
   for(int meta:new int[]{0,1,5,99,32767,65535})check(mesher.getItemModel(new ItemStack(plain,1,meta))==result,"non-subtyped original metadata-insensitive icon route "+meta);
   check(mesher.getItemModel(new ItemStack(plain))==result,"actual native ItemModelMesher plain lookup");check(mesher.getItemModel(new ItemStack(custom))==wrapped,"actual custom mesh lookup survives foundation");
   check(mesher.getItemModel(new ItemStack(unknown,1,7))==owned,"unknown metadata route remains native mesher owner");
   if(previous!=null){check(result!=previous&&result.getParticleTexture()!=previous.getParticleTexture(),"reload renews model and sprite identity");check(previous.getQuads(null,null,0).size()>2,"previous immutable model remains intact");}
   return result;
  }
 }
 static void serverBoundaries()throws Exception {
  set(null,net.minecraftforge.fml.relauncher.FMLLaunchHandler.class,"side",net.minecraftforge.fml.relauncher.Side.SERVER);
  net.minecraftforge.fml.common.asm.transformers.SideTransformer transformer=new net.minecraftforge.fml.common.asm.transformers.SideTransformer();
  for(Class<?> type:new Class<?>[]{NativeItemTextureRegistry.class,ItemEnumMulti.class,ItemDrillbit.class,ItemCircuit.class}) {
   byte[] original;try(InputStream input=type.getResourceAsStream("/"+type.getName().replace('.','/')+".class")){original=com.google.common.io.ByteStreams.toByteArray(input);}
   byte[] stripped=transformer.transform(type.getName(),type.getName(),original);
   org.objectweb.asm.tree.ClassNode node=new org.objectweb.asm.tree.ClassNode();new org.objectweb.asm.ClassReader(stripped).accept(node,0);
   for(org.objectweb.asm.tree.MethodNode method:node.methods)check(!method.desc.contains("net/minecraft/client"),"genuine server stripping removes client callback signature "+type.getSimpleName()+"."+method.name);
   if(type==ItemDrillbit.class||type==ItemCircuit.class)check(node.methods.stream().noneMatch(method->method.name.equals("addInformation")),"native client tooltip stripped for server "+type.getSimpleName());
   if(type==NativeItemTextureRegistry.class)check(node.methods.stream().anyMatch(method->method.name.equals("texture")),"common texture configuration survives real server side transformer");
  }
 }
 public static void main(String[] args)throws Exception {
  setup();enumBehavior();IBakedModel old=round(new FolderResourcePack(new File(args[0])),new File(args[2]),null);
  try(FileResourcePack jar=new FileResourcePack(new File(args[1]))){round(jar,new File(args[2]),old);}
  MinecraftForge.EVENT_BUS.unregister(callback);MinecraftForge.EVENT_BUS.unregister(foundation);serverBoundaries();
  System.out.println("PASS native item foundation: "+checks+" assertions; actual Forge event bus, registry, generated model, atlas sprite/PNG, mesher, native tooltip override, actual server-side stripping and reload. Complete listed production classes, no NTM collaborator stubs. Controlled headless atlas population, not live texture upload or full ModItems initialization.");
 }
}
