import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineElectricFurnace;
import com.hbm.tileentity.machine.TileEntityMachineElectricFurnace;
import com.hbm.inventory.container.ContainerElectricFurnace;
import com.hbm.items.machine.ItemBattery;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import com.mojang.authlib.GameProfile;
import java.util.*;

/** Combined actual furnace block/tile/container/battery, not separate target stand-ins.
 * Native World/Chunk lifecycle fixture adapted from FurnaceBlockNativeTest.
 * Lighting, entity spawning, persistence and networking are explicitly controlled.
 */
public final class ElectricFurnaceCombinedNativeTest {
    static int checks;
    static final BlockPos POS=new BlockPos(8,64,8);
    static void check(boolean ok,String text){checks++;if(!ok)throw new AssertionError(text);}
    static final class NativeWorld extends World {
        final Map<Long,Chunk> chunks=new HashMap<>();
        final List<EntityItem> drops=new ArrayList<>();
        long time;
        NativeWorld(){
            super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),false);
            provider.setWorld(this);chunkProvider=createChunkProvider();
            for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){Chunk c=new Chunk(this,x,z);c.markLoaded(true);chunks.put(key(x,z),c);}
        }
        static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
        @Override protected IChunkProvider createChunkProvider(){return new IChunkProvider(){
            public Chunk getLoadedChunk(int x,int z){return chunks.get(key(x,z));}
            public Chunk provideChunk(int x,int z){return getLoadedChunk(x,z);}
            public boolean tick(){return false;}
            public String makeString(){return "native combined furnace fixture";}
            public boolean isChunkGeneratedAt(int x,int z){return getLoadedChunk(x,z)!=null;}
        };}
        @Override protected boolean isChunkLoaded(int x,int z,boolean allowEmpty){return chunks.containsKey(key(x,z));}
        @Override public boolean checkLight(BlockPos pos){return true;}
        @Override public long getTotalWorldTime(){return time;}
        @Override public boolean spawnEntity(Entity entity){drops.add((EntityItem)entity);return true;}
    }
    static final class Player extends EntityPlayer {
        Player(World world){super(world,new GameProfile(new UUID(8,64),"combined-furnace"));}
        public boolean isSpectator(){return false;}
        public boolean isCreative(){return false;}
    }
    static <T extends Item>T item(String name,T item){ForgeRegistries.ITEMS.register(item.setRegistryName(new ResourceLocation("furnace_combined",name)));return item;}
    static MachineElectricFurnace block(boolean active){
        MachineElectricFurnace b=new MachineElectricFurnace(active);b.setTranslationKey("machine_electric_furnace_"+(active?"on":"off"));
        ForgeRegistries.BLOCKS.register(b.setRegistryName(new ResourceLocation("furnace_combined",active?"on":"off")));
        ForgeRegistries.ITEMS.register(new ItemBlock(b).setRegistryName(b.getRegistryName()));return b;
    }
    public static void main(String[] args)throws Exception {
        Bootstrap.register();TileEntity.register("hbm_combined_electric_furnace",TileEntityMachineElectricFurnace.class);
        MachineElectricFurnace off=block(false),on=block(true);ModBlocks.machine_electric_furnace_off=off;ModBlocks.machine_electric_furnace_on=on;
        ItemBattery battery=item("battery",new ItemBattery(10000,200,200));
        ItemMachineUpgrade speed=item("speed3",new ItemMachineUpgrade(UpgradeType.SPEED,3));
        NativeWorld world=new NativeWorld();check(world.setBlockState(POS,off.getDefaultState()),"native placement succeeds");
        TileEntityMachineElectricFurnace f=(TileEntityMachineElectricFurnace)world.getTileEntity(POS);
        check(f!=null&&f.getClass()==TileEntityMachineElectricFurnace.class,"actual production tile created by actual block");
        check(f.getWorld()==world&&f.getPos().equals(POS)&&!f.isInvalid(),"native tile world/position/liveness");
        Player player=new Player(world);player.setPosition(8.5,64.5,8.5);player.rotationYaw=90;
        off.onBlockPlacedBy(world,POS,world.getBlockState(POS),player,new ItemStack(off).setStackDisplayName("Combined furnace"));
        check(world.getTileEntity(POS)==f&&f.getName().equals("Combined furnace")&&off.getMetaFromState(world.getBlockState(POS))==5,"actual placement retains tile/name and facing");
        ContainerElectricFurnace container=new ContainerElectricFurnace(player.inventory,f);
        player.inventory.setInventorySlotContents(9,ItemBattery.getFullBattery(battery));
        player.inventory.setInventorySlotContents(10,new ItemStack(Blocks.IRON_ORE));
        container.transferStackInSlot(player,4);container.transferStackInSlot(player,5);
        check(f.slots[0].getItem()==battery&&f.slots[1].getItem()==Item.getItemFromBlock(Blocks.IRON_ORE),"actual native container routes battery/input");
        ItemStack[] slots=f.slots;ItemStack batteryAlias=f.slots[0];
        world.time=1;f.update();
        check(world.getBlockState(POS).getBlock()==on&&f.progress==1&&f.power==150,"actual first tick activates block and debits energy");
        for(int t=2;t<=99;t++){world.time=t;f.update();}
        check(world.getTileEntity(POS)==f&&f.slots==slots&&f.slots[0]==batteryAlias&&!f.isInvalid(),"99 native block transitions preserve tile/storage/item identity");
        check(f.getName().equals("Combined furnace")&&on.getMetaFromState(world.getBlockState(POS))==5&&world.drops.isEmpty(),"active swaps preserve name/facing without inventory drops");
        world.time=100;f.update();
        check(world.getBlockState(POS).getBlock()==off&&world.getTileEntity(POS)==f&&!f.isInvalid(),"actual completion deactivates same live tile");
        check(f.progress==0&&f.slots[1]==null&&f.slots[2].getItem()==Items.IRON_INGOT&&f.slots[2].getCount()==1,"actual combined100tick recipe input/output conservation");
        check(10000-battery.getCharge(batteryAlias)==f.power+5000,"actual combined battery debit equals stored receipt plus5000 consumed");
        check(f.slots==slots&&f.slots[0]==batteryAlias&&f.getName().equals("Combined furnace")&&world.drops.isEmpty(),"completion retains inventory aliases/name without drops");
        world.setBlockState(POS,world.getBlockState(POS).withProperty(MachineElectricFurnace.METADATA,2),2);
        check(world.getTileEntity(POS)==f&&f.slots==slots&&f.power==5000,"native same-block metadata edit preserves complete tile");
        // The already-created real container remains bound to the retained production tile.
        ItemStack output=container.transferStackInSlot(player,2);
        check(output.getCount()==1&&output.getItem()==Items.IRON_INGOT&&f.slots[2]==null&&player.inventory.getStackInSlot(8).getItem()==Items.IRON_INGOT,"pre-transition container extracts post-transition output once");
        player.inventory.setInventorySlotContents(9,new ItemStack(speed));player.inventory.setInventorySlotContents(10,new ItemStack(Blocks.IRON_ORE));
        container.transferStackInSlot(player,4);container.transferStackInSlot(player,5);
        for(int t=101;t<=125;t++){world.time=t;f.update();}
        check(f.maxProgress==25&&f.consumption==200&&f.power==0&&f.slots[2].getCount()==1,"actual cached speed3 upgrade25ticks at200HE");
        check(world.getTileEntity(POS)==f&&world.getBlockState(POS).getBlock()==off&&off.getMetaFromState(world.getBlockState(POS))==2,"upgraded completion keeps original tile/new facing");
        NBTTagCompound saved=f.writeToNBT(new NBTTagCompound());check(saved.getString("name").equals("Combined furnace")&&saved.getLong("power")==0&&saved.getInteger("progress")==0,"combined actual tile NBT state");
        world.setBlockToAir(POS);int batteries=0,ingots=0,upgrades=0;
        for(EntityItem drop:world.drops){ItemStack stack=drop.getItem();if(stack.getItem()==battery)batteries+=stack.getCount();if(stack.getItem()==Items.IRON_INGOT)ingots+=stack.getCount();if(stack.getItem()==speed)upgrades+=stack.getCount();}
        check(world.getTileEntity(POS)==null&&batteries==1&&ingots==1&&upgrades==1,"actual native break drops each remaining inventory item once");
        System.out.println("PASS combined actual furnace block/tile/container/battery/upgrade native World/Chunk smoke: "+checks+" assertions. No live gameplay, actual GUI, XP or transport claim.");
    }
}
