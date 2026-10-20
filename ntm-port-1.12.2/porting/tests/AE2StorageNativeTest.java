import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.*;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.storage.MEMonitorIInventory;
import com.hbm.handler.ae2.*;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineArcFurnaceLarge;
import com.hbm.tileentity.machine.storage.TileEntityMassStorage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;

/** Native AE2 storage, item identity, monitors and Forge capabilities; NTM surrounding state is isolated. */
public final class AE2StorageNativeTest {
    static int checks;
    static IItemStorageChannel items;
    static final IActionSource source=new IActionSource(){
        public Optional<EntityPlayer> player(){return Optional.empty();}
        public Optional<IActionHost> machine(){return Optional.empty();}
        public <T> Optional<T> context(Class<T> c){return Optional.empty();}
    };
    static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    static ItemStack stack(Item item,int n){return new ItemStack(item,n);}
    static IAEItemStack ae(Item item,long n){return items.createStack(stack(item,1)).setStackSize(n);}
    static long n(IAEItemStack s){return s==null?0:s.getStackSize();}
    static int n(ItemStack s){return s==null?0:s.getCount();}
    static TileEntityMassStorage msu(Item item){TileEntityMassStorage t=new TileEntityMassStorage();t.slots[1]=stack(item,1);return t;}
    static void msuCases(){
        TileEntityMassStorage t=msu(Items.IRON_INGOT);MassStorageMEInventory inv=new MassStorageMEInventory(t);
        IAEItemStack request=ae(Items.IRON_INGOT,250);
        check(n(inv.injectItems(request,Actionable.SIMULATE,source))==22,"capacity includes stockpile and two64 buffers");
        check(t.stack==0&&t.slots[0]==null&&t.slots[2]==null&&t.dirties==0&&request.getStackSize()==250,"simulation immutable");
        check(n(inv.injectItems(request,Actionable.MODULATE,source))==22,"same execute remainder");
        check(t.stack==100&&n(t.slots[0])==64&&n(t.slots[2])==64&&t.dirties==1,"reservoir/input/output order and dirty");
        check(t.slots[0].getItem()==Items.IRON_INGOT&&t.slots[2].getItem()==Items.IRON_INGOT,"new native stacks retain identity");
        check(n(inv.getAvailableItems(items.createList()).getFirstItem())==228,"enumeration exact total");
        t.output=false;t.locked=true;
        check(n(inv.extractItems(ae(Items.IRON_INGOT,150),Actionable.SIMULATE,source))==150&&t.stack==100&&t.dirties==1,"extract simulation");
        check(n(inv.extractItems(ae(Items.IRON_INGOT,150),Actionable.MODULATE,source))==150,"custom ME ignores ordinary lock/output");
        check(t.stack==0&&n(t.slots[0])==14&&n(t.slots[2])==64,"extract stockpile then input before output");
        check(n(inv.extractItems(ae(Items.IRON_INGOT,999),Actionable.MODULATE,source))==78,"partial exhaustion");
        check(t.slots[0]==null&&t.slots[2]==null&&inv.extractItems(ae(Items.IRON_INGOT,1),Actionable.MODULATE,source)==null,"empty extraction AE null");
        check(inv.injectItems(ae(Items.GOLD_INGOT,5),Actionable.MODULATE,source).getStackSize()==5,"wrong item refused");
        ItemStack tagged=stack(Items.IRON_INGOT,1);tagged.setTagCompound(new NBTTagCompound());tagged.getTagCompound().setString("kind","other");
        check(inv.injectItems(items.createStack(tagged),Actionable.MODULATE,source)!=null,"NBT mismatch refused");
        ItemStack damaged=stack(Items.IRON_INGOT,1);damaged.setItemDamage(1);
        check(inv.injectItems(items.createStack(damaged),Actionable.MODULATE,source)!=null,"damage mismatch refused");
        t.slots[1]=tagged.copy();t.capacity=0;
        check(inv.injectItems(items.createStack(tagged).setStackSize(65),Actionable.MODULATE,source)==null,"matching NBT accepted");
        check(ItemStack.areItemStackTagsEqual(t.slots[0],tagged)&&ItemStack.areItemStackTagsEqual(t.slots[2],tagged),"new buffer copies preserve NBT");
        check(n(inv.extractItems(items.createStack(tagged).setStackSize(65),Actionable.MODULATE,source))==65,"matching NBT extracted");
        t.slots[1]=ItemStack.EMPTY;check(inv.injectItems(request,Actionable.MODULATE,source)==request,"EMPTY filter refused");
        check(inv.extractItems(request,Actionable.MODULATE,source)==null&&t.getTotalStockpile()==0,"no filter extraction/list");
        for(Item item:new Item[]{Items.ENDER_PEARL,Items.DIAMOND_PICKAXE}){
            t=msu(item);t.capacity=1;inv=new MassStorageMEInventory(t);long capacity=1+2*stack(item,1).getMaxStackSize();
            check(n(inv.injectItems(ae(item,100),Actionable.MODULATE,source))==100-capacity,"native16/1 item caps");
        }
        t=msu(Items.IRON_INGOT);t.stack=100;t.slots[0]=stack(Items.GOLD_INGOT,12);t.slots[2]=ItemStack.EMPTY;inv=new MassStorageMEInventory(t);
        check(n(inv.injectItems(ae(Items.IRON_INGOT,70),Actionable.MODULATE,source))==6&&n(t.slots[0])==12,"incompatible buffer preserved and EMPTY treated empty");
        check(t.getTotalStockpile()==164,"incompatible buffer excluded");
        t=msu(Items.IRON_INGOT);inv=new MassStorageMEInventory(t);
        long overflow=1L+Integer.MAX_VALUE;
        check(n(inv.injectItems(ae(Items.IRON_INGOT,overflow),Actionable.SIMULATE,source))==Integer.MIN_VALUE,"original long-to-int insertion anomaly preserved");
        check(n(inv.extractItems(ae(Items.IRON_INGOT,overflow),Actionable.SIMULATE,source))==4294967296L,"original extraction arithmetic anomaly preserved");
    }
    static void aflCases(){
        Item ore=Item.getItemFromBlock(Blocks.IRON_ORE);
        TileEntityMachineArcFurnaceLarge t=new TileEntityMachineArcFurnaceLarge();ArcFurnaceLargeMEInventory inv=new ArcFurnaceLargeMEInventory(t);
        IAEItemStack electrodes=ae(Items.BLAZE_ROD,5);
        check(n(inv.injectItems(electrodes,Actionable.SIMULATE,source))==2&&t.slots[0]==null&&electrodes.getStackSize()==5,"electrode simulation");
        check(n(inv.injectItems(electrodes,Actionable.MODULATE,source))==2&&n(t.slots[0])==1&&n(t.slots[1])==1&&n(t.slots[2])==1,"three electrode slots");
        check(inv.extractItems(ae(Items.BLAZE_ROD,3),Actionable.MODULATE,source)==null,"electrodes not extractable");
        t=new TileEntityMachineArcFurnaceLarge();inv=new ArcFurnaceLargeMEInventory(t);
        check(n(inv.injectItems(ae(ore,700),Actionable.SIMULATE,source))==60&&t.slots[5]==null,"solid output ratio capacity640");
        check(n(inv.injectItems(ae(ore,700),Actionable.MODULATE,source))==60&&n(t.slots[5])==32&&n(t.slots[24])==32&&t.slots[25]==null,"recipe insertion slots5-24 only");
        check(inv.getAvailableItems(items.createList()).isEmpty(),"recipe inputs hidden");
        check(inv.extractItems(ae(ore,1),Actionable.MODULATE,source)==null,"recipe input not extractable");
        t=new TileEntityMachineArcFurnaceLarge();t.liquidMode=true;inv=new ArcFurnaceLargeMEInventory(t);
        check(n(inv.injectItems(ae(ore,1300),Actionable.MODULATE,source))==20&&n(t.slots[24])==64,"liquid capacity1280");
        t=new TileEntityMachineArcFurnaceLarge();t.slots[5]=stack(ore,30);t.slots[6]=stack(Items.GOLD_INGOT,7);inv=new ArcFurnaceLargeMEInventory(t);
        check(inv.injectItems(ae(ore,5),Actionable.SIMULATE,source)==null&&n(t.slots[5])==30&&t.slots[7]==null,"partial-merge simulation");
        check(inv.injectItems(ae(ore,5),Actionable.MODULATE,source)==null&&n(t.slots[5])==32&&n(t.slots[6])==7&&n(t.slots[7])==3,"partial merge skips incompatible slot");
        t=new TileEntityMachineArcFurnaceLarge();t.maxInput=3;inv=new ArcFurnaceLargeMEInventory(t);
        check(n(inv.injectItems(ae(ore,61),Actionable.MODULATE,source))==1&&n(t.slots[5])==3,"machine maxInput cap");
        t=new TileEntityMachineArcFurnaceLarge();t.lid=0;t.slots[0]=stack(Items.STICK,2);t.slots[5]=stack(Items.STICK,4);
        t.slots[3]=stack(Items.STICK,10);t.slots[4]=stack(Items.STICK,10);t.slots[25]=stack(Items.STICK,10);inv=new ArcFurnaceLargeMEInventory(t);
        check(n(inv.getAvailableItems(items.createList()).getFirstItem())==6,"exact extraction visibility excludes3,4,25+");
        check(n(inv.extractItems(ae(Items.STICK,5),Actionable.SIMULATE,source))==5&&n(t.slots[0])==2&&n(t.slots[5])==4,"AFL extraction simulation");
        check(n(inv.extractItems(ae(Items.STICK,5),Actionable.MODULATE,source))==5&&t.slots[0]==null&&n(t.slots[5])==1,"closed lid custom extraction order");
        check(n(inv.extractItems(ae(Items.STICK,99),Actionable.MODULATE,source))==1,"AFL partial result");
        check(n(t.slots[3])==10&&n(t.slots[25])==10,"hidden slots untouched");
        check(n(inv.injectItems(ae(Items.STICK,5),Actionable.MODULATE,source))==5,"nonrecipe refused");
        t.slots[0]=ItemStack.EMPTY;t.slots[5]=ItemStack.EMPTY;check(inv.getAvailableItems(items.createList()).isEmpty(),"native EMPTY excluded");
    }
    static Capability<IStorageMonitorableAccessor> capability() throws Exception {
        appeng.capabilities.Capabilities.register();
        Field providers=CapabilityManager.class.getDeclaredField("providers");providers.setAccessible(true);
        @SuppressWarnings("unchecked") Capability<IStorageMonitorableAccessor> cap=(Capability<IStorageMonitorableAccessor>)((Map<?,?>)providers.get(CapabilityManager.INSTANCE)).get(IStorageMonitorableAccessor.class.getName());
        appeng.capabilities.Capabilities.STORAGE_MONITORABLE_ACCESSOR=cap;
        return cap;
    }
    static void capabilityCases() throws Exception {
        Capability<IStorageMonitorableAccessor> cap=capability();check(cap!=null,"native capability registered");
        TileEntityMassStorage t=msu(Items.IRON_INGOT);AE2StorageCapability p=new AE2StorageCapability(t);
        Object identity=p.getCapability(cap,null);
        for(EnumFacing face:EnumFacing.values())check(p.hasCapability(cap,face)&&p.getCapability(cap,face)==identity,"all faces and stable accessor");
        IStorageMonitorable a=((IStorageMonitorableAccessor)identity).getInventory(source);
        IStorageMonitorable b=((IStorageMonitorableAccessor)identity).getInventory(source);
        IMEMonitor<IAEItemStack> monitor=a.getInventory(items);
        check(monitor instanceof MEMonitorIInventory&&monitor!=b.getInventory(items),"native monitor per bus/source");
        check(a.getInventory(AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class))==null,"unsupported fluid channel null");
        check(monitor.isPrioritized(ae(Items.IRON_INGOT,1))&&!monitor.isPrioritized(ae(Items.GOLD_INGOT,1)),"MSU selected type prioritized");
        List<Long> changes=new ArrayList<>();List<IActionSource> sources=new ArrayList<>();Object token=new Object();
        monitor.addListener(new IMEMonitorHandlerReceiver<IAEItemStack>(){
            public boolean isValid(Object x){return x==token;}
            public void postChange(IBaseMonitor<IAEItemStack> m,Iterable<IAEItemStack> list,IActionSource s){for(IAEItemStack x:list)changes.add(x.getStackSize());sources.add(s);}
            public void onListUpdate(){}
        },token);
        ((MEMonitorIInventory)monitor).setActionSource(source);
        monitor.injectItems(ae(Items.IRON_INGOT,10),Actionable.MODULATE,source);
        check(t.stack==10&&!changes.isEmpty()&&sources.get(0)==source,"native monitor execute reports changes/source");
        int before=changes.size();t.stack+=5;((MEMonitorIInventory)monitor).onTick();
        check(changes.size()>before&&n(monitor.getStorageList().getFirstItem())==15,"native monitor polls external changes");
        before=changes.size();monitor.injectItems(ae(Items.IRON_INGOT,3),Actionable.SIMULATE,source);check(t.stack==15&&changes.size()==before,"monitor simulation no listener mutation");
        TileEntityProxyCombo proxy=new TileEntityProxyCombo();AE2StorageCapability pp=new AE2StorageCapability(proxy);
        check(!pp.hasCapability(cap,null),"unresolved proxy no storage");
        proxy.core=new TileEntity() {};check(!pp.hasCapability(cap,null),"unrelated proxy no storage");
        proxy.core=t;check(!pp.hasCapability(cap,null),"original handler excludes MSU proxies");
        proxy.inventory=false;proxy.core=new TileEntityMachineArcFurnaceLarge();Object first=pp.getCapability(cap,null);
        check(first!=null&&first==pp.getCapability(cap,EnumFacing.DOWN),"AFL proxy accepts any inventory flag and stable identity");
        AE2StorageCapability directAfl=new AE2StorageCapability(proxy.core);Object aflIdentity=directAfl.getCapability(cap,null);
        for(EnumFacing face:EnumFacing.values()) {
            check(pp.hasCapability(cap,face)&&pp.getCapability(cap,face)==first,"proxy all faces stable");
            check(directAfl.hasCapability(cap,face)&&directAfl.getCapability(cap,face)==aflIdentity,"direct AFL all faces stable");
        }
        proxy.core.invalidate();check(!pp.hasCapability(cap,null),"invalid core no access (not a chunk-unload simulation)");
        proxy.core=new TileEntityMachineArcFurnaceLarge();Object rebound=pp.getCapability(cap,null);check(rebound!=null&&rebound!=first,"new core rebinds capability identity");
        proxy.core=null;check(pp.getCapability(cap,null)==null,"detached core drops accessor");
        t.invalidate();check(!p.hasCapability(cap,null),"direct invalid tile drops accessor");
        AE2StorageCapability.AttachHandler listener=new AE2StorageCapability.AttachHandler();
        for(TileEntity tile:new TileEntity[]{msu(Items.IRON_INGOT),new TileEntityMachineArcFurnaceLarge(),new TileEntityProxyCombo(),new TileEntity() {}}){
            AttachCapabilitiesEvent<TileEntity> event=new AttachCapabilitiesEvent<>(TileEntity.class,tile);listener.attach(event);
            check(event.getCapabilities().size()==(tile instanceof TileEntityMassStorage || tile instanceof TileEntityMachineArcFurnaceLarge || tile instanceof TileEntityProxyCombo ? 1 : 0),"native attach event target scope");
        }
    }
    public static void main(String[] args) throws Exception {
        Field home=net.minecraftforge.fml.relauncher.FMLInjectionData.class.getDeclaredField("minecraftHome");home.setAccessible(true);home.set(null,new File("."));
        Bootstrap.register();
        appeng.core.AEConfig.init(new File("ae2-test-config"));
        items=AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
        msuCases();aflCases();capabilityCases();
        System.out.println("PASS AE2 native storage: "+checks+" checks; real AE2 item channel, monitors, adapters, Forge capabilities; exact NTM helper bodies.");
    }
}
