import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.CustomMachineConfigJSON;
import com.hbm.config.CustomMachineConfigJSON.MachineConfiguration;
import com.hbm.handler.jei.MachineRecipeView;
import com.hbm.handler.jei.NativeRecipeLookup;
import com.hbm.handler.nei.CustomMachineHandler;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.recipes.CustomMachineRecipes;
import com.hbm.inventory.recipes.CustomMachineRecipes.CustomMachineRecipe;
import com.hbm.items.machine.ItemFluidIcon;
import com.hbm.util.Tuple.Pair;

import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.gui.Focus;
import mezz.jei.gui.GuiHelper;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.ingredients.Ingredients;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.TextFormatting;

/** Complete CustomMachineHandler; NTM-only fixtures are identified in the runner. */
public final class JeiCustomNativeTest {
    private static int checks;
    private static IngredientRegistry registry;

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private static IFocus<ItemStack> focus(IFocus.Mode mode, ItemStack stack) {
        return new Focus<>(mode, stack);
    }

    private static MachineConfiguration configuration(String name, String key) {
        MachineConfiguration configuration = new MachineConfiguration();
        configuration.unlocalizedName = name;
        configuration.localizedName = "Visible " + name;
        configuration.recipeKey = key;
        return configuration;
    }

    @SuppressWarnings("unchecked")
    private static CustomMachineRecipe recipe(Item input, Item output) {
        CustomMachineRecipe recipe = new CustomMachineRecipe();
        recipe.inputItems = new AStack[] {new AStack(new ItemStack(input))};
        recipe.outputItems = new Pair[] {new Pair<>(new ItemStack(output), 1F)};
        return recipe;
    }

    private static JeiCoreNativeTest.GuiSpy layout(MachineRecipeView view) {
        Ingredients ingredients = new Ingredients();
        view.getIngredients(ingredients);
        JeiCoreNativeTest.GuiSpy gui = new JeiCoreNativeTest.GuiSpy();
        view.getCategory().setRecipe(gui.layout, view, ingredients);
        return gui;
    }

    private static ItemStack tagged(Item item) {
        ItemStack stack = new ItemStack(item, 3);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("provenance", "unchanged");
        NBTTagCompound display = new NBTTagCompound();
        NBTTagList lore = new NBTTagList();
        lore.appendTag(new NBTTagString("original lore"));
        display.setTag("Lore", lore);
        display.setString("Name", "original name");
        tag.setTag("display", display);
        stack.setTagCompound(tag);
        return stack;
    }

