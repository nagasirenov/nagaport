#!/usr/bin/env python3
"""Exact CP79 inversion, original owner/resource audit, independent compiler receiver proof."""
import argparse,csv,hashlib,json,re,subprocess,collections
from pathlib import Path
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2];REPO=ROOT.parent
BASE='77b595c32b46e2f9309bcf9924d654ddc29a3c95'
p=argparse.ArgumentParser();p.add_argument('--original',type=Path,default=Path('/workspace/shared/nagaport/ntm-original-pristine'));p.add_argument('--resources',type=Path,default=Path('/workspace/shared/nagaport/ntm-port-1.12.2/src/main/resources'));a=p.parse_args()
def baseline(relative):return subprocess.check_output(['git','-C',str(REPO),'show',BASE+':ntm-port-1.12.2/'+relative])
rows=json.loads((HERE/'sites.json').read_text());assert len(rows)==15
path='src/main/java/com/hbm/blocks/ModBlocks.java';original=baseline(path);edited=(ROOT/path).read_bytes();inverse=edited
assert collections.Counter(r['owner'] for r in rows)=={'BlockDepth':7,'BlockDepthOre':8}
assert collections.Counter(r['item'] for r in rows)=={'ItemBlockBase':15}
for r in rows:
 before=r['before'].encode();after=r['after'].encode()
 assert original.count(before)==1 and edited.count(after)==1
 inverse=inverse.replace(after,before)
 assert re.search(r'new '+r['owner']+r'\(',r['before']) and r['before'].count('new ')==1
 assert r['registration'].encode() in original and r['registration'].encode() in edited
 assert r['line']==original.decode().splitlines().index(r['before'])+1
assert inverse==original,'Full ModBlocks must invert exactly, including all unselected sites and CRLF'
# This guard extends precisely two exact owner names; no subclass admission or model behavior changes.
path='src/main/java/com/hbm/blocks/PlainCubeTextures.java';old=baseline(path).decode();new=(ROOT/path).read_text()
expected=old.replace('StepEffect and Hazard/HotHazard cubes only.','StepEffect, Hazard/HotHazard and Depth/DepthOre cubes only.').replace('!owner.equals("com.hbm.blocks.generic.BlockHotHazard"))','!owner.equals("com.hbm.blocks.generic.BlockHotHazard") &&\n                !owner.equals("com.hbm.blocks.generic.BlockDepth") &&\n                !owner.equals("com.hbm.blocks.generic.BlockDepthOre"))')
assert new==expected
visual=r'\b(?:getIcon|registerBlockIcons|getRenderType|getRenderColor|colorMultiplier|setBlockBounds|isOpaqueCube|renderAsNormalBlock|getRenderBlockPass|getBlockTextureFromSide|getBlockTextureName|setBlockTextureName|createBlockState|getStateFromMeta|getMetaFromState|getActualState|getExtendedState|getBoundingBox|hasTileEntity|createTileEntity|createNewTileEntity|getBlockLayer|canRenderInLayer|getLightOpacity)\s*\('
owner_evidence={}
for owner,parent in [('BlockDepth','Block'),('BlockDepthOre','BlockDepth')]:
 path='src/main/java/com/hbm/blocks/generic/'+owner+'.java';current=(ROOT/path).read_bytes();legacy=(a.original/path).read_bytes()
 assert current==baseline(path),'Gameplay owner untouched'
 for source in [current.decode(),legacy.decode()]:
  assert re.search(r'class '+owner+r' extends '+parent+r'\b',source)
  assert not re.search(visual,source),'No custom visual/state path in owner hierarchy'
  assert 'blockIcon' not in source
 assert not re.search(r'\bBlockDepthOre\s*\(',current.decode()) if owner=='BlockDepthOre' else True
 owner_evidence[path]={'current_sha256':hashlib.sha256(current).hexdigest(),'original_sha256':hashlib.sha256(legacy).hexdigest(),'parent':parent}
