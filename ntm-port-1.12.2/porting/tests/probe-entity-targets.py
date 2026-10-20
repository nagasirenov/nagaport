#!/usr/bin/env python3
"""Compile complete custom target AI, execute native Minecraft selection regression."""
from pathlib import Path
import argparse, os, re, subprocess, tempfile
p=argparse.ArgumentParser()
p.add_argument('--java-home',type=Path,required=True)
p.add_argument('--classpath',required=True)
a=p.parse_args()
root=Path(__file__).resolve().parents[2]
pristine=root.parent/'ntm-original-pristine'
def method_body(source,name):
    match=re.search(r'public boolean '+name+r'\(Entity \w+\)\s*\{',source)
    if not match: raise AssertionError('Missing predicate '+name)
    start=match.end();i=start;depth=1
    while depth:
        depth+=(source[i]=='{')-(source[i]=='}');i+=1
    return re.sub(r'\s+','',source[start:i-1]).replace('.isEntityApplicable(','.apply(')
for name in ['EntityTankbot','EntityCyberCrab','EntityWarBehemoth','botprime/EntityBOTPrimeBase','botprime/EntityWormBaseNT','ai/EntityAINearestAttackableTargetNT']:
    relative=Path('src/main/java/com/hbm/entity/mob')/(name+'.java')
    old=(pristine/relative).read_text();new=(root/relative).read_text()
    assert method_body(old,'isEntityApplicable')==method_body(new,'apply'),name+' predicate changed'
    assert 'IEntitySelector' not in new and 'isEntityApplicable' not in new,name+' legacy selector remains'
print('PASS: all six production predicate decisions match pristine after method rename.',flush=True)
with tempfile.TemporaryDirectory(prefix='hbm-entity-target-probe-') as out:
    subprocess.run([str(a.java_home/'bin/javac'),'-source','8','-target','8','-cp',a.classpath,'-d',out,
        str(root/'src/main/java/com/hbm/entity/mob/ai/EntityAINearestAttackableTargetNT.java'),
        str(root/'porting/tests/EntityTargetMinecraftTest.java')],check=True)
    subprocess.run([str(a.java_home/'bin/java'),'-cp',out+os.pathsep+a.classpath,'EntityTargetMinecraftTest'],check=True)
