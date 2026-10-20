#!/usr/bin/env python3
"""Frozen CP81 inverse, true owner closure, original resources and full-source attribution."""
import argparse,collections,csv,hashlib,json,re,subprocess
from pathlib import Path
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2];REPO=ROOT.parent
BASE='242867906a53eb7da03c879f476ced61c1170e14'
p=argparse.ArgumentParser();p.add_argument('--original',type=Path,required=True);p.add_argument('--resources',type=Path,required=True);a=p.parse_args()
def old(path):return subprocess.check_output(['git','-C',str(REPO),'show',BASE+':ntm-port-1.12.2/'+path])
rows=json.loads((HERE/'sites.json').read_text());assert len(rows)==15
owners={'BlockNuclearWaste':3,'BlockHazardFalling':3,'BlockNetherCoal':1,'BlockSpeedy':2,'BlockNoDrop':2,'BlockRadResistant':1,'FragileBrick':1,'BlockMeteoriteTreasure':1,'BlockFalling':1}
assert collections.Counter(r['owner'] for r in rows)==owners
assert collections.Counter(r['item'] for r in rows)=={'ItemBlock':10,'ItemBlockBlastInfo':4,'ItemBlockBase':1}
path='src/main/java/com/hbm/blocks/ModBlocks.java';baseline=old(path);edited=(ROOT/path).read_bytes();inverse=edited
for r in rows:
 b=r['before'].encode();c=r['after'].encode();assert baseline.count(b)==edited.count(c)==1
 inverse=inverse.replace(c,b);assert r['line']==baseline.decode().splitlines().index(r['before'])+1
 assert r['registration'].encode() in baseline and r['registration'].encode() in edited
 assert r['before'].count('new ')==1 and '.setStepSound(' not in r['before']
 assert re.search(r'new '+r['owner']+r'\(',r['before'])
assert inverse==baseline,'Byte-exact ModBlocks inverse, including CRLF and every unselected expression'
assert 'GameRegistry.registerBlock(b, ItemBlockBase.class, b.getTranslationKey());' in edited.decode()
path='src/main/java/com/hbm/blocks/PlainCubeTextures.java';before=old(path).decode();after=(ROOT/path).read_text()
addition=' &&\n'+''.join('                !owner.equals("'+('net.minecraft.block.' if o=='BlockFalling' else 'com.hbm.blocks.generic.')+o+'")'+(' &&\n' if i<len(owners)-1 else ')') for i,o in enumerate(sorted(owners)))
expected=before.replace(' and Depth/DepthOre cubes only.',' and Depth/DepthOre cubes, plus the explicitly audited remainder only.').replace('!owner.equals("com.hbm.blocks.generic.BlockDepthOre"))','!owner.equals("com.hbm.blocks.generic.BlockDepthOre")'+addition)
assert after==expected,'Only nine exact class-name admissions, no broad subclass permission'
visual=r'\b(?:getIcon|registerBlockIcons|getRenderType|getRenderColor|colorMultiplier|setBlockBounds|isOpaqueCube|renderAsNormalBlock|getRenderBlockPass|getBlockTextureFromSide|getBlockTextureName|setBlockTextureName|createBlockState|getStateFromMeta|getMetaFromState|getActualState|getExtendedState|getBoundingBox|hasTileEntity|createTileEntity|createNewTileEntity|getBlockLayer|canRenderInLayer|getLightOpacity)\s*\('
parents={'BlockNuclearWaste':'BlockHazard','BlockHazard':'Block','BlockHazardFalling':'BlockFalling','BlockNetherCoal':'BlockOutgas','BlockOutgas':'Block','BlockSpeedy':'Block','BlockNoDrop':'Block','BlockRadResistant':'Block','FragileBrick':'Block','BlockMeteoriteTreasure':'Block'}
owner_audit={}
for owner,parent in parents.items():
 path='src/main/java/com/hbm/blocks/generic/'+owner+'.java';current=(ROOT/path).read_bytes();legacy=(a.original/path).read_bytes();assert current==old(path)
 for text in [current.decode(),legacy.decode()]:
  assert re.search(r'class '+owner+r' extends '+parent+r'\b',text)
  # Outgas calls the inherited state accessor for its drop check; it does not override state or rendering.
  audited=text.replace('this.getItemDropped(getStateFromMeta(0), rand, fortune)', 'this.getItemDropped(INHERITED_STATE, rand, fortune)') if owner=='BlockOutgas' else text
  assert not re.search(visual,audited),owner
  assert 'blockIcon' not in text and 'IBlockMulti' not in text
 owner_audit[path]={'parent':parent,'current_sha256':hashlib.sha256(current).hexdigest(),'original_sha256':hashlib.sha256(legacy).hexdigest()}
