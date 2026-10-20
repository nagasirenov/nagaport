#!/usr/bin/env python3
"""Execute complete migrated handlers against real pinned Minecraft/Forge/JEI APIs.

NTM-only fixture collaborators isolate recipe registries, settings, fluid/rod data,
and item/block registration from the still-unported full mod graph. These fixtures
are compiled only in a temporary directory, are not production replacements, and
are deliberately not evidence for the rest of NTM. All target Minecraft/Forge/JEI classes are actual dependency binaries. The whole production recipe
matcher is compiled without requiring NEI. Every selected handler and all three
shared core classes are compiled whole without text slicing.
"""
import argparse
import os
from pathlib import Path
import subprocess
import tempfile

parser = argparse.ArgumentParser()
parser.add_argument('--java-home', type=Path, required=True)
parser.add_argument('--classpath', required=True)
args = parser.parse_args()
def executable(name):
    for suffix in ('.exe', '') if os.name == 'nt' else ('', '.exe'):
        path = args.java_home / 'bin' / (name + suffix)
        if path.is_file():
            return str(path)
    raise FileNotFoundError('Missing Java executable: ' + name + ' under ' + str(args.java_home))
root = Path(__file__).resolve().parents[2]
sources = root / 'src/main/java/com/hbm'
fixtures = {
'config/ClientConfig.java': '''package com.hbm.config;
public final class ClientConfig {
    public static final Setting NEI_HIDE_SECRETS = new Setting();
    public static final class Setting {
        public boolean value;
        public boolean get() { return value; }
    }
}''',
'items/ModItems.java': '''package com.hbm.items;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.item.Item;
import net.minecraft.init.Items;
public final class ModItems {
    public static final Item item_secret = Items.NETHER_STAR;
    public static final Item blueprints = Items.PAPER;
    public static final Item fluid_icon = Items.DYE;
    public static final Set<Item> excludeNEI = new HashSet<>();
}''',
'blocks/ModBlocks.java': '''package com.hbm.blocks;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
public final class ModBlocks {
    public static final Block machine_waste_drum = Blocks.CAULDRON;
    public static final Block machine_annihilator = Blocks.OBSIDIAN;
    public static final Block machine_assembly_machine = Blocks.CRAFTING_TABLE;
    public static final Block machine_precass = Blocks.ANVIL;
    public static final Block machine_chemical_plant = Blocks.BREWING_STAND;
    public static final Block machine_chemical_factory = Blocks.BEACON;
    public static final Block machine_purex = Blocks.FURNACE;
    public static final Block fusion_torus = Blocks.IRON_BLOCK;
}''',
'inventory/FluidStack.java': '''package com.hbm.inventory;
public final class FluidStack {
    public final int type;
    public final int fill;
    public final int pressure;
    public FluidStack(int type, int fill, int pressure) {
        this.type = type; this.fill = fill; this.pressure = pressure;
    }
}''',
'inventory/RecipesCommon.java': '''package com.hbm.inventory;
import java.util.Arrays;
import java.util.List;
import net.minecraft.item.ItemStack;
public final class RecipesCommon {
    public static class AStack {
        private final ItemStack[] alternatives;
        public AStack(ItemStack... alternatives) { this.alternatives = alternatives; }
        public List<ItemStack> extractForNEI() { return Arrays.asList(alternatives); }
    }
}''',
'inventory/recipes/loader/GenericRecipe.java': '''package com.hbm.inventory.recipes.loader;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.FluidStack;
public class GenericRecipe {
    public AStack[] inputItem;
    public FluidStack[] inputFluid;
    public GenericRecipes.IOutput[] outputItem;
    public FluidStack[] outputFluid;
    public String[] pools;
    public boolean isPooled() { return pools != null; }
    public String[] getPools() { return pools; }
}''',
'inventory/recipes/loader/GenericRecipes.java': '''package com.hbm.inventory.recipes.loader;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
public class GenericRecipes<T extends GenericRecipe> {
    public static final String POOL_PREFIX_SECRET = "secret.";
    public final List<T> recipeOrderedList = new ArrayList<>();
    public interface IOutput { ItemStack[] getAllPossibilities(); }
}''',
'items/machine/ItemFluidIcon.java': '''package com.hbm.items.machine;
import com.hbm.inventory.FluidStack;
import com.hbm.items.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
public final class ItemFluidIcon {
    public static ItemStack make(FluidStack fluid) {
        ItemStack stack = new ItemStack(ModItems.fluid_icon, 1, fluid.type);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("fill", fluid.fill); tag.setInteger("pressure", fluid.pressure);
        stack.setTagCompound(tag); return stack;
    }
}''',
'items/machine/ItemBlueprints.java': '''package com.hbm.items.machine;
import com.hbm.items.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
public final class ItemBlueprints {
    public static ItemStack make(String pool) {
        ItemStack stack = new ItemStack(ModItems.blueprints);
        NBTTagCompound tag = new NBTTagCompound(); tag.setString("pool", pool);
        stack.setTagCompound(tag); return stack;
    }
}''',
'items/machine/ItemRBMKRod.java': '''package com.hbm.items.machine;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
public final class ItemRBMKRod extends Item {
    public ItemRBMKRod() { setRegistryName("jei_fixture", "rod"); }
    public static double getCoreHeat(ItemStack stack) {
        return stack.hasTagCompound() ? stack.getTagCompound().getDouble("core") : 0;
    }
    public static double getHullHeat(ItemStack stack) {
        return stack.hasTagCompound() ? stack.getTagCompound().getDouble("hull") : 0;
    }
}''',
'util/InventoryUtil.java': '''package com.hbm.util;
import com.hbm.inventory.RecipesCommon.AStack;
import net.minecraft.item.ItemStack;
public final class InventoryUtil {
    public static ItemStack[][] extractObject(Object value) {
        if (value instanceof ItemStack) return new ItemStack[][] {{((ItemStack) value).copy()}};
        if (value instanceof ItemStack[][]) return (ItemStack[][]) value;
        if (value instanceof ItemStack[]) {
            ItemStack[] values = (ItemStack[]) value;
            ItemStack[][] result = new ItemStack[values.length][];
            for (int i = 0; i < values.length; i++) result[i] = new ItemStack[] {values[i]};
            return result;
        }
        if (value instanceof AStack) return new ItemStack[][] {((AStack) value).extractForNEI().toArray(new ItemStack[0])};
        if (value instanceof Object[]) {
            Object[] values = (Object[]) value;
            ItemStack[][] result = new ItemStack[values.length][];
            for (int i = 0; i < values.length; i++) {
                Object element = values[i];
                if (element instanceof ItemStack[]) result[i] = (ItemStack[]) element;
                else result[i] = extractObject(element)[0];
            }
            return result;
        }
        throw new AssertionError("Unexpected fixture recipe representation: " + value);
    }
}''',
'inventory/recipes/FuelPoolRecipes.java': '''package com.hbm.inventory.recipes;
import java.util.HashMap;
public final class FuelPoolRecipes { public static final HashMap<Object, Object> recipes = new HashMap<>(); }''',
'inventory/recipes/AnnihilatorRecipes.java': '''package com.hbm.inventory.recipes;
import java.util.HashMap;
public final class AnnihilatorRecipes {
    public static final HashMap<Object, Object> recipes = new HashMap<>();
    public static HashMap<Object, Object> getRecipes() { return recipes; }
}'''
}
for name in ('AssemblyMachineRecipes', 'PrecAssRecipes', 'ChemicalPlantRecipes', 'PUREXRecipes', 'FusionRecipes'):
    fixtures['inventory/recipes/' + name + '.java'] = '''package com.hbm.inventory.recipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
public final class %s {
    public static final GenericRecipes<GenericRecipe> INSTANCE = new GenericRecipes<>();
}''' % name

