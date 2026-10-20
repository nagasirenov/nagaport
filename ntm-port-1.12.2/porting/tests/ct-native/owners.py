#!/usr/bin/env python3
"""Real native CT owners/loader proof. Reactor rendering hooks are extracted; NTM collaborators explicit."""
import argparse,hashlib,json,os,re,subprocess,tempfile,zipfile
from pathlib import Path
H=Path(__file__).resolve().parent;M=H.parents[2]
p=argparse.ArgumentParser();p.add_argument('--java-home',required=True,type=Path);p.add_argument('--classpath',required=True);p.add_argument('--original-root',required=True,type=Path);a=p.parse_args()
def method(source,signature):
 start=source.index(signature);op=source.index('{',start);end=op+1;depth=1
 while depth:
  depth+=(source[end]=='{')-(source[end]=='}');end+=1
 return source[start:end]
registry=(M/'src/main/java/com/hbm/blocks/ModBlocks.java').read_text()
constructors=[]
for line in registry.splitlines():
 match=re.search(r'(\w+) = (new (BlockDecoCT|BlockNTMGlassCT|BlockHadronCoil|WireCoated|BlockICF|BlockPWR)\(.*?)(?:\.setTranslationKey)',line)
 if match:constructors.append((match[1],match[2],match[3]))
assert len(constructors)==30
for name,ctor,cls in constructors:
 for suffix in ['', '_ct']:
  rel='src/main/resources/assets/hbm/textures/blocks/'+name+suffix+'.png'
  assert (M/rel).read_bytes()==(a.original_root/rel).read_bytes(),'Original texture altered '+rel
for name in ['icf_block_port','pwr_casing_port']:
 for suffix in ['', '_ct']:
  rel='src/main/resources/assets/hbm/textures/blocks/'+name+suffix+'.png';assert (M/rel).read_bytes()==(a.original_root/rel).read_bytes()
print('PASS all64 original CT texture files byte-identical',flush=True)
# Mechanic methods outside rendering must be unchanged except native zero-state drop API.
for rel,signatures in {
 'generic/BlockDecoCT.java':['public boolean canConnect','public BlockDecoCT noFortune','public int quantityDroppedWithBonus'],
 'machine/BlockHadronCoil.java':['public boolean canConnect','public void addInformation'],
 'network/WireCoated.java':['public TileEntity createNewTileEntity'],
 'machine/BlockICF.java':['public boolean canConnect','public TileEntity createNewTileEntity','public void breakBlock','public static class TileEntityBlockICF'],
 'machine/BlockPWR.java':['public boolean canConnect','public TileEntity createNewTileEntity','public void breakBlock','public static class TileEntityBlockPWR']}.items():
 with zipfile.ZipFile(H/'cp48-mechanics-sources.zip') as z: data=z.read(rel)
 assert hashlib.sha256(data).hexdigest()==json.loads((H/'cp48-mechanics-manifest.json').read_text())[rel], 'Pinned CP48 mechanics source changed'
 base=data.decode()
 current=(M/'src/main/java/com/hbm/blocks'/rel).read_text()
 for signature in signatures:
  before=method(base,signature);after=method(current,signature)
  if signature=='public int quantityDroppedWithBonus':before=before.replace('this.getItemDropped(0, rand, fortune)','this.getItemDropped(getStateFromMeta(0), rand, fortune)')
  assert before==after,'Mechanics changed '+rel+' '+signature
print('PASS exact CP48 mechanics method invariance including complete ICF/PWR nested tile bodies',flush=True)
production=['com/hbm/blocks/generic/BlockDecoCT.java','com/hbm/blocks/generic/BlockNTMGlass.java','com/hbm/blocks/generic/BlockNTMGlassCT.java','com/hbm/blocks/machine/BlockHadronCoil.java','com/hbm/blocks/network/WireCoated.java','com/hbm/blocks/ITooltipProvider.java','com/hbm/lib/RefStrings.java','com/hbm/main/CTModelHandler.java','com/hbm/render/block/RenderBlockCT.java','com/hbm/render/block/nativeblock/ConveyorCuboidGeometry.java']
production+=['com/hbm/render/block/ct/'+n+'.java' for n in ['CT','CTContext','IBlockCT','RenderBlocksCT','CTLightQuad']]
with tempfile.TemporaryDirectory(prefix='ct-owners-') as temp:
 d=Path(temp);files=[]
 def fixture(rel,source):
  f=d/rel;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(source);files.append(str(f))
 fixture('com/hbm/tileentity/network/TileEntityCableBaseNT.java','package com.hbm.tileentity.network; public class TileEntityCableBaseNT extends net.minecraft.tileentity.TileEntity {}')
 fixture('com/hbm/util/i18n/I18nUtil.java','package com.hbm.util.i18n; public class I18nUtil { public static String resolveKey(String key,Object...args){return key;} public static String[] resolveKeyArray(String key,Object...args){return new String[]{key};} }')
 names=[n for n,c,k in constructors]+['icf_controller','pwr_controller']
 fixture('com/hbm/blocks/ModBlocks.java','package com.hbm.blocks; import net.minecraft.block.Block; public class ModBlocks { public static Block '+','.join(names)+'; }')
 for cls in ['BlockICF','BlockPWR']:
  source=(M/'src/main/java/com/hbm/blocks/machine'/f'{cls}.java').read_text()
  methods='\n'.join(method(source,s) for s in ['protected net.minecraft.block.state.BlockStateContainer createBlockState','public net.minecraft.block.state.IBlockState getStateFromMeta','public int getMetaFromState','public net.minecraft.util.EnumBlockRenderType getRenderType','public net.minecraft.block.state.IBlockState getExtendedState','public int getCTMaterial','public boolean canConnect'])
  fixture('com/hbm/blocks/machine/'+cls+'.java','package com.hbm.blocks.machine; import com.hbm.blocks.ModBlocks; import com.hbm.render.block.ct.*; import net.minecraft.world.IBlockAccess; import net.minecraft.block.Block; import net.minecraft.block.material.Material; public class '+cls+' extends Block implements IBlockCT { public '+cls+'(Material mat){super(mat);} '+methods+'}')
 initializers=[]
 for name,ctor,cls in constructors:
  ctor=ctor.replace('.setSoundType(ModSoundTypes.legacyMetal)','') # Sound samples are covered by the existing native sound proof.
  initializers.append('ModBlocks.'+name+'='+ctor+'.setRegistryName("hbm","tile.'+name+'"); owners.put("'+name+'",ModBlocks.'+name+');')
 fixture('CTOwnersTest.java',(H/'CTOwnersTest.java.in').read_text().replace('INITIALIZERS','\n'.join(initializers)))
 for rel in production:
  f=M/'src/main/java'/rel;files.append(str(f));print('PRODUCTION',rel,hashlib.sha256(f.read_bytes()).hexdigest(),flush=True)
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',temp,'-cp',a.classpath,'-d',temp]+files,check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Xmx1g','-cp',temp+os.pathsep+a.classpath,'CTOwnersTest',str(M/'src/main/resources')],cwd=temp,check=True)
print('LIMIT: ICF/PWR render hooks only. Actual coated-wire tile behavior and localization dispatch are collaborator boundaries, not full mechanics proof.',flush=True)
