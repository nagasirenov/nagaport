#!/usr/bin/env python3
"""Bounded ownership/source-integration checks; not a gameplay substitute."""
import pathlib,json,hashlib
r=pathlib.Path(__file__).resolve().parents[2]
manifest=json.loads((r/'porting/tests/plants-production-manifest.json').read_text())
for f,digest in manifest.items():
 assert hashlib.sha256((r/f).read_bytes()).hexdigest()==digest,f
 if f.endswith('.java'):
  source=(r/f).read_text()
  for forbidden in ['ISimpleBlockRenderingHandler','RenderBlocks','Tessellator.instance','RenderingRegistry','com.hbm.render.icon','getMixedBrightnessForBlock']:
   assert forbidden not in source,(f,forbidden)
client=(r/'src/main/java/com/hbm/main/ClientProxy.java').read_text()
for name in ['RenderReeds','RenderCoral','RenderHangingVine','RenderAlgaeFilm']:
 assert 'registerBlockHandler(new '+name+'())' not in client,name
blocks=(r/'src/main/java/com/hbm/blocks/ModBlocks.java').read_text()
for name,kind in [('reeds','BlockReeds'),('laythe_coral','BlockCoral'),('vine_phosphor','BlockHangingVine'),('algae_film','BlockAlgaeFilm')]:
 line=next(x for x in blocks.splitlines() if name+' = new '+kind in x)
 assert 'setTranslationKey' in line,line
 assert 'setBlockName' not in line and 'setBlockTextureName' not in line,line
print('PASS exact frozen plant production digests, native APIs and parent four-owner/ClientProxy integration; no source exclusions')
