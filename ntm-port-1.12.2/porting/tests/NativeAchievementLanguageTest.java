import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.*;
import net.minecraftforge.fml.common.*;

/** Genuine Forge pack-format fallback/adapter selection and native Locale parsing. No GL. */
public final class NativeAchievementLanguageTest {
    private static int checks;
    private static void check(boolean ok,String why) { checks++; if(!ok) throw new AssertionError(why); }
    private static void set(Object target,String name,Object value) throws Exception { Field f=target.getClass().getDeclaredField(name);f.setAccessible(true);f.set(target,value); }
    public static void main(String[] args) throws Exception {
        String[] keys={"openInventory","mineWood","buildWorkBench","buildPickaxe","buildFurnace","acquireIron","cookFish"};
        for(String path:args) {
            final File source=new File(path);
            ModMetadata md=new ModMetadata();md.modId="hbm";md.name="Language fixture";md.version="1";
            DummyModContainer container=new DummyModContainer(md) {
                @Override public File getSource() { return source; }
                @Override public Class<?> getCustomResourcePackClass() { return source.isDirectory()?FMLFolderResourcePack.class:FMLFileResourcePack.class; }
            };
            MetadataSerializer serializer=new MetadataSerializer();
            serializer.registerMetadataSectionType(new PackMetadataSectionSerializer(),PackMetadataSection.class);
            IResourcePack original=source.isDirectory()?new FMLFolderResourcePack(container):new FMLFileResourcePack(container);
            check(((PackMetadataSection)original.getPackMetadata(serializer,"pack")).getPackFormat()==2,"Forge missing pack.mcmeta fallback is format2");
            FMLClientHandler handler=new FMLClientHandler();
            List<IResourcePack> packs=new ArrayList<>();
            set(handler,"resourcePackList",packs);set(handler,"resourcePackMap",new HashMap<String,IResourcePack>());set(handler,"metaSerializer",serializer);
            handler.addModAsResource(container);
            check(packs.size()==1 && packs.get(0) instanceof LegacyV2Adapter,"genuine FMLClientHandler selects native LegacyV2Adapter");
            ResourceLocation english=new ResourceLocation("hbm","lang/en_us.lang");
            check(packs.get(0).resourceExists(english),"native adapter resolves lower request to existing upper filename");
            SimpleReloadableResourceManager resources=new SimpleReloadableResourceManager(serializer);resources.reloadResources(packs);
            net.minecraft.client.resources.Locale locale=new net.minecraft.client.resources.Locale();
            locale.loadLocaleDataFiles(resources,Arrays.asList("en_us"));
            check(locale.formatMessage("achievement.openInventory",new Object[0]).equals("Taking Inventory"),"exact restored old English title");
            for(String key:keys) {
                String title="achievement."+key,description=title+".desc";
                check(!locale.formatMessage(title,new Object[0]).equals(title),"English legacy title loaded "+key);
                check(!locale.formatMessage(description,new Object[0]).equals(description),"English legacy description loaded "+key);
            }
            check(locale.formatMessage("achievement.openInventory.desc",new Object[] {"E"}).equals("Press 'E' to open your inventory."),"restored keybinding placeholder formats correctly");
            String before=locale.formatMessage("achievement.radPoison",new Object[0]);
            check(!before.equals("achievement.radPoison"),"original NTM English translation still loaded");
            locale.loadLocaleDataFiles(resources,Arrays.asList("en_us","ru_ru"));
            check(!locale.formatMessage("achievement.radPoison",new Object[0]).equals(before),"original Russian NTM translation still overrides English");
            check(locale.formatMessage("achievement.openInventory",new Object[0]).equals("Taking Inventory"),"missing old Russian key uses honest English fallback");
        }
        System.out.println("PASS "+checks+" genuine Forge language/legacy-adapter assertions (folder and archive)");
    }
}
