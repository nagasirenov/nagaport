#!/usr/bin/env python3
import hashlib,json,pathlib,re,sys
here=pathlib.Path(__file__).resolve().parent;root=here.parents[3]
m=json.loads((here/'manifest.json').read_text())
sha=lambda b:hashlib.sha256(b).hexdigest()
for path,digest in json.loads((here/'reused-proof-inputs.json').read_text()).items():
    assert sha((root/path).read_bytes())==digest,'sealed previous helper proof changed: '+path
helper=root/'ntm-port-1.12.2/src/main/java/com/hbm/util/NullableInventoryStackNbt.java'
assert sha(helper.read_bytes())==m['helper_sha256'],'sealed production helper changed'
def loop(s,needle):
    pos=s.index(needle);start=s.rfind('for',0,pos);brace=s.index('{',start);depth=1;end=brace+1
    while depth:
        depth+=(s[end]=='{')-(s[end]=='}');end+=1
    assert start<pos<end
    return s[start:end]
def compact(s):return re.sub(r'\s+','',s)
representatives={}
for row in m['callers']:
    b=(root/row['path']).read_bytes();s=b.decode();arr=row['array'];nl='\r\n' if '\r\n' in s else '\n'
    assert sha(b)==row['after_sha256']
    inverse=s.replace(nl+'import com.hbm.util.NullableInventoryStackNbt;','').replace('NullableInventoryStackNbt.read(nbt1)','ItemStack.loadItemStackFromNBT(nbt1)')
    assert sha(inverse.encode())==row['before_sha256']
    old=(root/row['pristine_path']).read_bytes();assert sha(old)==row['pristine_sha256']
    owner=(root/row['owner_path']).read_bytes();assert sha(owner)==row['owner_sha256']
    original_owner=(root/row['original_owner_path']).read_bytes();assert sha(original_owner)==row['original_owner_sha256']
    assert re.search(r'ItemStack\s*(?:\[\]\s*'+arr+r'\b|'+arr+r'\s*\[\])',original_owner.decode())
    assert re.search(r'ItemStack\s*(?:\[\]\s*'+arr+r'\b|'+arr+r'\s*\[\])',owner.decode())
    assert re.search(arr+r'\[\w+\]\s*!= null',s) and re.search(arr+r'\[\w+\]\s*!= null',old.decode())
    assert arr+'[i].writeToNBT(nbt1)' in s and arr+'[i].writeToNBT(nbt1)' in old.decode()
    current_loop=loop(s,'NullableInventoryStackNbt.read(nbt1)')
    old_loop=loop(old.decode(),'ItemStack.loadItemStackFromNBT(nbt1)')
    assert compact(current_loop.replace('NullableInventoryStackNbt.read(nbt1)','ItemStack.loadItemStackFromNBT(nbt1)'))==compact(old_loop)
    assert ('slots = new ItemStack[getSizeInventory()]' in inverse)==row['reset_on_load'] if arr=='slots' else not row['reset_on_load']
    representatives.setdefault(arr,(row['path'],current_loop))
assert len(m['callers'])==17 and set(representatives)=={'slots','locks','processing'}
print('PASS 17 exact incremental source reversals, pristine load loops, nullable owners/writers; sealed native helper unchanged')
if len(sys.argv)>1:
    out=['import net.minecraft.item.ItemStack;','import net.minecraft.nbt.NBTTagList;','import net.minecraft.nbt.NBTTagCompound;','import com.hbm.util.NullableInventoryStackNbt;','public class ExtractedMachineLoops {']
    for arr,(path,body) in representatives.items():
        out+=['// Exact load-loop extraction: '+path,'public static void '+arr+'(ItemStack[] slots, NBTTagList list) {']
        if arr!='slots':out+=['ItemStack[] '+arr+' = slots;']
        out+=[body,'}']
    out+=['}'];pathlib.Path(sys.argv[1]).write_text('\n'.join(out)+'\n')
    print('PASS generated three representative exact production load loops: slots, processing, locks')
