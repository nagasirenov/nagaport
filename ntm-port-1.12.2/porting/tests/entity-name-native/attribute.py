#!/usr/bin/env python3
"""Offline source parse/analyze, no code generation or full Gradle build."""
from pathlib import Path
import subprocess,tempfile,importlib.util,json,csv,collections,os,sys,re
sys.dont_write_bytecode=True
H=Path(__file__).resolve().parent;ROOT=H.parents[3];PROJECT=ROOT/'ntm-port-1.12.2';J=Path(os.environ.get('JAVA8_HOME','/workspace/shared/nagaport-toolchain/java8'));CPF=Path(os.environ.get('NATIVE_CP_FILE','/tmp/nagaport-ae2-native-classpath.txt'));OUT=Path(os.environ.get('NAME_ATTRIBUTION_OUT','/tmp/nagaport-entity-name-attribution'));OUT.mkdir(parents=True,exist_ok=True)
spec=importlib.util.spec_from_file_location('boundary',H/'source.py');b=importlib.util.module_from_spec(spec);spec.loader.exec_module(b);b.verify()
def run(args):
 p=subprocess.run(list(map(str,args)),stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True);assert p.returncode==0,p.stdout;return p.stdout
run([J/'bin/javac','-cp',J/'lib/tools.jar','-d',OUT,H/'NameAttribution.java'])
if '--verify-existing' not in sys.argv:
 with tempfile.TemporaryDirectory(prefix='entity-name-inverse-') as t:
  old=Path(t)
  for f in (PROJECT/'src/main/java').rglob('*.java'):
   rel=str(f.relative_to(ROOT));p=old/rel;p.parent.mkdir(parents=True,exist_ok=True)
   if rel in b.FILES:p.write_bytes(b.reverse_source(rel,f.read_bytes().decode()).encode())
   else:p.symlink_to(f)
  for tag,tree in [('before',old/'ntm-port-1.12.2'),('after',PROJECT)]:
   log=run([J/'bin/java','-Xmx4g','-cp',str(OUT)+':'+str(J/'lib/tools.jar'),'NameAttribution',tree,CPF,OUT/tag]);(OUT/(tag+'.log')).write_text(log);print(tag,log.strip(),flush=True)
def calls(tag):return [{k:re.sub(r'capture#\d+', 'capture#N', v) for k,v in r.items()} for r in csv.DictReader((OUT/tag/'calls.tsv').open(),delimiter='\t')]
def errs(tag):return collections.Counter((r[0],*r[2:])for r in csv.reader((OUT/tag/'errors.tsv').open(),delimiter='\t'))
before,after=calls('before'),calls('after');assert len(before)==len(after)
selected={(f['path'].split('ntm-port-1.12.2/',1)[1],str(e['line'])):(e['before'],e['after']) for f in b.MANIFEST['files']for e in f['edits']if not e['before'].startswith('public ')};observed=[]
for a,z in zip(before,after):
 key=(a['path'],a['line']);assert key==(z['path'],z['line'])
 if key in selected:
  assert (a['source'],z['source'])==selected[key];assert a['receiver_type']==z['receiver_type'];assert z['method_kind']=='METHOD' and z['method_owner']=='net.minecraft.entity.Entity' and z['method_signature']=='getName()';observed.append(z)
 else:assert a==z,(a,z)
assert len(observed)==2
old,new=errs('before'),errs('after');removed=old-new;added=new-old;assert not added,list(added.items())[:8];assert sum(removed.values())==3,list(removed.items())
assert collections.Counter(k[1]for k in removed)=={'compiler.err.cant.resolve.location.args':2,'compiler.err.method.does.not.override.superclass':1}
summary=dict(baseline=b.MANIFEST['baseline'],changed_production_files=3,selected_calls=2,override_declarations=1,excluded_calls=32,before_errors=sum(old.values()),after_errors=sum(new.values()),removed_errors=3,added_errors=0,selected_attribution=observed,removed_diagnostics=[dict(path=k[0],code=k[1],message=k[2],count=v)for k,v in removed.items()],limitation='All 4065 source units parsed and analyzed; code generation and Gradle/full build not invoked.')
(H/'attribution-summary.json').write_text(json.dumps(summary,indent=2)+'\n');print('PASS two native selector attributions and exact three-error removal with zero new errors; all other name-selector attributions unchanged.')
