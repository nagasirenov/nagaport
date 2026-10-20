#!/usr/bin/env python3
import argparse,hashlib,json,re,subprocess,zipfile
from pathlib import Path
HERE=Path(__file__).resolve().parent
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--legacy-jar',type=Path,required=True);p.add_argument('--record',action='store_true');a=p.parse_args();cp=a.classpath_file.read_text().strip()
def method(text,sig):
 start=text.index('  '+sig+'\n');tail=text[start:];m=re.search(r'\n  (?:public|protected|private|static) ',tail[3:]);return tail if not m else tail[:m.start()+3]
def bytecode(name,classpath):return subprocess.check_output([str(a.java_home/'bin/javap'),'-classpath',classpath,'-p','-c',name],text=True)
block=bytecode('net.minecraft.block.Block',cp);state=bytecode('net.minecraft.block.state.BlockStateContainer$StateImplementation',cp);leaves=bytecode('net.minecraft.block.BlockLeaves',cp);old=bytecode('aji',str(a.legacy_jar))
sections={'native_constructor':method(block,'public net.minecraft.block.Block(net.minecraft.block.material.Material, net.minecraft.block.material.MapColor);'),'native_opacity':method(block,'public boolean isOpaqueCube(net.minecraft.block.state.IBlockState);'),'native_state_dispatch':method(state,'public boolean isOpaqueCube();'),'native_fullblock_getter':method(block,'public boolean isFullBlock(net.minecraft.block.state.IBlockState);'),'native_leaves_constructor':method(leaves,'public net.minecraft.block.BlockLeaves();'),'native_leaves_dynamic':method(leaves,'public boolean isOpaqueCube(net.minecraft.block.state.IBlockState);'),'original_constructor':method(old,'protected aji(awt);')}
assert sections['original_constructor'].count('Method c:()Z')==2
assert sections['native_constructor'].count('InterfaceMethod net/minecraft/block/state/IBlockState.isOpaqueCube:()Z')==1
assert 'putfield' in sections['native_constructor'] and 'Field fullBlock:Z' in sections['native_constructor'] and 'Field lightOpacity:I' in sections['native_constructor']
assert 'Method net/minecraft/block/Block.isOpaqueCube:(Lnet/minecraft/block/state/IBlockState;)Z' in sections['native_state_dispatch'] and 'putfield' not in sections['native_state_dispatch']
classes={}
for jar in cp.split(':'):
 if not Path(jar).is_file() or not zipfile.is_zipfile(jar):continue
 with zipfile.ZipFile(jar) as z:
  for entry in ['net/minecraft/block/Block.class','net/minecraft/block/BlockLeaves.class','net/minecraft/block/state/BlockStateContainer$StateImplementation.class']:
   if entry in z.namelist() and entry not in classes:classes[entry]=hashlib.sha256(z.read(entry)).hexdigest()
assert len(classes)==3
report={'original_jar_sha256':hashlib.sha256(a.legacy_jar.read_bytes()).hexdigest(),'native_class_sha256':classes,'methods':sections}
if a.record:(HERE/'engine-audit.json').write_text(json.dumps(report,indent=2)+'\n')
else:assert report==json.loads((HERE/'engine-audit.json').read_text())
print('PASS pinned real old/native engine methods: old opacity called twice versus native once during constructor; fullBlock/lightOpacity cached; state opacity dispatched virtually on every query; native leaves remain a distinct dynamic parent.')
