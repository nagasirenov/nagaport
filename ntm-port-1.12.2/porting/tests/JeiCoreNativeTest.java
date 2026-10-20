import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.hbm.handler.jei.MachineRecipeCategory;
import com.hbm.handler.jei.MachineRecipeView;
import com.hbm.handler.jei.NativeRecipeLookup;

import mezz.jei.Internal;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IRecipesGui;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.gui.Focus;
import mezz.jei.gui.GuiHelper;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.ingredients.Ingredients;
import mezz.jei.plugins.vanilla.ingredients.ItemStackHelper;
import mezz.jei.plugins.vanilla.ingredients.ItemStackRenderer;
import mezz.jei.runtime.SubtypeRegistry;
import mezz.jei.startup.StackHelper;
import mezz.jei.util.IngredientSet;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/** Complete production core, native MC/JEI objects and observation-only GUI proxies. */
public final class JeiCoreNativeTest {
    private static int checks;

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private static void expect(Class<? extends Throwable> failure, Runnable task, String message) {
        checks++;
        try {
            task.run();
        } catch (Throwable actual) {
            if (failure.isInstance(actual)) return;
            throw new AssertionError(message, actual);
        }
        throw new AssertionError(message);
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler);
    }

    private static ItemStack tagged(int count, String tag) {
        ItemStack stack = new ItemStack(Items.APPLE, count);
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("recipe", tag);
        stack.setTagCompound(compound);
        return stack;
    }

    private static class Category extends MachineRecipeCategory {
        private final String uid;
        private final List<MachineRecipeView> recipes = new ArrayList<>();
        private ItemStack[] machines = new ItemStack[0];
        private IFocus<ItemStack> lastFocus;
        private int queryCount;
        private int drawCount;
        private MachineRecipeView drawn;
        private int drawnX;
        private int drawnY;

        private Category(String uid) { this.uid = uid; }
        @Override public String getUid() { return uid; }
        @Override public String getTitle() { return "Native core fixture"; }
        @Override public ItemStack[] getMachines() { return machines; }
        @Override public List<MachineRecipeView> query(IFocus<ItemStack> focus) {
            queryCount++;
            lastFocus = focus;
            if (focus == null) return recipes;
            // Deliberately stricter than JEI's default ingredient index: prove dispatch
            // preserves custom NBT decisions rather than independently matching stacks.
            if (focus.getValue().hasTagCompound()
                    && "accepted".equals(focus.getValue().getTagCompound().getString("recipe"))) {
                return recipes;
            }
            return Collections.emptyList();
        }
        @Override public void drawRecipe(MachineRecipeView view, net.minecraft.client.Minecraft minecraft, int x, int y) {
            drawCount++;
            drawn = view;
            drawnX = x;
            drawnY = y;
        }
    }

    static final class Slot {
        boolean input;
        int x;
        int y;
        List<ItemStack> variants;
        ItemStack displayed;
    }

    static final class GuiSpy {
        final Map<Integer, Slot> slots = new LinkedHashMap<>();
        private final Map<Integer, IGuiIngredient<ItemStack>> ingredients = new HashMap<>();
        private final IGuiItemStackGroup group = proxy(IGuiItemStackGroup.class, (self, method, args) -> {
            String name = method.getName();
            if (name.equals("init")) {
                check(args.length == 4, "uses native item slot overload");
                int index = (Integer) args[0];
                Slot slot = new Slot();
                slot.input = (Boolean) args[1];
                slot.x = (Integer) args[2];
                slot.y = (Integer) args[3];
                slots.put(index, slot);
                ingredients.put(index, proxy(IGuiIngredient.class, (ingredient, operation, values) -> {
                    switch (operation.getName()) {
                        case "getDisplayedIngredient": return slot.displayed;
                        case "getAllIngredients": return slot.variants;
                        case "isInput": return slot.input;
                        default: throw new AssertionError("Unexpected ingredient operation: " + operation);
                    }
                }));
                return null;
            }
            if (name.equals("set") && args.length == 2) {
                Slot slot = slots.get((Integer) args[0]);
                @SuppressWarnings("unchecked") List<ItemStack> variants = (List<ItemStack>) args[1];
                slot.variants = variants;
                slot.displayed = variants.isEmpty() ? null : variants.get(0);
                return null;
            }
            if (name.equals("getGuiIngredients")) return ingredients;
            throw new AssertionError("Unexpected GUI operation: " + method);
        });
        final IRecipeLayout layout = proxy(IRecipeLayout.class, (self, method, args) -> {
            if (method.getName().equals("getItemStacks")) return group;
            throw new AssertionError("Unexpected layout operation: " + method);
        });
    }

    static IngredientRegistry registry() {
        StackHelper stacks = new StackHelper(new SubtypeRegistry());
        ItemStackHelper items = new ItemStackHelper(stacks);
        Internal.setStackHelper(stacks);
        Map<IIngredientType, IngredientSet> ingredientSets = new HashMap<>();
        ingredientSets.put(VanillaTypes.ITEM, IngredientSet.create(VanillaTypes.ITEM, items));
        // Only item helper/renderer services are used here. No ingredient filtering,
        // game client, mod discovery or registered-content scans are involved.
        IngredientRegistry registry = new IngredientRegistry(null, null, ingredientSets,
                ImmutableMap.<IIngredientType, IIngredientHelper>of(VanillaTypes.ITEM, items),
                ImmutableMap.<IIngredientType, IIngredientRenderer>of(VanillaTypes.ITEM, new ItemStackRenderer()));
        Internal.setStackHelper(stacks);
        Internal.setIngredientRegistry(registry);
        return registry;
    }

    private static void ingredientsAndLayout(IngredientRegistry registry) {
        Category category = new Category("test:layout");
        expect(IllegalStateException.class, category::getBackground, "background requires category initialization");
        category.initialize(new GuiHelper(registry));
        check(category.getBackground().getWidth() == 166 && category.getBackground().getHeight() == 65,
                "native blank background dimensions");
        check(category.getModName().equals("hbm") && category.getMachines().length == 0, "category defaults");

        ItemStack first = tagged(3, "original");
        ItemStack second = new ItemStack(Items.STICK, 4);
        ItemStack output = new ItemStack(Items.DIAMOND, 2);
        ItemStack decoration = new ItemStack(Items.PAPER, 1);
        ItemStack[][] inputs = {{first, second}, {ItemStack.EMPTY, null}};
        ItemStack[][] outputs = {{output}};
        Object source = new Object();
        MachineRecipeView view = new MachineRecipeView(category, source, inputs, outputs)
                .addInputSlot(1, 12, 24).addOutputSlot(0, 102, 24)
                .addDecoration(new ItemStack[] {decoration}, 75, 10).addInputSlot(0, 48, 24);
        check(view.source == source && view.getCategory() == category, "source/category identity retained");
        check(view.inputs.length == 2 && view.inputs[1].length == 0, "empty/null variants sanitized without removing logical slots");
        first.setCount(19);
        first.getTagCompound().setString("recipe", "mutated source");
        inputs[0] = new ItemStack[] {new ItemStack(Items.COAL)};
        output.setCount(9);
        decoration.setCount(8);
        check(view.inputs[0][0].getCount() == 3
                && view.inputs[0][0].getTagCompound().getString("recipe").equals("original"), "constructor deep copies source NBT/count");
        view.inputs[0][0].setCount(31);
        view.inputs[0][0].getTagCompound().setString("recipe", "mutated public snapshot");
        view.outputs[0][0].setCount(30);

        Ingredients ingredients = new Ingredients();
        view.getIngredients(ingredients);
        List<List<ItemStack>> nativeInputs = ingredients.getInputs(VanillaTypes.ITEM);
        List<List<ItemStack>> nativeOutputs = ingredients.getOutputs(VanillaTypes.ITEM);
        check(nativeInputs.size() == 2 && nativeOutputs.size() == 1, "decorations absent from native ingredient indexing");
        check(nativeInputs.get(0).size() == 2 && nativeInputs.get(1).isEmpty(), "native alternatives/empty slots preserved");
        check(nativeInputs.get(0).get(0).getCount() == 3 && nativeOutputs.get(0).get(0).getCount() == 2,
                "public snapshots do not mutate stored ingredients");
        check(nativeInputs.get(0).get(0).getTagCompound().getString("recipe").equals("original"), "native NBT snapshot intact");
        nativeInputs.get(0).get(0).setCount(63);
        view.getIngredients(ingredients);
        check(ingredients.getInputs(VanillaTypes.ITEM).get(0).get(0).getCount() == 3, "repeated ingredient exports are independent copies");
        check(view.displayedInput(0).getCount() == 3 && view.displayedInput(1).isEmpty(), "pre-layout display fallback");

        GuiSpy gui = new GuiSpy();
        category.setRecipe(gui.layout, view, ingredients);
        check(gui.slots.size() == 4, "all native visual slots initialized including decoration");
        check(gui.slots.get(0).input && !gui.slots.get(1).input && !gui.slots.get(2).input && gui.slots.get(3).input,
                "inputs distinct from outputs/decorations for native transfer");
        check(gui.slots.get(0).x == 11 && gui.slots.get(0).y == 23
                && gui.slots.get(1).x == 101 && gui.slots.get(2).x == 74 && gui.slots.get(2).y == 9,
                "visible item coordinates corrected for JEI one-pixel padding");
        check(gui.slots.get(2).variants.get(0).getCount() == 1, "decoration source defensively copied");
        check(gui.slots.get(3).variants.get(0).getCount() == 3, "layout uses internal ingredients, not public snapshot");
        gui.slots.get(3).displayed = gui.slots.get(3).variants.get(1);
        check(view.displayedInput(0).getItem() == Items.STICK, "display accessor follows native JEI cycle/focus result");
        ItemStack displayed = view.displayedInput(0);
        displayed.setCount(60);
        check(gui.slots.get(3).displayed.getCount() == 4, "display accessor does not expose mutable GUI stack");
        check(view.displayedOutput(0).getCount() == 2 && view.displayedInput(1).isEmpty(), "logical indices independent of visual order");
        gui.slots.get(1).displayed = null;
        check(view.displayedOutput(0).isEmpty(), "null native displayed ingredient becomes EMPTY");
        expect(IndexOutOfBoundsException.class, () -> view.addInputSlot(2, 0, 0), "reject invalid logical input index");
        expect(IndexOutOfBoundsException.class, () -> view.addOutputSlot(-1, 0, 0), "reject invalid logical output index");
        expect(IndexOutOfBoundsException.class, () -> view.displayedOutput(1), "reject invalid displayed output index");
        expect(IllegalArgumentException.class, () -> new Category("test:other").setRecipe(gui.layout, view, ingredients),
                "reject layouts using a foreign category wrapper");
        view.drawInfo(null, 166, 65, 8, 9);
        check(category.drawCount == 1 && category.drawn == view && category.drawnX == 8 && category.drawnY == 9,
                "drawInfo delegates same wrapper and relative mouse coordinates");
        MachineRecipeView empty = new MachineRecipeView(category, null, null, null).addDecoration(null, 1, 1);
        empty.getIngredients(ingredients);
        check(ingredients.getInputs(VanillaTypes.ITEM).isEmpty() && ingredients.getOutputs(VanillaTypes.ITEM).isEmpty(),
                "null ingredient matrices and decoration variants supported");
    }

    private static void lookup(IngredientRegistry registry) {
        Category category = new Category("test:lookup");
        MachineRecipeView recipe = new MachineRecipeView(category, "first", new ItemStack[0][], new ItemStack[0][]);
        category.recipes.add(recipe);
        category.machines = new ItemStack[] {null, ItemStack.EMPTY, new ItemStack(Items.IRON_INGOT)};
        Category empty = new Category("test:empty");
        NativeRecipeLookup lookup = new NativeRecipeLookup(Arrays.asList(category, empty), registry);
        check(lookup.getRecipeWrappers(category).equals(Collections.singletonList(recipe)) && category.lastFocus == null,
                "all-recipe query dispatch");
        check(lookup.getRecipeCategoryUids(null).equals(Collections.singletonList(category.getUid())), "empty categories omitted from discovery");
        IFocus<ItemStack> input = new Focus<>(IFocus.Mode.INPUT, tagged(1, "accepted"));
        check(lookup.getRecipeWrappers(category, input).size() == 1 && category.lastFocus == input, "input focus passed through unchanged");
        check(lookup.getRecipeCategoryUids(input).equals(Collections.singletonList(category.getUid())), "category discovery honors custom query");
        IFocus<ItemStack> rejected = new Focus<>(IFocus.Mode.INPUT, tagged(1, "rejected"));
        check(lookup.getRecipeWrappers(category, rejected).isEmpty(), "custom NBT rejection preserved despite same item");
        IFocus<ItemStack> output = new Focus<>(IFocus.Mode.OUTPUT, tagged(1, "accepted"));
        check(lookup.getRecipeWrappers(category, output).size() == 1 && category.lastFocus == output, "output focus passed through unchanged");
        IFocus<ItemStack> catalyst = new Focus<>(IFocus.Mode.INPUT, new ItemStack(Items.IRON_INGOT, 25));
        check(lookup.getRecipeWrappers(category, catalyst).size() == 1 && category.lastFocus == null, "native catalyst match requests all recipes ignoring count");
        check(lookup.getRecipeCategoryUids(catalyst).equals(Collections.singletonList(category.getUid())), "native catalyst category discovery");
        IFocus<ItemStack> machineOutput = new Focus<>(IFocus.Mode.OUTPUT, catalyst.getValue());
        check(lookup.getRecipeWrappers(category, machineOutput).isEmpty() && category.lastFocus == machineOutput,
                "output focus is not incorrectly broadened as catalyst");
        int queries = category.queryCount;
        IFocus<String> unsupported = proxy(IFocus.class, (self, method, args) ->
                method.getName().equals("getMode") ? IFocus.Mode.INPUT : "not an item");
        check(lookup.getRecipeWrappers(category, unsupported).isEmpty() && lookup.getRecipeCategoryUids(unsupported).isEmpty()
                && category.queryCount == queries, "unsupported focus types never dispatched");
        IFocus<ItemStack> emptyFocus = proxy(IFocus.class, (self, method, args) ->
                method.getName().equals("getMode") ? IFocus.Mode.INPUT : ItemStack.EMPTY);
        check(lookup.getRecipeWrappers(category, emptyFocus).isEmpty() && category.queryCount == queries,
                "empty stack focus not mistaken for all recipes");
        Category impostor = new Category(category.getUid());
        check(lookup.getRecipeWrappers(impostor).isEmpty() && lookup.getRecipeWrappers(impostor, input).isEmpty(),
                "unregistered category identity not accepted merely by matching UID");
        category.recipes.add(new MachineRecipeView(category, "later", null, null));
        check(lookup.getRecipeWrappers(category).size() == 2, "queries see source changes after lookup registration");
        expect(IllegalArgumentException.class, () -> new NativeRecipeLookup(Arrays.asList(category, impostor), registry),
                "duplicate category UIDs rejected");
    }

    private static void loadNativeTranslations() throws Exception {
        net.minecraft.client.resources.Locale locale = new net.minecraft.client.resources.Locale();
        java.lang.reflect.Method load = locale.getClass().getDeclaredMethod("loadLocaleData", java.io.InputStream.class);
        load.setAccessible(true);
        try (java.io.InputStream language = Focus.class.getResourceAsStream("/assets/jei/lang/en_us.lang")) {
            check(language != null, "pinned JEI localization resource exists");
            load.invoke(locale, language);
        }
        java.lang.reflect.Method set = net.minecraft.client.resources.I18n.class.getDeclaredMethod("setLocale", locale.getClass());
        set.setAccessible(true);
        set.invoke(null, locale);
    }

    private static void navigation() {
        Category category = new Category("test:navigation");
        MachineRecipeView view = new MachineRecipeView(category, null, null, null);
        List<List<String>> navigations = new ArrayList<>();
        IRecipesGui gui = proxy(IRecipesGui.class, (self, method, args) -> {
            if (!method.getName().equals("showCategories")) throw new AssertionError("Unexpected navigation " + method);
            @SuppressWarnings("unchecked") List<String> uids = (List<String>) args[0];
            navigations.add(new ArrayList<>(uids));
            return null;
        });
        IJeiRuntime runtime = proxy(IJeiRuntime.class, (self, method, args) -> {
            if (method.getName().equals("getRecipesGui")) return gui;
            throw new AssertionError("Unexpected runtime operation " + method);
        });
        MachineRecipeCategory.setRuntime(null);
        check(!view.handleClick(null, 147, 1, 0), "navigation unavailable before runtime");
        MachineRecipeCategory.setRuntime(runtime);
        check(view.getTooltipStrings(147, 1).equals(Collections.singletonList("Show All Recipes")),
                "wrapper uses verified native JEI localization for category arrow tooltip");
        check(view.getTooltipStrings(146, 1).isEmpty() && category.getTooltipStrings(147, 1).isEmpty(),
                "tooltip is bounded and not duplicated through category callback");
        check(view.handleClick(null, 147, 1, 0), "left click at arrow top-left opens category");
        check(view.handleClick(null, 164, 18, 1), "right click at arrow bottom-right opens category");
        check(!view.handleClick(null, 146, 1, 0) && !view.handleClick(null, 165, 1, 0)
                && !view.handleClick(null, 147, 0, 0) && !view.handleClick(null, 147, 19, 0), "arrow bounds half-open and precise");
        check(!view.handleClick(null, 150, 10, 2), "other buttons not consumed");
        check(navigations.size() == 2 && navigations.get(0).equals(Collections.singletonList(category.getUid()))
                && navigations.get(1).equals(navigations.get(0)), "both supported buttons request complete category");
        Category custom = new Category("test:custom-navigation") {
            @Override public boolean isNavigationArea(int x, int y) { return x >= 20 && x < 30 && y >= 30 && y < 40; }
        };
        MachineRecipeView customView = new MachineRecipeView(custom, null, null, null);
        check(!customView.handleClick(null, 147, 1, 0) && customView.handleClick(null, 25, 35, 1),
                "custom navigation bounds control both default rejection and alternate right-click area");
        check(customView.getTooltipStrings(147, 1).isEmpty() && customView.getTooltipStrings(25, 35).size() == 1,
                "tooltip follows same custom navigation area as clicks");
        Category disabled = new Category("test:disabled-navigation") {
            @Override public boolean isNavigationArea(int x, int y) { return false; }
        };
        MachineRecipeView disabledView = new MachineRecipeView(disabled, null, null, null);
        check(!disabledView.handleClick(null, 147, 1, 0) && disabledView.getTooltipStrings(147, 1).isEmpty(),
                "categories without navigation areas have neither tooltip nor click target");
        MachineRecipeCategory.setRuntime(null);
    }

    public static void main(String[] args) throws Exception {
        Bootstrap.register();
        loadNativeTranslations();
        IngredientRegistry registry = registry();
        ingredientsAndLayout(registry);
        lookup(registry);
        navigation();
        check(ItemStack.EMPTY.isEmpty() && ItemStack.EMPTY.getCount() == 0, "native EMPTY sentinel unchanged");
        System.out.println("PASS native JEI core: " + checks + " assertions; actual MC/JEI ingredients and helpers, observed GUI APIs, no GL/gameplay test.");
    }
}
