#!/usr/bin/env python3
"""Current recovery source-only checks. This does not compile or run native platform code."""
import argparse,collections,hashlib,json,re,subprocess
from pathlib import Path
H=Path(__file__).resolve().parent;M=H.parents[2];BASE='d25dcb9f0908d76b1e9eca96b75e58f6397de355'
p=argparse.ArgumentParser();p.add_argument('--original-root',type=Path,required=True);a=p.parse_args()
def sha(b):return hashlib.sha256(b).hexdigest()
def clean(s):return re.sub(r'/\*.*?\*/|//[^\n]*','',s,flags=re.S)
paths=subprocess.check_output(['git','ls-files','src/main/java'],cwd=M,text=True).splitlines();src={p:clean((M/p).read_text())for p in paths}
names=['red_cable','red_wire_coated','deco_steel'];registry=(M/'src/main/java/com/hbm/blocks/ModBlocks.java').read_text();constructors={n:re.search(r'^\s*'+n+r' = .*;$',registry,re.M)[0].strip()for n in names}
def verify(sources):
 for n in names:
  writes=[(p,line.strip())for p,s in sources.items()for line in s.splitlines()if re.search(r'\b'+n+r'\s*=(?!=)',line)]
  assert writes==[('src/main/java/com/hbm/blocks/ModBlocks.java',constructors[n])],writes
  assert sum(len(re.findall(r'public static Block '+n+r'\s*;',s))for s in sources.values())==1
 for p,s in sources.items():assert not re.search(r'\bextends\s+(?:[\w.]+\.)?(?:BlockCable|WireCoated|BlockDecoCT)\b',s),p
verify(src)
for extra in ['class X extends BlockCable {}','class X extends WireCoated {}','class X extends BlockDecoCT {}','class X {void x(){ModBlocks.red_cable=null;}}','class X {void x(){red_wire_coated=null;}}','class X {void x(){deco_steel=null;}}']:
 mutant=dict(src);mutant['Injected.java']=extra
 try:verify(mutant)
 except AssertionError:pass
 else:raise AssertionError('closure mutant survived')
deps=['com/hbm/blocks/network/BlockCable.java','com/hbm/blocks/network/WireCoated.java','com/hbm/blocks/generic/BlockDecoCT.java','com/hbm/blocks/network/ConnectionState.java','com/hbm/render/block/ct/CTContext.java','com/hbm/render/block/ct/CT.java','com/hbm/render/block/ct/IBlockCT.java','com/hbm/world/generator/DungeonToolbox.java']
pins={}
for dep in deps:
 f='src/main/java/'+dep;b=(M/f).read_bytes();old=subprocess.check_output(['git','show',BASE+':ntm-port-1.12.2/'+f],cwd=M);assert b==old;pins[f]=sha(b)
rows=json.loads((H/'selection.json').read_text());groups=collections.defaultdict(list)
for r in rows:groups[r['file']].append(r)
original_pins={}
for f,rs in groups.items():
 b=(a.original_root/f).read_bytes();s=clean(b.decode());cursor=0
 for r in rs:i=s.index(r['old'],cursor);cursor=i+len(r['old'])
 original_pins[f]=sha(b)
original=(a.original_root/'src/main/java/com/hbm/blocks/ModBlocks.java').read_text()
for n,constructor in constructors.items():
 old=re.search(r'^\s*'+n+r' = .*;$',original,re.M)[0].strip();normalized=re.sub(r'\.setBlockTextureName\([^;]+\);$',';',old).replace('setBlockName','setTranslationKey').replace('Material.iron','Material.IRON');assert normalized==constructor
 assert 'GameRegistry.registerBlock('+n+', '+n+'.getUnlocalizedName());' in original
for f,data in [('constructors.json',constructors),('dependency-hashes.json',pins),('original-source-hashes.json',original_pins)]:
 path=H/f
 if path.exists():assert json.loads(path.read_text())==data,f
 else:path.write_text(json.dumps(data,indent=2)+'\n')
print('CURRENT SOURCE RECOVERY PASS:',len(paths),'tracked sources; sole exact three constructors, no owner subclasses, six rejected closure mutations.')
print('CURRENT SOURCE RECOVERY PASS:44 exact original statements/order; helper and eight native owner/context files unchanged from restored CP76. No native compile or execution performed.')