assert owner_audit==json.loads((HERE/'owner-audit.json').read_text())
for name in ['BlockNuclearWaste']:
 assert not re.search(r'\b'+name+r'\s*\(',(ROOT/'src/main/java/com/hbm/blocks/generic'/ (name+'.java')).read_text()),'Implicit constructor preserved'
# No affected NTM gameplay, sound, item callbacks, registration implementation or native model changed.
for path in ['com/hbm/main/PlainCubeModelHandler.java','com/hbm/render/model/PlainCubeModel.java','com/hbm/registry/GameRegistry.java','com/hbm/main/LegacyBaseModelHandler.java','com/hbm/main/LegacyIconRegistrationHandler.java','com/hbm/main/ModSoundEvents.java','com/hbm/blocks/ModSoundTypes.java','com/hbm/blocks/ModSoundType.java','com/hbm/blocks/IBlockMulti.java','com/hbm/blocks/ITooltipProvider.java','com/hbm/blocks/IStepTickReceiver.java','com/hbm/blocks/IRadResistantBlock.java','com/hbm/items/block/ItemBlockBase.java','com/hbm/items/block/ItemBlockBlastInfo.java']:
 assert (ROOT/'src/main/java'/path).read_bytes()==old('src/main/java/'+path),path
for name,parent in [('ItemBlockBase','ItemBlock'),('ItemBlockBlastInfo','ItemBlockBase')]:
 path='src/main/java/com/hbm/items/block/'+name+'.java';current=(ROOT/path).read_text();legacy=(a.original/path).read_text()
 assert 'class '+name+' extends '+parent in current and 'class '+name+' extends '+parent in legacy
 assert not re.search(r'\b(?:getIcon|getIconFromDamage|getColorFromItemStack|registerIcons|getModel|getMeshDefinition|hasEffect)\s*\(',current)
 if name=='ItemBlockBase':
  assert re.search(r'public IIcon getIconFromDamage\(int meta\)\s*\{\s*return this.field_150939_a.getIcon\(1, meta\);[^}]*\}',legacy)
  assert re.search(r'public int getColorFromItemStack\(ItemStack stack, int pass\)\s*\{\s*return this.field_150939_a.getRenderColor\(stack.getItemDamage\(\)\);\s*\}',legacy)
 else:assert not re.search(r'\b(?:getIcon|getIconFromDamage|getColorFromItemStack|registerIcons|getModel|getMeshDefinition|hasEffect)\s*\(',legacy)
# No renderer or other source explicitly consumes selected legacy face/tint methods.
names='|'.join(r['name'] for r in rows);calls=[]
for tree in [ROOT,a.original]:
 for file in (tree/'src/main/java').rglob('*.java'):
  if file.name=='ModBlocks.java':continue
  for n,line in enumerate(file.read_text(errors='replace').splitlines(),1):
   if re.search(r'\b(?:'+names+r')\b',line) and re.search(visual,line):calls.append([str(file.relative_to(tree)),n,line.strip()])
assert calls==json.loads((HERE/'inherited-visual-calls.json').read_text())
# Both original concrete consumers have already been natively ported to the same atlas resource.
for name in ['RenderConveyorLift','RenderConveyorChute']:
 path='src/main/java/com/hbm/render/block/'+name+'.java';assert (ROOT/path).read_bytes()==old(path);assert 'ConveyorMaterials.uniform(materials.concrete)' in (ROOT/path).read_text()
