#!/usr/bin/env python3
"""Bounded optional-NEI proof using actual pinned Minecraft/Forge/JEI/NEI binaries.

Complete production matcher, visibility policy, plugin, three JEI core files and
catalyst adapter compile against a classpath physically excluding NEI. Temporary
fixtures cover NTM registries, item implementations, category subclasses and the
anvil GUI adapter ONLY; no Minecraft/Forge/JEI/NEI API is replaced. A separate
native oracle run compares the matcher to the pinned original NEI utility and
visibility to the complete checkpoint21 NEIConfig, renamed only to coexist.

A rejecting URLClassLoader then executes all plugin lifecycle methods without
NEI, with the real optional NEIConfig as a failing negative control. This proves
only this isolated NTM fixture graph, not the whole mod/client, real item loading,
real anvil GUI integration, or Forge's optional-interface stripping lifecycle.
"""
import argparse
import os
from pathlib import Path
import hashlib
import json
import re
import subprocess
import tempfile
import zipfile

parser = argparse.ArgumentParser()
parser.add_argument('--java-home', type=Path, required=True)
parser.add_argument('--classpath', required=True)
args = parser.parse_args()
root = Path(__file__).resolve().parents[2]
src = root / 'src/main/java/com/hbm'
tests = root / 'porting/tests'
# Test-only retained source bytes, independent of local Git history.
BASELINE_SHA256 = '9d44102bb5e663aaebc71bc2e13c404b79e23b4a1727dd920f1f7cfd01d19584'
fixture_bytes = (root / 'porting/tests/jei-optional-browser-baseline.json').read_bytes()
assert hashlib.sha256(fixture_bytes).hexdigest() == BASELINE_SHA256, 'Retained baseline fixture checksum mismatch'
baseline_sources = json.loads(fixture_bytes.decode('utf-8'))['sources']
for relative, retained in baseline_sources.items():
    encoded = retained['source_utf8'].encode('utf-8')
    assert len(encoded) == retained['byte_length'], 'Retained source byte length mismatch: ' + relative
    assert hashlib.sha256(encoded).hexdigest() == retained['sha256'], 'Retained source checksum mismatch: ' + relative


def executable(name):
    for suffix in ('.exe', '') if os.name == 'nt' else ('', '.exe'):
        candidate = args.java_home / 'bin' / (name + suffix)
        if candidate.is_file():
            return str(candidate)
    raise FileNotFoundError(name)

paths = args.classpath.split(os.pathsep)
def contains_nei(path):
    if not Path(path).is_file() or not zipfile.is_zipfile(path):
        return False
    with zipfile.ZipFile(path) as archive:
        return 'codechicken/nei/api/IConfigureNEI.class' in archive.namelist()

nei_paths = [p for p in paths if contains_nei(p)]
assert len(nei_paths) == 1, 'Expected exactly the pinned native NEI jar'
assert '1.12.2-2.4.3.245' in nei_paths[0], 'Differential oracle must use the pinned NEI build'
without_nei = os.pathsep.join(p for p in paths if p not in nei_paths)

