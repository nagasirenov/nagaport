#!/usr/bin/env python3
import collections,csv,hashlib,json,re,subprocess
from pathlib import Path
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2];REPO=ROOT.parent
BASE='c986d3f35683a7b81fe0e2caf928f6a47a7a6b5f'
def base(p):return subprocess.check_output(['git','-C',str(REPO),'show',BASE+':ntm-port-1.12.2/'+p])
rows=json.loads((HERE/'sites.json').read_text());assert len(rows)==26
path='src/main/java/com/hbm/blocks/ModBlocks.java';old=base(path);new=(ROOT/path).read_bytes();inverse=new
for r in rows:
 b=r['before'].encode();a=r['after'].encode();assert old.count(b)==new.count(a)==1
 inverse=inverse.replace(a,b)
 expected='(('+r['builder_owner']+') '+r['receiver']+').withSoundType('+r['sound']+')'
 assert r['after']==r['before'].replace(r['receiver']+'.setStepSound('+r['sound']+')',expected)
 assert r['receiver'].startswith('new '+r['owner']+'(') and '{' not in r['receiver']
assert inverse==old,'Whole ModBlocks inversion, preserving CRLF and all unselected bytes'
for path,addition in json.loads((HERE/'additions.json').read_text()).items():
 s=(ROOT/path).read_bytes();assert s.count(addition.encode())==1 and s.replace(addition.encode(),b'')==base(path)
 name=Path(path).stem;assert f'public {name} withSoundType(net.minecraft.block.SoundType sound)'.encode() in s
 assert b'super.setSoundType(sound);\n\t\treturn this;' in s
 for method in ['setSoundType','setTranslationKey','setCreativeTab','setLightLevel','setHardness','setResistance']:
  assert not re.search(rb'\b(?:public|protected|private)\s+\w+\s+'+method.encode()+rb'\s*\(',s)
hot='src/main/java/com/hbm/blocks/generic/BlockHotHazard.java';s=(ROOT/hot).read_bytes();assert s==base(hot)
assert b'extends BlockHazard {' in s and not re.search(rb'\bBlockHotHazard\s*\(',s)
for method in ['withSoundType','setSoundType','setTranslationKey','setCreativeTab','setLightLevel','setHardness','setResistance','makeBeaconable','setDisplayEffect']:
 assert not re.search(rb'\b(?:public|protected|private)\s+\w+\s+'+method.encode()+rb'\s*\(',s)
before=list(csv.DictReader((HERE/'before.tsv').open(),delimiter='\t'));after=list(csv.DictReader((HERE/'after.tsv').open(),delimiter='\t'))
for r in rows:
 b=[x for x in before if int(x['line'])==r['line']];a=[x for x in after if int(x['line'])==r['line']]
 original=next(x for x in b if x['method']=='setStepSound');changed=next(x for x in a if x['method']=='withSoundType')
 assert original['receiver_source']==r['receiver'] and original['receiver_kind']=='DECLARED'
 assert changed['receiver_source']=='(('+r['builder_owner']+') '+r['receiver']+')'
 assert changed['method_owner']==changed['receiver_type']==changed['return_type']=='com.hbm.blocks.generic.'+r['builder_owner']
 assert changed['method_element_kind']=='METHOD' and changed['method_signature']=='withSoundType(net.minecraft.block.SoundType)'
 assert original['arguments_source']==changed['arguments_source']==r['sound']
 assert original['arguments_type']==changed['arguments_type'] and changed['arguments_kind']=='DECLARED'
 ctor=next(x for x in b if x['method']=='<init>');ctor2=next(x for x in a if x['method']=='<init>')
 assert ctor==ctor2 or all(ctor[k]==ctor2[k] for k in ctor if k not in ['start','end'])
 assert ctor['receiver_source']=='NAMED' and ctor['receiver_kind']=='DECLARED'
 assert ctor['method_element_kind']=='CONSTRUCTOR' and ctor['method_owner']=='com.hbm.blocks.generic.'+r['owner']
 preceding=[x for x in b if x['method']!='<init>' and x['full_invocation'] in r['receiver']]
 assert len(preceding)==r['receiver'].count(').')
 for x in preceding:
  assert x['method_element_kind']=='METHOD' and x['receiver_kind']=='DECLARED'
  if x['method'] in ['makeBeaconable','setDisplayEffect']:
   assert x['method_owner']==x['return_type']=='com.hbm.blocks.generic.'+r['builder_owner']
  else:assert x['method_owner']==x['return_type']=='net.minecraft.block.Block'
  same=next(y for y in a if y['method']==x['method'] and y['full_invocation']==x['full_invocation'])
  assert all(x[k]==same[k] for k in x if k not in ['start','end'])
 def errtexture(calls):return next(x for x in calls if x['method']=='setBlockTextureName')
 assert errtexture(b)['receiver_kind']=='ERROR'
 assert errtexture(a)['receiver_kind']=='DECLARED' and errtexture(a)['receiver_type']=='net.minecraft.block.Block'
 # Trailing texture API remains unresolved: no claim that texture/rendering was fixed.
 assert errtexture(a)['method_element_kind']!='METHOD'
def errors(name):return collections.Counter('\t'.join(x.split('\t')[:1]+x.split('\t')[2:]) for x in (HERE/(name+'-errors.tsv')).read_text().splitlines())
b=errors('before');a=errors('after');removed=b-a;added=a-b
assert sum(b.values())==sum(a.values())==15386
assert sum(removed.values())==sum(added.values())==26
assert all('setStepSound(' in x and x.startswith('src/main/java/com/hbm/blocks/ModBlocks.java\t') for x in removed)
assert all('setBlockTextureName(java.lang.String)' in x and x.startswith('src/main/java/com/hbm/blocks/ModBlocks.java\t') for x in added)
for path,digest in json.loads((HERE/'source-hashes.json').read_text()).items():assert hashlib.sha256((ROOT/path).read_bytes()).hexdigest()==digest,path
print('PASS 26 complete receiver/argument and named constructor attribution closures; native method owner/return bindings; all preceding return identities; three-file byte inversion; source and reused-proof hashes; exactly 26 sound diagnostics replaced by newly visible texture diagnostics, all other diagnostic messages unchanged (15386 total).')
