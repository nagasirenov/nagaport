#!/usr/bin/env python3
"""Exact owned-source boundary, pristine texture bytes and narrowly additive reactor hooks."""
import json,hashlib,pathlib,re
r=pathlib.Path(__file__).resolve().parents[2]
manifest=json.loads((r/'porting/tests/universal-production-scope.json').read_text())
for name,record in manifest['production_files'].items():
 assert hashlib.sha256((r/name).read_bytes()).hexdigest()==record['sha256'],'changed owned production input: '+name
hook='''
    /** Original panel metadata changed facing without replacing its stored controls. */
    @Override
    public boolean shouldRefresh(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos,
            net.minecraft.block.state.IBlockState oldState, net.minecraft.block.state.IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
'''
baselines=json.loads((r/'porting/tests/universal-panel-hook-baseline.json').read_text())
for name,record in baselines.items():
 current=(r/name).read_text();assert current.count(hook)==1,'missing/duplicate native retention hook'
 assert hashlib.sha256(record['source'].encode()).hexdigest()==record['sha256']
 assert current.replace(hook,'',1)==record['source'],'unrelated reactor implementation changed: '+name
all_sources=[p for p in (r/'src/main/java').rglob('*.java')]
for parent in ['TileEntityRBMKDisplay','TileEntityRBMKGauge','TileEntityRBMKKeyPad']:
 descendants=[str(p.relative_to(r)) for p in all_sources if re.search(r'\bextends\s+'+parent+r'\b',p.read_text())]
 assert not descendants,'review newly added panel tile descendants: '+str(descendants)
for material in ['sandbags','wood_barrier','rebar','concrete_liquid','rbmk/rbmk_display']:
 relative='src/main/resources/assets/hbm/textures/blocks/'+material+'.png'
 assert (r/relative).read_bytes()==(r.parent/'ntm-original-pristine'/relative).read_bytes(),'original texture changed '+material
for name in ['BlockSandbags','BlockBarrier','BlockWoodStructure','BlockRebar']:
 source=(r/('src/main/java/com/hbm/blocks/generic/'+name+'.java')).read_text()
 assert not re.search(r'\bRenderBlocks(?:NT)?\b|\bTessellator\.instance\b|\bgetBlockMetadata\b|\bsetBlockBounds\b',source),'removed API remains '+name
 assert 'getRenderBoxes(IBlockState state, boolean inventory)' in source
# The same-name source contract has no legacy registry IDs or removed renderer parameters.
interface=(r/'src/main/java/com/hbm/render/block/ISBRHUniversal.java').read_text()
assert not any(x in interface for x in ['RenderingRegistry','renderID','Object renderBlocks'])
client=(r/'src/main/java/com/hbm/main/ClientProxy.java').read_text()
assert not re.search(r'registerBlockHandler\(new RenderISBRHUniversal',client)
blocks=(r/'src/main/java/com/hbm/blocks/ModBlocks.java').read_text()
for name in ['sandbags','wood_barrier','wood_structure','rebar','rbmk_display','rbmk_key_pad','rbmk_gauge']:
 line=next(line for line in blocks.splitlines() if re.match(r'\s*'+name+r'\s*=',line))
 assert '.setTranslationKey("'+name+'")' in line and '.setBlockTextureName(' not in line,'native construction route '+name
 assert 'register('+name+');' in blocks,'existing registry entry dropped '+name
assert 'BlockRebar.renderRebar(Minecraft.getMinecraft().world.loadedTileEntityList, event.getPartialTicks())' in (r/'src/main/java/com/hbm/main/ModEventHandlerClient.java').read_text()
print('PASS exact16 owned production files, three narrowly additive panel retention hooks/no descendants, five unchanged original atlas PNGs and seven parent integration routes')
