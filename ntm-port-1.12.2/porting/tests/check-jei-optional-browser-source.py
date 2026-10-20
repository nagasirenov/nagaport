#!/usr/bin/env python3
"""Narrow source contract for removing incidental NEI links, not a native compile."""
from pathlib import Path
import hashlib
import json

root = Path(__file__).resolve().parents[2]
source = root / 'src/main/java/com/hbm'
# Test-only retained source bytes, independent of local Git history.
BASELINE_SHA256 = '9d44102bb5e663aaebc71bc2e13c404b79e23b4a1727dd920f1f7cfd01d19584'
fixture_bytes = (root / 'porting/tests/jei-optional-browser-baseline.json').read_bytes()
assert hashlib.sha256(fixture_bytes).hexdigest() == BASELINE_SHA256, 'Retained baseline fixture checksum mismatch'
baseline_sources = json.loads(fixture_bytes.decode('utf-8'))['sources']
for relative, retained in baseline_sources.items():
    encoded = retained['source_utf8'].encode('utf-8')
    assert len(encoded) == retained['byte_length'], 'Retained source byte length mismatch: ' + relative
    assert hashlib.sha256(encoded).hexdigest() == retained['sha256'], 'Retained source checksum mismatch: ' + relative

checks = 0

def original(relative):
    return baseline_sources[relative]['source_utf8'].replace('\r\n', '\n')

def check(condition, description):
    global checks
    assert condition, description
    checks += 1

changed = []
used = []
for relative in sorted(key for key in baseline_sources if key.startswith('handler/nei/')):
    path = source / relative
    old = original(relative)
    if 'import codechicken.nei.util.NEIServerUtils;' not in old:
        continue
    changed.append(path.name)
    if 'NEIServerUtils.' in old:
        used.append(path.name)
        expected = old.replace('import codechicken.nei.util.NEIServerUtils;',
                               'import com.hbm.handler.jei.RecipeStackMatcher;')
        expected = expected.replace('NEIServerUtils.areStacksSameTypeCrafting(', 'RecipeStackMatcher.matchesCrafting(')
        expected = expected.replace('NEIServerUtils.areStacksSameType(', 'RecipeStackMatcher.matchesType(')
    else:
        expected = old.replace('import codechicken.nei.util.NEIServerUtils;\n', '')
    check(path.read_text(encoding='utf-8') == expected, 'Only comparator import/call replacement: ' + path.name)
check(len(changed) == 20 and len(used) == 17, 'Exact 20 imports, 17 callers, 3 unused imports')


expected_plugin = original('handler/jei/NTMJeiPlugin.java')
expected_plugin = expected_plugin.replace('import com.hbm.config.VersatileConfig;\n', '')
expected_plugin = expected_plugin.replace('import net.minecraft.item.ItemStack;\n', '')
expected_plugin = expected_plugin.replace('com.hbm.main.NEIConfig.configureHiddenItems',
                                          'com.hbm.handler.RecipeBrowserVisibility.configureHiddenItems')
check((source / 'handler/jei/NTMJeiPlugin.java').read_text(encoding='utf-8') == expected_plugin,
      'Plugin changes only shared-policy delegation and two unused imports')

old_visibility = original('main/NEIConfig.java')
policy = old_visibility[old_visibility.index('    /** Both supported'):old_visibility.index('    @Override public String getName()')]
normalized_policy = '\n'.join(line.rstrip() for line in policy.splitlines())
normalized_new = '\n'.join(line.rstrip() for line in (source / 'handler/RecipeBrowserVisibility.java').read_text(encoding='utf-8').splitlines())
check(normalized_policy in normalized_new, 'Shared visibility policy moved unchanged apart from trailing whitespace')
check('implements IConfigureNEI' in (source / 'main/NEIConfig.java').read_text(encoding='utf-8'), 'Optional NEI adapter remains native')
for relative in ('main/NEIConfig.java', 'handler/jei/NTMJeiPlugin.java'):
    check('RecipeBrowserVisibility.configureHiddenItems(' in (source / relative).read_text(encoding='utf-8'),
          'Both browsers delegate to shared policy: ' + relative)
for relative in ('handler/RecipeBrowserVisibility.java', 'handler/jei/NTMJeiPlugin.java', 'handler/jei/RecipeStackMatcher.java'):
    text = (source / relative).read_text(encoding='utf-8')
    check('codechicken.nei' not in text and 'NEIConfig.' not in text, 'No incidental optional NEI link: ' + relative)
check((source / 'inventory/gui/GuiInfoContainer.java').read_text(encoding='utf-8') == original('inventory/gui/GuiInfoContainer.java'),
      'Existing optional NEI GUI integration unchanged; Forge stripping not proven here')
print('PASS:', checks, 'optional-browser source contracts; exact 20-file comparator-only delta, unchanged visibility and optional GUI.')
