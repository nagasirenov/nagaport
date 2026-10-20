#!/usr/bin/env python3
"""Exact CP67 whole-file inversion, original-method parity, hierarchy and call attribution."""
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
 d=json.loads((H/'selection.json').read_text());assert d['baseline']=='d860257bb1f4bc6043d5cb80bd752ede1ebc53bd'
 assert sha((H/'baseline.zip').read_bytes())==d['baseline_zip_sha256'];assert sha((H/'original-methods.json').read_bytes())==d['original_methods_sha256']
 originals=json.loads((H/'original-methods.json').read_text())
 with zipfile.ZipFile(H/'baseline.zip') as z:
  for e in d['operations']:
   before=z.read(e['path']);actual=(S/e['path']).read_bytes();assert before.decode().count(e['old'])==1
   assert before.decode().replace(e['old'],e['new']).encode()==actual,('unselected change',e['path'])
   assert actual.decode().count(e['new'])==1 and actual.decode().replace(e['new'],e['old']).encode()==before
  assert z.read('com/hbm/blocks/BlockDummyable.java')==(S/'com/hbm/blocks/BlockDummyable.java').read_bytes(),'helper changed outside this batch'
 for e,op in zip(d['selected'],d['operations']):
  orig=originals[e['path']];adapted=re.sub(r'getTileEntity\(([^,()]+), ([^,()]+), ([^,()]+)\)',r'getTileEntity(new net.minecraft.util.math.BlockPos(\1, \2, \3))',orig)
  assert adapted==e['method'],('original drift',e['path'])
  assert op['old'].replace('\r\n','\n')==e['method']
  prefix=('\n\t\t'+' '.join(op['used_locals'])) if op['used_locals'] else ''
  assert op['new'].split('{',1)[1].replace('\r\n','\n')==prefix+e['body']+'}'
 # Whole-source lexical scan includes excluded callbacks, nested/anonymous declarations and callers.
 classes={};declarations=[];calls=[];anonymous=[]
 for p in sorted(S.rglob('*.java')):
  s=p.read_text();code=lex(s);package=re.search(r'\bpackage\s+([\w.]+)',code)
  if not package:continue
  pkg=package[1];imports={m[1].split('.')[-1]:m[1] for m in re.finditer(r'\bimport\s+([\w.]+)\s*;',code)};depth=0;depths=[];top=[]
  for c in code:depths.append(depth);depth+=(c=='{')-(c=='}')
  for m in re.finditer(r'\bclass\s+(\w+)(?:\s*<[^{};]*?>)?\s*(?:extends\s+([\w.]+))?',code):
   if depths[m.start()]!=0:continue
   name=pkg+'.'+m[1];par=m[2];par=None if not par else par if '.' in par else imports.get(par,pkg+'.'+par);classes[name]={'path':str(p.relative_to(S)),'parent':par};top.append(name)
  for m in re.finditer(r'\b(?:public|protected)\s+(?:final\s+)?boolean\s+onBlockActivated\s*\(',code):
   assert depths[m.start()]==1 and len(top)==1,('nested callback requires explicit audit',str(p),s.count('\n',0,m.start())+1)
   declarations.append({'owner':top[0],'path':str(p.relative_to(S)),'line':s.count('\n',0,m.start())+1,'native':'BlockPos' in s[m.start():s.index(')',m.start())]})
  for no,line in enumerate(s.splitlines(),1):
   if any(x in line for x in ['onBlockActivated','func_149727_a','standardOpenBehavior','findCore(']):calls.append({'path':str(p.relative_to(S)),'line':no,'text':line.strip()})
 selected={next(x['owner'] for x in declarations if x['path']==e['path']) for e in d['selected']};owners={x['owner']:x for x in declarations};desc=[]
 for name,c in classes.items():
  par=c['parent'];chain=[];seen=set()
  while par in classes:
   assert par not in seen;seen.add(par);chain.append(par);par=classes[par]['parent']
  if selected.intersection(chain):
   assert name not in owners or owners[name]['native'],('excluded scalar override would be bypassed',name,chain)
   desc.append({'owner':name,'selected_ancestors':sorted(selected.intersection(chain)),'declares_callback':name in owners})
 assert len(selected)==105
 # No selected callback invokes the old callback (ordinary, super or reflective).
 assert all('onBlockActivated(' not in lex(e['body']) for e in d['selected'])
 report={'selected_callbacks':len(selected),'changed_files':len(d['operations']),'all_callback_declarations':declarations,'selected_descendants':desc,'all_callback_helper_occurrences':calls,'classes':classes}
 return report
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--report',type=Path);a=p.parse_args();r=audit()
 if a.report:a.report.write_text(json.dumps(r,indent=2)+'\n')
 print('PASS 105 original-method parity and CP67 whole-file forward/inverse checks; '+str(len(r['selected_descendants']))+' selected descendants; all callback declarations top-level and no excluded scalar override bypass.')
