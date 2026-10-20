#!/usr/bin/env python3
"""Reproduce exact native-owner absence and positive/negative compiler controls."""
import argparse,pathlib,subprocess,tempfile,shutil
p=argparse.ArgumentParser();p.add_argument('--java-home',type=pathlib.Path,required=True);p.add_argument('--classpath-file',type=pathlib.Path,required=True);a=p.parse_args()
h=pathlib.Path(__file__).resolve().parent;root=h.parents[2];j=a.java_home/'bin';tools=str(a.java_home/'lib/tools.jar')
with tempfile.TemporaryDirectory(prefix='vector-read-absence-') as td:
 d=pathlib.Path(td);subprocess.run([str(j/'javac'),'-cp',tools,'-d',str(d),str(h/'VectorFieldScanner.java')],check=True)
 def scan(files):
  f=d/'inputs';f.write_text('\n'.join(map(str,files))+'\n');return subprocess.check_output([str(j/'java'),'-Xmx3g','-cp',str(d)+':'+tools,'VectorFieldScanner',str(f),str(a.classpath_file)],text=True).splitlines()
 f=d/'VectorCases.java';shutil.copyfile(h/'VectorCases.java.fixture',f)
 rows=scan([f]);assert len(rows)==4,rows;assert [r.split('\t')[3] for r in rows]==['x','y','z','x'];print('PASS 4 positive native reads; 6 write/other-owner negatives; string exclusion')
 sources=sorted((root/'src/main/java').rglob('*.java'));rows=scan(sources);assert not rows,rows[:10];print('PASS zero native Vec3d legacy-coordinate reads across',len(sources),'production sources')
