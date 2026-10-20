#!/usr/bin/env python3
"""Full production native compile and bounded owned-particle lifecycle/data checks, not GL/game proof."""
import argparse,pathlib,subprocess,tempfile,re
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
assert a.classpath.strip(),'Supply a real native Forge compile classpath; clean removes files under build.'
root=pathlib.Path(__file__).resolve().parents[2]
names=['ParticleGasFlame','ParticleDebug','ParticleDebugLine','ParticleText','ParticleFoundry','ParticleRadiationFog','ParticleProblem','ParticleHaze','ParticleRBMKMush','ParticleContrail','ParticleVertexFormats','ParticleGlow','ParticleDigammaSmoke','ParticleDust','ParticleCoolingTower','ParticleSprites','ParticleQuadRenderer']
files=[root/'src/main/java/com/hbm/particle'/f'{n}.java' for n in names]
files += [root/'src/main/java/com/hbm/lib'/f'{n}.java' for n in ['RefStrings','ForgeDirection']]
files += [root/'porting/tests'/f'{n}.java' for n in ['NativeAtlasParticlesTest','NativeOwnedParticlesTest']]
for name in names[:10]:
 s=(root/'src/main/java/com/hbm/particle'/f'{name}.java').read_text()
 assert 'EntityFX' not in s and 'EntitySmokeFX' not in s
 for resource in re.findall(r'":(textures/[^\"]+)"',s):
  assert (root/'src/main/resources/assets/hbm'/resource).is_file(),(name,resource)
# Corrected known original typo uses its existing original particle PNG, not an invented asset.
assert ':textures/particle/theproblem.png' in (root/'src/main/java/com/hbm/particle/ParticleProblem.java').read_text()
exe='.exe' if (a.java_home/'bin/javac.exe').exists() else ''
with tempfile.TemporaryDirectory(prefix='owned-particle-native-') as temp:
 subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',temp]+[str(x) for x in files],check=True)
 import os
 subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-cp',temp+os.pathsep+a.classpath,'NativeOwnedParticlesTest'],check=True)
print('PASS complete ten newly migrated particles + actual shared production support; resource paths exist. No GL/font draw/fullmod acceptance.')