fixtures = {
'config/ClientConfig.java': '''package com.hbm.config;
public final class ClientConfig {
    public static final Setting NEI_HIDE_SECRETS = new Setting();
    public static final class Setting { public boolean value; public boolean get() { return value; } }
}''',
'main/MainRegistry.java': '''package com.hbm.main;
public final class MainRegistry { public static int polaroidID; }''',
'items/ItemEnums.java': '''package com.hbm.items;
public final class ItemEnums {
    public enum EnumIngotMetal { A, B, C }
    public enum EnumSecretType { A, B }
}''',
'items/weapon/sedna/ItemGunBaseNT.java': '''package com.hbm.items.weapon.sedna;
import java.util.Arrays; import java.util.List; import net.minecraft.item.Item; import net.minecraft.init.Items;
public final class ItemGunBaseNT { public static final List<Item> secrets = Arrays.asList(Items.BOW, Items.ARROW); }''',
'items/weapon/sedna/factory/GunFactory.java': '''package com.hbm.items.weapon.sedna.factory;
public final class GunFactory { public enum EnumAmmoSecret { A, B, C, D } }''',
'items/machine/ItemBattery.java': '''package com.hbm.items.machine;
import net.minecraft.item.Item; import net.minecraft.item.ItemStack; import net.minecraft.nbt.NBTTagCompound;
public final class ItemBattery {
    public static ItemStack getEmptyBattery(Item item) { return charged(item, 0); }
    public static ItemStack getFullBattery(Item item) { return charged(item, 100); }
    private static ItemStack charged(Item item, int charge) {
        ItemStack stack = new ItemStack(item); NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("charge", charge); stack.setTagCompound(nbt); return stack;
    }
}''',
'items/special/ItemBedrockOreNew.java': '''package com.hbm.items.special;
import net.minecraft.item.ItemStack; import net.minecraft.init.Items; import net.minecraft.nbt.NBTTagCompound;
public final class ItemBedrockOreNew {
    public enum BedrockOreGrade { BASE, LOW, HIGH }
    public enum CelestialBedrockOreType { A, B }
    public static final class CelestialBedrockOre {
        public static CelestialBedrockOreType[] getAllTypes() { return CelestialBedrockOreType.values(); }
    }
    public static ItemStack make(BedrockOreGrade grade, CelestialBedrockOreType type) {
        ItemStack stack = new ItemStack(Items.QUARTZ); NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("grade", grade.name()); nbt.setString("type", type.name()); stack.setTagCompound(nbt); return stack;
    }
}''',
'config/CustomMachineConfigJSON.java': '''package com.hbm.config;
import java.util.ArrayList; import java.util.List;
public final class CustomMachineConfigJSON {
    public static final List<MachineConfiguration> niceList = new ArrayList<>();
    public static final class MachineConfiguration {
        public final String uid; public MachineConfiguration(String uid) { this.uid = uid; }
    }
}''',
'main/NEIRegistry.java': '''package com.hbm.main;
import java.util.ArrayList; import java.util.List; import com.hbm.handler.jei.MachineRecipeCategory;
public final class NEIRegistry {
    public static final List<MachineRecipeCategory> handlers = new ArrayList<>();
    public static List<MachineRecipeCategory> listAllHandlers() { return new ArrayList<>(handlers); }
}''',
'handler/nei/AnvilRecipeHandler.java': '''package com.hbm.handler.nei;
import java.util.Collections; import java.util.List;
import com.hbm.handler.jei.MachineRecipeCategory; import com.hbm.handler.jei.MachineRecipeView;
import mezz.jei.api.IModRegistry; import mezz.jei.api.recipe.IFocus;
import net.minecraft.item.ItemStack; import net.minecraft.init.Items;
public final class AnvilRecipeHandler extends MachineRecipeCategory {
    public int guiRegistrations;
    public String getUid() { return "fixture:anvil"; }
    public String getTitle() { return "Fixture anvil"; }
    public List<MachineRecipeView> query(IFocus<ItemStack> focus) { return Collections.emptyList(); }
    public ItemStack[] getMachines() { return new ItemStack[] {null, ItemStack.EMPTY, new ItemStack(Items.DIAMOND)}; }
    public void registerGuiAreas(IModRegistry registry) { guiRegistrations++; }
}''',
'handler/nei/CustomMachineHandler.java': '''package com.hbm.handler.nei;
import java.util.Collections; import java.util.List;
import com.hbm.config.CustomMachineConfigJSON.MachineConfiguration;
import com.hbm.handler.jei.MachineRecipeCategory; import com.hbm.handler.jei.MachineRecipeView;
import mezz.jei.api.recipe.IFocus; import net.minecraft.item.ItemStack;
public final class CustomMachineHandler extends MachineRecipeCategory {
    private final MachineConfiguration configuration;
    public CustomMachineHandler(MachineConfiguration configuration) { this.configuration = configuration; }
    public String getUid() { return configuration.uid; }
    public String getTitle() { return "Fixture custom machine"; }
    public List<MachineRecipeView> query(IFocus<ItemStack> focus) { return Collections.emptyList(); }
}''',
'handler/nei/AnvilOverlayHandler.java': '''package com.hbm.handler.nei;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.entity.player.EntityPlayer; import net.minecraft.inventory.Container;
public final class AnvilOverlayHandler implements IRecipeTransferHandler<Container> {
    public static int constructions;
    public AnvilOverlayHandler(AnvilRecipeHandler category, IRecipeTransferHandlerHelper helper) { constructions++; }
    public Class<Container> getContainerClass() { return Container.class; }
    public IRecipeTransferError transferRecipe(Container container, IRecipeLayout layout,
            EntityPlayer player, boolean maxTransfer, boolean doTransfer) { return null; }
}'''
}
# Generate only NTM registry fixture declarations; production policy is compiled
# whole and its behavior is compared with the independently retained baseline.
visibility = (src / 'handler/RecipeBrowserVisibility.java').read_text(encoding='utf-8')
items = sorted(set(re.findall(r'ModItems\.(\w+)', visibility)))
blocks = sorted(set(re.findall(r'ModBlocks\.(\w+)', visibility)))
fixtures['items/ModItems.java'] = '''package com.hbm.items;
import net.minecraft.item.Item;
public final class ModItems {
''' + ''.join('    public static final Item %s = new Item().setHasSubtypes(true);\n' % name for name in items) + '}\n'
fixtures['blocks/ModBlocks.java'] = '''package com.hbm.blocks;
import net.minecraft.block.Block; import net.minecraft.init.Blocks;
public final class ModBlocks {
''' + ''.join('    public static final Block %s = Blocks.STONE;\n' % name for name in blocks) + '}\n'

