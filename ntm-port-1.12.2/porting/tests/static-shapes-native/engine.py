#!/usr/bin/env python3
"""Pin real geometry dispatch and original Forge patch review, independently of projected owners."""
import argparse,csv,hashlib,io,json,re,subprocess,zipfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--legacy-jar',type=Path,required=True);p.add_argument('--legacy-srg',type=Path,required=True);p.add_argument('--legacy-forge',type=Path,required=True);p.add_argument('--record',action='store_true');a=p.parse_args();H=Path(__file__).resolve().parent;cp=a.classpath_file.read_text().strip();sha=lambda b:hashlib.sha256(b).hexdigest()
assert sha(a.legacy_jar.read_bytes())=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
def dump(n,cp):return subprocess.check_output([str(a.java_home/'bin/javap'),'-classpath',cp,'-p','-c',n],text=True)
allbc={n:dump(n,str(a.legacy_jar)) for n in ['aji','ajr','azt']};allbc.update({n:dump(n,cp) for n in ['net.minecraft.block.Block','net.minecraft.block.BlockBush','net.minecraft.block.state.BlockStateContainer$StateImplementation','net.minecraft.util.math.AxisAlignedBB','net.minecraft.world.World']})
def method(n,sig):
 t=allbc[n];start=t.index('  '+sig+'\n');tail=t[start:];m=re.search(r'\n  (?:public|protected|private|static) ',tail[3:]);return tail if not m else tail[:m.start()+3]
