import com.hbm.render.entity.RenderEmpty;
import java.lang.reflect.Field;
import java.util.*;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

/** Real Forge registry and complete production RenderEmpty, without invoking GL or Minecraft startup. */
public final class RenderManagerFactoryTest {
    static int assertions;
    static void check(boolean value,String label) { assertions++;if(!value)throw new AssertionError(label); }
    static RenderManager managerFixture() throws Exception {
        Field field=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");field.setAccessible(true);
        // Real constructor was tried once: RenderLivingBase -> TextureUtil -> glGenTextures
        // requires LWJGL/GL context (failed with no lwjgl64). Do not replace graphics classes.
        // Native instance with deliberately uninitialized unrelated graphics services. Only identity is used.
        return (RenderManager)((sun.misc.Unsafe)field.get(null)).allocateInstance(RenderManager.class);
    }
    public static void main(String[] args) throws Exception {
        RenderManager first=managerFixture(),second=managerFixture();
        final int[] constructed={0}; final List<RenderEmpty> instances=new ArrayList<>();
        final Item[] eagerItems=new Item[8]; final Object[] observedItems={null};
        RenderingRegistry.registerEntityRenderingHandler(Entity.class,manager -> {
            constructed[0]++;observedItems[0]=eagerItems;
            RenderEmpty renderer=new RenderEmpty(manager);instances.add(renderer);return renderer;
        });
        RenderingRegistry.registerEntityRenderingHandler(EntityItem.class,manager -> {
            constructed[0]++;
            RenderEmpty renderer=new RenderEmpty(manager);instances.add(renderer);return renderer;
        });
        check(constructed[0]==0 && instances.isEmpty(),"registration does not instantiate factory renderers");
        Map<Class<? extends Entity>,Render<? extends Entity>> map=new HashMap<>();
        RenderingRegistry.loadEntityRenderers(first,map);
        check(constructed[0]==2 && map.size()==2,"real factory loader constructs/maps both registrations");
        check(map.get(Entity.class)!=map.get(EntityItem.class),"distinct renderer per entity registration");
        check(map.get(Entity.class).getRenderManager()==first && map.get(EntityItem.class).getRenderManager()==first,"supplied native manager identity retained");
        check(observedItems[0]==eagerItems,"eager argument object retained, not reconstructed in callback");
        Render<?> previous=map.get(Entity.class);Map<Class<? extends Entity>,Render<? extends Entity>> map2=new HashMap<>();
        RenderingRegistry.loadEntityRenderers(second,map2);
        check(constructed[0]==4 && map2.get(Entity.class)!=previous,"subsequent loader gets fresh instances, not captured renderers");
        check(map2.get(Entity.class).getRenderManager()==second,"second manager is forwarded, not global/captured first manager");
        RenderEmpty eager=new RenderEmpty(first);
        RenderingRegistry.registerEntityRenderingHandler(Entity.class,eager);
        Map<Class<? extends Entity>,Render<? extends Entity>> oldMap=new HashMap<>();
        RenderingRegistry.loadEntityRenderers(oldMap);
        check(oldMap.get(Entity.class)==eager && constructed[0]==4,"deprecated instance loader retains identity and is separate from factory loader");
        System.out.println("PASS "+assertions+" native registry assertions: deferred callbacks, supplied manager, distinct instances, eager captures and separate deprecated instance path.");
        System.out.println("LIMIT: complete RenderEmpty compiled; controlled real RenderManager identity fixtures, not its constructor, Minecraft startup, GL rendering or 52 complete renderer classes.");
    }
}
