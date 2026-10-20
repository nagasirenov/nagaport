#!/usr/bin/env python3
import argparse,json,subprocess,tempfile
from pathlib import Path
H=Path(__file__).resolve().parent
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);a=p.parse_args()
prefix='import net.minecraft.world.World;import com.hbm.lib.Library;'
fixtures={
'Native':(prefix+'class Native{void m(World w,int x){w.setBlock(x,0,0,Library.getRandomConcrete(),0,3);}}',True),
'Foreign':('import com.hbm.lib.Library;class Foreign{static class World{void setBlock(int x,int y,int z,Object b,int m,int f){}}void m(World w){w.setBlock(0,0,0,Library.getRandomConcrete(),0,3);}}',False),
'ShadowLibrary':('import net.minecraft.world.World;class ShadowLibrary{static class Library{static net.minecraft.block.Block getRandomConcrete(){return null;}}void m(World w){w.setBlock(0,0,0,Library.getRandomConcrete(),0,3);}}',False),
'Dynamic':(prefix+'class Dynamic{void m(World w,net.minecraft.block.Block b){w.setBlock(0,0,0,b,0,3);}}',False),
'ReturnUsed':(prefix+'class ReturnUsed{boolean m(World w){return w.setBlock(0,0,0,Library.getRandomConcrete(),0,3);}}',False),
'Effectful':(prefix+'class Effectful{int x;void m(World w){w.setBlock(x++,x++,x++,Library.getRandomConcrete(),0,3);}}',True),
'StringArg':(prefix+'class StringArg{void m(World w){w.setBlock("x",0,0,Library.getRandomConcrete(),0,3);}}',False),
'Qualified':('class Qualified{void m(net.minecraft.world.World w){w.setBlock(0,0,0,com.hbm.lib.Library.getRandomConcrete(),0,3);}}',True),
'Comments':('class Comments{String text="w.setBlock(0,0,0,Library.getRandomConcrete(),0,3)";/* w.setBlock(0,0,0,Library.getRandomConcrete(),0,3); */}',False),
'Unknown':(prefix+'class Unknown{void m(UnknownWorld w){w.setBlock(0,0,0,Library.getRandomConcrete(),0,3);}}',False),
'WrongMeta':(prefix+'class WrongMeta{void m(World w){w.setBlock(0,0,0,Library.getRandomConcrete(),1,3);}}',False),
'DynamicFlags':(prefix+'class DynamicFlags{void m(World w,int f){w.setBlock(0,0,0,Library.getRandomConcrete(),0,f);}}',False),
'Overload':(prefix+'class Overload{void m(World w){w.setBlock(0,0,0,Library.getRandomConcrete(2),0,3);}}',False),
}
with tempfile.TemporaryDirectory(prefix='concrete-owner-')as td:
 t=Path(td);paths=[]
 for name,(s,expected)in fixtures.items():f=t/(name+'.java');f.write_text(s);paths.append(str(f))
 # Real unchanged Library source supplies method identity. Its unrelated unresolved bodies do not prove anything.
 paths.append(str(H.parents[2]/'src/main/java/com/hbm/lib/Library.java'))
 tools=str(a.java_home/'lib/tools.jar');subprocess.run([str(a.java_home/'bin/javac'),'-cp',tools,'-d',td,str(H/'PlacementOwner.java')],check=True)
 r=subprocess.run([str(a.java_home/'bin/java'),'-cp',td+':'+tools,'PlacementOwner',a.classpath_file.read_text().strip()]+paths,capture_output=True,text=True,check=True);rows=[json.loads(l)for l in r.stdout.splitlines()]
 for name,(_,expected)in fixtures.items():
  actual=any(Path(r['file']).stem==name and r['world']and r['vanilla']and r['ints']and r['statement']and len(r['args'])==6 and r['args'][4:] == ['0','3'] for r in rows);assert actual==expected,(name,actual,expected)
 print('PASS',len(fixtures),'actual javac owner/primitive/static-helper/literal-metadata/flags fixtures using unchanged production Library source.')
