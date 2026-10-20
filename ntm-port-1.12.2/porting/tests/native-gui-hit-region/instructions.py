from pathlib import Path
import subprocess,re,json,hashlib
H=Path(__file__).resolve().parent;J='/workspace/shared/nagaport-toolchain/java8/bin/javap';CP=Path('/workspace/shared/nagaport-toolchain/native-classpath.txt').read_text().strip()
def method(owner,cp,sig):
 text=subprocess.check_output([J,'-p','-c','-classpath',cp,owner],text=True);lines=text[text.index(sig):].splitlines();result=[lines[0]]
 for line in lines[1:]:
  if line.startswith('  ') and not line.startswith('    ') and line.strip():break
  result.append(line)
 return '\n'.join(result)+'\n'
a=method('bex','/tmp/nagaport-legacy-client.jar','  protected boolean c(int, int, int, int, int, int);');b=method('net.minecraft.client.gui.inventory.GuiContainer',CP,'  protected boolean isPointInRegion(int, int, int, int, int, int);')
def canonical(s):
 rows=[x.strip() for x in s.splitlines() if re.match(r'\s*\d+:',x)]
 return [' '.join(re.sub(r'#\d+','#FIELD',x).replace('Field i:I','Field guiLeft:I').replace('Field r:I','Field guiTop:I').split()) for x in rows]
assert canonical(a)==canonical(b)
(H/'original-hit-method.javap').write_text(a);(H/'native-hit-method.javap').write_text(b);(H/'instruction-identity.json').write_text(json.dumps({'canonical_sha256':hashlib.sha256('\n'.join(canonical(a)).encode()).hexdigest(),'instructions':len(canonical(a)),'normalization':'javap whitespace, constant-pool reference numbers and the two officially mapped origin field names only; all opcode operands/branches unchanged'},indent=2)+'\n')
print('PASS',len(canonical(a)),'identical original/native method instructions after field linkage normalization')
