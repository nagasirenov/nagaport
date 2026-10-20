import java.util.*;
import com.hbm.handler.nei.SatelliteHandler;
import com.hbm.handler.jei.MachineRecipeView;
import com.hbm.itempool.ItemPool;
import com.hbm.items.ModItems;
import com.hbm.lib.WeightedRandomChestContent;
import com.hbm.saveddata.satellites.SatelliteMiner;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.gui.Focus;
import mezz.jei.ingredients.Ingredients;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

class JeiSatelliteNativeTest {
    static int checks;
    static void check(boolean value,String label) { checks++; if(!value) throw new AssertionError(label); }
    static Focus<ItemStack> focus(IFocus.Mode mode,ItemStack stack) { return new Focus<>(mode,stack); }
    static WeightedRandomChestContent entry(ItemStack stack,int weight) { return new WeightedRandomChestContent(stack,1,3,weight); }
    public static void main(String[] args) {
        Bootstrap.register(); JeiCoreNativeTest.registry();
        SatelliteHandler handler=new SatelliteHandler();
        ItemStack apple=new ItemStack(Items.APPLE,3);
        NBTTagCompound tag=new NBTTagCompound(); tag.setString("cargo","kept"); apple.setTagCompound(tag);
        WeightedRandomChestContent[] miner={entry(apple,1),entry(new ItemStack(Items.DIAMOND),2),entry(apple,1)};
        WeightedRandomChestContent[] lunar={entry(new ItemStack(Items.GOLD_INGOT),1)};
        ItemPool.pools.put("miner",miner); ItemPool.pools.put("lunar",lunar);
        SatelliteMiner.cargo.put(ModItems.sat_miner,"miner"); SatelliteMiner.cargo.put(ModItems.sat_lunar_miner,"lunar");
        check(handler.query(null).size()==2,"All recipes");
        MachineRecipeView v=handler.query(null).get(0);
        check(v.source==ModItems.sat_miner,"Stable source item identity");
        check(v.outputs.length==3,"Duplicates retained");
        check(v.outputs[0][0].getCount()==3,"Template count unchanged by loot min/max");
        check(v.outputs[0][0].getTagCompound().getString("cargo").equals("kept"),"Existing NBT retained");
        check(v.outputs[0][0].getTagCompound().getCompoundTag("display").getTagList("Lore",8).getStringTagAt(0).endsWith("25.0%"),"Weighted lore 25 percent");
        check(v.outputs[1][0].getTagCompound().getCompoundTag("display").getTagList("Lore",8).getStringTagAt(0).endsWith("50.0%"),"Weighted lore 50 percent");
        check(!apple.getTagCompound().hasKey("display"),"Source NBT not mutated");
        check(handler.query(focus(IFocus.Mode.OUTPUT,new ItemStack(Items.APPLE))).size()==1,"Duplicate matches produce one recipe");
        check(handler.query(focus(IFocus.Mode.OUTPUT,new ItemStack(Items.STICK))).isEmpty(),"Unknown output excluded");
        ItemStack satellite=new ItemStack(ModItems.sat_miner,7,19); satellite.setTagCompound(tag.copy());
        check(handler.query(focus(IFocus.Mode.INPUT,satellite)).size()==1,"Input ignores metadata/count/NBT");
        check(handler.query(focus(IFocus.Mode.INPUT,new ItemStack(Items.IRON_INGOT))).isEmpty(),"Dock not a usage ingredient");
        SatelliteMiner.cargo.remove(ModItems.sat_miner);
        check(handler.query(null).size()==1,"Null cargo mapping omitted");
        check(handler.query(focus(IFocus.Mode.INPUT,satellite)).size()==1,"Input uses direct pool despite absent cargo mapping");
        SatelliteMiner.cargo.put(ModItems.sat_miner,"lunar");
        check(handler.query(focus(IFocus.Mode.OUTPUT,new ItemStack(Items.GOLD_INGOT))).size()==2,"Dynamic cargo mapping and independent satellite recipes");
        SatelliteMiner.cargo.put(ModItems.sat_miner,"miner");
        for(int size : new int[]{0,1,17,18,19,35,36,37,55}) {
            WeightedRandomChestContent[] pool=new WeightedRandomChestContent[size];
            for(int i=0;i<size;i++) { ItemStack s=new ItemStack(Items.APPLE); NBTTagCompound t=new NBTTagCompound(); t.setInteger("entry",i); s.setTagCompound(t); pool[i]=entry(s,i+1); }
            ItemPool.pools.put("miner",pool); v=handler.query(null).get(0);
            check(v.outputs.length==Math.min(size,18),"Visible slots "+size);
            int count=0;
            for(int i=0;i<v.outputs.length;i++) for(int j=0;j<v.outputs[i].length;j++) { check(v.outputs[i][j].getTagCompound().getInteger("entry")==i+j*18,"Overflow sequence "+size); count++; }
            check(count==size,"All entries preserved "+size);
            Ingredients ingredients=new Ingredients(); v.getIngredients(ingredients);
            check(ingredients.getInputs(VanillaTypes.ITEM).size()==1 && ingredients.getOutputs(VanillaTypes.ITEM).size()==Math.min(size,18),"Dock excluded from logical ingredients");
            JeiCoreNativeTest.GuiSpy gui=new JeiCoreNativeTest.GuiSpy(); handler.setRecipe(gui.layout,v,ingredients);
            check(gui.slots.get(0).x==11 && gui.slots.get(0).y==23,"Input padding coordinates");
            int dock=Math.min(size,18)+1;
            check(gui.slots.get(dock).x==29 && gui.slots.get(dock).y==30,"Dock padding coordinates");
            for(int i=0;i<Math.min(size,18);i++) check(gui.slots.get(i+1).x==47+18*(i%6) && gui.slots.get(i+1).y==5+18*(i/6),"Output coordinates");
        }
        ItemPool.pools.put("miner",new WeightedRandomChestContent[]{entry(new ItemStack(Items.APPLE),Integer.MAX_VALUE),entry(new ItemStack(Items.DIAMOND),1)});
        v=handler.query(null).get(0);
        float chance=100F*Integer.MAX_VALUE/(Integer.MAX_VALUE+1);
        String expected=((int)(chance*10F)/10F)+"%";
        check(v.outputs[0][0].getTagCompound().getCompoundTag("display").getTagList("Lore",8).getStringTagAt(0).endsWith(expected),"Original integer sum overflow arithmetic retained");
        check(!handler.handleClick(v,null,147,1,0) && !handler.handleClick(v,null,147,1,1),"No phantom category hotspot");
        check(ItemStack.EMPTY.isEmpty() && !ItemStack.EMPTY.hasTagCompound(),"Native empty remains pristine");
        System.out.println("PASS Satellite whole-class native probe: "+checks+" assertions; actual MC/JEI and weighted loot; NTM registry fixtures/exact extracted tooltip method. No full-mod/gameplay claim.");
    }
}
