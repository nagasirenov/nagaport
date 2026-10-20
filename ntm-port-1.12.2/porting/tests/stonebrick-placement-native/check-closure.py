#!/usr/bin/env python3
import re,subprocess,zipfile,importlib.util
from pathlib import Path
H=Path(__file__).resolve().parent;M=H.parents[2]
spec=importlib.util.spec_from_file_location('ore',H.parent/'ore-callback-native/run.py');ore=importlib.util.module_from_spec(spec);spec.loader.exec_module(ore)
paths=subprocess.check_output(['git','ls-files','src/main/java'],cwd=M,text=True).splitlines();assert all((M/p).is_file()for p in paths)
def clean(s):return re.sub(r'/\*.*?\*/|//[^\n]*','',s,flags=re.S)
src={p:clean((M/p).read_text())for p in paths}
def verify(sources):
 for p,s in sources.items():
  assert not re.search(r'\bextends\s+(?:[\w.]+\.)?LibraryDungeon\b',s),p
  assert not re.search(r'new\s+LibraryDungeon\([^;{}]*\)\s*\{',s),p
  assert not re.search(r'extends\s+(?:java\.util\.)?Random\b',s),p
  assert not re.search(r'Blocks\.STONEBRICK\s*=(?!=)',s),p
 text=sources['src/main/java/com/hbm/world/dungeon/LibraryDungeon.java'];body=ore.extract(text,'public int getBrick(Random rand)');assert re.sub(r'\s+','',body)=='publicintgetBrick(Randomrand){returnrand.nextInt(3);}'
verify(src)
for extra in ['class X extends LibraryDungeon {}','class X {Object a=new LibraryDungeon(){};}','class X extends Random {}','class X {void x(){Blocks.STONEBRICK=null;}}']:
 mutant=dict(src);mutant['Injected.java']=extra
 try:verify(mutant)
 except AssertionError:pass
 else:raise AssertionError('closure mutant survived')
with zipfile.ZipFile(H/'sources.zip') as z:
 before=z.read('src/main/java/com/hbm/world/dungeon/LibraryDungeon.java').decode();after=(M/'src/main/java/com/hbm/world/dungeon/LibraryDungeon.java').read_text();assert ore.extract(before,'public int getBrick(Random rand)')==ore.extract(after,'public int getBrick(Random rand)')
 # Attribution includes the complete real parent; native Entity.world is inherited without a shadow.
 ancestor=z.read('src/main/java/com/hbm/entity/projectile/EntityThrowableNT.java').decode();assert 'extends Entity implements IProjectile' in ancestor and not re.search(r'\bWorld\s+world\s*[;=]',ancestor)
print('PASS',len(paths),'tracked-source selector/subclass/Random/owner closure, exact original selector body and four rejected closure mutations; full actual EntityThrowableNT ancestor supplied for chemical World attribution.')
print('Scoped getBrick range is 0..2 under java.util.Random.nextInt contract. External subclasses, noncontractual RNG overrides and final-field replacement are outside this proof, not normalized or explicitly rejected by production.')
