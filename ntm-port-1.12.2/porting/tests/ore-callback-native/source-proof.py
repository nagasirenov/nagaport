#!/usr/bin/env python3
import hashlib,json,zipfile,importlib.util
from pathlib import Path
H=Path(__file__).resolve().parent;M=H.parents[2]
spec=importlib.util.spec_from_file_location('runner',H/'run.py');runner=importlib.util.module_from_spec(spec);spec.loader.exec_module(runner)
with zipfile.ZipFile(H/'baseline.zip') as z:old=z.read('BlockOre.java').decode()
actual=(M/'src/main/java/com/hbm/blocks/generic/BlockOre.java').read_text();expected=old
for edit in reversed(json.loads((H/'edits.json').read_text())):
 assert expected[edit['start']:edit['end']]==edit['old'];expected=expected[:edit['start']]+edit['new']+expected[edit['end']:]
assert expected==actual
for signature in ['public Item getItemDropped(int','public int damageDropped(int','public int quantityDropped(Random','public int quantityDroppedWithBonus(','public void getSubBlocks(Item','public int getSubCount()']:
 assert runner.extract(old,signature)==runner.extract(actual,signature)
for path,digest in json.loads((H/'dependency-hashes.json').read_text()).items():assert hashlib.sha256((M/path).read_bytes()).hexdigest()==digest,path
placement=runner.extract(actual,'public void onBlockPlacedBy(World world, int')
assert placement.count('stack.getItemDamage()')==1 and placement.index('stack.getItemDamage()')<placement.index('if(world == null)')<placement.index('if(x >=')
assert actual.count('Retained NTM scalar domain hook')==4
print('PASS complete-source edit reconstruction; all original drop/quantity/creative/rectify algorithms byte-identical; four retained virtual domain hooks; damage evaluation once before null/bounds; descendant and native interface pins.')
