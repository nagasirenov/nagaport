#!/usr/bin/env python3
"""Portable CP49 pinned mechanics and exact shared-fragment audit, no git objects required."""
from pathlib import Path
import argparse,hashlib,json,re,zipfile
p=argparse.ArgumentParser();p.add_argument("--integrated-shared",action="store_true",help="Verify exact owned fragments without rejecting unrelated later shared-file edits");a=p.parse_args()
H=Path(__file__).resolve().parent;R=H.parents[2];S=R/'src/main/java'
def block(s,start):
 i=s.index('{',start)+1;depth=1
 while depth:depth+=(s[i]=='{')-(s[i]=='}');i+=1
 return s[start:i]
count=0;nested=0
with zipfile.ZipFile(H/'cp49-owner-sources.zip') as z:
 for rel,sha in json.loads((H/'cp49-owner-manifest.json').read_text()).items():
  raw=z.read(rel);assert hashlib.sha256(raw).hexdigest()==sha
  before=raw.decode().replace('\r\n','\n');after=(S/rel).read_text();cls=Path(rel).stem
  for m in re.finditer(r'(?m)^\s*((?:public|protected|private)\s+(?:(?:static|final|synchronized)\s+)*(?:[\w<>\[\],.?]+\s+)?(\w+)\([^\n]*\)\s*\{)',before):
   name=m[2]
   if name in ['registerBlockIcons','getIcon','colorMultiplier','getRenderType'] or cls=='CraneRouter' and name=='shouldSideBeRendered' or cls=='BlockFissure' and name=='updateTick':continue
   source=block(before,m.start(1))
   if name==cls:
    source=source.replace('Material.iron','Material.IRON').replace('Material.rock','Material.ROCK')
    source=re.sub(r'^.*this.setBlockTextureName\([^\n]*\);\n','',source,flags=re.M)
   assert source in after,'Nonrender method changed: '+rel+' '+name
   count+=1
  for m in re.finditer(r'public static class \w+[^\n]*\{',before):
   assert block(before,m.start()) in after,'Nested tile mechanics changed: '+rel
   nested+=1
  assert 'RenderBlockMultipass.currentPass' not in after and 'IBlockMultiPass.getRenderType' not in after
  print('PASS OWNER',rel)
f=(S/'com/hbm/blocks/generic/BlockFissure.java').read_text()
assert '''boolean crater = world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos)) != 0;
        net.minecraft.util.math.BlockPos above = pos.up();
        if(world.getBlockState(above).getBlock().isReplaceable(world, above)) world.setBlockState(above, (crater ? ModBlocks.rad_lava_block : ModBlocks.volcanic_lava_block).getDefaultState());''' in f
edits=json.loads((H/'shared-edits.json').read_text());shared=json.loads((H/'cp49-shared-manifest.json').read_text())
with zipfile.ZipFile(H/'cp49-shared-sources.zip') as z:
 for rel,sha in shared.items():
  raw=z.read(rel);assert hashlib.sha256(raw).hexdigest()==sha;s=raw.decode()
  for e in edits:
   if e['path']==rel:
    assert s.count(e['before'])==1,(rel,e['before']);s=s.replace(e['before'],e['after'],1)
  current=(R/rel).read_bytes()
  if not a.integrated_shared:assert s.encode()==current,'Nonowned shared bytes changed: '+rel
  else:
   for e in edits:
    if e['path']==rel:
     assert e['before'].encode() not in current,'Old shared fragment remains: '+rel
     if e['after']:assert current.count(e['after'].encode())==1,'Owned shared fragment changed: '+rel
  print('PASS '+('EXACT OWNED SHARED FRAGMENTS' if a.integrated_shared else 'BYTE-EXACT SHARED'),rel)
with zipfile.ZipFile(H/'cp49-shared-sources.zip') as z: original_registry=z.read('src/main/java/com/hbm/blocks/ModBlocks.java').decode()
registered=json.loads((H/'registrations.json').read_text());registry=(S/'com/hbm/blocks/ModBlocks.java').read_text()
assert len(registered)==71
for r in registered:
 n=r['name'];matches=re.findall(r'(?m)^\s*register\('+re.escape(n)+r'(?:, (\w+)\.class)?\);',registry)
 assert len(matches)==1,'Missing/duplicate actual native registration '+n
 legacy=re.search(r'GameRegistry\.registerBlock\('+re.escape(n)+r', (?:([\w.]+)\.class, )?'+re.escape(n)+r'\.getUnlocalizedName\(\)\);',original_registry)
 if legacy:expected=legacy[1] or 'ItemBlock'
 else:
  old=re.findall(r'(?m)^\s*register\('+re.escape(n)+r'(?:, (\w+)\.class)?\);',original_registry);assert len(old)==1;expected=old[0] or 'ItemBlockBase'
 assert (matches[0] or 'ItemBlockBase')==expected,'Original item class altered '+n
assert 'registerBlockHandler(new RenderBlockMultipass())' not in (S/'com/hbm/main/ClientProxy.java').read_text()
assert 'currentPass' not in (S/'com/hbm/render/block/RenderBlockMultipass.java').read_text()
print('PASS',count,'nonrender methods/constructors and',nested,'complete nested tile bodies invariant; explicit Fissure native tick mapping;129 exact shared fragments;71 actual native registrations.')