path='src/main/java/com/hbm/main/ConveyorBlockModelHandler.java';assert (ROOT/path).read_bytes()==old(path)
text=(ROOT/path).read_text();assert 'textures.add(new ResourceLocation(RefStrings.MODID,"blocks/concrete"))' in text and 'concrete=sprites.apply(new ResourceLocation(RefStrings.MODID,"blocks/concrete"))' in text
for tree in [ROOT,a.original]:
 client=(tree/'src/main/java/com/hbm/main/ClientProxy.java').read_text()
 assert not re.search(r'\b(?:'+names+'|'+'|'.join(parents)+r')\b',client),'No direct custom renderer registration'
for r in json.loads((HERE/'resource-audit.json').read_text()):
 path=Path(r['path']);current=(a.resources/str(path).removeprefix('src/main/resources/')).read_bytes();legacy=(a.original/path).read_bytes()
 assert current==legacy and hashlib.sha256(current).hexdigest()==r['sha256']==r['original_sha256']
for r in rows:
 meta=a.resources/('assets/hbm/textures/blocks/'+r['texture']+'.png.mcmeta');assert meta.exists()==(r['name']=='ore_nether_coal')
 if meta.exists():assert json.loads(meta.read_text())=={'animation':{'frametime':4}}
before=list(csv.DictReader((HERE/'before.tsv').open(),delimiter='\t'));after=list(csv.DictReader((HERE/'after.tsv').open(),delimiter='\t'));assert len(before)==len(after)==15
for r,b,c in zip(rows,before,after):
 assert b['line']==c['line']==str(r['line']) and b['method']=='setBlockTextureName'
 assert b['receiver_kind']=='DECLARED' and b['receiver_type']=='net.minecraft.block.Block'
 assert b['argument_count']=='1' and b['arguments_type']=='java.lang.String'
 assert c['method']=='bind' and c['method_element_kind']=='METHOD' and c['method_owner']=='com.hbm.blocks.PlainCubeTextures'
 assert c['method_signature']=='bind(net.minecraft.block.Block,java.lang.String)'
 assert c['arguments_kind']=='DECLARED || DECLARED' and c['arguments_type']=='net.minecraft.block.Block || java.lang.String'
 assert c['arguments_source']==b['receiver_source']+' || '+b['arguments_source']
 assert c['full_invocation']=='com.hbm.blocks.PlainCubeTextures.bind('+b['receiver_source']+', '+b['arguments_source']+')'
 assert b['full_invocation'] in r['before'] and c['full_invocation'] in r['after']
 assert 'otherBlock || '+b['arguments_source']!=c['arguments_source'] and b['receiver_source']+' || "hbm:wrong"'!=c['arguments_source']
old_diag=collections.Counter((HERE/'before-errors.tsv').read_text().splitlines());new_diag=collections.Counter((HERE/'after-errors.tsv').read_text().splitlines())
assert not new_diag-old_diag;removed=list((old_diag-new_diag).elements());assert len(removed)==15
assert {int(l.split('\t')[1]) for l in removed}=={r['line'] for r in rows}
assert all('setBlockTextureName' in l for l in removed)
assert not [l for l in new_diag if int(l.split('\t')[1]) in {r['line'] for r in rows}]
for name,count in [('before',12471),('after',12456)]:
 text=(HERE/(name+'.log')).read_text();assert 'Parsed 4065 units; parse errors=0' in text and 'source errors='+str(count) in text
for path,digest in json.loads((HERE/'source-hashes.json').read_text()).items():assert hashlib.sha256((ROOT/path).read_bytes()).hexdigest()==digest,path
print('PASS CP81 byte-exact full source inverse; nine exact plain-cube owners and complete parent closure; 15 compiler-attributed native bindings; 30 conservation controls; 15 pristine PNGs plus unchanged 4-tick animation metadata; unchanged gameplay/sound/registry/model; exactly 15 removed diagnostics and none added. Attribution is not a full Gradle compile.')
