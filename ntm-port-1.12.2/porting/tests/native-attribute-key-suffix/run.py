#!/usr/bin/env python3
import importlib.util,pathlib,subprocess,sys,tempfile,os,zipfile
h=pathlib.Path(__file__).resolve().parent;root=h.parents[2];java,cpf,legacy=map(pathlib.Path,sys.argv[1:]);cp=cpf.read_text().strip();tools=str(java/'lib/tools.jar')
spec=importlib.util.spec_from_file_location('suffix',h/'check_source.py');g=importlib.util.module_from_spec(spec);spec.loader.exec_module(g);g.main()
with tempfile.TemporaryDirectory(prefix='knockback-name-suffix-') as td:
 d=pathlib.Path(td);unsigned=d/'legacy-test-container.jar'
 with zipfile.ZipFile(legacy) as source,zipfile.ZipFile(unsigned,'w') as target:
  for info in source.infolist():
   if info.filename.upper().startswith('META-INF/') and info.filename.upper().endswith(('.SF','.RSA','.DSA')):continue
   target.writestr(info,source.read(info.filename))
 merged=cp+os.pathsep+str(unsigned);subprocess.run([str(java/'bin/javac'),'-cp',merged,'-d',str(d),str(h/'KnockbackNameTest.java')],check=True);subprocess.run([str(java/'bin/java'),'-cp',str(d)+os.pathsep+merged,'KnockbackNameTest'],check=True)
 subprocess.run([str(java/'bin/javac'),'-cp',tools,'-d',str(d),str(h/'KnockbackNameScanner.java')],check=True)
 for stage in ['before','after']:
  files=[]
  for rel,row in g.data['files'].items():
   raw=(root/rel).read_bytes();raw=g.reverse(raw,row) if stage=='before' else raw;dest=d/stage/rel;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(raw);files.append(str(dest))
  inp=d/(stage+'-inputs');inp.write_text('\n'.join(files));r=subprocess.run([str(java/'bin/java'),'-cp',str(d)+os.pathsep+tools,'KnockbackNameScanner',str(inp),str(cpf),stage],text=True,stdout=subprocess.PIPE,stderr=subprocess.PIPE,check=True);assert r.stdout==(h/(stage+'.tsv')).read_text();print(r.stderr.strip())
 print('PASS three genuine public static final IAttribute field owners and exact native name getter bindings; full containing classes remain unported')
 # Compose suffix inverse before primary inverse; no original baseline refresh.
 spec=importlib.util.spec_from_file_location('primary',h.parent/'native-attribute-keys/check_source.py');primary=importlib.util.module_from_spec(spec);spec.loader.exec_module(primary)
 for rel,row in primary.data['files'].items():
  raw=(root/rel).read_bytes()
  if rel in g.data['files']:raw=g.reverse(raw,g.data['files'][rel])
  primary.reverse(raw,row)
 print('PASS primary 19-file exact inverses after composing the three-getter suffix inverse')
