#!/usr/bin/env python3
"""Complete native particle classes; one exact NTM metadata-declaration boundary."""
import argparse, os, pathlib, re, subprocess, tempfile
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
assert a.classpath.strip(), 'Real native Forge classpath required'
root=pathlib.Path(__file__).resolve().parents[2]
names=['EntityFXRotating','ParticleBlackPowderSmoke','ParticleExplosionSmall','ParticleFlamethrower','ParticleAshes','ParticleDuoDec','ParticleDuoDec2','ParticleDeadLeaf','ParticleSplash','ParticleExSmoke','ParticleBlackPowderSpark','ParticleAtlasVertex','ParticleSprites']
files=[root/'src/main/java/com/hbm/particle'/(name+'.java') for name in names]
files += [root/'src/main/java/com/hbm/util/Vec3NT.java',root/'porting/tests/NativeMovingAtlasParticlesTest.java']
# FlameCreator's packet/main-proxy integration is outside this complete-particle compile.
# Extract its exact mutable metadata declarations, without fabricating Minecraft/Forge APIs.
s=(root/'src/main/java/com/hbm/particle/helper/FlameCreator.java').read_text()
constants=re.findall(r'public static int META_\w+ = \d+;',s)
assert len(constants)==5, constants
exe='.exe' if (a.java_home/'bin/javac.exe').exists() else ''
with tempfile.TemporaryDirectory(prefix='native-moving-atlas-') as tmp:
 t=pathlib.Path(tmp);fixture=t/'com/hbm/particle/helper/FlameCreator.java';fixture.parent.mkdir(parents=True)
 fixture.write_text('package com.hbm.particle.helper; public class FlameCreator {\n'+'\n'.join(constants)+'\n}')
 subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',tmp]+list(map(str,files))+[str(fixture)],check=True)
 subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-cp',tmp+os.pathsep+a.classpath,'NativeMovingAtlasParticlesTest'],cwd=tmp,check=True)
print('PASS eleven complete particle classes + native vertex/sprite/mutable-vector helpers; FlameCreator metadata only, not packet/helper integration.')
