#!/usr/bin/env python3
import hashlib,json,re,subprocess,importlib.util
from pathlib import Path
H=Path(__file__).resolve().parent;M=H.parents[2]
spec=importlib.util.spec_from_file_location('owners',H/'owners.py');owners=importlib.util.module_from_spec(spec);spec.loader.exec_module(owners)
paths=subprocess.check_output(['git','ls-files','src/main/java'],cwd=M,text=True).splitlines();assert all((M/p).is_file()for p in paths)
def clean(s):return re.sub(r'/\*.*?\*/|//[^\n]*','',s,flags=re.S)
src={p:clean((M/p).read_text())for p in paths}
def verify(sources):
 writes=[(p,line.strip())for p,s in sources.items()for line in s.splitlines()if re.search(r'\bbarbed_wire\s*=(?!=)',line)]
 assert writes==[('src/main/java/com/hbm/blocks/ModBlocks.java','barbed_wire = createBarbedWire("barbed_wire");')],writes
 assert sum(s.count('public static Block barbed_wire;')for s in sources.values())==1
 for p,s in sources.items():assert not re.search(r'\bextends\s+(?:[\w.]+\.)?BarbedWire\b',s),p
verify(src)
for extra in ['class X extends BarbedWire {}','class X {void x(){ModBlocks.barbed_wire=null;}}','class X {void x(){barbed_wire = null;}}']:
 mutant=dict(src);mutant['Injected.java']=extra
 try:verify(mutant)
 except AssertionError:pass
 else:raise AssertionError('closure mutant survived')
factory=owners.extract((M/'src/main/java/com/hbm/blocks/ModBlocks.java').read_text(),'private static Block createBarbedWire(')
assert factory==(H/'factory.txt').read_text().rstrip('\n')
for p,digest in json.loads((H/'dependency-hashes.json').read_text()).items():assert hashlib.sha256((M/p).read_bytes()).hexdigest()==digest,p
tracked=set(subprocess.check_output(['git','ls-files','src/main/resources'],cwd=M,text=True).splitlines())
for name in ['barbed_wire','barbed_wire_fire','barbed_wire_poison','barbed_wire_acid','barbed_wire_wither','barbed_wire_ultradeath']:
 assert 'src/main/resources/assets/hbm/textures/blocks/'+name+'_model.png'in tracked
 assert 'src/main/resources/assets/hbm/textures/blocks/tile.'+name+'_model.png'not in tracked
 assert 'src/main/resources/assets/hbm/blockstates/'+name+'.json'in tracked
 assert 'src/main/resources/assets/hbm/blockstates/tile.'+name+'.json'not in tracked
print('PASS',len(paths),'tracked sources: exact mutable static assignment/factory, no subclasses, three closure mutants; native owner/model/helper unchanged.')
print('INHERITED RESOURCE BLOCKER: intentional hbm:tile.barbed_wire* identity requests blocks/tile.barbed_wire*_model textures and tile.barbed_wire* state routes, whereas tracked assets use names without tile. No OBJ/resource runtime acceptance. Original registry identity is not changed.')
