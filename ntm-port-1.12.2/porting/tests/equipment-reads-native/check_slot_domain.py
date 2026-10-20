#!/usr/bin/env python3
"""Full parsed call closure: current owned inputs stay within the audited equipment domain."""
from pathlib import Path
import collections,json,subprocess,tempfile
H=Path(__file__).resolve().parent;R=H.parents[2];J=Path('/workspace/shared/nagaport-toolchain/java8')
with tempfile.TemporaryDirectory(prefix='equipment-slot-domain-') as td:
 d=Path(td);subprocess.run([str(J/'bin/javac'),'-cp',str(J/'lib/tools.jar'),'-d',str(d),str(H/'SlotDomainScanner.java')],check=True)
 inputs=d/'inputs';inputs.write_text('\n'.join(map(str,sorted((R/'src/main/java').rglob('*.java'))))+'\n')
 output=subprocess.check_output([str(J/'bin/java'),'-cp',str(d)+':'+str(J/'lib/tools.jar'),'SlotDomainScanner',str(inputs)],text=True)
 rows=list(map(json.loads,output.splitlines()))
for row in rows:row['file']=str(Path(row['file']).relative_to(R))
counts=collections.Counter(row['name'] for row in rows)
assert counts['getEquipmentNullable']==34 and counts['getArmorNullable']==10,counts
for row in rows:
 name=row['name'];args=row['args'];loops=row['loops'];slot=args[2] if name=='checkArmorPiece' else args[1]
 if name in ['hasAllProtection','hasAnyProtection','hasProtection']:assert slot=='3',row
 elif name=='damageSuit':
  assert slot in ['0','1','2','3'] or slot=='i' and '[int i = 0; i < 4; i++;]' in loops,row
 elif name=='checkArmorPiece':
  assert slot in ['2','3'] or slot=='3 - i' and '[int i = 0; i < 4; i++;]' in loops,row
 elif name=='checkArmorNull':assert slot=='slot' and row['method'] in ['hasAllProtection','hasAnyProtection','hasProtection','checkArmorPiece'],row
 elif name=='getEquipmentNullable':
  if slot=='slot + 1':assert row['method'] in ['hasAllProtection','hasAnyProtection','hasProtection','checkArmorPiece','checkArmorNull','damageSuit'],row
  elif slot=='i':assert any(x in loops for x in ['[int i = 0; i < 5; i++;]','[int i = 1; i < 5; i++;]','[int i = 1; i <= 4; i++;]']),row
  else:assert slot in ['0','1','2','3','4'],row
 elif name=='getArmorNullable':assert slot in ['2','3'] or slot=='i' and '[int i = 0; i < 4; i++;]' in loops,row
path=H/'slot-domain.json';evidence={'calls':rows,'counts':dict(counts),'claim':'Repository calls only: five equipment slots 0..4; wrappers receive armor slots 0..3; player armor 0..3. External legacy integer overrides/changed backing arrays are not recreated.'}
if path.exists():assert json.loads(path.read_text())==evidence,'slot domain call/loop closure drift'
else:path.write_text(json.dumps(evidence,indent=2)+'\n')
print('PASS full parsed slot closure: %d calls, 34 equipment reads in 0..4, 10 player armor reads in 0..3; all armor/protection wrapper inputs are literals, bounded loops, or audited forwarding'%len(rows))
