from pathlib import Path
import subprocess,tempfile,os,json
H=Path(__file__).resolve().parent;R=H.parents[2];J=Path('/workspace/shared/nagaport-toolchain/java8');CPF=Path('/workspace/shared/nagaport-toolchain/native-classpath.txt');M=json.loads((H/'manifest.json').read_text())
with tempfile.TemporaryDirectory(prefix='hit-attribution-') as td:
 d=Path(td);tools=J/'lib/tools.jar';subprocess.run([str(J/'bin/javac'),'-cp',str(tools),'-d',str(d),str(H/'GuiHitScanner.java')],check=True);inp=d/'inputs';inp.write_text('\n'.join(str(p)for p in sorted((R/'src/main/java').rglob('*.java'))))
 rows=subprocess.check_output([str(J/'bin/java'),'-Xmx3G','-cp',str(d)+os.pathsep+str(tools),'GuiHitScanner',str(inp),str(CPF)],text=True).splitlines();selected=[]
 for row in rows:
  path,a,b,text,owner=row.split('\t');rel=str(Path(path).relative_to(R))
  if rel not in M['files']:continue
  assert owner=='net.minecraft.client.gui.inventory.GuiContainer';assert text=='this.isPointInRegion(slot.xPos, slot.yPos, 16, 16, x, y)';selected.append(rel)
 assert len(selected)==5
 for p,d in M['files'].items():assert selected.count(p)==d['calls']
 (H/'attribution.tsv').write_text('\n'.join(rows)+'\n');print('PASS five genuine native GuiContainer targets, six exact int arguments and complete receiver closure')
