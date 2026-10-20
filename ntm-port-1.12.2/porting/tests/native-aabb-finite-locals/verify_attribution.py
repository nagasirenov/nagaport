#!/usr/bin/env python3
"""Exact changed-site native constructor bindings and line/code/message diagnostic delta."""
import collections,gzip,json,pathlib
H=pathlib.Path(__file__).resolve().parent;R=H.parents[2]
m=json.loads((H/'manifest.json').read_text())
def keypath(p):return 'src/main/java/'+p.split('/src/main/java/',1)[1]
def records(p):return [json.loads(x) for x in p.read_text().splitlines()]
after={(keypath(x['path']),x['start']):x for x in records(H/'proof/after.jsonl')}
expected=[]
for path,rec in m['files'].items():
 s=(R/path).read_bytes().decode('utf-8')
 for e in rec['edits']:
  start=len(s[:e['after_start']].encode('utf-16-le'))//2
  assert after[(path,start)]['descriptor']=='(DDDDDD)V'
  expected.append((path,s[:e['after_start']].count('\n')+1))
def diag(name):
 with gzip.open(H/('proof/'+name+'-diagnostics.jsonl.gz'),'rt') as f:return [json.loads(x) for x in f]
def key(x):return (keypath(x['path']),x['line'],x['code'],x['message'])
before=collections.Counter(map(key,diag('before')));current=collections.Counter(map(key,diag('after')))
removed=before-current;added=current-before
assert not added,added
assert sum(removed.values())==2
assert collections.Counter((k[0],k[1]) for k in removed)==collections.Counter(expected)
for k in removed:assert k[2]=='compiler.err.cant.resolve.location.args' and 'symbol:   method getBoundingBox(' in k[3] and 'location: class net.minecraft.util.math.AxisAlignedBB' in k[3]
print('PASS 2 exact native (DDDDDD)V bindings; javac attribution errors 10014 -> 10012; exactly 2 selected factory diagnostics removed, 0 added; all unrelated line/code/message diagnostic multiplicities equal (not a full build)')
