#!/usr/bin/env python3
"""Complete native AI classes plus exact bounded owner factory methods/statements."""
from pathlib import Path
import argparse, hashlib, importlib.util, io, os, re, subprocess, tempfile, zipfile
HERE=Path(__file__).resolve().parent
EXPECTED={'vd.class':'49a840a98368407f5415505f0fe49c65070bb4a558bde3561c66370b9acd2e33','uq.class':'6aca15966a2d214538f97253a8ed296d58950b885f332fb97f5359e5f9fabd28','ayg.class':'0793087c9066dbb72df5f64d2b1d77942d67cb89525b58d7adb9c5de4af31506','aye.class':'fad5e780e27c0507e632bce9ed7f511a9c513a52f9f103ae24a6f3b164991be1'}
def main():
    p=argparse.ArgumentParser();p.add_argument('--module',type=Path,default=HERE.parents[2]);p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--legacy-client',type=Path,required=True);p.add_argument('--legacy-forge-userdev',type=Path,required=True);a=p.parse_args()
    spec=importlib.util.spec_from_file_location('ai_mapping',HERE/'check-mapping.py');guard=importlib.util.module_from_spec(spec);spec.loader.exec_module(guard);guard.check(a.module)
    with zipfile.ZipFile(a.legacy_client) as jar:
        for path,expected in EXPECTED.items(): assert hashlib.sha256(jar.read(path)).hexdigest()==expected,'Unexpected original '+path
    with zipfile.ZipFile(a.legacy_forge_userdev) as forge:
        with zipfile.ZipFile(io.BytesIO(forge.read('forgepatches.zip'))) as patches:
            melee=patches.read('net/minecraft/entity/ai/EntityAIAttackOnCollide.java.patch')
            assert hashlib.sha256(melee).hexdigest()=='3debb81d385ace77c71005d314804a0954ff63007590b4e103afbade154cb517','Unexpected original Forge melee patch'
            assert not any('EntityAIArrowAttack' in n or 'PathFinder' in n for n in patches.namelist()),'Unreviewed original Forge ranged/path patch'
    print('PASS: original ranged/melee/path bytecode identities and original Forge melee patch verified.',flush=True)
    def factory(name):
        text=(a.module/('src/main/java/com/hbm/entity/mob/'+name+'.java')).read_text(encoding='utf-8')
        found=re.findall(r'protected EntityAIRangedAttackNT arrowAI\(\) \{\s*return new EntityAIRangedAttackNT\([^\n]+;\s*\}',text)
        assert len(found)==1,'Factory scope drift: '+name
        return found[0]
    fbi=(a.module/'src/main/java/com/hbm/entity/mob/EntityFBI.java').read_text(encoding='utf-8')
    statements=re.findall(r'this\.tasks\.addTask\([23], new EntityAI(?:RangedAttackNT|FBIMelee)\([^\n]+;',fbi)
    assert len(statements)==2,'FBI registration scope drift'
    generated=(HERE/'OwnerFactoriesTest.java.in').read_text(encoding='utf-8').replace('@CYBER_FACTORY@',factory('EntityCyberCrab')).replace('@TAINT_FACTORY@',factory('EntityTaintCrab')).replace('@FBI_REGISTRATION@','\n'.join(statements))
    suffix='.exe' if os.name=='nt' else '';javac=str(a.java_home/'bin'/('javac'+suffix));java=str(a.java_home/'bin'/('java'+suffix))
    with tempfile.TemporaryDirectory(prefix='nagaport-native-ai-') as tmp:
        out=Path(tmp);(out/'OwnerFactoriesTest.java').write_text(generated,encoding='utf-8')
        sources=[a.module/('src/main/java/com/hbm/entity/mob/ai/'+n+'.java') for n in ['EntityAIRangedAttackNT','EntityAIFBIMelee']]+[HERE/'OriginalRangedOracle.java',HERE/'NativeAiRootsTest.java',out/'OwnerFactoriesTest.java']
        subprocess.run([javac,'-source','8','-target','8','-encoding','UTF-8','-cp',a.classpath,'-d',tmp]+[str(s) for s in sources],check=True)
        cp=tmp+os.pathsep+a.classpath
        subprocess.run([java,'-cp',cp,'OriginalRangedOracle',str(a.legacy_client),tmp],check=True)
        for test in ['NativeAiRootsTest','OwnerFactoriesTest']:subprocess.run([java,'-cp',cp,test],check=True)
if __name__=='__main__':main()
