import argparse,hashlib,json,re,subprocess,zipfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--legacy-jar',type=Path,required=True);p.add_argument('--legacy-srg',type=Path,required=True);p.add_argument('--record',action='store_true');a=p.parse_args();E=Path(__file__).resolve().parent;cp=a.classpath_file.read_text().strip();sha=lambda b:hashlib.sha256(b).hexdigest()
assert sha(a.legacy_jar.read_bytes())=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
srg=zipfile.ZipFile(a.legacy_srg).read('joined.srg').decode();assert 'MD: aji/b ()I net/minecraft/block/Block/func_149645_b ()I' in srg;assert 'MD: blm/b (Laji;III)Z net/minecraft/client/renderer/RenderBlocks/func_147805_b' in srg
names=['net.minecraft.block.BlockContainer','net.minecraft.block.state.BlockStateContainer$StateImplementation','net.minecraft.client.renderer.BlockRendererDispatcher','net.minecraft.client.renderer.chunk.RenderChunk','blm','blo']
bc={n:subprocess.check_output([str(a.java_home/'bin/javap'),'-p','-c','-classpath',str(a.legacy_jar) if n in ['blm','blo'] else cp,n],text=True) for n in names}
def section(n,sig):
 tail=bc[n][bc[n].index('  '+sig+'\n'):];m=re.search(r'\n  (?:public|protected|private|static) ',tail[3:]);return tail[:m.start()+3] if m else tail
sections={
'old_world_dispatch':section('blm','public boolean b(aji, int, int, int);'),
'native_world_dispatch':section(names[2],'public boolean renderBlock(net.minecraft.block.state.IBlockState, net.minecraft.util.math.BlockPos, net.minecraft.world.IBlockAccess, net.minecraft.client.renderer.BufferBuilder);'),
'native_state_dispatch':section(names[1],'public net.minecraft.util.EnumBlockRenderType getRenderType();'),
'native_container_default':section(names[0],'public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState);')}
assert re.search(r'Method aji.b:\(\)I[\s\S]{0,240}iconst_m1[\s\S]{0,120}if_icmpne[\s\S]{0,100}iconst_0[\s\S]{0,60}ireturn',sections['old_world_dispatch'])
assert re.search(r'IBlockState.getRenderType:[\s\S]{0,220}EnumBlockRenderType.INVISIBLE:[\s\S]{0,130}if_acmpne[\s\S]{0,80}iconst_0[\s\S]{0,50}ireturn',sections['native_world_dispatch'])
assert 'Block.getRenderType:(Lnet/minecraft/block/state/IBlockState;)' in sections['native_state_dispatch']
assert 'EnumBlockRenderType.INVISIBLE' in sections['native_container_default']
# Independent tile registration precedes normal geometry rejection in both actual world compilation consumers.
assert bc['blo'].index('Method bmk.a:(Laor;)Z')<bc['blo'].index('Method blm.b:(Laji;III)Z')
assert bc[names[3]].index('CompiledChunk.addTileEntity:')<bc[names[3]].index('EnumBlockRenderType.INVISIBLE:')
for k,n,needle in [('old_tile_collection','blo','Method bmk.a:(Laor;)Z'),('native_tile_collection',names[3],'CompiledChunk.addTileEntity:')]:
 lines=bc[n].splitlines();i=next(i for i,l in enumerate(lines) if needle in l);sections[k]='\n'.join(lines[max(0,i-15):i+45])
record=dict(legacy_sha256=sha(a.legacy_jar.read_bytes()),srg_sha256=sha(a.legacy_srg.read_bytes()),class_sha256={n:sha(zipfile.ZipFile(next(Path(x) for x in cp.split(':') if Path(x).is_file() and zipfile.is_zipfile(x) and n.replace('.','/')+'.class' in zipfile.ZipFile(x).namelist())).read(n.replace('.','/')+'.class')) for n in names[:4]},sections=sections)
if a.record:(E/'engine-audit.json').write_text(json.dumps(record,indent=2)+'\n')
else:assert record==json.loads((E/'engine-audit.json').read_text())
print('PASS: pinned original/native dispatch and independent tile collection bytecode; no pixels/GL claim.')
