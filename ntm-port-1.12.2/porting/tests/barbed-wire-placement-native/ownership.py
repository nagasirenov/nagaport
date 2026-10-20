#!/usr/bin/env python3
import argparse,json,subprocess,tempfile
from pathlib import Path
H=Path(__file__).resolve().parent
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);a=p.parse_args()
methods={
'direct':('void direct(World w){w.setBlock(0,0,0,ModBlocks.barbed_wire,0,3);}',True),
'qualified':('void qualified(World w){w.setBlock(0,0,0,com.hbm.blocks.ModBlocks.barbed_wire,0,3);}',True),
'local':('void local(World w){Block barbed_wire=null;w.setBlock(0,0,0,barbed_wire,0,3);}',False),
'parameter':('void parameter(World w,Block barbed_wire){w.setBlock(0,0,0,barbed_wire,0,3);}',False),
'wrongOwner':('void wrongOwner(World w){Foreign ModBlocks=new Foreign();w.setBlock(0,0,0,ModBlocks.barbed_wire,0,3);}',False),
'wrongMeta':('void wrongMeta(World w){w.setBlock(0,0,0,ModBlocks.barbed_wire,1,3);}',False),
'wrongFlags':('void wrongFlags(World w){w.setBlock(0,0,0,ModBlocks.barbed_wire,0,2);}',False),
'used':('boolean used(World w){return w.setBlock(0,0,0,ModBlocks.barbed_wire,0,3);}',False),
'effectful':('void effectful(World w,int x){w.setBlock(x++,x++,x++,ModBlocks.barbed_wire,0,3);}',True),
'foreignReceiver':('void foreignReceiver(Foreign w){w.setBlock(0,0,0,ModBlocks.barbed_wire,0,3);}',False),
'wrongType':('void wrongType(World w){w.setBlock("x",0,0,ModBlocks.barbed_wire,0,3);}',False),
}
with tempfile.TemporaryDirectory()as td:
 t=Path(td);f=t/'Fixture.java';f.write_text('import net.minecraft.block.Block;import net.minecraft.world.World;import com.hbm.blocks.ModBlocks;class Foreign {Block barbed_wire;void setBlock(int x,int y,int z,Block b,int m,int f){}}class Fixture {'+'\n'.join(s for s,e in methods.values())+'}')
 owner=t/'ModBlocks.java';owner.write_text('package com.hbm.blocks;public class ModBlocks {public static net.minecraft.block.Block barbed_wire;}');tools=a.java_home/'lib/tools.jar'
 subprocess.run([str(a.java_home/'bin/javac'),'-cp',str(tools),'-d',td,str(H/'PaletteOwner.java')],check=True)
 r=subprocess.run([str(a.java_home/'bin/java'),'-cp',td+':'+str(tools),'PaletteOwner',a.classpath_file.read_text().strip(),str(f),str(owner)],capture_output=True,text=True,check=True);rows=[json.loads(l)for l in r.stdout.splitlines()]
 for name,(source,expected)in methods.items():
  selected=[r for r in rows if r['method']==name and r['world']and r['vanilla']and r['ints']and r['statement']and r['args'][4:]==['0','3']];assert bool(selected)==expected,(name,selected)
 print('PASS',len(methods),'actual Java8 static-owner/receiver/type/literal/statement controls; field-name and class-name shadowing excluded.')
