#!/usr/bin/env python3
"""Compare tick bodies with the supplied pristine original; not a full source guard."""
import argparse, pathlib, re
p=argparse.ArgumentParser();p.add_argument('--reference',type=pathlib.Path);a=p.parse_args()
root=pathlib.Path(__file__).resolve().parents[2]
reference=a.reference or root.parent/'ntm-original-pristine'
assert reference.is_dir(), 'Supply --reference pointing to the pristine original checkout.'
names=['ParticleBlackPowderSmoke','ParticleExplosionSmall','ParticleFlamethrower','ParticleAshes','ParticleDuoDec','ParticleDuoDec2','ParticleDeadLeaf','ParticleSplash','ParticleExSmoke','ParticleBlackPowderSpark']
def update(s):
 m=re.search(r'public void onUpdate\(\)\s*\{',s);assert m
 start=s.index('{',m.start());depth=1;end=start+1
 while depth:
  depth+=(s[end]=='{')-(s[end]=='}');end+=1
 return s[start:end]
for name in names:
 relative=pathlib.Path('src/main/java/com/hbm/particle')/(name+'.java')
 original=update((reference/relative).read_text());native=update((root/relative).read_text())
 restored=native.replace('this.setExpired()', 'this.setDead()').replace('this.move(', 'this.moveEntity(').replace('this.visualSeed','this.getEntityId()').replace('previousSpinDegrees','prevRotationPitch').replace('spinDegrees','rotationPitch').replace('world.spawnParticle(net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL,', 'worldObj.spawnParticle("smoke",')
 assert restored==original, name
print('PASS ten original update bodies byte-equal after documented native API/visual-ID/spin mappings.')
print('Inherited native movement/collision and shared Entity IDs intentionally are not legacy parity claims.')
