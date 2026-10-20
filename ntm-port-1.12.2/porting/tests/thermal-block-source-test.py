#!/usr/bin/env python3
"""Independent source/provenance guard for bounded native thermal block models."""
import hashlib,json,re
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2];SRC=ROOT/'src/main/java';RES=ROOT/'src/main/resources/assets/hbm'
m=json.loads((ROOT/'porting/tests/thermal-block-source-manifest.json').read_text())
for item in m['unchanged_assets']:
 path=ROOT/item['path'];assert path.is_file() and hashlib.sha256(path.read_bytes()).hexdigest()==item['sha256'],'Changed original asset '+item['path']
old=ROOT.parent/'ntm-original-pristine'
if old.is_dir():
 for item in m['original_sources']:
  assert hashlib.sha256((old/item['path']).read_bytes()).hexdigest()==item['sha256'],'Wrong pristine oracle '+item['path']
classes=['MachineBrickFurnace','MachineRtgFurnace','MachineDiFurnaceRTG','MachineDiFurnace','MachineDiFurnaceExtension']
for cls in classes:
 text=(SRC/('com/hbm/blocks/machine/'+cls+'.java')).read_text()
 for bad in ['IIcon','RenderingRegistry','registerBlockIcons','getBlockMetadata(', 'setBlockMetadataWithNotify(', 'Material.iron','Material.rock','func_147453_f','func_149730_j()','spawnParticle("']:
  assert bad not in text,'Residual legacy API in '+cls+': '+bad
 assert 'EnumHand hand' in text and 'BlockPos pos' in text,'Native full callback signatures '+cls
 assert 'updateBlockState(boolean isProcessing, World world, int x, int y, int z)' in text or cls.endswith('Extension'),'Preserve thermal tile update caller signature'
base=(SRC/'com/hbm/blocks/machine/BlockThermalFurnace.java').read_text();assert 'PropertyInteger.create("metadata", 0, 15)' in base and 'EnumBlockRenderType.MODEL' in base
assert not (SRC/'com/hbm/render/block/RenderDiFurnaceExtension.java').exists(),'Retire only targeted legacy extension renderer'
assert 'new RenderDiFurnaceExtension()' not in (SRC/'com/hbm/main/ClientProxy.java').read_text(),'No duplicate old renderer registration'
registry=(SRC/'com/hbm/blocks/ModBlocks.java').read_text()
ids=['machine_furnace_brick','machine_rtg_furnace','machine_difurnace_rtg','machine_difurnace']
for stem in ids:
 for active in ['off','on']:
  name=stem+'_'+active;line=next(x for x in registry.splitlines() if re.search(r'\b'+name+r'\s*=\s*new ',x))
  assert '"'+name+'"' in line,'Preserve save/registry naming '+name
  assert ('.setLightLevel(1.0F)' in line)==(active=='on'),'Original registry light '+name
  asset=RES/('blockstates/tile.'+name+'.json');data=json.loads(asset.read_text());variants=data['variants'];assert len(variants)==(32 if stem=='machine_difurnace' else 16),'All state variants '+name
  for meta in range(16):
   keys=['covered='+str(c).lower()+',metadata='+str(meta) for c in [False,True]] if stem=='machine_difurnace' else ['metadata='+str(meta)]
   for key in keys:
    assert key in variants,'Missing native state '+key
    model=variants[key];assert model['model'].startswith('hbm:native_thermal/'),'Real native loader, no missing placeholders'
    assert not any(k in model for k in ['x','y','uvlock']),'No extra rotation '+key
for tile in ['TileEntityFurnaceBrick','TileEntityRtgFurnace','TileEntityDiFurnaceRTG','TileEntityDiFurnace']:
 text=re.sub(r'//[^\n]*','',(SRC/('com/hbm/tileentity/machine/'+tile+'.java')).read_text());assert re.search(r'boolean shouldRefresh\([^)]*\)\s*\{\s*return oldState\.getBlock\(\) != newState\.getBlock\(\);',text),'Exact production native tile lifecycle '+tile
handler=(SRC/'com/hbm/main/ThermalFurnaceModelHandler.java').read_text();assert 'ModelLoaderRegistry.registerLoader' in handler and 'implements ICustomModelLoader' in handler and 'onResourceManagerReload' in handler
assert 'ModelBakeEvent' not in handler,'No late missing-model replacement stand-in'
for item in m.get('reviewed_production',[]):
 assert hashlib.sha256((ROOT/item['path']).read_bytes()).hexdigest()==item['sha256'],'Reviewed native thermal source changed '+item['path']
print('PASS thermal source: pristine selectors/render provenance, '+str(len(m['unchanged_assets']))+' unchanged assets, native full-block signatures, 160 cube state variants, original registry lighting and exact tile refresh policy')
