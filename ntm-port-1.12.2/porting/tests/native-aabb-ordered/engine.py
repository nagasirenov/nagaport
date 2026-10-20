#!/usr/bin/env python3
"""Verify hashes and inspect actual engine bytecode; no engine binaries retained."""
import hashlib,json,os,pathlib,re,subprocess,zipfile
H=pathlib.Path(__file__).resolve().parent;J=pathlib.Path(os.environ.get('JAVA8_HOME','/workspace/shared/nagaport-toolchain/java8'))
CP=pathlib.Path(os.environ.get('NATIVE_CP_FILE','/tmp/nagaport-ae2-native-classpath.txt')).read_text().strip()
OLD=pathlib.Path(os.environ.get('LEGACY_CLIENT_JAR','/tmp/nagaport-legacy-client.jar'))
record=json.loads((H/'binary-inputs.json').read_text());assert hashlib.sha256(OLD.read_bytes()).hexdigest()==record['original_jar_sha256']
def javap(cp,name):return subprocess.check_output([str(J/'bin/javap'),'-p','-c','-classpath',cp,name],text=True)
def method(s,signature):
 start=s.index('  '+signature);end=s.find('\n  ',start+3)
 # A declaration is exactly two spaces; bytecode lines begin four or more spaces.
 match=re.search(r'\n  \S',s[start+3:]);end=start+3+match.start() if match else len(s)
 return s[start:end]
old=javap(str(OLD),'azt');native=javap(CP,'net.minecraft.util.math.AxisAlignedBB');tile=javap(CP,'net.minecraft.tileentity.TileEntity');vec=javap(CP,'net.minecraft.util.math.Vec3i');block=javap(CP,'net.minecraft.util.math.BlockPos');mutable=javap(CP,'net.minecraft.util.math.BlockPos$MutableBlockPos')
a=method(old,'public static azt a(double, double, double, double, double, double);');b=method(old,'protected azt(double, double, double, double, double, double);');c=method(native,'public net.minecraft.util.math.AxisAlignedBB(double, double, double, double, double, double);')
assert 'new ' in a and b.count('putfield')==6 and 'Math.' not in b
assert c.count('Math.min:(DD)D')==3 and c.count('Math.max:(DD)D')==3
setpos=method(tile,'public void setPos(net.minecraft.util.math.BlockPos);');assert 'BlockPos.toImmutable' in setpos and 'Field pos:' in setpos
getters=[method(vec,'public int '+x+'();') for x in ['getX','getY','getZ']]
assert all('getfield' in g and 'invoke' not in g and 'ireturn' in g for g in getters)
assert all('private final int '+x+';' in vec for x in 'xyz')
imm=method(block,'public net.minecraft.util.math.BlockPos toImmutable();');mut=method(mutable,'public net.minecraft.util.math.BlockPos toImmutable();')
assert 'aload_0' in imm and 'areturn' in imm and 'new ' in mut and 'BlockPos' in mut
items=[('original factory',a),('original raw constructor',b),('native normalizing constructor',c),('native setPos',setpos),('native immutable identity',imm),('native mutable snapshot',mut)]+list(zip(['native '+x for x in ['getX','getY','getZ']],getters))
(H/'proof/engine-methods.txt').write_text('\n\n'.join(name+'\n'+body.rstrip() for name,body in items)+'\n')
classes=['net/minecraft/util/math/AxisAlignedBB.class','net/minecraft/tileentity/TileEntity.class','net/minecraft/util/math/Vec3i.class','net/minecraft/util/math/BlockPos.class','net/minecraft/util/math/BlockPos$MutableBlockPos.class']
hashes={}
for entry in CP.split(':'):
 if pathlib.Path(entry).is_file() and zipfile.is_zipfile(entry):
  with zipfile.ZipFile(entry) as z:
   for name in classes:
    if name in z.namelist() and name not in hashes:hashes[name]=hashlib.sha256(z.read(name)).hexdigest()
assert len(hashes)==len(classes);assert hashes[classes[0]]==record['native']['class_sha256']
(H/'proof/engine-hashes.json').write_text(json.dumps(hashes,indent=2)+'\n')
print('PASS actual original raw factory vs native normalizing constructor; native TileEntity.setPos calls toImmutable; native Vec3i getters read final int fields; mutable BlockPos snapshots verified. No binaries copied to evidence.')
