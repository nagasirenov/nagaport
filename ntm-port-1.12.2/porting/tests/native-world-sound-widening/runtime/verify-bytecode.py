#!/usr/bin/env python3
"""Compare actual javac invocation instruction sequences, not a numeric model."""
from pathlib import Path
import json,re,sys
out=Path(sys.argv[1])
def calls(path):
 result={};active=None
 for line in path.read_text().splitlines():
  m=re.match(r'  static void (call\d_\d)\(',line)
  if m:active=m.group(1);result[active]=[];continue
  if active and line and not line.startswith(' '):active=None
  if active and re.match(r'  (public|static|private|protected) ',line):active=None
  m=re.match(r'\s+\d+:\s+([a-z0-9_]+)(.*)',line)
  if active and m:
   value=(m.group(1)+' '+m.group(2).strip()).strip()
   value=re.sub(r'#\d+\s*','',value)
   value=re.sub(r'(Legacy|Native)NumericFixture','NumericFixture',value)
   value=value.replace('Lnet/minecraft/world/World;','LWORLD;').replace('Lahb;','LWORLD;')
   result[active].append(value)
 return result
old=calls(out/'legacy-wrapper.javap');native=calls(out/'native-wrapper.javap')
assert len(old)==len(native)==12
expected={0:['i2d']*3,1:['i2d'],2:['l2d','f2d','i2d','i2f','l2f'],3:['f2d']*3,4:['l2d']*3+['l2f']*2,5:[]}
report=[]
for method,before in old.items():
 after=native[method]
 assert before[-2]=='invokevirtual // Method ahb.a:(DDDLjava/lang/String;FF)V',(method,before[-2])
 assert after[-2]=='invokestatic // Method com/hbm/sound/WorldSoundEffects.play:(LWORLD;DDDLjava/lang/String;FF)V',(method,after[-2])
 assert before[:-2]==after[:-2] and before[-1]==after[-1]=='return',method
 conversions=[x for x in before if re.fullmatch(r'[ilfd]2[ilfd]',x)]
 assert conversions==expected[int(method[4])],(method,conversions)
 report.append({'method':method,'identical_preinvoke_instructions':len(before)-2,'numeric_conversion_opcodes':conversions,'original_call':before[-2],'native_call':after[-2]})
(out/'bytecode-comparison.json').write_text(json.dumps(report,indent=2)+'\n')
print('PASS exact javac pre-invocation bytecode sequence equality for all 12 generated wrappers; migration shapes use only i2d (three/one), float volume/pitch unchanged; separately labelled robustness conversions also match.')
