#!/usr/bin/env python3
import argparse,pathlib,subprocess,tempfile,hashlib,json,zipfile,re,importlib.util
p=argparse.ArgumentParser();p.add_argument('--java-home',type=pathlib.Path,required=True);p.add_argument('--classpath-file',type=pathlib.Path,required=True);p.add_argument('--legacy-client',type=pathlib.Path,required=True);p.add_argument('--original-source',type=pathlib.Path,required=True);p.add_argument('--verify-attribution',action='store_true');p.add_argument('--legacy-srg',type=pathlib.Path,required=True);p.add_argument('--legacy-forge',type=pathlib.Path,required=True);a=p.parse_args()
h=pathlib.Path(__file__).resolve().parent;root=h.parents[2];cp=a.classpath_file.read_text().strip();java=str(a.java_home/'bin/java');javac=str(a.java_home/'bin/javac');tools=str(a.java_home/'lib/tools.jar')
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
assert 'FD: aqo/i net/minecraft/world/WorldProvider/field_76574_g' in zipfile.ZipFile(a.legacy_srg).read('joined.srg').decode()
import io
patches=zipfile.ZipFile(io.BytesIO(zipfile.ZipFile(a.legacy_forge).read('forgepatches.zip')))
patch=patches.read(next(n for n in patches.namelist() if n.endswith('/WorldProvider.java.patch'))).decode()
assert '+    public void setDimension(int dim)\n+    {\n+        this.field_76574_g = dim;\n+    }' in patch
assert patch==(h/'forge-provider.patch').read_text()
bytecode=subprocess.check_output([str(a.java_home/'bin/javap'),'-c','-private','-classpath',cp,'net.minecraft.world.WorldProvider'],text=True)
getter=bytecode.split('public int getDimension();')[1].split('public java.lang.String')[0]
assert 'getfield' in getter and 'Field dimensionId:I' in getter and 'ireturn' in getter and 'invoke' not in getter
print('PASS original SRG identity and actual Forge1614 setter patch; native getter bytecode direct field (old patched setter not runtime-tested)')

