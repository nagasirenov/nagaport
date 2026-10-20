#!/usr/bin/env python3
import argparse,base64,collections,hashlib,json,subprocess,tempfile,os
from pathlib import Path
E=Path(__file__).resolve().parent;ROOT=E.parents[2];M=json.loads((E/'manifest.json').read_text())
def dec(s):return base64.b64decode(s).decode()
def records(path):
 out={}
 for line in path.read_text().splitlines():
  r=line.split('\t')
  if r[0]!='NEW':continue
  key=('src/main/java/'+r[1].split('/src/main/java/')[-1],r[7],r[8],dec(r[9]));out[key]=(r[4],int(r[5]),r[6],[dec(x) for x in r[11].split(',')],dec(r[13]))
 return out
def closure(text):
 lines=[x.split('\t') for x in text.splitlines()];fields=[r for r in lines if r[0]=='FIELD'];uses=[r for r in lines if r[0]=='USE']
 assert len(fields)==5 and all(r[3]=='[private]' for r in fields);assert len(uses)==34
 assert collections.Counter(r[1] for r in fields)=={'GuiStructureSave':4,'GuiStructureLoad':1}
 allowed={'initGui':{'setText(java.lang.String)'},'selectFile':{'setText(java.lang.String)'},'drawScreen':{'drawTextBox()'},'onGuiClosed':{'getText()'},'keyTyped':{'textboxKeyTyped(char,int)','getText()'},'mouseClicked':{'mouseClicked(int,int,int)','getText()'}}
 for r in uses:
  assert r[4]!='ESCAPE',(r,'widget escapes screen owner')
  if r[4]=='ASSIGN':assert r[3]=='initGui' and r[5].startswith('new GuiTextField(')
  else:
   assert r[4]=='CALL';owner,method=r[5].split(':',1);assert owner=='net.minecraft.client.gui.GuiTextField' and method in allowed[r[3]],r
 return lines
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path);p.add_argument('--classpath-file',type=Path);a=p.parse_args()
source=(ROOT/M['file']).read_bytes();assert hashlib.sha256(source).hexdigest()==M['after_sha256'];inverse=source.decode();group=collections.Counter(r['after'] for r in M['records'])
for after,n in group.items():
 assert inverse.count(after)==n;old=next(r['constructor'] for r in M['records'] if r['after']==after);inverse=inverse.replace(after,old)
assert hashlib.sha256(inverse.encode()).hexdigest()==M['before_sha256']
before=records(E.parent/'native-text-fields/after-attribution.tsv');after=records(E/'after-attribution.tsv');assert len(before)==len(after)==62
selected={(r['file'],r['owner'],r['method'],r['assignment']):r for r in M['records']};assert len(selected)==5
for key,value in before.items():
 if key not in selected:assert after[key]==value,key
 else:
  r=selected[key];n=after[key];assert value[0:3]==('net.minecraft.client.gui.GuiTextField',5,'true');assert n[0:3]==('net.minecraft.client.gui.GuiTextField',6,'true');assert n[3]==[r['id']]+value[3];assert n[4]=='net.minecraft.client.gui.GuiTextField:GuiTextField(int,net.minecraft.client.gui.FontRenderer,int,int,int,int)'
for owner in ['GuiStructureSave','GuiStructureLoad']:
 rs=[r for r in M['records'] if r['owner']==owner];assert [int(r['id']) for r in rs]==list(range(len(rs)))
for r in M['native_controls']:assert r['constructor'] in (ROOT/r['file']).read_text()
closure((E/'nested-closure.tsv').read_text())
if a.java_home:
 assert a.classpath_file;cp=a.classpath_file.read_text().strip();tools=str(a.java_home/'lib/tools.jar')
 with tempfile.TemporaryDirectory(prefix='structure-widget-closure-') as tmp:
  d=Path(tmp);subprocess.run([str(a.java_home/'bin/javac'),'-cp',tools,'-d',str(d),str(E/'NestedFieldScanner.java')],check=True)
  def scan(path):return subprocess.run([str(a.java_home/'bin/java'),'-cp',str(d)+os.pathsep+tools,'NestedFieldScanner',str(path),cp],check=True,text=True,capture_output=True).stdout
  assert closure(scan(ROOT/M['file']))==closure((E/'nested-closure.tsv').read_text())
  for label,extra in [('escape','System.out.println(textName);'),('responder','textName.setGuiResponder(null);')]:
   target=d/'BlockWandStructure.java';target.write_text(source.decode().replace('textName.setText(tile.name);','textName.setText(tile.name);'+extra,1));output=scan(target)
   try:closure(output)
   except AssertionError:print('REJECTED real-javac widget '+label+' control')
   else:raise AssertionError('surviving '+label+' control')
print('PASS five exact constructor insertions, whole-file inverse, 62-site before/after AST inventory, five private fields/34 references without escape or responder, seven existing native IDs and prior50 sites unchanged.')
