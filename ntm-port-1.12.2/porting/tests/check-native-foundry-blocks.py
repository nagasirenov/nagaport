#!/usr/bin/env python3
"""Exact foundry source/provenance/retirement/integration boundary; not runtime evidence."""
from pathlib import Path
import hashlib,json,importlib.util
root=Path(__file__).resolve().parents[2]
spec=importlib.util.spec_from_file_location('foundry_boundary',root/'porting/tests/check-native-foundry-light-boundary.py')
boundary=importlib.util.module_from_spec(spec);spec.loader.exec_module(boundary)
scope=json.loads((root/'porting/tests/native-foundry-blocks-scope.json').read_text())
for path,digest in {**scope['production'],**scope['coordinated_tile_additions']}.items():
 assert hashlib.sha256((root/path).read_bytes()).hexdigest()==digest,path
for path in scope['deleted']:assert not (root/path).exists(),path
for path,digest in scope['original_inputs'].items():
 current=(root/path).read_bytes().decode('utf-8') if (root/path).exists() else ''
 raw=boundary.reverse_source(path,current).encode('utf-8')
 assert hashlib.sha256(raw).hexdigest()==digest,path
client=(root/'src/main/java/com/hbm/main/ClientProxy.java').read_text()
for kind in ['Basin','Mold','Channel','Tank']:
 renderer='RenderFoundryFluid' if kind in ['Channel','Tank'] else 'RenderFoundry'
 assert client.count('ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFoundry'+kind+'.class, new '+renderer+'());')==1,kind
for kind in ['Basin','Mold','Channel','Tank','Outlet']:
 assert 'new RenderFoundry'+kind+'()' not in client,kind
for path in scope['production']:
 if path.endswith('.java'):
  text=(root/path).read_text()
  for removed in ['extends RenderBlocks','implements ISimpleBlockRenderingHandler','import net.minecraft.client.renderer.RenderBlocks;','import com.hbm.render.icon.IIcon;']:
   assert removed not in text,(path,removed)
for path in scope['tests']:assert (root/path).is_file(),path
print('PASS exact native foundry source hashes, original CP32 provenance, five retired renderers, four exact native TESR bindings and native declaration boundaries')
