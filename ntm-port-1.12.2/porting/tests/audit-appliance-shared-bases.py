#!/usr/bin/env python3
"""Audit descendants and ensure the two shared base edits do not rewrite existing hooks."""
import pathlib,re,subprocess,json
r=pathlib.Path(__file__).resolve().parents[2];s=r/'src/main/java'
def old(path):return subprocess.check_output(['git','show','66a7b7b:ntm-port-1.12.2/src/main/java/'+path],cwd=r,text=True)
p='com/hbm/blocks/BlockMulti.java';text=(s/p).read_text()
addition='\t@Override\n\tpublic int damageDropped(net.minecraft.block.state.IBlockState state) {\n\t\treturn damageDropped(getMetaFromState(state));\n\t}\n\n'
assert text.replace(addition,'\t@Override\n')==old(p)
p='com/hbm/blocks/BlockEnumMulti.java';assert (s/p).read_text()==old(p).replace('this.getUnlocalizedName()','this.getTranslationKey()').replace('super.getUnlocalizedName()','super.getTranslationKey()')
classes={}
for p in s.rglob('*.java'):
 text=p.read_text();match=re.search(r'public\s+(?:abstract\s+)?class\s+(\w+)\s+extends\s+(\w+)',text)
 if match:classes[match[1]]=(match[2],p,text)
parents={'BlockMulti'};result=[]
while True:
 added={n for n,(parent,_,_) in classes.items() if parent in parents}-parents
 if not added:break
 parents|=added
for name in sorted(parents-{'BlockMulti'}):
 parent,p,text=classes[name];result.append({'class':name,'parent':parent,'integer_damage_hook':bool(re.search(r'damageDropped\(int ',text)),'state_damage_hook':bool(re.search(r'damageDropped\(IBlockState ',text))})
print('PASS exact shared-base delta; original integer damage formulas and enum naming/texture/variant logic retained')
print(json.dumps(result,indent=2))
print('AUDIT ONLY: descendants outside appliance scope are not claimed fully native or runtime verified. Native callback dispatch is tested with inherited/overridden hooks and actual appliance owners.')

original=r.parent/'ntm-original-pristine'
for path in ['models/blocks/toaster.obj','models/blocks/puter.obj','textures/blocks/toaster_iron.png','textures/blocks/toaster_steel.png','textures/blocks/toaster_wood.png','textures/blocks/deco_computer.png']:
 rel='src/main/resources/assets/hbm/'+path
 assert (r/rel).read_bytes()==(original/rel).read_bytes(),path
print('PASS two original mesh assets and four material PNGs byte-identical to pristine')
