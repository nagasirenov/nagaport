"""Record terminal full-build diagnostics and exact generated Java input identity."""
from pathlib import Path
import hashlib,json,re,collections
H=Path(__file__).resolve().parent;R=H.parents[1]
exit_code=int((H/'full-compile.exit').read_text())
log=(H/'full-compile.log').read_text()
assert exit_code==1 and 'BUILD FAILED' in log and '> Task :compileJava FAILED' in log
errors=re.findall(r'^.*\.java:\d+: error: (.*)$',log,re.M)
totals=re.findall(r'^(\d+) errors?$',log,re.M)
assert totals and int(totals[-1])==len(errors),(totals,len(errors))
rows=[]
for source in sorted((R/'src/main/java').rglob('*.java')):
 rel=source.relative_to(R);generated=R/'build/sources/main/java'/source.relative_to(R/'src/main/java')
 assert source.read_bytes()==generated.read_bytes(),str(rel)
 rows.append({'path':str(rel),'sha256':hashlib.sha256(source.read_bytes()).hexdigest()})
assert len(rows)==len(list((R/'build/sources/main/java').rglob('*.java'))),'generated extra inputs'
(H/'source-inputs.json').write_text(json.dumps(rows,indent=2)+'\n')
(H/'diagnostics.json').write_text(json.dumps({'before':10164,'after':len(errors),'categories':collections.Counter(errors).most_common(),'symbols':collections.Counter(re.findall(r'symbol:\s+(.+)',log)).most_common()},indent=2)+'\n')
print('PASS terminal FAIL %d diagnostics; net decrease %d; all %d generated source inputs byte-identical'%(len(errors),10164-len(errors),len(rows)))
