import pathlib,json,hashlib
h=pathlib.Path(__file__).resolve().parent;root=h.parents[2];data=json.loads((h/'manifest.json').read_text())
def digest(b):return hashlib.sha256(b).hexdigest()
def forward(raw,entry):
 assert digest(raw)==entry['before_sha256'],'baseline drift'
 s=raw.decode('utf8').encode('utf-16-le')
 for e in reversed(entry['edits']):
  a,b=2*e['start'],2*e['end'];assert s[a:b].decode('utf-16-le')==e['before'];s=s[:a]+e['after'].encode('utf-16-le')+s[b:]
 result=s.decode('utf-16-le').encode('utf8');assert digest(result)==entry['after_sha256'];return result
def reverse(raw,entry):
 assert digest(raw)==entry['after_sha256'],'production drift'
 s=raw.decode('utf8').encode('utf-16-le');delta=0;adjusted=[]
 for e in entry['edits']:
  start=e['start']+delta;length=len(e['after'].encode('utf-16-le'))//2;adjusted.append((start,start+length,e));delta+=length-(e['end']-e['start'])
 for start,end,e in reversed(adjusted):
  assert s[start*2:end*2].decode('utf-16-le')==e['after'];s=s[:start*2]+e['before'].encode('utf-16-le')+s[end*2:]
 result=s.decode('utf-16-le').encode('utf8');assert digest(result)==entry['before_sha256'];return result
if __name__=='__main__':
 controls=0
 for rel,e in data['files'].items():
  after=(root/rel).read_bytes();before=reverse(after,e);assert forward(before,e)==after
  for raw,fn in [(after+b' ',reverse),(before+b' ',forward),(before,reverse)]:
   try:fn(raw,e)
   except AssertionError:controls+=1
   else:raise AssertionError('mutation accepted')
 print('PASS',len(data['files']),'exact forward/inverse files and',controls,'drift/reintroduction controls')
