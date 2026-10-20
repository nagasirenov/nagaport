#!/usr/bin/env python3
"""Strict isolated CP63 source proof; --fragments allows explicitly coordinated other roots."""
import argparse,base64,collections,hashlib,json,re,subprocess,tempfile,os
from pathlib import Path
E=Path(__file__).resolve().parent;ROOT=E.parents[2]
def dec(s):return base64.b64decode(s).decode()
def parse(text):
 out=[];calls=[]
 for line in text.splitlines():
  r=line.split('\t')
  if r[0]=='NEW':out.append(dict(file='src/main/java/'+r[1].split('/src/main/java/')[-1] if '/src/main/java/' in r[1] else Path(r[1]).name,start=int(r[2]),end=int(r[3]),type=r[4],arity=int(r[5]),plain=r[6]=='true',owner=r[7],method=r[8],assignment=dec(r[9]),loops=dec(r[10]),arguments=[dec(x) for x in r[11].split(',')],argument_types=dec(r[12]),executable=dec(r[13])))
  elif r[0]=='CALL':calls.append((dec(r[2]),dec(r[3])))
 return out,calls
def eligible(r):return r['type']=='net.minecraft.client.gui.GuiTextField' and r['arity']==5 and r['plain'] and r['method']=='initGui' and r['argument_types']=='[net.minecraft.client.gui.FontRenderer, int, int, int, int]'
def key(r):return r['file'],r['owner'],r['method'],r['assignment']
def inverse(text,records):
 for r in reversed(records):
  assert text.count(r['after'])==1,(r['file'],r['assignment'],'changed/duplicate constructor')
  text=text.replace(r['after'],r['constructor'],1)
 return text
p=argparse.ArgumentParser();p.add_argument('--fragments',action='store_true');p.add_argument('--java-home',type=Path);p.add_argument('--classpath-file',type=Path);a=p.parse_args()
m=json.loads((E/'manifest.json').read_text());selected=[r for r in m['records'] if r['selected']];assert len(selected)==50 and len(m['records'])==62 and len(m['files'])==35
before,bcalls=parse((E/'before-attribution.tsv').read_text());after,acalls=parse((E/'after-attribution.tsv').read_text());assert len(before)==len(after)==62
assert all(owner!='net.minecraft.client.gui.GuiTextField' for _,owner in bcalls+acalls),'ID read or responder attachment must be audited'
by_before={key(r):r for r in before};by_after={key(r):r for r in after};assert len(by_before)==len(by_after)==62
by_file=collections.defaultdict(list);by_owner=collections.defaultdict(list)
for r in m['records']:
 b=by_before[key(r)];n=by_after[key(r)]
 for f in ['type','plain','owner','method','assignment','loops']:assert b[f]==n[f]==r[f],(key(r),f)
 assert b['arguments']==r['arguments']
 if r['selected']:
  assert eligible(b);assert n['arguments']==[r['id']]+r['arguments'];assert n['arity']==6
  assert n['executable']=='net.minecraft.client.gui.GuiTextField:GuiTextField(int,net.minecraft.client.gui.FontRenderer,int,int,int,int)'
  assert n['argument_types'] in ('[int, net.minecraft.client.gui.FontRenderer, int, int, int, int]','[byte, net.minecraft.client.gui.FontRenderer, int, int, int, int]')
  assert re.fullmatch(r'(?:\d+|i(?: \+ \d+)?)',r['id']),'ID evaluation must be side-effect free'
  if r['loops']=='[]':assert r['instances']==1
  else:
   bound=re.fullmatch(r'\[(?:int|byte) i = 0;i < (\d+);i\+\+;\]',r['loops']);assert bound and r['instances']==int(bound.group(1)),('loop extent',key(r))
  by_file[r['file']].append(r);by_owner[r['owner']].append(r)
 else:
  assert b['arguments']==n['arguments'] and b['arity']==n['arity'];assert r['constructor'] in (ROOT/r['file']).read_text()
for owner,rs in by_owner.items():
 ids=[]
 for r in rs:
  for i in range(r['instances']):ids.append(eval(r['id'],{'__builtins__':{}},{'i':i}))
 assert sorted(ids)==list(range(len(ids))),(owner,ids,'collision or unstable gap')
for file,records in by_file.items():
 raw=(ROOT/file).read_bytes();restored=inverse(raw.decode(),records).encode()
 if not a.fragments:
  assert hashlib.sha256(raw).hexdigest()==m['files'][file]['after_sha256'],(file,'isolated postimage')
  assert hashlib.sha256(restored).hexdigest()==m['files'][file]['before_sha256'],(file,'whole-file inverse')
