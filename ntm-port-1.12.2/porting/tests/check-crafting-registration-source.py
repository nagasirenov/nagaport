#!/usr/bin/env python3
"""Prove the MineralRecipes migration changes registrations, not recipe expressions."""
import hashlib
import json
from pathlib import Path
import re
ROOT = Path(__file__).resolve().parents[2]
manifest = json.loads((ROOT / 'porting/tests/crafting-registration-boundary-manifest.json').read_text())
source = (ROOT / 'src/main/java/com/hbm/crafting/MineralRecipes.java').read_text()
restored = []
counts = {k: 0 for k in manifest['site_counts']}
for line in source.splitlines():
    match = re.search(r'NtmCraftingRegistry\.(shaped|shapeless)\("mineral/site_\d+", (true|false), ', line)
    if match:
        shape, ore = match.groups()
        counts[('ore_' if ore == 'true' else 'normal_') + shape] += 1
        if ore == 'true':
            line = line[:match.start()] + 'GameRegistry.addRecipe(new ' + ('ShapedOreRecipe' if shape == 'shaped' else 'ShapelessOreRecipe') + '(' + line[match.end():]
            end = line.rfind(');')
            line = line[:end] + '));' + line[end + 2:]
        else:
            line = line[:match.start()] + 'GameRegistry.' + ('addRecipe' if shape == 'shaped' else 'addShapelessRecipe') + '(' + line[match.end():]
    if not line.startswith('import '): restored.append(line)
assert counts == manifest['site_counts'], counts
assert hashlib.sha256('\n'.join(restored).encode()).hexdigest() == manifest['mineral_original_nonimports_sha256']
print('PASS all 269 Mineral registrations invert exactly to CP55 non-import source:', counts)
main = (ROOT / 'src/main/java/com/hbm/main/MainRegistry.java').read_text()
assert main.count('NtmCraftingTables.schedule(CraftingManager::mainRegistry)') == 1
assert main.count('NtmCraftingTables.schedule(OreDictManager::registerOres)') == 1
assert main.index('NtmCraftingTables.schedule(CraftingManager::mainRegistry)') < main.index('NtmCraftingTables.schedule(OreDictManager::registerOres)')
assert main.index('MinecraftForge.EVENT_BUS.register(oreMan)') < main.index('OreDictManager.registerGroups()') < main.index('NtmCraftingTables.schedule(OreDictManager::registerOres)')
print('PASS crafting and ore producer callbacks retained in order; ore listener/groups remain preInit')

manager = (ROOT / 'src/main/java/com/hbm/main/CraftingManager.java').read_text()
# Nine custom producers are separately frozen by the handler and dynamic source checks.
manager = re.sub(r'NtmCraftingRegistry\.special\("[^\"]+", ', 'GameRegistry.addRecipe(', manager)
for current, old in {
    'NtmCraftingRegistry.shaped("auto/ore_shaped", true, result, ins);': 'GameRegistry.addRecipe(new ShapedOreRecipe(result, ins));',
    'NtmCraftingRegistry.shaped("auto/shaped", false, result, ins);': 'GameRegistry.addRecipe(result, ins);',
    'NtmCraftingRegistry.shapeless("auto/ore_shapeless", true, result, ins);': 'GameRegistry.addRecipe(new ShapelessOreRecipe(result, ins));',
    'NtmCraftingRegistry.shapeless("auto/shapeless", false, result, ins);': 'GameRegistry.addShapelessRecipe(result, ins);',
}.items():
    assert manager.count(current) == 1
    manager = manager.replace(current, old)
manager = '\n'.join(line for line in manager.splitlines() if not line.startswith('import '))
assert hashlib.sha256(manager.encode()).hexdigest() == manifest['crafting_manager_original_nonimports_sha256']
print('PASS every CraftingManager recipe expression/body unchanged after reversing four helper branches and nine separately frozen custom registrations')
