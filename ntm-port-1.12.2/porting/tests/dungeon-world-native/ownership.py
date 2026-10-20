#!/usr/bin/env python3
"""Parse-only adversarial fixtures; these are NOT platform compile or gameplay proofs."""
import argparse,json,subprocess,tempfile
from pathlib import Path
HERE=Path(__file__).resolve().parent
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);a=p.parse_args()
fixtures={
 'Owned':'import net.minecraft.world.World; class Owned { void f(World world,int x,int y,int z){world.setBlock(x,y,z,Blocks.air,0,3);} }',
 'Other':'class Other { void f(Other world,int x,int y,int z){world.setBlock(x,y,z,Blocks.air,0,3);} }',
 'Reassigned':'import net.minecraft.world.World; class Reassigned { void f(World world,int x,int y,int z){world=null;world.setBlock(x,y,z,Blocks.air,0,3);} }',
 'Effect':'import net.minecraft.world.World; class Effect { void f(World world,int x,int y,int z){world.setBlock(x++,y,z,choose(),random(),flags());} }',
 'Nested':'import net.minecraft.world.World; class Nested { void f(World world,int x,int y,int z){new Object(){void g(){world.setBlock(x,y,z,Blocks.air,0,3);}};} }',
 'ShadowBlocks':'import net.minecraft.init.Blocks; class ShadowBlocks { Object Blocks; Object f(){return Blocks.air;} }',
 'Comments':'import net.minecraft.init.Blocks; class Comments { /* Blocks.air */ String s="Blocks.air";Object f(){return Blocks.air;} }'
}
with tempfile.TemporaryDirectory(prefix='dungeon-ownership-')as d:
 work=Path(d);paths=[]
 for name,source in fixtures.items():f=work/(name+'.java');f.write_text(source);paths.append(str(f))
 tools=str(a.java_home/'lib/tools.jar');subprocess.run([str(a.java_home/'bin/javac'),'-cp',tools,'-d',d,str(HERE/'PlacementAst.java')],check=True)
 rows=[json.loads(x)for x in subprocess.check_output([str(a.java_home/'bin/java'),'-cp',d+':'+tools,'PlacementAst']+paths,text=True).splitlines()]
 calls={Path(r['file']).stem:r for r in rows if r['kind']=='call'}
 assert calls['Owned']['owned']and calls['Owned']['statement']and calls['Owned']['xyzParams']
 for name in('Other','Reassigned','Nested'):assert not calls[name]['owned'],name
 assert not calls['Effect']['args'][0]['pure'];assert all(not a['pure']for a in calls['Effect']['args'][3:])
 constants=[Path(r['file']).stem for r in rows if r['kind']=='constant'];assert constants==['Comments'],constants
 print('PASS parse-only negative fixtures reject foreign/reassigned/nested receiver, increment/call arguments, shadowed constants, comments and strings')