    private static void routingAndBounds() {
        MachineConfiguration first = configuration("alphaMachine", "alphaRecipes");
        MachineConfiguration second = configuration("betaMachine", "betaRecipes");
        MachineConfiguration shared = configuration("sharedMachine", "alphaRecipes");
        MachineConfiguration absent = configuration("absentMachine", "absentRecipes");
        CustomMachineConfigJSON.niceList.addAll(Arrays.asList(first, second, shared, absent));
        CustomMachineRecipe alpha = recipe(Items.APPLE, Items.DIAMOND);
        CustomMachineRecipe beta = recipe(Items.CARROT, Items.GOLD_INGOT);
        CustomMachineRecipes.recipes.put(first.recipeKey, new ArrayList<>(Arrays.asList(alpha)));
        CustomMachineRecipes.recipes.put(second.recipeKey, new ArrayList<>(Arrays.asList(beta)));
        CustomMachineHandler a = new CustomMachineHandler(first);
        CustomMachineHandler b = new CustomMachineHandler(second);
        CustomMachineHandler sameSource = new CustomMachineHandler(shared);
        CustomMachineHandler missing = new CustomMachineHandler(absent);
        a.initialize(new GuiHelper(registry));
        check(a.conf == first && a.getUid().equals("ntm_alphaMachine") && b.getUid().equals("ntm_betaMachine")
                && sameSource.getUid().equals("ntm_sharedMachine"), "distinct configured UIDs use unlocalizedName, not recipeKey");
        check(a.getTitle().equals("Visible alphaMachine"), "configured title retained");
        check(a.getBackground().getWidth() == 166 && a.getBackground().getHeight() == 84,
                "native background reserves all 84 pixels through pollution baseline y75");
        check(a.query(null).get(0).source == alpha && b.query(null).get(0).source == beta
                && sameSource.query(null).get(0).source == alpha, "recipe-key routing and original source identity");
        check(missing.query(null).isEmpty()
                && missing.query(focus(IFocus.Mode.INPUT, new ItemStack(Items.APPLE))).isEmpty()
                && missing.query(focus(IFocus.Mode.OUTPUT, new ItemStack(Items.DIAMOND))).isEmpty(),
                "missing configured recipe key is empty for all/input/output lookups");
        check(a.query(focus(IFocus.Mode.INPUT, new ItemStack(Items.APPLE))).size() == 1
                && b.query(focus(IFocus.Mode.INPUT, new ItemStack(Items.APPLE))).isEmpty(), "input filters confined to configured recipe set");
        check(a.query(focus(IFocus.Mode.OUTPUT, new ItemStack(Items.DIAMOND))).size() == 1
                && b.query(focus(IFocus.Mode.OUTPUT, new ItemStack(Items.DIAMOND))).isEmpty(), "output filters confined to configured recipe set");
        NativeRecipeLookup lookup = new NativeRecipeLookup(Arrays.asList(a, b, sameSource, missing), registry);
        check(lookup.getRecipeCategoryUids(focus(IFocus.Mode.OUTPUT, new ItemStack(Items.DIAMOND)))
                .equals(Arrays.asList("ntm_alphaMachine", "ntm_sharedMachine")), "native plugin discovers both distinct categories sharing one recipe set");
        check(lookup.getRecipeWrappers(b).get(0).source == beta, "native wrapper dispatch preserves per-configuration category identity");
        ItemStack machine0 = layout(a.query(null).get(0)).slots.get(2).variants.get(0);
        ItemStack machine1 = layout(b.query(null).get(0)).slots.get(2).variants.get(0);
        check(machine0.getItem() == Item.getItemFromBlock(ModBlocks.custom_machine) && machine0.getMetadata() == 100
                && machine1.getMetadata() == 101, "machine decoration metadata is 100 plus configuration index");
        CustomMachineConfigJSON.niceList.remove(first);
        CustomMachineConfigJSON.niceList.add(first);
        check(layout(a.query(null).get(0)).slots.get(2).variants.get(0).getMetadata() == 103,
                "fresh view reflects current configuration ordering");
        CustomMachineRecipes.recipes.get(first.recipeKey).add(recipe(Items.STICK, Items.EMERALD));
        check(a.query(null).size() == 2 && sameSource.query(null).size() == 2, "recipe source changes visible without reregistering categories");
        check(a.isNavigationArea(65, 23) && a.isNavigationArea(100, 40)
                && !a.isNavigationArea(64, 23) && !a.isNavigationArea(101, 23)
                && !a.isNavigationArea(65, 22) && !a.isNavigationArea(65, 41)
                && !a.isNavigationArea(147, 1), "original custom-machine navigation rectangle, not generic top-right arrow");
    }

