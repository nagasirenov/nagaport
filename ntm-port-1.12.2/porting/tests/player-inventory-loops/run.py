from pathlib import Path
import json,hashlib,subprocess,tempfile,os,sys,importlib.util
H=Path(__file__).resolve().parent;R=H.parents[2];M=json.loads((H/'source-manifest.json').read_text());J=Path('/workspace/shared/nagaport-toolchain/java8');CP=Path('/workspace/shared/nagaport-toolchain/native-classpath.txt').read_text().strip()
def sha(x):return hashlib.sha256(x).hexdigest()
composed='--composed-source' in sys.argv
if composed:
 subprocess.run([sys.executable,str(H.parent/'check-native-player-inventory-range-source.py')],check=True)
 def module(name,path):
  spec=importlib.util.spec_from_file_location(name,path);m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m);return m
 read=module('reads',H.parent/'player-inventory-reads/check-source.py');write=module('writes',H.parent/'player-inventory-writes/check-source.py')
methods=[]
for f in M['files']:
 data=(R/f['path']).read_bytes()
 if composed:data=write.reverse_source(f['path'],read.reverse_source(f['path'],data.decode(),strict=False),strict=False).encode()
 assert sha(data)==f['after'];raw=data.decode().encode('utf-16-le');shift=0;positions=[]
 for e in f['edits']:
  a=(e['offset']+shift)*2;new=e['new'].encode('utf-16-le');assert raw[a:a+len(new)]==new;positions.append((a,new));shift+=len(e['new'])
  variable=e['variable'];guard=e['new'].strip();methods.append('static List<ItemStack> read%d(NonNullList<ItemStack> slots){List<ItemStack> visits=new ArrayList<>();for(ItemStack %s:slots){%s if(%s!=null)visits.add(%s);}return visits;}'%(len(methods),variable,guard,variable,variable))
 for a,new in reversed(positions):raw=raw[:a]+raw[a+len(new):]
 original=raw.decode('utf-16-le').encode();assert sha(original)==f['before'];assert original==subprocess.check_output(['git','-C',str(R),'show',M['base']+':ntm-port-1.12.2/'+f['path']])
assert len(methods)==10
print('PASS 8 exact CP85 whole-file inverses; ten attributed normalizers extracted unchanged',flush=True)
call='List<ItemStack> actual=null;switch(method){'+''.join('case %d:actual=read%d(slots);break;'%(i,i) for i in range(10))+'}'
source=(H/'NativeLoopTest.java.in').read_text().replace('@METHODS@','\n'.join(methods)).replace('@CALL@',call)
with tempfile.TemporaryDirectory(prefix='native-inventory-loops-') as tmp:
 p=Path(tmp);f=p/'NativeLoopTest.java';f.write_text(source)
 def compile():subprocess.run([str(J/'bin/javac'),'-cp',CP,'-d',tmp,str(f)],check=True)
 def test():return subprocess.run([str(J/'bin/java'),'-cp',tmp+os.pathsep+CP,'NativeLoopTest'],cwd=tmp,text=True,capture_output=True)
 compile();result=test();assert result.returncode==0,result.stdout+result.stderr;print(result.stdout,end='')
 first=M['files'][0]['edits'][0]['new'].strip();bad=source.replace(first,'',1);assert bad!=source;f.write_text(bad);compile();result=test();assert result.returncode!=0 and 'absent sentinel not visited 0' in result.stderr,result.stdout+result.stderr
 print('PASS compiled missing-sentinel guard mutant rejected',flush=True)

with tempfile.TemporaryDirectory(prefix='inventory-loop-scope-') as tmp:
 p=Path(tmp);fixture=p/'Fixture.java';fixture.write_bytes((H/'Fixture.java.in').read_bytes());inputs=p/'inputs.txt';inputs.write_text(str(fixture)+'\n')
 subprocess.run([str(J/'bin/javac'),'-cp',str(J/'lib/tools.jar'),'-d',tmp,str(H/'LoopScanner.java')],check=True)
 result=subprocess.run([str(J/'bin/java'),'-cp',tmp+os.pathsep+str(J/'lib/tools.jar'),'LoopScanner',str(inputs),'/workspace/shared/nagaport-toolchain/native-classpath.txt'],text=True,capture_output=True,check=True)
 lines=result.stdout.splitlines();assert len(lines)==2 and lines[0].endswith('mainInventory') and lines[1].endswith('armorInventory'),result.stdout+result.stderr
 print('PASS two genuine scope positives; shadow, unknown, static misuse, wrong element type and string controls excluded')
