#!/usr/bin/env python3
"""Read-only audit of original source declarations from the pinned Git tree."""
import argparse,pathlib,re,subprocess,tempfile
p=argparse.ArgumentParser();p.add_argument('--java-home',type=pathlib.Path,required=True);p.add_argument('--classpath-file',required=True);a=p.parse_args();h=pathlib.Path(__file__).resolve().parent;repo=h.parents[3];base='0732d318302d807ce8579878a0cdde6b18bbba86';prefix='ntm-original-pristine/src/main/java/'
paths=subprocess.check_output(['git','ls-tree','-r','--name-only',base,'--',prefix],cwd=repo,text=True).splitlines();names=('getHeldItem','getHeldItemMainhand','getItemStackFromSlot','getEquipmentInSlot','getCurrentItemOrArmor','getLastActiveItems','setCurrentItemOrArmor')
with tempfile.TemporaryDirectory(prefix='held-original-audit-') as td:
 d=pathlib.Path(td);inputs=[];total=0
 for path in paths:
  if not path.endswith('.java'):continue
  total+=1;s=subprocess.check_output(['git','show',base+':'+path],cwd=repo)
  # Include all name-bearing sources and Unicode-escape sources; parse full units.
  if not any(n.encode() in s for n in names) and b'\\u' not in s:continue
  f=d/path[len(prefix):];f.parent.mkdir(parents=True,exist_ok=True);f.write_bytes(s);inputs.append(f)
 inp=d/'inputs';inp.write_text('\n'.join(map(str,inputs))+'\n');classes=d/'classes';classes.mkdir();tools=str(a.java_home/'lib/tools.jar')
 subprocess.run([str(a.java_home/'bin/javac'),'-cp',tools,'-d',str(classes),str(h/'HeldItemScanner.java')],check=True)
 result=subprocess.run([str(a.java_home/'bin/java'),'-cp',str(classes)+':'+tools,'HeldItemScanner',str(inp),a.classpath_file,'--audit-only'],text=True,capture_output=True,check=True)
 declarations=[s for s in result.stderr.splitlines() if s.startswith('Override declaration audit:')];assert not declarations,declarations
 print('PASS pinned original source declaration audit:',total,'Java files screened;',len(inputs),'candidate/Unicode full units parsed; zero held/equipment-hook declarations')
