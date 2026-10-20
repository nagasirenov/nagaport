from pathlib import Path
import subprocess,tempfile,os,re,json,zipfile
H=Path(__file__).resolve().parent;R=H.parents[2];J=Path('/workspace/shared/nagaport-toolchain/java8');CPF=Path('/workspace/shared/nagaport-toolchain/native-classpath.txt');M=json.loads((H/'manifest.json').read_text())
cases=[('class T {int f(Slot s){return s.xDisplayPosition;}}',1),('class T {void f(Slot s){s.yDisplayPosition=4;}}',1),('class T extends Slot {T(){super(null,0,0,0);}int f(int yPos){return yDisplayPosition;}}',1),('class T extends Slot {int yPos;T(){super(null,0,0,0);}int f(){return yDisplayPosition;}}',0),('class T extends Slot {int yDisplayPosition;T(){super(null,0,0,0);}int f(){return yDisplayPosition;}}',0),('class T {int f(Object s){return s.xDisplayPosition;}}',0),('class T {int f(Missing s){return s.xDisplayPosition;}}',0),('class T {int f(){return Slot.xDisplayPosition;}}',0),('class T extends Slot {T(){super(null,0,0,0);}static int f(){return yDisplayPosition;}}',0),('class T extends Slot {T(){super(null,0,0,0);}class Inner {int f(){return yDisplayPosition;}}}',0),('class T extends Slot {T(){super(null,0,0,0);}class Inner {int f(){return T.this.yDisplayPosition;}}}',1),('class T {int xDisplayPosition;int f(T s){return s.xDisplayPosition;}}',0),('class T {int f(Slot s){String u="😀";return s.yDisplayPosition;}}',1),('class T<S extends Slot> {int f(S s){return s.xDisplayPosition;}}',0),('class T extends Slot implements S {T(){super(null,0,0,0);}int f(){return yDisplayPosition;}} interface S{int yPos=3;}',0),('class T extends Slot {T(){super(null,0,0,0);}int f(int yDisplayPosition){return yDisplayPosition;}}',0)]
with tempfile.TemporaryDirectory(prefix='slot-attribution-') as td:
 d=Path(td);tools=J/'lib/tools.jar';subprocess.run([str(J/'bin/javac'),'-cp',str(tools),'-d',str(d),str(H/'NativeFieldScanner.java')],check=True)
 def scan(paths):
  inp=d/'inputs';inp.write_text('\n'.join(map(str,paths)));return subprocess.check_output([str(J/'bin/java'),'-Xmx3G','-cp',str(d)+os.pathsep+str(tools),'NativeFieldScanner',str(inp),str(CPF)],text=True).splitlines()
 paths=[]
 for i,(s,n) in enumerate(cases):
  p=d/('T%d.java'%i);p.write_text('import net.minecraft.inventory.Slot;'+re.sub(r'\bT\b','T%d'%i,s));paths.append(p)
 rows=scan(paths)
 for i,(_,n) in enumerate(cases):assert sum(r.startswith(str(paths[i])+'\t')for r in rows)==n,(i,rows)
 print('PASS 16 typed receiver/shadow/static/nested/local/Unicode controls')
 tree=d/'before';paths=[]
 with zipfile.ZipFile(H/'baseline.zip') as z:
  for p in sorted((R/'src/main/java').rglob('*.java')):
   rel=str(p.relative_to(R));q=tree/rel;q.parent.mkdir(parents=True,exist_ok=True);q.write_bytes(z.read(rel) if rel in M['files'] else p.read_bytes());paths.append(q)
 actual=[]
 for row in scan(paths):
  p,a,b,new,t,old,owner=row.split('\t');actual.append((str(Path(p).relative_to(tree)),int(a),int(b),new,t,old,owner))
 expected=sorted((p,e['start'],e['end'],e['new'],e['receiver'],e['old'],e['owner'])for p,f in M['files'].items()for e in f['edits']);assert sorted(actual)==expected
 print('PASS full-source typed re-attribution of all54 selected reads/writes across13 owners')
