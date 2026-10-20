import hashlib,json,pathlib
H=pathlib.Path(__file__).resolve().parent;M=H.parents[2];data=json.loads((H/'manifest.json').read_text())
def sha(b):return hashlib.sha256(b).hexdigest()
def reverse(raw,info):
 text=raw.decode().encode('utf-16-le');delta=0;edits=[]
 for e in sorted(info['edits'],key=lambda e:e['start']):
  a=e['start']+delta;b=a+len(e['new']);assert text[a*2:b*2].decode('utf-16-le')==e['new'];edits.append((a,b,e));delta+=len(e['new'])-(e['end']-e['start'])
 for a,b,e in reversed(edits):text=text[:a*2]+e['old'].encode('utf-16-le')+text[b*2:]
 return text.decode('utf-16-le').encode()
if __name__=='__main__':
 for rel,info in data['files'].items():
  raw=(M/rel).read_bytes();assert sha(raw)==info['after'],rel;assert sha(reverse(raw,info))==info['before'],rel
 print('PASS exact source inverse',len(data['files']),'files',sum(len(x['edits']) for x in data['files'].values()),'edits')
