#!/usr/bin/env python3
"""Pinned, read-only original/native full-cube consumer proof; no mod compilation claim."""
import argparse,csv,hashlib,io,json,re,subprocess,zipfile
from pathlib import Path
P=argparse.ArgumentParser();P.add_argument('--java-home',type=Path,required=True);P.add_argument('--classpath-file',type=Path,required=True);P.add_argument('--legacy-jar',type=Path,required=True);P.add_argument('--legacy-srg',type=Path,required=True);P.add_argument('--legacy-userdev',type=Path,required=True);P.add_argument('--native-mcp-srg',type=Path,required=True);P.add_argument('--record',action='store_true');a=P.parse_args();HERE=Path(__file__).resolve().parent;cp=a.classpath_file.read_text().strip()
sha=lambda b:hashlib.sha256(b).hexdigest()
assert sha(a.legacy_jar.read_bytes())=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
classes=['net.minecraft.block.Block','net.minecraft.block.state.BlockStateContainer$StateImplementation','net.minecraft.block.state.BlockStateBase','net.minecraft.block.BlockFence','net.minecraft.block.BlockPane','net.minecraft.entity.Entity']
oldclasses=['aji','akz','aoa','ahb','sa']
def dump(n,c):return subprocess.check_output([str(a.java_home/'bin/javap'),'-classpath',c,'-p','-c',n],text=True)
bc={n:dump(n,cp) for n in classes};bc.update({n:dump(n,str(a.legacy_jar)) for n in oldclasses})
def method(n,sig):
 t=bc[n];start=t.index('  '+sig+'\n');tail=t[start:];m=re.search(r'\n  (?:public|protected|private|static) ',tail[3:]);return tail if not m else tail[:m.start()+3]
