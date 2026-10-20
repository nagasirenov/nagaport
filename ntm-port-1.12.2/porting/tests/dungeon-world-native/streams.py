#!/usr/bin/env python3
"""Independent pristine-source lexical placement oracle vs compiled native statements."""
import argparse,collections,gzip,json,re,subprocess,tempfile,zipfile
from pathlib import Path
HERE=Path(__file__).resolve().parent
TOKEN=re.compile(r'//[^\n]*|/\*[\s\S]*?\*/|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|[A-Za-z_$][\w$]*|\d+|\S')
def calls(source):
 toks=[m for m in TOKEN.finditer(source)if not m.group().startswith(('//','/*'))];out=[];i=0
 while i+3<len(toks):
  if [t.group()for t in toks[i:i+4]]!=['world','.','setBlock','(']:i+=1;continue
  start=toks[i].start();j=i+4;depth=0;parts=[];part=[]
  while j<len(toks):
   t=toks[j].group()
   if t==')'and depth==0:parts.append(''.join(part));break
   if t==','and depth==0:parts.append(''.join(part));part=[]
   else:
    part.append(t)
    if t in('(','[','{'):depth+=1
    if t in(')',']','}'):depth-=1
   j+=1
  out.append((start,parts));i=j+1
 return out

def value(expr,base,axis):
 m=re.fullmatch(axis+r'(?:([+-])(-?\d+))?',expr);assert m,expr
 n=base+(int(m[2])*(1 if m[1]=='+'else -1)if m[1]else 0)
 return (n+2**31)%2**32-2**31

def main():
 p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);a=p.parse_args()
 selected=json.loads(gzip.decompress((HERE/'stream-selection.json.gz').read_bytes()));groups=collections.defaultdict(list)
 for r in selected:groups[r['file']].append(r)
 mapping={}
 for line in(HERE/'native-mapping.tsv').read_text().splitlines():
  if line.startswith('MAP\t'):
   f=line.split('\t');mapping[f[1]]=int(f[4])
 originals={};active=0
 with zipfile.ZipFile(HERE/'sources.zip')as z:
  for file,rows in groups.items():
   base=calls(z.read('baseline/'+file).decode());old=calls(z.read('pristine/'+file).decode());assert [p for _,p in base]==[p for _,p in old],'Original placement sequence changed: '+file;active+=len(old)
   indices={start:i for i,(start,_)in enumerate(base)}
   originals[file]=[old[indices[r['start']]][1]for r in rows]
   for r,v in zip(rows,originals[file]):assert [re.sub(r'\s','',a['text'])for a in r['args']]==v
 origins=[(0,64,0),(-17,200,-31),(29999990,0,-29999990),(-30000000,255,29999999),(2**31-1,64,-2**31),(0,-256,0),(0,256,0),(29999999,64,29999999),(-30000001,64,-30000001)]
 expected=[]
 for origin,xyz in enumerate(origins):
  for file,rows in groups.items():
   for v in originals[file]:
    x,y,z=[value(v[i],xyz[i],'xyz'[i])for i in range(3)]
    if not(-30000000<=x<30000000 and 0<=y<256 and -30000000<=z<30000000):continue
    expected.append('\t'.join(map(str,[Path(file).stem,origin,x,y,z,mapping[v[3][7:]],int(v[4])if len(v)==6 else 0,int(v[5])if len(v)==6 else 3])))
 with tempfile.TemporaryDirectory(prefix='dungeon-streams-')as d:
  work=Path(d);parts=['import net.minecraft.world.World;import net.minecraft.init.Blocks;import net.minecraft.util.math.BlockPos; public final class GeneratedPlacements {'];run=[]
  for file,rows in groups.items():
   stem=Path(file).stem;run.append('world.stream="'+stem+'";')
   # Split test extraction only, not production algorithms; avoid JVM per-method code limit.
   for i in range(0,len(rows),150):
    name=stem+'_'+str(i);run.append(name+'(world,x,y,z);');parts.append('static void '+name+'(World world,int x,int y,int z){\n'+'\n'.join(r['native']for r in rows[i:i+150])+'\n}')
  parts.append('static void run(NativePlacementTest.RecordingWorld world,int x,int y,int z){'+''.join(run)+'}}');(work/'GeneratedPlacements.java').write_text('\n'.join(parts))
  subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-sourcepath',d,'-cp',a.classpath,'-d',d,str(HERE/'NativePlacementTest.java'),str(work/'GeneratedPlacements.java')],check=True)
  output=work/'actual.tsv';subprocess.run([str(a.java_home/'bin/java'),'-Xmx1g','-cp',d+':'+a.classpath,'NativePlacementTest',str(output)],check=True,cwd=d)
  actual=output.read_text().splitlines();assert actual==expected,next((f'Placement {i}: {a!r} != {e!r}'for i,(a,e)in enumerate(zip(actual,expected))if a!=e),'Stream length differs: '+str((len(actual),len(expected))))
  print('PASS independently lexed pristine placement sequence for',len(groups),'generators;',active,'original active placements source-compared;',len(selected),'selected native statements;',len(actual),'exact ordered identity/metadata/flags/coordinate events')
  print('LIMIT: selected call-site execution, not whole generators, random helpers, custom owners, chest loot or a full mod build')
if __name__=='__main__':main()
