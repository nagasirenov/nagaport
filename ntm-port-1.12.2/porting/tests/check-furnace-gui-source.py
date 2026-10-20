#!/usr/bin/env python3
"""Exact three-file GUI migration bridge, BEFORE checkpoint 17 EMPTY reversal.
No Git object dependency. Rejects any current drift before returning original bytes.
Also audits the actual lowercase stored resource name and pre-migration byte hash.
"""
from pathlib import Path
import hashlib, importlib.util, json, re
ROOT = Path(__file__).resolve().parents[2]
MANIFEST = json.loads((ROOT / 'porting/tests/furnace-gui-source-manifest.json').read_text(encoding='utf-8'))
FILES = {e['path']: e for e in MANIFEST['files']}
def sha(text):
    return hashlib.sha256(text.encode('utf-8')).hexdigest()
def reverse_source(relative, text):
    entry = FILES.get(relative)
    if entry is None:
        return text
    assert sha(text) == entry['after_sha256'], (relative, 'GUI native source drift')
    assert sha(entry['before_source']) == entry['before_sha256'], (relative, 'GUI frozen baseline corrupt')
    return entry['before_source']
def audit_closure(source_transform):
    sources = {p.relative_to(ROOT).as_posix(): source_transform(p.relative_to(ROOT).as_posix(), p.read_bytes().decode('utf-8')) for p in (ROOT / 'src/main/java').rglob('*.java')}
    parents = {}
    paths = {}
    for path, text in sources.items():
        for match in re.finditer(r'\bclass\s+(\w+)\s+extends\s+(\w+)', text):
            parents[match[1]] = match[2]
            paths[match[1]] = path
    descendants = {'GuiInfoContainer'}
    while True:
        expanded = descendants | {name for name, parent in parents.items() if parent in descendants}
        if expanded == descendants:
            break
        descendants = expanded
    actual = sorted({paths[name] for name in descendants})
    assert actual == MANIFEST['gui_descendant_files'], 'GUI descendant closure changed; audit new tooltip layering before updating manifest'
    # There were no preexisting implementations/calls of the native hover method.
    # The one added base call must stay the sole call, preventing duplicate slots.
    hover = [(path, text.count('renderHoveredToolTip(')) for path, text in sources.items() if 'renderHoveredToolTip(' in text]
    assert hover == [('src/main/java/com/hbm/inventory/gui/GuiInfoContainer.java', 1)], hover
    for path in actual:
        text = sources[path]
        for name in ('renderToolTip', 'getItemToolTip', 'drawHoveringText'):
            assert not re.search(r'\b(?:public|protected|private)\s+[^;{}\n]+\b' + name + r'\s*\(', text), (path, 'new override requires dispatch/layer audit', name)
    return len(actual)
def audit_render_parity(source_transform):
    for relative in ('src/main/java/com/hbm/inventory/gui/GuiInfoContainer.java', 'src/main/java/com/hbm/inventory/gui/GUIMachineElectricFurnace.java'):
        before = FILES[relative]['before_source']
        after = source_transform(relative, (ROOT / relative).read_bytes().decode('utf-8'))
        # These calls retain their exact source ordering, expressions and colors.
        for pattern in (r'(?:GL11|RenderHelper|OpenGlHelper)\.[^;]+;', r'(?:this\.)?drawGradientRect\([^;]+;', r'(?<!\w)drawTexturedModalRect\([^;]+;', r'(?:this\.)?zLevel\s*=\s*[^;]+;', r'itemRender\.zLevel\s*=\s*[^;]+;'):
            assert re.findall(pattern, before) == re.findall(pattern, after), (relative, 'GL/rectangle/layer source drift', pattern)
    relative = 'src/main/java/com/hbm/inventory/gui/GuiInfoContainer.java'
    before = FILES[relative]['before_source']
    after = source_transform(relative, (ROOT / relative).read_bytes().decode('utf-8'))
    signatures = lambda text: set(re.findall(r'^\s*((?:public|protected) [^;{}\n]+\([^;{}\n]*\))\s*\{', text, re.M))
    assert signatures(after) - signatures(before) == {'public void drawScreen(int mouseX, int mouseY, float partialTicks)'}, 'Unexpected helper signature addition/change'
    assert not signatures(before) - signatures(after), 'Existing helper signature removed/changed'
    assert 'super.drawScreen(mouseX, mouseY, partialTicks);\n\t\t// In 1.7.10 GuiContainer did this at the end of drawScreen itself.\n\t\tthis.renderHoveredToolTip(mouseX, mouseY);' in after.replace('\r\n', '\n'), 'Tooltip dispatch/layer order changed'
    assert after.count('itemRender.renderItemAndEffectIntoGUI(stack,') == 3
    assert after.count('itemRender.renderItemOverlayIntoGUI(') == 2
    assert 'if(stack.getCount() == 0)' in after
    assert '0xffff0000, 0xffff0000' in after and '0xffb0b0b0, 0xffb0b0b0' in after

def main(source_transform=None):
    if source_transform is None:
        # Keep reverse_source pure so aggregate histories compose each layer once.
        # Load once per standalone invocation, not once per descendant source.
        spec = importlib.util.spec_from_file_location('native_render_compat_gui', ROOT / 'porting/tests/check-thermal-family-source.py')
        module = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(module)
        source_transform = module.reverse_integration_source
    for relative in FILES:
        text = source_transform(relative, (ROOT / relative).read_bytes().decode('utf-8'))
        reverse_source(relative, text)
        try:
            reverse_source(relative, text + '\n')
        except AssertionError:
            pass
        else:
            raise AssertionError((relative, 'drift accepted'))
    texture = MANIFEST['texture']
    stored = {p.relative_to(ROOT).as_posix(): p for p in (ROOT / 'src/main/resources/assets/hbm/textures/gui').iterdir() if p.is_file()}
    assert texture['before'] not in stored and texture['after'] in stored, 'Texture case-only rename not stored canonically'
    assert hashlib.sha256(stored[texture['after']].read_bytes()).hexdigest() == texture['sha256'], 'Furnace PNG bytes changed'
    aliases = []
    for path in (ROOT / 'src/main').rglob('*'):
        if path.is_file() and path.suffix in ('.java', '.json', '.mcmeta', '.xml') and 'guielectricfurnace.png' in path.read_text(encoding='utf-8', errors='replace').lower():
            aliases.append(path.relative_to(ROOT).as_posix())
    assert sorted(aliases) == texture['source_alias_files'], ('new resource aliases need audit', aliases)
    audit_render_parity(source_transform)
    count = audit_closure(source_transform)
    print('PASS three exact GUI whole-file reversals/drift rejections; %d-file descendant closure; native hover dispatch; sole resource alias; original PNG hash and stored lowercase name' % count)
if __name__ == '__main__':
    main()