files = [sources / 'handler/jei' / (name + '.java') for name in
         ('MachineRecipeCategory', 'MachineRecipeView', 'NativeRecipeLookup', 'RecipeStackMatcher')]
files += [sources / 'handler/nei' / (name + '.java') for name in
          ('NEIUniversalHandler', 'FuelPoolHandler', 'AnnihilatorHandler', 'NEIGenericRecipeHandler',
           'AssemblyMachineRecipeHandler', 'PrecAssRecipeHandler', 'ChemicalPlantRecipeHandler',
           'PUREXRecipeHandler', 'FusionRecipeHandler')]
files += [sources / 'interfaces/NotableComments.java', sources / 'lib/RefStrings.java',
          root / 'porting/tests/JeiCoreNativeTest.java', root / 'porting/tests/JeiHandlersNativeTest.java']
with tempfile.TemporaryDirectory(prefix='jei-handlers-native-') as temporary:
    temporary = Path(temporary)
    output = temporary / 'classes'
    output.mkdir()
    for name, content in fixtures.items():
        path = temporary / 'fixture/com/hbm' / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content, encoding='utf-8')
        files.append(path)
    subprocess.run([
        executable('javac'), '-source', '8', '-target', '8',
        '-proc:none', '-implicit:none', '-encoding', 'UTF-8', '-cp', args.classpath,
        '-d', str(output)
    ] + [str(file) for file in files], check=True)
    subprocess.run([
        executable('java'), '-cp', str(output) + os.pathsep + args.classpath,
        'JeiHandlersNativeTest'
    ], cwd=temporary, check=True)
