import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.*;
import com.google.gson.*;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.registry.GameRegistry;
import com.hbm.render.item.NativeItemTextureRegistry;
import net.minecraft.item.*;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.RegistryManager;

/** Native platform classes plus full queue/texture service; exact extracted constructor/register statements. */
public final class ModItemsNativeTest {
    static int checks;
    static void check(boolean value,String label) { checks++; if(!value)throw new AssertionError(label); }
    static Item item(String field)throws Exception{return (Item)ModItems.class.getField(field).get(null);}
    static boolean present(JsonObject row,String field){return row.has(field)&&!row.get(field).isJsonNull();}
    static CreativeTabs tab(String name)throws Exception {
        if(name==null||name.equals("null"))return null;
        if(name.startsWith("MainRegistry."))return (CreativeTabs)MainRegistry.class.getField(name.substring(13)).get(null);
        throw new AssertionError("Unreviewed tab "+name);
    }
    public static void main(String[] args)throws Exception {
        net.minecraft.init.Bootstrap.register();
        FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
        ModMetadata metadata=new ModMetadata();metadata.modId="hbm";metadata.name="Native ModItems construction slice";metadata.version="1";
        DummyModContainer owner=new DummyModContainer(metadata);Loader.instance().setupTestHarness(owner);Loader.instance().setActiveModContainer(owner);
        JsonObject scope=new JsonParser().parse(new FileReader(args[0])).getAsJsonObject();
        JsonArray rows=scope.getAsJsonArray("items");
        ModItems.initializeItem();
        Set<Item> identities=Collections.newSetFromMap(new IdentityHashMap<Item,Boolean>());
        for(JsonElement element:rows) {
            JsonObject row=element.getAsJsonObject();String field=row.get("field").getAsString();Item value=item(field);
            check(value!=null&&identities.add(value),field+" constructed unique native object");
            check(value.getClass().getSimpleName().equals(row.get("type").getAsString()),field+" actual constructor type");
            check(value.getTranslationKey().equals(row.get("translation").getAsString()),field+" exact legacy item. translation identity");
            check(NativeItemTextureRegistry.texture(value).equals(row.get("texture").getAsString()),field+" exact texture string, including intentional mismatches");
            check(value.getRegistryName()==null,field+" identity not registered by construction");
            check(value.getItemStackLimit()==row.get("maximum").getAsInt(),field+" stack maximum preserved");
            check(value.isFull3D()==row.get("full3d").getAsBoolean(),field+" full3D flag preserved");
            check(value.getCreativeTab()==tab(present(row,"tab")?row.get("tab").getAsString():null),field+" tab/null identity preserved");
            if(present(row,"container"))check(value.getContainerItem()==item(row.get("container").getAsString()),field+" actual earlier container identity");
            else check(value.getContainerItem()==null,field+" no container added");
            if(row.has("food")) {
                JsonObject food=row.getAsJsonObject("food");ItemFood nativeFood=(ItemFood)value;ItemStack stack=new ItemStack(value);
                check(nativeFood.getHealAmount(stack)==food.get("heal").getAsInt(),field+" food healing unchanged");
                check(Math.abs(nativeFood.getSaturationModifier(stack)-food.get("saturation").getAsFloat())<.0001F,field+" food saturation unchanged");
                check(nativeFood.isWolfsFavoriteMeat()==food.get("wolf").getAsBoolean(),field+" wolf food flag unchanged");
            }
        }
        check(NativeItemTextureRegistry.textures().size()==scope.get("construction_count").getAsInt(),"every construction retained, including two original overwritten identities");
        GameRegistry.beginRegistrationTables();ModItems.registerItem();GameRegistry.finishRegistrationTables();
        for(JsonElement element:rows) {
            JsonObject row=element.getAsJsonObject();Item value=item(row.get("field").getAsString());ResourceLocation id=new ResourceLocation(row.get("registry").getAsString());
            check(value.getRegistryName()==null&&!ForgeRegistries.ITEMS.containsKey(id),"queue does not register early "+id);
        }
        MinecraftForge.EVENT_BUS.post(new RegistryEvent.Register<Block>(RegistryManager.ACTIVE.getName(ForgeRegistries.BLOCKS),ForgeRegistries.BLOCKS));
        for(Item value:identities)check(value.getRegistryName()==null,"Block event does not flush item queue");
        MinecraftForge.EVENT_BUS.post(new RegistryEvent.Register<Item>(RegistryManager.ACTIVE.getName(ForgeRegistries.ITEMS),ForgeRegistries.ITEMS));
        int previousId=-1;
        for(JsonElement field:scope.getAsJsonArray("registration_order")) {
            int numeric=Item.getIdFromItem(item(field.getAsString()));
            check(numeric>previousId,"exact production registration call order retained");previousId=numeric;
        }
        for(JsonElement element:rows) {
            JsonObject row=element.getAsJsonObject();Item value=item(row.get("field").getAsString());ResourceLocation id=new ResourceLocation(row.get("registry").getAsString());
            check(value.getRegistryName().equals(id),"exact legacy-prefixed registry path "+id);
            check(ForgeRegistries.ITEMS.getValue(id)==value,"real Forge registry retains constructor identity "+id);
            for(int damage:new int[]{0,1,32767}) {
                ItemStack stack=new ItemStack(value,3,damage);check(stack.getItem()==value,"native stack identity");
                check(stack.getTranslationKey().equals(row.get("translation").getAsString()),"native stack key forwards to same item");
            }
        }
        System.out.println("PASS "+checks+" assertions across "+rows.size()+" exact native construction and registry chains; full queue and texture service, actual Forge event bus. Not whole-ModItems compilation or full client launch.");
    }
}
