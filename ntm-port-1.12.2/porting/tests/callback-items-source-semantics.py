#!/usr/bin/env python3
"""Verify provider migration changes types only; inventory callbacks stay matrix-only."""
import importlib.util,pathlib,re
p=pathlib.Path(__file__).with_name('check-native-callback-items-source.py');s=importlib.util.spec_from_file_location('guard',p);g=importlib.util.module_from_spec(s);s.loader.exec_module(g)
providers=0
for rel in g.FILES:
 after=(g.ROOT/rel).read_bytes().decode();before=g.reverse_source(rel,after)
 if 'public IItemRenderer getRenderer()' not in before:continue
 expected=before.replace('public IItemRenderer getRenderer()','public ItemRenderBase getRenderer()').replace('import net.minecraftforge.client.IItemRenderer;', 'import com.hbm.render.item.ItemRenderBase;' if 'import com.hbm.render.item.ItemRenderBase;' not in before else '')
 assert g._newer.reverse_source(rel,after)==expected,(rel,'unexpected provider body edit');providers+=1
assert providers==74
count=0
allowed={'GL11.glTranslated','GL11.glScaled','GL11.glRotated','GL11.glRotatef','GL11.glScalef','setupRenderInv','setupRenderNonInv','if','System.currentTimeMillis'}
for p in (g.ROOT/'src/main/java').rglob('*.java'):
 text=p.read_text()
 if 'new ItemRenderBase' not in text and 'extends ItemRenderBase' not in text:continue
 count+=len(re.findall(r'new ItemRenderBase\s*\(',text))
 for m in re.finditer(r'public void (renderInventory|renderNonInv)\(\)\s*\{',text):
  i=m.end();start=i;depth=1
  while depth:
   depth+=(text[i]=='{')-(text[i]=='}');i+=1
  body=re.sub(r'//[^\n]*|/\*.*?\*/','',text[start:i-1],flags=re.S)
  assert set(re.findall(r'([\w.]+)\s*\(',body))<=allowed,(str(p),'non-matrix callback must not be captured')
assert count==90
armor=(g.ROOT/'src/main/java/com/hbm/items/armor/ArmorFSB.java').read_text()
for name in ('setupRenderInv','setupRenderNonInv'):
 m=re.search(r'public static void '+name+r'\(\)\s*\{([^}]+)\}',armor);assert m
 assert set(re.findall(r'([\w.]+)\s*\(',m.group(1))) <= {x for x in allowed if x.startswith('GL11.')}
print('PASS74exact provider/interface type-only migrations;90anonymous renderer definitions retained; inventory/noninventory callbacks and armor helpers audited matrix-only. Not full body compilation/runtime proof.')
