#!/usr/bin/env python3
import argparse,json,subprocess,tempfile
from pathlib import Path
H=Path(__file__).resolve().parent
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);a=p.parse_args()
fixtures={
'Native':('import net.minecraft.world.World;import net.minecraft.init.Blocks;class Native{void m(World w,int x){w.setBlock(x,0,0,Blocks.STONE);}}',True),
'Foreign':('import net.minecraft.init.Blocks;class Foreign{static class World{void setBlock(int x,int y,int z,Object b){}} void m(World w){w.setBlock(0,0,0,Blocks.STONE);}}',False),
'ShadowBlocks':('import net.minecraft.world.World;class ShadowBlocks{static class Blocks{static final Object STONE=null;}void m(World w){w.setBlock(0,0,0,Blocks.STONE);}}',False),
'Dynamic':('import net.minecraft.world.World;import net.minecraft.block.Block;class Dynamic{void m(World w,Block b){w.setBlock(0,0,0,b);}}',False),
'ReturnUsed':('import net.minecraft.world.World;import net.minecraft.init.Blocks;class ReturnUsed{boolean m(World w){return w.setBlock(0,0,0,Blocks.STONE);}}',False),
'Effectful':('import net.minecraft.world.World;import net.minecraft.init.Blocks;class Effectful{int x;void m(World w){w.setBlock(x++,x++,x++,Blocks.STONE);}}',True),
'StringArg':('import net.minecraft.world.World;import net.minecraft.init.Blocks;class StringArg{void m(World w){w.setBlock("x",0,0,Blocks.STONE);}}',False),
'Qualified':('class Qualified{void m(net.minecraft.world.World w){w.setBlock(0,0,0,net.minecraft.init.Blocks.STONE);}}',True),
'Comments':('class Comments{String text="w.setBlock(0,0,0,Blocks.STONE)";/* w.setBlock(0,0,0,Blocks.STONE); */}',False),
'Unknown':('import net.minecraft.init.Blocks;class Unknown{void m(UnknownWorld w){w.setBlock(0,0,0,Blocks.STONE);}}',False)}
with tempfile.TemporaryDirectory(prefix='placement-owner-')as td:
 t=Path(td);paths=[]
 for name,(s,expected)in fixtures.items():f=t/(name+'.java');f.write_text(s);paths.append(str(f))
 tools=str(a.java_home/'lib/tools.jar');subprocess.run([str(a.java_home/'bin/javac'),'-cp',tools,'-d',td,str(H/'PlacementOwner.java')],check=True)
 r=subprocess.run([str(a.java_home/'bin/java'),'-cp',td+':'+tools,'PlacementOwner',a.classpath]+paths,capture_output=True,text=True,check=True);rows=[json.loads(l)for l in r.stdout.splitlines()]
 for name,(_,expected)in fixtures.items():actual=any(Path(r['file']).stem==name and r['world']and r['vanilla']and r['ints']and r['statement']for r in rows);assert actual==expected,(name,actual,expected)
 print('PASS 10 actual native javac ownership fixtures: foreign/shadowed/unresolved/dynamic/used-return/comment exclusions; native/effectful/qualified inclusions.')
