#!/usr/bin/env python3
import argparse,json,subprocess,tempfile
from pathlib import Path
H=Path(__file__).resolve().parent
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);a=p.parse_args()
methods={
'field':('void field(World w){w.setBlock(0,0,0,Block2,0,3);}',True),
'qualified':('void qualified(World w){w.setBlock(0,0,0,this.Block2,0,3);}',True),
'local':('void local(World w){Block Block2=null;w.setBlock(0,0,0,Block2,0,3);}',False),
'parameter':('void parameter(World w,Block Block2){w.setBlock(0,0,0,Block2,0,3);}',False),
'wrongField':('void wrongField(World w){w.setBlock(0,0,0,Other,0,3);}',False),
'wrongMeta':('void wrongMeta(World w){w.setBlock(0,0,0,Block2,1,3);}',False),
'wrongFlag':('void wrongFlag(World w){w.setBlock(0,0,0,Block2,0,2);}',False),
'used':('boolean used(World w){return w.setBlock(0,0,0,Block2,0,3);}',False),
'effectful':('void effectful(World w,int x){w.setBlock(x++,x++,x++,Block2,0,3);}',True),
'foreign':('void foreign(Foreign w){w.setBlock(0,0,0,Block2,0,3);}',False),
'string':('void string(World w){w.setBlock("x",0,0,Block2,0,3);}',False),
'unknown':('void unknown(Unresolved w){w.setBlock(0,0,0,Block2,0,3);}',False),
'inherited':('void inherited(World w){w.setBlock(0,0,0,Block4,0,3);}',False),
}
source='package com.hbm.world.dungeon;import net.minecraft.block.Block;import net.minecraft.world.World;class Parent{Block Block4;} class Foreign{void setBlock(int x,int y,int z,Block b,int m,int f){}} public class DesertAtom002 extends Parent { Block Block2,Other;\n'+'\n'.join(s for s,e in methods.values())+'\n}'
targets=json.loads((H/'targets.json').read_text())
for cls,fs in targets.items():
 if cls=='DesertAtom002':continue
 members=[]
 for field in fs:
  method='declaring_'+cls+'_'+field;methods[method]=('',True);members.append('void '+method+'(World w){w.setBlock(0,0,0,'+field+',0,3);}')
 source+=' class '+cls+' {Block '+','.join(fs)+';'+' '.join(members)+'}'
with tempfile.TemporaryDirectory() as td:
 t=Path(td);f=t/'DesertAtom002.java';f.write_text(source);tools=a.java_home/'lib/tools.jar'
 subprocess.run([str(a.java_home/'bin/javac'),'-cp',str(tools),'-d',td,str(H/'PaletteOwner.java')],check=True)
 r=subprocess.run([str(a.java_home/'bin/java'),'-cp',td+':'+str(tools),'PaletteOwner',a.classpath_file.read_text().strip(),str(f)],capture_output=True,text=True,check=True)
 rows=[json.loads(l) for l in r.stdout.splitlines()]
 for name,(s,expected) in methods.items():
  selected=[r for r in rows if r['method']==name and r['world'] and r['vanilla'] and r['ints'] and r['statement'] and r['args'][4:]==['0','3']]
  assert bool(selected)==expected,(name,selected)
 print('PASS',len(methods),'actual Java8 field-owner/receiver/type/literal/statement fixtures; local, parameter, inherited and foreign fields excluded.')
