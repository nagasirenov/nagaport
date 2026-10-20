#!/usr/bin/env python3
import hashlib,json,pathlib
here=pathlib.Path(__file__).resolve().parent
root=here.parents[3]
m=json.loads((here/'manifest.json').read_text())
def sha(b): return hashlib.sha256(b).hexdigest()
assert len(m['callers'])==13
for entry in m['callers']:
    b=(root/entry['path']).read_bytes();s=b.decode()
    assert sha(b)==entry['after_sha256'],entry['path']
    assert s.count('NullableInventoryStackNbt.read(nbt1)')==1
    inverse=s.replace('\nimport com.hbm.util.NullableInventoryStackNbt;','').replace('NullableInventoryStackNbt.read(nbt1)','ItemStack.loadItemStackFromNBT(nbt1)')
    assert sha(inverse.encode())==entry['before_sha256'],entry['path']
    pristine=(root/entry['pristine_path']).read_bytes()
    assert sha(pristine)==entry['pristine_sha256']
    for text in ('slots = new ItemStack[getSizeInventory()]','b0 >= 0 && b0 < slots.length','slots[b0] = ItemStack.loadItemStackFromNBT(nbt1);','slots[i] != null','slots[i] = null'):
        assert text in inverse and text in pristine.decode(),(entry['path'],text)
print('PASS exact inverse to sealed CP70 bytes; 13 original nullable-array loading/consumer boundaries preserved')
