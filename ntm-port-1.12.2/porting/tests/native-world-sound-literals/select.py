#!/usr/bin/env python3
import collections,csv,copy,hashlib,json,re,zipfile
from pathlib import Path
r=Path('/workspace/shared/nagaport-world-sound-cp65');e=r/'ntm-port-1.12.2/porting/tests/native-world-sound-literals';real=Path('/workspace/shared/nagaport');orig=real/'ntm-original-pristine/src/main/resources';native=real/'ntm-port-1.12.2/src/main/resources';src=r/'ntm-port-1.12.2/src/main/java';defs=json.loads((orig/'assets/hbm/sounds.json').read_text());newdefs=json.loads((native/'assets/hbm/sounds.json').read_text());registry=(src/'com/hbm/main/ModSoundEvents.java').read_text();owned=set(re.findall(r'create\("([^"]+)"\)',registry));prior=set(re.findall(r'worldEffect\("([^"]+)"',registry));sha=lambda b:hashlib.sha256(b).hexdigest()
def decode(s):return re.sub(r'\\([\\trn])',lambda q:{'\\':'\\','t':'\t','r':'\r','n':'\n'}[q[1]],s)
rows=[{k:decode(v) for k,v in x.items()} for x in csv.DictReader(open('/tmp/world-sound-literals/before.tsv'),delimiter='\t')];selected=[];excluded=[];names={};files={}
for row in rows:
 reason=[];args=row['arguments_source'].split(' || ');types=row['arguments_type'].split(' || ');lit=row['string_literals'].split(' || ');tree=row['arguments_tree_kind'].split(' || ')
 if row['receiver_type']!='net.minecraft.world.World' or row['receiver_kind']!='DECLARED':reason.append('receiver not exact native World')
 if len(args)!=6 or tree[3]!='STRING_LITERAL':reason.append('not six arguments with direct literal name')
 else:
  name=lit[3]
  if not name.startswith('hbm:'):reason.append('vanilla or foreign name excluded')
  else:
   key=name[4:]
   if key in owned:reason.append('previously owned native path excluded')
   if key in ['misc.nullChopper','misc.nullCrashing']:reason.append('special trigger excluded')
   if key not in defs:reason.append('missing original bundled definition')
   else:
    definition=defs[key];converted=copy.deepcopy(definition);samples=[]
    for i,s in enumerate(definition['sounds']):
     name=s if isinstance(s,str) else s['name'];canonical=name if ':' in name else 'hbm:'+name;ns,path=canonical.split(':');oldpath='assets/'+ns+'/sounds/'+path+'.ogg';newpath=oldpath.lower()
     if isinstance(s,str):converted['sounds'][i]=canonical
     else:converted['sounds'][i]['name']=canonical
     if not (orig/oldpath).exists() or not (native/newpath).exists():reason.append('sample missing')
     elif (orig/oldpath).read_bytes()!=(native/newpath).read_bytes():reason.append('sample bytes changed')
     else:samples.append({'original_path':oldpath,'native_path':newpath,'sha256':sha((orig/oldpath).read_bytes())})
    if converted!=newdefs.get(key):reason.append('native definition differs beyond existing namespace qualification')
    resource={'category':definition['category'],'original_definition':definition,'native_definition':newdefs.get(key),'samples':samples}
  primitives={'byte','short','char','int','long','float','double'}
  if any(types[i] not in primitives for i in range(3)) or types[3]!='java.lang.String' or any(types[i] not in primitives-{'double'} for i in [4,5]):reason.append('not exact allowed primitive widening')
 if reason:excluded.append({'path':row['path'],'line':row['line'],'invocation':row['full_invocation'],'reasons':reason});continue
 assert row['method']=='playSoundEffect';name=lit[3];key=name[4:];assert key not in prior # previous scopes should have exhausted their direct-literal calls
 names[key]=resource;selected.append(row);files.setdefault(row['path'],[]).append(row)
manifest={'base':'27dceb0ef9e5815de3dc5f0494ca43aef9997370','calls':selected,'names':names,'files':{},'excluded':excluded,'universe':len(rows),'existing_owned_names':sorted(owned),'prior_world_names':sorted(prior)}
with zipfile.ZipFile(e/'sources.zip','w',zipfile.ZIP_DEFLATED) as z:
 for rel,calls in files.items():
  p=src/rel;b=p.read_bytes();z.writestr(rel,b);s=b.decode().encode('utf-16-le');changes=[]
  for row in sorted(calls,key=lambda x:int(x['start']),reverse=True):
   start,end=int(row['start'])*2,int(row['end'])*2;assert s[start:end].decode('utf-16-le')==row['full_invocation'];tail=s[int(row['select_end'])*2:end].decode('utf-16-le');assert tail.startswith('(');new='com.hbm.sound.WorldSoundEffects.play('+row['receiver_source']+', '+tail[1:];row['new_invocation']=new;s=s[:start]+new.encode('utf-16-le')+s[end:]
  out=s.decode('utf-16-le').encode();p.write_bytes(out);manifest['files'][rel]={'before':sha(b),'after':sha(out),'calls':len(calls)}
 old=registry.encode();z.writestr('com/hbm/main/ModSoundEvents.java',old);category={'record':'RECORDS','block':'BLOCKS','player':'PLAYERS','ambient':'AMBIENT','hostile':'HOSTILE','neutral':'NEUTRAL','weather':'WEATHER','master':'MASTER'}
 block='\n\t\t// Additional compiler-attributed literal coordinate effects with original bundled resources.\n'+''.join('\t\tworldEffect("'+n+'", SoundCategory.'+category[names[n]['category']]+');\n' for n in sorted(names));anchor='\n\t}\n\n\tprivate static void worldEffect(String path, SoundCategory category) {';assert registry.count(anchor)==1;new=registry.replace(anchor,block+anchor);(src/'com/hbm/main/ModSoundEvents.java').write_text(new);manifest['registry']={'before':sha(old),'after':sha(new.encode()),'inserted_block':block}
manifest['immutable']={str(p.relative_to(src)):sha(p.read_bytes()) for p in [src/'com/hbm/sound/WorldSoundEffects.java',src/'com/hbm/sound/WorldSoundCategories.java',src/'com/hbm/sound/SoundCategoryResolver.java']}
(e/'manifest.json').write_text(json.dumps(manifest,indent=2)+'\n');(e/'sites.tsv').write_text(''.join(x['path']+'\t'+x['line']+'\n' for x in selected));print('SELECTED',len(selected),'calls',len(files),'files',len(names),'new names');print('SHAPES',collections.Counter(x['arguments_type'] for x in selected));print('EXCLUSIONS',collections.Counter(y for x in excluded for y in x['reasons']))
