#!/usr/bin/env python3
import hashlib,json,subprocess,zipfile,re,io
from pathlib import Path
H=Path(__file__).resolve().parent;S=H.parents[2]/'src/main/java';J=Path('/workspace/shared/nagaport-toolchain/java8/bin/javap');old=Path('/tmp/nagaport-legacy-client.jar');forge=Path('/tmp/nagaport-forge1614-userdev.jar');cpf=Path('/tmp/nagaport-ae2-native-classpath.txt');cp=cpf.read_text().strip();sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
with zipfile.ZipFile(forge) as z:
 mappings=z.read('conf/packaged.srg').decode();names=z.read('conf/methods.csv').decode();mapping='\n'.join(l for l in mappings.splitlines() if l.startswith(('MD: aji/','MD: ahb/')) and any(n in l for n in ['func_149709_b','func_149748_c','func_149744_f','func_72879_k','func_94574_k','func_72864_z']));mapping+='\n'+'\n'.join(l for l in names.splitlines() if l.startswith(('func_149709_b,','func_149748_c,','func_149744_f,','func_72879_k,','func_94574_k,','func_72864_z,')));(H/'legacy-mapping.txt').write_text(mapping+'\n');(H/'original-ForgeDirection.txt').write_text('\n'.join(l.rstrip().expandtabs(4) for l in z.read('src/main/java/net/minecraftforge/common/util/ForgeDirection.java').decode().splitlines())+'\n')
 with zipfile.ZipFile(io.BytesIO(z.read('forgepatches.zip'))) as f:
  excerpts=[]
  for name in ['net/minecraft/block/Block.java.patch','net/minecraft/world/World.java.patch']:
   lines=f.read(name).decode().splitlines()
   for i,line in enumerate(lines):
    if 'shouldCheckWeakPower' in line:excerpts+=['// '+name]+lines[max(0,i-5):i+10]
  (H/'forge-power-patches.txt').write_text('\n'.join(l.rstrip() for l in excerpts)+'\n')
legacy=subprocess.check_output([str(J),'-c','-p','-classpath',str(old),'ahb','aji'],text=True);native=subprocess.check_output([str(J),'-c','-p','-classpath',cp,'net.minecraft.world.World','net.minecraft.block.Block','net.minecraft.block.state.BlockStateContainer$StateImplementation','net.minecraft.util.EnumFacing'],text=True)
assert 'Method aji.c:(Lahl;IIII)I' in legacy and 'Method aji.b:(Lahl;IIII)I' in legacy
assert 'InterfaceMethod net/minecraft/block/state/IBlockState.getWeakPower:' in native and 'InterfaceMethod net/minecraft/block/state/IBlockState.getStrongPower:' in native
(H/'legacy-engine.log').write_text(legacy);(H/'native-engine.log').write_text(native)
inputs={str(p):sha(p) for p in [old,forge,cpf,J,J.parent/'java',J.parent/'javac',S/'com/hbm/lib/ForgeDirection.java',S/'com/hbm/world/WorldBlockState.java',S/'com/hbm/tileentity/machine/TileEntityMachineRadarNT.java']}
for p in map(Path,cp.split(':')):
 if p.is_file():
  with zipfile.ZipFile(p) as z:
   if 'net/minecraft/world/World.class' in z.namelist():inputs[str(p)]=sha(p)
(H/'input-sha256.json').write_text(json.dumps(inputs,indent=2)+'\n')
text=Path('/workspace/shared/cp80-full-compile.log').read_text();errors=[]
for name in ['MachineRadar','MachineRadarLarge']:
 path='com/hbm/blocks/machine/'+name+'.java'
 with zipfile.ZipFile(H/'baseline.zip') as z:lines=z.read(path).decode().splitlines()
 for i,l in enumerate(lines):
  if any(n in l for n in ['public boolean canProvidePower(','public int isProvidingWeakPower(','public int isProvidingStrongPower(']):n=i
  elif 'world.getBlockMetadata(' in l or 'world.getTileEntity(' in l:n=i+1
  else:continue
  marker='/build/sources/main/java/'+path+':'+str(n)+': error:';assert marker in text;errors.append(next(l for l in text.splitlines() if marker in l))
assert len(errors)==9;(H/'cp80-diagnostics.txt').write_text('\n'.join(errors)+'\n');print('PASS mapped old/native power dispatch and exact nine CP80 diagnostics; native direction index and original ForgeDirection audited')