sections={}
def keep(key,n,sig):sections[key]=method(n,sig);return sections[key]
B='net.minecraft.block.Block';S='net.minecraft.block.state.BlockStateContainer$StateImplementation';F='net.minecraft.block.BlockFence';P='net.minecraft.block.BlockPane'
keep('old_constructor','aji','protected aji(awt);')
keep('old_render_normal','aji','public boolean d();')
keep('old_isBlockNormalCube','aji','public boolean q();')
keep('old_isNormalCube','aji','public boolean r();')
keep('old_cached_fullblock','aji','public boolean j();')
keep('old_side_culling','aji','public boolean a(ahl, int, int, int, int);')
keep('old_canSilkHarvest','aji','protected boolean E();')
keep('old_fence_connection','akz','public boolean e(ahl, int, int, int);')
keep('old_pane_connection','aoa','public final boolean a(aji);')
keep('old_top_surface_vanilla','ahb','public static boolean a(ahl, int, int, int);')
keep('old_suffocation_consumer','sa','public boolean aa();')
keep('native_suffocation_consumer','net.minecraft.entity.Entity','public boolean isEntityInsideOpaqueBlock();')
keep('native_constructor',B,'public net.minecraft.block.Block(net.minecraft.block.material.Material, net.minecraft.block.material.MapColor);')
keep('native_isFullCube',B,'public boolean isFullCube(net.minecraft.block.state.IBlockState);')
keep('native_isFullBlock',B,'public boolean isFullBlock(net.minecraft.block.state.IBlockState);')
keep('native_isBlockNormalCube',B,'public boolean isBlockNormalCube(net.minecraft.block.state.IBlockState);')
keep('native_isNormalCube',B,'public boolean isNormalCube(net.minecraft.block.state.IBlockState);')
keep('native_isNormalCube_world',B,'public boolean isNormalCube(net.minecraft.block.state.IBlockState, net.minecraft.world.IBlockAccess, net.minecraft.util.math.BlockPos);')
keep('native_isTopSolid',B,'public boolean isTopSolid(net.minecraft.block.state.IBlockState);')
keep('native_causesSuffocation',B,'public boolean causesSuffocation(net.minecraft.block.state.IBlockState);')
keep('native_side_culling',B,'public boolean shouldSideBeRendered(net.minecraft.block.state.IBlockState, net.minecraft.world.IBlockAccess, net.minecraft.util.math.BlockPos, net.minecraft.util.EnumFacing);')
keep('native_side_blocks_rendering',B,'public boolean doesSideBlockRendering(net.minecraft.block.state.IBlockState, net.minecraft.world.IBlockAccess, net.minecraft.util.math.BlockPos, net.minecraft.util.EnumFacing);')
keep('native_face_shape',B,'public net.minecraft.block.state.BlockFaceShape getBlockFaceShape(net.minecraft.world.IBlockAccess, net.minecraft.block.state.IBlockState, net.minecraft.util.math.BlockPos, net.minecraft.util.EnumFacing);')
keep('native_side_solid',B,'public boolean isSideSolid(net.minecraft.block.state.IBlockState, net.minecraft.world.IBlockAccess, net.minecraft.util.math.BlockPos, net.minecraft.util.EnumFacing);')
keep('native_canSilkHarvest',B,'protected boolean canSilkHarvest();')
keep('native_state_fullcube',S,'public boolean isFullCube();')
keep('native_state_normalcube',S,'public boolean isNormalCube();')
keep('native_state_constructor',S,'protected net.minecraft.block.state.BlockStateContainer$StateImplementation(net.minecraft.block.Block, com.google.common.collect.ImmutableMap<net.minecraft.block.properties.IProperty<?>, java.lang.Comparable<?>>);')
keep('native_fence_connection',F,'public boolean canConnectTo(net.minecraft.world.IBlockAccess, net.minecraft.util.math.BlockPos, net.minecraft.util.EnumFacing);')
keep('native_fence_neighbor_hook',F,'private boolean canFenceConnectTo(net.minecraft.world.IBlockAccess, net.minecraft.util.math.BlockPos, net.minecraft.util.EnumFacing);')
keep('native_fence_reverse_hook',F,'public boolean canBeConnectedTo(net.minecraft.world.IBlockAccess, net.minecraft.util.math.BlockPos, net.minecraft.util.EnumFacing);')
keep('native_fence_shape',F,'public net.minecraft.block.state.BlockFaceShape getBlockFaceShape(net.minecraft.world.IBlockAccess, net.minecraft.block.state.IBlockState, net.minecraft.util.math.BlockPos, net.minecraft.util.EnumFacing);')
keep('native_pane_connection',P,'public final boolean attachesTo(net.minecraft.world.IBlockAccess, net.minecraft.block.state.IBlockState, net.minecraft.util.math.BlockPos, net.minecraft.util.EnumFacing);')
keep('native_pane_neighbor_hook',P,'public boolean canPaneConnectTo(net.minecraft.world.IBlockAccess, net.minecraft.util.math.BlockPos, net.minecraft.util.EnumFacing);')
keep('native_pane_reverse_hook',P,'public boolean canBeConnectedTo(net.minecraft.world.IBlockAccess, net.minecraft.util.math.BlockPos, net.minecraft.util.EnumFacing);')
# Assertions establish the operative bytecode calls, not just API-spelling correspondence.
assert 'iconst_1' in sections['old_render_normal'] and 'invoke' not in sections['old_render_normal']
assert 'iconst_1' in sections['native_isFullCube'] and 'invoke' not in sections['native_isFullCube']
assert sections['old_constructor'].count('Method c:()Z')==2 and 'Method d:()Z' not in sections['old_constructor']
assert 'IBlockState.isOpaqueCube:()Z' in sections['native_constructor'] and 'isFullCube' not in sections['native_constructor']
assert 'Field fullBlock:Z' in sections['native_constructor'] and 'Field lightOpacity:I' in sections['native_constructor']
assert 'Field fullBlock:Z' in sections['native_isFullBlock'] and 'isFullCube' not in sections['native_isFullBlock']
assert 'Block.isFullCube:(Lnet/minecraft/block/state/IBlockState;)Z' in sections['native_state_fullcube'] and 'putfield' not in sections['native_state_fullcube']
assert 'isFullCube' not in sections['native_state_constructor'] and 'isFullCube' not in bc['net.minecraft.block.state.BlockStateBase']
for k in ['native_isBlockNormalCube','native_isNormalCube','native_isTopSolid','native_causesSuffocation','native_canSilkHarvest']:assert 'IBlockState.isFullCube:()Z' in sections[k],k
for k in ['old_isBlockNormalCube','old_isNormalCube','old_canSilkHarvest']:assert 'Method d:()Z' in sections[k],k
assert 'IBlockState.canProvidePower:()Z' in sections['native_isNormalCube']
assert 'Material.isOpaque:()Z' in sections['native_isNormalCube'] and 'isOpaqueCube' not in sections['native_isNormalCube']
assert 'IBlockState.doesSideBlockRendering:' in sections['native_side_culling'] and 'isFullCube' not in sections['native_side_culling']
assert 'IBlockState.isOpaqueCube:()Z' in sections['native_side_blocks_rendering'] and 'isFullCube' not in sections['native_side_blocks_rendering']
assert 'BlockFaceShape.SOLID' in sections['native_face_shape'] and 'invoke' not in sections['native_face_shape']
assert 'aji.d:()Z' in sections['old_fence_connection'] and 'awt.k:()Z' in sections['old_fence_connection']
assert 'aji.d:()Z' in sections['old_top_surface_vanilla']
assert 'aji.r:()Z' in sections['old_suffocation_consumer']
assert 'IBlockState.causesSuffocation:()Z' in sections['native_suffocation_consumer']
assert 'aji.j:()Z' in sections['old_pane_connection'] and 'aji.d:()Z' not in sections['old_pane_connection']
for k in ['native_fence_connection','native_pane_connection']:
 assert 'IBlockState.getBlockFaceShape:' in sections[k] and 'isFullCube' not in sections[k]
