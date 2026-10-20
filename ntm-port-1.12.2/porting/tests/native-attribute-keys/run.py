#!/usr/bin/env python3
import argparse,hashlib,importlib.util,json,os,pathlib,re,subprocess,tempfile,zipfile
p=argparse.ArgumentParser();p.add_argument('--java-home',type=pathlib.Path,required=True);p.add_argument('--classpath-file',type=pathlib.Path,required=True);p.add_argument('--legacy-client',type=pathlib.Path,required=True);p.add_argument('--legacy-srg',type=pathlib.Path,required=True);p.add_argument('--verify-attribution',action='store_true');a=p.parse_args();h=pathlib.Path(__file__).resolve().parent;root=h.parents[2];cp=a.classpath_file.read_text().strip();java=str(a.java_home/'bin/java');javac=str(a.java_home/'bin/javac');javap=str(a.java_home/'bin/javap');tools=str(a.java_home/'lib/tools.jar');sha=lambda b:hashlib.sha256(b).hexdigest()
assert sha(a.legacy_client.read_bytes())=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce';srg=zipfile.ZipFile(a.legacy_srg).read('joined.srg').decode()
for mapping in ['FD: yj/a net/minecraft/entity/SharedMonsterAttributes/field_111267_a','FD: yj/d net/minecraft/entity/SharedMonsterAttributes/field_111263_d','FD: yj/e net/minecraft/entity/SharedMonsterAttributes/field_111264_e','MD: th/a ()Ljava/lang/String; net/minecraft/entity/ai/attributes/IAttribute/func_111108_a ()Ljava/lang/String;']:assert mapping in srg
spec=importlib.util.spec_from_file_location('guard',h/'check_source.py');guard=importlib.util.module_from_spec(spec);spec.loader.exec_module(guard);guard.main()
with tempfile.TemporaryDirectory(prefix='attribute-keys-native-') as td:
 d=pathlib.Path(td)
 def run(cmd,**kwargs):return subprocess.check_output(cmd,stderr=subprocess.STDOUT,text=True,**kwargs)
 unsigned=d/'legacy-test-container.jar'
 with zipfile.ZipFile(a.legacy_client) as original,zipfile.ZipFile(unsigned,'w') as target:
  for info in original.infolist():
   if info.filename.upper().startswith('META-INF/') and info.filename.upper().endswith(('.SF','.RSA','.DSA')):continue
   target.writestr(info,original.read(info.filename))
 merged=cp+os.pathsep+str(unsigned)
 keys=[];assertions=[];mapping={'MAX_HEALTH':('a','generic.maxHealth'),'MOVEMENT_SPEED':('d','generic.movementSpeed'),'ATTACK_DAMAGE':('e','generic.attackDamage')}
 for rel,row in guard.data['files'].items():
  u=(root/rel).read_bytes().decode().encode('utf-16-le')
  for e in row['edits']:
   if e['kind']!='FIELD':continue
   nxt=next(v for v in row['edits'] if v['kind']=='NAME_GETTER' and v['start']>e['start']);lo=e['after_start']*2-len('SharedMonsterAttributes.'.encode('utf-16-le'));hi=(nxt['after_start']+len(nxt['new'])+2)*2;expression=u[lo:hi].decode('utf-16-le');assert expression=='SharedMonsterAttributes.'+e['new']+'.getName()';i=len(keys);keys.append('static String key'+str(i)+'(){return '+expression+';}');old,name=mapping[e['new']];assertions.append('ok(key'+str(i)+'().equals(yj.'+old+'.a())&&key'+str(i)+'().equals("'+name+'"),"exact extracted production key '+str(i)+'");')
 assert len(keys)==29
 def method(rel,signature):
  source=(root/'src/main/java'/rel).read_text();start=source.index(signature);end=source.index('{',start)+1;depth=1
  while depth:depth+=(source[end]=='{')-(source[end]=='}');end+=1
  return source[start:end]
 generated=(h/'AttributeKeyTest.java.in').read_text().replace('/*ALL_KEYS*/','\n'.join(keys)).replace('/*KEY_ASSERTIONS*/','\n'.join(assertions)).replace('/*SWORD_METHOD*/',method('com/hbm/items/tool/ItemSwordAbility.java','public Multimap getItemAttributeModifiers()')).replace('/*V1_METHOD*/',method('com/hbm/items/armor/ItemModV1.java','public Multimap getModifiers(ItemStack armor)'))
 test=d/'AttributeKeyTest.java';test.write_text(generated);print(run([javac,'-cp',merged,'-d',str(d),str(test)]));print(run([java,'-cp',str(d)+os.pathsep+merged,'AttributeKeyTest'],cwd=d))
 for cls,signature,field,classpath in [('tk','public java.lang.String a();','Field a:Ljava/lang/String;',str(a.legacy_client)),('net.minecraft.entity.ai.attributes.BaseAttribute','public java.lang.String getName();','Field translationKey:Ljava/lang/String;',cp)]:
  code=run([javap,'-p','-c','-classpath',classpath,cls]);body=code[code.index(signature)+len(signature):].split('\n\n',1)[0];assert re.findall(r'^\s*\d+: (\w+)',body,re.M)==['aload_0','getfield','areturn'] and field in body
 print('PASS actual original/native name getter bytecode is exactly aload_0/getfield/areturn')
 if a.verify_attribution:
  print(run([javac,'-cp',tools,'-d',str(d),str(h/'AttributeKeyScanner.java')]))
  diagnostics={}
  for stage in ['before','after']:
   files=[];temp=d/stage
   for f in sorted((root/'src/main/java').rglob('*.java')):
    rel=f.relative_to(root).as_posix();raw=f.read_bytes()
    if stage=='before' and rel in guard.data['files']:raw=guard.reverse(raw,guard.data['files'][rel])
    out=temp/rel;out.parent.mkdir(parents=True,exist_ok=True);out.write_bytes(raw);files.append(str(out))
   inputs=d/(stage+'-inputs');inputs.write_text('\n'.join(files)+'\n');proc=subprocess.run([java,'-Xmx3g','-cp',str(d)+os.pathsep+tools,'AttributeKeyScanner',str(inputs),str(a.classpath_file),stage,str(d/(stage+'-diagnostics'))],text=True,stdout=subprocess.PIPE,stderr=subprocess.PIPE,check=True);assert proc.stdout==(h/(stage+'.tsv')).read_text();print(proc.stderr.strip());diagnostics[stage]=__import__('collections').Counter((d/(stage+'-diagnostics')).read_text().splitlines())
  removed=diagnostics['before']-diagnostics['after'];added=diagnostics['after']-diagnostics['before'];assert sum(removed.values())==29 and not added,(removed,added)
  print('PASS diagnostic attribution: exactly 29 missing field errors removed; no newly exposed errors')
  print('PASS all 58 exact before/after native owner and zero-argument name-getter tokens re-attributed')