whole_files = [src / 'handler/jei' / (name + '.java') for name in (
    'MachineRecipeCategory', 'MachineRecipeView', 'NativeRecipeLookup', 'RecipeStackMatcher', 'NTMJeiPlugin')]
whole_files += [src / 'handler/RecipeBrowserVisibility.java', src / 'handler/imc/IMCHandlerNHNEI.java',
                src / 'lib/RefStrings.java', tests / 'JeiCoreNativeTest.java', tests / 'JeiOptionalBrowserNativeTest.java']
with tempfile.TemporaryDirectory(prefix='jei-optional-browser-native-') as temporary:
    temporary = Path(temporary)
    output = temporary / 'classes'; output.mkdir()
    for name, content in fixtures.items():
        path = temporary / 'fixture/com/hbm' / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content, encoding='utf-8'); whole_files.append(path)
    compile_flags = [executable('javac'), '-source', '8', '-target', '8', '-proc:none', '-implicit:none',
                     '-encoding', 'UTF-8', '-d', str(output)]
    subprocess.run(compile_flags + ['-cp', without_nei] + list(map(str, whole_files)), check=True)
    print('PASS: complete production plugin/core/matcher/visibility/catalyst sources compile with NEI jar removed.', flush=True)
    original = baseline_sources['main/NEIConfig.java']['source_utf8']
    original_file = temporary / 'OriginalNEIConfig.java'
    original_file.write_text(original.replace('public class NEIConfig ', 'public class OriginalNEIConfig '), encoding='utf-8')
    subprocess.run(compile_flags + ['-cp', str(output) + os.pathsep + args.classpath,
                                   str(original_file), str(src / 'main/NEIConfig.java'),
                                   str(tests / 'RecipeStackMatcherNativeTest.java'),
                                   str(tests / 'JeiWithoutNeiClassLoaderTest.java')], check=True)
    oracle_cp = str(output) + os.pathsep + args.classpath
    subprocess.run([executable('java'), '-cp', oracle_cp, 'RecipeStackMatcherNativeTest'], cwd=temporary, check=True)
    subprocess.run([executable('java'), '-cp', oracle_cp, 'JeiOptionalBrowserNativeTest', 'oracle'], cwd=temporary, check=True)
    scoped_cp = str(output) + os.pathsep + without_nei
    subprocess.run([executable('java'), '-cp', str(output), 'JeiWithoutNeiClassLoaderTest', scoped_cp], cwd=temporary, check=True)
print('Scope: complete scoped sources against isolated NTM collaborators, not full-mod/client or Forge optional GUI lifecycle.')