    @SuppressWarnings("unchecked")
    private static void displayAndFluidSemantics() {
        MachineConfiguration configuration = configuration("boundedMachine", "boundedRecipes");
        CustomMachineConfigJSON.niceList.add(configuration);
        CustomMachineRecipe recipe = new CustomMachineRecipe();
        recipe.inputFluids = new FluidStack[4];
        recipe.outputFluids = new FluidStack[4];
        recipe.inputItems = new AStack[7];
        recipe.outputItems = new Pair[7];
        ItemStack input = tagged(Items.APPLE);
        ItemStack output = tagged(Items.DIAMOND);
        for (int i = 0; i < 4; i++) {
            recipe.inputFluids[i] = new FluidStack(i, 1000 + i, 10 + i);
            recipe.outputFluids[i] = new FluidStack(8 + i, 2000 + i, 20 + i);
        }
        for (int i = 0; i < 7; i++) {
            recipe.inputItems[i] = new AStack(input, input.copy());
            recipe.outputItems[i] = new Pair<>(output, i == 0 ? 0.125F : 1F);
        }
        recipe.inputItems[6] = new AStack(new ItemStack(Items.CARROT));
        recipe.outputItems[6] = new Pair<>(new ItemStack(Items.GOLD_INGOT), 0.5F);
        recipe.radiationAmount = 7.5F;
        recipe.pollutionAmount = 2.25F;
        recipe.pollutionType = "SOOT";
        recipe.flux = 73;
        recipe.heat = 91;
        CustomMachineRecipes.recipes.put(configuration.recipeKey, new ArrayList<>(Arrays.asList(recipe)));
        CustomMachineHandler handler = new CustomMachineHandler(configuration);
        MachineRecipeView view = handler.query(null).get(0);
        check(view.source == recipe && view.inputs.length == 9 && view.outputs.length == 9,
                "original display bound is three fluids plus six items on each side");
        check(handler.query(focus(IFocus.Mode.INPUT, input)).size() == 1
                && handler.query(focus(IFocus.Mode.OUTPUT, output)).size() == 1,
                "duplicate input alternatives/output item slots add recipe only once");
        check(handler.query(focus(IFocus.Mode.INPUT, new ItemStack(Items.CARROT))).size() == 1
                && handler.query(focus(IFocus.Mode.OUTPUT, new ItemStack(Items.GOLD_INGOT))).size() == 1,
                "original lookup still considers seventh items outside displayed six-item bound");
        check(handler.query(focus(IFocus.Mode.INPUT, new ItemStack(Items.DYE, 1, 3))).size() == 1
                && handler.query(focus(IFocus.Mode.OUTPUT, new ItemStack(Items.DYE, 1, 11))).size() == 1,
                "original lookup still considers fourth fluids outside displayed three-fluid bound");
        for (int i = 0; i < 3; i++) {
            ItemStack fluidIn = view.inputs[i][0];
            ItemStack fluidOut = view.outputs[i][0];
            check(fluidIn.getMetadata() == i && fluidIn.getTagCompound().getInteger("fill") == 1000 + i
                    && fluidIn.getTagCompound().getInteger("pressure") == 10 + i, "display input fluid identity/quantity/pressure " + i);
            check(fluidOut.getMetadata() == 8 + i && fluidOut.getTagCompound().getInteger("fill") == 2000 + i
                    && fluidOut.getTagCompound().getInteger("pressure") == 20 + i, "display output fluid identity/quantity/pressure " + i);
            ItemStack focus = ItemFluidIcon.make(new FluidStack(i, 99999, 987));
            check(handler.query(focus(IFocus.Mode.INPUT, focus)).size() == 1,
                    "fluid focus ignores quantity/pressure NBT " + i);
        }
        check(!CustomMachineHandler.compareFluidStacks(new ItemStack(Items.DYE, 1, 1), new ItemStack(Items.DYE, 1, 2))
                && !CustomMachineHandler.compareFluidStacks(new ItemStack(Items.APPLE), new ItemStack(Items.DYE)),
                "fluid comparison requires both item identity and metadata");
        check(handler.query(focus(IFocus.Mode.INPUT, new ItemStack(Items.DYE, 1, 15))).isEmpty()
                && handler.query(focus(IFocus.Mode.OUTPUT, new ItemStack(Items.STICK))).isEmpty(), "unmatched fluid/item focus omitted");
        JeiCoreNativeTest.GuiSpy gui = layout(view);
        check(gui.slots.size() == 19, "bounded layout contains eighteen ingredients plus one machine decoration");
        for (int i = 0; i < 9; i++) {
            int originalX = i < 3 ? 12 + i * 18 : 12 + ((i - 3) % 3) * 18;
            int originalY = i < 3 ? 6 : i < 6 ? 24 : 42;
            check(gui.slots.get(i).input && gui.slots.get(i).x == originalX - 1 && gui.slots.get(i).y == originalY - 1,
                    "original custom input grid position " + i);
            check(!gui.slots.get(9 + i).input && gui.slots.get(9 + i).x == originalX + 90 - 1
                    && gui.slots.get(9 + i).y == originalY - 1, "original custom output grid position " + i);
        }
        check(gui.slots.get(18).x == 74 && gui.slots.get(18).y == 41, "original machine display position 75,42");
        Ingredients ingredients = new Ingredients();
        view.getIngredients(ingredients);
        check(ingredients.getInputs(VanillaTypes.ITEM).size() == 9 && ingredients.getOutputs(VanillaTypes.ITEM).size() == 9,
                "machine display excluded from indexed input/output ingredients");
        check(output.getCount() == 3 && output.getTagCompound().getCompoundTag("display").getString("Name").equals("original name")
                && output.getTagCompound().getCompoundTag("display").getTagList("Lore", 8).getStringTagAt(0).equals("original lore"),
                "chance tooltip creation does not mutate original output NBT/count");
        check(view.outputs[3][0].getTagCompound().getString("provenance").equals("unchanged")
                && view.outputs[3][0].getTagCompound().getCompoundTag("display").getTagList("Lore", 8).getStringTagAt(0)
                .equals(TextFormatting.RESET + "" + TextFormatting.GRAY + TextFormatting.RED + "12.5%"),
                "actual production tooltip utility preserves unrelated NBT and expected formatted chance lore");
        check(view.outputs[4][0].getTagCompound().getCompoundTag("display").getString("Name").equals("original name"),
                "chance exactly one does not overwrite existing display NBT");
        input.setCount(57);
        input.getTagCompound().setString("provenance", "mutated");
        output.setCount(59);
        output.getTagCompound().setString("provenance", "mutated");
        check(view.inputs[3][0].getCount() == 3 && view.inputs[3][0].getTagCompound().getString("provenance").equals("unchanged")
                && view.outputs[3][0].getCount() == 3 && view.outputs[3][0].getTagCompound().getString("provenance").equals("unchanged"),
                "custom recipe views are deeply detached from mutable source stacks");
    }

