import argparse,json,shutil,subprocess,tempfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--root',type=Path,required=True);p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);a=p.parse_args();E=Path(__file__).resolve().parent
with tempfile.TemporaryDirectory() as d:
 t=Path(d);subprocess.run([str(a.java_home/'bin/javac'),'-cp',str(a.java_home/'lib/tools.jar'),'-d',d,str(E/'RenderTypeAttribution.java')],check=True)
 before=t/'before';shutil.copytree(a.root/'src/main/java',before/'src/main/java')
 for r in json.loads((E/'sites.json').read_text()):
  p=before/r['path'];s=p.read_bytes().decode();o=r['offset'];assert s[o:o+len(r['after'])]==r['after'];p.write_bytes((s[:o]+r['before']+s[o+len(r['after']):]).encode())
 for name,root in [('before',before),('attribution',a.root)]:
  with (E/(name+'.log')).open('w') as out:subprocess.run([str(a.java_home/'bin/java'),'-Xmx2g','-cp',d+':'+str(a.java_home/'lib/tools.jar'),'RenderTypeAttribution',str(root),str(a.classpath_file),str(E/name)],stdout=out,stderr=subprocess.STDOUT,check=True)
