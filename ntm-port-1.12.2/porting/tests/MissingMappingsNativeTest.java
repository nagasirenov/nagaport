import java.util.*;
import com.hbm.main.MissingMappingHandler;
import com.hbm.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.MissingMappings.Action;
import net.minecraftforge.event.RegistryEvent.MissingMappings.Mapping;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.RegistryManager;

public final class MissingMappingsNativeTest {
    static int checks;
    static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    static Item item(String name){Item item=new Item().setRegistryName("hbm",name);ForgeRegistries.ITEMS.register(item);return item;}
    static void verifyAll() {
        LegacyMissingMappingOracle.prepare();
        LinkedHashSet<ResourceLocation> keys=new LinkedHashSet<>();
        for(String name:LegacyMissingMappingOracle.ignoreMappings)keys.add(new ResourceLocation(name));
        for(String name:LegacyMissingMappingOracle.remapItems.keySet())keys.add(new ResourceLocation(name));
        for(String name:new String[]{"hbm:item.ammo_", "hbm:item.ammo_custom", "hbm:item.ammunition", "foreign:item.ammo_test", "minecraft:item.gasflame1", "hbm:unknown", "hbm:item.canister_NITAN"})keys.add(new ResourceLocation(name));
        Set<ResourceLocation> ignored=new HashSet<>();for(String name:LegacyMissingMappingOracle.ignoreMappings)ignored.add(new ResourceLocation(name));
        List<Mapping<Item>> items=new ArrayList<>();List<Mapping<Block>> blocks=new ArrayList<>();int id=1;
        for(ResourceLocation key:keys){items.add(new Mapping<>(ForgeRegistries.ITEMS,ForgeRegistries.ITEMS,key,id));blocks.add(new Mapping<>(ForgeRegistries.BLOCKS,ForgeRegistries.BLOCKS,key,id++));}
        RegistryEvent.MissingMappings<Item> itemEvent=new RegistryEvent.MissingMappings<>(RegistryManager.ACTIVE.getName(ForgeRegistries.ITEMS),ForgeRegistries.ITEMS,items);
        RegistryEvent.MissingMappings<Block> blockEvent=new RegistryEvent.MissingMappings<>(RegistryManager.ACTIVE.getName(ForgeRegistries.BLOCKS),ForgeRegistries.BLOCKS,blocks);
        MinecraftForge.EVENT_BUS.post(blockEvent);MinecraftForge.EVENT_BUS.post(itemEvent);
        for(int i=0;i<items.size();i++){
            Mapping<Item> im=items.get(i);Mapping<Block> bm=blocks.get(i);boolean ignore=ignored.contains(im.key)||im.key.toString().startsWith("hbm:item.ammo_");
            Item target=LegacyMissingMappingOracle.remapItems.get(im.key.toString());
            check(im.getAction()==(ignore?Action.IGNORE:target!=null?Action.REMAP:Action.DEFAULT),"item decision "+im.key);
            check(im.getTarget()==(!ignore?target:null),"exact target identity "+im.key);
            check(bm.getAction()==(ignore?Action.IGNORE:Action.DEFAULT),"block decision, never item remap "+bm.key);
            check(bm.getTarget()==null,"no block target "+bm.key);
        }
        // The original event prepares its data on every dispatch, not once before item registration.
        check(items.size()>700,"all original retired IDs exercised");
    }
    public static void main(String[] args)throws Exception {
        Bootstrap.register();FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
        ModMetadata metadata=new ModMetadata();metadata.modId="hbm";metadata.name="Missing mappings probe";metadata.version="1";
        DummyModContainer owner=new DummyModContainer(metadata);Loader.instance().setupTestHarness(owner);Loader.instance().setActiveModContainer(owner);
        ModItems.early_explosive_lenses=item("early_explosive_lenses");ModItems.explosive_lenses=item("explosive_lenses");ModItems.briquette=item("briquette");ModItems.fuel_additive=item("fuel_additive");ModItems.toolbox=item("toolbox");
        Mod.EventBusSubscriber annotation=MissingMappingHandler.class.getAnnotation(Mod.EventBusSubscriber.class);
        check(annotation!=null&&annotation.modid().equals("hbm"),"actual common-side automatic subscriber declaration");
        MinecraftForge.EVENT_BUS.register(MissingMappingHandler.class);
        verifyAll();
        ModItems.toolbox=item("replacement_toolbox");verifyAll();
        ModItems.toolbox=null;verifyAll();
        MinecraftForge.EVENT_BUS.unregister(MissingMappingHandler.class);
        System.out.println("PASS "+checks+" actual typed native registry-event decisions, all original retired IDs/five remaps, canonical keys, foreign/unknown defaults, refreshed and absent targets. Not saved-world migration acceptance.");
    }
}
