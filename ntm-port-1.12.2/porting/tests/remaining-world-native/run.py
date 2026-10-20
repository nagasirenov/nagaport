#!/usr/bin/env python3
import argparse,hashlib,importlib.util,json,os,pathlib,re,subprocess,tempfile,zipfile
p=argparse.ArgumentParser();p.add_argument('--java-home',type=pathlib.Path,required=True);p.add_argument('--classpath-file',type=pathlib.Path,required=True);p.add_argument('--legacy-client',type=pathlib.Path,required=True);p.add_argument('--legacy-srg',type=pathlib.Path,required=True);p.add_argument('--verify-attribution',action='store_true');p.add_argument('--verify-closure',action='store_true');a=p.parse_args();h=pathlib.Path(__file__).resolve().parent;root=h.parents[2];cp=a.classpath_file.read_text().strip();java=str(a.java_home/'bin/java');javac=str(a.java_home/'bin/javac');javap=str(a.java_home/'bin/javap');tools=str(a.java_home/'lib/tools.jar');sha=lambda b:hashlib.sha256(b).hexdigest()
assert sha(a.legacy_client.read_bytes())=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
srg=zipfile.ZipFile(a.legacy_srg).read('joined.srg').decode()
for mapping in ['FD: apx/e net/minecraft/world/chunk/Chunk/field_76637_e','FD: aqx/c net/minecraft/world/gen/MapGenBase/field_75039_c','FD: aor/b net/minecraft/tileentity/TileEntity/field_145850_b','FD: sa/o net/minecraft/entity/Entity/field_70170_p']:assert mapping in srg
spec=importlib.util.spec_from_file_location('guard',h/'check_source.py');guard=importlib.util.module_from_spec(spec);spec.loader.exec_module(guard);guard.main()
if a.verify_closure:subprocess.run([os.sys.executable,str(h/'dependency_closure.py'),str(a.classpath_file)],check=True)
with tempfile.TemporaryDirectory(prefix='remaining-world-native-') as td:
 d=pathlib.Path(td)
 def run(cmd,**kwargs):return subprocess.check_output(cmd,stderr=subprocess.STDOUT,text=True,**kwargs)
 def extract(rel,line,prefix):
  row=guard.data['files']['src/main/java/'+rel];e=next(x for x in row['edits'] if x['line']==line);u=(root/'src/main/java'/rel).read_bytes().decode().encode('utf-16-le');s=e['after_start']*2;expected=prefix+e['new'];v=u[s-len(prefix.encode('utf-16-le')):s+len(e['new'].encode('utf-16-le'))].decode('utf-16-le');assert v==expected;return v
 replacements={
 'CHUNK_EXPRESSION':extract('com/hbm/handler/radiation/ChunkRadiationHandlerNT.java',381,'chunk.'),
 'CHAIN_EXPRESSION':extract('com/hbm/handler/radiation/ChunkRadiationHandlerNT.java',308,'p.parent.parent.chunk.'),
 'CLIENT_EXPRESSION':extract('com/hbm/main/ModEventHandlerClient.java',1046,'Minecraft.getMinecraft().player.'),
 'TILE_EXPRESSION':extract('com/hbm/tileentity/machine/TileEntityTesla.java',53,''),
 'OUTER_EXPRESSION':extract('com/hbm/tileentity/machine/rbmk/TileEntityRBMKGauge.java',124,''),
 'MAP_EXPRESSION':extract('com/hbm/world/gen/MapGenNTMFeatures.java',71,'this.')}
 source=(h/'NativeWorldReadTest.java.in').read_text()
 for key,value in replacements.items():source=source.replace('/*'+key+'*/',value)
 native=d/'NativeWorldReadTest.java';native.write_text(source)
 print(run([javac,'-cp',cp,'-d',str(d),str(native)]))
 print(run([java,'-cp',str(d)+os.pathsep+cp,'NativeWorldReadTest'],cwd=d))
 unsigned=d/'legacy-test-container.jar'
 with zipfile.ZipFile(a.legacy_client) as oldzip,zipfile.ZipFile(unsigned,'w') as outzip:
  for info in oldzip.infolist():
   if info.filename.upper().startswith('META-INF/') and info.filename.upper().endswith(('.SF','.RSA','.DSA')):continue
   outzip.writestr(info,oldzip.read(info.filename))
 print(run([javac,'-cp',str(a.legacy_client),'-d',str(d),str(h/'LegacyWorldReadTest.java')]))
 print(run([java,'-cp',str(d)+os.pathsep+str(unsigned)+os.pathsep+cp,'LegacyWorldReadTest'],cwd=d))
 bytecode=run([javap,'-p','-c','-classpath',cp,'net.minecraft.world.chunk.Chunk'])
 getter=re.search(r'public net\.minecraft\.world\.World getWorld\(\);\s+Code:\s+(.+?)(?:\n\n|\Z)',bytecode,re.S).group(1)
 ops=re.findall(r'^\s*\d+: (\w+)',getter,re.M);assert ops==['aload_0','getfield','areturn'] and 'Field world:Lnet/minecraft/world/World;' in getter
 compiled=run([javap,'-p','-c','-classpath',str(d)+os.pathsep+cp,'NativeWorldReadTest','NativeWorldReadTest$Tile','NativeWorldReadTest$TileEntityRBMKGauge$GaugeUnit','NativeWorldReadTest$Map'])
 assert 'Method net/minecraft/world/chunk/Chunk.getWorld:()Lnet/minecraft/world/World;' in compiled
 oldcompiled=run([javap,'-p','-c','-classpath',str(d)+os.pathsep+str(a.legacy_client),'LegacyWorldReadTest']);assert 'Field apx.e:Lahb;' in oldcompiled
 print('PASS actual native getter bytecode exactly aload_0/getfield/areturn, native invocation and original raw-field instructions')
 if a.verify_attribution:
  print(run([javac,'-cp',tools,'-d',str(d),str(h/'RemainingWorldScanner.java')]))
  diagnostic_sets={}
  for stage in ['before','after']:
   files=[];temp=d/stage
   for f in sorted((root/'src/main/java').rglob('*.java')):
    rel=f.relative_to(root).as_posix();raw=f.read_bytes()
    if stage=='before' and rel in guard.data['files']:raw=guard.reverse(raw,guard.data['files'][rel])
    out=temp/rel;out.parent.mkdir(parents=True,exist_ok=True);out.write_bytes(raw);files.append(str(out))
   inputs=d/(stage+'-inputs');inputs.write_text('\n'.join(files)+'\n')
   proc=subprocess.run([java,'-Xmx3g','-cp',str(d)+os.pathsep+tools,'RemainingWorldScanner',str(inputs),str(a.classpath_file),str(h/'targets.txt'),stage,str(d/(stage+'-diagnostics.txt'))],text=True,stdout=subprocess.PIPE,stderr=subprocess.PIPE,check=True)
   expected=(h/(stage+'.tsv')).read_text();assert proc.stdout==expected,(stage,proc.stdout,expected);print(proc.stderr.strip());diagnostic_sets[stage]=__import__('collections').Counter(re.sub(r'^(.*?):(\d+):\d+ ',r'\1:\2 ',line) for line in (d/(stage+'-diagnostics.txt')).read_text().splitlines())
  added=diagnostic_sets['after']-diagnostic_sets['before'];removed=diagnostic_sets['before']-diagnostic_sets['after']
  print('Compiler diagnostic delta: removed',sum(removed.values()),'added',sum(added.values()))
  for line,count in sorted(added.items()):print('EXPOSED',count,line)
  print('PASS all 31 frozen before-selection rows and all 31 exact native destination bindings re-attributed with genuine dependencies')