methods={}
def keep(key,n,sig):methods[key]=method(n,sig)
for key,sig in [('old_bounds','protected final void a(float, float, float, float, float, float);'),('old_selection','public azt g(ahb, int, int, int);'),('old_collision','public azt a(ahb, int, int, int);'),('old_add_collision','public void a(ahb, int, int, int, azt, java.util.List, sa);'),('old_ray','public azu a(ahb, int, int, int, azw, azw);'),('old_state_bounds_noop','public void a(ahl, int, int, int);'),('old_item_bounds_noop','public void g();')]:keep(key,'aji',sig)
keep('old_bush_collision','ajr','public azt a(ahb, int, int, int);')
keep('old_aabb','azt','protected azt(double, double, double, double, double, double);')
B='net.minecraft.block.Block';S='net.minecraft.block.state.BlockStateContainer$StateImplementation';U='net.minecraft.block.BlockBush'
for key,sig in [('native_bounds','public net.minecraft.util.math.AxisAlignedBB getBoundingBox(net.minecraft.block.state.IBlockState, net.minecraft.world.IBlockAccess, net.minecraft.util.math.BlockPos);'),('native_collision','public net.minecraft.util.math.AxisAlignedBB getCollisionBoundingBox(net.minecraft.block.state.IBlockState, net.minecraft.world.IBlockAccess, net.minecraft.util.math.BlockPos);'),('native_selection','public net.minecraft.util.math.AxisAlignedBB getSelectedBoundingBox(net.minecraft.block.state.IBlockState, net.minecraft.world.World, net.minecraft.util.math.BlockPos);'),('native_ray','public net.minecraft.util.math.RayTraceResult collisionRayTrace(net.minecraft.block.state.IBlockState, net.minecraft.world.World, net.minecraft.util.math.BlockPos, net.minecraft.util.math.Vec3d, net.minecraft.util.math.Vec3d);'),('native_add_collision','public void addCollisionBoxToList(net.minecraft.block.state.IBlockState, net.minecraft.world.World, net.minecraft.util.math.BlockPos, net.minecraft.util.math.AxisAlignedBB, java.util.List<net.minecraft.util.math.AxisAlignedBB>, net.minecraft.entity.Entity, boolean);')]:keep(key,B,sig)
keep('native_bush_collision',U,'public net.minecraft.util.math.AxisAlignedBB getCollisionBoundingBox(net.minecraft.block.state.IBlockState, net.minecraft.world.IBlockAccess, net.minecraft.util.math.BlockPos);')
for key,sig in [('native_state_bounds','public net.minecraft.util.math.AxisAlignedBB getBoundingBox(net.minecraft.world.IBlockAccess, net.minecraft.util.math.BlockPos);'),('native_state_collision','public net.minecraft.util.math.AxisAlignedBB getCollisionBoundingBox(net.minecraft.world.IBlockAccess, net.minecraft.util.math.BlockPos);'),('native_state_ray','public net.minecraft.util.math.RayTraceResult collisionRayTrace(net.minecraft.world.World, net.minecraft.util.math.BlockPos, net.minecraft.util.math.Vec3d, net.minecraft.util.math.Vec3d);')]:keep(key,S,sig)
keep('native_aabb','net.minecraft.util.math.AxisAlignedBB','public net.minecraft.util.math.AxisAlignedBB(double, double, double, double, double, double);')
keep('native_world_ray','net.minecraft.world.World','public net.minecraft.util.math.RayTraceResult rayTraceBlocks(net.minecraft.util.math.Vec3d, net.minecraft.util.math.Vec3d, boolean, boolean, boolean);')
keep('native_world_collision','net.minecraft.world.World','private boolean getCollisionBoxes(net.minecraft.entity.Entity, net.minecraft.util.math.AxisAlignedBB, boolean, java.util.List<net.minecraft.util.math.AxisAlignedBB>);')
assert 'invoke' not in methods['old_state_bounds_noop'] and 'invoke' not in methods['old_item_bounds_noop']
assert methods['old_bounds'].count('f2d')==6
assert 'aconst_null' in methods['old_bush_collision'] and 'NULL_AABB' in methods['native_bush_collision']
for k in ['old_selection','old_collision']:assert all('Field '+n+':D' in methods[k] for n in 'BCDEFG')
assert 'Method a:(Lahl;III)V' in methods['old_ray']
assert 'Method a:(Lahb;III)Lazt;' in methods['old_add_collision']
for k in ['native_selection','native_collision','native_ray']:assert 'IBlockState.getBoundingBox:' in methods[k]
assert 'IBlockState.getCollisionBoundingBox:' in methods['native_add_collision']
assert 'IBlockState.addCollisionBoxToList:' in methods['native_world_collision']
assert 'IBlockState.collisionRayTrace:' in methods['native_world_ray']
assert 'Math.min' in methods['native_aabb'] and 'Math.max' in methods['native_aabb']
# No inverted/NaN/infinite inputs are selected: exact source and runtime checks prove finite ordered bounds.
with zipfile.ZipFile(a.legacy_srg) as z:srg=z.read('joined.srg').decode()
with zipfile.ZipFile(a.legacy_forge) as z:
 names={r['name']:r['searge'] for r in csv.DictReader(io.StringIO(z.read('conf/methods.csv').decode()))}
 with zipfile.ZipFile(io.BytesIO(z.read('forgepatches.zip'))) as f:
  patches={n:f.read(n).decode() for n in ['net/minecraft/block/Block.java.patch','net/minecraft/block/BlockBush.java.patch']}
for name in ['setBlockBounds','setBlockBoundsBasedOnState','setBlockBoundsForItemRender','getSelectedBoundingBoxFromPool','getCollisionBoundingBoxFromPool','collisionRayTrace','addCollisionBoxesToList']:
 assert 'net/minecraft/block/Block/'+names[name] in srg
 # Additions/deletions in Forge1614 do not change the selected geometry callbacks.
 for patch in patches.values():
  assert not any(names[name] in l for l in patch.splitlines() if l.startswith(('+','-')) and not l.startswith(('+++','---'))),(name,'Forge geometry patch requires review')
report={'scope':'Actual original Block/BlockBush geometry callbacks and native state/world consumers; no full original/native World DDA equivalence claim. Constructor/callback projections only.','original_sha256':sha(a.legacy_jar.read_bytes()),'srg_sha256':sha(a.legacy_srg.read_bytes()),'forge1614_sha256':sha(a.legacy_forge.read_bytes()),'methods':methods,'forge_patches':patches}
f=H/'proof/engine.json'
if a.record:f.write_text(json.dumps(report,indent=2)+'\n')
else:assert report==json.loads(f.read_text()),'engine drift requires review'
print('PASS',len(methods),'actual original/native geometry methods; genuine Block/BlockBush dispatch; Forge1614 patch audit; finite ordered AABB scope')
