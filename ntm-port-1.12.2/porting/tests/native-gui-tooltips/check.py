#!/usr/bin/env python3
"""Exact bounded AST selection, byte inverse, native target/override closure, controls."""
import argparse,base64,collections,hashlib,json,subprocess,tempfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--project',type=Path,required=True);p.add_argument('--java-home',default='/workspace/shared/nagaport-toolchain/java8');p.add_argument('--classpath-file',default='/tmp/nagaport-ae2-native-classpath.txt');p.add_argument('--reattribute',action='store_true');a=p.parse_args();e=Path(__file__).resolve().parent;m=json.loads((e/'manifest.json').read_text());cp=Path(a.classpath_file).read_text().strip();j=Path(a.java_home)/'bin';dec=lambda s:base64.b64decode(s).decode();sha=lambda b:hashlib.sha256(b).hexdigest();old='func_146283_a';new='drawHoveringText';target='net.minecraft.client.gui.GuiScreen:drawHoveringText(java.util.List<java.lang.String>,int,int)'
members='[net.minecraft.client.gui.GuiScreen:drawHoveringText(java.lang.String,int,int), '+target+', net.minecraft.client.gui.GuiScreen:drawHoveringText(java.util.List<java.lang.String>,int,int,net.minecraft.client.gui.FontRenderer)]'
def rows(text):return [s.split('\t') for s in text.splitlines()]
def calls(rs):return [r for r in rs if r[0]=='CALL']
def path(r):return 'src/main/java/'+r[1].split('/src/main/java/')[1]
def eligible(r):return r[0]=='CALL' and r[4]==old and r[7]=='true' and r[8]=='3' and dec(r[9])=='[java.util.List<java.lang.String>, int, int]' and dec(r[13])==members
before=rows((e/'before-attribution.tsv').read_text());after=rows((e/'after-attribution.tsv').read_text());selected=[r for r in before if eligible(r)];assert len(selected)==len(m['records'])==80;assert len(m['files'])==39
assert len([r for r in calls(before) if r[4]==old])==80
assert collections.Counter(r[4] for r in calls(before))=={old:80,new:4,'drawCreativeTabHoveringText':12}
assert collections.Counter(r[4] for r in calls(after))=={new:84,'drawCreativeTabHoveringText':12}
byfile=collections.defaultdict(list)
for row,record in zip(selected,m['records']):
 assert path(row)==record['file'] and int(row[2])==record['start'] and int(row[3])==record['end'];assert [dec(s) for s in row[10].split(',')]==record['arguments'];assert dec(row[12])==record['method'] and dec(row[5])==record['receiver'] and dec(row[6])==record['receiver_type'];byfile[record['file']].append(record)
def inverse(file,actual):
 records=sorted(byfile[file],key=lambda r:r['start']);shift=0;positions=[]
 for r in records:positions.append((r['start']+shift,r['end']+shift+len(new)-len(old)));shift+=len(new)-len(old)
 for start,end in reversed(positions):assert actual[start:end]==new,(file,start,actual[start:end]);actual=actual[:start]+old+actual[end:]
 return actual
for file,hashes in m['files'].items():
 data=(a.project/file).read_bytes();assert sha(data)==hashes['after_sha256'],file
 restored=inverse(file,data.decode());assert sha(restored.encode())==hashes['before_sha256'],file
 base=subprocess.check_output(['git','show',m['base']+':ntm-port-1.12.2/'+file],cwd=a.project);assert base==restored.encode(),('exact Git base mismatch',file)
 # Each migrated-selector corruption must independently fail the strict source inverse/hash guard.
 for r in byfile[file]:
  shift=sum(len(new)-len(old) for q in byfile[file] if q['start']<r['start']);pos=r['start']+shift;bad=data.decode()[:pos]+'wrongHoverMethod'+data.decode()[pos+len(new):]
  try:inverse(file,bad)
  except AssertionError:pass
  else:raise AssertionError('selector mutant survived '+file)
