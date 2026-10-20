#!/usr/bin/env python3
"""Frozen-source contract, complementary to (never a substitute for) the native execution proof."""
import hashlib,json,pathlib,re
here=pathlib.Path(__file__).resolve().parent
root=here.parents[2];src=root/'src/main/java';old=root.parent/'ntm-original-pristine/src/main/java'
manifest=json.loads((here/'source-manifest.json').read_text())
for group,base in [('native',src),('pristine',old)]:
 for name,sha in manifest[group].items():
  assert hashlib.sha256((base/name).read_bytes()).hexdigest()==sha,group+' source changed: '+name

def text(base,name):return (base/name).read_text()
def compact(s):return re.sub(r'\s+','',s)
def method(s,signature):
 at=s.index('{',s.index(signature));end=at+1;depth=1
 while depth:
  depth+=(s[end]=='{')-(s[end]=='}');end+=1
 return s[at+1:end-1]
for name in ['com/hbm/wiaj/GuiWorldInAJar.java','com/hbm/inventory/gui/GUIScreenSlicePrinter.java']:
 assert compact(method(text(old,name),'private void setupRotation()'))==compact(method(text(src,name),'private void setupRotation()')),name+' transform drift'
name='com/hbm/wiaj/JarScript.java'
assert text(src,name)==text(old,name).replace('net.minecraft.util.MathHelper','net.minecraft.util.math.MathHelper').replace('MathHelper.clamp_float','MathHelper.clamp'),'only native MathHelper API mappings allowed in real JarScript'
name='com/hbm/render/util/RenderOverhead.java';a=text(old,name);b=text(src,name)
for signature in ['public static void clearActionPreview()','public static void setActionPreview(', 'public static class Marker']:
 assert compact(method(a,signature))==compact(method(b,signature)),signature+' drift'
a_vertices=re.findall(r'(?:tessellator|tess)\.addVertex\(([^;]+)\);',a)
b_vertices=re.findall(r'buffer\.pos\(([^;]+)\)\.color\([^;]+\)\.endVertex\(\);',b)
assert [compact(x) for x in a_vertices]==[compact(x) for x in b_vertices] and len(a_vertices)==52,'tag/thermal/marker coordinate/order drift'
assert 'if(INtmBossDisplay.isDisplayBoss(ent))' in b and 'isNonBoss' not in b,'coordinator-owned display predicate required, no gameplay surrogate'
for signature in ['public static void renderMarkers','public static void renderActionPreview']:
 for needle in (['marker.expire > 0 && Clock.get_ms() > marker.expire','marker.maxDist > 0','vec.length() > marker.maxDist'] if 'Markers' in signature else ['if(clearPreview)', 'actionPreviewWorld = null;', 'clearPreview = false;', 'if(actionPreviewWorld == null) return;', 'actionPreviewWorld.lightlevel = 15;']):
  # Signature pins the relevant full callback; hashes above prohibit extra/reordered code.
  assert needle in method(b,signature)
name='com/hbm/inventory/gui/GUIScreenSlicePrinter.java';a=text(old,name);b=text(src,name)
start='int dx = x;';end='renderer.renderBlockByRenderType'
assert compact(a[a.index(start):a.index(end)])==compact(b[b.index(start):b.index('// Original RenderBlocks')]),'original swizzle drift'
assert 'NativeSceneBlockDraw.sliceState(block, mc.world, destination)' in b
name='com/hbm/render/util/NativeSceneBlockDraw.java';b=text(src,name)
assert b.index('source.finishDrawing()')<b.index('ByteBuffer data = source.getByteBuffer()') and 'DefaultVertexFormats.BLOCK.equals' in b
assert '.addElement(DefaultVertexFormats.COLOR_4UB)' not in b and '.noColor()' not in re.sub(r'/\*.*?\*/','',b,flags=re.S)
print('PASS frozen native/pristine hashes, complete JarScript API-only comparison, exact transforms, 52 ordered vertex expressions, unchanged marker/preview mutators, display-only boss predicate, destination metadata/swizzle and native colorless upload contract. Runtime proof remains separate.')
