#!/usr/bin/env python3
"""One count-guarded, exact-whitelist migration for brick/iron/steel furnace GUIs.
Default: verify full current source, every reversible replacement, resource bytes,
and frozen originals. --apply is a one-time operation on the pre-port sources.
"""
import argparse, hashlib, json, re, subprocess
from pathlib import Path
ROOT = Path(__file__).resolve().parents[2]
MANIFEST_PATH = ROOT / 'porting/tests/thermal-gui-source-manifest.json'
WHITELIST = ['src/main/java/com/hbm/inventory/gui/GUIFurnace%s.java' % kind for kind in ('Brick', 'Iron', 'Steel')]
COMMON = [
    ('hasCustomInventoryName()', 'hasCustomName()', 1),
    ('getInventoryName()', 'getName()', 2),
    ('fontRendererObj', 'fontRenderer', 3),
]
RULES = {path: list(COMMON) for path in WHITELIST}
RULES[WHITELIST[0]] += [('furnace.getWorldObj().getTileEntity(furnace.xCoord, furnace.yCoord, furnace.zCoord)', 'furnace.getWorld().getTileEntity(furnace.getPos())', 2)]
RULES[WHITELIST[1]] += [
    ('this.mc.thePlayer.inventory.getItemStack() == null', 'this.mc.player.inventory.getItemStack().isEmpty()', 1),
    ('this.func_146283_a(bonuses, x, y)', 'this.drawHoveringText(bonuses, x, y)', 1),
]
def sha(text):
    return hashlib.sha256(text.encode('utf-8')).hexdigest()
def load_manifest():
    return json.loads(MANIFEST_PATH.read_text(encoding='utf-8'))
def reverse_source(relative, text):
    manifest = load_manifest()
    entry = next((e for e in manifest['files'] if e['path'] == relative), None)
    if entry is None:
        return text
    assert sha(text) == entry['after_sha256'], (relative, 'thermal GUI full-source drift')
    for old, new, count in reversed(RULES[relative]):
        assert text.count(new) == count, (relative, 'reverse count', new)
        text = text.replace(new, old)
    assert sha(text) == entry['before_sha256'], (relative, 'reversal hash')
    assert text == entry['before_source'], (relative, 'reversal exact bytes')
    return text
def apply():
    assert not MANIFEST_PATH.exists(), 'Refusing to overwrite an existing migration manifest'
    entries = []
    # Validate and transform the ENTIRE whitelist before touching any source.
    for relative in WHITELIST:
        before = (ROOT / relative).read_bytes().decode('utf-8')
        after = before
        for old, new, count in RULES[relative]:
            assert after.count(old) == count, (relative, old, 'expected', count, 'actual', after.count(old))
            assert new not in after.replace(old, ''), (relative, 'target token already present', new)
            after = after.replace(old, new)
        entries.append(dict(path=relative, before_source=before, before_sha256=sha(before), after_sha256=sha(after), rules=RULES[relative]))
    textures = []
    for kind in ('brick', 'iron', 'steel'):
        relative = 'src/main/resources/assets/hbm/textures/gui/processing/gui_furnace_%s.png' % kind
        path = ROOT / relative
        assert path.name in [p.name for p in path.parent.iterdir()], (relative, 'exact stored case')
        textures.append(dict(path=relative, sha256=hashlib.sha256(path.read_bytes()).hexdigest()))
    manifest = dict(baseline=subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=ROOT, text=True).strip(), whitelist=WHITELIST, files=entries, textures=textures)
    MANIFEST_PATH.write_text(json.dumps(manifest, indent=2) + '\n', encoding='utf-8')
    for entry in entries:
        after = entry['before_source']
        for old, new, count in RULES[entry['path']]:
            after = after.replace(old, new)
        (ROOT / entry['path']).write_bytes(after.encode('utf-8'))
def check():
    manifest = load_manifest()
    assert manifest['whitelist'] == WHITELIST
    assert [e['path'] for e in manifest['files']] == WHITELIST
    for entry in manifest['files']:
        relative = entry['path']; current = (ROOT / relative).read_bytes().decode('utf-8')
        assert entry['rules'] == [list(r) for r in RULES[relative]], (relative, 'rule whitelist drift')
        before = reverse_source(relative, current)
        for pattern in (r'(?:GL11|Minecraft)\.[^;]+;', r'(?<!\w)drawTexturedModalRect\([^;]+;', r'drawCustomInfoStat\([^;]+;', r'"(?:[^"\\]|\\.)*"', r'\b\d+(?:\.\d+)?(?:F|D|L)?\b'):
            assert re.findall(pattern, before) == re.findall(pattern, current), (relative, 'coordinate/text/GL/arithmetic parity', pattern)
        try:
            reverse_source(relative, current + '\n')
        except AssertionError:
            pass
        else:
            raise AssertionError((relative, 'full-file drift accepted'))
    for entry in manifest['textures']:
        path = ROOT / entry['path']
        assert path.name in [p.name for p in path.parent.iterdir()]
        assert path.name == path.name.lower()
        assert hashlib.sha256(path.read_bytes()).hexdigest() == entry['sha256']
    base = (ROOT / 'src/main/java/com/hbm/inventory/gui/GuiInfoContainer.java').read_text()
    assert base.count('this.renderHoveredToolTip(mouseX, mouseY);') == 1
    for relative in WHITELIST:
        assert 'renderHoveredToolTip(' not in (ROOT / relative).read_text(), 'No duplicate inherited native tooltip'
    print('PASS thermal GUI: three exact count-guarded full-file reversals and drift rejections; original text, coordinates, arithmetic and GL calls; unchanged exact-case texture hashes; single inherited native slot tooltip')
if __name__ == '__main__':
    parser = argparse.ArgumentParser(); parser.add_argument('--apply', action='store_true'); args = parser.parse_args()
    if args.apply:
        apply()
    check()
