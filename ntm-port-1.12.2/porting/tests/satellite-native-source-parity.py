#!/usr/bin/env python3
"""Scoped Satellite source/slot parity proof; not a Java compile or game test."""
from pathlib import Path
import hashlib
import subprocess

root = Path(__file__).resolve().parents[2]
relative = 'src/main/java/com/hbm/handler/nei/SatelliteHandler.java'
old = subprocess.check_output(['git', 'show', '5f720e0:./' + relative], cwd=root, text=True)
new = (root / relative).read_text()
checks = 0

def check(condition, description):
    global checks
    assert condition, description
    checks += 1

check('extends MachineRecipeCategory' in new, 'Native category')
for obsolete in ('TemplateRecipeHandler', 'PositionedStack', 'ICompatNHNEI', 'codechicken.lib.gui'):
    check(obsolete not in new, 'No legacy recipe UI: ' + obsolete)
for expression in ('SatelliteMiner.getCargoForItem(satelliteItem)',
                   'ItemPool.getPool(poolName)',
                   'ItemPool.getPool(ItemPoolsSatellite.POOL_SAT_MINER)',
                   'ItemPool.getPool(ItemPoolsSatellite.POOL_SAT_LUNAR)',
                   'poolEntry.theItemId.copy()',
                   'Arrays.stream(poolEntries).mapToInt(poolEntry -> poolEntry.itemWeight).sum()',
                   '100F * poolEntry.itemWeight / weight',
                   '((int)(chance * 10F) / 10F)',
                   '"ntmSatellite"', '"Satellite"'):
    check(expression in old and expression in new, 'Preserved source expression: ' + expression)
check('RecipeStackMatcher.matchesCrafting(poolEntry.theItemId, focus.getValue())' in new,
      'Native-equivalent crafting comparator, applied before tooltip mutation')
check('item == ModItems.sat_miner' in new and 'item == ModItems.sat_lunar_miner' in new,
      'Usage remains item-only')
check('new MachineRecipeView(this, poolItem,' in new, 'Satellite item is source identity')
check('view.addDecoration(new ItemStack[] {new ItemStack(ModBlocks.sat_dock)}, 30, 31)' in new,
      'Dock decoration remains at original position')
check('view.addInputSlot(0, 12, 24)' in new, 'Original input position')
for rect in ('11, 23, 113, 105, 18, 18', '47, 5, 5, 87, 108, 54', '29, 14, 131, 96, 18, 36'):
    check(rect in old and rect in new, 'Original texture rectangle: ' + rect)
check('transferRects' not in old and 'loadTransferRects' not in old,
      'Original has no recipe or GUI click areas')
check('return false;' in new and 'super.handleClick' not in new,
      'No invented recipe-local category hotspot')
# Exhaustively compare original output overflow grouping (after PositionedStack
# ignores null array padding) to native lists, including exact multiples of 18.
for size in range(257):
    original = []
    replacement = []
    for i in range(min(size, 18)):
        original.append([j * 18 + i for j in range(size // 18 + 1) if j * 18 + i < size])
        replacement.append(list(range(i, size, 18)))
    check(original == replacement, 'Overflow groups: ' + str(size))
    check(sorted(index for slot in replacement for index in slot) == list(range(size)),
          'No omitted or duplicated source entries: ' + str(size))
check('48 + 18 * (i % 6), 6 + 18 * (i / 6)' in new, 'Six-column output geometry')
print('PASS:', checks, 'scoped source/overflow assertions')
print('Baseline: 5f720e0f82340aa29f2613ff71fc706689d7c3aa')
print('SatelliteHandler SHA-256:', hashlib.sha256((root / relative).read_bytes()).hexdigest())
print('Scope: source-contract and mathematical slot grouping only; no complete-class compilation, OpenGL, or full-game claim.')
