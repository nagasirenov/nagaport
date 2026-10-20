#!/usr/bin/env python3
import importlib.util, sys
sys.dont_write_bytecode = True
from pathlib import Path
here = Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location('guard', here/'check-source.py')
guard = importlib.util.module_from_spec(spec); spec.loader.exec_module(guard)
old, new = guard.inputs()
guard.validate(old, new)
mod = guard.MOD
crate = 'src/main/java/com/hbm/blocks/generic/BlockCrate.java'
store = 'src/main/java/com/hbm/blocks/generic/BlockStorageCrate.java'
mutants = [
    ('drop crate sound assignment', crate, b'super.setSoundType(sound);', b';'),
    ('substitute storage sound argument', store, b'super.setSoundType(sound);', b'super.setSoundType(net.minecraft.block.SoundType.METAL);'),
    ('null crate builder return', crate, b'\t\treturn this;', b'\t\treturn null;'),
    ('substitute registry legacyMetal', mod, b'((BlockCrate) new BlockCrate(Material.IRON).setTranslationKey("crate_lead")).withSoundType(ModSoundTypes.legacyMetal)', b'((BlockCrate) new BlockCrate(Material.IRON).setTranslationKey("crate_lead")).withSoundType(SoundType.METAL)'),
    ('move sound before name', mod, b'((BlockCrate) new BlockCrate(Material.WOOD).setTranslationKey("crate")).withSoundType(SoundType.WOOD)', b'new BlockCrate(Material.WOOD).withSoundType(SoundType.WOOD).setTranslationKey("crate")'),
    ('invalid foreign-owner cast', mod, b'((BlockCrate) new BlockCrate(Material.WOOD)', b'((BlockStorageCrate) new BlockCrate(Material.WOOD)'),
    ('change crate constructor material', crate, b'super(p_i45394_1_);', b'super(Material.ROCK);'),
    ('change unrelated crate gameplay', crate, b'\t\treturn false;', b'\t\treturn true;'),
    ('change unrelated storage gameplay', store, b'private static boolean dropInv = true;', b'private static boolean dropInv = false;'),
    ('change sound definition', 'src/main/java/com/hbm/blocks/ModSoundTypes.java', b'1F, 1.5F', b'1F, 1.0F'),
]
for name, path, before, after in mutants:
    assert before in new[path], name
    mutated = dict(new); mutated[path] = new[path].replace(before, after, 1)
    try: guard.validate(old, mutated)
    except AssertionError: print('PASS source negative control:', name)
    else: raise AssertionError('Survived source negative control: ' + name)
