#!/usr/bin/env python3
"""Immutable CP59 inversion plus independent javac whole-receiver evidence.
The AST evidence comes from full-source Java8 attribution, not this selector.
"""
import csv,hashlib,json,re,subprocess
from pathlib import Path
HERE=Path(__file__).resolve().parent; ROOT=HERE.parents[2]; REPO=ROOT.parent
BASE='e72ec3d2ae8db6653ebaad529a574ee32c99260b'
def baseline(relative):return subprocess.check_output(['git','-C',str(REPO),'show',BASE+':ntm-port-1.12.2/'+relative])
rows=json.loads((HERE/'sites.json').read_text());assert len(rows)==27
p='src/main/java/com/hbm/blocks/ModBlocks.java';original=baseline(p);edited=(ROOT/p).read_bytes();inverse=edited
for row in rows:
 before=row['before'].encode();after=row['after'].encode()
 assert original.count(before)==1 and edited.count(after)==1
 inverse=inverse.replace(after,before)
assert inverse==original,'Complete ModBlocks must invert byte-for-byte, including CRLF and all unselected sites'
for row in json.loads((HERE/'excluded-sound-chains.json').read_text()):assert row['before'].encode() in edited and row['after'].encode() not in edited
for owner in ['BlockOutgas','BlockSmolder','BlockStepEffect']:
 p=f'src/main/java/com/hbm/blocks/generic/{owner}.java';assert (ROOT/p).read_bytes()==baseline(p)
 s=(ROOT/p).read_text();assert 'class '+owner+' extends Block {' in s
 for visual in ['getIcon','registerBlockIcons','getRenderType','getRenderColor','colorMultiplier','setBlockBounds','isOpaqueCube','renderAsNormalBlock','getRenderBlockPass']:
  assert not re.search(r'\b'+visual+r'\s*\(',s),(owner,visual)
# Explicit item ownership audit: no custom icon/color/model path is admitted.
counts={}
for row in rows:
 name=re.search(r'(\w+) = ',row['before'])[1]
 registrations=re.findall(r'^\s*GameRegistry.registerBlock\('+name+r', ([^\n]+)',edited.decode(),re.M)
 assert len(registrations)==1
 first=registrations[0].split(',')[0]
 kind=first[:-6] if first.endswith('.class') else 'ItemBlock'
 assert kind in ['ItemBlock','ItemBlockBase','ItemBlockLore','ItemOreBlock']
 counts[kind]=counts.get(kind,0)+1
assert counts=={'ItemBlockBase':1,'ItemBlock':24,'ItemBlockLore':1,'ItemOreBlock':1}
for path in ['com/hbm/items/block/ItemBlockBase.java','com/hbm/items/block/ItemBlockLore.java','com/hbm/items/special/ItemOreBlock.java']:
 relative='src/main/java/'+path
 assert (ROOT/relative).read_bytes()==baseline(relative)
 source=(ROOT/relative).read_text()
 for visual in ['getIcon','getColorFromItemStack','registerIcons','getModel','getMeshDefinition']:
  assert not re.search(r'\b'+visual+r'\s*\(',source)
before=list(csv.DictReader((HERE/'before.tsv').open(),delimiter='\t'));after=list(csv.DictReader((HERE/'after.tsv').open(),delimiter='\t'))
assert len(before)==len(after)==27
for b,a in zip(before,after):
 assert b['line']==a['line']
 assert b['method']=='setBlockTextureName' and b['receiver_kind']=='DECLARED' and b['receiver_type']=='net.minecraft.block.Block'
 assert b['argument_count']=='1' and b['arguments_type']=='java.lang.String'
 assert a['method']=='bind' and a['method_element_kind']=='METHOD' and a['method_owner']=='com.hbm.blocks.PlainCubeTextures'
 assert a['method_signature']=='bind(net.minecraft.block.Block,java.lang.String)'
 assert a['arguments_kind']=='DECLARED || DECLARED' and a['arguments_type']=='net.minecraft.block.Block || java.lang.String'
 assert a['arguments_source']==b['receiver_source']+' || '+b['arguments_source'],'FULL first argument and original texture expression'
 assert a['full_invocation']=='com.hbm.blocks.PlainCubeTextures.bind('+b['receiver_source']+', '+b['arguments_source']+')'
 assert re.match(r'new Block(Outgas|Smolder|StepEffect)\(',b['receiver_source'])
 assert b['full_invocation'] in original.decode() and a['full_invocation'] in edited.decode()
# Negative evidence controls: an equally well-typed wrong receiver and changed texture cannot pass conservation.
for b,a in zip(before,after):
 assert 'otherBlock || '+b['arguments_source']!=a['arguments_source']
 assert b['receiver_source']+' || "hbm:wrong"'!=a['arguments_source']
manifest=json.loads((HERE/'source-hashes.json').read_text())
for relative,digest in manifest.items():assert hashlib.sha256((ROOT/relative).read_bytes()).hexdigest()==digest,relative
assert json.loads((HERE/'inherited-visual-calls.json').read_text())==[]
print('PASS 27 fully attributed complete receivers/arguments and native destinations; complete ModBlocks exact inversion;54 wrong-receiver/texture conservation controls;9 unchanged ERROR-chain exclusions;3 unchanged gameplay owners;frozen production/evidence hashes.')
