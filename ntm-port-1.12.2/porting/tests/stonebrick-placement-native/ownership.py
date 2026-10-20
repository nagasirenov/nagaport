#!/usr/bin/env python3
import argparse,json,subprocess,tempfile
from pathlib import Path
H=Path(__file__).resolve().parent
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);a=p.parse_args()
methods={
'random':('void random(World w,Random rand){w.setBlock(0,0,0,Blocks.STONEBRICK,getBrick(rand),3);}',True),
'qualified':('void qualified(World w,Random rand){w.setBlock(0,0,0,net.minecraft.init.Blocks.STONEBRICK,this.getBrick(rand),3);}',True),
'literal':('void literal(World w){w.setBlock(0,0,0,Blocks.STONEBRICK,1,3);}',True),
'local':('void local(World w,Random rand){Block STONEBRICK=null;w.setBlock(0,0,0,STONEBRICK,getBrick(rand),3);}',False),
'wrongOwner':('void wrongOwner(World w,Random rand){Foreign Blocks=new Foreign();w.setBlock(0,0,0,Blocks.STONEBRICK,getBrick(rand),3);}',False),
'wrongSelector':('void wrongSelector(World w,Random rand){w.setBlock(0,0,0,Blocks.STONEBRICK,new Foreign().getBrick(rand),3);}',False),
'wrongMeta':('void wrongMeta(World w){w.setBlock(0,0,0,Blocks.STONEBRICK,15,3);}',False),
'wrongFlags':('void wrongFlags(World w,Random rand){w.setBlock(0,0,0,Blocks.STONEBRICK,getBrick(rand),2);}',False),
'used':('boolean used(World w,Random rand){return w.setBlock(0,0,0,Blocks.STONEBRICK,getBrick(rand),3);}',False),
'effectful':('void effectful(World w,Random rand,int x){w.setBlock(x++,x++,x++,Blocks.STONEBRICK,getBrick(rand),3);}',True),
'foreignReceiver':('void foreignReceiver(Foreign w){w.setBlock(0,0,0,Blocks.STONEBRICK,1,3);}',False),
'wrongType':('void wrongType(World w){w.setBlock("x",0,0,Blocks.STONEBRICK,1,3);}',False),
}
with tempfile.TemporaryDirectory()as td:
 t=Path(td);f=t/'LibraryDungeon.java';f.write_text('package com.hbm.world.dungeon;import java.util.Random;import net.minecraft.block.Block;import net.minecraft.world.World;import net.minecraft.init.Blocks;class Foreign {Block STONEBRICK;int getBrick(Random r){return 0;}void setBlock(int x,int y,int z,Block b,int m,int f){}}class LibraryDungeon {public int getBrick(Random rand){return rand.nextInt(3);}'+'\n'.join(s for s,e in methods.values())+'}')
 tools=a.java_home/'lib/tools.jar';subprocess.run([str(a.java_home/'bin/javac'),'-cp',str(tools),'-d',td,str(H/'PaletteOwner.java')],check=True)
 r=subprocess.run([str(a.java_home/'bin/java'),'-cp',td+':'+str(tools),'PaletteOwner',a.classpath_file.read_text().strip(),str(f)],capture_output=True,text=True,check=True);rows=[json.loads(l)for l in r.stdout.splitlines()]
 for name,(source,expected)in methods.items():
  selected=[r for r in rows if r['method']==name and r['world']and r['vanilla']and r['ints']and r['statement']and r['args'][5]=='3'];assert bool(selected)==expected,(name,selected)
 print('PASS',len(methods),'actual Java8 immutable-owner, selector-owner, receiver/type/statement controls; no unproved metadata provider selected.')
