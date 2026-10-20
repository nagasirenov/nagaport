#!/usr/bin/env python3
"""Portable exact structural source/resource and narrow parent integration guard."""
import pathlib,json,hashlib
root=pathlib.Path(__file__).resolve().parents[2]
scope=json.loads((root/'porting/tests/structural-production-scope.json').read_text())
for name,digest in scope['files'].items():
 assert hashlib.sha256((root/name).read_bytes()).hexdigest()==digest,name
for name,digest in scope['original_inputs'].items():
 assert hashlib.sha256((root.parent/'ntm-original-pristine'/name).read_bytes()).hexdigest()==digest,name
for name,digest in scope['unchanged_grate'].items():
 assert hashlib.sha256((root/name).read_bytes()).hexdigest()==digest,name
for name,digest in scope['unchanged_materials'].items():
 assert hashlib.sha256((root/name).read_bytes()).hexdigest()==digest,name
 assert hashlib.sha256((root.parent/'ntm-original-pristine'/name).read_bytes()).hexdigest()==digest,name
proxy=(root/'src/main/java/com/hbm/main/ClientProxy.java').read_text()
assert 'registerBlockHandler(new RenderFence())' not in proxy
assert 'registerBlockHandler(new RenderChain())' not in proxy
assert 'registerBlockHandler(new RenderGrate())' in proxy
registry=(root/'src/main/java/com/hbm/blocks/ModBlocks.java').read_text()
for line in scope['parent_constructor_lines']: assert line in registry,line
assert 'GameRegistry.registerBlock(chain, chain.getTranslationKey())' in registry
assert 'GameRegistry.registerBlock(fence_metal, ItemBlockBase.class, fence_metal.getTranslationKey())' in registry
print('PASS exact structural sources, original renderer oracle inputs, four unchanged materials, unchanged grate gate and parent integration')
