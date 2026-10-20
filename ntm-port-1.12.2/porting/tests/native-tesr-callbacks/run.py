#!/usr/bin/env python3
"""Offline evidence on exact CP70 + this patch. Usage: run.py JAVA_HOME CLASSPATH_FILE LEGACY_JAR SRG_ZIP OUTPUT_DIR"""
from pathlib import Path
import subprocess,sys,json,hashlib,csv,collections,zipfile,tarfile,io,tempfile
here=Path(__file__).resolve().parent; root=here.parents[3]
java,cpf,legacy,srg,out=map(lambda x:Path(x).resolve(),sys.argv[1:]);out.mkdir(parents=True,exist_ok=True)
manifest=json.loads((here/'manifest.json').read_text());cp=cpf.read_text().strip()
def run(args):
 p=subprocess.run(list(map(str,args)),stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True)
 if p.returncode:raise AssertionError(p.stdout)
 return p.stdout

def sha(data):return hashlib.sha256(data).hexdigest()
def rows(p):return list(csv.DictReader(p.open(),delimiter='\t'))
for p in (legacy,srg):print('INPUT',p.name,sha(p.read_bytes()))
# No source download, dependency installation or external workspace mutation.
classes=out/'compiler';classes.mkdir(exist_ok=True)
print(run([java/'bin/javac','-cp',java/'lib/tools.jar','-d',classes,here/'TesrAttribution.java']))
with tempfile.TemporaryDirectory(prefix='tesr-baseline-') as tmp:
 base=Path(tmp)
 archive=subprocess.check_output(['git','-C',str(root),'archive',manifest['baseline'],'ntm-port-1.12.2/src/main/java'])
 with tarfile.open(fileobj=io.BytesIO(archive)) as z:z.extractall(base)
 edits=0
 for f in manifest['files']:
  old=(base/f['path']).read_bytes();new=old.decode();assert sha(old)==f['before_sha256']
  for e in sorted(f['edits'],key=lambda e:e['start'],reverse=True):
   assert new[e['start']:e['end']]==e['old'];new=new[:e['start']]+e['new']+new[e['end']:];edits+=1
  assert new.encode()==(root/f['path']).read_bytes(),f['path']+' differs from exact sealed patch'
  assert sha(new.encode())==f['after_sha256']
 print('PASS exact forward and reverse source reconstruction:',edits,'edits,',len(manifest['files']),'files')
 for tag,tree in [('before',base/'ntm-port-1.12.2'),('after',root/'ntm-port-1.12.2')]:
  log=run([java/'bin/java','-Xmx5g','-cp',str(classes)+':'+str(java/'lib/tools.jar'),'TesrAttribution',tree,cpf,out/tag]);print(tag,log.strip());(out/(tag+'.log')).write_text(log)
 b=rows(out/'before/methods.tsv');a=rows(out/'after/methods.tsv');selected=[x for x in b if x['signature'].startswith('renderTileEntityAt(')]
 assert len(selected)==230
 ac={x['owner']:x for x in a if x['signature'].startswith('render(')};bc={x['owner']:x for x in rows(out/'before/classes.tsv')};cc={x['owner']:x for x in rows(out/'after/classes.tsv')}
 assert not any(x['signature'].startswith('renderTileEntityAt(') for x in a)
 for x in selected:
  y=ac[x['owner']];assert y['native_override']=='true' and x['native_override']=='false'
  assert x['body']==y['body'];assert bc[x['owner']]['tesr_subtype']==cc[x['owner']]['tesr_subtype']=='true'
  assert x['parameter_kinds']=='DECLARED || DOUBLE || DOUBLE || DOUBLE || FLOAT'
  assert y['parameter_kinds']=='DECLARED || DOUBLE || DOUBLE || DOUBLE || FLOAT || INT || FLOAT'
  assert 'nativeDestroyStage' not in x['source'] and 'nativeAlpha' not in x['source']
 owners={x['owner'] for x in selected};assert not any(x['superclass'] in owners for x in bc.values())
 assert not any(x['owner'] in owners and x['signature'].startswith('render(') for x in b)
 be=collections.Counter(tuple(x) for x in csv.reader((out/'before/errors.tsv').open(),delimiter='\t'));ae=collections.Counter(tuple(x) for x in csv.reader((out/'after/errors.tsv').open(),delimiter='\t'))
 removed=be-ae;assert not ae-be;assert sum(removed.values())==230
 assert all(x[2]=='compiler.err.method.does.not.override.superclass' for x in removed)
 calls=[x for x in rows(out/'after/calls.tsv') if x['path'].endswith('/RenderCrucible.java')]
 assert len(calls)==1 and calls[0]['method_owner']=='com.hbm.render.tileentity.RenderCrucible' and calls[0]['arguments'].endswith(' || -1 || 1.0F')
 summary=dict(selected=230,excluded=0,body_byte_parity=230,native_overrides=230,removed_override_errors=230,added_errors=0,before_errors=sum(be.values()),after_errors=sum(ae.values()),hierarchies=dict(collections.Counter(bc[x['owner']]['superclass'] for x in selected)),direct_call=calls[0])
 (out/'summary.json').write_text(json.dumps(summary,indent=2)+'\n');print(json.dumps(summary,indent=2))
 # Every untouched source byte, including approved foundry overlay, must remain unchanged.
 selected_paths={f['path'] for f in manifest['files']}
 for p in (base/'ntm-port-1.12.2/src/main/java').rglob('*.java'):
  rel=str(p.relative_to(base))
  if rel not in selected_paths:assert p.read_bytes()==(root/rel).read_bytes(),rel
 print('PASS untouched source, existing native TESRs, helper overloads and approved foundry overlay unchanged')
srgtext=zipfile.ZipFile(srg).read('joined.srg').decode()
assert 'MD: bml/a (Laor;DDDF)V net/minecraft/client/renderer/tileentity/TileEntitySpecialRenderer/func_147500_a (Lnet/minecraft/tileentity/TileEntity;DDDF)V' in srgtext
unsigned=out/'legacy-unsigned.jar'
with zipfile.ZipFile(legacy) as a,zipfile.ZipFile(unsigned,'w') as b:
 for e in a.infolist():
  if not e.filename.startswith('META-INF/'):b.writestr(e,a.read(e))
# Original engine class bytes unchanged; remove signatures only for default-package test coexistence.
c=cp+':'+str(unsigned)
print(run([java/'bin/javac','-cp',c,'-d',classes,here/'DispatchParity.java']))
print(run([java/'bin/java','-cp',str(classes)+':'+c,'DispatchParity']))
for name,cpath,cls in [('old-dispatcher',str(legacy),'bmk'),('old-tesr',str(legacy),'bml'),('native-dispatcher',cp,'net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher'),('native-tesr',cp,'net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer')]:
 (out/(name+'.javap')).write_text(run([java/'bin/javap','-p','-c','-l','-classpath',cpath,cls]))
print('PASS bounded signature root; remaining renderer bodies and game visuals NOT certified')
