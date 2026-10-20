#!/usr/bin/env python3
import hashlib,json,re,subprocess
from pathlib import Path
H=Path(__file__).resolve().parent;M=H.parents[2]
def clean(s):return re.sub(r'/\*.*?\*/|//[^\n]*','',s,flags=re.S)
paths=subprocess.check_output(['git','ls-files','src/main/java'],cwd=M,text=True).splitlines();assert all((M/p).is_file()for p in paths)
sources={p:clean((M/p).read_text())for p in paths}
expected='fence_metal = new BlockMetalFence(Material.IRON).setTranslationKey("fence_metal").setCreativeTab(MainRegistry.machineTab).setHardness(15.0F).setResistance(0.25F);'
def verify(src):
 writes=[(p,line.strip())for p,s in src.items()for line in s.splitlines()if re.search(r'\bfence_metal\s*=(?!=)',line)]
 assert writes==[('src/main/java/com/hbm/blocks/ModBlocks.java',expected)],writes
 assert sum(len(re.findall(r'public static Block fence_metal;',s))for s in src.values())==1
 for p,s in src.items():assert not re.search(r'\bextends\s+(?:[\w.]+\.)?BlockMetalFence\b',s),p
 assert 'GameRegistry.registerBlock(fence_metal, ItemBlockBase.class, fence_metal.getTranslationKey());' in src['src/main/java/com/hbm/blocks/ModBlocks.java']
verify(sources)
for extra in ['class Injected {void bad(){ModBlocks.fence_metal=null;}}','class Injected extends BlockMetalFence {}','class Injected {void bad(){fence_metal = null;}}']:
 mutant=dict(sources);mutant['Injected.java']=extra
 try:verify(mutant)
 except AssertionError:pass
 else:raise AssertionError('closure mutant survived')
print('PASS all',len(paths),'tracked source files; sole actual mutable static owner, exact constructor and ItemBlockBase registration, no subclasses; three rejected closure mutations.')
# Reuse the corrected, unchanged structural owner/model proof without expanding visual acceptance.
scope=json.loads((H.parent/'structural-production-scope.json').read_text())
def read(p):
 f=M/p
 return f.read_bytes()if f.exists()else subprocess.check_output(['git','show','HEAD:ntm-port-1.12.2/'+p],cwd=M)
for category in ['files','unchanged_materials']:
 for p,digest in scope[category].items():assert hashlib.sha256(read(p)).hexdigest()==digest,p
print('PASS original corrected structural owner/model/resource manifest unchanged; prior 208475 structural assertions and eight replacement-owner assertions reused, not rerun. No new lighting or pixel acceptance.')
