#!/usr/bin/env python3
"""Execute the entire production CustomMachineHandler against actual target binaries.

Minecraft/Forge, JEI and NEI are real dependencies. NTM configuration/recipe carrier,
fluid-display and item/block-registration collaborators are isolated fixtures only.
The exact production ItemStackUtil.addTooltipToStack method is extracted unchanged
for this scoped utility collaborator. CustomMachineHandler itself and the three JEI
core classes are compiled whole, never sliced. Annotation drawing is checked only
as a source contract, not as a rendered screenshot or gameplay test.
"""
import argparse
import os
from pathlib import Path
import re
import subprocess
import tempfile

parser = argparse.ArgumentParser()
parser.add_argument('--java-home', type=Path, required=True)
parser.add_argument('--classpath', required=True)
args = parser.parse_args()
root = Path(__file__).resolve().parents[2]
sources = root / 'src/main/java/com/hbm'

def executable(name):
    for suffix in ('.exe', '') if os.name == 'nt' else ('', '.exe'):
        path = args.java_home / 'bin' / (name + suffix)
        if path.is_file():
            return str(path)
    raise FileNotFoundError('Missing Java executable: ' + name + ' under ' + str(args.java_home))

def exact_method(source, signature):
    start = source.index(signature)
    opening = source.index('{', start)
    depth = 1
    end = opening + 1
    while depth:
        if source[end] == '{': depth += 1
        elif source[end] == '}': depth -= 1
        end += 1
    return source[start:end]

# Golden placement/conditions read from the original-pristine CustomMachineHandler.
# This section proves only source structure. It deliberately makes no GL claim.
custom_source = (sources / 'handler/nei/CustomMachineHandler.java').read_text(encoding='utf-8')
compact = re.sub(r'\s+', '', custom_source)
annotations = [
    'if(recipe.radiationAmount!=0)',
    'Stringradiation="Radiation:"+recipe.radiationAmount;',
    'drawString(radiation,160-minecraft.fontRenderer.getStringWidth(radiation),63,0x08FF00)',
    'if(recipe.pollutionAmount!=0)',
    'Stringpollution=recipe.pollutionType+":"+recipe.pollutionAmount;',
    'drawString(pollution,160-minecraft.fontRenderer.getStringWidth(pollution),75,0x404040)',
    'if(conf.fluxMode)',
    'Stringflux="Flux:"+recipe.flux;',
    'drawString(flux,83-minecraft.fontRenderer.getStringWidth(flux)/2,16,0x08FF00)',
    'if(conf.maxHeat>0&&recipe.heat>0)',
    'Stringheat="Heat:"+recipe.heat;',
    'drawString(heat,83-minecraft.fontRenderer.getStringWidth(heat)/2,8,0xFF0000)',
    'Gui.drawModalRectWithCustomSizedTexture(0,0,5,11,166,65,256,256)',
]
for annotation in annotations:
    if annotation not in compact:
        raise AssertionError('Custom annotation source contract changed: ' + annotation)
print('PASS custom annotations: %d source-only contracts; not GL rendering evidence.' % len(annotations), flush=True)

tooltip = exact_method((sources / 'util/ItemStackUtil.java').read_text(encoding='utf-8'),
                       'public static ItemStack addTooltipToStack(')
fixtures = {
'blocks/ModBlocks.java': '''package com.hbm.blocks;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
public final class ModBlocks { public static final Block custom_machine = Blocks.WOOL; }''',
'config/CustomMachineConfigJSON.java': '''package com.hbm.config;
import java.util.ArrayList;
import java.util.List;
public final class CustomMachineConfigJSON {
    public static final List<MachineConfiguration> niceList = new ArrayList<>();
    public static final class MachineConfiguration {
        public String recipeKey;
        public String unlocalizedName;
        public String localizedName;
        public boolean fluxMode;
        public int maxHeat;
    }
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
    public static final class AStack {
        private final ItemStack[] alternatives;
        public AStack(ItemStack... alternatives) { this.alternatives = alternatives; }
        public List<ItemStack> extractForNEI() { return Arrays.asList(alternatives); }
    }
}''',
'inventory/recipes/CustomMachineRecipes.java': '''package com.hbm.inventory.recipes;
import java.util.HashMap;
import java.util.List;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.util.Tuple.Pair;
import net.minecraft.item.ItemStack;
public final class CustomMachineRecipes {
    public static final HashMap<String, List<CustomMachineRecipe>> recipes = new HashMap<>();
    public static final class CustomMachineRecipe {
        public FluidStack[] inputFluids = new FluidStack[0];
        public AStack[] inputItems = new AStack[0];
        public FluidStack[] outputFluids = new FluidStack[0];
        public Pair<ItemStack, Float>[] outputItems = new Pair[0];
        public int flux;
        public int heat;
        public float radiationAmount;
        public String pollutionType;
        public float pollutionAmount;
    }
}''',
'items/machine/ItemFluidIcon.java': '''package com.hbm.items.machine;
import com.hbm.inventory.FluidStack;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
public final class ItemFluidIcon {
    public static ItemStack make(FluidStack fluid) {
        ItemStack stack = new ItemStack(Items.DYE, 1, fluid.type);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("fill", fluid.fill); tag.setInteger("pressure", fluid.pressure);
        stack.setTagCompound(tag); return stack;
    }
}''',
'util/ItemStackUtil.java': '''package com.hbm.util;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.TextFormatting;
/** Exact production tooltip method in an explicitly scoped test collaborator. */
public final class ItemStackUtil {
''' + tooltip + '\n}'
}
files = [sources / 'handler/jei' / (name + '.java') for name in
         ('MachineRecipeCategory', 'MachineRecipeView', 'NativeRecipeLookup', 'RecipeStackMatcher')]
files += [sources / 'handler/nei/CustomMachineHandler.java', sources / 'lib/RefStrings.java',
          sources / 'util/Tuple.java', sources / 'interfaces/Spaghetti.java',
          root / 'porting/tests/JeiCoreNativeTest.java', root / 'porting/tests/JeiCustomNativeTest.java']
with tempfile.TemporaryDirectory(prefix='jei-custom-native-') as temporary:
    temporary = Path(temporary)
    output = temporary / 'classes'
    output.mkdir()
    for name, content in fixtures.items():
        path = temporary / 'fixture/com/hbm' / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content, encoding='utf-8')
        files.append(path)
    subprocess.run([
        executable('javac'), '-source', '8', '-target', '8', '-proc:none', '-implicit:none',
        '-encoding', 'UTF-8', '-cp', args.classpath, '-d', str(output)
    ] + [str(file) for file in files], check=True)
    subprocess.run([
        executable('java'), '-cp', str(output) + os.pathsep + args.classpath, 'JeiCustomNativeTest'
    ], cwd=temporary, check=True)
