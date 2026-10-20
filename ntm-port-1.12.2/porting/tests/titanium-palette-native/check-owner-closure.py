#!/usr/bin/env python3
"""Conservative lexical closure checks; actual use-site Java attribution is separate."""
import pathlib,re,zipfile,hashlib,json
ROOT=pathlib.Path(__file__).resolve().parents[3]
TARGET=json.loads((pathlib.Path(__file__).resolve().parent/'targets.json').read_text())
MOD='src/main/java/com/hbm/blocks/ModBlocks.java'
BASE='149469c7403653ca6576515b730bc033a1574cfe'
def clean(s): return re.sub(r'/\*.*?\*/|//[^\n]*','',s,flags=re.S)
def verify(src):
 for cls,fields in TARGET.items():
  path='src/main/java/com/hbm/world/dungeon/'+cls+'.java'; s=clean(src[path])
  for field,owner in fields.items():
   declaration='Block '+field+' = ModBlocks.'+owner+';'
   assert sum(l.strip()==declaration for l in s.splitlines())==1,('declaration/modifiers',cls,field)
   for l in s.splitlines():
    hits=list(re.finditer(r'\b'+field+r'\b',l))
    if not hits:continue
    assert len(hits)==1,('duplicate/shadow reference',cls,field,l)
    assert l.strip()==declaration or re.search(r',\s*'+field+r'\s*,\s*0\s*,\s*3\s*\)',l) or re.search(r'\bDungeonToolbox.placeUnorientedStructureBlock\([^\n]*, '+field+r'\);',l),('unexpected field use/write/shadow',cls,field,l)
  for p,text in src.items():
   c=clean(text)
   assert not re.search(r'\bextends\s+(?:[\w.]+\.)?'+cls+r'\b',c),('subclass',p,cls)
   assert not re.search(r'\bnew\s+(?:[\w.]+\.)?'+cls+r'\s*\([^;{}]*\)\s*\{',c),('anonymous subclass',p,cls)
 # Reject every qualified palette reference outside these two owners. Conservative by design.
 for p,text in src.items():
  if p not in ['src/main/java/com/hbm/world/dungeon/'+c+'.java' for c in TARGET]:
   assert not re.search(r'\.\s*Block(?:15)\b',clean(text)),('external field access',p)
 owners=set(o for f in TARGET.values() for o in f.values())
 here=pathlib.Path(__file__).resolve().parent
 manifest=json.loads((here.parent/'dungeon-palette-native/owner-source-manifest.json').read_text())
 archive=here.parent/'dungeon-palette-native/owner-sources.zip'
 assert manifest['baseline']==BASE
 assert hashlib.sha256(archive.read_bytes()).hexdigest()=='5b56176b6b868bba17a83f82bd4995c02a11cd9c1f3a778526c5eb37f0603823'==manifest['archive_sha256']
 with zipfile.ZipFile(archive) as z: data=z.read(MOD)
 assert hashlib.sha256(data).hexdigest()==manifest['sources'][MOD]
 baseline=data.decode()
 for owner in owners:
  pat=r'\b'+owner+r'\s*=(?!=)'
  writes=[]
  for p,text in src.items():
   for m in re.finditer(pat,clean(text)):writes.append((p,clean(text).splitlines()[clean(text).count('\n',0,m.start())].strip()))
  expected=[l.strip() for l in baseline.splitlines() if re.search(pat,l)]
  assert len(expected)==1 and len(writes)==1 and writes[0]==(MOD,expected[0]),('owner write/constructor',owner,writes)
  assert 'new BlockOre(Material.IRON).noFortune()' in expected[0]
 return True
src={str(p.relative_to(ROOT)):p.read_text() for p in (ROOT/'src/main/java').rglob('*.java')}
verify(src)
p='src/main/java/com/hbm/world/dungeon/DesertAtom002.java'
mutants={
 'field reassignment':(p,'\nvoid bad(){Block15 = null;}'),
 'qualified reassignment':('src/main/java/Injected.java','class Injected {void bad(DesertAtom002 x){x.Block15 = null;}}'),
 'local shadow':(p,'\nvoid bad(){Block Block15 = null;}'),
 'parameter shadow':(p,'\nvoid bad(Block Block15){}'),
 'subclass':('src/main/java/Injected.java','class Injected extends DesertAtom002 {}'),
 'anonymous subclass':('src/main/java/Injected.java','class Injected {Object x = new DesertAtom002() {}; }'),
 'static owner write':('src/main/java/Injected.java','class Injected {void bad(){ModBlocks.deco_titanium = null;}}'),
 'unqualified owner write':('src/main/java/Injected.java','class Injected {void bad(){deco_titanium = null;}}'),
}
for name,(path,extra) in mutants.items():
 changed=dict(src);changed[path]=changed.get(path,'')+extra
 try:verify(changed)
 except AssertionError: print('REJECTED:',name)
 else:raise AssertionError('surviving mutant: '+name)
changed=dict(src);changed[p]=changed[p].replace('Block Block15 =','final Block Block15 =',1)
try: verify(changed)
except AssertionError:print('REJECTED: declaration modifier change')
else:raise AssertionError('modifier mutant survived')
print('PASS: two mutable palette fields, one exact titanium construction, closed source references, nine rejected mutations.')
