#!/usr/bin/env python3
"""Real native ItemStack plus exact bounded ingress loops and inventory setters.
Not whole block/cart lifecycle proof; no platform stand-ins.
"""
import argparse,pathlib,re,subprocess,tempfile,os,hashlib
p=argparse.ArgumentParser();p.add_argument('--java-home',required=True,type=pathlib.Path);p.add_argument('--classpath',required=True);p.add_argument('--legacy-client',required=True,type=pathlib.Path);a=p.parse_args()
h=pathlib.Path(__file__).resolve().parent;r=h.parents[2];s=r/'src/main/java/com/hbm'
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
def block(t,start):
 i=t.index('{',start);j=i+1;depth=1
 while depth:depth+=(t[j]=='{')-(t[j]=='}');j+=1
 return t[start:j]
def method(file,name):
 t=(s/file).read_text();m=re.search(r'public (?:void|int) '+name+r'\(',t);assert m;return block(t,m.start())
parts=[]
for cls,file in [('Crate','blocks/generic/BlockStorageCrate.java'),('Mass','blocks/machine/BlockMassStorage.java'),('Cart','entity/cart/EntityMinecartCrate.java')]:
 t=(s/file).read_text();expr='new ItemStack(stack.getTagCompound().getCompoundTag("slot" + i))';assert t.count(expr)==1
 at=t.index(expr);start=t.rfind('for(',0,at);loop=block(t,start)
 owner='entity/cart/EntityMinecartContainerBase.java' if cls=='Cart' else 'tileentity/machine/storage/TileEntityCrateBase.java'
 setter=method(owner,'setInventorySlotContents')
 parts.append('static class '+cls+' extends Fixture { '+setter+' void restore(ItemStack stack) { Fixture inv=this; '+loop+' } }')
with tempfile.TemporaryDirectory(prefix='stack-loading-native-') as td:
 d=pathlib.Path(td);f=d/'StackLoadingTest.java';f.write_text((h/'StackLoadingTest.java.in').read_text().replace('// @FIXTURES@','\n'.join(parts)))
 subprocess.run([str(a.java_home/'bin/javac'),'-cp',a.classpath,'-d',td,str(f)],check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-cp',td+os.pathsep+a.classpath,'StackLoadingTest',str(a.legacy_client)],cwd=td,check=True)
