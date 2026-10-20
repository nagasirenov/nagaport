import hashlib,json,zipfile
from pathlib import Path
H=Path(__file__).resolve().parent;R=H.parents[2];M=json.loads((H/'manifest.json').read_text())
def sha(b):return hashlib.sha256(b).hexdigest()
def restore_base(path,raw):
 if path not in M['files']:return raw
 d=M['files'][path];assert sha(raw)==d['after'],path
 before=raw.replace(b'this.isPointInRegion(',b'this.func_146978_c(');assert sha(before)==d['before'];return before

def main():
 assert len(M['files'])==4 and sum(d['calls'] for d in M['files'].values())==5
 with zipfile.ZipFile(H/'baseline.zip') as z:
  for p,d in M['files'].items():
   raw=(R/p).read_bytes();old=restore_base(p,raw);assert old==z.read(p);assert old.replace(b'this.func_146978_c(',b'this.isPointInRegion(')==raw
   for mutant in (raw+b'\n// unrelated\n',raw.replace(b'this.isPointInRegion(',b'this.func_146978_c(',1)):
    try:restore_base(p,mutant)
    except AssertionError:pass
    else:raise AssertionError('accepted source drift')
 print('PASS four complete-file inverses, five name-only edits and eight drift controls')
if __name__=='__main__':main()
