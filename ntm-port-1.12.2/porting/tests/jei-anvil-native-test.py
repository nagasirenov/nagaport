#!/usr/bin/env python3
"""Run whole Anvil category/transfer production classes on pinned native APIs.

Only com.hbm NTM domain/registry collaborators are isolated fixtures. No Minecraft,
Forge, NEI or JEI API classes are supplied by this test. GUIAnvil is an observation
collaborator extending the real GuiContainer, NOT a gameplay GUI implementation.
This proves native category/transfer behavior, not the unported GUI/container graph.
"""
import argparse
import os
from pathlib import Path
import re
import subprocess
import tempfile

parser = argparse.ArgumentParser(description=__doc__)
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
'blocks/ModBlocks.java': '''package com.hbm.blocks;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
public final class ModBlocks {
    public static final Block anvil_iron = Blocks.ANVIL;
    public static final Block anvil_lead = Blocks.IRON_BLOCK;
    public static final Block anvil_steel = Blocks.GOLD_BLOCK;
    public static final Block anvil_desh = Blocks.DIAMOND_BLOCK;
    public static final Block anvil_saturnite = Blocks.EMERALD_BLOCK;
    public static final Block anvil_ferrouranium = Blocks.REDSTONE_BLOCK;
    public static final Block anvil_bismuth_bronze = Blocks.LAPIS_BLOCK;
    public static final Block anvil_arsenic_bronze = Blocks.QUARTZ_BLOCK;
    public static final Block anvil_schrabidate = Blocks.OBSIDIAN;
    public static final Block anvil_dnt = Blocks.COAL_BLOCK;
    public static final Block anvil_osmiridium = Blocks.BRICK_BLOCK;
    public static final Block anvil_murky = Blocks.END_STONE;
}''',
'blocks/machine/NTMAnvil.java': '''package com.hbm.blocks.machine;
import java.util.Arrays;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Blocks;
public final class NTMAnvil {
    public static int requestedTier;
    public static List<ItemStack> getAnvilsFromTier(int tier) {
        requestedTier = tier;
        return Arrays.asList(new ItemStack(Blocks.ANVIL), new ItemStack(Blocks.IRON_BLOCK));
    }
}''',
'inventory/RecipesCommon.java': '''package com.hbm.inventory;
import java.util.Arrays;
import java.util.List;
import net.minecraft.item.ItemStack;
public final class RecipesCommon {
    public static final class AStack {
        private final List<ItemStack> alternatives;
        public AStack(ItemStack... alternatives) { this.alternatives = Arrays.asList(alternatives); }
        public List<ItemStack> extractForNEI() { return alternatives; }
    }
}''',
'inventory/recipes/anvil/AnvilRecipes.java': '''package com.hbm.inventory.recipes.anvil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.hbm.inventory.RecipesCommon.AStack;
import net.minecraft.item.ItemStack;
/** NTM-only registry/data fixture; mirrors production recipe field types. */
public final class AnvilRecipes {
    public static final List<AnvilConstructionRecipe> constructionRecipes = new ArrayList<>();
    public static List<AnvilConstructionRecipe> getConstruction() { return constructionRecipes; }
    public static final class AnvilConstructionRecipe {
        public final List<AStack> input;
        public final List<AnvilOutput> output;
        public int tierLower;
        public AnvilConstructionRecipe(AStack[] input, AnvilOutput[] output) {
            this.input = Arrays.asList(input); this.output = Arrays.asList(output);
        }
    }
    public static final class AnvilOutput {
        public ItemStack stack;
        public float chance;
        public AnvilOutput(ItemStack stack) { this(stack, 1F); }
        public AnvilOutput(ItemStack stack, float chance) { this.stack = stack; this.chance = chance; }
    }
    public enum OverlayType { NONE, CONSTRUCTION, RECYCLING, SMITHING }
}''',
'util/ItemStackUtil.java': '''package com.hbm.util;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
/** Observe the category's tooltip request; not a replacement tooltip implementation. */
public final class ItemStackUtil {
    public static void addTooltipToStack(ItemStack stack, String tooltip) {
        if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setString("observedFixtureTooltip", tooltip);
    }
}''',
'inventory/container/ContainerAnvil.java': '''package com.hbm.inventory.container;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
/** NTM-only fail-on-action collaborator using real native inventory classes. */
public final class ContainerAnvil extends Container {
    public final InventoryBasic sentinel = new InventoryBasic("Probe", false, 1);
    public ContainerAnvil() { addSlotToContainer(new Slot(sentinel, 0, 0, 0)); }
    @Override public boolean canInteractWith(EntityPlayer player) { throw new AssertionError("Unexpected container action"); }
    @Override public ItemStack transferStackInSlot(EntityPlayer player, int index) { throw new AssertionError("Unexpected inventory transfer"); }
    @Override public ItemStack slotClick(int slot, int button, ClickType type, EntityPlayer player) { throw new AssertionError("Unexpected inventory click"); }
    @Override public boolean equals(Object other) { return other instanceof ContainerAnvil; }
    @Override public int hashCode() { return 1; }
}''',
'inventory/gui/GUIAnvil.java': '''package com.hbm.inventory.gui;
import com.hbm.inventory.container.ContainerAnvil;
import com.hbm.inventory.recipes.anvil.AnvilRecipes.AnvilConstructionRecipe;
import net.minecraft.client.gui.inventory.GuiContainer;
/** NTM-only callback observer. Does NOT simulate production GUI selection/gameplay. */
public final class GUIAnvil extends GuiContainer {
    public int focusCalls;
    public AnvilConstructionRecipe focused;
    public GUIAnvil(ContainerAnvil container) { super(container); }
    public void focusRecipe(AnvilConstructionRecipe recipe) { focusCalls++; focused = recipe; }
    @Override protected void drawGuiContainerBackgroundLayer(float ticks, int x, int y) { throw new AssertionError("No rendering in scoped probe"); }
}''',
}

