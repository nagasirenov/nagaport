#!/usr/bin/env python3
import importlib.util,json
import sys
sys.dont_write_bytecode=True
from pathlib import Path
HERE=Path(__file__).resolve().parent
spec=importlib.util.spec_from_file_location('guard',HERE/'check-source.py');g=importlib.util.module_from_spec(spec);spec.loader.exec_module(g)
old,new=g.inputs();assert g.validate(old,new)==4071
mod=g.MOD;log=g.FAMILY[1];pillar=g.FAMILY[2];row=json.loads((HERE/'sites.json').read_text())[0]
mutants=[
 ('dropped assignment',log,b'super.setSoundType(sound);',b';'),
 ('wrong argument',pillar,b'super.setSoundType(sound);',b'super.setSoundType(net.minecraft.block.SoundType.STONE);'),
 ('null return',log,b'return this;',b'return null;'),
 ('wrong object at registry site',mod,row['after'].encode(),row['after'].replace('SoundType.CLOTH','SoundType.STONE').encode()),
 ('wrong owner cast',mod,row['after'].encode(),row['after'].replace('((BlockRotatablePillar)','((BlockLogNT)').encode()),
 ('relocated sound before tab',mod,row['after'].encode(),row['after'].replace('.setCreativeTab(MainRegistry.blockTab)).withSoundType(SoundType.CLOTH)',').withSoundType(SoundType.CLOTH).setCreativeTab(MainRegistry.blockTab)').encode()),
 ('constructor field assignment changed',log,b'this.sideIcon = sideIcon;',b'this.sideIcon = topIcon;'),
 ('pillar constructor changed',pillar,b'textureTop = top;',b'textureTop = "wrong";'),
 ('unrelated renderer changed',pillar,b'reg.registerIcon(textureTop)',b'reg.registerIcon("wrong")'),
 ('unrelated sound definition changed','src/main/java/com/hbm/blocks/ModSoundTypes.java',b'1.5F',b'1.7F'),
]
for name,path,b,a in mutants:
 assert b!=a and new[path].count(b)>=1,(name,path)
 trial=dict(new);trial[path]=trial[path].replace(b,a,1)
 try:g.validate(old,trial)
 except AssertionError: print('PASS source negative control:',name)
 else:raise AssertionError('Source mutant accepted: '+name)
print('PASS all ten independent in-memory source mutations rejected; production files untouched.')
