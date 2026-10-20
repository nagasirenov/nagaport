import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.hbm.blocks.machine.NTMAnvil;
import com.hbm.handler.jei.MachineRecipeView;
import com.hbm.handler.nei.AnvilOverlayHandler;
import com.hbm.handler.nei.AnvilRecipeHandler;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.container.ContainerAnvil;
import com.hbm.inventory.gui.GUIAnvil;
import com.hbm.inventory.recipes.anvil.AnvilRecipes;
import com.hbm.inventory.recipes.anvil.AnvilRecipes.AnvilConstructionRecipe;
import com.hbm.inventory.recipes.anvil.AnvilRecipes.AnvilOutput;

import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.gui.Focus;
import mezz.jei.gui.GuiHelper;
import mezz.jei.ingredients.Ingredients;
import mezz.jei.transfer.RecipeTransferErrorInternal;
import mezz.jei.transfer.RecipeTransferHandlerHelper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/** Complete production anvil category + transfer; NTM-only collaborators are explicit fixtures. */
final class JeiAnvilNativeTest {
    private static int checks;
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler);
    }
    private static final class Slot {
        boolean input;
        int x, y;
        List<ItemStack> variants;
    }
    private static final class Layout {
        final Map<Integer, Slot> slots = new LinkedHashMap<>();
        final Map<Integer, IGuiIngredient<ItemStack>> ingredients = new LinkedHashMap<>();
        final IGuiItemStackGroup group = proxy(IGuiItemStackGroup.class, (self, method, args) -> {
            switch (method.getName()) {
                case "init": {
                    check(args.length == 4, "native four-argument item slot initializer");
                    int index = (Integer) args[0];
                    Slot slot = new Slot();
                    slot.input = (Boolean) args[1]; slot.x = (Integer) args[2]; slot.y = (Integer) args[3];
                    slots.put(index, slot);
                    ingredients.put(index, proxy(IGuiIngredient.class, (ingredient, operation, values) -> {
                        switch (operation.getName()) {
                            case "getDisplayedIngredient": return slot.variants.isEmpty() ? null : slot.variants.get(0);
                            case "getAllIngredients": return slot.variants;
                            case "isInput": return slot.input;
                            default: throw new AssertionError("Unexpected ingredient call " + operation);
                        }
                    }));
                    return null;
                }
                case "set": {
                    @SuppressWarnings("unchecked") List<ItemStack> values = (List<ItemStack>) args[1];
                    slots.get((Integer) args[0]).variants = values;
                    return null;
                }
                case "getGuiIngredients": return ingredients;
                default: throw new AssertionError("Unexpected layout group call " + method);
            }
        });
        final IRecipeLayout layout = proxy(IRecipeLayout.class, (self, method, args) -> {
            switch (method.getName()) {
                case "getItemStacks": return group;
                case "hashCode": return System.identityHashCode(self);
                case "equals": return self == args[0];
                case "toString": return "Observed native recipe layout";
                default: throw new AssertionError("Unexpected recipe layout call " + method);
            }
        });
        void show(AnvilRecipeHandler category, MachineRecipeView view) {
            slots.clear(); ingredients.clear();
            Ingredients value = new Ingredients();
            view.getIngredients(value);
            category.setRecipe(layout, view, value);
        }
    }
    private static AnvilConstructionRecipe recipe(int inputs, int outputs) {
        AStack[] in = new AStack[inputs];
        AnvilOutput[] out = new AnvilOutput[outputs];
        for (int i = 0; i < inputs; i++) in[i] = new AStack(new ItemStack(Items.IRON_INGOT, i + 1));
        for (int i = 0; i < outputs; i++) out[i] = new AnvilOutput(new ItemStack(Items.DIAMOND, i + 1));
        AnvilConstructionRecipe recipe = new AnvilConstructionRecipe(in, out);
        recipe.tierLower = 3;
        return recipe;
    }
    private static void sourceQueries(AnvilRecipeHandler category) {
        AnvilConstructionRecipe first = recipe(1, 1), second = recipe(2, 1);
        first.input.set(0, new AStack(new ItemStack(Items.IRON_INGOT, 3), new ItemStack(Items.GOLD_INGOT, 4), new ItemStack(Items.IRON_INGOT, 3)));
        AnvilRecipes.constructionRecipes.clear();
        Collections.addAll(AnvilRecipes.constructionRecipes, first, second);
        List<MachineRecipeView> all = category.query(null);
        check(all.size() == 2 && all.get(0).source == first && all.get(1).source == second,
                "duplicate-output recipes retain source order and exact distinct identities");
        List<MachineRecipeView> output = category.query(new Focus<>(IFocus.Mode.OUTPUT, new ItemStack(Items.DIAMOND)));
        check(output.size() == 2 && output.get(0).source == first && output.get(1).source == second,
                "same-output focus retains distinct construction recipes, never reverse-matches only first output");
        List<MachineRecipeView> input = category.query(new Focus<>(IFocus.Mode.INPUT, new ItemStack(Items.IRON_INGOT)));
        check(input.size() == 2 && input.get(0).source == first && input.get(1).source == second,
                "duplicate matching inputs/alternatives add a source once");
        check(category.query(new Focus<>(IFocus.Mode.INPUT, new ItemStack(Items.GOLD_INGOT))).get(0).source == first,
                "alternate source ingredients participate in input lookup");
        check(category.query(new Focus<>(IFocus.Mode.OUTPUT, new ItemStack(Items.APPLE))).isEmpty(), "non-matching output is absent");
        check(category.query(new Focus<>(IFocus.Mode.INPUT, new ItemStack(Items.APPLE))).isEmpty(), "non-matching input is absent");
        first.input.set(0, new AStack(new ItemStack(Items.DYE, 2, OreDictionary.WILDCARD_VALUE)));
        check(category.query(new Focus<>(IFocus.Mode.INPUT, new ItemStack(Items.DYE, 1, 5))).get(0).source == first,
                "native NEI wildcard matching retained");
        AnvilRecipes.constructionRecipes.remove(first);
        check(category.query(null).size() == 1 && category.query(null).get(0).source == second,
                "query reads current source rather than frozen output index");
    }
    private static void shapes(AnvilRecipeHandler category) {
        int[][] cases = {{1, 1, 1, 1, 48, 24, 102, 24, 75}, {1, 7, 1, 6, 12, 24, 48, 6, 30},
                         {7, 1, 6, 1, 12, 6, 138, 24, 120}, {5, 5, 4, 4, 3, 6, 93, 6, 75}};
        for (int[] shape : cases) {
            AnvilConstructionRecipe recipe = recipe(shape[0], shape[1]);
            recipe.output.get(0).chance = 0.125F;
            AnvilRecipes.constructionRecipes.clear(); AnvilRecipes.constructionRecipes.add(recipe);
            MachineRecipeView view = category.query(null).get(0);
            check(view.source == recipe, "display wrapper retains exact recipe identity");
            check(NTMAnvil.requestedTier == 3, "decoration queries recipe's lower tier");
            Layout observed = new Layout(); observed.show(category, view);
            check(category.getConstructionRecipe(observed.layout) == recipe, "layout resolves exact displayed source");
            check(observed.slots.size() == shape[0] + shape[1] + 1, "one slot per logical input/output plus tier decoration");
            for (int i = 0; i < shape[0]; i++) {
                Slot slot = observed.slots.get(i);
                check(slot.input && slot.x == shape[4] + 18 * (i % shape[2]) - 1
                        && slot.y == shape[5] + 18 * (i / shape[2]) - 1, "input geometry including wrapping");
            }
            for (int i = 0; i < shape[1]; i++) {
                Slot slot = observed.slots.get(shape[0] + i);
                check(!slot.input && slot.x == shape[6] + 18 * (i % shape[3]) - 1
                        && slot.y == shape[7] + 18 * (i / shape[3]) - 1, "output geometry including wrapping");
            }
            Slot decoration = observed.slots.get(shape[0] + shape[1]);
            check(decoration.x == shape[8] - 1 && decoration.y == 30 && decoration.variants.size() == 2,
                    "tier alternatives have original anvil decoration placement");
            Ingredients ingredients = new Ingredients(); view.getIngredients(ingredients);
            check(ingredients.getInputs(VanillaTypes.ITEM).size() == shape[0]
                    && ingredients.getOutputs(VanillaTypes.ITEM).size() == shape[1], "tier decoration excluded from recipe ingredient indexing");
            check(view.outputs[0][0].getTagCompound().getString("observedFixtureTooltip").endsWith("12.5%"),
                    "category requests chance tooltip on display copy");
            check(!recipe.output.get(0).stack.hasTagCompound(), "chance decoration never mutates authoritative recipe stack");
        }
    }
    private static void transfers(AnvilRecipeHandler category) throws Exception {
        AnvilConstructionRecipe first = recipe(1, 1), second = recipe(2, 1);
        AnvilRecipes.constructionRecipes.clear(); Collections.addAll(AnvilRecipes.constructionRecipes, first, second);
        List<MachineRecipeView> views = category.query(null);
        Layout a = new Layout(), b = new Layout(), unknown = new Layout();
        a.show(category, views.get(0)); b.show(category, views.get(1));
        AnvilOverlayHandler transfer = new AnvilOverlayHandler(category, new RecipeTransferHandlerHelper());
        check(transfer.getContainerClass() == ContainerAnvil.class, "native transfer handler declares anvil container");
        check(AnvilOverlayHandler.class.getMethod("onGuiOpened", GuiOpenEvent.class).isAnnotationPresent(SubscribeEvent.class),
                "GUI tracking retains native Forge subscription annotation");
        ContainerAnvil container = new ContainerAnvil(), equalButDifferent = new ContainerAnvil();
        ItemStack sentinel = new ItemStack(Items.APPLE, 23);
        NBTTagCompound tag = new NBTTagCompound(); tag.setString("untouched", "sentinel"); sentinel.setTagCompound(tag);
        container.sentinel.setInventorySlotContents(0, sentinel);
        ItemStack before = sentinel.copy();
        GUIAnvil gui = new GUIAnvil(container);
        IRecipeTransferError error = transfer.transferRecipe(container, a.layout, null, false, true);
        check(error == RecipeTransferErrorInternal.INSTANCE && gui.focusCalls == 0, "missing target rejects without focus");
        transfer.onGuiOpened(new GuiOpenEvent(gui));
        for (boolean max : new boolean[] {false, true}) {
            check(transfer.transferRecipe(container, a.layout, null, max, false) == null, "valid dry-run succeeds");
            check(gui.focusCalls == 0 && gui.focused == null, "doTransfer=false never focuses GUI");
        }
        check(transfer.transferRecipe(container, a.layout, null, false, true) == null && gui.focused == first && gui.focusCalls == 1,
                "transfer focuses exact first source, not an output-derived reconstruction");
        check(transfer.transferRecipe(container, b.layout, null, true, true) == null && gui.focused == second && gui.focusCalls == 2,
                "max-transfer still selects exact second same-output recipe once");
        check(container.equals(equalButDifferent), "adversarial fixture containers compare equal");
        for (boolean execute : new boolean[] {false, true}) {
            check(transfer.transferRecipe(equalButDifferent, a.layout, null, false, execute) == RecipeTransferErrorInternal.INSTANCE,
                    "container guard uses identity, not equals");
            check(transfer.transferRecipe(container, unknown.layout, null, false, execute) == RecipeTransferErrorInternal.INSTANCE,
                    "undisplayed layout rejects without output guessing");
            check(transfer.transferRecipe(container, null, null, false, execute) == RecipeTransferErrorInternal.INSTANCE,
                    "null layout rejects without output guessing");
            check(transfer.transferRecipe(null, a.layout, null, false, execute) == RecipeTransferErrorInternal.INSTANCE,
                    "null container rejects without focus");
            check(gui.focusCalls == 2, "invalid transfers never focus");
        }
        a.show(category, views.get(1));
        check(transfer.transferRecipe(container, a.layout, null, false, true) == null && gui.focused == second && gui.focusCalls == 3,
                "reused JEI layout selects newly displayed source, not stale identity");
        // Exercise the actual native event with a real non-anvil GuiScreen. No JEI GUI rendering is simulated.
        transfer.onGuiOpened(new GuiOpenEvent(new GuiScreen() { }));
        check(transfer.transferRecipe(container, b.layout, null, false, true) == null && gui.focusCalls == 4 && gui.focused == second,
                "non-null recipe-screen opening retains underlying anvil callback target");
        transfer.onGuiOpened(new GuiOpenEvent(gui));
        check(gui.focused == second && gui.focusCalls == 4, "return-open event does not clear or reapply selected source");
        GUIAnvil replacement = new GUIAnvil(container);
        transfer.onGuiOpened(new GuiOpenEvent(replacement));
        check(transfer.transferRecipe(container, b.layout, null, false, true) == null
                && replacement.focused == second && replacement.focusCalls == 1 && gui.focusCalls == 4,
                "new anvil GUI replaces stale GUI target even with same container");
        transfer.onGuiOpened(new GuiOpenEvent(null));
        transfer.onGuiOpened(new GuiOpenEvent(new GuiScreen() { }));
        check(transfer.transferRecipe(container, b.layout, null, false, true) == RecipeTransferErrorInternal.INSTANCE
                && replacement.focusCalls == 1, "close clears target and unrelated later screen cannot restore it");
        transfer.onGuiOpened(new GuiOpenEvent(gui));
        check(transfer.transferRecipe(container, b.layout, null, false, false) == null && gui.focusCalls == 4,
                "reopen restores only dry-run eligibility, without crafting or focus");
        IRecipeTransferHandler<ContainerAnvil> nativeInterface = transfer;
        check(nativeInterface.transferRecipe(container, b.layout, null, true, false) == null && gui.focusCalls == 4,
                "real JEI transfer interface dispatch and compiler bridge preserve dry-run semantics");
        check(ItemStack.areItemStacksEqual(container.sentinel.getStackInSlot(0), before)
                && container.sentinel.getStackInSlot(0) == sentinel, "selection and dry-runs preserve native inventory item identity/count/NBT");
    }
    private static void noDirectCraftOrInventoryCalls() throws Exception {
        List<String> allowed = Arrays.asList("java/lang/Object.<init>",
                "net/minecraftforge/client/event/GuiOpenEvent.getGui",
                "com/hbm/handler/nei/AnvilRecipeHandler.getConstructionRecipe",
                "mezz/jei/api/recipe/transfer/IRecipeTransferHandlerHelper.createInternalError",
                "com/hbm/inventory/gui/GUIAnvil.focusRecipe",
                "com/hbm/handler/nei/AnvilOverlayHandler.transferRecipe");
        try (InputStream stream = AnvilOverlayHandler.class.getResourceAsStream("AnvilOverlayHandler.class")) {
            new ClassReader(stream).accept(new ClassVisitor(Opcodes.ASM5) {
                @Override public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    return new MethodVisitor(Opcodes.ASM5) {
                        @Override public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                            check(allowed.contains(owner + "." + name), "transfer class has no craft/packet/inventory call: " + owner + "." + name);
                        }
                        @Override public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                            check((opcode != Opcodes.PUTFIELD && opcode != Opcodes.PUTSTATIC)
                                    || owner.equals("com/hbm/handler/nei/AnvilOverlayHandler"), "transfer class never writes collaborator/inventory state directly");
                        }
                        @Override public void visitInvokeDynamicInsn(String name, String descriptor, org.objectweb.asm.Handle bootstrap, Object... args) {
                            throw new AssertionError("Unexpected dynamic call in selection-only transfer");
                        }
                    };
                }
            }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
    }
    public static void main(String[] args) throws Exception {
        Bootstrap.register();
        AnvilRecipeHandler category = new AnvilRecipeHandler();
        category.initialize(new GuiHelper(JeiCoreNativeTest.registry()));
        check(category.getBackground().getWidth() == 166 && category.getBackground().getHeight() == 65,
                "native JEI background size");
        check(category.getUid().equals("ntmAnvil") && category.getMachines().length == 12, "anvil UID and twelve catalyst entries");
        sourceQueries(category);
        shapes(category);
        transfers(category);
        noDirectCraftOrInventoryCalls();
        System.out.println("PASS: " + checks + " native AnvilRecipeHandler/AnvilOverlayHandler assertions (NTM-only domain/GUI fixtures; no full gameplay proof)");
    }
}
