import argparse,hashlib,subprocess,zipfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--legacy-client',type=Path,required=True);p.add_argument('--legacy-srg',type=Path,required=True);a=p.parse_args()
# Known original official client bytes, with no download performed by this probe.
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
assert hashlib.sha256(a.legacy_srg.read_bytes()).hexdigest()=='d49df5c445dcf00ada532ab3b0e4283bf4b05ac13f210647658dfd3cec8b34c2'
mapping=zipfile.ZipFile(a.legacy_srg).read('joined.srg').decode()
for line in ['CL: sa net/minecraft/entity/Entity','MD: sa/a (Lsa;Z)V net/minecraft/entity/Entity/func_82141_a (Lnet/minecraft/entity/Entity;Z)V','FD: sa/am net/minecraft/entity/Entity/field_71088_bW','FD: sa/aq net/minecraft/entity/Entity/field_82152_aq']:
 assert line in mapping,line
legacy=subprocess.check_output([str(a.java_home/'bin/javap'),'-p','-c','-classpath',str(a.legacy_client),'sa'],text=True)
body=legacy.split('public void a(sa, boolean);',1)[1].split('\n  public ',1)[0]
assert 'Method e:(Ldh;)V' in body and 'Method f:(Ldh;)V' in body and 'Field am:I' in body and 'Field aq:I' in body
assert 'removeTag' not in body and 'if' not in body
native=subprocess.check_output([str(a.java_home/'bin/javap'),'-p','-c','-classpath',a.classpath,'net.minecraft.entity.Entity'],text=True)
new=native.split('private void copyDataFromOld(net.minecraft.entity.Entity);',1)[1].split('\n  public ',1)[0]
assert 'String Dimension' in new and 'removeTag:' in new and 'Field timeUntilPortal:I' in new and 'Field teleportDirection:' in new
print('PASS original func_82141_a serializes full NBT without removing Dimension, then copies cooldown/direction; native private helper removes Dimension and also copies extra portal fields.')
for path in [a.legacy_client,a.legacy_srg]:print(path.name,hashlib.sha256(path.read_bytes()).hexdigest())
print('ORIGINAL BODY'+body);print('NATIVE PRIVATE HELPER'+new)