    @SuppressWarnings("unchecked")
    private static void chanceRoundingAndEmptyRecipe() {
        MachineConfiguration configuration = configuration("chanceMachine", "chanceRecipes");
        CustomMachineConfigJSON.niceList.add(configuration);
        CustomMachineHandler handler = new CustomMachineHandler(configuration);
        float[] chances = {0F, 0.0009F, 0.001F, 0.12345F, 0.33333F, 1F, 1.25F, -0.1F,
                Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
        List<CustomMachineRecipe> recipes = new ArrayList<>();
        for (float chance : chances) {
            CustomMachineRecipe recipe = new CustomMachineRecipe();
            recipe.outputItems = new Pair[] {new Pair<>(tagged(Items.DIAMOND), chance)};
            recipes.add(recipe);
        }
        CustomMachineRecipes.recipes.put(configuration.recipeKey, recipes);
        List<MachineRecipeView> views = handler.query(null);
        check(views.size() == chances.length, "all chance recipes retained in source list order");
        for (int i = 0; i < chances.length; i++) {
            check(views.get(i).source == recipes.get(i), "source identity for chance recipe " + i);
            String lore = views.get(i).outputs[0][0].getTagCompound().getCompoundTag("display").getTagList("Lore", 8).getStringTagAt(0);
            String expected = chances[i] == 1F ? "original lore"
                    : TextFormatting.RESET + "" + TextFormatting.GRAY + TextFormatting.RED + (((int) (chances[i] * 1000)) / 10D) + "%";
            check(lore.equals(expected), "original float-to-integer chance rounding " + chances[i]);
        }
        CustomMachineRecipe empty = new CustomMachineRecipe();
        recipes.clear();
        recipes.add(empty);
        MachineRecipeView view = handler.query(null).get(0);
        check(view.inputs.length == 0 && view.outputs.length == 0 && layout(view).slots.size() == 1,
                "zero-input/output source keeps machine display without invented ingredients");
    }

    public static void main(String[] args) {
        Bootstrap.register();
        registry = JeiCoreNativeTest.registry();
        routingAndBounds();
        displayAndFluidSemantics();
        chanceRoundingAndEmptyRecipe();
        check(ItemStack.EMPTY.isEmpty() && ItemStack.EMPTY.getCount() == 0, "native EMPTY remains unchanged");
        System.out.println("PASS complete CustomMachineHandler: " + checks + " executable assertions; actual MC/Forge/JEI, scoped NTM fixtures and exact production tooltip method; no GL/gameplay/full-mod claim.");
    }
}
