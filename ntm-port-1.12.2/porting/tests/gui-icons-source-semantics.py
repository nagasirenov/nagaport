#!/usr/bin/env python3
"""Ordered call/vertex expression audit, not runtime proof."""
import importlib.util,re,pathlib
p=pathlib.Path(__file__).with_name('check-native-gui-icons-source.py');s=importlib.util.spec_from_file_location('guard',p);g=importlib.util.module_from_spec(s);s.loader.exec_module(g)
normalize=lambda x:re.sub(r'\s+','',x)
calls=0
for rel in g.FILES:
 after=(g.ROOT/rel).read_text();before=g.reverse_source(rel,after)
 old=re.findall(r'ItemRenderer\.renderItemIn2D\(([^;]+)\);',before)
 new=re.findall(r'NativeIconExtrusion\.draw\(([^;]+)\);',after)
 assert [normalize(x.split(',',1)[1]) for x in old]==[normalize(x) for x in new],rel
 calls+=len(old)
 if rel.endswith('ActorFancyPanel.java'):
  old=re.findall(r'tessellator\.addVertex(?:WithUV)?\(([^;]+)\);',before)
  new=re.findall(r'tessellator\.pos\((.*?)\)(?:\.tex\((.*?)\))?\.color\([^;]+?\.endVertex\(\);',after)
  assert len(old)==len(new)==8
  assert [normalize(x) for x in old]==[normalize(a+(','+b if b else '')) for a,b in new]
assert calls==10
print('PASS ten extrusion argument tuples and eight ordered GUI vertex expressions unchanged. Source evidence, not GL/runtime proof.')