# Existing configuration/helper/dispatch remains byte-exact under the whole-file inverse above.
calc=(ROOT/'src/main/java/com/hbm/inventory/gui/GUICalculator.java').read_text()
assert 'private GuiTextField inputField;' in calc and 'inputField.setCanLoseFocus(false);' in calc
assert 'mouseClicked' not in calc and not re.search(r'\binputField\s*[,)]',calc),'Do not certify locked-focus difference without re-auditing dispatch'
assert sum('setCanLoseFocus(false)' in (ROOT/f).read_text() for f in m['files'])==1
# Each constructor independently gets a mutation rejection, plus no-op/out-of-scope edits.
mutants=0
for r in selected:
 text=(ROOT/r['file']).read_text();changed=r['after'].replace('new GuiTextField('+r['id']+', ','new GuiTextField(-999, ',1)
 try:inverse(text.replace(r['after'],changed,1),by_file[r['file']])
 except AssertionError:mutants+=1
 else:raise AssertionError('surviving ID mutant '+str(key(r)))
if a.java_home:
 assert a.classpath_file
 cp=a.classpath_file.read_text().strip();tools=str(a.java_home/'lib/tools.jar')
 with tempfile.TemporaryDirectory(prefix='text-field-attribution-') as tmp:
  d=Path(tmp);subprocess.run([str(a.java_home/'bin/javac'),'-cp',tools,'-d',str(d),str(E/'TextFieldScanner.java')],check=True)
  files=d/'sources.txt';files.write_text('\n'.join(str(x) for x in sorted((ROOT/'src/main/java').rglob('*.java')))+'\n')
  run=subprocess.run([str(a.java_home/'bin/java'),'-Xmx3g','-cp',str(d)+os.pathsep+tools,'TextFieldScanner',str(files),cp],check=True,text=True,capture_output=True)
  fresh,fcalls=parse(run.stdout);assert [(key(r),r['type'],r['arity'],r['arguments'],r['executable']) for r in fresh]==[(key(r),r['type'],r['arity'],r['arguments'],r['executable']) for r in after]
  assert all(owner!='net.minecraft.client.gui.GuiTextField' for _,owner in fcalls)
  print(run.stderr.strip())
  controls={
   'Positive':'import net.minecraft.client.gui.*; class Positive {FontRenderer f; GuiTextField field; void initGui(){field=new GuiTextField(f,1,2,3,4);}}',
   'ExistingId':'import net.minecraft.client.gui.*; class ExistingId {FontRenderer f; GuiTextField field; void initGui(){field=new GuiTextField(19,f,1,2,3,4);}}',
   'Anonymous':'import net.minecraft.client.gui.*; class Anonymous {FontRenderer f; GuiTextField field; void initGui(){field=new GuiTextField(f,1,2,3,4){};}}',
   'WrongFont':'import net.minecraft.client.gui.*; class WrongFont {Object f; GuiTextField field; void initGui(){field=new GuiTextField(f,1,2,3,4);}}',
   'WrongMethod':'import net.minecraft.client.gui.*; class WrongMethod {FontRenderer f; GuiTextField field; void other(){field=new GuiTextField(f,1,2,3,4);}}',
   'Shadow':'class Shadow {static class GuiTextField {GuiTextField(Object f,int a,int b,int c,int d){}} Object f; GuiTextField field; void initGui(){field=new GuiTextField(f,1,2,3,4);}}',
   'Qualified':'class Qualified {net.minecraft.client.gui.FontRenderer f; net.minecraft.client.gui.GuiTextField field; void initGui(){field=new net.minecraft.client.gui.GuiTextField(f,1,2,3,4);}}',
  }
  for name,source in controls.items():(d/(name+'.java')).write_text(source)
  files.write_text('\n'.join(str(d/(name+'.java')) for name in controls)+'\n')
  run=subprocess.run([str(a.java_home/'bin/java'),'-cp',str(d)+os.pathsep+tools,'TextFieldScanner',str(files),cp],check=True,text=True,capture_output=True)
  records,_=parse(run.stdout);assert len(records)==len(controls);assert {r['owner'] for r in records if eligible(r)}=={'Positive','Qualified'}
  print('PASS seven real-javac AST selection controls (two accepted, five rejected)')
print('PASS 62-site AST inventory, 50 native constructor resolutions, 35 exact source inverses'+(' (fragment mode: whole-file hashes intentionally not asserted)' if a.fragments else '')+', 128 collision-free per-screen IDs, 50 rejected ID mutants; 12 outside-scope constructors unchanged.')
