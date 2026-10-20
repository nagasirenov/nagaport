from pathlib import Path
import hashlib,json,zipfile
H=Path(__file__).resolve().parent;R=H.parents[2];data=json.loads((H/'manifest.json').read_text())
def digest(x):return hashlib.sha256(x).hexdigest()
def restore_base(rel,raw):
 e=data['files'][rel];assert digest(raw)==e['after'],rel
 text=raw.decode()
 # Reconstruct by the original offsets, adjusting only known wrapper lengths.
 delta=0
 for edit in e['edits']:
  a=edit['start']+delta;new=edit['new'];assert new=='new net.minecraft.util.math.BlockPos('+edit['old']+')';assert text[a:a+len(new)]==new
  delta+=len(new)-len(edit['old'])
 for edit in reversed(e['edits']):
  delta-=len(edit['new'])-len(edit['old']);a=edit['start']+delta;text=text[:a]+edit['old']+text[a+len(edit['new']):]
 old=text.encode();assert digest(old)==e['before'],rel;return old

def verify():
 count=0;mutants=0
 with zipfile.ZipFile(H/'baseline.zip')as z:
  for rel,e in data['files'].items():
   raw=(R/rel).read_bytes();before=z.read(rel);assert restore_base(rel,raw)==before
   text=before.decode()
   for edit in reversed(e['edits']):
    a=edit['start'];assert text[a:a+len(edit['old'])]==edit['old'];text=text[:a]+edit['new']+text[a+len(edit['old']):]
   assert text.encode()==raw
   for edit in e['edits']:
    mutant=raw.replace(edit['new'].encode(),edit['old'].encode(),1)
    try:restore_base(rel,mutant)
    except AssertionError:mutants+=1
    else:raise AssertionError('accepted removed wrapper')
   count+=len(e['edits'])
 assert len(data['files'])==13 and count==mutants==14
 print('PASS 13 exact complete-file inverses; 14 typed argument wrappers and 14 independently omitted-wrapper mutations rejected; all surrounding bytes unchanged')
if __name__=='__main__':verify()
