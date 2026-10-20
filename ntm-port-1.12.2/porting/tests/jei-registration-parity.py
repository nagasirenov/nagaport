#!/usr/bin/env python3
"""Source-contract check only: category coverage and legacy GUI-coordinate translation.
Not a Java compile, recipe-query test, or in-game interaction test.
"""
from pathlib import Path
import ast
import operator
import re

root = Path(__file__).resolve().parents[2]
legacy = root.parent / 'ntm-original-pristine'
checks = 0

def check(value, description):
    global checks
    checks += 1
    if not value:
        raise AssertionError(description)

def read(base, suffix):
    return (base / 'src/main/java/com/hbm' / suffix).read_text()

def uncomment(text):
    return re.sub(r'/\*.*?\*/|//[^\n]*', '', text, flags=re.S)

ops = {ast.Add: operator.add, ast.Sub: operator.sub, ast.Mult: operator.mul, ast.Div: operator.floordiv}
def number(expression):
    def visit(node):
        if isinstance(node, ast.Constant) and isinstance(node.value, int): return node.value
        if isinstance(node, ast.BinOp) and type(node.op) in ops: return ops[type(node.op)](visit(node.left), visit(node.right))
        if isinstance(node, ast.UnaryOp) and isinstance(node.op, ast.USub): return -visit(node.operand)
        raise AssertionError('Unexpected coordinate expression: ' + expression)
    return visit(ast.parse(expression.strip(), mode='eval').body)

old_registry = read(legacy, 'main/NEIRegistry.java')
new_registry = read(root, 'main/NEIRegistry.java')
old_names = re.findall(r'handlers.add\(new (\w+)\(', old_registry)
new_names = re.findall(r'handlers.add\(new (\w+)\(', new_registry)
check(len(old_names) == 72, 'original category inventory')
check(new_names == old_names, 'all built-in categories and original registration order')
check('if(VersatileConfig.rtgDecay()) handlers.add(new RTGRecipeHandler());' in new_registry, 'RTG condition')
uid_set = set()
for name in new_names:
    old = uncomment(read(legacy, 'handler/nei/' + name + '.java'))
    new = uncomment(read(root, 'handler/nei/' + name + '.java'))
    match = re.search(r'getUid\(\)\s*\{\s*return "([^"]+)"', new)
    check(match is not None, name + ' native UID')
    uid = match.group(1)
    check(uid not in uid_set, name + ' unique category UID')
    uid_set.add(uid)
    # These are GUI-relative rectangles, not the separate recipe-wrapper rectangles.
    rectangles = re.findall(r'transferRectsGui.add\(new RecipeTransferRect\(new Rectangle\((.*?)\), "([^"]+)"\)\);', old)
    guis = re.findall(r'guiGui.add\((\w+)\.class\)', old)
    if not guis:
        gui = re.search(r'getGuiClass\(\)\s*\{\s*return (\w+)\.class;', old)
        if gui:
            guis = [gui.group(1)]
            rectangles = re.findall(r'transferRects.add\(new RecipeTransferRect\(new Rectangle\((.*?)\), "([^"]+)"\)\);', old)
    expected = []
    for gui in guis:
        for coordinates, destination in rectangles:
            x, y, w, h = map(number, coordinates.split(','))
            expected.append((gui, x + 5, y + 11, w, h))
    actual = []
    for gui, coordinates in re.findall(r'addRecipeClickArea\((\w+)\.class, (.*?), (?:getUid\(\)|"[^"]+")\)', new):
        actual.append((gui, *map(number, coordinates.split(','))))
    check(actual == expected, name + ' exact GUI areas with legacy (5,11) offset: ' + repr((expected, actual)))

plugin = read(root, 'handler/jei/NTMJeiPlugin.java')
check('categories.addAll(com.hbm.main.NEIRegistry.listAllHandlers())' in plugin, 'native plugin consumes full registry')
check('CustomMachineConfigJSON.niceList' in plugin and 'new CustomMachineHandler(configuration)' in plugin, 'all dynamic configurations registered')
custom = read(root, 'handler/nei/CustomMachineHandler.java')
check('return "ntm_" + conf.unlocalizedName;' in custom, 'configuration-specific UID')
check('createBlankDrawable(166, 84)' in custom, 'custom pollution label y75 not clipped')
for p in (root / 'src/main/java/com/hbm/handler/nei').glob('*.java'):
    check(not re.search(r'TemplateRecipeHandler|PositionedStack|RecipeTransferRect|\barecipes\b', p.read_text()), p.name + ' no obsolete framework')
print('PASS:', checks, 'source-contract assertions; 72 ordered unique native categories; exact machine-GUI area origin translation; dynamic config coverage. No compile/gameplay claim.')
