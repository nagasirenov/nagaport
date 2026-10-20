"""One-time exact CP92 transform; all inputs are preflighted before any write."""
from pathlib import Path
import json,hashlib,zipfile,subprocess
H=Path(__file__).resolve().parent;R=H.parents[2];data={};base='0675e599e5ee31a60776b560af792372df191790'
for line in (H/'before.tsv').read_text().splitlines():
 p,a,b,expr,owner=line.split('\t');data.setdefault(p,[]).append((int(a),int(b)))
manifest={'base':base,'files':{}};prepared={}
for rel,sites in data.items():
 p=R/rel;raw=p.read_bytes();s=raw.decode();assert raw==subprocess.check_output(['git','show',base+':ntm-port-1.12.2/'+rel]),('not exact CP92',rel)
 edits=[]
 for a,b in sorted(sites,reverse=True):
  old=s[a:b];new='new net.minecraft.util.math.BlockPos('+old+')';edits.insert(0,{'start':a,'old':old,'new':new});s=s[:a]+new+s[b:]
 after=s.encode();prepared[rel]=(raw,after);manifest['files'][rel]={'before':hashlib.sha256(raw).hexdigest(),'after':hashlib.sha256(after).hexdigest(),'edits':edits}
assert len(prepared)==13 and sum(len(x['edits'])for x in manifest['files'].values())==14
# No source/evidence writes occur until every source passes exact baseline preflight.
with zipfile.ZipFile(H/'baseline.zip','w',zipfile.ZIP_DEFLATED)as z:
 for rel,(raw,after)in prepared.items():z.writestr(rel,raw)
for rel,(raw,after)in prepared.items():(R/rel).write_bytes(after)
(H/'manifest.json').write_text(json.dumps(manifest,indent=2)+'\n');print('Migrated 14 calls in13 files')
