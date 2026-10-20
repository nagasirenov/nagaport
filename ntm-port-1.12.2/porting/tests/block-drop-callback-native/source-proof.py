#!/usr/bin/env python3
"""Immutable CP65 byte inversion and complete callback declaration/hierarchy audit."""
import argparse,hashlib,json,re,zipfile
from pathlib import Path
H=Path(__file__).resolve().parent;M=H.parents[2];S=M/'src/main/java'
def sha(b):return hashlib.sha256(b).hexdigest()
def lex(s):
 return re.sub(r'//[^\r\n]*|/\*.*?\*/|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'',lambda m:''.join('\n' if c=='\n' else ' ' for c in m[0]),s,flags=re.S)
def method(s,sig):
 start=s.index(sig);code=lex(s);i=code.index('{',start)+1;n=1
 while n:n+=(code[i]=='{')-(code[i]=='}');i+=1
 return s[start:i]
def audit():
 d=json.loads((H/'selection.json').read_text());assert sha((H/'baseline.zip').read_bytes())==d['baseline_zip_sha256'];assert sha((H/'original-methods.json').read_bytes())==d['original_methods_sha256']
 with zipfile.ZipFile(H/'baseline.zip') as z:
  assert sha((H/'original-collaborators.json').read_bytes())==d['original_collaborators_sha256']
  for key,body in json.loads((H/'original-collaborators.json').read_text()).items():
   path,sig=key.split('#',1);assert method(z.read(path).decode(),sig)==body
  for e in d['operations']:
   before=z.read(e['path']);actual=(S/e['path']).read_bytes();expected=before.decode()
   for old,new in e['replacements']:assert expected.count(old)==1,(e['path'],old);expected=expected.replace(old,new)
   assert actual==expected.encode(),('unselected byte change',e['path'])
   inverse=actual.decode()
   for old,new in reversed(e['replacements']):assert inverse.count(new)==1;inverse=inverse.replace(new,old)
   assert inverse.encode()==before
 originals=json.loads((H/'original-methods.json').read_text());normalize=lambda s:re.sub(r'\b(Items|Blocks)\.(\w+)',lambda m:m[1]+'.'+m[2].upper(),s)
 for e in d['selected']:
  assert normalize(originals[e['path']])==e['method'],e['path'];body=e['method'].split('{',1)[1];assert not re.search(r'\b'+e['meta']+r'\b',lex(body)),e['path']
  current=method((S/e['path']).read_text(),'public Item getItemDropped(')
  assert current==e['method'].replace('public Item getItemDropped(int ','public Item getItemDropped(net.minecraft.block.state.IBlockState ',1)
 # Fully-qualified resolution avoids confusing native BlockDirt with NTM's same-named owner.
 classes={};declarations=[];anonymous=[];calls=[]
 for p in sorted(S.rglob('*.java')):
  s=p.read_text();code=lex(s);package=re.search(r'\bpackage\s+([\w.]+)',code)
  if not package:continue
  pkg=package[1];imports={m[1].split('.')[-1]:m[1] for m in re.finditer(r'\bimport\s+([\w.]+)\s*;',code)}
  depth=0;depths=[]
  for c in code:depths.append(depth);depth+=(c=='{')-(c=='}')
  top=[]
  for m in re.finditer(r'\bclass\s+(\w+)(?:\s*<[^{};]*?>)?\s*(?:extends\s+([\w.]+))?',code):
   if depths[m.start()]!=0:continue
   name=pkg+'.'+m[1];parent=m[2];resolved=None if not parent else parent if '.' in parent else imports.get(parent,pkg+'.'+parent);classes[name]={'path':str(p.relative_to(S)),'parent':resolved};top.append(name)
  for m in re.finditer(r'\b(?:public|protected)\s+(?:final\s+)?Item\s+getItemDropped\s*\(',code):
   assert depths[m.start()]==1,('nested/anonymous callback requires explicit attribution',str(p),s.count('\n',0,m.start())+1)
   assert len(top)==1;declarations.append({'owner':top[0],'path':str(p.relative_to(S)),'line':s.count('\n',0,m.start())+1})
  for no,line in enumerate(s.splitlines(),1):
   if 'getItemDropped' in line:calls.append({'path':str(p.relative_to(S)),'line':no,'text':line.strip()})
  for m in re.finditer(r'\bnew\s+([\w.]+)(?:\s*<[^{};]*?>)?\s*\(',code):
   if m[1].split('.')[-1].startswith('Block') or m[1].split('.')[-1] in {e['class'] for e in d['selected']}:
    i=code.index('(',m.start())+1;n=1
    while n and i<len(code):n+=(code[i]=='(')-(code[i]==')');i+=1
    if code[i:].lstrip().startswith('{'):anonymous.append({'path':str(p.relative_to(S)),'line':s.count('\n',0,m.start())+1,'type':m[1]})
 selected={next(x['owner'] for x in declarations if x['path']==e['path']) for e in d['selected']};declowners={x['owner'] for x in declarations};desc=[]
 for name,c in classes.items():
  parent=c['parent'];chain=[];seen=set()
  while parent in classes:
   assert parent not in seen;seen.add(parent);chain.append(parent);parent=classes[parent]['parent']
  selectedanc=selected.intersection(chain)
  if selectedanc:
   assert name not in declowners or name in selected,('excluded descendant callback',name,chain)
   desc.append({'owner':name,'selected_ancestors':sorted(selectedanc),'declares_callback':name in declowners})
 report={'selected_callbacks':len(selected),'changed_files':len(d['operations']),'all_callback_declarations':len(declarations),'selected_descendants':desc,'anonymous_block_constructions':anonymous,'all_callback_occurrences':calls,'classes':classes}
 assert len(selected)==83 and len(d['operations'])==84
 return report
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--report',type=Path);a=p.parse_args();r=audit()
 if a.report:a.report.write_text(json.dumps(r,indent=2)+'\n')
 print('PASS: 83 exact original metadata-unused callbacks; 84 exact whole-file forward/inverse checks; '+str(len(r['selected_descendants']))+' fully-qualified descendants; every callback declaration is top-level, no anonymous callback override.')
