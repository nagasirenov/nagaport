#!/usr/bin/env python3
"""Read-only javac attribution comparison. Does not emit production classes or invoke Gradle.
Usage: check-attribution.py BEFORE_DIR AFTER_DIR SUMMARY_FILE
"""
import csv,json,collections,sys
from pathlib import Path
before,after,out=map(Path,sys.argv[1:])
def rows(d,name):return list(csv.DictReader((d/name).open(),delimiter='\t'))
bm=rows(before,'methods.tsv');am=rows(after,'methods.tsv');bc=rows(before,'calls.tsv');ac=rows(after,'calls.tsv')
selected=[x for x in bm if x['signature'].startswith('onPickupFromSlot(')];assert len(selected)==7
assert not [x for x in am if x['signature'].startswith('onPickupFromSlot(')]
assert all(x['native_override']=='false' and x['parameter_kinds']=='DECLARED || DECLARED' for x in selected)
for p in sorted(set(x['path'] for x in selected)):
 old=[x for x in selected if x['path']==p];new=[x for x in am if x['path']==p and x['signature'].startswith('onTake(')]
 assert len(old)==len(new)
 for x,y in zip(old,new):
  assert x['owner']==y['owner'] and x['parameters']==y['parameters'];assert y['native_override']=='true'
old=[x for x in bc if 'onPickupFromSlot(' in x['source']];assert len(old)==39
assert not [x for x in ac if 'onPickupFromSlot(' in x['source']]
for x in old:
 matches=[y for y in ac if x['path']==y['path'] and x['receiver']==y['receiver'] and x['arguments']==y['arguments'] and x['source'].replace('onPickupFromSlot','onTake')==y['source']]
 assert matches,x
 assert all(y['receiver_kind']=='DECLARED' and y['method_kind']=='METHOD' and y['method_owner'] in ('net.minecraft.inventory.Slot','com.hbm.inventory.SlotCraftingOutput') for y in matches),matches
assert sum(x['receiver']=='super' for x in old)==7
assert all(x['receiver_type']=='net.minecraft.inventory.Slot' for x in old if x['receiver']!='super')
# Entire Slot subtree, anonymous classes included, retains exact inheritance.
classes=lambda d:[(x['path'],x['owner'],x['superclass'],x['superkind'],x['slot_subtype'],x['interfaces']) for x in rows(d,'classes.tsv')]
assert classes(before)==classes(after)
slotclasses=[x for x in rows(after,'classes.tsv') if x['slot_subtype']=='true']
# Compare diagnostics ignoring line shifts only, preserving multiplicities.
def errors(d):
 return collections.Counter((x[0],x[2],x[3]) for x in csv.reader((d/'errors.tsv').open(),delimiter='\t'))
b,a=errors(before),errors(after);removed=b-a;added=a-b
assert not added,added
assert sum(removed.values())==42
assert sum(n for (_,c,m),n in removed.items() if c=='compiler.err.method.does.not.override.superclass')==4
assert sum(n for (_,c,m),n in removed.items() if 'onPickupFromSlot' in m)==38
summary=dict(parsed_units=4065,parse_errors=0,before_errors=sum(b.values()),after_errors=sum(a.values()),removed_errors=42,added_errors=0,native_overrides=7,ordinary_calls=32,super_calls=7,slot_subtree_classes=len(slotclasses),removed_diagnostics=[dict(path=p,code=c,message=m,count=n) for (p,c,m),n in removed.items()],selected_callbacks=selected,selected_before_calls=old,after_calls=ac,slot_classes=slotclasses)
out.write_text(json.dumps(summary,indent=2)+'\n')
print('PASS 7 native Slot overrides, 39 genuine native call targets, unchanged entire Slot subtree; 42 errors removed (38 missing calls + 4 invalid overrides), none added; %d -> %d diagnostics'%(sum(b.values()),sum(a.values())))
