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
old=javap(str(OLD),'azt');native=javap(CP,'net.minecraft.util.math.AxisAlignedBB')
a=method(old,'public static azt a(double, double, double, double, double, double);');b=method(old,'protected azt(double, double, double, double, double, double);');c=method(native,'public net.minecraft.util.math.AxisAlignedBB(double, double, double, double, double, double);')
assert 'new ' in a and b.count('putfield')==6 and 'Math.' not in b
assert c.count('Math.min:(DD)D')==3 and c.count('Math.max:(DD)D')==3
(H/'proof/engine-methods.txt').write_text('original factory\n'+a+'\noriginal raw constructor\n'+b+'\nnative normalizing constructor\n'+c.rstrip()+'\n')
classes=['net/minecraft/util/math/AxisAlignedBB.class']
hashes={}
for entry in CP.split(':'):
 if pathlib.Path(entry).is_file() and zipfile.is_zipfile(entry):
  with zipfile.ZipFile(entry) as z:
   for name in classes:
    if name in z.namelist() and name not in hashes:hashes[name]=hashlib.sha256(z.read(name)).hexdigest()
assert len(hashes)==len(classes);assert hashes[classes[0]]==record['native']['class_sha256']
(H/'proof/engine-hashes.json').write_text(json.dumps(hashes,indent=2)+'\n')
print('PASS actual original raw factory/constructor and genuine native normalizing constructor bytecode with pinned inputs; no engine binaries retained')
