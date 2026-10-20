#!/usr/bin/env python3
import collections,csv,json
from pathlib import Path
HERE=Path(__file__).resolve().parent
rows=json.loads((HERE/'sites.json').read_text());before=list(csv.DictReader((HERE/'before.tsv').open(),delimiter='\t'));after=list(csv.DictReader((HERE/'after.tsv').open(),delimiter='\t'))
assert len(before)==len(after)==62
for row in rows:
 old=[r for r in before if int(r['line'])==row['line']];new=[r for r in after if int(r['line'])==row['line']]
 b=next(r for r in old if r['method']=='setStepSound');a=next(r for r in new if r['method']=='withSoundType');qualified='com.hbm.blocks.generic.'+row['owner']
 assert b['receiver_source']==row['receiver'] and b['receiver_kind']=='DECLARED' and b['receiver_type']=='net.minecraft.block.Block'
 assert a['receiver_source']=='(('+row['owner']+') '+row['receiver']+')'
 assert a['receiver_kind']=='DECLARED' and a['method_element_kind']=='METHOD'
 assert a['receiver_type']==a['method_owner']==a['return_type']==qualified
 assert a['method_signature']=='withSoundType(net.minecraft.block.SoundType)'
 assert a['arguments_source']==b['arguments_source']==row['sound']
 assert a['arguments_type']==b['arguments_type']=='net.minecraft.block.SoundType'
 assert a['arguments_kind']==b['arguments_kind']=='DECLARED'
 for method in ['<init>','setTranslationKey']:
  x=next(r for r in old if r['method']==method);y=next(r for r in new if r['method']==method)
  assert all(x[k]==y[k] for k in x if k not in ['start','end'])
  if method=='<init>':assert x['receiver_source']=='NAMED' and x['method_element_kind']=='CONSTRUCTOR' and x['method_owner']==x['receiver_type']==qualified
  else:assert x['method_element_kind']=='METHOD' and x['method_owner']==x['return_type']=='net.minecraft.block.Block'
 for method in ['setHardness','setResistance','setCreativeTab']:
  xs=[r for r in old if r['method']==method];ys=[r for r in new if r['method']==method];assert len(xs)==len(ys)
  for x,y in zip(xs,ys):
   assert y['method_element_kind']=='METHOD' and y['receiver_kind']=='DECLARED'
   assert y['method_owner']==y['return_type']=='net.minecraft.block.Block'
   assert x['arguments_source']==y['arguments_source'] and x['arguments_type']==y['arguments_type']
 if row['owner']=='BlockRotatablePillar':
  x=next(r for r in old if r['method']=='setBlockTextureName');y=next(r for r in new if r['method']=='setBlockTextureName')
  assert x['receiver_kind']=='ERROR' and y['receiver_kind']=='DECLARED' and y['receiver_type']=='net.minecraft.block.Block'
  assert y['method_element_kind']!='METHOD','Unported texture method must not be silently accepted'
def errors(name):return collections.Counter('\t'.join(x.split('\t')[:1]+x.split('\t')[2:]) for x in (HERE/(name+'-errors.tsv')).read_text().splitlines())
b,a=errors('before'),errors('after');removed,added=b-a,a-b
assert sum(b.values())==10366 and sum(a.values())==10359
assert sum(removed.values())==10 and sum(added.values())==3
assert all(x.startswith('src/main/java/com/hbm/blocks/ModBlocks.java\t') and 'setStepSound(' in x for x in removed)
assert all(x.startswith('src/main/java/com/hbm/blocks/ModBlocks.java\t') and 'setBlockTextureName(java.lang.String)' in x for x in added)
for stage in ['before','after']:
 log=(HERE/(stage+'.log')).read_text();assert 'Parsed 4071 units; parse errors=0' in log and 'Selected invocation count=62' in log
summary={'base':'CP92 0675e599e5ee31a60776b560af792372df191790','parsed_units_each':4071,'parse_errors':0,'before_errors':sum(b.values()),'after_errors':sum(a.values()),'removed':dict(removed),'added':dict(added),'scope':'Fresh all-source parse/attribution only, no class generation or full build.'}
(HERE/'attribution-summary.json').write_text(json.dumps(summary,indent=2)+'\n')
print('PASS all 4071 production units parse/attribute, zero syntax errors; 10 exact native owner-builder bindings; 10 legacy sound failures removed, 3 pre-existing texture failures exposed; 10366 -> 10359, no other diagnostic-message changes. No full build/class generation.')
