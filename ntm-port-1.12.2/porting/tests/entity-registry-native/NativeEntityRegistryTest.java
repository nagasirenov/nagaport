import com.hbm.entity.ModEntityList;
import com.hbm.entity.item.EntityFallingBlockNT;
import net.minecraft.entity.*;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.registry.*;

/** Real Forge registry and complete production wrapper. Fixture entities exercise
 * overload/egg cases; the representative production entity is the complete falling owner. */
public class NativeEntityRegistryTest {
    public static class EggEntity extends Entity {
        public EggEntity(World w) { super(w); }
        protected void entityInit() {}
        protected void readEntityFromNBT(NBTTagCompound n) {}
        protected void writeEntityToNBT(NBTTagCompound n) {}
    }
    public static class UnregisteredEntity extends EggEntity { public UnregisteredEntity(World w) {super(w);} }
    public static class SentinelEntity extends EggEntity { public SentinelEntity(World w) {super(w);} }
    public static class DefaultEntity extends EggEntity { public DefaultEntity(World w) {super(w);} }
    public static class CustomEggEntity extends EggEntity { public CustomEggEntity(World w) {super(w);} }
    static int checks;
    static void check(boolean b,String s) {checks++;if(!b)throw new AssertionError(s);}
    static void rejected(Runnable r) {boolean fail=false;try{r.run();}catch(IllegalArgumentException e){fail=true;}check(fail,"reject duplicate/invalid before side effects");}
    static ResourceLocation key(String s) {return new ResourceLocation("hbm",s);}
    public static void main(String[] args) throws Exception {
        Bootstrap.register();
        FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
        ModMetadata md=new ModMetadata();md.modId="hbm";md.name="Entity registry proof";md.version="1";
        DummyModContainer owner=new DummyModContainer(md);Loader.instance().setupTestHarness(owner);Loader.instance().setActiveModContainer(owner);
        check(EntityList.getKey(EntityFallingBlockNT.class)==null,"no early fake identity for actual owner");
        check(!ForgeRegistries.ENTITIES.containsKey(key("entity_falling_block_nt")),"native key absent before register");
        check(ModEntityList.getData(158)==null,"wrapper absent before register");
        ModEntityList.registerEntity(EntityFallingBlockNT.class,"entity_falling_block_nt",158,"hbm",1000,1,true);
        check(EntityList.getKey(EntityFallingBlockNT.class).equals(key("entity_falling_block_nt")),"real Forge registration creates identity");
        EntityRegistry.EntityRegistration registration=EntityRegistry.instance().lookupModSpawn(EntityFallingBlockNT.class,false);
        check(registration.getModEntityId()==158 && registration.getTrackingRange()==1000 && registration.getUpdateFrequency()==1 && registration.sendsVelocityUpdates(),"local ID/tracking/update/velocity preserved");
        check(ForgeRegistries.ENTITIES.getValue(key("entity_falling_block_nt")).getEntityClass()==EntityFallingBlockNT.class,"actual entry class");
        check(EntityList.createEntityByIDFromName(key("entity_falling_block_nt"),null) instanceof EntityFallingBlockNT,"native actual factory");
        check(ModEntityList.createEntityByID(158,null)==null,"original wrapper no-egg creation gate");
        check(ModEntityList.getEggFromEntity(new EntityFallingBlockNT(null)).isEmpty(),"actual no egg");
        check("hbm.entity_falling_block_nt".equals(ModEntityList.getName(158)),"legacy display name");
        check("hbm.entity_falling_block_nt".equals(EntityList.getTranslationName(key("entity_falling_block_nt"))),"native translation name retains legacy namespace");
        check(!ModEntityList.hasEntitiesWithEggs(),"no egg data yet");
        rejected(()->ModEntityList.registerEntityEgg(UnregisteredEntity.class,1,2));
        check(EntityList.getKey(UnregisteredEntity.class)==null,"egg registration cannot invent entity identity");
        ModEntityList.registerEntity(EggEntity.class,"proof_egg",178,"hbm",0x204131,0x75ce00);
        check(ModEntityList.hasEntitiesWithEggs(),"hasEgg enumeration");
        EntityList.EntityEggInfo info=EntityList.ENTITY_EGGS.get(key("proof_egg"));
        check(info!=null && info.primaryColor==0x204131 && info.secondaryColor==0x75ce00,"native egg colors");
        ItemStack egg=ModEntityList.getEggFromEntity(new EggEntity(null));
        check(egg.getItem()==Items.SPAWN_EGG && egg.getMetadata()==0,"native item not legacy integer metadata");
        check(key("proof_egg").equals(ItemMonsterPlacer.getNamedIdFrom(egg)),"native egg id API");
        check("hbm:proof_egg".equals(egg.getTagCompound().getCompoundTag("EntityTag").getString("id")),"actual EntityTag.id");
        ItemStack copy=egg.copy();copy.getTagCompound().getCompoundTag("EntityTag").setString("id","hbm:other");
        check(key("proof_egg").equals(ItemMonsterPlacer.getNamedIdFrom(egg)),"deep copy isolation");
        check(key("proof_egg").equals(ItemMonsterPlacer.getNamedIdFrom(new ItemStack(egg.writeToNBT(new NBTTagCompound())))),"egg NBT roundtrip");
        check(ModEntityList.createEntityByID(178,null).getClass()==EggEntity.class,"wrapper native factory route");
        java.lang.reflect.Field factory=EntityEntry.class.getDeclaredField("factory");factory.setAccessible(true);
        EntityEntry entry=ForgeRegistries.ENTITIES.getValue(key("proof_egg"));
        Object originalFactory=factory.get(entry);final int[] routed={0};
        factory.set(entry,(java.util.function.Function<World,Entity>)(w->{routed[0]++;return new EggEntity(w);}));
        check(ModEntityList.createEntityByID(178,null) instanceof EggEntity && routed[0]==1,"wrapper dispatches native EntityEntry factory, not private reflection");
        factory.set(entry,originalFactory);
        ModEntityList.registerEntity(SentinelEntity.class,"proof_sentinel",179,"hbm",-1,123);
        check(ModEntityList.getData(179).hasEgg && ModEntityList.createEntityByID(179,null) instanceof SentinelEntity,"historical hasEgg creation gate independent of sentinel color");
        check(ModEntityList.getEggFromEntity(new SentinelEntity(null)).isEmpty(),"sentinel color means no actual native egg");
        ModEntityList.registerEntity(DefaultEntity.class,"proof_default",180,"hbm");
        registration=EntityRegistry.instance().lookupModSpawn(DefaultEntity.class,false);
        check(registration.getTrackingRange()==80 && registration.getUpdateFrequency()==3 && registration.sendsVelocityUpdates(),"default tracking");
        ModEntityList.registerEntity(CustomEggEntity.class,"proof_custom",181,"hbm",33,7,false,7,8);
        registration=EntityRegistry.instance().lookupModSpawn(CustomEggEntity.class,false);
        check(registration.getTrackingRange()==33 && registration.getUpdateFrequency()==7 && !registration.sendsVelocityUpdates(),"custom egg tracking");
        check(ModEntityList.getDatasWithEggs().length==3,"hasEgg array includes sentinel");
        ModEntityList.registerEntityEgg(DefaultEntity.class,12,34);
        check(!ModEntityList.getEggFromEntity(new DefaultEntity(null)).isEmpty(),"public egg helper targets genuine registered class");
        check(!ModEntityList.getData(180).hasEgg && ModEntityList.createEntityByID(180,null)==null,"late egg helper does not change original wrapper creation gate");
        int size=ForgeRegistries.ENTITIES.getValuesCollection().size();
        rejected(()->ModEntityList.registerEntity(EggEntity.class,"duplicate",178,"hbm"));
        rejected(()->ModEntityList.registerEntity(EggEntity.class,"duplicate",190,"hbm"));
        rejected(()->ModEntityList.registerEntity(EggEntity.class,"bad",-1,"hbm"));
        rejected(()->ModEntityList.registerEntity(UnregisteredEntity.class,"proof_egg",191,"hbm"));
        check(EntityList.getKey(UnregisteredEntity.class)==null,"key collision leaves class unregistered");
        check(size==ForgeRegistries.ENTITIES.getValuesCollection().size() && ModEntityList.getData(190)==null,"duplicate no registry/wrapper mutation");
        check(ModEntityList.getData(-1)==null && ModEntityList.getData(99999)==null && ModEntityList.createEntityByID(-1,null)==null && ModEntityList.getName(-1)==null,"unknown IDs");
        check(ModEntityList.getEggFromEntity(null).isEmpty(),"null entity");
        System.out.println("PASS "+checks+" complete wrapper + genuine Forge registration/EntityList factory + actual EntityFallingBlockNT + native egg NBT/copy checks; no launched-game or complete EntityMappings startup claim.");
    }
}
