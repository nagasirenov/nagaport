#!/usr/bin/env python3
"""Compile full stationary effects against real Forge, then run bounded no-GL probes."""
import argparse, os, pathlib, subprocess, tempfile
p=argparse.ArgumentParser()
p.add_argument('--classpath', required=True)
p.add_argument('--java-home', type=pathlib.Path, required=True)
a=p.parse_args()
assert a.classpath.strip(), 'Supply the real native Forge compile classpath.'
root=pathlib.Path(__file__).resolve().parents[2]
names=['ParticleAmatFlash','ParticleHadron','ParticleMukeWave','ParticlePlasmaBlast','ParticleRBMKFlame','ParticleRBMKSteam','ParticleVertexFormats']
files=[root/'src/main/java/com/hbm/particle'/(name+'.java') for name in names]
files += [root/'src/main/java/com/hbm/lib/RefStrings.java',root/'porting/tests/NativeStationaryParticlesTest.java']
for source in files[:6]:
    text=source.read_text()
    assert 'EntityFX' not in text and 'extends Particle' in text, source
    assert 'renderParticle(BufferBuilder tess, Entity camera,' in text, source
exe='.exe' if (a.java_home/'bin/javac.exe').exists() else ''
with tempfile.TemporaryDirectory(prefix='stationary-particle-native-') as temp:
    subprocess.run([str(a.java_home/'bin'/('javac'+exe)), '-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',temp]+list(map(str,files)),check=True)
    subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-cp',temp+os.pathsep+a.classpath,'NativeStationaryParticlesTest',str(root/'src/main/resources')],check=True)
