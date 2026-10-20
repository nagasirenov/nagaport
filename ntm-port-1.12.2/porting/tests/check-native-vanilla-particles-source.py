#!/usr/bin/env python3
"""Frozen branch scope, not a claim that the entire ClientProxy compiles."""
import pathlib,json,hashlib,re
r=pathlib.Path(__file__).resolve().parents[2]
m=json.loads((r/'porting/tests/native-vanilla-particles-scope.json').read_text())
h=lambda text:hashlib.sha256(text.encode()).hexdigest()
for path,digest in m['whole_files'].items():
 assert h((r/path).read_text())==digest, 'changed whole source '+path
source=(r/'src/main/java/com/hbm/main/ClientProxy.java').read_text()
start=source.index('public void effectNT(');end=source.index('\n\t@Override',start);method=source[start:end]
assert h(method)==m['effectNT_sha256'], 'changed production callback scope'
for name,digest in m['branches'].items():
 marker='\t\tif("'+name+'".equals(type)) {';start=method.index(marker);i=method.index('{',start)+1;depth=1
 while depth:
  if method[i]=='{':depth+=1
  elif method[i]=='}':depth-=1
  i+=1
 assert h(method[start:i])==digest, 'changed extracted branch '+name
assert not re.search(r'\bEntity(?:Cloud|Flame|Reddust|Smoke|BlockDust|FireworkSpark|LargeExplode|Explode|Aura|Crit)FX\b|ReflectionHelper|\.noClip\b',method)
for path in (r/'src/main/java').rglob('*.java'):
 text=re.sub(r'/\*.*?\*/|//[^\n]*','',path.read_text(),flags=re.S)
 assert not re.search(r'net\.minecraft\.client\.particle\.Entity\w*FX\b',text), 'remaining vanilla removed import '+str(path)
print('PASS frozen native vanilla helper/creator and 15 exact ClientProxy branch hashes; no active removed vanilla particle imports repository-wide')
