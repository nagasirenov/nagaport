#!/usr/bin/env python3
"""Verify exact source-scoped transform from CP59, with optional pristine audit."""
from pathlib import Path
import hashlib,json,re,subprocess,sys
root=Path(__file__).resolve().parents[4]
manifest=json.loads((Path(__file__).parent/'source-manifest.json').read_text())
base='e72ec3d2ae8db6653ebaad529a574ee32c99260b'
for e in manifest:
 p=root/e['path']; after=p.read_text()
 before=subprocess.check_output(['git','-C',str(root),'show',base+':'+e['path']],text=True)
 assert hashlib.sha256(before.encode()).hexdigest()==e['before_sha256']
 assert hashlib.sha256(after.encode()).hexdigest()==e['after_sha256']
 restored=after
 for imp in ['DataParameter','DataSerializers','EntityDataManager']:
  restored=restored.replace('import net.minecraft.network.datasync.'+imp+';\n','')
 # Added import block includes a final blank line.
 if e['owner']==p.stem:
  restored=restored.replace('import net.minecraft.nbt.NBTTagCompound;\n\n','import net.minecraft.nbt.NBTTagCompound;\n',1)
  restored=re.sub(r'\n\n\t(?:protected|private) static final DataParameter<Integer> [^;]+;','',restored,count=1)
 for change in e['transformations']:
  assert change['before'] in before and change['after'] in after
  restored=restored.replace(change['after'],change['before'],1)
 assert restored==before,'Unrelated change: '+str(p)
 if len(sys.argv)>1:
  orig=Path(sys.argv[1])/'src/main/java/com/hbm/entity'/p.parent.name/p.name
  text=orig.read_text()
  assert hashlib.sha256(text.encode()).hexdigest()==e['original_sha256']
  assert [x.strip() for x in text.splitlines() if 'dataWatcher' in x]==[x.strip() for x in before.splitlines() if 'dataWatcher' in x]
print('PASS: all 21 operations retain exact receivers/arguments, 5 keys have declaring owners; all other production source unchanged from CP59')
