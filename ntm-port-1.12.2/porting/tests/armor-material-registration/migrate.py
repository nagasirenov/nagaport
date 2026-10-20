#!/usr/bin/env python3
"""Finite, byte-preserving native material migration; exact immutable-baseline inverse."""
import hashlib,json,re,subprocess
from pathlib import Path
HERE=Path(__file__).resolve().parent
ROOT=HERE.parents[2]
BASE='42dea8e6e3241d0c4ce4ba9c5459fab2bdfdc99b'
FILES=['src/main/java/com/hbm/main/MainRegistry.java','src/main/java/com/hbm/items/ModItemsArmor.java','src/main/java/com/hbm/items/ModItems.java']
CREATE=re.compile(rb'ArmorMaterial (aMat\w+) = EnumHelper\.addArmorMaterial\("([A-Z0-9_]+)", ([0-9]+), new int\[\] \{ ([0-9]+), ([0-9]+), ([0-9]+), ([0-9]+) \}, ([0-9]+)\);')
REPAIR=re.compile(rb'(aMat\w+)\.customCraftingMaterial = ModItems\.(\w+);')
def original(path):return subprocess.check_output(['git','-C',str(ROOT),'show',BASE+':ntm-port-1.12.2/'+path])
def sha(b):return hashlib.sha256(b).hexdigest()
def transform(path):
 old=original(path);changes=[];rows=[];repairs=[]
 for m in CREATE.finditer(old):
  v,n,d,h,c,l,f,e=[x.decode() for x in m.groups()]
  new=('ArmorMaterial %s = EnumHelper.addArmorMaterial("%s", "leather", %s, new int[] { %s, %s, %s, %s }, %s, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);'%(v,n,d,f,l,c,h,e)).encode()
  changes.append((m.start(),m.end(),m.group(),new))
  rows.append(dict(variable=v,name=n,durability=int(d),damage=[int(h),int(c),int(l),int(f)],enchantability=int(e),offset=m.start(),line=old[:m.start()].count(b'\n')+1,before=m.group().decode(),after=new.decode(),owner=path))
 for m in REPAIR.finditer(old):
  v,it=[x.decode() for x in m.groups()]
  new=('%s.repairMaterial = new ItemStack(ModItems.%s, 1, OreDictionary.WILDCARD_VALUE);'%(v,it)).encode()
  changes.append((m.start(),m.end(),m.group(),new))
  repairs.append(dict(variable=v,item=it,offset=m.start(),line=old[:m.start()].count(b'\n')+1,before=m.group().decode(),after=new.decode(),owner=path))
 # Add only absent imports after the package line, with the file's existing line ending.
 end=old.index(b'\n')+1;nl=b'\r\n' if old[:end].endswith(b'\r\n') else b'\n'
 imports=[x for x in ['net.minecraft.init.SoundEvents','net.minecraft.item.ItemStack','net.minecraftforge.oredict.OreDictionary'] if ('import '+x+';').encode() not in old]
 ins=nl.join(('import '+x+';').encode() for x in imports)+nl
 changes.append((end,end,b'',ins));changes.sort()
 out=old
 for s,e,b,a in reversed(changes):assert out[s:e]==b;out=out[:s]+a+out[e:]
 inverse=out;delta=0;translated=[]
 for s,e,b,a in changes:translated.append((s+delta,s+delta+len(a),b,a));delta+=len(a)-len(b)
 for s,e,b,a in reversed(translated):assert inverse[s:e]==a;inverse=inverse[:s]+b+inverse[e:]
 assert inverse==old
 return out,dict(path=path,before_sha256=sha(old),after_sha256=sha(out),inverse_sha256=sha(inverse),creations=rows,repairs=repairs,changes=[dict(start=s,end=e,before=b.decode(),after=a.decode()) for s,e,b,a in changes])
def restore_base(path,current):
 assert path in FILES,'Unowned file: '+path
 expected,_=transform(path)
 assert current==expected,'Unowned source drift: '+path
 return original(path)
def verify(write=False):
 records=[]
 for path in FILES:
  out,row=transform(path)
  if write:(ROOT/path).write_bytes(out)
  assert (ROOT/path).read_bytes()==out,'Unowned source drift: '+path
  records.append(row)
 assert [len(r['creations']) for r in records]==[16,14,1]
 assert [len(r['repairs']) for r in records]==[15,14,1]
 manifest=dict(baseline=BASE,files=records)
 (HERE/'source-manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
 print('PASS byte-exact inverse: 31 material creations, 30 repair assignments, three complete registration owners; all other source bytes unchanged.')
 return manifest
if __name__=='__main__':
 import sys
 verify('--write' in sys.argv)
