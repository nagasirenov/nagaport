#!/usr/bin/env python3
"""Fail-closed source identity and bounded lifecycle migration audit."""
import argparse, hashlib, importlib.util, json, re
from pathlib import Path
H=Path(__file__).resolve().parent; M=H.parents[2]
p=argparse.ArgumentParser(); p.add_argument('--original-root',type=Path,required=True); p.add_argument('--support-module',type=Path,required=True); a=p.parse_args()
spec=importlib.util.spec_from_file_location('oreproof',a.support_module/'porting/tests/ore-callback-native/run.py'); base=importlib.util.module_from_spec(spec); spec.loader.exec_module(base)
f=Path('src/main/java/com/hbm/blocks/generic/BlockOreFluid.java')
o=(a.original_root/f).read_text(); s=(M/f).read_text()
assert hashlib.sha256((a.original_root/f).read_bytes()).hexdigest()=='57af5a8fee747cafd3aeecede2f464c12e261dcf19bb5e4343da84b255f80eeb'
def compact(x):return re.sub(r'\s+','',re.sub(r'//[^\n]*','',x))
# Reverse only these native boundary edits, then compare the entire original owner.
s=s.replace('import net.minecraft.block.state.IBlockState;','').replace('import net.minecraft.util.math.BlockPos;','')
s=s.replace('public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos)', 'public void onNeighborBlockChange(World world, int x, int y, int z, Block block)')
s=s.replace('int x = pos.getX(), y = pos.getY(), z = pos.getZ();','')
s=s.replace('com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) )','world.getBlockMetadata(x, y, z)').replace('com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock()','world.getBlock(x, y - 1, z)')
for yy,block,codec in [('y','empty','empty.'),('y - 1','this','')]:
 native='if(x >= -30000000 && x < 30000000 && z >= -30000000 && z < 30000000 && '+yy+' >= 0 && '+yy+' < 256) { world.setBlockState(new BlockPos(x, '+yy+', z), '+codec+'getStateFromMeta(meta), 3); }'
 # Whitespace-independent exact expression replacement, not a broad source rewrite.
 s=compact(s).replace(compact(native),compact('world.setBlock(x, '+yy+', z, '+block+', meta, 3);'))
assert s==compact(o),'Unexpected original owner semantic/source change'
registry=(a.support_module/'src/main/java/com/hbm/blocks/ModBlocks.java').read_text()
bindings=re.findall(r'^\s*(ore_\w+) = new BlockOreFluid\(Material.ROCK, (\w+), ReserveType.(\w+)\).*?;$',registry,re.M)
assert set(bindings)=={('ore_oil','ore_oil_empty','OIL'),('ore_bedrock_oil','null','OIL'),('ore_gas','ore_gas_empty','GAS'),('ore_brine','ore_brine_empty','BRINE'),('ore_tekto','ore_tekto_empty','OIL')}
for _,empty,_ in bindings:
 if empty!='null':assert re.search(r'\b'+empty+r' = new BlockOre\(Material.ROCK\)',registry)
root=a.support_module/'src/main/java'
assert not [str(f) for f in root.rglob('*.java') if re.search(r'extends\s+BlockOreFluid\b',f.read_text())]
print('PASS whole original owner reversed-boundary comparison, pinned original hash, exact five bindings and no descendants')