# Separate source-level lifecycle guard, not compiled GUI/gameplay evidence.
def method_body(text, declaration):
    begin = text.index('{', text.index(declaration))
    depth = 1
    end = begin + 1
    while depth:
        if text[end] == '{': depth += 1
        elif text[end] == '}': depth -= 1
        end += 1
    return text[begin + 1:end - 1]

gui_source = (sources / 'inventory/gui/GUIAnvil.java').read_text(encoding='utf-8')
init = method_body(gui_source, 'public void initGui()')
assert not re.search(r'(?:regenerateRecipes|resetPaging)\s*\(|(?:this\.)?(?:selection|index)\s*=', init), \
    'Returning through initGui must not reset recipe selection/paging'
for declaration in ('public void focusRecipe(', 'private void regenerateRecipes()', 'private void resetPaging()'):
    body = method_body(gui_source, declaration)
    assert not re.search(r'Packet|sendToServer|slotClick|transferStack|decrStackSize|setInventorySlotContents', body), \
        'Selection-only GUI path must not craft or modify inventory: ' + declaration

files = [sources / 'handler/jei' / (name + '.java') for name in
         ('MachineRecipeCategory', 'MachineRecipeView', 'NativeRecipeLookup', 'RecipeStackMatcher', 'RecipeItemAlternatives')]
files += [sources / 'handler/nei/AnvilRecipeHandler.java', sources / 'handler/nei/AnvilOverlayHandler.java',
          sources / 'lib/RefStrings.java', root / 'porting/tests/JeiCoreNativeTest.java',
          root / 'porting/tests/jei-anvil-native-test.java']
with tempfile.TemporaryDirectory(prefix='jei-anvil-native-') as temporary:
    temporary = Path(temporary)
    output = temporary / 'classes'
    output.mkdir()
    for name, content in fixtures.items():
        path = temporary / 'fixture/com/hbm' / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content, encoding='utf-8')
        files.append(path)
    subprocess.run([executable('javac'), '-source', '8', '-target', '8', '-proc:none', '-implicit:none',
                    '-encoding', 'UTF-8', '-cp', args.classpath, '-d', str(output)]
                   + [str(file) for file in files], check=True)
    subprocess.run([executable('java'), '-cp', str(output) + os.pathsep + args.classpath,
                    'JeiAnvilNativeTest'], cwd=temporary, check=True)
print('SOURCE-ONLY GUI lifecycle guards passed; no full GUI/gameplay compilation claimed.')