# Tooltip/rarity code and interfaces cannot introduce a custom icon, tint, mesh, or IBlockMulti marker.
path='src/main/java/com/hbm/items/block/ItemBlockBase.java';s=(ROOT/path).read_bytes();assert s==baseline(path)
legacy_item=(a.original/path).read_text()
assert 'class ItemBlockBase extends ItemBlock' in s.decode() and 'class ItemBlockBase extends ItemBlock' in legacy_item
assert not re.search(r'\b(?:getIcon|getIconFromDamage|getColorFromItemStack|registerIcons|getModel|getMeshDefinition|hasEffect)\s*\(',s.decode())
# Original explicit delegates are equivalent only because the exact block owners
# inherit Block's metadata-independent icon and white render color (binary tested).
assert re.search(r'public IIcon getIconFromDamage\(int meta\)\s*\{\s*return this.field_150939_a.getIcon\(1, meta\);[^}]*\}',legacy_item)
assert re.search(r'public int getColorFromItemStack\(ItemStack stack, int pass\)\s*\{\s*return this.field_150939_a.getRenderColor\(stack.getItemDamage\(\)\);\s*\}',legacy_item)
assert not re.search(r'\b(?:registerIcons|getModel|getMeshDefinition|hasEffect)\s*\(',legacy_item)
assert hashlib.sha256((a.original/path).read_bytes()).hexdigest()==json.loads((HERE/'original-item-audit.json').read_text())['sha256']
for path in ['src/main/java/com/hbm/main/PlainCubeModelHandler.java','src/main/java/com/hbm/render/model/PlainCubeModel.java','src/main/java/com/hbm/blocks/ModSoundTypes.java','src/main/java/com/hbm/blocks/ModSoundType.java','src/main/java/com/hbm/main/ModSoundEvents.java','src/main/java/com/hbm/registry/GameRegistry.java','src/main/java/com/hbm/blocks/IBlockMulti.java','src/main/java/com/hbm/blocks/ITooltipProvider.java']:
 assert (ROOT/path).read_bytes()==baseline(path),path
# Direct selected-field visual consumers would require separately preserving an inherited icon API.
names='|'.join(r['name'] for r in rows);calls=[]
for tree in [ROOT,a.original]:
 for file in (tree/'src/main/java').rglob('*.java'):
  if file.name=='ModBlocks.java':continue
  for number,line in enumerate(file.read_text(errors='replace').splitlines(),1):
   if re.search(r'\b(?:'+names+r')\b',line) and re.search(visual,line):calls.append([str(file.relative_to(tree)),number,line.strip()])
assert calls==json.loads((HERE/'inherited-visual-calls.json').read_text())==[]
for r in json.loads((HERE/'resource-audit.json').read_text()):
 relative=Path(r['path']);data=(a.resources/str(relative).removeprefix('src/main/resources/')).read_bytes();legacy=(a.original/relative).read_bytes()
 assert data==legacy and hashlib.sha256(data).hexdigest()==r['sha256']==r['original_sha256']
before=list(csv.DictReader((HERE/'before.tsv').open(),delimiter='\t'));after=list(csv.DictReader((HERE/'after.tsv').open(),delimiter='\t'));assert len(before)==len(after)==15
for r,b,c in zip(rows,before,after):
 assert b['line']==c['line']==str(r['line'])
 assert b['method']=='setBlockTextureName' and b['receiver_kind']=='DECLARED' and b['receiver_type']=='net.minecraft.block.Block'
 assert b['argument_count']=='1' and b['arguments_type']=='java.lang.String'
 assert c['method']=='bind' and c['method_element_kind']=='METHOD' and c['method_owner']=='com.hbm.blocks.PlainCubeTextures'
 assert c['method_signature']=='bind(net.minecraft.block.Block,java.lang.String)'
 assert c['arguments_kind']=='DECLARED || DECLARED' and c['arguments_type']=='net.minecraft.block.Block || java.lang.String'
 assert c['arguments_source']==b['receiver_source']+' || '+b['arguments_source']
 assert c['full_invocation']=='com.hbm.blocks.PlainCubeTextures.bind('+b['receiver_source']+', '+b['arguments_source']+')'
 assert b['full_invocation'] in r['before'] and c['full_invocation'] in r['after']
 assert 'otherBlock || '+b['arguments_source']!=c['arguments_source']
 assert b['receiver_source']+' || "hbm:wrong"'!=c['arguments_source']
# Compiler evidence is full-source, not a selected-source success claim.
for name in ['before','after']:
 assert 'parse errors=0' in (HERE/(name+'.log')).read_text()
errors=[line.split('\t') for line in (HERE/'after-errors.tsv').read_text().splitlines()]
assert not [r for r in errors if int(r[1]) in {s['line'] for s in rows}],'No selected-line diagnostics remain'
old_diagnostics=collections.Counter((HERE/'before-errors.tsv').read_text().splitlines())
new_diagnostics=collections.Counter((HERE/'after-errors.tsv').read_text().splitlines())
assert not (new_diagnostics-old_diagnostics),'No newly introduced ModBlocks diagnostic'
removed=list((old_diagnostics-new_diagnostics).elements())
assert len(removed)==15 and {int(line.split('\t')[1]) for line in removed}=={r['line'] for r in rows}
assert all('setBlockTextureName' in line for line in removed),'Every removal is the intended obsolete texture call'
for rel,digest in json.loads((HERE/'source-hashes.json').read_text()).items():assert hashlib.sha256((ROOT/rel).read_bytes()).hexdigest()==digest,rel
assert owner_evidence==json.loads((HERE/'owner-audit.json').read_text())
print('PASS 15 full receiver/texture native attributions; 30 negative conservation controls; byte-exact complete CP79 inversion; 7 Depth + 8 DepthOre owners; 15 ItemBlockBase; 15 pristine PNGs; unchanged gameplay/model/sound/registry code; zero selected-line diagnostics.')
