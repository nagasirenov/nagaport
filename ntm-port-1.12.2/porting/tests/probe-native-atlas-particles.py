#!/usr/bin/env python3
"""Compile complete production particle classes against the actual pinned native API."""
import argparse, pathlib, subprocess, tempfile
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
root=pathlib.Path(__file__).resolve().parents[2]
names=['ParticleGlow','ParticleDigammaSmoke','ParticleDust','ParticleCoolingTower','ParticleSprites','ParticleQuadRenderer']
files=[root/'src/main/java/com/hbm/particle'/f'{name}.java' for name in names]+[root/'porting/tests/NativeAtlasParticlesTest.java']
# Exact integration-hunk checks, not a claim that the still-blocked full client handlers compile.
handler=(root/'src/main/java/com/hbm/main/ModEventHandlerClient.java').read_text()
assert handler.count('com.hbm.particle.ParticleSprites.register(event.getMap());')==1
for name in ['particle_base','dead_leaf','particlenote2','particlenote1','particle_splash','particle_ashes','yelflare']:
 assert 'register.registerIcon(RefStrings.MODID + ":blocks/particle/'+name+'")' in handler
client=(root/'src/main/java/com/hbm/main/ClientProxy.java').read_text()
tower=client[client.index('if("tower".equals(type)'):client.index('if("splash".equals(type) && pressure >= 0.08)')]
assert 'if(particleSetting == 0 || (particleSetting == 1 && rand.nextBoolean()))' in tower
assert 'fx.setMotion(motionX, motionY, motionZ);' in tower
assert 'fx.setLife(life / (particleSetting + 1));' in tower and 'life /= 8;' in tower
assert 'fx.setMotion(vec.x, 0, vec.z);' in client
for name in names[:4]:
 source=(root/'src/main/java/com/hbm/particle'/f'{name}.java').read_text()
 assert 'extends Particle' in source and 'this.canCollide = false;' in source
 for obsolete in ['EntityFX','ModEventHandlerClient','moveEntity(', 'setDead(', 'noClip', 'Tessellator']:
  assert obsolete not in source, (name, obsolete)
print('PASS native particle source integration/settings/real sprite routes')
with tempfile.TemporaryDirectory(prefix='native-atlas-particles-') as tmp:
 subprocess.run([str(a.java_home/'bin/javac'),'-source','8','-target','8','-encoding','UTF-8','-cp',a.classpath,'-d',tmp]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-cp',tmp+':'+a.classpath,'NativeAtlasParticlesTest',str(root/'src/main/resources')],cwd=tmp,check=True)
