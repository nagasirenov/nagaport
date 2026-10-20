import com.google.gson.*;
import com.hbm.render.item.*;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.resources.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

/** Real Forge/ItemModelMesher regression; named ItemDrop cases are native receiver fixtures, not mod-class runtime proof. */
public final class NativeDefaultItemMetadataTest {
 static final List<Item> matrix=new ArrayList<>(), owners=new ArrayList<>();
 static final Map<Item,IBakedModel> previous=new IdentityHashMap<>();
 static Item late, undeclared, itemBlock, customMesh;
 static ItemMeshDefinition originalMesh;
 static int checks, rounds, metadataLookups;
 static final int[] SAMPLES={0,1,2,255,256,257,32766,32767,32768,65534,65535};
 static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 static ModelResourceLocation loc(Item item){return NativeItemFoundationTest.location(item);}
 static Item declare(Item item,String name){NativeItemTextureRegistry.texture(item,"hbm:ingot_steel");return NativeItemFoundationTest.register(item,name);}
 static void setup(JsonArray scope)throws Exception {
  // Nondamageable/non-subtyped first: CP82 must fail specifically at damage 1.
  for(int maxDamage:new int[]{0,256})for(boolean subtypes:new boolean[]{false,true})
   matrix.add(declare(new Item().setMaxDamage(maxDamage).setHasSubtypes(subtypes),"metadata_matrix_"+maxDamage+"_"+subtypes));
  for(JsonElement entry:scope){JsonObject row=entry.getAsJsonObject();Item item=new Item().setTranslationKey(row.get("translation").getAsString());
   if(row.get("full3d").getAsBoolean())item.setFull3D();
   NativeItemTextureRegistry.texture(item,row.get("texture").getAsString());
   NativeItemFoundationTest.register(item,"item."+row.get("translation").getAsString());owners.add(item);
   check(item.getMaxDamage()==0&&!item.getHasSubtypes(),"ItemDrop receiver default metadata shape");}
  late=NativeItemTextureRegistry.texture(new Item(),"hbm:ingot_steel");
  undeclared=NativeItemFoundationTest.register(new Item(),"metadata_undeclared");
  itemBlock=declare(new ItemBlock(Blocks.STONE),"metadata_itemblock");
  customMesh=declare(new Item(),"metadata_unknown_mesh");
  originalMesh=stack->NativeItemFoundationTest.UNKNOWN;
  ModelLoader.setCustomMeshDefinition(customMesh,originalMesh);
 }
 static void lookup(ItemModelMesher mesher,Item item,IBakedModel expected,int damage,boolean raw)throws Exception {
  ItemStack stack=new ItemStack(item,3,raw?0:damage);
  if(raw){NativeItemFoundationTest.field(ItemStack.class,"itemDamage").setInt(stack,damage);stack.setCount(3);}
  NBTTagCompound tag=new NBTTagCompound();tag.setString("preserved","metadata "+damage);tag.setBoolean("Unbreakable",true);stack.setTagCompound(tag);
  int actual=stack.getItemDamage();
  check(!stack.isEmpty()&&stack.getItem()==item,"valid metadata retains registered item identity "+damage);
  check(actual==damage&&stack.getMetadata()==damage,"native metadata domain "+damage);
  check(mesher.getItemModel(stack)==expected,"actual native mesh damage-independent lookup "+item.getRegistryName()+" damage="+damage);
  check(stack.getItem()==item&&stack.getCount()==3&&stack.getItemDamage()==actual&&stack.getMetadata()==actual,"lookup never mutates stack identity/count/damage");
  check(stack.getTagCompound()==tag&&tag.getString("preserved").equals("metadata "+damage)&&tag.getBoolean("Unbreakable"),"lookup never mutates NBT identity/content");
  metadataLookups++;
 }
 public static final class Audit {
  @SubscribeEvent(priority=EventPriority.LOWEST) public void bake(ModelBakeEvent event)throws Exception {
   rounds++;
   ModelManager manager=(ModelManager)NativeItemFoundationTest.unsafe.allocateInstance(ModelManager.class);
   NativeItemFoundationTest.set(manager,ModelManager.class,"modelRegistry",event.getModelRegistry());
   IBakedModel missing=event.getModelRegistry().getObject(ModelBakery.MODEL_MISSING);
   NativeItemFoundationTest.set(manager,ModelManager.class,"defaultModel",missing);
   ItemModelMesher mesher=new ItemModelMesher(manager);ModelLoader.onRegisterItems(mesher);mesher.rebuildCache();
   Map<?,?> models=(Map<?,?>)NativeItemFoundationTest.get(null,ModelLoader.class,"customModels");
   Map<?,?> meshes=(Map<?,?>)NativeItemFoundationTest.get(null,ModelLoader.class,"customMeshDefinitions");
   for(Item item:matrix){IBakedModel expected=event.getModelRegistry().getObject(loc(item));
    // This checks the behavioral defect before the registration implementation.
    lookup(mesher,item,expected,1,false);
    check(meshes.containsKey(item.delegate)&&!models.containsKey(Pair.of(item.delegate,0)),"default icon uses mesh not metadata-zero-only route");
    for(int damage=0;damage<=65535;damage++)lookup(mesher,item,expected,damage,false);
    // Signed raw metadata is accepted by native isEmpty, although normal constructors/setDamage clamp it.
    for(int damage=-32768;damage<0;damage++)lookup(mesher,item,expected,damage,true);
    for(int input:new int[]{Integer.MIN_VALUE,-32769,-32768,-1}){
     ItemStack normalized=new ItemStack(item,1,input);check(normalized.getItemDamage()==0,"native constructor owns negative normalization");
     check(mesher.getItemModel(normalized)==expected,"constructor-normalized negative input route");}
    for(int invalid:new int[]{65536,Integer.MAX_VALUE}){
     ItemStack empty=new ItemStack(item,1,invalid);check(empty.isEmpty(),"above-domain stack stays native empty");
     check(mesher.getItemModel(empty)==missing,"above-domain stack is not revived by item mesh");}
    if(previous.containsKey(item))check(previous.get(item)!=expected&&previous.get(item).getParticleTexture()!=expected.getParticleTexture(),"matrix reload fresh model/sprite");
    previous.put(item,expected);
   }
   for(Item item:owners){IBakedModel expected=event.getModelRegistry().getObject(loc(item));
    check(expected!=null&&expected!=missing,"reachable ItemDrop declaration baked");
    check(expected.getParticleTexture().getIconName().equals(NativeItemTextureRegistry.atlasLocation(NativeItemTextureRegistry.texture(item)).toString()),"reachable ItemDrop original texture");
    for(int damage:SAMPLES)lookup(mesher,item,expected,damage,false);}
   check(meshes.get(customMesh.delegate)==originalMesh,"unknown custom mesh identity untouched");
   check(mesher.getItemModel(new ItemStack(customMesh,1,1))==event.getModelRegistry().getObject(NativeItemFoundationTest.UNKNOWN),"unknown mesh still supplies native lookup");
   check(mesher.getItemModel(new ItemStack(NativeItemFoundationTest.unknown,1,7))==event.getModelRegistry().getObject(NativeItemFoundationTest.UNKNOWN),"unknown metadata owner untouched");
   check(mesher.getItemModel(new ItemStack(NativeItemFoundationTest.unknown,1,1))==missing,"unknown partial metadata route never broadened");
   for(Item item:Arrays.asList(undeclared,itemBlock,NativeItemFoundationTest.foreign,NativeItemFoundationTest.dynamic,NativeItemFoundationTest.coke))
    check(!meshes.containsKey(item.delegate)&&!models.containsKey(Pair.of(item.delegate,0)),"ineligible/unconfigured owner not claimed "+item.getRegistryName());
   for(int damage:SAMPLES){
    check(mesher.getItemModel(new ItemStack(NativeItemFoundationTest.protectedModel,1,damage))==event.getModelRegistry().getObject(loc(NativeItemFoundationTest.protectedModel)),"resource-pack model kept at every metadata");
    check(mesher.getItemModel(new ItemStack(NativeItemFoundationTest.custom,1,damage))==event.getModelRegistry().getObject(loc(NativeItemFoundationTest.custom)),"specialized wrapper retained at every metadata");
   }
   if(rounds==1)check(late.getRegistryName()==null&&!meshes.containsKey(late.delegate),"unregistered declaration not prematurely owned");
   else {check(ForgeRegistries.ITEMS.getValue(late.getRegistryName())==late,"late registered identity");for(int damage:SAMPLES)lookup(mesher,late,event.getModelRegistry().getObject(loc(late)),damage,false);}
   Map<Item,String> snapshot=NativeItemTextureRegistry.textures();
   // Re-enter this provider only: specialized subscribers may legitimately recreate their own meshes.
   Map<?,?> oldMeshes=new IdentityHashMap<>((Map)meshes);NativeItemFoundationTest.foundation.register(new ModelRegistryEvent());
   for(Object key:oldMeshes.keySet())check(meshes.get(key)==oldMeshes.get(key),"idempotent model-registry event preserves mesh identity");
   check(snapshot.equals(NativeItemTextureRegistry.textures()),"model path never mutates declarations");
  }
 }
 public static void main(String[] args)throws Exception {
  NativeItemFoundationTest.setup();
  JsonArray scope=new JsonParser().parse(new FileReader(args[3])).getAsJsonArray();setup(scope);
  Audit audit=new Audit();MinecraftForge.EVENT_BUS.register(audit);
  IBakedModel old=NativeItemFoundationTest.round(new FolderResourcePack(new File(args[0])),new File(args[2]),null);
  NativeItemFoundationTest.register(late,"metadata_late_registration");
  try(FileResourcePack pack=new FileResourcePack(new File(args[1]))){NativeItemFoundationTest.round(pack,new File(args[2]),old);}
  check(rounds==2,"directory and packaged-resource reloads");
  OriginalDefaultIcons.run(new File(args[4]));
  System.out.println("PASS default item metadata: "+checks+" assertions / "+metadataLookups+" native mesher lookups, 4 maxDamage/subtype combinations, entire native nonempty raw metadata interval [-32768,65535], constructor-domain [0,65535], 11 existing ItemDrop native receiver fixtures, late registration, two reloads, unknown owners, resource models and specialized wrapper controls. No NTM class-constructor/full-mod/GL proof claimed.");
 }
}