# Compare every invocation, not just the selected family. Offsets may move, expressions may not.
bc=calls(before);ac=calls(after);assert len(bc)==len(ac)==96
for b,c in zip(bc,ac):
 assert path(b)==path(c);sel=eligible(b);assert c[4]==(new if sel else b[4]);assert b[5:11]==c[5:11];assert b[13]==c[13]
 assert dec(c[12])==dec(b[12]).replace(old,new);assert dec(c[14])==dec(b[14]).replace(old,new)
 assert dec(c[11])==target if sel else b[11]==c[11]
 shift=sum(len(new)-len(old) for r in byfile.get(path(b),[]) if r['start']<int(b[2]));assert int(c[2])==int(b[2])+shift;assert int(c[3])==int(c[2])+len(c[4])
assert [r for r in before if r[0]!='CALL']==[r for r in after if r[0]!='CALL']
declarations=[r for r in before if r[0]=='DECL'];assert len(declarations)==2;assert all('Actor' in dec(r[2]) and 'drawGradientRect' in dec(r[3]) for r in declarations)
assert not any(old in dec(r[3]) for r in before if r[0]=='Z'),'tooltip-containing method writes zLevel'
assert sum(r['receiver']=='<implicit>' and 'FolderButton' not in r['receiver_type'] and 'public void drawString' in r['method'] for r in m['records'])==3
with tempfile.TemporaryDirectory(prefix='tooltip-source-proof-') as temp:
 out=Path(temp);subprocess.run([str(j/'javac'),'-cp',str(Path(a.java_home)/'lib/tools.jar'),'-d',temp,str(e/'TooltipScanner.java')],check=True)
 def scan(files,name):
  listing=out/(name+'-sources.txt');listing.write_text('\n'.join(map(str,files))+'\n');result=subprocess.run([str(j/'java'),'-Xmx3g','-cp',temp+':'+str(Path(a.java_home)/'lib/tools.jar'),'TooltipScanner',str(listing),cp],text=True,stdout=subprocess.PIPE,stderr=subprocess.PIPE,check=True);return rows(result.stdout),result.stderr
 source='''import java.util.*;import net.minecraft.client.gui.*;
class GoodThis extends GuiScreen {void test(List<String> x){this.func_146283_a(x,1,2);}}
class GoodImplicit extends GuiScreen {void test(List<String> x){func_146283_a(x,1,2);}}
class GoodOuter extends GuiScreen {class Inner {void test(List<String> x){func_146283_a(x,1,2);}}}
class Foreign {void func_146283_a(List<String>x,int a,int b){} void test(List<String>x){func_146283_a(x,1,2);}}
class StringArgument extends GuiScreen {void test(String x){func_146283_a(x,1,2);}}
class WrongArity extends GuiScreen {void test(List<String>x){func_146283_a(x,1);}}
class OverrideThree extends GuiScreen {public void drawHoveringText(List<String>x,int a,int b){} void test(List<String>x){func_146283_a(x,1,2);}}
class OverrideFour extends GuiScreen {protected void drawHoveringText(List<String>x,int a,int b,FontRenderer f){} void test(List<String>x){func_146283_a(x,1,2);}}
class OldMethod extends GuiScreen {void func_146283_a(List<String>x,int a,int b){} void test(List<String>x){func_146283_a(x,1,2);}}
class Shadow extends GuiScreen {class Inner {void drawHoveringText(List<String>x,int a,int b){} void test(List<String>x){func_146283_a(x,1,2);}}}
'''
 control=out/'Controls.java';control.write_text(source);cr,_=scan([control],'controls');cc=calls(cr);assert len(cc)==10;assert [dec(r[6]) for r in cc if eligible(r)]==['GoodThis','GoodImplicit','GoodOuter']
 for r in sorted([r for r in cc if eligible(r)],key=lambda r:int(r[2]),reverse=True):source=source[:int(r[2])]+new+source[int(r[3]):]
 control.write_text(source);nr,_=scan([control],'controls-native');assert len([r for r in calls(nr) if dec(r[11])==target])==3
 if a.reattribute:
  files=sorted((a.project/'src/main/java').rglob('*.java'));assert len(files)==4063;actual,diagnostics=scan(files,'full');
  def norm(rs):return [[r[0],path(r)]+r[2:] for r in rs]
  assert norm(actual)==norm(after),'live full-source attribution differs from recorded after snapshot';print(diagnostics.strip())
print('PASS 80 AST-selected native targets, 39 exact whole-file inverses, all 96 calls compared, 3 lexical outer receivers, override/z-state closure, 80 selector mutants, 10 attributed controls (3 accepted, 7 rejected).')
