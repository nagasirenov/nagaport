#!/usr/bin/env python3
"""Offline Java8 evidence. Usage: run.py JAVA_HOME CLASSPATH_FILE LEGACY_JAR SRG_ZIP FORGE1614_USERDEV FIELDS_CSV"""
from pathlib import Path
import sys,subprocess,tempfile,json,zipfile,io,hashlib
here=Path(__file__).resolve().parent
root=here.parents[3]
java,cpf,legacy,srg,forge,fields=map(Path,sys.argv[1:])
cp=cpf.read_text().strip()
manifest=json.loads((here/'manifest.json').read_text())
def run(args,success=True):
 p=subprocess.run(list(map(str,args)),text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
 if (p.returncode==0)!=success:raise AssertionError(p.stdout)
 return p.stdout
for p in [legacy,srg,forge,fields]:print('INPUT',p.name,hashlib.sha256(p.read_bytes()).hexdigest())
srgtext=zipfile.ZipFile(srg).read('joined.srg').decode()
for obf,id,name in [('b','field_111265_b','FOLLOW_RANGE'),('c','field_111266_c','KNOCKBACK_RESISTANCE')]:
 assert 'FD: yj/'+obf+' net/minecraft/entity/SharedMonsterAttributes/'+id in srgtext
 assert id+','+name+',' in fields.read_text()
 print('IDENTITY','yj/'+obf,id,name)
with zipfile.ZipFile(forge) as z:
 for name in ['forgepatches.zip','fmlpatches.zip']:
  patch=zipfile.ZipFile(io.BytesIO(z.read(name)))
  assert not any('Attribute' in n for n in patch.namelist())
  print('FORGE',name,'no attribute-class patches')
count=0
for f in manifest['files']:
 path=root/f['path']; old=subprocess.check_output(['git','-C',str(root),'show',manifest['baseline']+':'+f['path']],text=True)
 new=old
 for e in sorted(f['edits'],key=lambda e:e['start'],reverse=True):
  assert new[e['start']:e['end']]==e['old']
  new=new[:e['start']]+e['new']+new[e['end']:]
  count+=1
 assert new==path.read_text(),f['path']+' has unrelated changes'
print('PASS exact source reconstruction',count,'edits',len(manifest['files']),'files; all formulas and registration calls unchanged')
with tempfile.TemporaryDirectory(prefix='native-attributes-') as tmp:
 t=Path(tmp); classes=t/'classes';classes.mkdir()
 javac=java/'bin/javac'; jvm=java/'bin/java'; tools=java/'lib/tools.jar'
 # Signed legacy default package cannot share a loader with unsigned test classes.
 # Copy byte-identical original class/resource bytes, omitting only META-INF signatures.
 unsigned=t/'legacy-unsigned.jar'
 with zipfile.ZipFile(legacy) as a,zipfile.ZipFile(unsigned,'w') as b:
  for e in a.infolist():
   if not e.filename.startswith('META-INF/'):b.writestr(e,a.read(e))
 c=cp+':'+str(unsigned)
 print(run([javac,'-cp',c,'-d',classes,here/'AttributeParity.java']))
 print(run([jvm,'-cp',str(classes)+':'+c,'AttributeParity']))
 print(run([javac,'-cp',tools,'-d',classes,here/'AttributeOwners.java']))
 originals=[]
 for i,f in enumerate(manifest['files']):
  d=t/('source'+str(i));d.mkdir();p=d/Path(f['path']).name
  p.write_text(subprocess.check_output(['git','-C',str(root),'show',manifest['baseline']+':'+f['path']],text=True)); originals.append(p)
 owners=run([jvm,'-cp',str(classes)+':'+str(tools),'AttributeOwners',cp]+originals)
 assert len(owners.splitlines())==count
 for line in owners.splitlines():
  p,a,b,s=line.split('\t');idx=originals.index(Path(p));assert any(e['start']==int(a) and e['end']==int(b) and e['old']==s for e in manifest['files'][idx]['edits'])
 print('PASS all production offsets re-attributed to native class')
 cases={
 'Good':'import net.minecraft.entity.SharedMonsterAttributes; class Good { Object a=SharedMonsterAttributes.followRange; Object b=net.minecraft.entity.SharedMonsterAttributes.knockbackResistance; }',
 'Shadow':'class Shadow { static class SharedMonsterAttributes { static int followRange; } int x=SharedMonsterAttributes.followRange; }',
 'Variable':'import net.minecraft.entity.SharedMonsterAttributes; class Variable { void x(SharedMonsterAttributes SharedMonsterAttributes) { Object x=SharedMonsterAttributes.followRange; } }',
 'Unknown':'class Unknown { Object x=SharedMonsterAttributes.followRange; }',
 'Text':'class Text { String s="SharedMonsterAttributes.followRange"; /* SharedMonsterAttributes.knockbackResistance */ }',
 'ImportOnly':'import static net.minecraft.entity.SharedMonsterAttributes.followRange; class ImportOnly { Object x=followRange; }'}
 for name,source in cases.items():
  p=t/(name+'.java');p.write_text(source)
  result=run([jvm,'-cp',str(classes)+':'+str(tools),'AttributeOwners',cp,p])
  assert len(result.splitlines())==(2 if name=='Good' else 0),(name,result)
 print('PASS owner controls: explicit import/FQ positive; shadow, variable, unresolved, text, static-import negatives')
 source=(root/'ntm-port-1.12.2/src/main/java/com/hbm/util/EntityDamageUtil.java').read_text()
 methods='import net.minecraft.entity.*;\npublic class ExtractedMethods {\n'
 for name in ['knockBack','setBeenAttacked']:
  a=source.index('\tpublic static void '+name+'(');b=source.index('{',a)+1;depth=1
  while depth:depth+=(source[b]=='{')-(source[b]=='}');b+=1
  methods+=source[a:b]+'\n'
 p=t/'ExtractedMethods.java';p.write_text(methods+'}\n')
 print(run([javac,'-cp',cp,'-d',classes,p,here/'ProductionMethods.java']))
 print(run([jvm,'-cp',str(classes)+':'+cp,'ProductionMethods']))
 p.write_text((methods+'}\n').replace('KNOCKBACK_RESISTANCE','knockbackResistance'))
 errors=run([javac,'-cp',cp,'-d',classes,p],False)
 assert 'knockbackResistance' in errors and 'cannot find symbol' in errors
 print('PASS reversed production methods fail native javac on old constant')
 for name in ['FOLLOW_RANGE','KNOCKBACK_RESISTANCE']:
  p=t/'ConstantControl.java';p.write_text('class ConstantControl { Object x=net.minecraft.entity.SharedMonsterAttributes.'+name+'; }')
  run([javac,'-cp',cp,'-d',classes,p])
  p.write_text(p.read_text().replace(name,{'FOLLOW_RANGE':'followRange','KNOCKBACK_RESISTANCE':'knockbackResistance'}[name]))
  assert 'cannot find symbol' in run([javac,'-cp',cp,'-d',classes,p],False)
 print('PASS both native constant positive/reverse compiler controls')
print('PASS bounded attribute migration; no full mod, mob construction or gameplay assertion')
