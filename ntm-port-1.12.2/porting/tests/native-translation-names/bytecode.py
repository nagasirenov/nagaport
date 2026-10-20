import pathlib,subprocess,re,json,hashlib
h=pathlib.Path(__file__).resolve().parent;j='/workspace/shared/nagaport-toolchain/java8/bin/javap';old='/tmp/nagaport-legacy-client.jar';native='/workspace/shared/nagaport/ntm-port-1.12.2/.gradle/minecraft/forgeBin-1.12.2-14.23.5.2768-PROJECT(ntm-port-1.12.2).jar'
def extract(jar,cls,sig):
 s=subprocess.check_output([j,'-classpath',jar,'-c',cls]).decode();a=s.index('  '+sig+'\n');b=s.find('\n\n',a);return s[a:b]
def ops(s,old):
 out=[]
 for line in s.splitlines():
  m=re.match(r'\s+\d+:\s+(.*)',line)
  if not m:continue
  op=m.group(1);op=re.sub(r'#\d+\s*','',op).replace('ldc_w','ldc');op=re.sub(r'\s+',' ',op)
  for a,b in [('Field m:','Field translationKey:'),('Field b:','Field translationKey:'),('Field a:Laji;','Field block:Lnet/minecraft/block/Block;'),('Method aji.a:','Method net/minecraft/block/Block.getTranslationKey:')]:op=op.replace(a,b)
  out.append(op)
 return out
pairs=[('adb','public adb c(java.lang.String);','net.minecraft.item.Item','public net.minecraft.item.Item setTranslationKey(java.lang.String);'),('adb','public java.lang.String a();','net.minecraft.item.Item','public java.lang.String getTranslationKey();'),('aji','public java.lang.String a();','net.minecraft.block.Block','public java.lang.String getTranslationKey();'),('abh','public java.lang.String a();','net.minecraft.item.ItemBlock','public java.lang.String getTranslationKey();')]
proof=[]
for a,b,c,d in pairs:
 x,y=extract(old,a,b),extract(native,c,d);assert ops(x,True)==ops(y,False),(x,y);proof.append(dict(old=x,native=y,normalized=ops(x,True)))
(h/'bytecode-proof.json').write_text(json.dumps(proof,indent=2)+'\n');(h/'binary-inputs.json').write_text(json.dumps({p:hashlib.sha256(pathlib.Path(p).read_bytes()).hexdigest() for p in [old,native,'/tmp/nagaport1710-srg.zip']},indent=2)+'\n');print('PASS 4 original/native methods have equivalent instruction streams after symbol/constant-pool/ldc-width normalization')