for k in ['native_fence_neighbor_hook','native_pane_neighbor_hook']:assert 'Block.canBeConnectedTo:' in sections[k]
with zipfile.ZipFile(a.legacy_srg) as z: srg=z.read('joined.srg')
with zipfile.ZipFile(a.legacy_userdev) as z:
 names=z.read('conf/methods.csv');patchjar=z.read('forgepatches.zip')
 with zipfile.ZipFile(io.BytesIO(patchjar)) as pz:
  patches={n:pz.read(n).decode() for n in ['net/minecraft/block/Block.java.patch','net/minecraft/block/BlockPane.java.patch','net/minecraft/world/World.java.patch','net/minecraft/entity/Entity.java.patch']}
assert 'MD: aji/d ()Z net/minecraft/block/Block/func_149686_d ()Z' in srg.decode()
native_mapping=a.native_mcp_srg.read_bytes()
assert 'MD: net/minecraft/block/Block/isFullCube (Lnet/minecraft/block/state/IBlockState;)Z net/minecraft/block/Block/func_149686_d (Lnet/minecraft/block/state/IBlockState;)Z' in native_mapping.decode()
name_rows={r['searge']:r for r in csv.DictReader(io.StringIO(names.decode()))}
assert name_rows['func_149686_d']['name']=='renderAsNormalBlock'
assert '+        return func_149688_o().func_76218_k() && func_149686_d() && !func_149744_f();' in patches['net/minecraft/block/Block.java.patch']
assert 'func_150098_a(world.func_147439_a(x, y, z)) ||' in patches['net/minecraft/block/BlockPane.java.patch'] and 'world.isSideSolid' in patches['net/minecraft/block/BlockPane.java.patch']
assert '+        return block.isSideSolid(p_147466_0_, p_147466_1_, p_147466_2_, p_147466_3_, ForgeDirection.UP);' in patches['net/minecraft/world/World.java.patch']
assert 'func_70094_T' not in patches['net/minecraft/entity/Entity.java.patch']
class_hashes={}
for entry in cp.split(':'):
 path=Path(entry)
 if not path.is_file() or not zipfile.is_zipfile(path):continue
 with zipfile.ZipFile(path) as z:
  for n in classes:
   k=n.replace('.','/')+'.class'
   if k not in class_hashes and k in z.namelist():class_hashes[k]=sha(z.read(k))
assert len(class_hashes)==len(classes)
report={'scope':'Pinned original signed client plus Forge1614 patch review; native 1.12.2 real dependency bytecode. Not full-mod or visual equivalence.',
 'original_jar_sha256':sha(a.legacy_jar.read_bytes()),'legacy_srg_zip_sha256':sha(a.legacy_srg.read_bytes()),'legacy_userdev_sha256':sha(a.legacy_userdev.read_bytes()),'legacy_joined_srg_sha256':sha(srg),'legacy_methods_csv_sha256':sha(names),'native_mcp_srg_sha256':sha(native_mapping),'native_class_sha256':class_hashes,'mapping':name_rows['func_149686_d'],'forge_patches':patches,'methods':sections}
f=HERE/'engine-audit.json'
if a.record:f.write_text(json.dumps(report,indent=2)+'\n')
else:assert report==json.loads(f.read_text()),'Bytecode, original engine, mapping or Forge patches changed; re-audit required.'
print('PASS original aji.d() -> func_149686_d -> renderAsNormalBlock mapping; 35 pinned native/original methods; no full-cube constructor/state cache; normalCube/topSolid/suffocation/silk consumers; opacity culling distinct; fence/pane shape/hook divergence explicitly retained.')
