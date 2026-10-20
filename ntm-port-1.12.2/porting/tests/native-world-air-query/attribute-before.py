from pathlib import Path
import subprocess,tempfile,os
H=Path(__file__).resolve().parent;R=H.parents[2];J=Path('/workspace/shared/nagaport-toolchain/java8');CPF=Path('/workspace/shared/nagaport-toolchain/native-classpath.txt')
with tempfile.TemporaryDirectory(prefix='world-air-attribution-')as td:
 d=Path(td);tools=J/'lib/tools.jar';subprocess.run([str(J/'bin/javac'),'-cp',str(tools),'-d',str(d),str(H/'WorldAirScanner.java')],check=True);inp=d/'inputs';inp.write_text('\n'.join(str(p)for p in sorted((R/'src/main/java').rglob('*.java'))))
 result=subprocess.run([str(J/'bin/java'),'-Xmx3G','-cp',str(d)+os.pathsep+str(tools),'WorldAirScanner',str(inp),str(CPF)],text=True,stdout=subprocess.PIPE,stderr=subprocess.PIPE);print(result.stderr,end='');assert result.returncode==0
 rows=[]
 for row in result.stdout.splitlines():
  path,a,b,text,owner=row.split('\t');rows.append('\t'.join([str(Path(path).relative_to(R)),a,b,text,owner]))
 assert len(rows)==14,len(rows)
 (H/'before.tsv').write_text('\n'.join(rows)+'\n');print('PASS 14 genuine World receiver calls, three int arguments and native target ownership')
