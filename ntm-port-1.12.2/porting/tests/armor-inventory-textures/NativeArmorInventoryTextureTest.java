import com.google.gson.*;
import com.hbm.registry.GameRegistry;
import com.hbm.render.item.*;
import com.hbm.render.item.nativeitem.callback.*;
import com.hbm.render.util.NativeItemBaseModel;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.*;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.RegistryManager;

/** Native inventory-route probe. Real ItemArmor/Item receivers, NOT complete mod armor constructors. */
public final class NativeArmorInventoryTextureTest {
 static final Map<String,Item> fields=new LinkedHashMap<>();
 static final Map<Item,JsonObject> expected=new IdentityHashMap<>();
 static final Map<Item,ItemRenderBase> renderers=new IdentityHashMap<>();
 static final Map<Item,IBakedModel> previous=new IdentityHashMap<>();
 static final List<Item> superseded=new ArrayList<>();
 static int checks,rounds;
 static void check(boolean v,String m){checks++;if(!v)throw new AssertionError(m);}
 static boolean provider(JsonObject row){return row.get("callback").getAsBoolean();}
 static final List<String> order=new ArrayList<>();
 /** Extracted ArmorFSB constructor body tail; native super fixture is explicit, not a migrated slot API. */
 static final class ConstructorTail extends ItemArmor {
  String texture;
  ConstructorTail(String texture) {
   super(ItemArmor.ArmorMaterial.IRON,0,EntityEquipmentSlot.HEAD);
   // EXACT_CONSTRUCTOR_TAIL
   order.add("constructed");
  }
  ConstructorTail observe(){check("hbm:armor".equals(NativeItemTextureRegistry.texture(this)),"original constructor default visible before fluent override");order.add("receiver");return this;}
 }
 static String argument(){order.add("argument");return "hbm:steel_helmet";}
 static void constructorTiming(){
  ConstructorTail original=new ConstructorTail("hbm:textures/armor/steel_1.png");
  ConstructorTail result=NativeItemTextureRegistry.texture(original.observe(),argument());
  check(result==original,"helper generic type and object identity");check(order.equals(Arrays.asList("constructed","receiver","argument")),"constructor before receiver before argument");
  check(result.texture.equals("hbm:textures/armor/steel_1.png"),"worn texture field not reassigned by item texture");
  check(NativeItemTextureRegistry.texture(result).equals("hbm:steel_helmet"),"explicit declaration supersedes constructor default");
 }
 static void register(JsonObject scope) throws Exception {
  int count=0;
  for(JsonElement e:scope.getAsJsonArray("items")) {
   JsonObject row=e.getAsJsonObject();String name=row.get("field").getAsString();
   Item item=row.get("owner").getAsString().equals("ItemDrop")?new Item():new ItemArmor(ItemArmor.ArmorMaterial.IRON,0,EntityEquipmentSlot.HEAD);
   item.setTranslationKey(row.get("translation").getAsString());
   if(!row.get("damageable").getAsBoolean())item.setMaxDamage(0);
   check(item.isDamageable()==row.get("damageable").getAsBoolean(),"actual source owner damageability boundary "+name);
   check(NativeItemTextureRegistry.texture(item,row.get("texture").getAsString())==item,"native receiver identity "+name);
   Item old=fields.put(name,item);if(old!=null){superseded.add(old);expected.remove(old);}
   expected.put(item,row);count++;
  }
  check(count==105&&fields.size()==103&&superseded.size()==2,"all declarations retain two original overwritten jacket identities");
  GameRegistry.beginRegistrationTables();ArmorRegistrationSlice.register(fields);GameRegistry.finishRegistrationTables();
  for(Item item:fields.values())check(item.getRegistryName()==null,"registration not early");
  MinecraftForge.EVENT_BUS.post(new RegistryEvent.Register<Block>(RegistryManager.ACTIVE.getName(ForgeRegistries.BLOCKS),ForgeRegistries.BLOCKS));
  for(Item item:fields.values())check(item.getRegistryName()==null,"block event does not flush items");
  MinecraftForge.EVENT_BUS.post(new RegistryEvent.Register<Item>(RegistryManager.ACTIVE.getName(ForgeRegistries.ITEMS),ForgeRegistries.ITEMS));
  int last=-1;
  for(JsonElement e:scope.getAsJsonArray("registration_order")) {
   Item item=fields.get(e.getAsString());int id=Item.getIdFromItem(item);check(id>last,"exact production registry table order");last=id;
   check(ForgeRegistries.ITEMS.getValue(item.getRegistryName())==item,"native Forge registry identity");
   if(provider(expected.get(item)))renderers.put(item,new ItemRenderBase(){});
  }
  for(Item item:superseded)check(item.getRegistryName()==null,"superseded jacket is never model owner");
 }
 public static final class Audit {
  @SubscribeEvent(priority=EventPriority.LOWEST) public void bake(ModelBakeEvent event) throws Exception {
   rounds++;
   ModelManager manager=(ModelManager)NativeItemFoundationTest.unsafe.allocateInstance(ModelManager.class);
   NativeItemFoundationTest.set(manager,ModelManager.class,"modelRegistry",event.getModelRegistry());
   NativeItemFoundationTest.set(manager,ModelManager.class,"defaultModel",event.getModelRegistry().getObject(ModelBakery.MODEL_MISSING));
   ItemModelMesher mesher=new ItemModelMesher(manager);ModelLoader.onRegisterItems(mesher);mesher.rebuildCache();
   Map<?,?> routes=(Map<?,?>)NativeItemFoundationTest.get(null,ModelLoader.class,"customModels");
   Map<?,?> meshes=(Map<?,?>)NativeItemFoundationTest.get(null,ModelLoader.class,"customMeshDefinitions");
   for(Map.Entry<Item,JsonObject> e:expected.entrySet()) {
    Item item=e.getKey();JsonObject row=e.getValue();String name=row.get("field").getAsString();
    ModelResourceLocation location=new ModelResourceLocation(item.getRegistryName(),"inventory");
    IBakedModel model=event.getModelRegistry().getObject(location);check(model!=null,"declared model exists "+name);
    IBakedModel base=model;
    if(provider(row)) {
     check(model instanceof CallbackItemModel,"custom armor keeps final callback model "+name);base=((NativeItemBaseModel)model).getBaseModel();
     check(item.getTileEntityItemStackRenderer()==renderers.get(item),"custom armor keeps exact TEISR owner "+name);
     check(meshes.containsKey(item.delegate)&&!routes.containsKey(org.apache.commons.lang3.tuple.Pair.of(item.delegate,0)),"foundation does not replace custom route "+name);
    } else check(!(model instanceof CallbackItemModel)&&meshes.containsKey(item.delegate)&&!routes.containsKey(org.apache.commons.lang3.tuple.Pair.of(item.delegate,0)),"plain armor native constant-mesh route without metadata0 table "+name);
    TextureAtlasSprite sprite=Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(row.get("atlas").getAsString());
    check(sprite!=Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite(),"original sprite is not missing "+name);
    check(base.getParticleTexture()==sprite,"original texture identity reaches model "+name);
    check(base.getQuads(null,null,0).size()>2,"real generated alpha extrusion "+name);
    for(int damage:new int[]{0,1,5,99,32767})check(mesher.getItemModel(new ItemStack(item,1,damage))==model,"native armor damage-independent icon lookup "+name+" "+damage);
    if(previous.containsKey(item)){check(model!=previous.get(item),"reload creates new model "+name);check(model.getParticleTexture()!=previous.get(item).getParticleTexture(),"reload creates new sprite "+name);}
    previous.put(item,model);
   }
   for(Item item:superseded)check(!routes.containsKey(org.apache.commons.lang3.tuple.Pair.of(item.delegate,0))&&!meshes.containsKey(item.delegate),"orphan jacket has no mesh or model route");
  }
 }
 public static void main(String[] args)throws Exception {
  NativeItemFoundationTest.setup();constructorTiming();
  JsonObject scope=new JsonParser().parse(new FileReader(args[3])).getAsJsonObject();register(scope);
  CallbackItemRegistration callbacks=new CallbackItemRegistration(renderers);Audit audit=new Audit();
  MinecraftForge.EVENT_BUS.register(callbacks);MinecraftForge.EVENT_BUS.register(audit);
  IBakedModel first=NativeItemFoundationTest.round(new FolderResourcePack(new File(args[0])),new File(args[2]),null);
  try(FileResourcePack jar=new FileResourcePack(new File(args[1]))){NativeItemFoundationTest.round(jar,new File(args[2]),first);}
  check(rounds==2,"directory and packaged resource reloads exercised");
  OriginalArmorIcons.run(new File(args[4]),scope);
  System.out.println("PASS armor inventory textures: "+checks+" checks, 105 declarations / 103 final fields / "+renderers.size()+" native custom callback owners, two actual Forge registry/model/atlas/mesher reloads. Native ItemArmor/Item receiver fixtures, extracted constructor tail and registration statements, not complete mod armor classes or GL rendering.");
 }
}
