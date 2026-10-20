#!/usr/bin/env python3
"""Offline: run.py JAVA_HOME CLASSPATH_FILE OLD_CLIENT_JAR SRG_ZIP FIELDS_CSV"""
from pathlib import Path
import sys,json,subprocess,tempfile,zipfile,hashlib
here=Path(__file__).resolve().parent;root=here.parents[3]
java,cpf,old,srg,fields=map(Path,sys.argv[1:]);cp=cpf.read_text().strip();manifest=json.loads((here/'manifest.json').read_text())
def run(args):
 p=subprocess.run(list(map(str,args)),capture_output=True,text=True);assert p.returncode==0,p.stdout+p.stderr;return p.stdout
assert 'FD: akm/a net/minecraft/block/BlockDispenser/field_149943_a' in zipfile.ZipFile(srg).read('joined.srg').decode()
assert 'field_149943_a,DISPENSE_BEHAVIOR_REGISTRY,' in fields.read_text()
assert hashlib.sha256(old.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
with tempfile.TemporaryDirectory(prefix='ntm-dispenser-') as td:
 t=Path(td);sources=[];expected=[]
 for i,f in enumerate(manifest['files']):
  a=subprocess.check_output(['git','-C',str(root),'show',manifest['baseline']+':'+f['path']]);assert hashlib.sha256(a).hexdigest()==f['before_sha256'];b=a.decode()
  for e in reversed(f['edits']):assert b[e['start']:e['end']]==e['old'];b=b[:e['start']]+e['new']+b[e['end']:]
  assert b.encode()==(root/f['path']).read_bytes();assert hashlib.sha256(b.encode()).hexdigest()==f['after_sha256']
  d=t/str(i);d.mkdir();p=d/Path(f['path']).name;p.write_bytes(a);sources.append(p)
  expected += [(str(p),str(e['start']),str(e['end']),e['old']) for e in f['edits']]
 run([java/'bin/javac','-cp',java/'lib/tools.jar','-d',t,here/'DispenserOwners.java'])
 got=run([java/'bin/java','-cp',str(t)+':'+str(java/'lib/tools.jar'),'DispenserOwners',cp]+sources)
 assert [tuple(x.split('\t')) for x in got.splitlines()]==expected
 # Same spelling on a value receiver or local fake class must not be selected.
 controls=t/'Controls.java';controls.write_text('class Controls { static class BlockDispenser { static Object dispenseBehaviorRegistry; } Object a=BlockDispenser.dispenseBehaviorRegistry; net.minecraft.block.BlockDispenser value; Object b=value.dispenseBehaviorRegistry; Object c=net.minecraft.block.BlockDispenser.dispenseBehaviorRegistry; String s="BlockDispenser.dispenseBehaviorRegistry"; }')
 selected=run([java/'bin/java','-cp',str(t)+':'+str(java/'lib/tools.jar'),'DispenserOwners',cp,controls]);assert len(selected.splitlines())==1
 unsigned=t/'old.jar'
 with zipfile.ZipFile(old) as a,zipfile.ZipFile(unsigned,'w') as b:
  for e in a.infolist():
   if not e.filename.startswith('META-INF/'):b.writestr(e,a.read(e))
 runtime=cp+':'+str(unsigned)
 run([java/'bin/javac','-cp',runtime,'-d',t,here/'DispenserRegistryParity.java'])
 print(run([java/'bin/java','-cp',str(t)+':'+runtime,'DispenserRegistryParity']))
 # Reversing the native spelling must fail actual javac, while native field type is genuine.
 check=t/'NativeField.java';check.write_text('class NativeField { Object registry=net.minecraft.block.BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY; }')
 run([java/'bin/javac','-cp',cp,'-d',t,check]);check.write_text(check.read_text().replace('DISPENSE_BEHAVIOR_REGISTRY','dispenseBehaviorRegistry'))
 result=subprocess.run([str(java/'bin/javac'),'-cp',cp,'-d',str(t),str(check)],capture_output=True);assert result.returncode!=0
print('PASS 49 exact attributed edits, two source reversals, native field identity and scope/compiler controls')
