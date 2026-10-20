import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.hbm.config.ClientConfig;
import com.hbm.config.CustomMachineConfigJSON;
import com.hbm.handler.RecipeBrowserVisibility;
import com.hbm.handler.jei.MachineRecipeCategory;
import com.hbm.handler.jei.MachineRecipeView;
import com.hbm.handler.jei.NTMJeiPlugin;
import com.hbm.handler.jei.NativeRecipeLookup;
import com.hbm.handler.jei.RecipeStackMatcher;
import com.hbm.handler.nei.AnvilOverlayHandler;
import com.hbm.handler.nei.AnvilRecipeHandler;
import com.hbm.main.MainRegistry;
import com.hbm.main.NEIRegistry;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.IRecipesGui;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import mezz.jei.gui.GuiHelper;
import mezz.jei.ingredients.IngredientRegistry;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;

/** Whole production plugin/policy/core; only NTM content and GUI collaborators are fixtures. */
public final class JeiOptionalBrowserNativeTest {
    private static int checks;
    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }
    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler);
    }
    private static boolean sameStack(ItemStack a, ItemStack b) {
        return a.getItem() == b.getItem() && a.getItemDamage() == b.getItemDamage()
                && a.getCount() == b.getCount() && ItemStack.areItemStackTagsEqual(a, b);
    }
    private static void sameSequence(List<ItemStack> expected, List<ItemStack> actual, String message) {
        check(expected.size() == actual.size(), message + " count");
        for (int i = 0; i < expected.size(); i++) check(sameStack(expected.get(i), actual.get(i)), message + " index " + i);
    }
    private static List<ItemStack> hidden() {
        List<ItemStack> hidden = new ArrayList<>();
        RecipeBrowserVisibility.configureHiddenItems(hidden::add);
        return hidden;
    }
    private static void visibility(boolean oracle) throws Exception {
        for (boolean secrets : new boolean[] {false, true}) {
            for (int polaroid : new int[] {0, 11}) {
                ClientConfig.NEI_HIDE_SECRETS.value = secrets;
                MainRegistry.polaroidID = polaroid;
                List<ItemStack> actual = hidden();
                // Counts belong to the explicit three-ingot/two-secret/four-ammo/
                // two-ore-types fixture, not an assertion about the real registries.
                check(actual.size() == 46 + (secrets ? 8 : 0) + (polaroid == 11 ? 0 : 4),
                        "visibility branches retain fixture counts");
                if (oracle) {
                    List<ItemStack> expected = new ArrayList<>();
                    Class.forName("com.hbm.main.OriginalNEIConfig").getMethod("configureHiddenItems", Consumer.class)
                            .invoke(null, (Consumer<ItemStack>) expected::add);
                    sameSequence(expected, actual, "complete checkpoint21 visibility oracle");
                }
                for (int i = actual.size() - 4; i < actual.size(); i++) {
                    check(actual.get(i).hasTagCompound(), "ore entries retain native NBT");
                    check(!"BASE".equals(actual.get(i).getTagCompound().getString("grade")), "base ore grade omitted");
                }
            }
        }
        ClientConfig.NEI_HIDE_SECRETS.value = false;
        MainRegistry.polaroidID = 11;
        List<ItemStack> baseline = hidden();
        check(baseline.get(3).getTagCompound().getInteger("charge") == 0, "empty battery remains hidden");
        check(baseline.get(4).getTagCompound().getInteger("charge") == 100, "full battery remains hidden");
    }
    private static void plugin() {
        IngredientRegistry ingredients = JeiCoreNativeTest.registry();
        List<ItemStack> blacklisted = new ArrayList<>();
        List<IRecipeCategory<?>> categories = new ArrayList<>();
        List<IRecipeRegistryPlugin> lookups = new ArrayList<>();
        List<String> catalysts = new ArrayList<>();
        List<String> transfers = new ArrayList<>();
        List<String> shown = new ArrayList<>();
        IIngredientBlacklist blacklist = proxy(IIngredientBlacklist.class, (self, method, args) -> {
            if (method.getName().equals("addIngredientToBlacklist")) { blacklisted.add((ItemStack) args[0]); return null; }
            throw new AssertionError("Unexpected blacklist call: " + method);
        });
        IRecipeTransferHandlerHelper transferHelper = proxy(IRecipeTransferHandlerHelper.class, (self, method, args) -> {
            throw new AssertionError("Unexpected transfer helper call: " + method);
        });
        GuiHelper gui = new GuiHelper(ingredients);
        IJeiHelpers helpers = proxy(IJeiHelpers.class, (self, method, args) -> {
            switch (method.getName()) {
                case "getGuiHelper": return gui;
                case "getIngredientBlacklist": return blacklist;
                case "recipeTransferHandlerHelper": return transferHelper;
                default: throw new AssertionError("Unexpected helpers call: " + method);
            }
        });
        IRecipeCategoryRegistration categoryRegistry = proxy(IRecipeCategoryRegistration.class, (self, method, args) -> {
            if (method.getName().equals("getJeiHelpers")) return helpers;
            if (method.getName().equals("addRecipeCategories")) {
                categories.addAll(Arrays.asList((IRecipeCategory<?>[]) args[0])); return null;
            }
            throw new AssertionError("Unexpected category registry call: " + method);
        });
        IRecipeTransferRegistry transferRegistry = proxy(IRecipeTransferRegistry.class, (self, method, args) -> {
            if (method.getName().equals("addRecipeTransferHandler") && args.length == 2) {
                check(args[0] instanceof AnvilOverlayHandler, "plugin installs its NTM anvil adapter");
                transfers.add((String) args[1]); return null;
            }
            throw new AssertionError("Unexpected transfer registry call: " + method);
        });
        IModRegistry registry = proxy(IModRegistry.class, (self, method, args) -> {
            switch (method.getName()) {
                case "getJeiHelpers": return helpers;
                case "getIngredientRegistry": return ingredients;
                case "addRecipeRegistryPlugin": lookups.add((IRecipeRegistryPlugin) args[0]); return null;
                case "addRecipeCatalyst":
                    check(((ItemStack) args[0]).getItem() == Items.DIAMOND, "real catalyst adapter omits null and EMPTY");
                    catalysts.addAll(Arrays.asList((String[]) args[1])); return null;
                case "getRecipeTransferRegistry": return transferRegistry;
                default: throw new AssertionError("Unexpected mod registry call: " + method);
            }
        });
        AnvilRecipeHandler anvil = new AnvilRecipeHandler();
        NEIRegistry.handlers.add(anvil);
        CustomMachineConfigJSON.niceList.add(new CustomMachineConfigJSON.MachineConfiguration("fixture:custom"));
        NTMJeiPlugin plugin = new NTMJeiPlugin();
        plugin.registerCategories(categoryRegistry);
        check(categories.size() == 2, "plugin registers static and custom categories");
        check(categories.get(0) == anvil && categories.get(1).getUid().equals("fixture:custom"), "category order retained");
        check(anvil.getBackground().getWidth() == 166, "production core initialized through real JEI GUI helper");
        categories.clear();
        plugin.registerCategories(categoryRegistry);
        check(categories.size() == 2, "re-registering categories clears previous entries");
        // Supply a real Forge container for this isolated registration call;
        // this is not a substitute for Forge mod discovery or client startup.
        ModContainer previous = Loader.instance().activeModContainer();
        ModMetadata metadata = new ModMetadata();
        metadata.modId = "optional_browser_fixture";
        metadata.name = "Optional browser native fixture";
        metadata.version = "1";
        Loader.instance().setupTestHarness(new DummyModContainer(metadata));
        try { plugin.register(registry); }
        finally { Loader.instance().setActiveModContainer(previous); }
        check(lookups.size() == 1 && lookups.get(0) instanceof NativeRecipeLookup, "production native lookup installed");
        check(catalysts.equals(Arrays.asList("fixture:anvil")), "whole production catalyst adapter executes");
        sameSequence(hidden(), blacklisted, "plugin delegates to browser-neutral visibility");
        check(anvil.guiRegistrations == 1, "plugin calls NTM GUI-area collaborator");
        check(transfers.equals(Arrays.asList("fixture:anvil")) && AnvilOverlayHandler.constructions == 1,
                "anvil transfer branch constructs and registers adapter");
        IRecipesGui recipesGui = proxy(IRecipesGui.class, (self, method, args) -> {
            if (method.getName().equals("showCategories")) {
                for (Object uid : (List<?>) args[0]) shown.add((String) uid); return null;
            }
            throw new AssertionError("Unexpected recipes GUI call: " + method);
        });
        IJeiRuntime runtime = proxy(IJeiRuntime.class, (self, method, args) -> {
            if (method.getName().equals("getRecipesGui")) return recipesGui;
            throw new AssertionError("Unexpected runtime call: " + method);
        });
        plugin.onRuntimeAvailable(runtime);
        MachineRecipeView view = new MachineRecipeView(anvil, null, null, null);
        check(view.handleClick(null, 147, 1, 0), "plugin publishes runtime to complete production core");
        check(shown.equals(Arrays.asList("fixture:anvil")), "core routes navigation into supplied JEI runtime");
        MachineRecipeCategory.setRuntime(null);
        check(RecipeStackMatcher.matchesType(ItemStack.EMPTY, ItemStack.EMPTY), "complete matcher executes without NEI");
        check(!RecipeStackMatcher.matchesCrafting(null, new ItemStack(Items.APPLE)), "crafting matcher executes without NEI");
    }
    public static void main(String[] args) throws Exception {
        Bootstrap.register();
        visibility(args.length > 0 && args[0].equals("oracle"));
        plugin();
        System.out.println("PASS: " + checks + " optional-browser checks (" + args[0]
                + "); actual MC/Forge/JEI with isolated NTM registry/category/GUI collaborators.");
    }
}
