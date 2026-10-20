#!/usr/bin/env python3
import argparse, importlib.util, re, subprocess, tempfile, hashlib, json
from pathlib import Path
H=Path(__file__).resolve().parent; M=H.parents[2]
p=argparse.ArgumentParser();p.add_argument('--support-module',type=Path,required=True);p.add_argument('--original-root',type=Path,required=True);p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);a=p.parse_args()
subprocess.run(['python3',str(H/'source-proof.py'),'--support-module',str(a.support_module),'--original-root',str(a.original_root)],check=True)
spec=importlib.util.spec_from_file_location('oreproof',a.support_module/'porting/tests/ore-callback-native/run.py');base=importlib.util.module_from_spec(spec);spec.loader.exec_module(base)
source=M/'src/main/java/com/hbm/blocks/generic/BlockOreFluid.java';s=source.read_text();original=(a.original_root/'src/main/java/com/hbm/blocks/generic/BlockOreFluid.java').read_text()
# Unedited original fluid/quantity/chance functions remain byte-exact, including all RNG expressions.
for sig in ['public String getUnlocalizedReserveType(', 'public FluidType getPrimaryFluid(', 'public FluidType getSecondaryFluid(', 'public int getBlockFluidAmount(', 'public int getPrimaryFluidAmount(', 'public int getSecondaryFluidAmount(', 'private double getDrainChance(', 'public boolean requiresFracking(', 'public static Block getFullBlock(']:
 assert base.extract(s,sig)==base.extract(original,sig),sig
bindings=re.findall(r'^\s*(ore_\w+) = new BlockOreFluid\(Material.ROCK, (\w+), ReserveType.(\w+)\).*?;$',(a.support_module/'src/main/java/com/hbm/blocks/ModBlocks.java').read_text(),re.M)
assert len(bindings)==5
for name,empty,typ in bindings:
 if empty!='null':assert re.search(r'\b'+empty+r' = new BlockOre\(Material.ROCK\)',(a.support_module/'src/main/java/com/hbm/blocks/ModBlocks.java').read_text())
with tempfile.TemporaryDirectory(prefix='oil-native-') as td:
 t=Path(td);files=base.generate(t)
 def fixture(path,body):
  f=t/path;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(body);files.append(f)
 names=sorted(set(re.findall(r'Fluids\.(\w+)',s)))
 fixture('com/hbm/inventory/fluid/FluidType.java','package com.hbm.inventory.fluid;public class FluidType {}')
 fixture('com/hbm/inventory/fluid/Fluids.java','package com.hbm.inventory.fluid;public class Fluids {'+''.join('public static final FluidType '+n+'=new FluidType();' for n in names)+'}')
 fields=sorted(set(re.findall(r'WorldConfig\.(\w+)',s)))
 fixture('com/hbm/config/WorldConfig.java','package com.hbm.config;public class WorldConfig {'+''.join('public static '+('double' if 'Chance' in n else 'int')+' '+n+' = '+('0.5' if 'Chance' in n else '30' if n.endswith('Max') else '10')+';' for n in fields)+'}')
 # Exact original owner, renaming only identity and platform calls. Original ordered bodies retained.
 oracle=original.replace('BlockOreFluid','OriginalOilOracle').replace('package com.hbm.blocks.generic;','package com.hbm.blocks.generic;\nimport net.minecraft.block.state.IBlockState;import net.minecraft.util.math.BlockPos;')
 oracle=oracle.replace('@Override\n\tpublic void onNeighborBlockChange','public void onNeighborBlockChange').replace('world.getBlockMetadata(x, y, z)','com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world,x,y,z))').replace('world.getBlock(x, y - 1, z)','com.hbm.world.WorldBlockState.read(world,x,y-1,z).getBlock()')
 oracle=oracle.replace('world.setBlock(', 'originalSet(world, ')
 at=oracle.rfind('}')
 oracle=oracle[:at]+'''public void neighborChanged(IBlockState s,World w,BlockPos p,Block b,BlockPos f){onNeighborBlockChange(w,p.getX(),p.getY(),p.getZ(),b);}
 static void originalSet(World w,int x,int y,int z,Block b,int m,int f){java.util.Objects.requireNonNull(w);if(x < -30000000||x >=30000000||z < -30000000||z >=30000000||y<0||y>=256)return;w.setBlockState(new BlockPos(x,y,z),b.getStateFromMeta(m),f);}
}'''
 fixture('com/hbm/blocks/generic/OriginalOilOracle.java',oracle)
 fixture('OilNativeTest.java',(H/'OilNativeTest.java.in').read_text())
 files += [source,a.support_module/'src/main/java/com/hbm/world/WorldBlockState.java']
 cp=a.classpath_file.read_text().strip()
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-sourcepath',str(t),'-cp',cp,'-d',str(t)]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Xmx400m','-cp',str(t)+':'+cp,'OilNativeTest'],cwd=t,check=True)
print('PASS exact original formula bodies and five production bindings; four native BlockOre targets plus null')
