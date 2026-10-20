import hashlib,json,pathlib,zipfile
H=pathlib.Path(__file__).resolve().parent; R=H.parents[2]
def sha(b):return hashlib.sha256(b).hexdigest()
def transform(b,edits):
 b=b.decode().encode('utf-16-le')
 for e in sorted(edits,key=lambda x:x['start'],reverse=True):
  a,z=e['start']*2,e['end']*2;assert b[a:z].decode('utf-16-le')==e['old'];b=b[:a]+e['new'].encode('utf-16-le')+b[z:]
 return b.decode('utf-16-le').encode()
def main():
 m=json.loads((H/'manifest.json').read_text());n=0
 with zipfile.ZipFile(H/'baseline.zip') as z:
  for p,f in m['files'].items():
   old=z.read(p);now=(R/p).read_bytes();assert sha(old)==f['before'];assert transform(old,f['edits'])==now;assert sha(now)==f['after'];n+=len(f['edits'])
   for e in f['edits']:
    changed=[x for x in f['edits'] if x is not e];assert transform(old,changed)!=now
 print('PASS exact whole-file inverse and',n,'individual omitted-token controls across',len(m['files']),'files')
if __name__=='__main__':main()
