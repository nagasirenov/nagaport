#!/usr/bin/env python3
import importlib.util,json,sys
sys.dont_write_bytecode=True
from pathlib import Path
HERE=Path(__file__).resolve().parent
spec=importlib.util.spec_from_file_location('guard',HERE/'check-source.py');g=importlib.util.module_from_spec(spec);spec.loader.exec_module(g)
old,new=g.inputs();assert g.validate(old,new)==4
mod=g.MOD;earth=g.FAMILY[1];log=g.FAMILY[2];leaves=g.FAMILY[3];rows=json.loads((HERE/'sites.json').read_text());row=rows[0]
mutants=[
 ('dropped assignment',earth,b'super.setSoundType(sound);',b';'),
 ('wrong argument',log,b'super.setSoundType(sound);',b'super.setSoundType(net.minecraft.block.SoundType.STONE);'),
 ('null return',leaves,b'return this;',b'return null;'),
 ('wrong registry sound object',mod,row['after'].encode(),row['after'].replace('SoundType.PLANT','SoundType.STONE').encode()),
 ('foreign owner cast',mod,row['after'].encode(),row['after'].replace('((WasteEarth)','((WasteLog)').encode()),
 ('relocated sound before name',mod,row['after'].encode(),row['after'].replace('new WasteEarth(Material.GROUND, true).setTranslationKey("waste_earth")).withSoundType(SoundType.PLANT)','new WasteEarth(Material.GROUND, true)).withSoundType(SoundType.PLANT).setTranslationKey("waste_earth")').encode()),
 ('wrong frozen constructor flag',mod,b'new WasteEarth(Material.GROUND, false)',b'new WasteEarth(Material.GROUND, true)'),
 ('changed constructor ticking',leaves,b'this.setTickRandomly(true);',b'this.setTickRandomly(false);'),
 ('changed construction-dispatched opacity',leaves,b'public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) {\n\t\treturn false;',b'public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) {\n\t\treturn true;'),
 ('removed texture call',mod,b'.setBlockTextureName(RefStrings.MODID + ":waste_earth");',b';'),
 ('unrelated icon renderer changed',log,b'":waste_log_top"',b'":wrong_texture"'),
 ('unrelated registry entry changed',mod,b'"ore_oil_sand"',b'"ore_oil_wrong"'),
]
for name,path,b,a in mutants:
 assert b!=a and new[path].count(b)>=1,(name,path)
 trial=dict(new);trial[path]=trial[path].replace(b,a,1)
 try:g.validate(old,trial)
 except AssertionError:print('PASS source negative control:',name)
 else:raise AssertionError('Source mutant accepted: '+name)
print('PASS all twelve independent in-memory source mutations rejected; production files untouched.')
