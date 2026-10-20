#!/usr/bin/env python3
"""Exact CP68 whole-file inverses and entire neighbor callback hierarchy/caller attribution."""
import argparse,hashlib,json,re,zipfile,difflib
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
 d=json.loads((H/'selection.json').read_text());assert d['baseline']=='175b014cbb5c5990107fced236f7131f234998b6'
 assert sha((H/'baseline.zip').read_bytes())==d['baseline_zip_sha256'];assert sha((H/'original-methods.json').read_bytes())==d['original_methods_sha256']
 with zipfile.ZipFile(H/'baseline.zip') as z:
  for op,e in zip(d['operations'],d['selected']):
   before=z.read(e['path']);actual=(S/e['path']).read_bytes();assert before.decode().count(op['old'])==1;assert actual==before.decode().replace(op['old'],op['new']).encode(),('unselected change',e['path']);assert actual.decode().count(op['new'])==1;assert actual.decode().replace(op['new'],op['old']).encode()==before
   body=op['old'].split('{',1)[1];expected=('\r\n' if '\r\n' in op['old'] else '\n')+'\t\t'+' '.join(op['used_locals'])+body if op['used_locals'] else body
   if op['power_replacement']:
    old,new=op['power_replacement'];assert expected.count(old)==op['power_count']==1;expected=expected.replace(old,new)
   assert op['new'].split('{',1)[1]==expected
   assert not re.search(r'\bnativeState\b|\bfromPos\b',lex(e['body']));assert 'onNeighborBlockChange(' not in lex(e['body'])
  for name in z.namelist():
   if name not in {e['path'] for e in d['selected']}:assert (S/name).read_bytes()==z.read(name),('collaborator changed',name)
 originals=json.loads((H/'original-methods.json').read_text());delta=[]
 for e in d['selected']:delta.extend(difflib.unified_diff((originals[e['path']]+'\n').splitlines(True),(e['method']+'\n').splitlines(True),fromfile='original/'+e['path'],tofile='CP68/'+e['path']))
 assert '\n'.join(line.expandtabs(4).rstrip() for line in ''.join(delta).splitlines())+'\n'==(H/'original-to-cp68.diff').read_text()
 classes={};declarations=[];calls=[]
 for p in sorted(S.rglob('*.java')):
  s=p.read_text();code=lex(s);pkg=re.search(r'\bpackage\s+([\w.]+)',code)
  if not pkg:continue
  imports={m[1].split('.')[-1]:m[1] for m in re.finditer(r'\bimport\s+([\w.]+)\s*;',code)};depth=0;depths=[];top=[]
  for c in code:depths.append(depth);depth+=(c=='{')-(c=='}')
  for m in re.finditer(r'\bclass\s+(\w+)(?:\s*<[^{};]*?>)?\s*(?:extends\s+([\w.]+))?',code):
   if depths[m.start()]!=0:continue
   name=pkg[1]+'.'+m[1];par=m[2];par=None if not par else par if '.' in par else imports.get(par,pkg[1]+'.'+par);classes[name]={'path':str(p.relative_to(S)),'parent':par};top.append(name)
  for m in re.finditer(r'\b(?:public|protected)\s+(?:final\s+)?void\s+(onNeighborBlockChange|neighborChanged)\s*\(',code):
   assert depths[m.start()]==1 and len(top)==1,('nested callback requires explicit attribution',str(p),s.count('\n',0,m.start())+1)
   declarations.append({'owner':top[0],'path':str(p.relative_to(S)),'line':s.count('\n',0,m.start())+1,'native':m[1]=='neighborChanged','signature':s[m.start():s.index(')',m.start())+1]})
  for no,line in enumerate(s.splitlines(),1):
   if any(x in line for x in ['onNeighborBlockChange','neighborChanged']):calls.append({'path':str(p.relative_to(S)),'line':no,'text':line.strip()})
 selected={next(x['owner'] for x in declarations if x['path']==e['path']) for e in d['selected']};owners={x['owner']:x for x in declarations};desc=[];ancestry=[]
 for name,c in classes.items():
  par=c['parent'];chain=[];seen=set()
  while par in classes:
   assert par not in seen;seen.add(par);chain.append(par);par=classes[par]['parent']
  if name in selected:ancestry.append({'owner':name,'chain':chain,'terminal':par})
  if selected.intersection(chain):
   assert name not in owners or owners[name]['native'],('excluded scalar override bypass',name,chain);desc.append({'owner':name,'selected_ancestors':sorted(selected.intersection(chain)),'declares_callback':name in owners})
 assert len(selected)==14;assert sum(op['power_count'] for op in d['operations'])==10
 return {'selected_callbacks':14,'all_declarations':declarations,'selected_ancestry':ancestry,'selected_descendants':desc,'all_occurrences':calls,'classes':classes}
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--report',type=Path);a=p.parse_args();r=audit()
 if a.report:a.report.write_text(json.dumps(r,indent=2)+'\n')
 print('PASS 14 exact CP68 whole-file inverses; 10 bounded boolean power-call adaptations; '+str(len(r['selected_descendants']))+' audited descendants; all callback declarations top-level.')
