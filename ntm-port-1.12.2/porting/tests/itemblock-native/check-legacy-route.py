#!/usr/bin/env python3
"""Finite source reachability proof; not a substituted runtime/color implementation."""
import argparse,pathlib,re,json
p=argparse.ArgumentParser();p.add_argument('--module',required=True,type=pathlib.Path);a=p.parse_args();s=a.module/'src/main/java';h=(s/'com/hbm/main/LegacyBaseModelHandler.java').read_text();b=(s/'com/hbm/render/model/LegacyBaseBakedModel.java').read_text()
checks=[
 ('eligible block guard','if(!isNTM(name) || !(block instanceof BlockBase) || !isEligibleCube((BlockBase) block)) continue;' in h),
 ('item owner guard','if(!(placedBlock instanceof BlockBase) || !eligibleBlocks.containsKey(placedBlock.getRegistryName())) continue;' in h),
 ('only two registration calls',len(re.findall(r'(?<!void )registerItemModels\(',h))==2),
 ('root route owns concrete block','registerItemModels(item, placedBlock, count, true);' in h),
 ('only other route is EnumMulti','else if(item instanceof ItemEnumMulti)' in h and 'registerItemModels(item, null, ((ItemEnumMulti) item).getSubCount(), false);' in h),
 ('color handlers consume recorded map','for(ItemModelInfo info : itemModels.values())' in h),
 ('root old callback already skipped','itemColor.getDeclaringClass() != ItemBlockBase.class' in h),
 ('same block metadata fallback','if(block instanceof BlockBase) return getLegacyRenderColor((BlockBase) block, stack.getMetadata());' in h),
 ('block item icon independent of root','TextureAtlasSprite particle = resolve(block.getIcon(1, metadata));' in b),
 ('custom render type gate','hasCustomLegacyRenderType(block)) return false;' in h and '((Integer) renderType).intValue() != 0' in h),
]
for name in ['BlockNTMFlower','BlockTallPlant']:
 text=(s/('com/hbm/blocks/generic/'+name+'.java')).read_text();checks.append((name+' excluded renderer',bool(re.search(r'public int getRenderType\(\)\s*\{\s*return 1;',text))))
for name,base in [('BlockVinylVine','BlockVine'),('BlockRubberLeaves','BlockLeaves')]:checks.append((name+' not BlockBase',('extends '+base) in (s/('com/hbm/blocks/'+name+'.java')).read_text()))
# Audit transitive ItemBlockBase descendants, not only direct children.
parents={}
for f in (s/'com/hbm/items').rglob('*.java'):
 for m in re.finditer(r'public (?:abstract )?class (\w+) extends (\w+)',f.read_text()):parents[m[1]]=m[2]
desc=[]
for name in parents:
 current=name;seen=set()
 while current in parents and current not in seen:
  seen.add(current);current=parents[current]
  if current=='ItemBlockBase':desc.append(name);break
for name in desc:
 text=(s/('com/hbm/items/block/'+name+'.java')).read_text()
 checks.append((name+' no removed callback override',not re.search(r'\b(?:getIconFromDamage|getColorFromItemStack)\s*\(',text)))
for label,ok in checks:assert ok,label
print('PASS legacy route source reachability: %d checks; descendants=%s'%(len(checks),','.join(sorted(desc))))
print('No runtime/model proof claimed: this checks actual production registration guards and unchanged fallback expressions.')
