import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.hbm.config.ClientConfig;
import com.hbm.handler.jei.MachineRecipeView;
import com.hbm.handler.jei.NativeRecipeLookup;
import com.hbm.handler.nei.AnnihilatorHandler;
import com.hbm.handler.nei.AssemblyMachineRecipeHandler;
import com.hbm.handler.nei.ChemicalPlantRecipeHandler;
import com.hbm.handler.nei.FuelPoolHandler;
import com.hbm.handler.nei.FusionRecipeHandler;
import com.hbm.handler.nei.NEIGenericRecipeHandler;
import com.hbm.handler.nei.NEIUniversalHandler;
import com.hbm.handler.nei.PUREXRecipeHandler;
import com.hbm.handler.nei.PrecAssRecipeHandler;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.recipes.AnnihilatorRecipes;
import com.hbm.inventory.recipes.AssemblyMachineRecipes;
import com.hbm.inventory.recipes.FuelPoolRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemRBMKRod;

import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.gui.Focus;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.ingredients.Ingredients;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

/** Whole production handlers. Only NTM registry/domain collaborators are fixtures. */
public final class JeiHandlersNativeTest {
    private static int checks;
    private static IngredientRegistry registry;

    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }

    private static IFocus<ItemStack> focus(IFocus.Mode mode, ItemStack stack) {
        return new Focus<>(mode, stack);
    }

    private static ItemStack tagged(Item item, String value) {
        ItemStack stack = new ItemStack(item);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("value", value);
        stack.setTagCompound(tag);
        return stack;
    }

    private static JeiCoreNativeTest.GuiSpy layout(MachineRecipeView view) {
        JeiCoreNativeTest.GuiSpy gui = new JeiCoreNativeTest.GuiSpy();
        Ingredients ingredients = new Ingredients();
        view.getIngredients(ingredients);
        view.getCategory().setRecipe(gui.layout, view, ingredients);
        return gui;
    }

    private static void universal() {
        HashMap<Object, Object> recipes = new HashMap<>();
        ItemStack[][] duplicateInputs = {{new ItemStack(Items.APPLE), new ItemStack(Items.APPLE)}, {new ItemStack(Items.STICK)}};
        ItemStack[][] duplicateOutputs = {{new ItemStack(Items.DIAMOND), new ItemStack(Items.DIAMOND)}};
        recipes.put(duplicateInputs, duplicateOutputs);
        recipes.put(new ItemStack(ModItems.item_secret), new ItemStack(Items.DIAMOND));
        recipes.put(new ItemStack(Items.APPLE), new ItemStack(ModItems.item_secret));
        NEIUniversalHandler handler = new NEIUniversalHandler("Fixture universal", new ItemStack(Items.IRON_INGOT), recipes) {
            @Override public String getUid() { return "test:universal"; }
        };
        check(handler.query(null).size() == 1, "universal hides secret marker on both input and output");
        check(handler.query(focus(IFocus.Mode.INPUT, new ItemStack(Items.APPLE))).size() == 1,
                "duplicate matching input alternatives yield one recipe");
        check(handler.query(focus(IFocus.Mode.OUTPUT, new ItemStack(Items.DIAMOND))).size() == 1,
                "duplicate matching output alternatives yield one recipe");
        check(handler.query(focus(IFocus.Mode.OUTPUT, new ItemStack(Items.GOLD_INGOT))).isEmpty(), "unknown focus excluded");
        MachineRecipeView view = handler.query(null).get(0);
        check(view.source == duplicateInputs && view.inputs.length == 2 && view.outputs.length == 1, "map source identity and slot matrices retained");
        JeiCoreNativeTest.GuiSpy gui = layout(view);
        check(gui.slots.get(0).x == 47 && gui.slots.get(1).x == 29 && gui.slots.get(2).x == 101,
                "universal two-input/output original coordinates preserved");
        check(gui.slots.get(3).x == 74 && gui.slots.get(3).y == 30
                && gui.slots.get(3).variants.get(0).getItem() == Items.IRON_INGOT, "machine decoration at original position");
        Ingredients ingredients = new Ingredients();
        view.getIngredients(ingredients);
        check(ingredients.getInputs(VanillaTypes.ITEM).size() == 2 && ingredients.getOutputs(VanillaTypes.ITEM).size() == 1,
                "machine decoration is not indexed as a universal recipe output");
        handler.machineOverrides = new HashMap<>();
        handler.machineOverrides.put(duplicateInputs, new ItemStack[] {new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.EMERALD)});
        gui = layout(handler.query(null).get(0));
        check(gui.slots.get(3).variants.size() == 2 && gui.slots.get(3).variants.get(0).getItem() == Items.GOLD_INGOT,
                "recipe-specific machine override cycles all alternatives");
        recipes.put(new ItemStack(Items.CARROT), new ItemStack(Items.POTATO));
        check(handler.query(null).size() == 2, "universal queries observe changes after construction");
        recipes.put(new ItemStack(Items.DYE, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(Items.BONE));
        check(handler.query(focus(IFocus.Mode.INPUT, new ItemStack(Items.DYE, 1, 7))).size() == 1,
                "official NEI 1.12 wildcard input semantics retained");
        for (int count = 0; count <= 12; count++) {
            check(NEIUniversalHandler.getInputCoords(count).length == count, "universal input coordinate count " + count);
            check(NEIUniversalHandler.getOutputCoords(count).length == count, "universal output coordinate count " + count);
        }
    }

    private static ItemStack rod(Item item, double core, double hull) {
        ItemStack stack = new ItemStack(item);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setDouble("core", core);
        tag.setDouble("hull", hull);
        stack.setTagCompound(tag);
        return stack;
    }

    private static void fuelPool() {
        Item rodItem = new ItemRBMKRod();
        FuelPoolRecipes.recipes.clear();
        FuelPoolRecipes.recipes.put(rod(rodItem, 80, 80), rod(rodItem, 0, 0));
        FuelPoolHandler handler = new FuelPoolHandler();
        check(handler.query(null).size() == 1, "fuel pool all-recipes ignores focus heat gate");
        double[] heat = {-1, 0, 49.999, 50, 50.001, 100, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
        for (double core : heat) {
            for (double hull : heat) {
                boolean inputAllowed = !(core < 50 && hull < 50);
                boolean outputAllowed = !(core >= 50 || hull >= 50);
                ItemStack selected = rod(rodItem, core, hull);
                check(!handler.query(focus(IFocus.Mode.INPUT, selected)).isEmpty() == inputAllowed,
                        "fuel pool input heat boundary " + core + "/" + hull);
                check(!handler.query(focus(IFocus.Mode.OUTPUT, selected)).isEmpty() == outputAllowed,
                        "fuel pool output heat boundary " + core + "/" + hull);
            }
        }
        NativeRecipeLookup lookup = new NativeRecipeLookup(Arrays.asList(handler), registry);
        check(lookup.getRecipeWrappers(handler, focus(IFocus.Mode.INPUT, rod(rodItem, 0, 0))).isEmpty(),
                "native registry preserves cold-rod usage exclusion");
        check(lookup.getRecipeWrappers(handler, focus(IFocus.Mode.OUTPUT, rod(rodItem, 50, 0))).isEmpty(),
                "native registry preserves hot-rod output exclusion");
        FuelPoolRecipes.recipes.put(new ItemStack(Items.APPLE), new ItemStack(Items.STICK));
        check(handler.query(focus(IFocus.Mode.INPUT, new ItemStack(Items.APPLE))).size() == 1,
                "non-rod inputs are not accidentally heat filtered");
    }

    private static void annihilator() {
        AnnihilatorRecipes.recipes.clear();
        ItemStack alpha = tagged(Items.DIAMOND, "alpha");
        ItemStack beta = tagged(Items.DIAMOND, "beta");
        AnnihilatorRecipes.recipes.put(new ItemStack(Items.APPLE), alpha);
        AnnihilatorRecipes.recipes.put(new ItemStack(Items.STICK), beta);
        AnnihilatorHandler handler = new AnnihilatorHandler();
        check(handler.query(null).size() == 2, "annihilator keeps both same-item NBT-distinct recipes");
        List<MachineRecipeView> matches = handler.query(focus(IFocus.Mode.OUTPUT, alpha));
        check(matches.size() == 1 && matches.get(0).inputs[0][0].getItem() == Items.APPLE,
                "annihilator output lookup respects full NBT");
        check(handler.query(focus(IFocus.Mode.OUTPUT, new ItemStack(Items.DIAMOND))).isEmpty(),
                "annihilator tagless focus does not match tagged output");
        check(handler.query(focus(IFocus.Mode.INPUT, tagged(Items.APPLE, "irrelevant"))).size() == 1,
                "annihilator input comparison remains crafting-style rather than output NBT rule");
        NativeRecipeLookup lookup = new NativeRecipeLookup(Arrays.asList(handler), registry);
        check(lookup.getRecipeWrappers(handler, focus(IFocus.Mode.OUTPUT, beta)).size() == 1,
                "native registry routes annihilator NBT-specific output query");
    }

    private static GenericRecipe recipe(Item input, Item output) {
        GenericRecipe result = new GenericRecipe();
        result.inputItem = new AStack[] {new AStack(new ItemStack(input))};
        result.outputItem = new GenericRecipes.IOutput[] {() -> new ItemStack[] {new ItemStack(output)}};
        return result;
    }

    private static void generic() {
        GenericRecipes<GenericRecipe> set = new GenericRecipes<>();
        NEIGenericRecipeHandler handler = new NEIGenericRecipeHandler("Fixture generic", set, new ItemStack(Items.IRON_INGOT)) {
            @Override public String getUid() { return "test:generic"; }
        };
        GenericRecipe ordinary = recipe(Items.APPLE, Items.DIAMOND);
        ordinary.inputItem = new AStack[] {new AStack(new ItemStack(Items.APPLE), new ItemStack(Items.APPLE), new ItemStack(Items.STICK))};
        ordinary.inputFluid = new FluidStack[] {new FluidStack(2, 1000, 3)};
        ordinary.outputFluid = new FluidStack[] {new FluidStack(5, 2000, 7)};
        ordinary.outputItem = new GenericRecipes.IOutput[] {() -> new ItemStack[] {tagged(Items.DIAMOND, "25%"), tagged(Items.DIAMOND, "75%")}};
        ordinary.pools = new String[] {"discover.alpha", "alt.beta"};
        GenericRecipe secretPool = recipe(Items.CARROT, Items.GOLD_INGOT);
        secretPool.pools = new String[] {"discover.visible", "secret.hidden"};
        GenericRecipe secretInput = recipe(Items.NETHER_STAR, Items.COAL);
        GenericRecipe secretOutput = recipe(Items.COAL, Items.NETHER_STAR);
        set.recipeOrderedList.addAll(Arrays.asList(ordinary, secretPool, secretInput, secretOutput));
        ModItems.excludeNEI.add(Items.NETHER_STAR);
        ClientConfig.NEI_HIDE_SECRETS.value = false;
        List<MachineRecipeView> all = handler.query(null);
        check(all.size() == 4 && all.get(0).source == ordinary && all.get(3).source == secretOutput,
                "generic source ordering and show-secrets setting preserved");
        ClientConfig.NEI_HIDE_SECRETS.value = true;
        all = handler.query(null);
        check(all.size() == 1 && all.get(0).source == ordinary, "generic hides secret pools plus excluded inputs and outputs");
        check(handler.query(focus(IFocus.Mode.INPUT, new ItemStack(Items.APPLE))).size() == 1,
                "generic duplicate input alternatives do not duplicate recipe");
        check(handler.query(focus(IFocus.Mode.OUTPUT, new ItemStack(Items.DIAMOND))).size() == 1,
                "generic chance alternatives retain crafting-style output focus semantics");
        check(handler.query(focus(IFocus.Mode.INPUT, new ItemStack(Items.NETHER_STAR))).isEmpty()
                && handler.query(focus(IFocus.Mode.OUTPUT, new ItemStack(Items.NETHER_STAR))).isEmpty(),
                "generic focused lookups apply same secret exclusion");
        ItemStack fluidInput = new ItemStack(ModItems.fluid_icon, 1, 2);
        ItemStack fluidOutput = new ItemStack(ModItems.fluid_icon, 1, 5);
        check(handler.query(focus(IFocus.Mode.INPUT, fluidInput)).size() == 1
                && handler.query(focus(IFocus.Mode.OUTPUT, fluidOutput)).size() == 1,
                "generic fluid focus matches item/meta without quantity/pressure NBT");
        check(handler.query(focus(IFocus.Mode.INPUT, fluidOutput)).isEmpty(), "generic fluid input/output identities distinguished");
        MachineRecipeView view = all.get(0);
        check(view.inputs.length == 2 && view.outputs.length == 2 && view.inputs[1][0].getTagCompound().getInteger("fill") == 1000
                && view.inputs[1][0].getTagCompound().getInteger("pressure") == 3
                && view.outputs[1][0].getTagCompound().getInteger("fill") == 2000
                && view.outputs[1][0].getTagCompound().getInteger("pressure") == 7,
                "generic fluid display carries original quantity and pressure");
        check(view.outputs[0].length == 2 && view.outputs[0][0].getTagCompound().getString("value").equals("25%"),
                "generic chance-output display NBT/alternatives retained");
        JeiCoreNativeTest.GuiSpy gui = layout(view);
        check(gui.slots.size() == 6 && gui.slots.get(0).x == 29 && gui.slots.get(1).x == 47,
                "generic item then fluid slots retain original layout");
        check(gui.slots.get(4).x == 74 && gui.slots.get(4).y == 9 && gui.slots.get(4).variants.size() == 2
                && gui.slots.get(4).variants.get(1).getTagCompound().getString("pool").equals("alt.beta"),
                "blueprints cycle pools as independent decoration");
        check(gui.slots.get(5).x == 74 && gui.slots.get(5).y == 37, "pooled recipe machine position preserved");
        Ingredients ingredients = new Ingredients();
        view.getIngredients(ingredients);
        check(ingredients.getInputs(VanillaTypes.ITEM).size() == 2 && ingredients.getOutputs(VanillaTypes.ITEM).size() == 2,
                "generic blueprints and machines excluded from recipe indexing");
        check(handler.query(focus(IFocus.Mode.INPUT, gui.slots.get(4).variants.get(0))).isEmpty(),
                "blueprint decoration is not silently made a consumable recipe input");
        ClientConfig.NEI_HIDE_SECRETS.value = false;
        check(handler.query(null).size() == 4, "generic visibility responds dynamically after registration");
        for (int count = 0; count <= 20; count++) {
            check(handler.getInputSlotPositions(count).length == count, "generic input coordinate count " + count);
            check(handler.getOutputSlotPositions(count).length == count, "generic output coordinate count " + count);
        }
    }

    private static void genericSubclasses() {
        AssemblyMachineRecipes.INSTANCE.recipeOrderedList.clear();
        GenericRecipe large = recipe(Items.APPLE, Items.DIAMOND);
        large.inputItem = new AStack[10];
        for (int i = 0; i < large.inputItem.length; i++) large.inputItem[i] = new AStack(new ItemStack(Items.APPLE));
        AssemblyMachineRecipes.INSTANCE.recipeOrderedList.add(large);
        AssemblyMachineRecipeHandler assembly = new AssemblyMachineRecipeHandler();
        MachineRecipeView view = assembly.query(null).get(0);
        JeiCoreNativeTest.GuiSpy gui = layout(view);
        check(assembly.getUid().equals("ntmAssemblyMachine") && gui.slots.get(0).x == 11 && gui.slots.get(10).x == 119
                && gui.slots.get(11).x == 92, "assembly large-recipe input/output/machine offsets preserved");
        large.inputItem = Arrays.copyOf(large.inputItem, 9);
        view = assembly.query(null).get(0);
        gui = layout(view);
        check(gui.slots.get(9).x == 101 && gui.slots.get(10).x == 74, "assembly nine-input boundary does not shift outputs/machine");
        check(new PrecAssRecipeHandler().getUid().equals("ntmPrecAss"), "precision assembly UID");
        check(new ChemicalPlantRecipeHandler().getUid().equals("ntmChemicalPlant")
                && new ChemicalPlantRecipeHandler().getMachines().length == 2, "chemical plant/factory category and catalyst variants");
        check(new PUREXRecipeHandler().getUid().equals("ntmPUREX"), "PUREX UID");
        check(new FusionRecipeHandler().getUid().equals("ntmFusion"), "fusion UID");
    }

    public static void main(String[] args) {
        Bootstrap.register();
        registry = JeiCoreNativeTest.registry();
        universal();
        fuelPool();
        annihilator();
        generic();
        genericSubclasses();
        check(ItemStack.EMPTY.isEmpty() && ItemStack.EMPTY.getCount() == 0, "native EMPTY remains unchanged");
        System.out.println("PASS migrated production JEI handlers: " + checks + " assertions. Real MC/JEI dependencies; scoped NTM-only registry/domain fixtures; no gameplay/full-mod claim.");
    }
}