spec=importlib.util.spec_from_file_location('guard',h/'check_source.py');guard=importlib.util.module_from_spec(spec);spec.loader.exec_module(guard);guard.main();m=guard.manifest()
with tempfile.TemporaryDirectory(prefix='dimension-proof-') as td:
 d=pathlib.Path(td);subprocess.run([javac,'-cp',tools,'-d',str(d),str(h/'DimensionReadScanner.java')],check=True)
 def scan(paths,after=False):
  inp=d/'inputs';inp.write_text('\n'.join(map(str,paths))+'\n');return subprocess.check_output([java,'-Xmx3g','-cp',str(d)+':'+tools,'DimensionReadScanner',str(inp),str(a.classpath_file)]+(['after'] if after else []),text=True).splitlines()
 prefix='import net.minecraft.world.WorldProvider; import net.minecraft.world.WorldProviderSurface; '
 cases=[('class T {int f(WorldProvider p){return p.dimensionId;}}',1),('class T {void f(WorldProvider p){p.dimensionId=1;}}',0),('class T {void f(WorldProvider p){p.dimensionId+=1;}}',0),('class T {int f(WorldProvider p){return ++p.dimensionId;}}',0),('class T {int f(WorldProvider p){return p.dimensionId--;}}',0),('class T extends WorldProviderSurface {int f(){return dimensionId;}}',1),('class T extends WorldProviderSurface {int f(){return this.dimensionId;}}',1),('class T extends WorldProviderSurface {int f(){return super.dimensionId;}}',1),('class T extends WorldProviderSurface {int dimensionId;int f(){return dimensionId;}}',0),('class T extends WorldProviderSurface {int dimensionId;int f(){return this.dimensionId;}}',0),('class T {int dimensionId;int f(T p){return p.dimensionId;}}',0),('class T {int f(Object p){return p.dimensionId;}}',0),('class T {int f(Missing p){return p.dimensionId;}}',0),('class T {int f(WorldProvider p){return ((WorldProvider)p).dimensionId;}}',1),('class T {int f(){return WorldProvider.dimensionId;}}',0),('class T extends WorldProviderSurface {static int f(){return dimensionId;}}',0),('class T extends WorldProviderSurface {int f(int dimensionId){return dimensionId;}}',0),('class T extends WorldProviderSurface {class Inner{int f(){return dimensionId;}}}',0),('class T<E extends WorldProvider> {int f(E p){return p.dimensionId;}}',0),('class T {int f(){return ((WorldProvider)null).dimensionId;}}',1),('class T extends WorldProviderSurface implements Shadow{int f(){return this.dimensionId;}} interface Shadow{int dimensionId=1;}',0),('class T {String s="dimensionId";int f(WorldProvider p){String s="😀";return p.dimensionId;}}',1)]
 paths=[]
 for i,(src,n) in enumerate(cases):
  f=d/('T'+str(i)+'.java');f.write_text(prefix+re.sub(r'\bT\b','T'+str(i),src));paths.append(f)
 rows=[r.split('\t') for r in scan(paths)]
 for i,(src,n) in enumerate(cases):assert len([r for r in rows if r[0]==str(paths[i])])==n,(i,rows)
 override=d/'Override.java';override.write_text(prefix+'class Override extends WorldProviderSurface {public int getDimension(){return 99;} int f(){return dimensionId;}}')
 try:scan([override]);raise AssertionError('override accepted')
 except subprocess.CalledProcessError:pass
 print('PASS',len(cases)+1,'javac attribution/write/shadow/unqualified/override controls')
 rel='src/main/java/com/hbm/dim/CelestialBody.java'
 def method(source):return re.search(r'public static boolean inOrbit\(World world\) \{[^{}]*\}',source).group()
 native=method((root/rel).read_text());original=method((a.original_source/rel).read_text());assert native==original.replace('dimensionId','getDimension()')
 (d/'GeneratedProduction.java').write_text('import net.minecraft.world.World; class GeneratedProduction {static class SpaceConfig{static int orbitDimension;} '+native+'}')
 (d/'LegacyProduction.java').write_text('class LegacyProduction {static String trace=""; static aqo receive(aqo p){trace+="R";return p;} static int rhs(){trace+="A";return 123;} static int compare(aqo p){return receive(p).i+rhs();} static class SpaceConfig{static int orbitDimension;} '+original.replace('World world','ahb world').replace('world.provider.dimensionId','world.t.i')+'}')
 subprocess.run([javac,'-cp',str(a.legacy_client),'-d',str(d),str(d/'LegacyProduction.java')],check=True)
 unsigned=d/'legacy-test-container.jar'
 with zipfile.ZipFile(a.legacy_client) as z,zipfile.ZipFile(unsigned,'w') as out:
  for i in z.infolist():
   if i.filename.upper().startswith('META-INF/') and i.filename.upper().endswith(('.SF','.RSA','.DSA')):continue
   out.writestr(i,z.read(i.filename))
 subprocess.run([javac,'-cp',cp,'-d',str(d),str(d/'GeneratedProduction.java'),str(h/'NativeDimensionTest.java')],check=True)
 subprocess.run([java,'-cp',str(d)+':'+cp+':'+str(unsigned),'NativeDimensionTest',str(unsigned)],check=True,cwd=d)
 if a.verify_attribution:
  paths=[];tmp=d/'sources'
  for source in sorted((root/'src/main/java').rglob('*.java')):
   rel=source.relative_to(root).as_posix();before=guard.reverse_source(rel,source.read_bytes().decode(),m);dest=tmp/rel;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(before.encode());paths.append(dest)
  rows=scan(paths);normalized=['\t'.join([pathlib.Path(r.split('\t')[0]).relative_to(tmp).as_posix()]+r.split('\t')[1:]) for r in rows]
  assert sorted(normalized)==sorted((h/'attributed-accesses.tsv').read_text().splitlines())
  print('PASS full reconstructed CP54 attribution:',len(rows),'exact positions')

  rows=scan(sorted((root/'src/main/java').rglob('*.java')),True);actual=set()
  for r in rows:
   r=r.split('\t');actual.add((pathlib.Path(r[0]).relative_to(root).as_posix(),int(r[1]),int(r[2]),r[3]))
  for rel,f in m['files'].items():
   for e in f['edits']:
    start=e['after_start']+(5 if e['new'].startswith('this.') else 0)
    assert (rel,start,start+14,'net.minecraft.world.WorldProvider') in actual,(rel,start)
  print('PASS all 231 replacement calls bind to actual native WorldProvider.getDimension()')
